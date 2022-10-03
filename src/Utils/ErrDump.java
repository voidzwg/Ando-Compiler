package Utils;

import Frontend.Token;
import IR.Value.Function;
import IR.Value.Value;
import javafx.util.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ErrDump {
    private static ArrayList<Pair<Integer, Character>> errors = new ArrayList<>();
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
            errors.add(new Pair<>(line, 'a'));
        }
    }

    public static void error_b(String ident, HashMap<String, Value> symTblNow, int line){
        if(symTblNow.containsKey(ident)){
            errors.add(new Pair<>(line, 'b'));
        }
    }

    //  由于error_c的确在visitor里处理更方便，因此error_c就在visitor里处理了
    public static void error_c(int line){
        errors.add(new Pair<>(line, 'c'));
    }

    public static void error_d(Function target, int funcRParamNum, int line){
        int funcFParamNum = target.getArgs().size();
        if(funcFParamNum != funcRParamNum){
            errors.add(new Pair<>(line, 'd'));
        }
    }

    public static void errDump() throws IOException {
        for (Pair<Integer, Character> pair : errors) {
            out.write(pair.getKey() + " " + pair.getValue() + "\n");
        }
        out.close();
    }
}
