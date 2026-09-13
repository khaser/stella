import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class SumTypesTest {
    @ParameterizedTest(name = "#sum-types well-typed {0}")
    @ValueSource(strings = {
            "tests/sum-types/well-typed/sum-types-1.stella",
            "tests/sum-types/well-typed/sum-types-2.stella",
            "tests/sum-types/well-typed/sum-types-3.stella",
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

    @ParameterizedTest(name = "#sum-types ill-typed {0}")
    @ValueSource(strings = {
            "tests/sum-types/ill-typed/bad-sum-types-1.stella",
            "tests/sum-types/ill-typed/bad-sum-types-2.stella",
            "tests/sum-types/ill-typed/bad-sum-types-3.stella",
            "tests/sum-types/ill-typed/bad-sum-types-4.stella",
            "tests/sum-types/ill-typed/bad-sum-types-5.stella",
            "tests/sum-types/ill-typed/bad-sum-types-6.stella",
            "tests/sum-types/ill-typed/bad-sum-types-7.stella",
            "tests/sum-types/ill-typed/bad-sum-types-8.stella",
            "tests/sum-types/ill-typed/bad-sum-types-9.stella",
            "tests/sum-types/ill-typed/bad-sum-types-10.stella",
            "tests/sum-types/ill-typed/bad-sum-types-11.stella",
            "tests/sum-types/ill-typed/bad-sum-types-12.stella",
            "tests/sum-types/ill-typed/bad-sum-types-13.stella",
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
