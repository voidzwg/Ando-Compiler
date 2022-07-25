import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLOutput;

public class Parser {
    private final String inputFile;
    private final BufferedWriter out;

    public Parser(String inputFile, String outputFile) throws IOException {
        this.inputFile = inputFile;
        this.out = new BufferedWriter(new FileWriter(outputFile));
    }

    public void parse() throws IOException {
        Lexer lexer = new Lexer(inputFile);
        Token token;
        while ((token = lexer.lex()) != null){
            String tokenType = token.getType().toString();
            String tokenVal = token.getVal();
            out.write(tokenType + " " + tokenVal + '\n');

        }
        out.close();
    }

}
