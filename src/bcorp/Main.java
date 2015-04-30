package bcorp;

import bcorp.tables.TableErrors;
import bcorp.tables.TableTokens;

import java.io.IOException;
import java.util.Vector;

public class Main {

    public static void main(String[] args) throws IOException {
        String file_name = "src/bcorp/text";
        FileChecker fileChecker = new FileChecker(file_name);
        if (fileChecker.check_for_utf_16()) {
            Scanner scanner = new Scanner("src/bcorp/text");
            if (scanner.scan())
                System.out.println("No lexical errors\n");

            Vector<String> reserved_words_table = scanner.tableReservedWords;
            for (int i = 0; i < reserved_words_table.size(); i++)
                System.out.println(i + " " + reserved_words_table.get(i));
            System.out.println();

            Vector<Character> one_symbol_tokens_table = scanner.tableOneSymbolTokens;
            for (int i = 0; i < one_symbol_tokens_table.size(); i++)
                System.out.println(i + " " + one_symbol_tokens_table.get(i));
            System.out.println();

            Vector<String> identifiers_table = scanner.tableIdentifiers;
            for (int i = 0; i < identifiers_table.size(); i++)
                System.out.println(i + " " + identifiers_table.get(i));
            System.out.println();

            TableTokens _tableTokens = scanner.tableTokens;
            for (int i = 0; i < _tableTokens.size(); i++)
                System.out.println(_tableTokens.get_token_type(i) + " " + _tableTokens.get_token_index_in_table(i) + " " + _tableTokens.get_token_file_row(i) + " " + _tableTokens.get_token_file_column(i));
            System.out.println();

            TableErrors _tableErrors = scanner.tableErrors;

            Parser parser = new Parser(reserved_words_table, one_symbol_tokens_table, identifiers_table, _tableTokens, _tableErrors);

            if (parser.parse()) {
                System.out.println("No syntax errors\n");
                parser.tree.print();
                System.out.println();
            } else System.out.println(false);

            _tableErrors = parser.tableErrors;
            for (int i = 0; i < _tableErrors.size(); i++)
                System.out.println(_tableErrors.get_error_row(i) + " " + _tableErrors.get_error_column(i) + " " + _tableErrors.get_error_comment(i));
            System.out.println();
        } else
            System.out.println("Use UTF-16BE");
    }
}