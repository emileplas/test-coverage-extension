package com.brabel.coverage.extension.core.util;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class FileUtil {

    public static class Filters {

        private Set<Predicate<File>> fileFilters = new HashSet<>();

        /**
         * Add a path filter to include files that are in a specific path
         * @param includePath The path to include
         */
        public void addPathIncludeFilter(String includePath) {
            if (includePath != null) {
                fileFilters.add(file ->
                        file.getParentFile() != null &&
                                file.getParentFile().getAbsolutePath().contains(includePath)
                );
            }
        }

        /**
         * Add a path filter to exclude files that are in a specific path
         * @param excludePath The path to exclude
         */
        public void addPathExcludeFilter(String excludePath) {
            if (excludePath != null) {
                fileFilters.add(file ->
                        file.getParentFile() == null ||
                                !file.getParentFile().getAbsolutePath().contains(excludePath)
                );
            }
        }

        /**
         * Add a pattern filter to include files that match a specific pattern
         * @param includePattern The pattern to include
         */
        public void addPatternIncludeFilter(String includePattern) {
            if (includePattern != null) {
                fileFilters.add(file ->
                        file.getName().matches(includePattern.replace("*", ".*"))
                );
            }
        }

        /**
         * Add a pattern filter to exclude files that match a specific pattern
         * @param excludePattern The pattern to exclude
         */
        public void addPatternExcludeFilter(String excludePattern) {
            if (excludePattern != null) {
                fileFilters.add(file ->
                        !file.getName().matches(excludePattern.replace("*", ".*"))
                );
            }
        }

        /**
         * Add a custom filter
         * @param customFilter
         */
        public void addCustomFilter(Predicate<File> customFilter) {
            if (customFilter != null) {
                fileFilters.add(customFilter);
            }
        }

        // Get all file filters
        public Set<Predicate<File>> getFileFilters() {
            return fileFilters;
        }
    }

    /**
     * Filter a set of files based on a set of filters
     * @param originalFileSet The original set of files
     * @param applicableFilters The filters to apply
     * @return The filtered set of files
     */
    public Set<File> filterFiles(Set<File> originalFileSet, Filters applicableFilters) {
        Set<File> filteredFiles = new HashSet<>();

        for (File file : originalFileSet) {
            boolean matchesAllFilters = true;
            for (Predicate<File> filter : applicableFilters.getFileFilters()) {
                if (!filter.test(file)) {
                    matchesAllFilters = false;
                    break;
                }
            }
            if (matchesAllFilters) {
                filteredFiles.add(file);
            }
        }

        return filteredFiles;
    }
}

