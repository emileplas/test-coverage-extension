// Verify that the build failed
def buildLog = new File(basedir, "build.log")
if (!buildLog.exists()) {
    throw new RuntimeException("Build log not found!")
}

// Define the expected class names and coverage percentages
def expectedClasses = [
        "src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java": 70.00,
        "src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java": 60.00
]

// Construct the expected failure message dynamically
def expectedMessage = new StringBuilder()
expectedMessage.append("[ERROR] The changed classes do not meet the overall required coverage of 80.00%: \n")
expectedMessage.append("[ERROR] The following classes are not sufficiently covered: \n")

expectedClasses.each { classPath, coverage ->
    def fullPath = new File(basedir, classPath).canonicalPath
    expectedMessage.append("[ERROR] ${fullPath} with an overall coverage of ${coverage}%\n")
}

expectedMessage.append("[ERROR] The following classes are sufficiently covered: \n")
expectedMessage.append("[ERROR] None")

// Check for the expected failure message in the build log
def logContent = buildLog.text
if (!logContent.contains(expectedMessage.toString())) {
    throw new RuntimeException("Expected failure message not found in the build log!")
}

println "Integration test passed: Build failed as expected with the correct message."