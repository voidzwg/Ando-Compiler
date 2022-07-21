import java.io.*;
import java.util.Scanner;

public class Compiler {
    public static void main(String[] args){
        String fileName = "testfile.txt";
        try {
            Scanner scanner = new Scanner(new FileReader(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
