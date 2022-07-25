import java.io.*;
import java.util.ArrayList;

public class Lexer {
    public int line = 1;

    private final PushbackReader in;
    private char c;
    private int readIn;

    //Symbol Table
    private int identNum = 0;
    private ArrayList<Token> symtlb = new ArrayList<>();

    public Lexer(String inputFile) throws FileNotFoundException {
        //Read File
        this.in = new PushbackReader(new FileReader(inputFile));
    }

    private boolean isAlpha(char x){
        return (x <= 'z' && x >= 'a') || (x <= 'Z' && x >= 'A');
    }

    private boolean isNumber(char x){
        return (x <= '9' && x >= '0');
    }

    private boolean isAlNum(char x){
        return (x <= 'z' && x >= 'a') || (x <= 'Z' && x >= 'A') || (x <= '9' && x >= '0');
    }

    private char readChar() throws IOException {
        readIn = in.read();
        c = (char) readIn;
        return c;
    }

    private Token Ident() throws IOException {
        StringBuilder identBuilder = new StringBuilder();
        while (isAlNum(c) || c == '_'){
            identBuilder.append(c);
            c = readChar();
        }
        in.unread(c);
        String ident = identBuilder.toString();
        switch (ident) {
            case "main":
                return new Token(Tokens.MAINTK,ident);
            case "const":
                return new Token(Tokens.CONSTTK,ident);
            case "int":
                return new Token(Tokens.INTTK,ident);
            case "break":
                return new Token(Tokens.BREAKTK,ident);
            case "continue":
                return new Token(Tokens.CONTINUETK,ident);
            case "if":
                return new Token(Tokens.IFTK,ident);
            case "else":
                return new Token(Tokens.ELSETK,ident);
            case "while":
                return new Token(Tokens.WHILETK,ident);
            case "getint":
                return new Token(Tokens.GETINTTK,ident);
            case "printf":
                return new Token(Tokens.PRINTFTK,ident);
            case "return":
                return new Token(Tokens.RETURNTK,ident);
            case "void":
                return new Token(Tokens.VOIDTK,ident);
            default:
                identNum++;
                Token token = new Token(Tokens.IDENFR, ident);
                symtlb.add(token);
                return token;
        }


    }
    private Token FString() throws IOException {
        StringBuilder FStringBuilder = new StringBuilder();
        FStringBuilder.append('"');
        while (readChar() != '"'){
            FStringBuilder.append(c);
        }
        FStringBuilder.append('"');
        String FString = FStringBuilder.toString();
        return new Token(Tokens.STRCON, FString);
    }

    private Token Number() throws IOException {
        StringBuilder numBuilder = new StringBuilder(Character.toString(c));
        while (isNumber(readChar())){
            numBuilder.append(c);
        }
        in.unread(c);
        String num = numBuilder.toString();
        return new Token(Tokens.INTCON, num);
    }

    private void Comment() throws IOException {
        if(c == '/'){
            while (c != '\n'){
                readChar();
            }
        }
        else if(c == '*'){
            readChar();
            while (true){
                if(readIn == -1) return;
                if(c == '*'){
                    if(readChar() == '/') return;
                    else {
                        in.unread(c);
                    }
                }
                readChar();
            }
        }

    }

    public Token lex() throws IOException {
        boolean flag = false;
        while ((readIn = in.read()) != -1){
            c = (char)readIn;

            while (c == ' ' || c == '\t' || c == '\n'|| c == '\r'){
                if(c == '\n') line++;
                readIn = in.read();
                c = (char)readIn;
            }

            if(isAlpha(c) || c == '_'){
                return Ident();
            }
            if(isNumber(c)){
                return Number();
            }
            if(c == '"'){
                return FString();
            }

            switch (c){
                case '/':
                    readChar();
                    if(c != '*' && c != '/'){
                        in.unread(c);
                        return new Token(Tokens.DIV, "/");
                    }
                    else Comment();
                    break;
                case '!':
                    if(readChar() == '=') {
                        return new Token(Tokens.NEQ, "!=");
                    }
                    else {
                        in.unread(readIn);
                        return new Token(Tokens.NOT, "!");
                    }
                case '&':
                    if(readChar() == '&'){
                        return new Token(Tokens.AND, "&&");
                    }
                case '|':
                    if(readChar() == '|'){
                        return new Token(Tokens.OR, "||");
                    }
                case '+':
                    return new Token(Tokens.PLUS, "+");
                case '-':
                    return new Token(Tokens.MINU, "-");
                case '*':
                    return new Token(Tokens.MULT, "*");
                case '%':
                    return new Token(Tokens.MOD, "%");
                case '<':
                    if(readChar() == '='){
                        return new Token(Tokens.LEQ, "<=");
                    }
                    else{
                        in.unread(readIn);
                        return new Token(Tokens.LSS, "<");
                    }
                case '>':
                    if(readChar() == '='){
                        return new Token(Tokens.GEQ, ">=");
                    }
                    else{
                        in.unread(readIn);
                        return new Token(Tokens.GRE, ">");
                    }
                case '=':
                    if(readChar() == '='){
                        return new Token(Tokens.EQL, "==");
                    }
                    else {
                        in.unread(readIn);
                        return new Token(Tokens.ASSIGN, "=");
                    }
                case ';':
                    return new Token(Tokens.SEMICN, ";");
                case ',':
                    return new Token(Tokens.COMMA, ",");
                case '(':
                    return new Token(Tokens.LPARENT, "(");
                case ')':
                    return new Token(Tokens.RPARENT, ")");
                case '[':
                    return new Token(Tokens.LBRACK, "[");
                case ']':
                    return new Token(Tokens.RBRACK, "]");
                case '{':
                    return new Token(Tokens.LBRACE, "{");
                case '}':
                    return new Token(Tokens.RBRACE, "}");
                default:
                    return null;
            }
        }
        return null;
    }

}
