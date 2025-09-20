# Java Kafka & Message Queues - Interview Guide

> **Complete guide to Apache Kafka, RabbitMQ, and other message queue technologies for Java developers**

---

## 📚 **Table of Contents**

1. [Message Queue Fundamentals](#message-queue-fundamentals)
2. [Apache Kafka Deep Dive](#apache-kafka-deep-dive)
3. [RabbitMQ Overview](#rabbitmq-overview)
4. [Apache ActiveMQ](#apache-activemq)
5. [AWS SQS/SNS](#aws-sqssns)
6. [Technology Comparison](#technology-comparison)
7. [Java Implementation Examples](#java-implementation-examples)
8. [Performance Benchmarks](#performance-benchmarks)
9. [Use Case Scenarios](#use-case-scenarios)
10. [Best Practices](#best-practices)
11. [Common Interview Questions](#common-interview-questions)

---

## 🔄 **Message Queue Fundamentals**

### **Core Concepts**

```java
/**
 * Essential message queue patterns and concepts
 */

// 1. Producer-Consumer Pattern
public interface MessageProducer<T> {
    void send(String topic, T message);
    void send(String topic, String key, T message);
    CompletableFuture<SendResult> sendAsync(String topic, T message);
}

public interface MessageConsumer<T> {
    void subscribe(String topic, MessageHandler<T> handler);
    void subscribe(List<String> topics, MessageHandler<T> handler);
    void unsubscribe();
}

@FunctionalInterface
public interface MessageHandler<T> {
    void handle(Message<T> message) throws Exception;
}

// 2. Message Structure
public class Message<T> {
    private final String id;
    private final String topic;
    private final String key;
    private final T payload;
    private final Map<String, String> headers;
    private final Instant timestamp;
    private final int partition;
    private final long offset;

    public Message(String topic, String key, T payload) {
        this.id = UUID.randomUUID().toString();
        this.topic = topic;
        this.key = key;
        this.payload = payload;
        this.headers = new HashMap<>();
        this.timestamp = Instant.now();
        this.partition = -1; // Will be assigned by broker
        this.offset = -1;    // Will be assigned by broker
    }

    // Getters and builders
    public String getId() { return id; }
    public String getTopic() { return topic; }
    public String getKey() { return key; }
    public T getPayload() { return payload; }
    public Map<String, String> getHeaders() { return Map.copyOf(headers); }
    public Instant getTimestamp() { return timestamp; }
    public int getPartition() { return partition; }
    public long getOffset() { return offset; }

    // Fluent API for headers
    public Message<T> withHeader(String key, String value) {
        var newHeaders = new HashMap<>(headers);
        newHeaders.put(key, value);
        return new Message<>(topic, this.key, payload, newHeaders, timestamp);
    }
}

// 3. Message Queue Patterns
public enum MessagingPattern {
    POINT_TO_POINT,    // One producer, one consumer (Queue)
    PUBLISH_SUBSCRIBE, // One producer, multiple consumers (Topic)
    REQUEST_REPLY,     // Synchronous communication
    SCATTER_GATHER,    // Distribute work, collect results
    ROUTING,           // Route based on message content
    COMPETING_CONSUMERS // Multiple consumers compete for messages
}

// 4. Delivery Guarantees
public enum DeliveryGuarantee {
    AT_MOST_ONCE,    // Fire and forget (may lose messages)
    AT_LEAST_ONCE,   // May deliver duplicates
    EXACTLY_ONCE     // Ideal but complex to implement
}

// 5. Message Ordering
public enum OrderingGuarantee {
    NO_ORDERING,     // Messages can arrive in any order
    PARTIAL_ORDERING, // Ordering within partitions/queues
    TOTAL_ORDERING   // Global ordering (performance impact)
}
```

### **Message Queue Benefits**

```java
/**
 * Why use message queues in distributed systems
 */
public class MessageQueueBenefits {

    // 1. Decoupling - Services don't need direct connections
    public class OrderService {
        private final MessageProducer<OrderEvent> producer;

        public void processOrder(Order order) {
            // Process order logic
            order.setStatus(OrderStatus.PROCESSED);

            // Publish event - no direct coupling to other services
            var event = new OrderProcessedEvent(order.getId(), order.getCustomerId());
            producer.send("order.processed", event);
        }
    }

    public class InventoryService {
        @MessageListener("order.processed")
        public void handleOrderProcessed(OrderProcessedEvent event) {
            // Update inventory - completely decoupled from OrderService
            updateInventory(event.getOrderId());
        }
    }

    // 2. Scalability - Horizontal scaling of consumers
    public class PaymentProcessor {
        @MessageListener(topic = "payment.requests", concurrency = "10")
        public void processPayment(PaymentRequest request) {
            // Multiple instances can process payments concurrently
            processPaymentLogic(request);
        }
    }

    // 3. Reliability - Persist messages for durability
    public class ReliableOrderProcessor {
        private final MessageProducer<OrderEvent> producer;

        public void createOrder(OrderRequest request) {
            try {
                var order = orderRepository.save(new Order(request));

                // Message persisted in queue - survives service restarts
                producer.send("orders", order.getId(), new OrderCreatedEvent(order));

            } catch (Exception e) {
                // Can implement compensation patterns
                producer.send("orders.failed", new OrderFailedEvent(request, e.getMessage()));
                throw e;
            }
        }
    }

    // 4. Load Leveling - Handle traffic spikes
    public class EmailService {
        @MessageListener(topic = "email.notifications",
                        maxConcurrency = "5",
                        batchSize = "10")
        public void sendEmails(List<EmailNotification> notifications) {
            // Process emails at sustainable rate
            // Queue absorbs traffic spikes
            emailProvider.sendBatch(notifications);
        }
    }

    // 5. Event Sourcing - Audit trail of all changes
    public class EventSourcedAccount {
        private final MessageProducer<AccountEvent> eventStore;

        public void debit(String accountId, BigDecimal amount) {
            var event = new AccountDebitedEvent(accountId, amount, Instant.now());

            // All state changes stored as events
            eventStore.send("account.events", accountId, event);
        }

        public AccountBalance getBalance(String accountId) {
            // Rebuild state from events
            return eventStore.consumeFromBeginning("account.events", accountId)
                           .stream()
                           .collect(AccountBalance.fromEvents());
        }
    }
}
```

---

## 🎯 **Apache Kafka Deep Dive**

### **Kafka Architecture**

```java
/**
 * Kafka core concepts and architecture
 */

// 1. Kafka Producer Configuration
public class KafkaProducerConfig {

    public static Properties createProducerProperties() {
        var props = new Properties();

        // Basic configuration
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Performance tuning
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);           // 16KB batch
        props.put(ProducerConfig.LINGER_MS_CONFIG, 5);               // Wait 5ms for batching
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // Compression
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);    // 32MB buffer

        // Reliability settings
        props.put(ProducerConfig.ACKS_CONFIG, "all");                // Wait for all replicas
        props.put(ProducerConfig.RETRIES_CONFIG, Integer.MAX_VALUE); // Retry on failure
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);   // Exactly-once semantics

        // Timeout configurations
        props.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);  // 30 seconds
        props.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000); // 2 minutes

        return props;
    }
}

// 2. Kafka Producer Implementation
public class KafkaMessageProducer<T> implements MessageProducer<T> {
    private final KafkaProducer<String, T> producer;
    private final String clientId;

    public KafkaMessageProducer(Properties config) {
        this.clientId = config.getProperty(ProducerConfig.CLIENT_ID_CONFIG, "default-producer");
        this.producer = new KafkaProducer<>(config);

        // Graceful shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::close));
    }

    @Override
    public void send(String topic, T message) {
        send(topic, null, message);
    }

    @Override
    public void send(String topic, String key, T message) {
        var record = new ProducerRecord<>(topic, key, message);

        // Add headers for tracing
        record.headers().add("producer-id", clientId.getBytes());
        record.headers().add("timestamp", String.valueOf(System.currentTimeMillis()).getBytes());
        record.headers().add("version", "1.0".getBytes());

        try {
            producer.send(record, new SendCallback(topic, key));
        } catch (Exception e) {
            throw new MessageSendException("Failed to send message to topic: " + topic, e);
        }
    }

    @Override
    public CompletableFuture<SendResult> sendAsync(String topic, T message) {
        var future = new CompletableFuture<SendResult>();
        var record = new ProducerRecord<>(topic, message);

        producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                future.completeExceptionally(exception);
            } else {
                var result = new SendResult(
                    metadata.topic(),
                    metadata.partition(),
                    metadata.offset(),
                    metadata.timestamp()
                );
                future.complete(result);
            }
        });

        return future;
    }

    // Transactional producer for exactly-once semantics
    public void sendTransactional(Map<String, List<T>> topicMessages) {
        producer.beginTransaction();
        try {
            topicMessages.forEach((topic, messages) -> {
                messages.forEach(message -> {
                    var record = new ProducerRecord<>(topic, message);
                    producer.send(record);
                });
            });
            producer.commitTransaction();
        } catch (Exception e) {
            producer.abortTransaction();
            throw new TransactionException("Transaction failed", e);
        }
    }

    public void close() {
        producer.close(Duration.ofSeconds(30));
    }

    private static class SendCallback implements Callback {
        private final String topic;
        private final String key;

        SendCallback(String topic, String key) {
            this.topic = topic;
            this.key = key;
        }

        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (exception != null) {
                logger.error("Failed to send message to topic={}, key={}", topic, key, exception);
            } else {
                logger.debug("Message sent successfully to topic={}, partition={}, offset={}",
                           metadata.topic(), metadata.partition(), metadata.offset());
            }
        }
    }
}

// 3. Kafka Consumer Configuration
public class KafkaConsumerConfig {

    public static Properties createConsumerProperties(String groupId) {
        var props = new Properties();

        // Basic configuration
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Offset management
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit

        // Performance tuning
        props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);      // 1KB minimum fetch
        props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);     // Max wait 500ms
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500);      // Max records per poll

        // Session management
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30000);   // 30 seconds
        props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000); // 3 seconds
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 5 minutes

        return props;
    }
}

// 4. Kafka Consumer Implementation
public class KafkaMessageConsumer<T> implements MessageConsumer<T> {
    private final KafkaConsumer<String, T> consumer;
    private final ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private MessageHandler<T> messageHandler;

    public KafkaMessageConsumer(Properties config) {
        this.consumer = new KafkaConsumer<>(config);
        this.executor = Executors.newSingleThreadExecutor(r -> {
            var thread = new Thread(r, "kafka-consumer-" + Thread.currentThread().getId());
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public void subscribe(String topic, MessageHandler<T> handler) {
        subscribe(List.of(topic), handler);
    }

    @Override
    public void subscribe(List<String> topics, MessageHandler<T> handler) {
        this.messageHandler = Objects.requireNonNull(handler);
        consumer.subscribe(topics);

        if (running.compareAndSet(false, true)) {
            executor.submit(this::pollLoop);
        }
    }

    @Override
    public void unsubscribe() {
        running.set(false);
        consumer.wakeup();
    }

    private void pollLoop() {
        try {
            while (running.get()) {
                var records = consumer.poll(Duration.ofMillis(1000));

                if (!records.isEmpty()) {
                    processRecords(records);

                    // Manual offset commit for better control
                    consumer.commitSync();
                }
            }
        } catch (WakeupException e) {
            // Expected when shutting down
        } catch (Exception e) {
            logger.error("Error in consumer poll loop", e);
        } finally {
            try {
                consumer.commitSync();
            } finally {
                consumer.close();
            }
        }
    }

    private void processRecords(ConsumerRecords<String, T> records) {
        for (var record : records) {
            try {
                var message = convertToMessage(record);
                messageHandler.handle(message);

            } catch (Exception e) {
                handleProcessingError(record, e);
            }
        }
    }

    private Message<T> convertToMessage(ConsumerRecord<String, T> record) {
        var headers = new HashMap<String, String>();
        record.headers().forEach(header ->
            headers.put(header.key(), new String(header.value())));

        return new Message<>(
            record.topic(),
            record.key(),
            record.value(),
            headers,
            Instant.ofEpochMilli(record.timestamp()),
            record.partition(),
            record.offset()
        );
    }

    private void handleProcessingError(ConsumerRecord<String, T> record, Exception e) {
        logger.error("Failed to process message from topic={}, partition={}, offset={}",
                   record.topic(), record.partition(), record.offset(), e);

        // Could implement retry logic, dead letter queue, etc.
        // For now, just log and continue
    }

    public void close() {
        unsubscribe();
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

// 5. Kafka Streams for Stream Processing
public class OrderEventProcessor {

    public static void main(String[] args) {
        var props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "order-event-processor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class);

        var builder = new StreamsBuilder();

        // 1. Simple stream processing
        var orderEvents = builder.stream("order.events");

        orderEvents
            .filter((key, order) -> order.getAmount().compareTo(BigDecimal.valueOf(1000)) > 0)
            .to("high-value-orders");

        // 2. Aggregation example
        var ordersByCustomer = orderEvents
            .groupBy((key, order) -> order.getCustomerId())
            .aggregate(
                OrderSummary::new,
                (customerId, order, summary) -> summary.addOrder(order),
                Materialized.as("customer-order-summary")
            );

        // 3. Join streams
        var customerStream = builder.stream("customers");
        var enrichedOrders = orderEvents
            .join(customerStream,
                  (order, customer) -> new EnrichedOrder(order, customer),
                  JoinWindows.of(Duration.ofMinutes(5)));

        // 4. Time windowing
        var hourlyOrderStats = orderEvents
            .groupByKey()
            .windowedBy(TimeWindows.of(Duration.ofHours(1)))
            .aggregate(
                OrderStats::new,
                (key, order, stats) -> stats.addOrder(order)
            );

        var streams = new KafkaStreams(builder.build(), props);
        streams.start();

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }
}
```

### **Kafka Advanced Features**

```java
/**
 * Advanced Kafka features and patterns
 */

// 1. Kafka Connect for Data Integration
public class KafkaConnectConfig {

    public void configureJdbcSourceConnector() {
        var config = Map.of(
            "name", "jdbc-source-users",
            "connector.class", "io.confluent.connect.jdbc.JdbcSourceConnector",
            "connection.url", "jdbc:postgresql://localhost/mydb",
            "connection.user", "postgres",
            "connection.password", "password",
            "table.whitelist", "users",
            "mode", "incrementing",
            "incrementing.column.name", "id",
            "topic.prefix", "db."
        );

        // This would stream database changes to Kafka topics
        // Topic name: db.users
    }

    public void configureSinkConnector() {
        var config = Map.of(
            "name", "elasticsearch-sink",
            "connector.class", "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
            "connection.url", "http://localhost:9200",
            "topics", "user.events,order.events",
            "type.name", "_doc",
            "key.ignore", "true"
        );

        // This would stream Kafka messages to Elasticsearch
    }
}

// 2. Schema Registry Integration
public class SchemaRegistryExample {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OrderEvent {
        private String orderId;
        private String customerId;
        private BigDecimal amount;
        private String status;
        private long timestamp;
    }

    public KafkaProducer<String, OrderEvent> createAvroProducer() {
        var props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        props.put("schema.registry.url", "http://localhost:8081");

        return new KafkaProducer<>(props);
    }

    public void sendWithSchema() {
        var producer = createAvroProducer();
        var event = new OrderEvent("ORDER-123", "CUST-456",
                                  BigDecimal.valueOf(99.99), "CONFIRMED",
                                  System.currentTimeMillis());

        var record = new ProducerRecord<>("order.events", event.getOrderId(), event);
        producer.send(record);
    }
}

// 3. Kafka Security Configuration
public class KafkaSecurityConfig {

    public Properties createSecureProducerConfig() {
        var props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9093");

        // SSL Configuration
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
        props.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, "/path/to/truststore.jks");
        props.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, "password");
        props.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, "/path/to/keystore.jks");
        props.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, "password");
        props.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, "password");

        // SASL Configuration (optional)
        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
        props.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
        props.put(SaslConfigs.SASL_JAAS_CONFIG,
                 "org.apache.kafka.common.security.plain.PlainLoginModule required " +
                 "username='user' password='password';");

        return props;
    }

    public Properties createACLConfig() {
        // Access Control Lists
        var props = new Properties();
        props.put("authorizer.class.name", "kafka.security.auth.SimpleAclAuthorizer");
        props.put("super.users", "User:admin");
        props.put("allow.everyone.if.no.acl.found", "false");

        return props;
    }
}

// 4. Monitoring and Metrics
public class KafkaMonitoring {

    public void setupJMXMetrics() {
        // Producer metrics
        var producerMetrics = List.of(
            "kafka.producer:type=producer-metrics,client-id=*",
            "kafka.producer:type=producer-topic-metrics,client-id=*,topic=*"
        );

        // Consumer metrics
        var consumerMetrics = List.of(
            "kafka.consumer:type=consumer-metrics,client-id=*",
            "kafka.consumer:type=consumer-coordinator-metrics,client-id=*"
        );

        // Key metrics to monitor:
        // - Producer: record-send-rate, record-error-rate, batch-size-avg
        // - Consumer: records-consumed-rate, records-lag-max, commit-rate
        // - Broker: messages-in-per-sec, bytes-in-per-sec, request-rate
    }

    @Component
    public static class KafkaHealthIndicator implements HealthIndicator {
        private final KafkaTemplate<String, Object> kafkaTemplate;

        public KafkaHealthIndicator(KafkaTemplate<String, Object> kafkaTemplate) {
            this.kafkaTemplate = kafkaTemplate;
        }

        @Override
        public Health health() {
            try {
                // Try to get metadata - tests connectivity
                var metadata = kafkaTemplate.getProducerFactory()
                                           .createProducer()
                                           .partitionsFor("health-check");

                return Health.up()
                           .withDetail("status", "Kafka is reachable")
                           .withDetail("topicCount", metadata.size())
                           .build();

            } catch (Exception e) {
                return Health.down()
                           .withDetail("status", "Kafka is unreachable")
                           .withException(e)
                           .build();
            }
        }
    }
}
```

---

## 🐰 **RabbitMQ Overview**

### **RabbitMQ Core Concepts**

```java
/**
 * RabbitMQ implementation with Java
 */

// 1. Basic RabbitMQ Configuration
@Configuration
@EnableRabbit
public class RabbitMQConfig {

    @Bean
    public ConnectionFactory connectionFactory() {
        var factory = new CachingConnectionFactory();
        factory.setHost("localhost");
        factory.setPort(5672);
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setVirtualHost("/");

        // Connection pooling
        factory.setChannelCacheSize(25);
        factory.setChannelCheckoutTimeout(30000);

        return factory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        var template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        template.setRetryTemplate(retryTemplate());
        template.setConfirmCallback(confirmCallback());
        template.setMandatory(true); // Ensure message routing

        return template;
    }

    @Bean
    public RetryTemplate retryTemplate() {
        return RetryTemplate.builder()
                           .maxAttempts(3)
                           .exponentialBackoff(1000, 2, 10000)
                           .build();
    }

    private RabbitTemplate.ConfirmCallback confirmCallback() {
        return (correlationData, ack, cause) -> {
            if (ack) {
                logger.info("Message delivered successfully");
            } else {
                logger.error("Message delivery failed: {}", cause);
            }
        };
    }
}

// 2. Exchange and Queue Declarations
@Configuration
public class RabbitMQTopology {

    // Direct Exchange - Route by exact key match
    @Bean
    public DirectExchange orderExchange() {
        return ExchangeBuilder.directExchange("order.exchange")
                             .durable(true)
                             .build();
    }

    // Topic Exchange - Route by pattern matching
    @Bean
    public TopicExchange notificationExchange() {
        return ExchangeBuilder.topicExchange("notification.exchange")
                             .durable(true)
                             .build();
    }

    // Fanout Exchange - Broadcast to all queues
    @Bean
    public FanoutExchange auditExchange() {
        return ExchangeBuilder.fanoutExchange("audit.exchange")
                             .durable(true)
                             .build();
    }

    // Headers Exchange - Route by message headers
    @Bean
    public HeadersExchange headersExchange() {
        return ExchangeBuilder.headersExchange("headers.exchange")
                             .durable(true)
                             .build();
    }

    // Queue declarations
    @Bean
    public Queue orderProcessingQueue() {
        return QueueBuilder.durable("order.processing")
                          .withArgument("x-message-ttl", 60000) // 60 seconds TTL
                          .withArgument("x-max-length", 1000)   // Max 1000 messages
                          .build();
    }

    @Bean
    public Queue deadLetterQueue() {
        return QueueBuilder.durable("order.processing.dlq")
                          .build();
    }

    @Bean
    public Queue orderProcessingWithDLQ() {
        return QueueBuilder.durable("order.processing")
                          .withArgument("x-dead-letter-exchange", "dlx.exchange")
                          .withArgument("x-dead-letter-routing-key", "failed")
                          .withArgument("x-message-ttl", 60000)
                          .withArgument("x-max-retries", 3)
                          .build();
    }

    // Bindings
    @Bean
    public Binding orderProcessingBinding() {
        return BindingBuilder.bind(orderProcessingQueue())
                           .to(orderExchange())
                           .with("order.process");
    }

    @Bean
    public Binding emailNotificationBinding() {
        return BindingBuilder.bind(emailQueue())
                           .to(notificationExchange())
                           .with("notification.email.*");
    }

    @Bean
    public Binding smsNotificationBinding() {
        return BindingBuilder.bind(smsQueue())
                           .to(notificationExchange())
                           .with("notification.sms.*");
    }
}

// 3. RabbitMQ Producer
@Service
public class RabbitMQProducer implements MessageProducer<Object> {
    private final RabbitTemplate rabbitTemplate;

    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void send(String routingKey, Object message) {
        send("default.exchange", routingKey, message);
    }

    public void send(String exchange, String routingKey, Object message) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message, messagePostProcessor());
        } catch (AmqpException e) {
            throw new MessageSendException("Failed to send message", e);
        }
    }

    @Override
    public CompletableFuture<SendResult> sendAsync(String routingKey, Object message) {
        return CompletableFuture.supplyAsync(() -> {
            send(routingKey, message);
            return new SendResult(routingKey, message);
        });
    }

    // Send with custom headers and properties
    public void sendWithHeaders(String exchange, String routingKey, Object message,
                               Map<String, Object> headers) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
            headers.forEach((key, value) -> msg.getMessageProperties().setHeader(key, value));
            return msg;
        });
    }

    // Send with priority
    public void sendWithPriority(String exchange, String routingKey, Object message, int priority) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
            msg.getMessageProperties().setPriority(priority);
            return msg;
        });
    }

    // Delayed message (requires rabbitmq-delayed-message-exchange plugin)
    public void sendDelayed(String exchange, String routingKey, Object message, Duration delay) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message, msg -> {
            msg.getMessageProperties().setHeader("x-delay", delay.toMillis());
            return msg;
        });
    }

    private MessagePostProcessor messagePostProcessor() {
        return message -> {
            var props = message.getMessageProperties();
            props.setHeader("timestamp", System.currentTimeMillis());
            props.setHeader("producer", "order-service");
            props.setContentType("application/json");
            props.setDeliveryMode(MessageDeliveryMode.PERSISTENT); // Persistent messages
            return message;
        };
    }
}

// 4. RabbitMQ Consumer
@Component
public class RabbitMQConsumer {

    // Basic consumer
    @RabbitListener(queues = "order.processing")
    public void processOrder(OrderEvent orderEvent,
                           @Header Map<String, Object> headers,
                           Channel channel,
                           @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            logger.info("Processing order: {}", orderEvent.getOrderId());

            // Business logic
            processOrderLogic(orderEvent);

            // Manual acknowledgment
            channel.basicAck(deliveryTag, false);

        } catch (Exception e) {
            logger.error("Failed to process order: {}", orderEvent.getOrderId(), e);

            try {
                // Reject and requeue (or send to DLQ)
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ioException) {
                logger.error("Failed to nack message", ioException);
            }
        }
    }

    // Batch consumer
    @RabbitListener(queues = "notification.batch",
                   containerFactory = "batchListenerContainerFactory")
    public void processBatchNotifications(List<NotificationEvent> notifications) {
        logger.info("Processing batch of {} notifications", notifications.size());

        notifications.forEach(this::processNotification);
    }

    // Consumer with retry and DLQ
    @RabbitListener(queues = "payment.processing")
    @RetryableTopic(
        attempts = "3",
        backoff = @Backoff(delay = 1000, multiplier = 2.0),
        dltStrategy = DltStrategy.FAIL_ON_ERROR,
        include = {PaymentException.class}
    )
    public void processPayment(PaymentEvent payment) {
        if (shouldSimulateFailure()) {
            throw new PaymentException("Simulated payment failure");
        }

        logger.info("Processing payment: {}", payment.getPaymentId());
        // Process payment logic
    }

    // Dead Letter Queue handler
    @RabbitListener(queues = "order.processing.dlq")
    public void handleFailedOrders(OrderEvent orderEvent,
                                 @Header Map<String, Object> headers) {
        logger.error("Handling failed order: {} with headers: {}",
                   orderEvent.getOrderId(), headers);

        // Could send to human review, alert system, etc.
        alertService.notifyFailedOrder(orderEvent);
    }

    // Priority queue consumer
    @RabbitListener(queues = "high.priority.orders")
    public void processHighPriorityOrder(OrderEvent orderEvent) {
        logger.info("Processing high priority order: {}", orderEvent.getOrderId());
        // Expedited processing logic
    }
}

// 5. Advanced RabbitMQ Patterns
@Service
public class RabbitMQAdvancedPatterns {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMQAdvancedPatterns(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Request-Reply Pattern
    public CompletableFuture<OrderValidationResult> validateOrderAsync(OrderValidationRequest request) {
        var future = new CompletableFuture<OrderValidationResult>();

        var correlationId = UUID.randomUUID().toString();
        var replyQueue = "validation.reply." + correlationId;

        // Send request
        rabbitTemplate.convertAndSend("validation.exchange", "validate", request, message -> {
            message.getMessageProperties().setReplyTo(replyQueue);
            message.getMessageProperties().setCorrelationId(correlationId);
            return message;
        });

        // Listen for reply
        rabbitTemplate.receiveAndConvert(replyQueue, 5000); // 5 second timeout

        return future;
    }

    // Publish-Subscribe with Topics
    public void publishUserEvent(String eventType, UserEvent event) {
        var routingKey = "user." + eventType + "." + event.getUserType();
        rabbitTemplate.convertAndSend("user.exchange", routingKey, event);
    }

    // Competing Consumers Pattern
    @RabbitListener(queues = "task.processing", concurrency = "5-10")
    public void processTask(TaskEvent task) {
        // Multiple consumers compete for tasks
        // Auto-scaling based on queue depth
        logger.info("Processing task: {} on thread: {}",
                  task.getTaskId(), Thread.currentThread().getName());
    }

    // Saga Pattern with Compensation
    public void executeOrderSaga(OrderEvent order) {
        var sagaId = UUID.randomUUID().toString();

        try {
            // Step 1: Reserve inventory
            var inventoryResult = sendAndReceive("inventory.reserve", order, sagaId);

            // Step 2: Process payment
            var paymentResult = sendAndReceive("payment.process", order, sagaId);

            // Step 3: Ship order
            sendAndReceive("shipping.ship", order, sagaId);

        } catch (Exception e) {
            // Compensate - reverse previous steps
            compensateOrderSaga(sagaId, order);
            throw e;
        }
    }

    private void compensateOrderSaga(String sagaId, OrderEvent order) {
        // Send compensation commands
        rabbitTemplate.convertAndSend("inventory.release", order);
        rabbitTemplate.convertAndSend("payment.refund", order);
    }
}
```

---

## 🔗 **Apache ActiveMQ**

### **ActiveMQ Implementation**

```java
/**
 * Apache ActiveMQ implementation patterns
 */

// 1. ActiveMQ Configuration
@Configuration
@EnableJms
public class ActiveMQConfig {

    @Bean
    public ConnectionFactory connectionFactory() {
        var factory = new ActiveMQConnectionFactory();
        factory.setBrokerURL("tcp://localhost:61616");
        factory.setUserName("admin");
        factory.setPassword("admin");

        // Connection pooling
        var pooled = new PooledConnectionFactory();
        pooled.setConnectionFactory(factory);
        pooled.setMaxConnections(10);
        pooled.setMaximumActiveSessionPerConnection(20);
        pooled.setBlockIfSessionPoolIsFull(true);

        return pooled;
    }

    @Bean
    public JmsTemplate jmsTemplate(ConnectionFactory connectionFactory) {
        var template = new JmsTemplate(connectionFactory);
        template.setDefaultDestinationName("default.queue");
        template.setMessageConverter(new MappingJackson2MessageConverter());
        template.setSessionTransacted(true);
        template.setReceiveTimeout(5000);

        return template;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        var factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setConcurrency("3-10"); // 3 to 10 concurrent consumers
        factory.setSessionTransacted(true);
        factory.setAutoStartup(true);

        return factory;
    }
}

// 2. ActiveMQ Producer
@Service
public class ActiveMQProducer implements MessageProducer<Object> {
    private final JmsTemplate jmsTemplate;

    public ActiveMQProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    @Override
    public void send(String destination, Object message) {
        jmsTemplate.convertAndSend(destination, message);
    }

    @Override
    public void send(String destination, String messageSelector, Object message) {
        jmsTemplate.convertAndSend(destination, message, messagePostProcessor -> {
            messagePostProcessor.setStringProperty("selector", messageSelector);
            return messagePostProcessor;
        });
    }

    @Override
    public CompletableFuture<SendResult> sendAsync(String destination, Object message) {
        return CompletableFuture.supplyAsync(() -> {
            send(destination, message);
            return new SendResult(destination, message);
        });
    }

    // Send to topic (publish-subscribe)
    public void sendToTopic(String topicName, Object message) {
        jmsTemplate.send(topicName, session -> {
            var topic = session.createTopic(topicName);
            var jmsMessage = session.createObjectMessage((Serializable) message);
            return jmsMessage;
        });
    }

    // Send with priority
    public void sendWithPriority(String destination, Object message, int priority) {
        jmsTemplate.send(destination, session -> {
            var jmsMessage = session.createObjectMessage((Serializable) message);
            jmsMessage.setJMSPriority(priority);
            return jmsMessage;
        });
    }

    // Send with delay (ActiveMQ Artemis feature)
    public void sendWithDelay(String destination, Object message, Duration delay) {
        jmsTemplate.send(destination, session -> {
            var jmsMessage = session.createObjectMessage((Serializable) message);
            jmsMessage.setLongProperty("_AMQ_SCHED_DELIVERY",
                                     System.currentTimeMillis() + delay.toMillis());
            return jmsMessage;
        });
    }
}

// 3. ActiveMQ Consumer
@Component
public class ActiveMQConsumer {

    // Basic queue consumer
    @JmsListener(destination = "order.queue")
    public void processOrder(OrderEvent order,
                           @Header Map<String, Object> headers,
                           Session session) throws JMSException {
        try {
            logger.info("Processing order: {}", order.getOrderId());
            processOrderLogic(order);

            // Transaction will auto-commit if no exception

        } catch (Exception e) {
            logger.error("Failed to process order", e);

            // Transaction will rollback, message returns to queue
            throw new JmsException("Processing failed", e);
        }
    }

    // Topic subscriber (durable subscription)
    @JmsListener(destination = "user.events.topic",
                subscription = "userEventSubscription",
                containerFactory = "topicListenerFactory")
    public void handleUserEvents(UserEvent event) {
        logger.info("Received user event: {} for user: {}",
                  event.getEventType(), event.getUserId());
    }

    // Message selector
    @JmsListener(destination = "notification.queue",
                selector = "priority > 5")
    public void processHighPriorityNotifications(NotificationEvent notification) {
        logger.info("Processing high priority notification: {}", notification.getId());
    }

    // Competing consumers with concurrency
    @JmsListener(destination = "task.queue", concurrency = "5-15")
    public void processTask(TaskEvent task) {
        logger.info("Processing task: {} on thread: {}",
                  task.getTaskId(), Thread.currentThread().getName());
    }
}

// 4. ActiveMQ Advanced Features
@Service
public class ActiveMQAdvancedFeatures {
    private final JmsTemplate jmsTemplate;

    public ActiveMQAdvancedFeatures(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    // Request-Reply Pattern
    public OrderValidationResult validateOrder(OrderValidationRequest request) {
        return (OrderValidationResult) jmsTemplate.convertSendAndReceive(
            "validation.queue", request);
    }

    // Virtual Topics (ActiveMQ specific)
    public void publishToVirtualTopic(String topicName, Object message) {
        var virtualTopic = "VirtualTopic." + topicName;
        jmsTemplate.convertAndSend(virtualTopic, message);
    }

    // Consumers for Virtual Topics subscribe to:
    // "Consumer.{consumerName}.VirtualTopic.{topicName}"
    @JmsListener(destination = "Consumer.OrderService.VirtualTopic.UserEvents")
    public void handleUserEventsForOrders(UserEvent event) {
        // Only order service gets these events
    }

    @JmsListener(destination = "Consumer.NotificationService.VirtualTopic.UserEvents")
    public void handleUserEventsForNotifications(UserEvent event) {
        // Only notification service gets these events
    }

    // Message Groups (ensure ordering)
    public void sendOrderedMessages(String groupId, List<Object> messages) {
        messages.forEach(message -> {
            jmsTemplate.send("ordered.queue", session -> {
                var jmsMessage = session.createObjectMessage((Serializable) message);
                jmsMessage.setStringProperty("JMSXGroupID", groupId);
                return jmsMessage;
            });
        });
    }
}
```

---

## ☁️ **AWS SQS/SNS**

### **AWS Message Services Implementation**

```java
/**
 * AWS SQS and SNS implementation with Java
 */

// 1. AWS SQS Configuration
@Configuration
public class AWSSQSConfig {

    @Bean
    public AmazonSQS amazonSQS() {
        return AmazonSQSClientBuilder.standard()
                                   .withRegion(Regions.US_EAST_1)
                                   .withCredentials(new DefaultAWSCredentialsProviderChain())
                                   .build();
    }

    @Bean
    public QueueMessagingTemplate queueMessagingTemplate(AmazonSQS amazonSQS) {
        return new QueueMessagingTemplate(amazonSQS);
    }
}

// 2. SQS Producer
@Service
public class SQSProducer implements MessageProducer<Object> {
    private final QueueMessagingTemplate messagingTemplate;
    private final AmazonSQS amazonSQS;

    public SQSProducer(QueueMessagingTemplate messagingTemplate, AmazonSQS amazonSQS) {
        this.messagingTemplate = messagingTemplate;
        this.amazonSQS = amazonSQS;
    }

    @Override
    public void send(String queueName, Object message) {
        messagingTemplate.convertAndSend(queueName, message);
    }

    @Override
    public void send(String queueName, String messageGroupId, Object message) {
        // For FIFO queues
        messagingTemplate.convertAndSend(queueName, message,
            Map.of("message-group-id", messageGroupId));
    }

    @Override
    public CompletableFuture<SendResult> sendAsync(String queueName, Object message) {
        return CompletableFuture.supplyAsync(() -> {
            send(queueName, message);
            return new SendResult(queueName, message);
        });
    }

    // Send with custom attributes
    public void sendWithAttributes(String queueName, Object message,
                                 Map<String, Object> attributes) {
        messagingTemplate.convertAndSend(queueName, message, attributes);
    }

    // Send with delay (up to 15 minutes)
    public void sendWithDelay(String queueName, Object message, Duration delay) {
        var delaySeconds = (int) delay.getSeconds();
        if (delaySeconds > 900) { // 15 minutes max
            throw new IllegalArgumentException("Delay cannot exceed 15 minutes");
        }

        var sendRequest = new SendMessageRequest()
            .withQueueUrl(getQueueUrl(queueName))
            .withMessageBody(serializeMessage(message))
            .withDelaySeconds(delaySeconds);

        amazonSQS.sendMessage(sendRequest);
    }

    // Batch send (up to 10 messages)
    public void sendBatch(String queueName, List<Object> messages) {
        if (messages.size() > 10) {
            throw new IllegalArgumentException("Batch size cannot exceed 10 messages");
        }

        var entries = messages.stream()
                             .map(this::createBatchEntry)
                             .collect(Collectors.toList());

        var batchRequest = new SendMessageBatchRequest()
            .withQueueUrl(getQueueUrl(queueName))
            .withEntries(entries);

        var result = amazonSQS.sendMessageBatch(batchRequest);

        if (!result.getFailed().isEmpty()) {
            logger.error("Failed to send {} messages", result.getFailed().size());
        }
    }

    private SendMessageBatchRequestEntry createBatchEntry(Object message) {
        return new SendMessageBatchRequestEntry()
            .withId(UUID.randomUUID().toString())
            .withMessageBody(serializeMessage(message));
    }

    private String getQueueUrl(String queueName) {
        return amazonSQS.getQueueUrl(queueName).getQueueUrl();
    }

    private String serializeMessage(Object message) {
        // Use Jackson or similar for serialization
        return JsonUtils.toJson(message);
    }
}

// 3. SQS Consumer
@Component
public class SQSConsumer {

    // Basic SQS listener
    @SqsListener("order-processing-queue")
    public void processOrder(OrderEvent order,
                           @Header Map<String, String> headers) {
        try {
            logger.info("Processing order: {}", order.getOrderId());
            processOrderLogic(order);

            // Message auto-acknowledged on successful completion

        } catch (Exception e) {
            logger.error("Failed to process order", e);
            // Message will be returned to queue based on visibility timeout
            throw e;
        }
    }

    // FIFO queue listener
    @SqsListener("order-processing-queue.fifo")
    public void processOrderFIFO(OrderEvent order,
                                @Header("message-group-id") String groupId) {
        logger.info("Processing FIFO order: {} in group: {}",
                  order.getOrderId(), groupId);
    }

    // Dead Letter Queue handler
    @SqsListener("order-processing-dlq")
    public void handleFailedOrders(OrderEvent order,
                                 @Header Map<String, String> headers) {
        logger.error("Handling failed order: {} with headers: {}",
                   order.getOrderId(), headers);

        // Send to human review or alert system
        alertService.notifyFailedOrder(order);
    }

    // Batch consumer
    @SqsListener(value = "batch-processing-queue", deletionPolicy = SqsMessageDeletionPolicy.NEVER)
    public void processBatch(List<OrderEvent> orders, Acknowledgment ack) {
        try {
            logger.info("Processing batch of {} orders", orders.size());

            orders.forEach(this::processOrderLogic);

            // Manual acknowledgment for batch processing
            ack.acknowledge();

        } catch (Exception e) {
            logger.error("Failed to process batch", e);
            // Don't acknowledge - messages will return to queue
        }
    }
}

// 4. AWS SNS Configuration and Usage
@Configuration
public class AWSSNSConfig {

    @Bean
    public AmazonSNS amazonSNS() {
        return AmazonSNSClientBuilder.standard()
                                   .withRegion(Regions.US_EAST_1)
                                   .withCredentials(new DefaultAWSCredentialsProviderChain())
                                   .build();
    }

    @Bean
    public NotificationMessagingTemplate notificationMessagingTemplate(AmazonSNS amazonSNS) {
        return new NotificationMessagingTemplate(amazonSNS);
    }
}

@Service
public class SNSPublisher {
    private final NotificationMessagingTemplate messagingTemplate;
    private final AmazonSNS amazonSNS;

    public SNSPublisher(NotificationMessagingTemplate messagingTemplate, AmazonSNS amazonSNS) {
        this.messagingTemplate = messagingTemplate;
        this.amazonSNS = amazonSNS;
    }

    // Publish to SNS topic
    public void publishToTopic(String topicArn, Object message) {
        messagingTemplate.convertAndSend(topicArn, message);
    }

    // Publish with message attributes
    public void publishWithAttributes(String topicArn, Object message,
                                    Map<String, Object> attributes) {
        messagingTemplate.convertAndSend(topicArn, message, attributes);
    }

    // Publish to multiple protocols (SMS, Email, HTTP, SQS)
    public void publishMultiProtocol(String topicArn, String subject, String message) {
        var publishRequest = new PublishRequest()
            .withTopicArn(topicArn)
            .withSubject(subject)
            .withMessage(message);

        amazonSNS.publish(publishRequest);
    }

    // Fan-out pattern: SNS -> Multiple SQS queues
    public void setupFanOut(String topicArn, List<String> queueArns) {
        queueArns.forEach(queueArn -> {
            var subscribeRequest = new SubscribeRequest()
                .withTopicArn(topicArn)
                .withProtocol("sqs")
                .withEndpoint(queueArn);

            amazonSNS.subscribe(subscribeRequest);
        });
    }
}

// 5. Combined SQS/SNS Patterns
@Service
public class AWSMessagingPatterns {
    private final SNSPublisher snsPublisher;
    private final SQSProducer sqsProducer;

    public AWSMessagingPatterns(SNSPublisher snsPublisher, SQSProducer sqsProducer) {
        this.snsPublisher = snsPublisher;
        this.sqsProducer = sqsProducer;
    }

    // Event-driven architecture with SNS fan-out
    public void publishOrderEvent(OrderEvent orderEvent) {
        // Publish to SNS topic
        var topicArn = "arn:aws:sns:us-east-1:123456789012:order-events";
        snsPublisher.publishToTopic(topicArn, orderEvent);

        // Multiple services subscribe to this topic:
        // - Inventory Service (SQS queue)
        // - Billing Service (SQS queue)
        // - Analytics Service (SQS queue)
        // - Email Service (Email endpoint)
        // - SMS Service (SMS endpoint)
    }

    // Request-Response with SQS
    public CompletableFuture<OrderValidationResult> validateOrderAsync(
            OrderValidationRequest request) {
        var future = new CompletableFuture<OrderValidationResult>();
        var correlationId = UUID.randomUUID().toString();

        // Send request to validation queue
        sqsProducer.sendWithAttributes("validation-requests", request,
            Map.of("correlation-id", correlationId,
                  "reply-queue", "validation-replies"));

        // Poll reply queue (or use SQS listener with correlation ID)
        // Implementation would involve setting up a correlation map

        return future;
    }

    // Dead Letter Queue with alarms
    public void setupDLQMonitoring(String dlqUrl) {
        // CloudWatch alarm would be set up to monitor DLQ depth
        // When messages appear in DLQ, trigger alerts
        var alarm = new PutMetricAlarmRequest()
            .withAlarmName("OrderProcessingDLQAlarm")
            .withComparisonOperator(ComparisonOperator.GreaterThanThreshold)
            .withEvaluationPeriods(1)
            .withMetricName("ApproximateNumberOfVisibleMessages")
            .withNamespace("AWS/SQS")
            .withPeriod(300)
            .withStatistic(Statistic.Sum)
            .withThreshold(1.0)
            .withActionsEnabled(true)
            .withAlarmActions("arn:aws:sns:us-east-1:123456789012:dlq-alerts")
            .withDimensions(new Dimension()
                .withName("QueueName")
                .withValue("order-processing-dlq"));
    }
}
```

---

## ⚖️ **Technology Comparison**

### **Feature Comparison Matrix**

| Feature | Apache Kafka | RabbitMQ | ActiveMQ | AWS SQS/SNS | Redis Pub/Sub |
|---------|-------------|----------|----------|-------------|---------------|
| **Message Ordering** | ✅ Per partition | ✅ Per queue | ✅ Per queue | ✅ FIFO queues only | ❌ No guarantees |
| **Persistence** | ✅ Disk-based | ✅ Optional | ✅ Optional | ✅ Managed | ❌ Memory only |
| **Message Retention** | ✅ Configurable (days/size) | ✅ Until consumed | ✅ Until consumed | ✅ 14 days max | ❌ No retention |
| **Throughput** | 🔥 Very High (1M+ msg/sec) | ⚡ High (50K msg/sec) | ⚡ High (100K msg/sec) | ⚡ High (managed) | 🔥 Very High |
| **Latency** | ⚡ Low (< 10ms) | ⚡ Low (< 5ms) | ⚡ Low (< 5ms) | ⚡ Low (managed) | 🔥 Very Low (< 1ms) |
| **Scalability** | 🔥 Horizontal | ⚡ Vertical + Clustering | ⚡ Vertical + Clustering | 🔥 Auto-scaling | ⚡ Clustering |
| **Message Routing** | ❌ Topic-based only | ✅ Advanced routing | ✅ Advanced routing | ⚡ Basic routing | ✅ Topic patterns |
| **Transaction Support** | ✅ Full ACID | ✅ Full ACID | ✅ Full ACID | ❌ None | ❌ None |
| **Multi-Consumer** | ✅ Consumer groups | ✅ Competing consumers | ✅ Competing consumers | ✅ Multiple queues | ✅ Multiple subscribers |
| **Dead Letter Queue** | ❌ Manual implementation | ✅ Built-in | ✅ Built-in | ✅ Built-in | ❌ Manual implementation |
| **Management UI** | ✅ Third-party | ✅ Built-in | ✅ Built-in | ✅ AWS Console | ✅ Third-party |
| **Cloud Native** | ⚡ Confluent Cloud | ⚡ CloudAMQP | ❌ Self-hosted mainly | 🔥 Fully managed | ✅ Redis Cloud |
| **Setup Complexity** | 🔥 High | ⚡ Medium | ⚡ Medium | 🔥 None (managed) | ⚡ Low |
| **Monitoring** | ⚡ JMX + tools | ✅ Built-in | ✅ Built-in | 🔥 CloudWatch | ⚡ Info commands |

### **Performance Benchmarks**

```java
/**
 * Performance comparison examples
 */

// Kafka Performance Test
public class KafkaPerformanceTest {

    @Test
    public void testKafkaThroughput() {
        var producer = createKafkaProducer();
        var messageCount = 1_000_000;
        var messageSize = 1024; // 1KB messages

        var startTime = System.currentTimeMillis();

        for (int i = 0; i < messageCount; i++) {
            var message = generateMessage(messageSize);
            producer.send(new ProducerRecord<>("test-topic", message));
        }

        producer.flush();
        var endTime = System.currentTimeMillis();

        var duration = endTime - startTime;
        var throughput = messageCount * 1000.0 / duration;

        logger.info("Kafka Throughput: {} messages/second", throughput);
        // Typical result: 500K-1M+ messages/second
    }

    @Test
    public void testKafkaLatency() {
        var producer = createKafkaProducer();
        var consumer = createKafkaConsumer();

        var latencies = new ArrayList<Long>();

        for (int i = 0; i < 10000; i++) {
            var sendTime = System.nanoTime();

            producer.send(new ProducerRecord<>("test-topic", "test-message-" + i));

            // Consumer polls and measures end-to-end latency
            var records = consumer.poll(Duration.ofMillis(100));
            if (!records.isEmpty()) {
                var receiveTime = System.nanoTime();
                latencies.add((receiveTime - sendTime) / 1_000_000); // Convert to ms
            }
        }

        var avgLatency = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
        logger.info("Kafka Average Latency: {}ms", avgLatency);
        // Typical result: 5-15ms
    }
}

// RabbitMQ Performance Test
public class RabbitMQPerformanceTest {

    @Test
    public void testRabbitMQThroughput() {
        var connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("localhost");

        try (var connection = connectionFactory.newConnection();
             var channel = connection.createChannel()) {

            var queueName = "test-queue";
            channel.queueDeclare(queueName, true, false, false, null);

            var messageCount = 100_000;
            var message = "test message".getBytes();

            var startTime = System.currentTimeMillis();

            for (int i = 0; i < messageCount; i++) {
                channel.basicPublish("", queueName, null, message);
            }

            var endTime = System.currentTimeMillis();
            var duration = endTime - startTime;
            var throughput = messageCount * 1000.0 / duration;

            logger.info("RabbitMQ Throughput: {} messages/second", throughput);
            // Typical result: 20K-50K messages/second
        }
    }
}

// SQS Performance Test
public class SQSPerformanceTest {

    @Test
    public void testSQSThroughput() {
        var sqs = AmazonSQSClientBuilder.defaultClient();
        var queueUrl = sqs.getQueueUrl("test-queue").getQueueUrl();

        var messageCount = 10_000;
        var batchSize = 10; // SQS max batch size

        var startTime = System.currentTimeMillis();

        for (int i = 0; i < messageCount; i += batchSize) {
            var entries = new ArrayList<SendMessageBatchRequestEntry>();

            for (int j = 0; j < batchSize && (i + j) < messageCount; j++) {
                entries.add(new SendMessageBatchRequestEntry()
                    .withId(String.valueOf(i + j))
                    .withMessageBody("test message " + (i + j)));
            }

            sqs.sendMessageBatch(new SendMessageBatchRequest()
                .withQueueUrl(queueUrl)
                .withEntries(entries));
        }

        var endTime = System.currentTimeMillis();
        var duration = endTime - startTime;
        var throughput = messageCount * 1000.0 / duration;

        logger.info("SQS Throughput: {} messages/second", throughput);
        // Typical result: 1K-5K messages/second (limited by API calls)
    }
}
```

### **Use Case Decision Matrix**

```java
/**
 * When to use each technology
 */
public class MessagingTechnologyDecisionGuide {

    // Use Kafka when:
    public static class KafkaUseCases {
        /*
         * ✅ Event Streaming & Real-time Analytics
         * ✅ High throughput requirements (100K+ msg/sec)
         * ✅ Event sourcing and audit logs
         * ✅ Stream processing with Kafka Streams
         * ✅ Log aggregation and monitoring
         * ✅ Data pipeline integration
         * ✅ Microservices event choreography
         *
         * ❌ Simple request-response patterns
         * ❌ Complex routing requirements
         * ❌ Low latency requirements (< 1ms)
         * ❌ Small team with limited ops experience
         */

        public void eventSourcingExample() {
            // Perfect for event sourcing
            kafkaProducer.send("user.events", new UserCreatedEvent());
            kafkaProducer.send("user.events", new UserUpdatedEvent());
            kafkaProducer.send("user.events", new UserDeletedEvent());

            // Can replay entire event history
            // Multiple services can consume same events
        }

        public void streamProcessingExample() {
            // Real-time stream processing
            var builder = new StreamsBuilder();

            builder.stream("user.events")
                   .filter((key, event) -> event.getEventType().equals("PURCHASE"))
                   .groupBy((key, event) -> event.getUserId())
                   .windowedBy(TimeWindows.of(Duration.ofHours(1)))
                   .aggregate(PurchaseStats::new,
                            (key, event, stats) -> stats.addPurchase(event));
        }
    }

    // Use RabbitMQ when:
    public static class RabbitMQUseCases {
        /*
         * ✅ Complex routing requirements
         * ✅ Request-response patterns
         * ✅ Workflow orchestration
         * ✅ Priority queues
         * ✅ Delayed message delivery
         * ✅ Dead letter queue handling
         * ✅ Traditional enterprise integration
         *
         * ❌ Very high throughput requirements
         * ❌ Event sourcing/replay scenarios
         * ❌ Stream processing requirements
         */

        public void complexRoutingExample() {
            // Topic exchange with pattern matching
            rabbitTemplate.convertAndSend("notification.exchange",
                                        "notification.email.urgent",
                                        new EmailNotification());

            rabbitTemplate.convertAndSend("notification.exchange",
                                        "notification.sms.normal",
                                        new SMSNotification());

            // Consumers can subscribe to patterns:
            // "notification.email.*" - all email notifications
            // "notification.*.urgent" - all urgent notifications
        }

        public void workflowOrchestrationExample() {
            // Saga pattern with compensation
            @RabbitListener(queues = "order.processing")
            public void processOrder(OrderEvent order) {
                try {
                    // Step 1: Process payment
                    var payment = processPayment(order);

                    // Step 2: Reserve inventory
                    var reservation = reserveInventory(order);

                    // Step 3: Ship order
                    shipOrder(order);

                } catch (Exception e) {
                    // Compensate
                    compensateOrder(order);
                }
            }
        }
    }

    // Use ActiveMQ when:
    public static class ActiveMQUseCases {
        /*
         * ✅ JMS compliance requirements
         * ✅ Enterprise Java applications
         * ✅ Existing ActiveMQ infrastructure
         * ✅ XA transaction support
         * ✅ Message persistence requirements
         * ✅ Classic enterprise patterns
         *
         * ❌ Modern microservices architecture
         * ❌ Cloud-native applications
         * ❌ Very high throughput requirements
         */

        public void jmsComplianceExample() {
            @JmsListener(destination = "order.queue")
            @Transactional
            public void processOrder(OrderEvent order, Session session) throws JMSException {
                // Full JMS API available
                // XA transactions supported
                // Enterprise integration patterns
            }
        }
    }

    // Use AWS SQS/SNS when:
    public static class AWSSQSUseCases {
        /*
         * ✅ AWS cloud environment
         * ✅ No infrastructure management
         * ✅ Auto-scaling requirements
         * ✅ Fan-out patterns (SNS)
         * ✅ Serverless architectures
         * ✅ FIFO requirements with scaling
         *
         * ❌ On-premises deployments
         * ❌ Very high throughput requirements
         * ❌ Complex routing needs
         * ❌ Long message retention
         */

        public void serverlessExample() {
            // Perfect for serverless
            @SqsListener("order-processing")
            public void processOrder(OrderEvent order) {
                // Lambda function triggered by SQS
                // Auto-scaling based on queue depth
                // No server management
            }
        }

        public void fanOutExample() {
            // SNS to multiple SQS queues
            snsPublisher.publish("order.events", orderEvent);

            // Automatically fans out to:
            // - billing-service-queue
            // - inventory-service-queue
            // - analytics-service-queue
            // - email-service (direct)
        }
    }

    // Use Redis Pub/Sub when:
    public static class RedisUseCases {
        /*
         * ✅ Very low latency requirements (< 1ms)
         * ✅ Simple pub/sub patterns
         * ✅ Real-time notifications
         * ✅ Cache invalidation
         * ✅ Chat applications
         * ✅ Live updates
         *
         * ❌ Message persistence requirements
         * ❌ Guaranteed delivery needs
         * ❌ Complex routing requirements
         * ❌ Ordered message processing
         */

        public void realTimeNotificationExample() {
            // Instant notifications
            redisTemplate.convertAndSend("user.notifications.123",
                                       new NotificationEvent());

            // Subscribers get immediate delivery
            // No persistence - fire and forget
        }
    }
}
```

### **Architectural Patterns Comparison**

```java
/**
 * Common messaging patterns across technologies
 */

// 1. Event-Driven Architecture
public class EventDrivenArchitectureComparison {

    // Kafka approach - Event streaming
    public void kafkaEventDriven() {
        // Producer
        kafkaProducer.send("domain.events",
                         new OrderPlacedEvent(orderId, customerId, amount));

        // Multiple consumers
        @KafkaListener(topics = "domain.events")
        public void handleOrderEvent(OrderPlacedEvent event) {
            // Each service processes relevant events
            // Events are persisted and can be replayed
        }
    }

    // RabbitMQ approach - Event routing
    public void rabbitMQEventDriven() {
        // Publisher
        rabbitTemplate.convertAndSend("domain.exchange",
                                    "order.placed",
                                    new OrderPlacedEvent());

        // Subscribers with routing
        @RabbitListener(bindings = @QueueBinding(
            value = @Queue("billing.orders"),
            exchange = @Exchange("domain.exchange"),
            key = "order.*"
        ))
        public void handleBillingEvents(OrderEvent event) {
            // Routing based on event type
        }
    }

    // SNS approach - Fan-out
    public void snsEventDriven() {
        // Publisher
        sns.publish("arn:aws:sns:region:account:domain-events", orderEvent);

        // Multiple SQS queues subscribed
        // Each service has its own queue
        @SqsListener("billing-service-queue")
        public void handleBillingEvents(OrderEvent event) {
            // Managed fan-out by AWS
        }
    }
}

// 2. Saga Pattern Implementation
public class SagaPatternComparison {

    // Kafka approach - Choreography
    public void kafkaSaga() {
        // Each service publishes events
        @KafkaListener(topics = "order.events")
        public void handleOrderCreated(OrderCreatedEvent event) {
            try {
                paymentService.processPayment(event.getOrderId());
                kafkaProducer.send("payment.events",
                                 new PaymentProcessedEvent(event.getOrderId()));
            } catch (Exception e) {
                kafkaProducer.send("payment.events",
                                 new PaymentFailedEvent(event.getOrderId()));
            }
        }

        @KafkaListener(topics = "payment.events")
        public void handlePaymentProcessed(PaymentProcessedEvent event) {
            inventoryService.reserveInventory(event.getOrderId());
        }
    }

    // RabbitMQ approach - Orchestration
    public void rabbitMQSaga() {
        // Central orchestrator
        @Component
        public class OrderSagaOrchestrator {

            public void processOrder(OrderEvent order) {
                var sagaId = UUID.randomUUID().toString();

                // Send commands to services
                rabbitTemplate.convertAndSend("payment.commands",
                    new ProcessPaymentCommand(sagaId, order));
            }

            @RabbitListener(queues = "saga.replies")
            public void handleSagaReply(SagaReply reply) {
                if (reply.isSuccess()) {
                    continueWorkflow(reply.getSagaId());
                } else {
                    compensateWorkflow(reply.getSagaId());
                }
            }
        }
    }
}

// 3. CQRS Implementation
public class CQRSPatternComparison {

    // Kafka approach - Event sourcing
    public void kafkaCQRS() {
        // Command side
        @PostMapping("/orders")
        public ResponseEntity<String> createOrder(@RequestBody CreateOrderCommand command) {
            var event = new OrderCreatedEvent(command);
            kafkaProducer.send("order.events", event);
            return ResponseEntity.accepted().build();
        }

        // Query side - materialized views
        @KafkaListener(topics = "order.events")
        public void updateOrderView(OrderEvent event) {
            orderViewRepository.updateView(event);
        }

        @GetMapping("/orders/{id}")
        public OrderView getOrder(@PathVariable String id) {
            return orderViewRepository.findById(id);
        }
    }

    // RabbitMQ approach - Command/Query separation
    public void rabbitMQCQRS() {
        // Command side
        @PostMapping("/orders")
        public ResponseEntity<String> createOrder(@RequestBody CreateOrderCommand command) {
            rabbitTemplate.convertAndSend("order.commands", command);
            return ResponseEntity.accepted().build();
        }

        // Separate query service
        @GetMapping("/orders/{id}")
        public OrderView getOrder(@PathVariable String id) {
            return queryService.getOrder(id);
        }
    }
}
```

---

## 💡 **Best Practices**

### **General Messaging Best Practices**

```java
/**
 * Universal best practices across all messaging technologies
 */

// 1. Message Design
public class MessageDesignBestPractices {

    // Good: Self-contained message
    public class OrderCreatedEvent {
        private String orderId;
        private String customerId;
        private BigDecimal amount;
        private List<OrderItem> items;
        private Instant timestamp;
        private String eventVersion = "1.0";

        // Include all necessary data
        // Version for schema evolution
        // Timestamp for ordering/debugging
    }

    // Bad: Reference-based message
    public class BadOrderEvent {
        private String orderId; // Only ID - requires lookup
        // Missing context, hard to process independently
    }

    // Message envelope pattern
    public class MessageEnvelope<T> {
        private String messageId;
        private String correlationId;
        private String causationId;
        private Instant timestamp;
        private String messageType;
        private Map<String, String> metadata;
        private T payload;

        // Provides traceability and context
    }
}

// 2. Error Handling and Resilience
public class ErrorHandlingBestPractices {

    // Retry with exponential backoff
    @Component
    public class ResilientMessageProcessor {

        @RetryableTopic(
            attempts = "5",
            backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 10000),
            dltStrategy = DltStrategy.FAIL_ON_ERROR
        )
        @KafkaListener(topics = "order.processing")
        public void processOrder(OrderEvent order) {
            try {
                orderService.processOrder(order);
            } catch (TransientException e) {
                // Will be retried
                throw e;
            } catch (PermanentException e) {
                // Send to DLQ immediately
                throw new NonRetryableException(e);
            }
        }

        // Dead letter queue handler
        @KafkaListener(topics = "order.processing.dlt")
        public void handleFailedOrder(OrderEvent order) {
            // Log for analysis
            // Send to human review
            // Trigger alerts
            failedOrderService.handleFailure(order);
        }
    }

    // Circuit breaker pattern
    @Component
    public class CircuitBreakerExample {

        @CircuitBreaker(name = "external-service", fallbackMethod = "fallbackProcess")
        @KafkaListener(topics = "external.requests")
        public void processWithExternalService(RequestEvent request) {
            externalService.process(request);
        }

        public void fallbackProcess(RequestEvent request, Exception ex) {
            // Store for later processing
            // Use cached data
            // Return default response
            fallbackService.handle(request);
        }
    }
}

// 3. Monitoring and Observability
public class MonitoringBestPractices {

    @Component
    public class MessageMetrics {
        private final MeterRegistry meterRegistry;
        private final Counter messagesSent;
        private final Counter messagesReceived;
        private final Timer processingTime;

        public MessageMetrics(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
            this.messagesSent = Counter.builder("messages.sent")
                                     .description("Total messages sent")
                                     .register(meterRegistry);
            this.messagesReceived = Counter.builder("messages.received")
                                          .description("Total messages received")
                                          .register(meterRegistry);
            this.processingTime = Timer.builder("message.processing.time")
                                      .description("Message processing time")
                                      .register(meterRegistry);
        }

        public void recordMessageSent(String topic) {
            messagesSent.increment(
                Tags.of(Tag.of("topic", topic)));
        }

        public void recordMessageProcessed(String topic, Duration duration) {
            messagesReceived.increment(
                Tags.of(Tag.of("topic", topic)));
            processingTime.record(duration);
        }
    }

    // Distributed tracing
    @Component
    public class TracingExample {

        @NewSpan("message-processing")
        @KafkaListener(topics = "order.processing")
        public void processOrder(OrderEvent order,
                               @SpanTag("order.id") String orderId) {
            // Tracing automatically propagated
            // Custom span tags for filtering
            orderService.processOrder(order);
        }

        // Correlation ID propagation
        public void sendMessageWithTracing(String topic, Object message) {
            var correlationId = MDC.get("correlationId");
            if (correlationId == null) {
                correlationId = UUID.randomUUID().toString();
            }

            kafkaTemplate.send(topic, message, messagePostProcessor -> {
                messagePostProcessor.getHeaders().put("correlationId", correlationId);
                return messagePostProcessor;
            });
        }
    }
}

// 4. Schema Evolution
public class SchemaEvolutionBestPractices {

    // Backward compatible changes
    public class OrderEventV1 {
        private String orderId;
        private String customerId;
        private BigDecimal amount;
        // Original fields
    }

    public class OrderEventV2 {
        private String orderId;
        private String customerId;
        private BigDecimal amount;
        // Backward compatible additions
        private String customerEmail; // New optional field
        private Map<String, String> metadata = new HashMap<>(); // New optional field
        private String version = "2.0"; // Version field
    }

    // Message router for different versions
    @Component
    public class VersionedMessageProcessor {

        @KafkaListener(topics = "order.events")
        public void processOrderEvent(ConsumerRecord<String, String> record) {
            var headers = record.headers();
            var version = getVersion(headers);

            switch (version) {
                case "1.0" -> processV1(deserializeV1(record.value()));
                case "2.0" -> processV2(deserializeV2(record.value()));
                default -> throw new UnsupportedVersionException("Version: " + version);
            }
        }

        private void processV1(OrderEventV1 event) {
            // Handle v1 events
        }

        private void processV2(OrderEventV2 event) {
            // Handle v2 events
        }
    }
}

// 5. Security Best Practices
public class SecurityBestPractices {

    // Message encryption
    @Component
    public class MessageEncryption {
        private final AESUtil aesUtil;

        @EventListener
        public void encryptSensitiveData(OrderEvent event) {
            if (containsSensitiveData(event)) {
                event.setCustomerData(aesUtil.encrypt(event.getCustomerData()));
                event.setEncrypted(true);
            }
        }

        @KafkaListener(topics = "encrypted.orders")
        public void processEncryptedOrder(OrderEvent event) {
            if (event.isEncrypted()) {
                event.setCustomerData(aesUtil.decrypt(event.getCustomerData()));
            }
            processOrder(event);
        }
    }

    // Access control
    @Configuration
    public class MessageSecurityConfig {

        @Bean
        public KafkaConsumerFactory<String, Object> secureConsumerFactory() {
            var props = new HashMap<String, Object>();

            // SASL/SSL configuration
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
            props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256");
            props.put(SaslConfigs.SASL_JAAS_CONFIG,
                     "org.apache.kafka.common.security.scram.ScramLoginModule required " +
                     "username=\"user\" password=\"password\";");

            return new DefaultKafkaConsumerFactory<>(props);
        }
    }
}
```

---

## ❓ **Common Interview Questions**

### **Q1: Explain the difference between message queues and event streams**

**Answer:**
```java
// Message Queue (RabbitMQ, SQS)
// - Messages consumed once and removed
// - Focus on task distribution
// - Temporary storage until processed

@RabbitListener(queues = "order.processing")
public void processOrder(OrderEvent order) {
    // Message consumed and removed from queue
    // Only one consumer processes this message
    orderService.processOrder(order);
}

// Event Stream (Kafka)
// - Events persist and can be replayed
// - Focus on data streaming and history
// - Long-term storage for multiple consumers

@KafkaListener(topics = "order.events")
public void handleOrderEvent(OrderEvent event) {
    // Event remains in topic
    // Multiple consumers can process same event
    // Can replay from beginning
    analyticsService.trackOrder(event);
}

// Use cases:
// Queue: Task processing, work distribution
// Stream: Event sourcing, analytics, audit logs
```

### **Q2: How do you ensure message ordering?**

**Answer:**
```java
// Kafka - Partition-level ordering
public void sendOrderedMessages() {
    var customerId = "CUST-123";

    // All messages with same key go to same partition
    kafkaProducer.send(new ProducerRecord<>("orders", customerId, orderEvent1));
    kafkaProducer.send(new ProducerRecord<>("orders", customerId, orderEvent2));
    kafkaProducer.send(new ProducerRecord<>("orders", customerId, orderEvent3));

    // Order guaranteed within partition for customerId
}

// RabbitMQ - Single consumer or message groups
@RabbitListener(queues = "orders", concurrency = "1") // Single consumer
public void processOrdersInSequence(OrderEvent event) {
    // Sequential processing ensures order
}

// ActiveMQ - Message groups
public void sendGroupedMessages() {
    jmsTemplate.send("orders", session -> {
        var message = session.createObjectMessage(orderEvent);
        message.setStringProperty("JMSXGroupID", "customer-123");
        return message;
    });
    // All messages with same group ID processed in order
}

// Trade-offs:
// - Ordering reduces parallelism
// - Single partition/consumer = bottleneck
// - Consider if strict ordering is necessary
```

### **Q3: How do you handle duplicate messages?**

**Answer:**
```java
// 1. Idempotent processing
@Component
public class IdempotentOrderProcessor {
    private final RedisTemplate<String, String> redisTemplate;

    @KafkaListener(topics = "orders")
    public void processOrder(OrderEvent event) {
        var messageId = event.getMessageId();
        var lockKey = "processing:" + messageId;

        // Redis SET with NX (only if not exists)
        var acquired = redisTemplate.opsForValue()
                                   .setIfAbsent(lockKey, "processing",
                                               Duration.ofMinutes(5));

        if (!acquired) {
            logger.info("Message {} already processed", messageId);
            return; // Skip duplicate
        }

        try {
            orderService.processOrder(event);
            redisTemplate.opsForValue().set(lockKey, "completed");
        } catch (Exception e) {
            redisTemplate.delete(lockKey); // Allow retry
            throw e;
        }
    }
}

// 2. Database unique constraints
@Entity
public class ProcessedMessage {
    @Id
    private String messageId; // Unique constraint prevents duplicates
    private Instant processedAt;

    @PrePersist
    public void setProcessedAt() {
        this.processedAt = Instant.now();
    }
}

// 3. Kafka exactly-once semantics
public void configureExactlyOnce() {
    var props = new Properties();
    props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
    props.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, "order-producer");

    var producer = new KafkaProducer<>(props);
    producer.initTransactions();

    try {
        producer.beginTransaction();
        producer.send(new ProducerRecord<>("orders", order));
        producer.commitTransaction();
    } catch (Exception e) {
        producer.abortTransaction();
    }
}
```

### **Q4: Explain backpressure handling in message systems**

**Answer:**
```java
// 1. Consumer-side backpressure
@Component
public class BackpressureAwareConsumer {

    @KafkaListener(
        topics = "high-volume-topic",
        concurrency = "1-10", // Auto-scale consumers
        containerFactory = "backpressureContainerFactory"
    )
    public void processMessage(MessageEvent event) {
        // Slow processing creates backpressure
        heavyProcessingService.process(event);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object>
           backpressureContainerFactory() {
        var factory = new ConcurrentKafkaListenerContainerFactory<>();

        // Limit max poll records
        var consumerProps = new HashMap<String, Object>();
        consumerProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 100);
        consumerProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 1000);

        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerProps));
        return factory;
    }
}

// 2. Producer-side flow control
@Component
public class FlowControlledProducer {
    private final Semaphore semaphore = new Semaphore(1000); // Limit in-flight

    public void sendWithFlowControl(String topic, Object message) {
        try {
            semaphore.acquire(); // Block if too many in-flight

            kafkaProducer.send(new ProducerRecord<>(topic, message),
                (metadata, exception) -> {
                    semaphore.release(); // Release on completion

                    if (exception != null) {
                        // Handle send failure
                        handleSendFailure(message, exception);
                    }
                });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while waiting for permit", e);
        }
    }
}

// 3. Circuit breaker for downstream services
@Component
public class CircuitBreakerConsumer {

    @CircuitBreaker(
        name = "downstream-service",
        fallbackMethod = "fallbackProcess"
    )
    @KafkaListener(topics = "requests")
    public void processRequest(RequestEvent request) {
        downstreamService.process(request);
    }

    public void fallbackProcess(RequestEvent request, Exception ex) {
        // Store for later processing when service recovers
        fallbackStorage.store(request);
    }
}

// 4. Reactive streams approach
@Component
public class ReactiveMessageProcessor {

    public Flux<ProcessedEvent> processMessages(Flux<MessageEvent> messages) {
        return messages
            .buffer(Duration.ofSeconds(1), 100) // Batch processing
            .flatMap(this::processBatch, 5) // Limit concurrent batches
            .onBackpressureBuffer(1000) // Buffer overflow messages
            .doOnError(this::handleError);
    }

    private Flux<ProcessedEvent> processBatch(List<MessageEvent> batch) {
        return Flux.fromIterable(batch)
                  .flatMap(this::processMessage, 10); // Parallel processing
    }
}
```

### **Q5: How do you implement the Saga pattern?**

**Answer:**
```java
// Choreography-based Saga (Event-driven)
@Component
public class OrderSagaChoreography {

    // Order Service
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        // Publish event for next step
        eventPublisher.publishEvent(new PaymentRequestedEvent(event.getOrderId()));
    }

    // Payment Service
    @EventListener
    public void handlePaymentRequested(PaymentRequestedEvent event) {
        try {
            paymentProcessor.processPayment(event.getOrderId());
            eventPublisher.publishEvent(new PaymentCompletedEvent(event.getOrderId()));
        } catch (PaymentFailedException e) {
            eventPublisher.publishEvent(new PaymentFailedEvent(event.getOrderId()));
        }
    }

    // Inventory Service
    @EventListener
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        try {
            inventoryService.reserveItems(event.getOrderId());
            eventPublisher.publishEvent(new InventoryReservedEvent(event.getOrderId()));
        } catch (InsufficientInventoryException e) {
            // Compensate - refund payment
            eventPublisher.publishEvent(new PaymentRefundRequestedEvent(event.getOrderId()));
        }
    }

    // Compensation handlers
    @EventListener
    public void handlePaymentRefundRequested(PaymentRefundRequestedEvent event) {
        paymentProcessor.refundPayment(event.getOrderId());
        eventPublisher.publishEvent(new OrderFailedEvent(event.getOrderId()));
    }
}

// Orchestration-based Saga (Centralized)
@Component
public class OrderSagaOrchestrator {

    @SagaOrchestrationStart
    public void processOrder(OrderCreatedEvent event) {
        var sagaManager = SagaManager.create(event.getOrderId());

        sagaManager
            .step("payment")
                .action(() -> paymentService.processPayment(event))
                .compensation(() -> paymentService.refundPayment(event))
            .step("inventory")
                .action(() -> inventoryService.reserveItems(event))
                .compensation(() -> inventoryService.releaseItems(event))
            .step("shipping")
                .action(() -> shippingService.createShipment(event))
                .compensation(() -> shippingService.cancelShipment(event))
            .execute();
    }

    @Component
    public static class SagaManager {
        private final List<SagaStep> steps = new ArrayList<>();
        private final String sagaId;

        public static SagaManager create(String sagaId) {
            return new SagaManager(sagaId);
        }

        public StepBuilder step(String stepName) {
            return new StepBuilder(this, stepName);
        }

        public void execute() {
            var completedSteps = new ArrayList<SagaStep>();

            try {
                for (var step : steps) {
                    step.execute();
                    completedSteps.add(step);
                }
            } catch (Exception e) {
                // Compensate in reverse order
                Collections.reverse(completedSteps);
                completedSteps.forEach(SagaStep::compensate);
                throw new SagaExecutionException("Saga failed", e);
            }
        }
    }
}

// Event sourcing for Saga state
@Entity
public class SagaEvent {
    @Id
    private String id;
    private String sagaId;
    private String stepName;
    private SagaEventType eventType;
    private Instant timestamp;
    private String payload;

    public enum SagaEventType {
        STEP_STARTED, STEP_COMPLETED, STEP_FAILED, SAGA_COMPLETED, SAGA_FAILED
    }
}

@Component
public class SagaEventStore {

    public void recordSagaEvent(String sagaId, String stepName,
                               SagaEventType eventType, Object payload) {
        var event = new SagaEvent();
        event.setSagaId(sagaId);
        event.setStepName(stepName);
        event.setEventType(eventType);
        event.setPayload(JsonUtils.toJson(payload));
        event.setTimestamp(Instant.now());

        sagaEventRepository.save(event);

        // Publish for monitoring/recovery
        eventPublisher.publishEvent(event);
    }

    public List<SagaEvent> getSagaHistory(String sagaId) {
        return sagaEventRepository.findBySagaIdOrderByTimestamp(sagaId);
    }

    public SagaState getCurrentState(String sagaId) {
        var events = getSagaHistory(sagaId);
        return SagaState.fromEvents(events);
    }
}
```

---

## 🎯 **Summary**

### **Technology Selection Guide**

- **Apache Kafka**: High-throughput event streaming, event sourcing, real-time analytics
- **RabbitMQ**: Complex routing, enterprise integration, traditional messaging patterns
- **ActiveMQ**: JMS compliance, enterprise Java, existing infrastructure
- **AWS SQS/SNS**: Cloud-native, serverless, managed infrastructure
- **Redis Pub/Sub**: Ultra-low latency, simple pub/sub, cache invalidation

### **Key Considerations**

1. **Throughput vs Latency**: Kafka for throughput, Redis for latency
2. **Durability**: Kafka for long-term storage, SQS for managed durability
3. **Complexity**: Redis for simplicity, Kafka for advanced features
4. **Operations**: AWS managed services vs self-hosted solutions
5. **Ecosystem**: Java/JMS (ActiveMQ), Cloud (AWS), Microservices (Kafka)

---

*This comprehensive guide covers all major message queue technologies with practical Java examples for technical interviews.*