// Verify that the build failed
def buildLog = new File(basedir, "build.log")
if (!buildLog.exists()) {
    throw new RuntimeException("Build log not found!")
}

// Check for the expected failure message in the build log
def logContent = buildLog.text
if (!logContent.contains("[ERROR] The changed lines do not meet the required coverage of 80.00% per class: \n" +
        "[ERROR] The following classes were changed but those changes are not sufficently covered: \n" +
        "[ERROR] /Users/emileplas/Plas Advocaten Dropbox/Emile Plas/coderen/test-coverage-extension/maven-plugin/target/it/changed-lines-per-class-failure/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java with a coverage of for the changed lines of 50.00%\n" +
        "[ERROR] /Users/emileplas/Plas Advocaten Dropbox/Emile Plas/coderen/test-coverage-extension/maven-plugin/target/it/changed-lines-per-class-failure/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java with a coverage of for the changed lines of 40.00%\n" +
        "[ERROR] The following classes are sufficently covered: \n" +
        "[ERROR] None")) {
    throw new RuntimeException("Expected failure message not found in the build log!")
}

println "Integration test passed: Build failed as expected with the correct message."