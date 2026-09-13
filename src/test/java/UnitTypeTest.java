import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class UnitTypeTest {
    @ParameterizedTest(name = "#unit-type well-typed {0}")
    @ValueSource(strings = {
            "tests/units/well-typed/ignore.stella",
            "tests/units/well-typed/test-1.stella",
            "tests/units/well-typed/test-2.stella",
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

    @ParameterizedTest(name = "#unit-type ill-typed {0}")
    @ValueSource(strings = {
            "tests/units/ill-typed/bad_ignore.stella",
            "tests/units/ill-typed/test-1.stella",
            "tests/units/ill-typed/test-2.stella",
            "tests/units/ill-typed/test-3.stella",
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
