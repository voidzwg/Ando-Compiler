package Frontend;

import Utils.Global;

import java.io.*;

public class Lexer {
    public int line = 1;

    private final PushbackReader in;
    private char c;
    private int readIn;


    public Lexer() throws FileNotFoundException {
        //Read File
        this.in = new PushbackReader(new FileReader(Global.inputFile));
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
                return new Token(Tokens.MAINTK, ident, line);
            case "const":
                return new Token(Tokens.CONSTTK, ident, line);
            case "int":
                return new Token(Tokens.INTTK, ident, line);
            case "break":
                return new Token(Tokens.BREAKTK, ident, line);
            case "continue":
                return new Token(Tokens.CONTINUETK, ident, line);
            case "if":
                return new Token(Tokens.IFTK, ident, line);
            case "else":
                return new Token(Tokens.ELSETK, ident, line);
            case "while":
                return new Token(Tokens.WHILETK, ident, line);
            case "getint":
                return new Token(Tokens.GETINTTK, ident, line);
            case "printf":
                return new Token(Tokens.PRINTFTK, ident, line);
            case "return":
                return new Token(Tokens.RETURNTK, ident, line);
            case "void":
                return new Token(Tokens.VOIDTK, ident, line);
            default:
                return new Token(Tokens.IDENFR, ident, line);
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
        return new Token(Tokens.STRCON, FString, line);
    }

    private Token Number() throws IOException {
        StringBuilder numBuilder = new StringBuilder(Character.toString(c));
        while (isNumber(readChar())){
            numBuilder.append(c);
        }
        in.unread(c);
        String num = numBuilder.toString();
        return new Token(Tokens.INTCON, num, line);
    }

    private void Comment() throws IOException {
        if(c == '/'){
            while (c != '\n'){
                readChar();
            }
            line++;
        }
        else if(c == '*'){
            readChar();
            while (true){
                if(readIn == -1) return;
                if(c == '\n') line++;
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

    public Token getTok() throws IOException {
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
                        return new Token(Tokens.DIV, "/", line);
                    }
                    else Comment();
                    break;
                case '!':
                    if(readChar() == '=') {
                        return new Token(Tokens.NEQ, "!=", line);
                    }
                    else {
                        in.unread(readIn);
                        return new Token(Tokens.NOT, "!", line);
                    }
                case '&':
                    if(readChar() == '&'){
                        return new Token(Tokens.AND, "&&", line);
                    }
                case '|':
                    if(readChar() == '|'){
                        return new Token(Tokens.OR, "||", line);
                    }
                case '+':
                    return new Token(Tokens.PLUS, "+", line);
                case '-':
                    return new Token(Tokens.MINU, "-", line);
                case '*':
                    return new Token(Tokens.MULT, "*", line);
                case '%':
                    return new Token(Tokens.MOD, "%", line);
                case '<':
                    if(readChar() == '='){
                        return new Token(Tokens.LEQ, "<=", line);
                    }
                    else{
                        in.unread(readIn);
                        return new Token(Tokens.LSS, "<", line);
                    }
                case '>':
                    if(readChar() == '='){
                        return new Token(Tokens.GEQ, ">=", line);
                    }
                    else{
                        in.unread(readIn);
                        return new Token(Tokens.GRE, ">", line);
                    }
                case '=':
                    if(readChar() == '='){
                        return new Token(Tokens.EQL, "==", line);
                    }
                    else {
                        in.unread(readIn);
                        return new Token(Tokens.ASSIGN, "=", line);
                    }
                case ';':
                    return new Token(Tokens.SEMICN, ";", line);
                case ',':
                    return new Token(Tokens.COMMA, ",", line);
                case '(':
                    return new Token(Tokens.LPARENT, "(", line);
                case ')':
                    return new Token(Tokens.RPARENT, ")", line);
                case '[':
                    return new Token(Tokens.LBRACK, "[", line);
                case ']':
                    return new Token(Tokens.RBRACK, "]", line);
                case '{':
                    return new Token(Tokens.LBRACE, "{", line);
                case '}':
                    return new Token(Tokens.RBRACE, "}", line);
                default:
                    return null;
            }
        }
        return null;
    }

}
