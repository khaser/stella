import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class ExceptionsTest {
    @ParameterizedTest(name = "#exceptions well-typed {0}")
    @ValueSource(strings = {
            "tests/exceptions/well-typed/panic-1.stella",
            "tests/exceptions/well-typed/panic-2.stella",
            "tests/exceptions/well-typed/panic-3.stella",
            "tests/exceptions/well-typed/throw-1.stella",
            "tests/exceptions/well-typed/throw-2.stella",
            "tests/exceptions/well-typed/try-catch-1.stella",
            "tests/exceptions/well-typed/try-catch-2.stella",
            "tests/exceptions/well-typed/try-with-1.stella",
            "tests/exceptions/well-typed/try-with-2.stella",
            "tests/exceptions/well-typed/extra-tests/open-variant-exceptions.stella",
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

    @ParameterizedTest(name = "#exceptions ill-typed {0}")
    @ValueSource(strings = {
            "tests/exceptions/ill-typed/ambiguous-panic-type-1.stella",
            "tests/exceptions/ill-typed/ambiguous-panic-type-2.stella",
            "tests/exceptions/ill-typed/ambiguous-panic-type-3.stella",
            "tests/exceptions/ill-typed/ambiguous-panic-type-4.stella",
            "tests/exceptions/ill-typed/ambiguous-throw-type-1.stella",
            "tests/exceptions/ill-typed/ambiguous-throw-type-2.stella",
            "tests/exceptions/ill-typed/ambiguous-throw-type-3.stella",
            "tests/exceptions/ill-typed/exception-type-not-declared-throw.stella",
            "tests/exceptions/ill-typed/exception-type-not-declared-try-catch.stella",
            "tests/exceptions/ill-typed/exception-type-not-declared-try-with.stella",
            "tests/exceptions/ill-typed/unexpected-type-for-expression-throw-1.stella",
            "tests/exceptions/ill-typed/unexpected-type-for-expression-throw-2.stella",
            "tests/exceptions/ill-typed/unexpected-type-for-expression-try-catch-1.stella",
            "tests/exceptions/ill-typed/unexpected-type-for-expression-try-catch-2.stella",
            "tests/exceptions/ill-typed/unexpected-type-for-expression-try-catch-3.stella",
            "tests/exceptions/ill-typed/unexpected-type-for-expression-try-with-1.stella",
            "tests/exceptions/ill-typed/unexpected-type-for-expression-try-with-2.stella",
            "tests/exceptions/ill-typed/unexpected-type-for-expression-try-with-3.stella",
            "tests/exceptions/ill-typed/extra-tests/open-variant-exceptions.stella",
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
