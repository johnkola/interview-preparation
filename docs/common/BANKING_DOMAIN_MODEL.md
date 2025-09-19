# Banking Domain Model Reference

## 📋 Overview
This document defines the common banking domain model used across all interview preparation guides. It provides standardized entity definitions, service patterns, and examples that maintain consistency throughout the documentation.

---

## 🏦 Core Banking Entities

### Account Entity
```java
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Column(name = "balance", precision = 15, scale = 2, nullable = false)
    private BigDecimal balance;

    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode = "USD";

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status = AccountStatus.ACTIVE;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version; // For optimistic locking

    // Constructors, getters, setters
    public Account() {}

    public Account(String accountNumber, Long customerId, AccountType accountType) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.accountType = accountType;
        this.balance = BigDecimal.ZERO;
    }

    // Business methods
    public void debit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient balance");
        }
        this.balance = this.balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        this.balance = this.balance.add(amount);
    }

    // Getters and setters...
}
```

### Transaction Entity
```java
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", unique = true, nullable = false)
    private String transactionId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_after", precision = 15, scale = 2, nullable = false)
    private BigDecimal balanceAfter;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "reference_number", length = 50)
    private String referenceNumber;

    @CreationTimestamp
    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    // Constructors, getters, setters
    public Transaction() {}

    public Transaction(String transactionId, Long accountId, TransactionType type,
                      BigDecimal amount, BigDecimal balanceAfter) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.transactionType = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
    }

    // Getters and setters...
}
```

### Customer Entity
```java
@Entity
@Table(name = "customers")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_number", unique = true, nullable = false)
    private String customerNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // One customer can have multiple accounts
    @OneToMany(mappedBy = "customerId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    // Constructors, getters, setters
    public Customer() {}

    public Customer(String customerNumber, String firstName, String lastName, String email) {
        this.customerNumber = customerNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    // Getters and setters...
}
```

---

## 🔄 Enumerations

### AccountType
```java
public enum AccountType {
    CHECKING("Checking Account"),
    SAVINGS("Savings Account"),
    MONEY_MARKET("Money Market Account"),
    CREDIT("Credit Account"),
    LOAN("Loan Account");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
```

### AccountStatus
```java
public enum AccountStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    SUSPENDED("Suspended"),
    CLOSED("Closed"),
    FROZEN("Frozen");

    private final String displayName;

    AccountStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
```

### TransactionType
```java
public enum TransactionType {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    TRANSFER_IN("Transfer In"),
    TRANSFER_OUT("Transfer Out"),
    FEE("Fee"),
    INTEREST("Interest"),
    ADJUSTMENT("Adjustment");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
```

### TransactionStatus
```java
public enum TransactionStatus {
    PENDING("Pending"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled"),
    REVERSED("Reversed");

    private final String displayName;

    TransactionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
```

---

## 💼 Service Layer Patterns

### AccountService
```java
@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionService transactionService;
    private final AuditService auditService;

    public AccountService(AccountRepository accountRepository,
                         TransactionService transactionService,
                         AuditService auditService) {
        this.accountRepository = accountRepository;
        this.transactionService = transactionService;
        this.auditService = auditService;
    }

    @Cacheable(value = "accounts", key = "#accountNumber")
    public Account findByAccountNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
    }

    @CacheEvict(value = "accounts", key = "#account.accountNumber")
    public Account updateAccount(Account account) {
        Account saved = accountRepository.save(account);
        auditService.logAccountUpdate(saved);
        return saved;
    }

    public Account createAccount(String customerNumber, AccountType accountType) {
        String accountNumber = generateAccountNumber();
        Account account = new Account(accountNumber, getCustomerId(customerNumber), accountType);
        Account saved = accountRepository.save(account);
        auditService.logAccountCreation(saved);
        return saved;
    }

    @Retryable(
        value = {OptimisticLockingFailureException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void transferFunds(String fromAccountNumber, String toAccountNumber, BigDecimal amount) {
        Account fromAccount = findByAccountNumber(fromAccountNumber);
        Account toAccount = findByAccountNumber(toAccountNumber);

        // Validate transfer
        validateTransfer(fromAccount, toAccount, amount);

        // Perform transfer
        fromAccount.debit(amount);
        toAccount.credit(amount);

        // Save both accounts
        accountRepository.saveAll(Arrays.asList(fromAccount, toAccount));

        // Record transactions
        transactionService.recordTransfer(fromAccount, toAccount, amount);

        // Audit logging
        auditService.logTransfer(fromAccount.getAccountNumber(),
                                toAccount.getAccountNumber(), amount);
    }

    private void validateTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException("Source account is not active");
        }
        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountInactiveException("Destination account is not active");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be positive");
        }
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }
    }

    private String generateAccountNumber() {
        // Implementation for generating unique account numbers
        return "TD" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    private Long getCustomerId(String customerNumber) {
        // Implementation to get customer ID from customer number
        return 1L; // Simplified for example
    }
}
```

### TransactionService
```java
@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AuditService auditService;

    public TransactionService(TransactionRepository transactionRepository, AuditService auditService) {
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
    }

    public Transaction recordDeposit(Account account, BigDecimal amount, String description) {
        String transactionId = generateTransactionId();
        BigDecimal balanceAfter = account.getBalance().add(amount);

        Transaction transaction = new Transaction(
            transactionId,
            account.getId(),
            TransactionType.DEPOSIT,
            amount,
            balanceAfter
        );
        transaction.setDescription(description);
        transaction.setStatus(TransactionStatus.COMPLETED);

        Transaction saved = transactionRepository.save(transaction);
        auditService.logTransaction(saved);
        return saved;
    }

    public Transaction recordWithdrawal(Account account, BigDecimal amount, String description) {
        String transactionId = generateTransactionId();
        BigDecimal balanceAfter = account.getBalance().subtract(amount);

        Transaction transaction = new Transaction(
            transactionId,
            account.getId(),
            TransactionType.WITHDRAWAL,
            amount,
            balanceAfter
        );
        transaction.setDescription(description);
        transaction.setStatus(TransactionStatus.COMPLETED);

        Transaction saved = transactionRepository.save(transaction);
        auditService.logTransaction(saved);
        return saved;
    }

    public void recordTransfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        // Record debit transaction for source account
        recordTransaction(fromAccount, TransactionType.TRANSFER_OUT, amount,
            "Transfer to " + toAccount.getAccountNumber());

        // Record credit transaction for destination account
        recordTransaction(toAccount, TransactionType.TRANSFER_IN, amount,
            "Transfer from " + fromAccount.getAccountNumber());
    }

    private Transaction recordTransaction(Account account, TransactionType type,
                                       BigDecimal amount, String description) {
        String transactionId = generateTransactionId();

        Transaction transaction = new Transaction(
            transactionId,
            account.getId(),
            type,
            amount,
            account.getBalance()
        );
        transaction.setDescription(description);
        transaction.setStatus(TransactionStatus.COMPLETED);

        return transactionRepository.save(transaction);
    }

    @Async
    public CompletableFuture<List<Transaction>> getTransactionHistory(String accountNumber,
                                                                     LocalDateTime fromDate,
                                                                     LocalDateTime toDate) {
        List<Transaction> transactions = transactionRepository
            .findByAccountNumberAndDateRange(accountNumber, fromDate, toDate);
        return CompletableFuture.completedFuture(transactions);
    }

    private String generateTransactionId() {
        return "TXN" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(10000, 99999);
    }
}
```

---

## 📊 Repository Patterns

### AccountRepository
```java
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(Long customerId);

    List<Account> findByAccountTypeAndStatus(AccountType accountType, AccountStatus status);

    @Query("SELECT a FROM Account a WHERE a.accountNumber = :accountNumber FOR UPDATE")
    Optional<Account> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);

    @Query("SELECT a FROM Account a WHERE a.balance >= :minBalance AND a.status = :status")
    List<Account> findAccountsWithMinimumBalance(@Param("minBalance") BigDecimal minBalance,
                                               @Param("status") AccountStatus status);

    @Modifying
    @Query("UPDATE Account a SET a.status = :status WHERE a.id IN :accountIds")
    int updateAccountStatus(@Param("accountIds") List<Long> accountIds,
                           @Param("status") AccountStatus status);
}
```

### TransactionRepository
```java
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByAccountIdOrderByProcessedAtDesc(Long accountId);

    List<Transaction> findByAccountIdAndTransactionType(Long accountId, TransactionType transactionType);

    @Query("SELECT t FROM Transaction t WHERE t.accountId = " +
           "(SELECT a.id FROM Account a WHERE a.accountNumber = :accountNumber) " +
           "AND t.processedAt BETWEEN :fromDate AND :toDate " +
           "ORDER BY t.processedAt DESC")
    List<Transaction> findByAccountNumberAndDateRange(@Param("accountNumber") String accountNumber,
                                                     @Param("fromDate") LocalDateTime fromDate,
                                                     @Param("toDate") LocalDateTime toDate);

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.accountId = :accountId " +
           "AND t.transactionType = :transactionType AND t.status = 'COMPLETED'")
    BigDecimal sumAmountByAccountAndType(@Param("accountId") Long accountId,
                                        @Param("transactionType") TransactionType transactionType);

    @Query(value = "SELECT * FROM transactions WHERE account_id = ?1 " +
                  "ORDER BY processed_at DESC LIMIT ?2", nativeQuery = true)
    List<Transaction> findRecentTransactions(Long accountId, int limit);
}
```

---

## 🔥 Exception Handling

### Banking-Specific Exceptions
```java
// Base banking exception
public class BankingException extends RuntimeException {
    public BankingException(String message) {
        super(message);
    }

    public BankingException(String message, Throwable cause) {
        super(message, cause);
    }
}

// Account-specific exceptions
public class AccountNotFoundException extends BankingException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}

public class AccountInactiveException extends BankingException {
    public AccountInactiveException(String message) {
        super(message);
    }
}

public class InsufficientFundsException extends BankingException {
    public InsufficientFundsException(String message) {
        super(message);
    }
}

// Transaction-specific exceptions
public class TransactionFailedException extends BankingException {
    public TransactionFailedException(String message) {
        super(message);
    }

    public TransactionFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

public class DuplicateTransactionException extends BankingException {
    public DuplicateTransactionException(String message) {
        super(message);
    }
}
```

---

## 📋 DTO Patterns

### Account DTOs
```java
// Account response DTO
public class AccountResponse {
    private String accountNumber;
    private String customerNumber;
    private AccountType accountType;
    private BigDecimal balance;
    private String currencyCode;
    private AccountStatus status;
    private LocalDateTime createdAt;

    // Constructors, getters, setters
}

// Account creation request DTO
public class CreateAccountRequest {
    @NotBlank(message = "Customer number is required")
    private String customerNumber;

    @NotNull(message = "Account type is required")
    private AccountType accountType;

    @Size(max = 3, message = "Currency code must be 3 characters")
    private String currencyCode = "USD";

    // Getters, setters
}

// Transfer request DTO
public class TransferRequest {
    @NotBlank(message = "From account number is required")
    private String fromAccountNumber;

    @NotBlank(message = "To account number is required")
    private String toAccountNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    // Getters, setters
}
```

---

## 📄 Configuration Examples

### Database Configuration
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/td_banking
    username: ${DB_USERNAME:banking_user}
    password: ${DB_PASSWORD:banking_pass}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
        jdbc:
          batch_size: 25
        order_inserts: true
        order_updates: true

  cache:
    type: redis
    redis:
      host: localhost
      port: 6379
      database: 0
      timeout: 2000ms

  transaction:
    default-timeout: 30
    rollback-on-commit-failure: true
```

---

## 🎯 Usage Guidelines

### When to Use This Reference
- **Consistency**: Use these patterns when creating banking examples across different guides
- **Testing**: Reference these entities when writing test cases
- **Architecture**: Use these service patterns when demonstrating Spring Boot features
- **Documentation**: Reference this model when explaining banking concepts

### Cross-Reference Locations
This banking domain model is referenced in:
- [Java Concurrency Guide](../java/JAVA_CONCURRENCY_COMPREHENSIVE.md) - Thread-safe banking operations
- [Spring Boot Guide](../spring/SPRING_BOOT_COMPREHENSIVE.md) - Service implementations and testing
- [Security Deep Dive](../security/SECURITY_DEEP_DIVE.md) - Securing banking operations
- [Microservices Communication](../microservices/MICROSERVICES_COMMUNICATION_COMPREHENSIVE.md) - Distributed banking services

### Best Practices
1. **Always use BigDecimal** for monetary amounts
2. **Implement optimistic locking** for account updates
3. **Use transactions** for multi-step operations
4. **Include audit logging** for compliance
5. **Validate business rules** before persistence
6. **Handle exceptions gracefully** with meaningful messages

---

This banking domain model provides a solid foundation for all banking-related examples throughout the interview preparation documentation, ensuring consistency and reducing redundancy across guides.