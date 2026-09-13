import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class LetBindingsTest {
    @ParameterizedTest(name = "#let-bindings well-typed {0}")
    @ValueSource(strings = {
            "tests/let-bindings/well-typed/let-1.stella",
            "tests/let-bindings/well-typed/let-2.stella",
            "tests/let-bindings/well-typed/let-3.stella",
            "tests/let-bindings/well-typed/let-4.stella",
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

    @ParameterizedTest(name = "#let-bindings ill-typed {0}")
    @ValueSource(strings = {
            "tests/let-bindings/ill-typed/bad-let-1.stella",
            "tests/let-bindings/ill-typed/bad-let-2.stella",
            "tests/let-bindings/ill-typed/bad-let-3.stella",
            "tests/let-bindings/ill-typed/bad-let-4.stella",
            "tests/let-bindings/ill-typed/bad-let-5.stella",
            "tests/let-bindings/ill-typed/bad-let-6.stella",
            "tests/let-bindings/ill-typed/bad-let-7.stella",
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
