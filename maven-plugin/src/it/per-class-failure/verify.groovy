// Verify that the build failed
def buildLog = new File(basedir, "build.log")
if (!buildLog.exists()) {
    throw new RuntimeException("Build log not found!")
}

// Check for the expected failure message in the build log
def logContent = buildLog.text
if (!logContent.contains("[ERROR] The changed classes do not meet the overall required coverage of 80.00%: \n" +
        "[ERROR] The following classes are not sufficiently covered: \n" +
        "[ERROR] /Users/emileplas/Plas Advocaten Dropbox/Emile Plas/coderen/test-coverage-extension/maven-plugin/target/it/per-class-failure/src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java with an overall coverage of 70.00%\n" +
        "[ERROR] /Users/emileplas/Plas Advocaten Dropbox/Emile Plas/coderen/test-coverage-extension/maven-plugin/target/it/per-class-failure/src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java with an overall coverage of 60.00%\n" +
        "[ERROR] The following classes are sufficiently covered: \n" +
        "[ERROR] None")) {
    throw new RuntimeException("Expected failure message not found in the build log!")
}

println "Integration test passed: Build failed as expected with the correct message."