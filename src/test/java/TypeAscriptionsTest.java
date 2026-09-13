import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class TypeAscriptionsTest {
    @ParameterizedTest(name = "#type-ascriptions well-typed {0}")
    @ValueSource(strings = {
            "tests/ascriptions/well-typed/good-anonymous-function.stella",
            "tests/ascriptions/well-typed/good-pair.stella",
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

    @ParameterizedTest(name = "#type-ascriptions ill-typed {0}")
    @ValueSource(strings = {
            "tests/ascriptions/ill-typed/bad-anonymous-function.stella",
            "tests/ascriptions/ill-typed/bad-pair.stella",
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
