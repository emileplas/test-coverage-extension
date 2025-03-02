// Verify that the build failed
def buildLog = new File(basedir, "build.log")
if (!buildLog.exists()) {
    throw new RuntimeException("Build log not found!")
}

// Check for the expected failure message in the build log
def logContent = buildLog.text
if (!logContent.contains("All changed classes meet the required overall test coverage of 50.00% per class")) {
    throw new RuntimeException("Expected success message not found in the build log!")
}

println "Integration test passed: Build success as expected with the correct message."