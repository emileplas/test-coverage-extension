package tech.linebyline.coverage.extension.core.integration;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class to interact with git
 */
public class GitInteractor {

    /**
     * Get an overview of the changed files in the current branch compared to the branchToCompare
     * @param nameOfBranchToCompare the branch to compare the current branch to
     * @return a set of files that have changed
     * @throws RuntimeException
     */
    public static Set<File> getOverviewOfChangedFiles(String nameOfBranchToCompare) throws RuntimeException {
        Set<File> changedFiles = new HashSet<>();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder();
            processBuilder.command("git", "diff", "--name-status", nameOfBranchToCompare);

            Process process = processBuilder.start();

            // Read the output of the command
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Add each changed file (line) to the list
                    changedFiles.add(new File(line.split("\t")[1]));
                }
            }

            int exitCode = process.waitFor();

            if(exitCode != 0){
                String completeError = "";
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        // Add each changed file (line) to the list
                        completeError += line;
                    }
                }
                throw new RuntimeException("Error getting overview of changed files. Exit code: " + completeError);
            }



        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Unable to create list of changed files: " + e);
        }

        return changedFiles;
    }

    /**
     * Returns an overview of the changed lines per file in the current branch compared to the branchToCompare
     * @param branchToCompare the branch to compare the current branch to
     * @return a map with the file as key and an array of changed lines as value. These line numbers need to match with the line numbers that Jacoco uses
     * @throws IOException if the git command fails
     * @throws InterruptedException if the git command fails
     */
    public static HashMap<String, int[]> getChangedLines(String branchToCompare) throws IOException, InterruptedException {
        // Prepare the git diff command to get detailed changes
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("git", "diff", branchToCompare);

        // Start the process
        Process process = processBuilder.start();

        HashMap<String, int[]> changedLinesPerFile = new HashMap<>();

        // Read the output
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            boolean passedFirstFileLine = false;
            boolean passedFirstClassLine = false;
            ArrayList<Integer> lines = null;
            int lineIndex = -2;
            String file = null;
            while ((line = reader.readLine()) != null) {

                if(isLineDiffGitLine(line)){
                    file = line;
                    if(passedFirstClassLine){
                        changedLinesPerFile.put(file, lines.stream().mapToInt(Integer::intValue).toArray());
                    }

                    passedFirstFileLine = false;
                    passedFirstClassLine = false;
                    lines = new ArrayList<>();
                    lineIndex = -2;
                }

                if(line.startsWith("@@") && line.endsWith("@@")){
                    passedFirstFileLine = true;
                    lineIndex = -1;
                }

                if(passedFirstFileLine){
                    lineIndex++;
                }

                if(line.contains("class") && !passedFirstClassLine){
                    passedFirstClassLine = true;
                }

                if(passedFirstClassLine){
                    if(line.startsWith("+")){
                        lines.add(lineIndex);
                    }
                }
            }
        }

        // Wait for the process to complete
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Error executing git command: " + exitCode);
        }

        return changedLinesPerFile;
    }

    private static final String REGEX = "^diff --git a/.* b/.*$";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    /**
     * Check if a line is a git diff line in the format of "diff --git a/... b/..."
     * @param line the line to check
     * @return true if the line is a git diff line, false otherwise
     */
    protected static boolean isLineDiffGitLine(String line){
        Matcher matcher = PATTERN.matcher(line);
        return matcher.matches();
    }
}
