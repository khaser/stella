import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class NestedFunctionDeclarationsTest {
    @ParameterizedTest(name = "#nested-function-declarations well-typed {0}")
    @ValueSource(strings = {
            "tests/core/well-typed/extra-tests/nested-function-declarations/test-1.stella",
            "tests/core/well-typed/extra-tests/nested-function-declarations/test-2.stella",
            "tests/core/well-typed/extra-tests/nested-function-declarations/test-3.stella",
    })
    void testWellTyped(String filepath) throws Exception {
        final InputStream original = System.in;
        try (FileInputStream fips = new FileInputStream(filepath)) {
            System.setIn(fips);
            assertDoesNotThrow(() -> Main.main(new String[0]));
        } finally {
            System.setIn(original);
        }
    }

    @ParameterizedTest(name = "#nested-function-declarations ill-typed {0}")
    @ValueSource(strings = {
            "tests/core/ill-typed/extra-tests/nested-function-declarations/test-1.stella",
            "tests/core/ill-typed/extra-tests/nested-function-declarations/test-2.stella",
            "tests/core/ill-typed/extra-tests/nested-function-declarations/test-3.stella",
    })
    void testIllTyped(String filepath) throws Exception {
        final InputStream original = System.in;
        try (FileInputStream fips = new FileInputStream(filepath)) {
            System.setIn(fips);
            assertThrows(Exception.class, () -> Main.main(new String[0]));
        } finally {
            System.setIn(original);
        }
    }
}
