import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class StructuralPatternsTest {
    @ParameterizedTest(name = "#structural-patterns well-typed {0}")
    @ValueSource(strings = {
            "tests/structurals/well-typed/bool-1.stella",
            "tests/structurals/well-typed/bool-2.stella",
            "tests/structurals/well-typed/lists-1.stella",
            "tests/structurals/well-typed/nat-1.stella",
            "tests/structurals/well-typed/pairs-1.stella",
            "tests/structurals/well-typed/pairs-2.stella",
            "tests/structurals/well-typed/pairs-3.stella",
            "tests/structurals/well-typed/pairs.stella",
            "tests/structurals/well-typed/records.stella",
            "tests/structurals/well-typed/structurals-1.stella",
            "tests/structurals/well-typed/structure-patterns.stella",
            "tests/structurals/well-typed/sum-types-1.stella",
            "tests/structurals/well-typed/sum-types-2.stella",
            "tests/structurals/well-typed/tuples-1.stella",
            "tests/structurals/well-typed/unit.stella",
            "tests/let-bindings/structural-patterns/well-typed/test-1.stella",
            "tests/let-bindings/structural-patterns/well-typed/test-2.stella",
            "tests/let-bindings/structural-patterns/well-typed/test-4.stella",
            "tests/let-bindings/structural-patterns/well-typed/test-5.stella",
            "tests/let-bindings/structural-patterns/well-typed/test-6.stella",
            "tests/let-bindings/structural-patterns/well-typed/test-7.stella",
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

    @ParameterizedTest(name = "#structural-patterns ill-typed {0}")
    @ValueSource(strings = {
            "tests/structurals/ill-typed/bad-structurals-1.stella",
            "tests/structurals/ill-typed/bad-unexpected-pattern-for-type.stella",
            "tests/structurals/ill-typed/bool.stella",
            "tests/structurals/ill-typed/lists-1.stella",
            "tests/structurals/ill-typed/nat.stella",
            "tests/structurals/ill-typed/pairs.stella",
            "tests/structurals/ill-typed/records.stella",
            "tests/structurals/ill-typed/structure-patterns.stella",
            "tests/structurals/ill-typed/tuples-1.stella",
            "tests/structurals/ill-typed/unit.stella",
            "tests/let-bindings/structural-patterns/ill-typed/test-1-1.stella",
            "tests/let-bindings/structural-patterns/ill-typed/test-1-2.stella",
            "tests/let-bindings/structural-patterns/ill-typed/test-1-3.stella",
            "tests/let-bindings/structural-patterns/ill-typed/test-1-4.stella",
            "tests/let-bindings/structural-patterns/ill-typed/test-2-1.stella",
            "tests/let-bindings/structural-patterns/ill-typed/test-2-2.stella",
            "tests/let-bindings/structural-patterns/ill-typed/test-3.stella",
            "tests/let-bindings/structural-patterns/ill-typed/test-5-1.stella",
            "tests/let-bindings/structural-patterns/ill-typed/test-6-1.stella",
            "tests/let-bindings/structural-patterns/ill-typed/test-7-1.stella",
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
