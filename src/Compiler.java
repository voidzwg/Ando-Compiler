import Backend.MCModule;
import Frontend.AST.CompUnitAST;
import Frontend.Parser;
import IR.IRModule;
import IR.Visitor;
import Pass.PassManager;
import Utils.ASTDump;
import Utils.ErrDump;
import Utils.IRDump;
import Utils.MIPSDump;

import java.io.*;

public class Compiler {
    public static void main(String[] args) throws IOException {

        //  前端
        Parser parser = new Parser();
        CompUnitAST compUnitAST = parser.parseCompUnitAST();

//        //  前端输出
//        ASTDump.DumpCompUnit(compUnitAST);

        //  中端
        Visitor visitor = new Visitor();
        IRModule irModule = visitor.VisitCompUnit(compUnitAST);

        //  错误处理
//        ErrDump.errDump();

        //  中端优化
        PassManager passManager = PassManager.getInstance();
        passManager.runIRPasses(irModule);

        //  中端输出
        IRDump.DumpModule(irModule);

        //  后端
        MCModule mcModule = new MCModule();
        mcModule.genMips(irModule);

//        //  后端优化
//        passManager.runMCPasses(mcModule);

        //  后端输出
        MIPSDump.DumpMCModule(mcModule);
    }
}
