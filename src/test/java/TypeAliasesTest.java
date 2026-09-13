import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class TypeAliasesTest {
    @ParameterizedTest(name = "#type-aliases well-typed {0}")
    @ValueSource(strings = {
            "tests/typealiases/well-typed/double-alias.stella",
            "tests/typealiases/well-typed/simple-alias.stella",
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

    @ParameterizedTest(name = "#type-aliases ill-typed {0}")
    @ValueSource(strings = {
            "tests/typealiases/ill-typed/wrong-double-alias.stella",
            "tests/typealiases/ill-typed/wrong-simple-alias.stella",
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
