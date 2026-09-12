import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

class TestSuiteV2Test {

    private static final Path BASE = Paths.get("test_suite_v2");

    static Stream<String> wellTypedFiles() throws IOException {
        Path dir = BASE.resolve("well-typed");
        return Files.walk(dir, 1)
                .filter(p -> p.toString().endsWith(".stella"))
                .map(Path::toString)
                .sorted();
    }

    static Stream<Arguments> illTypedFiles() throws IOException {
        Path dir = BASE.resolve("ill-typed");
        return Files.walk(dir)
                .filter(p -> p.toString().endsWith(".stella"))
                .map(p -> {
                    // Directory name (parent of the file) is the expected error in snake_case
                    String dirName = p.getParent().getFileName().toString();
                    String expectedError = "ERROR_" + dirName.toUpperCase();
                    return Arguments.of(p.toString(), expectedError);
                })
                .sorted(Comparator.comparing(a -> (String) a.get()[0]));
    }

    @ParameterizedTest(name = "v2 well-typed {0}")
    @MethodSource("wellTypedFiles")
    void testWellTyped(String filepath) throws Exception {
        InputStream original = System.in;
        try (FileInputStream fips = new FileInputStream(filepath)) {
            System.setIn(fips);
            assertDoesNotThrow(() -> Main.main(new String[0]));
        } finally {
            System.setIn(original);
        }
    }

    @ParameterizedTest(name = "v2 ill-typed {0}")
    @MethodSource("illTypedFiles")
    void testIllTyped(String filepath, String expectedError) throws Exception {
        InputStream original = System.in;
        try (FileInputStream fips = new FileInputStream(filepath)) {
            System.setIn(fips);
            Exception ex = assertThrows(Exception.class, () -> Main.main(new String[0]));
            assertTrue(
                ex.getMessage() != null && ex.getMessage().contains(expectedError),
                "Expected error containing '" + expectedError + "' but got: '" + ex.getMessage() + "'"
            );
        } finally {
            System.setIn(original);
        }
    }
}
