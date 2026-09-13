import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.file.Paths;

class CoreTest {
    @ParameterizedTest(name = "Core well-typed {0}")
    @ValueSource(strings = {
            "tests/core/well-typed/abstract-function.stella",
            "tests/core/well-typed/added-test-1.stella",
            "tests/core/well-typed/added-test-2.stella",
            "tests/core/well-typed/apply-increase.stella",
            "tests/core/well-typed/applying-actual-function-3.stella",
            "tests/core/well-typed/bool-to-nat.stella",
            "tests/core/well-typed/cubes.stella",
            "tests/core/well-typed/double-application.stella",
            "tests/core/well-typed/factorial.stella",
            "tests/core/well-typed/good-if-2.stella",
            "tests/core/well-typed/good-if.stella",
            "tests/core/well-typed/good-succ-1.stella",
            "tests/core/well-typed/good-succ-2.stella",
            "tests/core/well-typed/higher-order-1.stella",
            "tests/core/well-typed/higher-order-2.stella",
            "tests/core/well-typed/if-funcs.stella",
            "tests/core/well-typed/increment-triple.stella",
            "tests/core/well-typed/increment_twice.stella",
            "tests/core/well-typed/inner-if.stella",
            "tests/core/well-typed/logical-operators.stella",
            "tests/core/well-typed/many-if.stella",
            "tests/core/well-typed/my-good-if.stella",
            "tests/core/well-typed/my-good-succ.stella",
            "tests/core/well-typed/my-well-typed-1.stella",
            "tests/core/well-typed/my-well-typed-2.stella",
            "tests/core/well-typed/nat-to-bool.stella",
            "tests/core/well-typed/negate.stella",
            "tests/core/well-typed/nested.stella",
            "tests/core/well-typed/shadowed-variable-2.stella",
            "tests/core/well-typed/simple-if.stella",
            "tests/core/well-typed/simple-succ.stella",
            "tests/core/well-typed/simple-types.stella",
            "tests/core/well-typed/squares.stella",
            "tests/core/well-typed/succ-with-func.stella",
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

    @ParameterizedTest(name = "Core ill-typed {0}")
    @ValueSource(strings = {
            "tests/core/ill-typed/missing_main/missing-main-1.stella",
            "tests/core/ill-typed/missing_main/missing-main-2.stella",
            "tests/core/ill-typed/not-a-function/applying-non-function-1.stella",
            "tests/core/ill-typed/not-a-function/applying-non-function-2.stella",
            "tests/core/ill-typed/not-a-function/applying-non-function-3.stella",
            "tests/core/ill-typed/not-a-function/if-funcs.stella",
            "tests/core/ill-typed/not-a-function/my-factorial.stella",
            "tests/core/ill-typed/not-a-function/not-a-function-1.stella",
            "tests/core/ill-typed/not-a-function/shadowed-variable-1.stella",
            "tests/core/ill-typed/not-a-function/shadowed-variable-2.stella",
            "tests/core/ill-typed/undefined-variable/bad-if-and-undefined-variable-1.stella",
            "tests/core/ill-typed/undefined-variable/undef-var-1.stella",
            "tests/core/ill-typed/undefined-variable/undefined-variable-1.stella",
            "tests/core/ill-typed/undefined-variable/undefined-variable-2.stella",
            "tests/core/ill-typed/undefined-variable/undefined-variable-3.stella",
            "tests/core/ill-typed/unexpected-lambda/bad-function-call.stella",
            "tests/core/ill-typed/unexpected-lambda/bad-if-4.stella",
            "tests/core/ill-typed/unexpected-lambda/bad-squares-1.stella",
            "tests/core/ill-typed/unexpected-lambda/bad-succ-2.stella",
            "tests/core/ill-typed/unexpected-lambda/if-funcs.stella",
            "tests/core/ill-typed/unexpected-lambda/my-ill-typed-2.stella",
            "tests/core/ill-typed/unexpected-lambda/unexpected-lambda-1.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/added-test-2.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/application-param-type.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/argument-type-mismatch-1.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/bad-abstraction.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/bad-factorial.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/bad-higher-order-1.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/bad-if-1.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/bad-if-2.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/bad-if-3.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/bad-iszero.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/bad-nat-2.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/bad-nat-rec-1.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/bad-nat-rec-2.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/bad-return-type.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/bad-squares-2.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/bad-succ-1.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/bad-succ-3.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/function-mismatch.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/if-funcs.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/my-ill-test-2.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/my-ill-typed-1.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/my-mismatch.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/nat__rec-parameters.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/unexpected-type-for-expression-1.stella",
            "tests/core/ill-typed/unexpected-type-for-parameter/added-test-1.stella",
            "tests/core/ill-typed/unexpected-type-for-parameter/argument-type-mismatch-2.stella",
            "tests/core/ill-typed/unexpected-type-for-parameter/argument-type-mismatch-3.stella",
            "tests/core/ill-typed/unexpected-type-for-expression/bad-nat-1.stella",
            "tests/core/ill-typed/unexpected-type-for-parameter/if-funcs.stella",
            "tests/core/ill-typed/unexpected-type-for-parameter/invalid-nat.stella",
            "tests/core/ill-typed/unexpected-type-for-parameter/invalid-not_.stella",
            "tests/core/ill-typed/unexpected-type-for-parameter/unexpected-type-for-parameter-1.stella",
    })
    void testIllTyped(String filepath) throws Exception {
        final InputStream original = System.in;
        String dirName = Paths.get(filepath).getParent().getFileName().toString();
        String expectedError = "ERROR_" + dirName.toUpperCase().replace('-', '_');
        try (FileInputStream fips = new FileInputStream(filepath)) {
            System.setIn(fips);
            Exception ex = assertThrows(Exception.class, () -> Main.main(new String[0]));
            assertTrue(
                ex.getMessage() != null && ex.getMessage().contains(expectedError),
                "Expected error containing '" + expectedError + "' but got: '" + ex.getMessage() + "'"
            );
        } finally {
            System.setIn(original);
        }
    }
}
