package tech.linebyline.coverage.extension.core.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FileUtilTest {

    private Set<File> fileSet;
    private FileUtil fileUtil;

    @BeforeEach
    public void setUp() {
        fileUtil = new FileUtil();

        // Creating a mock set of files for testing
        fileSet = new HashSet<>();
        fileSet.add(new File("/path/to/directory/TestFile1.java"));
        fileSet.add(new File("/path/to/directory/ExampleTest.java"));
        fileSet.add(new File("/path/to/other/Example.java"));
        fileSet.add(new File("/another/path/SampleTest.java"));
        fileSet.add(new File("/another/path/Sample.java"));
    }

    @Test
    public void testPathIncludeFilter() {
        FileUtil.Filters filters = new FileUtil.Filters();
        filters.addPathIncludeFilter("/path/to/directory");

        Set<File> result = fileUtil.filterFiles(fileSet, filters);

        // Check that only files in "/path/to/directory" are returned
        assertEquals(2, result.size());
        assertTrue(result.contains(new File("/path/to/directory/TestFile1.java")));
        assertTrue(result.contains(new File("/path/to/directory/ExampleTest.java")));
    }

    @Test
    public void testPathExcludeFilter() {
        FileUtil.Filters filters = new FileUtil.Filters();
        filters.addPathExcludeFilter("/another/path");

        Set<File> result = fileUtil.filterFiles(fileSet, filters);

        // Check that files not in "/another/path" are returned
        assertEquals(3, result.size());
        assertFalse(result.contains(new File("/another/path/SampleTest.java")));
        assertFalse(result.contains(new File("/another/path/Sample.java")));
    }

    @Test
    public void testPatternIncludeFilter() {
        FileUtil.Filters filters = new FileUtil.Filters();
        filters.addPatternIncludeFilter("*Test.java");

        Set<File> result = fileUtil.filterFiles(fileSet, filters);

        // Check that only files matching the pattern "*Test.java" are returned
        assertEquals(2, result.size());
        assertTrue(result.contains(new File("/path/to/directory/ExampleTest.java")));
        assertTrue(result.contains(new File("/another/path/SampleTest.java")));
        assertFalse(result.contains(new File("/path/to/directory/TestFile1.java")));
    }

    @Test
    public void testPatternExcludeFilter() {
        FileUtil.Filters filters = new FileUtil.Filters();
        filters.addPatternExcludeFilter("*Test.java");

        Set<File> result = fileUtil.filterFiles(fileSet, filters);

        // Check that files not matching the pattern "*Test.java" are returned
        assertEquals(3, result.size());
        assertTrue(result.contains(new File("/path/to/other/Example.java")));
        assertTrue(result.contains(new File("/another/path/Sample.java")));
        assertTrue(result.contains(new File("/path/to/directory/TestFile1.java")));
    }

    @Test
    public void testCombinedPathIncludeAndPatternExcludeFilter() {
        FileUtil.Filters filters = new FileUtil.Filters();
        filters.addPathIncludeFilter("/path/to/directory");
        filters.addPatternExcludeFilter("*Test.java");

        Set<File> result = fileUtil.filterFiles(fileSet, filters);

        // Check that only files in "/path/to/directory" that do not match "*Test.java" are returned
        assertEquals(1, result.size());
        assertTrue(result.contains(new File("/path/to/directory/TestFile1.java")));
        assertFalse(result.contains(new File("/path/to/directory/ExampleTest")));// Adjust based on expected files
    }

    @Test
    public void testCustomFilter() {
        FileUtil.Filters filters = new FileUtil.Filters();
        // Custom filter to include only files with ".java" extension
        filters.addCustomFilter(file -> file.getName().endsWith(".java"));

        Set<File> result = fileUtil.filterFiles(fileSet, filters);

        // Verify that only files ending with ".java" are included
        assertFalse(result.isEmpty());
        assertTrue(result.stream().allMatch(file -> file.getName().endsWith(".java")));
    }

    @Test
    public void testEmptyFileSet() {
        FileUtil.Filters filters = new FileUtil.Filters();
        filters.addPatternIncludeFilter("*Test.java");

        Set<File> emptyFileSet = new HashSet<>();
        Set<File> result = fileUtil.filterFiles(emptyFileSet, filters);

        // Empty input set should return an empty result set
        assertTrue(result.isEmpty());
    }

    @Test
    public void testNoFilters() {
        FileUtil.Filters filters = new FileUtil.Filters();

        Set<File> result = fileUtil.filterFiles(fileSet, filters);

        // No filters should return the original set unmodified
        assertEquals(fileSet.size(), result.size());
        assertTrue(result.containsAll(fileSet));
    }
}
