import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.FileInputStream;
import java.io.InputStream;

final class Main {
    public static void main(String[] args) throws Exception {
        try {
            InputStream input;

            if (args.length > 0) {
                input = new FileInputStream(args[0]);
                System.out.println("Parsing file: " + args[0]);
            } else {
                input = System.in;
                System.out.println("Reading from stdin (Ctrl+D to end input):");
            }

            CharStream charStream = CharStreams.fromStream(input);
            stellaLexer lexer = new stellaLexer(charStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            stellaParser parser = new stellaParser(tokens);
            ParseTree tree = parser.start_Program();

            System.out.println("\n=== Parse Tree ===");
            System.out.println(tree.toStringTree(parser));

            if (parser.getNumberOfSyntaxErrors() > 0) {
                System.err.println("\nParsing completed with " + parser.getNumberOfSyntaxErrors() + " error(s)");
                throw new Exception("Parsing errors recognized");
            } else {
                System.out.println("\n=== Parsing successful! ===");
                System.out.println("\n=== Typechecking ===");
                try {
                    TypeChecker checker = new TypeChecker();
                    checker.visit(tree);
                    System.out.println("Typechecking successful!");
                } catch (Exception e) {
                    System.err.println("Type error: " + e.getMessage());
                    throw e;
                }
            }

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
}
