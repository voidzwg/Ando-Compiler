package Utils;

import Frontend.AST.BlockAST;
import Frontend.AST.BlockItemAST;
import Frontend.AST.StmtAST;
import Frontend.Token;
import IR.Type.Type;
import IR.Value.*;
import IR.Value.Instructions.AllocInst;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public class ErrDump {
    private static final ArrayList<String> errors = new ArrayList<>();
    private static final BufferedWriter out;

    static {
        try {
            out = new BufferedWriter(new FileWriter(Global.errorFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void error_a(Token fStrTok){
        String fString = fStrTok.getVal();
        int line = fStrTok.getLine();
        int len = fString.length();
        boolean ok = true;
        for(int i = 0; i < len; i++){
            if(i == 0 || i == len - 1) continue;
            char c = fString.charAt(i);
            if(c == '%'){
                if(fString.charAt(i + 1) != 'd'){
                    ok = false;
                    break;
                }
            }
            else if(c == '\\'){
                if(fString.charAt(i + 1) != 'n'){
                    ok = false;
                    break;
                }
            }
            else if(c != 32 && c != 33 && (c < 40 || c > 126)){
                ok = false;
                break;
            }
        }
        if(!ok){
            errors.add(line + " a");
        }
    }

    public static void error_b(String ident, HashMap<String, Value> symTblNow, int line){
        if(symTblNow.containsKey(ident)){
            errors.add(line + " b");
        }
    }

    //  由于error_c的确在visitor里处理更方便，因此error_c就在visitor里处理了
    public static void error_c(int line){
        errors.add(line + " c");
    }

    public static void error_d(Function target, int funcRParamNum, int line){
        int funcFParamNum = target.getArgs().size();
        if(funcFParamNum != funcRParamNum){
            errors.add(line + " d");
        }
    }

    //  error_e显然只能在unaryExp.getType() == 3的时候会出现
    public static void error_e(Function target, ArrayList<Value> rParams, int line){
        ArrayList<Argument> args = target.getArgs();
        int len = Math.min(args.size(), rParams.size());
        for(int i = 0; i < len; i++){
            Type type_rP = rParams.get(i).getType();
            Type type_arg = args.get(i).getType();
            if(!type_rP.toString().equals(type_arg.toString())){
                errors.add(line + " e");
                return;
            }
        }
    }

    //  void有return Exp;
    public static void error_f(BlockAST blockAST){
        boolean ok = true;
        int line = blockAST.getLine();
        ArrayList<BlockItemAST> blockItems = blockAST.getBlockItems();
        int len = blockItems.size();
        if(len != 0) {
            BlockItemAST blockItemAST = blockItems.get(len - 1);

            if (blockItemAST.getType() == 2) {
                StmtAST stmtAST = blockItemAST.getStmtAST();
                if (stmtAST.getType() == 1) {
                    line = stmtAST.getLine();
                    ok = false;
                    blockAST.rmBlockItem(len - 1);
                }
                if (stmtAST.getType() != 12){
                    blockAST.addBlockItem(new BlockItemAST(new StmtAST(-1)));
                }
            }
        }

        if(!ok){
            errors.add(line + " f");
        }
    }

    //  非void无return;
    public static void error_g(BlockAST blockAST){
        boolean ok = true;
        //  BlockAST的line存储的就是'}'所在的行数
        int line = blockAST.getLine();
        ArrayList<BlockItemAST> blockItems = blockAST.getBlockItems();
        int len = blockItems.size();
        if(len == 0){
            ok = false;
        }
        else {
            BlockItemAST blockItemAST = blockItems.get(len - 1);

            if (blockItemAST.getType() == 2) {
                StmtAST stmtAST = blockItemAST.getStmtAST();
                if (stmtAST.getType() != 1) {
                    ok = false;
                }
            } else ok = false;
        }

        if(!ok){
            errors.add(line + " g");
        }
    }

    //  对常量赋值
    public static void error_h(Value value, int line){
        boolean ok = true;
        if(value instanceof GlobalVar){
            GlobalVar globalVar = (GlobalVar) value;
            if(globalVar.isConst()){
                ok = false;
            }
        }
        else if(value instanceof AllocInst){
            AllocInst allocInst = (AllocInst) value;
            if(allocInst.isConst()){
                ok = false;
            }
        }
        else if(value instanceof ConstInteger){
            ok = false;
        }
        if(!ok) {
            errors.add(line + " h");
        }
    }

    public static void error_i(int line){
        errors.add(line + " i");
    }

    public static void error_j(int line){
        errors.add(line + " j");
    }

    public static void error_k(int line){
        errors.add(line + " k");
    }

    //  printf中%d数不等于exp数
    public static void error_l(String fString, int num, int line){
        int len = fString.length();
        int fExpNum = 0;
        for(int i = 0; i < len; i++){
            if(i == 0 || i == len - 1) continue;
            if(fString.charAt(i) == '%'){
                if(fString.charAt(i + 1) == 'd'){
                    fExpNum++;
                }
            }
        }
        if(fExpNum != num){
            errors.add(line + " l");
        }
    }

    //  break/continue不在循环体
    public static void error_m(int line){
        errors.add(line + " m");
    }

    public static void errDump() throws IOException {
        errors.sort(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                String[] s1 = o1.split(" ");
                String[] s2 = o2.split(" ");
                int num1 = Integer.parseInt(s1[0]);
                int num2 = Integer.parseInt(s2[0]);
                return num1 - num2;
            }
        });

        for (String error : errors) {
            out.write(error + "\n");
        }
        out.close();
    }
}
