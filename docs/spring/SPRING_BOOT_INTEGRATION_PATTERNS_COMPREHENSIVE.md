# Spring Boot Integration Patterns - Comprehensive Guide

## Table of Contents
1. [Introduction to Integration Patterns](#introduction-to-integration-patterns)
2. [Spring Integration Framework](#spring-integration-framework)
3. [Message Channels](#message-channels)
4. [Message Endpoints](#message-endpoints)
5. [Enterprise Integration Patterns](#enterprise-integration-patterns)
6. [File Integration](#file-integration)
7. [Database Integration](#database-integration)
8. [REST API Integration](#rest-api-integration)
9. [Message Queue Integration](#message-queue-integration)
10. [Event-Driven Architecture](#event-driven-architecture)
11. [Batch Processing Integration](#batch-processing-integration)
12. [Error Handling and Retry](#error-handling-and-retry)
13. [Monitoring and Observability](#monitoring-and-observability)
14. [Security in Integration](#security-in-integration)
15. [Banking Domain Examples](#banking-domain-examples)
16. [Testing Integration Flows](#testing-integration-flows)
17. [Best Practices](#best-practices)
18. [Performance Optimization](#performance-optimization)
19. [Common Pitfalls](#common-pitfalls)
20. [Interview Questions](#interview-questions)

## Introduction to Integration Patterns

Integration patterns are well-established solutions for common problems in enterprise application integration. They provide standardized approaches for connecting disparate systems, transforming data, and orchestrating business processes.

### Key Concepts

**Enterprise Integration Patterns (EIP)**
- Message routing
- Message transformation
- Message channels
- Message endpoints
- Content-based routing
- Aggregation and splitting

**Integration Styles**
- File Transfer
- Shared Database
- Remote Procedure Invocation
- Messaging

### Benefits of Spring Integration

- **Declarative Integration**: XML and annotation-based configuration
- **Message-Driven Architecture**: Loose coupling between components
- **Enterprise Integration Patterns**: Built-in support for common patterns
- **Channel Adapters**: Easy integration with external systems
- **Testing Support**: Comprehensive testing framework


[⬆️ Back to Top](#table-of-contents)
## Spring Integration Framework

### Core Dependencies

```xml
<dependencies>
    <!-- Spring Boot Integration Starter -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-integration</artifactId>
    </dependency>

    <!-- Spring Integration JDBC -->
    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-jdbc</artifactId>
    </dependency>

    <!-- Spring Integration File -->
    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-file</artifactId>
    </dependency>

    <!-- Spring Integration HTTP -->
    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-http</artifactId>
    </dependency>

    <!-- Spring Integration JMS -->
    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-jms</artifactId>
    </dependency>

    <!-- Spring Integration Mail -->
    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-mail</artifactId>
    </dependency>

    <!-- Spring Integration FTP -->
    <dependency>
        <groupId>org.springframework.integration</groupId>
        <artifactId>spring-integration-ftp</artifactId>
    </dependency>
</dependencies>
```

### Basic Configuration

```java
@Configuration
@EnableIntegration
@ComponentScan
public class IntegrationConfig {

    @Bean
    public IntegrationFlow basicFlow() {
        return IntegrationFlows
            .from("inputChannel")
            .transform(String.class, String::toUpperCase)
            .channel("outputChannel")
            .get();
    }

    @Bean
    public MessageChannel inputChannel() {
        return MessageChannels.direct().get();
    }

    @Bean
    public MessageChannel outputChannel() {
        return MessageChannels.direct().get();
    }

    @Bean
    public MessageChannel errorChannel() {
        return MessageChannels.direct().get();
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Message Channels

### Channel Types

```java
@Configuration
public class ChannelConfiguration {

    // Direct Channel - Synchronous, single-threaded
    @Bean
    public MessageChannel directChannel() {
        return MessageChannels.direct().get();
    }

    // Queue Channel - Asynchronous with internal queue
    @Bean
    public MessageChannel queueChannel() {
        return MessageChannels.queue(100).get(); // Buffer size 100
    }

    // Publish-Subscribe Channel - Multiple subscribers
    @Bean
    public MessageChannel pubSubChannel() {
        return MessageChannels.publishSubscribe().get();
    }

    // Executor Channel - Asynchronous with custom executor
    @Bean
    public MessageChannel executorChannel() {
        return MessageChannels.executor(taskExecutor()).get();
    }

    // Priority Channel - Message ordering by priority
    @Bean
    public MessageChannel priorityChannel() {
        return MessageChannels.priority().get();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("integration-");
        executor.initialize();
        return executor;
    }
}
```

### Channel Interceptors

```java
@Component
public class CustomChannelInterceptor implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(CustomChannelInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        logger.info("Pre-send: {} to channel: {}", message, channel);

        // Add correlation ID if not present
        if (!message.getHeaders().containsKey("correlationId")) {
            return MessageBuilder.fromMessage(message)
                .setHeader("correlationId", UUID.randomUUID().toString())
                .build();
        }

        return message;
    }

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        if (sent) {
            logger.info("Message sent successfully: {}", message.getHeaders().get("correlationId"));
        } else {
            logger.warn("Message send failed: {}", message.getHeaders().get("correlationId"));
        }
    }

    @Override
    public boolean preReceive(MessageChannel channel) {
        logger.debug("Pre-receive from channel: {}", channel);
        return true;
    }

    @Override
    public Message<?> postReceive(Message<?> message, MessageChannel channel) {
        if (message != null) {
            logger.info("Post-receive: {} from channel: {}",
                message.getHeaders().get("correlationId"), channel);
        }
        return message;
    }
}

@Configuration
public class ChannelInterceptorConfig {

    @Autowired
    private CustomChannelInterceptor channelInterceptor;

    @Bean
    @GlobalChannelInterceptor(patterns = "*")
    public ChannelInterceptor globalInterceptor() {
        return channelInterceptor;
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Message Endpoints

### Service Activators

```java
@Component
public class AccountServiceActivator {

    private final AccountService accountService;

    @ServiceActivator(inputChannel = "accountProcessingChannel")
    public Account processAccount(Account account) {
        return accountService.processAccount(account);
    }

    @ServiceActivator(inputChannel = "accountValidationChannel",
                     outputChannel = "validAccountChannel")
    public Message<Account> validateAccount(Message<Account> message) {
        Account account = message.getPayload();

        if (accountService.isValid(account)) {
            return MessageBuilder.withPayload(account)
                .copyHeaders(message.getHeaders())
                .setHeader("validation.status", "VALID")
                .build();
        } else {
            throw new AccountValidationException("Invalid account: " + account.getAccountNumber());
        }
    }

    @ServiceActivator(inputChannel = "asyncAccountChannel")
    @Async
    public void processAccountAsync(Account account) {
        // Asynchronous processing
        try {
            Thread.sleep(1000); // Simulate processing time
            accountService.processAccount(account);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

### Transformers

```java
@Component
public class MessageTransformers {

    @Transformer(inputChannel = "csvInputChannel", outputChannel = "accountChannel")
    public Account csvToAccount(String csvLine) {
        String[] fields = csvLine.split(",");
        return Account.builder()
            .accountNumber(fields[0])
            .balance(new BigDecimal(fields[1]))
            .accountType(AccountType.valueOf(fields[2]))
            .customerId(Long.parseLong(fields[3]))
            .build();
    }

    @Transformer(inputChannel = "accountChannel", outputChannel = "xmlOutputChannel")
    public String accountToXml(Account account) {
        return String.format(
            "<account><number>%s</number><balance>%s</balance><type>%s</type></account>",
            account.getAccountNumber(),
            account.getBalance(),
            account.getAccountType()
        );
    }

    @Transformer(inputChannel = "enrichmentChannel", outputChannel = "enrichedChannel")
    public Message<Account> enrichAccount(Message<Account> message) {
        Account account = message.getPayload();

        // Enrich with additional data
        Customer customer = customerService.findById(account.getCustomerId());

        return MessageBuilder.withPayload(account)
            .copyHeaders(message.getHeaders())
            .setHeader("customer.name", customer.getFullName())
            .setHeader("customer.email", customer.getEmail())
            .setHeader("enrichment.timestamp", System.currentTimeMillis())
            .build();
    }
}
```

### Filters

```java
@Component
public class MessageFilters {

    @Filter(inputChannel = "allAccountsChannel", outputChannel = "highValueAccountsChannel")
    public boolean filterHighValueAccounts(Account account) {
        return account.getBalance().compareTo(BigDecimal.valueOf(10000)) >= 0;
    }

    @Filter(inputChannel = "transactionChannel", outputChannel = "suspiciousTransactionChannel")
    public boolean filterSuspiciousTransactions(Message<Transaction> message) {
        Transaction transaction = message.getPayload();

        // Filter based on amount and frequency
        boolean highAmount = transaction.getAmount().compareTo(BigDecimal.valueOf(50000)) > 0;
        boolean frequentTransactions = checkTransactionFrequency(transaction);

        return highAmount || frequentTransactions;
    }

    @Filter(inputChannel = "customerChannel", outputChannel = "activeCustomerChannel",
            discardChannel = "inactiveCustomerChannel")
    public boolean filterActiveCustomers(Customer customer) {
        return customer.getStatus() == CustomerStatus.ACTIVE;
    }

    private boolean checkTransactionFrequency(Transaction transaction) {
        // Implementation to check transaction frequency
        return false; // Placeholder
    }
}
```

### Routers

```java
@Component
public class MessageRouters {

    @Router(inputChannel = "transactionRoutingChannel")
    public String routeByTransactionType(Transaction transaction) {
        switch (transaction.getTransactionType()) {
            case DEPOSIT:
                return "depositProcessingChannel";
            case WITHDRAWAL:
                return "withdrawalProcessingChannel";
            case TRANSFER:
                return "transferProcessingChannel";
            default:
                return "defaultTransactionChannel";
        }
    }

    @Router(inputChannel = "accountRoutingChannel")
    public Collection<String> routeByAccountType(Account account) {
        List<String> channels = new ArrayList<>();

        // All accounts go to audit
        channels.add("auditChannel");

        // Route based on account type
        switch (account.getAccountType()) {
            case SAVINGS:
                channels.add("savingsProcessingChannel");
                break;
            case CHECKING:
                channels.add("checkingProcessingChannel");
                break;
            case INVESTMENT:
                channels.add("investmentProcessingChannel");
                channels.add("complianceChannel"); // Investment accounts need compliance check
                break;
        }

        return channels;
    }

    @Router(inputChannel = "priorityRoutingChannel")
    public String routeByPriority(Message<?> message) {
        String priority = (String) message.getHeaders().get("priority");

        if ("HIGH".equals(priority)) {
            return "highPriorityChannel";
        } else if ("MEDIUM".equals(priority)) {
            return "mediumPriorityChannel";
        } else {
            return "lowPriorityChannel";
        }
    }
}
```

### Splitters and Aggregators

```java
@Component
public class SplitterAggregatorComponents {

    @Splitter(inputChannel = "batchAccountChannel", outputChannel = "singleAccountChannel")
    public List<Account> splitAccountBatch(List<Account> accounts) {
        return accounts;
    }

    @Splitter(inputChannel = "csvBatchChannel", outputChannel = "csvLineChannel")
    public List<String> splitCsvFile(String csvContent) {
        return Arrays.asList(csvContent.split("\n"));
    }

    @Aggregator(inputChannel = "singleAccountChannel", outputChannel = "aggregatedAccountChannel")
    public List<Account> aggregateAccounts(List<Account> accounts) {
        // Group accounts by customer
        return accounts;
    }

    @Aggregator(inputChannel = "transactionResponseChannel",
                outputChannel = "consolidatedResponseChannel",
                correlationStrategy = "correlationStrategy",
                releaseStrategy = "releaseStrategy")
    public TransactionSummary aggregateTransactionResponses(List<TransactionResponse> responses) {
        return TransactionSummary.builder()
            .totalTransactions(responses.size())
            .successfulTransactions(responses.stream()
                .mapToInt(r -> r.isSuccess() ? 1 : 0)
                .sum())
            .totalAmount(responses.stream()
                .filter(TransactionResponse::isSuccess)
                .map(TransactionResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add))
            .build();
    }

    @Bean
    public CorrelationStrategy correlationStrategy() {
        return message -> message.getHeaders().get("batchId");
    }

    @Bean
    public ReleaseStrategy releaseStrategy() {
        return new MessageCountReleaseStrategy(10); // Release after 10 messages
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Enterprise Integration Patterns

### Content-Based Router

```java
@Configuration
public class ContentBasedRoutingConfig {

    @Bean
    public IntegrationFlow contentBasedRoutingFlow() {
        return IntegrationFlows
            .from("paymentRequestChannel")
            .<PaymentRequest, String>route(
                request -> request.getPaymentType().toString(),
                mapping -> mapping
                    .subFlowMapping("DOMESTIC", domesticPaymentFlow())
                    .subFlowMapping("INTERNATIONAL", internationalPaymentFlow())
                    .subFlowMapping("WIRE", wireTransferFlow())
                    .defaultSubFlowMapping(defaultPaymentFlow())
            )
            .get();
    }

    private IntegrationFlow domesticPaymentFlow() {
        return flow -> flow
            .transform(PaymentRequest.class, this::validateDomesticPayment)
            .handle("domesticPaymentService", "processPayment")
            .channel("paymentResponseChannel");
    }

    private IntegrationFlow internationalPaymentFlow() {
        return flow -> flow
            .transform(PaymentRequest.class, this::validateInternationalPayment)
            .enrich(enricher -> enricher
                .requestChannel("exchangeRateChannel")
                .propertyExpression("exchangeRate", "payload.rate"))
            .handle("internationalPaymentService", "processPayment")
            .channel("paymentResponseChannel");
    }

    private IntegrationFlow wireTransferFlow() {
        return flow -> flow
            .filter(PaymentRequest.class, request ->
                request.getAmount().compareTo(BigDecimal.valueOf(10000)) <= 0)
            .transform(PaymentRequest.class, this::addComplianceChecks)
            .handle("wireTransferService", "processWireTransfer")
            .channel("paymentResponseChannel");
    }

    private IntegrationFlow defaultPaymentFlow() {
        return flow -> flow
            .transform(PaymentRequest.class, request -> {
                throw new UnsupportedPaymentTypeException(
                    "Unsupported payment type: " + request.getPaymentType());
            });
    }

    private PaymentRequest validateDomesticPayment(PaymentRequest request) {
        // Validation logic for domestic payments
        return request;
    }

    private PaymentRequest validateInternationalPayment(PaymentRequest request) {
        // Validation logic for international payments
        return request;
    }

    private PaymentRequest addComplianceChecks(PaymentRequest request) {
        // Add compliance metadata
        return request;
    }
}
```

### Message Store and Claim Check Pattern

```java
@Configuration
public class ClaimCheckPatternConfig {

    @Bean
    public MessageStore messageStore() {
        return new SimpleMessageStore();
    }

    @Bean
    public IntegrationFlow claimCheckFlow() {
        return IntegrationFlows
            .from("largePayloadChannel")
            .claimCheckIn(messageStore())
            .channel("processClaimCheckChannel")
            .claimCheckOut(messageStore())
            .channel("outputChannel")
            .get();
    }

    @Bean
    public IntegrationFlow documentProcessingFlow() {
        return IntegrationFlows
            .from("documentUploadChannel")
            .transform(Document.class, this::storeDocumentAndReturnReference)
            .channel("documentReferenceChannel")
            .transform(DocumentReference.class, this::processDocumentReference)
            .transform(DocumentReference.class, this::retrieveAndProcessDocument)
            .channel("processedDocumentChannel")
            .get();
    }

    private DocumentReference storeDocumentAndReturnReference(Document document) {
        String documentId = documentStorageService.store(document);
        return DocumentReference.builder()
            .documentId(documentId)
            .documentType(document.getType())
            .timestamp(LocalDateTime.now())
            .size(document.getContent().length)
            .build();
    }

    private DocumentReference processDocumentReference(DocumentReference reference) {
        // Process the reference (metadata operations)
        return reference;
    }

    private Document retrieveAndProcessDocument(DocumentReference reference) {
        Document document = documentStorageService.retrieve(reference.getDocumentId());
        // Process the full document
        return document;
    }
}
```

### Scatter-Gather Pattern

```java
@Configuration
public class ScatterGatherConfig {

    @Bean
    public IntegrationFlow scatterGatherFlow() {
        return IntegrationFlows
            .from("creditCheckRequestChannel")
            .scatterGather(
                scatterer -> scatterer
                    .applySequence(true)
                    .recipientFlow("bureauAChannel")
                    .recipientFlow("bureauBChannel")
                    .recipientFlow("bureauCChannel"),
                gatherer -> gatherer
                    .outputProcessor(group -> {
                        List<CreditScore> scores = group.stream()
                            .map(message -> (CreditScore) message.getPayload())
                            .collect(Collectors.toList());
                        return consolidateCreditScores(scores);
                    })
            )
            .channel("consolidatedCreditScoreChannel")
            .get();
    }

    @Bean
    public IntegrationFlow bureauAFlow() {
        return IntegrationFlows
            .from("bureauAChannel")
            .handle("creditBureauAService", "getCreditScore")
            .get();
    }

    @Bean
    public IntegrationFlow bureauBFlow() {
        return IntegrationFlows
            .from("bureauBChannel")
            .handle("creditBureauBService", "getCreditScore")
            .get();
    }

    @Bean
    public IntegrationFlow bureauCFlow() {
        return IntegrationFlows
            .from("bureauCChannel")
            .handle("creditBureauCService", "getCreditScore")
            .get();
    }

    private ConsolidatedCreditScore consolidateCreditScores(List<CreditScore> scores) {
        return ConsolidatedCreditScore.builder()
            .averageScore(scores.stream()
                .mapToInt(CreditScore::getScore)
                .average()
                .orElse(0.0))
            .highestScore(scores.stream()
                .mapToInt(CreditScore::getScore)
                .max()
                .orElse(0))
            .lowestScore(scores.stream()
                .mapToInt(CreditScore::getScore)
                .min()
                .orElse(0))
            .scoresCount(scores.size())
            .timestamp(LocalDateTime.now())
            .build();
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## File Integration

### File Inbound Adapter

```java
@Configuration
public class FileIntegrationConfig {

    @Bean
    public IntegrationFlow fileInboundFlow() {
        return IntegrationFlows
            .from(Files.inboundAdapter(new File("/data/incoming"))
                .patternFilter("*.csv")
                .autoCreateDirectory(true)
                .preventDuplicates(true)
                .deleteSourceFiles(true),
                poller -> poller.fixedDelay(5000))
            .transform(File.class, this::fileToString)
            .split(this::splitCsvLines)
            .transform(String.class, this::csvLineToTransaction)
            .aggregate(aggregator -> aggregator
                .correlationStrategy(message -> "batch")
                .releaseStrategy(new MessageCountReleaseStrategy(100))
                .sendTimeout(60000))
            .handle("transactionBatchService", "processBatch")
            .get();
    }

    @Bean
    public IntegrationFlow fileOutboundFlow() {
        return IntegrationFlows
            .from("reportGenerationChannel")
            .transform(Report.class, this::reportToCsv)
            .handle(Files.outboundAdapter("/data/outgoing")
                .fileNameExpression("'report_' + T(java.time.LocalDate).now() + '.csv'")
                .appendNewLine(true)
                .charset("UTF-8"))
            .get();
    }

    private String fileToString(File file) {
        try {
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + file.getName(), e);
        }
    }

    private List<String> splitCsvLines(String content) {
        return Arrays.stream(content.split("\n"))
            .filter(line -> !line.trim().isEmpty())
            .skip(1) // Skip header
            .collect(Collectors.toList());
    }

    private Transaction csvLineToTransaction(String csvLine) {
        String[] fields = csvLine.split(",");
        return Transaction.builder()
            .accountId(Long.parseLong(fields[0]))
            .transactionType(TransactionType.valueOf(fields[1]))
            .amount(new BigDecimal(fields[2]))
            .description(fields[3])
            .timestamp(LocalDateTime.parse(fields[4]))
            .build();
    }

    private String reportToCsv(Report report) {
        // Convert report to CSV format
        return report.toCsvString();
    }
}
```

### SFTP Integration

```java
@Configuration
public class SftpIntegrationConfig {

    @Bean
    public SessionFactory<SftpClient.DirEntry> sftpSessionFactory() {
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost("sftp.bank.com");
        factory.setPort(22);
        factory.setUser("integration_user");
        factory.setPassword("secure_password");
        factory.setAllowUnknownKeys(true);
        return factory;
    }

    @Bean
    public IntegrationFlow sftpInboundFlow() {
        return IntegrationFlows
            .from(Sftp.inboundAdapter(sftpSessionFactory())
                .deleteRemoteFiles(true)
                .remoteDirectory("/incoming")
                .localDirectory(new File("/local/incoming"))
                .regexFilter(".*\\.xml$"),
                poller -> poller.fixedDelay(10000))
            .transform(File.class, this::parseXmlFile)
            .channel("xmlProcessingChannel")
            .get();
    }

    @Bean
    public IntegrationFlow sftpOutboundFlow() {
        return IntegrationFlows
            .from("sftpUploadChannel")
            .handle(Sftp.outboundAdapter(sftpSessionFactory(), FileExistsMode.REPLACE)
                .useTemporaryFileName(true)
                .remoteDirectory("/outgoing")
                .remoteFilenameExpression("headers.filename"))
            .get();
    }

    private XmlDocument parseXmlFile(File file) {
        // Parse XML file
        return new XmlDocument(); // Placeholder
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Database Integration

### JDBC Integration

```java
@Configuration
public class DatabaseIntegrationConfig {

    @Bean
    public IntegrationFlow jdbcInboundFlow() {
        return IntegrationFlows
            .from(Jdbc.inboundAdapter(dataSource())
                .query("SELECT * FROM pending_transactions WHERE processed = false")
                .updateSql("UPDATE pending_transactions SET processed = true WHERE id = :id")
                .rowMapper(new TransactionRowMapper()),
                poller -> poller.fixedDelay(30000).maxMessagesPerPoll(100))
            .channel("pendingTransactionChannel")
            .get();
    }

    @Bean
    public IntegrationFlow jdbcOutboundFlow() {
        return IntegrationFlows
            .from("processedTransactionChannel")
            .handle(Jdbc.outboundAdapter(dataSource())
                .messageSqlParameterSourceFactory(new BeanPropertySqlParameterSourceFactory())
                .sql("INSERT INTO processed_transactions (account_id, amount, transaction_type, processed_at) " +
                     "VALUES (:accountId, :amount, :transactionType, :processedAt)"))
            .get();
    }

    @Bean
    public IntegrationFlow storedProcedureFlow() {
        return IntegrationFlows
            .from("storedProcedureChannel")
            .handle(Jdbc.storedProcedure(dataSource(), "process_daily_interest")
                .parameter("account_type", "accountType")
                .parameter("calculation_date", "calculationDate")
                .function(true))
            .channel("storedProcedureResultChannel")
            .get();
    }

    private static class TransactionRowMapper implements RowMapper<Transaction> {
        @Override
        public Transaction mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Transaction.builder()
                .id(rs.getLong("id"))
                .accountId(rs.getLong("account_id"))
                .amount(rs.getBigDecimal("amount"))
                .transactionType(TransactionType.valueOf(rs.getString("transaction_type")))
                .description(rs.getString("description"))
                .timestamp(rs.getTimestamp("created_at").toLocalDateTime())
                .build();
        }
    }
}
```

### JPA Integration

```java
@Configuration
public class JpaIntegrationConfig {

    @Bean
    public IntegrationFlow jpaInboundFlow() {
        return IntegrationFlows
            .from(Jpa.inboundAdapter(entityManagerFactory())
                .jpaQuery("SELECT a FROM Account a WHERE a.lastUpdated < :cutoffDate")
                .parameter("cutoffDate", LocalDateTime.now().minusHours(1))
                .entityClass(Account.class)
                .maxResults(50),
                poller -> poller.fixedDelay(60000))
            .channel("staleAccountChannel")
            .get();
    }

    @Bean
    public IntegrationFlow jpaOutboundFlow() {
        return IntegrationFlows
            .from("auditLogChannel")
            .handle(Jpa.outboundAdapter(entityManagerFactory())
                .entityClass(AuditLog.class)
                .persistMode(PersistMode.PERSIST))
            .get();
    }

    @Bean
    public IntegrationFlow jpaUpdatingFlow() {
        return IntegrationFlows
            .from("accountUpdateChannel")
            .handle(Jpa.updatingOutboundAdapter(entityManagerFactory())
                .entityClass(Account.class)
                .jpaQuery("UPDATE Account a SET a.lastUpdated = :lastUpdated WHERE a.id = :id")
                .parameter("lastUpdated", LocalDateTime.now())
                .parameterExpression("id", "payload.id"))
            .get();
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## REST API Integration

### HTTP Inbound Gateway

```java
@Configuration
@EnableWebMvc
public class HttpIntegrationConfig {

    @Bean
    public IntegrationFlow httpInboundGateway() {
        return IntegrationFlows
            .from(Http.inboundGateway("/api/integration/accounts")
                .requestMapping(mapping -> mapping.methods(HttpMethod.POST))
                .requestPayloadType(AccountRequest.class)
                .replyTimeout(30000))
            .transform(AccountRequest.class, this::validateAndTransform)
            .handle("accountIntegrationService", "processAccount")
            .transform(Account.class, this::transformToResponse)
            .get();
    }

    @Bean
    public IntegrationFlow httpOutboundGateway() {
        return IntegrationFlows
            .from("externalApiChannel")
            .handle(Http.outboundGateway("https://api.external-bank.com/accounts/{accountId}")
                .httpMethod(HttpMethod.GET)
                .uriVariable("accountId", "payload.accountId")
                .expectedResponseType(ExternalAccountResponse.class)
                .headerExpression("Authorization", "'Bearer ' + @tokenService.getToken()")
                .charset("UTF-8"))
            .transform(ExternalAccountResponse.class, this::transformExternalResponse)
            .channel("externalAccountResponseChannel")
            .get();
    }

    @Bean
    public IntegrationFlow restTemplateFlow() {
        return IntegrationFlows
            .from("restApiCallChannel")
            .enrichHeaders(headers -> headers
                .header("Content-Type", "application/json")
                .header("X-Request-ID", () -> UUID.randomUUID().toString()))
            .handle(Http.outboundGateway("https://api.payment-processor.com/payments")
                .httpMethod(HttpMethod.POST)
                .extractPayload(true))
            .channel("paymentResponseChannel")
            .get();
    }

    private AccountRequest validateAndTransform(AccountRequest request) {
        // Validation and transformation logic
        return request;
    }

    private AccountResponse transformToResponse(Account account) {
        return AccountResponse.builder()
            .accountId(account.getId())
            .accountNumber(account.getAccountNumber())
            .balance(account.getBalance())
            .status("SUCCESS")
            .build();
    }

    private Account transformExternalResponse(ExternalAccountResponse response) {
        // Transform external response to internal Account
        return new Account(); // Placeholder
    }
}
```

### WebFlux Integration

```java
@Configuration
public class WebFluxIntegrationConfig {

    @Bean
    public IntegrationFlow webFluxInboundFlow() {
        return IntegrationFlows
            .from(WebFlux.inboundGateway("/api/reactive/transactions")
                .requestMapping(mapping -> mapping.methods(HttpMethod.POST))
                .requestPayloadType(Flux.class, Transaction.class))
            .split()
            .transform(Transaction.class, this::validateTransaction)
            .aggregate()
            .handle("reactiveTransactionService", "processTransactions")
            .get();
    }

    @Bean
    public IntegrationFlow webFluxOutboundFlow() {
        return IntegrationFlows
            .from("reactiveApiCallChannel")
            .handle(WebFlux.outboundGateway("https://api.reactive-service.com/process")
                .httpMethod(HttpMethod.POST)
                .expectedResponseType(String.class))
            .channel("reactiveResponseChannel")
            .get();
    }

    private Transaction validateTransaction(Transaction transaction) {
        // Validation logic
        return transaction;
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Message Queue Integration

### JMS Integration

```java
@Configuration
public class JmsIntegrationConfig {

    @Bean
    public IntegrationFlow jmsInboundFlow() {
        return IntegrationFlows
            .from(Jms.inboundAdapter(connectionFactory())
                .destination("payment.queue")
                .configureListenerContainer(container -> container
                    .sessionTransacted(true)
                    .concurrentConsumers(5)
                    .maxConcurrentConsumers(10)))
            .transform(String.class, this::parsePaymentMessage)
            .channel("paymentProcessingChannel")
            .get();
    }

    @Bean
    public IntegrationFlow jmsOutboundFlow() {
        return IntegrationFlows
            .from("notificationChannel")
            .transform(Notification.class, this::notificationToString)
            .handle(Jms.outboundAdapter(connectionFactory())
                .destination("notification.queue")
                .configureJmsTemplate(template -> template
                    .deliveryPersistent(true)
                    .priority(9)))
            .get();
    }

    @Bean
    public IntegrationFlow jmsGatewayFlow() {
        return IntegrationFlows
            .from("requestReplyChannel")
            .handle(Jms.outboundGateway(connectionFactory())
                .requestDestination("request.queue")
                .replyDestination("reply.queue")
                .replyTimeout(30000))
            .channel("responseChannel")
            .get();
    }

    private PaymentMessage parsePaymentMessage(String message) {
        // Parse JSON or XML message
        return new PaymentMessage(); // Placeholder
    }

    private String notificationToString(Notification notification) {
        // Convert notification to string format
        return notification.toString();
    }
}
```

### RabbitMQ Integration

```java
@Configuration
public class RabbitMqIntegrationConfig {

    @Bean
    public IntegrationFlow rabbitInboundFlow() {
        return IntegrationFlows
            .from(Amqp.inboundAdapter(connectionFactory(), "transaction.queue")
                .messageConverter(new Jackson2JsonMessageConverter())
                .configureContainer(container -> container
                    .acknowledgeMode(AcknowledgeMode.AUTO)
                    .concurrentConsumers(3)
                    .maxConcurrentConsumers(6)))
            .transform(TransactionMessage.class, this::processTransactionMessage)
            .channel("processedTransactionChannel")
            .get();
    }

    @Bean
    public IntegrationFlow rabbitOutboundFlow() {
        return IntegrationFlows
            .from("eventPublishingChannel")
            .transform(DomainEvent.class, this::eventToMessage)
            .handle(Amqp.outboundAdapter(rabbitTemplate())
                .exchangeName("banking.exchange")
                .routingKeyExpression("payload.eventType")
                .confirmCorrelationExpression("headers.id")
                .confirmAckChannel("confirmationChannel"))
            .get();
    }

    @Bean
    public IntegrationFlow rabbitTopicFlow() {
        return IntegrationFlows
            .from(Amqp.inboundAdapter(connectionFactory(), "account.events")
                .configureContainer(container -> container
                    .queueNames("account.created", "account.updated", "account.deleted")))
            .route(Message.class, message -> {
                String routingKey = (String) message.getHeaders().get("amqp_receivedRoutingKey");
                return routingKey.replace(".", "_") + "Channel";
            })
            .get();
    }

    private Transaction processTransactionMessage(TransactionMessage message) {
        // Process the transaction message
        return new Transaction(); // Placeholder
    }

    private String eventToMessage(DomainEvent event) {
        // Convert domain event to message
        return event.toJson();
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Event-Driven Architecture

### Domain Events

```java
// Domain Event
public abstract class DomainEvent {
    private final String eventId;
    private final LocalDateTime timestamp;
    private final String eventType;

    protected DomainEvent(String eventType) {
        this.eventId = UUID.randomUUID().toString();
        this.timestamp = LocalDateTime.now();
        this.eventType = eventType;
    }

    // Getters
}

public class AccountCreatedEvent extends DomainEvent {
    private final Long accountId;
    private final String accountNumber;
    private final Long customerId;
    private final BigDecimal initialBalance;

    public AccountCreatedEvent(Long accountId, String accountNumber,
                              Long customerId, BigDecimal initialBalance) {
        super("ACCOUNT_CREATED");
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.initialBalance = initialBalance;
    }

    // Getters
}

public class TransactionProcessedEvent extends DomainEvent {
    private final Long transactionId;
    private final Long accountId;
    private final BigDecimal amount;
    private final TransactionType transactionType;

    public TransactionProcessedEvent(Long transactionId, Long accountId,
                                   BigDecimal amount, TransactionType transactionType) {
        super("TRANSACTION_PROCESSED");
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.transactionType = transactionType;
    }

    // Getters
}
```

### Event Publishing

```java
@Configuration
public class EventDrivenConfig {

    @Bean
    public IntegrationFlow eventPublishingFlow() {
        return IntegrationFlows
            .from("domainEventChannel")
            .publishSubscribeChannel(pubsub -> pubsub
                .subscribe(auditEventFlow())
                .subscribe(notificationEventFlow())
                .subscribe(reportingEventFlow())
                .subscribe(externalSystemEventFlow()))
            .get();
    }

    private IntegrationFlow auditEventFlow() {
        return flow -> flow
            .filter(DomainEvent.class, this::shouldAudit)
            .transform(DomainEvent.class, this::transformToAuditLog)
            .handle("auditService", "logEvent");
    }

    private IntegrationFlow notificationEventFlow() {
        return flow -> flow
            .filter(DomainEvent.class, this::shouldNotify)
            .transform(DomainEvent.class, this::transformToNotification)
            .handle("notificationService", "sendNotification");
    }

    private IntegrationFlow reportingEventFlow() {
        return flow -> flow
            .filter(DomainEvent.class, this::shouldReport)
            .aggregate(aggregator -> aggregator
                .correlationStrategy(message -> "reporting")
                .releaseStrategy(new TimeoutCountSequenceSizeReleaseStrategy(100, 60000))
                .outputProcessor(this::createReportingBatch))
            .handle("reportingService", "processEventBatch");
    }

    private IntegrationFlow externalSystemEventFlow() {
        return flow -> flow
            .filter(DomainEvent.class, this::shouldSendToExternalSystems)
            .transform(DomainEvent.class, this::transformToExternalFormat)
            .channel("externalSystemChannel");
    }

    private boolean shouldAudit(DomainEvent event) {
        return true; // Audit all events
    }

    private boolean shouldNotify(DomainEvent event) {
        return event instanceof AccountCreatedEvent ||
               event instanceof TransactionProcessedEvent;
    }

    private boolean shouldReport(DomainEvent event) {
        return event instanceof TransactionProcessedEvent;
    }

    private boolean shouldSendToExternalSystems(DomainEvent event) {
        return event instanceof AccountCreatedEvent;
    }

    private AuditLog transformToAuditLog(DomainEvent event) {
        return AuditLog.builder()
            .eventId(event.getEventId())
            .eventType(event.getEventType())
            .timestamp(event.getTimestamp())
            .data(serializeEvent(event))
            .build();
    }

    private Notification transformToNotification(DomainEvent event) {
        return Notification.builder()
            .eventId(event.getEventId())
            .type(event.getEventType())
            .timestamp(event.getTimestamp())
            .message(generateNotificationMessage(event))
            .build();
    }

    private ExternalEvent transformToExternalFormat(DomainEvent event) {
        return ExternalEvent.builder()
            .id(event.getEventId())
            .type(event.getEventType())
            .timestamp(event.getTimestamp())
            .payload(serializeEvent(event))
            .build();
    }
}
```

### Event Sourcing Integration

```java
@Configuration
public class EventSourcingConfig {

    @Bean
    public IntegrationFlow eventStoreFlow() {
        return IntegrationFlows
            .from("eventStoreChannel")
            .transform(DomainEvent.class, this::transformToEventRecord)
            .handle("eventStore", "append")
            .publishSubscribeChannel(pubsub -> pubsub
                .subscribe(eventProjectionFlow())
                .subscribe(eventReplicationFlow()))
            .get();
    }

    private IntegrationFlow eventProjectionFlow() {
        return flow -> flow
            .filter(EventRecord.class, this::shouldProject)
            .handle("projectionService", "updateProjection");
    }

    private IntegrationFlow eventReplicationFlow() {
        return flow -> flow
            .transform(EventRecord.class, this::transformForReplication)
            .channel("replicationChannel");
    }

    @Bean
    public IntegrationFlow eventReplayFlow() {
        return IntegrationFlows
            .from("eventReplayChannel")
            .handle("eventStore", "replay")
            .split()
            .transform(EventRecord.class, this::transformToEvent)
            .aggregate(aggregator -> aggregator
                .correlationStrategy(message -> "replay")
                .releaseStrategy(new SequenceSizeReleaseStrategy()))
            .handle("aggregateService", "rebuildFromEvents")
            .get();
    }

    private EventRecord transformToEventRecord(DomainEvent event) {
        return EventRecord.builder()
            .eventId(event.getEventId())
            .eventType(event.getEventType())
            .aggregateId(extractAggregateId(event))
            .aggregateType(extractAggregateType(event))
            .eventData(serializeEvent(event))
            .timestamp(event.getTimestamp())
            .version(1L)
            .build();
    }

    private boolean shouldProject(EventRecord record) {
        return true; // Project all events
    }

    private ReplicationEvent transformForReplication(EventRecord record) {
        return ReplicationEvent.builder()
            .eventId(record.getEventId())
            .eventType(record.getEventType())
            .data(record.getEventData())
            .timestamp(record.getTimestamp())
            .build();
    }

    private String extractAggregateId(DomainEvent event) {
        // Extract aggregate ID from event
        return ""; // Placeholder
    }

    private String extractAggregateType(DomainEvent event) {
        // Extract aggregate type from event
        return ""; // Placeholder
    }

    private String serializeEvent(DomainEvent event) {
        // Serialize event to JSON
        return ""; // Placeholder
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Batch Processing Integration

### Spring Batch Integration

```java
@Configuration
@EnableBatchProcessing
public class BatchIntegrationConfig {

    @Bean
    public IntegrationFlow batchTriggerFlow() {
        return IntegrationFlows
            .from("batchTriggerChannel")
            .transform(BatchRequest.class, this::prepareBatchJobParameters)
            .handle("jobLaunchingGateway")
            .channel("batchResultChannel")
            .get();
    }

    @Bean
    public JobLaunchingGateway jobLaunchingGateway() {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());

        JobLaunchingGateway gateway = new JobLaunchingGateway(jobLauncher);
        gateway.setJob(dailyTransactionProcessingJob());
        return gateway;
    }

    @Bean
    public IntegrationFlow batchStatusFlow() {
        return IntegrationFlows
            .from("batchStatusChannel")
            .filter(JobExecution.class, execution ->
                execution.getStatus() == BatchStatus.COMPLETED ||
                execution.getStatus() == BatchStatus.FAILED)
            .route(JobExecution.class, execution ->
                execution.getStatus() == BatchStatus.COMPLETED ?
                "batchSuccessChannel" : "batchFailureChannel")
            .get();
    }

    @Bean
    public IntegrationFlow batchChunkFlow() {
        return IntegrationFlows
            .from("transactionBatchChannel")
            .split()
            .transform(Transaction.class, this::validateTransaction)
            .filter(Transaction.class, this::isValidTransaction)
            .aggregate(aggregator -> aggregator
                .correlationStrategy(message -> "chunk")
                .releaseStrategy(new MessageCountReleaseStrategy(1000))
                .outputProcessor(this::createTransactionChunk))
            .handle("batchProcessingService", "processChunk")
            .get();
    }

    @Bean
    public Job dailyTransactionProcessingJob() {
        return jobBuilderFactory.get("dailyTransactionProcessing")
            .start(readTransactionsStep())
            .next(validateTransactionsStep())
            .next(processTransactionsStep())
            .next(writeTransactionsStep())
            .build();
    }

    private JobParameters prepareBatchJobParameters(BatchRequest request) {
        return new JobParametersBuilder()
            .addString("inputFile", request.getInputFile())
            .addString("outputFile", request.getOutputFile())
            .addDate("processDate", new Date())
            .toJobParameters();
    }

    private Transaction validateTransaction(Transaction transaction) {
        // Validation logic
        return transaction;
    }

    private boolean isValidTransaction(Transaction transaction) {
        // Validation logic
        return true;
    }

    private List<Transaction> createTransactionChunk(List<Message<Transaction>> messages) {
        return messages.stream()
            .map(Message::getPayload)
            .collect(Collectors.toList());
    }
}
```

### File-Based Batch Processing

```java
@Configuration
public class FileBatchProcessingConfig {

    @Bean
    public IntegrationFlow dailyReportGenerationFlow() {
        return IntegrationFlows
            .from("reportGenerationTrigger")
            .handle("reportGenerationService", "generateDailyReport")
            .split(Report.class, this::splitReportByCustomer)
            .transform(CustomerReport.class, this::formatReport)
            .aggregate(aggregator -> aggregator
                .correlationStrategy(message -> "daily_report")
                .releaseStrategy(new SequenceSizeReleaseStrategy()))
            .handle(Files.outboundAdapter("/reports/daily")
                .fileNameExpression("'daily_report_' + T(java.time.LocalDate).now() + '.pdf'"))
            .get();
    }

    @Bean
    public IntegrationFlow batchTransactionReconciliationFlow() {
        return IntegrationFlows
            .from(Files.inboundAdapter(new File("/reconciliation/incoming"))
                .patternFilter("reconciliation_*.csv"),
                poller -> poller.fixedDelay(3600000)) // Every hour
            .transform(File.class, this::parseReconciliationFile)
            .split()
            .transform(ReconciliationRecord.class, this::validateRecord)
            .routeToRecipients(route -> route
                .recipient("matchedRecordsChannel", this::isMatched)
                .recipient("unmatchedRecordsChannel", this::isUnmatched)
                .recipient("exceptionRecordsChannel", this::hasException))
            .get();
    }

    @Bean
    public IntegrationFlow endOfDayProcessingFlow() {
        return IntegrationFlows
            .from("endOfDayTrigger")
            .gateway("interestCalculationChannel", gateway -> gateway.replyTimeout(300000))
            .gateway("accountBalanceUpdateChannel", gateway -> gateway.replyTimeout(300000))
            .gateway("dailyReportGenerationChannel", gateway -> gateway.replyTimeout(300000))
            .handle("endOfDayService", "completeProcessing")
            .get();
    }

    private List<CustomerReport> splitReportByCustomer(Report report) {
        return report.getCustomerReports();
    }

    private String formatReport(CustomerReport report) {
        // Format report for output
        return report.toString();
    }

    private List<ReconciliationRecord> parseReconciliationFile(File file) {
        // Parse reconciliation file
        return Collections.emptyList(); // Placeholder
    }

    private ReconciliationRecord validateRecord(ReconciliationRecord record) {
        // Validation logic
        return record;
    }

    private boolean isMatched(ReconciliationRecord record) {
        return record.getStatus() == ReconciliationStatus.MATCHED;
    }

    private boolean isUnmatched(ReconciliationRecord record) {
        return record.getStatus() == ReconciliationStatus.UNMATCHED;
    }

    private boolean hasException(ReconciliationRecord record) {
        return record.getStatus() == ReconciliationStatus.EXCEPTION;
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Error Handling and Retry

### Error Handling Configuration

```java
@Configuration
public class ErrorHandlingConfig {

    @Bean
    public IntegrationFlow errorHandlingFlow() {
        return IntegrationFlows
            .from("inputChannel")
            .transform(String.class, this::processMessage)
            .channel(MessageChannels.direct("outputChannel")
                .interceptor(errorHandlingInterceptor()))
            .get();
    }

    @Bean
    public IntegrationFlow errorRecoveryFlow() {
        return IntegrationFlows
            .from("errorChannel")
            .filter(Exception.class, this::isRecoverableError)
            .delay("retryDelayGroup", delayExpression())
            .transform(ErrorMessage.class, this::extractOriginalMessage)
            .channel("retryChannel")
            .get();
    }

    @Bean
    public IntegrationFlow deadLetterFlow() {
        return IntegrationFlows
            .from("errorChannel")
            .filter(Exception.class, this::isNonRecoverableError)
            .transform(ErrorMessage.class, this::createDeadLetterMessage)
            .handle("deadLetterService", "handleDeadLetter")
            .get();
    }

    @ServiceActivator(inputChannel = "errorChannel")
    public void handleError(ErrorMessage errorMessage) {
        Throwable cause = errorMessage.getPayload();
        Message<?> originalMessage = errorMessage.getOriginalMessage();

        logger.error("Error processing message: {}", originalMessage, cause);

        // Determine error handling strategy
        if (isRetryableError(cause)) {
            handleRetryableError(originalMessage, cause);
        } else if (isCompensatableError(cause)) {
            handleCompensatableError(originalMessage, cause);
        } else {
            handleFatalError(originalMessage, cause);
        }
    }

    private boolean isRecoverableError(Exception exception) {
        return exception instanceof TransientDataAccessException ||
               exception instanceof ConnectTimeoutException ||
               exception instanceof ServiceUnavailableException;
    }

    private boolean isNonRecoverableError(Exception exception) {
        return !isRecoverableError(exception);
    }

    private boolean isRetryableError(Throwable cause) {
        return isRecoverableError((Exception) cause);
    }

    private boolean isCompensatableError(Throwable cause) {
        return cause instanceof BusinessException;
    }

    private void handleRetryableError(Message<?> message, Throwable cause) {
        // Implement retry logic
        Integer retryCount = (Integer) message.getHeaders().get("retryCount");
        if (retryCount == null) retryCount = 0;

        if (retryCount < 3) {
            // Retry with exponential backoff
            Message<?> retryMessage = MessageBuilder.fromMessage(message)
                .setHeader("retryCount", retryCount + 1)
                .setHeader("retryDelay", calculateDelay(retryCount))
                .build();

            // Send to retry channel
            messagingTemplate.send("retryChannel", retryMessage);
        } else {
            // Max retries exceeded, send to dead letter
            handleFatalError(message, cause);
        }
    }

    private void handleCompensatableError(Message<?> message, Throwable cause) {
        // Implement compensation logic
        try {
            compensationService.compensate(message);
        } catch (Exception e) {
            logger.error("Compensation failed for message: {}", message, e);
            handleFatalError(message, cause);
        }
    }

    private void handleFatalError(Message<?> message, Throwable cause) {
        // Send to dead letter queue
        DeadLetterMessage deadLetter = DeadLetterMessage.builder()
            .originalMessage(message)
            .error(cause.getMessage())
            .timestamp(LocalDateTime.now())
            .build();

        messagingTemplate.send("deadLetterChannel", deadLetter);
    }

    private long calculateDelay(int retryCount) {
        return (long) Math.pow(2, retryCount) * 1000; // Exponential backoff
    }

    private Expression delayExpression() {
        return new SpelExpressionParser().parseExpression("headers.retryDelay ?: 1000");
    }

    private Message<?> extractOriginalMessage(ErrorMessage errorMessage) {
        return errorMessage.getOriginalMessage();
    }

    private DeadLetterMessage createDeadLetterMessage(ErrorMessage errorMessage) {
        return DeadLetterMessage.builder()
            .originalMessage(errorMessage.getOriginalMessage())
            .error(errorMessage.getPayload().getMessage())
            .timestamp(LocalDateTime.now())
            .build();
    }
}
```

### Circuit Breaker Integration

```java
@Configuration
public class CircuitBreakerConfig {

    @Bean
    public IntegrationFlow circuitBreakerFlow() {
        return IntegrationFlows
            .from("externalServiceChannel")
            .handle("circuitBreakerService", "callExternalService")
            .get();
    }

    @Component
    public static class CircuitBreakerService {

        private final CircuitBreaker circuitBreaker;
        private final ExternalService externalService;

        public CircuitBreakerService(ExternalService externalService) {
            this.externalService = externalService;
            this.circuitBreaker = CircuitBreaker.ofDefaults("externalService");

            circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                    logger.info("Circuit breaker state transition: {} -> {}",
                        event.getStateTransition().getFromState(),
                        event.getStateTransition().getToState()));
        }

        public String callExternalService(String request) {
            return circuitBreaker.executeSupplier(() -> {
                try {
                    return externalService.call(request);
                } catch (Exception e) {
                    logger.error("External service call failed", e);
                    throw new ExternalServiceException("Service unavailable", e);
                }
            });
        }
    }

    @Component
    public static class FallbackService {

        @ServiceActivator(inputChannel = "fallbackChannel")
        public String handleFallback(Message<?> message) {
            logger.warn("Fallback triggered for message: {}", message);
            return "Service temporarily unavailable. Please try again later.";
        }
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Monitoring and Observability

### Metrics and Monitoring

```java
@Configuration
public class MonitoringConfig {

    @Bean
    public IntegrationFlow monitoredFlow() {
        return IntegrationFlows
            .from("monitoredInputChannel")
            .log(LoggingHandler.Level.INFO, "integration.flow",
                 message -> "Processing message: " + message.getHeaders().get("id"))
            .transform(String.class, this::processWithMetrics)
            .handle("businessService", "process")
            .log(LoggingHandler.Level.INFO, "integration.flow",
                 message -> "Completed processing: " + message.getHeaders().get("id"))
            .get();
    }

    @Bean
    public IntegrationFlow metricsCollectionFlow() {
        return IntegrationFlows
            .from("metricsChannel")
            .aggregate(aggregator -> aggregator
                .correlationStrategy(message -> "metrics")
                .releaseStrategy(new TimeoutCountSequenceSizeReleaseStrategy(100, 60000))
                .outputProcessor(this::calculateMetrics))
            .handle("metricsService", "recordMetrics")
            .get();
    }

    @EventListener
    public void handleIntegrationEvent(ApplicationEvent event) {
        if (event instanceof MessagingException) {
            MessagingException ex = (MessagingException) event;
            meterRegistry.counter("integration.errors",
                "cause", ex.getCause().getClass().getSimpleName()).increment();
        }
    }

    private String processWithMetrics(String input) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            String result = processInput(input);
            meterRegistry.counter("integration.messages.processed", "status", "success").increment();
            return result;
        } catch (Exception e) {
            meterRegistry.counter("integration.messages.processed", "status", "error").increment();
            throw e;
        } finally {
            sample.stop(Timer.builder("integration.processing.duration")
                .register(meterRegistry));
        }
    }

    private MetricsSummary calculateMetrics(List<Message<?>> messages) {
        return MetricsSummary.builder()
            .messageCount(messages.size())
            .averageProcessingTime(calculateAverageProcessingTime(messages))
            .errorCount(countErrors(messages))
            .timestamp(LocalDateTime.now())
            .build();
    }
}
```

### Health Checks

```java
@Component
public class IntegrationHealthIndicator implements HealthIndicator {

    private final MessageChannel healthCheckChannel;
    private final MessagingTemplate messagingTemplate;

    @Override
    public Health health() {
        try {
            // Test message flow
            Message<String> testMessage = MessageBuilder
                .withPayload("health-check")
                .setHeader("test", true)
                .build();

            Message<?> response = messagingTemplate.sendAndReceive(healthCheckChannel, testMessage);

            if (response != null) {
                return Health.up()
                    .withDetail("integration.flow", "operational")
                    .withDetail("last.check", LocalDateTime.now())
                    .build();
            } else {
                return Health.down()
                    .withDetail("integration.flow", "no response")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("integration.flow", "failed")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}

@Component
public class ExternalServiceHealthCheck implements HealthIndicator {

    private final ExternalService externalService;
    private final CircuitBreaker circuitBreaker;

    @Override
    public Health health() {
        CircuitBreaker.State state = circuitBreaker.getState();

        Health.Builder healthBuilder = state == CircuitBreaker.State.CLOSED ?
            Health.up() : Health.down();

        return healthBuilder
            .withDetail("circuit.breaker.state", state.toString())
            .withDetail("failure.rate", circuitBreaker.getMetrics().getFailureRate())
            .withDetail("calls.permitted", circuitBreaker.getState() != CircuitBreaker.State.OPEN)
            .build();
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Security in Integration

### Message Security

```java
@Configuration
public class SecurityConfig {

    @Bean
    public IntegrationFlow secureFlow() {
        return IntegrationFlows
            .from("secureInputChannel")
            .transform(this::decryptMessage)
            .transform(this::validateSignature)
            .filter(this::authorizeMessage)
            .handle("secureService", "process")
            .transform(this::signResponse)
            .transform(this::encryptResponse)
            .channel("secureOutputChannel")
            .get();
    }

    @Bean
    public IntegrationFlow sslClientFlow() {
        HttpClient httpClient = HttpClient.create()
            .secure(sslContextSpec -> sslContextSpec
                .sslContext(createSSLContext())
                .handshakeTimeout(Duration.ofSeconds(30)));

        return IntegrationFlows
            .from("sslClientChannel")
            .handle(WebFlux.outboundGateway("https://secure-api.bank.com/api")
                .httpMethod(HttpMethod.POST)
                .expectedResponseType(String.class))
            .get();
    }

    private String decryptMessage(String encryptedMessage) {
        try {
            return encryptionService.decrypt(encryptedMessage);
        } catch (Exception e) {
            throw new SecurityException("Message decryption failed", e);
        }
    }

    private String validateSignature(String message) {
        if (!signatureService.validate(message)) {
            throw new SecurityException("Message signature validation failed");
        }
        return message;
    }

    private boolean authorizeMessage(String message) {
        String userId = extractUserId(message);
        return authorizationService.isAuthorized(userId, "PROCESS_PAYMENT");
    }

    private String signResponse(String response) {
        return signatureService.sign(response);
    }

    private String encryptResponse(String response) {
        return encryptionService.encrypt(response);
    }

    private SSLContext createSSLContext() {
        // Create SSL context for secure communication
        return null; // Placeholder
    }
}
```

### OAuth Integration

```java
@Configuration
public class OAuthIntegrationConfig {

    @Bean
    public IntegrationFlow oauthProtectedFlow() {
        return IntegrationFlows
            .from("oauthChannel")
            .enrichHeaders(headers -> headers
                .headerExpression("Authorization",
                    "'Bearer ' + @oauthTokenService.getAccessToken()"))
            .handle(Http.outboundGateway("https://api.oauth-protected.com/data")
                .httpMethod(HttpMethod.GET)
                .expectedResponseType(String.class))
            .get();
    }

    @Component
    public static class OAuthTokenService {

        private String accessToken;
        private LocalDateTime tokenExpiry;

        public String getAccessToken() {
            if (accessToken == null || isTokenExpired()) {
                refreshToken();
            }
            return accessToken;
        }

        private void refreshToken() {
            // OAuth token refresh logic
            try {
                OAuthTokenResponse response = oauthClient.getToken(
                    clientId, clientSecret, "client_credentials");

                this.accessToken = response.getAccessToken();
                this.tokenExpiry = LocalDateTime.now()
                    .plusSeconds(response.getExpiresIn() - 60); // Refresh 1 minute early

            } catch (Exception e) {
                throw new SecurityException("Failed to refresh OAuth token", e);
            }
        }

        private boolean isTokenExpired() {
            return tokenExpiry == null || LocalDateTime.now().isAfter(tokenExpiry);
        }
    }
}
```


[⬆️ Back to Top](#table-of-contents)
## Banking Domain Examples

### Complete Payment Processing System

```java
@Configuration
public class PaymentProcessingIntegrationConfig {

    // Main payment processing flow
    @Bean
    public IntegrationFlow paymentProcessingFlow() {
        return IntegrationFlows
            .from("paymentRequestChannel")
            .transform(PaymentRequest.class, this::validatePaymentRequest)
            .enrichHeaders(headers -> headers
                .header("correlationId", UUID.randomUUID().toString())
                .header("timestamp", System.currentTimeMillis()))
            .route(PaymentRequest.class, this::routeByPaymentType)
            .get();
    }

    // Domestic payment processing
    @Bean
    public IntegrationFlow domesticPaymentFlow() {
        return IntegrationFlows
            .from("domesticPaymentChannel")
            .filter(PaymentRequest.class, this::validateDomesticLimits)
            .handle("accountService", "validateAccount")
            .handle("fraudDetectionService", "checkFraud")
            .handle("domesticPaymentService", "processPayment")
            .publishSubscribeChannel(pubsub -> pubsub
                .subscribe(paymentNotificationFlow())
                .subscribe(paymentAuditFlow())
                .subscribe(paymentReportingFlow()))
            .get();
    }

    // International payment processing with SWIFT
    @Bean
    public IntegrationFlow internationalPaymentFlow() {
        return IntegrationFlows
            .from("internationalPaymentChannel")
            .filter(PaymentRequest.class, this::validateInternationalLimits)
            .enrich(enricher -> enricher
                .requestChannel("exchangeRateChannel")
                .requestPayloadExpression("currency")
                .propertyExpression("exchangeRate", "payload.rate"))
            .handle("complianceService", "checkSanctions")
            .handle("swiftService", "sendSwiftMessage")
            .delay("internationalPaymentGroup", Duration.ofMinutes(5))
            .handle("internationalPaymentService", "processPayment")
            .channel("paymentCompletionChannel")
            .get();
    }

    // Real-time payment processing (RTP)
    @Bean
    public IntegrationFlow realTimePaymentFlow() {
        return IntegrationFlows
            .from("rtpChannel")
            .transform(PaymentRequest.class, this::formatForRTP)
            .handle(Http.outboundGateway("https://rtp.clearinghouse.com/api/payments")
                .httpMethod(HttpMethod.POST)
                .expectedResponseType(RTPResponse.class)
                .headerExpression("Authorization", "'Bearer ' + @rtpTokenService.getToken()")
                .timeout(Duration.ofSeconds(10))) // RTP requires fast response
            .filter(RTPResponse.class, response -> response.getStatus().equals("ACCEPTED"))
            .transform(RTPResponse.class, this::transformRTPResponse)
            .channel("paymentCompletionChannel")
            .get();
    }

    // Payment notification flow
    private IntegrationFlow paymentNotificationFlow() {
        return flow -> flow
            .transform(PaymentResult.class, this::createNotification)
            .route(Notification.class, notification -> {
                switch (notification.getType()) {
                    case SMS:
                        return "smsNotificationChannel";
                    case EMAIL:
                        return "emailNotificationChannel";
                    case PUSH:
                        return "pushNotificationChannel";
                    default:
                        return "defaultNotificationChannel";
                }
            });
    }

    // Payment audit flow
    private IntegrationFlow paymentAuditFlow() {
        return flow -> flow
            .transform(PaymentResult.class, this::createAuditRecord)
            .handle("auditService", "logPayment");
    }

    // Payment reporting flow
    private IntegrationFlow paymentReportingFlow() {
        return flow -> flow
            .aggregate(aggregator -> aggregator
                .correlationStrategy(message -> "daily_payments")
                .releaseStrategy(new TimeoutCountSequenceSizeReleaseStrategy(1000, 3600000))
                .outputProcessor(this::createPaymentSummary))
            .handle("reportingService", "updateDailyStats");
    }

    // Wire transfer flow
    @Bean
    public IntegrationFlow wireTransferFlow() {
        return IntegrationFlows
            .from("wireTransferChannel")
            .filter(PaymentRequest.class, this::validateWireTransferLimits)
            .transform(PaymentRequest.class, this::addWireTransferMetadata)
            .handle("complianceService", "performEnhancedDueDiligence")
            .handle("fedwireService", "sendFedwire")
            .handle("wireTransferService", "processWireTransfer")
            .channel("paymentCompletionChannel")
            .get();
    }

    // Batch payment processing
    @Bean
    public IntegrationFlow batchPaymentFlow() {
        return IntegrationFlows
            .from(Files.inboundAdapter(new File("/batch/incoming"))
                .patternFilter("payments_*.csv"),
                poller -> poller.fixedDelay(300000)) // Every 5 minutes
            .transform(File.class, this::parseBatchFile)
            .split()
            .transform(PaymentRequest.class, this::validateBatchPayment)
            .aggregate(aggregator -> aggregator
                .correlationStrategy(message -> message.getHeaders().get("batchId"))
                .releaseStrategy(new SequenceSizeReleaseStrategy())
                .outputProcessor(this::processBatchPayments))
            .handle("batchPaymentService", "processBatch")
            .channel("batchCompletionChannel")
            .get();
    }

    // Account statement generation
    @Bean
    public IntegrationFlow statementGenerationFlow() {
        return IntegrationFlows
            .from("statementRequestChannel")
            .transform(StatementRequest.class, this::validateStatementRequest)
            .handle("transactionService", "getTransactionHistory")
            .transform(List.class, this::formatStatement)
            .handle(Files.outboundAdapter("/statements/generated")
                .fileNameExpression("'statement_' + headers.accountId + '_' + T(java.time.LocalDate).now() + '.pdf'"))
            .publishSubscribeChannel(pubsub -> pubsub
                .subscribe(emailStatementFlow())
                .subscribe(archiveStatementFlow()))
            .get();
    }

    private IntegrationFlow emailStatementFlow() {
        return flow -> flow
            .transform(StatementFile.class, this::createEmailWithAttachment)
            .handle("emailService", "sendStatement");
    }

    private IntegrationFlow archiveStatementFlow() {
        return flow -> flow
            .handle("documentArchiveService", "archiveStatement");
    }

    // Helper methods
    private String routeByPaymentType(PaymentRequest request) {
        switch (request.getType()) {
            case DOMESTIC:
                return "domesticPaymentChannel";
            case INTERNATIONAL:
                return "internationalPaymentChannel";
            case REAL_TIME:
                return "rtpChannel";
            case WIRE:
                return "wireTransferChannel";
            default:
                throw new IllegalArgumentException("Unknown payment type: " + request.getType());
        }
    }

    private boolean validateDomesticLimits(PaymentRequest request) {
        return request.getAmount().compareTo(BigDecimal.valueOf(25000)) <= 0;
    }

    private boolean validateInternationalLimits(PaymentRequest request) {
        return request.getAmount().compareTo(BigDecimal.valueOf(10000)) <= 0;
    }

    private boolean validateWireTransferLimits(PaymentRequest request) {
        return request.getAmount().compareTo(BigDecimal.valueOf(100000)) <= 0;
    }

    // Additional helper methods would be implemented here...
}
```


[⬆️ Back to Top](#table-of-contents)
## Testing Integration Flows

### Unit Testing

```java
@SpringBootTest
@DirtiesContext
class IntegrationFlowTest {

    @Autowired
    private MessageChannel inputChannel;

    @Autowired
    private PollableChannel outputChannel;

    @Autowired
    private MessageCollector messageCollector;

    @MockBean
    private ExternalService externalService;

    @Test
    void shouldProcessMessageSuccessfully() {
        // Given
        String inputMessage = "test message";
        when(externalService.process(any())).thenReturn("processed: " + inputMessage);

        // When
        inputChannel.send(MessageBuilder.withPayload(inputMessage).build());

        // Then
        Message<?> result = outputChannel.receive(5000);
        assertThat(result).isNotNull();
        assertThat(result.getPayload()).isEqualTo("processed: test message");
    }

    @Test
    void shouldHandleErrorsGracefully() {
        // Given
        String inputMessage = "error message";
        when(externalService.process(any())).thenThrow(new RuntimeException("Service error"));

        // When
        inputChannel.send(MessageBuilder.withPayload(inputMessage).build());

        // Then
        Message<?> errorMessage = messageCollector.forChannel(errorChannel).poll();
        assertThat(errorMessage).isNotNull();
        assertThat(errorMessage.getPayload()).isInstanceOf(Exception.class);
    }

    @Test
    void shouldSplitAndAggregateCorrectly() {
        // Given
        List<String> messages = Arrays.asList("msg1", "msg2", "msg3");

        // When
        inputChannel.send(MessageBuilder.withPayload(messages).build());

        // Then
        Message<?> result = outputChannel.receive(5000);
        assertThat(result).isNotNull();
        assertThat(result.getPayload()).isInstanceOf(List.class);

        List<?> resultList = (List<?>) result.getPayload();
        assertThat(resultList).hasSize(3);
    }
}
```

### Integration Testing

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.integration.channels.auto-create=false",
    "spring.integration.channels.max-unicast-subscribers=2",
    "spring.integration.channels.max-broadcast-subscribers=3"
})
class PaymentIntegrationTest {

    @Autowired
    private PaymentGateway paymentGateway;

    @Autowired
    private TestChannels testChannels;

    @Test
    void shouldProcessDomesticPaymentEndToEnd() {
        // Given
        PaymentRequest request = PaymentRequest.builder()
            .fromAccount("123456789")
            .toAccount("987654321")
            .amount(BigDecimal.valueOf(1000))
            .type(PaymentType.DOMESTIC)
            .build();

        // When
        PaymentResult result = paymentGateway.processPayment(request);

        // Then
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.getTransactionId()).isNotNull();

        // Verify notifications were sent
        Message<?> notification = testChannels.notificationOutput().receive(5000);
        assertThat(notification).isNotNull();
        assertThat(notification.getPayload()).isInstanceOf(PaymentNotification.class);

        // Verify audit record was created
        Message<?> auditRecord = testChannels.auditOutput().receive(5000);
        assertThat(auditRecord).isNotNull();
    }

    @Test
    void shouldRejectPaymentWhenFraudDetected() {
        // Given
        PaymentRequest suspiciousRequest = PaymentRequest.builder()
            .fromAccount("123456789")
            .toAccount("999999999") // Suspicious account
            .amount(BigDecimal.valueOf(50000))
            .type(PaymentType.DOMESTIC)
            .build();

        // When
        PaymentResult result = paymentGateway.processPayment(suspiciousRequest);

        // Then
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.REJECTED);
        assertThat(result.getRejectionReason()).contains("fraud");
    }
}
```

### Load Testing

```java
@Test
void shouldHandleHighVolumePayments() {
    int numberOfPayments = 10000;
    CountDownLatch latch = new CountDownLatch(numberOfPayments);

    // Create thread pool for concurrent processing
    ExecutorService executor = Executors.newFixedThreadPool(50);

    // Submit payment requests
    for (int i = 0; i < numberOfPayments; i++) {
        executor.submit(() -> {
            try {
                PaymentRequest request = createRandomPaymentRequest();
                PaymentResult result = paymentGateway.processPayment(request);
                assertThat(result.getStatus()).isIn(PaymentStatus.COMPLETED, PaymentStatus.PENDING);
            } finally {
                latch.countDown();
            }
        });
    }

    // Wait for completion
    boolean completed = latch.await(60, TimeUnit.SECONDS);
    assertThat(completed).isTrue();

    executor.shutdown();
}
```


[⬆️ Back to Top](#table-of-contents)
## Best Practices

### Design Principles

1. **Loose Coupling**: Use message channels to decouple components
2. **Single Responsibility**: Each integration component should have one responsibility
3. **Idempotency**: Design flows to handle duplicate messages gracefully
4. **Error Handling**: Implement comprehensive error handling and recovery
5. **Monitoring**: Add observability to all integration flows

### Performance Optimization

```java
@Configuration
public class PerformanceOptimizationConfig {

    // Use direct channels for synchronous processing
    @Bean
    public MessageChannel fastProcessingChannel() {
        return MessageChannels.direct().get();
    }

    // Use queue channels with appropriate capacity
    @Bean
    public MessageChannel bufferedChannel() {
        return MessageChannels.queue(1000).get();
    }

    // Use executor channels for parallel processing
    @Bean
    public MessageChannel parallelProcessingChannel() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(100);
        executor.initialize();

        return MessageChannels.executor(executor).get();
    }

    // Optimize aggregators with appropriate release strategies
    @Bean
    public IntegrationFlow optimizedAggregatorFlow() {
        return IntegrationFlows
            .from("aggregatorInputChannel")
            .aggregate(aggregator -> aggregator
                .correlationStrategy(message -> message.getHeaders().get("correlationId"))
                .releaseStrategy(new MessageCountReleaseStrategy(100))
                .sendTimeout(5000)
                .groupTimeout(30000))
            .get();
    }
}
```

### Security Best Practices

1. **Message Encryption**: Encrypt sensitive data in messages
2. **Authentication**: Implement proper authentication for external systems
3. **Authorization**: Control access to integration endpoints
4. **Audit Logging**: Log all security-relevant events
5. **Secure Transport**: Use TLS for all external communication


[⬆️ Back to Top](#table-of-contents)
## Common Pitfalls

### 1. Memory Leaks

```java
// ❌ Unbounded aggregators can cause memory leaks
@Bean
public IntegrationFlow problematicFlow() {
    return IntegrationFlows
        .from("inputChannel")
        .aggregate() // No release strategy!
        .get();
}

// ✅ Always define release strategies
@Bean
public IntegrationFlow properFlow() {
    return IntegrationFlows
        .from("inputChannel")
        .aggregate(aggregator -> aggregator
            .releaseStrategy(new MessageCountReleaseStrategy(100))
            .groupTimeout(60000))
        .get();
}
```

### 2. Error Swallowing

```java
// ❌ Don't ignore errors
@ServiceActivator(inputChannel = "processChannel")
public void processMessage(String message) {
    try {
        businessService.process(message);
    } catch (Exception e) {
        // Silently ignoring errors!
    }
}

// ✅ Proper error handling
@ServiceActivator(inputChannel = "processChannel")
public void processMessage(String message) {
    try {
        businessService.process(message);
    } catch (BusinessException e) {
        logger.warn("Business error processing message: {}", message, e);
        throw e; // Re-throw for error channel handling
    } catch (Exception e) {
        logger.error("Unexpected error processing message: {}", message, e);
        throw new MessagingException("Processing failed", e);
    }
}
```

### 3. Blocking Operations

```java
// ❌ Blocking operations in integration flows
@ServiceActivator(inputChannel = "processChannel")
public String processMessage(String message) {
    // This blocks the thread!
    return restTemplate.postForObject("http://slow-service.com/api", message, String.class);
}

// ✅ Use async processing
@ServiceActivator(inputChannel = "processChannel", outputChannel = "responseChannel")
@Async
public CompletableFuture<String> processMessage(String message) {
    return CompletableFuture.supplyAsync(() ->
        restTemplate.postForObject("http://slow-service.com/api", message, String.class));
}
```


[⬆️ Back to Top](#table-of-contents)
## Interview Questions

### Beginner Level

**Q1: What are the main components of Spring Integration?**

A: The main components are:
- **Message**: Data wrapper with headers and payload
- **Channel**: Conduit for messages between components
- **Endpoint**: Component that connects application code to messaging framework
- **Transformer**: Converts message content or structure
- **Filter**: Determines which messages pass through
- **Router**: Directs messages to different channels
- **Splitter**: Breaks down composite messages
- **Aggregator**: Combines multiple messages

**Q2: What's the difference between direct and queue channels?**

A:
- **Direct Channel**: Synchronous, single-threaded, point-to-point
- **Queue Channel**: Asynchronous, uses internal queue, allows buffering

**Q3: How do you handle errors in Spring Integration?**

A: Through:
- Error channels
- Exception handling in service activators
- Retry mechanisms
- Dead letter queues
- Circuit breakers

### Intermediate Level

**Q4: Explain the Enterprise Integration Patterns you've used.**

A: Common patterns include:
- **Message Router**: Route messages based on content
- **Content-Based Router**: Route based on message content
- **Scatter-Gather**: Send to multiple recipients and aggregate responses
- **Message Filter**: Filter messages based on criteria
- **Message Translator**: Transform message format
- **Claim Check**: Store large payloads separately

**Q5: How do you implement transaction management in Spring Integration?**

A: Through:
- Transactional endpoints with @Transactional
- Transactional channel adapters
- Pseudo-transactions for file operations
- JMS transaction managers
- Database transaction managers

### Advanced Level

**Q6: How would you design a high-throughput payment processing system?**

A: Design considerations:
- Parallel processing channels
- Load balancing across multiple instances
- Circuit breakers for external services
- Async processing where possible
- Efficient aggregation strategies
- Proper error handling and compensation
- Comprehensive monitoring and alerting

**Q7: How do you ensure message delivery guarantees?**

A: Through:
- Persistent message stores
- Transaction management
- Message acknowledgments
- Retry mechanisms with exponential backoff
- Dead letter queues
- Idempotent message processing

**Q8: Describe your approach to monitoring integration flows.**

A: Monitoring strategy includes:
- Message flow metrics (throughput, latency, errors)
- Channel metrics (queue depth, processing time)
- Health checks for external dependencies
- Business metrics (payment success rates)
- Alerting on anomalies
- Distributed tracing for complex flows


[⬆️ Back to Top](#table-of-contents)
This comprehensive guide covers the essential aspects of Spring Boot Integration Patterns, providing practical examples and best practices for building robust, scalable integration solutions in banking and financial services.