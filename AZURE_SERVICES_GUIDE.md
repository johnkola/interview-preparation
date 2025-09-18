# Azure Services Interview Guide

## ☁️ Azure App Service

### Q1: What is Azure App Service and how do you deploy Spring Boot applications to it?

**Answer:**
Azure App Service is a Platform-as-a-Service (PaaS) offering for hosting web applications, REST APIs, and mobile backends.

**Spring Boot Deployment Configuration:**
```yaml
# application-azure.yml
server:
  port: ${PORT:8080}

spring:
  datasource:
    url: ${AZURE_SQL_CONNECTION_STRING}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver

  jpa:
    hibernate:
      ddl-auto: validate
    database-platform: org.hibernate.dialect.SQLServerDialect

logging:
  level:
    org.springframework.security: DEBUG
    com.bank: INFO

azure:
  keyvault:
    uri: ${AZURE_KEYVAULT_URI}
    client-id: ${AZURE_CLIENT_ID}
    client-secret: ${AZURE_CLIENT_SECRET}
    tenant-id: ${AZURE_TENANT_ID}
```

**Azure Configuration Class:**
```java
@Configuration
@EnableConfigurationProperties(AzureProperties.class)
public class AzureConfig {

    @Bean
    @ConditionalOnProperty("azure.storage.account-name")
    public BlobServiceClient blobServiceClient(AzureProperties properties) {
        return new BlobServiceClientBuilder()
            .endpoint("https://" + properties.getStorage().getAccountName() + ".blob.core.windows.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
    }

    @Bean
    @ConditionalOnProperty("azure.servicebus.namespace")
    public ServiceBusClientBuilder.ServiceBusSenderClient serviceBusSender(AzureProperties properties) {
        return new ServiceBusClientBuilder()
            .connectionString(properties.getServicebus().getConnectionString())
            .sender()
            .queueName(properties.getServicebus().getQueueName())
            .buildClient();
    }
}

@ConfigurationProperties(prefix = "azure")
@Data
public class AzureProperties {
    private Storage storage = new Storage();
    private Servicebus servicebus = new Servicebus();
    private Keyvault keyvault = new Keyvault();

    @Data
    public static class Storage {
        private String accountName;
        private String containerName;
    }

    @Data
    public static class Servicebus {
        private String connectionString;
        private String queueName;
        private String topicName;
    }

    @Data
    public static class Keyvault {
        private String uri;
        private String clientId;
        private String clientSecret;
        private String tenantId;
    }
}
```

**Deployment Methods:**

1. **Maven Plugin Deployment:**
```xml
<plugin>
    <groupId>com.microsoft.azure</groupId>
    <artifactId>azure-webapp-maven-plugin</artifactId>
    <version>2.5.0</version>
    <configuration>
        <subscriptionId>${AZURE_SUBSCRIPTION_ID}</subscriptionId>
        <resourceGroup>${AZURE_RESOURCE_GROUP}</resourceGroup>
        <appName>${AZURE_APP_NAME}</appName>
        <pricingTier>P1v2</pricingTier>
        <region>East US</region>
        <runtime>
            <os>Linux</os>
            <javaVersion>Java 17</javaVersion>
            <webContainer>Java SE</webContainer>
        </runtime>
        <appSettings>
            <property>
                <name>SPRING_PROFILES_ACTIVE</name>
                <value>azure</value>
            </property>
            <property>
                <name>JAVA_OPTS</name>
                <value>-Dserver.port=80 -Xms512m -Xmx1024m</value>
            </property>
        </appSettings>
        <deployment>
            <resources>
                <resource>
                    <directory>${project.basedir}/target</directory>
                    <includes>
                        <include>*.jar</include>
                    </includes>
                </resource>
            </resources>
        </deployment>
    </configuration>
</plugin>
```

2. **GitHub Actions CI/CD:**
```yaml
# .github/workflows/azure-deploy.yml
name: Deploy to Azure App Service

on:
  push:
    branches: [ main ]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest

    steps:
    - uses: actions/checkout@v2

    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'temurin'

    - name: Cache Maven packages
      uses: actions/cache@v2
      with:
        path: ~/.m2
        key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}

    - name: Run tests
      run: mvn clean test

    - name: Build with Maven
      run: mvn clean package -DskipTests

    - name: Deploy to Azure Web App
      uses: azure/webapps-deploy@v2
      with:
        app-name: ${{ secrets.AZURE_WEBAPP_NAME }}
        publish-profile: ${{ secrets.AZURE_WEBAPP_PUBLISH_PROFILE }}
        package: target/*.jar
```

**Health Check and Monitoring:**
```java
@RestController
@RequestMapping("/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @GetMapping("/custom")
    public ResponseEntity<Map<String, Object>> customHealth() {
        Map<String, Object> health = new HashMap<>();

        try {
            // Database check
            try (Connection conn = dataSource.getConnection()) {
                health.put("database", "UP");
            }
        } catch (Exception e) {
            health.put("database", "DOWN");
            health.put("error", e.getMessage());
        }

        // Azure services check
        health.put("azure", checkAzureServices());
        health.put("timestamp", Instant.now());
        health.put("version", getClass().getPackage().getImplementationVersion());

        return ResponseEntity.ok(health);
    }

    private Map<String, String> checkAzureServices() {
        Map<String, String> azureHealth = new HashMap<>();

        // Check Service Bus connectivity
        try {
            // Implement actual service bus health check
            azureHealth.put("servicebus", "UP");
        } catch (Exception e) {
            azureHealth.put("servicebus", "DOWN");
        }

        // Check Key Vault connectivity
        try {
            // Implement actual key vault health check
            azureHealth.put("keyvault", "UP");
        } catch (Exception e) {
            azureHealth.put("keyvault", "DOWN");
        }

        return azureHealth;
    }
}
```

### Q2: How do you handle scaling and performance in Azure App Service?

**Answer:**

**Auto-scaling Configuration:**
```java
@Component
public class AzureMetricsCollector {

    private final MeterRegistry meterRegistry;

    public AzureMetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @EventListener
    public void handleRequest(RequestProcessedEvent event) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("http.request.duration")
            .description("HTTP request duration")
            .tag("method", event.getMethod())
            .tag("status", String.valueOf(event.getStatus()))
            .register(meterRegistry));

        // Custom metrics for Azure monitoring
        Counter.builder("http.requests.total")
            .description("Total HTTP requests")
            .tag("method", event.getMethod())
            .tag("endpoint", event.getEndpoint())
            .register(meterRegistry)
            .increment();
    }

    @Scheduled(fixedDelay = 60000) // Every minute
    public void publishCustomMetrics() {
        // Memory usage
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();

        Gauge.builder("memory.heap.used")
            .description("Used heap memory")
            .register(meterRegistry, heapUsed);

        Gauge.builder("memory.heap.utilization")
            .description("Heap memory utilization")
            .register(meterRegistry, (double) heapUsed / heapMax);

        // Database connection pool metrics
        // Implement based on your connection pool (HikariCP example)
        // HikariDataSource hikariDataSource = (HikariDataSource) dataSource;
        // Gauge.builder("db.connections.active").register(meterRegistry,
        //     hikariDataSource.getHikariPoolMXBean().getActiveConnections());
    }
}
```

---

## ⚡ Azure Functions

### Q3: How do you create Azure Functions with Spring Boot?

**Answer:**

**Azure Functions Setup:**
```xml
<dependency>
    <groupId>com.microsoft.azure.functions</groupId>
    <artifactId>azure-functions-java-library</artifactId>
    <version>2.0.1</version>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-function-adapter-azure</artifactId>
    <version>4.0.0</version>
</dependency>
```

**Function Implementation:**
```java
// HTTP Trigger Function
@Component
public class BankingFunctions {

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService transactionService;

    @FunctionName("getAccountBalance")
    public HttpResponseMessage getAccountBalance(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET},
                authLevel = AuthorizationLevel.FUNCTION,
                route = "accounts/{accountNumber}/balance"
            ) HttpRequestMessage<Optional<String>> request,
            @BindingName("accountNumber") String accountNumber,
            ExecutionContext context) {

        context.getLogger().info("Getting balance for account: " + accountNumber);

        try {
            BigDecimal balance = accountService.getBalance(accountNumber);

            return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(Map.of(
                    "accountNumber", accountNumber,
                    "balance", balance,
                    "timestamp", Instant.now()
                ))
                .build();

        } catch (AccountNotFoundException e) {
            return request.createResponseBuilder(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Account not found"))
                .build();
        } catch (Exception e) {
            context.getLogger().severe("Error getting balance: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"))
                .build();
        }
    }

    @FunctionName("processTransaction")
    public HttpResponseMessage processTransaction(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.POST},
                authLevel = AuthorizationLevel.FUNCTION,
                route = "transactions"
            ) HttpRequestMessage<TransactionRequest> request,
            ExecutionContext context) {

        context.getLogger().info("Processing transaction");

        try {
            TransactionRequest txRequest = request.getBody();

            // Validate request
            if (txRequest == null || txRequest.getAmount() == null ||
                txRequest.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid transaction amount"))
                    .build();
            }

            TransactionResult result = transactionService.processTransaction(txRequest);

            return request.createResponseBuilder(HttpStatus.OK)
                .body(result)
                .build();

        } catch (Exception e) {
            context.getLogger().severe("Transaction failed: " + e.getMessage());
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Transaction processing failed"))
                .build();
        }
    }

    // Timer Trigger Function
    @FunctionName("dailyInterestCalculation")
    public void calculateDailyInterest(
            @TimerTrigger(
                name = "timerInfo",
                schedule = "0 0 2 * * *" // Every day at 2 AM
            ) String timerInfo,
            ExecutionContext context) {

        context.getLogger().info("Starting daily interest calculation");

        try {
            List<Account> savingsAccounts = accountService.getSavingsAccounts();

            for (Account account : savingsAccounts) {
                BigDecimal interest = calculateInterest(account);
                accountService.addInterest(account.getAccountNumber(), interest);

                context.getLogger().info(
                    String.format("Added interest %.2f to account %s",
                    interest, account.getAccountNumber())
                );
            }

            context.getLogger().info("Daily interest calculation completed");

        } catch (Exception e) {
            context.getLogger().severe("Interest calculation failed: " + e.getMessage());
            throw e; // This will trigger retry mechanism
        }
    }

    // Service Bus Trigger Function
    @FunctionName("processTransactionMessage")
    public void processTransactionMessage(
            @ServiceBusQueueTrigger(
                name = "message",
                queueName = "transaction-queue",
                connection = "ServiceBusConnection"
            ) String message,
            ExecutionContext context) {

        context.getLogger().info("Received transaction message: " + message);

        try {
            ObjectMapper mapper = new ObjectMapper();
            TransactionMessage txMessage = mapper.readValue(message, TransactionMessage.class);

            // Process the transaction
            transactionService.processAsyncTransaction(txMessage);

            context.getLogger().info("Transaction processed successfully");

        } catch (Exception e) {
            context.getLogger().severe("Failed to process transaction message: " + e.getMessage());
            // Message will be moved to dead letter queue after max retries
            throw e;
        }
    }

    // Blob Trigger Function
    @FunctionName("processTransactionFile")
    public void processTransactionFile(
            @BlobTrigger(
                name = "blob",
                path = "transactions/{name}",
                connection = "StorageConnection"
            ) byte[] content,
            @BindingName("name") String fileName,
            ExecutionContext context) {

        context.getLogger().info("Processing transaction file: " + fileName);

        try {
            String fileContent = new String(content, StandardCharsets.UTF_8);
            List<TransactionRecord> transactions = parseTransactionFile(fileContent);

            for (TransactionRecord transaction : transactions) {
                transactionService.processBatchTransaction(transaction);
            }

            context.getLogger().info("Processed " + transactions.size() + " transactions from " + fileName);

        } catch (Exception e) {
            context.getLogger().severe("Failed to process file " + fileName + ": " + e.getMessage());
            throw e;
        }
    }

    private BigDecimal calculateInterest(Account account) {
        // Simple interest calculation - 2% annual rate
        BigDecimal annualRate = new BigDecimal("0.02");
        BigDecimal dailyRate = annualRate.divide(new BigDecimal("365"), 6, RoundingMode.HALF_UP);
        return account.getBalance().multiply(dailyRate);
    }

    private List<TransactionRecord> parseTransactionFile(String content) {
        // Parse CSV or JSON file content
        // Implementation depends on file format
        return new ArrayList<>();
    }
}
```

**Configuration for Azure Functions:**
```java
@Configuration
@ComponentScan
public class AzureFunctionConfiguration {

    @Bean
    @Primary
    public AzureConfigurationPropertiesBean azureConfigurationPropertiesBean() {
        return new AzureConfigurationPropertiesBean();
    }

    @Bean
    public Function<TransactionRequest, TransactionResult> processTransaction() {
        return transactionService::processTransaction;
    }

    @Bean
    public Consumer<String> processTransactionMessage() {
        return message -> {
            // Process Service Bus message
            ObjectMapper mapper = new ObjectMapper();
            try {
                TransactionMessage txMessage = mapper.readValue(message, TransactionMessage.class);
                transactionService.processAsyncTransaction(txMessage);
            } catch (Exception e) {
                throw new RuntimeException("Failed to process message", e);
            }
        };
    }

    @Autowired
    private TransactionService transactionService;
}
```

---

## 🚌 Azure Service Bus

### Q4: How do you integrate Azure Service Bus with Spring Boot?

**Answer:**

**Service Bus Configuration:**
```xml
<dependency>
    <groupId>com.azure.spring</groupId>
    <artifactId>spring-cloud-azure-starter-servicebus</artifactId>
</dependency>
```

```yaml
spring:
  cloud:
    azure:
      servicebus:
        connection-string: ${AZURE_SERVICEBUS_CONNECTION_STRING}
        processor:
          max-concurrent-calls: 5
          max-auto-lock-renewal-duration: PT5M
```

**Message Producer:**
```java
@Service
public class TransactionMessageService {

    private final ServiceBusClientBuilder.ServiceBusSenderClient queueSender;
    private final ServiceBusClientBuilder.ServiceBusSenderClient topicSender;

    public TransactionMessageService(
            @Value("${spring.cloud.azure.servicebus.connection-string}") String connectionString) {

        // Queue sender
        this.queueSender = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .queueName("transaction-processing")
            .buildClient();

        // Topic sender
        this.topicSender = new ServiceBusClientBuilder()
            .connectionString(connectionString)
            .sender()
            .topicName("account-events")
            .buildClient();
    }

    public void sendTransactionForProcessing(TransactionMessage message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String messageBody = mapper.writeValueAsString(message);

            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(messageBody)
                .setContentType("application/json")
                .setCorrelationId(message.getTransactionId())
                .setTimeToLive(Duration.ofMinutes(30))
                .setScheduledEnqueueTime(OffsetDateTime.now().plusMinutes(1)); // Delay processing

            // Add custom properties
            serviceBusMessage.getApplicationProperties().put("accountType", message.getAccountType());
            serviceBusMessage.getApplicationProperties().put("priority", message.getPriority());
            serviceBusMessage.getApplicationProperties().put("source", "banking-api");

            queueSender.sendMessage(serviceBusMessage);

            log.info("Sent transaction message for processing: {}", message.getTransactionId());

        } catch (Exception e) {
            log.error("Failed to send transaction message: {}", e.getMessage(), e);
            throw new MessagingException("Failed to send message", e);
        }
    }

    public void publishAccountEvent(AccountEvent event) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String messageBody = mapper.writeValueAsString(event);

            ServiceBusMessage message = new ServiceBusMessage(messageBody)
                .setContentType("application/json")
                .setSubject(event.getEventType()) // Used for topic subscriptions filtering
                .setCorrelationId(event.getAccountNumber());

            // Add properties for subscription filters
            message.getApplicationProperties().put("eventType", event.getEventType());
            message.getApplicationProperties().put("accountType", event.getAccountType());
            message.getApplicationProperties().put("customerId", event.getCustomerId());

            topicSender.sendMessage(message);

            log.info("Published account event: {} for account: {}",
                event.getEventType(), event.getAccountNumber());

        } catch (Exception e) {
            log.error("Failed to publish account event: {}", e.getMessage(), e);
            throw new MessagingException("Failed to publish event", e);
        }
    }

    public void sendBatchMessages(List<TransactionMessage> messages) {
        try {
            List<ServiceBusMessage> serviceBusMessages = messages.stream()
                .map(msg -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        String messageBody = mapper.writeValueAsString(msg);
                        return new ServiceBusMessage(messageBody)
                            .setContentType("application/json")
                            .setCorrelationId(msg.getTransactionId());
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to serialize message", e);
                    }
                })
                .collect(Collectors.toList());

            queueSender.sendMessages(serviceBusMessages);

            log.info("Sent batch of {} transaction messages", messages.size());

        } catch (Exception e) {
            log.error("Failed to send batch messages: {}", e.getMessage(), e);
            throw new MessagingException("Failed to send batch messages", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        queueSender.close();
        topicSender.close();
    }
}
```

**Message Consumer:**
```java
@Component
@Slf4j
public class TransactionMessageProcessor {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private DeadLetterService deadLetterService;

    @ServiceBusQueueListener(destination = "transaction-processing")
    public void processTransactionMessage(
            @Payload TransactionMessage message,
            @Header Map<String, Object> headers,
            ServiceBusReceivedMessageContext messageContext) {

        String messageId = messageContext.getMessage().getMessageId();
        log.info("Processing transaction message: {}", messageId);

        try {
            // Validate message
            validateTransactionMessage(message);

            // Process the transaction
            TransactionResult result = transactionService.processTransaction(message);

            if (result.isSuccessful()) {
                // Complete the message
                messageContext.complete();
                log.info("Transaction processed successfully: {}", message.getTransactionId());
            } else {
                // Abandon the message for retry
                messageContext.abandon();
                log.warn("Transaction processing failed, message abandoned: {}", message.getTransactionId());
            }

        } catch (ValidationException e) {
            // Dead letter for validation errors (no retry)
            messageContext.deadLetter("Validation failed", e.getMessage());
            log.error("Transaction message validation failed: {}", e.getMessage());

        } catch (BusinessException e) {
            // Dead letter for business logic errors
            messageContext.deadLetter("Business rule violation", e.getMessage());
            log.error("Transaction business rule violation: {}", e.getMessage());

        } catch (Exception e) {
            // Abandon for technical errors (will retry)
            messageContext.abandon();
            log.error("Technical error processing transaction: {}", e.getMessage(), e);
        }
    }

    @ServiceBusTopicListener(destination = "account-events", subscription = "audit-service")
    public void auditAccountEvent(
            @Payload AccountEvent event,
            ServiceBusReceivedMessageContext messageContext) {

        log.info("Auditing account event: {} for account: {}",
            event.getEventType(), event.getAccountNumber());

        try {
            // Store audit record
            AuditRecord audit = new AuditRecord();
            audit.setEventType(event.getEventType());
            audit.setAccountNumber(event.getAccountNumber());
            audit.setCustomerId(event.getCustomerId());
            audit.setTimestamp(event.getTimestamp());
            audit.setDetails(event.getDetails());

            auditService.saveAuditRecord(audit);

            messageContext.complete();

        } catch (Exception e) {
            log.error("Failed to audit account event: {}", e.getMessage(), e);
            messageContext.abandon();
        }
    }

    @ServiceBusTopicListener(destination = "account-events", subscription = "notification-service")
    public void sendNotification(
            @Payload AccountEvent event,
            ServiceBusReceivedMessageContext messageContext) {

        log.info("Sending notification for event: {} on account: {}",
            event.getEventType(), event.getAccountNumber());

        try {
            // Determine notification type based on event
            NotificationType notificationType = determineNotificationType(event);

            if (notificationType != null) {
                notificationService.sendNotification(
                    event.getCustomerId(),
                    notificationType,
                    event.getDetails()
                );
            }

            messageContext.complete();

        } catch (Exception e) {
            log.error("Failed to send notification: {}", e.getMessage(), e);
            messageContext.abandon();
        }
    }

    private void validateTransactionMessage(TransactionMessage message) {
        if (message.getTransactionId() == null || message.getTransactionId().trim().isEmpty()) {
            throw new ValidationException("Transaction ID is required");
        }

        if (message.getAmount() == null || message.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Valid transaction amount is required");
        }

        if (message.getFromAccount() == null || message.getToAccount() == null) {
            throw new ValidationException("Source and destination accounts are required");
        }
    }

    private NotificationType determineNotificationType(AccountEvent event) {
        return switch (event.getEventType()) {
            case "LARGE_DEPOSIT" -> NotificationType.LARGE_TRANSACTION_ALERT;
            case "SUSPICIOUS_ACTIVITY" -> NotificationType.SECURITY_ALERT;
            case "LOW_BALANCE" -> NotificationType.BALANCE_WARNING;
            case "ACCOUNT_LOCKED" -> NotificationType.ACCOUNT_STATUS_CHANGE;
            default -> null;
        };
    }
}
```

**Dead Letter Queue Handler:**
```java
@Component
public class DeadLetterProcessor {

    @ServiceBusQueueListener(destination = "transaction-processing/$deadletterqueue")
    public void processDeadLetterMessage(
            @Payload String messageBody,
            @Header Map<String, Object> headers,
            ServiceBusReceivedMessageContext messageContext) {

        log.warn("Processing dead letter message: {}", messageContext.getMessage().getMessageId());

        try {
            // Log the dead letter reason
            String deadLetterReason = (String) headers.get("DeadLetterReason");
            String deadLetterDescription = (String) headers.get("DeadLetterErrorDescription");

            log.error("Dead letter reason: {}, Description: {}", deadLetterReason, deadLetterDescription);

            // Store for manual review
            DeadLetterRecord record = new DeadLetterRecord();
            record.setMessageId(messageContext.getMessage().getMessageId());
            record.setMessageBody(messageBody);
            record.setDeadLetterReason(deadLetterReason);
            record.setDeadLetterDescription(deadLetterDescription);
            record.setTimestamp(LocalDateTime.now());

            deadLetterService.saveDeadLetterRecord(record);

            // Send alert to operations team
            alertService.sendDeadLetterAlert(record);

            messageContext.complete();

        } catch (Exception e) {
            log.error("Failed to process dead letter message: {}", e.getMessage(), e);
            messageContext.abandon();
        }
    }
}
```

---

## 🔐 Azure Key Vault

### Q5: How do you integrate Azure Key Vault with Spring Boot for secrets management?

**Answer:**

**Key Vault Configuration:**
```xml
<dependency>
    <groupId>com.azure.spring</groupId>
    <artifactId>spring-cloud-azure-starter-keyvault-secrets</artifactId>
</dependency>
```

```yaml
spring:
  cloud:
    azure:
      keyvault:
        secret:
          endpoint: ${AZURE_KEYVAULT_ENDPOINT}
          property-sources:
            - endpoint: ${AZURE_KEYVAULT_ENDPOINT}
              name: banking-secrets
              refresh-interval: PT30M # Refresh every 30 minutes
  config:
    import: "azure-keyvault://"
```

**Key Vault Service Implementation:**
```java
@Service
@Slf4j
public class KeyVaultService {

    private final SecretClient secretClient;

    public KeyVaultService(@Value("${spring.cloud.azure.keyvault.secret.endpoint}") String vaultUrl) {
        this.secretClient = new SecretClientBuilder()
            .vaultUrl(vaultUrl)
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();
    }

    public String getSecret(String secretName) {
        try {
            KeyVaultSecret secret = secretClient.getSecret(secretName);
            log.debug("Retrieved secret: {}", secretName);
            return secret.getValue();
        } catch (ResourceNotFoundException e) {
            log.error("Secret not found: {}", secretName);
            throw new SecretNotFoundException("Secret not found: " + secretName);
        } catch (Exception e) {
            log.error("Error retrieving secret {}: {}", secretName, e.getMessage());
            throw new SecretRetrievalException("Failed to retrieve secret: " + secretName, e);
        }
    }

    public void createOrUpdateSecret(String secretName, String secretValue) {
        try {
            secretClient.setSecret(secretName, secretValue);
            log.info("Secret created/updated: {}", secretName);
        } catch (Exception e) {
            log.error("Error creating/updating secret {}: {}", secretName, e.getMessage());
            throw new SecretUpdateException("Failed to create/update secret: " + secretName, e);
        }
    }

    public void createSecretWithMetadata(String secretName, String secretValue,
                                       Map<String, String> tags, OffsetDateTime expiryDate) {
        try {
            Secret secret = new Secret(secretName, secretValue)
                .setProperties(new SecretProperties()
                    .setExpiresOn(expiryDate)
                    .setTags(tags));

            secretClient.setSecret(secret);
            log.info("Secret with metadata created: {}", secretName);
        } catch (Exception e) {
            log.error("Error creating secret with metadata {}: {}", secretName, e.getMessage());
            throw new SecretUpdateException("Failed to create secret with metadata: " + secretName, e);
        }
    }

    public List<SecretProperties> listSecrets() {
        try {
            return secretClient.listPropertiesOfSecrets()
                .stream()
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error listing secrets: {}", e.getMessage());
            throw new SecretRetrievalException("Failed to list secrets", e);
        }
    }

    public void deleteSecret(String secretName) {
        try {
            SyncPoller<DeletedSecret, Void> deletePoller = secretClient.beginDeleteSecret(secretName);
            deletePoller.waitForCompletion();
            log.info("Secret deleted: {}", secretName);
        } catch (Exception e) {
            log.error("Error deleting secret {}: {}", secretName, e.getMessage());
            throw new SecretUpdateException("Failed to delete secret: " + secretName, e);
        }
    }

    public SecretProperties getSecretProperties(String secretName) {
        try {
            return secretClient.getSecret(secretName).getProperties();
        } catch (Exception e) {
            log.error("Error getting secret properties {}: {}", secretName, e.getMessage());
            throw new SecretRetrievalException("Failed to get secret properties: " + secretName, e);
        }
    }

    @Retryable(value = {Exception.class}, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public String getSecretWithRetry(String secretName) {
        return getSecret(secretName);
    }
}
```

**Database Configuration with Key Vault:**
```java
@Configuration
public class DatabaseConfig {

    @Value("${azure.keyvault.database-url:}")
    private String databaseUrl;

    @Value("${azure.keyvault.database-username:}")
    private String databaseUsername;

    @Value("${azure.keyvault.database-password:}")
    private String databasePassword;

    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();

        // Use secrets from Key Vault
        config.setJdbcUrl(databaseUrl);
        config.setUsername(databaseUsername);
        config.setPassword(databasePassword);

        // Connection pool settings
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setLeakDetectionThreshold(60000);

        // Security settings
        config.addDataSourceProperty("ssl", "true");
        config.addDataSourceProperty("sslmode", "require");
        config.addDataSourceProperty("trustServerCertificate", "false");

        return new HikariDataSource(config);
    }
}
```

**External API Configuration with Key Vault:**
```java
@Configuration
@ConfigurationProperties(prefix = "external.api")
@RefreshScope // Allows configuration refresh without restart
public class ExternalApiConfig {

    @Value("${azure.keyvault.external-api-key:}")
    private String apiKey;

    @Value("${azure.keyvault.external-api-secret:}")
    private String apiSecret;

    @Value("${external.api.base-url}")
    private String baseUrl;

    @Value("${external.api.timeout:30000}")
    private int timeout;

    @Bean
    public RestTemplate externalApiRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // Add authentication interceptor
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("X-API-Key", apiKey);
            request.getHeaders().add("Authorization", "Bearer " + generateToken(apiSecret));
            return execution.execute(request, body);
        });

        // Configure timeout
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectionRequestTimeout(timeout);
        factory.setConnectTimeout(timeout);
        factory.setReadTimeout(timeout);
        restTemplate.setRequestFactory(factory);

        return restTemplate;
    }

    private String generateToken(String secret) {
        // Implement token generation logic
        return JwtTokenGenerator.generate(secret);
    }

    // Getters and setters
}
```

**Secret Rotation Service:**
```java
@Service
public class SecretRotationService {

    @Autowired
    private KeyVaultService keyVaultService;

    @Autowired
    private NotificationService notificationService;

    @Scheduled(cron = "0 0 2 * * SUN") // Every Sunday at 2 AM
    public void checkSecretExpiration() {
        log.info("Checking for expiring secrets");

        List<SecretProperties> secrets = keyVaultService.listSecrets();
        OffsetDateTime thirtyDaysFromNow = OffsetDateTime.now().plusDays(30);

        for (SecretProperties secret : secrets) {
            if (secret.getExpiresOn() != null &&
                secret.getExpiresOn().isBefore(thirtyDaysFromNow)) {

                log.warn("Secret expiring soon: {} expires on {}",
                    secret.getName(), secret.getExpiresOn());

                // Send notification
                notificationService.sendSecretExpirationAlert(
                    secret.getName(),
                    secret.getExpiresOn()
                );

                // Auto-rotate if configured
                if (isAutoRotationEnabled(secret.getName())) {
                    rotateSecret(secret.getName());
                }
            }
        }
    }

    public void rotateSecret(String secretName) {
        try {
            log.info("Rotating secret: {}", secretName);

            // Generate new secret value based on secret type
            String newSecretValue = generateNewSecretValue(secretName);

            // Update the secret in Key Vault
            Map<String, String> tags = Map.of(
                "rotated-on", OffsetDateTime.now().toString(),
                "rotated-by", "auto-rotation-service"
            );

            keyVaultService.createSecretWithMetadata(
                secretName,
                newSecretValue,
                tags,
                OffsetDateTime.now().plusMonths(6) // 6 months expiry
            );

            // Notify relevant services to refresh their configuration
            refreshConfigurationInServices(secretName);

            log.info("Secret rotated successfully: {}", secretName);

        } catch (Exception e) {
            log.error("Failed to rotate secret {}: {}", secretName, e.getMessage(), e);
            notificationService.sendSecretRotationFailureAlert(secretName, e.getMessage());
        }
    }

    private String generateNewSecretValue(String secretName) {
        // Different generation strategies based on secret type
        if (secretName.contains("password")) {
            return PasswordGenerator.generateSecurePassword(32);
        } else if (secretName.contains("api-key")) {
            return UUID.randomUUID().toString().replace("-", "");
        } else if (secretName.contains("certificate")) {
            return CertificateGenerator.generateNewCertificate();
        } else {
            return UUID.randomUUID().toString();
        }
    }

    private boolean isAutoRotationEnabled(String secretName) {
        SecretProperties properties = keyVaultService.getSecretProperties(secretName);
        return properties.getTags() != null &&
               "true".equals(properties.getTags().get("auto-rotate"));
    }

    private void refreshConfigurationInServices(String secretName) {
        // Send refresh event to Spring Cloud Bus or trigger webhook
        // This would notify all application instances to refresh their configuration
        ConfigRefreshEvent event = new ConfigRefreshEvent(secretName);
        applicationEventPublisher.publishEvent(event);
    }

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
}
```

**Key Vault Health Check:**
```java
@Component
public class KeyVaultHealthIndicator implements HealthIndicator {

    @Autowired
    private KeyVaultService keyVaultService;

    @Override
    public Health health() {
        try {
            // Try to access a test secret or list secrets
            keyVaultService.listSecrets();

            return Health.up()
                .withDetail("vault", "accessible")
                .withDetail("timestamp", Instant.now())
                .build();

        } catch (Exception e) {
            return Health.down()
                .withDetail("vault", "inaccessible")
                .withDetail("error", e.getMessage())
                .withDetail("timestamp", Instant.now())
                .build();
        }
    }
}
```

This comprehensive guide covers all the major Spring Boot and Azure components you'll need for the TD Bank interview. Each section includes practical implementations that demonstrate real-world usage patterns.