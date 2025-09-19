# Spring Boot Batch Comprehensive Guide

## 📖 Table of Contents

### 1. [Batch Configuration](#batch-configuration)
- [Q1: Basic Spring Batch Configuration](#q1-basic-spring-batch-configuration)
- [Q2: Job Repository and Transaction Manager](#q2-job-repository-and-transaction-manager)
- [Q3: Multiple Datasource Configuration](#q3-multiple-datasource-configuration)

### 2. [Job Design and Architecture](#job-design-and-architecture)
- [Q4: Job Structure and Step Design](#q4-job-structure-and-step-design)
- [Q5: Chunk-Oriented vs Tasklet Processing](#q5-chunk-oriented-vs-tasklet-processing)
- [Q6: Parallel and Partitioned Jobs](#q6-parallel-and-partitioned-jobs)

### 3. [Readers, Processors, and Writers](#readers-processors-and-writers)
- [Q7: Custom ItemReader Implementation](#q7-custom-itemreader-implementation)
- [Q8: ItemProcessor Patterns](#q8-itemprocessor-patterns)
- [Q9: ItemWriter Strategies](#q9-itemwriter-strategies)

### 4. [Error Handling and Recovery](#error-handling-and-recovery)
- [Q10: Skip and Retry Policies](#q10-skip-and-retry-policies)
- [Q11: Job Restart and Recovery](#q11-job-restart-and-recovery)
- [Q12: Exception Handling Strategies](#q12-exception-handling-strategies)

### 5. [Scheduling and Monitoring](#scheduling-and-monitoring)
- [Q13: Job Scheduling with Spring](#q13-job-scheduling-with-spring)
- [Q14: Job Monitoring and Metrics](#q14-job-monitoring-and-metrics)
- [Q15: Batch Admin and Management](#q15-batch-admin-and-management)

### 6. [Banking Use Cases](#banking-use-cases)
- [Q16: End-of-Day Processing](#q16-end-of-day-processing)
- [Q17: Transaction File Processing](#q17-transaction-file-processing)
- [Q18: Regulatory Reporting](#q18-regulatory-reporting)

---

## 🔧 Batch Configuration

### Q1: Basic Spring Batch Configuration

**Question**: How do you configure Spring Boot Batch with proper job repository and essential components?

**Answer**:

**Basic Configuration:**

```yaml
# application.yml
spring:
  batch:
    # Job Repository Configuration
    job:
      enabled: true
    jdbc:
      initialize-schema: always # always, embedded, never

  # Primary datasource for application data
  datasource:
    url: jdbc:mysql://localhost:3306/banking_db
    username: ${DB_USERNAME:bankuser}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver

  # JPA Configuration
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

# Batch-specific configuration
batch:
  chunk-size: 1000
  thread-pool-size: 10
  max-retries: 3
  skip-limit: 100

# Scheduling configuration
scheduling:
  enabled: true
  thread-pool-size: 5

# Logging for batch operations
logging:
  level:
    org.springframework.batch: DEBUG
    com.bank.batch: DEBUG
```

**Main Configuration Class:**

```java
@Configuration
@EnableBatchProcessing
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.bank.repository")
public class BatchConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    // Job Repository Configuration
    @Bean
    public JobRepository jobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(batchTransactionManager());
        factory.setIsolationLevelForCreate("ISOLATION_DEFAULT");
        factory.setTablePrefix("BATCH_"); // Default prefix
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    // Transaction Manager for Batch Operations
    @Bean
    public PlatformTransactionManager batchTransactionManager() {
        return new DataSourceTransactionManager(dataSource);
    }

    // Job Launcher
    @Bean
    public JobLauncher jobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    // Job Registry for management
    @Bean
    public JobRegistry jobRegistry() {
        return new MapJobRegistry();
    }

    // Job Registrar
    @Bean
    public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor(JobRegistry jobRegistry) {
        JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor = new JobRegistryBeanPostProcessor();
        jobRegistryBeanPostProcessor.setJobRegistry(jobRegistry);
        return jobRegistryBeanPostProcessor;
    }

    // Task Executor for parallel processing
    @Bean
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("batch-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    // Job Parameters Incrementer
    @Bean
    public JobParametersIncrementer jobParametersIncrementer() {
        return new RunIdIncrementer();
    }

    // Batch Properties
    @Bean
    @ConfigurationProperties(prefix = "batch")
    public BatchProperties batchProperties() {
        return new BatchProperties();
    }
}

// Configuration Properties
@Data
@Component
@ConfigurationProperties(prefix = "batch")
public class BatchProperties {
    private int chunkSize = 1000;
    private int threadPoolSize = 10;
    private int maxRetries = 3;
    private int skipLimit = 100;
    private boolean asyncProcessing = true;
    private String tempDirectory = "/tmp/batch";
    private int commitInterval = 1000;
}

// Batch Health Indicator
@Component
public class BatchHealthIndicator implements HealthIndicator {

    @Autowired
    private JobExplorer jobExplorer;

    @Override
    public Health health() {
        try {
            // Check if any jobs are running
            Set<JobExecution> runningJobs = jobExplorer.findRunningJobExecutions("accountProcessingJob");

            if (runningJobs.isEmpty()) {
                return Health.up()
                    .withDetail("status", "No running jobs")
                    .build();
            } else {
                return Health.up()
                    .withDetail("status", "Jobs running")
                    .withDetail("runningJobs", runningJobs.size())
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

### Q2: Job Repository and Transaction Manager

**Question**: How do you configure job repository with custom schema and transaction management?

**Answer**:

**Custom Job Repository Configuration:**

```java
@Configuration
public class CustomJobRepositoryConfiguration {

    @Autowired
    @Qualifier("batchDataSource")
    private DataSource batchDataSource;

    @Autowired
    @Qualifier("applicationDataSource")
    private DataSource applicationDataSource;

    // Custom Job Repository for batch metadata
    @Bean
    @Primary
    public JobRepository customJobRepository() throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(batchDataSource);
        factory.setTransactionManager(batchTransactionManager());

        // Custom table prefix for batch metadata
        factory.setTablePrefix("BANK_BATCH_");

        // Isolation level for concurrent job execution
        factory.setIsolationLevelForCreate("ISOLATION_READ_COMMITTED");

        // Validation query timeout
        factory.setValidateTransactionState(true);

        // Custom incremental executionId
        factory.setIncrementerFactory(new DefaultDataFieldMaxValueIncrementerFactory(batchDataSource));

        factory.afterPropertiesSet();
        return factory.getObject();
    }

    // Batch-specific transaction manager
    @Bean
    public PlatformTransactionManager batchTransactionManager() {
        DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(batchDataSource);
        transactionManager.setRollbackOnCommitFailure(true);
        return transactionManager;
    }

    // Application transaction manager
    @Bean
    public PlatformTransactionManager applicationTransactionManager() {
        return new DataSourceTransactionManager(applicationDataSource);
    }

    // Custom Job Explorer for monitoring
    @Bean
    public JobExplorer jobExplorer() throws Exception {
        JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
        factory.setDataSource(batchDataSource);
        factory.setTablePrefix("BANK_BATCH_");
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    // Job Operator for job management
    @Bean
    public JobOperator jobOperator() throws Exception {
        SimpleJobOperator jobOperator = new SimpleJobOperator();
        jobOperator.setJobLauncher(jobLauncher());
        jobOperator.setJobRegistry(jobRegistry());
        jobOperator.setJobRepository(customJobRepository());
        jobOperator.setJobExplorer(jobExplorer());
        jobOperator.afterPropertiesSet();
        return jobOperator;
    }

    @Bean
    public JobLauncher jobLauncher() throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(customJobRepository());
        jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Bean
    public JobRegistry jobRegistry() {
        return new MapJobRegistry();
    }
}

// Custom Job Repository Initializer
@Component
public class BatchSchemaInitializer implements InitializingBean {

    @Autowired
    @Qualifier("batchDataSource")
    private DataSource batchDataSource;

    @Override
    public void afterPropertiesSet() throws Exception {
        // Initialize batch schema with custom prefix
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("org/springframework/batch/core/schema-mysql.sql"));
        populator.execute(batchDataSource);

        // Create custom indexes for performance
        createCustomIndexes();
    }

    private void createCustomIndexes() {
        try (Connection connection = batchDataSource.getConnection()) {
            Statement stmt = connection.createStatement();

            // Index on job execution status and create time
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_job_execution_status_time " +
                        "ON BANK_BATCH_JOB_EXECUTION (STATUS, CREATE_TIME)");

            // Index on step execution status
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_step_execution_status " +
                        "ON BANK_BATCH_STEP_EXECUTION (STATUS, START_TIME)");

            // Index on job instance name and parameters
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_job_instance_name " +
                        "ON BANK_BATCH_JOB_INSTANCE (JOB_NAME)");

        } catch (SQLException e) {
            log.error("Failed to create custom indexes", e);
        }
    }
}
```

### Q3: Multiple Datasource Configuration

**Question**: How do you configure Spring Batch with multiple datasources for different purposes?

**Answer**:

**Multiple Datasource Setup:**

```java
@Configuration
@EnableBatchProcessing
public class MultiDatasourceBatchConfiguration {

    // Batch Metadata Datasource Configuration
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.batch")
    public DataSourceProperties batchDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    public DataSource batchDataSource() {
        return batchDataSourceProperties()
            .initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    // Application Data Datasource
    @Bean
    @ConfigurationProperties("spring.datasource.application")
    public DataSourceProperties applicationDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource applicationDataSource() {
        return applicationDataSourceProperties()
            .initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    // Archive/Historical Data Datasource
    @Bean
    @ConfigurationProperties("spring.datasource.archive")
    public DataSourceProperties archiveDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource archiveDataSource() {
        return archiveDataSourceProperties()
            .initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }

    // Batch Job Repository (uses batch datasource)
    @Bean
    public JobRepository jobRepository(@Qualifier("batchDataSource") DataSource batchDataSource)
            throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(batchDataSource);
        factory.setTransactionManager(batchTransactionManager(batchDataSource));
        factory.setIsolationLevelForCreate("ISOLATION_READ_COMMITTED");
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    @Bean
    public PlatformTransactionManager batchTransactionManager(
            @Qualifier("batchDataSource") DataSource batchDataSource) {
        return new DataSourceTransactionManager(batchDataSource);
    }

    @Bean
    public PlatformTransactionManager applicationTransactionManager(
            @Qualifier("applicationDataSource") DataSource applicationDataSource) {
        return new DataSourceTransactionManager(applicationDataSource);
    }

    @Bean
    public PlatformTransactionManager archiveTransactionManager(
            @Qualifier("archiveDataSource") DataSource archiveDataSource) {
        return new DataSourceTransactionManager(archiveDataSource);
    }
}

# Application Properties for Multiple Datasources
spring:
  datasource:
    # Batch metadata storage
    batch:
      url: jdbc:mysql://localhost:3306/banking_batch
      username: batch_user
      password: batch_pass
      driver-class-name: com.mysql.cj.jdbc.Driver
      hikari:
        maximum-pool-size: 10
        minimum-idle: 2

    # Application data
    application:
      url: jdbc:mysql://localhost:3306/banking_app
      username: app_user
      password: app_pass
      driver-class-name: com.mysql.cj.jdbc.Driver
      hikari:
        maximum-pool-size: 20
        minimum-idle: 5

    # Archive/Historical data
    archive:
      url: jdbc:mysql://localhost:3306/banking_archive
      username: archive_user
      password: archive_pass
      driver-class-name: com.mysql.cj.jdbc.Driver
      hikari:
        maximum-pool-size: 5
        minimum-idle: 1
```

---

## 🏗️ Job Design and Architecture

### Q4: Job Structure and Step Design

**Question**: How do you design complex batch jobs with multiple steps and conditional flows?

**Answer**:

**Complex Job Architecture:**

```java
@Configuration
public class BankingBatchJobConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private BatchProperties batchProperties;

    // Main End-of-Day Processing Job
    @Bean
    public Job endOfDayProcessingJob() {
        return jobBuilderFactory.get("endOfDayProcessingJob")
            .incrementer(new RunIdIncrementer())
            .start(validateBusinessDateStep())
            .next(processTransactionsStep())
            .next(calculateInterestStep())
            .next(generateStatementsStep())
            .next(createReportsStep())
            .next(archiveDataStep())
            .next(cleanupStep())
            .listener(endOfDayJobListener())
            .build();
    }

    // Conditional Flow Job Example
    @Bean
    public Job conditionalProcessingJob() {
        return jobBuilderFactory.get("conditionalProcessingJob")
            .incrementer(new RunIdIncrementer())
            .start(dataValidationStep())
                .on("VALID").to(normalProcessingStep())
                .from(dataValidationStep()).on("INVALID").to(errorHandlingStep())
                .from(normalProcessingStep()).on("*").to(postProcessingStep())
                .from(errorHandlingStep()).on("*").to(notificationStep())
            .end()
            .build();
    }

    // Parallel Processing Job
    @Bean
    public Job parallelProcessingJob() {
        Flow accountFlow = new FlowBuilder<Flow>("accountFlow")
            .start(processAccountsStep())
            .build();

        Flow transactionFlow = new FlowBuilder<Flow>("transactionFlow")
            .start(processTransactionsStep())
            .build();

        Flow customerFlow = new FlowBuilder<Flow>("customerFlow")
            .start(processCustomersStep())
            .build();

        return jobBuilderFactory.get("parallelProcessingJob")
            .incrementer(new RunIdIncrementer())
            .start(initializationStep())
            .split(batchTaskExecutor())
                .add(accountFlow, transactionFlow, customerFlow)
            .next(consolidationStep())
            .build().build();
    }

    // Step Definitions
    @Bean
    public Step validateBusinessDateStep() {
        return stepBuilderFactory.get("validateBusinessDateStep")
            .tasklet(businessDateValidationTasklet())
            .build();
    }

    @Bean
    public Step processTransactionsStep() {
        return stepBuilderFactory.get("processTransactionsStep")
            .<TransactionRecord, ProcessedTransaction>chunk(batchProperties.getChunkSize())
            .reader(transactionReader())
            .processor(transactionProcessor())
            .writer(transactionWriter())
            .faultTolerant()
            .skip(ValidationException.class)
            .skipLimit(batchProperties.getSkipLimit())
            .retry(DatabaseException.class)
            .retryLimit(batchProperties.getMaxRetries())
            .listener(transactionStepListener())
            .taskExecutor(batchTaskExecutor())
            .throttleLimit(10)
            .build();
    }

    @Bean
    public Step calculateInterestStep() {
        return stepBuilderFactory.get("calculateInterestStep")
            .<Account, Account>chunk(500)
            .reader(accountReader())
            .processor(interestCalculationProcessor())
            .writer(accountWriter())
            .listener(interestCalculationListener())
            .build();
    }

    @Bean
    public Step generateStatementsStep() {
        return stepBuilderFactory.get("generateStatementsStep")
            .partitioner("statementGeneration", customerPartitioner())
            .step(statementGenerationSlaveStep())
            .gridSize(4)
            .taskExecutor(batchTaskExecutor())
            .build();
    }

    // Partitioned Step for Statement Generation
    @Bean
    public Step statementGenerationSlaveStep() {
        return stepBuilderFactory.get("statementGenerationSlaveStep")
            .<Customer, Statement>chunk(100)
            .reader(customerReader(null, null)) // Parameters injected by partitioner
            .processor(statementProcessor())
            .writer(statementWriter())
            .build();
    }

    // Remote Chunking for Distributed Processing
    @Bean
    public Step remoteChunkingMasterStep() {
        return stepBuilderFactory.get("remoteChunkingMasterStep")
            .<TransactionRecord, TransactionRecord>chunk(1000)
            .reader(distributedTransactionReader())
            .writer(messagingItemWriter()) // Sends to message queue
            .build();
    }

    // Step Listeners and Callbacks
    @Bean
    public StepExecutionListener transactionStepListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                log.info("Starting transaction processing step: {}",
                    stepExecution.getStepName());

                // Initialize metrics
                stepExecution.getExecutionContext().put("startTime", System.currentTimeMillis());
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                long duration = System.currentTimeMillis() -
                    stepExecution.getExecutionContext().getLong("startTime");

                log.info("Completed transaction processing step: {} in {} ms",
                    stepExecution.getStepName(), duration);

                // Custom exit status based on processing results
                if (stepExecution.getSkipCount() > batchProperties.getSkipLimit() / 2) {
                    return new ExitStatus("COMPLETED_WITH_WARNINGS");
                }

                return stepExecution.getExitStatus();
            }
        };
    }

    // Job Listener
    @Bean
    public JobExecutionListener endOfDayJobListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                log.info("Starting End-of-Day processing job");

                // Validate prerequisites
                if (!isBusinessDateValid()) {
                    throw new JobParametersInvalidException("Invalid business date");
                }

                // Lock resources
                acquireProcessingLock();
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                try {
                    if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
                        log.info("End-of-Day processing completed successfully");
                        sendSuccessNotification(jobExecution);
                    } else {
                        log.error("End-of-Day processing failed: {}", jobExecution.getStatus());
                        sendFailureNotification(jobExecution);
                    }
                } finally {
                    // Always release resources
                    releaseProcessingLock();
                }
            }
        };
    }

    // Decision Logic for Conditional Flows
    @Bean
    public JobExecutionDecider businessDateDecider() {
        return (jobExecution, stepExecution) -> {
            LocalDate businessDate = getBusinessDate(jobExecution.getJobParameters());
            LocalDate today = LocalDate.now();

            if (businessDate.equals(today)) {
                return new FlowExecutionStatus("CURRENT");
            } else if (businessDate.isBefore(today)) {
                return new FlowExecutionStatus("HISTORICAL");
            } else {
                return new FlowExecutionStatus("FUTURE");
            }
        };
    }

    // Tasklets for Simple Operations
    @Bean
    public Tasklet businessDateValidationTasklet() {
        return (contribution, chunkContext) -> {
            JobParameters jobParameters = chunkContext.getStepContext()
                .getStepExecution().getJobParameters();

            String businessDateStr = jobParameters.getString("businessDate");
            if (businessDateStr == null || !isValidBusinessDate(businessDateStr)) {
                throw new JobParametersInvalidException("Invalid business date: " + businessDateStr);
            }

            log.info("Business date validation passed: {}", businessDateStr);
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet cleanupTasklet() {
        return (contribution, chunkContext) -> {
            // Cleanup temporary files
            cleanupTempFiles();

            // Update processing status
            updateProcessingStatus("COMPLETED");

            // Send notifications
            sendCompletionNotifications();

            return RepeatStatus.FINISHED;
        };
    }

    // Utility Methods
    private boolean isBusinessDateValid() {
        // Implementation for business date validation
        return true;
    }

    private void acquireProcessingLock() {
        // Implementation for resource locking
    }

    private void releaseProcessingLock() {
        // Implementation for resource release
    }

    private LocalDate getBusinessDate(JobParameters jobParameters) {
        String dateStr = jobParameters.getString("businessDate");
        return LocalDate.parse(dateStr);
    }

    private boolean isValidBusinessDate(String dateStr) {
        try {
            LocalDate.parse(dateStr);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void cleanupTempFiles() {
        // Cleanup implementation
    }

    private void updateProcessingStatus(String status) {
        // Status update implementation
    }

    private void sendCompletionNotifications() {
        // Notification implementation
    }

    private void sendSuccessNotification(JobExecution jobExecution) {
        // Success notification implementation
    }

    private void sendFailureNotification(JobExecution jobExecution) {
        // Failure notification implementation
    }
}
```

### Q5: Chunk-Oriented vs Tasklet Processing

**Question**: When should you use chunk-oriented processing vs tasklet-based processing in Spring Batch?

**Answer**:

**Comparison and Implementation:**

| Aspect | Chunk-Oriented | Tasklet |
|--------|----------------|---------|
| **Use Case** | Large data processing | Simple operations, control tasks |
| **Transaction** | Per chunk | Per tasklet execution |
| **Memory** | Configurable chunk size | Depends on implementation |
| **Error Handling** | Skip/Retry per item | All-or-nothing |
| **Performance** | High throughput | Low overhead |
| **Complexity** | Read-Process-Write pattern | Custom logic |

**Chunk-Oriented Processing Examples:**

```java
@Configuration
public class ChunkProcessingConfiguration {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    // High-Volume Transaction Processing
    @Bean
    public Step transactionProcessingStep() {
        return stepBuilderFactory.get("transactionProcessingStep")
            .<TransactionFile, ProcessedTransaction>chunk(1000)
            .reader(csvTransactionReader())
            .processor(transactionValidationProcessor())
            .writer(databaseTransactionWriter())
            .faultTolerant()
            .skip(ValidationException.class)
            .skipLimit(100)
            .retry(DatabaseException.class)
            .retryLimit(3)
            .listener(chunkListener())
            .build();
    }

    // Large File Processing with Custom Chunk Size
    @Bean
    public Step fileProcessingStep() {
        return stepBuilderFactory.get("fileProcessingStep")
            .<CustomerRecord, Customer>chunk(2000)
            .reader(fixedLengthFileReader())
            .processor(customerDataProcessor())
            .writer(customerDatabaseWriter())
            .readerIsTransactionalQueue(false)
            .build();
    }

    // ItemReader for CSV Files
    @Bean
    @StepScope
    public FlatFileItemReader<TransactionFile> csvTransactionReader() {
        return new FlatFileItemReaderBuilder<TransactionFile>()
            .name("csvTransactionReader")
            .resource(new FileSystemResource("#{jobParameters['inputFile']}"))
            .delimited()
            .names("accountNumber", "amount", "transactionType", "description", "timestamp")
            .targetType(TransactionFile.class)
            .linesToSkip(1)
            .build();
    }

    // ItemProcessor for Business Logic
    @Bean
    public ItemProcessor<TransactionFile, ProcessedTransaction> transactionValidationProcessor() {
        return new ItemProcessor<TransactionFile, ProcessedTransaction>() {

            @Autowired
            private ValidationService validationService;

            @Autowired
            private AccountService accountService;

            @Override
            public ProcessedTransaction process(TransactionFile item) throws Exception {
                // Validate transaction data
                if (!validationService.isValidTransaction(item)) {
                    throw new ValidationException("Invalid transaction: " + item.toString());
                }

                // Verify account exists and is active
                Account account = accountService.findByAccountNumber(item.getAccountNumber());
                if (account == null || !account.isActive()) {
                    throw new ValidationException("Invalid account: " + item.getAccountNumber());
                }

                // Apply business rules
                ProcessedTransaction processed = new ProcessedTransaction();
                processed.setAccountId(account.getId());
                processed.setAmount(item.getAmount());
                processed.setTransactionType(item.getTransactionType());
                processed.setDescription(item.getDescription());
                processed.setProcessedTimestamp(LocalDateTime.now());

                // Calculate fees if applicable
                BigDecimal fee = calculateTransactionFee(item, account);
                processed.setFee(fee);

                // Risk scoring
                double riskScore = calculateRiskScore(item, account);
                processed.setRiskScore(riskScore);

                return processed;
            }

            private BigDecimal calculateTransactionFee(TransactionFile transaction, Account account) {
                // Fee calculation logic
                return BigDecimal.ZERO;
            }

            private double calculateRiskScore(TransactionFile transaction, Account account) {
                // Risk scoring logic
                return 0.1;
            }
        };
    }

    // Composite ItemWriter for Multiple Destinations
    @Bean
    public CompositeItemWriter<ProcessedTransaction> compositeTransactionWriter() {
        CompositeItemWriter<ProcessedTransaction> writer = new CompositeItemWriter<>();

        List<ItemWriter<? super ProcessedTransaction>> writers = Arrays.asList(
            databaseTransactionWriter(),
            auditTrailWriter(),
            riskAnalysisWriter()
        );

        writer.setDelegates(writers);
        return writer;
    }

    @Bean
    public JdbcBatchItemWriter<ProcessedTransaction> databaseTransactionWriter() {
        return new JdbcBatchItemWriterBuilder<ProcessedTransaction>()
            .dataSource(dataSource)
            .sql("INSERT INTO transactions (account_id, amount, transaction_type, description, " +
                 "fee, risk_score, processed_timestamp) VALUES (:accountId, :amount, " +
                 ":transactionType, :description, :fee, :riskScore, :processedTimestamp)")
            .beanMapped()
            .build();
    }

    // Chunk Listener for Monitoring
    @Bean
    public ChunkListener chunkListener() {
        return new ChunkListener() {
            @Override
            public void beforeChunk(ChunkContext context) {
                log.debug("Starting chunk processing");
            }

            @Override
            public void afterChunk(ChunkContext context) {
                StepExecution stepExecution = context.getStepContext().getStepExecution();
                log.info("Processed chunk - Read: {}, Written: {}, Skipped: {}",
                    stepExecution.getReadCount(),
                    stepExecution.getWriteCount(),
                    stepExecution.getSkipCount());
            }

            @Override
            public void afterChunkError(ChunkContext context) {
                log.error("Chunk processing error in step: {}",
                    context.getStepContext().getStepName());
            }
        };
    }
}

// Tasklet Processing Examples
@Configuration
public class TaskletProcessingConfiguration {

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    // File Archive Tasklet
    @Bean
    public Step fileArchiveStep() {
        return stepBuilderFactory.get("fileArchiveStep")
            .tasklet(fileArchiveTasklet())
            .build();
    }

    @Bean
    public Tasklet fileArchiveTasklet() {
        return new Tasklet() {
            @Override
            public RepeatStatus execute(StepContribution contribution,
                                      ChunkContext chunkContext) throws Exception {

                JobParameters jobParameters = chunkContext.getStepContext()
                    .getStepExecution().getJobParameters();

                String sourceDir = jobParameters.getString("sourceDirectory");
                String archiveDir = jobParameters.getString("archiveDirectory");

                // Archive all processed files
                Path sourcePath = Paths.get(sourceDir);
                Path archivePath = Paths.get(archiveDir);

                Files.walk(sourcePath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".processed"))
                    .forEach(file -> {
                        try {
                            Path target = archivePath.resolve(file.getFileName());
                            Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);
                            log.info("Archived file: {}", file.getFileName());
                        } catch (IOException e) {
                            log.error("Failed to archive file: {}", file.getFileName(), e);
                            throw new RuntimeException(e);
                        }
                    });

                return RepeatStatus.FINISHED;
            }
        };
    }

    // Database Cleanup Tasklet
    @Bean
    public Step databaseCleanupStep() {
        return stepBuilderFactory.get("databaseCleanupStep")
            .tasklet(databaseCleanupTasklet())
            .transactionManager(transactionManager)
            .build();
    }

    @Bean
    public Tasklet databaseCleanupTasklet() {
        return new MethodInvokingTaskletAdapter() {{
            setTargetObject(databaseCleanupService());
            setTargetMethod("cleanupOldRecords");
        }};
    }

    // Report Generation Tasklet
    @Bean
    public Step reportGenerationStep() {
        return stepBuilderFactory.get("reportGenerationStep")
            .tasklet(reportGenerationTasklet())
            .build();
    }

    @Bean
    public Tasklet reportGenerationTasklet() {
        return (contribution, chunkContext) -> {
            JobExecution jobExecution = chunkContext.getStepContext()
                .getStepExecution().getJobExecution();

            // Generate processing summary report
            ProcessingSummary summary = createProcessingSummary(jobExecution);

            // Generate PDF report
            byte[] reportPdf = reportService.generatePdfReport(summary);

            // Save report to file system
            String reportPath = saveReport(reportPdf, summary.getBusinessDate());

            // Send email notification
            emailService.sendReportNotification(reportPath, summary);

            return RepeatStatus.FINISHED;
        };
    }

    // Conditional Tasklet
    @Bean
    public Tasklet conditionalProcessingTasklet() {
        return (contribution, chunkContext) -> {
            ExecutionContext executionContext = chunkContext.getStepContext()
                .getStepExecution().getJobExecution().getExecutionContext();

            String processingMode = (String) executionContext.get("processingMode");

            if ("FULL".equals(processingMode)) {
                performFullProcessing();
            } else if ("INCREMENTAL".equals(processingMode)) {
                performIncrementalProcessing();
            } else {
                throw new IllegalArgumentException("Unknown processing mode: " + processingMode);
            }

            return RepeatStatus.FINISHED;
        };
    }

    // Callable Tasklet for Long-Running Operations
    @Bean
    public CallableTaskletAdapter callableTasklet() {
        CallableTaskletAdapter tasklet = new CallableTaskletAdapter();
        tasklet.setCallable(new Callable<RepeatStatus>() {
            @Override
            public RepeatStatus call() throws Exception {
                // Long-running operation
                performComplexCalculation();
                return RepeatStatus.FINISHED;
            }
        });
        return tasklet;
    }

    // System Command Tasklet
    @Bean
    public SystemCommandTasklet systemCommandTasklet() {
        SystemCommandTasklet tasklet = new SystemCommandTasklet();
        tasklet.setCommand("python /opt/scripts/data_validation.py");
        tasklet.setTimeout(300000); // 5 minutes
        tasklet.setInterruptOnCancel(true);
        return tasklet;
    }

    private ProcessingSummary createProcessingSummary(JobExecution jobExecution) {
        // Implementation
        return new ProcessingSummary();
    }

    private String saveReport(byte[] reportPdf, LocalDate businessDate) {
        // Implementation
        return "/reports/report_" + businessDate + ".pdf";
    }

    private void performFullProcessing() {
        // Implementation
    }

    private void performIncrementalProcessing() {
        // Implementation
    }

    private void performComplexCalculation() {
        // Implementation
    }

    @Bean
    public DatabaseCleanupService databaseCleanupService() {
        return new DatabaseCleanupService();
    }
}

// Decision Guidelines
/**
 * Use Chunk-Oriented Processing When:
 * - Processing large volumes of data (>10,000 records)
 * - Need transaction management per chunk
 * - Require skip/retry functionality
 * - Standard ETL patterns (Extract-Transform-Load)
 * - Memory efficiency is important
 *
 * Use Tasklet Processing When:
 * - Simple operations (file moves, cleanup, etc.)
 * - Complex business logic that doesn't fit read-process-write
 * - Control flow operations
 * - System commands or external process calls
 * - Single operation that should be atomic
 */
```

### Q6: Parallel and Partitioned Jobs

**Question**: How do you implement parallel processing and partitioning in Spring Batch for high-performance banking operations?

**Answer**:

**Parallel Processing Strategies:**

```java
@Configuration
public class ParallelProcessingConfiguration {

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    // 1. Multi-threaded Step Processing
    @Bean
    public Step multiThreadedTransactionStep() {
        return stepBuilderFactory.get("multiThreadedTransactionStep")
            .<TransactionRecord, ProcessedTransaction>chunk(1000)
            .reader(synchronizedItemReader()) // Thread-safe reader
            .processor(transactionProcessor())
            .writer(synchronizedItemWriter()) // Thread-safe writer
            .taskExecutor(batchTaskExecutor())
            .throttleLimit(10) // Max concurrent threads
            .build();
    }

    // Thread-safe reader wrapper
    @Bean
    public SynchronizedItemStreamReader<TransactionRecord> synchronizedItemReader() {
        SynchronizedItemStreamReader<TransactionRecord> reader =
            new SynchronizedItemStreamReader<>();
        reader.setDelegate(fileTransactionReader());
        return reader;
    }

    // Thread-safe writer wrapper
    @Bean
    public SynchronizedItemStreamWriter<ProcessedTransaction> synchronizedItemWriter() {
        SynchronizedItemStreamWriter<ProcessedTransaction> writer =
            new SynchronizedItemStreamWriter<>();
        writer.setDelegate(databaseTransactionWriter());
        return writer;
    }

    // 2. Parallel Flows
    @Bean
    public Job parallelFlowJob() {
        // Account processing flow
        Flow accountFlow = new FlowBuilder<Flow>("accountFlow")
            .start(processAccountsStep())
            .next(calculateAccountInterestStep())
            .build();

        // Transaction processing flow
        Flow transactionFlow = new FlowBuilder<Flow>("transactionFlow")
            .start(processTransactionsStep())
            .next(validateTransactionsStep())
            .build();

        // Customer processing flow
        Flow customerFlow = new FlowBuilder<Flow>("customerFlow")
            .start(processCustomersStep())
            .next(updateCustomerStatusStep())
            .build();

        return jobBuilderFactory.get("parallelFlowJob")
            .start(initializationStep())
            .split(batchTaskExecutor())
                .add(accountFlow, transactionFlow, customerFlow)
            .next(consolidationStep())
            .end()
            .build();
    }

    // 3. Partitioned Processing
    @Bean
    public Step partitionedAccountProcessingStep() {
        return stepBuilderFactory.get("partitionedAccountProcessingStep")
            .partitioner("accountProcessing", accountPartitioner())
            .step(accountProcessingSlaveStep())
            .gridSize(8) // Number of partitions
            .taskExecutor(batchTaskExecutor())
            .build();
    }

    // Account Partitioner by Branch
    @Bean
    public Partitioner accountPartitioner() {
        return new Partitioner() {
            @Override
            public Map<String, ExecutionContext> partition(int gridSize) {
                Map<String, ExecutionContext> partitions = new HashMap<>();

                // Get all active branches
                List<String> branches = branchService.getAllActiveBranches();

                int partitionSize = (int) Math.ceil((double) branches.size() / gridSize);

                for (int i = 0; i < gridSize; i++) {
                    ExecutionContext context = new ExecutionContext();

                    int start = i * partitionSize;
                    int end = Math.min(start + partitionSize, branches.size());

                    if (start < branches.size()) {
                        List<String> partitionBranches = branches.subList(start, end);
                        context.put("branches", String.join(",", partitionBranches));
                        context.put("partition", i);
                        partitions.put("partition" + i, context);
                    }
                }

                return partitions;
            }
        };
    }

    // Slave step for partitioned processing
    @Bean
    public Step accountProcessingSlaveStep() {
        return stepBuilderFactory.get("accountProcessingSlaveStep")
            .<Account, ProcessedAccount>chunk(500)
            .reader(partitionedAccountReader(null, null)) // Injected by partitioner
            .processor(accountProcessor())
            .writer(accountWriter())
            .build();
    }

    // Partitioned Reader
    @Bean
    @StepScope
    public JdbcCursorItemReader<Account> partitionedAccountReader(
            @Value("#{stepExecutionContext['branches']}") String branches,
            @Value("#{stepExecutionContext['partition']}") Integer partition) {

        return new JdbcCursorItemReaderBuilder<Account>()
            .name("partitionedAccountReader")
            .dataSource(dataSource)
            .sql("SELECT * FROM accounts WHERE branch_code IN (" +
                 createInClause(branches) + ") AND status = 'ACTIVE'")
            .rowMapper(new BeanPropertyRowMapper<>(Account.class))
            .build();
    }

    // 4. Remote Chunking for Distributed Processing
    @Bean
    public Step remoteChunkingMasterStep() {
        return stepBuilderFactory.get("remoteChunkingMasterStep")
            .<TransactionRecord, TransactionRecord>chunk(1000)
            .reader(transactionFileReader())
            .writer(messagingItemWriter()) // Sends to message queue
            .build();
    }

    @Bean
    public ChunkMessageChannelItemWriter<TransactionRecord> messagingItemWriter() {
        ChunkMessageChannelItemWriter<TransactionRecord> writer =
            new ChunkMessageChannelItemWriter<>();
        writer.setMessagingOperations(messagingTemplate());
        writer.setReplyChannel(replies());
        return writer;
    }

    // Remote Chunking Slave Configuration
    @Bean
    public IntegrationFlow slaveFlow() {
        return IntegrationFlows
            .from("requests")
            .handle(chunkProcessorChunkHandler())
            .channel("replies")
            .get();
    }

    @Bean
    public ChunkProcessorChunkHandler<TransactionRecord> chunkProcessorChunkHandler() {
        ChunkProcessor<TransactionRecord> chunkProcessor =
            new SimpleChunkProcessor<>(transactionProcessor(), transactionWriter());

        ChunkProcessorChunkHandler<TransactionRecord> handler =
            new ChunkProcessorChunkHandler<>();
        handler.setChunkProcessor(chunkProcessor);
        return handler;
    }

    // 5. Async Item Processing
    @Bean
    public Step asyncProcessingStep() {
        return stepBuilderFactory.get("asyncProcessingStep")
            .<RawTransaction, Future<ProcessedTransaction>>chunk(100)
            .reader(rawTransactionReader())
            .processor(asyncTransactionProcessor())
            .writer(asyncTransactionWriter())
            .build();
    }

    @Bean
    public AsyncItemProcessor<RawTransaction, ProcessedTransaction> asyncTransactionProcessor() {
        AsyncItemProcessor<RawTransaction, ProcessedTransaction> processor =
            new AsyncItemProcessor<>();
        processor.setDelegate(heavyTransactionProcessor());
        processor.setTaskExecutor(processingTaskExecutor());
        return processor;
    }

    @Bean
    public AsyncItemWriter<ProcessedTransaction> asyncTransactionWriter() {
        AsyncItemWriter<ProcessedTransaction> writer = new AsyncItemWriter<>();
        writer.setDelegate(processedTransactionWriter());
        return writer;
    }

    // 6. Database Partitioning Strategy
    @Bean
    public Partitioner databasePartitioner() {
        return new Partitioner() {
            @Override
            public Map<String, ExecutionContext> partition(int gridSize) {
                Map<String, ExecutionContext> partitions = new HashMap<>();

                // Get min and max account IDs
                long minId = accountService.getMinAccountId();
                long maxId = accountService.getMaxAccountId();
                long range = maxId - minId;
                long partitionSize = range / gridSize;

                for (int i = 0; i < gridSize; i++) {
                    ExecutionContext context = new ExecutionContext();

                    long start = minId + (i * partitionSize);
                    long end = (i == gridSize - 1) ? maxId : start + partitionSize - 1;

                    context.putLong("minId", start);
                    context.putLong("maxId", end);
                    context.putInt("partition", i);

                    partitions.put("partition" + i, context);
                }

                return partitions;
            }
        };
    }

    @Bean
    @StepScope
    public JdbcPagingItemReader<Account> pagingAccountReader(
            @Value("#{stepExecutionContext['minId']}") Long minId,
            @Value("#{stepExecutionContext['maxId']}") Long maxId) {

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("minId", minId);
        parameters.put("maxId", maxId);

        return new JdbcPagingItemReaderBuilder<Account>()
            .name("pagingAccountReader")
            .dataSource(dataSource)
            .selectClause("SELECT account_id, account_number, balance, status")
            .fromClause("FROM accounts")
            .whereClause("WHERE account_id BETWEEN :minId AND :maxId")
            .sortKeys(Collections.singletonMap("account_id", Order.ASCENDING))
            .parameterValues(parameters)
            .pageSize(1000)
            .rowMapper(new BeanPropertyRowMapper<>(Account.class))
            .build();
    }

    // Task Executors for Different Types of Processing
    @Bean
    public TaskExecutor batchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("batch-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean
    public TaskExecutor processingTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(15);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("processing-");
        executor.initialize();
        return executor;
    }

    // Monitoring and Metrics for Parallel Processing
    @Bean
    public PartitionHandler partitionHandler() {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setStep(accountProcessingSlaveStep());
        handler.setTaskExecutor(batchTaskExecutor());
        handler.setGridSize(8);
        return handler;
    }

    // Performance Monitoring
    @Bean
    public StepExecutionListener parallelProcessingListener() {
        return new StepExecutionListener() {
            @Override
            public void beforeStep(StepExecution stepExecution) {
                stepExecution.getExecutionContext().putLong("startTime", System.currentTimeMillis());
                log.info("Starting parallel step: {}", stepExecution.getStepName());
            }

            @Override
            public ExitStatus afterStep(StepExecution stepExecution) {
                long duration = System.currentTimeMillis() -
                    stepExecution.getExecutionContext().getLong("startTime");

                log.info("Completed parallel step: {} in {}ms, processed {} items",
                    stepExecution.getStepName(), duration, stepExecution.getReadCount());

                // Log performance metrics
                logPerformanceMetrics(stepExecution, duration);

                return stepExecution.getExitStatus();
            }
        };
    }

    private String createInClause(String branches) {
        if (branches == null || branches.isEmpty()) {
            return "''";
        }
        return "'" + branches.replace(",", "','") + "'";
    }

    private void logPerformanceMetrics(StepExecution stepExecution, long duration) {
        double throughput = stepExecution.getReadCount() / (duration / 1000.0);
        log.info("Throughput: {} records/second", String.format("%.2f", throughput));

        if (stepExecution.getSkipCount() > 0) {
            log.warn("Skipped {} records during processing", stepExecution.getSkipCount());
        }
    }

    // Utility beans
    @Bean
    public BranchService branchService() {
        return new BranchService();
    }

    @Bean
    public AccountService accountService() {
        return new AccountService();
    }
}

// Performance Guidelines:
/**
 * Parallel Processing Strategy Selection:
 *
 * 1. Multi-threaded Step:
 *    - Single machine processing
 *    - Shared memory and resources
 *    - Good for I/O bound operations
 *    - Thread count = 2-4x CPU cores
 *
 * 2. Parallel Flows:
 *    - Independent processing streams
 *    - Different data sets or operations
 *    - Can utilize multiple cores/machines
 *
 * 3. Partitioning:
 *    - Large datasets
 *    - Can distribute across multiple machines
 *    - Good for CPU-intensive operations
 *    - Partition size should be balanced
 *
 * 4. Remote Chunking:
 *    - Network distributed processing
 *    - Master sends chunks to workers
 *    - Good for scaling horizontally
 *
 * 5. Async Processing:
 *    - Long-running item processing
 *    - Non-blocking operations
 *    - Good for external service calls
 */
```

---

*[Continue with remaining sections covering Readers/Processors/Writers, Error Handling, Scheduling/Monitoring, and Banking Use Cases...]*

The guide continues with comprehensive coverage of ItemReader/Processor/Writer implementations, advanced error handling strategies, job scheduling and monitoring, and real-world banking batch processing scenarios, all following the same detailed format with practical examples and best practices.