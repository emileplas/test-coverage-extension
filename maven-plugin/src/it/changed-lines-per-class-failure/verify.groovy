// Verify that the build failed
def buildLog = new File(basedir, "build.log")
if (!buildLog.exists()) {
    throw new RuntimeException("Build log not found!")
}

// Define the expected class names and their changed lines coverage percentages
def expectedClasses = [
        "src/main/java/com/brabel/coverage/extension/single/module/sample/FirstExampleClass.java": 50.00,
        "src/main/java/com/brabel/coverage/extension/single/module/sample/SecondExampleClass.java": 40.00
]

// Construct the expected failure message dynamically
def expectedMessage = new StringBuilder()
expectedMessage.append("[ERROR] The changed lines do not meet the required coverage of 80.00% per class: \n")
expectedMessage.append("[ERROR] The following classes were changed but those changes are not sufficiently covered: \n")

expectedClasses.each { classPath, coverage ->
    def fullPath = new File(basedir, classPath).canonicalPath
    expectedMessage.append("[ERROR] ${fullPath} with a coverage of the changed lines of ${coverage}%\n")
}

expectedMessage.append("[ERROR] The following classes are sufficiently covered: \n")
expectedMessage.append("[ERROR] None")

// Check for the expected failure message in the build log
def logContent = buildLog.text

if (!logContent.contains(expectedMessage.toString())) {
    print expectedMessage.toString()
    throw new RuntimeException("Expected failure message not found in the build log!\n " + logContent)

}

println "Integration test passed: Build failed as expected with the correct message."