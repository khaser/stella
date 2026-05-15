import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class CoreEssentialTest {
    @ParameterizedTest(name = "Essential well-typed core {0}")
    @ValueSource(strings = {
            "tests/core/well-typed/simple-succ.stella",
            "tests/core/well-typed/good-if.stella",
            "tests/core/well-typed/logical-operators.stella",
            "tests/core/well-typed/higher-order-1.stella",
            "tests/core/well-typed/factorial.stella",
            "tests/core/well-typed/simple-types.stella",
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

    @ParameterizedTest(name = "Essential ill-typed core {0}")
    @ValueSource(strings = {
            "tests/core/ill-typed/unexpected-type-for-expression/bad-if-1.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/bad-succ-1.stella",
            "tests/core/ill-typed/missing_main/missing-main-1.stella",
    })
    void testIllTyped(String filepath) throws Exception {
        final FileInputStream fips = new FileInputStream(filepath);
        System.setIn(fips);
        assertThrows(Exception.class, () -> Main.main(new String[0]));
    }
}

