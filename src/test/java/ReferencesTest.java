import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class ReferencesTest {
    @ParameterizedTest(name = "#references well-typed {0}")
    @ValueSource(strings = {
            "tests/references/well-typed/refs-1.stella",
            "tests/references/well-typed/refs-2.stella",
            "tests/references/well-typed/refs-3.stella",
            "tests/references/well-typed/refs-4.stella",
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

    @ParameterizedTest(name = "#references ill-typed {0}")
    @ValueSource(strings = {
            "tests/references/ill-typed/ambiguous-reference-type-1.stella",
            "tests/references/ill-typed/not-a-reference-1.stella",
            "tests/references/ill-typed/not-a-reference-2.stella",
            "tests/references/ill-typed/not-a-reference-3.stella",
            "tests/references/ill-typed/not-a-reference-4.stella",
            "tests/references/ill-typed/not-a-reference-5.stella",
            "tests/references/ill-typed/unexpected-memory-address-1.stella",
            "tests/references/ill-typed/unexpected-memory-address-2.stella",
            "tests/references/ill-typed/unexpected-memory-address-3.stella",
            "tests/references/ill-typed/unexpected-memory-address-4.stella",
            "tests/references/ill-typed/unexpected-type-for-expression-1.stella",
            "tests/references/ill-typed/unexpected-type-for-expression-2.stella",
            "tests/references/ill-typed/unexpected-type-for-expression-3.stella",
            "tests/references/ill-typed/unexpected-type-for-expression-4.stella",
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
