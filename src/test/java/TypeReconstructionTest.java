import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class TypeReconstructionTest {
    @ParameterizedTest(name = "#type-reconstruction well-typed {0}")
    @ValueSource(strings = {
            "tests/type-reconstruction/well-typed/functions/functions-1.stella",
            "tests/type-reconstruction/well-typed/functions/functions-2.stella",
            "tests/type-reconstruction/well-typed/functions/functions-3.stella",
            "tests/type-reconstruction/well-typed/functions/functions-4.stella",
            "tests/type-reconstruction/well-typed/functions/functions-5.stella",
            "tests/type-reconstruction/well-typed/lists/lists-1.stella",
            "tests/type-reconstruction/well-typed/lists/lists-2.stella",
            "tests/type-reconstruction/well-typed/lists/lists-3.stella",
            "tests/type-reconstruction/well-typed/lists/lists-4.stella",
            "tests/type-reconstruction/well-typed/pairs/pairs-1.stella",
            "tests/type-reconstruction/well-typed/pairs/pairs-2.stella",
            "tests/type-reconstruction/well-typed/pairs/pairs-3.stella",
            "tests/type-reconstruction/well-typed/pairs/pairs-4.stella",
            "tests/type-reconstruction/well-typed/pairs/pairs-5.stella",
            "tests/type-reconstruction/well-typed/pairs/pairs-6.stella",
            "tests/type-reconstruction/well-typed/records/records-1.stella",
            "tests/type-reconstruction/well-typed/records/records-2.stella",
            "tests/type-reconstruction/well-typed/records/records-3.stella",
            "tests/type-reconstruction/well-typed/records/records-4.stella",
            "tests/type-reconstruction/well-typed/records/records-5.stella",
            "tests/type-reconstruction/well-typed/records/records-6.stella",
            "tests/type-reconstruction/well-typed/records/records-7.stella",
            "tests/type-reconstruction/well-typed/references/references-1.stella",
            "tests/type-reconstruction/well-typed/references/references-2.stella",
            "tests/type-reconstruction/well-typed/references/references-3.stella",
            "tests/type-reconstruction/well-typed/references/references-4.stella",
            "tests/type-reconstruction/well-typed/references/references-5.stella",
            "tests/type-reconstruction/well-typed/sum-types/sum-types-1.stella",
            "tests/type-reconstruction/well-typed/sum-types/sum-types-2.stella",
            "tests/type-reconstruction/well-typed/sum-types/sum-types-3.stella",
            "tests/type-reconstruction/well-typed/exceptions/exceptions-1.stella",
            "tests/type-reconstruction/well-typed/exceptions/exceptions-2.stella",
            "tests/type-reconstruction/well-typed/exceptions/exceptions-3.stella",
            "tests/type-reconstruction/well-typed/exceptions/exceptions-4.stella",
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

    @ParameterizedTest(name = "#type-reconstruction ill-typed {0}")
    @ValueSource(strings = {
            "tests/type-reconstruction/ill-typed/functions/functions-1.stella",
            "tests/type-reconstruction/ill-typed/functions/functions-2.stella",
            "tests/type-reconstruction/ill-typed/functions/functions-3.stella",
            "tests/type-reconstruction/ill-typed/functions/functions-4.stella",
            "tests/type-reconstruction/ill-typed/functions/functions-5.stella",
            "tests/type-reconstruction/ill-typed/lists/lists-1.stella",
            "tests/type-reconstruction/ill-typed/lists/lists-2.stella",
            "tests/type-reconstruction/ill-typed/lists/lists-3.stella",
            "tests/type-reconstruction/ill-typed/lists/lists-4.stella",
            "tests/type-reconstruction/ill-typed/pairs/pairs-1.stella",
            "tests/type-reconstruction/ill-typed/pairs/pairs-2.stella",
            "tests/type-reconstruction/ill-typed/pairs/pairs-3.stella",
            "tests/type-reconstruction/ill-typed/pairs/pairs-4.stella",
            "tests/type-reconstruction/ill-typed/pairs/pairs-5.stella",
            "tests/type-reconstruction/ill-typed/records/records-1.stella",
            "tests/type-reconstruction/ill-typed/records/records-2.stella",
            "tests/type-reconstruction/ill-typed/records/records-3.stella",
            "tests/type-reconstruction/ill-typed/records/records-4.stella",
            "tests/type-reconstruction/ill-typed/records/records-5.stella",
            "tests/type-reconstruction/ill-typed/records/records-6.stella",
            "tests/type-reconstruction/ill-typed/records/records-7.stella",
            "tests/type-reconstruction/ill-typed/references/references-1.stella",
            "tests/type-reconstruction/ill-typed/references/references-2.stella",
            "tests/type-reconstruction/ill-typed/references/references-3.stella",
            "tests/type-reconstruction/ill-typed/references/references-4.stella",
            "tests/type-reconstruction/ill-typed/references/references-5.stella",
            "tests/type-reconstruction/ill-typed/sum-types/sum-types-1.stella",
            "tests/type-reconstruction/ill-typed/sum-types/sum-types-2.stella",
            "tests/type-reconstruction/ill-typed/sum-types/sum-types-3.stella",
            "tests/type-reconstruction/ill-typed/exceptions/exceptions-1.stella",
            "tests/type-reconstruction/ill-typed/exceptions/exceptions-2.stella",
            "tests/type-reconstruction/ill-typed/exceptions/exceptions-3.stella",
            "tests/type-reconstruction/ill-typed/exceptions/exceptions-4.stella",
            "tests/type-reconstruction/ill-typed/exceptions/exceptions-5.stella",
            "tests/type-reconstruction/ill-typed/infinite-type/infinite-type-1.stella",
            "tests/type-reconstruction/ill-typed/infinite-type/infinite-type-2.stella",
            "tests/type-reconstruction/ill-typed/infinite-type/infinite-type-3.stella",
            "tests/type-reconstruction/ill-typed/infinite-type/infinite-type-4.stella",
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
