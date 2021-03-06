package Compiler;

import java.util.Vector;


import Compiler.syntax_tree.*;
import Compiler.tables.TableErrors;
import Compiler.tables.TableTokens;


public class Parser {
    protected Vector<String> tableReservedWords;
    protected Vector<Character> tableOneSymbolTokens;
    protected Vector<String> tableIdentifiers;
    protected TableTokens tableTokens;
    public TableErrors tableErrors;

    int posInTokensTable;
    private boolean was_error;
    public SyntaxTree syntaxTree;
    private boolean useTableIndexes = true;

    public Parser(Vector<String> tableReservedWords,
                  Vector<Character> tableOneSymbolTokens,
                  Vector<String> tableIdentifiers,
                  TableTokens tableTokens,
                  TableErrors tableErrors) {
        this.tableReservedWords = tableReservedWords;
        this.tableOneSymbolTokens = tableOneSymbolTokens;
        this.tableIdentifiers = tableIdentifiers;
        this.tableTokens = tableTokens;
        this.tableErrors = tableErrors;
        posInTokensTable = -1;
        syntaxTree = new SyntaxTree();
        was_error = false;
    }

    // Adds error with <comment> to errors table
    private boolean addErrorWRAP(String comment) {
        if (!was_error) {
            if (posInTokensTable < tableTokens.Tokens.size())  // if unexpected Token
                tableErrors.addError(tableTokens.get(posInTokensTable).file_row,
                        tableTokens.get(posInTokensTable).file_column,
                        comment);
            else {  // if we don't have enough Tokens
                int len = 0;
                if (tableTokens.get(posInTokensTable - 1).type == 0)
                    len = tableReservedWords.get(tableTokens.get(posInTokensTable - 1).index_in_table).length();
                else if (tableTokens.get(posInTokensTable - 1).type == 1)
                    len = 1;
                else if (tableTokens.get(posInTokensTable - 1).type == 2)
                    len = tableIdentifiers.get(tableTokens.get(posInTokensTable - 1).index_in_table).length();
                tableErrors.addError(tableTokens.get(posInTokensTable - 1).file_row,
                        tableTokens.get(posInTokensTable - 1).file_column + len,
                        comment);
            }
        }
        was_error = true;
        return false;
    }

    protected String getToken(int type, int index) {
        if (type == 0)
            return tableReservedWords.get(index);
        if (type == 1)
            return tableOneSymbolTokens.get(index).toString();
        return tableIdentifiers.get(index);
    }

    protected String getTokenByTokensTableIndex(int index) {
        if (index < tableTokens.Tokens.size())
            return getToken(tableTokens.get(index).type, tableTokens.get(index).index_in_table);
        return "";
    }

    private String getTokenByPos() {
        return getTokenByTokensTableIndex(++posInTokensTable);
    }

    int getTokenIndexInTable() {
        if (posInTokensTable < tableTokens.Tokens.size())
            return tableTokens.get(posInTokensTable).index_in_table;
        return -1;
    }

    public boolean parse() {
        return tableErrors.errors_rows.isEmpty() && program();
    }

    boolean program() {
        String _token = getTokenByPos();
        if (_token.equals("PROGRAM")) {    // PROGRAM <identifier> ; <block>.
            syntaxTree.programBranch = true;
            if (identifier()) {
                if (useTableIndexes)
                    syntaxTree.identifier = posInTokensTable;
                else
                    syntaxTree.identifier = getTokenIndexInTable();
                if (getTokenByPos().equals(";")) {
                    syntaxTree.block = new Block();
                    if (block(syntaxTree.block))
                        if (getTokenByPos().equals("."))
                            return getTokenByPos().equals("") || addErrorWRAP("End of file expected");
                        else return addErrorWRAP("'.' expected");
                    else return false;
                } else return addErrorWRAP("';' expected");
            } else return addErrorWRAP("Identifier expected");
        } else if (_token.equals("PROCEDURE")) {   // PROCEDURE <identifier><parameters-list> ; <block> ;
            syntaxTree.programBranch = false;
            if (identifier()) {
                if (useTableIndexes)
                    syntaxTree.identifier = posInTokensTable;
                else
                    syntaxTree.identifier = getTokenIndexInTable();
                syntaxTree.parametersList = new ParametersList();
                if (parameters_list(syntaxTree.parametersList))
                    if (getTokenByPos().equals(";")) {
                        syntaxTree.block = new Block();
                        if (block(syntaxTree.block))
                            if (getTokenByPos().equals(";"))
                                return getTokenByPos().equals("") || addErrorWRAP("End of file expected");
                            else return addErrorWRAP("';' expected");
                        else return false;

//                                return (block(syntaxTree.block) && getTokenByPos().equals(";") || addErrorWRAP("';' expected"));
                    } else return addErrorWRAP("';' expected");
                else return false;
            } else return addErrorWRAP("Identifier expected");
        }
        return addErrorWRAP("'PROGRAM' or 'PROCEDURE' expected");
    }

    boolean block(Block block) {
        if (getTokenByPos().equals("BEGIN")) {
            block.expressionsList = new ExpressionsList();
            return expressionsList(block.expressionsList) && (getTokenByPos().equals("END") || addErrorWRAP("'END' expected"));
        } else return addErrorWRAP("'BEGIN' expected");
    }

    boolean expressionsList(ExpressionsList expressionsList) {
        if (identifier()) {
            posInTokensTable--;
            expressionsList.expression = new Expression();
            int save_pos_in_tokens_table = posInTokensTable;

            if (expression(expressionsList.expression)) {
                expressionsList.empty = false;
                expressionsList.expressionsList = new ExpressionsList();
                return expressionsList(expressionsList.expressionsList);
            } else posInTokensTable = save_pos_in_tokens_table;
        }

        // Empty
        expressionsList.empty = true;
        posInTokensTable--;
        return true;
    }

    boolean expression(Expression expression) {
        if (identifier()) {
            if (useTableIndexes)
                expression.leftOperand = posInTokensTable;
            else
                expression.leftOperand = getTokenIndexInTable();
//            expression.leftOperand = getTokenIndexInTable();
            if (getTokenByPos().equals("=")) {
                if (identifier()) {
                    if (useTableIndexes)
                        expression.middleOperand = posInTokensTable;
                    else
                        expression.middleOperand = getTokenIndexInTable();
//                    expression.middleOperand = getTokenIndexInTable();
                    if (getTokenByPos().equals("+")) {
                        if (identifier()) {
                            if (useTableIndexes)
                                expression.rightOperand = posInTokensTable;
                            else
                                expression.rightOperand = getTokenIndexInTable();
//                            expression.rightOperand = getTokenIndexInTable();
                            return getTokenByPos().equals(";") || addErrorWRAP("';' expected");
                        } else return addErrorWRAP("Identifier expected");
                    } else return addErrorWRAP("'+' expected");
                } else return addErrorWRAP("Identifier expected");
            } else return addErrorWRAP("'=' expected");
        } else return addErrorWRAP("Identifier expected");
    }

    boolean parameters_list(ParametersList _parametersList) {     // (<declarations-list>)|<empty>
        if (getTokenByPos().equals("(")) {  // ( <declarations-list> ) | <empty>
            _parametersList.declarationsList = new DeclarationsList();
            if (declarations_list(_parametersList.declarationsList)) {
                if (getTokenByPos().equals(")")) {
                    _parametersList.empty = false;
                    return true;
                } else return addErrorWRAP("')' expected");
            } else return false;
        }

        // Empty
        _parametersList.empty = true;
        posInTokensTable--;
        return true;
    }

    boolean declarations_list(DeclarationsList _declarationsList) {   // <Declaration><declarations-list>|<empty>
        if (identifier()) {
            posInTokensTable--;
            _declarationsList.declaration = new Declaration();
            int save_pos_in_tokens_table = posInTokensTable;
            if (declaration(_declarationsList.declaration)) {
                _declarationsList.empty = false;
                _declarationsList.declarationsList = new DeclarationsList();
                return declarations_list(_declarationsList.declarationsList);
            } else posInTokensTable = save_pos_in_tokens_table;
        }

        // Empty
        _declarationsList.empty = true;
        posInTokensTable--;
        return true;
    }

    boolean identifier() {
        posInTokensTable++;
        return posInTokensTable < tableTokens.Tokens.size()
                && tableTokens.get(posInTokensTable).type == 2;
    }

    boolean declaration(Declaration _declaration) {     // <identifier><IdentifiersList>:<attribute><AttributesList>;
        if (identifier()) {
            if (useTableIndexes)
                _declaration.identifier = posInTokensTable;
            else
                _declaration.identifier = getTokenIndexInTable();
            _declaration.identifiersList = new IdentifiersList();
            if (identifiers_list(_declaration.identifiersList)) {
                if (getTokenByPos().equals(":")) {
                    if (attribute()) {
                        if (useTableIndexes)
                            _declaration.attribute = posInTokensTable;
                        else
                            _declaration.attribute = getTokenIndexInTable();
                        _declaration.attributesList = new AttributesList();
                        return attributes_list(_declaration.attributesList) && (getTokenByPos().equals(";") || addErrorWRAP("';' expected"));
                    } else return addErrorWRAP("Attribute expected");
                } else return addErrorWRAP("':' expected");
            } else return false;
        } else return addErrorWRAP("Identifier expected");
    }

    boolean identifiers_list(IdentifiersList _identifiersList) {
        if (getTokenByPos().equals(",")) {
            if (identifier()) {
                if (useTableIndexes)
                    _identifiersList.identifier = posInTokensTable;
                else
                    _identifiersList.identifier = getTokenIndexInTable();
                _identifiersList.empty = false;
                _identifiersList.identifiersList = new IdentifiersList();
                return identifiers_list(_identifiersList.identifiersList);
            } else return addErrorWRAP("Identifier expected");
        }

        // Empty
        _identifiersList.empty = true;
        posInTokensTable--;
        return true;
    }

    boolean attributes_list(AttributesList _attributesList) {
        if (attribute()) {
            if (useTableIndexes)
                _attributesList.attribute = posInTokensTable;
            else
                _attributesList.attribute = getTokenIndexInTable();
            _attributesList.empty = false;
            _attributesList.attributesList = new AttributesList();
            return attributes_list(_attributesList.attributesList);
        }

        // Empty
        _attributesList.empty = true;
        posInTokensTable--;
        return true;
    }

    boolean attribute() {
        String _token = getTokenByPos();
        return _token.equals("SIGNAL") || _token.equals("COMPLEX") || _token.equals("INTEGER")
                || _token.equals("FLOAT") || _token.equals("BLOCKFLOAT") || _token.equals("EXT");
    }
}
