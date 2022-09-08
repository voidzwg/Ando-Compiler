import Frontend.AST.CompUnitAST;
import Frontend.Parser;
import IR.IRModule;
import IR.Visitor;

import java.io.*;

public class Compiler {
    public static void main(String[] args) throws IOException {
        String inputFile = "testfile.txt";
        String outputFile = "output.txt";

        Parser parser = new Parser(inputFile, outputFile);
        CompUnitAST compUnitAST = parser.parseCompUnitAST();

        Visitor visitor = new Visitor();
        IRModule module = visitor.VisitCompUnit(compUnitAST);
    }
}
