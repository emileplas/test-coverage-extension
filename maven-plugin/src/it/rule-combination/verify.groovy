// Verify that the build failed
def buildLog = new File(basedir, "build.log")
if (!buildLog.exists()) {
    throw new RuntimeException("Build log not found!")
}

// Check for the expected failure message in the build log -> order not fixed
def logContent = buildLog.text
if (!logContent.contains("[ERROR] The overall coverage of the changed lines is below the required percentage. Required: 100.00% Actual: 42.86%\n")) {
    throw new RuntimeException("Expected failure message not found in the build log!")
}

if (!logContent.contains("[ERROR] --------------------------------------------------\n")) {
    throw new RuntimeException("Expected failure line break not found in the build log!")
}

if (!logContent.contains("[ERROR] The overall coverage is below the required percentage. Required: 80.00% Actual: 66.67%\n")) {
    throw new RuntimeException("Expected failure message not found in the build log!")
}

println "Integration test passed: Build failure as expected with the correct message."