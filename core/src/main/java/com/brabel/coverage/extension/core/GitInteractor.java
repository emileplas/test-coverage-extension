package com.brabel.coverage.extension.core;

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

public class GitInteractor {

    public static Set<File> getOverviewOfChangedFiles(String nameOfBranchToCompare) throws RuntimeException {
        Set<File> changedFiles = new HashSet<>();

        try {
            //String currentBranch = executeGitCommand("rev-parse --abbrev-ref HEAD");

            //String baseBranch = nameOfBranchToCompare;
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

    //TODO: implement
    private static String getBaseBranch(String currentBranch) {
        throw new RuntimeException("Not implemented");
    }

    private static String executeGitCommand(String command) throws IOException, InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder("git", command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }

        process.waitFor(); // Wait for the process to finish
        return output.toString().trim(); // Return the output as a string
    }


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

                System.out.println(line);

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


                //System.out.println(line); // Output the detailed changes
            }
        }

        // Wait for the process to complete
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            System.err.println("Error executing git command: " + exitCode);
        }

        return changedLinesPerFile;
    }

    private static final String REGEX = "^diff --git a/.* b/.*$";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

    protected static boolean isLineDiffGitLine(String line){
        Matcher matcher = PATTERN.matcher(line);
        return matcher.matches();
    }
}
