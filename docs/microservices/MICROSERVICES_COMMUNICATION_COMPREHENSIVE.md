# Microservices Communication Patterns - Comprehensive Guide

## 📖 Table of Contents

1. [Introduction to Microservices Communication](#introduction)
2. [Synchronous Communication Patterns](#synchronous-patterns)
3. [Asynchronous Communication Patterns](#asynchronous-patterns)
4. [Event-Driven Architecture](#event-driven)
5. [Service Discovery & Load Balancing](#service-discovery)
6. [Circuit Breaker & Resilience Patterns](#resilience-patterns)
7. [Data Consistency Patterns](#data-consistency)
8. [Security in Microservices Communication](#security)
9. [Monitoring & Observability](#monitoring)
10. [Banking-Specific Communication Patterns](#banking-patterns)

---

## 🌐 Introduction to Microservices Communication {#introduction}

Microservices communication is the backbone of distributed systems. In banking applications, services need to communicate reliably, securely, and efficiently while maintaining data consistency and handling failures gracefully.

### Key Communication Challenges in Banking:
- **Data Consistency**: Ensuring transactions are ACID compliant across services
- **Security**: Protecting sensitive financial data in transit
- **Performance**: Low-latency requirements for real-time transactions
- **Reliability**: High availability and fault tolerance
- **Compliance**: Meeting regulatory requirements for audit trails

### Communication Patterns Overview:

```java
/**
 * Banking Microservices Architecture Overview
 *
 * Services:
 * - Account Service: Manages customer accounts
 * - Transaction Service: Processes financial transactions
 * - Fraud Detection Service: Monitors suspicious activities
 * - Notification Service: Sends alerts and confirmations
 * - Audit Service: Maintains compliance logs
 * - Customer Service: Manages customer information
 */

@Component
public class BankingServicesOverview {

    // Service endpoints in a typical banking microservices architecture
    private static final String ACCOUNT_SERVICE = "http://account-service";
    private static final String TRANSACTION_SERVICE = "http://transaction-service";
    private static final String FRAUD_SERVICE = "http://fraud-detection-service";
    private static final String NOTIFICATION_SERVICE = "http://notification-service";
    private static final String AUDIT_SERVICE = "http://audit-service";
    private static final String CUSTOMER_SERVICE = "http://customer-service";

    /**
     * Communication Flow for Money Transfer:
     * 1. Transaction Service → Account Service (check balances)
     * 2. Transaction Service → Fraud Service (fraud check)
     * 3. Transaction Service → Account Service (update balances)
     * 4. Transaction Service → Audit Service (log transaction)
     * 5. Transaction Service → Notification Service (send confirmations)
     */
}
```

---

## 🔄 Synchronous Communication Patterns {#synchronous-patterns}

Synchronous communication involves direct, blocking calls between services where the caller waits for a response.

### 1. REST API Communication

#### Basic REST Client Implementation

```java
@Service
public class AccountServiceClient {

    private final RestTemplate restTemplate;
    private final String accountServiceUrl;

    public AccountServiceClient(RestTemplate restTemplate,
                               @Value("${services.account.url}") String accountServiceUrl) {
        this.restTemplate = restTemplate;
        this.accountServiceUrl = accountServiceUrl;
    }

    /**
     * Retrieves account information synchronously
     *
     * @param accountNumber the account to retrieve
     * @return AccountInfo object with account details
     * @throws AccountServiceException if account not found or service unavailable
     */
    public AccountInfo getAccount(String accountNumber) {
        try {
            String url = accountServiceUrl + "/accounts/" + accountNumber;

            ResponseEntity<AccountInfo> response = restTemplate.getForEntity(
                url,
                AccountInfo.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                throw new AccountServiceException("Failed to retrieve account: " + response.getStatusCode());
            }

        } catch (ResourceAccessException e) {
            throw new AccountServiceException("Account service unavailable", e);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new AccountNotFoundException("Account not found: " + accountNumber);
            }
            throw new AccountServiceException("Account service error: " + e.getMessage(), e);
        }
    }

    /**
     * Updates account balance with optimistic locking
     *
     * @param accountNumber account to update
     * @param newBalance new balance amount
     * @param version current version for optimistic locking
     * @return updated AccountInfo
     */
    public AccountInfo updateBalance(String accountNumber, BigDecimal newBalance, Long version) {
        try {
            String url = accountServiceUrl + "/accounts/" + accountNumber + "/balance";

            BalanceUpdateRequest request = new BalanceUpdateRequest(newBalance, version);

            ResponseEntity<AccountInfo> response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                new HttpEntity<>(request),
                AccountInfo.class
            );

            return response.getBody();

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new OptimisticLockException("Account was modified by another transaction");
            }
            throw new AccountServiceException("Failed to update balance", e);
        }
    }

    /**
     * Reserves funds for a transaction (2-phase commit pattern)
     *
     * @param accountNumber account to reserve funds from
     * @param amount amount to reserve
     * @param transactionId unique transaction identifier
     * @return reservation ID for later commit/rollback
     */
    public String reserveFunds(String accountNumber, BigDecimal amount, String transactionId) {
        try {
            String url = accountServiceUrl + "/accounts/" + accountNumber + "/reserve";

            FundsReservationRequest request = new FundsReservationRequest(
                amount,
                transactionId,
                Duration.ofMinutes(5) // Reservation timeout
            );

            ResponseEntity<FundsReservationResponse> response = restTemplate.postForEntity(
                url,
                request,
                FundsReservationResponse.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED) {
                return response.getBody().getReservationId();
            } else {
                throw new AccountServiceException("Failed to reserve funds");
            }

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new InsufficientFundsException("Insufficient funds for reservation");
            }
            throw new AccountServiceException("Failed to reserve funds", e);
        }
    }

    /**
     * Commits a funds reservation
     */
    public void commitReservation(String reservationId) {
        try {
            String url = accountServiceUrl + "/reservations/" + reservationId + "/commit";
            restTemplate.postForObject(url, null, Void.class);
        } catch (HttpClientErrorException e) {
            throw new AccountServiceException("Failed to commit reservation: " + reservationId, e);
        }
    }

    /**
     * Rollbacks a funds reservation
     */
    public void rollbackReservation(String reservationId) {
        try {
            String url = accountServiceUrl + "/reservations/" + reservationId + "/rollback";
            restTemplate.postForObject(url, null, Void.class);
        } catch (HttpClientErrorException e) {
            // Log error but don't throw - rollback should be idempotent
            System.err.println("Failed to rollback reservation: " + reservationId + ", " + e.getMessage());
        }
    }
}
```

#### Advanced REST Client with Retry and Circuit Breaker

```java
@Service
public class ResilientAccountServiceClient {

    private final RestTemplate restTemplate;
    private final CircuitBreaker circuitBreaker;
    private final RetryTemplate retryTemplate;
    private final String accountServiceUrl;

    public ResilientAccountServiceClient(RestTemplate restTemplate,
                                       @Value("${services.account.url}") String accountServiceUrl) {
        this.restTemplate = restTemplate;
        this.accountServiceUrl = accountServiceUrl;

        // Configure Circuit Breaker
        this.circuitBreaker = CircuitBreaker.ofDefaults("accountService");
        configureCircuitBreaker();

        // Configure Retry
        this.retryTemplate = RetryTemplate.builder()
            .maxAttempts(3)
            .exponentialBackoff(1000, 2, 10000)
            .retryOn(ResourceAccessException.class, HttpServerErrorException.class)
            .build();
    }

    private void configureCircuitBreaker() {
        circuitBreaker.getEventPublisher()
            .onStateTransition(event ->
                System.out.println("Circuit breaker state transition: " + event))
            .onFailureRateExceeded(event ->
                System.out.println("Circuit breaker failure rate exceeded: " + event))
            .onSlowCallRateExceeded(event ->
                System.out.println("Circuit breaker slow call rate exceeded: " + event));
    }

    /**
     * Resilient account retrieval with circuit breaker and retry
     */
    public AccountInfo getAccountResilient(String accountNumber) {
        return circuitBreaker.executeSupplier(() ->
            retryTemplate.execute(context -> {
                System.out.println("Attempt " + (context.getRetryCount() + 1) +
                                 " to retrieve account: " + accountNumber);

                return getAccount(accountNumber);
            })
        );
    }

    /**
     * Fallback method when circuit breaker is open
     */
    public AccountInfo getAccountFallback(String accountNumber, Exception ex) {
        System.out.println("Using fallback for account: " + accountNumber +
                         ", reason: " + ex.getMessage());

        // Return cached data or default response
        return AccountInfo.builder()
            .accountNumber(accountNumber)
            .balance(BigDecimal.ZERO)
            .status("UNAVAILABLE")
            .lastUpdated(LocalDateTime.now())
            .build();
    }

    private AccountInfo getAccount(String accountNumber) {
        // Implementation from previous example
        String url = accountServiceUrl + "/accounts/" + accountNumber;
        ResponseEntity<AccountInfo> response = restTemplate.getForEntity(url, AccountInfo.class);
        return response.getBody();
    }
}
```

### 2. gRPC Communication

```java
/**
 * gRPC provides high-performance, type-safe communication
 * Ideal for internal service-to-service communication in banking systems
 */

// Proto definition (account.proto)
/*
syntax = "proto3";

package banking.account;

service AccountService {
  rpc GetAccount(GetAccountRequest) returns (GetAccountResponse);
  rpc UpdateBalance(UpdateBalanceRequest) returns (UpdateBalanceResponse);
  rpc TransferFunds(TransferFundsRequest) returns (TransferFundsResponse);
}

message GetAccountRequest {
  string account_number = 1;
}

message GetAccountResponse {
  string account_number = 1;
  string balance = 2;  // Using string to avoid precision issues
  string currency = 3;
  string status = 4;
  int64 version = 5;
}

message UpdateBalanceRequest {
  string account_number = 1;
  string new_balance = 2;
  int64 version = 3;
}

message UpdateBalanceResponse {
  string account_number = 1;
  string balance = 2;
  int64 version = 3;
  bool success = 4;
}
*/

@Service
public class GrpcAccountServiceClient {

    private final AccountServiceGrpc.AccountServiceBlockingStub accountServiceStub;
    private final AccountServiceGrpc.AccountServiceStub accountServiceAsyncStub;

    public GrpcAccountServiceClient(NettyChannelBuilder channelBuilder) {
        ManagedChannel channel = channelBuilder
            .forAddress("account-service", 9090)
            .keepAliveTime(30, TimeUnit.SECONDS)
            .keepAliveTimeout(5, TimeUnit.SECONDS)
            .keepAliveWithoutCalls(true)
            .maxInboundMessageSize(1024 * 1024) // 1MB
            .usePlaintext() // Use TLS in production
            .build();

        this.accountServiceStub = AccountServiceGrpc.newBlockingStub(channel)
            .withDeadlineAfter(5, TimeUnit.SECONDS); // Request timeout

        this.accountServiceAsyncStub = AccountServiceGrpc.newStub(channel);
    }

    /**
     * Synchronous gRPC call to get account information
     */
    public AccountInfo getAccount(String accountNumber) {
        try {
            GetAccountRequest request = GetAccountRequest.newBuilder()
                .setAccountNumber(accountNumber)
                .build();

            GetAccountResponse response = accountServiceStub.getAccount(request);

            return AccountInfo.builder()
                .accountNumber(response.getAccountNumber())
                .balance(new BigDecimal(response.getBalance()))
                .currency(response.getCurrency())
                .status(response.getStatus())
                .version(response.getVersion())
                .build();

        } catch (StatusRuntimeException e) {
            if (e.getStatus().getCode() == Status.Code.NOT_FOUND) {
                throw new AccountNotFoundException("Account not found: " + accountNumber);
            } else if (e.getStatus().getCode() == Status.Code.UNAVAILABLE) {
                throw new AccountServiceException("Account service unavailable");
            }
            throw new AccountServiceException("gRPC call failed: " + e.getMessage());
        }
    }

    /**
     * Asynchronous gRPC call with callback
     */
    public CompletableFuture<AccountInfo> getAccountAsync(String accountNumber) {
        CompletableFuture<AccountInfo> future = new CompletableFuture<>();

        GetAccountRequest request = GetAccountRequest.newBuilder()
            .setAccountNumber(accountNumber)
            .build();

        accountServiceAsyncStub.getAccount(request, new StreamObserver<GetAccountResponse>() {
            @Override
            public void onNext(GetAccountResponse response) {
                AccountInfo accountInfo = AccountInfo.builder()
                    .accountNumber(response.getAccountNumber())
                    .balance(new BigDecimal(response.getBalance()))
                    .currency(response.getCurrency())
                    .status(response.getStatus())
                    .version(response.getVersion())
                    .build();

                future.complete(accountInfo);
            }

            @Override
            public void onError(Throwable t) {
                if (t instanceof StatusRuntimeException) {
                    StatusRuntimeException sre = (StatusRuntimeException) t;
                    if (sre.getStatus().getCode() == Status.Code.NOT_FOUND) {
                        future.completeExceptionally(
                            new AccountNotFoundException("Account not found: " + accountNumber));
                    } else {
                        future.completeExceptionally(
                            new AccountServiceException("gRPC call failed: " + sre.getMessage()));
                    }
                } else {
                    future.completeExceptionally(new AccountServiceException("Unexpected error", t));
                }
            }

            @Override
            public void onCompleted() {
                // Response already handled in onNext
            }
        });

        return future;
    }

    /**
     * Batch account retrieval using gRPC streaming
     */
    public List<AccountInfo> getAccountsBatch(List<String> accountNumbers) {
        List<AccountInfo> accounts = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<Exception> error = new AtomicReference<>();

        StreamObserver<GetAccountResponse> responseObserver = new StreamObserver<GetAccountResponse>() {
            @Override
            public void onNext(GetAccountResponse response) {
                AccountInfo accountInfo = AccountInfo.builder()
                    .accountNumber(response.getAccountNumber())
                    .balance(new BigDecimal(response.getBalance()))
                    .currency(response.getCurrency())
                    .status(response.getStatus())
                    .version(response.getVersion())
                    .build();

                accounts.add(accountInfo);
            }

            @Override
            public void onError(Throwable t) {
                error.set(new AccountServiceException("Batch request failed", t));
                latch.countDown();
            }

            @Override
            public void onCompleted() {
                latch.countDown();
            }
        };

        StreamObserver<GetAccountRequest> requestObserver =
            accountServiceAsyncStub.getAccountsBatch(responseObserver);

        try {
            // Send all account requests
            for (String accountNumber : accountNumbers) {
                GetAccountRequest request = GetAccountRequest.newBuilder()
                    .setAccountNumber(accountNumber)
                    .build();
                requestObserver.onNext(request);
            }

            requestObserver.onCompleted();

            // Wait for completion
            latch.await(30, TimeUnit.SECONDS);

            if (error.get() != null) {
                throw error.get();
            }

            return accounts;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AccountServiceException("Batch request interrupted");
        } catch (Exception e) {
            throw new AccountServiceException("Batch request failed", e);
        }
    }
}
```

---

## 📨 Asynchronous Communication Patterns {#asynchronous-patterns}

Asynchronous communication allows services to communicate without blocking, improving system resilience and scalability.

### 1. Message Queue Communication

#### RabbitMQ Implementation

```java
@Configuration
@EnableRabbit
public class RabbitMQConfig {

    // Exchange declarations
    @Bean
    public TopicExchange bankingExchange() {
        return ExchangeBuilder
            .topicExchange("banking.exchange")
            .durable(true)
            .build();
    }

    // Queue declarations
    @Bean
    public Queue transactionQueue() {
        return QueueBuilder
            .durable("banking.transactions")
            .withArgument("x-dead-letter-exchange", "banking.dlx")
            .withArgument("x-dead-letter-routing-key", "failed.transaction")
            .withArgument("x-max-retries", 3)
            .build();
    }

    @Bean
    public Queue fraudDetectionQueue() {
        return QueueBuilder
            .durable("banking.fraud-detection")
            .withArgument("x-max-priority", 10) // Priority queue for urgent fraud alerts
            .build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder
            .durable("banking.notifications")
            .build();
    }

    @Bean
    public Queue auditQueue() {
        return QueueBuilder
            .durable("banking.audit")
            .build();
    }

    // Bindings
    @Bean
    public Binding transactionBinding() {
        return BindingBuilder
            .bind(transactionQueue())
            .to(bankingExchange())
            .with("transaction.#");
    }

    @Bean
    public Binding fraudBinding() {
        return BindingBuilder
            .bind(fraudDetectionQueue())
            .to(bankingExchange())
            .with("fraud.#");
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
            .bind(notificationQueue())
            .to(bankingExchange())
            .with("notification.#");
    }

    @Bean
    public Binding auditBinding() {
        return BindingBuilder
            .bind(auditQueue())
            .to(bankingExchange())
            .with("audit.#");
    }

    // Dead Letter Exchange for failed messages
    @Bean
    public DirectExchange deadLetterExchange() {
        return ExchangeBuilder
            .directExchange("banking.dlx")
            .durable(true)
            .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder
            .durable("banking.failed-messages")
            .build();
    }

    @Bean
    public Binding deadLetterBinding() {
        return BindingBuilder
            .bind(deadLetterQueue())
            .to(deadLetterExchange())
            .with("failed.#");
    }

    // RabbitTemplate configuration
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setDefaultReceiveQueue("banking.transactions");
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("Message delivered successfully: " + correlationData);
            } else {
                System.err.println("Message delivery failed: " + cause);
            }
        });
        template.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            System.err.println("Message returned: " + replyText);
        });
        return template;
    }
}

@Service
public class TransactionMessagePublisher {

    private final RabbitTemplate rabbitTemplate;

    public TransactionMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publishes a transaction event for processing
     */
    public void publishTransactionEvent(TransactionEvent event) {
        try {
            // Set message properties
            MessageProperties properties = new MessageProperties();
            properties.setCorrelationId(event.getTransactionId());
            properties.setTimestamp(new Date());
            properties.setExpiration("300000"); // 5 minutes TTL
            properties.setPriority(event.getPriority());

            // Add custom headers
            properties.setHeader("event-type", event.getEventType());
            properties.setHeader("source-service", "transaction-service");
            properties.setHeader("version", "1.0");

            Message message = new Message(
                objectToByteArray(event),
                properties
            );

            // Publish with routing key based on event type
            String routingKey = "transaction." + event.getEventType().toLowerCase();

            rabbitTemplate.send("banking.exchange", routingKey, message);

            System.out.println("Published transaction event: " + event.getTransactionId() +
                             " with routing key: " + routingKey);

        } catch (Exception e) {
            System.err.println("Failed to publish transaction event: " + e.getMessage());
            throw new MessagePublishingException("Failed to publish event", e);
        }
    }

    /**
     * Publishes fraud alert with high priority
     */
    public void publishFraudAlert(FraudAlert alert) {
        try {
            MessageProperties properties = new MessageProperties();
            properties.setCorrelationId(alert.getAlertId());
            properties.setTimestamp(new Date());
            properties.setPriority(10); // Highest priority
            properties.setHeader("alert-severity", alert.getSeverity());
            properties.setHeader("account-number", alert.getAccountNumber());

            Message message = new Message(objectToByteArray(alert), properties);

            rabbitTemplate.send("banking.exchange", "fraud.alert.high", message);

            System.out.println("Published fraud alert: " + alert.getAlertId());

        } catch (Exception e) {
            System.err.println("Failed to publish fraud alert: " + e.getMessage());
            throw new MessagePublishingException("Failed to publish fraud alert", e);
        }
    }

    /**
     * Publishes audit event for compliance
     */
    public void publishAuditEvent(AuditEvent event) {
        try {
            MessageProperties properties = new MessageProperties();
            properties.setCorrelationId(event.getEventId());
            properties.setTimestamp(new Date());
            properties.setHeader("compliance-required", event.isComplianceRequired());
            properties.setHeader("retention-period", event.getRetentionPeriod());

            Message message = new Message(objectToByteArray(event), properties);

            rabbitTemplate.send("banking.exchange", "audit.event", message);

            System.out.println("Published audit event: " + event.getEventId());

        } catch (Exception e) {
            System.err.println("Failed to publish audit event: " + e.getMessage());
            // Audit events are critical - consider alternative storage
            storeAuditEventLocally(event);
        }
    }

    private byte[] objectToByteArray(Object obj) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsBytes(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize object", e);
        }
    }

    private void storeAuditEventLocally(AuditEvent event) {
        // Implement local storage as fallback for audit events
        System.out.println("Storing audit event locally: " + event.getEventId());
    }
}

@Component
public class TransactionMessageConsumer {

    private final TransactionService transactionService;
    private final FraudDetectionService fraudService;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public TransactionMessageConsumer(TransactionService transactionService,
                                    FraudDetectionService fraudService,
                                    NotificationService notificationService,
                                    AuditService auditService) {
        this.transactionService = transactionService;
        this.fraudService = fraudService;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    /**
     * Processes transaction events
     */
    @RabbitListener(queues = "banking.transactions")
    public void processTransactionEvent(
            @Payload TransactionEvent event,
            @Header Map<String, Object> headers,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        String transactionId = event.getTransactionId();
        System.out.println("Processing transaction event: " + transactionId);

        try {
            // Validate message
            validateTransactionEvent(event);

            // Process based on event type
            switch (event.getEventType()) {
                case "CREATED":
                    transactionService.processNewTransaction(event);
                    break;
                case "UPDATED":
                    transactionService.updateTransaction(event);
                    break;
                case "CANCELLED":
                    transactionService.cancelTransaction(event);
                    break;
                default:
                    throw new UnsupportedOperationException("Unknown event type: " + event.getEventType());
            }

            // Acknowledge successful processing
            channel.basicAck(deliveryTag, false);

            System.out.println("Successfully processed transaction: " + transactionId);

        } catch (ValidationException e) {
            // Validation errors - send to dead letter queue
            System.err.println("Validation failed for transaction " + transactionId + ": " + e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                System.err.println("Failed to nack message: " + ioException.getMessage());
            }

        } catch (BusinessException e) {
            // Business logic errors - may be retryable
            System.err.println("Business error for transaction " + transactionId + ": " + e.getMessage());

            if (e.isRetryable() && getRetryCount(headers) < 3) {
                // Reject for retry
                try {
                    channel.basicNack(deliveryTag, false, true);
                } catch (IOException ioException) {
                    System.err.println("Failed to nack message for retry: " + ioException.getMessage());
                }
            } else {
                // Max retries reached or non-retryable - send to DLQ
                try {
                    channel.basicNack(deliveryTag, false, false);
                } catch (IOException ioException) {
                    System.err.println("Failed to nack message: " + ioException.getMessage());
                }
            }

        } catch (Exception e) {
            // Unexpected errors
            System.err.println("Unexpected error processing transaction " + transactionId + ": " + e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, true); // Retry unexpected errors
            } catch (IOException ioException) {
                System.err.println("Failed to nack message: " + ioException.getMessage());
            }
        }
    }

    /**
     * Processes fraud alerts with high priority
     */
    @RabbitListener(queues = "banking.fraud-detection", priority = "10")
    public void processFraudAlert(
            @Payload FraudAlert alert,
            @Header Map<String, Object> headers) {

        System.out.println("Processing fraud alert: " + alert.getAlertId());

        try {
            // Immediate fraud processing
            FraudAnalysisResult result = fraudService.analyzeAlert(alert);

            if (result.isHighRisk()) {
                // Immediate account freeze
                transactionService.freezeAccount(alert.getAccountNumber(), alert.getAlertId());

                // Send urgent notification
                notificationService.sendUrgentAlert(alert);

                // Log security event
                auditService.logSecurityEvent(alert, result);
            }

            System.out.println("Fraud alert processed: " + alert.getAlertId() +
                             ", Risk Level: " + result.getRiskLevel());

        } catch (Exception e) {
            System.err.println("Failed to process fraud alert: " + alert.getAlertId() +
                             ", Error: " + e.getMessage());

            // Fraud alerts are critical - implement fallback processing
            fraudService.processAlertFallback(alert);
        }
    }

    /**
     * Processes notification requests
     */
    @RabbitListener(queues = "banking.notifications")
    public void processNotification(@Payload NotificationEvent notification) {
        System.out.println("Processing notification: " + notification.getNotificationId());

        try {
            switch (notification.getType()) {
                case "EMAIL":
                    notificationService.sendEmail(notification);
                    break;
                case "SMS":
                    notificationService.sendSms(notification);
                    break;
                case "PUSH":
                    notificationService.sendPushNotification(notification);
                    break;
                default:
                    System.err.println("Unknown notification type: " + notification.getType());
            }

            System.out.println("Notification sent: " + notification.getNotificationId());

        } catch (Exception e) {
            System.err.println("Failed to send notification: " + notification.getNotificationId() +
                             ", Error: " + e.getMessage());
            // Consider retry logic for failed notifications
        }
    }

    /**
     * Processes audit events for compliance
     */
    @RabbitListener(queues = "banking.audit")
    public void processAuditEvent(@Payload AuditEvent event) {
        System.out.println("Processing audit event: " + event.getEventId());

        try {
            // Store audit event in compliance database
            auditService.storeAuditEvent(event);

            // Check if regulatory reporting is required
            if (event.isComplianceRequired()) {
                auditService.generateComplianceReport(event);
            }

            System.out.println("Audit event processed: " + event.getEventId());

        } catch (Exception e) {
            System.err.println("Failed to process audit event: " + event.getEventId() +
                             ", Error: " + e.getMessage());

            // Audit events must not be lost - implement persistent retry
            auditService.scheduleAuditRetry(event);
        }
    }

    private void validateTransactionEvent(TransactionEvent event) {
        if (event.getTransactionId() == null || event.getTransactionId().trim().isEmpty()) {
            throw new ValidationException("Transaction ID is required");
        }
        if (event.getAmount() == null || event.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Valid transaction amount is required");
        }
        // Add more validation as needed
    }

    private int getRetryCount(Map<String, Object> headers) {
        Object retryCount = headers.get("x-retry-count");
        return retryCount != null ? (Integer) retryCount : 0;
    }
}
```

### 2. Apache Kafka Implementation

```java
@Configuration
@EnableKafka
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // Producer Configuration
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Banking-specific configurations for reliability
        configProps.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
        configProps.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE);
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true); // Exactly-once semantics
        configProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 1); // Ordering guarantee

        // Performance tuning
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory());

        // Configure callback for delivery confirmation
        template.setDefaultTopic("banking-events");
        template.setProducerListener(new ProducerListener<String, Object>() {
            @Override
            public void onSuccess(ProducerRecord<String, Object> producerRecord,
                                RecordMetadata recordMetadata) {
                System.out.println("Message sent successfully: " +
                                 "Topic=" + recordMetadata.topic() +
                                 ", Partition=" + recordMetadata.partition() +
                                 ", Offset=" + recordMetadata.offset());
            }

            @Override
            public void onError(ProducerRecord<String, Object> producerRecord,
                              Exception exception) {
                System.err.println("Failed to send message: " + exception.getMessage());
            }
        });

        return template;
    }

    // Consumer Configuration
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Banking-specific configurations for reliability
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual offset management
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed"); // Read only committed messages

        // Consumer group configuration
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "banking-transaction-processors");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 10000);

        // Deserializer configuration
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.bank.events");

        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3); // Number of consumer threads

        // Configure manual acknowledgment
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);

        // Configure error handling
        factory.setErrorHandler(new SeekToCurrentErrorHandler(
            new FixedBackOff(1000L, 3L))); // Retry 3 times with 1 second delay

        return factory;
    }

    // Topic Configuration
    @Bean
    public NewTopic transactionEventsTopic() {
        return TopicBuilder.name("banking-transaction-events")
            .partitions(6) // For parallel processing
            .replicas(3)   // For fault tolerance
            .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 days retention
            .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE)
            .build();
    }

    @Bean
    public NewTopic fraudAlertsTopic() {
        return TopicBuilder.name("banking-fraud-alerts")
            .partitions(3)
            .replicas(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, "2592000000") // 30 days retention
            .build();
    }

    @Bean
    public NewTopic auditEventsTopic() {
        return TopicBuilder.name("banking-audit-events")
            .partitions(12) // High throughput for audit events
            .replicas(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, "31536000000") // 1 year retention
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "gzip") // Compress audit data
            .build();
    }
}

@Service
public class KafkaTransactionEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaTransactionEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes transaction events to Kafka with partitioning by account number
     */
    public void publishTransactionEvent(TransactionEvent event) {
        try {
            // Use account number as key for partitioning to ensure ordering per account
            String key = event.getFromAccount();

            // Add event metadata
            event.setPublishedAt(LocalDateTime.now());
            event.setPublisherService("transaction-service");
            event.setEventVersion("1.0");

            // Send to specific topic
            SendResult<String, Object> result = kafkaTemplate.send(
                "banking-transaction-events",
                key,
                event
            ).get(10, TimeUnit.SECONDS); // Wait up to 10 seconds for confirmation

            System.out.println("Transaction event published successfully: " +
                             "Transaction=" + event.getTransactionId() +
                             ", Partition=" + result.getRecordMetadata().partition() +
                             ", Offset=" + result.getRecordMetadata().offset());

        } catch (ExecutionException e) {
            if (e.getCause() instanceof RetriableException) {
                System.err.println("Retriable error publishing event: " + e.getCause().getMessage());
                // Implement retry logic
                scheduleEventRetry(event);
            } else {
                System.err.println("Non-retriable error publishing event: " + e.getCause().getMessage());
                // Store event locally for manual retry
                storeEventForManualRetry(event);
            }
        } catch (Exception e) {
            System.err.println("Failed to publish transaction event: " + e.getMessage());
            storeEventForManualRetry(event);
        }
    }

    /**
     * Publishes fraud alerts with urgent priority
     */
    public void publishFraudAlert(FraudAlert alert) {
        try {
            // Use alert severity as key for prioritization
            String key = alert.getSeverity().name();

            // Add headers for message routing and processing
            ProducerRecord<String, Object> record = new ProducerRecord<>(
                "banking-fraud-alerts",
                key,
                alert
            );

            record.headers().add("alert-severity", alert.getSeverity().name().getBytes());
            record.headers().add("account-number", alert.getAccountNumber().getBytes());
            record.headers().add("timestamp", LocalDateTime.now().toString().getBytes());

            kafkaTemplate.send(record).addCallback(
                result -> System.out.println("Fraud alert published: " + alert.getAlertId()),
                failure -> System.err.println("Failed to publish fraud alert: " + failure.getMessage())
            );

        } catch (Exception e) {
            System.err.println("Error publishing fraud alert: " + e.getMessage());
            // Fraud alerts are critical - use synchronous fallback
            fraudAlertFallbackPublisher.publishImmediate(alert);
        }
    }

    /**
     * Batch publish for high-throughput scenarios
     */
    public void publishTransactionEventsBatch(List<TransactionEvent> events) {
        List<CompletableFuture<SendResult<String, Object>>> futures = new ArrayList<>();

        for (TransactionEvent event : events) {
            String key = event.getFromAccount();

            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate
                .send("banking-transaction-events", key, event)
                .completableFuture();

            futures.add(future);
        }

        // Wait for all messages to be sent
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
            futures.toArray(new CompletableFuture[0])
        );

        try {
            allFutures.get(30, TimeUnit.SECONDS);
            System.out.println("Batch of " + events.size() + " events published successfully");
        } catch (Exception e) {
            System.err.println("Batch publish failed: " + e.getMessage());
            // Handle partial failures
            handleBatchPublishFailure(events, futures);
        }
    }

    private void scheduleEventRetry(TransactionEvent event) {
        // Implement exponential backoff retry logic
        System.out.println("Scheduling retry for event: " + event.getTransactionId());
    }

    private void storeEventForManualRetry(TransactionEvent event) {
        // Store in local database for manual retry
        System.out.println("Storing event for manual retry: " + event.getTransactionId());
    }

    private void handleBatchPublishFailure(List<TransactionEvent> events,
                                         List<CompletableFuture<SendResult<String, Object>>> futures) {
        for (int i = 0; i < futures.size(); i++) {
            CompletableFuture<SendResult<String, Object>> future = futures.get(i);
            if (future.isCompletedExceptionally()) {
                TransactionEvent failedEvent = events.get(i);
                System.err.println("Failed to publish event: " + failedEvent.getTransactionId());
                storeEventForManualRetry(failedEvent);
            }
        }
    }
}

@Component
public class KafkaTransactionEventConsumer {

    private final TransactionService transactionService;
    private final FraudDetectionService fraudService;
    private final AuditService auditService;

    public KafkaTransactionEventConsumer(TransactionService transactionService,
                                       FraudDetectionService fraudService,
                                       AuditService auditService) {
        this.transactionService = transactionService;
        this.fraudService = fraudService;
        this.auditService = auditService;
    }

    /**
     * Consumes transaction events with manual acknowledgment
     */
    @KafkaListener(
        topics = "banking-transaction-events",
        groupId = "transaction-processors",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeTransactionEvent(
            ConsumerRecord<String, TransactionEvent> record,
            Acknowledgment acknowledgment) {

        TransactionEvent event = record.value();
        String transactionId = event.getTransactionId();

        System.out.println("Consuming transaction event: " + transactionId +
                         " from partition: " + record.partition() +
                         " offset: " + record.offset());

        try {
            // Process the transaction event
            transactionService.processTransactionEvent(event);

            // Trigger fraud detection asynchronously
            CompletableFuture.runAsync(() -> {
                try {
                    fraudService.analyzeTransaction(event);
                } catch (Exception e) {
                    System.err.println("Fraud analysis failed for " + transactionId + ": " + e.getMessage());
                }
            });

            // Log audit event
            auditService.logTransactionEvent(event);

            // Acknowledge successful processing
            acknowledgment.acknowledge();

            System.out.println("Successfully processed transaction event: " + transactionId);

        } catch (BusinessException e) {
            System.err.println("Business logic error for transaction " + transactionId + ": " + e.getMessage());

            if (e.isRetryable()) {
                // Don't acknowledge - message will be retried
                System.out.println("Transaction will be retried: " + transactionId);
            } else {
                // Acknowledge to prevent infinite retry
                acknowledgment.acknowledge();
                // Send to dead letter topic or error handling service
                handleNonRetryableError(event, e);
            }

        } catch (Exception e) {
            System.err.println("Unexpected error processing transaction " + transactionId + ": " + e.getMessage());

            // Check if this is a duplicate by checking offset tracking
            if (isDuplicateMessage(record)) {
                System.out.println("Duplicate message detected, acknowledging: " + transactionId);
                acknowledgment.acknowledge();
            } else {
                // Don't acknowledge for retry
                System.out.println("Message will be retried: " + transactionId);
            }
        }
    }

    /**
     * Consumes fraud alerts with immediate processing
     */
    @KafkaListener(
        topics = "banking-fraud-alerts",
        groupId = "fraud-processors"
    )
    public void consumeFraudAlert(
            ConsumerRecord<String, FraudAlert> record,
            Acknowledgment acknowledgment) {

        FraudAlert alert = record.value();
        System.out.println("Processing fraud alert: " + alert.getAlertId());

        try {
            // Immediate fraud processing
            FraudAnalysisResult result = fraudService.processAlert(alert);

            if (result.requiresImmediateAction()) {
                // Take immediate protective action
                transactionService.suspendAccount(alert.getAccountNumber());

                // Send urgent notifications
                notificationService.sendUrgentSecurityAlert(alert);
            }

            acknowledgment.acknowledge();
            System.out.println("Fraud alert processed: " + alert.getAlertId());

        } catch (Exception e) {
            System.err.println("Failed to process fraud alert: " + alert.getAlertId() +
                             ", Error: " + e.getMessage());

            // Fraud alerts are critical - implement immediate fallback
            fraudService.processAlertFallback(alert);
            acknowledgment.acknowledge(); // Acknowledge to prevent blocking
        }
    }

    /**
     * Consumes audit events for compliance logging
     */
    @KafkaListener(
        topics = "banking-audit-events",
        groupId = "audit-processors"
    )
    public void consumeAuditEvent(
            ConsumerRecord<String, AuditEvent> record,
            Acknowledgment acknowledgment) {

        AuditEvent event = record.value();
        System.out.println("Processing audit event: " + event.getEventId());

        try {
            // Store audit event in compliance database
            auditService.storeAuditEvent(event);

            // Generate regulatory reports if required
            if (event.requiresRegulatoryReporting()) {
                auditService.generateRegulatoryReport(event);
            }

            acknowledgment.acknowledge();
            System.out.println("Audit event stored: " + event.getEventId());

        } catch (Exception e) {
            System.err.println("Failed to process audit event: " + event.getEventId() +
                             ", Error: " + e.getMessage());

            // Audit events must never be lost
            auditService.storeAuditEventFallback(event);
            acknowledgment.acknowledge();
        }
    }

    private boolean isDuplicateMessage(ConsumerRecord<?, ?> record) {
        // Implement duplicate detection logic
        // This could be based on message offset tracking or idempotency keys
        return false;
    }

    private void handleNonRetryableError(TransactionEvent event, BusinessException e) {
        // Send to error handling service or dead letter topic
        System.err.println("Handling non-retryable error for transaction: " + event.getTransactionId());
    }
}
```

---

## 🎭 Event-Driven Architecture {#event-driven}

Event-driven architecture enables loose coupling between services and supports complex business processes through event choreography.

### 1. Event Sourcing Implementation

```java
@Entity
@Table(name = "event_store")
public class EventStoreEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;

    @Column(name = "aggregate_type", nullable = false)
    private String aggregateType;

    @Column(name = "event_type", nullable = false)
    private String eventType;

    @Column(name = "event_data", columnDefinition = "TEXT")
    private String eventData;

    @Column(name = "event_version", nullable = false)
    private Long eventVersion;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "correlation_id")
    private String correlationId;

    @Column(name = "causation_id")
    private String causationId;

    // Constructors, getters, setters
}

@Service
public class EventStore {

    private final EventStoreEntryRepository eventStoreRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public EventStore(EventStoreEntryRepository eventStoreRepository,
                     ApplicationEventPublisher eventPublisher,
                     ObjectMapper objectMapper) {
        this.eventStoreRepository = eventStoreRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    /**
     * Appends events to the event store
     */
    public void appendEvents(String aggregateId, String aggregateType,
                           List<DomainEvent> events, Long expectedVersion) {

        // Optimistic concurrency check
        Long currentVersion = getCurrentVersion(aggregateId);
        if (!Objects.equals(currentVersion, expectedVersion)) {
            throw new OptimisticConcurrencyException(
                "Expected version " + expectedVersion + " but current version is " + currentVersion);
        }

        List<EventStoreEntry> entries = new ArrayList<>();

        for (int i = 0; i < events.size(); i++) {
            DomainEvent event = events.get(i);
            Long newVersion = currentVersion + i + 1;

            EventStoreEntry entry = new EventStoreEntry();
            entry.setAggregateId(aggregateId);
            entry.setAggregateType(aggregateType);
            entry.setEventType(event.getClass().getSimpleName());
            entry.setEventData(serializeEvent(event));
            entry.setEventVersion(newVersion);
            entry.setTimestamp(LocalDateTime.now());
            entry.setCorrelationId(event.getCorrelationId());
            entry.setCausationId(event.getCausationId());

            entries.add(entry);
        }

        // Save all events atomically
        eventStoreRepository.saveAll(entries);

        // Publish events for projection updates and side effects
        events.forEach(eventPublisher::publishEvent);

        System.out.println("Appended " + events.size() + " events for aggregate: " + aggregateId);
    }

    /**
     * Loads events for an aggregate
     */
    public List<DomainEvent> loadEvents(String aggregateId) {
        List<EventStoreEntry> entries = eventStoreRepository
            .findByAggregateIdOrderByEventVersion(aggregateId);

        return entries.stream()
            .map(this::deserializeEvent)
            .collect(Collectors.toList());
    }

    /**
     * Loads events from a specific version
     */
    public List<DomainEvent> loadEventsFromVersion(String aggregateId, Long fromVersion) {
        List<EventStoreEntry> entries = eventStoreRepository
            .findByAggregateIdAndEventVersionGreaterThanOrderByEventVersion(aggregateId, fromVersion);

        return entries.stream()
            .map(this::deserializeEvent)
            .collect(Collectors.toList());
    }

    /**
     * Loads all events of a specific type across all aggregates
     */
    public List<DomainEvent> loadEventsByType(String eventType, LocalDateTime since) {
        List<EventStoreEntry> entries = eventStoreRepository
            .findByEventTypeAndTimestampAfterOrderByTimestamp(eventType, since);

        return entries.stream()
            .map(this::deserializeEvent)
            .collect(Collectors.toList());
    }

    private Long getCurrentVersion(String aggregateId) {
        return eventStoreRepository.findMaxVersionByAggregateId(aggregateId)
            .orElse(0L);
    }

    private String serializeEvent(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new EventSerializationException("Failed to serialize event", e);
        }
    }

    private DomainEvent deserializeEvent(EventStoreEntry entry) {
        try {
            Class<?> eventClass = Class.forName("com.bank.events." + entry.getEventType());
            return (DomainEvent) objectMapper.readValue(entry.getEventData(), eventClass);
        } catch (Exception e) {
            throw new EventDeserializationException("Failed to deserialize event", e);
        }
    }
}

// Base classes for events and aggregates
public abstract class DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredOn;
    private String correlationId;
    private String causationId;

    protected DomainEvent() {
        this.eventId = UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
    }

    // Getters and setters
}

public abstract class AggregateRoot {
    private String id;
    private Long version = 0L;
    private List<DomainEvent> uncomittedEvents = new ArrayList<>();

    protected void applyEvent(DomainEvent event) {
        applyChange(event);
        uncomittedEvents.add(event);
    }

    protected abstract void applyChange(DomainEvent event);

    public List<DomainEvent> getUncomittedEvents() {
        return new ArrayList<>(uncomittedEvents);
    }

    public void markEventsAsCommitted() {
        uncomittedEvents.clear();
    }

    // Getters and setters
}

// Banking domain events
public class AccountCreatedEvent extends DomainEvent {
    private String accountNumber;
    private String customerId;
    private BigDecimal initialBalance;
    private String accountType;

    // Constructors, getters, setters
}

public class MoneyDepositedEvent extends DomainEvent {
    private String accountNumber;
    private BigDecimal amount;
    private String transactionId;
    private BigDecimal balanceAfter;

    // Constructors, getters, setters
}

public class MoneyWithdrawnEvent extends DomainEvent {
    private String accountNumber;
    private BigDecimal amount;
    private String transactionId;
    private BigDecimal balanceAfter;

    // Constructors, getters, setters
}

public class AccountFrozenEvent extends DomainEvent {
    private String accountNumber;
    private String reason;
    private String frozenBy;

    // Constructors, getters, setters
}

// Account aggregate
public class Account extends AggregateRoot {
    private String accountNumber;
    private String customerId;
    private BigDecimal balance;
    private AccountStatus status;
    private String accountType;

    public Account() {
        // Default constructor for reconstruction
    }

    public Account(String accountNumber, String customerId, BigDecimal initialBalance, String accountType) {
        AccountCreatedEvent event = new AccountCreatedEvent();
        event.setAccountNumber(accountNumber);
        event.setCustomerId(customerId);
        event.setInitialBalance(initialBalance);
        event.setAccountType(accountType);

        applyEvent(event);
    }

    public void deposit(BigDecimal amount, String transactionId) {
        if (status == AccountStatus.FROZEN) {
            throw new AccountFrozenException("Cannot deposit to frozen account");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        MoneyDepositedEvent event = new MoneyDepositedEvent();
        event.setAccountNumber(accountNumber);
        event.setAmount(amount);
        event.setTransactionId(transactionId);
        event.setBalanceAfter(balance.add(amount));

        applyEvent(event);
    }

    public void withdraw(BigDecimal amount, String transactionId) {
        if (status == AccountStatus.FROZEN) {
            throw new AccountFrozenException("Cannot withdraw from frozen account");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds for withdrawal");
        }

        MoneyWithdrawnEvent event = new MoneyWithdrawnEvent();
        event.setAccountNumber(accountNumber);
        event.setAmount(amount);
        event.setTransactionId(transactionId);
        event.setBalanceAfter(balance.subtract(amount));

        applyEvent(event);
    }

    public void freeze(String reason, String frozenBy) {
        if (status == AccountStatus.FROZEN) {
            return; // Already frozen
        }

        AccountFrozenEvent event = new AccountFrozenEvent();
        event.setAccountNumber(accountNumber);
        event.setReason(reason);
        event.setFrozenBy(frozenBy);

        applyEvent(event);
    }

    @Override
    protected void applyChange(DomainEvent event) {
        if (event instanceof AccountCreatedEvent) {
            apply((AccountCreatedEvent) event);
        } else if (event instanceof MoneyDepositedEvent) {
            apply((MoneyDepositedEvent) event);
        } else if (event instanceof MoneyWithdrawnEvent) {
            apply((MoneyWithdrawnEvent) event);
        } else if (event instanceof AccountFrozenEvent) {
            apply((AccountFrozenEvent) event);
        }
    }

    private void apply(AccountCreatedEvent event) {
        this.accountNumber = event.getAccountNumber();
        this.customerId = event.getCustomerId();
        this.balance = event.getInitialBalance();
        this.accountType = event.getAccountType();
        this.status = AccountStatus.ACTIVE;
    }

    private void apply(MoneyDepositedEvent event) {
        this.balance = event.getBalanceAfter();
    }

    private void apply(MoneyWithdrawnEvent event) {
        this.balance = event.getBalanceAfter();
    }

    private void apply(AccountFrozenEvent event) {
        this.status = AccountStatus.FROZEN;
    }

    // Getters
    public String getAccountNumber() { return accountNumber; }
    public String getCustomerId() { return customerId; }
    public BigDecimal getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public String getAccountType() { return accountType; }
}

enum AccountStatus {
    ACTIVE, FROZEN, CLOSED
}

// Repository for loading and saving aggregates
@Service
public class AccountRepository {

    private final EventStore eventStore;

    public AccountRepository(EventStore eventStore) {
        this.eventStore = eventStore;
    }

    public Account findById(String accountNumber) {
        List<DomainEvent> events = eventStore.loadEvents(accountNumber);

        if (events.isEmpty()) {
            return null;
        }

        Account account = new Account();
        account.setId(accountNumber);

        // Replay events to reconstruct state
        for (DomainEvent event : events) {
            account.applyChange(event);
        }

        account.setVersion((long) events.size());
        return account;
    }

    public void save(Account account) {
        List<DomainEvent> events = account.getUncomittedEvents();

        if (!events.isEmpty()) {
            eventStore.appendEvents(
                account.getId(),
                "Account",
                events,
                account.getVersion()
            );

            account.markEventsAsCommitted();
            account.setVersion(account.getVersion() + events.size());
        }
    }
}
```

### 2. CQRS Implementation

```java
// Command side
@Component
public class AccountCommandHandler {

    private final AccountRepository accountRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AccountCommandHandler(AccountRepository accountRepository,
                               ApplicationEventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.eventPublisher = eventPublisher;
    }

    @CommandHandler
    public void handle(CreateAccountCommand command) {
        // Validate command
        validateCreateAccountCommand(command);

        // Check if account already exists
        Account existingAccount = accountRepository.findById(command.getAccountNumber());
        if (existingAccount != null) {
            throw new AccountAlreadyExistsException("Account already exists: " + command.getAccountNumber());
        }

        // Create new account
        Account account = new Account(
            command.getAccountNumber(),
            command.getCustomerId(),
            command.getInitialBalance(),
            command.getAccountType()
        );

        // Save account (will publish events)
        accountRepository.save(account);

        System.out.println("Account created: " + command.getAccountNumber());
    }

    @CommandHandler
    public void handle(DepositMoneyCommand command) {
        Account account = accountRepository.findById(command.getAccountNumber());
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + command.getAccountNumber());
        }

        account.deposit(command.getAmount(), command.getTransactionId());
        accountRepository.save(account);

        System.out.println("Money deposited: " + command.getAmount() + " to " + command.getAccountNumber());
    }

    @CommandHandler
    public void handle(WithdrawMoneyCommand command) {
        Account account = accountRepository.findById(command.getAccountNumber());
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + command.getAccountNumber());
        }

        account.withdraw(command.getAmount(), command.getTransactionId());
        accountRepository.save(account);

        System.out.println("Money withdrawn: " + command.getAmount() + " from " + command.getAccountNumber());
    }

    @CommandHandler
    public void handle(FreezeAccountCommand command) {
        Account account = accountRepository.findById(command.getAccountNumber());
        if (account == null) {
            throw new AccountNotFoundException("Account not found: " + command.getAccountNumber());
        }

        account.freeze(command.getReason(), command.getFrozenBy());
        accountRepository.save(account);

        System.out.println("Account frozen: " + command.getAccountNumber());
    }

    private void validateCreateAccountCommand(CreateAccountCommand command) {
        if (command.getAccountNumber() == null || command.getAccountNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Account number is required");
        }
        if (command.getCustomerId() == null || command.getCustomerId().trim().isEmpty()) {
            throw new IllegalArgumentException("Customer ID is required");
        }
        if (command.getInitialBalance() == null || command.getInitialBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Initial balance must be non-negative");
        }
    }
}

// Commands
public class CreateAccountCommand {
    private String accountNumber;
    private String customerId;
    private BigDecimal initialBalance;
    private String accountType;

    // Constructors, getters, setters
}

public class DepositMoneyCommand {
    private String accountNumber;
    private BigDecimal amount;
    private String transactionId;

    // Constructors, getters, setters
}

public class WithdrawMoneyCommand {
    private String accountNumber;
    private BigDecimal amount;
    private String transactionId;

    // Constructors, getters, setters
}

public class FreezeAccountCommand {
    private String accountNumber;
    private String reason;
    private String frozenBy;

    // Constructors, getters, setters
}

// Query side
@Entity
@Table(name = "account_read_model")
public class AccountReadModel {
    @Id
    private String accountNumber;
    private String customerId;
    private String customerName;
    private BigDecimal balance;
    private String accountType;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdated;
    private String lastTransactionId;

    // Constructors, getters, setters
}

@Repository
public interface AccountReadModelRepository extends JpaRepository<AccountReadModel, String> {
    List<AccountReadModel> findByCustomerId(String customerId);
    List<AccountReadModel> findByStatus(String status);
    List<AccountReadModel> findByAccountType(String accountType);

    @Query("SELECT a FROM AccountReadModel a WHERE a.balance >= :minBalance AND a.balance <= :maxBalance")
    List<AccountReadModel> findByBalanceRange(@Param("minBalance") BigDecimal minBalance,
                                            @Param("maxBalance") BigDecimal maxBalance);
}

@Component
public class AccountProjectionHandler {

    private final AccountReadModelRepository readModelRepository;
    private final CustomerService customerService;

    public AccountProjectionHandler(AccountReadModelRepository readModelRepository,
                                  CustomerService customerService) {
        this.readModelRepository = readModelRepository;
        this.customerService = customerService;
    }

    @EventHandler
    public void on(AccountCreatedEvent event) {
        AccountReadModel readModel = new AccountReadModel();
        readModel.setAccountNumber(event.getAccountNumber());
        readModel.setCustomerId(event.getCustomerId());
        readModel.setBalance(event.getInitialBalance());
        readModel.setAccountType(event.getAccountType());
        readModel.setStatus("ACTIVE");
        readModel.setCreatedAt(event.getOccurredOn());
        readModel.setLastUpdated(event.getOccurredOn());

        // Enrich with customer information
        try {
            CustomerInfo customer = customerService.getCustomer(event.getCustomerId());
            readModel.setCustomerName(customer.getFullName());
        } catch (Exception e) {
            System.err.println("Failed to enrich account with customer info: " + e.getMessage());
            readModel.setCustomerName("Unknown");
        }

        readModelRepository.save(readModel);

        System.out.println("Account read model created: " + event.getAccountNumber());
    }

    @EventHandler
    public void on(MoneyDepositedEvent event) {
        AccountReadModel readModel = readModelRepository.findById(event.getAccountNumber())
            .orElseThrow(() -> new IllegalStateException("Account read model not found: " + event.getAccountNumber()));

        readModel.setBalance(event.getBalanceAfter());
        readModel.setLastUpdated(event.getOccurredOn());
        readModel.setLastTransactionId(event.getTransactionId());

        readModelRepository.save(readModel);

        System.out.println("Account balance updated in read model: " + event.getAccountNumber());
    }

    @EventHandler
    public void on(MoneyWithdrawnEvent event) {
        AccountReadModel readModel = readModelRepository.findById(event.getAccountNumber())
            .orElseThrow(() -> new IllegalStateException("Account read model not found: " + event.getAccountNumber()));

        readModel.setBalance(event.getBalanceAfter());
        readModel.setLastUpdated(event.getOccurredOn());
        readModel.setLastTransactionId(event.getTransactionId());

        readModelRepository.save(readModel);

        System.out.println("Account balance updated in read model: " + event.getAccountNumber());
    }

    @EventHandler
    public void on(AccountFrozenEvent event) {
        AccountReadModel readModel = readModelRepository.findById(event.getAccountNumber())
            .orElseThrow(() -> new IllegalStateException("Account read model not found: " + event.getAccountNumber()));

        readModel.setStatus("FROZEN");
        readModel.setLastUpdated(event.getOccurredOn());

        readModelRepository.save(readModel);

        System.out.println("Account status updated to FROZEN in read model: " + event.getAccountNumber());
    }
}

// Query handlers
@Component
public class AccountQueryHandler {

    private final AccountReadModelRepository readModelRepository;

    public AccountQueryHandler(AccountReadModelRepository readModelRepository) {
        this.readModelRepository = readModelRepository;
    }

    @QueryHandler
    public AccountReadModel handle(GetAccountQuery query) {
        return readModelRepository.findById(query.getAccountNumber())
            .orElseThrow(() -> new AccountNotFoundException("Account not found: " + query.getAccountNumber()));
    }

    @QueryHandler
    public List<AccountReadModel> handle(GetAccountsByCustomerQuery query) {
        return readModelRepository.findByCustomerId(query.getCustomerId());
    }

    @QueryHandler
    public List<AccountReadModel> handle(GetAccountsByStatusQuery query) {
        return readModelRepository.findByStatus(query.getStatus());
    }

    @QueryHandler
    public List<AccountReadModel> handle(GetAccountsByBalanceRangeQuery query) {
        return readModelRepository.findByBalanceRange(query.getMinBalance(), query.getMaxBalance());
    }
}

// Queries
public class GetAccountQuery {
    private String accountNumber;

    // Constructor, getters, setters
}

public class GetAccountsByCustomerQuery {
    private String customerId;

    // Constructor, getters, setters
}

public class GetAccountsByStatusQuery {
    private String status;

    // Constructor, getters, setters
}

public class GetAccountsByBalanceRangeQuery {
    private BigDecimal minBalance;
    private BigDecimal maxBalance;

    // Constructor, getters, setters
}
```

This comprehensive guide provides detailed descriptions and implementations for microservices communication patterns, covering synchronous REST and gRPC communication, asynchronous messaging with RabbitMQ and Kafka, and event-driven architecture with Event Sourcing and CQRS patterns. Each section includes practical banking examples with proper error handling, resilience patterns, and best practices for enterprise applications.