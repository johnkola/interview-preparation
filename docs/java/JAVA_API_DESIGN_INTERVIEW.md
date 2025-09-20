# Java API Design - Interview Guide

> **Complete guide to designing robust Java library APIs and best practices**

---

## 📚 **Table of Contents**

1. [API Design Fundamentals](#api-design-fundamentals)
2. [Class and Interface Design](#class-and-interface-design)
3. [Method Design Principles](#method-design-principles)
4. [Exception Handling in APIs](#exception-handling-in-apis)
5. [Generic API Design](#generic-api-design)
6. [Builder Pattern for APIs](#builder-pattern-for-apis)
7. [Fluent Interface Design](#fluent-interface-design)
8. [Immutable API Design](#immutable-api-design)
9. [Version Compatibility](#version-compatibility)
10. [Performance Considerations](#performance-considerations)
11. [Documentation and Naming](#documentation-and-naming)
12. [Common Design Patterns](#common-design-patterns)
13. [API Testing Strategies](#api-testing-strategies)
14. [Interview Questions](#interview-questions)

---

## 🎯 **API Design Fundamentals**

### **Core Principles**

```java
/**
 * SOLID Principles in API Design
 */

// 1. Single Responsibility Principle
public interface UserRepository {
    User findById(String id);
    void save(User user);
    void delete(String id);
    // Each method has single purpose
}

// 2. Open/Closed Principle
public abstract class PaymentProcessor {
    public final PaymentResult process(Payment payment) {
        validate(payment);
        return doProcess(payment);
    }

    protected abstract PaymentResult doProcess(Payment payment);
    protected void validate(Payment payment) { /* default validation */ }
}

// 3. Liskov Substitution Principle
public interface Shape {
    double area();
    double perimeter();
}

public class Rectangle implements Shape {
    // Must work wherever Shape is expected
}

// 4. Interface Segregation Principle
public interface Readable {
    String read();
}

public interface Writable {
    void write(String data);
}

// Don't force clients to depend on unused methods
public class FileHandler implements Readable, Writable {
    // Implement only what's needed
}

// 5. Dependency Inversion Principle
public class OrderService {
    private final PaymentProvider paymentProvider;
    private final NotificationService notificationService;

    // Depend on abstractions, not concretions
    public OrderService(PaymentProvider paymentProvider,
                       NotificationService notificationService) {
        this.paymentProvider = paymentProvider;
        this.notificationService = notificationService;
    }
}
```

### **API Design Guidelines**

```java
/**
 * Essential API Design Rules
 */
public class ApiDesignGuidelines {

    // 1. Minimize accessibility
    public class PublicApi {
        private final InternalHelper helper = new InternalHelper();

        public String processData(String input) {  // Public only when needed
            return helper.process(input);
        }

        private static class InternalHelper {      // Hide implementation
            String process(String input) {
                return input.toUpperCase();
            }
        }
    }

    // 2. Favor composition over inheritance
    public class EmailService {
        private final EmailValidator validator;
        private final EmailSender sender;

        public EmailService(EmailValidator validator, EmailSender sender) {
            this.validator = validator;
            this.sender = sender;
        }

        public void sendEmail(Email email) {
            if (validator.isValid(email)) {
                sender.send(email);
            }
        }
    }

    // 3. Design for evolution
    public interface ConfigurableService {
        // Use configuration objects for flexibility
        void configure(ServiceConfig config);

        record ServiceConfig(
            Duration timeout,
            int retryCount,
            boolean enableLogging,
            Map<String, String> properties  // Extensible
        ) {}
    }

    // 4. Fail fast principle
    public class BankAccount {
        private final String accountNumber;
        private BigDecimal balance;

        public BankAccount(String accountNumber, BigDecimal initialBalance) {
            this.accountNumber = Objects.requireNonNull(accountNumber,
                "Account number cannot be null");
            if (initialBalance.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Initial balance cannot be negative");
            }
            this.balance = initialBalance;
        }

        public void withdraw(BigDecimal amount) {
            Objects.requireNonNull(amount, "Amount cannot be null");
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Amount must be positive");
            }
            if (balance.compareTo(amount) < 0) {
                throw new InsufficientFundsException("Insufficient balance");
            }
            balance = balance.subtract(amount);
        }
    }
}
```

---

## 🏗️ **Class and Interface Design**

### **Interface Design Best Practices**

```java
/**
 * Well-designed interfaces
 */

// 1. Cohesive interface
public interface DocumentProcessor {
    ProcessingResult process(Document document);
    boolean canProcess(DocumentType type);
    Set<DocumentType> getSupportedTypes();
}

// 2. Functional interface for single responsibility
@FunctionalInterface
public interface DataTransformer<T, R> {
    R transform(T input);

    // Default methods for composition
    default <V> DataTransformer<T, V> andThen(DataTransformer<R, V> after) {
        return input -> after.transform(transform(input));
    }
}

// 3. Marker interface for type safety
public interface Serializable {
    // Marker interface - no methods
}

// 4. Resource management interface
public interface AutoCloseableResource extends AutoCloseable {
    @Override
    void close() throws Exception;

    boolean isOpen();

    // Template method pattern
    default void executeWithResource(ResourceOperation operation) throws Exception {
        try {
            operation.execute(this);
        } finally {
            close();
        }
    }

    @FunctionalInterface
    interface ResourceOperation {
        void execute(AutoCloseableResource resource) throws Exception;
    }
}
```

### **Abstract Class Design**

```java
/**
 * Effective abstract class design
 */
public abstract class HttpClient {
    protected final HttpConfig config;

    protected HttpClient(HttpConfig config) {
        this.config = Objects.requireNonNull(config);
    }

    // Template method - final to prevent override
    public final HttpResponse execute(HttpRequest request) {
        validateRequest(request);
        return doExecute(request);
    }

    // Hook method for subclasses
    protected void validateRequest(HttpRequest request) {
        Objects.requireNonNull(request, "Request cannot be null");
    }

    // Abstract method for implementation
    protected abstract HttpResponse doExecute(HttpRequest request);

    // Convenience methods
    public HttpResponse get(String url) {
        return execute(HttpRequest.get(url));
    }

    public HttpResponse post(String url, String body) {
        return execute(HttpRequest.post(url).body(body));
    }
}

// Configuration object pattern
public record HttpConfig(
    Duration connectTimeout,
    Duration readTimeout,
    int maxRetries,
    Map<String, String> defaultHeaders
) {
    public HttpConfig {
        Objects.requireNonNull(connectTimeout);
        Objects.requireNonNull(readTimeout);
        if (maxRetries < 0) {
            throw new IllegalArgumentException("Max retries cannot be negative");
        }
        defaultHeaders = Map.copyOf(defaultHeaders != null ? defaultHeaders : Map.of());
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Duration connectTimeout = Duration.ofSeconds(10);
        private Duration readTimeout = Duration.ofSeconds(30);
        private int maxRetries = 3;
        private Map<String, String> defaultHeaders = new HashMap<>();

        public Builder connectTimeout(Duration timeout) {
            this.connectTimeout = Objects.requireNonNull(timeout);
            return this;
        }

        public Builder readTimeout(Duration timeout) {
            this.readTimeout = Objects.requireNonNull(timeout);
            return this;
        }

        public Builder maxRetries(int retries) {
            this.maxRetries = retries;
            return this;
        }

        public Builder addHeader(String name, String value) {
            this.defaultHeaders.put(
                Objects.requireNonNull(name),
                Objects.requireNonNull(value)
            );
            return this;
        }

        public HttpConfig build() {
            return new HttpConfig(connectTimeout, readTimeout, maxRetries, defaultHeaders);
        }
    }
}
```

### **Effective Class Hierarchies**

```java
/**
 * Well-designed class hierarchy
 */

// Base class with common functionality
public abstract class Vehicle {
    private final String make;
    private final String model;
    private final int year;

    protected Vehicle(String make, String model, int year) {
        this.make = Objects.requireNonNull(make);
        this.model = Objects.requireNonNull(model);
        if (year < 1886) {  // First automobile year
            throw new IllegalArgumentException("Invalid year: " + year);
        }
        this.year = year;
    }

    // Final methods to prevent override
    public final String getMake() { return make; }
    public final String getModel() { return model; }
    public final int getYear() { return year; }

    // Abstract methods for subclasses
    public abstract FuelType getFuelType();
    public abstract double getFuelEfficiency();

    // Template method
    public final double calculateFuelCost(double distance, double fuelPrice) {
        var consumption = distance / getFuelEfficiency();
        return consumption * fuelPrice;
    }

    @Override
    public final boolean equals(Object obj) {
        return obj instanceof Vehicle other &&
               make.equals(other.make) &&
               model.equals(other.model) &&
               year == other.year;
    }

    @Override
    public final int hashCode() {
        return Objects.hash(make, model, year);
    }
}

// Concrete implementations
public class ElectricCar extends Vehicle {
    private final double batteryCapacity;
    private final double efficiency; // miles per kWh

    public ElectricCar(String make, String model, int year,
                      double batteryCapacity, double efficiency) {
        super(make, model, year);
        this.batteryCapacity = batteryCapacity;
        this.efficiency = efficiency;
    }

    @Override
    public FuelType getFuelType() {
        return FuelType.ELECTRIC;
    }

    @Override
    public double getFuelEfficiency() {
        return efficiency;
    }

    public double getRange() {
        return batteryCapacity * efficiency;
    }
}

public enum FuelType {
    GASOLINE, DIESEL, ELECTRIC, HYBRID
}
```

---

## 🔧 **Method Design Principles**

### **Method Naming and Signature Design**

```java
/**
 * Method design best practices
 */
public class MethodDesignExamples {

    // 1. Clear, intention-revealing names
    public boolean isEligibleForDiscount(Customer customer) {
        return customer.getAge() >= 65 || customer.isMember();
    }

    // 2. Avoid long parameter lists
    public Order createOrder(OrderRequest request) {  // Single parameter object
        return new Order(request.customerId(), request.items(),
                        request.shippingAddress(), request.paymentMethod());
    }

    public record OrderRequest(
        String customerId,
        List<OrderItem> items,
        Address shippingAddress,
        PaymentMethod paymentMethod
    ) {}

    // 3. Use overloading judiciously
    public void processPayment(BigDecimal amount) {
        processPayment(amount, PaymentMethod.DEFAULT);
    }

    public void processPayment(BigDecimal amount, PaymentMethod method) {
        processPayment(amount, method, Duration.ofMinutes(5));
    }

    public void processPayment(BigDecimal amount, PaymentMethod method, Duration timeout) {
        // Implementation
    }

    // 4. Return empty collections, not null
    public List<Transaction> getTransactions(String accountId) {
        var transactions = repository.findByAccountId(accountId);
        return transactions != null ? transactions : List.of();
    }

    // 5. Use Optional for potentially missing values
    public Optional<Customer> findCustomerByEmail(String email) {
        return repository.findByEmail(email);
    }

    // 6. Defensive copying for mutable parameters/returns
    private final List<String> items = new ArrayList<>();

    public List<String> getItems() {
        return List.copyOf(items);  // Immutable copy
    }

    public void setItems(List<String> newItems) {
        this.items.clear();
        this.items.addAll(Objects.requireNonNull(newItems));  // Defensive copy
    }

    // 7. Consistent error handling
    public Customer createCustomer(String email, String name) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or blank");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }

        return new Customer(email, name);
    }

    // 8. Use varargs carefully
    public void logMessages(LogLevel level, String... messages) {
        Objects.requireNonNull(level);
        Objects.requireNonNull(messages);

        for (var message : messages) {
            if (message != null) {
                logger.log(level, message);
            }
        }
    }

    // 9. Side-effect free methods when possible
    public List<String> formatNames(List<String> names) {
        return names.stream()
                   .filter(Objects::nonNull)
                   .map(String::trim)
                   .map(String::toLowerCase)
                   .toList();
    }
}
```

### **Method Chaining and Fluent APIs**

```java
/**
 * Fluent interface design
 */
public class QueryBuilder {
    private final StringBuilder query = new StringBuilder();
    private final List<Object> parameters = new ArrayList<>();

    public static QueryBuilder select(String... columns) {
        var builder = new QueryBuilder();
        builder.query.append("SELECT ");
        builder.query.append(String.join(", ", columns));
        return builder;
    }

    public QueryBuilder from(String table) {
        query.append(" FROM ").append(table);
        return this;
    }

    public QueryBuilder where(String condition, Object... values) {
        query.append(" WHERE ").append(condition);
        parameters.addAll(Arrays.asList(values));
        return this;
    }

    public QueryBuilder and(String condition, Object... values) {
        query.append(" AND ").append(condition);
        parameters.addAll(Arrays.asList(values));
        return this;
    }

    public QueryBuilder or(String condition, Object... values) {
        query.append(" OR ").append(condition);
        parameters.addAll(Arrays.asList(values));
        return this;
    }

    public QueryBuilder orderBy(String column, SortOrder order) {
        query.append(" ORDER BY ").append(column).append(" ").append(order);
        return this;
    }

    public QueryBuilder limit(int count) {
        query.append(" LIMIT ").append(count);
        return this;
    }

    public Query build() {
        return new Query(query.toString(), List.copyOf(parameters));
    }

    // Usage example
    public static void main(String[] args) {
        var query = QueryBuilder
            .select("name", "email", "age")
            .from("users")
            .where("age > ?", 18)
            .and("status = ?", "ACTIVE")
            .orderBy("name", SortOrder.ASC)
            .limit(100)
            .build();
    }
}

public enum SortOrder {
    ASC, DESC
}

public record Query(String sql, List<Object> parameters) {
    public Query {
        Objects.requireNonNull(sql);
        parameters = List.copyOf(parameters);
    }
}
```

---

## ⚠️ **Exception Handling in APIs**

### **Exception Hierarchy Design**

```java
/**
 * Well-designed exception hierarchy
 */

// Base exception for the library
public class ProcessingException extends Exception {
    private final String errorCode;
    private final Map<String, Object> context;

    public ProcessingException(String message, String errorCode) {
        this(message, errorCode, null, Map.of());
    }

    public ProcessingException(String message, String errorCode,
                             Throwable cause, Map<String, Object> context) {
        super(message, cause);
        this.errorCode = Objects.requireNonNull(errorCode);
        this.context = Map.copyOf(context != null ? context : Map.of());
    }

    public String getErrorCode() { return errorCode; }
    public Map<String, Object> getContext() { return context; }
}

// Specific exceptions for different scenarios
public class ValidationException extends ProcessingException {
    private final List<ValidationError> errors;

    public ValidationException(List<ValidationError> errors) {
        super(createMessage(errors), "VALIDATION_FAILED", null,
              Map.of("errors", errors));
        this.errors = List.copyOf(errors);
    }

    public List<ValidationError> getValidationErrors() { return errors; }

    private static String createMessage(List<ValidationError> errors) {
        return "Validation failed with " + errors.size() + " error(s)";
    }
}

public record ValidationError(String field, String message, Object rejectedValue) {}

// Resource-related exceptions
public class ResourceException extends ProcessingException {
    public enum Type {
        NOT_FOUND, ALREADY_EXISTS, ACCESS_DENIED, RESOURCE_BUSY
    }

    private final Type type;
    private final String resourceId;

    public ResourceException(Type type, String resourceId, String message) {
        super(message, "RESOURCE_" + type.name(), null,
              Map.of("resourceId", resourceId, "type", type));
        this.type = type;
        this.resourceId = resourceId;
    }

    public Type getType() { return type; }
    public String getResourceId() { return resourceId; }

    // Factory methods for common cases
    public static ResourceException notFound(String resourceId) {
        return new ResourceException(Type.NOT_FOUND, resourceId,
                                   "Resource not found: " + resourceId);
    }

    public static ResourceException alreadyExists(String resourceId) {
        return new ResourceException(Type.ALREADY_EXISTS, resourceId,
                                   "Resource already exists: " + resourceId);
    }
}
```

### **Exception Usage Patterns**

```java
/**
 * Exception handling patterns in API design
 */
public class ExceptionPatterns {

    // 1. Try-with-resources support
    public class ManagedResource implements AutoCloseable {
        private boolean closed = false;

        public void performOperation() throws ProcessingException {
            if (closed) {
                throw new IllegalStateException("Resource is closed");
            }
            // Implementation
        }

        @Override
        public void close() throws ProcessingException {
            if (!closed) {
                // Cleanup logic
                closed = true;
            }
        }
    }

    // 2. Result pattern for recoverable errors
    public sealed interface Result<T, E> permits Success, Failure {
        boolean isSuccess();
        boolean isFailure();

        default T orElse(T defaultValue) {
            return switch (this) {
                case Success<T, E> success -> success.value();
                case Failure<T, E> failure -> defaultValue;
            };
        }

        default T orElseThrow() {
            return switch (this) {
                case Success<T, E> success -> success.value();
                case Failure<T, E> failure -> throw new RuntimeException(
                    failure.error().toString());
            };
        }
    }

    public record Success<T, E>(T value) implements Result<T, E> {
        public boolean isSuccess() { return true; }
        public boolean isFailure() { return false; }
    }

    public record Failure<T, E>(E error) implements Result<T, E> {
        public boolean isSuccess() { return false; }
        public boolean isFailure() { return true; }
    }

    // Usage example
    public Result<Customer, ValidationError> validateAndCreateCustomer(String email, String name) {
        if (email == null || !isValidEmail(email)) {
            return new Failure<>(new ValidationError("email", "Invalid email", email));
        }
        if (name == null || name.isBlank()) {
            return new Failure<>(new ValidationError("name", "Name required", name));
        }

        return new Success<>(new Customer(email, name));
    }

    // 3. Multiple exception handling
    public void processData(String data) throws ValidationException, ProcessingException {
        var errors = new ArrayList<ValidationError>();

        if (data == null) {
            errors.add(new ValidationError("data", "Data cannot be null", null));
        } else if (data.isBlank()) {
            errors.add(new ValidationError("data", "Data cannot be blank", data));
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        try {
            // Processing logic
            doProcess(data);
        } catch (IOException e) {
            throw new ProcessingException("Failed to process data", "IO_ERROR", e,
                                        Map.of("data", data));
        }
    }

    private boolean isValidEmail(String email) { return true; }
    private void doProcess(String data) throws IOException {}
}
```

---

## 🧩 **Generic API Design**

### **Effective Generics Usage**

```java
/**
 * Generic API design patterns
 */

// 1. Repository pattern with generics
public interface Repository<T, ID> {
    Optional<T> findById(ID id);
    List<T> findAll();
    T save(T entity);
    void deleteById(ID id);
    boolean existsById(ID id);
}

// 2. Bounded type parameters
public class DataProcessor<T extends Serializable & Comparable<T>> {
    public List<T> processAndSort(List<T> data) {
        return data.stream()
                  .map(this::processItem)
                  .sorted()
                  .toList();
    }

    private T processItem(T item) {
        // Processing logic
        return item;
    }
}

// 3. Wildcard usage (PECS principle)
public class CollectionUtils {

    // Producer extends (? extends T)
    public static <T> void copy(List<? extends T> source, List<? super T> destination) {
        for (T item : source) {
            destination.add(item);
        }
    }

    // Consumer super (? super T)
    public static <T> T findMax(List<? extends T> list, Comparator<? super T> comparator) {
        return list.stream().max(comparator).orElse(null);
    }

    // Multiple bounds
    public static <T extends Comparable<T> & Serializable> void sortAndSerialize(
            List<T> list, OutputStream output) throws IOException {
        list.sort(Comparator.naturalOrder());
        try (var oos = new ObjectOutputStream(output)) {
            oos.writeObject(list);
        }
    }
}

// 4. Generic factory pattern
public interface Factory<T> {
    T create();
}

public class FactoryRegistry {
    private final Map<Class<?>, Factory<?>> factories = new ConcurrentHashMap<>();

    public <T> void register(Class<T> type, Factory<T> factory) {
        factories.put(Objects.requireNonNull(type), Objects.requireNonNull(factory));
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> create(Class<T> type) {
        var factory = (Factory<T>) factories.get(type);
        return factory != null ? Optional.of(factory.create()) : Optional.empty();
    }
}

// 5. Type-safe heterogeneous container
public class TypeSafeMap {
    private final Map<TypeToken<?>, Object> map = new HashMap<>();

    public <T> void put(TypeToken<T> key, T value) {
        map.put(Objects.requireNonNull(key), Objects.requireNonNull(value));
    }

    @SuppressWarnings("unchecked")
    public <T> T get(TypeToken<T> key) {
        return (T) map.get(key);
    }

    // Usage
    public static void main(String[] args) {
        var typeMap = new TypeSafeMap();
        typeMap.put(TypeToken.of(String.class), "Hello");
        typeMap.put(TypeToken.of(Integer.class), 42);
        typeMap.put(new TypeToken<List<String>>() {}, List.of("a", "b", "c"));

        String str = typeMap.get(TypeToken.of(String.class));
        Integer num = typeMap.get(TypeToken.of(Integer.class));
        List<String> list = typeMap.get(new TypeToken<List<String>>() {});
    }
}

// Type token implementation
public abstract class TypeToken<T> {
    private final Type type;

    protected TypeToken() {
        var superclass = getClass().getGenericSuperclass();
        this.type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    public static <T> TypeToken<T> of(Class<T> clazz) {
        return new TypeToken<T>() {};
    }

    public Type getType() { return type; }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TypeToken<?> other && type.equals(other.type);
    }

    @Override
    public int hashCode() { return type.hashCode(); }
}
```

---

## 🏗️ **Builder Pattern for APIs**

### **Comprehensive Builder Implementation**

```java
/**
 * Builder pattern for complex API objects
 */

// 1. Classic builder with validation
public class DatabaseConnection {
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final Duration connectionTimeout;
    private final Duration queryTimeout;
    private final int maxConnections;
    private final boolean autoCommit;
    private final Map<String, String> properties;

    private DatabaseConnection(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.database = builder.database;
        this.username = builder.username;
        this.password = builder.password;
        this.connectionTimeout = builder.connectionTimeout;
        this.queryTimeout = builder.queryTimeout;
        this.maxConnections = builder.maxConnections;
        this.autoCommit = builder.autoCommit;
        this.properties = Map.copyOf(builder.properties);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String host;
        private int port = 5432;
        private String database;
        private String username;
        private String password;
        private Duration connectionTimeout = Duration.ofSeconds(30);
        private Duration queryTimeout = Duration.ofSeconds(60);
        private int maxConnections = 10;
        private boolean autoCommit = true;
        private final Map<String, String> properties = new HashMap<>();

        public Builder host(String host) {
            this.host = Objects.requireNonNull(host, "Host cannot be null");
            return this;
        }

        public Builder port(int port) {
            if (port <= 0 || port > 65535) {
                throw new IllegalArgumentException("Port must be between 1 and 65535");
            }
            this.port = port;
            return this;
        }

        public Builder database(String database) {
            this.database = Objects.requireNonNull(database, "Database cannot be null");
            return this;
        }

        public Builder credentials(String username, String password) {
            this.username = Objects.requireNonNull(username, "Username cannot be null");
            this.password = Objects.requireNonNull(password, "Password cannot be null");
            return this;
        }

        public Builder connectionTimeout(Duration timeout) {
            if (timeout.isNegative() || timeout.isZero()) {
                throw new IllegalArgumentException("Connection timeout must be positive");
            }
            this.connectionTimeout = timeout;
            return this;
        }

        public Builder queryTimeout(Duration timeout) {
            if (timeout.isNegative() || timeout.isZero()) {
                throw new IllegalArgumentException("Query timeout must be positive");
            }
            this.queryTimeout = timeout;
            return this;
        }

        public Builder maxConnections(int maxConnections) {
            if (maxConnections <= 0) {
                throw new IllegalArgumentException("Max connections must be positive");
            }
            this.maxConnections = maxConnections;
            return this;
        }

        public Builder autoCommit(boolean autoCommit) {
            this.autoCommit = autoCommit;
            return this;
        }

        public Builder property(String key, String value) {
            this.properties.put(
                Objects.requireNonNull(key, "Property key cannot be null"),
                Objects.requireNonNull(value, "Property value cannot be null")
            );
            return this;
        }

        public Builder properties(Map<String, String> properties) {
            Objects.requireNonNull(properties, "Properties cannot be null");
            this.properties.putAll(properties);
            return this;
        }

        public DatabaseConnection build() {
            // Validation
            Objects.requireNonNull(host, "Host is required");
            Objects.requireNonNull(database, "Database is required");
            Objects.requireNonNull(username, "Username is required");
            Objects.requireNonNull(password, "Password is required");

            return new DatabaseConnection(this);
        }
    }

    // Getters
    public String getHost() { return host; }
    public int getPort() { return port; }
    public String getDatabase() { return database; }
    // ... other getters
}

// 2. Step builder pattern (enforced order)
public class EmailBuilder {

    public static RecipientStep newEmail() {
        return new Steps();
    }

    public interface RecipientStep {
        SubjectStep to(String recipient);
        SubjectStep to(String... recipients);
        SubjectStep to(Collection<String> recipients);
    }

    public interface SubjectStep {
        BodyStep subject(String subject);
    }

    public interface BodyStep {
        BuildStep body(String body);
        BuildStep htmlBody(String htmlBody);
    }

    public interface BuildStep {
        BuildStep cc(String... recipients);
        BuildStep bcc(String... recipients);
        BuildStep attachment(String filename, byte[] content);
        BuildStep priority(Priority priority);
        Email build();
    }

    private static class Steps implements RecipientStep, SubjectStep, BodyStep, BuildStep {
        private final Set<String> recipients = new LinkedHashSet<>();
        private final Set<String> ccRecipients = new LinkedHashSet<>();
        private final Set<String> bccRecipients = new LinkedHashSet<>();
        private final Map<String, byte[]> attachments = new HashMap<>();
        private String subject;
        private String body;
        private boolean isHtml;
        private Priority priority = Priority.NORMAL;

        @Override
        public SubjectStep to(String recipient) {
            recipients.add(Objects.requireNonNull(recipient));
            return this;
        }

        @Override
        public SubjectStep to(String... recipients) {
            return to(Arrays.asList(recipients));
        }

        @Override
        public SubjectStep to(Collection<String> recipients) {
            Objects.requireNonNull(recipients);
            this.recipients.addAll(recipients);
            return this;
        }

        @Override
        public BodyStep subject(String subject) {
            this.subject = Objects.requireNonNull(subject);
            return this;
        }

        @Override
        public BuildStep body(String body) {
            this.body = Objects.requireNonNull(body);
            this.isHtml = false;
            return this;
        }

        @Override
        public BuildStep htmlBody(String htmlBody) {
            this.body = Objects.requireNonNull(htmlBody);
            this.isHtml = true;
            return this;
        }

        @Override
        public BuildStep cc(String... recipients) {
            ccRecipients.addAll(Arrays.asList(recipients));
            return this;
        }

        @Override
        public BuildStep bcc(String... recipients) {
            bccRecipients.addAll(Arrays.asList(recipients));
            return this;
        }

        @Override
        public BuildStep attachment(String filename, byte[] content) {
            attachments.put(
                Objects.requireNonNull(filename),
                Objects.requireNonNull(content).clone()
            );
            return this;
        }

        @Override
        public BuildStep priority(Priority priority) {
            this.priority = Objects.requireNonNull(priority);
            return this;
        }

        @Override
        public Email build() {
            if (recipients.isEmpty()) {
                throw new IllegalStateException("At least one recipient is required");
            }
            return new Email(this);
        }
    }

    // Usage
    public static void main(String[] args) {
        var email = EmailBuilder.newEmail()
            .to("user@example.com")
            .subject("Important Update")
            .htmlBody("<h1>Hello World</h1>")
            .cc("manager@example.com")
            .priority(Priority.HIGH)
            .build();
    }
}

public enum Priority {
    LOW, NORMAL, HIGH, URGENT
}

public class Email {
    // Implementation details
    Email(EmailBuilder.Steps steps) {
        // Constructor implementation
    }
}
```

---

## 🌊 **Fluent Interface Design**

### **Advanced Fluent API Patterns**

```java
/**
 * Stream-like fluent API design
 */
public class DataPipeline<T> {
    private final Stream<T> stream;

    private DataPipeline(Stream<T> stream) {
        this.stream = stream;
    }

    public static <T> DataPipeline<T> from(Collection<T> data) {
        return new DataPipeline<>(data.stream());
    }

    public static <T> DataPipeline<T> from(Stream<T> stream) {
        return new DataPipeline<>(stream);
    }

    public <R> DataPipeline<R> transform(Function<T, R> transformer) {
        return new DataPipeline<>(stream.map(transformer));
    }

    public DataPipeline<T> filter(Predicate<T> predicate) {
        return new DataPipeline<>(stream.filter(predicate));
    }

    public DataPipeline<T> distinct() {
        return new DataPipeline<>(stream.distinct());
    }

    public DataPipeline<T> sorted(Comparator<T> comparator) {
        return new DataPipeline<>(stream.sorted(comparator));
    }

    public DataPipeline<T> limit(long maxSize) {
        return new DataPipeline<>(stream.limit(maxSize));
    }

    public DataPipeline<T> skip(long n) {
        return new DataPipeline<>(stream.skip(n));
    }

    // Terminal operations
    public List<T> toList() {
        return stream.toList();
    }

    public Set<T> toSet() {
        return stream.collect(Collectors.toSet());
    }

    public Optional<T> findFirst() {
        return stream.findFirst();
    }

    public Optional<T> findAny() {
        return stream.findAny();
    }

    public long count() {
        return stream.count();
    }

    public void forEach(Consumer<T> action) {
        stream.forEach(action);
    }

    // Usage example
    public static void main(String[] args) {
        var result = DataPipeline.from(List.of("apple", "banana", "cherry", "date"))
                .filter(s -> s.length() > 4)
                .transform(String::toUpperCase)
                .sorted(String::compareTo)
                .limit(2)
                .toList();
    }
}

// 2. Configuration fluent API
public class HttpClientConfig {
    private Duration connectTimeout = Duration.ofSeconds(10);
    private Duration readTimeout = Duration.ofSeconds(30);
    private int maxRetries = 3;
    private boolean followRedirects = true;
    private final Map<String, String> headers = new HashMap<>();
    private final List<HttpInterceptor> interceptors = new ArrayList<>();

    public static HttpClientConfig create() {
        return new HttpClientConfig();
    }

    public HttpClientConfig timeout(Duration connect, Duration read) {
        this.connectTimeout = Objects.requireNonNull(connect);
        this.readTimeout = Objects.requireNonNull(read);
        return this;
    }

    public HttpClientConfig retries(int maxRetries) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException("Max retries cannot be negative");
        }
        this.maxRetries = maxRetries;
        return this;
    }

    public HttpClientConfig followRedirects(boolean follow) {
        this.followRedirects = follow;
        return this;
    }

    public HttpClientConfig header(String name, String value) {
        headers.put(Objects.requireNonNull(name), Objects.requireNonNull(value));
        return this;
    }

    public HttpClientConfig headers(Map<String, String> headers) {
        this.headers.putAll(Objects.requireNonNull(headers));
        return this;
    }

    public HttpClientConfig interceptor(HttpInterceptor interceptor) {
        interceptors.add(Objects.requireNonNull(interceptor));
        return this;
    }

    public HttpClientConfig interceptors(HttpInterceptor... interceptors) {
        this.interceptors.addAll(Arrays.asList(interceptors));
        return this;
    }

    // Build method
    public HttpClient build() {
        return new HttpClient(this);
    }

    // Getters for HttpClient constructor
    Duration getConnectTimeout() { return connectTimeout; }
    Duration getReadTimeout() { return readTimeout; }
    int getMaxRetries() { return maxRetries; }
    boolean isFollowRedirects() { return followRedirects; }
    Map<String, String> getHeaders() { return Map.copyOf(headers); }
    List<HttpInterceptor> getInterceptors() { return List.copyOf(interceptors); }

    // Usage
    public static void main(String[] args) {
        var client = HttpClientConfig.create()
                .timeout(Duration.ofSeconds(5), Duration.ofSeconds(15))
                .retries(5)
                .followRedirects(false)
                .header("User-Agent", "MyApp/1.0")
                .header("Accept", "application/json")
                .interceptor(new LoggingInterceptor())
                .interceptor(new AuthInterceptor())
                .build();
    }
}

// 3. DSL-style fluent API
public class SqlQueryBuilder {
    private final StringBuilder query = new StringBuilder();
    private final List<Object> parameters = new ArrayList<>();

    public static SelectBuilder select(String... columns) {
        return new SelectBuilder(columns);
    }

    public static class SelectBuilder {
        private final SqlQueryBuilder builder = new SqlQueryBuilder();

        SelectBuilder(String... columns) {
            builder.query.append("SELECT ");
            if (columns.length == 0) {
                builder.query.append("*");
            } else {
                builder.query.append(String.join(", ", columns));
            }
        }

        public FromBuilder from(String table) {
            builder.query.append(" FROM ").append(table);
            return new FromBuilder(builder);
        }
    }

    public static class FromBuilder {
        private final SqlQueryBuilder builder;

        FromBuilder(SqlQueryBuilder builder) {
            this.builder = builder;
        }

        public WhereBuilder where(String condition, Object... params) {
            builder.query.append(" WHERE ").append(condition);
            builder.parameters.addAll(Arrays.asList(params));
            return new WhereBuilder(builder);
        }

        public OrderByBuilder orderBy(String column, SortOrder order) {
            builder.query.append(" ORDER BY ").append(column).append(" ").append(order);
            return new OrderByBuilder(builder);
        }

        public LimitBuilder limit(int count) {
            builder.query.append(" LIMIT ").append(count);
            return new LimitBuilder(builder);
        }

        public SqlQuery build() {
            return new SqlQuery(builder.query.toString(), List.copyOf(builder.parameters));
        }
    }

    public static class WhereBuilder {
        private final SqlQueryBuilder builder;

        WhereBuilder(SqlQueryBuilder builder) {
            this.builder = builder;
        }

        public WhereBuilder and(String condition, Object... params) {
            builder.query.append(" AND ").append(condition);
            builder.parameters.addAll(Arrays.asList(params));
            return this;
        }

        public WhereBuilder or(String condition, Object... params) {
            builder.query.append(" OR ").append(condition);
            builder.parameters.addAll(Arrays.asList(params));
            return this;
        }

        public OrderByBuilder orderBy(String column, SortOrder order) {
            builder.query.append(" ORDER BY ").append(column).append(" ").append(order);
            return new OrderByBuilder(builder);
        }

        public LimitBuilder limit(int count) {
            builder.query.append(" LIMIT ").append(count);
            return new LimitBuilder(builder);
        }

        public SqlQuery build() {
            return new SqlQuery(builder.query.toString(), List.copyOf(builder.parameters));
        }
    }

    public static class OrderByBuilder {
        private final SqlQueryBuilder builder;

        OrderByBuilder(SqlQueryBuilder builder) {
            this.builder = builder;
        }

        public OrderByBuilder thenBy(String column, SortOrder order) {
            builder.query.append(", ").append(column).append(" ").append(order);
            return this;
        }

        public LimitBuilder limit(int count) {
            builder.query.append(" LIMIT ").append(count);
            return new LimitBuilder(builder);
        }

        public SqlQuery build() {
            return new SqlQuery(builder.query.toString(), List.copyOf(builder.parameters));
        }
    }

    public static class LimitBuilder {
        private final SqlQueryBuilder builder;

        LimitBuilder(SqlQueryBuilder builder) {
            this.builder = builder;
        }

        public SqlQuery build() {
            return new SqlQuery(builder.query.toString(), List.copyOf(builder.parameters));
        }
    }

    // Usage example
    public static void main(String[] args) {
        var query = SqlQueryBuilder
            .select("id", "name", "email")
            .from("users")
            .where("age > ?", 18)
            .and("status = ?", "ACTIVE")
            .orderBy("name", SortOrder.ASC)
            .thenBy("email", SortOrder.DESC)
            .limit(50)
            .build();
    }
}

public record SqlQuery(String sql, List<Object> parameters) {}

interface HttpInterceptor {}
class LoggingInterceptor implements HttpInterceptor {}
class AuthInterceptor implements HttpInterceptor {}
class HttpClient {
    HttpClient(HttpClientConfig config) {}
}
```

---

## 🔒 **Immutable API Design**

### **Immutable Object Patterns**

```java
/**
 * Immutable API design patterns
 */

// 1. Record-based immutable objects (Java 17)
public record Customer(
    String id,
    String name,
    String email,
    List<Address> addresses,
    Set<String> tags,
    CustomerStatus status,
    Instant createdAt
) {
    public Customer {
        Objects.requireNonNull(id, "ID cannot be null");
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(email, "Email cannot be null");
        Objects.requireNonNull(status, "Status cannot be null");
        Objects.requireNonNull(createdAt, "Created timestamp cannot be null");

        // Defensive copying for mutable fields
        addresses = addresses != null ? List.copyOf(addresses) : List.of();
        tags = tags != null ? Set.copyOf(tags) : Set.of();

        // Validation
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    // Convenience factory methods
    public static Customer of(String id, String name, String email) {
        return new Customer(id, name, email, List.of(), Set.of(),
                          CustomerStatus.ACTIVE, Instant.now());
    }

    // Immutable update methods
    public Customer withName(String newName) {
        return new Customer(id, newName, email, addresses, tags, status, createdAt);
    }

    public Customer withEmail(String newEmail) {
        return new Customer(id, name, newEmail, addresses, tags, status, createdAt);
    }

    public Customer withStatus(CustomerStatus newStatus) {
        return new Customer(id, name, email, addresses, tags, newStatus, createdAt);
    }

    public Customer addAddress(Address address) {
        var newAddresses = new ArrayList<>(addresses);
        newAddresses.add(Objects.requireNonNull(address));
        return new Customer(id, name, email, newAddresses, tags, status, createdAt);
    }

    public Customer addTag(String tag) {
        var newTags = new HashSet<>(tags);
        newTags.add(Objects.requireNonNull(tag));
        return new Customer(id, name, email, addresses, newTags, status, createdAt);
    }

    private static boolean isValidEmail(String email) {
        return email.contains("@");  // Simplified validation
    }
}

// 2. Builder for complex immutable objects
public final class Order {
    private final String orderId;
    private final String customerId;
    private final List<OrderItem> items;
    private final BigDecimal totalAmount;
    private final OrderStatus status;
    private final Instant orderDate;
    private final Address shippingAddress;
    private final PaymentMethod paymentMethod;

    private Order(Builder builder) {
        this.orderId = builder.orderId;
        this.customerId = builder.customerId;
        this.items = List.copyOf(builder.items);
        this.totalAmount = builder.totalAmount;
        this.status = builder.status;
        this.orderDate = builder.orderDate;
        this.shippingAddress = builder.shippingAddress;
        this.paymentMethod = builder.paymentMethod;
    }

    public static Builder builder() {
        return new Builder();
    }

    // Immutable update methods
    public Order withStatus(OrderStatus newStatus) {
        return toBuilder().status(newStatus).build();
    }

    public Order addItem(OrderItem item) {
        var newItems = new ArrayList<>(items);
        newItems.add(item);
        return toBuilder().items(newItems).build();
    }

    // Convert to builder for modifications
    public Builder toBuilder() {
        return new Builder()
            .orderId(orderId)
            .customerId(customerId)
            .items(items)
            .totalAmount(totalAmount)
            .status(status)
            .orderDate(orderDate)
            .shippingAddress(shippingAddress)
            .paymentMethod(paymentMethod);
    }

    public static class Builder {
        private String orderId;
        private String customerId;
        private List<OrderItem> items = new ArrayList<>();
        private BigDecimal totalAmount;
        private OrderStatus status = OrderStatus.PENDING;
        private Instant orderDate = Instant.now();
        private Address shippingAddress;
        private PaymentMethod paymentMethod;

        public Builder orderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder customerId(String customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder items(Collection<OrderItem> items) {
            this.items = new ArrayList<>(Objects.requireNonNull(items));
            return this;
        }

        public Builder addItem(OrderItem item) {
            this.items.add(Objects.requireNonNull(item));
            return this;
        }

        public Builder totalAmount(BigDecimal totalAmount) {
            this.totalAmount = totalAmount;
            return this;
        }

        public Builder status(OrderStatus status) {
            this.status = status;
            return this;
        }

        public Builder orderDate(Instant orderDate) {
            this.orderDate = orderDate;
            return this;
        }

        public Builder shippingAddress(Address shippingAddress) {
            this.shippingAddress = shippingAddress;
            return this;
        }

        public Builder paymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Order build() {
            Objects.requireNonNull(orderId, "Order ID is required");
            Objects.requireNonNull(customerId, "Customer ID is required");
            if (items.isEmpty()) {
                throw new IllegalStateException("Order must have at least one item");
            }
            Objects.requireNonNull(shippingAddress, "Shipping address is required");
            Objects.requireNonNull(paymentMethod, "Payment method is required");

            // Calculate total if not provided
            if (totalAmount == null) {
                totalAmount = items.stream()
                    .map(OrderItem::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            }

            return new Order(this);
        }
    }

    // Getters
    public String getOrderId() { return orderId; }
    public String getCustomerId() { return customerId; }
    public List<OrderItem> getItems() { return items; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public OrderStatus getStatus() { return status; }
    public Instant getOrderDate() { return orderDate; }
    public Address getShippingAddress() { return shippingAddress; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Order other &&
               Objects.equals(orderId, other.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderId);
    }
}

// 3. Collection wrappers for immutability
public final class ImmutableOrderHistory {
    private final List<Order> orders;
    private final String customerId;

    public ImmutableOrderHistory(String customerId, Collection<Order> orders) {
        this.customerId = Objects.requireNonNull(customerId);
        this.orders = List.copyOf(Objects.requireNonNull(orders));
    }

    public List<Order> getOrders() {
        return orders;  // Already immutable
    }

    public String getCustomerId() {
        return customerId;
    }

    public ImmutableOrderHistory addOrder(Order order) {
        Objects.requireNonNull(order);
        if (!customerId.equals(order.getCustomerId())) {
            throw new IllegalArgumentException("Order customer ID doesn't match");
        }

        var newOrders = new ArrayList<>(orders);
        newOrders.add(order);
        return new ImmutableOrderHistory(customerId, newOrders);
    }

    public ImmutableOrderHistory updateOrder(String orderId, Order updatedOrder) {
        Objects.requireNonNull(orderId);
        Objects.requireNonNull(updatedOrder);

        var newOrders = orders.stream()
            .map(order -> order.getOrderId().equals(orderId) ? updatedOrder : order)
            .toList();

        return new ImmutableOrderHistory(customerId, newOrders);
    }

    public Optional<Order> findOrder(String orderId) {
        return orders.stream()
            .filter(order -> order.getOrderId().equals(orderId))
            .findFirst();
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orders.stream()
            .filter(order -> order.getStatus() == status)
            .toList();
    }

    public int getOrderCount() {
        return orders.size();
    }

    public BigDecimal getTotalValue() {
        return orders.stream()
            .map(Order::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

// Supporting classes
public record Address(String street, String city, String state, String zipCode) {}
public record OrderItem(String productId, int quantity, BigDecimal unitPrice) {
    public BigDecimal getTotal() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }
}

public enum CustomerStatus { ACTIVE, INACTIVE, SUSPENDED }
public enum OrderStatus { PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED }
public enum PaymentMethod { CREDIT_CARD, DEBIT_CARD, PAYPAL, BANK_TRANSFER }
```

---

## 📈 **Version Compatibility**

### **API Versioning Strategies**

```java
/**
 * API versioning and backward compatibility
 */

// 1. Semantic versioning approach
public class ApiVersion {
    public static final String CURRENT_VERSION = "2.1.0";

    // Version compatibility matrix
    public static boolean isCompatible(String requiredVersion, String providedVersion) {
        var required = parseVersion(requiredVersion);
        var provided = parseVersion(providedVersion);

        // Major version must match
        if (required.major() != provided.major()) {
            return false;
        }

        // Minor version of provided must be >= required
        if (provided.minor() < required.minor()) {
            return false;
        }

        // Patch version doesn't affect compatibility
        return true;
    }

    private static Version parseVersion(String version) {
        var parts = version.split("\\.");
        return new Version(
            Integer.parseInt(parts[0]),
            Integer.parseInt(parts[1]),
            Integer.parseInt(parts[2])
        );
    }

    private record Version(int major, int minor, int patch) {}
}

// 2. Deprecated annotation usage
public class UserService {

    // Current method
    public User createUser(UserRequest request) {
        return createUser(request.name(), request.email(), request.preferences());
    }

    // Deprecated method with migration path
    @Deprecated(since = "2.0.0", forRemoval = true)
    public User createUser(String name, String email) {
        return createUser(name, email, UserPreferences.defaults());
    }

    public User createUser(String name, String email, UserPreferences preferences) {
        // Implementation
        return new User(name, email, preferences);
    }

    // Overloaded methods for backward compatibility
    public List<User> findUsers() {
        return findUsers(Pageable.unpaged());
    }

    public List<User> findUsers(int page, int size) {
        return findUsers(Pageable.of(page, size));
    }

    public List<User> findUsers(Pageable pageable) {
        // New implementation with pagination
        return List.of();
    }
}

// 3. Interface evolution
public interface PaymentProcessor {
    // Original method
    PaymentResult process(Payment payment);

    // Added in v2.0 with default implementation
    default PaymentResult processWithMetadata(Payment payment, Map<String, String> metadata) {
        // Default implementation for backward compatibility
        return process(payment);
    }

    // Added in v2.1 with default implementation
    default boolean supportsPaymentType(PaymentType type) {
        return true;  // Conservative default
    }

    // Added in v2.2 - will be abstract in v3.0
    default Optional<PaymentEstimate> estimateFees(Payment payment) {
        return Optional.empty();  // Temporary default
    }
}

// 4. Configuration object evolution
public class DatabaseConfig {
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final Duration connectionTimeout;
    private final Duration queryTimeout;
    private final Map<String, Object> properties;

    // Builder pattern allows adding new fields without breaking existing code
    public static class Builder {
        private String host = "localhost";
        private int port = 5432;
        private String database;
        private String username;
        private String password;
        private Duration connectionTimeout = Duration.ofSeconds(30);
        private Duration queryTimeout = Duration.ofSeconds(60);

        // Added in v2.0
        private Map<String, Object> properties = new HashMap<>();

        // Original methods
        public Builder host(String host) { this.host = host; return this; }
        public Builder port(int port) { this.port = port; return this; }
        public Builder database(String database) { this.database = database; return this; }
        public Builder credentials(String username, String password) {
            this.username = username;
            this.password = password;
            return this;
        }

        // Added in v1.5 - backward compatible
        public Builder connectionTimeout(Duration timeout) {
            this.connectionTimeout = timeout;
            return this;
        }

        public Builder queryTimeout(Duration timeout) {
            this.queryTimeout = timeout;
            return this;
        }

        // Added in v2.0 - backward compatible
        public Builder property(String key, Object value) {
            this.properties.put(key, value);
            return this;
        }

        public Builder properties(Map<String, Object> properties) {
            this.properties.putAll(properties);
            return this;
        }

        public DatabaseConfig build() {
            return new DatabaseConfig(this);
        }
    }

    private DatabaseConfig(Builder builder) {
        this.host = builder.host;
        this.port = builder.port;
        this.database = builder.database;
        this.username = builder.username;
        this.password = builder.password;
        this.connectionTimeout = builder.connectionTimeout;
        this.queryTimeout = builder.queryTimeout;
        this.properties = Map.copyOf(builder.properties);
    }

    // Getters remain stable across versions
    public String getHost() { return host; }
    public int getPort() { return port; }
    // ... other getters
}

// 5. Feature flags for gradual rollout
public class FeatureFlags {
    private static final Map<String, Boolean> flags = new ConcurrentHashMap<>();

    static {
        // Default feature flags
        flags.put("NEW_PAYMENT_FLOW", false);
        flags.put("ENHANCED_LOGGING", true);
        flags.put("ASYNC_PROCESSING", false);
    }

    public static boolean isEnabled(String feature) {
        return flags.getOrDefault(feature, false);
    }

    public static void enable(String feature) {
        flags.put(feature, true);
    }

    public static void disable(String feature) {
        flags.put(feature, false);
    }
}

public class OrderProcessor {
    public void processOrder(Order order) {
        if (FeatureFlags.isEnabled("NEW_PAYMENT_FLOW")) {
            processOrderV2(order);
        } else {
            processOrderV1(order);
        }
    }

    private void processOrderV1(Order order) {
        // Legacy implementation
    }

    private void processOrderV2(Order order) {
        // New implementation
    }
}
```

---

## ⚡ **Performance Considerations**

### **Performance-Aware API Design**

```java
/**
 * Performance considerations in API design
 */

// 1. Lazy loading and caching
public class UserRepository {
    private final Cache<String, User> userCache = Caffeine.newBuilder()
        .maximumSize(1000)
        .expireAfterWrite(Duration.ofMinutes(30))
        .build();

    private final LoadingCache<String, List<Order>> orderCache = Caffeine.newBuilder()
        .maximumSize(500)
        .expireAfterWrite(Duration.ofMinutes(10))
        .build(this::loadOrdersFromDatabase);

    public Optional<User> findUser(String id) {
        return Optional.ofNullable(userCache.get(id, this::loadUserFromDatabase));
    }

    public List<Order> getUserOrders(String userId) {
        return orderCache.get(userId);
    }

    // Batch operations for efficiency
    public Map<String, User> findUsers(Set<String> userIds) {
        var result = new HashMap<String, User>();
        var uncachedIds = new HashSet<String>();

        // Check cache first
        for (var id : userIds) {
            var cached = userCache.getIfPresent(id);
            if (cached != null) {
                result.put(id, cached);
            } else {
                uncachedIds.add(id);
            }
        }

        // Batch load uncached users
        if (!uncachedIds.isEmpty()) {
            var loaded = loadUsersFromDatabase(uncachedIds);
            loaded.forEach((id, user) -> {
                result.put(id, user);
                userCache.put(id, user);
            });
        }

        return result;
    }

    private User loadUserFromDatabase(String id) {
        // Database call
        return new User();
    }

    private List<Order> loadOrdersFromDatabase(String userId) {
        // Database call
        return List.of();
    }

    private Map<String, User> loadUsersFromDatabase(Set<String> ids) {
        // Batch database call
        return Map.of();
    }
}

// 2. Streaming and pagination
public class DataService {

    // Stream large datasets
    public Stream<Record> streamRecords(RecordFilter filter) {
        return StreamSupport.stream(
            new RecordSpliterator(filter),
            false
        );
    }

    // Pagination for large result sets
    public Page<Record> getRecords(RecordFilter filter, Pageable pageable) {
        var records = repository.findRecords(filter, pageable);
        var totalCount = repository.countRecords(filter);
        return new Page<>(records, pageable, totalCount);
    }

    // Cursor-based pagination for real-time data
    public CursorPage<Record> getRecordsCursor(String cursor, int limit) {
        var records = repository.findRecordsAfter(cursor, limit + 1);

        String nextCursor = null;
        if (records.size() > limit) {
            nextCursor = records.get(limit - 1).getId();
            records = records.subList(0, limit);
        }

        return new CursorPage<>(records, nextCursor);
    }

    private static class RecordSpliterator implements Spliterator<Record> {
        private final RecordFilter filter;
        private Iterator<Record> currentBatch;
        private String cursor;

        RecordSpliterator(RecordFilter filter) {
            this.filter = filter;
            loadNextBatch();
        }

        @Override
        public boolean tryAdvance(Consumer<? super Record> action) {
            if (!currentBatch.hasNext()) {
                loadNextBatch();
            }

            if (currentBatch.hasNext()) {
                action.accept(currentBatch.next());
                return true;
            }

            return false;
        }

        @Override
        public Spliterator<Record> trySplit() {
            return null;  // Sequential processing
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;  // Unknown size
        }

        @Override
        public int characteristics() {
            return ORDERED | NONNULL;
        }

        private void loadNextBatch() {
            // Load next batch from database
            var batch = repository.findRecordsAfter(cursor, 1000);
            currentBatch = batch.iterator();
            if (!batch.isEmpty()) {
                cursor = batch.get(batch.size() - 1).getId();
            }
        }
    }
}

// 3. Object pooling for expensive resources
public class ConnectionPool {
    private final BlockingQueue<Connection> available;
    private final Set<Connection> inUse;
    private final int maxConnections;
    private final Duration maxWaitTime;

    public ConnectionPool(int maxConnections, Duration maxWaitTime) {
        this.maxConnections = maxConnections;
        this.maxWaitTime = maxWaitTime;
        this.available = new ArrayBlockingQueue<>(maxConnections);
        this.inUse = ConcurrentHashMap.newKeySet();

        // Pre-populate pool
        for (int i = 0; i < maxConnections; i++) {
            available.offer(createConnection());
        }
    }

    public Connection acquire() throws InterruptedException {
        var connection = available.poll(maxWaitTime.toMillis(), TimeUnit.MILLISECONDS);
        if (connection == null) {
            throw new RuntimeException("Connection acquisition timeout");
        }

        inUse.add(connection);
        return connection;
    }

    public void release(Connection connection) {
        if (inUse.remove(connection)) {
            if (isValid(connection)) {
                available.offer(connection);
            } else {
                available.offer(createConnection());
            }
        }
    }

    private Connection createConnection() {
        return new Connection();
    }

    private boolean isValid(Connection connection) {
        return true;  // Validate connection
    }
}

// 4. Memory-efficient collections
public class MemoryEfficientCollections {

    // Use specialized collections for primitives
    public static class IntegerSet {
        private final BitSet bits = new BitSet();

        public void add(int value) {
            if (value < 0) throw new IllegalArgumentException("Negative values not supported");
            bits.set(value);
        }

        public boolean contains(int value) {
            return value >= 0 && bits.get(value);
        }

        public int size() {
            return bits.cardinality();
        }

        public IntStream stream() {
            return bits.stream();
        }
    }

    // Compact string storage
    public static class CompactStringList {
        private final List<String> interned = new ArrayList<>();

        public void add(String value) {
            interned.add(value.intern());  // Use string interning
        }

        public String get(int index) {
            return interned.get(index);
        }

        public int size() {
            return interned.size();
        }
    }

    // Flyweight pattern for immutable objects
    public static class Status {
        private static final Map<String, Status> cache = new ConcurrentHashMap<>();

        private final String code;
        private final String description;

        private Status(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public static Status of(String code, String description) {
            return cache.computeIfAbsent(code + ":" + description,
                k -> new Status(code, description));
        }

        public String getCode() { return code; }
        public String getDescription() { return description; }
    }
}

// Supporting classes
public record Page<T>(List<T> content, Pageable pageable, long totalElements) {}
public record CursorPage<T>(List<T> content, String nextCursor) {}
public record Pageable(int page, int size) {
    public static Pageable of(int page, int size) { return new Pageable(page, size); }
    public static Pageable unpaged() { return new Pageable(0, Integer.MAX_VALUE); }
}
public class Record { public String getId() { return ""; } }
public class RecordFilter {}
public class Connection {}
public class User {}
public class UserPreferences { public static UserPreferences defaults() { return new UserPreferences(); } }
public record UserRequest(String name, String email, UserPreferences preferences) {}
```

---

## 📖 **Documentation and Naming**

### **JavaDoc Best Practices**

```java
/**
 * Comprehensive JavaDoc examples for API documentation
 */

/**
 * Manages customer orders in the e-commerce system.
 *
 * <p>This service provides operations for creating, updating, and querying orders.
 * All operations are thread-safe and support transactional processing.</p>
 *
 * <p><b>Usage Example:</b></p>
 * <pre>{@code
 * var orderService = new OrderService(orderRepository, paymentService);
 * var request = OrderRequest.builder()
 *     .customerId("CUST-123")
 *     .addItem("PROD-456", 2)
 *     .build();
 * var order = orderService.createOrder(request);
 * }</pre>
 *
 * @author Development Team
 * @version 2.1.0
 * @since 1.0.0
 * @see OrderRepository
 * @see PaymentService
 */
public class OrderService {

    /**
     * Creates a new order for the specified customer.
     *
     * <p>The order will be validated before creation. If validation fails,
     * a {@link ValidationException} will be thrown with details about the
     * validation errors.</p>
     *
     * <p><b>Validation Rules:</b></p>
     * <ul>
     *   <li>Customer must exist and be active</li>
     *   <li>At least one order item is required</li>
     *   <li>All products must be available</li>
     *   <li>Payment method must be valid</li>
     * </ul>
     *
     * @param request the order creation request containing customer and item details
     * @return the created order with generated ID and status
     * @throws ValidationException if the order request fails validation
     * @throws CustomerNotFoundException if the customer doesn't exist
     * @throws PaymentException if payment processing fails
     * @throws IllegalArgumentException if {@code request} is {@code null}
     * @see OrderRequest
     * @see Order
     * @since 1.0.0
     */
    public Order createOrder(OrderRequest request) throws ValidationException {
        Objects.requireNonNull(request, "Order request cannot be null");

        // Validate the request
        validateOrderRequest(request);

        // Create and process the order
        var order = processOrder(request);

        return order;
    }

    /**
     * Finds orders by customer ID with pagination support.
     *
     * <p>This method supports efficient pagination for large result sets.
     * Use {@link Pageable#unpaged()} to retrieve all orders without pagination.</p>
     *
     * @param customerId the ID of the customer whose orders to retrieve
     * @param pageable pagination information including page number and size
     * @return a page containing the customer's orders, never {@code null}
     * @throws IllegalArgumentException if {@code customerId} is {@code null} or blank
     * @throws IllegalArgumentException if {@code pageable} is {@code null}
     * @implNote This method uses database indexes on customer_id for optimal performance
     * @see Page
     * @see Pageable
     * @since 1.2.0
     */
    public Page<Order> findOrdersByCustomer(String customerId, Pageable pageable) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be null or blank");
        }
        Objects.requireNonNull(pageable, "Pageable cannot be null");

        return orderRepository.findByCustomerId(customerId, pageable);
    }

    /**
     * Cancels an order if it's in a cancellable state.
     *
     * <p><b>Cancellation Rules:</b></p>
     * <table border="1">
     *   <caption>Order Status Cancellation Matrix</caption>
     *   <tr><th>Status</th><th>Can Cancel</th><th>Refund Policy</th></tr>
     *   <tr><td>PENDING</td><td>Yes</td><td>Full refund</td></tr>
     *   <tr><td>CONFIRMED</td><td>Yes</td><td>Full refund</td></tr>
     *   <tr><td>SHIPPED</td><td>No</td><td>N/A</td></tr>
     *   <tr><td>DELIVERED</td><td>No</td><td>Return policy applies</td></tr>
     * </table>
     *
     * @param orderId the ID of the order to cancel
     * @param reason the reason for cancellation (for audit purposes)
     * @return {@code true} if the order was successfully cancelled, {@code false} otherwise
     * @throws OrderNotFoundException if no order exists with the given ID
     * @throws IllegalStateException if the order cannot be cancelled in its current state
     * @apiNote This operation is idempotent - calling it multiple times has the same effect
     * @since 1.1.0
     */
    public boolean cancelOrder(String orderId, String reason) {
        // Implementation
        return true;
    }

    /**
     * Configuration builder for the OrderService.
     *
     * <p>This builder allows customization of service behavior including
     * timeout settings, retry policies, and validation rules.</p>
     *
     * @since 2.0.0
     */
    public static class Builder {
        /**
         * Sets the maximum time to wait for order processing.
         *
         * @param timeout the processing timeout, must be positive
         * @return this builder instance for method chaining
         * @throws IllegalArgumentException if timeout is zero or negative
         */
        public Builder processingTimeout(Duration timeout) {
            // Implementation
            return this;
        }
    }
}

/**
 * Enumeration of order statuses in the order lifecycle.
 *
 * <p>Orders progress through these statuses in a defined sequence.
 * Not all transitions are valid - see the state diagram below.</p>
 *
 * <p><b>State Transitions:</b></p>
 * <pre>
 * PENDING → CONFIRMED → SHIPPED → DELIVERED
 *    ↓         ↓
 * CANCELLED  CANCELLED
 * </pre>
 *
 * @since 1.0.0
 */
public enum OrderStatus {
    /**
     * Order has been created but not yet confirmed.
     * Payment may still be processing.
     */
    PENDING,

    /**
     * Order has been confirmed and payment processed.
     * Ready for fulfillment.
     */
    CONFIRMED,

    /**
     * Order has been shipped to the customer.
     * Tracking information is available.
     */
    SHIPPED,

    /**
     * Order has been delivered to the customer.
     * This is the final successful state.
     */
    DELIVERED,

    /**
     * Order has been cancelled.
     * This is a terminal state.
     */
    CANCELLED
}
```

### **Naming Conventions**

```java
/**
 * Consistent naming patterns for API elements
 */

// 1. Class names - nouns, PascalCase
public class CustomerRepository { }        // Good: clear purpose
public class EmailNotificationService { } // Good: descriptive
public class UserMgr { }                  // Bad: abbreviated
public class DoStuff { }                  // Bad: vague

// 2. Interface names - nouns or adjectives
public interface PaymentProcessor { }      // Good: noun describing capability
public interface Serializable { }         // Good: adjective describing property
public interface Readable { }             // Good: capability
public interface IPaymentProcessor { }     // Bad: Hungarian notation

// 3. Method names - verbs, camelCase
public class UserService {
    // Good: verb + object
    public User createUser(String name) { return null; }
    public void deleteUser(String id) { }
    public List<User> findActiveUsers() { return null; }
    public boolean isUserActive(String id) { return true; }
    public boolean hasPermission(String userId, String permission) { return true; }

    // Bad examples
    public User user(String name) { return null; }        // Bad: not a verb
    public void remove(String id) { }                     // Bad: vague
    public List<User> users() { return null; }            // Bad: not descriptive
}

// 4. Constants - SCREAMING_SNAKE_CASE
public class ApiConstants {
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final int MAX_RETRY_ATTEMPTS = 3;
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);

    // Bad
    public static final String defaultEncoding = "UTF-8";  // Bad: camelCase
    public static final int maxRetries = 3;                // Bad: camelCase
}

// 5. Package names - lowercase, hierarchical
// Good package structure:
// com.company.ecommerce.order.service
// com.company.ecommerce.order.repository
// com.company.ecommerce.customer.api
// com.company.ecommerce.payment.processor

// Bad:
// com.Company.Ecommerce.Order     // Bad: capitals
// com.company.ecommerceOrder      // Bad: not hierarchical
// order                           // Bad: too generic

// 6. Generic type parameters
public class Repository<T, ID> { }           // Good: standard conventions
public class Cache<K, V> { }                 // Good: Key, Value
public class Function<T, R> { }              // Good: Type, Return
public class Converter<F, T> { }             // Good: From, To

public class Repository<Type, Identifier> { } // Bad: too verbose
public class Cache<A, B> { }                  // Bad: meaningless names

// 7. Boolean methods and variables
public class User {
    private boolean active;                   // Good: adjective
    private boolean emailVerified;            // Good: past participle

    public boolean isActive() { return active; }
    public boolean hasPermission(String perm) { return true; }
    public boolean canEdit() { return true; }
    public boolean shouldNotify() { return true; }

    // Bad
    private boolean activate;                 // Bad: verb, not state
    public boolean getActive() { return true; } // Bad: use 'is' for boolean
}

// 8. Collection and array naming
public class OrderService {
    private final List<OrderValidator> validators;     // Good: plural noun
    private final Map<String, Order> orderCache;       // Good: descriptive
    private final Set<String> supportedPaymentMethods; // Good: descriptive

    // Bad
    private final List<OrderValidator> validatorList;  // Bad: redundant suffix
    private final Map<String, Order> orders;           // Bad: vague
}

// 9. Exception naming
public class CustomerNotFoundException extends RuntimeException { } // Good: specific + Exception
public class InvalidOrderStateException extends RuntimeException { } // Good: clear cause
public class PaymentProcessingException extends Exception { }        // Good: business domain

// Bad
public class CustomerException extends Exception { }     // Bad: too generic
public class OrderError extends Error { }               // Bad: Error for runtime issues
public class BadCustomer extends RuntimeException { }    // Bad: informal naming
```

---

## 🎨 **Common Design Patterns**

### **Essential Patterns for API Design**

```java
/**
 * Design patterns commonly used in Java APIs
 */

// 1. Factory Pattern
public interface PaymentProcessorFactory {
    PaymentProcessor createProcessor(PaymentType type);

    Set<PaymentType> getSupportedTypes();
}

public class PaymentProcessorFactoryImpl implements PaymentProcessorFactory {
    private final Map<PaymentType, Supplier<PaymentProcessor>> processors;

    public PaymentProcessorFactoryImpl() {
        processors = Map.of(
            PaymentType.CREDIT_CARD, CreditCardProcessor::new,
            PaymentType.PAYPAL, PayPalProcessor::new,
            PaymentType.BANK_TRANSFER, BankTransferProcessor::new
        );
    }

    @Override
    public PaymentProcessor createProcessor(PaymentType type) {
        var supplier = processors.get(type);
        if (supplier == null) {
            throw new UnsupportedOperationException("Unsupported payment type: " + type);
        }
        return supplier.get();
    }

    @Override
    public Set<PaymentType> getSupportedTypes() {
        return processors.keySet();
    }
}

// 2. Strategy Pattern
public interface TaxCalculationStrategy {
    BigDecimal calculateTax(Order order);
    boolean appliesTo(Address address);
}

public class USTaxStrategy implements TaxCalculationStrategy {
    @Override
    public BigDecimal calculateTax(Order order) {
        // US tax calculation logic
        return order.getSubtotal().multiply(BigDecimal.valueOf(0.08));
    }

    @Override
    public boolean appliesTo(Address address) {
        return "US".equals(address.country());
    }
}

public class EUTaxStrategy implements TaxCalculationStrategy {
    @Override
    public BigDecimal calculateTax(Order order) {
        // EU VAT calculation logic
        return order.getSubtotal().multiply(BigDecimal.valueOf(0.20));
    }

    @Override
    public boolean appliesTo(Address address) {
        return EU_COUNTRIES.contains(address.country());
    }

    private static final Set<String> EU_COUNTRIES = Set.of("DE", "FR", "IT", "ES");
}

public class TaxCalculator {
    private final List<TaxCalculationStrategy> strategies;

    public TaxCalculator(List<TaxCalculationStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    public BigDecimal calculateTax(Order order) {
        return strategies.stream()
            .filter(strategy -> strategy.appliesTo(order.getBillingAddress()))
            .findFirst()
            .map(strategy -> strategy.calculateTax(order))
            .orElse(BigDecimal.ZERO);
    }
}

// 3. Observer Pattern (using modern approach)
public class OrderEventPublisher {
    private final Set<OrderEventListener> listeners = ConcurrentHashMap.newKeySet();

    public void addListener(OrderEventListener listener) {
        listeners.add(Objects.requireNonNull(listener));
    }

    public void removeListener(OrderEventListener listener) {
        listeners.remove(listener);
    }

    public void publishOrderCreated(Order order) {
        var event = new OrderCreatedEvent(order, Instant.now());
        listeners.forEach(listener -> {
            try {
                listener.onOrderCreated(event);
            } catch (Exception e) {
                // Log error but don't affect other listeners
                logger.error("Error in order event listener", e);
            }
        });
    }

    public void publishOrderCancelled(Order order, String reason) {
        var event = new OrderCancelledEvent(order, reason, Instant.now());
        listeners.forEach(listener -> {
            try {
                listener.onOrderCancelled(event);
            } catch (Exception e) {
                logger.error("Error in order event listener", e);
            }
        });
    }
}

public interface OrderEventListener {
    default void onOrderCreated(OrderCreatedEvent event) { }
    default void onOrderCancelled(OrderCancelledEvent event) { }
    default void onOrderShipped(OrderShippedEvent event) { }
}

// 4. Command Pattern
public interface Command<T> {
    T execute();
    void undo();
    boolean canUndo();
}

public class CreateOrderCommand implements Command<Order> {
    private final OrderService orderService;
    private final OrderRequest request;
    private Order createdOrder;

    public CreateOrderCommand(OrderService orderService, OrderRequest request) {
        this.orderService = Objects.requireNonNull(orderService);
        this.request = Objects.requireNonNull(request);
    }

    @Override
    public Order execute() {
        if (createdOrder != null) {
            throw new IllegalStateException("Command already executed");
        }

        createdOrder = orderService.createOrder(request);
        return createdOrder;
    }

    @Override
    public void undo() {
        if (createdOrder == null) {
            throw new IllegalStateException("Command not executed");
        }

        orderService.cancelOrder(createdOrder.getOrderId(), "Command undo");
        createdOrder = null;
    }

    @Override
    public boolean canUndo() {
        return createdOrder != null &&
               createdOrder.getStatus().canBeCancelled();
    }
}

public class CommandExecutor {
    private final Deque<Command<?>> history = new ArrayDeque<>();
    private final int maxHistorySize;

    public CommandExecutor(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }

    public <T> T execute(Command<T> command) {
        T result = command.execute();

        if (command.canUndo()) {
            history.addLast(command);
            // Limit history size
            while (history.size() > maxHistorySize) {
                history.removeFirst();
            }
        }

        return result;
    }

    public void undoLast() {
        if (history.isEmpty()) {
            throw new IllegalStateException("No commands to undo");
        }

        var lastCommand = history.removeLast();
        lastCommand.undo();
    }

    public int getHistorySize() {
        return history.size();
    }
}

// 5. Decorator Pattern
public interface NotificationService {
    void sendNotification(String recipient, String message);
}

public class EmailNotificationService implements NotificationService {
    @Override
    public void sendNotification(String recipient, String message) {
        // Send email implementation
        System.out.println("Email sent to " + recipient + ": " + message);
    }
}

public abstract class NotificationDecorator implements NotificationService {
    protected final NotificationService wrapped;

    protected NotificationDecorator(NotificationService wrapped) {
        this.wrapped = Objects.requireNonNull(wrapped);
    }
}

public class LoggingNotificationDecorator extends NotificationDecorator {
    private static final Logger logger = LoggerFactory.getLogger(LoggingNotificationDecorator.class);

    public LoggingNotificationDecorator(NotificationService wrapped) {
        super(wrapped);
    }

    @Override
    public void sendNotification(String recipient, String message) {
        logger.info("Sending notification to {}", recipient);
        var start = System.currentTimeMillis();

        try {
            wrapped.sendNotification(recipient, message);
            logger.info("Notification sent successfully in {}ms",
                       System.currentTimeMillis() - start);
        } catch (Exception e) {
            logger.error("Failed to send notification to {}", recipient, e);
            throw e;
        }
    }
}

public class RetryNotificationDecorator extends NotificationDecorator {
    private final int maxRetries;
    private final Duration retryDelay;

    public RetryNotificationDecorator(NotificationService wrapped,
                                    int maxRetries, Duration retryDelay) {
        super(wrapped);
        this.maxRetries = maxRetries;
        this.retryDelay = retryDelay;
    }

    @Override
    public void sendNotification(String recipient, String message) {
        var attempt = 0;
        Exception lastException = null;

        while (attempt <= maxRetries) {
            try {
                wrapped.sendNotification(recipient, message);
                return; // Success
            } catch (Exception e) {
                lastException = e;
                attempt++;

                if (attempt <= maxRetries) {
                    try {
                        Thread.sleep(retryDelay.toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry", ie);
                    }
                }
            }
        }

        throw new RuntimeException("Failed after " + maxRetries + " retries", lastException);
    }
}

// Usage example
public class NotificationExample {
    public static void main(String[] args) {
        NotificationService service = new EmailNotificationService();

        // Add logging
        service = new LoggingNotificationDecorator(service);

        // Add retry capability
        service = new RetryNotificationDecorator(service, 3, Duration.ofSeconds(1));

        service.sendNotification("user@example.com", "Welcome!");
    }
}

// 6. Template Method Pattern
public abstract class DataProcessor<T> {

    // Template method - final to prevent override
    public final ProcessingResult process(T data) {
        var result = new ProcessingResult();

        try {
            // Step 1: Validate input
            validateInput(data);

            // Step 2: Pre-process (hook method)
            preProcess(data);

            // Step 3: Main processing (abstract method)
            var processed = doProcess(data);

            // Step 4: Post-process (hook method)
            postProcess(processed);

            result.setSuccess(true);
            result.setData(processed);

        } catch (Exception e) {
            result.setSuccess(false);
            result.setError(e.getMessage());
            handleError(e);
        }

        return result;
    }

    // Abstract method - must be implemented
    protected abstract T doProcess(T data);

    // Hook methods - can be overridden
    protected void validateInput(T data) {
        Objects.requireNonNull(data, "Input data cannot be null");
    }

    protected void preProcess(T data) {
        // Default: do nothing
    }

    protected void postProcess(T data) {
        // Default: do nothing
    }

    protected void handleError(Exception e) {
        // Default: log error
        System.err.println("Processing error: " + e.getMessage());
    }
}

// Concrete implementation
public class StringDataProcessor extends DataProcessor<String> {

    @Override
    protected String doProcess(String data) {
        return data.trim().toUpperCase();
    }

    @Override
    protected void validateInput(String data) {
        super.validateInput(data);
        if (data.isBlank()) {
            throw new IllegalArgumentException("Input cannot be blank");
        }
    }

    @Override
    protected void preProcess(String data) {
        System.out.println("Pre-processing: " + data);
    }

    @Override
    protected void postProcess(String data) {
        System.out.println("Post-processing result: " + data);
    }
}

// Supporting classes
public enum PaymentType { CREDIT_CARD, PAYPAL, BANK_TRANSFER }
public interface PaymentProcessor { }
public class CreditCardProcessor implements PaymentProcessor { }
public class PayPalProcessor implements PaymentProcessor { }
public class BankTransferProcessor implements PaymentProcessor { }

public record Address(String street, String city, String state, String country) { }
public class Order {
    public BigDecimal getSubtotal() { return BigDecimal.TEN; }
    public Address getBillingAddress() { return new Address("", "", "", "US"); }
    public String getOrderId() { return ""; }
    public OrderStatus getStatus() { return OrderStatus.PENDING; }
}

public record OrderCreatedEvent(Order order, Instant timestamp) { }
public record OrderCancelledEvent(Order order, String reason, Instant timestamp) { }
public record OrderShippedEvent(Order order, String trackingNumber, Instant timestamp) { }

public class ProcessingResult {
    private boolean success;
    private Object data;
    private String error;

    public void setSuccess(boolean success) { this.success = success; }
    public void setData(Object data) { this.data = data; }
    public void setError(String error) { this.error = error; }
}
```

---

## 🧪 **API Testing Strategies**

### **Comprehensive Testing Approaches**

```java
/**
 * Testing strategies for Java APIs
 */

// 1. Unit testing with mocks
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentService paymentService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OrderService orderService;

    @Test
    @DisplayName("Should create order successfully with valid request")
    void shouldCreateOrderSuccessfully() {
        // Given
        var request = OrderRequest.builder()
            .customerId("CUST-123")
            .addItem("PROD-456", 2, BigDecimal.valueOf(10.00))
            .build();

        var expectedOrder = Order.builder()
            .orderId("ORDER-789")
            .customerId("CUST-123")
            .status(OrderStatus.PENDING)
            .build();

        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);
        when(paymentService.processPayment(any())).thenReturn(PaymentResult.success());

        // When
        var result = orderService.createOrder(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getOrderId()).isEqualTo("ORDER-789");
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);

        verify(orderRepository).save(any(Order.class));
        verify(paymentService).processPayment(any());
        verify(notificationService).sendOrderConfirmation(result);
    }

    @Test
    @DisplayName("Should throw ValidationException for invalid request")
    void shouldThrowValidationExceptionForInvalidRequest() {
        // Given
        var invalidRequest = OrderRequest.builder()
            .customerId("")  // Invalid: empty customer ID
            .build();

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(invalidRequest))
            .isInstanceOf(ValidationException.class)
            .hasMessageContaining("Customer ID cannot be empty");

        verify(orderRepository, never()).save(any());
        verify(paymentService, never()).processPayment(any());
    }

    @Test
    @DisplayName("Should handle payment failure gracefully")
    void shouldHandlePaymentFailureGracefully() {
        // Given
        var request = OrderRequest.builder()
            .customerId("CUST-123")
            .addItem("PROD-456", 1, BigDecimal.valueOf(10.00))
            .build();

        when(paymentService.processPayment(any()))
            .thenThrow(new PaymentException("Insufficient funds"));

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(PaymentException.class)
            .hasMessageContaining("Insufficient funds");

        verify(orderRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "null"})
    @DisplayName("Should reject invalid customer IDs")
    void shouldRejectInvalidCustomerIds(String customerId) {
        // Given
        var request = OrderRequest.builder()
            .customerId("null".equals(customerId) ? null : customerId)
            .addItem("PROD-456", 1, BigDecimal.valueOf(10.00))
            .build();

        // When & Then
        assertThatThrownBy(() -> orderService.createOrder(request))
            .isInstanceOf(ValidationException.class);
    }
}

// 2. Integration testing
@SpringBootTest
@Testcontainers
class OrderServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
        .withDatabaseName("testdb")
        .withUsername("test")
        .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OrderService orderService;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @Transactional
    @Rollback
    @DisplayName("Should persist order to database")
    void shouldPersistOrderToDatabase() {
        // Given
        var customer = new Customer("CUST-123", "John Doe", "john@example.com");
        entityManager.persistAndFlush(customer);

        var request = OrderRequest.builder()
            .customerId("CUST-123")
            .addItem("PROD-456", 2, BigDecimal.valueOf(15.00))
            .build();

        // When
        var order = orderService.createOrder(request);

        // Then
        entityManager.flush();
        var savedOrder = entityManager.find(Order.class, order.getOrderId());

        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getCustomerId()).isEqualTo("CUST-123");
        assertThat(savedOrder.getItems()).hasSize(1);
        assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(30.00));
    }

    @Test
    @DisplayName("Should handle concurrent order creation")
    void shouldHandleConcurrentOrderCreation() throws InterruptedException {
        // Given
        var customer = new Customer("CUST-456", "Jane Doe", "jane@example.com");
        entityManager.persistAndFlush(customer);

        var executor = Executors.newFixedThreadPool(10);
        var latch = new CountDownLatch(10);
        var results = new ConcurrentLinkedQueue<Order>();
        var errors = new ConcurrentLinkedQueue<Exception>();

        // When
        for (int i = 0; i < 10; i++) {
            var finalI = i;
            executor.submit(() -> {
                try {
                    var request = OrderRequest.builder()
                        .customerId("CUST-456")
                        .addItem("PROD-" + finalI, 1, BigDecimal.valueOf(10.00))
                        .build();
                    var order = orderService.createOrder(request);
                    results.add(order);
                } catch (Exception e) {
                    errors.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Then
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(results).hasSize(10);
        assertThat(errors).isEmpty();
        assertThat(results.stream().map(Order::getOrderId).distinct()).hasSize(10);
    }
}

// 3. Contract testing
@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "payment-service")
class PaymentServiceContractTest {

    @Pact(consumer = "order-service")
    public RequestResponsePact createValidPaymentPact(PactDslWithProvider builder) {
        return builder
            .given("payment service is available")
            .uponReceiving("a valid payment request")
            .path("/api/payments")
            .method("POST")
            .headers(Map.of("Content-Type", "application/json"))
            .body(LambdaDsl.newJsonBody(body -> body
                .stringType("orderId", "ORDER-123")
                .numberType("amount", 29.99)
                .stringType("currency", "USD")
                .stringType("paymentMethod", "CREDIT_CARD")
            ).build())
            .willRespondWith()
            .status(200)
            .headers(Map.of("Content-Type", "application/json"))
            .body(LambdaDsl.newJsonBody(body -> body
                .stringType("transactionId", "TXN-456")
                .stringType("status", "SUCCESS")
                .numberType("amount", 29.99)
            ).build())
            .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "createValidPaymentPact")
    void shouldProcessPaymentSuccessfully(MockServer mockServer) {
        // Given
        var paymentClient = new PaymentClient(mockServer.getUrl());
        var paymentRequest = new PaymentRequest("ORDER-123",
            BigDecimal.valueOf(29.99), "USD", "CREDIT_CARD");

        // When
        var result = paymentClient.processPayment(paymentRequest);

        // Then
        assertThat(result.getTransactionId()).isEqualTo("TXN-456");
        assertThat(result.getStatus()).isEqualTo("SUCCESS");
        assertThat(result.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(29.99));
    }
}

// 4. Property-based testing
class OrderValidationPropertyTest {

    @Property
    @DisplayName("Order total should always equal sum of item totals")
    void orderTotalShouldEqualSumOfItems(
        @ForAll("validOrderItems") List<OrderItem> items) {

        // Given
        var order = Order.builder()
            .orderId("ORDER-123")
            .customerId("CUST-456")
            .items(items)
            .build();

        // When
        var calculatedTotal = order.calculateTotal();
        var expectedTotal = items.stream()
            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Then
        assertThat(calculatedTotal).isEqualByComparingTo(expectedTotal);
    }

    @Provide
    Arbitrary<List<OrderItem>> validOrderItems() {
        return Arbitraries.of(
            new OrderItem("PROD-1", 1, BigDecimal.valueOf(10.00)),
            new OrderItem("PROD-2", 2, BigDecimal.valueOf(15.50)),
            new OrderItem("PROD-3", 3, BigDecimal.valueOf(5.99))
        ).list().ofMinSize(1).ofMaxSize(5);
    }

    @Property
    @DisplayName("Customer ID should never be null or empty after validation")
    void customerIdShouldNeverBeNullOrEmpty(
        @ForAll @StringLength(min = 1, max = 50) @AlphaChars String customerId) {

        // Given
        var request = OrderRequest.builder()
            .customerId(customerId)
            .addItem("PROD-123", 1, BigDecimal.valueOf(10.00))
            .build();

        // When
        var validator = new OrderRequestValidator();
        var result = validator.validate(request);

        // Then
        assertThat(result.getCustomerId()).isNotNull();
        assertThat(result.getCustomerId()).isNotEmpty();
        assertThat(result.getCustomerId().trim()).isEqualTo(result.getCustomerId());
    }
}

// 5. Performance testing
@ExtendWith(SpringExtension.class)
class OrderServicePerformanceTest {

    @Autowired
    private OrderService orderService;

    @Test
    @DisplayName("Should handle high load of order creation")
    void shouldHandleHighLoadOrderCreation() {
        var iterations = 1000;
        var requests = generateOrderRequests(iterations);

        var startTime = System.currentTimeMillis();

        var results = requests.parallelStream()
            .map(orderService::createOrder)
            .collect(Collectors.toList());

        var endTime = System.currentTimeMillis();
        var duration = endTime - startTime;

        // Assertions
        assertThat(results).hasSize(iterations);
        assertThat(duration).isLessThan(5000); // Should complete within 5 seconds

        var avgResponseTime = (double) duration / iterations;
        assertThat(avgResponseTime).isLessThan(50.0); // Average < 50ms per order
    }

    @RepeatedTest(10)
    @DisplayName("Should maintain consistent performance across multiple runs")
    void shouldMaintainConsistentPerformance() {
        var request = OrderRequest.builder()
            .customerId("CUST-PERF")
            .addItem("PROD-PERF", 1, BigDecimal.valueOf(10.00))
            .build();

        var startTime = System.nanoTime();
        var order = orderService.createOrder(request);
        var endTime = System.nanoTime();

        var durationMs = (endTime - startTime) / 1_000_000.0;

        assertThat(order).isNotNull();
        assertThat(durationMs).isLessThan(100.0); // Should complete within 100ms
    }

    private List<OrderRequest> generateOrderRequests(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> OrderRequest.builder()
                .customerId("CUST-" + i)
                .addItem("PROD-" + (i % 10), 1, BigDecimal.valueOf(10.00))
                .build())
            .collect(Collectors.toList());
    }
}

// Supporting test classes
public class PaymentClient {
    private final String baseUrl;

    public PaymentClient(String baseUrl) { this.baseUrl = baseUrl; }

    public PaymentResult processPayment(PaymentRequest request) {
        // HTTP client implementation
        return new PaymentResult("TXN-456", "SUCCESS", request.amount());
    }
}

public record PaymentRequest(String orderId, BigDecimal amount, String currency, String paymentMethod) {}
public record PaymentResult(String transactionId, String status, BigDecimal amount) {
    public static PaymentResult success() { return new PaymentResult("TXN-123", "SUCCESS", BigDecimal.TEN); }
}

public class OrderRequestValidator {
    public OrderRequest validate(OrderRequest request) {
        // Validation logic
        return request;
    }
}
```

---

## ❓ **Interview Questions**

### **Q1: What are the key principles of good API design?**

**Answer:**
```java
// 1. Consistency - Similar operations should work similarly
public interface UserService {
    User createUser(CreateUserRequest request);    // Consistent naming
    User updateUser(UpdateUserRequest request);    // Consistent parameters
    void deleteUser(String userId);               // Consistent ID pattern
}

// 2. Simplicity - Easy to understand and use
public class EmailService {
    public void sendEmail(String to, String subject, String body) {
        // Simple, focused method
    }

    // Better than complex method with many parameters
    // public void sendComplexEmail(String to, String from, String subject,
    //     String body, boolean isHtml, Priority priority, List<Attachment> attachments...)
}

// 3. Predictability - Methods should behave as expected
public class BankAccount {
    public void withdraw(BigDecimal amount) {
        if (amount.compareTo(balance) > 0) {
            throw new InsufficientFundsException(); // Predictable failure
        }
        balance = balance.subtract(amount);
    }
}

// 4. Error handling - Clear and helpful error messages
public class ValidationException extends Exception {
    private final List<String> errors;

    public ValidationException(List<String> errors) {
        super("Validation failed: " + String.join(", ", errors));
        this.errors = List.copyOf(errors);
    }
}
```

### **Q2: How do you design for backward compatibility?**

**Answer:**
```java
// 1. Use builder pattern for new parameters
public class ApiRequest {
    public static class Builder {
        // Existing fields
        private String id;
        private String name;

        // New field added in v2.0 - backward compatible
        private String description = "";  // Default value

        public Builder id(String id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder description(String description) { this.description = description; return this; }
    }
}

// 2. Method overloading for new parameters
public class OrderService {
    // Original method - still works
    public Order createOrder(String customerId, List<Item> items) {
        return createOrder(customerId, items, PaymentMethod.DEFAULT);
    }

    // New method with additional parameter
    public Order createOrder(String customerId, List<Item> items, PaymentMethod paymentMethod) {
        // Implementation
        return null;
    }
}

// 3. Interface evolution with default methods
public interface PaymentProcessor {
    // Original method
    PaymentResult process(Payment payment);

    // Added in v2.0 - backward compatible
    default boolean supportsRecurring() {
        return false;  // Safe default
    }
}
```

### **Q3: What's the difference between checked and unchecked exceptions in API design?**

**Answer:**
```java
// Checked exceptions - for recoverable errors
public class OrderService {
    public Order processOrder(OrderRequest request) throws ValidationException {
        // Client must handle or declare
        if (!isValid(request)) {
            throw new ValidationException("Invalid request");
        }
        return new Order();
    }
}

// Unchecked exceptions - for programming errors
public class Calculator {
    public double divide(double a, double b) {
        if (b == 0) {
            throw new IllegalArgumentException("Division by zero"); // RuntimeException
        }
        return a / b;
    }
}

// Guidelines:
// - Use checked exceptions for expected, recoverable conditions
// - Use unchecked exceptions for programming errors
// - Don't force clients to catch exceptions they can't meaningfully handle
```

### **Q4: How do you implement thread-safe APIs?**

**Answer:**
```java
// 1. Immutable objects - inherently thread-safe
public final class CustomerRecord {
    private final String id;
    private final String name;

    public CustomerRecord(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public CustomerRecord withName(String newName) {
        return new CustomerRecord(id, newName);
    }
}

// 2. Synchronized methods for mutable state
public class CounterService {
    private int count = 0;

    public synchronized int increment() {
        return ++count;
    }

    public synchronized int getCount() {
        return count;
    }
}

// 3. Concurrent collections
public class UserCache {
    private final ConcurrentHashMap<String, User> cache = new ConcurrentHashMap<>();

    public void putUser(String id, User user) {
        cache.put(id, user);  // Thread-safe
    }

    public User getUser(String id) {
        return cache.get(id);  // Thread-safe
    }
}

// 4. Atomic operations
public class Statistics {
    private final AtomicLong requestCount = new AtomicLong(0);

    public void recordRequest() {
        requestCount.incrementAndGet();  // Thread-safe atomic operation
    }
}
```

### **Q5: How do you design fluent APIs effectively?**

**Answer:**
```java
// 1. Return 'this' for method chaining
public class HttpRequestBuilder {
    private String url;
    private Map<String, String> headers = new HashMap<>();

    public HttpRequestBuilder url(String url) {
        this.url = url;
        return this;  // Enable chaining
    }

    public HttpRequestBuilder header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public HttpRequest build() {
        return new HttpRequest(url, headers);
    }
}

// 2. Use step builders for required sequences
public interface UrlStep {
    HeaderStep url(String url);
}

public interface HeaderStep {
    HeaderStep header(String name, String value);
    BuildStep build();
}

public interface BuildStep {
    HttpRequest build();
}

// 3. DSL-style APIs
var request = HttpRequestBuilder.create()
    .url("https://api.example.com")
    .header("Authorization", "Bearer token")
    .header("Content-Type", "application/json")
    .build();
```

### **Q6: What are common API design anti-patterns?**

**Answer:**
```java
// Anti-pattern 1: Returning null instead of Optional
// Bad
public User findUser(String id) {
    return null;  // Forces null checks
}

// Good
public Optional<User> findUser(String id) {
    return Optional.empty();  // Explicit absence
}

// Anti-pattern 2: Inconsistent naming
// Bad
public class UserService {
    public User createUser(String name) { return null; }
    public User getUserById(String id) { return null; }  // Inconsistent prefix
    public void removeUser(String id) { }               // Inconsistent verb
}

// Good
public class UserService {
    public User createUser(String name) { return null; }
    public User findUser(String id) { return null; }    // Consistent naming
    public void deleteUser(String id) { }               // Consistent verbs
}

// Anti-pattern 3: Exposing implementation details
// Bad
public List<User> getUsers() {
    return internalUserList;  // Exposes mutable internal state
}

// Good
public List<User> getUsers() {
    return List.copyOf(internalUserList);  // Defensive copy
}

// Anti-pattern 4: Boolean parameters
// Bad
public void processOrder(Order order, boolean expedited, boolean gift) {
    // What do these booleans mean?
}

// Good
public void processOrder(Order order, ProcessingOptions options) {
    // Clear intent
}

public enum ProcessingType { STANDARD, EXPEDITED }
public record ProcessingOptions(ProcessingType type, boolean isGift) {}
```

### **Q7: How do you handle versioning in API design?**

**Answer:**
```java
// 1. Semantic versioning in package names
package com.company.api.v1;
public class UserService {
    // Version 1 implementation
}

package com.company.api.v2;
public class UserService {
    // Version 2 implementation
}

// 2. Interface evolution
public interface PaymentProcessor {
    // v1.0
    PaymentResult process(Payment payment);

    // v1.1 - added with default implementation
    default boolean supportsRefunds() {
        return false;
    }

    // v2.0 - will become abstract
    default CompletableFuture<PaymentResult> processAsync(Payment payment) {
        return CompletableFuture.supplyAsync(() -> process(payment));
    }
}

// 3. Deprecation strategy
public class LegacyService {
    @Deprecated(since = "2.0", forRemoval = true)
    public void oldMethod() {
        // Delegate to new method
        newMethod();
    }

    public void newMethod() {
        // New implementation
    }
}

// 4. Configuration-based versioning
public class ApiConfiguration {
    public enum Version { V1, V2 }

    private final Version version;

    public boolean supportsFeature(String feature) {
        return switch (version) {
            case V1 -> Set.of("basic").contains(feature);
            case V2 -> Set.of("basic", "advanced").contains(feature);
        };
    }
}
```

---

## 🎯 **Summary**

### **Key API Design Principles**

1. **Consistency** - Similar operations should work similarly
2. **Simplicity** - Easy to understand and use
3. **Predictability** - Behavior matches expectations
4. **Composability** - Components work well together
5. **Extensibility** - Can evolve without breaking changes

### **Best Practices Checklist**

- ✅ Use descriptive names for classes, methods, and parameters
- ✅ Fail fast with clear error messages
- ✅ Return empty collections instead of null
- ✅ Use Optional for potentially missing values
- ✅ Design for immutability when possible
- ✅ Provide builder patterns for complex objects
- ✅ Document with comprehensive JavaDoc
- ✅ Follow established conventions
- ✅ Test thoroughly with multiple strategies

### **Common Patterns**

- **Builder Pattern** - Complex object construction
- **Factory Pattern** - Object creation with flexibility
- **Strategy Pattern** - Pluggable algorithms
- **Observer Pattern** - Event notification
- **Decorator Pattern** - Behavior extension
- **Template Method** - Algorithm structure

---

*This guide covers comprehensive Java API design for technical interviews. Focus on practical examples and real-world application scenarios.*