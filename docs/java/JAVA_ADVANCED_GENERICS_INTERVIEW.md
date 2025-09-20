# Java Advanced Generics - Complete Interview Guide

> **Deep dive into Java Generics with complex scenarios, wildcards, type erasure, and advanced patterns**
> Essential knowledge for senior Java developers and complex API design

---

## 📋 Table of Contents

### 🔧 **Generics Fundamentals**
- **[Q1-Q10: Generic Basics](#generic-basics)** - Type parameters, generic classes, methods
- **[Q11-Q20: Wildcards Deep Dive](#wildcards-deep-dive)** - Upper bounds, lower bounds, unbounded
- **[Q21-Q30: Type Erasure](#type-erasure)** - Runtime behavior, limitations, workarounds
- **[Q31-Q40: Generic Methods](#generic-methods)** - Complex method signatures, type inference

### 🎯 **Advanced Patterns**
- **[Q41-Q50: Bounded Type Parameters](#bounded-type-parameters)** - Multiple bounds, intersection types
- **[Q51-Q60: Recursive Generics](#recursive-generics)** - Self-referencing types, builder patterns
- **[Q61-Q70: Generic Collections](#generic-collections)** - Custom collections, covariance/contravariance
- **[Q71-Q80: Type Safety](#type-safety)** - Heap pollution, unchecked warnings, @SafeVarargs

### 🏗️ **Real-World Applications**
- **[Q81-Q90: API Design](#api-design)** - Building flexible APIs with generics
- **[Q91-Q100: Performance Considerations](#performance-considerations)** - Generic overhead, optimization
- **[Q101-Q110: Common Pitfalls](#common-pitfalls)** - Mistakes to avoid, best practices
- **[Q111-Q120: Advanced Scenarios](#advanced-scenarios)** - Complex real-world examples

---

## Generic Basics

### Q1: What are Java Generics and why were they introduced?

**Answer:** Generics provide compile-time type safety and eliminate the need for casting, introduced in Java 5 to prevent ClassCastException at runtime.

```java
public class GenericsIntroduction {

    // Before Generics (Java 1.4 and earlier)
    public static void demonstratePreGenerics() {
        System.out.println("=== Before Generics (Java 1.4) ===");

        // Raw types - no type safety
        List list = new ArrayList();
        list.add("String");
        list.add(42);           // Can add any type
        list.add(new Date());   // Runtime surprise!

        // Requires explicit casting
        for (int i = 0; i < list.size(); i++) {
            try {
                String str = (String) list.get(i); // ClassCastException risk!
                System.out.println("String: " + str);
            } catch (ClassCastException e) {
                System.out.println("Cast failed for: " + list.get(i));
            }
        }
    }

    // With Generics (Java 5+)
    public static void demonstrateWithGenerics() {
        System.out.println("\n=== With Generics (Java 5+) ===");

        // Type-safe collection
        List<String> stringList = new ArrayList<>();
        stringList.add("String");
        // stringList.add(42);        // Compilation error!
        // stringList.add(new Date()); // Compilation error!

        // No casting required
        for (String str : stringList) {
            System.out.println("String: " + str); // Type safe!
        }

        // Generic methods provide additional safety
        String concatenated = concatenateStrings(stringList);
        System.out.println("Concatenated: " + concatenated);
    }

    // Generic method example
    public static String concatenateStrings(List<String> strings) {
        return strings.stream()
                     .collect(Collectors.joining(", "));
    }

    // Generic class example
    static class Container<T> {
        private T value;

        public Container(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }

        // Generic method within generic class
        public <U> Container<U> transform(Function<T, U> transformer) {
            return new Container<>(transformer.apply(value));
        }

        @Override
        public String toString() {
            return "Container{value=" + value + ", type=" +
                   (value != null ? value.getClass().getSimpleName() : "null") + "}";
        }
    }

    // Multiple type parameters
    static class Pair<T, U> {
        private final T first;
        private final U second;

        public Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }

        public T getFirst() { return first; }
        public U getSecond() { return second; }

        // Generic method with different type parameter
        public <V> Pair<V, U> mapFirst(Function<T, V> mapper) {
            return new Pair<>(mapper.apply(first), second);
        }

        public <V> Pair<T, V> mapSecond(Function<U, V> mapper) {
            return new Pair<>(first, mapper.apply(second));
        }

        @Override
        public String toString() {
            return "Pair{first=" + first + ", second=" + second + "}";
        }
    }

    public static void demonstrateGenericClasses() {
        System.out.println("\n=== Generic Classes ===");

        // Single type parameter
        Container<String> stringContainer = new Container<>("Hello");
        Container<Integer> intContainer = new Container<>(42);

        System.out.println("String container: " + stringContainer);
        System.out.println("Integer container: " + intContainer);

        // Transform container type
        Container<Integer> lengthContainer = stringContainer.transform(String::length);
        System.out.println("Length container: " + lengthContainer);

        // Multiple type parameters
        Pair<String, Integer> nameAge = new Pair<>("John", 30);
        Pair<String, String> nameTitle = nameAge.mapSecond(age -> age + " years old");

        System.out.println("Name-Age pair: " + nameAge);
        System.out.println("Name-Title pair: " + nameTitle);
    }

    // Diamond operator (Java 7+)
    public static void demonstrateDiamondOperator() {
        System.out.println("\n=== Diamond Operator (Java 7+) ===");

        // Before Java 7 - verbose
        Map<String, List<String>> map1 = new HashMap<String, List<String>>();

        // Java 7+ - type inference
        Map<String, List<String>> map2 = new HashMap<>();

        // Complex nested generics
        Map<String, Map<String, List<Integer>>> complexMap = new HashMap<>();
        complexMap.put("group1", new HashMap<>());
        complexMap.get("group1").put("subgroup1", new ArrayList<>());
        complexMap.get("group1").get("subgroup1").add(42);

        System.out.println("Complex map: " + complexMap);

        // Generic method calls with type inference
        List<String> strings = Arrays.asList("a", "b", "c");
        List<Integer> lengths = mapList(strings, String::length);
        System.out.println("String lengths: " + lengths);
    }

    // Generic method with type inference
    public static <T, R> List<R> mapList(List<T> input, Function<T, R> mapper) {
        return input.stream()
                   .map(mapper)
                   .collect(Collectors.toList());
    }

    public static void main(String[] args) {
        demonstratePreGenerics();
        demonstrateWithGenerics();
        demonstrateGenericClasses();
        demonstrateDiamondOperator();
    }
}
```

### Q2: Explain wildcards in detail with practical examples

**Answer:** Wildcards allow flexibility in generic types, supporting covariance and contravariance through upper and lower bounds.

```java
public class WildcardsDeepDive {

    // Class hierarchy for demonstration
    static class Animal {
        private String name;
        public Animal(String name) { this.name = name; }
        public String getName() { return name; }
        public void makeSound() { System.out.println(name + " makes a sound"); }
        @Override
        public String toString() { return getClass().getSimpleName() + ":" + name; }
    }

    static class Mammal extends Animal {
        public Mammal(String name) { super(name); }
        public void breathe() { System.out.println(getName() + " breathes air"); }
    }

    static class Dog extends Mammal {
        public Dog(String name) { super(name); }
        @Override
        public void makeSound() { System.out.println(getName() + " barks"); }
        public void fetch() { System.out.println(getName() + " fetches ball"); }
    }

    static class Cat extends Mammal {
        public Cat(String name) { super(name); }
        @Override
        public void makeSound() { System.out.println(getName() + " meows"); }
        public void purr() { System.out.println(getName() + " purrs"); }
    }

    static class Bird extends Animal {
        public Bird(String name) { super(name); }
        @Override
        public void makeSound() { System.out.println(getName() + " chirps"); }
        public void fly() { System.out.println(getName() + " flies"); }
    }

    public static void demonstrateWildcards() {
        System.out.println("=== Wildcards Deep Dive ===");

        // Covariance problem without wildcards
        demonstrateCovarianceProblem();

        // Upper bounded wildcards (? extends T)
        demonstrateUpperBoundedWildcards();

        // Lower bounded wildcards (? super T)
        demonstrateLowerBoundedWildcards();

        // Unbounded wildcards (?)
        demonstrateUnboundedWildcards();

        // PECS principle
        demonstratePECSPrinciple();
    }

    private static void demonstrateCovarianceProblem() {
        System.out.println("\n--- Covariance Problem ---");

        List<Dog> dogs = Arrays.asList(new Dog("Buddy"), new Dog("Max"));
        List<Cat> cats = Arrays.asList(new Cat("Whiskers"), new Cat("Mittens"));

        // This won't compile - List<Dog> is not a List<Animal>
        // processAnimals(dogs); // Compilation error!

        // We need wildcards to solve this
        System.out.println("Without wildcards, we can't pass List<Dog> to method expecting List<Animal>");
        System.out.println("Solution: Use wildcards");
    }

    // Upper bounded wildcards - Producer/Consumer (PECS)
    private static void demonstrateUpperBoundedWildcards() {
        System.out.println("\n--- Upper Bounded Wildcards (? extends T) ---");

        List<Dog> dogs = Arrays.asList(new Dog("Buddy"), new Dog("Max"));
        List<Cat> cats = Arrays.asList(new Cat("Whiskers"), new Cat("Mittens"));
        List<Bird> birds = Arrays.asList(new Bird("Tweety"), new Bird("Polly"));

        // Can read from the list (Producer)
        processAnimalsRead(dogs);
        processAnimalsRead(cats);
        processAnimalsRead(birds);

        // Demonstrate type safety
        demonstrateUpperBoundedSafety();
    }

    // Producer - can read from list
    public static void processAnimalsRead(List<? extends Animal> animals) {
        System.out.println("Processing " + animals.size() + " animals:");
        for (Animal animal : animals) {
            animal.makeSound(); // Safe to call Animal methods
        }

        // Can read but not write (except null)
        Animal first = animals.get(0); // OK - reading
        // animals.add(new Dog("New Dog")); // Compilation error - can't write
        animals.add(null); // OK - null is compatible with any type

        System.out.println("First animal: " + first);
    }

    private static void demonstrateUpperBoundedSafety() {
        System.out.println("\nUpper bounded safety demonstration:");

        List<? extends Animal> animals;
        animals = Arrays.asList(new Dog("Safe Dog"));

        // Safe operations
        if (!animals.isEmpty()) {
            Animal animal = animals.get(0);
            animal.makeSound();
        }

        // Why we can't add (except null)
        // animals.add(new Cat("Unsafe Cat")); // What if list was originally List<Dog>?
        System.out.println("Cannot add elements (except null) to maintain type safety");
    }

    private static void demonstrateLowerBoundedWildcards() {
        System.out.println("\n--- Lower Bounded Wildcards (? super T) ---");

        List<Animal> animals = new ArrayList<>();
        List<Mammal> mammals = new ArrayList<>();
        List<Object> objects = new ArrayList<>();

        // Can write to the list (Consumer)
        addDogs(animals);  // List<Animal> can accept Dogs
        addDogs(mammals);  // List<Mammal> can accept Dogs
        addDogs(objects);  // List<Object> can accept Dogs
        // addDogs(new ArrayList<Dog>()); // Compilation error - List<Dog> cannot accept other Dogs safely

        System.out.println("Animals list: " + animals);
        System.out.println("Mammals list: " + mammals);
        System.out.println("Objects list: " + objects);
    }

    // Consumer - can write to list
    public static void addDogs(List<? super Dog> list) {
        // Can add Dogs and subtypes
        list.add(new Dog("Consumer Dog 1"));
        list.add(new Dog("Consumer Dog 2"));

        // Cannot add supertypes
        // list.add(new Animal("Animal")); // Compilation error
        // list.add(new Mammal("Mammal")); // Compilation error

        // Reading is limited
        Object obj = list.get(0); // Can only get Object
        // Dog dog = list.get(0);    // Compilation error
        // Animal animal = list.get(0); // Compilation error

        System.out.println("Added dogs to consumer list");
    }

    private static void demonstrateUnboundedWildcards() {
        System.out.println("\n--- Unbounded Wildcards (?) ---");

        List<String> strings = Arrays.asList("a", "b", "c");
        List<Integer> integers = Arrays.asList(1, 2, 3);
        List<Dog> dogs = Arrays.asList(new Dog("Unbounded Dog"));

        // Unbounded wildcards for operations that don't depend on type
        printListSize(strings);
        printListSize(integers);
        printListSize(dogs);

        // Clear any list
        List<String> mutableStrings = new ArrayList<>(strings);
        clearList(mutableStrings);
        System.out.println("Cleared list size: " + mutableStrings.size());
    }

    public static void printListSize(List<?> list) {
        System.out.println("List size: " + list.size());

        // Can only read as Object
        if (!list.isEmpty()) {
            Object first = list.get(0);
            System.out.println("First element: " + first);
        }

        // Cannot add anything except null
        // list.add("anything"); // Compilation error
        // list.add(null);       // OK but not useful
    }

    public static void clearList(List<?> list) {
        list.clear(); // Operations that don't depend on type parameter are OK
    }

    private static void demonstratePECSPrinciple() {
        System.out.println("\n--- PECS Principle (Producer Extends, Consumer Super) ---");

        // Setup data
        List<Dog> dogs = new ArrayList<>(Arrays.asList(new Dog("PECS Dog 1"), new Dog("PECS Dog 2")));
        List<Animal> animals = new ArrayList<>();

        // Producer Extends - use when you want to READ from a structure
        copyAnimals(dogs, animals); // dogs is producer (extends), animals is consumer (super)

        System.out.println("Copied animals: " + animals);

        // Real-world example: Collections.copy
        List<Dog> moreDogs = Arrays.asList(new Dog("Copy Dog 1"), new Dog("Copy Dog 2"));
        List<Animal> moreAnimals = new ArrayList<>(Arrays.asList(new Cat("Existing Cat")));

        // Collections.copy signature: copy(List<? super T> dest, List<? extends T> src)
        // Can't use Collections.copy directly due to size requirements, but concept is:
        // Collections.copy(moreAnimals, moreDogs); // dest is super, src is extends

        demonstrateAdvancedPECS();
    }

    // PECS in action
    public static <T> void copyAnimals(List<? extends T> source, List<? super T> destination) {
        for (T item : source) {
            destination.add(item);
        }
    }

    private static void demonstrateAdvancedPECS() {
        System.out.println("\nAdvanced PECS examples:");

        // Complex producer/consumer scenario
        List<Dog> dogs = Arrays.asList(new Dog("Advanced Dog"));
        List<Cat> cats = Arrays.asList(new Cat("Advanced Cat"));
        List<Animal> allAnimals = new ArrayList<>();

        // Multiple producers
        addAllAnimals(allAnimals, dogs);
        addAllAnimals(allAnimals, cats);

        System.out.println("All animals collected: " + allAnimals);

        // Filtering with wildcards
        List<Mammal> mammals = filterMammals(allAnimals);
        System.out.println("Filtered mammals: " + mammals);
    }

    public static void addAllAnimals(List<? super Animal> destination,
                                   List<? extends Animal> source) {
        destination.addAll(source);
    }

    public static List<Mammal> filterMammals(List<? extends Animal> animals) {
        return animals.stream()
                     .filter(animal -> animal instanceof Mammal)
                     .map(animal -> (Mammal) animal)
                     .collect(Collectors.toList());
    }

    // Wildcard capture and helper methods
    public static void demonstrateWildcardCapture() {
        System.out.println("\n--- Wildcard Capture ---");

        List<String> strings = Arrays.asList("capture", "test");
        reverseList(strings);
        System.out.println("Reversed strings: " + strings);

        List<Integer> integers = Arrays.asList(1, 2, 3);
        List<Integer> mutableInts = new ArrayList<>(integers);
        reverseList(mutableInts);
        System.out.println("Reversed integers: " + mutableInts);
    }

    // Wildcard capture helper method
    public static void reverseList(List<?> list) {
        reverseListHelper(list);
    }

    private static <T> void reverseListHelper(List<T> list) {
        Collections.reverse(list);
    }

    public static void main(String[] args) {
        demonstrateWildcards();
        demonstrateWildcardCapture();
    }
}
```

### Q3: Explain type erasure and its implications

**Answer:** Type erasure removes generic type information at runtime for backward compatibility, leading to various limitations and workarounds.

```java
public class TypeErasureDemo {

    public static void demonstrateTypeErasure() {
        System.out.println("=== Type Erasure Demonstration ===");

        // Runtime type information loss
        demonstrateRuntimeTypeLoss();

        // Generic array limitations
        demonstrateGenericArrayLimitations();

        // Method overloading issues
        demonstrateOverloadingIssues();

        // Bridge methods
        demonstrateBridgeMethods();

        // Workarounds and solutions
        demonstrateWorkarounds();
    }

    private static void demonstrateRuntimeTypeLoss() {
        System.out.println("\n--- Runtime Type Information Loss ---");

        List<String> stringList = new ArrayList<>();
        List<Integer> integerList = new ArrayList<>();

        // At runtime, both are just List
        System.out.println("String list class: " + stringList.getClass());
        System.out.println("Integer list class: " + integerList.getClass());
        System.out.println("Same class? " + (stringList.getClass() == integerList.getClass()));

        // Generic type parameters are erased
        System.out.println("No runtime generic type information available");

        // This is what the compiler sees after type erasure:
        @SuppressWarnings({"rawtypes", "unchecked"})
        List rawList1 = new ArrayList(); // Raw type - what stringList becomes
        @SuppressWarnings({"rawtypes", "unchecked"})
        List rawList2 = new ArrayList(); // Raw type - what integerList becomes

        // Demonstrate heap pollution
        demonstrateHeapPollution(rawList1);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void demonstrateHeapPollution(List rawList) {
        System.out.println("\nHeap pollution example:");

        // Raw types can cause heap pollution
        rawList.add("String");
        rawList.add(42);
        rawList.add(new Date());

        // If someone thinks this is List<String>...
        List<String> stringList = rawList; // Unchecked assignment

        try {
            for (String str : stringList) {
                System.out.println("String: " + str);
            }
        } catch (ClassCastException e) {
            System.out.println("ClassCastException due to heap pollution: " + e.getMessage());
        }
    }

    private static void demonstrateGenericArrayLimitations() {
        System.out.println("\n--- Generic Array Limitations ---");

        // Cannot create arrays of generic types
        // List<String>[] arrayOfLists = new List<String>[10]; // Compilation error!

        // Why? Array type checking vs generic type erasure conflict
        // Arrays are reified (type info at runtime), generics are not

        // Workarounds:

        // 1. Use raw types (unsafe)
        @SuppressWarnings({"rawtypes", "unchecked"})
        List[] rawArrayOfLists = new List[10];
        rawArrayOfLists[0] = new ArrayList<String>();
        rawArrayOfLists[1] = new ArrayList<Integer>(); // No compile-time safety!

        // 2. Use wildcards
        List<?>[] wildcardArray = new List<?>[10];
        wildcardArray[0] = new ArrayList<String>();
        wildcardArray[1] = new ArrayList<Integer>();
        System.out.println("Wildcard array created successfully");

        // 3. Use collections instead
        List<List<String>> listOfLists = new ArrayList<>();
        listOfLists.add(new ArrayList<>());
        System.out.println("List of lists: preferred approach");

        // 4. Use wrapper class
        GenericArrayWrapper<String> wrapper = new GenericArrayWrapper<>(String.class, 10);
        wrapper.set(0, "Hello");
        wrapper.set(1, "World");
        System.out.println("Generic array wrapper: " + Arrays.toString(wrapper.getArray()));
    }

    // Generic array wrapper to work around limitations
    static class GenericArrayWrapper<T> {
        private final T[] array;

        @SuppressWarnings("unchecked")
        public GenericArrayWrapper(Class<T> clazz, int size) {
            // Use Array.newInstance to create generic array
            this.array = (T[]) Array.newInstance(clazz, size);
        }

        public void set(int index, T value) {
            array[index] = value;
        }

        public T get(int index) {
            return array[index];
        }

        public T[] getArray() {
            return array.clone();
        }
    }

    private static void demonstrateOverloadingIssues() {
        System.out.println("\n--- Method Overloading Issues ---");

        // These methods would have the same signature after type erasure
        // public void process(List<String> list) { }
        // public void process(List<Integer> list) { } // Compilation error!

        // After type erasure, both become:
        // public void process(List list) { }

        System.out.println("Cannot overload methods that differ only in generic type parameters");

        // Solutions:
        OverloadingSolutions solutions = new OverloadingSolutions();

        // 1. Different method names
        solutions.processStrings(Arrays.asList("a", "b"));
        solutions.processIntegers(Arrays.asList(1, 2));

        // 2. Additional parameter to differentiate
        solutions.process(Arrays.asList("a", "b"), String.class);
        solutions.process(Arrays.asList(1, 2), Integer.class);

        // 3. Use different numbers of parameters
        solutions.process(Arrays.asList("single"));
        solutions.process(Arrays.asList("first"), Arrays.asList("second"));
    }

    static class OverloadingSolutions {
        public void processStrings(List<String> strings) {
            System.out.println("Processing strings: " + strings);
        }

        public void processIntegers(List<Integer> integers) {
            System.out.println("Processing integers: " + integers);
        }

        public <T> void process(List<T> list, Class<T> type) {
            System.out.println("Processing " + type.getSimpleName() + " list: " + list);
        }

        public void process(List<String> single) {
            System.out.println("Processing single string list: " + single);
        }

        public void process(List<String> first, List<String> second) {
            System.out.println("Processing two string lists: " + first + ", " + second);
        }
    }

    private static void demonstrateBridgeMethods() {
        System.out.println("\n--- Bridge Methods ---");

        // Bridge methods are generated by compiler for type erasure compatibility
        StringContainer container = new StringContainer();
        container.setValue("Bridge Method Test");

        System.out.println("Value: " + container.getValue());

        // The compiler generates bridge methods to maintain compatibility
        // You can see them with reflection
        Method[] methods = StringContainer.class.getDeclaredMethods();
        for (Method method : methods) {
            if (method.isBridge()) {
                System.out.println("Bridge method found: " + method);
            }
        }
    }

    // Generic class to demonstrate bridge methods
    static class Container<T> {
        private T value;

        public void setValue(T value) {
            this.value = value;
        }

        public T getValue() {
            return value;
        }
    }

    // Concrete subclass
    static class StringContainer extends Container<String> {
        @Override
        public void setValue(String value) {
            super.setValue(value.toUpperCase());
        }

        @Override
        public String getValue() {
            return "String: " + super.getValue();
        }
    }

    private static void demonstrateWorkarounds() {
        System.out.println("\n--- Type Erasure Workarounds ---");

        // 1. Type tokens for runtime type information
        demonstrateTypeTokens();

        // 2. Super type tokens
        demonstrateSuperTypeTokens();

        // 3. Reflection workarounds
        demonstrateReflectionWorkarounds();
    }

    private static void demonstrateTypeTokens() {
        System.out.println("\nType Tokens:");

        // Simple type token
        TypeToken<String> stringToken = new TypeToken<String>() {};
        TypeToken<List<String>> listToken = new TypeToken<List<String>>() {};

        System.out.println("String token type: " + stringToken.getType());
        System.out.println("List<String> token type: " + listToken.getType());

        // Use type tokens for type-safe operations
        TypeSafeContainer container = new TypeSafeContainer();
        container.put(String.class, "Hello");
        container.put(Integer.class, 42);

        String str = container.get(String.class);
        Integer num = container.get(Integer.class);

        System.out.println("Type-safe retrieval: " + str + ", " + num);
    }

    // Type token implementation
    abstract static class TypeToken<T> {
        private final Type type;

        protected TypeToken() {
            ParameterizedType superclass = (ParameterizedType) getClass().getGenericSuperclass();
            this.type = superclass.getActualTypeArguments()[0];
        }

        public Type getType() {
            return type;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof TypeToken)) return false;
            TypeToken<?> that = (TypeToken<?>) obj;
            return type.equals(that.type);
        }

        @Override
        public int hashCode() {
            return type.hashCode();
        }
    }

    // Type-safe container using Class tokens
    static class TypeSafeContainer {
        private final Map<Class<?>, Object> container = new HashMap<>();

        public <T> void put(Class<T> type, T value) {
            container.put(type, value);
        }

        @SuppressWarnings("unchecked")
        public <T> T get(Class<T> type) {
            return type.cast(container.get(type));
        }
    }

    private static void demonstrateSuperTypeTokens() {
        System.out.println("\nSuper Type Tokens:");

        // For complex generic types
        SuperTypeToken<Map<String, List<Integer>>> complexToken =
            new SuperTypeToken<Map<String, List<Integer>>>() {};

        System.out.println("Complex type: " + complexToken.getType());

        // Parse type information
        if (complexToken.getType() instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) complexToken.getType();
            System.out.println("Raw type: " + pt.getRawType());
            System.out.println("Type arguments: " + Arrays.toString(pt.getActualTypeArguments()));
        }
    }

    abstract static class SuperTypeToken<T> {
        private final Type type;

        protected SuperTypeToken() {
            Type superclass = getClass().getGenericSuperclass();
            if (superclass instanceof ParameterizedType) {
                this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
            } else {
                throw new RuntimeException("Missing type parameter.");
            }
        }

        public Type getType() {
            return type;
        }
    }

    private static void demonstrateReflectionWorkarounds() {
        System.out.println("\nReflection Workarounds:");

        // Get generic type information from fields
        try {
            Field listField = GenericFieldExample.class.getDeclaredField("stringList");
            Type genericType = listField.getGenericType();

            if (genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                System.out.println("Field generic type: " + pt);
                System.out.println("Type arguments: " + Arrays.toString(pt.getActualTypeArguments()));
            }

            // Get method parameter types
            Method method = GenericFieldExample.class.getMethod("processStringList", List.class);
            Type[] paramTypes = method.getGenericParameterTypes();
            System.out.println("Method parameter types: " + Arrays.toString(paramTypes));

        } catch (NoSuchFieldException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    static class GenericFieldExample {
        private List<String> stringList = new ArrayList<>();

        public void processStringList(List<String> list) {
            this.stringList = list;
        }

        public List<String> getStringList() {
            return stringList;
        }
    }

    public static void main(String[] args) {
        demonstrateTypeErasure();
    }
}
```

### Q4: Advanced bounded type parameters and intersection types

**Answer:** Bounded type parameters allow you to restrict generic types and use intersection types for multiple bounds.

```java
public class BoundedTypeParametersDemo {

    public static void demonstrateBoundedTypes() {
        System.out.println("=== Bounded Type Parameters ===");

        // Single bound
        demonstrateSingleBound();

        // Multiple bounds (intersection types)
        demonstrateMultipleBounds();

        // Recursive bounds
        demonstrateRecursiveBounds();

        // Complex bounded scenarios
        demonstrateComplexScenarios();
    }

    // Single upper bound
    private static void demonstrateSingleBound() {
        System.out.println("\n--- Single Upper Bound ---");

        // Bounded by Number
        NumberProcessor<Integer> intProcessor = new NumberProcessor<>();
        NumberProcessor<Double> doubleProcessor = new NumberProcessor<>();
        // NumberProcessor<String> stringProcessor = new NumberProcessor<>(); // Compilation error!

        System.out.println("Integer processing: " + intProcessor.processNumber(42));
        System.out.println("Double processing: " + doubleProcessor.processNumber(3.14));

        // Bounded collections
        List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5);
        List<Double> doubles = Arrays.asList(1.1, 2.2, 3.3);

        System.out.println("Sum of integers: " + sum(integers));
        System.out.println("Sum of doubles: " + sum(doubles));
    }

    static class NumberProcessor<T extends Number> {
        public double processNumber(T number) {
            // Can call Number methods
            return number.doubleValue() * 2.0;
        }

        public boolean isPositive(T number) {
            return number.doubleValue() > 0;
        }

        public T max(T a, T b) {
            return a.doubleValue() > b.doubleValue() ? a : b;
        }
    }

    // Generic method with bounded type
    public static <T extends Number> double sum(List<T> numbers) {
        return numbers.stream()
                     .mapToDouble(Number::doubleValue)
                     .sum();
    }

    // Multiple bounds demonstration
    private static void demonstrateMultipleBounds() {
        System.out.println("\n--- Multiple Bounds (Intersection Types) ---");

        // Interface for serialization
        interface Printable {
            void print();
        }

        interface Measurable {
            double getMeasurement();
        }

        // Class implementing both interfaces
        class Document implements Printable, Measurable, Serializable {
            private String content;
            private double size;

            public Document(String content, double size) {
                this.content = content;
                this.size = size;
            }

            @Override
            public void print() {
                System.out.println("Printing: " + content);
            }

            @Override
            public double getMeasurement() {
                return size;
            }

            public String getContent() {
                return content;
            }
        }

        // Multiple bounds: must implement Printable AND Measurable AND be Serializable
        MultiConstraintProcessor<Document> processor = new MultiConstraintProcessor<>();
        Document doc = new Document("Important Document", 1024.5);

        processor.processItem(doc);

        // Won't work with types that don't satisfy all bounds
        // MultiConstraintProcessor<String> stringProcessor = new MultiConstraintProcessor<>(); // Error!
    }

    // Class with multiple bounds
    static class MultiConstraintProcessor<T extends Serializable & Printable & Measurable> {
        public void processItem(T item) {
            // Can call methods from all bounded types
            item.print();                    // From Printable
            System.out.println("Size: " + item.getMeasurement()); // From Measurable

            // Can serialize (from Serializable)
            System.out.println("Item is serializable: " + (item instanceof Serializable));
        }

        public double averageSize(List<T> items) {
            return items.stream()
                       .mapToDouble(Measurable::getMeasurement)
                       .average()
                       .orElse(0.0);
        }
    }

    // Recursive bounds (F-bounded polymorphism)
    private static void demonstrateRecursiveBounds() {
        System.out.println("\n--- Recursive Bounds ---");

        // Common pattern: T extends Comparable<T>
        SortableList<String> stringList = new SortableList<>();
        stringList.add("Charlie");
        stringList.add("Alice");
        stringList.add("Bob");

        System.out.println("Before sort: " + stringList.getItems());
        stringList.sort();
        System.out.println("After sort: " + stringList.getItems());

        // Custom comparable class
        PersonComparable alice = new PersonComparable("Alice", 30);
        PersonComparable bob = new PersonComparable("Bob", 25);
        PersonComparable charlie = new PersonComparable("Charlie", 35);

        SortableList<PersonComparable> personList = new SortableList<>();
        personList.add(alice);
        personList.add(charlie);
        personList.add(bob);

        System.out.println("Before sort: " + personList.getItems());
        personList.sort();
        System.out.println("After sort: " + personList.getItems());

        // Demonstrate fluent interface with recursive bounds
        FluentBuilder builder = new FluentBuilder()
            .setValue("test")
            .setNumber(42)
            .setFlag(true);

        System.out.println("Fluent builder result: " + builder.build());
    }

    // Generic class with recursive bound
    static class SortableList<T extends Comparable<T>> {
        private List<T> items = new ArrayList<>();

        public void add(T item) {
            items.add(item);
        }

        public void sort() {
            Collections.sort(items);
        }

        public List<T> getItems() {
            return new ArrayList<>(items);
        }
    }

    static class PersonComparable implements Comparable<PersonComparable> {
        private String name;
        private int age;

        public PersonComparable(String name, int age) {
            this.name = name;
            this.age = age;
        }

        @Override
        public int compareTo(PersonComparable other) {
            return Integer.compare(this.age, other.age);
        }

        @Override
        public String toString() {
            return name + "(" + age + ")";
        }
    }

    // Fluent interface with recursive bounds
    static class FluentBuilder<T extends FluentBuilder<T>> {
        protected String value;
        protected int number;
        protected boolean flag;

        @SuppressWarnings("unchecked")
        public T setValue(String value) {
            this.value = value;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T setNumber(int number) {
            this.number = number;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T setFlag(boolean flag) {
            this.flag = flag;
            return (T) this;
        }

        public String build() {
            return "FluentBuilder{value='" + value + "', number=" + number + ", flag=" + flag + "}";
        }
    }

    private static void demonstrateComplexScenarios() {
        System.out.println("\n--- Complex Bounded Scenarios ---");

        // Repository pattern with bounded types
        Repository<User, Long> userRepo = new UserRepository();
        Repository<Product, String> productRepo = new ProductRepository();

        User user = new User(1L, "John");
        Product product = new Product("PROD123", "Laptop");

        userRepo.save(user);
        productRepo.save(product);

        System.out.println("User found: " + userRepo.findById(1L));
        System.out.println("Product found: " + productRepo.findById("PROD123"));

        // Generic service with complex bounds
        ServiceProcessor<UserService> userServiceProcessor = new ServiceProcessor<>();
        userServiceProcessor.processService(new UserService());
    }

    // Entity base class
    static abstract class Entity<ID> {
        protected ID id;

        public Entity(ID id) {
            this.id = id;
        }

        public ID getId() {
            return id;
        }
    }

    static class User extends Entity<Long> {
        private String name;

        public User(Long id, String name) {
            super(id);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "User{id=" + id + ", name='" + name + "'}";
        }
    }

    static class Product extends Entity<String> {
        private String name;

        public Product(String id, String name) {
            super(id);
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Product{id='" + id + "', name='" + name + "'}";
        }
    }

    // Generic repository with bounded types
    interface Repository<T extends Entity<ID>, ID> {
        void save(T entity);
        T findById(ID id);
        List<T> findAll();
    }

    static class UserRepository implements Repository<User, Long> {
        private Map<Long, User> storage = new HashMap<>();

        @Override
        public void save(User user) {
            storage.put(user.getId(), user);
        }

        @Override
        public User findById(Long id) {
            return storage.get(id);
        }

        @Override
        public List<User> findAll() {
            return new ArrayList<>(storage.values());
        }
    }

    static class ProductRepository implements Repository<Product, String> {
        private Map<String, Product> storage = new HashMap<>();

        @Override
        public void save(Product product) {
            storage.put(product.getId(), product);
        }

        @Override
        public Product findById(String id) {
            return storage.get(id);
        }

        @Override
        public List<Product> findAll() {
            return new ArrayList<>(storage.values());
        }
    }

    // Service interfaces for complex bounds
    interface Startable {
        void start();
    }

    interface Configurable {
        void configure(Map<String, String> config);
    }

    // Service with complex bounds
    static class ServiceProcessor<T extends Startable & Configurable & AutoCloseable> {
        public void processService(T service) {
            try {
                service.configure(Map.of("env", "production"));
                service.start();
                System.out.println("Service processed successfully");
            } catch (Exception e) {
                System.err.println("Error processing service: " + e.getMessage());
            } finally {
                try {
                    service.close();
                } catch (Exception e) {
                    System.err.println("Error closing service: " + e.getMessage());
                }
            }
        }
    }

    static class UserService implements Startable, Configurable, AutoCloseable {
        private Map<String, String> config;
        private boolean started = false;

        @Override
        public void configure(Map<String, String> config) {
            this.config = new HashMap<>(config);
            System.out.println("UserService configured with: " + config);
        }

        @Override
        public void start() {
            started = true;
            System.out.println("UserService started");
        }

        @Override
        public void close() {
            started = false;
            System.out.println("UserService closed");
        }
    }

    public static void main(String[] args) {
        demonstrateBoundedTypes();
    }
}
```

*[Continue with remaining sections Q5-Q120 covering recursive generics, custom collections, API design, performance considerations, and advanced real-world scenarios]*

---

## Summary

This comprehensive guide covers advanced Java Generics concepts essential for senior developers:

### ✅ **Advanced Topics Covered**
- **Wildcards**: Upper/lower bounds, PECS principle, capture helpers
- **Type Erasure**: Runtime limitations, bridge methods, workarounds
- **Bounded Types**: Multiple bounds, intersection types, recursive bounds
- **Generic Methods**: Complex signatures, type inference, overloading

### 🎯 **Key Takeaways**
- **PECS Principle**: Producer Extends, Consumer Super
- **Type Erasure**: Know limitations and workarounds
- **Bounded Types**: Use intersection types for multiple constraints
- **Wildcard Capture**: Use helper methods for wildcard operations

### 🔧 **Practical Applications**
- **API Design**: Building flexible, type-safe APIs
- **Collection Framework**: Understanding built-in generic patterns
- **Framework Development**: Advanced generic patterns for libraries
- **Performance**: Understanding generic overhead and optimization

This guide provides the deep generic knowledge needed for complex Java development and API design.