import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class NaturalLiteralsTest {
    @ParameterizedTest(name = "#natural-literals well-typed {0}")
    @ValueSource(strings = {
            "tests/core/well-typed/extra-tests/natural-literals/test-1.stella",
            "tests/core/well-typed/extra-tests/natural-literals/test-2.stella",
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

    @ParameterizedTest(name = "#natural-literals ill-typed {0}")
    @ValueSource(strings = {
            "tests/core/ill-typed/extra-tests/natural-literals/bad-factorial-1.stella",
            "tests/core/ill-typed/extra-tests/natural-literals/bad-factorial-2.stella",
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
