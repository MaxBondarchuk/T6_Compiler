package Compiler;

import Compiler.syntax_tree.*;
import Compiler.tables.TableErrors;
import Compiler.tables.TableTokens;

import java.util.Vector;

public class CodeGenerator extends Parser {
    public Vector<String> asmCode;

    private Vector<String> parametersIdentifiers;
    private Vector<Integer> parametersAttributes;

//    private int sizeSIGNAL = 4;     // As a link
//    private int sizeCOMPLEX = 8;    // For two integers
//    private int sizeINTEGER = 4;
//    private int sizeFLOAT = 4;
//    private int sizeBLOCKFLOAT = 8;
//    private int sizeEXT = 8;
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
            asmCode.add(String.format("%-8s", "?BEGIN:"));
            asmCode.add(String.format("%-8s%s", "END", "?BEGIN"));
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
        parametersIdentifiers = new Vector<String>();
        parametersAttributes = new Vector<Integer>();
        generateDeclarationsList(parametersList.declarationsList);

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
//        generateIdentifier(declaration.identifier, declaration.attribute);

        int attributesType = getAttributesType(declaration.attribute, declaration.attributesList);

        if (attributesType == -1) {
            addError(declaration.attribute, "Attributes list doesn't match any correct combination\n");
        }

        IdentifiersList allIdentifiers = new IdentifiersList();
        allIdentifiers.identifier = declaration.identifier;
        allIdentifiers.identifiersList = declaration.identifiersList;
        generateIdentifiersList(allIdentifiers, attributesType);
    }

    // Returns type of attributes list
    // 0 - [EXT] INTEGER
    // 1 - [EXT] FLOAT
    // 2 - [EXT] BLOCKFLOAT
    // 3 - [EXT] COMPLEX INTEGER
    // 4 - [EXT] COMPLEX FLOAT
    // 5 - [EXT] COMPLEX BLOCKFLOAT
    // 6 - [EXT] SIGNAL INTEGER
    // 7 - [EXT] SIGNAL FLOAT
    // 8 - [EXT] SIGNAL BLOCKFLOAT
    private int getAttributesType(int attribute, AttributesList attributesList) {
        AttributesList attributesListWithoutEXT = new AttributesList();
        attributesListWithoutEXT.attribute = attribute;
        attributesListWithoutEXT.attributesList = attributesList;
        if (getTokenByTokensTableIndex(attributesListWithoutEXT.attribute).equals("EXT"))
            attributesListWithoutEXT = attributesListWithoutEXT.attributesList;

        if (attributesListWithoutEXT.empty) {
            addError(attributesList.attribute, "Missing one of basic types for EXT");
            return -1;
        }

        if (getTokenByTokensTableIndex(attributesListWithoutEXT.attribute).equals("INTEGER")
                && attributesListWithoutEXT.attributesList.empty)
            return 0;

        if (getTokenByTokensTableIndex(attributesListWithoutEXT.attribute).equals("FLOAT")
                && attributesListWithoutEXT.attributesList.empty)
            return 1;

        if (getTokenByTokensTableIndex(attributesListWithoutEXT.attribute).equals("BLOCKFLOAT")
                && attributesListWithoutEXT.attributesList.empty)
            return 2;

        if (getTokenByTokensTableIndex(attributesListWithoutEXT.attribute).equals("COMPLEX")
                && !attributesListWithoutEXT.attributesList.empty
                && getTokenByTokensTableIndex(attributesListWithoutEXT.attributesList.attribute).equals("INTEGER")
                && attributesListWithoutEXT.attributesList.attributesList.empty)
            return 3;

        if (getTokenByTokensTableIndex(attributesListWithoutEXT.attribute).equals("COMPLEX")
                && !attributesListWithoutEXT.attributesList.empty
                && getTokenByTokensTableIndex(attributesListWithoutEXT.attributesList.attribute).equals("FLOAT")
                && attributesListWithoutEXT.attributesList.attributesList.empty)
            return 4;

        if (getTokenByTokensTableIndex(attributesListWithoutEXT.attribute).equals("COMPLEX")
                && !attributesListWithoutEXT.attributesList.empty
                && getTokenByTokensTableIndex(attributesListWithoutEXT.attributesList.attribute).equals("BLOCKFLOAT")
                && attributesListWithoutEXT.attributesList.attributesList.empty)
            return 5;

        if (getTokenByTokensTableIndex(attributesListWithoutEXT.attribute).equals("SIGNAL")
                && !attributesListWithoutEXT.attributesList.empty
                && getTokenByTokensTableIndex(attributesListWithoutEXT.attributesList.attribute).equals("INTEGER")
                && attributesListWithoutEXT.attributesList.attributesList.empty)
            return 6;

        if (getTokenByTokensTableIndex(attributesListWithoutEXT.attribute).equals("SIGNAL")
                && !attributesListWithoutEXT.attributesList.empty
                && getTokenByTokensTableIndex(attributesListWithoutEXT.attributesList.attribute).equals("FLOAT")
                && attributesListWithoutEXT.attributesList.attributesList.empty)
            return 7;

        if (getTokenByTokensTableIndex(attributesListWithoutEXT.attribute).equals("SIGNAL")
                && !attributesListWithoutEXT.attributesList.empty
                && getTokenByTokensTableIndex(attributesListWithoutEXT.attributesList.attribute).equals("BLOCKFLOAT")
                && attributesListWithoutEXT.attributesList.attributesList.empty)
            return 8;

//        addError(attributesList.attribute, "Attributes list doesn't match any correct combination");
        return -1;
    }

//    // Returns list of attributes. E.g. "EXT COMPLEX INTEGER"
//    private Vector<String> getAttributes(AttributesList attributesList) {
//        Vector<String> v = new Vector<String>();
//        if (attributesList.empty)
//            return v;
//
//        v.add(getTokenByTokensTableIndex(attributesList.attribute));
//        v.addAll(getAttributes(attributesList.attributesList));
//        return v;
//    }
//
//    // Returns type of attributes list
//    // 0 - [EXT] INTEGER
//    // 1 - [EXT] FLOAT
//    // 2 - [EXT] BLOCKFLOAT
//    // 3 - [EXT] COMPLEX INTEGER
//    // 4 - [EXT] COMPLEX FLOAT
//    // 5 - [EXT] COMPLEX BLOCKFLOAT
//    // 6 - [EXT] SIGNAL
//    private int getAttributesNumber(Vector<String> attributes) {
//        if (attributes.get(0).equals("EXT"))
//            attributes.remove(0);
//
//        if (attributes.get(0).equals("INTEGER"))
//            return 0;
//        if (attributes.get(0).equals("FLOAT"))
//            return 1;
//        if (attributes.get(0).equals("BLOCKFLOAT"))
//            return 1;
//
//        if (attributes.get(0).equals("COMPLEX")) {
//            if (attributes.size() != 2) {
//                addError(identifier, String.format("Repeat of parameter: %s", _identifier));
//                return -1;
//            }
//        }
//
//        return -1;
//    }

    private void generateIdentifiersList(IdentifiersList identifiersList, int attributeType) {
        if (identifiersList.empty)
            return;

        generateIdentifier(identifiersList.identifier, attributeType);
        generateIdentifiersList(identifiersList.identifiersList, attributeType);
    }

    private void generateIdentifier(int identifier, int attributeType) {
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
        asmCode.add(String.format("\t\t%-8s%-8s%s", _identifier, "EQU", String.format("[EBP + %d]", parameterOffset)));
        parameterOffset += parameterSize(attributeType);
    }

    // Returns parameter size by attributes' type (e.g. INTEGER - 4)
    private int parameterSize(int parameterAttribute) {
        // 0 - [EXT] INTEGER
        // 1 - [EXT] FLOAT
        // 2 - [EXT] BLOCKFLOAT
        // 3 - [EXT] COMPLEX INTEGER
        // 4 - [EXT] COMPLEX FLOAT
        // 5 - [EXT] COMPLEX BLOCKFLOAT
        // 6 - [EXT] SIGNAL INTEGER
        // 7 - [EXT] SIGNAL FLOAT
        // 8 - [EXT] SIGNAL BLOCKFLOAT

        switch (parameterAttribute) {
            case 0: return 4;
            case 1: return 4;
            case 2: return 4;
            case 3: return 4*2;
            case 4: return 4*2;
            case 5: return 4*2;
            case 6: return 4;
            case 7: return 4;
//            case 8: return 4;
            default: return 4;
        }
    }
}
