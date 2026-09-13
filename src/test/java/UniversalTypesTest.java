import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class UniversalTypesTest {
    @ParameterizedTest(name = "#universal-types well-typed {0}")
    @ValueSource(strings = {
            "tests/universal-types/well-typed/universal-types-1.stella",
            "tests/universal-types/well-typed/universal-types-2.stella",
            "tests/universal-types/well-typed/universal-types-3.stella",
            "tests/universal-types/well-typed/universal-types-4.stella",
            "tests/universal-types/well-typed/universal-types-5.stella",
            "tests/universal-types/well-typed/universal-types-6.stella",
            "tests/universal-types/well-typed/universal-types-7.stella",
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

    @ParameterizedTest(name = "#universal-types ill-typed {0}")
    @ValueSource(strings = {
            "tests/universal-types/ill-typed/bad-universal-types-1.stella",
            "tests/universal-types/ill-typed/bad-universal-types-2.stella",
            "tests/universal-types/ill-typed/bad-universal-types-3.stella",
            "tests/universal-types/ill-typed/bad-universal-types-4.stella",
            "tests/universal-types/ill-typed/bad-universal-types-5.stella",
            "tests/universal-types/ill-typed/bad-universal-types-6.stella",
            "tests/universal-types/ill-typed/bad-universal-types-7.stella",
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
