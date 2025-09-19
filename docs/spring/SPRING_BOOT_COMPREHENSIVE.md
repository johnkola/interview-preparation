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

## ☁️ Azure Services Guide

<function_calls>
<invoke name="TodoWrite">
<parameter name="todos">[{"content":"Add comprehensive OAuth questions and answers","status":"completed","activeForm":"Adding comprehensive OAuth questions and answers"},{"content":"Add LDAP integration questions and answers","status":"completed","activeForm":"Adding LDAP integration questions and answers"},{"content":"Add SSO implementation questions","status":"completed","activeForm":"Adding SSO implementation questions"},{"content":"Add MFA/2FA questions and implementation details","status":"completed","activeForm":"Adding MFA/2FA questions and implementation details"},{"content":"Add PingFederate specific questions","status":"completed","activeForm":"Adding PingFederate specific questions"},{"content":"Create Spring Boot comprehensive guide","status":"completed","activeForm":"Creating Spring Boot comprehensive guide"},{"content":"Create Azure services guide","status":"in_progress","activeForm":"Creating Azure services guide"}]