import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class FixpointTest {
    @ParameterizedTest(name = "#fixpoint-combinator well-typed {0}")
    @ValueSource(strings = {
            "tests/recursions/well-typed/fix.stella",
            "tests/recursions/well-typed/fix-2.stella",
            "tests/recursions/well-typed/general-recursion.stella",
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

    @ParameterizedTest(name = "#fixpoint-combinator ill-typed {0}")
    @ValueSource(strings = {
            "tests/recursions/ill-typed/fix.stella",
            "tests/recursions/ill-typed/fix-2.stella",
            "tests/recursions/ill-typed/general-recursion.stella",
            "tests/recursions/ill-typed/divergent-param-and-return-types.stella",
            "tests/recursions/ill-typed/not-a-function.stella",
            "tests/recursions/ill-typed/two-parameters.stella",
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
