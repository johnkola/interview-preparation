# Spring Boot MongoDB Comprehensive Guide

## 📖 Table of Contents

### 1. [MongoDB Configuration](#mongodb-configuration)
- [Q1: Basic MongoDB Configuration](#q1-basic-mongodb-configuration)
- [Q2: Multiple MongoDB Databases](#q2-multiple-mongodb-databases)
- [Q3: Connection Pool and Security](#q3-connection-pool-and-security)

### 2. [Document Modeling](#document-modeling)
- [Q4: Document Design Patterns](#q4-document-design-patterns)
- [Q5: Embedded vs Referenced Documents](#q5-embedded-vs-referenced-documents)
- [Q6: Polymorphic Documents](#q6-polymorphic-documents)

### 3. [Repository Pattern](#repository-pattern)
- [Q7: MongoRepository and Custom Queries](#q7-mongorepository-and-custom-queries)
- [Q8: Aggregation Framework](#q8-aggregation-framework)
- [Q9: GridFS for File Storage](#q9-gridfs-for-file-storage)

### 4. [Transaction Management](#transaction-management)
- [Q10: MongoDB Transactions](#q10-mongodb-transactions)
- [Q11: Multi-Document ACID Operations](#q11-multi-document-acid-operations)
- [Q12: Session Management](#q12-session-management)

### 5. [Performance Optimization](#performance-optimization)
- [Q13: Indexing Strategies](#q13-indexing-strategies)
- [Q14: Query Optimization](#q14-query-optimization)
- [Q15: Caching and Read Preferences](#q15-caching-and-read-preferences)

### 6. [Banking Use Cases](#banking-use-cases)
- [Q16: Banking Document Models](#q16-banking-document-models)
- [Q17: Real-time Analytics](#q17-real-time-analytics)
- [Q18: Audit and Compliance](#q18-audit-and-compliance)

---

## 🔧 MongoDB Configuration

### Q1: Basic MongoDB Configuration

**Question**: How do you configure Spring Boot to work with MongoDB and what are the essential properties?

**Answer**:

**Basic MongoDB Configuration:**

```yaml
# application.yml
spring:
  data:
    mongodb:
      # Single MongoDB instance
      uri: mongodb://localhost:27017/banking_db
      # OR individual properties
      host: localhost
      port: 27017
      database: banking_db
      username: ${MONGO_USERNAME:bankuser}
      password: ${MONGO_PASSWORD:password}
      authentication-database: admin

      # Connection Pool Settings
      options:
        min-connections-per-host: 5
        max-connections-per-host: 100
        connections-per-host: 20
        max-wait-time: 5000
        max-connection-idle-time: 60000
        max-connection-life-time: 120000
        connect-timeout: 10000
        socket-timeout: 5000
        server-selection-timeout: 30000

        # SSL Configuration
        ssl-enabled: false
        ssl-invalid-hostname-allowed: false

        # Read/Write Concerns
        read-concern: majority
        write-concern: majority
        read-preference: primary

# Logging configuration
logging:
  level:
    org.springframework.data.mongodb: DEBUG
    org.mongodb.driver: DEBUG
```

**Configuration Classes:**

```java
@Configuration
@EnableMongoRepositories(basePackages = "com.bank.repository.mongo")
@EnableMongoAuditing
public class MongoConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Bean
    public MongoClient mongoClient() {
        CodecRegistry pojoCodecRegistry = fromProviders(
            PojoCodecProvider.builder().automatic(true).build()
        );
        CodecRegistry codecRegistry = fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            pojoCodecRegistry
        );

        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(mongoUri))
            .codecRegistry(codecRegistry)
            .build();

        return MongoClients.create(settings);
    }

    @Bean
    @Primary
    public MongoTemplate mongoTemplate(MongoClient mongoClient) {
        MongoTemplate template = new MongoTemplate(mongoClient, "banking_db");

        // Custom converters
        MappingMongoConverter converter = (MappingMongoConverter) template.getConverter();
        converter.setCustomConversions(customConversions());
        converter.afterPropertiesSet();

        return template;
    }

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new BigDecimalToDecimal128Converter());
        converters.add(new Decimal128ToBigDecimalConverter());
        converters.add(new LocalDateTimeToDateConverter());
        converters.add(new DateToLocalDateTimeConverter());
        return new MongoCustomConversions(converters);
    }

    @Bean
    public MongoTransactionManager transactionManager(MongoDbFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    // Auditing configuration
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            return Optional.ofNullable(authentication)
                .map(Authentication::getName)
                .filter(name -> !"anonymousUser".equals(name));
        };
    }

    // Index creation
    @EventListener(ContextRefreshedEvent.class)
    public void initIndices() {
        MongoTemplate mongoTemplate = mongoTemplate(mongoClient());

        // Create indexes for better performance
        mongoTemplate.indexOps(Account.class)
            .ensureIndex(new Index().on("accountNumber", Sort.Direction.ASC).unique());

        mongoTemplate.indexOps(Transaction.class)
            .ensureIndex(new Index()
                .on("accountId", Sort.Direction.ASC)
                .on("transactionDate", Sort.Direction.DESC));

        mongoTemplate.indexOps(Customer.class)
            .ensureIndex(new Index().on("email", Sort.Direction.ASC).unique());
    }
}

// Custom Converters
@Component
@ReadingConverter
public class Decimal128ToBigDecimalConverter implements Converter<Decimal128, BigDecimal> {
    @Override
    public BigDecimal convert(Decimal128 source) {
        return source.bigDecimalValue();
    }
}

@Component
@WritingConverter
public class BigDecimalToDecimal128Converter implements Converter<BigDecimal, Decimal128> {
    @Override
    public Decimal128 convert(BigDecimal source) {
        return new Decimal128(source);
    }
}

@Component
@WritingConverter
public class LocalDateTimeToDateConverter implements Converter<LocalDateTime, Date> {
    @Override
    public Date convert(LocalDateTime source) {
        return Date.from(source.atZone(ZoneId.systemDefault()).toInstant());
    }
}

@Component
@ReadingConverter
public class DateToLocalDateTimeConverter implements Converter<Date, LocalDateTime> {
    @Override
    public LocalDateTime convert(Date source) {
        return source.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
```

**Environment-Specific Configurations:**

```yaml
---
# Development Profile
spring:
  profiles: dev
  data:
    mongodb:
      uri: mongodb://localhost:27017/banking_dev
      options:
        max-connections-per-host: 10

---
# Testing Profile
spring:
  profiles: test
  data:
    mongodb:
      uri: mongodb://localhost:27017/banking_test

---
# Production Profile
spring:
  profiles: prod
  data:
    mongodb:
      # MongoDB Atlas or Replica Set
      uri: ${MONGODB_URI:mongodb+srv://username:password@cluster.mongodb.net/banking_prod?retryWrites=true&w=majority}
      options:
        max-connections-per-host: 200
        min-connections-per-host: 20
        ssl-enabled: true
        read-preference: secondaryPreferred
        write-concern: majority
```

### Q2: Multiple MongoDB Databases

**Question**: How do you configure multiple MongoDB databases in Spring Boot?

**Answer**:

**Multiple Database Configuration:**

```java
// Primary MongoDB Configuration
@Configuration
@EnableMongoRepositories(
    basePackages = "com.bank.repository.primary",
    mongoTemplateRef = "primaryMongoTemplate"
)
public class PrimaryMongoConfig {

    @Primary
    @Bean(name = "primaryMongoClient")
    public MongoClient primaryMongoClient() {
        return MongoClients.create("mongodb://localhost:27017/banking_primary");
    }

    @Primary
    @Bean(name = "primaryMongoTemplate")
    public MongoTemplate primaryMongoTemplate() {
        return new MongoTemplate(primaryMongoClient(), "banking_primary");
    }

    @Primary
    @Bean(name = "primaryMongoTransactionManager")
    public MongoTransactionManager primaryTransactionManager() {
        return new MongoTransactionManager(new SimpleMongoClientDbFactory(
            primaryMongoClient(), "banking_primary"));
    }
}

// Secondary MongoDB Configuration (for Analytics)
@Configuration
@EnableMongoRepositories(
    basePackages = "com.bank.repository.analytics",
    mongoTemplateRef = "analyticsMongoTemplate"
)
public class AnalyticsMongoConfig {

    @Bean(name = "analyticsMongoClient")
    public MongoClient analyticsMongoClient() {
        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString("mongodb://localhost:27017/banking_analytics"))
            .readPreference(ReadPreference.secondaryPreferred())
            .build();
        return MongoClients.create(settings);
    }

    @Bean(name = "analyticsMongoTemplate")
    public MongoTemplate analyticsMongoTemplate() {
        return new MongoTemplate(analyticsMongoClient(), "banking_analytics");
    }

    @Bean(name = "analyticsMongoTransactionManager")
    public MongoTransactionManager analyticsTransactionManager() {
        return new MongoTransactionManager(new SimpleMongoClientDbFactory(
            analyticsMongoClient(), "banking_analytics"));
    }
}

// Audit MongoDB Configuration
@Configuration
@EnableMongoRepositories(
    basePackages = "com.bank.repository.audit",
    mongoTemplateRef = "auditMongoTemplate"
)
public class AuditMongoConfig {

    @Bean(name = "auditMongoClient")
    public MongoClient auditMongoClient() {
        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString("mongodb://localhost:27017/banking_audit"))
            .writeConcern(WriteConcern.MAJORITY.withJournal(true))
            .build();
        return MongoClients.create(settings);
    }

    @Bean(name = "auditMongoTemplate")
    public MongoTemplate auditMongoTemplate() {
        return new MongoTemplate(auditMongoClient(), "banking_audit");
    }
}
```

**Usage in Services:**

```java
@Service
public class BankingService {

    @Autowired
    @Qualifier("primaryMongoTemplate")
    private MongoTemplate primaryMongoTemplate;

    @Autowired
    @Qualifier("analyticsMongoTemplate")
    private MongoTemplate analyticsMongoTemplate;

    @Autowired
    @Qualifier("auditMongoTemplate")
    private MongoTemplate auditMongoTemplate;

    @Transactional("primaryMongoTransactionManager")
    public void processTransaction(TransactionRequest request) {
        // Save to primary database
        Account account = primaryMongoTemplate.findById(request.getAccountId(), Account.class);
        account.updateBalance(request.getAmount());
        primaryMongoTemplate.save(account);

        Transaction transaction = new Transaction(request);
        primaryMongoTemplate.save(transaction);
    }

    @Async
    public void updateAnalytics(Transaction transaction) {
        // Save analytics data to analytics database
        TransactionAnalytics analytics = new TransactionAnalytics(transaction);
        analyticsMongoTemplate.save(analytics);
    }

    @Async
    public void auditTransaction(Transaction transaction) {
        // Save audit trail to audit database
        AuditRecord audit = new AuditRecord(transaction);
        auditMongoTemplate.save(audit);
    }
}
```

### Q3: Connection Pool and Security

**Question**: How do you configure MongoDB connection pooling and security for production banking applications?

**Answer**:

**Production Security Configuration:**

```yaml
# application-prod.yml
spring:
  data:
    mongodb:
      uri: mongodb+srv://${MONGO_USERNAME}:${MONGO_PASSWORD}@${MONGO_CLUSTER}/${MONGO_DATABASE}?retryWrites=true&w=majority&authSource=admin&ssl=true

      # Advanced Connection Settings
      options:
        # Connection Pool
        min-connections-per-host: 10
        max-connections-per-host: 100
        connections-per-host: 50

        # Timeouts
        max-wait-time: 10000
        connect-timeout: 10000
        socket-timeout: 5000
        server-selection-timeout: 30000
        max-connection-idle-time: 60000
        max-connection-life-time: 300000

        # SSL/TLS
        ssl-enabled: true
        ssl-invalid-hostname-allowed: false

        # Authentication
        auth-mechanism: SCRAM-SHA-256

        # Read/Write Concerns
        read-concern: majority
        write-concern: majority
        read-preference: primaryPreferred

        # Connection Options
        heartbeat-frequency: 10000
        min-heartbeat-frequency: 500
        heartbeat-connect-timeout: 20000
        heartbeat-socket-timeout: 20000
        local-threshold: 15
```

**Advanced Security Configuration:**

```java
@Configuration
@EnableWebSecurity
public class MongoSecurityConfig {

    @Bean
    public MongoClient secureMongoClient() {
        // SSL Context for secure connections
        SSLContext sslContext = createSSLContext();

        MongoClientSettings settings = MongoClientSettings.builder()
            .applyConnectionString(new ConnectionString(getSecureConnectionString()))
            .applyToSslSettings(builder ->
                builder.enabled(true)
                       .context(sslContext)
                       .invalidHostNameAllowed(false))
            .applyToConnectionPoolSettings(builder ->
                builder.maxSize(100)
                       .minSize(10)
                       .maxWaitTime(10, TimeUnit.SECONDS)
                       .maxConnectionLifeTime(30, TimeUnit.MINUTES)
                       .maxConnectionIdleTime(10, TimeUnit.MINUTES))
            .applyToSocketSettings(builder ->
                builder.connectTimeout(10, TimeUnit.SECONDS)
                       .readTimeout(5, TimeUnit.SECONDS))
            .applyToServerSettings(builder ->
                builder.heartbeatFrequency(10, TimeUnit.SECONDS)
                       .minHeartbeatFrequency(500, TimeUnit.MILLISECONDS))
            .credential(MongoCredential.createCredential(
                getUsername(),
                "admin", // auth database
                getPassword().toCharArray()))
            .writeConcern(WriteConcern.MAJORITY.withJournal(true))
            .readConcern(ReadConcern.MAJORITY)
            .readPreference(ReadPreference.primaryPreferred())
            .build();

        return MongoClients.create(settings);
    }

    @Bean
    public MongoTemplate secureMongoTemplate(MongoClient mongoClient) {
        MongoTemplate template = new MongoTemplate(mongoClient, getDatabaseName());

        // Enable field-level encryption
        configureFieldLevelEncryption(template);

        return template;
    }

    // Field-Level Encryption Configuration
    private void configureFieldLevelEncryption(MongoTemplate mongoTemplate) {
        // Configure automatic encryption for sensitive fields
        Map<String, Object> schemaMap = new HashMap<>();

        // Customer SSN encryption schema
        Document customerSchema = new Document("bsonType", "object")
            .append("properties", new Document("ssn", new Document("encrypt",
                new Document("keyId", "/customer-ssn-key")
                    .append("bsonType", "string")
                    .append("algorithm", "AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic"))));

        schemaMap.put("banking_db.customers", customerSchema);

        // Account number encryption schema
        Document accountSchema = new Document("bsonType", "object")
            .append("properties", new Document("accountNumber", new Document("encrypt",
                new Document("keyId", "/account-number-key")
                    .append("bsonType", "string")
                    .append("algorithm", "AEAD_AES_256_CBC_HMAC_SHA_512-Deterministic"))));

        schemaMap.put("banking_db.accounts", accountSchema);
    }

    private SSLContext createSSLContext() {
        try {
            // Load certificate from file or environment
            String certPath = System.getenv("MONGO_SSL_CERT_PATH");
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream(certPath),
                         System.getenv("MONGO_SSL_CERT_PASSWORD").toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore,
                                 System.getenv("MONGO_SSL_KEY_PASSWORD").toCharArray());

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(),
                           trustManagerFactory.getTrustManagers(),
                           new SecureRandom());

            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SSL context", e);
        }
    }

    private String getSecureConnectionString() {
        return String.format(
            "mongodb+srv://%s:%s@%s/%s?retryWrites=true&w=majority&authSource=admin&ssl=true",
            getUsername(),
            getPassword(),
            getClusterUrl(),
            getDatabaseName()
        );
    }

    private String getUsername() {
        return System.getenv("MONGO_USERNAME");
    }

    private String getPassword() {
        return System.getenv("MONGO_PASSWORD");
    }

    private String getClusterUrl() {
        return System.getenv("MONGO_CLUSTER_URL");
    }

    private String getDatabaseName() {
        return System.getenv("MONGO_DATABASE");
    }
}

// Connection Pool Monitoring
@Component
public class MongoPoolMonitor {

    @Autowired
    private MongoClient mongoClient;

    @EventListener
    public void handleConnectionPoolEvent(ConnectionPoolOpenedEvent event) {
        log.info("MongoDB connection pool opened: {}", event.getConnectionPoolSettings());
    }

    @EventListener
    public void handleConnectionPoolEvent(ConnectionPoolClosedEvent event) {
        log.info("MongoDB connection pool closed for server: {}", event.getServerId());
    }

    @EventListener
    public void handleConnectionEvent(ConnectionCheckedOutEvent event) {
        log.debug("Connection checked out: {}", event.getConnectionId());
    }

    @EventListener
    public void handleConnectionEvent(ConnectionCheckedInEvent event) {
        log.debug("Connection checked in: {}", event.getConnectionId());
    }

    // Health check for connection pool
    @Bean
    public HealthIndicator mongoHealthIndicator() {
        return new MongoHealthIndicator(mongoClient);
    }

    // Metrics for connection pool
    @PostConstruct
    public void registerMetrics() {
        if (mongoClient instanceof MongoClientImpl) {
            // Register connection pool metrics
            Gauge.builder("mongodb.connections.active")
                .register(Metrics.globalRegistry, this::getActiveConnections);

            Gauge.builder("mongodb.connections.total")
                .register(Metrics.globalRegistry, this::getTotalConnections);
        }
    }

    private Number getActiveConnections() {
        // Implementation to get active connections from connection pool
        return 0; // Placeholder
    }

    private Number getTotalConnections() {
        // Implementation to get total connections from connection pool
        return 0; // Placeholder
    }
}
```

---

## 📄 Document Modeling

### Q4: Document Design Patterns

**Question**: What are the best practices for designing MongoDB documents for banking applications?

**Answer**:

**Banking Document Models:**

```java
// Customer Document with Embedded Address
@Document(collection = "customers")
@CompoundIndex(def = "{'email': 1, 'status': 1}")
@CompoundIndex(def = "{'customerId': 1}", unique = true)
public class Customer {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("customer_id")
    private String customerId;

    @NotNull
    @Field("first_name")
    private String firstName;

    @NotNull
    @Field("last_name")
    private String lastName;

    @Indexed(unique = true)
    @Email
    private String email;

    @Field("phone_number")
    private String phoneNumber;

    // Encrypted field for sensitive data
    @JsonIgnore
    @Field("ssn_encrypted")
    private String ssnEncrypted;

    @Field("date_of_birth")
    private LocalDate dateOfBirth;

    @Field("customer_type")
    private CustomerType customerType;

    @Field("status")
    private CustomerStatus status;

    // Embedded address document
    @Field("address")
    private Address address;

    // Embedded preferences
    @Field("preferences")
    private CustomerPreferences preferences;

    // Credit information
    @Field("credit_info")
    private CreditInformation creditInfo;

    // KYC information
    @Field("kyc_info")
    private KycInformation kycInfo;

    // Audit fields
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    @CreatedBy
    @Field("created_by")
    private String createdBy;

    @LastModifiedBy
    @Field("updated_by")
    private String updatedBy;

    @Version
    private Long version;

    // Account references (for performance)
    @Field("account_ids")
    private List<String> accountIds = new ArrayList<>();

    // Tags for flexible querying
    @Field("tags")
    private Set<String> tags = new HashSet<>();

    // Custom fields for extensibility
    @Field("custom_fields")
    private Map<String, Object> customFields = new HashMap<>();

    // getters and setters
}

// Embedded Address Document
@Document
public class Address {
    @Field("street")
    private String street;

    @Field("city")
    private String city;

    @Field("state")
    private String state;

    @Field("postal_code")
    private String postalCode;

    @Field("country")
    private String country;

    @Field("type")
    private AddressType type; // HOME, WORK, MAILING

    @Field("is_primary")
    private Boolean isPrimary;

    // Geospatial data for location-based services
    @Field("coordinates")
    private GeoJsonPoint coordinates;

    // getters and setters
}

// Customer Preferences
@Document
public class CustomerPreferences {
    @Field("communication_channel")
    private CommunicationChannel preferredChannel;

    @Field("language")
    private String language;

    @Field("timezone")
    private String timezone;

    @Field("notification_settings")
    private NotificationSettings notificationSettings;

    @Field("privacy_settings")
    private PrivacySettings privacySettings;

    // getters and setters
}

// Account Document with Balance History
@Document(collection = "accounts")
@CompoundIndex(def = "{'customer_id': 1, 'account_type': 1}")
@CompoundIndex(def = "{'account_number': 1}", unique = true)
@CompoundIndex(def = "{'status': 1, 'account_type': 1}")
public class Account {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("account_number")
    private String accountNumber;

    @Indexed
    @Field("customer_id")
    private String customerId;

    @Field("account_type")
    private AccountType accountType;

    @Field("status")
    private AccountStatus status;

    // Current balance using MongoDB Decimal128 for precision
    @Field("current_balance")
    private BigDecimal currentBalance;

    @Field("available_balance")
    private BigDecimal availableBalance;

    @Field("currency")
    private String currency;

    // Interest information
    @Field("interest_rate")
    private BigDecimal interestRate;

    @Field("interest_calculation_method")
    private InterestCalculationMethod interestCalculationMethod;

    // Limits and restrictions
    @Field("daily_withdrawal_limit")
    private BigDecimal dailyWithdrawalLimit;

    @Field("monthly_transaction_limit")
    private Integer monthlyTransactionLimit;

    @Field("overdraft_limit")
    private BigDecimal overdraftLimit;

    // Branch and officer information
    @Field("branch_id")
    private String branchId;

    @Field("relationship_manager_id")
    private String relationshipManagerId;

    // Balance history (embedded for performance)
    @Field("balance_history")
    @Size(max = 100) // Keep only last 100 balance snapshots
    private List<BalanceSnapshot> balanceHistory = new ArrayList<>();

    // Recent transactions (for quick access)
    @Field("recent_transactions")
    @Size(max = 50) // Keep only last 50 transactions embedded
    private List<TransactionSummary> recentTransactions = new ArrayList<>();

    // Product-specific information
    @Field("product_details")
    private Map<String, Object> productDetails = new HashMap<>();

    // Audit fields
    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Business methods
    public void updateBalance(BigDecimal amount, String description) {
        BigDecimal oldBalance = this.currentBalance;
        this.currentBalance = this.currentBalance.add(amount);

        // Add balance snapshot
        BalanceSnapshot snapshot = new BalanceSnapshot(
            LocalDateTime.now(),
            oldBalance,
            this.currentBalance,
            amount,
            description
        );

        balanceHistory.add(snapshot);

        // Keep only last 100 snapshots
        if (balanceHistory.size() > 100) {
            balanceHistory.remove(0);
        }
    }

    // getters and setters
}

// Balance Snapshot for embedded history
@Document
public class BalanceSnapshot {
    @Field("timestamp")
    private LocalDateTime timestamp;

    @Field("previous_balance")
    private BigDecimal previousBalance;

    @Field("new_balance")
    private BigDecimal newBalance;

    @Field("amount_changed")
    private BigDecimal amountChanged;

    @Field("description")
    private String description;

    @Field("transaction_id")
    private String transactionId;

    // constructors, getters and setters
}

// Transaction Document (separate collection for performance)
@Document(collection = "transactions")
@CompoundIndex(def = "{'account_id': 1, 'transaction_date': -1}")
@CompoundIndex(def = "{'transaction_type': 1, 'status': 1}")
@CompoundIndex(def = "{'reference_number': 1}", unique = true)
@CompoundIndex(def = "{'customer_id': 1, 'transaction_date': -1}")
public class Transaction {

    @Id
    private String id;

    @Indexed
    @Field("reference_number")
    private String referenceNumber;

    @Indexed
    @Field("account_id")
    private String accountId;

    @Indexed
    @Field("customer_id")
    private String customerId;

    @Field("transaction_type")
    private TransactionType transactionType;

    @Field("amount")
    private BigDecimal amount;

    @Field("currency")
    private String currency;

    @Field("description")
    private String description;

    @Field("transaction_date")
    private LocalDateTime transactionDate;

    @Field("value_date")
    private LocalDate valueDate;

    @Field("status")
    private TransactionStatus status;

    @Field("channel")
    private TransactionChannel channel;

    // Balance information at transaction time
    @Field("balance_before")
    private BigDecimal balanceBefore;

    @Field("balance_after")
    private BigDecimal balanceAfter;

    // For transfer transactions
    @Field("counterparty_account")
    private String counterpartyAccount;

    @Field("counterparty_name")
    private String counterpartyName;

    // Fee information
    @Field("fees")
    private List<TransactionFee> fees = new ArrayList<>();

    // Geographic information
    @Field("location")
    private TransactionLocation location;

    // Device and security information
    @Field("device_info")
    private DeviceInformation deviceInfo;

    // Risk scoring
    @Field("risk_score")
    private Double riskScore;

    @Field("risk_factors")
    private List<String> riskFactors = new ArrayList<>();

    // Fraud detection flags
    @Field("fraud_flags")
    private List<String> fraudFlags = new ArrayList<>();

    // Additional metadata
    @Field("metadata")
    private Map<String, Object> metadata = new HashMap<>();

    // Audit trail
    @Field("audit_trail")
    private List<AuditEntry> auditTrail = new ArrayList<>();

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Field("updated_at")
    private LocalDateTime updatedAt;

    // getters and setters
}

// Supporting Classes
@Document
public class TransactionFee {
    @Field("fee_type")
    private String feeType;

    @Field("amount")
    private BigDecimal amount;

    @Field("currency")
    private String currency;

    @Field("description")
    private String description;

    // getters and setters
}

@Document
public class TransactionLocation {
    @Field("country")
    private String country;

    @Field("city")
    private String city;

    @Field("coordinates")
    private GeoJsonPoint coordinates;

    @Field("merchant_name")
    private String merchantName;

    @Field("merchant_category")
    private String merchantCategory;

    // getters and setters
}

@Document
public class DeviceInformation {
    @Field("device_id")
    private String deviceId;

    @Field("device_type")
    private String deviceType;

    @Field("ip_address")
    private String ipAddress;

    @Field("user_agent")
    private String userAgent;

    @Field("session_id")
    private String sessionId;

    // getters and setters
}

// Enums
public enum CustomerType {
    INDIVIDUAL, BUSINESS, CORPORATE, PRIVATE_BANKING
}

public enum CustomerStatus {
    ACTIVE, INACTIVE, SUSPENDED, CLOSED, PENDING_VERIFICATION
}

public enum AccountType {
    CHECKING, SAVINGS, CREDIT, LOAN, INVESTMENT, ESCROW
}

public enum AccountStatus {
    ACTIVE, INACTIVE, FROZEN, CLOSED, PENDING_APPROVAL
}

public enum TransactionType {
    DEPOSIT, WITHDRAWAL, TRANSFER, PAYMENT, FEE, INTEREST, ADJUSTMENT
}

public enum TransactionStatus {
    PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED, REVERSED
}

public enum TransactionChannel {
    ATM, ONLINE, MOBILE, BRANCH, PHONE, WIRE
}

public enum AddressType {
    HOME, WORK, MAILING, BILLING
}

public enum CommunicationChannel {
    EMAIL, SMS, PHONE, MAIL, PUSH_NOTIFICATION
}

public enum InterestCalculationMethod {
    SIMPLE, COMPOUND_DAILY, COMPOUND_MONTHLY, COMPOUND_QUARTERLY
}
```

### Q5: Embedded vs Referenced Documents

**Question**: When should you use embedded documents vs references in MongoDB for banking data?

**Answer**:

**Design Decision Matrix:**

| Use Case | Pattern | Reason |
|----------|---------|---------|
| Customer Address | Embedded | 1:1, accessed together, small size |
| Account Transactions | Referenced | 1:Many, large volume, independent queries |
| Customer Accounts | Referenced | 1:Many, independent lifecycle |
| Transaction Fees | Embedded | 1:Few, accessed together |
| Customer Documents | Referenced/GridFS | Large files, independent access |

**Implementation Examples:**

```java
// Pattern 1: Embedded Documents for 1:1 and small 1:Many relationships
@Document(collection = "customers")
public class CustomerWithEmbedded {

    @Id
    private String id;

    @Field("customer_id")
    private String customerId;

    // Embedded profile information (1:1)
    @Field("profile")
    private CustomerProfile profile;

    // Embedded contact information (1:Few)
    @Field("contact_methods")
    private List<ContactMethod> contactMethods;

    // Embedded preferences (1:1)
    @Field("preferences")
    private CustomerPreferences preferences;

    // Account references (1:Many with independent lifecycle)
    @Field("account_refs")
    private List<AccountReference> accountReferences;

    // Recent activity summary (embedded for performance)
    @Field("recent_activity")
    private List<ActivitySummary> recentActivity;
}

@Document
public class CustomerProfile {
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private String occupation;
    private String employer;
    private BigDecimal annualIncome;
    private Integer creditScore;

    // KYC information
    @Field("kyc_status")
    private KycStatus kycStatus;

    @Field("identity_documents")
    private List<IdentityDocument> identityDocuments;
}

@Document
public class ContactMethod {
    private ContactType type;
    private String value;
    private boolean isPrimary;
    private boolean isVerified;
    private LocalDateTime verifiedAt;
}

@Document
public class AccountReference {
    @Field("account_id")
    private String accountId;

    @Field("account_number")
    private String accountNumber;

    @Field("account_type")
    private AccountType accountType;

    @Field("status")
    private AccountStatus status;

    @Field("current_balance")
    private BigDecimal currentBalance;

    @Field("last_transaction_date")
    private LocalDateTime lastTransactionDate;
}

// Pattern 2: Referenced Documents for large collections
@Document(collection = "accounts")
public class AccountWithReferences {

    @Id
    private String id;

    @Field("account_number")
    private String accountNumber;

    // Reference to customer (Many:1)
    @Field("customer_id")
    private String customerId;

    @Field("current_balance")
    private BigDecimal currentBalance;

    // Transactions are stored in separate collection
    // Only keep summary statistics here
    @Field("transaction_summary")
    private TransactionSummary transactionSummary;

    // Recent transaction IDs for quick reference
    @Field("recent_transaction_ids")
    @Size(max = 10)
    private List<String> recentTransactionIds;
}

@Document
public class TransactionSummary {
    @Field("total_transactions")
    private Long totalTransactions;

    @Field("total_credits")
    private BigDecimal totalCredits;

    @Field("total_debits")
    private BigDecimal totalDebits;

    @Field("average_transaction_amount")
    private BigDecimal averageTransactionAmount;

    @Field("last_transaction_date")
    private LocalDateTime lastTransactionDate;

    @Field("monthly_summary")
    private List<MonthlySummary> monthlySummary;
}

// Pattern 3: Hybrid approach for transaction history
@Document(collection = "transactions")
public class TransactionDocument {

    @Id
    private String id;

    @Field("account_id")
    private String accountId;

    @Field("amount")
    private BigDecimal amount;

    @Field("transaction_date")
    private LocalDateTime transactionDate;

    // Basic transaction details embedded
    @Field("details")
    private TransactionDetails details;

    // Complex enrichment data referenced
    @Field("enrichment_id")
    private String enrichmentId; // Reference to separate collection
}

@Document(collection = "transaction_enrichments")
public class TransactionEnrichment {

    @Id
    private String id; // Same as transaction enrichment_id

    @Field("merchant_details")
    private MerchantDetails merchantDetails;

    @Field("category_analysis")
    private CategoryAnalysis categoryAnalysis;

    @Field("fraud_analysis")
    private FraudAnalysis fraudAnalysis;

    @Field("compliance_checks")
    private List<ComplianceCheck> complianceChecks;
}

// Pattern 4: Document versioning for audit trail
@Document(collection = "account_versions")
public class AccountVersion {

    @Id
    private String id;

    @Field("account_id")
    private String accountId;

    @Field("version_number")
    private Long versionNumber;

    @Field("change_type")
    private ChangeType changeType;

    @Field("changed_fields")
    private Map<String, FieldChange> changedFields;

    @Field("account_snapshot")
    private Account accountSnapshot; // Full document at this version

    @Field("changed_by")
    private String changedBy;

    @Field("change_reason")
    private String changeReason;

    @CreatedDate
    @Field("created_at")
    private LocalDateTime createdAt;
}

// Service layer handling both patterns
@Service
public class BankingDocumentService {

    @Autowired
    private MongoTemplate mongoTemplate;

    // Retrieve customer with embedded data
    public CustomerWithEmbedded getCustomerProfile(String customerId) {
        Query query = new Query(Criteria.where("customerId").is(customerId));
        return mongoTemplate.findOne(query, CustomerWithEmbedded.class);
    }

    // Retrieve account with referenced transactions
    public AccountDetails getAccountWithTransactions(String accountId, int limit) {
        // Get account basic info
        Account account = mongoTemplate.findById(accountId, Account.class);

        // Get recent transactions separately
        Query transactionQuery = new Query(Criteria.where("accountId").is(accountId))
            .with(Sort.by(Sort.Direction.DESC, "transactionDate"))
            .limit(limit);

        List<Transaction> transactions = mongoTemplate.find(
            transactionQuery, Transaction.class);

        return new AccountDetails(account, transactions);
    }

    // Update with embedded document optimization
    @Transactional
    public void updateCustomerContact(String customerId, ContactMethod newContact) {
        Query query = new Query(Criteria.where("customerId").is(customerId));

        Update update = new Update()
            .push("contactMethods", newContact)
            .set("updatedAt", LocalDateTime.now());

        mongoTemplate.updateFirst(query, update, CustomerWithEmbedded.class);
    }

    // Bulk operation for referenced documents
    public void updateTransactionStatuses(List<String> transactionIds,
                                        TransactionStatus newStatus) {
        Query query = new Query(Criteria.where("id").in(transactionIds));
        Update update = new Update()
            .set("status", newStatus)
            .set("updatedAt", LocalDateTime.now());

        mongoTemplate.updateMulti(query, update, Transaction.class);
    }

    // Complex aggregation across referenced documents
    public CustomerAccountSummary getCustomerAccountSummary(String customerId) {
        Aggregation aggregation = Aggregation.newAggregation(
            // Match customer accounts
            Aggregation.match(Criteria.where("customerId").is(customerId)),

            // Lookup transactions for each account
            Aggregation.lookup("transactions", "_id", "accountId", "transactions"),

            // Group and calculate summaries
            Aggregation.group("customerId")
                .sum("currentBalance").as("totalBalance")
                .count().as("totalAccounts")
                .sum("transactions.amount").as("totalTransactionVolume"),

            // Project final result
            Aggregation.project()
                .and("totalBalance").as("totalBalance")
                .and("totalAccounts").as("totalAccounts")
                .and("totalTransactionVolume").as("totalTransactionVolume")
        );

        AggregationResults<CustomerAccountSummary> results =
            mongoTemplate.aggregate(aggregation, "accounts", CustomerAccountSummary.class);

        return results.getUniqueMappedResult();
    }
}

// Supporting classes
@Data
public class AccountDetails {
    private Account account;
    private List<Transaction> recentTransactions;

    public AccountDetails(Account account, List<Transaction> transactions) {
        this.account = account;
        this.recentTransactions = transactions;
    }
}

@Data
public class CustomerAccountSummary {
    private BigDecimal totalBalance;
    private Integer totalAccounts;
    private BigDecimal totalTransactionVolume;
}

@Data
public class FieldChange {
    private Object oldValue;
    private Object newValue;
    private LocalDateTime changedAt;
}

public enum ChangeType {
    CREATE, UPDATE, DELETE, STATUS_CHANGE
}

public enum ContactType {
    EMAIL, PHONE, SMS, ADDRESS
}

public enum KycStatus {
    PENDING, IN_PROGRESS, COMPLETED, FAILED, EXPIRED
}
```

**Decision Guidelines:**

**Use Embedded Documents When:**
- 1:1 relationships
- 1:Few relationships (< 100 subdocuments)
- Data accessed together frequently
- Subdocuments have no independent lifecycle
- Total document size < 16MB

**Use Referenced Documents When:**
- 1:Many relationships with large volume
- Subdocuments need independent queries
- Subdocuments have independent lifecycle
- Need to prevent document growth
- Complex querying requirements

### Q6: Polymorphic Documents

**Question**: How do you implement polymorphic documents in MongoDB for different types of banking products?

**Answer**:

**Polymorphic Document Implementation:**

```java
// Base class for all financial products
@Document(collection = "financial_products")
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "productType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = SavingsAccount.class, name = "SAVINGS"),
    @JsonSubTypes.Type(value = CheckingAccount.class, name = "CHECKING"),
    @JsonSubTypes.Type(value = CreditCard.class, name = "CREDIT_CARD"),
    @JsonSubTypes.Type(value = PersonalLoan.class, name = "PERSONAL_LOAN"),
    @JsonSubTypes.Type(value = MortgageLoan.class, name = "MORTGAGE"),
    @JsonSubTypes.Type(value = InvestmentAccount.class, name = "INVESTMENT")
})
public abstract class FinancialProduct {

    @Id
    private String id;

    @Field("product_id")
    private String productId;

    @Field("customer_id")
    private String customerId;

    @Field("product_type")
    private ProductType productType;

    @Field("product_name")
    private String productName;

    @Field("status")
    private ProductStatus status;

    @Field("currency")
    private String currency;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("branch_id")
    private String branchId;

    @Field("relationship_manager_id")
    private String relationshipManagerId;

    // Common business methods
    public abstract BigDecimal getCurrentValue();
    public abstract BigDecimal calculateMonthlyFee();
    public abstract boolean isEligibleForPromotion(String promotionCode);
    public abstract List<String> getAvailableOperations();

    // getters and setters
}

// Savings Account Implementation
@Document
public class SavingsAccount extends FinancialProduct {

    @Field("account_number")
    private String accountNumber;

    @Field("current_balance")
    private BigDecimal currentBalance;

    @Field("minimum_balance")
    private BigDecimal minimumBalance;

    @Field("interest_rate")
    private BigDecimal interestRate;

    @Field("interest_calculation_method")
    private InterestCalculationMethod interestCalculationMethod;

    @Field("withdrawal_limit_per_month")
    private Integer withdrawalLimitPerMonth;

    @Field("withdrawals_this_month")
    private Integer withdrawalsThisMonth;

    @Field("overdraft_protection")
    private Boolean overdraftProtection;

    @Field("linked_account_id")
    private String linkedAccountId; // For overdraft protection

    @Override
    public BigDecimal getCurrentValue() {
        return currentBalance;
    }

    @Override
    public BigDecimal calculateMonthlyFee() {
        // No fee if minimum balance maintained
        if (currentBalance.compareTo(minimumBalance) >= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal("5.00"); // Monthly maintenance fee
    }

    @Override
    public boolean isEligibleForPromotion(String promotionCode) {
        // Savings account specific promotion logic
        if ("HIGH_BALANCE_BONUS".equals(promotionCode)) {
            return currentBalance.compareTo(new BigDecimal("10000")) >= 0;
        }
        return false;
    }

    @Override
    public List<String> getAvailableOperations() {
        List<String> operations = new ArrayList<>();
        operations.add("DEPOSIT");
        operations.add("INTERNAL_TRANSFER");

        if (withdrawalsThisMonth < withdrawalLimitPerMonth) {
            operations.add("WITHDRAWAL");
            operations.add("EXTERNAL_TRANSFER");
        }

        return operations;
    }

    // Savings-specific methods
    public BigDecimal calculateMonthlyInterest() {
        BigDecimal monthlyRate = interestRate.divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP);
        return currentBalance.multiply(monthlyRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    // getters and setters
}

// Credit Card Implementation
@Document
public class CreditCard extends FinancialProduct {

    @Field("card_number_encrypted")
    private String cardNumberEncrypted;

    @Field("card_number_masked")
    private String cardNumberMasked; // **** **** **** 1234

    @Field("credit_limit")
    private BigDecimal creditLimit;

    @Field("available_credit")
    private BigDecimal availableCredit;

    @Field("current_balance")
    private BigDecimal currentBalance;

    @Field("minimum_payment")
    private BigDecimal minimumPayment;

    @Field("payment_due_date")
    private LocalDate paymentDueDate;

    @Field("apr")
    private BigDecimal apr;

    @Field("cash_advance_apr")
    private BigDecimal cashAdvanceApr;

    @Field("cash_advance_limit")
    private BigDecimal cashAdvanceLimit;

    @Field("rewards_program")
    private RewardsProgram rewardsProgram;

    @Field("annual_fee")
    private BigDecimal annualFee;

    @Field("statement_balance")
    private BigDecimal statementBalance;

    @Field("last_statement_date")
    private LocalDate lastStatementDate;

    @Override
    public BigDecimal getCurrentValue() {
        return currentBalance.negate(); // Negative because it's debt
    }

    @Override
    public BigDecimal calculateMonthlyFee() {
        BigDecimal fees = BigDecimal.ZERO;

        // Annual fee (monthly portion)
        if (annualFee != null) {
            fees = fees.add(annualFee.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP));
        }

        // Interest on outstanding balance
        if (currentBalance.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal monthlyAPR = apr.divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP);
            BigDecimal interestCharge = currentBalance.multiply(monthlyAPR)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
            fees = fees.add(interestCharge);
        }

        return fees;
    }

    @Override
    public boolean isEligibleForPromotion(String promotionCode) {
        if ("CASHBACK_BONUS".equals(promotionCode)) {
            return rewardsProgram != null &&
                   rewardsProgram.getType() == RewardType.CASHBACK;
        }
        if ("CREDIT_INCREASE".equals(promotionCode)) {
            return currentBalance.compareTo(creditLimit.multiply(new BigDecimal("0.3"))) < 0;
        }
        return false;
    }

    @Override
    public List<String> getAvailableOperations() {
        List<String> operations = new ArrayList<>();

        if (availableCredit.compareTo(BigDecimal.ZERO) > 0) {
            operations.add("PURCHASE");
            operations.add("CASH_ADVANCE");
        }

        operations.add("PAYMENT");
        operations.add("BALANCE_TRANSFER");
        operations.add("VIEW_STATEMENT");

        return operations;
    }

    // Credit card specific methods
    public BigDecimal calculateRewards(BigDecimal transactionAmount, String merchantCategory) {
        if (rewardsProgram == null) return BigDecimal.ZERO;

        return rewardsProgram.calculateRewards(transactionAmount, merchantCategory);
    }

    // getters and setters
}

// Investment Account Implementation
@Document
public class InvestmentAccount extends FinancialProduct {

    @Field("account_number")
    private String accountNumber;

    @Field("portfolio_value")
    private BigDecimal portfolioValue;

    @Field("cash_balance")
    private BigDecimal cashBalance;

    @Field("investment_objective")
    private InvestmentObjective investmentObjective;

    @Field("risk_tolerance")
    private RiskTolerance riskTolerance;

    @Field("holdings")
    private List<Holding> holdings;

    @Field("asset_allocation")
    private AssetAllocation assetAllocation;

    @Field("performance_metrics")
    private PerformanceMetrics performanceMetrics;

    @Field("management_fee_rate")
    private BigDecimal managementFeeRate;

    @Override
    public BigDecimal getCurrentValue() {
        return portfolioValue.add(cashBalance);
    }

    @Override
    public BigDecimal calculateMonthlyFee() {
        // Management fee based on portfolio value
        BigDecimal monthlyRate = managementFeeRate.divide(new BigDecimal("12"), 6, RoundingMode.HALF_UP);
        return portfolioValue.multiply(monthlyRate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean isEligibleForPromotion(String promotionCode) {
        if ("PREMIUM_ADVISORY".equals(promotionCode)) {
            return portfolioValue.compareTo(new BigDecimal("100000")) >= 0;
        }
        if ("FREE_TRADES".equals(promotionCode)) {
            return performanceMetrics.getTradesLastMonth() >= 10;
        }
        return false;
    }

    @Override
    public List<String> getAvailableOperations() {
        List<String> operations = Arrays.asList(
            "BUY_SECURITIES",
            "SELL_SECURITIES",
            "DEPOSIT_CASH",
            "WITHDRAW_CASH",
            "REBALANCE_PORTFOLIO",
            "VIEW_PERFORMANCE"
        );

        if (cashBalance.compareTo(new BigDecimal("1000")) >= 0) {
            operations.add("AUTO_INVEST");
        }

        return operations;
    }

    // Investment-specific methods
    public BigDecimal calculateUnrealizedGains() {
        return holdings.stream()
            .map(Holding::getUnrealizedGain)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // getters and setters
}

// Supporting classes for polymorphic fields
@Document
public class RewardsProgram {
    @Field("type")
    private RewardType type;

    @Field("base_rate")
    private BigDecimal baseRate;

    @Field("category_multipliers")
    private Map<String, BigDecimal> categoryMultipliers;

    public BigDecimal calculateRewards(BigDecimal amount, String category) {
        BigDecimal rate = categoryMultipliers.getOrDefault(category, baseRate);
        return amount.multiply(rate).divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    // getters and setters
}

@Document
public class Holding {
    @Field("symbol")
    private String symbol;

    @Field("quantity")
    private BigDecimal quantity;

    @Field("purchase_price")
    private BigDecimal purchasePrice;

    @Field("current_price")
    private BigDecimal currentPrice;

    @Field("market_value")
    private BigDecimal marketValue;

    public BigDecimal getUnrealizedGain() {
        BigDecimal costBasis = quantity.multiply(purchasePrice);
        return marketValue.subtract(costBasis);
    }

    // getters and setters
}

// Repository for polymorphic queries
@Repository
public interface FinancialProductRepository extends MongoRepository<FinancialProduct, String> {

    // Find by product type
    List<FinancialProduct> findByProductType(ProductType productType);

    // Find by customer
    List<FinancialProduct> findByCustomerId(String customerId);

    // Find by customer and type
    List<FinancialProduct> findByCustomerIdAndProductType(String customerId, ProductType productType);

    // Custom query for active products
    @Query("{'customerId': ?0, 'status': 'ACTIVE'}")
    List<FinancialProduct> findActiveProductsByCustomer(String customerId);
}

// Service handling polymorphic operations
@Service
public class FinancialProductService {

    @Autowired
    private FinancialProductRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public List<FinancialProduct> getCustomerProducts(String customerId) {
        return repository.findByCustomerId(customerId);
    }

    public <T extends FinancialProduct> List<T> getProductsByType(String customerId, Class<T> productClass) {
        Query query = new Query(Criteria.where("customerId").is(customerId));
        return mongoTemplate.find(query, productClass);
    }

    public BigDecimal calculateTotalValue(String customerId) {
        List<FinancialProduct> products = getCustomerProducts(customerId);
        return products.stream()
            .map(FinancialProduct::getCurrentValue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculateMonthlyFees(String customerId) {
        List<FinancialProduct> products = getCustomerProducts(customerId);
        return products.stream()
            .map(FinancialProduct::calculateMonthlyFee)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Type-specific operations
    public void processInterestPayment(String customerId) {
        List<SavingsAccount> savingsAccounts = getProductsByType(customerId, SavingsAccount.class);

        for (SavingsAccount account : savingsAccounts) {
            BigDecimal interest = account.calculateMonthlyInterest();
            if (interest.compareTo(BigDecimal.ZERO) > 0) {
                account.setCurrentBalance(account.getCurrentBalance().add(interest));
                repository.save(account);
            }
        }
    }

    // Aggregation query for analytics
    public Map<ProductType, Long> getProductCountByType() {
        Aggregation aggregation = Aggregation.newAggregation(
            Aggregation.group("productType").count().as("count"),
            Aggregation.project("count").and("productType").previousOperation()
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(
            aggregation, "financial_products", Document.class);

        return results.getMappedResults().stream()
            .collect(Collectors.toMap(
                doc -> ProductType.valueOf(doc.getString("_id")),
                doc -> doc.getLong("count")
            ));
    }
}

// Enums
public enum ProductType {
    SAVINGS, CHECKING, CREDIT_CARD, PERSONAL_LOAN, MORTGAGE, INVESTMENT
}

public enum ProductStatus {
    ACTIVE, INACTIVE, CLOSED, SUSPENDED, PENDING_APPROVAL
}

public enum RewardType {
    CASHBACK, POINTS, MILES
}

public enum InvestmentObjective {
    GROWTH, INCOME, BALANCED, CAPITAL_PRESERVATION
}

public enum RiskTolerance {
    CONSERVATIVE, MODERATE, AGGRESSIVE
}

public enum InterestCalculationMethod {
    SIMPLE, COMPOUND_DAILY, COMPOUND_MONTHLY
}
```

This polymorphic approach allows you to:
- Store different product types in the same collection
- Query across all product types or filter by specific types
- Implement type-specific business logic
- Maintain data consistency while allowing flexibility
- Support future product types without schema changes

---

*[Continue with remaining sections covering Repository Pattern, Transaction Management, Performance Optimization, and Banking Use Cases...]*