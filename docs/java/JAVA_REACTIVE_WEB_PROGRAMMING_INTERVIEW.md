# Java Reactive Web Programming - Interview Guide

> **Complete guide to reactive programming with Spring WebFlux, Project Reactor, and reactive systems for technical interviews**

---

## 📚 **Table of Contents**

1. [Reactive Programming Fundamentals](#reactive-programming-fundamentals)
2. [Project Reactor Core](#project-reactor-core)
3. [Spring WebFlux Framework](#spring-webflux-framework)
4. [Reactive Data Access](#reactive-data-access)
5. [WebClient for HTTP](#webclient-for-http)
6. [Server-Sent Events & WebSockets](#server-sent-events--websockets)
7. [Error Handling & Resilience](#error-handling--resilience)
8. [Performance & Testing](#performance--testing)
9. [Reactive vs Traditional Comparison](#reactive-vs-traditional-comparison)
10. [Common Interview Questions](#common-interview-questions)

---

## 🔄 **Reactive Programming Fundamentals**

### **Core Concepts**

```java
/**
 * Reactive programming fundamentals with Java
 */

// 1. Reactive Streams Specification
// - Publisher: Source of data
// - Subscriber: Consumer of data
// - Subscription: Connection between Publisher and Subscriber
// - Processor: Both Publisher and Subscriber

import org.reactivestreams.*;

// Publisher interface from Reactive Streams
public interface Publisher<T> {
    void subscribe(Subscriber<? super T> subscriber);
}

// Subscriber interface
public interface Subscriber<T> {
    void onSubscribe(Subscription subscription);
    void onNext(T item);
    void onError(Throwable throwable);
    void onComplete();
}

// 2. Reactive Principles
public class ReactivePrinciples {

    // Responsive: System responds in timely manner
    public Mono<String> responsiveService() {
        return Mono.just("response")
                   .timeout(Duration.ofSeconds(2))
                   .onErrorReturn("timeout_response");
    }

    // Resilient: System remains responsive in face of failure
    public Mono<String> resilientService() {
        return callExternalService()
                .retry(3)
                .onErrorReturn("fallback_response");
    }

    // Elastic: System remains responsive under varying workload
    public Flux<String> elasticService() {
        return Flux.range(1, 1000000)
                   .map(i -> "item-" + i)
                   .subscribeOn(Schedulers.parallel())
                   .publishOn(Schedulers.boundedElastic());
    }

    // Message-driven: System relies on asynchronous message-passing
    public Mono<String> messageDrivenService() {
        return Mono.fromCallable(() -> processMessage())
                   .subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<String> callExternalService() {
        return Mono.just("external_response");
    }

    private String processMessage() {
        return "processed";
    }
}

// 3. Reactive Streams vs Traditional Approach
public class ReactiveVsTraditional {

    // Traditional imperative approach
    public List<String> traditionalProcessing() {
        List<String> results = new ArrayList<>();

        for (int i = 1; i <= 1000; i++) {
            String item = fetchData(i);      // Blocking
            String processed = process(item); // Blocking
            results.add(processed);
        }

        return results; // Returns all at once
    }

    // Reactive declarative approach
    public Flux<String> reactiveProcessing() {
        return Flux.range(1, 1000)
                   .flatMap(this::fetchDataAsync)    // Non-blocking
                   .map(this::process)               // Transform
                   .publishOn(Schedulers.parallel()) // Asynchronous
                   .onErrorContinue((err, item) ->
                       logger.error("Error processing {}", item, err));
    }

    private String fetchData(int id) {
        // Simulates blocking I/O
        try { Thread.sleep(10); } catch (InterruptedException e) {}
        return "data-" + id;
    }

    private Mono<String> fetchDataAsync(int id) {
        return Mono.fromCallable(() -> "data-" + id)
                   .subscribeOn(Schedulers.boundedElastic())
                   .delayElement(Duration.ofMillis(10));
    }

    private String process(String data) {
        return data.toUpperCase();
    }
}
```

### **Backpressure Handling**

```java
/**
 * Backpressure management in reactive streams
 */
public class BackpressureExamples {

    // Producer faster than consumer
    public void demonstrateBackpressure() {
        Flux.interval(Duration.ofMillis(1))      // Fast producer
            .onBackpressureBuffer(1000)          // Buffer up to 1000 items
            .publishOn(Schedulers.single())
            .doOnNext(this::slowConsumer)        // Slow consumer
            .subscribe();
    }

    // Different backpressure strategies
    public Flux<Long> bufferStrategy() {
        return Flux.interval(Duration.ofMillis(1))
                   .onBackpressureBuffer(100,               // Buffer size
                                       BufferOverflowStrategy.DROP_LATEST); // Strategy when full
    }

    public Flux<Long> dropStrategy() {
        return Flux.interval(Duration.ofMillis(1))
                   .onBackpressureDrop(item ->
                       logger.warn("Dropped item: {}", item));
    }

    public Flux<Long> latestStrategy() {
        return Flux.interval(Duration.ofMillis(1))
                   .onBackpressureLatest();  // Keep only latest item
    }

    public Flux<Long> errorStrategy() {
        return Flux.interval(Duration.ofMillis(1))
                   .onBackpressureError();   // Throw error when buffer full
    }

    // Custom backpressure handling
    public Flux<String> customBackpressure() {
        return Flux.<String>create(sink -> {
            // Custom producer
            for (int i = 0; i < 1000; i++) {
                sink.next("item-" + i);

                // Check if subscriber can handle more
                if (sink.requestedFromDownstream() == 0) {
                    // Pause production or buffer
                    try { Thread.sleep(10); } catch (InterruptedException e) {}
                }
            }
            sink.complete();
        }, FluxSink.OverflowStrategy.BUFFER);
    }

    // Reactive pull model
    public void demonstrateReactivePull() {
        Flux.range(1, 1000)
            .subscribe(new BaseSubscriber<Integer>() {
                @Override
                protected void hookOnSubscribe(Subscription subscription) {
                    request(1); // Request only 1 item initially
                }

                @Override
                protected void hookOnNext(Integer value) {
                    process(value);
                    request(1); // Request next item after processing
                }

                private void process(Integer value) {
                    // Simulate processing time
                    try { Thread.sleep(100); } catch (InterruptedException e) {}
                    logger.info("Processed: {}", value);
                }
            });
    }

    private void slowConsumer(Long item) {
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        logger.info("Consumed: {}", item);
    }
}
```

---

## ⚛️ **Project Reactor Core**

### **Mono and Flux Operations**

```java
/**
 * Comprehensive Mono and Flux operations
 */
public class ReactorOperations {

    // Mono - 0 or 1 element
    public void monoOperations() {
        // Creation
        Mono<String> empty = Mono.empty();
        Mono<String> just = Mono.just("Hello");
        Mono<String> fromCallable = Mono.fromCallable(() -> "World");
        Mono<String> fromSupplier = Mono.fromSupplier(() -> "Reactive");
        Mono<String> defer = Mono.defer(() -> Mono.just("Deferred"));

        // Transformation
        Mono<String> mapped = just.map(String::toUpperCase);
        Mono<Integer> flatMapped = just.flatMap(s -> Mono.just(s.length()));
        Mono<String> filtered = just.filter(s -> s.length() > 3);

        // Combination
        Mono<String> zipped = Mono.zip(just, fromCallable)
                                 .map(tuple -> tuple.getT1() + " " + tuple.getT2());

        // Error handling
        Mono<String> withFallback = just
            .map(s -> {
                if (s.equals("Hello")) throw new RuntimeException("Error");
                return s;
            })
            .onErrorReturn("Fallback")
            .retry(2);

        // Timing
        Mono<String> delayed = just.delayElement(Duration.ofSeconds(1));
        Mono<String> timeout = just.timeout(Duration.ofSeconds(5));

        // Side effects
        Mono<String> withSideEffects = just
            .doOnSubscribe(sub -> logger.info("Subscribed"))
            .doOnNext(value -> logger.info("Received: {}", value))
            .doOnSuccess(value -> logger.info("Success: {}", value))
            .doOnError(error -> logger.error("Error", error))
            .doFinally(signal -> logger.info("Finally: {}", signal));
    }

    // Flux - 0 to N elements
    public void fluxOperations() {
        // Creation
        Flux<Integer> range = Flux.range(1, 10);
        Flux<String> fromIterable = Flux.fromIterable(List.of("a", "b", "c"));
        Flux<Long> interval = Flux.interval(Duration.ofSeconds(1));
        Flux<String> generate = Flux.generate(
            () -> 0,                                    // Initial state
            (state, sink) -> {                         // Generator
                sink.next("item-" + state);
                if (state == 10) sink.complete();
                return state + 1;
            }
        );

        // Transformation
        Flux<String> mapped = range.map(i -> "item-" + i);
        Flux<String> flatMapped = range.flatMap(i ->
            Flux.just("a-" + i, "b-" + i));
        Flux<List<Integer>> buffered = range.buffer(3);
        Flux<List<Integer>> windowed = range.window(3).flatMap(Flux::collectList);

        // Filtering
        Flux<Integer> filtered = range.filter(i -> i % 2 == 0);
        Flux<Integer> distinct = Flux.just(1, 2, 2, 3, 3, 4).distinct();
        Flux<Integer> taken = range.take(5);
        Flux<Integer> skipped = range.skip(3);

        // Aggregation
        Mono<Integer> sum = range.reduce(Integer::sum);
        Mono<List<Integer>> collected = range.collectList();
        Mono<Map<Boolean, List<Integer>>> grouped = range
            .collectMultimap(i -> i % 2 == 0);

        // Combination
        Flux<String> merged = Flux.merge(
            Flux.just("a", "b"),
            Flux.just("c", "d")
        );

        Flux<String> concatenated = Flux.concat(
            Flux.just("first"),
            Flux.just("second")
        );

        Flux<String> zipped = Flux.zip(
            Flux.just("1", "2", "3"),
            Flux.just("a", "b", "c"),
            (num, letter) -> num + letter
        );

        // Error handling
        Flux<Integer> withErrorHandling = range
            .map(i -> {
                if (i == 5) throw new RuntimeException("Error at 5");
                return i;
            })
            .onErrorContinue((error, item) ->
                logger.error("Error processing {}", item, error))
            .retry(2);

        // Parallel processing
        Flux<String> parallel = range
            .parallel(4)                              // 4 parallel rails
            .runOn(Schedulers.parallel())            // Run on parallel scheduler
            .map(i -> processItem(i))                // CPU-intensive work
            .sequential();                           // Merge back to single stream
    }

    // Advanced operators
    public void advancedOperations() {
        // Switch operators
        Flux<String> switchMap = Flux.interval(Duration.ofSeconds(1))
            .switchMap(i -> Flux.just("item-" + i)
                               .delayElement(Duration.ofMillis(500)));

        // Publish operators
        ConnectableFlux<Integer> published = Flux.range(1, 5).publish();
        published.subscribe(i -> logger.info("Subscriber 1: {}", i));
        published.subscribe(i -> logger.info("Subscriber 2: {}", i));
        published.connect(); // Start emission

        // Replay
        Flux<Integer> replayed = Flux.range(1, 3)
            .replay(2)      // Cache last 2 items
            .autoConnect(); // Auto-connect when first subscriber subscribes

        // Share
        Flux<Long> shared = Flux.interval(Duration.ofSeconds(1))
            .share(); // Multiple subscribers share single subscription

        // Sample and throttle
        Flux<Long> sampled = Flux.interval(Duration.ofMillis(100))
            .sample(Duration.ofSeconds(1)); // Emit latest item every second

        Flux<Long> throttled = Flux.interval(Duration.ofMillis(100))
            .throttleFirst(Duration.ofSeconds(1)); // Emit first item then throttle
    }

    // Custom operators
    public <T> Function<Flux<T>, Flux<T>> logItems(String prefix) {
        return flux -> flux.doOnNext(item ->
            logger.info("{}: {}", prefix, item));
    }

    public <T> Function<Flux<T>, Flux<T>> retryWithDelay(int attempts, Duration delay) {
        return flux -> flux.retryWhen(Retry.backoff(attempts, delay));
    }

    // Usage of custom operators
    public void useCustomOperators() {
        Flux.range(1, 10)
            .transform(logItems("Processing"))
            .map(i -> {
                if (i == 5) throw new RuntimeException("Error");
                return i;
            })
            .transform(retryWithDelay(3, Duration.ofSeconds(1)))
            .subscribe();
    }

    private String processItem(Integer item) {
        // Simulate CPU-intensive work
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        return "processed-" + item;
    }
}
```

### **Schedulers and Threading**

```java
/**
 * Reactive scheduling and threading models
 */
public class ReactiveScheduling {

    // Different scheduler types
    public void schedulerTypes() {
        // Immediate - current thread
        Flux.range(1, 5)
            .subscribeOn(Schedulers.immediate())
            .subscribe(i -> logger.info("Immediate: {} on {}",
                      i, Thread.currentThread().getName()));

        // Single - single dedicated thread
        Flux.range(1, 5)
            .subscribeOn(Schedulers.single())
            .subscribe(i -> logger.info("Single: {} on {}",
                      i, Thread.currentThread().getName()));

        // Parallel - CPU intensive work
        Flux.range(1, 5)
            .subscribeOn(Schedulers.parallel())
            .subscribe(i -> logger.info("Parallel: {} on {}",
                      i, Thread.currentThread().getName()));

        // BoundedElastic - I/O operations
        Flux.range(1, 5)
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(i -> logger.info("BoundedElastic: {} on {}",
                      i, Thread.currentThread().getName()));
    }

    // subscribeOn vs publishOn
    public void subscribeOnVsPublishOn() {
        Flux.range(1, 3)
            .doOnNext(i -> logger.info("1. Source: {} on {}",
                     i, Thread.currentThread().getName()))
            .subscribeOn(Schedulers.boundedElastic())  // Affects upstream
            .doOnNext(i -> logger.info("2. After subscribeOn: {} on {}",
                     i, Thread.currentThread().getName()))
            .publishOn(Schedulers.parallel())          // Affects downstream
            .doOnNext(i -> logger.info("3. After publishOn: {} on {}",
                     i, Thread.currentThread().getName()))
            .subscribe(i -> logger.info("4. Final: {} on {}",
                      i, Thread.currentThread().getName()));
    }

    // Custom scheduler
    public void customScheduler() {
        var customExecutor = Executors.newFixedThreadPool(4, r -> {
            var thread = new Thread(r, "custom-thread");
            thread.setDaemon(true);
            return thread;
        });

        var customScheduler = Schedulers.fromExecutor(customExecutor);

        Flux.range(1, 10)
            .subscribeOn(customScheduler)
            .doOnNext(i -> logger.info("Custom: {} on {}",
                     i, Thread.currentThread().getName()))
            .subscribe();

        // Remember to dispose
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            customScheduler.dispose();
            customExecutor.shutdown();
        }));
    }

    // Parallel processing patterns
    public void parallelProcessing() {
        // Parallel rails
        Flux.range(1, 1000)
            .parallel(4)                           // Split into 4 rails
            .runOn(Schedulers.parallel())         // Each rail on different thread
            .map(this::cpuIntensiveWork)          // Parallel processing
            .sequential()                         // Merge back
            .subscribe();

        // FlatMap for I/O concurrency
        Flux.range(1, 100)
            .flatMap(this::ioOperation, 10)       // Max 10 concurrent operations
            .subscribe();

        // Group and process in parallel
        Flux.range(1, 1000)
            .groupBy(i -> i % 4)                  // Group by modulo 4
            .flatMap(group -> group
                .subscribeOn(Schedulers.parallel()) // Each group on different thread
                .map(this::cpuIntensiveWork))
            .subscribe();
    }

    // Thread safety considerations
    @Component
    public static class ThreadSafeReactiveService {
        private final AtomicLong counter = new AtomicLong(0);
        private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();

        public Mono<Long> getNextId() {
            return Mono.fromCallable(counter::incrementAndGet)
                      .subscribeOn(Schedulers.boundedElastic());
        }

        public Mono<String> getCachedValue(String key) {
            return Mono.fromCallable(() -> cache.get(key))
                      .subscribeOn(Schedulers.boundedElastic());
        }

        public Mono<Void> putCachedValue(String key, String value) {
            return Mono.fromRunnable(() -> cache.put(key, value))
                      .subscribeOn(Schedulers.boundedElastic())
                      .then();
        }
    }

    private String cpuIntensiveWork(Integer input) {
        // Simulate CPU work
        var sum = 0;
        for (int i = 0; i < 1000000; i++) {
            sum += i;
        }
        return "processed-" + input + "-" + sum;
    }

    private Mono<String> ioOperation(Integer input) {
        return Mono.fromCallable(() -> {
            // Simulate I/O
            Thread.sleep(100);
            return "io-result-" + input;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
```

---

## 🌐 **Spring WebFlux Framework**

### **WebFlux Controllers**

```java
/**
 * Spring WebFlux reactive controllers
 */

// Annotation-based controllers
@RestController
@RequestMapping("/api/v1")
public class ReactiveController {

    private final UserService userService;
    private final NotificationService notificationService;

    public ReactiveController(UserService userService,
                             NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    // Basic CRUD operations
    @GetMapping("/users/{id}")
    public Mono<ResponseEntity<User>> getUser(@PathVariable String id) {
        return userService.findById(id)
                         .map(ResponseEntity::ok)
                         .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/users")
    public Flux<User> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return userService.findAll(PageRequest.of(page, size));
    }

    @PostMapping("/users")
    public Mono<ResponseEntity<User>> createUser(@RequestBody @Valid CreateUserRequest request) {
        return userService.create(request)
                         .map(user -> ResponseEntity.status(HttpStatus.CREATED).body(user))
                         .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PutMapping("/users/{id}")
    public Mono<ResponseEntity<User>> updateUser(
            @PathVariable String id,
            @RequestBody @Valid UpdateUserRequest request) {
        return userService.update(id, request)
                         .map(ResponseEntity::ok)
                         .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable String id) {
        return userService.deleteById(id)
                         .then(Mono.just(ResponseEntity.noContent().<Void>build()))
                         .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Streaming responses
    @GetMapping(value = "/users/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<User> streamUsers() {
        return userService.findAllStream()
                         .delayElements(Duration.ofMillis(100)); // Simulate delay
    }

    // Server-Sent Events
    @GetMapping(value = "/users/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<UserEvent>> streamUserEvents() {
        return userService.getUserEvents()
                         .map(event -> ServerSentEvent.builder(event)
                                                     .id(event.getId())
                                                     .event(event.getType())
                                                     .build());
    }

    // File upload
    @PostMapping(value = "/users/{id}/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<ResponseEntity<Void>> uploadAvatar(
            @PathVariable String id,
            @RequestPart("file") Mono<FilePart> filePart) {
        return filePart
                .flatMap(file -> userService.saveAvatar(id, file))
                .then(Mono.just(ResponseEntity.ok().<Void>build()))
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    // Batch operations
    @PostMapping("/users/batch")
    public Flux<User> createUsers(@RequestBody Flux<CreateUserRequest> requests) {
        return requests
                .flatMap(userService::create, 5) // Max 5 concurrent operations
                .onErrorContinue((error, item) ->
                    logger.error("Failed to create user {}", item, error));
    }

    // Complex operations with multiple services
    @PostMapping("/users/{id}/notify")
    public Mono<ResponseEntity<Void>> notifyUser(
            @PathVariable String id,
            @RequestBody NotificationRequest request) {
        return userService.findById(id)
                         .flatMap(user -> notificationService.send(user, request))
                         .then(Mono.just(ResponseEntity.accepted().<Void>build()))
                         .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Error handling
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        return ResponseEntity.badRequest()
                           .body(new ErrorResponse("VALIDATION_ERROR", ex.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }
}

// Functional routing approach
@Configuration
public class UserRoutes {

    @Bean
    public RouterFunction<ServerResponse> userRouter(UserHandler userHandler) {
        return RouterFunctions
                .route(GET("/api/v1/users/{id}"), userHandler::getUser)
                .andRoute(GET("/api/v1/users"), userHandler::listUsers)
                .andRoute(POST("/api/v1/users"), userHandler::createUser)
                .andRoute(PUT("/api/v1/users/{id}"), userHandler::updateUser)
                .andRoute(DELETE("/api/v1/users/{id}"), userHandler::deleteUser)
                .andRoute(GET("/api/v1/users/stream"), userHandler::streamUsers)
                .filter(this::loggingFilter)
                .filter(this::authenticationFilter);
    }

    private Mono<ServerResponse> loggingFilter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        var start = System.currentTimeMillis();
        return next.handle(request)
                  .doFinally(signal -> {
                      var duration = System.currentTimeMillis() - start;
                      logger.info("Request {} {} took {}ms",
                                request.method(), request.path(), duration);
                  });
    }

    private Mono<ServerResponse> authenticationFilter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        return request.headers().firstHeader("Authorization")
                     .map(this::validateToken)
                     .filter(valid -> valid)
                     .flatMap(valid -> next.handle(request))
                     .switchIfEmpty(ServerResponse.status(HttpStatus.UNAUTHORIZED).build());
    }

    private boolean validateToken(String token) {
        // Token validation logic
        return token != null && token.startsWith("Bearer ");
    }
}

@Component
public class UserHandler {
    private final UserService userService;

    public UserHandler(UserService userService) {
        this.userService = userService;
    }

    public Mono<ServerResponse> getUser(ServerRequest request) {
        var id = request.pathVariable("id");
        return userService.findById(id)
                         .flatMap(user -> ServerResponse.ok().bodyValue(user))
                         .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> listUsers(ServerRequest request) {
        var page = request.queryParam("page").map(Integer::parseInt).orElse(0);
        var size = request.queryParam("size").map(Integer::parseInt).orElse(10);

        return ServerResponse.ok()
                           .body(userService.findAll(PageRequest.of(page, size)), User.class);
    }

    public Mono<ServerResponse> createUser(ServerRequest request) {
        return request.bodyToMono(CreateUserRequest.class)
                     .flatMap(userService::create)
                     .flatMap(user -> ServerResponse.status(HttpStatus.CREATED).bodyValue(user))
                     .onErrorResume(ValidationException.class,
                                  ex -> ServerResponse.badRequest().bodyValue(ex.getMessage()));
    }

    public Mono<ServerResponse> updateUser(ServerRequest request) {
        var id = request.pathVariable("id");
        return request.bodyToMono(UpdateUserRequest.class)
                     .flatMap(updateRequest -> userService.update(id, updateRequest))
                     .flatMap(user -> ServerResponse.ok().bodyValue(user))
                     .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteUser(ServerRequest request) {
        var id = request.pathVariable("id");
        return userService.deleteById(id)
                         .then(ServerResponse.noContent().build())
                         .onErrorResume(UserNotFoundException.class,
                                      ex -> ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> streamUsers(ServerRequest request) {
        return ServerResponse.ok()
                           .contentType(MediaType.APPLICATION_NDJSON)
                           .body(userService.findAllStream(), User.class);
    }
}
```

### **WebFlux Configuration**

```java
/**
 * Spring WebFlux configuration
 */

@Configuration
@EnableWebFlux
public class WebFluxConfig implements WebFluxConfigurer {

    // Custom message converters
    @Override
    public void configureHttpMessageCodecs(ServerCodecConfigurer configurer) {
        // Increase max in-memory size for file uploads
        configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024); // 10MB

        // Custom JSON configuration
        configurer.defaultCodecs().jackson2JsonEncoder(
            new Jackson2JsonEncoder(createObjectMapper()));
        configurer.defaultCodecs().jackson2JsonDecoder(
            new Jackson2JsonDecoder(createObjectMapper()));

        // Protobuf support
        configurer.defaultCodecs().protobufEncoder(new ProtobufEncoder());
        configurer.defaultCodecs().protobufDecoder(new ProtobufDecoder());
    }

    // CORS configuration
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:3000", "https://mydomain.com")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // Global error handling
    @Bean
    public WebExceptionHandler globalExceptionHandler() {
        return new GlobalWebExceptionHandler();
    }

    // Custom ObjectMapper
    private ObjectMapper createObjectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .registerModule(new JavaTimeModule());
    }

    // WebClient configuration
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                       .clientConnector(createConnector())
                       .defaultHeader(HttpHeaders.USER_AGENT, "MyApp/1.0")
                       .filter(loggingFilter())
                       .filter(retryFilter())
                       .build();
    }

    private ClientHttpConnector createConnector() {
        var httpClient = HttpClient.create()
                                  .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                                  .responseTimeout(Duration.ofSeconds(30))
                                  .doOnConnected(conn ->
                                      conn.addHandlerLast(new ReadTimeoutHandler(30, TimeUnit.SECONDS))
                                          .addHandlerLast(new WriteTimeoutHandler(30, TimeUnit.SECONDS)));

        return new ReactorClientHttpConnector(httpClient);
    }

    private ExchangeFilterFunction loggingFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            logger.info("Request: {} {}", request.method(), request.url());
            return Mono.just(request);
        });
    }

    private ExchangeFilterFunction retryFilter() {
        return (request, next) -> next.exchange(request)
                                     .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                                                   .filter(throwable -> throwable instanceof ConnectException));
    }
}

@Component
public class GlobalWebExceptionHandler implements WebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        var response = exchange.getResponse();
        var request = exchange.getRequest();

        logger.error("Error processing request {} {}", request.getMethod(), request.getPath(), ex);

        if (ex instanceof ValidationException) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return writeErrorResponse(response, "VALIDATION_ERROR", ex.getMessage());
        } else if (ex instanceof UserNotFoundException) {
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return writeErrorResponse(response, "USER_NOT_FOUND", ex.getMessage());
        } else if (ex instanceof ResponseStatusException rse) {
            response.setStatusCode(rse.getStatusCode());
            return writeErrorResponse(response, "HTTP_ERROR", rse.getReason());
        } else {
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            return writeErrorResponse(response, "INTERNAL_ERROR", "An unexpected error occurred");
        }
    }

    private Mono<Void> writeErrorResponse(ServerHttpResponse response, String code, String message) {
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);

        var errorResponse = new ErrorResponse(code, message, Instant.now());
        var buffer = response.bufferFactory().wrap(JsonUtils.toJson(errorResponse).getBytes());

        return response.writeWith(Mono.just(buffer));
    }
}

// Security configuration for WebFlux
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        return http
                .csrf().disable()
                .authorizeExchange(exchanges -> exchanges
                    .pathMatchers("/api/public/**").permitAll()
                    .pathMatchers(HttpMethod.POST, "/api/users").hasRole("ADMIN")
                    .pathMatchers("/api/users/**").hasAnyRole("USER", "ADMIN")
                    .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt())
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        return ReactiveJwtDecoders.fromIssuerLocation("https://auth.example.com");
    }

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>> jwtAuthenticationConverter() {
        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            var authorities = jwt.getClaimAsStringList("authorities");
            return authorities.stream()
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toList());
        });
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
```

---

## 💾 **Reactive Data Access**

### **Reactive Repositories**

```java
/**
 * Reactive data access with Spring Data
 */

// Reactive MongoDB repository
@Repository
public interface ReactiveUserRepository extends ReactiveMongoRepository<User, String> {

    // Query methods
    Flux<User> findByLastName(String lastName);
    Flux<User> findByAgeGreaterThan(int age);
    Mono<User> findByEmail(String email);
    Flux<User> findByActiveTrue();

    // Custom queries
    @Query("{ 'age': { $gte: ?0, $lte: ?1 } }")
    Flux<User> findByAgeBetween(int minAge, int maxAge);

    @Query("{ 'tags': { $in: ?0 } }")
    Flux<User> findByTagsIn(List<String> tags);

    // Aggregation
    @Aggregation(pipeline = {
        "{ $group: { _id: '$department', count: { $sum: 1 } } }",
        "{ $sort: { count: -1 } }"
    })
    Flux<DepartmentCount> countByDepartment();

    // Streaming
    @Tailable
    Flux<User> findByActiveTrue();

    // Delete operations
    Mono<Long> deleteByActive(boolean active);
    Mono<Void> deleteByEmailContaining(String emailPattern);
}

// Custom reactive repository implementation
@Component
public class CustomUserRepositoryImpl implements CustomUserRepository {

    private final ReactiveMongoTemplate mongoTemplate;

    public CustomUserRepositoryImpl(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Flux<User> findUsersWithComplexCriteria(UserSearchCriteria criteria) {
        var query = new Query();

        if (criteria.getName() != null) {
            query.addCriteria(Criteria.where("name").regex(criteria.getName(), "i"));
        }

        if (criteria.getMinAge() != null) {
            query.addCriteria(Criteria.where("age").gte(criteria.getMinAge()));
        }

        if (criteria.getMaxAge() != null) {
            query.addCriteria(Criteria.where("age").lte(criteria.getMaxAge()));
        }

        if (criteria.getTags() != null && !criteria.getTags().isEmpty()) {
            query.addCriteria(Criteria.where("tags").in(criteria.getTags()));
        }

        return mongoTemplate.find(query, User.class);
    }

    @Override
    public Mono<User> findAndUpdateUser(String id, UpdateUserRequest request) {
        var query = Query.query(Criteria.where("id").is(id));
        var update = new Update()
                .set("name", request.getName())
                .set("email", request.getEmail())
                .set("lastModified", Instant.now());

        var options = FindAndModifyOptions.options()
                .returnNew(true)
                .upsert(false);

        return mongoTemplate.findAndModify(query, update, options, User.class);
    }

    @Override
    public Flux<User> findUsersNearLocation(double latitude, double longitude, double radiusKm) {
        var point = new Point(longitude, latitude);
        var distance = new Distance(radiusKm, Metrics.KILOMETERS);
        var circle = new Circle(point, distance);

        var query = Query.query(Criteria.where("location").withinSphere(circle));

        return mongoTemplate.find(query, User.class);
    }
}

// Reactive R2DBC repository (SQL databases)
@Repository
public interface ReactiveOrderRepository extends ReactiveCrudRepository<Order, Long> {

    @Query("SELECT * FROM orders WHERE customer_id = :customerId ORDER BY created_at DESC")
    Flux<Order> findByCustomerIdOrderByCreatedAtDesc(String customerId);

    @Query("SELECT * FROM orders WHERE status = :status AND created_at >= :since")
    Flux<Order> findByStatusAndCreatedAtAfter(OrderStatus status, Instant since);

    @Modifying
    @Query("UPDATE orders SET status = :status WHERE id = :id")
    Mono<Integer> updateOrderStatus(Long id, OrderStatus status);

    @Query("SELECT COUNT(*) FROM orders WHERE created_at >= :start AND created_at < :end")
    Mono<Long> countOrdersBetween(Instant start, Instant end);
}

// Custom R2DBC repository
@Component
public class CustomOrderRepositoryImpl {

    private final DatabaseClient databaseClient;

    public CustomOrderRepositoryImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    public Flux<OrderSummary> getOrderSummaryByMonth(int year) {
        var sql = """
            SELECT
                EXTRACT(MONTH FROM created_at) as month,
                COUNT(*) as order_count,
                SUM(total_amount) as total_revenue
            FROM orders
            WHERE EXTRACT(YEAR FROM created_at) = :year
            GROUP BY EXTRACT(MONTH FROM created_at)
            ORDER BY month
            """;

        return databaseClient.sql(sql)
                           .bind("year", year)
                           .map(row -> new OrderSummary(
                               row.get("month", Integer.class),
                               row.get("order_count", Long.class),
                               row.get("total_revenue", BigDecimal.class)
                           ))
                           .all();
    }

    public Mono<Void> bulkUpdateOrderStatus(List<Long> orderIds, OrderStatus status) {
        var sql = "UPDATE orders SET status = :status WHERE id = ANY(:ids)";

        return databaseClient.sql(sql)
                           .bind("status", status)
                           .bind("ids", orderIds.toArray(new Long[0]))
                           .fetch()
                           .rowsUpdated()
                           .then();
    }
}

// Transaction management
@Service
@Transactional
public class TransactionalUserService {

    private final ReactiveUserRepository userRepository;
    private final ReactiveAuditRepository auditRepository;

    public TransactionalUserService(ReactiveUserRepository userRepository,
                                   ReactiveAuditRepository auditRepository) {
        this.userRepository = userRepository;
        this.auditRepository = auditRepository;
    }

    @Transactional
    public Mono<User> createUserWithAudit(CreateUserRequest request) {
        return userRepository.save(new User(request))
                           .flatMap(user -> {
                               var audit = new AuditEvent("USER_CREATED", user.getId());
                               return auditRepository.save(audit)
                                                   .thenReturn(user);
                           });
    }

    @Transactional
    public Mono<Void> transferUserData(String fromUserId, String toUserId) {
        return userRepository.findById(fromUserId)
                           .zipWith(userRepository.findById(toUserId))
                           .flatMap(tuple -> {
                               var fromUser = tuple.getT1();
                               var toUser = tuple.getT2();

                               // Transfer data logic
                               toUser.addData(fromUser.getData());
                               fromUser.clearData();

                               return userRepository.save(toUser)
                                                 .then(userRepository.save(fromUser))
                                                 .then();
                           });
    }

    @Transactional(rollbackFor = Exception.class)
    public Mono<User> updateUserWithRollback(String id, UpdateUserRequest request) {
        return userRepository.findById(id)
                           .flatMap(user -> {
                               user.update(request);
                               return userRepository.save(user);
                           })
                           .flatMap(user -> {
                               // Simulate potential failure
                               if (user.getEmail().contains("fail")) {
                                   return Mono.error(new RuntimeException("Simulated failure"));
                               }
                               return Mono.just(user);
                           });
    }
}
```

### **Database Connection Management**

```java
/**
 * Reactive database connection configuration
 */

// MongoDB configuration
@Configuration
public class ReactiveMongoConfig extends AbstractReactiveMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "reactive_app";
    }

    @Override
    public MongoClient reactiveMongoClient() {
        var connectionString = ConnectionString.builder()
                .applyConnectionString(new ConnectionString("mongodb://localhost:27017"))
                .readPreference(ReadPreference.secondaryPreferred())
                .writeConcern(WriteConcern.MAJORITY)
                .readConcern(ReadConcern.MAJORITY)
                .build();

        var settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .codecRegistry(createCodecRegistry())
                .build();

        return MongoClients.create(settings);
    }

    @Override
    public ReactiveMongoTemplate reactiveMongoTemplate() {
        return new ReactiveMongoTemplate(reactiveMongoClient(), getDatabaseName());
    }

    private CodecRegistry createCodecRegistry() {
        return CodecRegistries.fromRegistries(
            MongoClientSettings.getDefaultCodecRegistry(),
            CodecRegistries.fromProviders(new PojoCodecProvider())
        );
    }

    // Custom converter for reactive operations
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return new MongoCustomConversions(List.of(
            new InstantToDateConverter(),
            new DateToInstantConverter()
        ));
    }

    @WritingConverter
    public static class InstantToDateConverter implements Converter<Instant, Date> {
        @Override
        public Date convert(Instant source) {
            return Date.from(source);
        }
    }

    @ReadingConverter
    public static class DateToInstantConverter implements Converter<Date, Instant> {
        @Override
        public Instant convert(Date source) {
            return source.toInstant();
        }
    }
}

// R2DBC configuration
@Configuration
@EnableR2dbcRepositories
public class R2dbcConfig extends AbstractR2dbcConfiguration {

    @Bean
    @Primary
    public ConnectionFactory connectionFactory() {
        return ConnectionFactories.get(ConnectionFactoryOptions.builder()
            .option(ConnectionFactoryOptions.DRIVER, "postgresql")
            .option(ConnectionFactoryOptions.HOST, "localhost")
            .option(ConnectionFactoryOptions.PORT, 5432)
            .option(ConnectionFactoryOptions.USER, "postgres")
            .option(ConnectionFactoryOptions.PASSWORD, "password")
            .option(ConnectionFactoryOptions.DATABASE, "reactive_db")
            .build());
    }

    @Bean
    public R2dbcTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }

    @Bean
    public DatabaseClient databaseClient(ConnectionFactory connectionFactory) {
        return DatabaseClient.builder()
                           .connectionFactory(connectionFactory)
                           .namedParameters(true)
                           .build();
    }

    // Connection pool configuration
    @Bean
    public ConnectionFactory pooledConnectionFactory() {
        var baseConnectionFactory = ConnectionFactories.get(
            "r2dbc:postgresql://postgres:password@localhost:5432/reactive_db");

        var configuration = ConnectionPoolConfiguration.builder(baseConnectionFactory)
                .initialSize(5)
                .maxSize(20)
                .maxIdleTime(Duration.ofMinutes(30))
                .maxCreateConnectionTime(Duration.ofSeconds(10))
                .validationQuery("SELECT 1")
                .build();

        return new ConnectionPool(configuration);
    }

    // Custom converters
    @Override
    protected List<Object> getCustomConverters() {
        return List.of(
            new InstantToLocalDateTimeConverter(),
            new LocalDateTimeToInstantConverter(),
            new JsonToMapConverter(),
            new MapToJsonConverter()
        );
    }

    @WritingConverter
    public static class InstantToLocalDateTimeConverter implements Converter<Instant, LocalDateTime> {
        @Override
        public LocalDateTime convert(Instant source) {
            return LocalDateTime.ofInstant(source, ZoneOffset.UTC);
        }
    }

    @ReadingConverter
    public static class LocalDateTimeToInstantConverter implements Converter<LocalDateTime, Instant> {
        @Override
        public Instant convert(LocalDateTime source) {
            return source.toInstant(ZoneOffset.UTC);
        }
    }
}

// Connection health monitoring
@Component
public class DatabaseHealthIndicator implements ReactiveHealthIndicator {

    private final DatabaseClient databaseClient;

    public DatabaseHealthIndicator(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Health> health() {
        return databaseClient.sql("SELECT 1")
                           .fetch()
                           .one()
                           .map(result -> Health.up()
                                              .withDetail("database", "PostgreSQL")
                                              .withDetail("status", "Connected")
                                              .build())
                           .onErrorReturn(Health.down()
                                              .withDetail("database", "PostgreSQL")
                                              .withDetail("status", "Connection failed")
                                              .build())
                           .timeout(Duration.ofSeconds(5));
    }
}
```

---

## 🌐 **WebClient for HTTP**

### **WebClient Configuration and Usage**

```java
/**
 * Comprehensive WebClient examples for reactive HTTP calls
 */

@Configuration
public class WebClientConfig {

    @Bean
    @Primary
    public WebClient defaultWebClient() {
        return WebClient.builder()
                       .clientConnector(createConnector())
                       .defaultHeaders(this::addDefaultHeaders)
                       .filter(loggingFilter())
                       .filter(errorHandlingFilter())
                       .filter(retryFilter())
                       .filter(circuitBreakerFilter())
                       .build();
    }

    @Bean
    @Qualifier("authWebClient")
    public WebClient authWebClient() {
        return WebClient.builder()
                       .baseUrl("https://auth.example.com")
                       .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                       .filter(oauth2Filter())
                       .build();
    }

    private ClientHttpConnector createConnector() {
        var httpClient = HttpClient.create()
                                  .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                                  .responseTimeout(Duration.ofSeconds(30))
                                  .doOnConnected(conn -> conn
                                      .addHandlerLast(new ReadTimeoutHandler(30))
                                      .addHandlerLast(new WriteTimeoutHandler(30)))
                                  .metrics(true); // Enable metrics

        return new ReactorClientHttpConnector(httpClient);
    }

    private void addDefaultHeaders(HttpHeaders headers) {
        headers.add(HttpHeaders.USER_AGENT, "MyApp/1.0");
        headers.add("X-Request-ID", UUID.randomUUID().toString());
    }

    private ExchangeFilterFunction loggingFilter() {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            logger.info("Request: {} {} {}",
                       request.method(), request.url(), request.headers());
            return Mono.just(request);
        });
    }

    private ExchangeFilterFunction errorHandlingFilter() {
        return ExchangeFilterFunction.ofResponseProcessor(response -> {
            if (response.statusCode().isError()) {
                return response.bodyToMono(String.class)
                             .flatMap(body -> Mono.error(
                                 new HttpClientException(response.statusCode(), body)));
            }
            return Mono.just(response);
        });
    }

    private ExchangeFilterFunction retryFilter() {
        return (request, next) -> next.exchange(request)
                                     .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                                                   .filter(this::isRetryableError));
    }

    private ExchangeFilterFunction circuitBreakerFilter() {
        return (request, next) ->
            CircuitBreaker.decorateSupplier(circuitBreaker, () -> next.exchange(request))
                         .get();
    }

    private ExchangeFilterFunction oauth2Filter() {
        return (request, next) ->
            getAccessToken()
                .map(token -> ClientRequest.from(request)
                                          .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                          .build())
                .flatMap(next::exchange);
    }

    private boolean isRetryableError(Throwable throwable) {
        return throwable instanceof ConnectException ||
               throwable instanceof TimeoutException ||
               (throwable instanceof WebClientResponseException wre &&
                wre.getStatusCode().is5xxServerError());
    }

    private Mono<String> getAccessToken() {
        return Mono.just("mock-token"); // Implement actual token retrieval
    }
}

@Service
public class ReactiveHttpService {
    private final WebClient webClient;
    private final WebClient authWebClient;

    public ReactiveHttpService(WebClient webClient,
                              @Qualifier("authWebClient") WebClient authWebClient) {
        this.webClient = webClient;
        this.authWebClient = authWebClient;
    }

    // Basic GET request
    public Mono<User> getUser(String userId) {
        return webClient.get()
                       .uri("/users/{id}", userId)
                       .retrieve()
                       .bodyToMono(User.class)
                       .timeout(Duration.ofSeconds(10))
                       .onErrorReturn(new User("default", "Default User"));
    }

    // GET with query parameters
    public Flux<User> getUsers(UserSearchCriteria criteria) {
        return webClient.get()
                       .uri(uriBuilder -> uriBuilder
                           .path("/users")
                           .queryParamIfPresent("name", Optional.ofNullable(criteria.getName()))
                           .queryParamIfPresent("age", Optional.ofNullable(criteria.getAge()))
                           .queryParamIfPresent("department", Optional.ofNullable(criteria.getDepartment()))
                           .build())
                       .retrieve()
                       .bodyToFlux(User.class)
                       .onErrorResume(error -> {
                           logger.error("Error fetching users", error);
                           return Flux.empty();
                       });
    }

    // POST request with request body
    public Mono<User> createUser(CreateUserRequest request) {
        return webClient.post()
                       .uri("/users")
                       .body(Mono.just(request), CreateUserRequest.class)
                       .retrieve()
                       .bodyToMono(User.class);
    }

    // PUT request with custom headers
    public Mono<User> updateUser(String userId, UpdateUserRequest request) {
        return webClient.put()
                       .uri("/users/{id}", userId)
                       .header("X-Update-Reason", "User profile update")
                       .body(Mono.just(request), UpdateUserRequest.class)
                       .retrieve()
                       .bodyToMono(User.class);
    }

    // DELETE request with response status handling
    public Mono<Void> deleteUser(String userId) {
        return webClient.delete()
                       .uri("/users/{id}", userId)
                       .exchangeToMono(response -> {
                           if (response.statusCode().equals(HttpStatus.NO_CONTENT)) {
                               return response.releaseBody();
                           } else if (response.statusCode().equals(HttpStatus.NOT_FOUND)) {
                               return Mono.error(new UserNotFoundException("User not found: " + userId));
                           } else {
                               return response.createException().flatMap(Mono::error);
                           }
                       });
    }

    // File upload
    public Mono<String> uploadFile(String userId, FilePart filePart) {
        var multipartData = MultipartBodyBuilder.create()
                                               .part("file", filePart)
                                               .part("userId", userId)
                                               .build();

        return webClient.post()
                       .uri("/users/{id}/upload", userId)
                       .contentType(MediaType.MULTIPART_FORM_DATA)
                       .body(BodyInserters.fromMultipartData(multipartData))
                       .retrieve()
                       .bodyToMono(String.class);
    }

    // Streaming response
    public Flux<UserEvent> streamUserEvents() {
        return webClient.get()
                       .uri("/users/events/stream")
                       .accept(MediaType.APPLICATION_NDJSON)
                       .retrieve()
                       .bodyToFlux(UserEvent.class)
                       .doOnNext(event -> logger.info("Received event: {}", event))
                       .onErrorContinue((error, item) ->
                           logger.error("Error processing event: {}", item, error));
    }

    // Parallel requests
    public Mono<UserProfile> getUserProfile(String userId) {
        var userMono = getUser(userId);
        var ordersMono = getUserOrders(userId);
        var preferencesMono = getUserPreferences(userId);

        return Mono.zip(userMono, ordersMono, preferencesMono)
                  .map(tuple -> new UserProfile(
                      tuple.getT1(),    // User
                      tuple.getT2(),    // Orders
                      tuple.getT3()     // Preferences
                  ));
    }

    // Sequential requests with dependency
    public Mono<OrderResult> processOrder(CreateOrderRequest request) {
        return validateUser(request.getUserId())
               .flatMap(user -> checkInventory(request.getItems()))
               .flatMap(inventory -> processPayment(request.getPayment()))
               .flatMap(payment -> createOrder(request, payment))
               .flatMap(this::sendConfirmation);
    }

    // Conditional requests
    public Mono<User> getUserWithFallback(String userId) {
        return getUser(userId)
               .onErrorResume(error -> {
                   logger.warn("Primary user service failed, trying backup", error);
                   return getUserFromBackup(userId);
               })
               .onErrorResume(error -> {
                   logger.warn("Backup service also failed, using cache", error);
                   return getUserFromCache(userId);
               })
               .switchIfEmpty(Mono.just(createDefaultUser(userId)));
    }

    // Batch requests with concurrency control
    public Flux<User> getUsersBatch(List<String> userIds) {
        return Flux.fromIterable(userIds)
                  .flatMap(this::getUser, 5) // Max 5 concurrent requests
                  .onErrorContinue((error, userId) ->
                      logger.error("Failed to fetch user: {}", userId, error));
    }

    // Request with custom exchange
    public Mono<ApiResponse<User>> getUserWithMetadata(String userId) {
        return webClient.get()
                       .uri("/users/{id}", userId)
                       .exchangeToMono(response -> {
                           var status = response.statusCode();
                           var headers = response.headers().asHttpHeaders();

                           if (status.is2xxSuccessful()) {
                               return response.bodyToMono(User.class)
                                            .map(user -> new ApiResponse<>(user, status, headers));
                           } else {
                               return response.createException()
                                            .flatMap(Mono::error);
                           }
                       });
    }

    // Authentication flow
    public Mono<String> authenticateAndGetToken(LoginRequest request) {
        return authWebClient.post()
                           .uri("/oauth/token")
                           .body(Mono.just(request), LoginRequest.class)
                           .retrieve()
                           .bodyToMono(TokenResponse.class)
                           .map(TokenResponse::getAccessToken)
                           .doOnNext(token -> logger.info("Authentication successful"))
                           .doOnError(error -> logger.error("Authentication failed", error));
    }

    // Health check
    public Mono<Boolean> isServiceHealthy(String serviceName) {
        return webClient.get()
                       .uri("/health")
                       .retrieve()
                       .bodyToMono(HealthResponse.class)
                       .map(health -> "UP".equals(health.getStatus()))
                       .timeout(Duration.ofSeconds(5))
                       .onErrorReturn(false);
    }

    // Helper methods
    private Mono<List<Order>> getUserOrders(String userId) {
        return webClient.get()
                       .uri("/users/{id}/orders", userId)
                       .retrieve()
                       .bodyToFlux(Order.class)
                       .collectList();
    }

    private Mono<UserPreferences> getUserPreferences(String userId) {
        return webClient.get()
                       .uri("/users/{id}/preferences", userId)
                       .retrieve()
                       .bodyToMono(UserPreferences.class);
    }

    private Mono<User> validateUser(String userId) {
        return getUser(userId)
               .filter(User::isActive)
               .switchIfEmpty(Mono.error(new UserNotActiveException("User is not active")));
    }

    private Mono<InventoryResult> checkInventory(List<OrderItem> items) {
        return webClient.post()
                       .uri("/inventory/check")
                       .body(Mono.just(items), new ParameterizedTypeReference<List<OrderItem>>() {})
                       .retrieve()
                       .bodyToMono(InventoryResult.class);
    }

    private Mono<PaymentResult> processPayment(PaymentRequest payment) {
        return webClient.post()
                       .uri("/payments/process")
                       .body(Mono.just(payment), PaymentRequest.class)
                       .retrieve()
                       .bodyToMono(PaymentResult.class);
    }

    private Mono<OrderResult> createOrder(CreateOrderRequest request, PaymentResult payment) {
        var orderRequest = new OrderRequest(request, payment);
        return webClient.post()
                       .uri("/orders")
                       .body(Mono.just(orderRequest), OrderRequest.class)
                       .retrieve()
                       .bodyToMono(OrderResult.class);
    }

    private Mono<OrderResult> sendConfirmation(OrderResult order) {
        return webClient.post()
                       .uri("/notifications/order-confirmation")
                       .body(Mono.just(order), OrderResult.class)
                       .retrieve()
                       .bodyToMono(String.class)
                       .thenReturn(order);
    }

    private Mono<User> getUserFromBackup(String userId) {
        return webClient.get()
                       .uri("http://backup-service/users/{id}", userId)
                       .retrieve()
                       .bodyToMono(User.class);
    }

    private Mono<User> getUserFromCache(String userId) {
        return Mono.fromCallable(() -> cacheService.getUser(userId))
                  .subscribeOn(Schedulers.boundedElastic());
    }

    private User createDefaultUser(String userId) {
        return new User(userId, "Unknown User");
    }
}
```

---

## 📡 **Server-Sent Events & WebSockets**

### **Server-Sent Events Implementation**

```java
/**
 * Server-Sent Events with Spring WebFlux
 */

@RestController
@RequestMapping("/api/events")
public class ServerSentEventsController {

    private final NotificationService notificationService;
    private final UserActivityService userActivityService;

    public ServerSentEventsController(NotificationService notificationService,
                                     UserActivityService userActivityService) {
        this.notificationService = notificationService;
        this.userActivityService = userActivityService;
    }

    // Basic SSE endpoint
    @GetMapping(value = "/notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<Notification>> streamNotifications() {
        return notificationService.getNotificationStream()
                                 .map(notification -> ServerSentEvent.builder(notification)
                                                                    .id(notification.getId())
                                                                    .event("notification")
                                                                    .retry(Duration.ofSeconds(5))
                                                                    .build())
                                 .doOnSubscribe(sub -> logger.info("Client subscribed to notifications"))
                                 .doOnCancel(() -> logger.info("Client unsubscribed from notifications"));
    }

    // SSE with user-specific filtering
    @GetMapping(value = "/user/{userId}/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<UserEvent>> streamUserEvents(@PathVariable String userId) {
        return userActivityService.getUserEventStream(userId)
                                 .filter(event -> event.getUserId().equals(userId))
                                 .map(event -> ServerSentEvent.builder(event)
                                                             .id(event.getId())
                                                             .event(event.getType())
                                                             .comment("User event for " + userId)
                                                             .build())
                                 .onErrorResume(error -> {
                                     logger.error("Error in user event stream", error);
                                     return Flux.just(ServerSentEvent.builder(
                                         new UserEvent("error", "Stream error occurred"))
                                         .event("error")
                                         .build());
                                 });
    }

    // SSE with heartbeat
    @GetMapping(value = "/heartbeat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> heartbeat() {
        var heartbeat = Flux.interval(Duration.ofSeconds(30))
                           .map(seq -> ServerSentEvent.builder("heartbeat-" + seq)
                                                     .event("heartbeat")
                                                     .build());

        var data = notificationService.getNotificationStream()
                                     .map(notification -> ServerSentEvent.builder(notification.getMessage())
                                                                        .event("data")
                                                                        .build());

        return Flux.merge(heartbeat, data);
    }

    // SSE with custom headers and CORS
    @GetMapping(value = "/public-events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @CrossOrigin(origins = "*")
    public ResponseEntity<Flux<ServerSentEvent<PublicEvent>>> streamPublicEvents() {
        var eventStream = notificationService.getPublicEventStream()
                                            .map(event -> ServerSentEvent.builder(event)
                                                                        .id(event.getId())
                                                                        .event("public")
                                                                        .build());

        return ResponseEntity.ok()
                           .header("Cache-Control", "no-cache")
                           .header("Connection", "keep-alive")
                           .body(eventStream);
    }

    // SSE with backpressure handling
    @GetMapping(value = "/high-volume", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> highVolumeStream() {
        return Flux.interval(Duration.ofMillis(10))
                  .onBackpressureBuffer(1000)
                  .map(seq -> "High volume event " + seq)
                  .map(data -> ServerSentEvent.builder(data)
                                             .event("high-volume")
                                             .build())
                  .doOnRequest(requested -> logger.info("Client requested {} events", requested));
    }
}

@Service
public class NotificationService {
    private final Sinks.Many<Notification> notificationSink =
        Sinks.many().multicast().directBestEffort();

    public Flux<Notification> getNotificationStream() {
        return notificationSink.asFlux()
                              .onBackpressureBuffer(Duration.ofSeconds(1), 1000)
                              .doOnNext(notification ->
                                  logger.info("Broadcasting notification: {}", notification.getId()));
    }

    public void sendNotification(Notification notification) {
        var result = notificationSink.tryEmitNext(notification);
        if (result.isFailure()) {
            logger.error("Failed to emit notification: {}", result);
        }
    }

    public Flux<PublicEvent> getPublicEventStream() {
        return Flux.interval(Duration.ofSeconds(5))
                  .map(seq -> new PublicEvent("public-" + seq, "Public event " + seq));
    }
}

// Client-side SSE consumption
@Component
public class SSEClient {

    private final WebClient webClient;

    public SSEClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Notification> subscribeToNotifications() {
        return webClient.get()
                       .uri("/api/events/notifications")
                       .accept(MediaType.TEXT_EVENT_STREAM)
                       .retrieve()
                       .bodyToFlux(String.class)
                       .map(this::parseSSEData)
                       .filter(Objects::nonNull)
                       .cast(Notification.class);
    }

    private Object parseSSEData(String sseData) {
        // Parse SSE format: data: {...}
        if (sseData.startsWith("data: ")) {
            var jsonData = sseData.substring(6);
            try {
                return JsonUtils.fromJson(jsonData, Notification.class);
            } catch (Exception e) {
                logger.error("Failed to parse SSE data: {}", jsonData, e);
                return null;
            }
        }
        return null;
    }
}
```

### **WebSocket Implementation**

```java
/**
 * WebSocket implementation with Spring WebFlux
 */

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOrigins("*")
                .withSockJS();
    }

    // Low-level WebSocket handler
    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        var map = Map.of("/websocket", new ReactiveWebSocketHandler());
        var handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setUrlMap(map);
        handlerMapping.setOrder(-1);
        return handlerMapping;
    }

    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter();
    }
}

@Component
public class ReactiveWebSocketHandler implements WebSocketHandler {
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        sessions.add(session);

        // Handle incoming messages
        var input = session.receive()
                          .map(WebSocketMessage::getPayloadAsText)
                          .doOnNext(message -> handleMessage(session, message))
                          .doOnError(error -> logger.error("WebSocket error", error))
                          .then();

        // Send outgoing messages
        var output = Flux.interval(Duration.ofSeconds(5))
                        .map(seq -> "Server message " + seq)
                        .map(session::textMessage)
                        .as(session::send);

        // Clean up on disconnect
        var cleanup = Mono.fromRunnable(() -> {
            sessions.remove(session);
            logger.info("WebSocket session disconnected: {}", session.getId());
        });

        return Mono.zip(input, output)
                  .then()
                  .doFinally(signal -> cleanup.subscribe());
    }

    private void handleMessage(WebSocketSession session, String message) {
        logger.info("Received message from {}: {}", session.getId(), message);

        try {
            var messageObj = JsonUtils.fromJson(message, WebSocketMessage.class);
            processMessage(session, messageObj);
        } catch (Exception e) {
            logger.error("Failed to process message", e);
            sendError(session, "Invalid message format");
        }
    }

    private void processMessage(WebSocketSession session, WebSocketMessage message) {
        switch (message.getType()) {
            case "chat" -> processChatMessage(session, message);
            case "ping" -> sendPong(session);
            case "subscribe" -> subscribeToChannel(session, message.getChannel());
            case "unsubscribe" -> unsubscribeFromChannel(session, message.getChannel());
            default -> sendError(session, "Unknown message type: " + message.getType());
        }
    }

    private void processChatMessage(WebSocketSession session, WebSocketMessage message) {
        var chatMessage = new ChatMessage(
            session.getId(),
            message.getContent(),
            Instant.now()
        );

        // Broadcast to all sessions
        broadcastMessage(chatMessage);
    }

    private void broadcastMessage(Object message) {
        var jsonMessage = JsonUtils.toJson(message);
        var webSocketMessage = sessions.iterator().next().textMessage(jsonMessage);

        sessions.forEach(session -> {
            if (session.isOpen()) {
                session.send(Mono.just(webSocketMessage))
                       .subscribe(
                           null,
                           error -> logger.error("Failed to send message to session {}",
                                                session.getId(), error)
                       );
            }
        });
    }

    private void sendPong(WebSocketSession session) {
        var pongMessage = session.textMessage("{\"type\":\"pong\"}");
        session.send(Mono.just(pongMessage)).subscribe();
    }

    private void sendError(WebSocketSession session, String error) {
        var errorMessage = session.textMessage(
            "{\"type\":\"error\",\"message\":\"" + error + "\"}");
        session.send(Mono.just(errorMessage)).subscribe();
    }
}

// STOMP WebSocket controller
@Controller
public class WebSocketStompController {

    @MessageMapping("/chat.send")
    @SendTo("/topic/chat")
    public Mono<ChatMessage> sendMessage(ChatMessage message) {
        return Mono.just(new ChatMessage(
            message.getUser(),
            message.getContent(),
            Instant.now()
        ));
    }

    @MessageMapping("/chat.join")
    @SendTo("/topic/chat")
    public Mono<ChatMessage> joinChat(@Payload ChatMessage message,
                                     SimpMessageHeaderAccessor headerAccessor) {
        headerAccessor.getSessionAttributes().put("username", message.getUser());
        return Mono.just(new ChatMessage(
            "System",
            message.getUser() + " joined the chat",
            Instant.now()
        ));
    }

    @MessageMapping("/user.activity")
    @SendToUser("/queue/activity")
    public Mono<UserActivity> trackUserActivity(@Payload UserActivity activity) {
        return userActivityService.recordActivity(activity)
                                 .then(Mono.just(activity));
    }

    @SubscribeMapping("/topic/notifications")
    public Flux<Notification> subscribeToNotifications() {
        return notificationService.getNotificationStream()
                                 .take(Duration.ofMinutes(30)); // Auto-unsubscribe after 30 minutes
    }
}

// WebSocket event listeners
@Component
public class WebSocketEventListener {

    @EventListener
    public void handleWebSocketConnect(SessionConnectedEvent event) {
        var username = getUsernameFromSession(event);
        logger.info("User {} connected via WebSocket", username);
    }

    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        var username = getUsernameFromSession(event);
        logger.info("User {} disconnected from WebSocket", username);
    }

    private String getUsernameFromSession(AbstractSubProtocolEvent event) {
        var headers = SimpMessageHeaderAccessor.wrap(event.getMessage());
        return (String) headers.getSessionAttributes().get("username");
    }
}

// WebSocket security
@Configuration
public class WebSocketSecurityConfig {

    @Bean
    public AuthorizationManager<Message<?>> messageAuthorizationManager() {
        return (authentication, context) -> {
            var message = context.getObject();
            var destination = SimpMessageHeaderAccessor.getDestination(message.getHeaders());

            if (destination != null && destination.startsWith("/user")) {
                return AuthorizationDecision.of(authentication.get().isAuthenticated());
            }

            return AuthorizationDecision.of(true);
        };
    }
}
```

---

## ⚠️ **Error Handling & Resilience**

### **Reactive Error Handling Patterns**

```java
/**
 * Comprehensive error handling in reactive applications
 */

@Component
public class ReactiveErrorHandling {

    // Basic error handling operators
    public Mono<String> basicErrorHandling() {
        return callExternalService()
               .onErrorReturn("fallback_value")
               .onErrorResume(error -> {
                   logger.error("Service call failed", error);
                   return Mono.just("recovered_value");
               })
               .onErrorMap(IOException.class, ex ->
                   new ServiceUnavailableException("External service error", ex))
               .doOnError(error -> logger.error("Final error", error));
    }

    // Error filtering and continuation
    public Flux<String> errorContinuation() {
        return Flux.range(1, 10)
                  .map(i -> {
                      if (i == 5) throw new RuntimeException("Error at " + i);
                      return "item-" + i;
                  })
                  .onErrorContinue((error, item) -> {
                      logger.error("Error processing item {}", item, error);
                      // Continue with next item
                  });
    }

    // Retry strategies
    public Mono<String> retryStrategies() {
        return callExternalService()
               // Simple retry
               .retry(3)

               // Retry with delay
               .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))

               // Conditional retry
               .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                             .filter(this::isRetryableError)
                             .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) ->
                                 new ServiceException("Service unavailable after retries")));
    }

    // Circuit breaker pattern
    @Component
    public static class CircuitBreakerService {
        private final CircuitBreaker circuitBreaker;

        public CircuitBreakerService() {
            this.circuitBreaker = CircuitBreaker.ofDefaults("externalService");
            circuitBreaker.getEventPublisher()
                         .onStateTransition(event ->
                             logger.info("Circuit breaker state transition: {}", event));
        }

        public Mono<String> callWithCircuitBreaker() {
            return Mono.fromSupplier(CircuitBreaker.decorateSupplier(circuitBreaker, () ->
                    callExternalServiceBlocking()))
                      .subscribeOn(Schedulers.boundedElastic())
                      .onErrorResume(CallNotPermittedException.class, ex ->
                          Mono.just("Circuit breaker is open"));
        }
    }

    // Timeout handling
    public Mono<String> timeoutHandling() {
        return callSlowService()
               .timeout(Duration.ofSeconds(5))
               .onErrorResume(TimeoutException.class, ex ->
                   Mono.just("timeout_fallback"))
               .doOnSuccess(value -> logger.info("Service call completed: {}", value))
               .doOnError(error -> logger.error("Service call failed", error));
    }

    // Bulkhead pattern
    @Component
    public static class BulkheadService {
        private final Scheduler criticalScheduler;
        private final Scheduler nonCriticalScheduler;

        public BulkheadService() {
            this.criticalScheduler = Schedulers.newBoundedElastic(
                5, 100, "critical-pool");
            this.nonCriticalScheduler = Schedulers.newBoundedElastic(
                2, 50, "non-critical-pool");
        }

        public Mono<String> criticalOperation() {
            return Mono.fromCallable(this::performCriticalWork)
                      .subscribeOn(criticalScheduler)
                      .timeout(Duration.ofSeconds(10));
        }

        public Mono<String> nonCriticalOperation() {
            return Mono.fromCallable(this::performNonCriticalWork)
                      .subscribeOn(nonCriticalScheduler)
                      .timeout(Duration.ofSeconds(30));
        }

        private String performCriticalWork() { return "critical_result"; }
        private String performNonCriticalWork() { return "non_critical_result"; }
    }

    // Fallback chains
    public Mono<User> fallbackChain(String userId) {
        return primaryUserService.getUser(userId)
               .onErrorResume(error -> {
                   logger.warn("Primary service failed, trying secondary", error);
                   return secondaryUserService.getUser(userId);
               })
               .onErrorResume(error -> {
                   logger.warn("Secondary service failed, trying cache", error);
                   return getCachedUser(userId);
               })
               .onErrorResume(error -> {
                   logger.warn("Cache failed, creating default user", error);
                   return Mono.just(createDefaultUser(userId));
               });
    }

    // Error metrics and monitoring
    @Component
    public static class ErrorMetricsService {
        private final Counter errorCounter;
        private final Timer serviceTimer;

        public ErrorMetricsService(MeterRegistry meterRegistry) {
            this.errorCounter = Counter.builder("service.errors")
                                     .description("Service error count")
                                     .register(meterRegistry);
            this.serviceTimer = Timer.builder("service.calls")
                                   .description("Service call duration")
                                   .register(meterRegistry);
        }

        public <T> Mono<T> instrumentServiceCall(Mono<T> serviceCall, String serviceName) {
            return serviceCall
                   .doOnError(error -> {
                       errorCounter.increment(
                           Tags.of(
                               Tag.of("service", serviceName),
                               Tag.of("error.type", error.getClass().getSimpleName())
                           ));
                   })
                   .transformDeferred(call -> Timer.Sample.start(serviceTimer)
                                                         .stop(call));
        }
    }

    // Global error handling
    @Component
    public static class GlobalErrorHandler {

        public <T> Mono<T> handleGlobalErrors(Mono<T> operation) {
            return operation
                   .onErrorResume(ValidationException.class, this::handleValidationError)
                   .onErrorResume(AuthenticationException.class, this::handleAuthError)
                   .onErrorResume(AuthorizationException.class, this::handleAuthzError)
                   .onErrorResume(ServiceException.class, this::handleServiceError)
                   .onErrorResume(Exception.class, this::handleGenericError);
        }

        private <T> Mono<T> handleValidationError(ValidationException ex) {
            logger.warn("Validation error: {}", ex.getMessage());
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage()));
        }

        private <T> Mono<T> handleAuthError(AuthenticationException ex) {
            logger.warn("Authentication error: {}", ex.getMessage());
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
        }

        private <T> Mono<T> handleAuthzError(AuthorizationException ex) {
            logger.warn("Authorization error: {}", ex.getMessage());
            return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));
        }

        private <T> Mono<T> handleServiceError(ServiceException ex) {
            logger.error("Service error", ex);
            return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Service temporarily unavailable"));
        }

        private <T> Mono<T> handleGenericError(Exception ex) {
            logger.error("Unexpected error", ex);
            return Mono.error(new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error"));
        }
    }

    // Dead letter queue pattern
    @Component
    public static class DeadLetterQueueHandler {

        public <T> Mono<T> withDeadLetterQueue(Mono<T> operation, String operationName) {
            return operation
                   .onErrorResume(error -> {
                       if (isFatalError(error)) {
                           return sendToDeadLetterQueue(operationName, error)
                                  .then(Mono.error(error));
                       }
                       return Mono.error(error);
                   });
        }

        private boolean isFatalError(Throwable error) {
            return error instanceof SerializationException ||
                   error instanceof MessageCorruptedException ||
                   error instanceof UnrecoverableException;
        }

        private Mono<Void> sendToDeadLetterQueue(String operationName, Throwable error) {
            var dlqMessage = new DeadLetterMessage(
                operationName,
                error.getMessage(),
                Instant.now()
            );

            return Mono.fromRunnable(() -> {
                // Send to DLQ (e.g., Kafka, RabbitMQ, database)
                logger.error("Sending to DLQ: operation={}, error={}", operationName, error.getMessage());
            });
        }
    }

    // Helper methods
    private Mono<String> callExternalService() {
        return Mono.fromCallable(() -> {
            if (Math.random() < 0.3) {
                throw new IOException("Network error");
            }
            return "service_response";
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private Mono<String> callSlowService() {
        return Mono.fromCallable(() -> {
            try { Thread.sleep(10000); } catch (InterruptedException e) {}
            return "slow_response";
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private String callExternalServiceBlocking() {
        if (Math.random() < 0.3) {
            throw new RuntimeException("Service error");
        }
        return "blocking_response";
    }

    private boolean isRetryableError(Throwable error) {
        return error instanceof IOException ||
               error instanceof TimeoutException ||
               (error instanceof ServiceException se && se.isRetryable());
    }

    private Mono<User> getCachedUser(String userId) {
        return Mono.fromCallable(() -> cacheService.getUser(userId))
                  .subscribeOn(Schedulers.boundedElastic());
    }

    private User createDefaultUser(String userId) {
        return new User(userId, "Unknown User");
    }
}

// Custom exceptions
public class ServiceException extends RuntimeException {
    private final boolean retryable;

    public ServiceException(String message, boolean retryable) {
        super(message);
        this.retryable = retryable;
    }

    public boolean isRetryable() { return retryable; }
}

public class ValidationException extends RuntimeException {
    public ValidationException(String message) { super(message); }
}

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) { super(message); }
}

public class AuthorizationException extends RuntimeException {
    public AuthorizationException(String message) { super(message); }
}

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message, Throwable cause) { super(message, cause); }
}

public class UnrecoverableException extends RuntimeException {
    public UnrecoverableException(String message) { super(message); }
}

public class MessageCorruptedException extends RuntimeException {
    public MessageCorruptedException(String message) { super(message); }
}

public record DeadLetterMessage(String operation, String error, Instant timestamp) {}
```

---

## 🧪 **Performance & Testing**

### **Reactive Performance Optimization**

```java
/**
 * Performance optimization techniques for reactive applications
 */

@Component
public class ReactivePerformanceOptimization {

    // Scheduler optimization
    public void schedulerOptimization() {
        // CPU-intensive work - use parallel scheduler
        Flux.range(1, 1000000)
            .parallel(Runtime.getRuntime().availableProcessors())
            .runOn(Schedulers.parallel())
            .map(this::cpuIntensiveWork)
            .sequential()
            .subscribe();

        // I/O work - use bounded elastic scheduler
        Flux.range(1, 1000)
            .flatMap(i -> Mono.fromCallable(() -> ioOperation(i))
                             .subscribeOn(Schedulers.boundedElastic()), 10)
            .subscribe();

        // Custom scheduler for specific workloads
        var customScheduler = Schedulers.fromExecutor(
            Executors.newFixedThreadPool(4, r -> {
                var thread = new Thread(r, "custom-scheduler");
                thread.setDaemon(true);
                return thread;
            }));

        Flux.range(1, 100)
            .subscribeOn(customScheduler)
            .subscribe();
    }

    // Batching optimization
    public Flux<ProcessedBatch> batchProcessing(Flux<String> input) {
        return input
               .buffer(100, Duration.ofSeconds(5))  // Batch by size or time
               .filter(batch -> !batch.isEmpty())
               .flatMap(this::processBatch, 3)      // Max 3 concurrent batches
               .onErrorContinue((error, batch) ->
                   logger.error("Batch processing failed", error));
    }

    // Caching optimization
    @Component
    public static class CachedReactiveService {
        private final Cache<String, Mono<User>> userCache;

        public CachedReactiveService() {
            this.userCache = Caffeine.newBuilder()
                                   .maximumSize(1000)
                                   .expireAfterWrite(Duration.ofMinutes(10))
                                   .build();
        }

        public Mono<User> getUser(String userId) {
            return userCache.get(userId, key ->
                fetchUserFromDatabase(key)
                    .cache(Duration.ofMinutes(5))  // Cache the Mono itself
            );
        }

        private Mono<User> fetchUserFromDatabase(String userId) {
            return Mono.fromCallable(() -> {
                // Database call
                Thread.sleep(100);
                return new User(userId, "User " + userId);
            }).subscribeOn(Schedulers.boundedElastic());
        }
    }

    // Connection pooling
    @Bean
    public WebClient optimizedWebClient() {
        var connectionProvider = ConnectionProvider.builder("custom")
                                                  .maxConnections(100)
                                                  .maxIdleTime(Duration.ofSeconds(20))
                                                  .maxLifeTime(Duration.ofSeconds(60))
                                                  .pendingAcquireTimeout(Duration.ofSeconds(10))
                                                  .metrics(true)
                                                  .build();

        var httpClient = HttpClient.create(connectionProvider)
                                  .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                                  .responseTimeout(Duration.ofSeconds(30))
                                  .keepAlive(true);

        return WebClient.builder()
                       .clientConnector(new ReactorClientHttpConnector(httpClient))
                       .build();
    }

    // Memory optimization
    public Flux<String> memoryOptimizedProcessing(Flux<LargeObject> input) {
        return input
               .publishOn(Schedulers.boundedElastic(), 1)  // Small buffer
               .map(this::extractImportantData)            // Extract only needed data
               .doOnNext(data -> {
                   // Process immediately, don't accumulate
                   processData(data);
               })
               .onBackpressureBuffer(10)                   // Small buffer
               .share();                                   // Share among subscribers
    }

    // Parallel processing optimization
    public Flux<String> parallelProcessing(Flux<String> input) {
        return input
               .parallel(4)                              // Split into 4 rails
               .runOn(Schedulers.parallel())            // Each rail on different thread
               .map(this::expensiveTransformation)       // CPU-intensive work
               .filter(Objects::nonNull)                // Filter in parallel
               .sequential()                            // Merge back
               .publishOn(Schedulers.single());         // Single thread for downstream
    }

    // Database optimization
    @Repository
    public static class OptimizedReactiveRepository {
        private final DatabaseClient databaseClient;

        public OptimizedReactiveRepository(DatabaseClient databaseClient) {
            this.databaseClient = databaseClient;
        }

        // Batch operations
        public Mono<Integer> batchInsert(List<User> users) {
            if (users.isEmpty()) return Mono.just(0);

            var sql = "INSERT INTO users (id, name, email) VALUES ($1, $2, $3)";

            return Flux.fromIterable(users)
                      .flatMap(user -> databaseClient.sql(sql)
                                                   .bind(0, user.getId())
                                                   .bind(1, user.getName())
                                                   .bind(2, user.getEmail())
                                                   .fetch()
                                                   .rowsUpdated())
                      .reduce(Integer::sum);
        }

        // Streaming results
        public Flux<User> streamAllUsers() {
            return databaseClient.sql("SELECT * FROM users")
                                .fetch()
                                .all()
                                .map(this::mapToUser)
                                .publishOn(Schedulers.boundedElastic(), 100); // Small buffer
        }

        private User mapToUser(Map<String, Object> row) {
            return new User(
                (String) row.get("id"),
                (String) row.get("name"),
                (String) row.get("email")
            );
        }
    }

    // Monitoring and metrics
    @Component
    public static class ReactiveMetricsCollector {
        private final Timer processingTimer;
        private final Counter errorCounter;
        private final Gauge activeSubscriptions;

        public ReactiveMetricsCollector(MeterRegistry meterRegistry) {
            this.processingTimer = Timer.builder("reactive.processing.time")
                                      .description("Processing time for reactive operations")
                                      .register(meterRegistry);

            this.errorCounter = Counter.builder("reactive.errors")
                                     .description("Reactive operation errors")
                                     .register(meterRegistry);

            this.activeSubscriptions = Gauge.builder("reactive.subscriptions.active")
                                           .description("Active reactive subscriptions")
                                           .register(meterRegistry, this, obj -> subscriptionCount.get());
        }

        private final AtomicLong subscriptionCount = new AtomicLong(0);

        public <T> Mono<T> instrumentMono(Mono<T> mono, String operation) {
            return mono
                   .doOnSubscribe(sub -> subscriptionCount.incrementAndGet())
                   .doOnTerminate(() -> subscriptionCount.decrementAndGet())
                   .doOnError(error -> errorCounter.increment(
                       Tags.of(Tag.of("operation", operation))))
                   .transformDeferred(Timer.Sample.start(processingTimer)::stop);
        }

        public <T> Flux<T> instrumentFlux(Flux<T> flux, String operation) {
            return flux
                   .doOnSubscribe(sub -> subscriptionCount.incrementAndGet())
                   .doOnTerminate(() -> subscriptionCount.decrementAndGet())
                   .doOnError(error -> errorCounter.increment(
                       Tags.of(Tag.of("operation", operation))));
        }
    }

    // Helper methods
    private String cpuIntensiveWork(Integer input) {
        // Simulate CPU work
        var result = 0;
        for (int i = 0; i < 1000000; i++) {
            result += i * input;
        }
        return "processed-" + input + "-" + result;
    }

    private String ioOperation(Integer input) {
        try { Thread.sleep(10); } catch (InterruptedException e) {}
        return "io-result-" + input;
    }

    private Mono<ProcessedBatch> processBatch(List<String> batch) {
        return Mono.fromCallable(() -> {
            // Process batch
            return new ProcessedBatch(batch.size(), "processed");
        }).subscribeOn(Schedulers.boundedElastic());
    }

    private String extractImportantData(LargeObject obj) {
        return obj.getImportantField();
    }

    private void processData(String data) {
        // Processing logic
    }

    private String expensiveTransformation(String input) {
        // Expensive CPU work
        return input.toUpperCase().repeat(3);
    }

    public record ProcessedBatch(int size, String status) {}
    public record LargeObject(String importantField, byte[] largeData) {}
}
```

### **Reactive Testing**

```java
/**
 * Comprehensive testing strategies for reactive applications
 */

@ExtendWith(MockitoExtension.class)
class ReactiveServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ReactiveUserService userService;

    // Basic Mono testing
    @Test
    void shouldReturnUserWhenExists() {
        // Given
        var userId = "user123";
        var user = new User(userId, "John Doe", "john@example.com");
        when(userRepository.findById(userId)).thenReturn(Mono.just(user));

        // When
        var result = userService.getUser(userId);

        // Then
        StepVerifier.create(result)
                   .expectNext(user)
                   .verifyComplete();
    }

    @Test
    void shouldReturnEmptyWhenUserNotExists() {
        // Given
        var userId = "nonexistent";
        when(userRepository.findById(userId)).thenReturn(Mono.empty());

        // When
        var result = userService.getUser(userId);

        // Then
        StepVerifier.create(result)
                   .verifyComplete();
    }

    @Test
    void shouldPropagateErrorWhenRepositoryFails() {
        // Given
        var userId = "user123";
        when(userRepository.findById(userId))
            .thenReturn(Mono.error(new RuntimeException("Database error")));

        // When
        var result = userService.getUser(userId);

        // Then
        StepVerifier.create(result)
                   .expectError(RuntimeException.class)
                   .verify();
    }

    // Flux testing
    @Test
    void shouldReturnAllUsers() {
        // Given
        var users = List.of(
            new User("1", "Alice", "alice@example.com"),
            new User("2", "Bob", "bob@example.com")
        );
        when(userRepository.findAll()).thenReturn(Flux.fromIterable(users));

        // When
        var result = userService.getAllUsers();

        // Then
        StepVerifier.create(result)
                   .expectNext(users.get(0))
                   .expectNext(users.get(1))
                   .verifyComplete();
    }

    @Test
    void shouldHandleEmptyUserList() {
        // Given
        when(userRepository.findAll()).thenReturn(Flux.empty());

        // When
        var result = userService.getAllUsers();

        // Then
        StepVerifier.create(result)
                   .expectNextCount(0)
                   .verifyComplete();
    }

    // Testing with virtual time
    @Test
    void shouldDelayProcessing() {
        StepVerifier.withVirtualTime(() -> userService.getDelayedUser("user123"))
                   .expectSubscription()
                   .expectNoEvent(Duration.ofSeconds(4))
                   .thenAwait(Duration.ofSeconds(1))
                   .expectNextMatches(user -> "user123".equals(user.getId()))
                   .verifyComplete();
    }

    // Testing backpressure
    @Test
    void shouldHandleBackpressure() {
        // Given
        var publisher = TestPublisher.<String>create();

        // When
        var result = userService.processStream(publisher.flux());

        // Then
        StepVerifier.create(result, 0) // Don't request initially
                   .then(() -> publisher.next("item1"))
                   .expectNoEvent(Duration.ofMillis(100)) // Shouldn't receive anything
                   .thenRequest(1)
                   .expectNext("ITEM1")
                   .then(() -> publisher.next("item2"))
                   .expectNoEvent(Duration.ofMillis(100))
                   .thenRequest(1)
                   .expectNext("ITEM2")
                   .then(publisher::complete)
                   .verifyComplete();
    }

    // Testing error scenarios
    @Test
    void shouldRetryOnTransientErrors() {
        // Given
        when(userRepository.findById("user123"))
            .thenReturn(Mono.error(new IOException("Transient error")))
            .thenReturn(Mono.error(new IOException("Transient error")))
            .thenReturn(Mono.just(new User("user123", "John", "john@example.com")));

        // When
        var result = userService.getUserWithRetry("user123");

        // Then
        StepVerifier.create(result)
                   .expectNextMatches(user -> "user123".equals(user.getId()))
                   .verifyComplete();

        verify(userRepository, times(3)).findById("user123");
    }

    // Testing timeout
    @Test
    void shouldTimeoutSlowOperations() {
        // Given
        when(userRepository.findById("user123"))
            .thenReturn(Mono.delay(Duration.ofSeconds(10))
                           .map(i -> new User("user123", "John", "john@example.com")));

        // When
        var result = userService.getUserWithTimeout("user123");

        // Then
        StepVerifier.create(result)
                   .expectError(TimeoutException.class)
                   .verify(Duration.ofSeconds(6));
    }

    // Testing context propagation
    @Test
    void shouldPropagateContext() {
        // Given
        var userId = "user123";
        var correlationId = "corr-123";
        when(userRepository.findById(userId))
            .thenReturn(Mono.just(new User(userId, "John", "john@example.com")));

        // When
        var result = userService.getUser(userId)
                               .contextWrite(Context.of("correlationId", correlationId));

        // Then
        StepVerifier.create(result)
                   .expectAccessibleContext()
                   .contains("correlationId", correlationId)
                   .then()
                   .expectNextMatches(user -> userId.equals(user.getId()))
                   .verifyComplete();
    }
}

// Integration testing
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
class ReactiveIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReactiveUserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll().block();
    }

    @Test
    @Order(1)
    void shouldCreateUser() {
        // Given
        var createRequest = new CreateUserRequest("John Doe", "john@example.com");

        // When & Then
        webTestClient.post()
                    .uri("/api/users")
                    .bodyValue(createRequest)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(User.class)
                    .value(user -> {
                        assertThat(user.getName()).isEqualTo("John Doe");
                        assertThat(user.getEmail()).isEqualTo("john@example.com");
                        assertThat(user.getId()).isNotNull();
                    });
    }

    @Test
    @Order(2)
    void shouldGetUser() {
        // Given
        var user = userRepository.save(new User("test123", "Jane Doe", "jane@example.com"))
                                .block();

        // When & Then
        webTestClient.get()
                    .uri("/api/users/{id}", user.getId())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(User.class)
                    .isEqualTo(user);
    }

    @Test
    void shouldReturnNotFoundForNonexistentUser() {
        webTestClient.get()
                    .uri("/api/users/nonexistent")
                    .exchange()
                    .expectStatus().isNotFound();
    }

    @Test
    void shouldStreamUsers() {
        // Given
        var users = List.of(
            new User("1", "Alice", "alice@example.com"),
            new User("2", "Bob", "bob@example.com"),
            new User("3", "Charlie", "charlie@example.com")
        );
        userRepository.saveAll(users).blockLast();

        // When & Then
        webTestClient.get()
                    .uri("/api/users/stream")
                    .accept(MediaType.APPLICATION_NDJSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectHeader().contentType(MediaType.APPLICATION_NDJSON)
                    .expectBodyList(User.class)
                    .hasSize(3);
    }

    @Test
    void shouldHandleServerSentEvents() {
        var eventFlux = webTestClient.get()
                                   .uri("/api/events/notifications")
                                   .accept(MediaType.TEXT_EVENT_STREAM)
                                   .exchange()
                                   .expectStatus().isOk()
                                   .returnResult(String.class)
                                   .getResponseBody();

        StepVerifier.create(eventFlux.take(3))
                   .expectNextMatches(data -> data.contains("data:"))
                   .expectNextMatches(data -> data.contains("data:"))
                   .expectNextMatches(data -> data.contains("data:"))
                   .thenCancel()
                   .verify(Duration.ofSeconds(10));
    }
}

// Custom test utilities
@TestConfiguration
public class ReactiveTestConfig {

    @Bean
    @Primary
    public ReactiveUserRepository mockUserRepository() {
        return Mockito.mock(ReactiveUserRepository.class);
    }

    @Bean
    public TestWebClient testWebClient() {
        return TestWebClient.create();
    }
}

// Performance testing
@Component
public class ReactivePerformanceTest {

    @Test
    void shouldHandleHighThroughput() {
        var userService = new ReactiveUserService();
        var startTime = System.currentTimeMillis();
        var requestCount = 10000;

        var result = Flux.range(1, requestCount)
                        .flatMap(i -> userService.getUser("user" + i), 100) // 100 concurrent
                        .collectList()
                        .block(Duration.ofSeconds(30));

        var endTime = System.currentTimeMillis();
        var duration = endTime - startTime;
        var throughput = requestCount * 1000.0 / duration;

        assertThat(result).hasSize(requestCount);
        assertThat(throughput).isGreaterThan(1000); // More than 1000 req/sec

        logger.info("Processed {} requests in {}ms, throughput: {} req/sec",
                   requestCount, duration, throughput);
    }

    @Test
    void shouldMaintainLowLatency() {
        var userService = new ReactiveUserService();
        var latencies = new ArrayList<Long>();

        for (int i = 0; i < 1000; i++) {
            var start = System.nanoTime();

            userService.getUser("user" + i).block();

            var end = System.nanoTime();
            latencies.add((end - start) / 1_000_000); // Convert to milliseconds
        }

        var avgLatency = latencies.stream().mapToLong(Long::longValue).average().orElse(0);
        var p95Latency = latencies.stream().sorted().skip((long)(latencies.size() * 0.95)).findFirst().orElse(0L);

        assertThat(avgLatency).isLessThan(10.0); // Average < 10ms
        assertThat(p95Latency).isLessThan(50.0); // P95 < 50ms

        logger.info("Average latency: {}ms, P95 latency: {}ms", avgLatency, p95Latency);
    }
}

// Test data builders
public class TestDataBuilder {

    public static User.Builder aUser() {
        return User.builder()
                  .id(UUID.randomUUID().toString())
                  .name("Test User")
                  .email("test@example.com")
                  .active(true);
    }

    public static CreateUserRequest.Builder aCreateUserRequest() {
        return CreateUserRequest.builder()
                               .name("Test User")
                               .email("test@example.com");
    }

    public static Flux<User> manyUsers(int count) {
        return Flux.range(1, count)
                  .map(i -> aUser()
                      .id("user" + i)
                      .name("User " + i)
                      .email("user" + i + "@example.com")
                      .build());
    }
}
```

---

## ⚖️ **Reactive vs Traditional Comparison**

### **Performance and Scalability Comparison**

```java
/**
 * Comprehensive comparison between reactive and traditional approaches
 */

// Traditional blocking approach
@RestController
@RequestMapping("/api/traditional")
public class TraditionalController {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;

    public TraditionalController(RestTemplate restTemplate, UserRepository userRepository) {
        this.restTemplate = restTemplate;
        this.userRepository = userRepository;
    }

    // Traditional blocking implementation
    @GetMapping("/users/{id}/profile")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable String id) {
        try {
            // Each call blocks the thread
            var user = userRepository.findById(id);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var orders = restTemplate.getForObject("/orders/user/" + id, Order[].class);
            var preferences = restTemplate.getForObject("/preferences/user/" + id, UserPreferences.class);
            var notifications = restTemplate.getForObject("/notifications/user/" + id, Notification[].class);

            var profile = new UserProfile(user.get(),
                                        Arrays.asList(orders != null ? orders : new Order[0]),
                                        preferences,
                                        Arrays.asList(notifications != null ? notifications : new Notification[0]));

            return ResponseEntity.ok(profile);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Traditional streaming (limited)
    @GetMapping(value = "/users/export", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void exportUsers(HttpServletResponse response) throws IOException {
        response.setHeader("Content-Disposition", "attachment; filename=users.csv");

        try (var writer = new PrintWriter(response.getOutputStream())) {
            writer.println("id,name,email");

            // Process in chunks to avoid memory issues
            var page = 0;
            var size = 1000;
            Page<User> users;

            do {
                users = userRepository.findAll(PageRequest.of(page, size));
                for (var user : users.getContent()) {
                    writer.printf("%s,%s,%s%n", user.getId(), user.getName(), user.getEmail());
                    writer.flush(); // Force write
                }
                page++;
            } while (users.hasNext());
        }
    }

    // Traditional error handling
    @GetMapping("/users/{id}/orders")
    public ResponseEntity<List<Order>> getUserOrders(@PathVariable String id) {
        try {
            var user = userRepository.findById(id);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var orders = restTemplate.getForObject("/orders/user/" + id, Order[].class);
            return ResponseEntity.ok(Arrays.asList(orders != null ? orders : new Order[0]));

        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.notFound().build();
        } catch (HttpServerErrorException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

// Reactive non-blocking approach
@RestController
@RequestMapping("/api/reactive")
public class ReactiveController {

    private final WebClient webClient;
    private final ReactiveUserRepository userRepository;

    public ReactiveController(WebClient webClient, ReactiveUserRepository userRepository) {
        this.webClient = webClient;
        this.userRepository = userRepository;
    }

    // Reactive non-blocking implementation
    @GetMapping("/users/{id}/profile")
    public Mono<ResponseEntity<UserProfile>> getUserProfile(@PathVariable String id) {
        return userRepository.findById(id)
                           .switchIfEmpty(Mono.just(ResponseEntity.notFound().<UserProfile>build()))
                           .flatMap(user -> {
                               // Parallel execution of multiple calls
                               var ordersMono = webClient.get()
                                                        .uri("/orders/user/" + id)
                                                        .retrieve()
                                                        .bodyToFlux(Order.class)
                                                        .collectList()
                                                        .onErrorReturn(List.of());

                               var preferencesMono = webClient.get()
                                                            .uri("/preferences/user/" + id)
                                                            .retrieve()
                                                            .bodyToMono(UserPreferences.class)
                                                            .onErrorReturn(new UserPreferences());

                               var notificationsMono = webClient.get()
                                                               .uri("/notifications/user/" + id)
                                                               .retrieve()
                                                               .bodyToFlux(Notification.class)
                                                               .collectList()
                                                               .onErrorReturn(List.of());

                               return Mono.zip(ordersMono, preferencesMono, notificationsMono)
                                         .map(tuple -> new UserProfile(user,
                                                                     tuple.getT1(),
                                                                     tuple.getT2(),
                                                                     tuple.getT3()))
                                         .map(ResponseEntity::ok);
                           });
    }

    // Reactive streaming
    @GetMapping(value = "/users/export", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<String> exportUsers() {
        return userRepository.findAll()
                           .map(user -> String.format("%s,%s,%s",
                                                     user.getId(), user.getName(), user.getEmail()))
                           .startWith("id,name,email") // Header
                           .onErrorContinue((error, item) ->
                               logger.error("Error processing user: {}", item, error));
    }

    // Reactive error handling
    @GetMapping("/users/{id}/orders")
    public Mono<ResponseEntity<List<Order>>> getUserOrders(@PathVariable String id) {
        return userRepository.findById(id)
                           .switchIfEmpty(Mono.just(ResponseEntity.notFound().<List<Order>>build()))
                           .flatMap(user -> webClient.get()
                                                   .uri("/orders/user/" + id)
                                                   .retrieve()
                                                   .onStatus(HttpStatus::is4xxClientError,
                                                           response -> Mono.just(new UserNotFoundException()))
                                                   .onStatus(HttpStatus::is5xxServerError,
                                                           response -> Mono.just(new ServiceUnavailableException()))
                                                   .bodyToFlux(Order.class)
                                                   .collectList()
                                                   .map(ResponseEntity::ok)
                                                   .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()));
    }
}

// Performance comparison test
@Component
public class PerformanceComparison {

    // Traditional approach benchmark
    public void traditionalApproachBenchmark() {
        var executor = Executors.newFixedThreadPool(200); // Thread pool
        var latch = new CountDownLatch(1000);
        var startTime = System.currentTimeMillis();

        for (int i = 0; i < 1000; i++) {
            var requestId = i;
            executor.submit(() -> {
                try {
                    // Simulate blocking I/O
                    Thread.sleep(100);
                    processTraditionalRequest(requestId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
            var endTime = System.currentTimeMillis();
            logger.info("Traditional approach: {}ms, Threads: 200", endTime - startTime);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }

    // Reactive approach benchmark
    public void reactiveApproachBenchmark() {
        var startTime = System.currentTimeMillis();

        Flux.range(1, 1000)
            .flatMap(this::processReactiveRequest, 10) // Only 10 concurrent
            .blockLast();

        var endTime = System.currentTimeMillis();
        logger.info("Reactive approach: {}ms, Concurrency: 10", endTime - startTime);
    }

    // Memory usage comparison
    public void memoryUsageComparison() {
        var runtime = Runtime.getRuntime();

        // Traditional approach - loads all data into memory
        runtime.gc();
        var beforeTraditional = runtime.totalMemory() - runtime.freeMemory();

        var traditionalData = loadAllDataTraditional();
        var afterTraditional = runtime.totalMemory() - runtime.freeMemory();

        // Reactive approach - streams data
        runtime.gc();
        var beforeReactive = runtime.totalMemory() - runtime.freeMemory();

        streamDataReactive().blockLast();
        var afterReactive = runtime.totalMemory() - runtime.freeMemory();

        logger.info("Traditional memory usage: {} MB",
                   (afterTraditional - beforeTraditional) / 1024 / 1024);
        logger.info("Reactive memory usage: {} MB",
                   (afterReactive - beforeReactive) / 1024 / 1024);
    }

    // Scalability comparison
    public void scalabilityComparison() {
        var threadCounts = List.of(10, 50, 100, 200, 500, 1000);

        for (var threadCount : threadCounts) {
            // Traditional approach
            var traditionalTime = measureTraditionalWithThreads(threadCount);

            // Reactive approach (always uses same small thread pool)
            var reactiveTime = measureReactiveWithConcurrency(Math.min(threadCount, 20));

            logger.info("Threads: {}, Traditional: {}ms, Reactive: {}ms",
                       threadCount, traditionalTime, reactiveTime);
        }
    }

    // Latency comparison under load
    public void latencyComparisonUnderLoad() {
        // Traditional - latency increases with load
        var traditionalLatencies = measureTraditionalLatencies(1000);

        // Reactive - more consistent latency
        var reactiveLatencies = measureReactiveLatencies(1000);

        logger.info("Traditional P95 latency: {}ms", calculateP95(traditionalLatencies));
        logger.info("Reactive P95 latency: {}ms", calculateP95(reactiveLatencies));
    }

    // Resource utilization comparison
    public void resourceUtilizationComparison() {
        var mxBean = ManagementFactory.getThreadMXBean();

        // Traditional approach
        var traditionalThreadsBefore = mxBean.getThreadCount();
        runTraditionalWorkload();
        var traditionalThreadsAfter = mxBean.getThreadCount();

        // Reactive approach
        var reactiveThreadsBefore = mxBean.getThreadCount();
        runReactiveWorkload();
        var reactiveThreadsAfter = mxBean.getThreadCount();

        logger.info("Traditional threads created: {}",
                   traditionalThreadsAfter - traditionalThreadsBefore);
        logger.info("Reactive threads created: {}",
                   reactiveThreadsAfter - reactiveThreadsBefore);
    }

    // Helper methods
    private void processTraditionalRequest(int requestId) {
        // Simulate processing
    }

    private Mono<String> processReactiveRequest(int requestId) {
        return Mono.delay(Duration.ofMillis(100))
                  .map(i -> "processed-" + requestId);
    }

    private List<String> loadAllDataTraditional() {
        return IntStream.range(1, 100000)
                       .mapToObj(i -> "data-" + i)
                       .collect(Collectors.toList());
    }

    private Flux<String> streamDataReactive() {
        return Flux.range(1, 100000)
                  .map(i -> "data-" + i);
    }

    private long measureTraditionalWithThreads(int threadCount) {
        var executor = Executors.newFixedThreadPool(threadCount);
        var startTime = System.currentTimeMillis();
        var latch = new CountDownLatch(1000);

        for (int i = 0; i < 1000; i++) {
            executor.submit(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
            return System.currentTimeMillis() - startTime;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        } finally {
            executor.shutdown();
        }
    }

    private long measureReactiveWithConcurrency(int concurrency) {
        var startTime = System.currentTimeMillis();

        Flux.range(1, 1000)
            .flatMap(i -> Mono.delay(Duration.ofMillis(10)), concurrency)
            .blockLast();

        return System.currentTimeMillis() - startTime;
    }

    private List<Long> measureTraditionalLatencies(int requests) {
        var latencies = new ArrayList<Long>();
        var executor = Executors.newFixedThreadPool(100);

        for (int i = 0; i < requests; i++) {
            var future = executor.submit(() -> {
                var start = System.nanoTime();
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return (System.nanoTime() - start) / 1_000_000;
            });

            try {
                latencies.add(future.get());
            } catch (Exception e) {
                // Handle error
            }
        }

        executor.shutdown();
        return latencies;
    }

    private List<Long> measureReactiveLatencies(int requests) {
        return Flux.range(1, requests)
                  .flatMap(i -> {
                      var start = System.nanoTime();
                      return Mono.delay(Duration.ofMillis(10))
                                .map(ignored -> (System.nanoTime() - start) / 1_000_000);
                  }, 20)
                  .collectList()
                  .block();
    }

    private long calculateP95(List<Long> latencies) {
        var sorted = latencies.stream().sorted().collect(Collectors.toList());
        var index = (int) (sorted.size() * 0.95);
        return sorted.get(index);
    }

    private void runTraditionalWorkload() {
        var executor = Executors.newFixedThreadPool(100);
        var latch = new CountDownLatch(1000);

        for (int i = 0; i < 1000; i++) {
            executor.submit(() -> {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            executor.shutdown();
        }
    }

    private void runReactiveWorkload() {
        Flux.range(1, 1000)
            .flatMap(i -> Mono.delay(Duration.ofMillis(50)), 20)
            .blockLast();
    }
}
```

---

## ❓ **Common Interview Questions**

### **Q1: What is reactive programming and what problems does it solve?**

**Answer:**
```java
// Traditional blocking approach problems:
public class TraditionalProblems {
    public String fetchUserData(String userId) {
        var user = userService.getUser(userId);        // Blocks thread
        var orders = orderService.getOrders(userId);   // Blocks thread
        var prefs = prefService.getPrefs(userId);      // Blocks thread

        // Thread is blocked for entire duration
        // Poor resource utilization
        // Limited scalability
        return combineData(user, orders, prefs);
    }
}

// Reactive approach solutions:
public class ReactiveSolutions {
    public Mono<String> fetchUserData(String userId) {
        var userMono = userService.getUser(userId);
        var ordersMono = orderService.getOrders(userId);
        var prefsMono = prefService.getPrefs(userId);

        return Mono.zip(userMono, ordersMono, prefsMono)
                  .map(tuple -> combineData(tuple.getT1(), tuple.getT2(), tuple.getT3()));

        // Non-blocking, asynchronous
        // Better resource utilization
        // Higher scalability
        // Backpressure handling
        // Composable operations
    }
}
```

### **Q2: Explain the difference between Mono and Flux**

**Answer:**
```java
// Mono - 0 or 1 element
public class MonoExamples {
    public Mono<User> getUser(String id) {
        return userRepository.findById(id);  // Returns single user or empty
    }

    public Mono<Void> deleteUser(String id) {
        return userRepository.deleteById(id);  // Returns completion signal
    }

    public Mono<String> processData(String data) {
        return Mono.just(data.toUpperCase());  // Single transformation
    }
}

// Flux - 0 to N elements
public class FluxExamples {
    public Flux<User> getAllUsers() {
        return userRepository.findAll();  // Stream of users
    }

    public Flux<String> processStream(Flux<String> input) {
        return input.map(String::toUpperCase)
                   .filter(s -> s.length() > 3);  // Stream processing
    }

    public Flux<Long> generateSequence() {
        return Flux.interval(Duration.ofSeconds(1));  // Infinite stream
    }
}

// When to use which:
// Mono: Single HTTP response, database save operation, single computation
// Flux: Streaming data, collections, real-time events, file processing
```

### **Q3: How do you handle errors in reactive streams?**

**Answer:**
```java
public class ReactiveErrorHandling {

    // Basic error handling
    public Mono<String> basicErrorHandling() {
        return callService()
               .onErrorReturn("fallback")                    // Return fallback value
               .onErrorResume(ex -> Mono.just("recovered"))  // Switch to alternative stream
               .onErrorMap(IOException.class,
                          ex -> new ServiceException(ex));   // Transform error type
    }

    // Error continuation
    public Flux<String> errorContinuation() {
        return Flux.range(1, 10)
                  .map(i -> {
                      if (i == 5) throw new RuntimeException("Error at " + i);
                      return "item-" + i;
                  })
                  .onErrorContinue((error, item) -> {
                      logger.error("Error processing {}", item, error);
                      // Continue with next items
                  });
    }

    // Retry strategies
    public Mono<String> retryStrategies() {
        return callService()
               .retry(3)  // Simple retry
               .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                             .filter(this::isRetryable));
    }

    // Global error handling
    public <T> Mono<T> globalErrorHandler(Mono<T> operation) {
        return operation
               .onErrorResume(ValidationException.class, this::handleValidation)
               .onErrorResume(AuthException.class, this::handleAuth)
               .onErrorResume(Exception.class, this::handleGeneric);
    }
}
```

### **Q4: What is backpressure and how do you handle it?**

**Answer:**
```java
public class BackpressureHandling {

    // Problem: Fast producer, slow consumer
    public void demonstrateBackpressure() {
        Flux.interval(Duration.ofMillis(1))      // Fast producer (1000/sec)
            .doOnNext(this::slowConsumer)        // Slow consumer (10/sec)
            .subscribe();
        // Result: OutOfMemoryError due to unbounded buffering
    }

    // Solution 1: Buffer with strategy
    public Flux<Long> bufferStrategy() {
        return Flux.interval(Duration.ofMillis(1))
                   .onBackpressureBuffer(1000,              // Buffer size
                                       BufferOverflowStrategy.DROP_LATEST);
    }

    // Solution 2: Drop excess items
    public Flux<Long> dropStrategy() {
        return Flux.interval(Duration.ofMillis(1))
                   .onBackpressureDrop(item ->
                       logger.warn("Dropped: {}", item));
    }

    // Solution 3: Keep only latest
    public Flux<Long> latestStrategy() {
        return Flux.interval(Duration.ofMillis(1))
                   .onBackpressureLatest();
    }

    // Solution 4: Reactive pull
    public void reactivePull() {
        Flux.range(1, 1000)
            .subscribe(new BaseSubscriber<Integer>() {
                @Override
                protected void hookOnSubscribe(Subscription subscription) {
                    request(1);  // Request only 1 initially
                }

                @Override
                protected void hookOnNext(Integer value) {
                    processSlowly(value);
                    request(1);  // Request next after processing
                }
            });
    }
}
```

### **Q5: How do you test reactive code?**

**Answer:**
```java
@ExtendWith(MockitoExtension.class)
class ReactiveTestingExamples {

    @Mock
    private UserRepository userRepository;

    // Basic testing with StepVerifier
    @Test
    void shouldReturnUser() {
        // Given
        var user = new User("123", "John");
        when(userRepository.findById("123")).thenReturn(Mono.just(user));

        // When
        var result = userService.getUser("123");

        // Then
        StepVerifier.create(result)
                   .expectNext(user)
                   .verifyComplete();
    }

    // Testing errors
    @Test
    void shouldHandleError() {
        when(userRepository.findById("123"))
            .thenReturn(Mono.error(new RuntimeException("DB error")));

        StepVerifier.create(userService.getUser("123"))
                   .expectError(RuntimeException.class)
                   .verify();
    }

    // Testing with virtual time
    @Test
    void shouldDelayProcessing() {
        StepVerifier.withVirtualTime(() ->
                        userService.getDelayedUser("123"))
                   .expectSubscription()
                   .expectNoEvent(Duration.ofSeconds(4))
                   .thenAwait(Duration.ofSeconds(1))
                   .expectNextCount(1)
                   .verifyComplete();
    }

    // Testing backpressure
    @Test
    void shouldHandleBackpressure() {
        var publisher = TestPublisher.<String>create();

        StepVerifier.create(userService.processStream(publisher.flux()), 0)
                   .then(() -> publisher.next("item1"))
                   .expectNoEvent(Duration.ofMillis(100))
                   .thenRequest(1)
                   .expectNext("ITEM1")
                   .thenCancel()
                   .verify();
    }

    // Integration testing
    @Test
    void shouldHandleWebRequest() {
        webTestClient.get()
                    .uri("/users/123")
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(User.class)
                    .value(user -> assertThat(user.getId()).isEqualTo("123"));
    }
}
```

### **Q6: When would you choose reactive over traditional imperative programming?**

**Answer:**
```java
// Choose Reactive when:
public class ReactiveUseCase {

    // 1. High concurrency requirements
    public Flux<String> highConcurrency() {
        return Flux.range(1, 1000000)
                  .flatMap(this::processAsync, 1000)  // 1000 concurrent operations
                  .onBackpressureBuffer();
        // Traditional: Would need 1000 threads
        // Reactive: Uses small thread pool efficiently
    }

    // 2. I/O intensive operations
    public Mono<UserProfile> ioIntensive(String userId) {
        return Mono.zip(
            userService.getUser(userId),         // Database call
            orderService.getOrders(userId),     // HTTP call
            prefService.getPrefs(userId),       // Cache call
            notifyService.getNotifications(userId) // Message queue
        ).map(this::combineProfile);
        // All calls execute in parallel, non-blocking
    }

    // 3. Streaming data
    public Flux<LogEntry> streamProcessing() {
        return logStream
               .filter(log -> log.getLevel() == ERROR)
               .buffer(Duration.ofSeconds(5))
               .flatMap(this::analyzeLogBatch)
               .publishOn(Schedulers.boundedElastic());
        // Real-time processing of continuous data
    }

    // 4. Backpressure handling needed
    public Flux<ProcessedData> backpressureScenario() {
        return dataSource
               .onBackpressureBuffer(1000)
               .publishOn(Schedulers.parallel(), 8)  // Small buffer
               .map(this::expensiveProcessing);
        // Handles varying processing speeds
    }
}

// Choose Traditional when:
public class TraditionalUseCase {

    // 1. Simple CRUD operations
    public User simpleOperation(String id) {
        return userRepository.findById(id).orElse(null);
        // Simple, straightforward, no need for complexity
    }

    // 2. CPU-intensive algorithms
    public BigInteger factorial(int n) {
        BigInteger result = BigInteger.ONE;
        for (int i = 2; i <= n; i++) {
            result = result.multiply(BigInteger.valueOf(i));
        }
        return result;
        // Sequential processing, no I/O, simple logic
    }

    // 3. Existing synchronous APIs
    public void legacyIntegration() {
        var connection = legacyDatabase.getConnection();
        var result = connection.executeQuery("SELECT * FROM users");
        // Working with existing blocking APIs
    }

    // 4. Small applications with low concurrency
    public String smallApp(String input) {
        return input.toUpperCase().trim();
        // Simple transformations, low load
    }
}
```

### **Q7: Explain the difference between subscribeOn and publishOn**

**Answer:**
```java
public class SchedulerDifference {

    public void demonstrateSchedulers() {
        Flux.range(1, 3)
            .doOnNext(i -> log("1. Source: " + i))
            .subscribeOn(Schedulers.boundedElastic())    // Affects upstream
            .doOnNext(i -> log("2. After subscribeOn: " + i))
            .publishOn(Schedulers.parallel())            // Affects downstream
            .doOnNext(i -> log("3. After publishOn: " + i))
            .subscribe(i -> log("4. Final: " + i));
    }

    // Output shows:
    // 1. Source: 1 on boundedElastic-1
    // 2. After subscribeOn: 1 on boundedElastic-1
    // 3. After publishOn: 1 on parallel-1
    // 4. Final: 1 on parallel-1

    // subscribeOn: Changes where subscription happens (affects entire chain upstream)
    // publishOn: Changes where signals are emitted (affects downstream only)

    public void multipleSchedulers() {
        Flux.range(1, 3)
            .subscribeOn(Schedulers.boundedElastic())  // Subscription on boundedElastic
            .map(i -> i * 2)                          // Runs on boundedElastic
            .publishOn(Schedulers.parallel())         // Switch to parallel
            .map(i -> i + 10)                         // Runs on parallel
            .publishOn(Schedulers.single())           // Switch to single
            .subscribe(i -> log(i));                  // Runs on single
    }
}

// Guidelines:
// - Use subscribeOn for I/O operations (database, HTTP calls)
// - Use publishOn when you need to switch threading context
// - subscribeOn only affects subscription, publishOn affects emission
// - Multiple publishOn calls create multiple thread switches
// - Only the first subscribeOn in the chain has effect
```

---

## 🎯 **Summary**

### **Key Reactive Programming Concepts**

1. **Non-blocking I/O**: Operations don't block threads, improving resource utilization
2. **Asynchronous Processing**: Operations complete without waiting, enabling parallelism
3. **Backpressure**: Handling different processing speeds between producers/consumers
4. **Composability**: Building complex operations from simple, reusable components
5. **Declarative Style**: Describing what to do rather than how to do it

### **When to Use Reactive Programming**

✅ **Use Reactive When:**
- High concurrency requirements (thousands of connections)
- I/O intensive operations (database, HTTP, messaging)
- Streaming data processing
- Real-time applications
- Need for backpressure handling
- Microservices with many external calls

❌ **Avoid Reactive When:**
- Simple CRUD applications
- CPU-intensive algorithms
- Legacy system integration
- Team lacks reactive experience
- Debugging complexity is a concern

### **Performance Benefits**

- **Resource Efficiency**: Fewer threads handle more concurrent operations
- **Scalability**: Linear scaling with load
- **Memory Usage**: Controlled through backpressure
- **Latency**: Better response times under high load
- **Throughput**: Higher requests per second

### **Best Practices**

1. **Error Handling**: Always provide error handling strategies
2. **Backpressure**: Choose appropriate backpressure strategies
3. **Scheduler Selection**: Use correct schedulers for workload types
4. **Testing**: Use StepVerifier for comprehensive testing
5. **Monitoring**: Implement proper metrics and observability
6. **Learning Curve**: Invest in team training and reactive thinking

---

*This comprehensive guide covers reactive programming with Spring WebFlux for technical interviews, providing practical examples and real-world scenarios.*