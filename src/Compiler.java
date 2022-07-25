import java.io.*;
import java.util.Scanner;

public class Compiler {
    public static void main(String[] args) throws IOException {
        String inputFile = "testfile.txt";
        String outputFile = "output.txt";

        Parser parser = new Parser(inputFile, outputFile);
        parser.parse();
    }
}
