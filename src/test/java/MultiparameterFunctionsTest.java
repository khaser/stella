import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class MultiparameterFunctionsTest {
    @ParameterizedTest(name = "#nullary-functions/#multiparameter-functions well-typed {0}")
    @ValueSource(strings = {
            "tests/core/well-typed/extra-tests/nullary-functions/test-1.stella",
            "tests/core/well-typed/extra-tests/nullary-functions/test-2.stella",
            "tests/core/well-typed/extra-tests/multiparameter-functions/test-1.stella",
            "tests/core/well-typed/extra-tests/multiparameter-functions/test-2.stella",
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

    @ParameterizedTest(name = "#nullary-functions/#multiparameter-functions ill-typed {0}")
    @ValueSource(strings = {
            "tests/core/ill-typed/extra-tests/incorrect_arity_of_main/my-test.stella",
            "tests/core/ill-typed/extra-tests/incorrect_arity_of_main/my-test-2.stella",
            "tests/core/ill-typed/extra-tests/incorrect_number_of_arguments/test-1.stella",
            "tests/core/ill-typed/extra-tests/incorrect_number_of_arguments/test-2.stella",
            "tests/core/ill-typed/extra-tests/nullary-functions/test-1.stella",
            "tests/core/ill-typed/extra-tests/nullary-functions/test-2.stella",
            "tests/core/ill-typed/extra-tests/nullary-functions/test-3.stella",
            "tests/core/ill-typed/extra-tests/nullary-functions/test-4.stella",
            "tests/core/ill-typed/extra-tests/nullary-functions/test-5.stella",
            "tests/core/ill-typed/extra-tests/multiparameter-functions/bad-add-1.stella",
            "tests/core/ill-typed/extra-tests/multiparameter-functions/bad-cmp-1.stella",
            "tests/core/ill-typed/extra-tests/multiparameter-functions/bad-cmp-2.stella",
            "tests/core/ill-typed/extra-tests/multiparameter-functions/bad-cmp-3.stella",
            "tests/core/ill-typed/extra-tests/multiparameter-functions/bad-cmp-4.stella",
            "tests/core/ill-typed/extra-tests/multiparameter-functions/bad-cmp-5.stella",
            "tests/core/ill-typed/extra-tests/multiparameter-functions/bad-cmp-6.stella",
            "tests/core/ill-typed/extra-tests/multiparameter-functions/bad-logic-and-1.stella",
            "tests/core/ill-typed/extra-tests/multiparameter-functions/bad-logic-or-1.stella",
            "tests/core/ill-typed/extra-tests/multiparameter-functions/bad-multiply-1.stella",
            "tests/core/ill-typed/extra-tests/multiparameter-functions/test-1.stella",
            "tests/core/ill-typed/extra-tests/multiparameter-functions/test-2.stella",
            "tests/core/ill-typed/extra-tests/multiparameter-functions/test-3.stella",
            "tests/core/ill-typed/extra-tests/multiparameter-functions/test-4.stella",
            "tests/core/ill-typed/extra-tests/multiparameter-functions/test-5.stella",
            "tests/core/ill-typed/extra-tests/multiparameter-functions/test-6.stella",
            "tests/core/ill-typed/extra-tests/multiparameter-functions/test-7.stella",
            "tests/core/ill-typed/extra-tests/unexpected_number_of_parameters_in_lambda/test-1.stella",
            "tests/core/ill-typed/extra-tests/unexpected_number_of_parameters_in_lambda/test-2.stella",
            "tests/core/ill-typed/extra-tests/unexpected_number_of_parameters_in_lambda/test-3.stella",
            "tests/core/ill-typed/extra-tests/unexpected_number_of_parameters_in_lambda/test-4.stella",
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
