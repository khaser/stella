import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class RecordsTest {
    @ParameterizedTest(name = "#records well-typed {0}")
    @ValueSource(strings = {
            "tests/records/well-typed/records-1.stella",
            "tests/records/well-typed/records-2.stella",
            "tests/records/well-typed/records-4.stella",
            "tests/records/well-typed/records-5.stella",
            "tests/records/well-typed/records-6.stella",
            "tests/records/well-typed/records-7.stella",
            "tests/records/well-typed/records-8.stella",
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

    @ParameterizedTest(name = "#records ill-typed {0}")
    @ValueSource(strings = {
            "tests/records/ill-typed/bad-records-1.stella",
            "tests/records/ill-typed/bad-records-2.stella",
            "tests/records/ill-typed/bad-records-3.stella",
            "tests/records/ill-typed/bad-records-4.stella",
            "tests/records/ill-typed/bad-records-5.stella",
            "tests/records/ill-typed/bad-records-6.stella",
            "tests/records/ill-typed/bad-records-7.stella",
            "tests/records/ill-typed/bad-records-8.stella",
            "tests/records/ill-typed/bad-records-9.stella",
            "tests/records/ill-typed/bad-records-10.stella",
            "tests/records/ill-typed/bad-records-11.stella",
            "tests/records/ill-typed/bad-records-12.stella",
            "tests/records/ill-typed/bad-records-13.stella",
            "tests/records/ill-typed/bad-records-14.stella",
            "tests/records/ill-typed/bad-records-15.stella",
            "tests/records/ill-typed/bad-records-16.stella",
            "tests/records/ill-typed/bad-records-17.stella",
            "tests/records/ill-typed/bad-records-18.stella",
            "tests/records/ill-typed/bad-records-19.stella",
            "tests/records/ill-typed/bad-records-20.stella",
            "tests/records/ill-typed/bad-records-21.stella",
            "tests/records/ill-typed/bad-records-23.stella",
            "tests/records/ill-typed/records-3.stella",
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
