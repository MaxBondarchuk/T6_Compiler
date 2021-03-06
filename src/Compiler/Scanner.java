package Compiler;

import java.util.Vector;

import Compiler.tables.TableErrors;
import Compiler.tables.TableTokens;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Scanner {
    String file_name;
    public Vector<Character> tableDelimiters, tableOneSymbolTokens;
    public Vector<String> tableReservedWords;
    public TableTokens tableTokens;
    public Vector<String> tableIdentifiers;
    public TableErrors tableErrors;

    Scanner(String file_name) {
        this.file_name = file_name;
        tableTokens = new TableTokens();
        tableIdentifiers = new Vector<String>();
        tableErrors = new TableErrors();

        tableReservedWords = new Vector<String>();
        tableReservedWords.add("PROGRAM");
        tableReservedWords.add("PROCEDURE");
        tableReservedWords.add("BEGIN");
        tableReservedWords.add("END");
        tableReservedWords.add("SIGNAL");
        tableReservedWords.add("COMPLEX");
        tableReservedWords.add("INTEGER");
        tableReservedWords.add("FLOAT");
        tableReservedWords.add("BLOCKFLOAT");
        tableReservedWords.add("EXT");

        tableOneSymbolTokens = new Vector<Character>();
        tableOneSymbolTokens.add(';');
        tableOneSymbolTokens.add('.');
        tableOneSymbolTokens.add('(');
        tableOneSymbolTokens.add(')');
        tableOneSymbolTokens.add(':');
        tableOneSymbolTokens.add(',');
        tableOneSymbolTokens.add('+');
        tableOneSymbolTokens.add('=');

        tableDelimiters = new Vector<Character>();
        tableDelimiters.addAll(tableOneSymbolTokens);
        tableDelimiters.add(' ');
        tableDelimiters.add('\t');
        tableDelimiters.add('\r');
        tableDelimiters.add('\n');
    }

    // Checks symbol for delimiter
    private boolean is_delimiter(char symbol) {
        return tableDelimiters.contains(symbol);
    }

    // Checks symbol for one-symbol token
    private boolean is_one_symbol_token(char symbol) {
        return tableOneSymbolTokens.contains(symbol);
    }

    // Checks word for reserved word
    private boolean is_reserved_word(String word) {
        return tableReservedWords.contains(word);
    }

    private boolean isUpperCaseLetter(char symbol) {
        return ('A' <= symbol && symbol <= 'Z');
    }

    // Checks word for identifier
    private boolean is_identifier(String word) {
        if (!isUpperCaseLetter(word.charAt(0)))
            return false;
        for (int i = 1; i < word.length(); i++)
            if (!isUpperCaseLetter(word.charAt(i)) && !Character.isDigit(word.charAt(i)))
                return false;
        return true;

//        if (Character.isLetter((word.charAt(0))) && Character.isUpperCase(word.charAt(0))) {
//            for (int i = 1; i < word.length(); i++) {
//                if (!Character.isLetterOrDigit(word.charAt(i)))
//                    return false;
//                if (Character.isLetter(word.charAt(i)) && !Character.isUpperCase(word.charAt(i)))
//                    return false;
//            }
//        } else
//            return false;
//        return true;
    }

    // Adds word (token) to appropriate table
    private void add_token(String word, int file_row, int file_column) {
        byte token_type;
        int index_in_table = -1;

        if (is_reserved_word(word)) {   // To reserved words table
            token_type = 0;
            index_in_table = tableReservedWords.indexOf(word);
            file_column -= word.length() + 1;
        } else if (is_one_symbol_token(word.charAt(0))) {   // To one-symbol tokens table
            token_type = 1;
            index_in_table = tableOneSymbolTokens.indexOf(word.charAt(0));
            file_column--;
        } else if (is_identifier(word)) {   // To identifiers table
            token_type = 2;
            if (tableIdentifiers.contains(word))    // If this identifier already in table
                index_in_table = tableIdentifiers.indexOf(word);
            else {
                tableIdentifiers.add(word);
                index_in_table = tableIdentifiers.size() - 1;
            }
            file_column -= word.length() + 1;
        } else {    // Unresolved token (error)
            token_type = 3;
            file_column -= word.length() + 1;
            tableErrors.addError(file_row, file_column, String.format("Unresolved Token: %s", word));
        }
        tableTokens.add_token(token_type, index_in_table, file_row, file_column);
    }

    // Performs lexical analysis
    public boolean scan() throws IOException {
        RandomAccessFile file = new RandomAccessFile(new File(file_name), "r"); // File with SIGNAL code

        long file_length = file.length();
        long i = 2;     // One symbol weights 2 Bytes
        file.seek(i);
        boolean inside_comment = false;
        String word = "";
        int file_row = 1;
        int file_column = 1;
        int comment_start_row = -1;
        int comment_start_column = -1;

        while (i < file_length) {
            file_column++;
            char symbol = file.readChar();
            i += 2;

            if (!inside_comment) {  // If we are not inside comment
                if (is_delimiter(symbol)) { // If current symbol is delimiter
                    if (symbol == '(' && i < file_length) { // Check for comment begin
                        char next_symbol = file.readChar();
                        if (next_symbol == '*') {   // If really comment
                            comment_start_row = file_row;
                            comment_start_column = file_column - 1;
                            i += 2;
                            inside_comment = true;
                            if (!word.equals("")) {
                                add_token(word, file_row, file_column);
                                word = "";
                            }
                            file_column++;
                            continue;
                        } else  // Not a comment. Rollback
                            file.seek(i);
                    }

                    if (!word.equals("")) { // Two adjecent one-symbol tokens
                        add_token(word, file_row, file_column);
                        word = "";
                    }

                    if (is_one_symbol_token(symbol)) {  // If delimiter is also one-symbol token
                        word += symbol;
                        add_token(word, file_row, file_column);
                        word = "";
                        continue;
                    }

                    if (symbol == '\n') {   // If we going to new line
                        file_row++;
                        file_column = 1;
                        continue;
                    }

                    if (symbol == '\t')     // If we splitting 4 spaces
                        file_column += 3;
                } else  // Not a delimiter (add to current "long" token)
                    word += symbol;
            } else {    // We are inside comment
                if (symbol == '*' && i < file_length) { // Check for its end
                    char next_symbol = file.readChar();
                    file_column++;
                    if (next_symbol == ')') {   // If it really ends
                        inside_comment = false;
                        i += 2;
                    } else
                        file.seek(i);   // In the comment yet
                } else if (symbol == '\n') {    // If we going to new line
                    file_row++;
                    file_column = 1;
                }
            }
        }
        if (!word.equals(""))   // If we have something that we didn't add
            add_token(word, file_row, ++file_column);
        if (inside_comment)     // If comment isn't closed
            tableErrors.addError(comment_start_row, comment_start_column, "Comment not closed");

        file.close();

        return tableErrors.size() == 0;
    }
}