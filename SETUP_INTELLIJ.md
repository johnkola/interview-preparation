# IntelliJ IDEA Setup Guide for Java Interview Project

## Quick Setup Steps

### 1. Open Project in IntelliJ
1. Open IntelliJ IDEA
2. Click "Open" or "Open or Import"
3. Navigate to your project folder: `/mnt/c/IdeaProjects/JavaInterview`
4. Select the folder and click "OK"
5. IntelliJ should automatically detect it as a Gradle project

### 2. Configure Gradle Import
When IntelliJ opens the project:
1. A popup should appear asking about Gradle import
2. Click "Import Gradle Project"
3. In the import dialog:
   - **Use Gradle from**: `gradle-wrapper.properties file`
   - **Gradle JVM**: Select Java 17 (corretto-17 or any JDK 17)
   - **Build and run using**: Gradle
   - **Run tests using**: Gradle
4. Click "OK"

### 3. Verify Project Structure
After import, your project structure should look like:
```
JavaInterview/
├── src/
│   ├── main/java/com/interview/
│   └── test/java/com/interview/
├── build.gradle
├── settings.gradle
└── gradle.properties
```

### 4. Configure Java SDK (if needed)
1. Go to `File` → `Project Structure` (Ctrl+Alt+Shift+S)
2. Under `Project Settings` → `Project`:
   - **Project SDK**: Select Java 17
   - **Project language level**: 17
3. Under `Project Settings` → `Modules`:
   - Verify the module is using Java 17
4. Click "Apply" and "OK"

## Running Examples

### Method 1: Use Pre-configured Run Configurations
IntelliJ should show these run configurations in the dropdown:
- **Run Interactive Menu** - Main interactive menu
- **Advanced Executors** - Advanced executor examples
- **Thread Safe Counters** - Thread safety examples
- **Command Pattern** - Command pattern examples

### Method 2: Use Gradle Tasks
1. Open the Gradle tool window: `View` → `Tool Windows` → `Gradle`
2. Navigate to: `Tasks` → `interview-examples`
3. Double-click any task to run:
   - `runInteractiveMenu`
   - `runAdvancedExecutors`
   - `runThreadCreation`
   - etc.

### Method 3: Use Terminal
Open the built-in terminal (`View` → `Tool Windows` → `Terminal`) and run:
```bash
./gradlew runInteractiveMenu
./gradlew runAdvancedExecutors
./gradlew listExamples
```

### Method 4: Right-click Main Methods
1. Navigate to any class with a `main` method
2. Right-click on the class file
3. Select "Run 'ClassName.main()'"

## Troubleshooting

### Problem: "Project SDK is not defined"
**Solution:**
1. Go to `File` → `Project Structure`
2. Select `Project` on the left
3. Choose your Java 17 SDK from the dropdown
4. If not available, click "Add SDK" → "Download JDK" → Select version 17

### Problem: "Cannot resolve symbol" errors
**Solution:**
1. Try `File` → `Invalidate Caches and Restart`
2. Or refresh Gradle: Open Gradle tool window → Click refresh icon
3. Or re-import: `File` → `New` → `Project from Existing Sources`

### Problem: Gradle import fails
**Solution:**
1. Make sure you have internet connection (Gradle needs to download dependencies)
2. Check that Java 17 is installed: `java -version`
3. Try: `./gradlew clean build` in terminal
4. If still fails, delete `.gradle` folder and re-import

### Problem: "Execution failed for task ':compileJava'"
**Solution:**
1. Verify you're using Java 17
2. Check `build.gradle` has correct Java version settings
3. Run `./gradlew clean build` in terminal

## Testing the Setup

Run this command to verify everything works:
```bash
./gradlew build runHelper
```

This should:
1. Compile all Java files
2. Run tests
3. Show the help menu with available examples

## Available Examples

Use `./gradlew listExamples` to see all available examples, or run:

**Quick Examples:**
- `./gradlew runInteractiveMenu` - Interactive menu for all examples
- `./gradlew runAdvancedExecutors` - Advanced executor and futures examples
- `./gradlew runThreadSafeCounters` - Thread safety demonstrations
- `./gradlew runExample -Pexample=threads` - Run specific example by name

## IDE Features

### Code Navigation
- **Ctrl+Click** on any class/method to navigate
- **Ctrl+Shift+N** to find files
- **Ctrl+N** to find classes

### Debugging
- Set breakpoints by clicking in the gutter
- Use "Debug" instead of "Run" for any configuration
- Step through concurrency examples to see thread behavior

### Gradle Integration
- Build: `Build` → `Build Project`
- Clean: Use Gradle tool window → `Tasks` → `build` → `clean`
- Dependencies: View in Gradle tool window → `Dependencies`

Your project is now ready for Java interview practice with full IntelliJ IDEA integration!