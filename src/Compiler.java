import AST.CompUnitAST;

import java.io.*;

public class Compiler {
    public static void main(String[] args) throws IOException {
        String inputFile = "testfile.txt";
        String outputFile = "output.txt";

        Parser parser = new Parser(inputFile, outputFile);
        CompUnitAST compUnitAST = parser.parseCompUnitAST();
    }
}
