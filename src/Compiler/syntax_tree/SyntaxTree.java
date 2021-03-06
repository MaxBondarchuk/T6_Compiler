package Compiler.syntax_tree;

public class SyntaxTree {
    public boolean programBranch;
    public int identifier; // Index in identifiers table
    public ParametersList parametersList;
    public Block block;

    public void print() {
        System.out.println("Identifier: ");
        print_text_with_tabs(1, Integer.toString(identifier));
        if (!programBranch) {
            System.out.println("Parameters list: ");
            print_parameters_list(parametersList, 1);
        }
        System.out.println("Block: ");
        printBlock(block, 1);
    }

    private void print_text_with_tabs(int tabs_count, String text) {
        for (int i = 0; i < tabs_count; i++)
            System.out.print('\t');
        System.out.println(text);
    }

    private void print_parameters_list(ParametersList _parametersList, int tab_count) {
        if (!_parametersList.empty) {
            print_text_with_tabs(tab_count, "Declarations list: ");
            print_declarations_list(_parametersList.declarationsList, tab_count + 1);
        } else print_text_with_tabs(tab_count, "<>");
    }

    private void print_declarations_list(DeclarationsList _declarationsList, int tab_count) {
        if (!_declarationsList.empty) {
            print_text_with_tabs(tab_count, "Declaration: ");
            print_declaration(_declarationsList.declaration, tab_count + 1);
            print_text_with_tabs(tab_count, "Declarations list: ");
            print_declarations_list(_declarationsList.declarationsList, tab_count + 1);
        } else print_text_with_tabs(tab_count, "<>");
    }

    private void print_declaration(Declaration _declaration, int tab_count) {
        print_text_with_tabs(tab_count, "Identifier: ");
        print_text_with_tabs(tab_count + 1, Integer.toString(_declaration.identifier));
        print_text_with_tabs(tab_count, "Identifiers list: ");
        print_identifiers_list(_declaration.identifiersList, tab_count + 1);
        print_text_with_tabs(tab_count, "Attribute: ");
        print_text_with_tabs(tab_count + 1, Integer.toString(_declaration.attribute));
        print_text_with_tabs(tab_count, "Attributes list: ");
        print_attributes_list(_declaration.attributesList, tab_count + 1);
    }

    private void print_identifiers_list(IdentifiersList _identifiersList, int tab_count) {
        if (!_identifiersList.empty) {
            print_text_with_tabs(tab_count, "Identifier: ");
            print_text_with_tabs(tab_count + 1, Integer.toString(_identifiersList.identifier));
            print_text_with_tabs(tab_count, "Identifiers list: ");
            print_identifiers_list(_identifiersList.identifiersList, tab_count + 1);
        } else print_text_with_tabs(tab_count, "<>");
    }

    private void print_attributes_list(AttributesList _attributesList, int tab_count) {
        if (!_attributesList.empty) {
            print_text_with_tabs(tab_count, "Attribute: ");
            print_text_with_tabs(tab_count + 1, Integer.toString(_attributesList.attribute));
            print_text_with_tabs(tab_count, "Attributes list: ");
            print_attributes_list(_attributesList.attributesList, tab_count + 1);
        } else print_text_with_tabs(tab_count, "<>");
    }

    private void printBlock(Block block, int tab_count) {
        print_text_with_tabs(tab_count, "Expressions list: ");
        printExpressionsList(block.expressionsList, tab_count + 1);
    }

    private void printExpressionsList(ExpressionsList expressionsList, int tab_count) {
        if (!expressionsList.empty) {
            print_text_with_tabs(tab_count, "Expression: ");
            printExpression(expressionsList.expression, tab_count + 1);
            print_text_with_tabs(tab_count, "Expressions list: ");
            printExpressionsList(expressionsList.expressionsList, tab_count + 1);
        } else print_text_with_tabs(tab_count, "<>");
    }

    private void printExpression(Expression expression, int tab_count) {
        print_text_with_tabs(tab_count, "Left operand: ");
        print_text_with_tabs(tab_count + 1, Integer.toString(expression.leftOperand));
        print_text_with_tabs(tab_count, "Middle operand: ");
        print_text_with_tabs(tab_count + 1, Integer.toString(expression.middleOperand));
        print_text_with_tabs(tab_count, "Right operand: ");
        print_text_with_tabs(tab_count + 1, Integer.toString(expression.rightOperand));
    }
}
