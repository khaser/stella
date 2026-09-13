import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class VariantsTest {
    @ParameterizedTest(name = "#variants well-typed {0}")
    @ValueSource(strings = {
            "tests/variants/well-typed/variants-1.stella",
            "tests/variants/well-typed/variants-2.stella",
            "tests/variants/well-typed/variants-3.stella",
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

    @ParameterizedTest(name = "#variants ill-typed {0}")
    @ValueSource(strings = {
            "tests/variants/ill-typed/ambiguous-variant-type-1.stella",
            "tests/variants/ill-typed/ambiguous-variant-type-2.stella",
            "tests/variants/ill-typed/ambiguous-variant-type-3.stella",
            "tests/variants/ill-typed/non-exaustive-match-patterns-1.stella",
            "tests/variants/ill-typed/undefined-variable-1.stella",
            "tests/variants/ill-typed/unexpected-pattern-match-1.stella",
            "tests/variants/ill-typed/unexpected-pattern-match-2.stella",
            "tests/variants/ill-typed/unexpected-type-of-expression-1.stella",
            "tests/variants/ill-typed/unexpected-type-of-expression-2.stella",
            "tests/variants/ill-typed/unexpected-type-of-expression-3.stella",
            "tests/variants/ill-typed/unexpected-type-of-expression-4.stella",
            "tests/variants/ill-typed/unexpected-type-of-expression-with-alternative-errors.stella",
            "tests/variants/ill-typed/unexpected-variable-label-1.stella",
            "tests/variants/ill-typed/unexpected-variable-label-2.stella",
            "tests/variants/ill-typed/unexpected-variant-1.stella",
            "tests/variants/ill-typed/unexpected-variant-2.stella",
            "tests/variants/ill-typed/unexpected-variant-3.stella",
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
