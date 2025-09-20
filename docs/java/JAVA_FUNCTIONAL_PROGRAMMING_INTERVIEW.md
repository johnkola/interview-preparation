# Java 17 Functional Programming Interview Questions and Examples

> **Complete guide using Java 17 features including records, sealed classes, pattern matching, and text blocks**

## Table of Contents
- [Advanced Lambda Patterns](#advanced-lambda-patterns)
- [Advanced Functional Interfaces](#advanced-functional-interfaces)
- [Functional Programming with Streams](#functional-programming-with-streams)
- [Advanced Optional Patterns](#advanced-optional-patterns)
- [Advanced CompletableFuture Patterns](#advanced-completablefuture-patterns)
- [Functional Programming Patterns](#functional-programming-patterns)
- [Real-World Scenarios](#real-world-scenarios)

---

*📋 **Note:** For basic Lambda expressions, built-in Functional Interfaces, and Method References, see: `JAVA_17_1Z0_829_EXAM_INTERVIEW_GUIDE.md`*

## Advanced Lambda Patterns

### Q1: Advanced Lambda Expression Patterns
```java
// Java 17 with pattern matching and advanced lambda usage
public class AdvancedLambdaPatterns {

    // Multi-line lambda with pattern matching (Java 17)
    Function<Object, String> advancedProcessor = obj -> switch (obj) {
        case null -> "null value";
        case String s when s.length() > 10 -> "Long string: " + s.substring(0, 10) + "...";
        case String s -> "String: " + s;
        case Integer i when i < 0 -> "Negative integer: " + i;
        case Integer i -> "Positive integer: " + i;
        case List<?> list when list.isEmpty() -> "Empty list";
        case List<?> list -> "List with " + list.size() + " items";
        default -> "Unknown type: " + obj.getClass().getSimpleName();
    };

    // Lambda with complex control flow
    BiFunction<List<String>, Predicate<String>, Map<Boolean, List<String>>> partitioner =
        (list, predicate) -> list.stream()
            .collect(Collectors.partitioningBy(predicate));
}

## Advanced Functional Interfaces

### Q2: Create custom Functional Interfaces for complex use cases
```java
// Advanced custom functional interfaces with Java 17 features
@FunctionalInterface
interface TriFunction<T, U, V, R> {
    R apply(T t, U u, V v);

    default <W> TriFunction<T, U, V, W> andThen(Function<? super R, ? extends W> after) {
        return (t, u, v) -> after.apply(apply(t, u, v));
    }
}

// Sealed interface for controlled implementations (Java 17)
sealed interface Result<T> permits Result.Success, Result.Failure {
    record Success<T>(T value) implements Result<T> {}
    record Failure<T>(String error) implements Result<T> {}
}

@FunctionalInterface
interface ThrowingSupplier<T> {
    T get() throws Exception;

    static <T> Supplier<Result<T>> safe(ThrowingSupplier<T> supplier) {
        return () -> {
            try {
                return new Result.Success<>(supplier.get());
            } catch (Exception e) {
                return new Result.Failure<>(e.getMessage());
            }
        };
    }
}

// Advanced functional interface with complex generics
@FunctionalInterface
interface AsyncProcessor<T, R> {
    CompletableFuture<R> processAsync(T input);

    default <S> AsyncProcessor<T, S> thenCompose(Function<R, CompletableFuture<S>> mapper) {
        return input -> processAsync(input).thenCompose(mapper);
    }
}
```

---

## Functional Programming with Streams

*📋 **Note:** For comprehensive Stream API coverage including detailed operations, intermediate/terminal operations, custom collectors, parallel processing, and performance optimization, see: `JAVA_COLLECTIONS_STREAMS.md` and `JAVA_17_1Z0_829_EXAM_INTERVIEW_GUIDE.md`*

### Q11: How do functional programming patterns integrate with Streams?

**Answer:** Focus on advanced functional patterns that build upon basic Stream operations:

```java
// Advanced functional patterns with streams
public class AdvancedFunctionalStreamPatterns {

    // Monad-like chaining with error handling
    public <T, R> Function<List<T>, List<R>> createSafeMapper(Function<T, R> mapper) {
        return list -> list.stream()
            .map(item -> Try.of(() -> mapper.apply(item)))
            .filter(Try::isSuccess)
            .map(Try::get)
            .toList();
    }

    // Functional composition in stream pipeline
    public Function<List<String>, List<String>> createTextProcessor() {
        Function<String, String> normalize = String::toLowerCase;
        Function<String, String> trim = String::trim;
        Function<String, String> capitalize = s -> s.substring(0, 1).toUpperCase() + s.substring(1);

        return list -> list.stream()
            .map(normalize.compose(trim).andThen(capitalize))
            .toList();
    }
}

---

## Advanced Optional Patterns

*📋 **Note:** For basic Optional usage, creation, and best practices, see: `JAVA_17_1Z0_829_EXAM_INTERVIEW_GUIDE.md`*

### Q3: Advanced Optional patterns with functional composition
```java
// Advanced Optional patterns for functional programming
public class AdvancedOptionalPatterns {

    // Optional as a Monad with complex chaining
    public Optional<String> processUserProfile(String userId) {
        return findUser(userId)
            .filter(User::isActive)
            .map(User::getProfile)
            .filter(Profile::isComplete)
            .map(Profile::getDisplayName)
            .map(String::toUpperCase);
    }

    // Combining multiple Optionals with functional composition
    public Optional<OrderSummary> createOrderSummary(String orderId) {
        return Optional.of(orderId)
            .map(this::findOrder)
            .flatMap(Function.identity())
            .flatMap(order ->
                findCustomer(order.customerId())
                    .flatMap(customer ->
                        calculateTax(order)
                            .map(tax -> new OrderSummary(order, customer, tax))
                    )
            );
    }

    // Optional with error accumulation using Either pattern
    public Either<List<String>, User> validateAndCreateUser(UserData data) {
        var errors = new ArrayList<String>();

        var nameOpt = validateName(data.name()).or(() -> {
            errors.add("Invalid name");
            return Optional.empty();
        });

        var emailOpt = validateEmail(data.email()).or(() -> {
            errors.add("Invalid email");
            return Optional.empty();
        });

        if (errors.isEmpty()) {
            return Either.right(new User(nameOpt.get(), emailOpt.get()));
        } else {
            return Either.left(errors);
        }
    }
}

---

## Advanced CompletableFuture Patterns

*📋 **Note:** For basic CompletableFuture usage including creation, chaining, and combining, see: `JAVA_CONCURRENCY_COMPREHENSIVE.md`*

### Q4: Advanced CompletableFuture functional patterns
```java
// Advanced CompletableFuture patterns with functional programming
public class AdvancedCompletableFuturePatterns {

    // Functional composition with CompletableFuture as a Monad
    public CompletableFuture<String> processDataPipeline(String input) {
        return CompletableFuture
            .supplyAsync(() -> validateInput(input))
            .thenCompose(this::enrichData)
            .thenCompose(this::transformData)
            .thenCompose(this::saveData)
            .exceptionally(this::handleError);
    }

    // Combining multiple async operations with functional style
    public CompletableFuture<Report> generateComplexReport(String reportId) {
        var dataFuture = fetchReportData(reportId);
        var templateFuture = loadTemplate(reportId);
        var configFuture = getReportConfig(reportId);

        return CompletableFuture.allOf(dataFuture, templateFuture, configFuture)
            .thenApply(v -> new ReportBuilder()
                .withData(dataFuture.join())
                .withTemplate(templateFuture.join())
                .withConfig(configFuture.join())
                .build());
    }

### Q18: CompletableFuture performance patterns
```java
public class CompletableFuturePerformance {

    // Batching and rate limiting
    public class BatchProcessor {
        private final int batchSize = 10;
        private final Semaphore semaphore = new Semaphore(5); // Rate limit

        public CompletableFuture<List<Result>> processBatch(List<String> items) {
            List<CompletableFuture<Result>> futures = items.stream()
                .map(item -> processWithRateLimit(item))
                .collect(Collectors.toList());

            return CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
            ).thenApply(v -> futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList())
            );
        }

        private CompletableFuture<Result> processWithRateLimit(String item) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    semaphore.acquire();
                    try {
                        return process(item);
                    } finally {
                        semaphore.release();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    // Retry pattern
    public <T> CompletableFuture<T> retryAsync(Supplier<T> supplier,
                                                int maxRetries) {
        return CompletableFuture.supplyAsync(supplier)
            .handle((result, ex) -> {
                if (ex == null) {
                    return CompletableFuture.completedFuture(result);
                } else if (maxRetries > 0) {
                    return retryAsync(supplier, maxRetries - 1);
                } else {
                    CompletableFuture<T> failed = new CompletableFuture<>();
                    failed.completeExceptionally(ex);
                    return failed;
                }
            })
            .thenCompose(Function.identity());
    }

    // Circuit breaker pattern
    public class CircuitBreaker {
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final int threshold = 3;
        private volatile boolean open = false;

        public <T> CompletableFuture<T> execute(Supplier<T> supplier) {
            if (open) {
                CompletableFuture<T> failed = new CompletableFuture<>();
                failed.completeExceptionally(
                    new RuntimeException("Circuit breaker is open")
                );
                return failed;
            }

            return CompletableFuture.supplyAsync(supplier)
                .handle((result, ex) -> {
                    if (ex != null) {
                        if (failureCount.incrementAndGet() >= threshold) {
                            open = true;
                            scheduleReset();
                        }
                        throw new RuntimeException(ex);
                    } else {
                        failureCount.set(0);
                        return result;
                    }
                });
        }

        private void scheduleReset() {
            CompletableFuture.delayedExecutor(5, TimeUnit.SECONDS)
                .execute(() -> {
                    open = false;
                    failureCount.set(0);
                });
        }
    }
}
```

---

## Functional Programming Patterns

### Q19: Implement common FP patterns in Java
```java
// Java 17 with pattern matching and sealed classes
public class FunctionalPatterns {

    // Monad pattern with Optional
    public class MonadPattern {
        public Optional<Integer> parseAndDouble(String input) {
            return Optional.ofNullable(input)
                .filter(s -> s.matches("\\d+"))
                .map(Integer::parseInt)
                .map(i -> i * 2);
        }
    }

    // Either type using sealed classes (Java 17)
    public sealed interface Either<L, R>
            permits Either.Left, Either.Right {

        record Left<L, R>(L value) implements Either<L, R> {}
        record Right<L, R>(R value) implements Either<L, R> {}

        static <L, R> Either<L, R> left(L value) {
            return new Left<>(value);
        }

        static <L, R> Either<L, R> right(R value) {
            return new Right<>(value);
        }

        default <T> T fold(Function<L, T> leftMapper, Function<R, T> rightMapper) {
            return switch (this) {
                case Left(var l) -> leftMapper.apply(l);
                case Right(var r) -> rightMapper.apply(r);
            };
        }
    }

    // Memoization pattern with method handles (more efficient in Java 17)
    public class Memoization {
        public <T, R> Function<T, R> memoize(Function<T, R> function) {
            var cache = new ConcurrentHashMap<T, R>();
            return input -> cache.computeIfAbsent(input, function);
        }

        // Example usage
        public void example() {
            Function<Integer, Integer> expensive = n -> {
                System.out.println("Computing for: " + n);
                return n * n;
            };

            Function<Integer, Integer> memoized = memoize(expensive);
            memoized.apply(5); // Computes
            memoized.apply(5); // Returns cached
        }
    }

    // Currying pattern
    public class Currying {
        public Function<Integer, Function<Integer, Integer>> curriedAdd() {
            return a -> b -> a + b;
        }

        public void example() {
            Function<Integer, Function<Integer, Integer>> add = curriedAdd();
            Function<Integer, Integer> add5 = add.apply(5);

            int result = add5.apply(3); // 8
        }
    }

    // Partial application
    public class PartialApplication {
        public BiFunction<String, String, String> concat = String::concat;

        public Function<String, String> partial(String prefix) {
            return suffix -> concat.apply(prefix, suffix);
        }

        public void example() {
            Function<String, String> greet = partial("Hello ");
            String greeting = greet.apply("World"); // "Hello World"
        }
    }
}
```

### Q20: Functional composition and pipelines
```java
public class FunctionalComposition {

    // Function composition
    public class Composition {
        public <T, R, V> Function<T, V> compose(Function<T, R> f1,
                                                 Function<R, V> f2) {
            return f1.andThen(f2);
        }

        public void example() {
            Function<String, Integer> parseToInt = Integer::parseInt;
            Function<Integer, Double> toDouble = Integer::doubleValue;
            Function<Double, String> format = d -> String.format("%.2f", d);

            Function<String, String> pipeline = parseToInt
                .andThen(toDouble)
                .andThen(format);

            String result = pipeline.apply("123"); // "123.00"
        }
    }

    // Pipeline pattern
    public class Pipeline<T> {
        private final T value;

        private Pipeline(T value) {
            this.value = value;
        }

        public static <T> Pipeline<T> of(T value) {
            return new Pipeline<>(value);
        }

        public <R> Pipeline<R> map(Function<T, R> mapper) {
            return new Pipeline<>(mapper.apply(value));
        }

        public Pipeline<T> filter(Predicate<T> predicate) {
            if (predicate.test(value)) {
                return this;
            }
            return new Pipeline<>(null);
        }

        public T get() {
            return value;
        }
    }

    // Validation pipeline
    public class ValidationPipeline {
        public interface Validator<T> {
            ValidationResult validate(T input);
        }

        public class ValidationResult {
            private final boolean valid;
            private final List<String> errors;

            private ValidationResult(boolean valid, List<String> errors) {
                this.valid = valid;
                this.errors = errors;
            }

            public static ValidationResult valid() {
                return new ValidationResult(true, Collections.emptyList());
            }

            public static ValidationResult invalid(String... errors) {
                return new ValidationResult(false, Arrays.asList(errors));
            }
        }

        public <T> Validator<T> combine(Validator<T>... validators) {
            return input -> {
                List<String> errors = new ArrayList<>();
                for (Validator<T> validator : validators) {
                    ValidationResult result = validator.validate(input);
                    if (!result.valid) {
                        errors.addAll(result.errors);
                    }
                }
                return errors.isEmpty() ?
                    ValidationResult.valid() :
                    ValidationResult.invalid(errors.toArray(new String[0]));
            };
        }
    }
}
```

---

## Real-World Scenarios

### Q21: Implement a functional reactive system
```java
// Java 17 with concurrent improvements
public class FunctionalReactiveSystem {

    // Event processing system with var and improved concurrency
    public class EventProcessor {
        private final var handlers = new ConcurrentHashMap<Class<?>, List<Consumer<?>>>();

        public <T> void subscribe(Class<T> eventType, Consumer<T> handler) {
            handlers.computeIfAbsent(eventType, k -> new ArrayList<>())
                   .add(handler);
        }

        @SuppressWarnings("unchecked")
        public <T> void publish(T event) {
            List<Consumer<?>> eventHandlers =
                handlers.get(event.getClass());
            if (eventHandlers != null) {
                eventHandlers.parallelStream()
                    .forEach(handler -> ((Consumer<T>) handler).accept(event));
            }
        }
    }

    // Functional data transformation pipeline with records
    record RawData(String id, String content) {}
    record EnrichedData(RawData raw, Map<String, Object> metadata) {}
    record ProcessedData(String id, String result, Instant timestamp) {
        static ProcessedData empty() {
            return new ProcessedData("empty", "", Instant.now());
        }
    }

    public class DataPipeline {
        public CompletableFuture<ProcessedData> process(RawData raw) {
            return CompletableFuture
                .supplyAsync(() -> validate(raw))
                .thenCompose(this::enrich)
                .thenApply(this::transform)
                .thenCompose(this::persist)
                .exceptionally(this::handleError);
        }

        private RawData validate(RawData data) {
            return Optional.ofNullable(data)
                .filter(d -> d.isValid())
                .orElseThrow(() -> new ValidationException("Invalid data"));
        }

        private CompletableFuture<EnrichedData> enrich(RawData data) {
            return CompletableFuture.supplyAsync(() -> {
                // Enrich with external data
                return new EnrichedData(data);
            });
        }

        private ProcessedData transform(EnrichedData data) {
            return new ProcessedData(data);
        }

        private CompletableFuture<ProcessedData> persist(ProcessedData data) {
            return CompletableFuture.supplyAsync(() -> {
                // Save to database
                return data;
            });
        }

        private ProcessedData handleError(Throwable error) {
            log.error("Processing failed", error);
            return ProcessedData.empty();
        }
    }
}
```

### Q22: Build a functional caching system
```java
// Java 17 with records and enhanced collections
public class FunctionalCache {

    // Time-based cache with record and Duration
    public class TimeBasedCache<K, V> {
        private record CacheEntry<V>(V value, Instant timestamp) {
            boolean isExpired(Duration ttl) {
                return Duration.between(timestamp, Instant.now()).compareTo(ttl) > 0;
            }
        }

        private final var cache = new ConcurrentHashMap<K, CacheEntry<V>>();
        private final Duration ttl;

        public TimeBasedCache(Duration ttl) {
            this.ttl = ttl;
        }

        public V computeIfAbsent(K key, Function<K, V> loader) {
            var entry = cache.compute(key, (k, existing) -> {
                if (existing != null && !existing.isExpired(ttl)) {
                    return existing;
                }
                var value = loader.apply(k);
                return new CacheEntry<>(value, Instant.now());
            });
            return entry.value();
        }
    }

    // LRU cache with functional operations
    public class LRUCache<K, V> {
        private final int capacity;
        private final LinkedHashMap<K, V> cache;

        public LRUCache(int capacity) {
            this.capacity = capacity;
            this.cache = new LinkedHashMap<K, V>(capacity, 0.75f, true) {
                protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                    return size() > capacity;
                }
            };
        }

        public V get(K key, Function<K, V> loader) {
            return cache.computeIfAbsent(key, loader);
        }

        public Optional<V> get(K key) {
            return Optional.ofNullable(cache.get(key));
        }
    }

    // Async cache with CompletableFuture
    public class AsyncCache<K, V> {
        private final Map<K, CompletableFuture<V>> cache =
            new ConcurrentHashMap<>();

        public CompletableFuture<V> get(K key, Function<K, V> loader) {
            return cache.computeIfAbsent(key, k ->
                CompletableFuture.supplyAsync(() -> loader.apply(k))
            );
        }

        public void invalidate(K key) {
            cache.remove(key);
        }

        public void invalidateAll() {
            cache.clear();
        }
    }
}
```

### Q23: Implement functional error handling strategies
```java
// Java 17 with sealed classes and pattern matching
public class FunctionalErrorHandling {

    // Try monad with sealed classes (Java 17)
    public sealed interface Try<T> permits Try.Success, Try.Failure {
        record Success<T>(T value) implements Try<T> {}
        record Failure<T>(Throwable error) implements Try<T> {}

        static <T> Try<T> of(Supplier<T> supplier) {
            try {
                return new Success<>(supplier.get());
            } catch (Exception e) {
                return new Failure<>(e);
            }
        }

        default <R> Try<R> map(Function<T, R> mapper) {
            return switch (this) {
                case Success(var value) -> Try.of(() -> mapper.apply(value));
                case Failure(var error) -> new Failure<>(error);
            };
        }

        default <R> Try<R> flatMap(Function<T, Try<R>> mapper) {
            return switch (this) {
                case Success(var value) -> mapper.apply(value);
                case Failure(var error) -> new Failure<>(error);
            };
        }

        default T recover(Function<Throwable, T> recovery) {
            return switch (this) {
                case Success(var value) -> value;
                case Failure(var error) -> recovery.apply(error);
            };
        }

        default T getOrElse(T defaultValue) {
            return switch (this) {
                case Success(var value) -> value;
                case Failure(var error) -> defaultValue;
            };
        }
    }

    // Functional retry with backoff
    public class RetryWithBackoff {
        public <T> T retryWithExponentialBackoff(Supplier<T> operation,
                                                 int maxRetries,
                                                 long initialDelay) {
            return Stream.iterate(initialDelay, d -> d * 2)
                .limit(maxRetries)
                .map(delay -> {
                    try {
                        return Optional.of(operation.get());
                    } catch (Exception e) {
                        try {
                            Thread.sleep(delay);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                        return Optional.<T>empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Max retries exceeded"));
        }
    }

    // Validation combinator
    public class ValidationCombinator {
        public interface Validation<T> extends Function<T, ValidationResult> {
            default Validation<T> and(Validation<T> other) {
                return t -> {
                    ValidationResult result = this.apply(t);
                    return result.isValid() ? other.apply(t) : result;
                };
            }

            default Validation<T> or(Validation<T> other) {
                return t -> {
                    ValidationResult result = this.apply(t);
                    return result.isValid() ? result : other.apply(t);
                };
            }
        }

        public class ValidationResult {
            private final boolean valid;
            private final String message;

            private ValidationResult(boolean valid, String message) {
                this.valid = valid;
                this.message = message;
            }

            public boolean isValid() { return valid; }
            public String getMessage() { return message; }

            public static ValidationResult ok() {
                return new ValidationResult(true, null);
            }

            public static ValidationResult fail(String message) {
                return new ValidationResult(false, message);
            }
        }
    }
}
```

### Q24: Implement a functional state machine
```java
public class FunctionalStateMachine {

    // State machine with functional transitions
    public class StateMachine<S, E> {
        private S currentState;
        private final Map<S, Map<E, Function<S, S>>> transitions;
        private final List<BiConsumer<S, S>> listeners;

        public StateMachine(S initialState) {
            this.currentState = initialState;
            this.transitions = new HashMap<>();
            this.listeners = new ArrayList<>();
        }

        public void addTransition(S from, E event, Function<S, S> transition) {
            transitions.computeIfAbsent(from, k -> new HashMap<>())
                      .put(event, transition);
        }

        public void addListener(BiConsumer<S, S> listener) {
            listeners.add(listener);
        }

        public Optional<S> fire(E event) {
            return Optional.ofNullable(transitions.get(currentState))
                .map(events -> events.get(event))
                .map(transition -> {
                    S oldState = currentState;
                    S newState = transition.apply(currentState);
                    currentState = newState;
                    listeners.forEach(l -> l.accept(oldState, newState));
                    return newState;
                });
        }

        public S getCurrentState() {
            return currentState;
        }
    }

    // Example: Order processing state machine
    public class OrderStateMachine {
        public enum State {
            CREATED, PAID, SHIPPED, DELIVERED, CANCELLED
        }

        public enum Event {
            PAY, SHIP, DELIVER, CANCEL
        }

        public StateMachine<State, Event> create() {
            StateMachine<State, Event> machine =
                new StateMachine<>(State.CREATED);

            // Define transitions
            machine.addTransition(State.CREATED, Event.PAY,
                s -> State.PAID);
            machine.addTransition(State.PAID, Event.SHIP,
                s -> State.SHIPPED);
            machine.addTransition(State.SHIPPED, Event.DELIVER,
                s -> State.DELIVERED);

            // Cancel from any state except delivered
            Arrays.asList(State.CREATED, State.PAID, State.SHIPPED)
                .forEach(state ->
                    machine.addTransition(state, Event.CANCEL,
                        s -> State.CANCELLED)
                );

            // Add logging
            machine.addListener((oldState, newState) ->
                System.out.println("Transition: " + oldState + " -> " + newState)
            );

            return machine;
        }
    }
}
```

---

## Interview Tips

### Common Functional Programming Interview Questions:

1. **Explain the difference between imperative and functional programming**
2. **What are the benefits of immutability in functional programming?**
3. **How do you handle side effects in functional programming?**
4. **Explain referential transparency**
5. **What is a pure function?**
6. **How does lazy evaluation work in Java Streams?**
7. **Explain the difference between map() and flatMap()**
8. **When would you use Optional vs null checks?**
9. **How do you debug functional code?**
10. **What are the performance implications of functional programming?**

### Key Concepts to Master:

- **Immutability**: Understanding why and how to work with immutable data
- **Higher-order functions**: Functions that take or return other functions
- **Composition**: Building complex operations from simple functions
- **Side effects**: Managing and isolating side effects
- **Monads**: Optional, Stream, CompletableFuture as monadic structures
- **Lazy evaluation**: How streams defer computation
- **Parallel processing**: Using parallel streams effectively

### Best Practices:

1. Prefer immutability and pure functions
2. Use method references where possible for cleaner code
3. Leverage built-in functional interfaces before creating custom ones
4. Be careful with parallel streams - measure performance
5. Use Optional for return types, not parameters or fields
6. Chain operations rather than nesting
7. Handle exceptions functionally with Try or Either patterns
8. Test functional code with property-based testing when appropriate

---

[Back to Top](#table-of-contents)