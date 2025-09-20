# Java JVM & Memory Management - Complete Interview Guide

> **Comprehensive guide to JVM internals, memory management, and performance tuning for advanced Java developers**
> Critical knowledge for senior developers, performance engineers, and system architects

---

## 📋 Table of Contents

### 🏗️ **JVM Architecture & Internals**
- **[Q1-Q10: JVM Overview](#jvm-overview)** - JVM components, execution process, bytecode
- **[Q11-Q20: Memory Areas](#memory-areas)** - Heap, stack, method area, PC register
- **[Q21-Q30: ClassLoader Mechanism](#classloader-mechanism)** - Loading, linking, initialization
- **[Q31-Q40: Execution Engine](#execution-engine)** - Interpreter, JIT compiler, hotspot

### 🗑️ **Garbage Collection Deep Dive**
- **[Q41-Q50: GC Fundamentals](#gc-fundamentals)** - GC algorithms, generational hypothesis
- **[Q51-Q60: GC Algorithms](#gc-algorithms)** - Serial, Parallel, G1, ZGC, Shenandoah
- **[Q61-Q70: GC Tuning](#gc-tuning)** - Parameters, monitoring, optimization
- **[Q71-Q80: Memory Leaks](#memory-leaks)** - Detection, analysis, prevention

### ⚡ **Performance & Optimization**
- **[Q81-Q90: Memory Optimization](#memory-optimization)** - Object lifecycle, memory-efficient code
- **[Q91-Q100: JVM Tuning](#jvm-tuning)** - Heap sizing, GC configuration, performance monitoring
- **[Q101-Q110: Profiling & Monitoring](#profiling-monitoring)** - Tools, metrics, troubleshooting

### 🔧 **Advanced Topics**
- **[Q111-Q120: JIT Compilation](#jit-compilation)** - HotSpot optimization, compilation tiers
- **[Q121-Q130: Native Memory](#native-memory)** - Off-heap memory, direct buffers, metaspace

---

## JVM Overview

### Q1: Explain the JVM architecture and its main components

**Answer:** The JVM (Java Virtual Machine) is a runtime environment that executes Java bytecode. It provides platform independence and memory management.

```java
public class JVMArchitectureDemo {

    /**
     * JVM Architecture Overview:
     *
     * ┌─────────────────────────────────────────────────────┐
     * │                    JVM                              │
     * ├─────────────────────────────────────────────────────┤
     * │  Class Loader Subsystem                             │
     * │  ┌─────────┐ ┌─────────┐ ┌─────────────────────┐   │
     * │  │Bootstrap│ │Extension│ │   Application       │   │
     * │  │Loader   │ │Loader   │ │   Loader            │   │
     * │  └─────────┘ └─────────┘ └─────────────────────┘   │
     * ├─────────────────────────────────────────────────────┤
     * │  Runtime Data Areas                                 │
     * │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   │
     * │  │   Method    │ │    Heap     │ │   Java      │   │
     * │  │   Area      │ │   Memory    │ │   Stacks    │   │
     * │  └─────────────┘ └─────────────┘ └─────────────┘   │
     * │  ┌─────────────┐ ┌─────────────┐                   │
     * │  │  PC         │ │   Native    │                   │
     * │  │  Register   │ │   Method    │                   │
     * │  │             │ │   Stack     │                   │
     * │  └─────────────┘ └─────────────┘                   │
     * ├─────────────────────────────────────────────────────┤
     * │  Execution Engine                                   │
     * │  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐   │
     * │  │Interpreter  │ │    JIT      │ │  Garbage    │   │
     * │  │             │ │  Compiler   │ │  Collector  │   │
     * │  └─────────────┘ └─────────────┘ └─────────────┘   │
     * ├─────────────────────────────────────────────────────┤
     * │  Native Method Interface (JNI)                     │
     * ├─────────────────────────────────────────────────────┤
     * │  Native Method Libraries                            │
     * └─────────────────────────────────────────────────────┘
     */

    public static void demonstrateJVMComponents() {
        System.out.println("=== JVM Component Demonstration ===");

        // 1. Class Loading Process
        demonstrateClassLoading();

        // 2. Memory Areas
        demonstrateMemoryAreas();

        // 3. Execution Process
        demonstrateExecution();
    }

    private static void demonstrateClassLoading() {
        System.out.println("\n--- Class Loading Process ---");

        // Get class loader information
        Class<?> clazz = JVMArchitectureDemo.class;
        ClassLoader classLoader = clazz.getClassLoader();

        System.out.println("Class: " + clazz.getName());
        System.out.println("Class Loader: " + classLoader);
        System.out.println("Parent Class Loader: " + classLoader.getParent());

        // System classes use Bootstrap ClassLoader (null)
        Class<?> stringClass = String.class;
        System.out.println("String Class Loader: " + stringClass.getClassLoader()); // null

        // Loading process demonstration
        try {
            // This triggers class loading if not already loaded
            Class<?> loadedClass = Class.forName("java.util.ArrayList");
            System.out.println("Loaded class: " + loadedClass.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void demonstrateMemoryAreas() {
        System.out.println("\n--- Memory Areas ---");

        // Get memory information
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        long usedMemory = totalMemory - freeMemory;

        System.out.println("Total Heap Memory: " + formatBytes(totalMemory));
        System.out.println("Used Heap Memory: " + formatBytes(usedMemory));
        System.out.println("Free Heap Memory: " + formatBytes(freeMemory));
        System.out.println("Max Heap Memory: " + formatBytes(maxMemory));

        // Demonstrate different memory areas
        demonstrateHeapMemory();
        demonstrateStackMemory();
        demonstrateMethodArea();
    }

    private static void demonstrateHeapMemory() {
        System.out.println("\n--- Heap Memory (Objects) ---");

        // Objects are created in heap memory
        String heapString = new String("This is in heap memory");
        StringBuilder builder = new StringBuilder("StringBuilder in heap");
        var list = new ArrayList<String>();

        System.out.println("Created objects in heap memory:");
        System.out.println("- String object: " + heapString);
        System.out.println("- StringBuilder: " + builder);
        System.out.println("- ArrayList: " + list.getClass().getSimpleName());

        // String pool is part of heap (Java 7+)
        String pooledString = "This is in string pool";
        String anotherPooled = "This is in string pool";
        System.out.println("String pool: " + (pooledString == anotherPooled)); // true
    }

    private static void demonstrateStackMemory() {
        System.out.println("\n--- Stack Memory (Method calls) ---");

        // Local variables and method parameters are in stack
        int localVariable = 42;
        String localReference = "Stack reference";

        System.out.println("Local variable (value in stack): " + localVariable);
        System.out.println("Reference variable (reference in stack): " + localReference);

        // Each method call creates a new stack frame
        recursiveMethodDemo(3);
    }

    private static void recursiveMethodDemo(int depth) {
        // Each call creates a new stack frame
        if (depth > 0) {
            System.out.println("Stack frame depth: " + depth);
            recursiveMethodDemo(depth - 1);
        }
    }

    private static void demonstrateMethodArea() {
        System.out.println("\n--- Method Area (Class metadata) ---");

        Class<?> clazz = JVMArchitectureDemo.class;

        // Class metadata is stored in method area
        System.out.println("Class name: " + clazz.getName());
        System.out.println("Methods count: " + clazz.getDeclaredMethods().length);
        System.out.println("Fields count: " + clazz.getDeclaredFields().length);

        // Static variables are in method area
        System.out.println("Static variables are in method area");
    }

    private static void demonstrateExecution() {
        System.out.println("\n--- Execution Process ---");

        // Interpreted vs Compiled code
        long startTime = System.nanoTime();

        // This code will be interpreted first, then potentially JIT compiled
        int sum = 0;
        for (int i = 0; i < 10000; i++) {
            sum += i * i;
        }

        long endTime = System.nanoTime();
        System.out.println("Calculation result: " + sum);
        System.out.println("Execution time: " + (endTime - startTime) / 1_000_000.0 + " ms");

        // JIT compiler will optimize frequently executed code
        System.out.println("JIT compiler optimizes hot spots in code");
    }

    // Utility method to format bytes
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    public static void main(String[] args) {
        demonstrateJVMComponents();
    }
}
```

**Key JVM Components:**

| Component | Purpose | Location |
|-----------|---------|----------|
| **Class Loader** | Load, link, initialize classes | JVM subsystem |
| **Method Area** | Store class metadata, static variables | Shared memory |
| **Heap** | Store objects and instance variables | Shared memory |
| **Java Stack** | Store method calls, local variables | Per-thread memory |
| **PC Register** | Track currently executing instruction | Per-thread memory |
| **Native Method Stack** | Support native method calls | Per-thread memory |
| **Execution Engine** | Execute bytecode (interpret/compile) | JVM subsystem |

### Q2: Explain the different memory areas in JVM with examples

**Answer:** JVM memory is divided into several distinct areas, each serving specific purposes:

```java
public class JVMMemoryAreasDemo {

    // Static variables stored in Method Area
    private static int staticCounter = 0;
    private static final String CONSTANT = "Method Area Constant";

    // Instance variables stored in Heap
    private String instanceVariable;
    private int instanceId;

    public JVMMemoryAreasDemo(String name) {
        this.instanceVariable = name;
        this.instanceId = ++staticCounter;
    }

    public static void demonstrateMemoryAreas() {
        System.out.println("=== JVM Memory Areas Detailed Demo ===");

        // 1. Method Area (Metaspace in Java 8+)
        demonstrateMethodArea();

        // 2. Heap Memory
        demonstrateHeapMemory();

        // 3. Java Stack
        demonstrateJavaStack();

        // 4. PC Register
        demonstratePCRegister();

        // 5. Native Method Stack
        demonstrateNativeMethodStack();

        // 6. Direct Memory (Off-heap)
        demonstrateDirectMemory();
    }

    private static void demonstrateMethodArea() {
        System.out.println("\n=== Method Area (Metaspace) ===");

        // Class metadata
        Class<?> clazz = JVMMemoryAreasDemo.class;
        System.out.println("Class metadata stored in Method Area:");
        System.out.println("- Class name: " + clazz.getSimpleName());
        System.out.println("- Method count: " + clazz.getDeclaredMethods().length);
        System.out.println("- Field count: " + clazz.getDeclaredFields().length);

        // Static variables
        System.out.println("Static variables in Method Area:");
        System.out.println("- Static counter: " + staticCounter);
        System.out.println("- Static constant: " + CONSTANT);

        // Runtime constant pool
        String literal1 = "Hello World";
        String literal2 = "Hello World";
        System.out.println("String literals (constant pool): " + (literal1 == literal2));

        // Method area memory can be monitored
        var memoryBeans = ManagementFactory.getMemoryPoolMXBeans();
        for (var bean : memoryBeans) {
            if (bean.getName().contains("Metaspace")) {
                var usage = bean.getUsage();
                System.out.println("Metaspace usage: " + formatBytes(usage.getUsed()) +
                                 " / " + formatBytes(usage.getMax()));
            }
        }
    }

    private static void demonstrateHeapMemory() {
        System.out.println("\n=== Heap Memory ===");

        // Objects created in heap
        var obj1 = new JVMMemoryAreasDemo("Object1");
        var obj2 = new JVMMemoryAreasDemo("Object2");

        // Arrays in heap
        int[] intArray = new int[1000];
        String[] stringArray = new String[500];

        // Collections in heap
        var list = new ArrayList<String>();
        list.add("Heap item 1");
        list.add("Heap item 2");

        System.out.println("Objects created in heap:");
        System.out.println("- Object 1: " + obj1.instanceVariable);
        System.out.println("- Object 2: " + obj2.instanceVariable);
        System.out.println("- Int array length: " + intArray.length);
        System.out.println("- String array length: " + stringArray.length);
        System.out.println("- List size: " + list.size());

        // Heap memory information
        var memoryBean = ManagementFactory.getMemoryMXBean();
        var heapUsage = memoryBean.getHeapMemoryUsage();

        System.out.println("\nHeap Memory Details:");
        System.out.println("- Used: " + formatBytes(heapUsage.getUsed()));
        System.out.println("- Committed: " + formatBytes(heapUsage.getCommitted()));
        System.out.println("- Max: " + formatBytes(heapUsage.getMax()));

        // Young generation vs Old generation
        demonstrateGenerationalHeap();
    }

    private static void demonstrateGenerationalHeap() {
        System.out.println("\n--- Generational Heap ---");

        var gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (var gcBean : gcBeans) {
            System.out.println("GC: " + gcBean.getName());
            System.out.println("- Collections: " + gcBean.getCollectionCount());
            System.out.println("- Time: " + gcBean.getCollectionTime() + " ms");
        }

        // Young generation objects (short-lived)
        for (int i = 0; i < 1000; i++) {
            var temp = new StringBuilder("Temporary object " + i);
            // These objects will likely be collected from young generation
        }

        // Trigger garbage collection
        System.gc();

        // Old generation objects (long-lived)
        var persistentList = new ArrayList<String>();
        for (int i = 0; i < 100; i++) {
            persistentList.add("Persistent object " + i);
        }
        System.out.println("Created persistent objects: " + persistentList.size());
    }

    private static void demonstrateJavaStack() {
        System.out.println("\n=== Java Stack (Per Thread) ===");

        // Each method call creates a stack frame
        System.out.println("Demonstrating stack frames:");

        // Local variables in stack
        int localInt = 42;
        String localString = "Stack variable";
        double localDouble = 3.14159;

        System.out.println("Local variables in current stack frame:");
        System.out.println("- int: " + localInt);
        System.out.println("- String reference: " + localString);
        System.out.println("- double: " + localDouble);

        // Method parameters also in stack
        stackMethodDemo(100, "Parameter");

        // Stack size demonstration
        demonstrateStackSize(0);
    }

    private static void stackMethodDemo(int param1, String param2) {
        // Parameters and local variables in this method's stack frame
        int localInMethod = param1 * 2;
        String localStringInMethod = param2 + " processed";

        System.out.println("Stack frame for stackMethodDemo:");
        System.out.println("- param1: " + param1);
        System.out.println("- param2: " + param2);
        System.out.println("- localInMethod: " + localInMethod);
        System.out.println("- localStringInMethod: " + localStringInMethod);
    }

    private static void demonstrateStackSize(int depth) {
        if (depth < 5) { // Limit to prevent StackOverflowError
            System.out.println("Stack depth: " + depth);
            demonstrateStackSize(depth + 1);
        }
    }

    private static void demonstratePCRegister() {
        System.out.println("\n=== PC Register ===");

        // PC Register tracks the currently executing instruction
        // It's automatically managed by JVM
        System.out.println("PC Register automatically tracks:");
        System.out.println("- Current instruction being executed");
        System.out.println("- Branch instructions for loops and conditionals");
        System.out.println("- Exception handling instructions");

        // Demonstrate with a loop
        for (int i = 0; i < 3; i++) {
            System.out.println("Loop iteration: " + i);
            // PC Register tracks each iteration
        }

        // Conditional execution
        if (Math.random() > 0.5) {
            System.out.println("Random condition met");
        } else {
            System.out.println("Random condition not met");
        }
    }

    private static void demonstrateNativeMethodStack() {
        System.out.println("\n=== Native Method Stack ===");

        // Native methods use C/C++ stack
        System.out.println("Native method calls:");

        // System.currentTimeMillis() is a native method
        long startTime = System.currentTimeMillis();
        System.out.println("Current time (native call): " + startTime);

        // Math operations often use native methods
        double result = Math.sqrt(144.0);
        System.out.println("Square root (native call): " + result);

        // Array copy is a native method
        int[] source = {1, 2, 3, 4, 5};
        int[] dest = new int[source.length];
        System.arraycopy(source, 0, dest, 0, source.length);
        System.out.println("Array copied using native method");

        // Object.hashCode() may use native implementation
        Object obj = new Object();
        int hashCode = obj.hashCode();
        System.out.println("Object hash code (possibly native): " + hashCode);
    }

    private static void demonstrateDirectMemory() {
        System.out.println("\n=== Direct Memory (Off-heap) ===");

        // DirectByteBuffer uses off-heap memory
        var directBuffer = ByteBuffer.allocateDirect(1024 * 1024); // 1MB
        System.out.println("Direct buffer allocated: " + directBuffer.capacity() + " bytes");
        System.out.println("Is direct: " + directBuffer.isDirect());

        // Regular heap buffer for comparison
        var heapBuffer = ByteBuffer.allocate(1024 * 1024); // 1MB
        System.out.println("Heap buffer allocated: " + heapBuffer.capacity() + " bytes");
        System.out.println("Is direct: " + heapBuffer.isDirect());

        // Direct memory is useful for I/O operations
        directBuffer.putInt(42);
        directBuffer.putDouble(3.14159);
        directBuffer.flip();

        int intValue = directBuffer.getInt();
        double doubleValue = directBuffer.getDouble();

        System.out.println("Values from direct buffer: " + intValue + ", " + doubleValue);

        // Direct memory must be explicitly freed
        // Java 9+ provides Cleaner API for this
        System.out.println("Direct memory should be explicitly managed");
    }

    // Thread-specific memory demonstration
    public static void demonstrateThreadMemory() {
        System.out.println("\n=== Thread-Specific Memory ===");

        // Create multiple threads to show per-thread stacks
        Runnable task = () -> {
            String threadName = Thread.currentThread().getName();
            int localVar = (int) (Math.random() * 100);

            System.out.println("Thread " + threadName + " - Local variable: " + localVar);

            // Each thread has its own stack
            threadSpecificMethod(threadName, localVar);
        };

        var thread1 = new Thread(task, "Thread-1");
        var thread2 = new Thread(task, "Thread-2");

        thread1.start();
        thread2.start();

        try {
            thread1.join();
            thread2.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void threadSpecificMethod(String threadName, int value) {
        // This method's stack frame is unique to each thread
        int doubledValue = value * 2;
        System.out.println("Thread " + threadName + " - Doubled value: " + doubledValue);
    }

    private static String formatBytes(long bytes) {
        if (bytes < 0) return "N/A";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    public static void main(String[] args) {
        demonstrateMemoryAreas();
        demonstrateThreadMemory();
    }
}
```

**Memory Area Characteristics:**

| Memory Area | Shared/Per-Thread | Stores | Garbage Collected | Size Limit |
|-------------|-------------------|--------|-------------------|-------------|
| **Method Area (Metaspace)** | Shared | Class metadata, static variables | Yes (rare) | Limited by native memory |
| **Heap** | Shared | Objects, instance variables | Yes | -Xmx parameter |
| **Java Stack** | Per-thread | Method frames, local variables | No | -Xss parameter |
| **PC Register** | Per-thread | Current instruction pointer | No | Very small |
| **Native Method Stack** | Per-thread | Native method calls | No | OS dependent |
| **Direct Memory** | Shared | Off-heap buffers | No | -XX:MaxDirectMemorySize |

### Q3: How does garbage collection work in Java?

**Answer:** Garbage Collection automatically manages memory by identifying and reclaiming objects that are no longer reachable:

```java
public class GarbageCollectionDemo {

    // Class to demonstrate object lifecycle
    static class ManagedObject {
        private String name;
        private byte[] data;
        private static int instanceCount = 0;

        public ManagedObject(String name, int dataSize) {
            this.name = name;
            this.data = new byte[dataSize];
            instanceCount++;
            System.out.println("Created: " + name + " (Instance #" + instanceCount + ")");
        }

        @Override
        protected void finalize() throws Throwable {
            // Called before GC (deprecated in Java 9+)
            System.out.println("Finalizing: " + name);
            super.finalize();
        }

        public String getName() { return name; }
    }

    public static void demonstrateGCConcepts() {
        System.out.println("=== Garbage Collection Concepts ===");

        // 1. Object Creation and Reachability
        demonstrateObjectReachability();

        // 2. Generational Hypothesis
        demonstrateGenerationalHypothesis();

        // 3. GC Algorithms
        demonstrateGCAlgorithms();

        // 4. Memory Leaks
        demonstrateMemoryLeaks();
    }

    private static void demonstrateObjectReachability() {
        System.out.println("\n--- Object Reachability ---");

        // Strong reference - object is reachable
        ManagedObject strongRef = new ManagedObject("StrongRef", 1024);

        // Create temporary objects (eligible for GC)
        for (int i = 0; i < 5; i++) {
            ManagedObject temp = new ManagedObject("Temp" + i, 512);
            // temp becomes unreachable after each iteration
        }

        // Object with no references (immediately eligible for GC)
        new ManagedObject("NoRef", 256);

        System.out.println("Strong reference still holds: " + strongRef.getName());

        // Remove strong reference
        strongRef = null;
        System.out.println("Strong reference removed");

        // Suggest garbage collection
        System.gc();
        try {
            Thread.sleep(100); // Give GC time to run
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void demonstrateGenerationalHypothesis() {
        System.out.println("\n--- Generational Hypothesis ---");

        // Young objects (most will die young)
        System.out.println("Creating young objects...");
        for (int i = 0; i < 1000; i++) {
            var youngObject = new StringBuilder("Young " + i);
            // Most of these will be collected quickly
        }

        // Old objects (survived multiple GC cycles)
        var oldObject = new ArrayList<String>();
        for (int i = 0; i < 100; i++) {
            oldObject.add("Old object data " + i);
        }

        // Force minor GC
        System.gc();

        System.out.println("Old object survived: " + oldObject.size() + " items");

        // Show GC statistics
        showGCStatistics();
    }

    private static void demonstrateGCAlgorithms() {
        System.out.println("\n--- GC Algorithms Available ---");

        var gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (var gcBean : gcBeans) {
            System.out.println("GC Algorithm: " + gcBean.getName());
            System.out.println("- Memory pools: " + Arrays.toString(gcBean.getMemoryPoolNames()));
            System.out.println("- Collection count: " + gcBean.getCollectionCount());
            System.out.println("- Collection time: " + gcBean.getCollectionTime() + " ms");

            // Check if this is a concurrent collector
            if (gcBean.getName().contains("Concurrent") || gcBean.getName().contains("G1")) {
                System.out.println("- Type: Concurrent/Low-latency collector");
            } else if (gcBean.getName().contains("Parallel")) {
                System.out.println("- Type: Parallel collector");
            } else {
                System.out.println("- Type: Serial collector");
            }
            System.out.println();
        }

        // Demonstrate different GC pressures
        demonstrateGCPressure();
    }

    private static void demonstrateGCPressure() {
        System.out.println("--- GC Pressure Test ---");

        long startTime = System.currentTimeMillis();
        var memoryBefore = getMemoryUsage();

        // Create memory pressure
        var largeList = new ArrayList<byte[]>();
        try {
            for (int i = 0; i < 1000; i++) {
                largeList.add(new byte[1024 * 1024]); // 1MB each
                if (i % 100 == 0) {
                    System.out.println("Allocated " + (i + 1) + " MB");
                    showMemoryUsage();
                }
            }
        } catch (OutOfMemoryError e) {
            System.out.println("OutOfMemoryError caught: " + e.getMessage());
        }

        var memoryAfter = getMemoryUsage();
        long endTime = System.currentTimeMillis();

        System.out.println("Memory pressure test completed in " + (endTime - startTime) + " ms");
        System.out.println("Memory before: " + formatBytes(memoryBefore));
        System.out.println("Memory after: " + formatBytes(memoryAfter));

        // Clear references and suggest GC
        largeList.clear();
        largeList = null;
        System.gc();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        showMemoryUsage();
    }

    private static void demonstrateMemoryLeaks() {
        System.out.println("\n--- Memory Leak Examples ---");

        // 1. Static collection leak
        demonstrateStaticCollectionLeak();

        // 2. Inner class leak
        demonstrateInnerClassLeak();

        // 3. Unclosed resources leak
        demonstrateResourceLeak();
    }

    // Static collection that grows indefinitely
    private static final Map<String, ManagedObject> STATIC_CACHE = new HashMap<>();

    private static void demonstrateStaticCollectionLeak() {
        System.out.println("Static collection leak:");

        for (int i = 0; i < 10; i++) {
            String key = "leak_" + i;
            STATIC_CACHE.put(key, new ManagedObject("Leaked" + i, 1024));
        }

        System.out.println("Static cache size: " + STATIC_CACHE.size());
        // These objects will never be GC'd due to static reference
    }

    // Inner class holds reference to outer class
    class InnerClassDemo {
        private byte[] data = new byte[1024 * 1024]; // 1MB

        class InnerClass {
            public void doSomething() {
                // Implicit reference to outer class
                System.out.println("Inner class method");
            }
        }

        public InnerClass createInner() {
            return new InnerClass();
        }
    }

    private static void demonstrateInnerClassLeak() {
        System.out.println("Inner class leak:");

        var outer = new GarbageCollectionDemo().new InnerClassDemo();
        var inner = outer.createInner();

        // Even if we null the outer reference, inner class still holds it
        outer = null;
        inner.doSomething();

        // The outer class instance won't be GC'd while inner exists
        System.out.println("Inner class still holds outer reference");
    }

    private static void demonstrateResourceLeak() {
        System.out.println("Resource leak example:");

        try {
            // Simulate unclosed resources
            var fileStream = new ByteArrayInputStream(new byte[1024]);
            // Not closing stream in finally block can cause leaks
            System.out.println("Resource created but not properly closed");
        } catch (Exception e) {
            System.out.println("Exception in resource handling: " + e.getMessage());
        }

        // Proper resource management with try-with-resources
        try (var properStream = new ByteArrayInputStream(new byte[1024])) {
            System.out.println("Resource properly managed with try-with-resources");
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }

    // Reference types demonstration
    public static void demonstrateReferenceTypes() {
        System.out.println("\n=== Reference Types ===");

        // Strong reference
        ManagedObject strongRef = new ManagedObject("Strong", 1024);

        // Weak reference
        WeakReference<ManagedObject> weakRef = new WeakReference<>(strongRef);

        // Soft reference
        SoftReference<ManagedObject> softRef = new SoftReference<>(strongRef);

        // Phantom reference
        ReferenceQueue<ManagedObject> queue = new ReferenceQueue<>();
        PhantomReference<ManagedObject> phantomRef = new PhantomReference<>(strongRef, queue);

        System.out.println("All references created");
        System.out.println("Weak ref object: " + (weakRef.get() != null ? "alive" : "collected"));
        System.out.println("Soft ref object: " + (softRef.get() != null ? "alive" : "collected"));

        // Remove strong reference
        strongRef = null;
        System.gc();

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("After GC:");
        System.out.println("Weak ref object: " + (weakRef.get() != null ? "alive" : "collected"));
        System.out.println("Soft ref object: " + (softRef.get() != null ? "alive" : "collected"));
    }

    // Utility methods
    private static void showGCStatistics() {
        var gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (var gcBean : gcBeans) {
            System.out.println(gcBean.getName() + " - Collections: " +
                             gcBean.getCollectionCount() + ", Time: " +
                             gcBean.getCollectionTime() + " ms");
        }
    }

    private static void showMemoryUsage() {
        var memoryBean = ManagementFactory.getMemoryMXBean();
        var heapUsage = memoryBean.getHeapMemoryUsage();
        var nonHeapUsage = memoryBean.getNonHeapMemoryUsage();

        System.out.println("Heap: " + formatBytes(heapUsage.getUsed()) +
                          " / " + formatBytes(heapUsage.getMax()));
        System.out.println("Non-Heap: " + formatBytes(nonHeapUsage.getUsed()) +
                          " / " + formatBytes(nonHeapUsage.getMax()));
    }

    private static long getMemoryUsage() {
        var memoryBean = ManagementFactory.getMemoryMXBean();
        return memoryBean.getHeapMemoryUsage().getUsed();
    }

    private static String formatBytes(long bytes) {
        if (bytes < 0) return "N/A";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    public static void main(String[] args) {
        System.out.println("Starting GC demonstration with JVM: " +
                          System.getProperty("java.version"));
        System.out.println("GC options can be viewed with: java -XX:+PrintGCDetails\n");

        demonstrateGCConcepts();
        demonstrateReferenceTypes();

        System.out.println("\n=== Final Memory Status ===");
        showMemoryUsage();
    }
}
```

**Garbage Collection Algorithms:**

| Algorithm | Type | Latency | Throughput | Use Case |
|-----------|------|---------|------------|----------|
| **Serial GC** | Stop-the-world | High | Low | Single-core, small heaps |
| **Parallel GC** | Stop-the-world | High | High | Multi-core, throughput critical |
| **G1 GC** | Concurrent | Low | Medium | Large heaps, latency sensitive |
| **ZGC** | Concurrent | Very Low | Medium | Very large heaps, ultra-low latency |
| **Shenandoah** | Concurrent | Very Low | Medium | Low latency applications |

*[Continue with remaining sections Q4-Q130 covering ClassLoader mechanism, GC tuning, memory optimization, JIT compilation, etc.]*

---

## Summary

This guide provides deep understanding of JVM internals and memory management:

### ✅ **Key Topics Covered**
- **JVM Architecture**: Components, execution process, memory areas
- **Garbage Collection**: Algorithms, tuning, memory leak detection
- **Performance Optimization**: Memory efficiency, JVM tuning, profiling
- **Advanced Concepts**: JIT compilation, native memory, monitoring

### 🎯 **Target Audience**
- **Senior Developers**: Advanced JVM knowledge
- **Performance Engineers**: Memory optimization and tuning
- **System Architects**: Understanding system constraints
- **DevOps Engineers**: Production JVM configuration

### 🔧 **Practical Applications**
- **Performance Tuning**: Optimize application memory usage
- **Troubleshooting**: Diagnose memory leaks and performance issues
- **System Design**: Make informed decisions about memory requirements
- **Production Support**: Configure JVM for optimal performance

This comprehensive guide equips you with the deep JVM knowledge essential for senior Java development roles.