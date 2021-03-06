package Compiler;

import Compiler.code_generator.AttributeFull;
import Compiler.syntax_tree.*;
import Compiler.tables.TableErrors;
import Compiler.tables.TableTokens;

import java.util.Vector;

public class CodeGenerator extends Parser {
    public Vector<String> asmCode;

    private Vector<String> parametersIdentifiers;
    private Vector<AttributeFull> parametersAttributes;

    private int parameterOffset = 8;
    private String programOrProcedureIdentifier;

    public CodeGenerator(Vector<String> tableReservedWords,
                         Vector<Character> tableOneSymbolTokens,
                         Vector<String> tableIdentifiers,
                         TableTokens tableTokens,
                         TableErrors tableErrors,
                         SyntaxTree syntaxTree) {
        super(tableReservedWords, tableOneSymbolTokens, tableIdentifiers, tableTokens, tableErrors);
        this.syntaxTree = syntaxTree;
        asmCode = new Vector<String>();
    }

    private void addError(int indexInTokensTable, String comment) {
        if (indexInTokensTable == -1)
            tableErrors.addError(-1, -1, comment);
        else
            tableErrors.addError(tableTokens.getTokenFileRow(indexInTokensTable),
                    tableTokens.getTokenFileColumn(indexInTokensTable), comment);
    }

    public void generate() {
        asmCode.add(".386\n");
        programOrProcedureIdentifier = getTokenByTokensTableIndex(syntaxTree.identifier);

        if (syntaxTree.programBranch) {
            if (!syntaxTree.block.expressionsList.empty) {
                addError(syntaxTree.identifier + 3, "Expressions in block are forbidden.");
                return;
            }

            asmCode.add(String.format("%-8s%s\n", "TITLE", programOrProcedureIdentifier));
            asmCode.add(".CODE\n");
            asmCode.add(String.format("%-8s", "_BEGIN:"));
            asmCode.add(String.format("%-8s%s", "END", "_BEGIN"));
            return;
        }

        asmCode.add(".CODE\n");
        asmCode.add(String.format("%-8s%s", programOrProcedureIdentifier, "PROC"));
        generateParametersList(syntaxTree.parametersList);
        asmCode.add(String.format("%-8s%s", programOrProcedureIdentifier, "ENDP"));
    }

    private void generateParametersList(ParametersList parametersList) {
        if (parametersList.empty || parametersList.declarationsList.empty)
            return;

        asmCode.add(String.format("\t\t%-8s", "; Save registers for getting parameters"));
        asmCode.add(String.format("\t\t%-8s%s", "PUSH", "EBP"));
        asmCode.add(String.format("\t\t%-8s%s", "MOV", "EBP, ESP"));
        asmCode.add(String.format("\t\t%-8s%s\n", "PUSH", "ESI"));

        asmCode.add(String.format("\t\t%-8s", "; Get parameters"));
        asmCode.add(String.format("\t\t%-8s", "; 8B offset in stack = 4B - address for return + 4B - EBP"));
        parametersIdentifiers = new Vector<String>();
        parametersAttributes = new Vector<AttributeFull>();
        generateDeclarationsList(parametersList.declarationsList);

        generateBlock(syntaxTree.block);

        asmCode.add(String.format("\n\t\t%-8s", "; Restore registers"));
        asmCode.add(String.format("\t\t%-8s%s", "POP", "ESI"));
        asmCode.add(String.format("\t\t%-8s%s", "POP", "EBP"));
        asmCode.add(String.format("\t\t%-8s", "RET"));
    }

    private void generateDeclarationsList(DeclarationsList declarationsList) {
        if (declarationsList.empty)
            return;

        generateDeclaration(declarationsList.declaration);
        generateDeclarationsList(declarationsList.declarationsList);
    }

    private void generateDeclaration(Declaration declaration) {
        AttributesList attributesAll = new AttributesList();
        attributesAll.attribute = declaration.attribute;
        attributesAll.attributesList = declaration.attributesList;

        AttributeFull attributeFull = getAttributesType(attributesAll);

        if (attributeFull.type.equals("WRONG")) {
            addError(declaration.attribute, "Attributes list doesn't match any correct combination\n");
        }

        IdentifiersList allIdentifiers = new IdentifiersList();
        allIdentifiers.identifier = declaration.identifier;
        allIdentifiers.identifiersList = declaration.identifiersList;
        generateIdentifiersList(allIdentifiers, attributeFull);
    }

    // Returns full attributes list
    private AttributeFull getAttributesType(AttributesList attributesList) {
        AttributeFull attributeFull = new AttributeFull();

        boolean wasChanges;
        do {
            wasChanges = false;

            if (attributesList.empty)
                return attributeFull;

            if (getTokenByTokensTableIndex(attributesList.attribute).equals("EXT")) {
                attributeFull.hasEXT = true;
                wasChanges = true;
                attributesList = attributesList.attributesList;
            }

            if (getTokenByTokensTableIndex(attributesList.attribute).equals("SIGNAL")) {
                attributeFull.hasSIGNAL = true;
                wasChanges = true;
                attributesList = attributesList.attributesList;
            }

            if (getTokenByTokensTableIndex(attributesList.attribute).equals("COMPLEX")) {
                attributeFull.hasCOMPLEX = true;
                wasChanges = true;
                attributesList = attributesList.attributesList;
            }
        } while (wasChanges);

        if (attributesList.empty)
            return attributeFull;

        String type = getTokenByTokensTableIndex(attributesList.attribute);
        if (type.equals("INTEGER") || type.equals("FLOAT") || type.equals("BLOCKFLOAT")) {
            attributeFull.type = type;
            attributesList = attributesList.attributesList;
        }

        if (!attributesList.empty)
            attributeFull.type = "WRONG";

        return attributeFull;
    }

    private void generateIdentifiersList(IdentifiersList identifiersList, AttributeFull attributeType) {
        if (identifiersList.empty)
            return;

        generateIdentifier(identifiersList.identifier, attributeType);
        generateIdentifiersList(identifiersList.identifiersList, attributeType);
    }

    private void generateIdentifier(int identifier, AttributeFull attributeType) {
        String _identifier = getTokenByTokensTableIndex(identifier);
        if (parametersIdentifiers.contains(_identifier)) {
            addError(identifier, String.format("Repeat of parameter: %s", _identifier));
            return;
        }


        if (_identifier.equals(programOrProcedureIdentifier)) {
            addError(identifier, String.format("Parameter has the same name as a procedure: %s", _identifier));
            return;
        }

        parametersIdentifiers.add(_identifier);
        parametersAttributes.add(attributeType);
        asmCode.add(String.format("\t\t_%-8s%-8s%s", _identifier, "EQU", String.format("[EBP + %d]", parameterOffset)));
        parameterOffset += parameterSize(attributeType);
    }

    // Returns parameter size by attributes' type (e.g. INTEGER - 4)
    private int parameterSize(AttributeFull parameterAttribute) {
        int size = 0;
        if (parameterAttribute.type.equals("INTEGER"))
            size = 4;
        else if (parameterAttribute.type.equals("FLOAT"))
            size = 4;
        else if (parameterAttribute.type.equals("BLOCKFLOAT"))
            size = 4;

        if (parameterAttribute.hasCOMPLEX)
            size *= 2;

        return size;
    }

    private AttributeFull getAttributeFullByTokensTableIndex(int index) {
        String identifier = getTokenByTokensTableIndex(index);
        int indexInParametersTable = parametersIdentifiers.indexOf(identifier);
        return parametersAttributes.get(indexInParametersTable);
    }

    private boolean isBasicType(AttributeFull attributeFull) {
        return !attributeFull.hasSIGNAL && !attributeFull.hasCOMPLEX && !attributeFull.hasEXT;
    }

    // Additional task
    private void generateBlock(Block block) {
        generateExpressionsList(block.expressionsList);
    }

    private void generateExpressionsList(ExpressionsList expressionsList) {
        if (expressionsList.empty)
            return;

        AttributeFull left = getAttributeFullByTokensTableIndex(expressionsList.expression.leftOperand);
        AttributeFull middle = getAttributeFullByTokensTableIndex(expressionsList.expression.middleOperand);
        AttributeFull right = getAttributeFullByTokensTableIndex(expressionsList.expression.rightOperand);
        String nameLeft = getTokenByTokensTableIndex(expressionsList.expression.leftOperand);
        String nameMiddle = getTokenByTokensTableIndex(expressionsList.expression.middleOperand);
        String nameRight = getTokenByTokensTableIndex(expressionsList.expression.rightOperand);

        if (!left.type.equals(middle.type) || !middle.type.equals(right.type)) {
            addError(expressionsList.expression.leftOperand, "All 3 operands must have the same type");
            return;
        }

        String errorSimpleType = "Operand must be without SIGNAL, EXT and COMPLEX";
        if (!isBasicType(left)) {
            addError(expressionsList.expression.leftOperand, errorSimpleType);
            return;
        }
        if (!isBasicType(middle)) {
            addError(expressionsList.expression.middleOperand, errorSimpleType);
            return;
        }
        if (!isBasicType(right)) {
            addError(expressionsList.expression.rightOperand, errorSimpleType);
            return;
        }

        asmCode.add(String.format("\n\t\t; %s = %s + %s", nameLeft, nameMiddle, nameRight));

        // MOV  EAX, DWORD PTR MIDDLE
        // ADD  EAX, DWORD PTR RIGHT
        // MOV  ESI, DWORD PTR LEFT
        // MOV  DWORD PTR [ESI], EAX
        if (left.type.equals("INTEGER")) {
            asmCode.add(String.format("\t\t%-8s%s, %s", "MOV", "EAX", String.format("DWORD PTR _%s", nameMiddle)));
            asmCode.add(String.format("\t\t%-8s%s, %s", "ADD", "EAX", String.format("DWORD PTR _%s", nameRight)));
            asmCode.add(String.format("\t\t%-8s%s, %s", "MOV", "ESI", String.format("DWORD PTR _%s", nameLeft)));
            asmCode.add(String.format("\t\t%-8s%s, %s", "MOV", "DWORD PTR [ESI]", "EAX"));
        }
    }
}
