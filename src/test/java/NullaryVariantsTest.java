import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class NullaryVariantsTest {
    @ParameterizedTest(name = "#nullary-variant-labels well-typed {0}")
    @ValueSource(strings = {
            "tests/variants/nullary/well-typed/nullary-1.stella",
            "tests/variants/nullary/well-typed/nullary-2.stella",
            "tests/variants/nullary/well-typed/nullary-3.stella",
            "tests/variants/nullary/well-typed/nullary-4.stella",
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

    @ParameterizedTest(name = "#nullary-variant-labels ill-typed {0}")
    @ValueSource(strings = {
            "tests/variants/nullary/ill-typed/missing_data_for_label.stella",
            "tests/variants/nullary/ill-typed/non_nullary_var_pattern.stella",
            "tests/variants/nullary/ill-typed/nullary_var_pattern.stella",
            "tests/variants/nullary/ill-typed/unexpecteed_data_nullary.stella",
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
