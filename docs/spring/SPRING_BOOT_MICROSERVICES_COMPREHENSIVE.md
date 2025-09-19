# Spring Boot Microservices Architecture Comprehensive Guide

## 📖 Table of Contents

### 1. [Microservices Fundamentals](#microservices-fundamentals)
- [Q1: Microservices Architecture Principles](#q1-microservices-architecture-principles)
- [Q2: Spring Boot for Microservices](#q2-spring-boot-for-microservices)
- [Q3: Service Decomposition Strategies](#q3-service-decomposition-strategies)

### 2. [Service Discovery](#service-discovery)
- [Q4: Eureka Service Discovery](#q4-eureka-service-discovery)
- [Q5: Consul Integration](#q5-consul-integration)
- [Q6: Client-Side vs Server-Side Discovery](#q6-client-side-vs-server-side-discovery)

### 3. [API Gateway Patterns](#api-gateway-patterns)
- [Q7: Spring Cloud Gateway Implementation](#q7-spring-cloud-gateway-implementation)
- [Q8: Rate Limiting and Security](#q8-rate-limiting-and-security)
- [Q9: Request Routing and Load Balancing](#q9-request-routing-and-load-balancing)

### 4. [Circuit Breakers and Resilience](#circuit-breakers-and-resilience)
- [Q10: Resilience4j Implementation](#q10-resilience4j-implementation)
- [Q11: Bulkhead and Timeout Patterns](#q11-bulkhead-and-timeout-patterns)
- [Q12: Fallback Strategies](#q12-fallback-strategies)

### 5. [Configuration Management](#configuration-management)
- [Q13: Spring Cloud Config Server](#q13-spring-cloud-config-server)
- [Q14: Distributed Configuration Patterns](#q14-distributed-configuration-patterns)
- [Q15: Configuration Refresh and Updates](#q15-configuration-refresh-and-updates)

### 6. [Banking Microservices Use Cases](#banking-microservices-use-cases)
- [Q16: Account Management Service](#q16-account-management-service)
- [Q17: Payment Processing Service](#q17-payment-processing-service)
- [Q18: Event-Driven Transaction Processing](#q18-event-driven-transaction-processing)

---

## 🏗️ Microservices Fundamentals

### Q1: Microservices Architecture Principles

**Question**: What are the core principles of microservices architecture and how does Spring Boot facilitate their implementation?

**Answer**:

**Core Microservices Principles:**

1. **Single Responsibility**: Each service owns a specific business capability
2. **Autonomous**: Services can be developed, deployed, and scaled independently
3. **Decentralized**: No central coordination, services make local decisions
4. **Resilient**: Failures in one service don't cascade to others
5. **Observable**: Comprehensive monitoring and logging
6. **Automation**: Infrastructure and deployment automation

**Spring Boot Implementation:**

```java
// 1. Service-Oriented Architecture Base
@SpringBootApplication
@EnableDiscoveryClient
@EnableCircuitBreaker
@EnableConfigurationProperties
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }

    // Service configuration
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}

// 2. Domain-Driven Design Implementation
@RestController
@RequestMapping("/api/accounts")
@Validated
public class AccountController {

    private final AccountService accountService;
    private final AccountEventPublisher eventPublisher;

    public AccountController(AccountService accountService,
                           AccountEventPublisher eventPublisher) {
        this.accountService = accountService;
        this.eventPublisher = eventPublisher;
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody CreateAccountRequest request) {

        Account account = accountService.createAccount(request);

        // Publish domain event
        eventPublisher.publishAccountCreated(account);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(AccountMapper.toResponse(account));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String id) {
        Account account = accountService.findById(id);
        return ResponseEntity.ok(AccountMapper.toResponse(account));
    }

    @PutMapping("/{id}/balance")
    @Transactional
    public ResponseEntity<Void> updateBalance(
            @PathVariable String id,
            @Valid @RequestBody UpdateBalanceRequest request) {

        accountService.updateBalance(id, request.getAmount(), request.getType());
        return ResponseEntity.ok().build();
    }
}

// 3. Business Logic Layer
@Service
@Transactional
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerServiceClient customerServiceClient;
    private final AuditService auditService;

    public AccountService(AccountRepository accountRepository,
                         CustomerServiceClient customerServiceClient,
                         AuditService auditService) {
        this.accountRepository = accountRepository;
        this.customerServiceClient = customerServiceClient;
        this.auditService = auditService;
    }

    public Account createAccount(CreateAccountRequest request) {
        // Validate customer exists (cross-service call)
        Customer customer = customerServiceClient.getCustomer(request.getCustomerId());
        if (customer == null) {
            throw new CustomerNotFoundException("Customer not found: " + request.getCustomerId());
        }

        // Apply business rules
        validateAccountCreationRules(request, customer);

        // Create account
        Account account = Account.builder()
            .accountNumber(generateAccountNumber())
            .customerId(request.getCustomerId())
            .accountType(request.getAccountType())
            .balance(request.getInitialDeposit())
            .currency(request.getCurrency())
            .status(AccountStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .build();

        account = accountRepository.save(account);

        // Audit trail
        auditService.logAccountCreation(account);

        return account;
    }

    @CircuitBreaker(name = "account-service", fallbackMethod = "fallbackUpdateBalance")
    @Retry(name = "account-service")
    @TimeLimiter(name = "account-service")
    public void updateBalance(String accountId, BigDecimal amount, TransactionType type) {
        Account account = findById(accountId);

        // Business validation
        if (type == TransactionType.DEBIT && account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient funds");
        }

        // Update balance
        BigDecimal newBalance = type == TransactionType.CREDIT
            ? account.getBalance().add(amount)
            : account.getBalance().subtract(amount);

        account.setBalance(newBalance);
        account.setUpdatedAt(LocalDateTime.now());

        accountRepository.save(account);

        // Async notification to other services
        eventPublisher.publishBalanceUpdated(account, amount, type);
    }

    public CompletableFuture<Void> fallbackUpdateBalance(String accountId, BigDecimal amount,
                                                        TransactionType type, Exception ex) {
        log.error("Circuit breaker activated for account balance update: {}", ex.getMessage());
        // Queue for later processing or notify operations team
        return CompletableFuture.completedFuture(null);
    }

    private void validateAccountCreationRules(CreateAccountRequest request, Customer customer) {
        // Business validation logic
        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new InvalidCustomerStatusException("Customer is not active");
        }

        if (request.getInitialDeposit().compareTo(getMinimumDeposit(request.getAccountType())) < 0) {
            throw new InsufficientDepositException("Initial deposit below minimum");
        }
    }

    private BigDecimal getMinimumDeposit(AccountType accountType) {
        // Business rules for minimum deposit
        return switch (accountType) {
            case CHECKING -> new BigDecimal("25.00");
            case SAVINGS -> new BigDecimal("50.00");
            case PREMIUM -> new BigDecimal("1000.00");
            default -> new BigDecimal("0.00");
        };
    }

    private String generateAccountNumber() {
        // Account number generation logic
        return "ACC" + System.currentTimeMillis();
    }
}

// 4. Data Access Layer
@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    List<Account> findByCustomerId(String customerId);

    List<Account> findByAccountType(AccountType accountType);

    @Query("SELECT a FROM Account a WHERE a.balance > :amount")
    List<Account> findAccountsWithBalanceGreaterThan(@Param("amount") BigDecimal amount);

    @Modifying
    @Query("UPDATE Account a SET a.status = :status WHERE a.customerId = :customerId")
    int updateAccountStatusByCustomerId(@Param("customerId") String customerId,
                                       @Param("status") AccountStatus status);
}

// 5. External Service Integration
@Component
public class CustomerServiceClient {

    private final WebClient webClient;
    private final String customerServiceUrl;

    public CustomerServiceClient(WebClient.Builder webClientBuilder,
                               @Value("${services.customer.url}") String customerServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(customerServiceUrl).build();
        this.customerServiceUrl = customerServiceUrl;
    }

    @CircuitBreaker(name = "customer-service", fallbackMethod = "getCustomerFallback")
    @Retry(name = "customer-service")
    public Customer getCustomer(String customerId) {
        return webClient.get()
            .uri("/api/customers/{id}", customerId)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError,
                response -> Mono.error(new CustomerNotFoundException("Customer not found")))
            .onStatus(HttpStatus::is5xxServerError,
                response -> Mono.error(new ServiceUnavailableException("Customer service unavailable")))
            .bodyToMono(Customer.class)
            .timeout(Duration.ofSeconds(5))
            .block();
    }

    public Customer getCustomerFallback(String customerId, Exception ex) {
        log.warn("Customer service fallback triggered for customer: {}, error: {}",
                customerId, ex.getMessage());

        // Return cached customer data or default
        return customerCacheService.getCachedCustomer(customerId)
            .orElse(Customer.builder()
                .id(customerId)
                .status(CustomerStatus.UNKNOWN)
                .build());
    }
}

// 6. Event Publishing for Inter-Service Communication
@Component
public class AccountEventPublisher {

    private final ApplicationEventPublisher eventPublisher;
    private final MessageProducer messageProducer;

    public AccountEventPublisher(ApplicationEventPublisher eventPublisher,
                               MessageProducer messageProducer) {
        this.eventPublisher = eventPublisher;
        this.messageProducer = messageProducer;
    }

    public void publishAccountCreated(Account account) {
        AccountCreatedEvent event = AccountCreatedEvent.builder()
            .accountId(account.getId())
            .customerId(account.getCustomerId())
            .accountType(account.getAccountType())
            .initialBalance(account.getBalance())
            .timestamp(LocalDateTime.now())
            .build();

        // Local event publishing
        eventPublisher.publishEvent(event);

        // External event publishing (message queue)
        messageProducer.sendAccountCreatedEvent(event);
    }

    public void publishBalanceUpdated(Account account, BigDecimal amount, TransactionType type) {
        BalanceUpdatedEvent event = BalanceUpdatedEvent.builder()
            .accountId(account.getId())
            .previousBalance(account.getBalance().subtract(amount))
            .newBalance(account.getBalance())
            .transactionAmount(amount)
            .transactionType(type)
            .timestamp(LocalDateTime.now())
            .build();

        eventPublisher.publishEvent(event);
        messageProducer.sendBalanceUpdatedEvent(event);
    }
}

// 7. Configuration Management
@Configuration
@ConfigurationProperties(prefix = "microservice")
public class MicroserviceConfiguration {

    private String serviceName;
    private String version;
    private Security security = new Security();
    private Resilience resilience = new Resilience();
    private Monitoring monitoring = new Monitoring();

    @Data
    public static class Security {
        private boolean enabled = true;
        private String algorithm = "RS256";
        private String keyStore;
        private String keyStorePassword;
    }

    @Data
    public static class Resilience {
        private int retryAttempts = 3;
        private Duration timeout = Duration.ofSeconds(30);
        private double failureRateThreshold = 50.0;
        private int slidingWindowSize = 10;
    }

    @Data
    public static class Monitoring {
        private boolean metricsEnabled = true;
        private boolean tracingEnabled = true;
        private String tracingUrl;
    }

    // getters and setters
}
```

**Application Properties for Microservice:**

```yaml
# application.yml
spring:
  application:
    name: account-service
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  # Database configuration
  datasource:
    url: jdbc:postgresql://localhost:5432/account_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect

  # Messaging
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:guest}
    password: ${RABBITMQ_PASSWORD:guest}

# Service Discovery
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka}
    register-with-eureka: true
    fetch-registry: true
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 10
    lease-expiration-duration-in-seconds: 30

# Microservice Configuration
microservice:
  service-name: account-service
  version: 1.0.0
  security:
    enabled: true
    algorithm: RS256
  resilience:
    retry-attempts: 3
    timeout: 30s
    failure-rate-threshold: 50.0
    sliding-window-size: 10

# External Service URLs
services:
  customer:
    url: ${CUSTOMER_SERVICE_URL:http://customer-service}
  notification:
    url: ${NOTIFICATION_SERVICE_URL:http://notification-service}
  audit:
    url: ${AUDIT_SERVICE_URL:http://audit-service}

# Resilience4j Configuration
resilience4j:
  circuitbreaker:
    instances:
      customer-service:
        registerHealthIndicator: true
        slidingWindowSize: 10
        permittedNumberOfCallsInHalfOpenState: 3
        slidingWindowType: COUNT_BASED
        waitDurationInOpenState: 5s
        failureRateThreshold: 50
        eventConsumerBufferSize: 10
  retry:
    instances:
      customer-service:
        maxAttempts: 3
        waitDuration: 1s
        retryExceptions:
          - java.net.ConnectException
          - java.net.SocketTimeoutException
  timelimiter:
    instances:
      customer-service:
        timeoutDuration: 5s

# Management endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true

# Logging
logging:
  level:
    com.bank.account: DEBUG
    org.springframework.cloud: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId},%X{spanId}] %logger{36} - %msg%n"
```

### Q2: Spring Boot for Microservices

**Question**: How do you structure a Spring Boot application for optimal microservice development?

**Answer**:

**Project Structure Best Practices:**

```
account-service/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/bank/account/
│   │   │       ├── AccountServiceApplication.java
│   │   │       ├── config/
│   │   │       │   ├── DatabaseConfig.java
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── MessagingConfig.java
│   │   │       │   └── WebConfig.java
│   │   │       ├── controller/
│   │   │       │   ├── AccountController.java
│   │   │       │   └── HealthController.java
│   │   │       ├── service/
│   │   │       │   ├── AccountService.java
│   │   │       │   ├── AuditService.java
│   │   │       │   └── ValidationService.java
│   │   │       ├── repository/
│   │   │       │   └── AccountRepository.java
│   │   │       ├── model/
│   │   │       │   ├── entity/
│   │   │       │   │   └── Account.java
│   │   │       │   ├── dto/
│   │   │       │   │   ├── request/
│   │   │       │   │   └── response/
│   │   │       │   └── event/
│   │   │       ├── client/
│   │   │       │   ├── CustomerServiceClient.java
│   │   │       │   └── NotificationServiceClient.java
│   │   │       ├── exception/
│   │   │       │   ├── GlobalExceptionHandler.java
│   │   │       │   └── AccountException.java
│   │   │       └── util/
│   │   │           ├── mapper/
│   │   │           └── validator/
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-prod.yml
│   │       ├── db/migration/
│   │       └── static/
│   └── test/
│       ├── java/
│       │   └── com/bank/account/
│       │       ├── integration/
│       │       ├── unit/
│       │       └── contract/
│       └── resources/
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
├── k8s/
│   ├── deployment.yaml
│   ├── service.yaml
│   └── configmap.yaml
├── pom.xml
└── README.md
```

**Core Dependencies (pom.xml):**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.0</version>
        <relativePath/>
    </parent>

    <groupId>com.bank</groupId>
    <artifactId>account-service</artifactId>
    <version>1.0.0</version>
    <name>account-service</name>
    <description>Account Management Microservice</description>

    <properties>
        <java.version>17</java.version>
        <spring-cloud.version>2023.0.0</spring-cloud.version>
        <resilience4j.version>2.1.0</resilience4j.version>
        <mapstruct.version>1.5.5.Final</mapstruct.version>
    </properties>

    <dependencies>
        <!-- Spring Boot Starters -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>

        <!-- Spring Cloud -->
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-loadbalancer</artifactId>
        </dependency>

        <!-- Resilience -->
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-spring-boot3</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-reactor</artifactId>
        </dependency>

        <!-- Database -->
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>

        <!-- Monitoring -->
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>

        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-tracing-bridge-brave</artifactId>
        </dependency>

        <!-- Utilities -->
        <dependency>
            <groupId>org.mapstruct</groupId>
            <artifactId>mapstruct</artifactId>
            <version>${mapstruct.version}</version>
        </dependency>

        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-contract-wiremock</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>postgresql</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>${spring-cloud.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.mapstruct</groupId>
                            <artifactId>mapstruct-processor</artifactId>
                            <version>${mapstruct.version}</version>
                        </path>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

**Advanced Configuration Classes:**

```java
// Database Configuration
@Configuration
@EnableJpaRepositories(basePackages = "com.bank.account.repository")
@EnableJpaAuditing
public class DatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource() {
        return dataSourceProperties()
            .initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("system");
            }
            return Optional.of(authentication.getName());
        };
    }
}

// Web Configuration
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(false)
            .maxAge(3600);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RequestLoggingInterceptor())
            .addPathPatterns("/api/**");
    }

    @Bean
    public FilterRegistrationBean<RequestResponseLoggingFilter> loggingFilter() {
        FilterRegistrationBean<RequestResponseLoggingFilter> registrationBean =
            new FilterRegistrationBean<>();
        registrationBean.setFilter(new RequestResponseLoggingFilter());
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }
}

// Messaging Configuration
@Configuration
@EnableRabbit
public class MessagingConfig {

    @Bean
    public TopicExchange accountExchange() {
        return ExchangeBuilder.topicExchange("account.exchange")
            .durable(true)
            .build();
    }

    @Bean
    public Queue accountCreatedQueue() {
        return QueueBuilder.durable("account.created.queue")
            .withArgument("x-dead-letter-exchange", "account.dlx")
            .build();
    }

    @Bean
    public Binding accountCreatedBinding() {
        return BindingBuilder.bind(accountCreatedQueue())
            .to(accountExchange())
            .with("account.created");
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        template.setRetryTemplate(retryTemplate());
        return template;
    }

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(2000);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}

// Security Configuration
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/accounts/**")
                    .hasAnyRole("USER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/accounts")
                    .hasAnyRole("USER", "ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtDecoder(jwtDecoder())));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation("${spring.security.oauth2.resourceserver.jwt.issuer-uri}");
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthorityPrefix("ROLE_");
        authoritiesConverter.setAuthoritiesClaimName("roles");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return authenticationConverter;
    }
}
```

### Q3: Service Decomposition Strategies

**Question**: How do you decompose a monolithic banking application into microservices using domain-driven design principles?

**Answer**:

**Domain-Driven Decomposition Strategy:**

```java
// 1. Bounded Context Identification
/**
 * Banking Domain Bounded Contexts:
 *
 * 1. Customer Management Context
 *    - Customer profiles, KYC, preferences
 *    - Services: customer-service
 *
 * 2. Account Management Context
 *    - Account lifecycle, balance management
 *    - Services: account-service
 *
 * 3. Transaction Processing Context
 *    - Payment processing, transfers, transactions
 *    - Services: transaction-service, payment-service
 *
 * 4. Credit Management Context
 *    - Credit scoring, loan processing, risk assessment
 *    - Services: credit-service, risk-service
 *
 * 5. Notification Context
 *    - Alerts, communications, messaging
 *    - Services: notification-service
 *
 * 6. Audit & Compliance Context
 *    - Audit trails, regulatory reporting
 *    - Services: audit-service, compliance-service
 */

// 2. Customer Service (Bounded Context)
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        Customer customer = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(CustomerMapper.toResponse(customer));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomer(@PathVariable String id) {
        Customer customer = customerService.findById(id);
        return ResponseEntity.ok(CustomerMapper.toResponse(customer));
    }

    @PutMapping("/{id}/kyc")
    public ResponseEntity<Void> updateKyc(
            @PathVariable String id,
            @Valid @RequestBody UpdateKycRequest request) {
        customerService.updateKyc(id, request);
        return ResponseEntity.ok().build();
    }
}

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final KycService kycService;
    private final CustomerEventPublisher eventPublisher;

    public Customer createCustomer(CreateCustomerRequest request) {
        // Domain validation
        validateCustomerData(request);

        Customer customer = Customer.builder()
            .customerId(generateCustomerId())
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .phoneNumber(request.getPhoneNumber())
            .dateOfBirth(request.getDateOfBirth())
            .status(CustomerStatus.PENDING_VERIFICATION)
            .createdAt(LocalDateTime.now())
            .build();

        customer = customerRepository.save(customer);

        // Publish domain event
        eventPublisher.publishCustomerCreated(customer);

        return customer;
    }

    public void updateKyc(String customerId, UpdateKycRequest request) {
        Customer customer = findById(customerId);

        // Apply KYC business rules
        KycResult kycResult = kycService.performKyc(request);

        customer.setKycStatus(kycResult.getStatus());
        customer.setKycCompletedAt(LocalDateTime.now());

        if (kycResult.isApproved()) {
            customer.setStatus(CustomerStatus.ACTIVE);
            eventPublisher.publishCustomerActivated(customer);
        } else {
            customer.setStatus(CustomerStatus.KYC_FAILED);
            eventPublisher.publishCustomerKycFailed(customer, kycResult.getFailureReasons());
        }

        customerRepository.save(customer);
    }

    private void validateCustomerData(CreateCustomerRequest request) {
        // Business validation logic
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new CustomerAlreadyExistsException("Customer with email already exists");
        }

        if (!isValidAge(request.getDateOfBirth())) {
            throw new InvalidAgeException("Customer must be at least 18 years old");
        }
    }

    private boolean isValidAge(LocalDate dateOfBirth) {
        return Period.between(dateOfBirth, LocalDate.now()).getYears() >= 18;
    }
}

// 3. Transaction Service (Bounded Context)
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @Valid @RequestBody TransferRequest request) {
        Transaction transaction = transactionService.processTransfer(request);
        return ResponseEntity.ok(TransactionMapper.toResponse(transaction));
    }

    @PostMapping("/payment")
    public ResponseEntity<TransactionResponse> payment(
            @Valid @RequestBody PaymentRequest request) {
        Transaction transaction = transactionService.processPayment(request);
        return ResponseEntity.ok(TransactionMapper.toResponse(transaction));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<Transaction> transactions = transactionService.getTransactionHistory(accountId, pageable);

        List<TransactionResponse> responses = transactions.getContent().stream()
            .map(TransactionMapper::toResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }
}

@Service
@Transactional
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountServiceClient accountServiceClient;
    private final FraudDetectionService fraudDetectionService;
    private final TransactionEventPublisher eventPublisher;

    @Transactional
    public Transaction processTransfer(TransferRequest request) {
        // Validate accounts exist
        Account fromAccount = accountServiceClient.getAccount(request.getFromAccountId());
        Account toAccount = accountServiceClient.getAccount(request.getToAccountId());

        validateTransferRequest(request, fromAccount);

        // Fraud detection
        FraudAssessment fraudAssessment = fraudDetectionService.assessTransfer(request, fromAccount);
        if (fraudAssessment.isHighRisk()) {
            throw new SuspiciousTransactionException("Transaction blocked due to fraud risk");
        }

        // Create transaction record
        Transaction transaction = Transaction.builder()
            .transactionId(generateTransactionId())
            .fromAccountId(request.getFromAccountId())
            .toAccountId(request.getToAccountId())
            .amount(request.getAmount())
            .currency(request.getCurrency())
            .description(request.getDescription())
            .transactionType(TransactionType.TRANSFER)
            .status(TransactionStatus.PENDING)
            .createdAt(LocalDateTime.now())
            .build();

        transaction = transactionRepository.save(transaction);

        try {
            // Execute account updates
            accountServiceClient.debitAccount(request.getFromAccountId(), request.getAmount());
            accountServiceClient.creditAccount(request.getToAccountId(), request.getAmount());

            // Update transaction status
            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setCompletedAt(LocalDateTime.now());

            // Publish success event
            eventPublisher.publishTransferCompleted(transaction);

        } catch (Exception e) {
            // Compensating action
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setFailureReason(e.getMessage());

            eventPublisher.publishTransferFailed(transaction, e);
            throw new TransactionProcessingException("Transfer failed", e);
        } finally {
            transactionRepository.save(transaction);
        }

        return transaction;
    }

    private void validateTransferRequest(TransferRequest request, Account fromAccount) {
        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidAmountException("Transfer amount must be positive");
        }

        // Daily transfer limit check
        BigDecimal dailyTransferAmount = getDailyTransferAmount(request.getFromAccountId());
        BigDecimal dailyLimit = fromAccount.getDailyTransferLimit();

        if (dailyTransferAmount.add(request.getAmount()).compareTo(dailyLimit) > 0) {
            throw new DailyLimitExceededException("Daily transfer limit exceeded");
        }
    }
}

// 4. Saga Pattern for Distributed Transactions
@Component
public class TransferSaga {

    private final AccountServiceClient accountServiceClient;
    private final NotificationServiceClient notificationServiceClient;
    private final TransactionRepository transactionRepository;

    @SagaOrchestrationStart
    public void processTransfer(TransferRequest request) {
        String sagaId = UUID.randomUUID().toString();

        try {
            // Step 1: Reserve funds
            SagaStep reserveStep = SagaStep.builder()
                .sagaId(sagaId)
                .stepName("RESERVE_FUNDS")
                .compensationAction("RELEASE_RESERVATION")
                .build();

            accountServiceClient.reserveFunds(request.getFromAccountId(), request.getAmount(), sagaId);

            // Step 2: Validate target account
            SagaStep validateStep = SagaStep.builder()
                .sagaId(sagaId)
                .stepName("VALIDATE_TARGET_ACCOUNT")
                .build();

            accountServiceClient.validateAccount(request.getToAccountId());

            // Step 3: Execute transfer
            SagaStep transferStep = SagaStep.builder()
                .sagaId(sagaId)
                .stepName("EXECUTE_TRANSFER")
                .compensationAction("REVERSE_TRANSFER")
                .build();

            executeTransfer(request, sagaId);

            // Step 4: Send notification
            notificationServiceClient.sendTransferNotification(request, sagaId);

            // Complete saga
            completeSaga(sagaId);

        } catch (Exception e) {
            // Trigger compensation
            compensateSaga(sagaId, e);
        }
    }

    @SagaCompensation
    public void compensateSaga(String sagaId, Exception error) {
        log.error("Saga compensation triggered for sagaId: {}, error: {}", sagaId, error.getMessage());

        // Execute compensation actions in reverse order
        List<SagaStep> steps = getSagaSteps(sagaId);

        for (int i = steps.size() - 1; i >= 0; i--) {
            SagaStep step = steps.get(i);
            if (step.getCompensationAction() != null) {
                executeCompensation(step);
            }
        }
    }
}

// 5. Event-Driven Communication Between Services
@Component
public class TransactionEventHandler {

    private final AccountServiceClient accountServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    @EventListener
    public void handleCustomerActivated(CustomerActivatedEvent event) {
        // Create default savings account for new customer
        CreateAccountRequest accountRequest = CreateAccountRequest.builder()
            .customerId(event.getCustomerId())
            .accountType(AccountType.SAVINGS)
            .initialDeposit(BigDecimal.ZERO)
            .currency("USD")
            .build();

        accountServiceClient.createAccount(accountRequest);
    }

    @EventListener
    public void handleAccountCreated(AccountCreatedEvent event) {
        // Send welcome notification
        WelcomeNotificationRequest notificationRequest = WelcomeNotificationRequest.builder()
            .customerId(event.getCustomerId())
            .accountNumber(event.getAccountNumber())
            .accountType(event.getAccountType())
            .build();

        notificationServiceClient.sendWelcomeNotification(notificationRequest);
    }

    @RabbitListener(queues = "transaction.completed.queue")
    public void handleTransactionCompleted(TransactionCompletedEvent event) {
        // Update account balances, send notifications, etc.
        processTransactionCompletion(event);
    }
}

// 6. Service Contracts and API Versioning
@RestController
@RequestMapping("/api/v1/accounts")
@Api(tags = "Account Management API v1")
public class AccountV1Controller {
    // V1 implementation
}

@RestController
@RequestMapping("/api/v2/accounts")
@Api(tags = "Account Management API v2")
public class AccountV2Controller {
    // V2 implementation with enhanced features
}

// 7. Database Per Service Pattern
@Configuration
@EnableJpaRepositories(
    basePackages = "com.bank.account.repository",
    entityManagerFactoryRef = "accountEntityManagerFactory",
    transactionManagerRef = "accountTransactionManager"
)
public class AccountDatabaseConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.account")
    public DataSourceProperties accountDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource accountDataSource() {
        return accountDataSourceProperties()
            .initializeDataSourceBuilder()
            .build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean accountEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(accountDataSource());
        factory.setPackagesToScan("com.bank.account.model.entity");
        factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
        return factory;
    }

    @Bean
    @Primary
    public PlatformTransactionManager accountTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(accountEntityManagerFactory().getObject());
        return transactionManager;
    }
}
```

**Service Decomposition Guidelines:**

1. **Business Capability Alignment**: Each service should represent a distinct business capability
2. **Data Ownership**: Each service owns its data and database
3. **Team Ownership**: Services should align with team boundaries (Conway's Law)
4. **Technology Diversity**: Services can use different technologies as appropriate
5. **Independent Deployment**: Services should be deployable independently
6. **Failure Isolation**: Failure in one service shouldn't cascade to others

---


[⬆️ Back to Top](#table-of-contents)
## 🔍 Service Discovery

### Q4: Eureka Service Discovery

**Question**: How do you implement service discovery using Netflix Eureka in a Spring Boot microservices environment?

**Answer**:

**Eureka Server Configuration:**

```java
// Eureka Server Application
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```

**Eureka Server Properties:**

```yaml
# eureka-server application.yml
server:
  port: 8761

spring:
  application:
    name: eureka-server

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false
    fetch-registry: false
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
  server:
    enable-self-preservation: false
    eviction-interval-timer-in-ms: 60000

# Management endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always
```

**Eureka Client Implementation:**

```java
// Service Application with Eureka Client
@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }

    // Load-balanced RestTemplate
    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Load-balanced WebClient
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}

// Service Client with Load Balancing
@Component
public class CustomerServiceClient {

    private final RestTemplate restTemplate;
    private final WebClient webClient;

    public CustomerServiceClient(@LoadBalanced RestTemplate restTemplate,
                               @LoadBalanced WebClient.Builder webClientBuilder) {
        this.restTemplate = restTemplate;
        this.webClient = webClientBuilder.build();
    }

    // Using RestTemplate with service name
    public Customer getCustomerRestTemplate(String customerId) {
        String url = "http://customer-service/api/customers/" + customerId;
        return restTemplate.getForObject(url, Customer.class);
    }

    // Using WebClient with service name
    @CircuitBreaker(name = "customer-service", fallbackMethod = "getCustomerFallback")
    public Mono<Customer> getCustomerWebClient(String customerId) {
        return webClient.get()
            .uri("http://customer-service/api/customers/{id}", customerId)
            .retrieve()
            .bodyToMono(Customer.class)
            .timeout(Duration.ofSeconds(5));
    }

    public Mono<Customer> getCustomerFallback(String customerId, Exception ex) {
        log.warn("Fallback triggered for customer service: {}", ex.getMessage());
        return Mono.just(Customer.builder()
            .id(customerId)
            .status(CustomerStatus.UNKNOWN)
            .build());
    }
}

// Advanced Service Discovery Configuration
@Configuration
public class ServiceDiscoveryConfig {

    @Bean
    public DiscoveryClientHealthIndicator discoveryClientHealthIndicator(DiscoveryClient discoveryClient) {
        return new DiscoveryClientHealthIndicator(discoveryClient);
    }

    @Bean
    @ConditionalOnMissingBean
    public LoadBalancerClientFactory loadBalancerClientFactory() {
        LoadBalancerClientFactory factory = new LoadBalancerClientFactory();
        factory.setConfigurations(loadBalancerConfigurations());
        return factory;
    }

    @Bean
    public List<LoadBalancerClientSpecification> loadBalancerConfigurations() {
        return Arrays.asList(
            new LoadBalancerClientSpecification("customer-service",
                new Class[]{CustomerServiceLoadBalancerConfig.class}),
            new LoadBalancerClientSpecification("notification-service",
                new Class[]{NotificationServiceLoadBalancerConfig.class})
        );
    }
}

// Custom Load Balancer Configuration
@Configuration
public class CustomerServiceLoadBalancerConfig {

    @Bean
    public ReactorLoadBalancer<ServiceInstance> reactorServiceInstanceLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {

        String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);

        return new RoundRobinLoadBalancer(
            loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class),
            name);
    }
}

// Service Registration Customization
@Component
public class CustomInstanceInfoContributor implements HealthIndicator {

    @Autowired
    private EurekaRegistration eurekaRegistration;

    @Override
    public Health health() {
        Map<String, String> metadata = eurekaRegistration.getMetadata();

        return Health.up()
            .withDetail("serviceId", eurekaRegistration.getServiceId())
            .withDetail("instanceId", eurekaRegistration.getInstanceId())
            .withDetail("host", eurekaRegistration.getHost())
            .withDetail("port", eurekaRegistration.getPort())
            .withDetail("metadata", metadata)
            .build();
    }
}
```

**Client Properties:**

```yaml
# account-service application.yml
spring:
  application:
    name: account-service

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
    registry-fetch-interval-seconds: 30
    initial-instance-info-replication-interval-seconds: 40
  instance:
    prefer-ip-address: true
    lease-renewal-interval-in-seconds: 30
    lease-expiration-duration-in-seconds: 90
    instance-id: ${spring.application.name}:${server.port}
    metadata-map:
      version: 1.0.0
      environment: ${spring.profiles.active}
      region: us-east-1

# Load balancer configuration
spring:
  cloud:
    loadbalancer:
      health-check:
        initial-delay: 1s
        interval: 10s
      ribbon:
        enabled: false

# Service-specific configurations
customer-service:
  ribbon:
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.RoundRobinRule
    ConnectTimeout: 5000
    ReadTimeout: 10000
    MaxAutoRetries: 1
    MaxAutoRetriesNextServer: 1

notification-service:
  ribbon:
    NFLoadBalancerRuleClassName: com.netflix.loadbalancer.WeightedResponseTimeRule
```

**High Availability Eureka Setup:**

```yaml
# eureka-server-peer1.yml
spring:
  profiles: peer1
  application:
    name: eureka-server

server:
  port: 8761

eureka:
  instance:
    hostname: eureka-peer1
  client:
    service-url:
      defaultZone: http://eureka-peer2:8762/eureka/

---
# eureka-server-peer2.yml
spring:
  profiles: peer2
  application:
    name: eureka-server

server:
  port: 8762

eureka:
  instance:
    hostname: eureka-peer2
  client:
    service-url:
      defaultZone: http://eureka-peer1:8761/eureka/
```

**Service Discovery with Health Checks:**

```java
@Component
public class ServiceHealthMonitor {

    private final DiscoveryClient discoveryClient;
    private final RestTemplate restTemplate;

    public ServiceHealthMonitor(DiscoveryClient discoveryClient, RestTemplate restTemplate) {
        this.discoveryClient = discoveryClient;
        this.restTemplate = restTemplate;
    }

    @Scheduled(fixedRate = 30000)
    public void monitorServiceHealth() {
        List<String> services = discoveryClient.getServices();

        for (String serviceName : services) {
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);

            for (ServiceInstance instance : instances) {
                checkInstanceHealth(instance);
            }
        }
    }

    private void checkInstanceHealth(ServiceInstance instance) {
        try {
            String healthUrl = instance.getUri() + "/actuator/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(healthUrl, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.debug("Service {} is healthy", instance.getServiceId());
            } else {
                log.warn("Service {} health check failed with status: {}",
                    instance.getServiceId(), response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Health check failed for service {}: {}",
                instance.getServiceId(), e.getMessage());
        }
    }

    @EventListener
    public void handleInstanceRegistration(InstanceRegisteredEvent event) {
        log.info("New service instance registered: {}", event.getInstanceInfo());
    }

    @EventListener
    public void handleInstanceCancellation(InstanceCancelledEvent event) {
        log.info("Service instance cancelled: {}", event.getInstanceInfo());
    }
}
```

### Q5: Consul Integration

**Question**: How do you integrate HashiCorp Consul for service discovery and configuration management?

**Answer**:

**Consul Integration Configuration:**

```java
// Consul-enabled Application
@SpringBootApplication
@EnableDiscoveryClient
public class AccountServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

// Consul Configuration
@Configuration
@EnableConfigurationProperties(ConsulProperties.class)
public class ConsulConfig {

    @Bean
    public ConsulClient consulClient(ConsulProperties consulProperties) {
        return new ConsulClient(consulProperties.getHost(), consulProperties.getPort());
    }

    @Bean
    public ConsulServiceRegistry consulServiceRegistry(ConsulClient consulClient) {
        return new ConsulServiceRegistry(consulClient);
    }

    @Bean
    public ConsulRegistration consulRegistration(ConsulProperties consulProperties) {
        return ConsulRegistration.builder()
            .id(consulProperties.getInstanceId())
            .name(consulProperties.getServiceName())
            .address(consulProperties.getHost())
            .port(consulProperties.getPort())
            .check(ConsulRegistration.RegCheck.ttl(consulProperties.getHealthCheckInterval()))
            .tags(consulProperties.getTags())
            .build();
    }
}

// Consul Properties
@ConfigurationProperties(prefix = "spring.cloud.consul")
@Data
public class ConsulProperties {
    private String host = "localhost";
    private int port = 8500;
    private String serviceId;
    private String serviceName;
    private String instanceId;
    private Duration healthCheckInterval = Duration.ofSeconds(10);
    private List<String> tags = new ArrayList<>();
    private boolean enableTagOverride = false;
}

// Advanced Consul Service Discovery
@Component
public class ConsulServiceDiscovery {

    private final ConsulClient consulClient;
    private final LoadBalancerClient loadBalancerClient;

    public ConsulServiceDiscovery(ConsulClient consulClient, LoadBalancerClient loadBalancerClient) {
        this.consulClient = consulClient;
        this.loadBalancerClient = loadBalancerClient;
    }

    public List<ServiceInstance> getHealthyInstances(String serviceName) {
        Response<List<HealthService>> response = consulClient.getHealthServices(
            serviceName, true, QueryParams.DEFAULT);

        return response.getValue().stream()
            .map(this::convertToServiceInstance)
            .collect(Collectors.toList());
    }

    public ServiceInstance chooseInstance(String serviceName) {
        return loadBalancerClient.choose(serviceName);
    }

    public void registerService(String serviceName, String instanceId,
                              String host, int port, List<String> tags) {
        NewService newService = new NewService();
        newService.setId(instanceId);
        newService.setName(serviceName);
        newService.setAddress(host);
        newService.setPort(port);
        newService.setTags(tags);

        // Health check configuration
        NewService.Check check = new NewService.Check();
        check.setHttp(String.format("http://%s:%d/actuator/health", host, port));
        check.setInterval("10s");
        check.setTimeout("5s");
        newService.setCheck(check);

        consulClient.agentServiceRegister(newService);
    }

    public void deregisterService(String instanceId) {
        consulClient.agentServiceDeregister(instanceId);
    }

    private ServiceInstance convertToServiceInstance(HealthService healthService) {
        Service service = healthService.getService();
        return new DefaultServiceInstance(
            service.getId(),
            service.getService(),
            service.getAddress(),
            service.getPort(),
            false,
            convertTagsToMetadata(service.getTags())
        );
    }

    private Map<String, String> convertTagsToMetadata(List<String> tags) {
        return tags.stream()
            .filter(tag -> tag.contains("="))
            .map(tag -> tag.split("=", 2))
            .collect(Collectors.toMap(
                parts -> parts[0],
                parts -> parts[1],
                (existing, replacement) -> replacement
            ));
    }
}

// Consul Health Check Implementation
@Component
public class ConsulHealthIndicator implements HealthIndicator {

    private final ConsulClient consulClient;

    public ConsulHealthIndicator(ConsulClient consulClient) {
        this.consulClient = consulClient;
    }

    @Override
    public Health health() {
        try {
            Response<String> response = consulClient.getStatusLeader();

            if (response.getValue() != null && !response.getValue().isEmpty()) {
                return Health.up()
                    .withDetail("leader", response.getValue())
                    .withDetail("consul-client", "Available")
                    .build();
            } else {
                return Health.down()
                    .withDetail("error", "No Consul leader found")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("consul-client", "Unavailable")
                .build();
        }
    }
}
```

**Consul Properties Configuration:**

```yaml
# application.yml with Consul
spring:
  application:
    name: account-service
  cloud:
    consul:
      host: localhost
      port: 8500
      discovery:
        service-name: ${spring.application.name}
        hostname: ${server.address:localhost}
        port: ${server.port}
        prefer-ip-address: true
        instance-id: ${spring.application.name}:${server.port}
        health-check-path: /actuator/health
        health-check-interval: 15s
        health-check-timeout: 5s
        health-check-critical-timeout: 30s
        tags:
          - version=1.0.0
          - environment=${spring.profiles.active:dev}
          - region=us-east-1
        metadata:
          version: 1.0.0
          environment: ${spring.profiles.active:dev}
      config:
        enabled: true
        format: YAML
        data-key: config
        watch:
          enabled: true
          delay: 1000

# Load balancer configuration for Consul
spring:
  cloud:
    loadbalancer:
      consul:
        enabled: true
      cache:
        enabled: true
        ttl: 35s
        capacity: 256
```

### Q6: Client-Side vs Server-Side Discovery

**Question**: What are the differences between client-side and server-side service discovery, and when should you use each approach?

**Answer**:

**Comparison and Implementation:**

| Aspect | Client-Side Discovery | Server-Side Discovery |
|--------|----------------------|----------------------|
| **Discovery Logic** | Client queries registry | Load balancer queries registry |
| **Load Balancing** | Client-side logic | Server-side logic |
| **Failure Handling** | Client responsibility | Load balancer responsibility |
| **Network Hops** | Direct communication | Additional hop through LB |
| **Complexity** | Higher client complexity | Lower client complexity |
| **Performance** | Lower latency | Higher latency |
| **Examples** | Eureka, Consul | AWS ALB, Nginx, API Gateway |

**Client-Side Discovery Implementation:**

```java
// Client-Side Discovery with Eureka
@Component
public class ClientSideDiscoveryService {

    private final DiscoveryClient discoveryClient;
    private final LoadBalancerClient loadBalancerClient;
    private final RestTemplate restTemplate;

    public ClientSideDiscoveryService(DiscoveryClient discoveryClient,
                                    LoadBalancerClient loadBalancerClient,
                                    RestTemplate restTemplate) {
        this.discoveryClient = discoveryClient;
        this.loadBalancerClient = loadBalancerClient;
        this.restTemplate = restTemplate;
    }

    // Manual service discovery and load balancing
    public Customer getCustomerManual(String customerId) {
        List<ServiceInstance> instances = discoveryClient.getInstances("customer-service");

        if (instances.isEmpty()) {
            throw new ServiceUnavailableException("No customer-service instances available");
        }

        // Simple round-robin selection
        ServiceInstance instance = selectInstance(instances);

        String url = String.format("http://%s:%d/api/customers/%s",
            instance.getHost(), instance.getPort(), customerId);

        return restTemplate.getForObject(url, Customer.class);
    }

    // Using LoadBalancerClient
    public Customer getCustomerWithLoadBalancer(String customerId) {
        ServiceInstance instance = loadBalancerClient.choose("customer-service");

        if (instance == null) {
            throw new ServiceUnavailableException("No customer-service instances available");
        }

        String url = String.format("http://%s:%d/api/customers/%s",
            instance.getHost(), instance.getPort(), customerId);

        return restTemplate.getForObject(url, Customer.class);
    }

    // Using @LoadBalanced RestTemplate (recommended)
    @LoadBalanced
    @Bean
    public RestTemplate loadBalancedRestTemplate() {
        return new RestTemplate();
    }

    @Autowired
    @LoadBalanced
    private RestTemplate loadBalancedRestTemplate;

    public Customer getCustomerLoadBalanced(String customerId) {
        String url = "http://customer-service/api/customers/" + customerId;
        return loadBalancedRestTemplate.getForObject(url, Customer.class);
    }

    private ServiceInstance selectInstance(List<ServiceInstance> instances) {
        // Simple round-robin implementation
        int index = (int) (System.currentTimeMillis() % instances.size());
        return instances.get(index);
    }
}

// Advanced Client-Side Load Balancing
@Configuration
public class CustomLoadBalancerConfig {

    @Bean
    public IRule ribbonRule() {
        return new WeightedResponseTimeRule();
    }

    @Bean
    public IPing ribbonPing() {
        return new PingUrl();
    }

    @Bean
    public ServerListSubsetFilter serverListFilter() {
        ServerListSubsetFilter filter = new ServerListSubsetFilter();
        filter.setSize(3); // Limit to 3 servers for load balancing
        return filter;
    }
}

// Circuit Breaker with Client-Side Discovery
@Component
public class ResilientCustomerServiceClient {

    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    @CircuitBreaker(name = "customer-service", fallbackMethod = "getCustomerFallback")
    @Retry(name = "customer-service")
    @TimeLimiter(name = "customer-service")
    public CompletableFuture<Customer> getCustomerAsync(String customerId) {
        return CompletableFuture.supplyAsync(() -> {
            String url = "http://customer-service/api/customers/" + customerId;
            return restTemplate.getForObject(url, Customer.class);
        });
    }

    public CompletableFuture<Customer> getCustomerFallback(String customerId, Exception ex) {
        log.warn("Fallback triggered for customer {}: {}", customerId, ex.getMessage());
        return CompletableFuture.completedFuture(
            Customer.builder()
                .id(customerId)
                .status(CustomerStatus.UNKNOWN)
                .build()
        );
    }
}
```

**Server-Side Discovery with API Gateway:**

```java
// Gateway Configuration
@Configuration
@EnableWebFluxSecurity
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("customer-service", r -> r
                .path("/api/customers/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Request-Source", "gateway")
                    .circuitBreaker(config -> config
                        .setName("customer-service-cb")
                        .setFallbackUri("forward:/fallback/customer"))
                    .retry(config -> config
                        .setRetries(3)
                        .setMethods(HttpMethod.GET)
                        .setBackoff(Duration.ofMillis(100), Duration.ofMillis(1000), 2, false)))
                .uri("lb://customer-service"))

            .route("account-service", r -> r
                .path("/api/accounts/**")
                .filters(f -> f
                    .stripPrefix(1)
                    .addRequestHeader("X-Request-Source", "gateway"))
                .uri("lb://account-service"))

            .build();
    }

    @Bean
    public GlobalFilter customGlobalFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            log.info("Processing request: {} {}", request.getMethod(), request.getURI());

            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                ServerHttpResponse response = exchange.getResponse();
                log.info("Response status: {}", response.getStatusCode());
            }));
        };
    }
}

// Load Balancer Configuration for Gateway
@Configuration
public class LoadBalancerConfiguration {

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    // Custom load balancer for specific services
    @Bean
    public ReactorLoadBalancer<ServiceInstance> customerServiceLoadBalancer(
            Environment environment,
            LoadBalancerClientFactory loadBalancerClientFactory) {

        String name = "customer-service";
        return new RoundRobinLoadBalancer(
            loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class),
            name);
    }

    // Health-check based load balancing
    @Bean
    public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier(
            ConfigurableApplicationContext context) {
        return ServiceInstanceListSupplier.builder()
            .withDiscoveryClient()
            .withHealthChecks()
            .withCaching(Duration.ofSeconds(30))
            .build(context);
    }
}

// Fallback Controller for Server-Side Discovery
@RestController
public class FallbackController {

    @GetMapping("/fallback/customer")
    public ResponseEntity<Map<String, String>> customerFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Customer service is currently unavailable");
        response.put("status", "fallback");
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @GetMapping("/fallback/account")
    public ResponseEntity<Map<String, String>> accountFallback() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Account service is currently unavailable");
        response.put("status", "fallback");
        response.put("timestamp", LocalDateTime.now().toString());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
```

**Decision Guidelines:**

**Use Client-Side Discovery When:**
- You need fine-grained control over load balancing
- Low latency is critical
- You want to avoid additional network hops
- Services are within a trusted network
- You can handle the complexity in clients

**Use Server-Side Discovery When:**
- You want to centralize routing logic
- Clients should be simple and language-agnostic
- You need advanced gateway features (rate limiting, authentication)
- Services are deployed across different networks
- You want to abstract service topology from clients

**Hybrid Approach:**
Many organizations use both patterns:
- API Gateway for external traffic (server-side)
- Direct service communication with client-side discovery for internal traffic

---

*[Continue with remaining sections covering API Gateway Patterns, Circuit Breakers, Configuration Management, and Banking Use Cases...]*

The guide continues with comprehensive coverage of Spring Cloud Gateway implementation, Resilience4j patterns, distributed configuration management, and real-world banking microservices scenarios.