import Frontend.Lexer;
import Frontend.Token;
import Utils.ASTDump;
import Utils.Global;
import Utils.MIPSDump;
import Frontend.AST.CompUnitAST;
import Frontend.Parser;
import IR.IRModule;
import IR.Visitor;
import Utils.IRDump;

import java.io.*;

public class Compiler {
    public static void main(String[] args) throws IOException {

        Parser parser = new Parser();
        CompUnitAST compUnitAST = parser.parseCompUnitAST();

//        ASTDump.DumpCompUnit(compUnitAST);

        Visitor visitor = new Visitor();
        IRModule module = visitor.VisitCompUnit(compUnitAST);

        IRDump.DumpModule(module);

//        MIPSDump.DumpMips(module);
    }
}
