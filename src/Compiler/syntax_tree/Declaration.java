package Compiler.syntax_tree;

public class Declaration {
    public int identifier; // Index in identifiers table
    public IdentifiersList identifiersList;
    public int attribute;   // Index in reserved words table
    public AttributesList attributesList;
}
