// Verify that the build failed
def buildLog = new File(basedir, "build.log")
if (!buildLog.exists()) {
    throw new RuntimeException("Build log not found!")
}

// Check for the expected failure message in the build log
def logContent = buildLog.text
if (!logContent.contains("The overall coverage is below the required percentage. Required: 80.00% Actual: 66.67%")) {
    throw new RuntimeException("Expected failure message not found in the build log!")
}

println "Integration test passed: Build failed as expected with the correct message."