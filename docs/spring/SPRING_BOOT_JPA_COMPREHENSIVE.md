# Spring Boot JPA Comprehensive Guide

## 📖 Table of Contents

### 1. [JPA Configuration](#jpa-configuration)
- [Q1: Basic JPA Configuration and Properties](#q1-basic-jpa-configuration-and-properties)
- [Q2: Multiple DataSource Configuration](#q2-multiple-datasource-configuration)
- [Q3: Connection Pool Configuration](#q3-connection-pool-configuration)

### 2. [Entity Design and Mapping](#entity-design-and-mapping)
- [Q4: Entity Relationships and Mapping Strategies](#q4-entity-relationships-and-mapping-strategies)
- [Q5: Advanced JPA Annotations](#q5-advanced-jpa-annotations)
- [Q6: Inheritance Mapping Strategies](#q6-inheritance-mapping-strategies)

### 3. [Repository Pattern](#repository-pattern)
- [Q7: Custom Repository Implementation](#q7-custom-repository-implementation)
- [Q8: Query Methods and Specifications](#q8-query-methods-and-specifications)
- [Q9: Projections and DTOs](#q9-projections-and-dtos)

### 4. [Transaction Management](#transaction-management)
- [Q10: Declarative Transaction Management](#q10-declarative-transaction-management)
- [Q11: Programmatic Transactions](#q11-programmatic-transactions)
- [Q12: Transaction Isolation and Propagation](#q12-transaction-isolation-and-propagation)

### 5. [Performance Optimization](#performance-optimization)
- [Q13: Query Optimization and N+1 Problem](#q13-query-optimization-and-n1-problem)
- [Q14: Caching Strategies](#q14-caching-strategies)
- [Q15: Pagination and Sorting](#q15-pagination-and-sorting)

### 6. [Banking Use Cases](#banking-use-cases)
- [Q16: Banking Entity Model](#q16-banking-entity-model)
- [Q17: Audit Trail Implementation](#q17-audit-trail-implementation)
- [Q18: Soft Delete Implementation](#q18-soft-delete-implementation)

---

## 🔧 JPA Configuration

### Q1: Basic JPA Configuration and Properties

**Question**: How do you configure Spring Boot JPA with different databases and what are the essential properties?

**Answer**:

**Basic Configuration - MySQL:**

```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/banking_db?useSSL=false&serverTimezone=UTC
    username: ${DB_USERNAME:bankuser}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver

    # Connection Pool Configuration
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000

  jpa:
    # Hibernate Configuration
    hibernate:
      ddl-auto: validate  # validate, update, create, create-drop
      naming:
        physical-strategy: org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl
        implicit-strategy: org.hibernate.boot.model.naming.ImplicitNamingStrategyLegacyJpaImpl

    # SQL Configuration
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
        use_sql_comments: true
        generate_statistics: false

        # Query Configuration
        default_batch_fetch_size: 16
        jdbc:
          batch_size: 20
          batch_versioned_data: true
        order_inserts: true
        order_updates: true

        # Cache Configuration
        cache:
          use_second_level_cache: true
          use_query_cache: true
          region:
            factory_class: org.hibernate.cache.jcache.JCacheRegionFactory

# Logging Configuration
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
    org.springframework.transaction: DEBUG
```

**Configuration Class:**

```java
@Configuration
@EnableJpaRepositories(
    basePackages = "com.bank.repository",
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager"
)
@EnableTransactionManagement
public class JpaConfiguration {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource() {
        return dataSourceProperties()
            .initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
            DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean factory =
            new LocalContainerEntityManagerFactoryBean();

        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.bank.entity");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        jpaProperties.put("hibernate.hbm2ddl.auto", "validate");
        jpaProperties.put("hibernate.show_sql", "false");
        jpaProperties.put("hibernate.format_sql", "true");
        jpaProperties.put("hibernate.default_batch_fetch_size", "16");
        jpaProperties.put("hibernate.jdbc.batch_size", "20");
        jpaProperties.put("hibernate.order_inserts", "true");
        jpaProperties.put("hibernate.order_updates", "true");

        factory.setJpaProperties(jpaProperties);

        return factory;
    }

    @Bean
    @Primary
    public PlatformTransactionManager transactionManager(
            EntityManagerFactory entityManagerFactory) {

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        return transactionManager;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
```

**Environment-Specific Configurations:**

```yaml
---
# Development Profile
spring:
  profiles: dev
  datasource:
    url: jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: ""
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  h2:
    console:
      enabled: true
      path: /h2-console

---
# Testing Profile
spring:
  profiles: test
  datasource:
    url: jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
    username: sa
    password: ""
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

---
# Production Profile
spring:
  profiles: prod
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 30
      minimum-idle: 10
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        generate_statistics: true
```

### Q2: Multiple DataSource Configuration

**Question**: How do you configure multiple databases in Spring Boot JPA?

**Answer**:

**Multiple DataSource Configuration:**

```java
// Primary DataSource Configuration
@Configuration
@EnableJpaRepositories(
    basePackages = "com.bank.primary.repository",
    entityManagerFactoryRef = "primaryEntityManagerFactory",
    transactionManagerRef = "primaryTransactionManager"
)
public class PrimaryDatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.primary")
    public DataSourceProperties primaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.primary.hikari")
    public DataSource primaryDataSource() {
        return primaryDataSourceProperties()
            .initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean primaryEntityManagerFactory(
            @Qualifier("primaryDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean factory =
            new LocalContainerEntityManagerFactoryBean();

        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.bank.primary.entity");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setPersistenceUnitName("primaryPU");

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.MySQL8Dialect");
        jpaProperties.put("hibernate.hbm2ddl.auto", "validate");

        factory.setJpaProperties(jpaProperties);
        return factory;
    }

    @Bean
    @Primary
    public PlatformTransactionManager primaryTransactionManager(
            @Qualifier("primaryEntityManagerFactory") EntityManagerFactory factory) {

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(factory);
        return transactionManager;
    }
}

// Secondary DataSource Configuration
@Configuration
@EnableJpaRepositories(
    basePackages = "com.bank.secondary.repository",
    entityManagerFactoryRef = "secondaryEntityManagerFactory",
    transactionManagerRef = "secondaryTransactionManager"
)
public class SecondaryDatabaseConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.secondary")
    public DataSourceProperties secondaryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.secondary.hikari")
    public DataSource secondaryDataSource() {
        return secondaryDataSourceProperties()
            .initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean secondaryEntityManagerFactory(
            @Qualifier("secondaryDataSource") DataSource dataSource) {

        LocalContainerEntityManagerFactoryBean factory =
            new LocalContainerEntityManagerFactoryBean();

        factory.setDataSource(dataSource);
        factory.setPackagesToScan("com.bank.secondary.entity");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        factory.setPersistenceUnitName("secondaryPU");

        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        jpaProperties.put("hibernate.hbm2ddl.auto", "validate");

        factory.setJpaProperties(jpaProperties);
        return factory;
    }

    @Bean
    public PlatformTransactionManager secondaryTransactionManager(
            @Qualifier("secondaryEntityManagerFactory") EntityManagerFactory factory) {

        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(factory);
        return transactionManager;
    }
}

// Application Properties
spring:
  datasource:
    primary:
      url: jdbc:mysql://localhost:3306/banking_primary
      username: user1
      password: pass1
      driver-class-name: com.mysql.cj.jdbc.Driver
      hikari:
        maximum-pool-size: 20
        minimum-idle: 5

    secondary:
      url: jdbc:postgresql://localhost:5432/banking_secondary
      username: user2
      password: pass2
      driver-class-name: org.postgresql.Driver
      hikari:
        maximum-pool-size: 15
        minimum-idle: 3
```

**Usage in Services:**

```java
@Service
@Transactional
public class BankingService {

    // Primary database repositories
    @Autowired
    private AccountRepository accountRepository; // Primary DB

    @Autowired
    private TransactionRepository transactionRepository; // Primary DB

    // Secondary database repositories
    @Autowired
    private AuditLogRepository auditLogRepository; // Secondary DB

    @Autowired
    private ReportRepository reportRepository; // Secondary DB

    @Transactional("primaryTransactionManager")
    public void processTransaction(TransferRequest request) {
        // Operations on primary database
        Account fromAccount = accountRepository.findByAccountNumber(
            request.getFromAccount());
        Account toAccount = accountRepository.findByAccountNumber(
            request.getToAccount());

        // Update balances
        fromAccount.debit(request.getAmount());
        toAccount.credit(request.getAmount());

        accountRepository.saveAll(Arrays.asList(fromAccount, toAccount));

        // Save transaction record
        Transaction transaction = new Transaction(
            request.getFromAccount(),
            request.getToAccount(),
            request.getAmount()
        );
        transactionRepository.save(transaction);
    }

    @Transactional("secondaryTransactionManager")
    public void logAuditEvent(AuditEvent event) {
        // Operations on secondary database
        AuditLog auditLog = new AuditLog(
            event.getUserId(),
            event.getAction(),
            event.getTimestamp(),
            event.getDetails()
        );
        auditLogRepository.save(auditLog);
    }

    // Cross-database operation with separate transactions
    public void processWithAudit(TransferRequest request) {
        try {
            processTransaction(request);

            // Log successful transaction
            AuditEvent event = new AuditEvent(
                request.getUserId(),
                "TRANSFER_SUCCESS",
                LocalDateTime.now(),
                "Transfer completed successfully"
            );
            logAuditEvent(event);

        } catch (Exception e) {
            // Log failed transaction
            AuditEvent event = new AuditEvent(
                request.getUserId(),
                "TRANSFER_FAILED",
                LocalDateTime.now(),
                "Transfer failed: " + e.getMessage()
            );
            logAuditEvent(event);
            throw e;
        }
    }
}
```

### Q3: Connection Pool Configuration

**Question**: How do you optimize connection pool settings for high-performance banking applications?

**Answer**:

**HikariCP Configuration:**

```yaml
spring:
  datasource:
    # Basic Connection Settings
    url: jdbc:mysql://localhost:3306/banking_db
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver

    hikari:
      # Pool Size Configuration
      maximum-pool-size: 20          # Maximum number of connections
      minimum-idle: 5                # Minimum idle connections

      # Timeout Configuration
      connection-timeout: 30000      # 30 seconds to get connection
      idle-timeout: 600000           # 10 minutes idle timeout
      max-lifetime: 1800000          # 30 minutes maximum lifetime

      # Performance Settings
      leak-detection-threshold: 60000 # 1 minute leak detection
      validation-timeout: 5000       # 5 seconds validation timeout

      # Connection Properties
      auto-commit: false             # Disable auto-commit for transactions
      read-only: false               # Allow write operations

      # Database-specific properties
      data-source-properties:
        cachePrepStmts: true
        prepStmtCacheSize: 250
        prepStmtCacheSqlLimit: 2048
        useServerPrepStmts: true
        useLocalSessionState: true
        rewriteBatchedStatements: true
        cacheResultSetMetadata: true
        cacheServerConfiguration: true
        elideSetAutoCommits: true
        maintainTimeStats: false
```

**Custom Connection Pool Configuration:**

```java
@Configuration
public class ConnectionPoolConfig {

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public HikariConfig hikariConfig() {
        HikariConfig config = new HikariConfig();

        // Pool sizing for banking workload
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);

        // Timeout settings for high availability
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);

        // Performance optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        // Banking-specific settings
        config.setAutoCommit(false);
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");

        return config;
    }

    @Bean
    @Primary
    public DataSource dataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password,
            HikariConfig hikariConfig) {

        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        return new HikariDataSource(hikariConfig);
    }

    // Connection pool monitoring
    @Bean
    public DataSourceHealthIndicator dataSourceHealthIndicator(DataSource dataSource) {
        return new DataSourceHealthIndicator(dataSource, "SELECT 1");
    }

    // Custom connection pool metrics
    @EventListener
    public void handleContextRefresh(ContextRefreshedEvent event) {
        DataSource dataSource = event.getApplicationContext().getBean(DataSource.class);
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
            HikariPoolMXBean poolBean = hikariDataSource.getHikariPoolMXBean();

            // Register metrics
            Gauge.builder("hikari.connections.active")
                .register(Metrics.globalRegistry, poolBean::getActiveConnections);

            Gauge.builder("hikari.connections.idle")
                .register(Metrics.globalRegistry, poolBean::getIdleConnections);

            Gauge.builder("hikari.connections.pending")
                .register(Metrics.globalRegistry, poolBean::getThreadsAwaitingConnection);

            Gauge.builder("hikari.connections.total")
                .register(Metrics.globalRegistry, poolBean::getTotalConnections);
        }
    }
}

// Production-optimized configuration
@Configuration
@Profile("prod")
public class ProductionConnectionPoolConfig {

    @Bean
    @Primary
    public DataSource productionDataSource(
            @Value("${spring.datasource.url}") String url,
            @Value("${spring.datasource.username}") String username,
            @Value("${spring.datasource.password}") String password) {

        HikariConfig config = new HikariConfig();

        // Production pool sizing
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(50);        // Higher for production load
        config.setMinimumIdle(10);            // Higher minimum for quick response

        // Aggressive timeout settings
        config.setConnectionTimeout(20000);   // Shorter timeout
        config.setIdleTimeout(300000);        // 5 minutes idle
        config.setMaxLifetime(900000);        // 15 minutes max lifetime
        config.setLeakDetectionThreshold(30000); // 30 seconds leak detection

        // Production optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "500");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "4096");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        // SSL and security
        config.addDataSourceProperty("useSSL", "true");
        config.addDataSourceProperty("requireSSL", "true");
        config.addDataSourceProperty("verifyServerCertificate", "true");

        return new HikariDataSource(config);
    }
}
```

---

## 🗂️ Entity Design and Mapping

### Q4: Entity Relationships and Mapping Strategies

**Question**: How do you design JPA entities for complex banking relationships with proper mapping strategies?

**Answer**:

**Banking Entity Model:**

```java
// Base Entity with Audit Fields
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private String updatedBy;

    @Version
    private Long version;

    // getters and setters
}

// Customer Entity
@Entity
@Table(name = "customers", indexes = {
    @Index(name = "idx_customer_email", columnList = "email"),
    @Index(name = "idx_customer_ssn", columnList = "ssn")
})
@NamedQueries({
    @NamedQuery(
        name = "Customer.findByEmailDomain",
        query = "SELECT c FROM Customer c WHERE c.email LIKE :domain"
    ),
    @NamedQuery(
        name = "Customer.findActiveCustomers",
        query = "SELECT c FROM Customer c WHERE c.status = 'ACTIVE'"
    )
})
public class Customer extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "ssn", unique = true, length = 11)
    private String ssn;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CustomerStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "customer_type", nullable = false)
    private CustomerType customerType;

    // Address as Embedded Entity
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "street", column = @Column(name = "address_street")),
        @AttributeOverride(name = "city", column = @Column(name = "address_city")),
        @AttributeOverride(name = "state", column = @Column(name = "address_state")),
        @AttributeOverride(name = "zipCode", column = @Column(name = "address_zip_code")),
        @AttributeOverride(name = "country", column = @Column(name = "address_country"))
    })
    private Address address;

    // One-to-Many: Customer -> Accounts
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("createdAt DESC")
    private Set<Account> accounts = new HashSet<>();

    // One-to-Many: Customer -> Cards
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Card> cards = new HashSet<>();

    // One-to-One: Customer -> CustomerProfile
    @OneToOne(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @PrimaryKeyJoinColumn
    private CustomerProfile customerProfile;

    // Utility methods
    public void addAccount(Account account) {
        accounts.add(account);
        account.setCustomer(this);
    }

    public void removeAccount(Account account) {
        accounts.remove(account);
        account.setCustomer(null);
    }

    // getters and setters
}

// Account Entity
@Entity
@Table(name = "accounts", indexes = {
    @Index(name = "idx_account_number", columnList = "account_number", unique = true),
    @Index(name = "idx_account_customer", columnList = "customer_id"),
    @Index(name = "idx_account_type_status", columnList = "account_type, status")
})
public class Account extends BaseEntity {

    @Column(name = "account_number", nullable = false, unique = true, length = 20)
    private String accountNumber;

    @Column(name = "balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false)
    private AccountType accountType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private AccountStatus status;

    @Column(name = "interest_rate", precision = 5, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "overdraft_limit", precision = 19, scale = 2)
    private BigDecimal overdraftLimit;

    // Many-to-One: Account -> Customer
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_account_customer"))
    private Customer customer;

    // Many-to-One: Account -> Branch
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id",
                foreignKey = @ForeignKey(name = "fk_account_branch"))
    private Branch branch;

    // One-to-Many: Account -> Transactions
    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("transactionDate DESC")
    private List<Transaction> transactions = new ArrayList<>();

    // Many-to-Many: Account -> Services
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "account_services",
        joinColumns = @JoinColumn(name = "account_id"),
        inverseJoinColumns = @JoinColumn(name = "service_id"),
        foreignKey = @ForeignKey(name = "fk_account_services_account"),
        inverseForeignKey = @ForeignKey(name = "fk_account_services_service")
    )
    private Set<BankingService> services = new HashSet<>();

    // Business methods
    public void debit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Debit amount must be positive");
        }

        BigDecimal newBalance = balance.subtract(amount);
        BigDecimal minAllowedBalance = overdraftLimit != null ?
            overdraftLimit.negate() : BigDecimal.ZERO;

        if (newBalance.compareTo(minAllowedBalance) < 0) {
            throw new InsufficientFundsException(
                "Insufficient funds. Available: " + getAvailableBalance());
        }

        this.balance = newBalance;
    }

    public void credit(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Credit amount must be positive");
        }
        this.balance = balance.add(amount);
    }

    public BigDecimal getAvailableBalance() {
        return overdraftLimit != null ?
            balance.add(overdraftLimit) : balance;
    }

    // getters and setters
}

// Transaction Entity
@Entity
@Table(name = "transactions", indexes = {
    @Index(name = "idx_transaction_account", columnList = "account_id"),
    @Index(name = "idx_transaction_date", columnList = "transaction_date"),
    @Index(name = "idx_transaction_type", columnList = "transaction_type"),
    @Index(name = "idx_transaction_reference", columnList = "reference_number")
})
public class Transaction extends BaseEntity {

    @Column(name = "reference_number", nullable = false, unique = true, length = 50)
    private String referenceNumber;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "balance_after", precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    // Many-to-One: Transaction -> Account
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_transaction_account"))
    private Account account;

    // Self-referencing for transfer transactions
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_transaction_id",
                foreignKey = @ForeignKey(name = "fk_transaction_related"))
    private Transaction relatedTransaction;

    @OneToMany(mappedBy = "relatedTransaction", fetch = FetchType.LAZY)
    private List<Transaction> relatedTransactions = new ArrayList<>();

    // Additional fields for complex transactions
    @ElementCollection
    @CollectionTable(
        name = "transaction_metadata",
        joinColumns = @JoinColumn(name = "transaction_id"),
        foreignKey = @ForeignKey(name = "fk_transaction_metadata")
    )
    @MapKeyColumn(name = "metadata_key")
    @Column(name = "metadata_value")
    private Map<String, String> metadata = new HashMap<>();

    // getters and setters
}

// Address Embeddable
@Embeddable
public class Address {

    @Column(name = "street", length = 255)
    private String street;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 50)
    private String state;

    @Column(name = "zip_code", length = 10)
    private String zipCode;

    @Column(name = "country", length = 100)
    private String country;

    // getters and setters
}

// Enums
public enum CustomerStatus {
    ACTIVE, INACTIVE, SUSPENDED, CLOSED
}

public enum CustomerType {
    INDIVIDUAL, BUSINESS, CORPORATE
}

public enum AccountType {
    CHECKING, SAVINGS, CREDIT, LOAN, INVESTMENT
}

public enum AccountStatus {
    ACTIVE, INACTIVE, FROZEN, CLOSED
}

public enum TransactionType {
    DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT, FEE, INTEREST
}

public enum TransactionStatus {
    PENDING, COMPLETED, FAILED, CANCELLED
}
```

### Q5: Advanced JPA Annotations

**Question**: What are the advanced JPA annotations and how do you use them in complex banking scenarios?

**Answer**:

**Advanced Annotations Usage:**

```java
// Entity with Custom ID Generation
@Entity
@Table(name = "loans",
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_loan_number", columnNames = "loan_number")
       })
@SQLDelete(sql = "UPDATE loans SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Loan extends BaseEntity {

    // Custom ID Generator
    @Id
    @GenericGenerator(
        name = "loan-id-generator",
        strategy = "com.bank.generator.LoanIdGenerator"
    )
    @GeneratedValue(generator = "loan-id-generator")
    @Column(name = "loan_number", length = 20)
    private String loanNumber;

    @Column(name = "principal_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "term_months", nullable = false)
    private Integer termMonths;

    // Formula-based field
    @Formula("principal_amount * interest_rate / 100 / 12")
    private BigDecimal monthlyInterest;

    // JSON column for PostgreSQL
    @Type(type = "com.vladmihalcea.hibernate.type.json.JsonBinaryType")
    @Column(name = "loan_terms", columnDefinition = "jsonb")
    private LoanTerms loanTerms;

    // Soft delete flag
    @Column(name = "deleted", nullable = false)
    private Boolean deleted = false;

    // Optimistic locking with custom column
    @Version
    @Column(name = "version")
    private Long version;

    // Batch size optimization
    @BatchSize(size = 20)
    @OneToMany(mappedBy = "loan", fetch = FetchType.LAZY)
    @OrderBy("payment_date ASC")
    private List<LoanPayment> payments = new ArrayList<>();

    // Cascade operations
    @OneToMany(mappedBy = "loan",
               cascade = {CascadeType.PERSIST, CascadeType.MERGE},
               orphanRemoval = true)
    private List<LoanDocument> documents = new ArrayList<>();

    // Custom fetch mode
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    @Fetch(FetchMode.SELECT)
    private Customer customer;

    // Lazy loading with proxy
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @PrimaryKeyJoinColumn
    @LazyToOne(LazyToOneOption.NO_PROXY)
    private LoanAgreement agreement;

    // getters and setters
}

// Value object for JSON mapping
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoanTerms {
    private String collateralType;
    private BigDecimal collateralValue;
    private String paymentFrequency;
    private Boolean prepaymentAllowed;
    private BigDecimal prepaymentPenalty;
    private List<String> covenants;
}

// Custom ID Generator
public class LoanIdGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        String prefix = "LN";
        String year = String.valueOf(LocalDate.now().getYear());

        // Query for next sequence number
        String sql = "SELECT COALESCE(MAX(CAST(SUBSTRING(loan_number, 7) AS UNSIGNED)), 0) + 1 " +
                    "FROM loans WHERE loan_number LIKE ?";

        Query query = session.createNativeQuery(sql);
        query.setParameter(1, prefix + year + "%");

        Number nextVal = (Number) query.uniqueResult();
        String sequence = String.format("%06d", nextVal.intValue());

        return prefix + year + sequence;
    }
}

// Entity with Custom Column Types
@Entity
@Table(name = "investment_portfolios")
@TypeDefs({
    @TypeDef(name = "json", typeClass = JsonBinaryType.class),
    @TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
})
public class InvestmentPortfolio extends BaseEntity {

    @Column(name = "portfolio_name", nullable = false)
    private String portfolioName;

    // Array type for PostgreSQL
    @Type(type = "com.vladmihalcea.hibernate.type.array.StringArrayType")
    @Column(name = "asset_classes", columnDefinition = "text[]")
    private String[] assetClasses;

    // JSON type for complex data
    @Type(type = "jsonb")
    @Column(name = "allocation_strategy", columnDefinition = "jsonb")
    private AllocationStrategy allocationStrategy;

    // Money type with custom converter
    @Convert(converter = MoneyConverter.class)
    @Column(name = "total_value")
    private Money totalValue;

    // Encrypted field
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "account_access_key")
    private String accountAccessKey;

    // Temporal type with custom precision
    @CreationTimestamp
    @Column(name = "created_at",
            columnDefinition = "TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6)")
    private LocalDateTime createdAt;

    // Collection of basic types
    @ElementCollection
    @CollectionTable(name = "portfolio_tags",
                    joinColumns = @JoinColumn(name = "portfolio_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    // Map of basic types
    @ElementCollection
    @CollectionTable(name = "portfolio_metrics",
                    joinColumns = @JoinColumn(name = "portfolio_id"))
    @MapKeyColumn(name = "metric_name")
    @Column(name = "metric_value")
    private Map<String, BigDecimal> metrics = new HashMap<>();

    // getters and setters
}

// Custom Converters
@Converter(autoApply = true)
public class MoneyConverter implements AttributeConverter<Money, BigDecimal> {

    @Override
    public BigDecimal convertToDatabaseColumn(Money money) {
        return money != null ? money.getAmount() : null;
    }

    @Override
    public Money convertToEntityAttribute(BigDecimal dbData) {
        return dbData != null ? Money.of(dbData, "USD") : null;
    }
}

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    private final AESUtil encryptionUtil = new AESUtil();

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            return encryptionUtil.encrypt(attribute);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return encryptionUtil.decrypt(dbData);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}

// Entity Listeners for Complex Auditing
@EntityListeners({AuditingEntityListener.class, CustomAuditListener.class})
@Entity
public class HighValueTransaction extends BaseEntity {

    @Column(name = "amount", precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "requires_approval")
    private Boolean requiresApproval;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "approval_timestamp")
    private LocalDateTime approvalTimestamp;

    // Lifecycle callbacks
    @PrePersist
    public void prePersist() {
        if (amount.compareTo(new BigDecimal("10000")) > 0) {
            requiresApproval = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        // Custom validation before update
        if (requiresApproval && approvedBy == null) {
            throw new IllegalStateException("High-value transaction requires approval");
        }
    }

    // getters and setters
}

// Custom Entity Listener
public class CustomAuditListener {

    @PrePersist
    @PreUpdate
    public void beforeSave(Object entity) {
        if (entity instanceof HighValueTransaction) {
            HighValueTransaction transaction = (HighValueTransaction) entity;

            // Log high-value transaction attempt
            auditService.logHighValueTransaction(transaction);

            // Send notification if required
            if (transaction.getRequiresApproval()) {
                notificationService.notifyApprovers(transaction);
            }
        }
    }

    @Autowired
    private AuditService auditService;

    @Autowired
    private NotificationService notificationService;
}
```

### Q6: Inheritance Mapping Strategies

**Question**: How do you implement inheritance mapping strategies in JPA for banking domain models?

**Answer**:

**1. Single Table Strategy:**

```java
// Single Table Inheritance - Good for simple hierarchies
@Entity
@Table(name = "bank_products")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "product_type", discriminatorType = DiscriminatorType.STRING)
public abstract class BankProduct extends BaseEntity {

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "description")
    private String description;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status;

    @Column(name = "annual_fee", precision = 10, scale = 2)
    private BigDecimal annualFee;

    // Common business logic
    public abstract BigDecimal calculateMonthlyFee();
    public abstract boolean isEligibleForCustomer(Customer customer);

    // getters and setters
}

@Entity
@DiscriminatorValue("SAVINGS_ACCOUNT")
public class SavingsAccount extends BankProduct {

    @Column(name = "interest_rate", precision = 5, scale = 4)
    private BigDecimal interestRate;

    @Column(name = "minimum_balance", precision = 19, scale = 2)
    private BigDecimal minimumBalance;

    @Column(name = "withdrawal_limit")
    private Integer monthlyWithdrawalLimit;

    @Override
    public BigDecimal calculateMonthlyFee() {
        // Free if minimum balance maintained
        return getMinimumBalance() != null &&
               getCurrentBalance().compareTo(getMinimumBalance()) >= 0 ?
               BigDecimal.ZERO : new BigDecimal("5.00");
    }

    @Override
    public boolean isEligibleForCustomer(Customer customer) {
        // Basic eligibility: active customer
        return customer.getStatus() == CustomerStatus.ACTIVE;
    }

    private BigDecimal getCurrentBalance() {
        // Implementation to get current balance
        return BigDecimal.ZERO; // Placeholder
    }

    // getters and setters
}

@Entity
@DiscriminatorValue("CREDIT_CARD")
public class CreditCard extends BankProduct {

    @Column(name = "credit_limit", precision = 19, scale = 2)
    private BigDecimal creditLimit;

    @Column(name = "apr", precision = 5, scale = 4)
    private BigDecimal apr;

    @Column(name = "cash_advance_limit", precision = 19, scale = 2)
    private BigDecimal cashAdvanceLimit;

    @Column(name = "rewards_program")
    private String rewardsProgram;

    @Override
    public BigDecimal calculateMonthlyFee() {
        return getAnnualFee() != null ?
               getAnnualFee().divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP) :
               BigDecimal.ZERO;
    }

    @Override
    public boolean isEligibleForCustomer(Customer customer) {
        // Credit card eligibility: credit score and income
        return customer.getStatus() == CustomerStatus.ACTIVE &&
               customer.getCreditScore() >= 650 &&
               customer.getAnnualIncome().compareTo(new BigDecimal("30000")) >= 0;
    }

    // getters and setters
}
```

**2. Joined Table Strategy:**

```java
// Joined Table Inheritance - Better for complex hierarchies with many fields
@Entity
@Table(name = "financial_instruments")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "instrument_type")
public abstract class FinancialInstrument extends BaseEntity {

    @Column(name = "symbol", unique = true, nullable = false)
    private String symbol;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "market_value", precision = 19, scale = 4)
    private BigDecimal marketValue;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    // Abstract methods
    public abstract BigDecimal calculateRisk();
    public abstract BigDecimal calculateExpectedReturn();

    // getters and setters
}

@Entity
@Table(name = "stocks")
@DiscriminatorValue("STOCK")
@PrimaryKeyJoinColumn(name = "instrument_id")
public class Stock extends FinancialInstrument {

    @Column(name = "sector")
    private String sector;

    @Column(name = "market_cap", precision = 19, scale = 2)
    private BigDecimal marketCap;

    @Column(name = "dividend_yield", precision = 5, scale = 4)
    private BigDecimal dividendYield;

    @Column(name = "beta", precision = 5, scale = 4)
    private BigDecimal beta;

    @Column(name = "pe_ratio", precision = 8, scale = 2)
    private BigDecimal peRatio;

    @Column(name = "shares_outstanding")
    private Long sharesOutstanding;

    @Override
    public BigDecimal calculateRisk() {
        // Risk calculation based on beta and sector volatility
        BigDecimal sectorRisk = getSectorRiskFactor();
        return getBeta().multiply(sectorRisk);
    }

    @Override
    public BigDecimal calculateExpectedReturn() {
        // Expected return based on dividend yield and historical performance
        return getDividendYield().add(getHistoricalGrowthRate());
    }

    private BigDecimal getSectorRiskFactor() {
        // Implementation to get sector-specific risk factor
        return new BigDecimal("1.2"); // Placeholder
    }

    private BigDecimal getHistoricalGrowthRate() {
        // Implementation to calculate historical growth
        return new BigDecimal("0.08"); // Placeholder
    }

    // getters and setters
}

@Entity
@Table(name = "bonds")
@DiscriminatorValue("BOND")
@PrimaryKeyJoinColumn(name = "instrument_id")
public class Bond extends FinancialInstrument {

    @Column(name = "face_value", precision = 19, scale = 2)
    private BigDecimal faceValue;

    @Column(name = "coupon_rate", precision = 5, scale = 4)
    private BigDecimal couponRate;

    @Column(name = "maturity_date")
    private LocalDate maturityDate;

    @Column(name = "credit_rating")
    private String creditRating;

    @Column(name = "issuer")
    private String issuer;

    @Column(name = "duration", precision = 8, scale = 4)
    private BigDecimal duration;

    @Override
    public BigDecimal calculateRisk() {
        // Bond risk based on duration and credit rating
        BigDecimal creditRisk = getCreditRiskFactor();
        BigDecimal interestRateRisk = getDuration().multiply(new BigDecimal("0.01"));
        return creditRisk.add(interestRateRisk);
    }

    @Override
    public BigDecimal calculateExpectedReturn() {
        // Expected return is primarily the coupon rate
        return getCouponRate();
    }

    private BigDecimal getCreditRiskFactor() {
        // Map credit rating to risk factor
        switch (getCreditRating()) {
            case "AAA": return new BigDecimal("0.01");
            case "AA": return new BigDecimal("0.02");
            case "A": return new BigDecimal("0.03");
            case "BBB": return new BigDecimal("0.05");
            default: return new BigDecimal("0.10");
        }
    }

    // getters and setters
}
```

**3. Table Per Class Strategy:**

```java
// Table Per Class Inheritance - Each class has its own table
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class LoanProduct extends BaseEntity {

    @Column(name = "product_code", unique = true)
    private String productCode;

    @Column(name = "maximum_amount", precision = 19, scale = 2)
    private BigDecimal maximumAmount;

    @Column(name = "minimum_amount", precision = 19, scale = 2)
    private BigDecimal minimumAmount;

    @Column(name = "base_interest_rate", precision = 5, scale = 4)
    private BigDecimal baseInterestRate;

    // Abstract methods
    public abstract BigDecimal calculateInterestRate(Customer customer);
    public abstract List<String> getRequiredDocuments();
    public abstract boolean validateEligibility(Customer customer);

    // getters and setters
}

@Entity
@Table(name = "personal_loans")
public class PersonalLoan extends LoanProduct {

    @Column(name = "maximum_term_months")
    private Integer maximumTermMonths;

    @Column(name = "minimum_income_required", precision = 19, scale = 2)
    private BigDecimal minimumIncomeRequired;

    @Column(name = "requires_collateral")
    private Boolean requiresCollateral;

    @Override
    public BigDecimal calculateInterestRate(Customer customer) {
        BigDecimal baseRate = getBaseInterestRate();

        // Adjust based on credit score
        int creditScore = customer.getCreditScore();
        if (creditScore >= 750) {
            return baseRate; // Best rate
        } else if (creditScore >= 700) {
            return baseRate.add(new BigDecimal("0.5"));
        } else if (creditScore >= 650) {
            return baseRate.add(new BigDecimal("1.0"));
        } else {
            return baseRate.add(new BigDecimal("2.0"));
        }
    }

    @Override
    public List<String> getRequiredDocuments() {
        return Arrays.asList(
            "Income Statement",
            "Tax Returns",
            "Bank Statements",
            "Employment Verification"
        );
    }

    @Override
    public boolean validateEligibility(Customer customer) {
        return customer.getAnnualIncome().compareTo(getMinimumIncomeRequired()) >= 0 &&
               customer.getCreditScore() >= 600 &&
               customer.getEmploymentStatus() == EmploymentStatus.EMPLOYED;
    }

    // getters and setters
}

@Entity
@Table(name = "mortgage_loans")
public class MortgageLoan extends LoanProduct {

    @Column(name = "maximum_ltv_ratio", precision = 5, scale = 4)
    private BigDecimal maximumLtvRatio; // Loan-to-Value ratio

    @Column(name = "maximum_term_years")
    private Integer maximumTermYears;

    @Column(name = "property_type")
    @Enumerated(EnumType.STRING)
    private PropertyType propertyType;

    @Column(name = "requires_pmi")
    private Boolean requiresPmi; // Private Mortgage Insurance

    @Override
    public BigDecimal calculateInterestRate(Customer customer) {
        BigDecimal baseRate = getBaseInterestRate();

        // Adjust based on loan-to-value ratio and credit score
        BigDecimal ltvAdjustment = calculateLtvAdjustment(customer);
        BigDecimal creditAdjustment = calculateCreditAdjustment(customer);

        return baseRate.add(ltvAdjustment).add(creditAdjustment);
    }

    @Override
    public List<String> getRequiredDocuments() {
        return Arrays.asList(
            "Income Verification",
            "Tax Returns",
            "Property Appraisal",
            "Title Search",
            "Home Inspection Report",
            "Insurance Documentation",
            "Down Payment Verification"
        );
    }

    @Override
    public boolean validateEligibility(Customer customer) {
        BigDecimal monthlyIncome = customer.getMonthlyIncome();
        BigDecimal debtToIncomeRatio = customer.calculateDebtToIncomeRatio();

        return customer.getCreditScore() >= 620 &&
               debtToIncomeRatio.compareTo(new BigDecimal("0.43")) <= 0 &&
               monthlyIncome.compareTo(new BigDecimal("3000")) >= 0;
    }

    private BigDecimal calculateLtvAdjustment(Customer customer) {
        // Higher LTV = higher rate
        BigDecimal ltv = customer.getRequestedLoanToValueRatio();
        if (ltv.compareTo(new BigDecimal("0.95")) > 0) {
            return new BigDecimal("0.75");
        } else if (ltv.compareTo(new BigDecimal("0.90")) > 0) {
            return new BigDecimal("0.50");
        } else if (ltv.compareTo(new BigDecimal("0.80")) > 0) {
            return new BigDecimal("0.25");
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal calculateCreditAdjustment(Customer customer) {
        // Credit score adjustment
        int creditScore = customer.getCreditScore();
        if (creditScore >= 760) {
            return BigDecimal.ZERO;
        } else if (creditScore >= 700) {
            return new BigDecimal("0.25");
        } else if (creditScore >= 660) {
            return new BigDecimal("0.50");
        } else {
            return new BigDecimal("1.00");
        }
    }

    // getters and setters
}

// Supporting enums
public enum PropertyType {
    SINGLE_FAMILY, CONDO, TOWNHOUSE, MULTI_FAMILY, INVESTMENT
}

public enum EmploymentStatus {
    EMPLOYED, SELF_EMPLOYED, UNEMPLOYED, RETIRED
}
```

**Inheritance Strategy Comparison:**

| Strategy | Use Case | Pros | Cons |
|----------|----------|------|------|
| Single Table | Simple hierarchies, few differences | Fast queries, simple schema | Nullable columns, large tables |
| Joined Table | Complex hierarchies, many fields | Normalized, flexible | Multiple joins, slower queries |
| Table Per Class | Independent entities, no polymorphic queries | Simple queries, no joins | Duplicate columns, complex polymorphic queries |

---

## 📊 Repository Pattern

*[Continue with remaining sections...]*

The documentation continues with comprehensive coverage of Repository patterns, Transaction Management, Performance Optimization, and Banking-specific use cases, following the same detailed format with practical examples and best practices.