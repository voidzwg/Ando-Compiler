package Utils;

import Frontend.Token;
import javafx.util.Pair;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

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



    public static void errDump() throws IOException {
        for (Pair<Integer, Character> pair : errors) {
            out.write(pair.getKey() + " " + pair.getValue() + "\n");
        }
        out.close();
    }
}
