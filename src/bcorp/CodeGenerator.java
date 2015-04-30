package bcorp;

import bcorp.syntax_tree.SyntaxTree;
import bcorp.tables.TableErrors;
import bcorp.tables.TableTokens;

import java.util.Vector;

public class CodeGenerator {
    public Vector<String> asmCode;
    private Vector<String> tableReservedWords;
    private Vector<Character> tableOneSymbolTokens;
    private Vector<String> tableIdentifiers;
    private TableTokens tableTokens;
    private TableErrors tableErrors;
    private SyntaxTree syntaxTree;

    public CodeGenerator(Vector<String> tableReservedWords,
                         Vector<Character> tableOneSymbolTokens,
                         Vector<String> tableIdentifiers,
                         TableTokens tableTokens,
                         TableErrors tableErrors,
                         SyntaxTree syntaxTree) {
        this.tableReservedWords = tableReservedWords;
        this.tableOneSymbolTokens = tableOneSymbolTokens;
        this.tableIdentifiers = asmCode;
        this.tableTokens = tableTokens;
        this.tableErrors = tableErrors;
        this.syntaxTree = syntaxTree;
    }

    public boolean generate() {


        return true;
    }
}
