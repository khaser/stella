import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class TuplesTest {
    @ParameterizedTest(name = "#pairs/#tuples well-typed {0}")
    @ValueSource(strings = {
            "tests/pairs/well-typed/pairs-1.stella",
            "tests/pairs/well-typed/pairs-2.stella",
            "tests/pairs/well-typed/pairs-3.stella",
            "tests/pairs/well-typed/pairs-4.stella",
            "tests/pairs/well-typed/pairs-5.stella",
            "tests/pairs/well-typed/pairs-6.stella",
            "tests/tuples/well-typed/tuples-1.stella",
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

    @ParameterizedTest(name = "#pairs/#tuples ill-typed {0}")
    @ValueSource(strings = {
            "tests/pairs/ill-typed/bad-pairs-1.stella",
            "tests/pairs/ill-typed/bad-pairs-2.stella",
            "tests/pairs/ill-typed/bad-pairs-3.stella",
            "tests/pairs/ill-typed/bad-pairs-4.stella",
            "tests/pairs/ill-typed/bad-pairs-5.stella",
            "tests/pairs/ill-typed/bad-pairs-6.stella",
            "tests/pairs/ill-typed/bad-pairs-7.stella",
            "tests/pairs/ill-typed/bad-pairs-8.stella",
            "tests/pairs/ill-typed/bad-pairs-9.stella",
            "tests/pairs/ill-typed/bad-pairs-10.stella",
            "tests/pairs/ill-typed/bad-pairs-11.stella",
            "tests/pairs/ill-typed/bad-pairs-12.stella",
            "tests/pairs/ill-typed/bad-pairs-13.stella",
            "tests/pairs/ill-typed/bad-pairs-14.stella",
            "tests/pairs/ill-typed/bad-pairs-15.stella",
            "tests/pairs/ill-typed/bad-pairs-16.stella",
            "tests/pairs/ill-typed/bad-pairs-17.stella",
            "tests/tuples/ill-typed/bad-tuples-1.stella",
            "tests/tuples/ill-typed/bad-tuples-2.stella",
            "tests/tuples/ill-typed/bad-tuples-3.stella",
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
