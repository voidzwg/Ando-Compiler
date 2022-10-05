import Backend.MCModule;
import Frontend.AST.CompUnitAST;
import Frontend.Parser;
import IR.IRModule;
import IR.Visitor;
import Utils.ErrDump;
import Utils.IRDump;
import Utils.MIPSDump;

import java.io.*;

public class Compiler {
    public static void main(String[] args) throws IOException {

        //  前端
        Parser parser = new Parser();
        CompUnitAST compUnitAST = parser.parseCompUnitAST();

//        ASTDump.DumpCompUnit(compUnitAST);

        //  中端
        Visitor visitor = new Visitor();
        IRModule module = visitor.VisitCompUnit(compUnitAST);

//        ErrDump.errDump();
        IRDump.DumpModule(module);

        //  后端
//        MCModule mcModule = new MCModule();
//        mcModule.genMips(module);
//
//        MIPSDump.DumpMCModule(mcModule);
    }
}
