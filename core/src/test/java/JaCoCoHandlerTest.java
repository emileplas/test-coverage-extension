import com.brabel.coverage.extension.core.CodeCoverage;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static com.brabel.coverage.extension.core.JaCoCoHandler.getTotalCodeCoverage;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class JaCoCoHandlerTest {

    @Test
    public void testGetTotalCodeCoverageSingleModuleProject(){
        File singleModuleFile = new File("src/test/resources/jacoco-examples-exec/single-module-example-jacoco-output.exec");
        CodeCoverage expectedCodeCoverage = new CodeCoverage(null, CodeCoverage.CoverageType.TOTAL, 21, 51,  5, 10);

        try {
            CodeCoverage actualCodeCoverage = getTotalCodeCoverage(singleModuleFile, new File("../single-module-example/target/classes/com"));

            assertEquals(expectedCodeCoverage.getCoverageType(), actualCodeCoverage.getCoverageType());
            assertNull(actualCodeCoverage.getFilePath());
            assertEquals(expectedCodeCoverage.getInstructionsCovered(), actualCodeCoverage.getInstructionsCovered());
            assertEquals(expectedCodeCoverage.getInstructionsMissed(), actualCodeCoverage.getInstructionsMissed());
            assertEquals(expectedCodeCoverage.getLinesMissed(), actualCodeCoverage.getLinesMissed());
            assertEquals(expectedCodeCoverage.getLinesCovered(), actualCodeCoverage.getLinesCovered());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
