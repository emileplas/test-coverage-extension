package com.brabel.coverage.extension.core;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

public class FileUtil {

    public static class Filters {
        private Set<Predicate<File>> fileFilters = new HashSet<>();

        // Method to add an include path filter
        public void addPathIncludeFilter(String includePath) {
            if (includePath != null) {
                fileFilters.add(file ->
                        file.getParentFile() != null &&
                                file.getParentFile().getAbsolutePath().contains(includePath)
                );
            }
        }

        // Method to add an exclude path filter
        public void addPathExcludeFilter(String excludePath) {
            if (excludePath != null) {
                fileFilters.add(file ->
                        file.getParentFile() == null ||
                                !file.getParentFile().getAbsolutePath().contains(excludePath)
                );
            }
        }

        // Method to add an include pattern filter
        public void addPatternIncludeFilter(String includePattern) {
            if (includePattern != null) {
                fileFilters.add(file ->
                        file.getName().matches(includePattern.replace("*", ".*"))
                );
            }
        }

        // Method to add an exclude pattern filter
        public void addPatternExcludeFilter(String excludePattern) {
            if (excludePattern != null) {
                fileFilters.add(file ->
                        !file.getName().matches(excludePattern.replace("*", ".*"))
                );
            }
        }

        // Allows adding custom predicates for extensibility
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

