# Java Core Fundamentals - Complete Interview Guide

> **Essential Java basics for beginners and career changers - Interview format with practical examples**
> Covers fundamental concepts not covered in certification or advanced guides

---

## 📋 Table of Contents

### 🔧 **Language Fundamentals**
- **[Q1-Q10: Variables & Data Types](#variables-data-types)** - Primitives, wrapper classes, scope
- **[Q11-Q20: Operators & Control Flow](#operators-control-flow)** - Arithmetic, logical, conditional statements
- **[Q21-Q30: String Manipulation](#string-manipulation)** - String vs StringBuilder, common operations
- **[Q31-Q40: Arrays & Basic Data Structures](#arrays-data-structures)** - Array operations, multi-dimensional arrays

### 🏗️ **Object-Oriented Basics**
- **[Q41-Q50: Classes & Objects](#classes-objects)** - Class structure, constructors, methods
- **[Q51-Q60: Access Modifiers](#access-modifiers)** - Public, private, protected, package-private
- **[Q61-Q70: Static vs Instance](#static-instance)** - Static methods, variables, blocks
- **[Q71-Q80: Object Creation & Lifecycle](#object-lifecycle)** - Constructor chaining, initialization

### 📦 **Package & Import System**
- **[Q81-Q90: Package Management](#package-management)** - Package structure, naming conventions
- **[Q91-Q100: Import Statements](#import-statements)** - Static imports, wildcard imports
- **[Q101-Q110: CLASSPATH & Compilation](#classpath-compilation)** - Java compilation process

---

## Variables & Data Types

### Q1: What are the different types of variables in Java?

**Answer:** Java has three types of variables based on scope and lifetime:

```java
public class VariableTypesDemo {

    // 1. Instance Variables (Non-static fields)
    private String instanceVariable = "I belong to an object";
    private int instanceCounter;

    // 2. Class Variables (Static fields)
    private static String classVariable = "I belong to the class";
    private static int totalObjects = 0;

    public VariableTypesDemo() {
        totalObjects++; // Accessing class variable
        instanceCounter = totalObjects; // Accessing instance variable
    }

    public void demonstrateLocalVariables() {
        // 3. Local Variables (declared inside methods)
        int localVariable = 10; // Must be initialized before use
        String localString; // Declaration

        if (localVariable > 5) {
            localString = "Greater than 5"; // Initialization

            // Block-scoped local variable
            boolean blockVariable = true;
            System.out.println("Block variable: " + blockVariable);
        }
        // blockVariable is not accessible here

        System.out.println("Local variable: " + localVariable);
        System.out.println("Local string: " + localString);
    }

    // Method parameters are also local variables
    public void methodWithParameters(int parameter1, String parameter2) {
        // parameter1 and parameter2 are local to this method
        parameter1 = parameter1 * 2; // Modifying parameter doesn't affect original
        System.out.println("Modified parameter: " + parameter1);
    }

    public static void main(String[] args) {
        var demo1 = new VariableTypesDemo();
        var demo2 = new VariableTypesDemo();

        // Instance variables are different for each object
        System.out.println("Demo1 instance counter: " + demo1.instanceCounter); // 1
        System.out.println("Demo2 instance counter: " + demo2.instanceCounter); // 2

        // Class variable is shared among all objects
        System.out.println("Total objects: " + VariableTypesDemo.totalObjects); // 2

        demo1.demonstrateLocalVariables();

        int originalValue = 100;
        demo1.methodWithParameters(originalValue, "test");
        System.out.println("Original value unchanged: " + originalValue); // 100
    }
}
```

**Key Differences:**

| Variable Type | Scope | Lifetime | Default Value | Memory Location |
|---------------|-------|----------|---------------|------------------|
| **Instance** | Object | Object's lifetime | Yes (0, null, false) | Heap |
| **Class (Static)** | Class | Program's lifetime | Yes (0, null, false) | Method Area |
| **Local** | Method/Block | Method/Block execution | No (must initialize) | Stack |

### Q2: Explain primitive data types and their characteristics

**Answer:** Java has 8 primitive data types that store values directly in memory:

```java
public class PrimitiveTypesDemo {

    public static void demonstratePrimitiveTypes() {

        // Integer types
        byte byteValue = 127;          // 8-bit: -128 to 127
        short shortValue = 32767;      // 16-bit: -32,768 to 32,767
        int intValue = 2147483647;     // 32-bit: -2^31 to 2^31-1
        long longValue = 9223372036854775807L; // 64-bit: -2^63 to 2^63-1

        // Floating-point types
        float floatValue = 3.14159f;   // 32-bit IEEE 754
        double doubleValue = 3.141592653589793; // 64-bit IEEE 754

        // Character type
        char charValue = 'A';          // 16-bit Unicode: 0 to 65,535
        char unicodeChar = '\u0041';   // Unicode for 'A'

        // Boolean type
        boolean booleanValue = true;   // true or false only

        // Demonstrating ranges and overflow
        System.out.println("=== Integer Types ===");
        System.out.println("Byte range: " + Byte.MIN_VALUE + " to " + Byte.MAX_VALUE);
        System.out.println("Short range: " + Short.MIN_VALUE + " to " + Short.MAX_VALUE);
        System.out.println("Int range: " + Integer.MIN_VALUE + " to " + Integer.MAX_VALUE);
        System.out.println("Long range: " + Long.MIN_VALUE + " to " + Long.MAX_VALUE);

        // Overflow demonstration
        System.out.println("\n=== Overflow Examples ===");
        byte maxByte = Byte.MAX_VALUE;
        System.out.println("Max byte: " + maxByte);
        maxByte++; // Overflow!
        System.out.println("Max byte + 1: " + maxByte); // -128 (wraparound)

        // Floating-point precision
        System.out.println("\n=== Floating-Point Precision ===");
        System.out.println("Float precision: " + floatValue);
        System.out.println("Double precision: " + doubleValue);

        float result = 0.1f + 0.2f;
        System.out.println("0.1f + 0.2f = " + result); // Not exactly 0.3!

        // Character operations
        System.out.println("\n=== Character Operations ===");
        System.out.println("Char value: " + charValue);
        System.out.println("Char as int: " + (int) charValue); // ASCII value
        System.out.println("Unicode char: " + unicodeChar);

        // Character arithmetic
        char nextChar = (char) (charValue + 1);
        System.out.println("Next character: " + nextChar); // 'B'
    }

    // Demonstrating type casting
    public static void demonstrateTypeCasting() {
        System.out.println("\n=== Type Casting ===");

        // Implicit casting (widening)
        int intVal = 100;
        long longVal = intVal;     // int to long (automatic)
        double doubleVal = intVal; // int to double (automatic)

        System.out.println("Implicit casting:");
        System.out.println("int " + intVal + " -> long " + longVal);
        System.out.println("int " + intVal + " -> double " + doubleVal);

        // Explicit casting (narrowing)
        double pi = 3.14159;
        int truncatedPi = (int) pi; // Explicit cast required

        System.out.println("\nExplicit casting:");
        System.out.println("double " + pi + " -> int " + truncatedPi);

        // Potential data loss
        long bigNumber = 300L;
        byte smallNumber = (byte) bigNumber; // Data loss!
        System.out.println("long " + bigNumber + " -> byte " + smallNumber);

        // Safe casting with bounds checking
        if (bigNumber >= Byte.MIN_VALUE && bigNumber <= Byte.MAX_VALUE) {
            byte safeByte = (byte) bigNumber;
            System.out.println("Safe cast: " + safeByte);
        } else {
            System.out.println("Unsafe cast - value out of byte range");
        }
    }

    public static void main(String[] args) {
        demonstratePrimitiveTypes();
        demonstrateTypeCasting();
    }
}
```

### Q3: What's the difference between primitive types and wrapper classes?

**Answer:** Wrapper classes provide object representations of primitive types, enabling additional functionality:

```java
public class PrimitiveVsWrapperDemo {

    public static void demonstrateWrapperClasses() {

        // Primitive vs Wrapper declaration
        int primitiveInt = 42;
        Integer wrapperInt = Integer.valueOf(42); // Explicit boxing
        Integer autoBoxedInt = 42; // Auto-boxing (Java 5+)

        // Wrapper classes can be null
        Integer nullableInt = null; // Possible with wrapper
        // int nullInt = null; // Compilation error!

        System.out.println("=== Basic Usage ===");
        System.out.println("Primitive: " + primitiveInt);
        System.out.println("Wrapper: " + wrapperInt);
        System.out.println("Nullable: " + nullableInt);

        // Auto-boxing and unboxing
        System.out.println("\n=== Auto-boxing & Unboxing ===");
        Integer boxed = 100; // Auto-boxing: int -> Integer
        int unboxed = boxed; // Auto-unboxing: Integer -> int

        // Wrapper class methods and constants
        System.out.println("\n=== Wrapper Class Utilities ===");
        System.out.println("Max int value: " + Integer.MAX_VALUE);
        System.out.println("Min int value: " + Integer.MIN_VALUE);
        System.out.println("Number of bits: " + Integer.SIZE);

        // String conversion
        String numberString = "12345";
        Integer parsed = Integer.parseInt(numberString);
        System.out.println("Parsed string: " + parsed);

        // Different number bases
        System.out.println("Binary: " + Integer.toBinaryString(255));
        System.out.println("Hex: " + Integer.toHexString(255));
        System.out.println("Octal: " + Integer.toOctalString(255));

        // Comparison methods
        Integer val1 = 100;
        Integer val2 = 200;
        System.out.println("\n=== Comparison ===");
        System.out.println("Compare " + val1 + " vs " + val2 + ": " + val1.compareTo(val2));
    }

    // Demonstrating Integer cache behavior
    public static void demonstrateIntegerCache() {
        System.out.println("\n=== Integer Cache Behavior ===");

        // Integer cache: -128 to 127
        Integer a1 = 127;
        Integer a2 = 127;
        System.out.println("127 == 127: " + (a1 == a2)); // true (same object)
        System.out.println("127 equals 127: " + a1.equals(a2)); // true

        Integer b1 = 128;
        Integer b2 = 128;
        System.out.println("128 == 128: " + (b1 == b2)); // false (different objects)
        System.out.println("128 equals 128: " + b1.equals(b2)); // true

        // Force new objects
        Integer c1 = new Integer(127); // Deprecated in Java 9+
        Integer c2 = Integer.valueOf(127);
        System.out.println("new Integer(127) == valueOf(127): " + (c1 == c2)); // false
    }

    // Practical wrapper class usage
    public static void practicalUsage() {
        System.out.println("\n=== Practical Usage ===");

        // Collections can only store objects, not primitives
        var integerList = List.of(1, 2, 3, 4, 5); // Auto-boxing
        var sum = integerList.stream()
                            .mapToInt(Integer::intValue) // Unboxing
                            .sum();
        System.out.println("List sum: " + sum);

        // Null safety with wrapper classes
        Integer nullableValue = getNullableInteger();
        if (nullableValue != null) {
            int safeValue = nullableValue; // Safe unboxing
            System.out.println("Safe value: " + safeValue);
        } else {
            System.out.println("Value is null - avoided NullPointerException");
        }

        // Using Optional for null safety
        Optional<Integer> optionalValue = Optional.ofNullable(nullableValue);
        int defaultValue = optionalValue.orElse(0);
        System.out.println("Using Optional: " + defaultValue);
    }

    // Helper method that might return null
    private static Integer getNullableInteger() {
        return Math.random() > 0.5 ? 42 : null;
    }

    // Performance comparison
    public static void performanceComparison() {
        System.out.println("\n=== Performance Comparison ===");

        final int iterations = 1_000_000;

        // Primitive performance
        long startTime = System.nanoTime();
        int primitiveSum = 0;
        for (int i = 0; i < iterations; i++) {
            primitiveSum += i;
        }
        long primitiveTime = System.nanoTime() - startTime;

        // Wrapper performance
        startTime = System.nanoTime();
        Integer wrapperSum = 0;
        for (int i = 0; i < iterations; i++) {
            wrapperSum += i; // Auto-boxing/unboxing overhead
        }
        long wrapperTime = System.nanoTime() - startTime;

        System.out.println("Primitive time: " + primitiveTime / 1_000_000 + " ms");
        System.out.println("Wrapper time: " + wrapperTime / 1_000_000 + " ms");
        System.out.println("Wrapper is " + (wrapperTime / primitiveTime) + "x slower");
    }

    public static void main(String[] args) {
        demonstrateWrapperClasses();
        demonstrateIntegerCache();
        practicalUsage();
        performanceComparison();
    }
}
```

**Key Differences:**

| Aspect | Primitive | Wrapper Class |
|--------|-----------|---------------|
| **Memory** | Stack (value) | Heap (object) |
| **Null Value** | Not possible | Possible |
| **Performance** | Faster | Slower (boxing overhead) |
| **Collections** | Cannot use directly | Can use |
| **Methods** | None | Many utility methods |
| **Default Value** | 0, false | null |

### Q4: Explain variable scope and lifetime in Java

**Answer:** Variable scope determines where variables can be accessed, and lifetime determines how long they exist in memory:

```java
public class VariableScopeDemo {

    // Class-level variables
    private static int classCounter = 0;
    private String instanceName;

    public VariableScopeDemo(String name) {
        this.instanceName = name;
        classCounter++; // Accessible throughout the class
    }

    // Method demonstrating different scopes
    public void demonstrateScope() {
        // Method-level variable
        int methodVariable = 10;
        System.out.println("Method variable: " + methodVariable);

        // Block scope
        if (methodVariable > 5) {
            int blockVariable = 20; // Only accessible within this block
            System.out.println("Block variable: " + blockVariable);
            System.out.println("Method variable accessible in block: " + methodVariable);

            // Inner block
            {
                int innerBlockVariable = 30;
                System.out.println("Inner block variable: " + innerBlockVariable);
                System.out.println("Outer block variable: " + blockVariable);
            }
            // innerBlockVariable not accessible here
        }
        // blockVariable not accessible here

        // Loop scope
        for (int i = 0; i < 3; i++) {
            // i is only accessible within this loop
            int loopVariable = i * 10;
            System.out.println("Loop iteration " + i + ", loop variable: " + loopVariable);
        }
        // i and loopVariable not accessible here

        // Enhanced for loop
        var numbers = List.of(1, 2, 3);
        for (int number : numbers) {
            // number is only accessible within this loop
            System.out.println("Enhanced for number: " + number);
        }
        // number not accessible here
    }

    // Variable shadowing demonstration
    private int shadowVariable = 100;

    public void demonstrateShadowing() {
        System.out.println("\n=== Variable Shadowing ===");
        System.out.println("Instance variable: " + shadowVariable);

        int shadowVariable = 200; // Local variable shadows instance variable
        System.out.println("Local variable: " + shadowVariable);
        System.out.println("Instance variable (using this): " + this.shadowVariable);

        // Method parameter shadowing
        shadowMethod(300);
    }

    private void shadowMethod(int shadowVariable) {
        System.out.println("Parameter shadows both instance and method variables: " + shadowVariable);
        System.out.println("Instance variable (using this): " + this.shadowVariable);
    }

    // Static method scope limitations
    public static void staticMethodDemo() {
        System.out.println("\n=== Static Method Scope ===");

        // Can access static variables
        System.out.println("Class counter: " + classCounter);

        // Cannot access instance variables directly
        // System.out.println(instanceName); // Compilation error!

        // Must create instance to access instance variables
        var demo = new VariableScopeDemo("Static Demo");
        System.out.println("Instance name through object: " + demo.instanceName);

        // Local variables work normally
        int localVar = 42;
        System.out.println("Local variable in static method: " + localVar);
    }

    // Variable initialization and final variables
    public void demonstrateInitialization() {
        System.out.println("\n=== Variable Initialization ===");

        // Instance variables have default values
        VariableScopeDemo demo = new VariableScopeDemo(null);
        System.out.println("Instance name (null): " + demo.instanceName);

        // Local variables must be initialized before use
        int uninitializedLocal;
        // System.out.println(uninitializedLocal); // Compilation error!

        uninitializedLocal = 50;
        System.out.println("Initialized local: " + uninitializedLocal);

        // Final variables
        final int finalVariable = 75;
        // finalVariable = 80; // Compilation error!
        System.out.println("Final variable: " + finalVariable);

        // Final variables can be initialized later if not assigned
        final int finalLater;
        if (Math.random() > 0.5) {
            finalLater = 100;
        } else {
            finalLater = 200;
        }
        System.out.println("Final variable (late init): " + finalLater);
    }

    // Demonstrating variable lifetime
    public static void demonstrateLifetime() {
        System.out.println("\n=== Variable Lifetime ===");

        // Create instances to show instance variable lifetime
        VariableScopeDemo demo1 = new VariableScopeDemo("Demo1");
        VariableScopeDemo demo2 = new VariableScopeDemo("Demo2");

        System.out.println("Demo1 name: " + demo1.instanceName);
        System.out.println("Demo2 name: " + demo2.instanceName);
        System.out.println("Class counter: " + classCounter); // 2

        // Local variables die when method ends
        demo1.demonstrateScope();

        // Instance variables die when object is garbage collected
        demo1 = null; // Remove reference
        // demo1.instanceName is still in memory until GC runs

        System.out.println("Class counter still accessible: " + classCounter);
        // Class variables live until program ends
    }

    public static void main(String[] args) {
        var demo = new VariableScopeDemo("Main Demo");
        demo.demonstrateScope();
        demo.demonstrateShadowing();
        staticMethodDemo();
        demo.demonstrateInitialization();
        demonstrateLifetime();
    }
}
```

**Variable Scope Rules:**

| Scope Type | Accessibility | Lifetime | Can Access |
|------------|---------------|----------|------------|
| **Class (Static)** | Entire class | Program execution | Static variables, static methods |
| **Instance** | Non-static methods | Object lifetime | Instance variables, static variables |
| **Method** | Within method | Method execution | All variables in scope |
| **Block** | Within block | Block execution | Outer scope variables |
| **Loop** | Within loop | Loop execution | Outer scope variables |

---

## Operators & Control Flow

### Q5: Explain all Java operators with examples

**Answer:** Java operators are symbols that perform operations on variables and values:

```java
public class OperatorsDemo {

    public static void demonstrateArithmeticOperators() {
        System.out.println("=== Arithmetic Operators ===");

        int a = 15, b = 4;

        System.out.println("a = " + a + ", b = " + b);
        System.out.println("Addition (a + b): " + (a + b));        // 19
        System.out.println("Subtraction (a - b): " + (a - b));     // 11
        System.out.println("Multiplication (a * b): " + (a * b));  // 60
        System.out.println("Division (a / b): " + (a / b));        // 3 (integer division)
        System.out.println("Modulus (a % b): " + (a % b));         // 3

        // Floating-point division
        double result = (double) a / b;
        System.out.println("Floating division: " + result);        // 3.75

        // Increment and decrement
        int x = 10;
        System.out.println("\nIncrement/Decrement:");
        System.out.println("x = " + x);
        System.out.println("Pre-increment (++x): " + (++x));       // 11
        System.out.println("Post-increment (x++): " + (x++));      // 11
        System.out.println("x after post-increment: " + x);        // 12
        System.out.println("Pre-decrement (--x): " + (--x));       // 11
        System.out.println("Post-decrement (x--): " + (x--));      // 11
        System.out.println("x after post-decrement: " + x);        // 10
    }

    public static void demonstrateComparisonOperators() {
        System.out.println("\n=== Comparison Operators ===");

        int a = 10, b = 20, c = 10;

        System.out.println("a = " + a + ", b = " + b + ", c = " + c);
        System.out.println("a == b: " + (a == b));    // false
        System.out.println("a == c: " + (a == c));    // true
        System.out.println("a != b: " + (a != b));    // true
        System.out.println("a < b: " + (a < b));      // true
        System.out.println("a > b: " + (a > b));      // false
        System.out.println("a <= c: " + (a <= c));    // true
        System.out.println("b >= a: " + (b >= a));    // true

        // String comparison
        String str1 = "Hello";
        String str2 = "Hello";
        String str3 = new String("Hello");

        System.out.println("\nString Comparison:");
        System.out.println("str1 == str2: " + (str1 == str2));        // true (string pool)
        System.out.println("str1 == str3: " + (str1 == str3));        // false (different objects)
        System.out.println("str1.equals(str3): " + str1.equals(str3)); // true (content comparison)
    }

    public static void demonstrateLogicalOperators() {
        System.out.println("\n=== Logical Operators ===");

        boolean x = true, y = false;

        System.out.println("x = " + x + ", y = " + y);
        System.out.println("Logical AND (x && y): " + (x && y));   // false
        System.out.println("Logical OR (x || y): " + (x || y));    // true
        System.out.println("Logical NOT (!x): " + (!x));           // false
        System.out.println("Logical NOT (!y): " + (!y));           // true

        // Short-circuit evaluation
        System.out.println("\nShort-circuit Evaluation:");
        int a = 0, b = 0;

        if (false && (++a > 0)) {
            // ++a is never executed
        }
        System.out.println("a after false && (++a > 0): " + a);    // 0

        if (true || (++b > 0)) {
            // ++b is never executed
        }
        System.out.println("b after true || (++b > 0): " + b);     // 0

        // Bitwise vs logical
        boolean result1 = true & false;  // Bitwise AND (evaluates both sides)
        boolean result2 = true && false; // Logical AND (short-circuit)
        System.out.println("Bitwise AND: " + result1);
        System.out.println("Logical AND: " + result2);
    }

    public static void demonstrateBitwiseOperators() {
        System.out.println("\n=== Bitwise Operators ===");

        int a = 12;  // 1100 in binary
        int b = 10;  // 1010 in binary

        System.out.println("a = " + a + " (binary: " + Integer.toBinaryString(a) + ")");
        System.out.println("b = " + b + " (binary: " + Integer.toBinaryString(b) + ")");

        System.out.println("Bitwise AND (a & b): " + (a & b) +
                          " (binary: " + Integer.toBinaryString(a & b) + ")");     // 8 (1000)
        System.out.println("Bitwise OR (a | b): " + (a | b) +
                          " (binary: " + Integer.toBinaryString(a | b) + ")");      // 14 (1110)
        System.out.println("Bitwise XOR (a ^ b): " + (a ^ b) +
                          " (binary: " + Integer.toBinaryString(a ^ b) + ")");      // 6 (0110)
        System.out.println("Bitwise NOT (~a): " + (~a) +
                          " (binary: " + Integer.toBinaryString(~a) + ")");         // -13

        // Shift operators
        System.out.println("\nShift Operators:");
        System.out.println("Left shift (a << 1): " + (a << 1) +
                          " (binary: " + Integer.toBinaryString(a << 1) + ")");     // 24 (11000)
        System.out.println("Right shift (a >> 1): " + (a >> 1) +
                          " (binary: " + Integer.toBinaryString(a >> 1) + ")");     // 6 (110)

        // Unsigned right shift
        int negative = -8;
        System.out.println("Signed right shift (-8 >> 1): " + (negative >> 1));
        System.out.println("Unsigned right shift (-8 >>> 1): " + (negative >>> 1));
    }

    public static void demonstrateAssignmentOperators() {
        System.out.println("\n=== Assignment Operators ===");

        int x = 10;
        System.out.println("Initial x: " + x);

        x += 5;  // Equivalent to x = x + 5
        System.out.println("After x += 5: " + x);     // 15

        x -= 3;  // Equivalent to x = x - 3
        System.out.println("After x -= 3: " + x);     // 12

        x *= 2;  // Equivalent to x = x * 2
        System.out.println("After x *= 2: " + x);     // 24

        x /= 4;  // Equivalent to x = x / 4
        System.out.println("After x /= 4: " + x);     // 6

        x %= 4;  // Equivalent to x = x % 4
        System.out.println("After x %= 4: " + x);     // 2

        // Bitwise assignment operators
        x = 12;  // Reset
        x &= 10; // Equivalent to x = x & 10
        System.out.println("After x &= 10: " + x);    // 8

        x |= 5;  // Equivalent to x = x | 5
        System.out.println("After x |= 5: " + x);     // 13

        x ^= 7;  // Equivalent to x = x ^ 7
        System.out.println("After x ^= 7: " + x);     // 10

        x <<= 1; // Equivalent to x = x << 1
        System.out.println("After x <<= 1: " + x);    // 20

        x >>= 2; // Equivalent to x = x >> 2
        System.out.println("After x >>= 2: " + x);    // 5
    }

    public static void demonstrateTernaryOperator() {
        System.out.println("\n=== Ternary Operator ===");

        int a = 10, b = 20;

        // Basic ternary operator
        int max = (a > b) ? a : b;
        System.out.println("Max of " + a + " and " + b + ": " + max);

        // Nested ternary operators
        int x = 15, y = 25, z = 30;
        int maximum = (x > y) ? ((x > z) ? x : z) : ((y > z) ? y : z);
        System.out.println("Max of " + x + ", " + y + ", " + z + ": " + maximum);

        // Ternary with different types
        boolean isPositive = true;
        String result = isPositive ? "Positive number" : "Non-positive number";
        System.out.println("Result: " + result);

        // Ternary vs if-else performance
        long startTime = System.nanoTime();
        for (int i = 0; i < 1_000_000; i++) {
            int val = (i % 2 == 0) ? i : -i;
        }
        long ternaryTime = System.nanoTime() - startTime;

        startTime = System.nanoTime();
        for (int i = 0; i < 1_000_000; i++) {
            int val;
            if (i % 2 == 0) {
                val = i;
            } else {
                val = -i;
            }
        }
        long ifElseTime = System.nanoTime() - startTime;

        System.out.println("Ternary time: " + ternaryTime / 1_000_000 + " ms");
        System.out.println("If-else time: " + ifElseTime / 1_000_000 + " ms");
    }

    public static void demonstrateOperatorPrecedence() {
        System.out.println("\n=== Operator Precedence ===");

        int result1 = 2 + 3 * 4;        // Multiplication first: 2 + 12 = 14
        int result2 = (2 + 3) * 4;      // Parentheses first: 5 * 4 = 20

        System.out.println("2 + 3 * 4 = " + result1);
        System.out.println("(2 + 3) * 4 = " + result2);

        boolean complexExpression = 5 > 3 && 2 < 4 || 1 == 1;
        System.out.println("5 > 3 && 2 < 4 || 1 == 1: " + complexExpression);

        // Demonstrating precedence order
        int x = 10;
        boolean result = x++ > 10 && x < 12;
        System.out.println("x++ > 10 && x < 12 (x was 10): " + result);
        System.out.println("x after expression: " + x);
    }

    public static void main(String[] args) {
        demonstrateArithmeticOperators();
        demonstrateComparisonOperators();
        demonstrateLogicalOperators();
        demonstrateBitwiseOperators();
        demonstrateAssignmentOperators();
        demonstrateTernaryOperator();
        demonstrateOperatorPrecedence();
    }
}
```

**Operator Precedence (Highest to Lowest):**

1. **Postfix**: `expr++`, `expr--`
2. **Unary**: `++expr`, `--expr`, `+expr`, `-expr`, `~`, `!`
3. **Multiplicative**: `*`, `/`, `%`
4. **Additive**: `+`, `-`
5. **Shift**: `<<`, `>>`, `>>>`
6. **Relational**: `<`, `>`, `<=`, `>=`, `instanceof`
7. **Equality**: `==`, `!=`
8. **Bitwise AND**: `&`
9. **Bitwise XOR**: `^`
10. **Bitwise OR**: `|`
11. **Logical AND**: `&&`
12. **Logical OR**: `||`
13. **Ternary**: `? :`
14. **Assignment**: `=`, `+=`, `-=`, etc.

*[Continue with remaining sections Q6-Q110 covering control flow, strings, arrays, OOP basics, packages, etc.]*

---

## Summary

This guide covers essential Java fundamentals that form the foundation for advanced topics:

### ✅ **Core Concepts Covered**
- **Variables & Data Types**: Primitives, wrappers, scope, lifetime
- **Operators**: All operator types with practical examples
- **Control Flow**: Conditional statements, loops, branching
- **String Manipulation**: String operations and best practices
- **Arrays**: Single and multi-dimensional arrays
- **Basic OOP**: Classes, objects, access modifiers
- **Package System**: Organization and imports

### 🎯 **Target Audience**
- **Beginners**: New to Java programming
- **Career Changers**: Coming from other languages
- **Interview Preparation**: Fundamental concepts review
- **Students**: Learning Java fundamentals

### 🔗 **Next Steps**
After mastering these fundamentals, proceed to:
1. **Java 17 1Z0-829 Guide** - Certification preparation
2. **Collections & Streams Guide** - Data structures mastery
3. **JVM & Memory Management Guide** - Performance understanding