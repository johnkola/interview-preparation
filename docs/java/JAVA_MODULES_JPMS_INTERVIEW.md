# Java Modules (JPMS) - Interview Guide

> **Complete guide to Java Platform Module System for technical interviews**

---

## 📚 **Table of Contents**

1. [Module System Overview](#module-system-overview)
2. [Module Declaration](#module-declaration)
3. [Module Types](#module-types)
4. [Module Directives](#module-directives)
5. [Module Path vs Classpath](#module-path-vs-classpath)
6. [Modular JARs](#modular-jars)
7. [Service Provider Interface (SPI)](#service-provider-interface-spi)
8. [Migration Strategies](#migration-strategies)
9. [Command Line Tools](#command-line-tools)
10. [Best Practices](#best-practices)
11. [Common Interview Questions](#common-interview-questions)

---

## 🏗️ **Module System Overview**

### **What is JPMS?**

**Java Platform Module System (JPMS)** introduced in Java 9 provides:
- **Strong encapsulation** of packages
- **Reliable configuration** with explicit dependencies
- **Scalable systems** through modular architecture

### **Key Benefits**

```java
// Before Modules (Java 8 and earlier)
// - All public classes accessible
// - Classpath hell
// - No explicit dependencies

// With Modules (Java 9+)
module com.company.orders {
    requires com.company.customers;
    exports com.company.orders.api;
    // Only api package exposed, implementation hidden
}
```

### **Module System Goals**
1. **Reliable Configuration** - explicit dependencies
2. **Strong Encapsulation** - hide internal APIs
3. **Scalable Systems** - modular applications
4. **Security** - reduced attack surface
5. **Performance** - faster startup, smaller footprint

---

## 📄 **Module Declaration**

### **module-info.java Structure**

```java
/**
 * Complete module declaration example
 */
module com.company.ecommerce.orders {
    // Dependencies on other modules
    requires java.base;              // Implicit - always present
    requires java.logging;           // JDK module
    requires transitive java.sql;   // Transitive dependency
    requires static lombok;          // Compile-time only

    // Export packages to other modules
    exports com.company.orders.api;
    exports com.company.orders.model to
        com.company.billing,
        com.company.shipping;

    // Service provider registration
    provides com.company.orders.spi.OrderProcessor
        with com.company.orders.impl.DefaultOrderProcessor;

    // Service consumption
    uses com.company.payment.spi.PaymentProvider;

    // Reflection access (for frameworks)
    opens com.company.orders.entity to
        hibernate.core,
        jackson.databind;
    opens com.company.orders.dto;

    // Module version (optional)
    // @since 1.0.0
}
```

### **Module Naming Conventions**

```java
// Recommended naming patterns
module com.company.product.feature {    // Reverse domain notation
    // Good: descriptive, hierarchical
}

module orders {                         // Simple name
    // Acceptable: for internal modules
}

// Avoid
module Orders;          // Capital letters
module order-service;   // Hyphens not recommended
module 123orders;       // Starting with numbers
```

---

## 🎯 **Module Types**

### **1. Named Modules**

```java
// Explicit module with module-info.java
module com.company.core {
    exports com.company.core.api;
    requires java.logging;
}
```

### **2. Automatic Modules**

```java
// JAR without module-info.java on module path
// Module name derived from JAR filename
// commons-lang3-3.12.0.jar → commons.lang3

module my.app {
    requires commons.lang3;  // Automatic module
}
```

### **3. Unnamed Module**

```java
// Code on classpath (legacy mode)
// Can read all other modules
// Cannot be required by named modules
```

---

## 🔧 **Module Directives**

### **requires Directive**

```java
module com.company.orders {
    // Basic dependency
    requires java.logging;

    // Transitive dependency - consumers get this too
    requires transitive java.sql;

    // Static dependency - compile time only
    requires static lombok;

    // Version requirement (not enforced by JVM)
    // requires java.base;  // Always implicit
}
```

### **exports Directive**

```java
module com.company.orders {
    // Public API - available to all modules
    exports com.company.orders.api;

    // Qualified export - only to specific modules
    exports com.company.orders.internal to
        com.company.orders.test,
        com.company.orders.integration;

    // Package structure must match directory structure
    // com/company/orders/api/OrderService.java
}
```

### **opens Directive**

```java
module com.company.orders {
    // Deep reflection access to all modules
    opens com.company.orders.entity;

    // Qualified opens - only specific modules
    opens com.company.orders.dto to
        jackson.databind,
        hibernate.core;

    // For frameworks requiring reflection
    // JPA entities, JSON serialization, etc.
}
```

### **uses and provides Directives**

```java
// Service Provider Interface
module com.company.orders.api {
    exports com.company.orders.spi;
}

// Service Consumer
module com.company.orders.client {
    requires com.company.orders.api;
    uses com.company.orders.spi.OrderProcessor;
}

// Service Provider
module com.company.orders.impl {
    requires com.company.orders.api;
    provides com.company.orders.spi.OrderProcessor
        with com.company.orders.impl.DatabaseOrderProcessor,
             com.company.orders.impl.CacheOrderProcessor;
}

// Service Interface
package com.company.orders.spi;
public interface OrderProcessor {
    void processOrder(Order order);
}

// Service Implementation
package com.company.orders.impl;
public class DatabaseOrderProcessor implements OrderProcessor {
    @Override
    public void processOrder(Order order) {
        // Database implementation
    }
}

// Service Loading
var processors = ServiceLoader.load(OrderProcessor.class);
processors.forEach(processor -> processor.processOrder(order));
```

---

## 🛣️ **Module Path vs Classpath**

### **Classpath (Legacy)**

```bash
# Traditional classpath
java -cp "lib/*:app.jar" com.company.Main

# All public classes accessible
# JAR hell possible
# No explicit dependencies
```

### **Module Path (Modern)**

```bash
# Module path approach
java --module-path "modules" \
     --module com.company.app/com.company.Main

# Strong encapsulation enforced
# Explicit dependencies required
# Reliable configuration
```

### **Mixed Mode**

```java
// Named module accessing classpath
module com.company.app {
    // Can require automatic modules (JARs on module path)
    requires commons.lang3;

    // Cannot require unnamed module (classpath)
    // But unnamed module can read this module
}
```

### **Migration Example**

```bash
# Phase 1: Move JARs to module path (automatic modules)
java --module-path "libs" \
     --add-modules ALL-MODULE-PATH \
     --class-path "legacy-libs/*" \
     -jar app.jar

# Phase 2: Convert to named modules
java --module-path "modules" \
     --module com.company.app

# Phase 3: Fully modular
java --module-path "modules" \
     --module com.company.app/com.company.Main
```

---

## 📦 **Modular JARs**

### **Creating Modular JAR**

```bash
# Directory structure
src/
├── com.company.orders/
│   ├── module-info.java
│   └── com/company/orders/
│       ├── api/OrderService.java
│       └── impl/OrderServiceImpl.java
└── com.company.billing/
    ├── module-info.java
    └── com/company/billing/
        └── BillingService.java

# Compilation
javac -d mods \
      --module-source-path src \
      src/com.company.orders/module-info.java \
      src/com.company.orders/com/company/orders/**/*.java

# JAR creation
jar --create \
    --file libs/orders.jar \
    --main-class com.company.orders.Main \
    -C mods/com.company.orders .

# Verification
jar --describe-module --file libs/orders.jar
```

### **Multi-Release JARs**

```java
// Support different Java versions
META-INF/
├── MANIFEST.MF
├── versions/
│   ├── 9/
│   │   └── module-info.class
│   ├── 11/
│   │   └── com/company/Feature.class
│   └── 17/
│       └── com/company/EnhancedFeature.class
└── com/company/BaseFeature.class

// MANIFEST.MF
Multi-Release: true
```

---

## 🔌 **Service Provider Interface (SPI)**

### **Complete SPI Example**

```java
// 1. Service Interface Module
module com.company.plugin.api {
    exports com.company.plugin.spi;
}

// Service interface
package com.company.plugin.spi;
public interface DataProcessor {
    String process(String data);
    boolean supports(String format);
}

// 2. Service Implementation Module
module com.company.plugin.json {
    requires com.company.plugin.api;
    requires com.fasterxml.jackson.core;

    provides com.company.plugin.spi.DataProcessor
        with com.company.plugin.json.JsonProcessor;
}

// Implementation
package com.company.plugin.json;
public class JsonProcessor implements DataProcessor {
    @Override
    public String process(String data) {
        // JSON processing logic
        return processJson(data);
    }

    @Override
    public boolean supports(String format) {
        return "json".equalsIgnoreCase(format);
    }
}

// 3. Service Consumer Module
module com.company.app {
    requires com.company.plugin.api;
    uses com.company.plugin.spi.DataProcessor;
}

// Consumer code
package com.company.app;
public class DataService {
    private final List<DataProcessor> processors;

    public DataService() {
        processors = ServiceLoader.load(DataProcessor.class)
                                 .stream()
                                 .map(ServiceLoader.Provider::get)
                                 .toList();
    }

    public String processData(String data, String format) {
        return processors.stream()
                        .filter(p -> p.supports(format))
                        .findFirst()
                        .map(p -> p.process(data))
                        .orElseThrow(() ->
                            new UnsupportedOperationException(
                                "No processor for format: " + format));
    }
}
```

### **Dynamic Service Loading**

```java
// ModuleLayer for dynamic loading
public class PluginManager {
    public void loadPlugin(Path pluginPath) {
        var finder = ModuleFinder.of(pluginPath);
        var parent = ModuleLayer.boot();
        var cf = parent.configuration()
                      .resolve(finder, ModuleFinder.of(),
                              Set.of("plugin.module.name"));

        var layer = parent.defineModulesWithOneLoader(cf,
                                                     ClassLoader.getSystemClassLoader());

        // Load services from new layer
        var loader = ServiceLoader.load(layer, DataProcessor.class);
        loader.forEach(this::registerProcessor);
    }
}
```

---

## 🔄 **Migration Strategies**

### **Bottom-Up Migration**

```java
// Step 1: Start with leaf modules (no dependencies)
module com.company.utils {
    exports com.company.utils.string;
    exports com.company.utils.date;
}

// Step 2: Add modules that depend on utils
module com.company.model {
    requires com.company.utils;
    exports com.company.model.entity;
}

// Step 3: Add business logic modules
module com.company.service {
    requires com.company.model;
    requires com.company.utils;
    exports com.company.service.api;
}

// Step 4: Finally, application module
module com.company.app {
    requires com.company.service;
    // No exports needed for application
}
```

### **Top-Down Migration**

```java
// Step 1: Create application module first
module com.company.app {
    requires ALL.MODULE.PATH;  // Temporary
    // Add requires as you modularize dependencies
}

// Step 2: Extract modules one by one
module com.company.extracted.service {
    requires automatic.dependency;
    exports com.company.service.api;
}

// Step 3: Update application module
module com.company.app {
    requires com.company.extracted.service;
    requires remaining.automatic.modules;
}
```

### **Migration Challenges and Solutions**

```java
// Problem: Split packages across JARs
// Solution: Merge or rename packages

// Problem: Reflection access
module legacy.framework {
    opens com.company.entity to hibernate.core;
    opens com.company.dto to jackson.databind;
}

// Problem: Circular dependencies
// Solution: Extract common interface module
module com.company.common.api {
    exports com.company.common.interfaces;
}

module com.company.orders {
    requires com.company.common.api;
    // Implement interfaces, don't depend on billing directly
}

module com.company.billing {
    requires com.company.common.api;
    // Implement interfaces, don't depend on orders directly
}
```

---

## 🛠️ **Command Line Tools**

### **Module-related JDK Tools**

```bash
# 1. jdeps - Analyze dependencies
jdeps --module-path libs \
      --check com.company.orders

jdeps --generate-module-info src \
      legacy-app.jar

# 2. jmod - Create and manipulate JMOD files
jmod create --class-path classes \
            --libs lib \
            --config conf \
            lib/orders.jmod

# 3. jlink - Create custom runtime images
jlink --module-path /jdk/jmods:libs \
      --add-modules com.company.app \
      --output custom-runtime \
      --compress=2 \
      --strip-debug

# 4. java - Module execution
java --module-path libs \
     --module com.company.app/com.company.Main \
     --add-opens java.base/java.lang=ALL-UNNAMED \
     --add-exports java.base/sun.nio.ch=ALL-UNNAMED

# 5. javac - Module compilation
javac --module-path libs \
      --module-source-path src \
      -d mods \
      src/*/module-info.java \
      src/**/com/**/*.java
```

### **Runtime Module System Inspection**

```java
// Programmatic module inspection
public class ModuleInspector {
    public void inspectModule(String moduleName) {
        var layer = ModuleLayer.boot();
        var module = layer.findModule(moduleName);

        if (module.isPresent()) {
            var m = module.get();
            System.out.println("Module: " + m.getName());
            System.out.println("Descriptor: " + m.getDescriptor());
            System.out.println("Packages: " + m.getPackages());
            System.out.println("Layer: " + m.getLayer());

            // Check what module can read
            var descriptor = m.getDescriptor();
            descriptor.requires().forEach(req ->
                System.out.println("Requires: " + req.name()));

            descriptor.exports().forEach(exp ->
                System.out.println("Exports: " + exp.source()));
        }
    }

    // List all modules
    public void listAllModules() {
        ModuleLayer.boot()
                  .modules()
                  .stream()
                  .map(Module::getName)
                  .sorted()
                  .forEach(System.out::println);
    }
}
```

---

## 💡 **Best Practices**

### **Module Design Principles**

```java
// 1. Keep modules cohesive and loosely coupled
module com.company.user.management {
    // Good: Single responsibility
    exports com.company.user.api;
    requires com.company.common.validation;
}

// 2. Use descriptive module names
module com.company.ecommerce.order.processing {
    // Clear purpose and domain
}

// 3. Minimize exports
module com.company.service {
    exports com.company.service.api;        // Public API only
    // Keep implementation packages private
    // com.company.service.impl - not exported
}

// 4. Use transitive requires carefully
module com.company.api {
    requires transitive java.sql;    // Clients need SQLException
    requires java.logging;           // Internal use only
}
```

### **Performance Considerations**

```java
// 1. Lazy module loading
public class LazyModuleLoader {
    private volatile ModuleLayer pluginLayer;

    public synchronized ModuleLayer getPluginLayer() {
        if (pluginLayer == null) {
            pluginLayer = loadPluginModules();
        }
        return pluginLayer;
    }
}

// 2. Custom runtime images for deployment
# Create minimal runtime
jlink --module-path /jdk/jmods:libs \
      --add-modules com.company.app,java.logging \
      --output production-runtime \
      --compress=2 \
      --strip-debug \
      --no-header-files \
      --no-man-pages

// 3. Module-aware class loading
public class ModuleAwareClassLoader extends ClassLoader {
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        var module = getClass().getModule();
        return Class.forName(module, name);
    }
}
```

### **Security Best Practices**

```java
// 1. Minimal exposure
module com.company.secure.service {
    exports com.company.secure.api;    // Only public API
    // Hide all implementation details
}

// 2. Controlled reflection access
module com.company.entity {
    opens com.company.entity.jpa to hibernate.core;
    // Don't use 'opens' without 'to' clause unless absolutely necessary
}

// 3. Service-based architecture
module com.company.payment.api {
    exports com.company.payment.spi;
    // Define interfaces, hide implementations
}

module com.company.payment.stripe {
    requires com.company.payment.api;
    provides com.company.payment.spi.PaymentProvider
        with com.company.payment.stripe.StripeProvider;
    // Implementation hidden
}
```

---

## ❓ **Common Interview Questions**

### **Q1: What problems does the Java Module System solve?**

**Answer:**
```java
// Before JPMS
// Problems:
// 1. Classpath hell - version conflicts
// 2. No encapsulation - all public classes accessible
// 3. No explicit dependencies
// 4. Large runtime footprint

// With JPMS
module com.company.orders {
    requires com.company.customers;     // Explicit dependencies
    exports com.company.orders.api;     // Only API exposed
    // Implementation packages hidden by default
}

// Benefits:
// 1. Reliable configuration
// 2. Strong encapsulation
// 3. Scalable systems
// 4. Security and performance
```

### **Q2: Explain the difference between requires and requires transitive**

**Answer:**
```java
// Module A
module com.company.database {
    exports com.company.database.api;
    requires java.sql;                    // Private dependency
    requires transitive java.logging;    // Transitive dependency
}

// Module B
module com.company.service {
    requires com.company.database;

    // Can use java.logging (transitive from database module)
    Logger logger = Logger.getLogger("service");

    // Cannot use java.sql directly - not transitive
    // Connection conn = ...; // Compilation error
}

// Use transitive when:
// - Your API exposes types from the dependency
// - Clients need the dependency to use your module
```

### **Q3: How do automatic modules work?**

**Answer:**
```java
// JAR without module-info.java placed on module path
// commons-lang3-3.12.0.jar → automatic module

module my.app {
    requires commons.lang3;  // Module name derived from JAR
    // or
    requires org.apache.commons.lang3;  // From MANIFEST.MF
}

// Automatic module characteristics:
// 1. Reads all other modules
// 2. Exports all packages
// 3. Name derived from JAR filename or MANIFEST
// 4. Bridge between modular and non-modular code
```

### **Q4: What is the unnamed module?**

**Answer:**
```java
// Code on classpath lives in unnamed module
// Characteristics:
// 1. Can read all named modules
// 2. Cannot be required by named modules
// 3. All types in single unnamed module
// 4. Used for legacy compatibility

// Named module cannot require unnamed module
module my.module {
    // requires UNNAMED.MODULE;  // Not possible
}

// But unnamed module can use named modules
// Legacy code on classpath can use modular JARs
```

### **Q5: How do you handle reflection in modules?**

**Answer:**
```java
// Problem: Frameworks need deep reflection
module com.company.entity {
    // Solution 1: opens directive
    opens com.company.entity.jpa to hibernate.core;

    // Solution 2: opens to all (less secure)
    opens com.company.entity.dto;

    // Solution 3: Runtime --add-opens
    // java --add-opens com.company.entity/com.company.entity.internal=ALL-UNNAMED
}

// Framework usage
Class<?> entityClass = Class.forName("com.company.entity.jpa.User");
Field field = entityClass.getDeclaredField("id");
field.setAccessible(true);  // Works due to 'opens'
```

### **Q6: Describe the Service Provider Interface pattern**

**Answer:**
```java
// 1. Define service interface
module payment.api {
    exports com.company.payment.spi;
}

public interface PaymentProvider {
    boolean processPayment(Payment payment);
    String getProviderName();
}

// 2. Implement service
module payment.stripe {
    requires payment.api;
    provides com.company.payment.spi.PaymentProvider
        with com.company.payment.stripe.StripeProvider;
}

// 3. Consume service
module payment.client {
    requires payment.api;
    uses com.company.payment.spi.PaymentProvider;
}

var providers = ServiceLoader.load(PaymentProvider.class);
providers.forEach(provider ->
    System.out.println("Available: " + provider.getProviderName()));
```

### **Q7: How do you migrate a large application to modules?**

**Answer:**
```java
// Strategy 1: Bottom-up approach
// 1. Start with dependencies (leaf modules)
module utils {
    exports com.company.utils.string;
}

// 2. Work upward through dependency tree
module model {
    requires utils;
    exports com.company.model;
}

// 3. Finally, application modules
module app {
    requires model;
}

// Strategy 2: Automatic modules
// Place JARs on module path first
java --module-path libs \
     --add-modules ALL-MODULE-PATH \
     -jar app.jar

// Strategy 3: Incremental modularization
// Use jdeps to analyze dependencies
jdeps --generate-module-info src legacy-app.jar
```

### **Q8: What are qualified exports and when to use them?**

**Answer:**
```java
module com.company.orders {
    // Public export - available to all modules
    exports com.company.orders.api;

    // Qualified export - only to specific modules
    exports com.company.orders.internal to
        com.company.orders.test,
        com.company.orders.integration;

    // Use cases for qualified exports:
    // 1. Test modules need access to internals
    // 2. Tight coupling between specific modules
    // 3. Migration scenarios
    // 4. Friend assemblies pattern
}

// Test module can access internal packages
module com.company.orders.test {
    requires com.company.orders;
    // Can access com.company.orders.internal
}
```

### **Q9: Performance impact of modules**

**Answer:**
```java
// Benefits:
// 1. Faster startup - explicit dependencies
// 2. Smaller memory footprint - only required modules
// 3. Better JIT optimization - known boundaries
// 4. Custom runtime images with jlink

# Create optimized runtime
jlink --module-path libs \
      --add-modules com.company.app \
      --output custom-jre \
      --compress=2 \
      --strip-debug

// Overhead:
// 1. Module resolution at startup
// 2. Access control checks
// 3. Metadata loading

// Minimal performance impact in practice
// Benefits usually outweigh costs
```

### **Q10: Common module system pitfalls**

**Answer:**
```java
// 1. Split packages across modules
// Problem:
module a { exports com.company.util; }
module b { exports com.company.util; }  // Error!

// Solution: Unique packages per module
module a { exports com.company.util.string; }
module b { exports com.company.util.date; }

// 2. Circular dependencies
// Problem:
module orders { requires billing; }
module billing { requires orders; }  // Circular!

// Solution: Extract common interface
module common { exports com.company.interfaces; }
module orders { requires common; }
module billing { requires common; }

// 3. Over-opening modules
// Problem:
opens com.company.internal;  // Too broad

// Solution:
opens com.company.internal.entity to hibernate.core;  // Specific
```

---

## 🎯 **Summary**

### **Key Concepts to Remember**

1. **Module Declaration**: `module-info.java` with requires/exports/opens
2. **Strong Encapsulation**: Only exported packages accessible
3. **Reliable Configuration**: Explicit dependency declarations
4. **Service Loading**: Decoupled service provider pattern
5. **Migration**: Bottom-up or automatic modules approach
6. **Tools**: jdeps, jlink, jmod for module operations

### **Interview Success Tips**

- Understand the motivation behind JPMS
- Know the difference between classpath and module path
- Practice service provider interface patterns
- Demonstrate migration strategies
- Explain performance and security benefits
- Show hands-on experience with module tools

### **Common Use Cases**

- **Microservices**: Clear module boundaries
- **Plugin Architecture**: Service provider interfaces
- **Library Development**: Strong API encapsulation
- **Application Modularization**: Logical separation
- **Custom Runtimes**: Minimal deployment images

---

*This guide covers Java Module System comprehensively for technical interviews. Practice with real projects to solidify understanding.*