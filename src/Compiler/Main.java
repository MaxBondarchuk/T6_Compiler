package Compiler;

import java.io.IOException;

import java.io.PrintWriter;

// I`m master
public class Main {

    public static void main(String[] args) throws IOException {
        FileChecker fileChecker = new FileChecker("src/Compiler/signal");
        PrintWriter asmFile = new PrintWriter("src/Compiler/asm");

        if (!fileChecker.check_for_utf_16())
            System.out.println("Use UTF-16BE");

        Scanner scanner = new Scanner("src/Compiler/signal");
        if (!scanner.scan()) {
            for (int i = 0; i < scanner.tableErrors.size(); i++)
                asmFile.format("%4s%4s %s", scanner.tableErrors.getErrorRow(i), scanner.tableErrors.getErrorColumn(i), scanner.tableErrors.getErrorComment(i));
            asmFile.close();
            return;
        }

        if (scanner.tableTokens.size() == 0)
            return; // File with signal code is empty

        for (int i = 0; i < scanner.tableReservedWords.size(); i++)
            System.out.format("%4d %s\n", i, scanner.tableReservedWords.get(i));
        System.out.println();

        for (int i = 0; i < scanner.tableOneSymbolTokens.size(); i++)
            System.out.format("%4d %s\n", i, scanner.tableOneSymbolTokens.get(i));
        System.out.println();

        for (int i = 0; i < scanner.tableIdentifiers.size(); i++)
            System.out.format("%4d %s\n", i, scanner.tableIdentifiers.get(i));
        System.out.println();

        for (int i = 0; i < scanner.tableTokens.size(); i++)
            System.out.format("%4d.%4s%4s%4s%4s\n", i, scanner.tableTokens.getTokenType(i), scanner.tableTokens.getTokenIndexInTable(i), scanner.tableTokens.getTokenFileRow(i), scanner.tableTokens.getTokenFileColumn(i));
        System.out.println();

        Parser parser = new Parser(scanner.tableReservedWords, scanner.tableOneSymbolTokens, scanner.tableIdentifiers, scanner.tableTokens, scanner.tableErrors);
        if (!parser.parse()) {
            for (int i = 0; i < scanner.tableErrors.size(); i++)
                asmFile.format("%4s%4s %s", scanner.tableErrors.getErrorRow(i), scanner.tableErrors.getErrorColumn(i), scanner.tableErrors.getErrorComment(i));
            asmFile.close();
            return;
        }

        parser.syntaxTree.print();
        System.out.println();

        CodeGenerator codeGenerator = new CodeGenerator(scanner.tableReservedWords, scanner.tableOneSymbolTokens, scanner.tableIdentifiers, scanner.tableTokens, parser.tableErrors, parser.syntaxTree);
        codeGenerator.generate();
        if (codeGenerator.tableErrors.size() != 0) {
            for (int i = 0; i < codeGenerator.tableErrors.size(); i++)
                asmFile.format("%4s%4s %s", codeGenerator.tableErrors.getErrorRow(i), codeGenerator.tableErrors.getErrorColumn(i), codeGenerator.tableErrors.getErrorComment(i));
            asmFile.close();
            return;
        }

        for (int i = 0; i < codeGenerator.asmCode.size(); i++)
            asmFile.println(codeGenerator.asmCode.get(i));
        asmFile.close();
    }
}