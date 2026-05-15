import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

class PairsRecordsTest {
    @ParameterizedTest(name = "Well-typed pair/record {0}")
    @ValueSource(strings = {
            "tests/pairs/well-typed/pairs-1.stella",
            "tests/pairs/well-typed/pairs-2.stella",
            "tests/pairs/well-typed/pairs-3.stella",
            "tests/pairs/well-typed/pairs-4.stella",
            "tests/pairs/well-typed/pairs-5.stella",
            "tests/pairs/well-typed/pairs-6.stella",
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

    @ParameterizedTest(name = "Ill-typed pair/record {0}")
    @ValueSource(strings = {
            "tests/pairs/ill-typed/bad-pairs-1.stella",
            "tests/pairs/ill-typed/bad-pairs-2.stella",
            "tests/pairs/ill-typed/bad-pairs-3.stella",
            "tests/pairs/ill-typed/bad-pairs-4.stella",
            "tests/pairs/ill-typed/bad-pairs-5.stella",
            "tests/pairs/ill-typed/bad-pairs-6.stella",
            "tests/pairs/ill-typed/bad-pairs-7.stella",
            "tests/pairs/ill-typed/bad-pairs-8.stella",
            "tests/pairs/ill-typed/bad-pairs-9.stella",
            "tests/pairs/ill-typed/bad-pairs-10.stella",
            "tests/pairs/ill-typed/bad-pairs-11.stella",
            "tests/pairs/ill-typed/bad-pairs-12.stella",
            "tests/pairs/ill-typed/bad-pairs-13.stella",
            "tests/pairs/ill-typed/bad-pairs-14.stella",
            "tests/pairs/ill-typed/bad-pairs-15.stella",
            "tests/pairs/ill-typed/bad-pairs-16.stella",
            "tests/pairs/ill-typed/bad-pairs-17.stella",
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
        final FileInputStream fips = new FileInputStream(filepath);
        System.setIn(fips);
        assertThrows(Exception.class, () -> Main.main(new String[0]));
    }
}

