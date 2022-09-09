import Frontend.AST.CompUnitAST;
import Frontend.Parser;
import IR.IRDump;
import IR.IRModule;
import IR.Visitor;

import java.io.*;

public class Compiler {
    public static void main(String[] args) throws IOException {

        Parser parser = new Parser();
        CompUnitAST compUnitAST = parser.parseCompUnitAST();

        Visitor visitor = new Visitor();
        IRModule module = visitor.VisitCompUnit(compUnitAST);

        IRDump.DumpModule(module);
    }
}
