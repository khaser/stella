import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class ListsTest {
    @ParameterizedTest(name = "#lists well-typed {0}")
    @ValueSource(strings = {
            "tests/lists/well-typed/empty-list.stella",
            "tests/lists/well-typed/lists-1.stella",
            "tests/lists/well-typed/lists-2.stella",
            "tests/lists/well-typed/lists-3.stella",
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

    @ParameterizedTest(name = "#lists ill-typed {0}")
    @ValueSource(strings = {
            "tests/lists/ill-typed/ambiguous-list-type-isempty.stella",
            "tests/lists/ill-typed/bad-lists-1.stella",
            "tests/lists/ill-typed/bad-lists-2.stella",
            "tests/lists/ill-typed/bad-variants-1.stella",
            "tests/lists/ill-typed/not-a-list-head.stella",
            "tests/lists/ill-typed/not-a-list-isempty.stella",
            "tests/lists/ill-typed/not-a-list-tail.stella",
            "tests/lists/ill-typed/unexepected-list-unit.stella",
            "tests/lists/ill-typed/unexpected-empty-list.stella",
            "tests/lists/ill-typed/unexpected-list-if-condition.stella",
            "tests/lists/ill-typed/unexpected-list-let-bindings.stella",
            "tests/lists/ill-typed/unexpected-list-natrec.stella",
            "tests/lists/ill-typed/unexpected-list-pair.stella",
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
