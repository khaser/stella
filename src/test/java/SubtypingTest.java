import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class SubtypingTest {
    @ParameterizedTest(name = "#subtyping well-typed {0}")
    @ValueSource(strings = {
            "tests/subtyping/well-typed/subtyping-1.stella",
            "tests/subtyping/well-typed/subtyping-2.stella",
            "tests/subtyping/well-typed/subtyping-3.stella",
            "tests/subtyping/well-typed/subtyping-4.stella",
            "tests/subtyping/well-typed/subtyping-5.stella",
            "tests/subtyping/well-typed/subtyping-6.stella",
            "tests/subtyping/well-typed/subtyping-7.stella",
            "tests/subtyping/well-typed/subtyping-8.stella",
            "tests/subtyping/well-typed/subtyping-9.stella",
            "tests/subtyping/well-typed/subtyping-10.stella",
            "tests/subtyping/well-typed/subtyping-11.stella",
            "tests/subtyping/well-typed/subtyping-12.stella",
            "tests/subtyping/functions/well-typed/functions-1.stella",
            "tests/subtyping/functions/well-typed/functions-2.stella",
            "tests/subtyping/functions/well-typed/functions-3.stella",
            "tests/subtyping/functions/well-typed/functions-4.stella",
            "tests/subtyping/pairs/well-typed/pairs-1.stella",
            "tests/subtyping/pairs/well-typed/pairs-2.stella",
            "tests/subtyping/pairs/well-typed/pairs-3.stella",
            "tests/subtyping/pairs/well-typed/pairs-4.stella",
            "tests/subtyping/pairs/well-typed/pairs-5.stella",
            "tests/subtyping/lists/well-typed/lists-1.stella",
            "tests/subtyping/lists/well-typed/lists-2.stella",
            "tests/subtyping/lists/well-typed/lists-3.stella",
            "tests/subtyping/lists/well-typed/lists-4.stella",
            "tests/subtyping/lists/well-typed/lists-5.stella",
            "tests/subtyping/sum-types/well-typed/sum-types-1.stella",
            "tests/subtyping/sum-types/well-typed/sum-types-2.stella",
            "tests/subtyping/sum-types/well-typed/sum-types-3.stella",
            "tests/subtyping/sum-types/well-typed/sum-types-4.stella",
            "tests/subtyping/sum-types/well-typed/sum-types-5.stella",
            "tests/subtyping/variants/well-typed/variants-1.stella",
            "tests/subtyping/variants/well-typed/variants-2.stella",
            "tests/subtyping/variants/well-typed/variants-3.stella",
            "tests/subtyping/variants/well-typed/variants-4.stella",
            "tests/subtyping/variants/well-typed/variants-5.stella",
            "tests/subtyping/references/well-typed/references-1.stella",
            "tests/subtyping/references/well-typed/references-2.stella",
            "tests/subtyping/references/well-typed/references-3.stella",
            "tests/subtyping/references/well-typed/references-4.stella",
            "tests/subtyping/references/well-typed/references-5.stella",
            "tests/subtyping/references/well-typed/references-6.stella",
            "tests/subtyping/exceptions/well-typed/exceptions-1.stella",
            "tests/subtyping/exceptions/well-typed/exceptions-2.stella",
            "tests/subtyping/exceptions/well-typed/exceptions-3.stella",
            "tests/subtyping/exceptions/well-typed/exceptions-4.stella",
            "tests/subtyping/ambiguous-type-as-bottom/well-typed/inl.stella",
            "tests/subtyping/ambiguous-type-as-bottom/well-typed/inr.stella",
            "tests/subtyping/ambiguous-type-as-bottom/well-typed/lists.stella",
            "tests/subtyping/ambiguous-type-as-bottom/well-typed/panic.stella",
            "tests/subtyping/ambiguous-type-as-bottom/well-typed/throw.stella",
            "tests/subtyping/type-cast/well-typed/type-cast-1.stella",
            "tests/subtyping/type-cast/well-typed/type-cast-2.stella",
            "tests/subtyping/type-cast-patterns/well-typed/type-cast-patterns-1.stella",
            "tests/subtyping/type-cast-patterns/well-typed/type-cast-patterns-2.stella",
            "tests/subtyping/type-cast-patterns/well-typed/type-cast-patterns-3.stella",
            "tests/subtyping/type-cast-patterns/well-typed/type-cast-patterns-4.stella",
            "tests/subtyping/type-cast-patterns/well-typed/type-cast-patterns-5.stella",
            "tests/subtyping/try-cast-as/well-typed/try-cast-as-1.stella",
            "tests/subtyping/try-cast-as/well-typed/try-cast-as-2.stella",
            "tests/subtyping/try-cast-as/well-typed/try-cast-as-3.stella",
            "tests/subtyping/try-cast-as/well-typed/try-cast-as-4.stella",
            "tests/subtyping/try-cast-as/well-typed/try-cast-as-5.stella",
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

    @ParameterizedTest(name = "#subtyping ill-typed {0}")
    @ValueSource(strings = {
            "tests/subtyping/ill-typed/bad-subtyping-1.stella",
            "tests/subtyping/ill-typed/bad-subtyping-2.stella",
            "tests/subtyping/ill-typed/bad-subtyping-3.stella",
            "tests/subtyping/ill-typed/bad-subtyping-4.stella",
            "tests/subtyping/ill-typed/bad-subtyping-5.stella",
            "tests/subtyping/ill-typed/bad-subtyping-6.stella",
            "tests/subtyping/ill-typed/bad-subtyping-7.stella",
            "tests/subtyping/ill-typed/bad-subtyping-8.stella",
            "tests/subtyping/ill-typed/bad-subtyping-9.stella",
            "tests/subtyping/functions/ill-typed/functions-1.stella",
            "tests/subtyping/functions/ill-typed/functions-2.stella",
            "tests/subtyping/functions/ill-typed/functions-3.stella",
            "tests/subtyping/functions/ill-typed/functions-4.stella",
            "tests/subtyping/pairs/ill-typed/pairs-1.stella",
            "tests/subtyping/pairs/ill-typed/pairs-2.stella",
            "tests/subtyping/pairs/ill-typed/pairs-3.stella",
            "tests/subtyping/pairs/ill-typed/pairs-4.stella",
            "tests/subtyping/pairs/ill-typed/pairs-5.stella",
            "tests/subtyping/lists/ill-typed/lists-1.stella",
            "tests/subtyping/lists/ill-typed/lists-2.stella",
            "tests/subtyping/lists/ill-typed/lists-3.stella",
            "tests/subtyping/lists/ill-typed/lists-4.stella",
            "tests/subtyping/lists/ill-typed/lists-5.stella",
            "tests/subtyping/sum-types/ill-typed/sum-types-1.stella",
            "tests/subtyping/sum-types/ill-typed/sum-types-2.stella",
            "tests/subtyping/sum-types/ill-typed/sum-types-3.stella",
            "tests/subtyping/sum-types/ill-typed/sum-types-4.stella",
            "tests/subtyping/sum-types/ill-typed/sum-types-5.stella",
            "tests/subtyping/variants/ill-typed/variants-1.stella",
            "tests/subtyping/variants/ill-typed/variants-2.stella",
            "tests/subtyping/variants/ill-typed/variants-3.stella",
            "tests/subtyping/variants/ill-typed/variants-4.stella",
            "tests/subtyping/variants/ill-typed/variants-5.stella",
            "tests/subtyping/references/ill-typed/references-1.stella",
            "tests/subtyping/references/ill-typed/references-2.stella",
            "tests/subtyping/references/ill-typed/references-3.stella",
            "tests/subtyping/references/ill-typed/references-4.stella",
            "tests/subtyping/references/ill-typed/references-5.stella",
            "tests/subtyping/references/ill-typed/references-6.stella",
            "tests/subtyping/references/ill-typed/references-7.stella",
            "tests/subtyping/exceptions/ill-typed/exceptions-1.stella",
            "tests/subtyping/exceptions/ill-typed/exceptions-2.stella",
            "tests/subtyping/exceptions/ill-typed/exceptions-3.stella",
            "tests/subtyping/exceptions/ill-typed/exceptions-4.stella",
            "tests/subtyping/ambiguous-type-as-bottom/ill-typed/inl.stella",
            "tests/subtyping/ambiguous-type-as-bottom/ill-typed/inr.stella",
            "tests/subtyping/ambiguous-type-as-bottom/ill-typed/lists.stella",
            "tests/subtyping/ambiguous-type-as-bottom/ill-typed/throw.stella",
            "tests/subtyping/type-cast/ill-typed/type-cast-1.stella",
            "tests/subtyping/type-cast/ill-typed/type-cast-2.stella",
            "tests/subtyping/type-cast-patterns/ill-typed/type-cast-patterns-1.stella",
            "tests/subtyping/type-cast-patterns/ill-typed/type-cast-patterns-2.stella",
            "tests/subtyping/try-cast-as/ill-typed/try-cast-as-1.stella",
            "tests/subtyping/try-cast-as/ill-typed/try-cast-as-2.stella",
            "tests/subtyping/try-cast-as/ill-typed/try-cast-as-3.stella",
            "tests/subtyping/try-cast-as/ill-typed/try-cast-as-4.stella",
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
