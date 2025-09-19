# Spring Boot Reactive Programming - Comprehensive Guide

## Table of Contents
1. [Introduction to Reactive Programming](#introduction-to-reactive-programming)
2. [Spring WebFlux vs Spring MVC](#spring-webflux-vs-spring-mvc)
3. [Setting up Reactive Spring Boot](#setting-up-reactive-spring-boot)
4. [Reactive Data Types](#reactive-data-types)
5. [Reactive Controllers](#reactive-controllers)
6. [Reactive Data Access](#reactive-data-access)
7. [R2DBC Integration](#r2dbc-integration)
8. [Reactive MongoDB](#reactive-mongodb)
9. [Error Handling](#error-handling)
10. [Testing Reactive Applications](#testing-reactive-applications)
11. [Performance and Backpressure](#performance-and-backpressure)
12. [Reactive Security](#reactive-security)
13. [Streaming and Server-Sent Events](#streaming-and-server-sent-events)
14. [WebClient for Reactive HTTP](#webclient-for-reactive-http)
15. [Banking Domain Examples](#banking-domain-examples)
16. [Best Practices](#best-practices)
17. [Common Pitfalls](#common-pitfalls)
18. [Interview Questions](#interview-questions)

## Introduction to Reactive Programming

Reactive programming is a programming paradigm oriented around data flows and the propagation of change. In Spring Boot, reactive programming is implemented using Project Reactor.

### Key Concepts

**Reactive Streams**
- Publisher: Emits data
- Subscriber: Consumes data
- Subscription: Connection between Publisher and Subscriber
- Processor: Transforms data between Publisher and Subscriber

**Benefits**
- Non-blocking I/O
- Better resource utilization
- Scalability for high-throughput applications
- Backpressure handling

[⬆️ Back to Top](#table-of-contents)

## Spring WebFlux vs Spring MVC

### Architecture Comparison

```java
// Traditional Spring MVC - Blocking
@RestController
public class TraditionalController {

    @GetMapping("/accounts/{id}")
    public Account getAccount(@PathVariable Long id) {
        // Blocking database call
        return accountService.findById(id);
    }

    @PostMapping("/accounts")
    public Account createAccount(@RequestBody Account account) {
        // Blocking save operation
        return accountService.save(account);
    }
}

// Spring WebFlux - Non-blocking
@RestController
public class ReactiveController {

    @GetMapping("/accounts/{id}")
    public Mono<Account> getAccount(@PathVariable Long id) {
        // Non-blocking reactive call
        return accountService.findById(id);
    }

    @PostMapping("/accounts")
    public Mono<Account> createAccount(@RequestBody Mono<Account> account) {
        // Non-blocking reactive save
        return account.flatMap(accountService::save);
    }
}
```

### When to Use WebFlux

**Use WebFlux When:**
- High concurrency requirements
- I/O intensive operations
- Streaming data
- Microservices with many external calls
- Need for backpressure handling

**Use Spring MVC When:**
- Traditional blocking dependencies
- Simple CRUD applications
- Team unfamiliarity with reactive programming
- Debugging simplicity is priority

[⬆️ Back to Top](#table-of-contents)

## Setting up Reactive Spring Boot

### Dependencies

```xml
<dependencies>
    <!-- Spring Boot WebFlux Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>

    <!-- Reactive Data Access -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-r2dbc</artifactId>
    </dependency>

    <!-- H2 R2DBC Driver -->
    <dependency>
        <groupId>io.r2dbc</groupId>
        <artifactId>r2dbc-h2</artifactId>
    </dependency>

    <!-- Reactive MongoDB -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
    </dependency>

    <!-- Testing -->
    <dependency>
        <groupId>io.projectreactor</groupId>
        <artifactId>reactor-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

### Configuration

```java
@SpringBootApplication
@EnableR2dbcRepositories
public class ReactiveBankingApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReactiveBankingApplication.class, args);
    }

    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
            .option(DRIVER, "h2")
            .option(PROTOCOL, "mem")
            .option(DATABASE, "testdb")
            .option(USER, "sa")
            .option(PASSWORD, "")
            .build());
    }

    @Bean
    public R2dbcTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Reactive Data Types

### Mono and Flux

```java
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public class ReactiveDataTypesExample {

    // Mono - 0 or 1 element
    public Mono<Account> findAccountById(Long id) {
        return Mono.fromCallable(() -> {
            // Simulate database call
            return new Account(id, "123456", BigDecimal.valueOf(1000.00));
        });
    }

    // Flux - 0 to N elements
    public Flux<Account> findAllAccounts() {
        return Flux.fromIterable(List.of(
            new Account(1L, "123456", BigDecimal.valueOf(1000.00)),
            new Account(2L, "789012", BigDecimal.valueOf(2500.00)),
            new Account(3L, "345678", BigDecimal.valueOf(750.00))
        ));
    }

    // Creating reactive streams
    public void createReactiveStreams() {
        // Empty Mono
        Mono<String> emptyMono = Mono.empty();

        // Mono with value
        Mono<String> monoWithValue = Mono.just("Hello");

        // Mono from supplier
        Mono<String> monoFromSupplier = Mono.fromSupplier(() -> "Generated");

        // Empty Flux
        Flux<String> emptyFlux = Flux.empty();

        // Flux with values
        Flux<String> fluxWithValues = Flux.just("A", "B", "C");

        // Flux from iterable
        Flux<Integer> fluxFromRange = Flux.range(1, 5);

        // Flux from interval
        Flux<Long> fluxFromInterval = Flux.interval(Duration.ofSeconds(1));
    }
}
```

### Operators

```java
public class ReactiveOperatorsExample {

    public void transformationOperators() {
        // Map - Transform each element
        Flux<String> accountNumbers = Flux.just("123456", "789012", "345678")
            .map(String::toUpperCase);

        // FlatMap - Transform and flatten
        Flux<Transaction> transactions = Flux.just(1L, 2L, 3L)
            .flatMap(this::getTransactionsByAccountId);

        // Filter - Keep elements matching predicate
        Flux<Account> highValueAccounts = findAllAccounts()
            .filter(account -> account.getBalance().compareTo(BigDecimal.valueOf(1000)) > 0);

        // Take - Limit number of elements
        Flux<Account> firstTwoAccounts = findAllAccounts().take(2);

        // Skip - Skip first N elements
        Flux<Account> skipFirstAccount = findAllAccounts().skip(1);
    }

    public void combiningOperators() {
        Flux<Account> savingsAccounts = getSavingsAccounts();
        Flux<Account> checkingAccounts = getCheckingAccounts();

        // Merge - Combine streams as they emit
        Flux<Account> allAccounts = Flux.merge(savingsAccounts, checkingAccounts);

        // Concat - Combine streams sequentially
        Flux<Account> concatenatedAccounts = Flux.concat(savingsAccounts, checkingAccounts);

        // Zip - Combine corresponding elements
        Flux<String> accountSummary = Flux.zip(
            findAllAccounts(),
            getAccountTypes(),
            (account, type) -> account.getAccountNumber() + " - " + type
        );
    }

    public void errorHandlingOperators() {
        Flux<Account> accounts = findAllAccounts()
            // Provide fallback value on error
            .onErrorReturn(new Account())

            // Resume with alternative stream on error
            .onErrorResume(throwable -> {
                log.error("Error occurred: ", throwable);
                return Flux.empty();
            })

            // Transform error
            .onErrorMap(SQLException.class, ex -> new DataAccessException("Database error", ex));
    }

    private Flux<Transaction> getTransactionsByAccountId(Long accountId) {
        return Flux.fromIterable(transactionRepository.findByAccountId(accountId));
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Reactive Controllers

### Basic Controllers

```java
@RestController
@RequestMapping("/api/v1/accounts")
@Validated
public class ReactiveAccountController {

    private final ReactiveAccountService accountService;

    public ReactiveAccountController(ReactiveAccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public Flux<AccountDto> getAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return accountService.findAll(PageRequest.of(page, size))
            .map(this::convertToDto);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<AccountDto>> getAccount(@PathVariable Long id) {
        return accountService.findById(id)
            .map(this::convertToDto)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Mono<ResponseEntity<AccountDto>> createAccount(
            @Valid @RequestBody Mono<CreateAccountRequest> request) {
        return request
            .flatMap(req -> accountService.createAccount(req))
            .map(this::convertToDto)
            .map(account -> ResponseEntity.status(HttpStatus.CREATED).body(account))
            .onErrorResume(ConstraintViolationException.class,
                ex -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<AccountDto>> updateAccount(
            @PathVariable Long id,
            @Valid @RequestBody Mono<UpdateAccountRequest> request) {
        return request
            .flatMap(req -> accountService.updateAccount(id, req))
            .map(this::convertToDto)
            .map(ResponseEntity::ok)
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteAccount(@PathVariable Long id) {
        return accountService.deleteById(id)
            .map(deleted -> deleted ?
                ResponseEntity.noContent().<Void>build() :
                ResponseEntity.notFound().<Void>build());
    }
}
```

### Streaming Endpoints

```java
@RestController
@RequestMapping("/api/v1/transactions")
public class ReactiveTransactionController {

    private final ReactiveTransactionService transactionService;

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<TransactionDto> streamTransactions() {
        return transactionService.streamAllTransactions()
            .map(this::convertToDto);
    }

    @GetMapping(value = "/account/{accountId}/stream",
                produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<TransactionDto> streamAccountTransactions(@PathVariable Long accountId) {
        return transactionService.streamTransactionsByAccount(accountId)
            .map(this::convertToDto)
            .delayElements(Duration.ofMillis(100)); // Throttle for demo
    }

    @PostMapping("/bulk")
    public Flux<TransactionDto> processBulkTransactions(
            @RequestBody Flux<CreateTransactionRequest> transactions) {
        return transactions
            .buffer(100) // Process in batches of 100
            .flatMap(batch -> transactionService.processBatch(batch))
            .map(this::convertToDto);
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Reactive Data Access

### R2DBC Repository

```java
@Entity
@Table("accounts")
public class Account {

    @Id
    private Long id;

    @Column("account_number")
    private String accountNumber;

    @Column("balance")
    private BigDecimal balance;

    @Column("account_type")
    private AccountType accountType;

    @Column("customer_id")
    private Long customerId;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    // Constructors, getters, setters
}

public interface ReactiveAccountRepository extends R2dbcRepository<Account, Long> {

    Flux<Account> findByCustomerId(Long customerId);

    Flux<Account> findByAccountType(AccountType accountType);

    Flux<Account> findByBalanceGreaterThan(BigDecimal balance);

    @Query("SELECT * FROM accounts WHERE balance BETWEEN :minBalance AND :maxBalance")
    Flux<Account> findByBalanceRange(BigDecimal minBalance, BigDecimal maxBalance);

    @Query("SELECT * FROM accounts WHERE customer_id = :customerId AND account_type = :type")
    Flux<Account> findByCustomerIdAndAccountType(Long customerId, AccountType type);

    @Modifying
    @Query("UPDATE accounts SET balance = balance + :amount WHERE id = :accountId")
    Mono<Integer> updateBalance(Long accountId, BigDecimal amount);
}
```

### Service Layer

```java
@Service
@Transactional
public class ReactiveAccountService {

    private final ReactiveAccountRepository accountRepository;
    private final ReactiveCustomerRepository customerRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ReactiveAccountService(ReactiveAccountRepository accountRepository,
                                ReactiveCustomerRepository customerRepository,
                                ApplicationEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    public Mono<Account> createAccount(CreateAccountRequest request) {
        return customerRepository.existsById(request.getCustomerId())
            .filter(exists -> exists)
            .switchIfEmpty(Mono.error(new CustomerNotFoundException("Customer not found")))
            .then(Mono.fromCallable(() -> buildAccount(request)))
            .flatMap(accountRepository::save)
            .doOnNext(account -> eventPublisher.publishEvent(new AccountCreatedEvent(account.getId())));
    }

    public Mono<Account> findById(Long id) {
        return accountRepository.findById(id)
            .switchIfEmpty(Mono.error(new AccountNotFoundException("Account not found: " + id)));
    }

    public Flux<Account> findAll(Pageable pageable) {
        return accountRepository.findAll()
            .skip(pageable.getOffset())
            .take(pageable.getPageSize());
    }

    public Flux<Account> findByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    public Mono<Account> updateBalance(Long accountId, BigDecimal amount) {
        return accountRepository.findById(accountId)
            .switchIfEmpty(Mono.error(new AccountNotFoundException("Account not found")))
            .flatMap(account -> {
                BigDecimal newBalance = account.getBalance().add(amount);
                if (newBalance.compareTo(BigDecimal.ZERO) < 0) {
                    return Mono.error(new InsufficientFundsException("Insufficient funds"));
                }
                account.setBalance(newBalance);
                account.setUpdatedAt(LocalDateTime.now());
                return accountRepository.save(account);
            })
            .doOnNext(account -> eventPublisher.publishEvent(
                new BalanceUpdatedEvent(account.getId(), amount)));
    }

    @Transactional
    public Mono<TransferResult> transfer(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        return Mono.zip(
                accountRepository.findById(fromAccountId),
                accountRepository.findById(toAccountId)
            )
            .switchIfEmpty(Mono.error(new AccountNotFoundException("One or both accounts not found")))
            .flatMap(tuple -> {
                Account fromAccount = tuple.getT1();
                Account toAccount = tuple.getT2();

                if (fromAccount.getBalance().compareTo(amount) < 0) {
                    return Mono.error(new InsufficientFundsException("Insufficient funds"));
                }

                fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
                toAccount.setBalance(toAccount.getBalance().add(amount));

                return Mono.zip(
                    accountRepository.save(fromAccount),
                    accountRepository.save(toAccount)
                ).map(saved -> new TransferResult(fromAccountId, toAccountId, amount));
            });
    }

    private Account buildAccount(CreateAccountRequest request) {
        Account account = new Account();
        account.setAccountNumber(generateAccountNumber());
        account.setBalance(request.getInitialBalance());
        account.setAccountType(request.getAccountType());
        account.setCustomerId(request.getCustomerId());
        account.setCreatedAt(LocalDateTime.now());
        account.setUpdatedAt(LocalDateTime.now());
        return account;
    }

    private String generateAccountNumber() {
        return "ACC" + System.currentTimeMillis();
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## R2DBC Integration

### Configuration

```java
@Configuration
@EnableR2dbcRepositories
public class R2dbcConfig extends AbstractR2dbcConfiguration {

    @Bean
    @Override
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
            .option(DRIVER, "postgresql")
            .option(HOST, "localhost")
            .option(PORT, 5432)
            .option(USER, "banking_user")
            .option(PASSWORD, "banking_password")
            .option(DATABASE, "banking_db")
            .build());
    }

    @Bean
    public R2dbcTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Override
    protected List<Object> getCustomConverters() {
        return List.of(
            new AccountTypeReadConverter(),
            new AccountTypeWriteConverter(),
            new LocalDateTimeReadConverter(),
            new LocalDateTimeWriteConverter()
        );
    }
}

// Custom Converters
@ReadingConverter
public class AccountTypeReadConverter implements Converter<String, AccountType> {
    @Override
    public AccountType convert(String source) {
        return AccountType.valueOf(source.toUpperCase());
    }
}

@WritingConverter
public class AccountTypeWriteConverter implements Converter<AccountType, String> {
    @Override
    public String convert(AccountType source) {
        return source.name().toLowerCase();
    }
}
```

### Database Migration

```sql
-- Schema initialization
CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS accounts (
    id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    account_type VARCHAR(20) NOT NULL,
    customer_id BIGINT NOT NULL REFERENCES customers(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT NOT NULL REFERENCES accounts(id),
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    description TEXT,
    reference_number VARCHAR(50) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_accounts_customer_id ON accounts(customer_id);
CREATE INDEX idx_accounts_account_type ON accounts(account_type);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);
```


[⬆️ Back to Top](#table-of-contents)
## Reactive MongoDB

### Configuration

```java
@Configuration
@EnableReactiveMongoRepositories
public class ReactiveMongoConfig extends AbstractReactiveMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "reactive_banking";
    }

    @Override
    public MongoClient reactiveMongoClient() {
        return MongoClients.create("mongodb://localhost:27017");
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate() {
        return new ReactiveMongoTemplate(reactiveMongoClient(), getDatabaseName());
    }
}
```

### Document Model

```java
@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;

    @Indexed
    private String entityType;

    @Indexed
    private String entityId;

    private String action;

    private String userId;

    private Map<String, Object> oldValues;

    private Map<String, Object> newValues;

    @Indexed
    private LocalDateTime timestamp;

    private String ipAddress;

    private String userAgent;

    // Constructors, getters, setters
}

public interface ReactiveAuditLogRepository extends ReactiveMongoRepository<AuditLog, String> {

    Flux<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId);

    Flux<AuditLog> findByUserIdAndTimestampBetween(String userId, LocalDateTime start, LocalDateTime end);

    Flux<AuditLog> findByTimestampGreaterThanOrderByTimestampDesc(LocalDateTime timestamp);

    @Query("{ 'action': ?0, 'timestamp': { $gte: ?1, $lte: ?2 } }")
    Flux<AuditLog> findByActionAndTimestampRange(String action, LocalDateTime start, LocalDateTime end);
}
```

### Service Implementation

```java
@Service
public class ReactiveAuditService {

    private final ReactiveAuditLogRepository auditLogRepository;
    private final ReactiveMongoTemplate mongoTemplate;

    public ReactiveAuditService(ReactiveAuditLogRepository auditLogRepository,
                              ReactiveMongoTemplate mongoTemplate) {
        this.auditLogRepository = auditLogRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public Mono<AuditLog> logAction(String entityType, String entityId, String action,
                                   String userId, Map<String, Object> oldValues,
                                   Map<String, Object> newValues, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
            .entityType(entityType)
            .entityId(entityId)
            .action(action)
            .userId(userId)
            .oldValues(oldValues)
            .newValues(newValues)
            .timestamp(LocalDateTime.now())
            .ipAddress(ipAddress)
            .build();

        return auditLogRepository.save(auditLog);
    }

    public Flux<AuditLog> getEntityAuditTrail(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId)
            .sort((a1, a2) -> a2.getTimestamp().compareTo(a1.getTimestamp()));
    }

    public Flux<AuditLog> getUserActivity(String userId, LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByUserIdAndTimestampBetween(userId, start, end);
    }

    public Flux<AuditLog> searchAuditLogs(AuditSearchCriteria criteria) {
        Query query = new Query();

        if (criteria.getEntityType() != null) {
            query.addCriteria(Criteria.where("entityType").is(criteria.getEntityType()));
        }

        if (criteria.getAction() != null) {
            query.addCriteria(Criteria.where("action").is(criteria.getAction()));
        }

        if (criteria.getUserId() != null) {
            query.addCriteria(Criteria.where("userId").is(criteria.getUserId()));
        }

        if (criteria.getStartDate() != null && criteria.getEndDate() != null) {
            query.addCriteria(Criteria.where("timestamp")
                .gte(criteria.getStartDate())
                .lte(criteria.getEndDate()));
        }

        query.with(Sort.by(Sort.Direction.DESC, "timestamp"));
        query.limit(criteria.getLimit());

        return mongoTemplate.find(query, AuditLog.class);
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Error Handling

### Global Error Handler

```java
@Component
@Order(-2)
public class ReactiveGlobalExceptionHandler implements WebExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(ReactiveGlobalExceptionHandler.class);

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();

        if (ex instanceof AccountNotFoundException) {
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return writeErrorResponse(response, "Account not found", HttpStatus.NOT_FOUND);
        } else if (ex instanceof InsufficientFundsException) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return writeErrorResponse(response, ex.getMessage(), HttpStatus.BAD_REQUEST);
        } else if (ex instanceof ConstraintViolationException) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return writeErrorResponse(response, "Validation failed", HttpStatus.BAD_REQUEST);
        } else if (ex instanceof DataAccessException) {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            logger.error("Database error", ex);
            return writeErrorResponse(response, "Database error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            logger.error("Unexpected error", ex);
            return writeErrorResponse(response, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Mono<Void> writeErrorResponse(ServerHttpResponse response, String message, HttpStatus status) {
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse errorResponse = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(status.value())
            .error(status.getReasonPhrase())
            .message(message)
            .build();

        String errorJson;
        try {
            errorJson = new ObjectMapper().writeValueAsString(errorResponse);
        } catch (Exception e) {
            errorJson = "{\"error\":\"Internal server error\"}";
        }

        DataBuffer buffer = response.bufferFactory().wrap(errorJson.getBytes());
        return response.writeWith(Mono.just(buffer));
    }
}
```

### Service-Level Error Handling

```java
@Service
public class ReactivePaymentService {

    private final ReactiveAccountService accountService;
    private final ReactivePaymentRepository paymentRepository;

    public Mono<Payment> processPayment(PaymentRequest request) {
        return validatePaymentRequest(request)
            .then(accountService.findById(request.getFromAccountId()))
            .flatMap(account -> validateAccountBalance(account, request.getAmount()))
            .flatMap(account -> createPendingPayment(request))
            .flatMap(payment -> executePayment(payment))
            .onErrorResume(ValidationException.class, this::handleValidationError)
            .onErrorResume(InsufficientFundsException.class, this::handleInsufficientFunds)
            .onErrorResume(ExternalServiceException.class, this::handleExternalServiceError)
            .doOnError(throwable -> logger.error("Payment processing failed", throwable));
    }

    private Mono<PaymentRequest> validatePaymentRequest(PaymentRequest request) {
        return Mono.fromCallable(() -> {
            if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException("Payment amount must be positive");
            }
            if (request.getFromAccountId().equals(request.getToAccountId())) {
                throw new ValidationException("Cannot transfer to the same account");
            }
            return request;
        });
    }

    private Mono<Account> validateAccountBalance(Account account, BigDecimal amount) {
        return Mono.fromCallable(() -> {
            if (account.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException(
                    "Insufficient funds. Available: " + account.getBalance() +
                    ", Required: " + amount);
            }
            return account;
        });
    }

    private Mono<Payment> handleValidationError(ValidationException ex) {
        logger.warn("Payment validation failed: {}", ex.getMessage());
        return Mono.error(ex);
    }

    private Mono<Payment> handleInsufficientFunds(InsufficientFundsException ex) {
        logger.warn("Insufficient funds for payment: {}", ex.getMessage());
        return Mono.error(ex);
    }

    private Mono<Payment> handleExternalServiceError(ExternalServiceException ex) {
        logger.error("External service error during payment: {}", ex.getMessage());

        // Retry logic for external service failures
        return Mono.delay(Duration.ofSeconds(1))
            .then(Mono.error(new PaymentProcessingException("Payment temporarily unavailable")));
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Testing Reactive Applications

### Unit Testing

```java
@ExtendWith(MockitoExtension.class)
class ReactiveAccountServiceTest {

    @Mock
    private ReactiveAccountRepository accountRepository;

    @Mock
    private ReactiveCustomerRepository customerRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ReactiveAccountService accountService;

    @Test
    void createAccount_Success() {
        // Given
        CreateAccountRequest request = CreateAccountRequest.builder()
            .customerId(1L)
            .accountType(AccountType.SAVINGS)
            .initialBalance(BigDecimal.valueOf(1000))
            .build();

        Account savedAccount = Account.builder()
            .id(1L)
            .accountNumber("ACC123456")
            .balance(BigDecimal.valueOf(1000))
            .accountType(AccountType.SAVINGS)
            .customerId(1L)
            .build();

        when(customerRepository.existsById(1L)).thenReturn(Mono.just(true));
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.just(savedAccount));

        // When & Then
        StepVerifier.create(accountService.createAccount(request))
            .expectNext(savedAccount)
            .verifyComplete();

        verify(eventPublisher).publishEvent(any(AccountCreatedEvent.class));
    }

    @Test
    void createAccount_CustomerNotFound() {
        // Given
        CreateAccountRequest request = CreateAccountRequest.builder()
            .customerId(999L)
            .accountType(AccountType.SAVINGS)
            .initialBalance(BigDecimal.valueOf(1000))
            .build();

        when(customerRepository.existsById(999L)).thenReturn(Mono.just(false));

        // When & Then
        StepVerifier.create(accountService.createAccount(request))
            .expectError(CustomerNotFoundException.class)
            .verify();
    }

    @Test
    void transfer_Success() {
        // Given
        Account fromAccount = Account.builder()
            .id(1L)
            .accountNumber("ACC123456")
            .balance(BigDecimal.valueOf(1000))
            .build();

        Account toAccount = Account.builder()
            .id(2L)
            .accountNumber("ACC789012")
            .balance(BigDecimal.valueOf(500))
            .build();

        when(accountRepository.findById(1L)).thenReturn(Mono.just(fromAccount));
        when(accountRepository.findById(2L)).thenReturn(Mono.just(toAccount));
        when(accountRepository.save(any(Account.class)))
            .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        // When & Then
        StepVerifier.create(accountService.transfer(1L, 2L, BigDecimal.valueOf(200)))
            .assertNext(result -> {
                assertThat(result.getFromAccountId()).isEqualTo(1L);
                assertThat(result.getToAccountId()).isEqualTo(2L);
                assertThat(result.getAmount()).isEqualTo(BigDecimal.valueOf(200));
            })
            .verifyComplete();
    }

    @Test
    void updateBalance_InsufficientFunds() {
        // Given
        Account account = Account.builder()
            .id(1L)
            .balance(BigDecimal.valueOf(100))
            .build();

        when(accountRepository.findById(1L)).thenReturn(Mono.just(account));

        // When & Then
        StepVerifier.create(accountService.updateBalance(1L, BigDecimal.valueOf(-200)))
            .expectError(InsufficientFundsException.class)
            .verify();
    }
}
```

### Integration Testing

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.r2dbc.url=r2dbc:h2:mem:///testdb",
    "spring.r2dbc.username=sa",
    "spring.r2dbc.password="
})
class ReactiveAccountControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReactiveAccountRepository accountRepository;

    @Autowired
    private ReactiveCustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        // Clean database
        accountRepository.deleteAll().block();
        customerRepository.deleteAll().block();

        // Create test customer
        Customer customer = Customer.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .build();
        customerRepository.save(customer).block();
    }

    @Test
    void createAccount_Success() {
        CreateAccountRequest request = CreateAccountRequest.builder()
            .customerId(1L)
            .accountType(AccountType.SAVINGS)
            .initialBalance(BigDecimal.valueOf(1000))
            .build();

        webTestClient.post()
            .uri("/api/v1/accounts")
            .contentType(MediaType.APPLICATION_JSON)
            .body(Mono.just(request), CreateAccountRequest.class)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(AccountDto.class)
            .value(account -> {
                assertThat(account.getAccountType()).isEqualTo(AccountType.SAVINGS);
                assertThat(account.getBalance()).isEqualTo(BigDecimal.valueOf(1000));
                assertThat(account.getCustomerId()).isEqualTo(1L);
            });
    }

    @Test
    void getAccount_NotFound() {
        webTestClient.get()
            .uri("/api/v1/accounts/999")
            .exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void streamTransactions() {
        // Create test account with transactions
        Account account = createTestAccountWithTransactions();

        FluxExchangeResult<TransactionDto> result = webTestClient.get()
            .uri("/api/v1/transactions/account/{accountId}/stream", account.getId())
            .accept(MediaType.TEXT_EVENT_STREAM)
            .exchange()
            .expectStatus().isOk()
            .returnResult(TransactionDto.class);

        StepVerifier.create(result.getResponseBody())
            .expectNextCount(5) // Expecting 5 test transactions
            .thenCancel()
            .verify();
    }

    private Account createTestAccountWithTransactions() {
        // Implementation to create test data
        return null; // Placeholder
    }
}
```

### Performance Testing

```java
@Component
public class ReactivePerformanceTest {

    private final ReactiveAccountService accountService;

    @Test
    void loadTest_ConcurrentAccountCreation() {
        int numberOfAccounts = 1000;
        int concurrentRequests = 100;

        Flux<CreateAccountRequest> requests = Flux.range(1, numberOfAccounts)
            .map(i -> CreateAccountRequest.builder()
                .customerId((long) (i % 10) + 1) // 10 customers
                .accountType(AccountType.SAVINGS)
                .initialBalance(BigDecimal.valueOf(1000))
                .build());

        Duration testDuration = StepVerifier.create(
                requests.flatMap(accountService::createAccount, concurrentRequests)
                    .collectList()
            )
            .expectNextMatches(accounts -> accounts.size() == numberOfAccounts)
            .verifyComplete();

        logger.info("Created {} accounts in {} with {} concurrent requests",
                   numberOfAccounts, testDuration, concurrentRequests);

        assertThat(testDuration).isLessThan(Duration.ofSeconds(30));
    }

    @Test
    void backpressureTest() {
        int numberOfEvents = 10000;

        Flux<Integer> fastProducer = Flux.range(1, numberOfEvents)
            .delayElements(Duration.ofMillis(1)); // Fast producer

        Flux<Integer> slowConsumer = fastProducer
            .onBackpressureBuffer(1000) // Buffer up to 1000 items
            .delayElements(Duration.ofMillis(10)); // Slow consumer

        StepVerifier.create(slowConsumer)
            .expectNextCount(numberOfEvents)
            .verifyComplete();
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Performance and Backpressure

### Backpressure Strategies

```java
@Service
public class ReactiveDataProcessingService {

    // Buffer - Store elements until downstream is ready
    public Flux<ProcessedData> processWithBuffer(Flux<RawData> dataStream) {
        return dataStream
            .onBackpressureBuffer(1000) // Buffer up to 1000 items
            .map(this::processData)
            .publishOn(Schedulers.parallel());
    }

    // Drop - Drop elements when downstream is overwhelmed
    public Flux<ProcessedData> processWithDrop(Flux<RawData> dataStream) {
        return dataStream
            .onBackpressureDrop(dropped -> logger.warn("Dropped item: {}", dropped))
            .map(this::processData);
    }

    // Latest - Keep only the latest element
    public Flux<ProcessedData> processWithLatest(Flux<RawData> dataStream) {
        return dataStream
            .onBackpressureLatest()
            .map(this::processData);
    }

    // Error - Fail when backpressure occurs
    public Flux<ProcessedData> processWithError(Flux<RawData> dataStream) {
        return dataStream
            .onBackpressureError()
            .map(this::processData)
            .onErrorResume(Exception.class, ex -> {
                logger.error("Backpressure error occurred", ex);
                return Flux.empty();
            });
    }

    private ProcessedData processData(RawData data) {
        // Simulate processing
        return new ProcessedData(data.getId(), data.getValue().toUpperCase());
    }
}
```

### Threading and Schedulers

```java
@Service
public class ReactiveSchedulerService {

    // CPU-intensive operations
    public Flux<CalculationResult> performCalculations(Flux<CalculationRequest> requests) {
        return requests
            .publishOn(Schedulers.parallel()) // Switch to parallel scheduler
            .map(this::performHeavyCalculation)
            .subscribeOn(Schedulers.boundedElastic()); // For blocking I/O if needed
    }

    // I/O operations
    public Flux<ExternalApiResponse> callExternalServices(Flux<ApiRequest> requests) {
        return requests
            .publishOn(Schedulers.boundedElastic()) // For blocking I/O
            .flatMap(this::callExternalApi, 10) // Limit concurrency to 10
            .timeout(Duration.ofSeconds(30)); // Add timeout
    }

    // Mixed operations
    public Flux<Result> processWithMixedOperations(Flux<Input> inputs) {
        return inputs
            .subscribeOn(Schedulers.parallel()) // Initial subscription on parallel
            .map(this::cpuIntensiveTransform)
            .publishOn(Schedulers.boundedElastic()) // Switch for I/O
            .flatMap(this::ioOperation)
            .publishOn(Schedulers.parallel()) // Switch back for CPU work
            .map(this::finalTransform);
    }

    private CalculationResult performHeavyCalculation(CalculationRequest request) {
        // Simulate CPU-intensive work
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return new CalculationResult(request.getId(), request.getValue() * 2);
    }

    private Mono<ExternalApiResponse> callExternalApi(ApiRequest request) {
        // Simulate external API call
        return Mono.delay(Duration.ofMillis(200))
            .map(delay -> new ExternalApiResponse(request.getId(), "Response for " + request.getId()));
    }
}
```

### Performance Optimization

```java
@Service
public class OptimizedReactiveService {

    private final WebClient webClient;
    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    // Batch processing for efficiency
    public Flux<BatchResult> processBatches(Flux<DataItem> items) {
        return items
            .buffer(100) // Process in batches of 100
            .flatMap(this::processBatch, 5) // Max 5 concurrent batches
            .onErrorContinue((throwable, item) -> {
                logger.error("Error processing batch item: {}", item, throwable);
            });
    }

    // Caching with Redis
    public Mono<AccountSummary> getAccountSummaryWithCache(Long accountId) {
        String cacheKey = "account:summary:" + accountId;

        return redisTemplate.opsForValue().get(cacheKey)
            .cast(AccountSummary.class)
            .switchIfEmpty(
                generateAccountSummary(accountId)
                    .flatMap(summary -> redisTemplate.opsForValue()
                        .set(cacheKey, summary, Duration.ofMinutes(15))
                        .thenReturn(summary))
            );
    }

    // Connection pooling and retries
    public Mono<ExternalResponse> callExternalServiceWithResilience(String endpoint, Object request) {
        return webClient.post()
            .uri(endpoint)
            .body(Mono.just(request), Object.class)
            .retrieve()
            .bodyToMono(ExternalResponse.class)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                .filter(throwable -> throwable instanceof ConnectTimeoutException))
            .timeout(Duration.ofSeconds(10))
            .onErrorResume(TimeoutException.class, ex -> {
                logger.warn("External service call timed out for endpoint: {}", endpoint);
                return Mono.just(ExternalResponse.getDefaultResponse());
            });
    }

    // Memory-efficient streaming
    public Flux<ProcessedRecord> processLargeDataset(String dataSource) {
        return dataRepository.streamAllData(dataSource)
            .window(1000) // Process in windows of 1000
            .flatMap(window -> window
                .collectList()
                .flatMapMany(this::processRecordBatch)
                .subscribeOn(Schedulers.parallel())
            );
    }

    private Mono<BatchResult> processBatch(List<DataItem> batch) {
        return Mono.fromCallable(() -> {
            // Batch processing logic
            return BatchResult.builder()
                .batchSize(batch.size())
                .processedCount(batch.size())
                .timestamp(LocalDateTime.now())
                .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<AccountSummary> generateAccountSummary(Long accountId) {
        return Mono.fromCallable(() -> {
            // Generate summary logic
            return AccountSummary.builder()
                .accountId(accountId)
                .balance(BigDecimal.valueOf(1000))
                .transactionCount(50)
                .lastActivity(LocalDateTime.now())
                .build();
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Reactive Security

### WebFlux Security Configuration

```java
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class ReactiveSecurityConfig {

    private final ReactiveUserDetailsService userDetailsService;
    private final JwtAuthenticationManager jwtAuthenticationManager;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf().disable()
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/api/v1/auth/**").permitAll()
                .pathMatchers("/api/v1/public/**").permitAll()
                .pathMatchers(HttpMethod.GET, "/api/v1/accounts/**").hasRole("USER")
                .pathMatchers(HttpMethod.POST, "/api/v1/accounts/**").hasRole("USER")
                .pathMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .pathMatchers("/actuator/**").hasRole("ADMIN")
                .anyExchange().authenticated()
            )
            .authenticationManager(jwtAuthenticationManager)
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .build();
    }

    @Bean
    public ReactivePasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService() {
        return username -> userRepository.findByUsername(username)
            .map(user -> User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                    .collect(Collectors.toList()))
                .build());
    }
}
```

### JWT Authentication

```java
@Component
public class JwtAuthenticationManager implements ReactiveAuthenticationManager {

    private final JwtUtil jwtUtil;
    private final ReactiveUserDetailsService userDetailsService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();

        return Mono.fromCallable(() -> jwtUtil.extractUsername(authToken))
            .flatMap(username -> userDetailsService.findByUsername(username)
                .flatMap(userDetails -> {
                    if (jwtUtil.validateToken(authToken, userDetails.getUsername())) {
                        return Mono.just(new UsernamePasswordAuthenticationToken(
                            userDetails.getUsername(),
                            null,
                            userDetails.getAuthorities()
                        ));
                    } else {
                        return Mono.error(new BadCredentialsException("Invalid token"));
                    }
                }))
            .onErrorResume(Exception.class, ex -> Mono.error(new BadCredentialsException("Invalid token")));
    }
}

@Component
public class JwtAuthenticationWebFilter implements WebFilter {

    private final JwtAuthenticationManager authenticationManager;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        return extractToken(exchange.getRequest())
            .flatMap(token -> {
                Authentication authentication = new UsernamePasswordAuthenticationToken(token, token);
                return authenticationManager.authenticate(authentication);
            })
            .flatMap(authentication -> {
                SecurityContext context = new SecurityContextImpl(authentication);
                return chain.filter(exchange)
                    .contextWrite(ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context)));
            })
            .onErrorResume(Exception.class, ex -> {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            });
    }

    private Mono<String> extractToken(ServerHttpRequest request) {
        return Mono.fromCallable(() -> {
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                return authHeader.substring(7);
            }
            throw new IllegalArgumentException("No valid JWT token found");
        });
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/v1/auth/") || path.startsWith("/api/v1/public/");
    }
}
```

### Method-Level Security

```java
@Service
@PreAuthorize("hasRole('USER')")
public class SecureReactiveAccountService {

    private final ReactiveAccountRepository accountRepository;

    @PreAuthorize("hasRole('ADMIN') or @accountSecurityService.isAccountOwner(authentication.name, #accountId)")
    public Mono<Account> findById(Long accountId) {
        return accountRepository.findById(accountId);
    }

    @PreAuthorize("@accountSecurityService.isAccountOwner(authentication.name, #accountId)")
    public Mono<Account> updateBalance(Long accountId, BigDecimal amount) {
        return accountRepository.findById(accountId)
            .flatMap(account -> {
                account.setBalance(account.getBalance().add(amount));
                return accountRepository.save(account);
            });
    }

    @PostAuthorize("hasRole('ADMIN') or returnObject.stream().allMatch(account -> @accountSecurityService.isAccountOwner(authentication.name, account.id))")
    public Flux<Account> findByCustomerId(Long customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public Flux<Account> findAll() {
        return accountRepository.findAll();
    }
}

@Component
public class AccountSecurityService {

    private final ReactiveAccountRepository accountRepository;
    private final ReactiveCustomerRepository customerRepository;

    public Mono<Boolean> isAccountOwner(String username, Long accountId) {
        return customerRepository.findByUsername(username)
            .flatMap(customer -> accountRepository.findById(accountId)
                .map(account -> account.getCustomerId().equals(customer.getId()))
                .defaultIfEmpty(false));
    }

    public Mono<Boolean> canAccessAccount(String username, Long accountId, String operation) {
        return isAccountOwner(username, accountId)
            .flatMap(isOwner -> {
                if (isOwner) {
                    return Mono.just(true);
                }
                // Check for additional permissions based on operation
                return checkAdditionalPermissions(username, accountId, operation);
            });
    }

    private Mono<Boolean> checkAdditionalPermissions(String username, Long accountId, String operation) {
        // Implementation for checking additional permissions
        return Mono.just(false);
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Streaming and Server-Sent Events

### Server-Sent Events

```java
@RestController
@RequestMapping("/api/v1/events")
public class ReactiveEventController {

    private final ReactiveTransactionService transactionService;
    private final ApplicationEventPublisher eventPublisher;

    @GetMapping(value = "/transactions", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<TransactionDto>> streamTransactions() {
        return transactionService.streamAllTransactions()
            .map(transaction -> ServerSentEvent.<TransactionDto>builder()
                .id(String.valueOf(transaction.getId()))
                .event("transaction")
                .data(convertToDto(transaction))
                .build())
            .delayElements(Duration.ofMillis(100));
    }

    @GetMapping(value = "/account/{accountId}/balance", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<BalanceUpdateDto>> streamBalanceUpdates(@PathVariable Long accountId) {
        return balanceUpdateProcessor
            .filter(update -> update.getAccountId().equals(accountId))
            .map(update -> ServerSentEvent.<BalanceUpdateDto>builder()
                .id(String.valueOf(update.getTimestamp().toEpochMilli()))
                .event("balance-update")
                .data(convertToBalanceUpdateDto(update))
                .build());
    }

    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<NotificationDto>> streamNotifications(
            @RequestParam String userId,
            @RequestHeader(value = "Last-Event-ID", required = false) String lastEventId) {

        return notificationService.streamNotifications(userId, lastEventId)
            .map(notification -> ServerSentEvent.<NotificationDto>builder()
                .id(notification.getId())
                .event(notification.getType())
                .data(convertToNotificationDto(notification))
                .comment("Notification for user: " + userId)
                .build())
            .doOnCancel(() -> logger.info("Client disconnected from notifications stream"));
    }
}
```

### Real-time Data Processing

```java
@Component
public class ReactiveEventProcessor {

    private final Sinks.Many<TransactionEvent> transactionSink;
    private final Sinks.Many<BalanceUpdateEvent> balanceUpdateSink;

    public ReactiveEventProcessor() {
        this.transactionSink = Sinks.many().multicast().onBackpressureBuffer();
        this.balanceUpdateSink = Sinks.many().multicast().onBackpressureBuffer();
    }

    @EventListener
    public void handleTransactionEvent(TransactionEvent event) {
        transactionSink.tryEmitNext(event);
    }

    @EventListener
    public void handleBalanceUpdateEvent(BalanceUpdateEvent event) {
        balanceUpdateSink.tryEmitNext(event);
    }

    public Flux<TransactionEvent> getTransactionStream() {
        return transactionSink.asFlux();
    }

    public Flux<BalanceUpdateEvent> getBalanceUpdateStream() {
        return balanceUpdateSink.asFlux();
    }

    public Flux<AggregatedData> processRealTimeAggregation() {
        return getTransactionStream()
            .window(Duration.ofSeconds(10)) // 10-second windows
            .flatMap(window -> window
                .groupBy(TransactionEvent::getAccountId)
                .flatMap(group -> group
                    .reduce(new AggregatedData(group.key()), this::aggregateTransaction)
                    .map(aggregated -> {
                        aggregated.setTimestamp(LocalDateTime.now());
                        return aggregated;
                    })
                )
            );
    }

    private AggregatedData aggregateTransaction(AggregatedData aggregated, TransactionEvent transaction) {
        aggregated.addTransaction(transaction.getAmount());
        return aggregated;
    }
}
```

### WebSocket Integration

```java
@Component
public class ReactiveWebSocketHandler implements WebSocketHandler {

    private final ReactiveEventProcessor eventProcessor;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String userId = getUserId(session);

        Mono<Void> input = session.receive()
            .map(WebSocketMessage::getPayloadAsText)
            .flatMap(this::processClientMessage)
            .then();

        Mono<Void> output = eventProcessor.getTransactionStream()
            .filter(event -> belongsToUser(event, userId))
            .map(this::convertToWebSocketMessage)
            .map(session::textMessage)
            .as(session::send);

        return Mono.zip(input, output).then();
    }

    private Mono<Void> processClientMessage(String message) {
        return Mono.fromCallable(() -> {
            // Process incoming WebSocket messages
            logger.info("Received message: {}", message);
            return null;
        }).then();
    }

    private String convertToWebSocketMessage(TransactionEvent event) {
        try {
            return objectMapper.writeValueAsString(Map.of(
                "type", "transaction",
                "data", event
            ));
        } catch (Exception e) {
            logger.error("Error converting event to JSON", e);
            return "{}";
        }
    }

    private String getUserId(WebSocketSession session) {
        return session.getHandshakeInfo().getHeaders().getFirst("User-ID");
    }

    private boolean belongsToUser(TransactionEvent event, String userId) {
        // Logic to determine if event belongs to user
        return true; // Simplified
    }
}

@Configuration
@EnableWebFluxSecurity
public class WebSocketConfig {

    @Bean
    public HandlerMapping handlerMapping(ReactiveWebSocketHandler webSocketHandler) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws/transactions", webSocketHandler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(map);
        mapping.setOrder(-1);
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## WebClient for Reactive HTTP

### Configuration

```java
@Configuration
public class WebClientConfig {

    @Bean
    @Primary
    public WebClient defaultWebClient() {
        return WebClient.builder()
            .baseUrl("https://api.external-bank.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, "Reactive-Banking-App/1.0")
            .codecs(configurer -> configurer
                .defaultCodecs()
                .maxInMemorySize(1024 * 1024)) // 1MB buffer
            .build();
    }

    @Bean
    @Qualifier("externalPaymentClient")
    public WebClient externalPaymentWebClient() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
            .responseTimeout(Duration.ofSeconds(30))
            .doOnConnected(conn ->
                conn.addHandlerLast(new ReadTimeoutHandler(30))
                    .addHandlerLast(new WriteTimeoutHandler(30)));

        return WebClient.builder()
            .baseUrl("https://payments.external-service.com")
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
                logger.info("Request: {} {}", clientRequest.method(), clientRequest.url());
                return Mono.just(clientRequest);
            }))
            .filter(ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
                logger.info("Response: {}", clientResponse.statusCode());
                return Mono.just(clientResponse);
            }))
            .build();
    }
}
```

### Service Implementation

```java
@Service
public class ReactiveExternalPaymentService {

    private final WebClient externalPaymentClient;
    private final ReactiveCircuitBreaker circuitBreaker;

    public ReactiveExternalPaymentService(@Qualifier("externalPaymentClient") WebClient externalPaymentClient,
                                        ReactiveCircuitBreakerFactory circuitBreakerFactory) {
        this.externalPaymentClient = externalPaymentClient;
        this.circuitBreaker = circuitBreakerFactory.create("external-payment");
    }

    public Mono<PaymentResponse> processExternalPayment(ExternalPaymentRequest request) {
        return circuitBreaker.run(
            externalPaymentClient.post()
                .uri("/payments")
                .header("Authorization", "Bearer " + getAuthToken())
                .body(Mono.just(request), ExternalPaymentRequest.class)
                .retrieve()
                .onStatus(HttpStatus::isError, response -> {
                    return response.bodyToMono(String.class)
                        .flatMap(errorBody -> {
                            logger.error("External payment failed: {} - {}", response.statusCode(), errorBody);
                            return Mono.error(new ExternalPaymentException(
                                "Payment failed with status: " + response.statusCode()));
                        });
                })
                .bodyToMono(PaymentResponse.class)
                .timeout(Duration.ofSeconds(30))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                    .filter(throwable -> !(throwable instanceof ExternalPaymentException))),
            throwable -> {
                logger.error("Circuit breaker fallback triggered", throwable);
                return getFallbackPaymentResponse(request);
            });
    }

    public Flux<ExchangeRate> getExchangeRates() {
        return externalPaymentClient.get()
            .uri("/exchange-rates")
            .retrieve()
            .bodyToFlux(ExchangeRate.class)
            .onErrorResume(WebClientException.class, ex -> {
                logger.warn("Failed to fetch exchange rates from external service", ex);
                return getDefaultExchangeRates();
            });
    }

    public Mono<AccountValidationResponse> validateExternalAccount(String accountNumber) {
        return externalPaymentClient.post()
            .uri("/accounts/validate")
            .body(Mono.just(Map.of("accountNumber", accountNumber)), Map.class)
            .retrieve()
            .bodyToMono(AccountValidationResponse.class)
            .onErrorReturn(new AccountValidationResponse(false, "External validation unavailable"));
    }

    public Flux<TransactionStatus> streamPaymentUpdates(String paymentId) {
        return externalPaymentClient.get()
            .uri("/payments/{paymentId}/status-stream", paymentId)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .retrieve()
            .bodyToFlux(TransactionStatus.class)
            .takeUntil(status -> status.isFinal())
            .doOnComplete(() -> logger.info("Payment status stream completed for: {}", paymentId));
    }

    private Mono<PaymentResponse> getFallbackPaymentResponse(ExternalPaymentRequest request) {
        return Mono.just(PaymentResponse.builder()
            .paymentId("FALLBACK_" + UUID.randomUUID().toString())
            .status("PENDING")
            .message("Payment queued for retry")
            .build());
    }

    private Flux<ExchangeRate> getDefaultExchangeRates() {
        return Flux.just(
            new ExchangeRate("USD", "EUR", BigDecimal.valueOf(0.85)),
            new ExchangeRate("USD", "GBP", BigDecimal.valueOf(0.75))
        );
    }

    private String getAuthToken() {
        // Implementation to get auth token
        return "mock-token";
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Banking Domain Examples

### Complete Banking Application

```java
// Account Management
@RestController
@RequestMapping("/api/v1/banking")
public class CompleteBankingController {

    private final ReactiveAccountService accountService;
    private final ReactiveTransactionService transactionService;
    private final ReactiveCustomerService customerService;

    // Account operations
    @PostMapping("/customers/{customerId}/accounts")
    public Mono<ResponseEntity<AccountDto>> openAccount(
            @PathVariable Long customerId,
            @Valid @RequestBody OpenAccountRequest request) {

        return customerService.validateCustomer(customerId)
            .then(accountService.openAccount(customerId, request))
            .map(account -> ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(account)))
            .onErrorResume(CustomerNotFoundException.class,
                ex -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @PostMapping("/accounts/{accountId}/close")
    public Mono<ResponseEntity<Void>> closeAccount(@PathVariable Long accountId) {
        return accountService.closeAccount(accountId)
            .map(closed -> closed ?
                ResponseEntity.noContent().<Void>build() :
                ResponseEntity.badRequest().<Void>build());
    }

    // Transaction operations
    @PostMapping("/transactions/transfer")
    public Mono<ResponseEntity<TransferResultDto>> transfer(
            @Valid @RequestBody TransferRequest request) {

        return transactionService.transfer(
                request.getFromAccountId(),
                request.getToAccountId(),
                request.getAmount(),
                request.getDescription())
            .map(result -> ResponseEntity.ok(convertToDto(result)))
            .onErrorResume(InsufficientFundsException.class,
                ex -> Mono.just(ResponseEntity.badRequest().build()));
    }

    @PostMapping("/transactions/deposit")
    public Mono<ResponseEntity<TransactionDto>> deposit(
            @Valid @RequestBody DepositRequest request) {

        return transactionService.deposit(
                request.getAccountId(),
                request.getAmount(),
                request.getDescription())
            .map(transaction -> ResponseEntity.status(HttpStatus.CREATED).body(convertToDto(transaction)));
    }

    @PostMapping("/transactions/withdraw")
    public Mono<ResponseEntity<TransactionDto>> withdraw(
            @Valid @RequestBody WithdrawRequest request) {

        return transactionService.withdraw(
                request.getAccountId(),
                request.getAmount(),
                request.getDescription())
            .map(transaction -> ResponseEntity.ok(convertToDto(transaction)))
            .onErrorResume(InsufficientFundsException.class,
                ex -> Mono.just(ResponseEntity.badRequest().build()));
    }

    // Statement and reporting
    @GetMapping("/accounts/{accountId}/statements")
    public Flux<TransactionDto> getAccountStatement(
            @PathVariable Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {

        return transactionService.getAccountStatement(accountId, fromDate, toDate)
            .map(this::convertToDto);
    }

    @GetMapping("/customers/{customerId}/portfolio")
    public Mono<CustomerPortfolioDto> getCustomerPortfolio(@PathVariable Long customerId) {
        return customerService.generatePortfolio(customerId)
            .map(this::convertToDto);
    }
}

// Core Services
@Service
@Transactional
public class CompleteBankingService {

    private final ReactiveAccountRepository accountRepository;
    private final ReactiveTransactionRepository transactionRepository;
    private final ReactiveCustomerRepository customerRepository;

    public Mono<Account> openAccount(Long customerId, OpenAccountRequest request) {
        return customerRepository.findById(customerId)
            .switchIfEmpty(Mono.error(new CustomerNotFoundException("Customer not found")))
            .flatMap(customer -> validateAccountOpening(customer, request))
            .flatMap(customer -> createAccount(customer, request))
            .flatMap(accountRepository::save)
            .doOnNext(account -> publishAccountOpenedEvent(account));
    }

    public Mono<TransferResult> performTransfer(Long fromAccountId, Long toAccountId,
                                              BigDecimal amount, String description) {
        return Mono.zip(
                accountRepository.findById(fromAccountId),
                accountRepository.findById(toAccountId))
            .switchIfEmpty(Mono.error(new AccountNotFoundException("One or both accounts not found")))
            .flatMap(tuple -> validateTransfer(tuple.getT1(), tuple.getT2(), amount))
            .flatMap(tuple -> executeTransfer(tuple.getT1(), tuple.getT2(), amount, description))
            .doOnNext(result -> publishTransferCompletedEvent(result));
    }

    public Flux<MonthlyStatement> generateMonthlyStatements(int year, int month) {
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);

        return accountRepository.findAll()
            .flatMap(account -> generateAccountStatement(account, startDate, endDate))
            .buffer(100) // Process in batches
            .flatMap(statements -> Flux.fromIterable(statements));
    }

    private Mono<Customer> validateAccountOpening(Customer customer, OpenAccountRequest request) {
        return Mono.fromCallable(() -> {
            if (customer.getStatus() != CustomerStatus.ACTIVE) {
                throw new BusinessRuleException("Customer account is not active");
            }
            if (request.getInitialBalance().compareTo(BigDecimal.valueOf(100)) < 0) {
                throw new BusinessRuleException("Minimum initial balance is $100");
            }
            return customer;
        });
    }

    private Mono<Account> createAccount(Customer customer, OpenAccountRequest request) {
        return generateAccountNumber()
            .map(accountNumber -> Account.builder()
                .accountNumber(accountNumber)
                .accountType(request.getAccountType())
                .balance(request.getInitialBalance())
                .customerId(customer.getId())
                .status(AccountStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build());
    }

    private Mono<String> generateAccountNumber() {
        return Mono.fromCallable(() -> "ACC" + System.currentTimeMillis())
            .flatMap(number -> accountRepository.existsByAccountNumber(number)
                .flatMap(exists -> exists ? generateAccountNumber() : Mono.just(number)));
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Best Practices

### Design Principles

```java
// 1. Prefer composition over inheritance
@Service
public class ReactivePaymentProcessor {

    private final List<PaymentValidator> validators;
    private final List<PaymentProcessor> processors;
    private final List<PaymentNotifier> notifiers;

    public Mono<PaymentResult> processPayment(PaymentRequest request) {
        return validatePayment(request)
            .flatMap(this::executePayment)
            .flatMap(this::sendNotifications);
    }

    private Mono<PaymentRequest> validatePayment(PaymentRequest request) {
        return Flux.fromIterable(validators)
            .flatMap(validator -> validator.validate(request))
            .then(Mono.just(request));
    }
}

// 2. Use proper error handling
@Service
public class RobustReactiveService {

    public Mono<Result> processWithErrorHandling(Input input) {
        return validateInput(input)
            .flatMap(this::processInput)
            .onErrorResume(ValidationException.class, this::handleValidationError)
            .onErrorResume(DataAccessException.class, this::handleDataAccessError)
            .doOnError(throwable -> logger.error("Unexpected error", throwable))
            .onErrorReturn(Result.getDefaultResult());
    }

    private Mono<Result> handleValidationError(ValidationException ex) {
        return Mono.just(Result.validationError(ex.getMessage()));
    }

    private Mono<Result> handleDataAccessError(DataAccessException ex) {
        return Mono.just(Result.systemError("Temporary service unavailable"));
    }
}

// 3. Implement proper resource management
@Service
public class ResourceAwareService {

    private final Scheduler customScheduler;

    @PostConstruct
    public void init() {
        this.customScheduler = Schedulers.newBoundedElastic(
            50, // thread cap
            100000, // queue capacity
            "custom-scheduler");
    }

    @PreDestroy
    public void cleanup() {
        customScheduler.dispose();
    }

    public Mono<Result> processWithCustomScheduler(Input input) {
        return Mono.fromCallable(() -> heavyProcessing(input))
            .subscribeOn(customScheduler)
            .timeout(Duration.ofSeconds(30));
    }
}
```

### Testing Best Practices

```java
// Use StepVerifier for testing
@Test
void shouldHandleBackpressure() {
    Flux<Integer> source = Flux.range(1, 1000)
        .onBackpressureBuffer(100);

    StepVerifier.create(source, 0) // Start with 0 demand
        .expectSubscription()
        .thenRequest(10)
        .expectNextCount(10)
        .thenRequest(90)
        .expectNextCount(90)
        .thenCancel()
        .verify();
}

// Test error scenarios
@Test
void shouldHandleErrors() {
    Mono<String> source = Mono.error(new RuntimeException("Test error"));

    StepVerifier.create(source)
        .expectError(RuntimeException.class)
        .verify();
}

// Test timing
@Test
void shouldRespectTiming() {
    Flux<Long> source = Flux.interval(Duration.ofSeconds(1)).take(3);

    StepVerifier.withVirtualTime(() -> source)
        .expectSubscription()
        .expectNoEvent(Duration.ofSeconds(1))
        .expectNext(0L)
        .thenAwait(Duration.ofSeconds(1))
        .expectNext(1L)
        .thenAwait(Duration.ofSeconds(1))
        .expectNext(2L)
        .verifyComplete();
}
```


[⬆️ Back to Top](#table-of-contents)
## Common Pitfalls

### 1. Blocking Operations

```java
// ❌ Don't do this - blocks the thread
@Service
public class BlockingService {

    public Mono<Account> getAccount(Long id) {
        return Mono.fromCallable(() -> {
            // This blocks!
            Thread.sleep(1000);
            return accountRepository.findById(id).block(); // blocks!
        });
    }
}

// ✅ Do this instead
@Service
public class NonBlockingService {

    public Mono<Account> getAccount(Long id) {
        return accountRepository.findById(id)
            .delayElement(Duration.ofSeconds(1)); // Non-blocking delay
    }
}
```

### 2. Memory Leaks

```java
// ❌ Memory leak - unbounded buffer
public Flux<Data> processData() {
    return dataSource.getStream()
        .onBackpressureBuffer(); // Unbounded buffer!
}

// ✅ Proper backpressure handling
public Flux<Data> processData() {
    return dataSource.getStream()
        .onBackpressureBuffer(1000) // Limited buffer
        .onBackpressureDrop(); // Drop excess items
}
```

### 3. Exception Handling

```java
// ❌ Swallowing exceptions
public Mono<Result> processRequest(Request request) {
    return someOperation(request)
        .onErrorReturn(Result.empty()); // Loses error information
}

// ✅ Proper error handling
public Mono<Result> processRequest(Request request) {
    return someOperation(request)
        .onErrorResume(Exception.class, ex -> {
            logger.error("Processing failed for request: {}", request, ex);
            return Mono.just(Result.error(ex.getMessage()));
        });
}
```


[⬆️ Back to Top](#table-of-contents)
## Interview Questions

### Beginner Level

**Q1: What is reactive programming and how does it differ from imperative programming?**

A: Reactive programming is a programming paradigm oriented around data flows and the propagation of change. Key differences:

- **Imperative**: Pull-based, blocking, synchronous
- **Reactive**: Push-based, non-blocking, asynchronous

```java
// Imperative
List<Account> accounts = accountService.getAllAccounts(); // Blocking
List<Account> filtered = accounts.stream()
    .filter(account -> account.getBalance() > 1000)
    .collect(toList());

// Reactive
Flux<Account> accounts = accountService.getAllAccounts(); // Non-blocking
Flux<Account> filtered = accounts
    .filter(account -> account.getBalance() > 1000);
```

**Q2: Explain the difference between Mono and Flux.**

A:
- **Mono**: Represents 0 or 1 element
- **Flux**: Represents 0 to N elements

```java
Mono<Account> account = accountService.findById(1L);
Flux<Account> accounts = accountService.findAll();
```

**Q3: What is backpressure and why is it important?**

A: Backpressure is a mechanism to handle situations where a fast producer overwhelms a slow consumer. It's important to prevent memory issues and system crashes.

### Intermediate Level

**Q4: How do you handle errors in reactive streams?**

A: Multiple strategies:
- `onErrorReturn()`: Provide fallback value
- `onErrorResume()`: Switch to alternative stream
- `onErrorMap()`: Transform error
- `retry()`: Retry on error

**Q5: Explain the different types of Schedulers in Project Reactor.**

A:
- `Schedulers.immediate()`: Current thread
- `Schedulers.single()`: Single reusable thread
- `Schedulers.parallel()`: Fixed pool for CPU-intensive work
- `Schedulers.boundedElastic()`: Elastic pool for I/O operations

**Q6: How do you test reactive code?**

A: Use `StepVerifier` for testing reactive streams:

```java
StepVerifier.create(service.getAccount(1L))
    .expectNext(expectedAccount)
    .verifyComplete();
```

### Advanced Level

**Q7: How would you implement a reactive transaction manager?**

A: Implementation would involve:
- Managing R2DBC connections
- Handling transaction boundaries
- Implementing proper rollback mechanisms
- Supporting nested transactions

**Q8: Explain how you would implement circuit breaker pattern in reactive applications.**

A: Using Spring Cloud Circuit Breaker:

```java
@Component
public class ReactiveCircuitBreakerService {

    private final ReactiveCircuitBreaker circuitBreaker;

    public Mono<Response> callExternalService(Request request) {
        return circuitBreaker.run(
            externalService.call(request),
            throwable -> getFallbackResponse(request)
        );
    }
}
```

**Q9: How do you handle distributed transactions in a reactive microservices architecture?**

A: Approaches include:
- Saga pattern
- Event sourcing
- Eventual consistency
- Compensation patterns


[⬆️ Back to Top](#table-of-contents)
This comprehensive guide covers the essential aspects of Spring Boot Reactive Programming. The reactive paradigm offers significant benefits for high-throughput, I/O-intensive applications, especially in banking and financial services where scalability and responsiveness are crucial.