# Spring Boot Comprehensive Interview Guide

## 📖 Table of Contents

### 1. [Spring Boot Fundamentals](#spring-boot-fundamentals)
- [Q1: What is Spring Boot and how does Auto-Configuration work?](#q1-what-is-spring-boot-and-how-does-auto-configuration-work)
- [Q2: Explain Spring Boot Starters and their benefits](#q2-explain-spring-boot-starters-and-their-benefits)
- [Q3: How does Spring Boot Actuator work and what endpoints are available?](#q3-how-does-spring-boot-actuator-work-and-what-endpoints-are-available)

### 2. [Spring Data JPA](#spring-data-jpa)
- [Q4: Explain Repository Pattern and different repository interfaces](#q4-explain-repository-pattern-and-different-repository-interfaces)
- [Q5: How do you handle Transactions in Spring Data JPA?](#q5-how-do-you-handle-transactions-in-spring-data-jpa)

### 3. [Spring JDBC](#spring-jdbc)
- [Q6: How to use JdbcTemplate and NamedParameterJdbcTemplate?](#q6-how-to-use-jdbctemplate-and-namedparameterjdbctemplate)

### 4. [Configuration and Properties](#configuration-and-properties)
- [Q7: Configuration Properties and Profiles](#q7-configuration-properties-and-profiles)
- [Q8: External Configuration Management](#q8-external-configuration-management)

### 5. [Security Integration](#security-integration)
- [Q9: Spring Security Configuration](#q9-spring-security-configuration)
- [Q10: JWT Authentication Implementation](#q10-jwt-authentication-implementation)

### 6. [Testing Strategies](#testing-strategies)
- [Q11: Unit Testing with @SpringBootTest](#q11-unit-testing-with-springboottest)
- [Q12: Integration Testing Best Practices](#q12-integration-testing-best-practices)
- [Q13: Test Slices and Mock Testing](#q13-test-slices-and-mock-testing)

### 7. [Performance and Monitoring](#performance-and-monitoring)
- [Q14: Application Performance Tuning](#q14-application-performance-tuning)
- [Q15: Monitoring and Metrics](#q15-monitoring-and-metrics)

### 8. [Build Tools and DevOps](#build-tools-and-devops)
- [Q16: Maven vs Gradle Configuration](#q16-maven-vs-gradle-configuration)
- [Q17: Docker and Containerization](#q17-docker-and-containerization)

### 9. [Best Practices](#best-practices)
- [Production Deployment Guidelines](#production-deployment-guidelines)
- [Security Best Practices](#security-best-practices)
- [Performance Optimization Tips](#performance-optimization-tips)

---

## 🚀 Spring Boot Fundamentals

### Q1: What is Spring Boot and how does Auto-Configuration work?

**Answer:**
Spring Boot is an opinionated framework that simplifies Spring application development by providing auto-configuration, starter dependencies, and embedded servers.

**Auto-Configuration Process:**
1. Spring Boot scans classpath for dependencies
2. Uses `@Conditional` annotations to determine what to configure
3. Applies sensible defaults
4. Can be customized or disabled

```java
// Example: DataSource Auto-Configuration
@Configuration
@ConditionalOnClass({ DataSource.class, EmbeddedDatabaseType.class })
@ConditionalOnMissingBean(type = "javax.sql.DataSource")
@EnableConfigurationProperties(DataSourceProperties.class)
public class DataSourceAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "spring.datasource.type")
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}

// Custom Auto-Configuration
@Configuration
@ConditionalOnWebApplication
@ConditionalOnClass(RestTemplate.class)
@EnableConfigurationProperties(RestTemplateProperties.class)
public class CustomRestTemplateAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public RestTemplate restTemplate(RestTemplateProperties properties) {
        RestTemplate restTemplate = new RestTemplate();

        // Configure timeouts
        HttpComponentsClientHttpRequestFactory factory =
            new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(properties.getConnectTimeout());
        factory.setReadTimeout(properties.getReadTimeout());

        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }
}

// Enable auto-configuration
@SpringBootApplication // includes @EnableAutoConfiguration
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

// Exclude specific auto-configurations
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class CustomApplication {
    // Custom configuration
}
```

### Q2: Explain Spring Boot Starters and their benefits

**Answer:**
Starters are dependency descriptors that include all necessary dependencies for a specific functionality.

**Common Starters:**
```xml
<!-- Web Applications -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Database Access -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Testing -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

**Creating Custom Starter:**
```java
// 1. Create AutoConfiguration
@Configuration
@ConditionalOnClass(MyService.class)
@EnableConfigurationProperties(MyServiceProperties.class)
public class MyServiceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MyService myService(MyServiceProperties properties) {
        return new MyService(properties);
    }
}

// 2. Configuration Properties
@ConfigurationProperties(prefix = "myservice")
public class MyServiceProperties {
    private String apiKey;
    private int timeout = 5000;
    private boolean enabled = true;

    // getters and setters
}

// 3. Create spring.factories file
// src/main/resources/META-INF/spring.factories
org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
com.company.autoconfigure.MyServiceAutoConfiguration

// 4. Starter pom.xml
<dependencies>
    <dependency>
        <groupId>com.company</groupId>
        <artifactId>myservice-autoconfigure</artifactId>
    </dependency>
    <dependency>
        <groupId>com.company</groupId>
        <artifactId>myservice-core</artifactId>
    </dependency>
</dependencies>
```

### Q3: How does Spring Boot Actuator work and what endpoints are available?

**Answer:**
Actuator provides production-ready features for monitoring and managing applications.

**Setup and Configuration:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```yaml
# application.yml
management:
  endpoints:
    web:
      exposure:
        include: "*"  # Expose all endpoints (be careful in production)
      base-path: "/actuator"
  endpoint:
    health:
      show-details: always
    shutdown:
      enabled: true
  health:
    db:
      enabled: true
    disk-space:
      enabled: true
```

**Common Endpoints:**
```java
// Custom Health Indicator
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                    .withDetail("database", "Available")
                    .withDetail("validationQuery", "SELECT 1")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("database", "Unavailable")
                .withException(e)
                .build();
        }
        return Health.down().build();
    }
}

// Custom Info Contributor
@Component
public class CustomInfoContributor implements InfoContributor {

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("app", Map.of(
            "name", "Banking API",
            "version", "1.0.0",
            "author", "Development Team"
        ));

        builder.withDetail("build", Map.of(
            "timestamp", Instant.now(),
            "javaVersion", System.getProperty("java.version")
        ));
    }
}

// Custom Metrics
@RestController
public class BankingController {

    private final MeterRegistry meterRegistry;
    private final Counter transactionCounter;

    public BankingController(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.transactionCounter = Counter.builder("banking.transactions")
            .description("Total banking transactions")
            .register(meterRegistry);
    }

    @PostMapping("/transfer")
    @Timed(name = "transfer.time", description = "Time taken for transfers")
    public ResponseEntity<?> transfer(@RequestBody TransferRequest request) {
        transactionCounter.increment(
            Tags.of("type", "transfer", "currency", request.getCurrency())
        );

        // Process transfer
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            // Business logic
            return ResponseEntity.ok().build();
        } finally {
            sample.stop(Timer.builder("transfer.duration")
                .description("Transfer processing time")
                .register(meterRegistry));
        }
    }
}

// Security for Actuator
@Configuration
public class ActuatorSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain actuatorFilterChain(HttpSecurity http) throws Exception {
        http.requestMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers(EndpointRequest.to("health", "info")).permitAll()
                .requestMatchers(EndpointRequest.to("prometheus")).hasRole("MONITORING")
                .anyRequest().hasRole("ADMIN")
            )
            .httpBasic(withDefaults());
        return http.build();
    }
}
```

**Key Actuator Endpoints:**
- `/actuator/health` - Application health
- `/actuator/info` - Application information
- `/actuator/metrics` - Application metrics
- `/actuator/env` - Environment properties
- `/actuator/configprops` - Configuration properties
- `/actuator/beans` - Application beans
- `/actuator/mappings` - Request mappings
- `/actuator/threaddump` - Thread dump
- `/actuator/heapdump` - Heap dump

---


[⬆️ Back to Top](#table-of-contents)
## 💾 Spring Data JPA

### Q4: Explain Repository Pattern and different repository interfaces

**Answer:**

**Repository Hierarchy:**
```java
// 1. Repository<T, ID> - Marker interface
public interface Repository<T, ID> {
    // No methods, just a marker
}

// 2. CrudRepository<T, ID> - Basic CRUD operations
public interface CrudRepository<T, ID> extends Repository<T, ID> {
    <S extends T> S save(S entity);
    Optional<T> findById(ID id);
    Iterable<T> findAll();
    void deleteById(ID id);
    // ... more CRUD methods
}

// 3. PagingAndSortingRepository<T, ID> - Adds pagination and sorting
public interface PagingAndSortingRepository<T, ID> extends CrudRepository<T, ID> {
    Iterable<T> findAll(Sort sort);
    Page<T> findAll(Pageable pageable);
}

// 4. JpaRepository<T, ID> - JPA specific features
public interface JpaRepository<T, ID> extends PagingAndSortingRepository<T, ID> {
    List<T> findAll();
    void flush();
    <S extends T> S saveAndFlush(S entity);
    void deleteInBatch(Iterable<T> entities);
    // ... more JPA specific methods
}
```

**Custom Repository Implementation:**
```java
// Entity
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true)
    private String accountNumber;

    @Column(name = "balance")
    private BigDecimal balance;

    @Column(name = "account_type")
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(name = "customer_id")
    private Long customerId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // getters and setters
}

// Repository Interface
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    // Query Methods
    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerId(Long customerId);

    List<Account> findByAccountTypeAndBalanceGreaterThan(
        AccountType accountType,
        BigDecimal balance
    );

    // Custom Query with @Query
    @Query("SELECT a FROM Account a WHERE a.balance > :minBalance " +
           "AND a.accountType = :type ORDER BY a.balance DESC")
    List<Account> findHighBalanceAccounts(
        @Param("minBalance") BigDecimal minBalance,
        @Param("type") AccountType accountType
    );

    // Native Query
    @Query(value = "SELECT * FROM accounts WHERE " +
                   "balance BETWEEN :min AND :max", nativeQuery = true)
    List<Account> findAccountsInBalanceRange(
        @Param("min") BigDecimal min,
        @Param("max") BigDecimal max
    );

    // Modifying Query
    @Modifying
    @Query("UPDATE Account a SET a.balance = a.balance + :amount " +
           "WHERE a.accountNumber = :accountNumber")
    int updateBalance(@Param("accountNumber") String accountNumber,
                     @Param("amount") BigDecimal amount);

    // Pagination and Sorting
    Page<Account> findByAccountType(AccountType accountType, Pageable pageable);

    // Projection
    @Query("SELECT new com.bank.dto.AccountSummary(a.accountNumber, a.balance, a.accountType) " +
           "FROM Account a WHERE a.customerId = :customerId")
    List<AccountSummary> findAccountSummariesByCustomerId(@Param("customerId") Long customerId);

    // Custom repository method (requires implementation)
    List<Account> findAccountsWithComplexCriteria(AccountSearchCriteria criteria);
}

// Custom Repository Implementation
public interface AccountRepositoryCustom {
    List<Account> findAccountsWithComplexCriteria(AccountSearchCriteria criteria);
    BigDecimal calculateTotalBalanceByCustomer(Long customerId);
}

@Repository
public class AccountRepositoryImpl implements AccountRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Account> findAccountsWithComplexCriteria(AccountSearchCriteria criteria) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Account> query = cb.createQuery(Account.class);
        Root<Account> account = query.from(Account.class);

        List<Predicate> predicates = new ArrayList<>();

        if (criteria.getCustomerId() != null) {
            predicates.add(cb.equal(account.get("customerId"), criteria.getCustomerId()));
        }

        if (criteria.getMinBalance() != null) {
            predicates.add(cb.greaterThanOrEqualTo(account.get("balance"), criteria.getMinBalance()));
        }

        if (criteria.getMaxBalance() != null) {
            predicates.add(cb.lessThanOrEqualTo(account.get("balance"), criteria.getMaxBalance()));
        }

        if (criteria.getAccountTypes() != null && !criteria.getAccountTypes().isEmpty()) {
            predicates.add(account.get("accountType").in(criteria.getAccountTypes()));
        }

        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(account.get("balance")));

        return entityManager.createQuery(query)
            .setMaxResults(criteria.getLimit())
            .getResultList();
    }

    @Override
    public BigDecimal calculateTotalBalanceByCustomer(Long customerId) {
        String jpql = "SELECT COALESCE(SUM(a.balance), 0) FROM Account a WHERE a.customerId = :customerId";

        return entityManager.createQuery(jpql, BigDecimal.class)
            .setParameter("customerId", customerId)
            .getSingleResult();
    }
}

// Make sure your repository extends both interfaces
public interface AccountRepository extends JpaRepository<Account, Long>, AccountRepositoryCustom {
    // ... existing methods
}
```

### Q5: How do you handle Transactions in Spring Data JPA?

**Answer:**

**Transaction Configuration:**
```java
@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    @Bean
    public PlatformTransactionManager transactionManager(EntityManagerFactory emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf);
        return transactionManager;
    }
}

// Service Layer with Transactions
@Service
@Transactional(readOnly = true) // Default for all methods
public class BankingService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    // Write transaction
    @Transactional(
        isolation = Isolation.READ_COMMITTED,
        propagation = Propagation.REQUIRED,
        rollbackFor = {BankingException.class, IllegalArgumentException.class},
        timeout = 30
    )
    public void transferMoney(String fromAccount, String toAccount, BigDecimal amount) {

        Account from = accountRepository.findByAccountNumber(fromAccount)
            .orElseThrow(() -> new AccountNotFoundException("From account not found"));

        Account to = accountRepository.findByAccountNumber(toAccount)
            .orElseThrow(() -> new AccountNotFoundException("To account not found"));

        // Business validation
        if (from.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient balance");
        }

        // Update balances
        from.setBalance(from.getBalance().subtract(amount));
        to.setBalance(to.getBalance().add(amount));

        accountRepository.saveAll(Arrays.asList(from, to));

        // Create transaction records
        BankTransaction debitTx = new BankTransaction();
        debitTx.setAccountId(from.getId());
        debitTx.setAmount(amount.negate());
        debitTx.setType(TransactionType.DEBIT);
        debitTx.setDescription("Transfer to " + toAccount);

        BankTransaction creditTx = new BankTransaction();
        creditTx.setAccountId(to.getId());
        creditTx.setAmount(amount);
        creditTx.setType(TransactionType.CREDIT);
        creditTx.setDescription("Transfer from " + fromAccount);

        transactionRepository.saveAll(Arrays.asList(debitTx, creditTx));

        // This could throw an exception and rollback the entire transaction
        auditService.logTransfer(fromAccount, toAccount, amount);
    }

    // Programmatic transaction management
    @Autowired
    private TransactionTemplate transactionTemplate;

    public void programmaticTransaction() {
        transactionTemplate.execute(status -> {
            try {
                // Your transactional code here
                accountRepository.save(new Account());

                // You can set rollback only
                if (someCondition) {
                    status.setRollbackOnly();
                }

                return "Success";
            } catch (Exception e) {
                status.setRollbackOnly();
                throw e;
            }
        });
    }

    // Read-only transaction with custom timeout
    @Transactional(readOnly = true, timeout = 60)
    public List<Account> generateLargeReport() {
        // Long-running read operation
        return accountRepository.findAll();
    }

    // Different propagation behaviors
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void auditLog(String operation) {
        // This will always run in a new transaction
        // Even if called from another transactional method
        auditRepository.save(new AuditEntry(operation));
    }

    @Transactional(propagation = Propagation.NESTED)
    public void nestedOperation() {
        // Creates a savepoint that can be rolled back
        // independently of the parent transaction
    }
}

// Exception handling for transactions
@ControllerAdvice
public class TransactionExceptionHandler {

    @ExceptionHandler(TransactionException.class)
    public ResponseEntity<ErrorResponse> handleTransactionException(TransactionException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(new ErrorResponse("Transaction failed: " + e.getMessage()));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponse> handleOptimisticLocking(OptimisticLockingFailureException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(new ErrorResponse("Data was modified by another user. Please refresh and try again."));
    }
}
```

**Transaction Propagation Types:**
- `REQUIRED` (default): Join existing transaction or create new one
- `REQUIRES_NEW`: Always create new transaction
- `NESTED`: Create nested transaction with savepoint
- `SUPPORTS`: Join existing transaction or run without transaction
- `NOT_SUPPORTED`: Run without transaction, suspend current if exists
- `NEVER`: Throw exception if transaction exists
- `MANDATORY`: Throw exception if no transaction exists

---


[⬆️ Back to Top](#table-of-contents)
## 🔗 Spring JDBC

### Q6: How to use JdbcTemplate and NamedParameterJdbcTemplate?

**Answer:**

**JdbcTemplate Configuration:**
```java
@Configuration
public class JdbcConfig {

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }
}

// Repository using JdbcTemplate
@Repository
public class AccountJdbcRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private NamedParameterJdbcTemplate namedJdbcTemplate;

    // Simple query
    public int getAccountCount() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM accounts", Integer.class);
    }

    // Query with parameters
    public Account findByAccountNumber(String accountNumber) {
        String sql = "SELECT * FROM accounts WHERE account_number = ?";

        return jdbcTemplate.queryForObject(sql, new Object[]{accountNumber},
            (rs, rowNum) -> {
                Account account = new Account();
                account.setId(rs.getLong("id"));
                account.setAccountNumber(rs.getString("account_number"));
                account.setBalance(rs.getBigDecimal("balance"));
                account.setAccountType(AccountType.valueOf(rs.getString("account_type")));
                account.setCustomerId(rs.getLong("customer_id"));
                return account;
            });
    }

    // Query returning list
    public List<Account> findByCustomerId(Long customerId) {
        String sql = "SELECT * FROM accounts WHERE customer_id = ?";

        return jdbcTemplate.query(sql, new Object[]{customerId},
            new BeanPropertyRowMapper<>(Account.class));
    }

    // Using NamedParameterJdbcTemplate
    public List<Account> findAccountsInBalanceRange(BigDecimal minBalance, BigDecimal maxBalance) {
        String sql = "SELECT * FROM accounts WHERE balance BETWEEN :minBalance AND :maxBalance " +
                    "ORDER BY balance DESC";

        Map<String, Object> params = new HashMap<>();
        params.put("minBalance", minBalance);
        params.put("maxBalance", maxBalance);

        return namedJdbcTemplate.query(sql, params, new AccountRowMapper());
    }

    // Using SqlParameterSource
    public void updateAccount(Account account) {
        String sql = "UPDATE accounts SET balance = :balance, updated_at = :updatedAt " +
                    "WHERE account_number = :accountNumber";

        SqlParameterSource params = new BeanPropertySqlParameterSource(account);
        namedJdbcTemplate.update(sql, params);
    }

    // Batch operations
    public void batchInsertAccounts(List<Account> accounts) {
        String sql = "INSERT INTO accounts (account_number, balance, account_type, customer_id) " +
                    "VALUES (:accountNumber, :balance, :accountType, :customerId)";

        SqlParameterSource[] batchParams = accounts.stream()
            .map(BeanPropertySqlParameterSource::new)
            .toArray(SqlParameterSource[]::new);

        namedJdbcTemplate.batchUpdate(sql, batchParams);
    }

    // Complex query with JOIN
    public List<AccountWithCustomer> findAccountsWithCustomerInfo() {
        String sql = """
            SELECT a.id, a.account_number, a.balance, a.account_type,
                   c.id as customer_id, c.first_name, c.last_name, c.email
            FROM accounts a
            JOIN customers c ON a.customer_id = c.id
            WHERE a.balance > :minBalance
            ORDER BY a.balance DESC
            """;

        Map<String, Object> params = Map.of("minBalance", new BigDecimal("1000"));

        return namedJdbcTemplate.query(sql, params,
            (rs, rowNum) -> {
                AccountWithCustomer result = new AccountWithCustomer();
                result.setAccountId(rs.getLong("id"));
                result.setAccountNumber(rs.getString("account_number"));
                result.setBalance(rs.getBigDecimal("balance"));
                result.setAccountType(AccountType.valueOf(rs.getString("account_type")));
                result.setCustomerId(rs.getLong("customer_id"));
                result.setCustomerName(rs.getString("first_name") + " " + rs.getString("last_name"));
                result.setCustomerEmail(rs.getString("email"));
                return result;
            });
    }

    // Stored procedure call
    public void callBalanceUpdateProcedure(String accountNumber, BigDecimal amount) {
        SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
            .withProcedureName("update_account_balance")
            .withoutProcedureColumnMetaDataAccess()
            .declareParameters(
                new SqlParameter("account_num", Types.VARCHAR),
                new SqlParameter("amount", Types.DECIMAL),
                new SqlOutParameter("result", Types.INTEGER)
            );

        Map<String, Object> params = new HashMap<>();
        params.put("account_num", accountNumber);
        params.put("amount", amount);

        Map<String, Object> result = jdbcCall.execute(params);
        Integer updateResult = (Integer) result.get("result");

        if (updateResult != 1) {
            throw new RuntimeException("Failed to update balance");
        }
    }

    // Transaction with callback
    @Autowired
    private TransactionTemplate transactionTemplate;

    public void transferWithJdbc(String fromAccount, String toAccount, BigDecimal amount) {
        transactionTemplate.execute(status -> {
            try {
                // Debit from account
                String debitSql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
                int debitRows = jdbcTemplate.update(debitSql, amount, fromAccount);

                if (debitRows == 0) {
                    throw new RuntimeException("From account not found");
                }

                // Credit to account
                String creditSql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
                int creditRows = jdbcTemplate.update(creditSql, amount, toAccount);

                if (creditRows == 0) {
                    throw new RuntimeException("To account not found");
                }

                // Insert transaction log
                String logSql = "INSERT INTO transaction_log (from_account, to_account, amount, timestamp) " +
                               "VALUES (?, ?, ?, ?)";
                jdbcTemplate.update(logSql, fromAccount, toAccount, amount, new Timestamp(System.currentTimeMillis()));

                return null;
            } catch (Exception e) {
                status.setRollbackOnly();
                throw e;
            }
        });
    }
}

// Custom RowMapper
public class AccountRowMapper implements RowMapper<Account> {
    @Override
    public Account mapRow(ResultSet rs, int rowNum) throws SQLException {
        Account account = new Account();
        account.setId(rs.getLong("id"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setBalance(rs.getBigDecimal("balance"));
        account.setAccountType(AccountType.valueOf(rs.getString("account_type")));
        account.setCustomerId(rs.getLong("customer_id"));
        account.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        account.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return account;
    }
}

// ResultSet Extractor for complex mappings
public class AccountTransactionExtractor implements ResultSetExtractor<Map<Account, List<BankTransaction>>> {
    @Override
    public Map<Account, List<BankTransaction>> extractData(ResultSet rs) throws SQLException {
        Map<Account, List<BankTransaction>> result = new HashMap<>();

        while (rs.next()) {
            Long accountId = rs.getLong("account_id");
            Account account = result.keySet().stream()
                .filter(a -> a.getId().equals(accountId))
                .findFirst()
                .orElseGet(() -> {
                    Account newAccount = new Account();
                    newAccount.setId(accountId);
                    newAccount.setAccountNumber(rs.getString("account_number"));
                    newAccount.setBalance(rs.getBigDecimal("balance"));
                    result.put(newAccount, new ArrayList<>());
                    return newAccount;
                });

            if (rs.getLong("transaction_id") != 0) {
                BankTransaction transaction = new BankTransaction();
                transaction.setId(rs.getLong("transaction_id"));
                transaction.setAmount(rs.getBigDecimal("transaction_amount"));
                transaction.setType(TransactionType.valueOf(rs.getString("transaction_type")));
                transaction.setTimestamp(rs.getTimestamp("transaction_timestamp").toLocalDateTime());

                result.get(account).add(transaction);
            }
        }

        return result;
    }
}
```

---


[⬆️ Back to Top](#table-of-contents)
## ⚙️ Configuration and Properties

### Q7: Configuration Properties and Profiles

**Answer:**
Spring Boot provides multiple ways to manage configuration through properties and profiles.

**Configuration Properties:**

```java
// Application Properties File - application.yml
server:
  port: 8080
  servlet:
    context-path: /api

spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:mysql://localhost:3306/bankingdb
    username: bankuser
    password: ${DB_PASSWORD:defaultpass}
    driver-class-name: com.mysql.cj.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true

banking:
  transaction:
    daily-limit: 10000
    max-retries: 3
  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: 86400
  notification:
    email:
      enabled: true
      smtp-host: smtp.bank.com

---
# Development Profile
spring:
  profiles: dev
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password: ""
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: create-drop

banking:
  transaction:
    daily-limit: 1000
  security:
    jwt:
      secret: dev-secret

---
# Production Profile
spring:
  profiles: prod
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

banking:
  transaction:
    daily-limit: 50000
  security:
    jwt:
      secret: ${JWT_SECRET}

logging:
  level:
    com.bank: INFO
    org.springframework.security: DEBUG
```

**Configuration Properties Classes:**

```java
// Type-safe configuration properties
@ConfigurationProperties(prefix = "banking")
@Data
@Component
@Validated
public class BankingProperties {

    @NotNull
    private Transaction transaction = new Transaction();

    @NotNull
    private Security security = new Security();

    @NotNull
    private Notification notification = new Notification();

    @Data
    public static class Transaction {
        @Min(value = 100, message = "Daily limit must be at least 100")
        @Max(value = 100000, message = "Daily limit cannot exceed 100000")
        private BigDecimal dailyLimit = new BigDecimal("10000");

        @Min(1)
        @Max(10)
        private int maxRetries = 3;

        @NotBlank
        private String currency = "USD";
    }

    @Data
    public static class Security {
        @NotNull
        private Jwt jwt = new Jwt();

        @Data
        public static class Jwt {
            @NotBlank(message = "JWT secret is required")
            private String secret;

            @Min(value = 3600, message = "Expiration must be at least 1 hour")
            private long expiration = 86400;

            private String issuer = "banking-app";
        }
    }

    @Data
    public static class Notification {
        @NotNull
        private Email email = new Email();

        @Data
        public static class Email {
            private boolean enabled = true;

            @Email
            private String fromAddress = "noreply@bank.com";

            private String smtpHost = "localhost";

            @Min(1)
            @Max(65535)
            private int smtpPort = 587;
        }
    }
}

// Using configuration properties
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final BankingProperties bankingProperties;

    public void validateTransactionLimit(BigDecimal amount) {
        BigDecimal dailyLimit = bankingProperties.getTransaction().getDailyLimit();

        if (amount.compareTo(dailyLimit) > 0) {
            throw new TransactionLimitExceededException(
                "Transaction amount " + amount + " exceeds daily limit " + dailyLimit
            );
        }
    }
}
```

**Profile-specific Configuration:**

```java
// Different configurations for different profiles
@Configuration
@Profile("dev")
public class DevConfiguration {

    @Bean
    @Primary
    public EmailService mockEmailService() {
        return new MockEmailService();
    }

    @Bean
    public DataSource h2DataSource() {
        return new EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("classpath:schema-dev.sql")
            .addScript("classpath:data-dev.sql")
            .build();
    }
}

@Configuration
@Profile("prod")
public class ProductionConfiguration {

    @Bean
    public EmailService realEmailService() {
        return new SmtpEmailService();
    }

    @Bean
    public DataSource productionDataSource(@Value("${spring.datasource.url}") String url,
                                         @Value("${spring.datasource.username}") String username,
                                         @Value("${spring.datasource.password}") String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }
}

// Conditional configuration
@Configuration
@ConditionalOnProperty(name = "banking.features.fraud-detection", havingValue = "true")
public class FraudDetectionConfiguration {

    @Bean
    public FraudDetectionService fraudDetectionService() {
        return new MLFraudDetectionService();
    }
}
```

### Q8: External Configuration Management

**Answer:**
Spring Boot supports various external configuration sources with a specific precedence order.

**Configuration Precedence (highest to lowest):**

```java
// 1. Command line arguments
java -jar banking-app.jar --server.port=8081 --spring.profiles.active=prod

// 2. System properties
System.setProperty("server.port", "8081");

// 3. OS environment variables
export SERVER_PORT=8081
export SPRING_PROFILES_ACTIVE=prod

// 4. application-{profile}.properties/yml
// application-prod.properties
// application-dev.properties

// 5. application.properties/yml

// 6. @PropertySource annotations
@Configuration
@PropertySource("classpath:banking.properties")
public class ExternalConfig {
    // Configuration
}
```

**Environment-specific Configuration:**

```java
// Configuration for different environments
@Component
public class EnvironmentConfigExample {

    @Value("${banking.environment:development}")
    private String environment;

    @Value("${banking.api.base-url}")
    private String apiBaseUrl;

    @Value("${banking.cache.ttl:3600}")
    private int cacheTtl;

    // Configuration with default values
    @Value("${banking.features.mobile-app:false}")
    private boolean mobileAppEnabled;

    // Array/List configuration
    @Value("${banking.supported-currencies}")
    private String[] supportedCurrencies;

    @Value("#{'${banking.admin-emails}'.split(',')}")
    private List<String> adminEmails;

    // Complex SpEL expressions
    @Value("#{${banking.transaction.limits}}")
    private Map<String, BigDecimal> transactionLimits;
}

// External configuration file loading
@Configuration
@EnableConfigurationProperties
public class ExternalConfigurationLoader {

    @Bean
    @ConfigurationProperties(prefix = "external")
    public ExternalSettings externalSettings() {
        return new ExternalSettings();
    }

    // Load configuration from external file
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();

        Resource[] resources = new Resource[] {
            new ClassPathResource("application.properties"),
            new FileSystemResource("/etc/banking/application.properties"),
            new FileSystemResource("${user.home}/.banking/application.properties")
        };

        configurer.setLocations(resources);
        configurer.setIgnoreResourceNotFound(true);
        configurer.setIgnoreUnresolvablePlaceholders(false);

        return configurer;
    }
}
```

---


[⬆️ Back to Top](#table-of-contents)
## 🔒 Security Integration

### Q9: Spring Security Configuration

**Answer:**
Spring Security integration in Spring Boot provides comprehensive security features.

**Complete Security Configuration:**

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtRequestFilter jwtRequestFilter;
    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(ex ->
                ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // Account management - requires CUSTOMER role
                .requestMatchers(HttpMethod.GET, "/api/accounts/**")
                    .hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/accounts")
                    .hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/accounts/**")
                    .hasAnyRole("CUSTOMER", "ADMIN")

                // Transaction endpoints
                .requestMatchers(HttpMethod.GET, "/api/transactions/**")
                    .hasAnyRole("CUSTOMER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/transactions")
                    .hasRole("CUSTOMER")

                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/reports/**").hasRole("ADMIN")

                // All other requests need authentication
                .anyRequest().authenticated()
            );

        // Add JWT filter before username/password authentication filter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

// Custom User Details Service
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameOrEmail(username, username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return UserPrincipal.builder()
            .id(user.getId())
            .username(user.getUsername())
            .email(user.getEmail())
            .password(user.getPassword())
            .authorities(user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList()))
            .accountNonExpired(!user.isExpired())
            .accountNonLocked(!user.isLocked())
            .credentialsNonExpired(!user.isCredentialsExpired())
            .enabled(user.isActive())
            .build();
    }
}

// Method-level security
@Service
@RequiredArgsConstructor
public class AccountService {

    @PreAuthorize("hasRole('ADMIN') or @accountOwnershipService.isOwner(authentication.name, #accountId)")
    public Account getAccount(Long accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountId));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostAuthorize("@accountOwnershipService.isOwner(authentication.name, returnObject.id)")
    public Account createAccount(CreateAccountRequest request) {
        // Create account logic
        return savedAccount;
    }

    @PreAuthorize("@transactionAuthorizationService.canPerformTransaction(authentication.name, #request)")
    public Transaction performTransaction(TransactionRequest request) {
        // Transaction logic
        return transaction;
    }
}
```

### Q10: JWT Authentication Implementation

**Answer:**
JWT (JSON Web Token) authentication implementation in Spring Boot.

**JWT Implementation:**

```java
// JWT Utility Class
@Component
public class JwtTokenUtil {

    private static final String SECRET = "mySecretKey";
    private static final int JWT_TOKEN_VALIDITY = 5 * 60 * 60; // 5 hours

    private final Key key;

    public JwtTokenUtil(@Value("${banking.security.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Add custom claims
        if (userDetails instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) userDetails;
            claims.put("userId", userPrincipal.getId());
            claims.put("email", userPrincipal.getEmail());
            claims.put("roles", userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        }

        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + JWT_TOKEN_VALIDITY * 1000);

        return Jwts.builder()
            .setClaims(claims)
            .setSubject(subject)
            .setIssuedAt(now)
            .setExpiration(expiryDate)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = getUsernameFromToken(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String refreshToken(String token) {
        try {
            Claims claims = getAllClaimsFromToken(token);
            claims.setIssuedAt(new Date());
            claims.setExpiration(new Date(System.currentTimeMillis() + JWT_TOKEN_VALIDITY * 1000));

            return Jwts.builder()
                .setClaims(claims)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
        } catch (Exception e) {
            throw new InvalidTokenException("Cannot refresh token", e);
        }
    }
}

// JWT Request Filter
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {

    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // JWT Token is in the form "Bearer token"
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (IllegalArgumentException e) {
                log.error("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                log.error("JWT Token has expired");
            } catch (JwtException e) {
                log.error("Invalid JWT Token: {}", e.getMessage());
            }
        }

        // Validate token and set authentication
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtTokenUtil.validateToken(jwtToken, userDetails)) {
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/public/") ||
               path.startsWith("/actuator/health") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs/");
    }
}

// Authentication Controller
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final CustomUserDetailsService userDetailsService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {

        authenticate(loginRequest.getUsername(), loginRequest.getPassword());

        final UserDetails userDetails = userDetailsService
            .loadUserByUsername(loginRequest.getUsername());

        final String token = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(JwtResponse.builder()
            .token(token)
            .type("Bearer")
            .username(userDetails.getUsername())
            .authorities(userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()))
            .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {

        try {
            String refreshedToken = jwtTokenUtil.refreshToken(request.getToken());
            String username = jwtTokenUtil.getUsernameFromToken(refreshedToken);

            return ResponseEntity.ok(JwtResponse.builder()
                .token(refreshedToken)
                .type("Bearer")
                .username(username)
                .build());

        } catch (Exception e) {
            throw new InvalidTokenException("Cannot refresh token");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {

        if (userService.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException("Username is already taken!");
        }

        if (userService.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email is already in use!");
        }

        userService.createUser(request);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    private void authenticate(String username, String password) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new UserDisabledException("User is disabled", e);
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException("Invalid credentials", e);
        }
    }
}
```

---


[⬆️ Back to Top](#table-of-contents)
## 🧪 Testing Strategies

### Q11: Unit Testing with @SpringBootTest

**Answer:**
Comprehensive testing strategies for Spring Boot applications.

**Spring Boot Test Annotations:**

```java
// Full Integration Test
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-test.properties")
@ActiveProfiles("test")
class BankingApplicationIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private AccountRepository accountRepository;

    @MockBean
    private EmailService emailService;

    @Test
    @Transactional
    @Rollback
    void shouldCreateAccountSuccessfully() {
        // Given
        CreateAccountRequest request = CreateAccountRequest.builder()
            .customerId(1L)
            .accountType(AccountType.SAVINGS)
            .initialDeposit(new BigDecimal("1000.00"))
            .build();

        // When
        ResponseEntity<Account> response = restTemplate.postForEntity(
            "/api/accounts", request, Account.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getBalance()).isEqualTo(new BigDecimal("1000.00"));

        // Verify database state
        Optional<Account> savedAccount = accountRepository
            .findByAccountNumber(response.getBody().getAccountNumber());
        assertThat(savedAccount).isPresent();

        // Verify mock interaction
        verify(emailService).sendAccountCreationNotification(any(Account.class));
    }
}

// Service Layer Test
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private FraudDetectionService fraudDetectionService;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldProcessTransactionSuccessfully() {
        // Given
        Long fromAccountId = 1L;
        Long toAccountId = 2L;
        BigDecimal amount = new BigDecimal("500.00");

        Account fromAccount = Account.builder()
            .id(fromAccountId)
            .balance(new BigDecimal("1000.00"))
            .status(AccountStatus.ACTIVE)
            .build();

        Account toAccount = Account.builder()
            .id(toAccountId)
            .balance(new BigDecimal("500.00"))
            .status(AccountStatus.ACTIVE)
            .build();

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));
        when(accountRepository.findById(toAccountId)).thenReturn(Optional.of(toAccount));
        when(fraudDetectionService.isTransactionSuspicious(any())).thenReturn(false);
        when(transactionRepository.save(any(Transaction.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        TransactionResult result = transactionService.transferMoney(
            fromAccountId, toAccountId, amount, "Test transfer");

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTransactionId()).isNotNull();

        verify(accountRepository).save(argThat(account ->
            account.getId().equals(fromAccountId) &&
            account.getBalance().equals(new BigDecimal("500.00"))));

        verify(accountRepository).save(argThat(account ->
            account.getId().equals(toAccountId) &&
            account.getBalance().equals(new BigDecimal("1000.00"))));

        verify(notificationService).sendTransactionNotification(any(Transaction.class));
    }

    @Test
    void shouldFailTransactionWhenInsufficientFunds() {
        // Given
        Long fromAccountId = 1L;
        Long toAccountId = 2L;
        BigDecimal amount = new BigDecimal("1500.00");

        Account fromAccount = Account.builder()
            .id(fromAccountId)
            .balance(new BigDecimal("1000.00"))
            .status(AccountStatus.ACTIVE)
            .build();

        when(accountRepository.findById(fromAccountId)).thenReturn(Optional.of(fromAccount));

        // When & Then
        assertThatThrownBy(() -> transactionService.transferMoney(
            fromAccountId, toAccountId, amount, "Test transfer"))
            .isInstanceOf(InsufficientFundsException.class)
            .hasMessage("Insufficient funds for transaction");

        verify(accountRepository, never()).save(any(Account.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
```

### Q12: Integration Testing Best Practices

**Answer:**
Best practices for integration testing in Spring Boot applications.

**Test Slices and Specialized Testing:**

```java
// Web Layer Testing
@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @MockBean
    private JwtTokenUtil jwtTokenUtil;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldReturnAccountDetails() throws Exception {
        // Given
        Long accountId = 1L;
        Account account = Account.builder()
            .id(accountId)
            .accountNumber("ACC001")
            .balance(new BigDecimal("1000.00"))
            .accountType(AccountType.SAVINGS)
            .build();

        when(accountService.getAccount(accountId)).thenReturn(account);

        // When & Then
        mockMvc.perform(get("/api/accounts/{id}", accountId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(accountId))
                .andExpect(jsonPath("$.accountNumber").value("ACC001"))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.accountType").value("SAVINGS"));

        verify(accountService).getAccount(accountId);
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void shouldValidateCreateAccountRequest() throws Exception {
        // Given
        String invalidRequest = """
            {
                "customerId": null,
                "accountType": "INVALID_TYPE",
                "initialDeposit": -100
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[*].field",
                    hasItems("customerId", "accountType", "initialDeposit")));
    }
}

// Repository Layer Testing
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AccountRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void shouldFindAccountsByCustomerId() {
        // Given
        Customer customer = Customer.builder()
            .username("testuser")
            .email("test@example.com")
            .build();
        entityManager.persistAndFlush(customer);

        Account account1 = Account.builder()
            .accountNumber("ACC001")
            .customerId(customer.getId())
            .balance(new BigDecimal("1000.00"))
            .accountType(AccountType.SAVINGS)
            .status(AccountStatus.ACTIVE)
            .build();

        Account account2 = Account.builder()
            .accountNumber("ACC002")
            .customerId(customer.getId())
            .balance(new BigDecimal("2000.00"))
            .accountType(AccountType.CHECKING)
            .status(AccountStatus.ACTIVE)
            .build();

        entityManager.persist(account1);
        entityManager.persist(account2);
        entityManager.flush();

        // When
        List<Account> accounts = accountRepository.findByCustomerId(customer.getId());

        // Then
        assertThat(accounts).hasSize(2);
        assertThat(accounts).extracting(Account::getAccountNumber)
            .containsExactlyInAnyOrder("ACC001", "ACC002");
    }

    @Test
    void shouldFindActiveAccountsWithBalanceGreaterThan() {
        // Given
        Account activeAccount = Account.builder()
            .accountNumber("ACC001")
            .balance(new BigDecimal("1500.00"))
            .status(AccountStatus.ACTIVE)
            .build();

        Account inactiveAccount = Account.builder()
            .accountNumber("ACC002")
            .balance(new BigDecimal("2000.00"))
            .status(AccountStatus.INACTIVE)
            .build();

        Account lowBalanceAccount = Account.builder()
            .accountNumber("ACC003")
            .balance(new BigDecimal("500.00"))
            .status(AccountStatus.ACTIVE)
            .build();

        entityManager.persist(activeAccount);
        entityManager.persist(inactiveAccount);
        entityManager.persist(lowBalanceAccount);
        entityManager.flush();

        // When
        List<Account> accounts = accountRepository
            .findActiveAccountsWithMinimumBalance(new BigDecimal("1000.00"));

        // Then
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getAccountNumber()).isEqualTo("ACC001");
    }
}

// Security Testing
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SecurityIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Test
    void shouldDenyAccessWithoutAuthentication() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/accounts/1", String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldAllowAccessWithValidToken() {
        // Given
        UserDetails userDetails = User.builder()
            .username("testuser")
            .password("password")
            .authorities("ROLE_CUSTOMER")
            .build();

        String token = jwtTokenUtil.generateToken(userDetails);

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // When
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/accounts/1", HttpMethod.GET, entity, String.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

### Q13: Test Slices and Mock Testing

**Answer:**
Using Spring Boot test slices for focused testing.

**Comprehensive Test Slices:**

```java
// JSON Serialization Testing
@JsonTest
class AccountJsonTest {

    @Autowired
    private JacksonTester<Account> json;

    @Test
    void shouldSerializeAccount() throws Exception {
        // Given
        Account account = Account.builder()
            .id(1L)
            .accountNumber("ACC001")
            .balance(new BigDecimal("1000.00"))
            .accountType(AccountType.SAVINGS)
            .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
            .build();

        // When & Then
        assertThat(json.write(account))
            .hasJsonPathStringValue("$.accountNumber", "ACC001")
            .hasJsonPathNumberValue("$.balance", 1000.00)
            .hasJsonPathStringValue("$.accountType", "SAVINGS")
            .hasJsonPathStringValue("$.createdAt", "2024-01-15T10:30:00");
    }

    @Test
    void shouldDeserializeAccount() throws Exception {
        // Given
        String jsonContent = """
            {
                "id": 1,
                "accountNumber": "ACC001",
                "balance": 1000.00,
                "accountType": "SAVINGS",
                "createdAt": "2024-01-15T10:30:00"
            }
            """;

        // When & Then
        assertThat(json.parse(jsonContent))
            .usingRecursiveComparison()
            .isEqualTo(Account.builder()
                .id(1L)
                .accountNumber("ACC001")
                .balance(new BigDecimal("1000.00"))
                .accountType(AccountType.SAVINGS)
                .createdAt(LocalDateTime.of(2024, 1, 15, 10, 30, 0))
                .build());
    }
}

// Redis Testing
@DataRedisTest
class CacheServiceTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private AccountCacheService accountCacheService;

    @Test
    void shouldCacheAccount() {
        // Given
        Account account = Account.builder()
            .id(1L)
            .accountNumber("ACC001")
            .balance(new BigDecimal("1000.00"))
            .build();

        // When
        accountCacheService.cacheAccount(account);

        // Then
        Account cachedAccount = accountCacheService.getAccountFromCache(1L);
        assertThat(cachedAccount).isEqualTo(account);
    }

    @Test
    void shouldExpireCachedAccount() throws InterruptedException {
        // Given
        Account account = Account.builder()
            .id(1L)
            .accountNumber("ACC001")
            .build();

        // When
        accountCacheService.cacheAccountWithTtl(account, 1); // 1 second TTL

        // Then
        assertThat(accountCacheService.getAccountFromCache(1L)).isNotNull();

        Thread.sleep(1100); // Wait for expiration

        assertThat(accountCacheService.getAccountFromCache(1L)).isNull();
    }
}

// Message Queue Testing
@SpringBootTest
@DirtiesContext
class MessageQueueIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private TransactionEventPublisher eventPublisher;

    @RabbitListener(queues = "transaction.events")
    private List<TransactionEvent> receivedEvents = new ArrayList<>();

    @Test
    void shouldPublishAndReceiveTransactionEvent() throws InterruptedException {
        // Given
        TransactionEvent event = TransactionEvent.builder()
            .transactionId("TXN001")
            .accountId(1L)
            .amount(new BigDecimal("500.00"))
            .type(TransactionType.TRANSFER)
            .timestamp(LocalDateTime.now())
            .build();

        // When
        eventPublisher.publishTransactionEvent(event);

        // Then
        await().atMost(5, TimeUnit.SECONDS)
            .until(() -> receivedEvents.size() == 1);

        assertThat(receivedEvents.get(0))
            .usingRecursiveComparison()
            .isEqualTo(event);
    }
}

// Custom Test Configuration
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public Clock testClock() {
        return Clock.fixed(
            Instant.parse("2024-01-15T10:30:00Z"),
            ZoneOffset.UTC
        );
    }

    @Bean
    @Primary
    public EmailService mockEmailService() {
        return Mockito.mock(EmailService.class);
    }

    @EventListener
    public void handleTestEvent(TransactionEvent event) {
        // Test event handling
    }
}

// Performance Testing
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PerformanceTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void shouldResponseQuicklyUnderLoad() {
        // When
        List<CompletableFuture<ResponseEntity<String>>> futures =
            IntStream.range(0, 100)
                .mapToObj(i -> CompletableFuture.supplyAsync(() ->
                    restTemplate.getForEntity("/api/accounts/1", String.class)))
                .collect(Collectors.toList());

        // Then
        List<ResponseEntity<String>> responses = futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());

        assertThat(responses).hasSize(100);
        assertThat(responses).allMatch(response ->
            response.getStatusCode().is2xxSuccessful());
    }
}
```

---