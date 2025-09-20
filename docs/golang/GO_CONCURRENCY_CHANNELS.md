# Go Concurrency and Channels - Advanced Patterns

**Scope:** This guide focuses on advanced concurrency patterns, channel usage, context management, and complex synchronization scenarios not covered in the comprehensive guide. For basic goroutines and simple channel examples, see GOLANG_INTERVIEW_COMPREHENSIVE.md or GO_FUNDAMENTALS.md.

## Table of Contents
1. [Advanced Channel Patterns](#advanced-channel-patterns)
2. [Context and Cancellation](#context-and-cancellation)
3. [Complex Synchronization](#complex-synchronization)
4. [External Services and Message Queues](#external-services-and-message-queues)
5. [Performance Optimization](#performance-optimization)

## Advanced Channel Patterns

[⬆️ Back to Top](#table-of-contents)

### Advanced Channel Communication Patterns

```go
package main

import (
    "fmt"
    "time"
)

// Channel direction restrictions for type safety
func channelDirectionsExample() {
    fmt.Println("=== Channel Directions Example ===")

    // Create bidirectional channel
    ch := make(chan int, 2)

    // Pass send-only channel to producer
    go producer(ch)

    // Pass receive-only channel to consumer
    go consumer(ch)

    time.Sleep(2 * time.Second)
}

// Send-only channel parameter
func producer(ch chan<- int) {
    for i := 1; i <= 5; i++ {
        ch <- i
        fmt.Printf("Produced: %d\n", i)
        time.Sleep(200 * time.Millisecond)
    }
    close(ch)
}

// Receive-only channel parameter
func consumer(ch <-chan int) {
    for value := range ch {
        fmt.Printf("Consumed: %d\n", value)
    }
}

// Channel of channels pattern
func channelOfChannelsExample() {
    fmt.Println("=== Channel of Channels Example ===")

    // Channel that carries other channels
    respChannels := make(chan chan int)

    // Worker that processes requests
    go func() {
        for respCh := range respChannels {
            go func(rc chan int) {
                // Simulate work
                time.Sleep(100 * time.Millisecond)
                rc <- rand.Intn(100)
                close(rc)
            }(respCh)
        }
    }()

    // Make multiple requests
    for i := 0; i < 3; i++ {
        respCh := make(chan int)
        respChannels <- respCh

        result := <-respCh
        fmt.Printf("Request %d result: %d\n", i+1, result)
    }

    close(respChannels)
}

// Advanced Fan-out, fan-in with error handling
func advancedFanOutFanInExample() {
    fmt.Println("=== Advanced Fan-out Fan-in Example ===")

    // Input with error handling
    type WorkItem struct {
        ID   int
        Data interface{}
    }

    type Result struct {
        WorkID int
        Value  interface{}
        Error  error
    }

    input := make(chan WorkItem, 10)
    results := make(chan Result, 10)

    // Generate work items
    go func() {
        defer close(input)
        for i := 1; i <= 10; i++ {
            input <- WorkItem{ID: i, Data: i}
        }
    }()

    // Start multiple workers
    const numWorkers = 3
    var wg sync.WaitGroup

    for i := 0; i < numWorkers; i++ {
        wg.Add(1)
        go func(workerID int) {
            defer wg.Done()
            for item := range input {
                // Simulate work that might fail
                time.Sleep(100 * time.Millisecond)

                var result Result
                result.WorkID = item.ID

                if item.ID%4 == 0 {
                    result.Error = fmt.Errorf("simulated error for item %d", item.ID)
                } else {
                    result.Value = item.Data.(int) * item.Data.(int)
                }

                results <- result
            }
        }(i)
    }

    // Close results channel when all workers are done
    go func() {
        wg.Wait()
        close(results)
    }()

    // Collect results with error handling
    var successful, failed int
    for result := range results {
        if result.Error != nil {
            fmt.Printf("Work %d failed: %v\n", result.WorkID, result.Error)
            failed++
        } else {
            fmt.Printf("Work %d completed: %v\n", result.WorkID, result.Value)
            successful++
        }
    }

    fmt.Printf("Completed: %d successful, %d failed\n", successful, failed)
}

// Advanced Pipeline with error propagation and cancellation
func advancedPipelineExample() {
    fmt.Println("=== Advanced Pipeline Example ===")

    ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
    defer cancel()

    // Pipeline stages with context and error handling
    type PipelineData struct {
        Value int
        Error error
    }

    // Stage 1: Generate numbers with possible errors
    stage1 := func(ctx context.Context) <-chan PipelineData {
        out := make(chan PipelineData)
        go func() {
            defer close(out)
            for i := 1; i <= 10; i++ {
                select {
                case <-ctx.Done():
                    return
                case out <- PipelineData{Value: i}:
                    time.Sleep(50 * time.Millisecond)
                }
            }
        }()
        return out
    }

    // Stage 2: Process with validation
    stage2 := func(ctx context.Context, in <-chan PipelineData) <-chan PipelineData {
        out := make(chan PipelineData)
        go func() {
            defer close(out)
            for data := range in {
                if data.Error != nil {
                    select {
                    case <-ctx.Done():
                        return
                    case out <- data: // Propagate error
                    }
                    continue
                }

                // Simulate processing that might fail
                var result PipelineData
                if data.Value%3 == 0 {
                    result.Error = fmt.Errorf("value %d is divisible by 3", data.Value)
                } else {
                    result.Value = data.Value * data.Value
                }

                select {
                case <-ctx.Done():
                    return
                case out <- result:
                }
            }
        }()
        return out
    }

    // Stage 3: Final processing
    stage3 := func(ctx context.Context, in <-chan PipelineData) <-chan PipelineData {
        out := make(chan PipelineData)
        go func() {
            defer close(out)
            for data := range in {
                if data.Error != nil {
                    select {
                    case <-ctx.Done():
                        return
                    case out <- data: // Propagate error
                    }
                    continue
                }

                result := PipelineData{Value: data.Value * 2}
                select {
                case <-ctx.Done():
                    return
                case out <- result:
                }
            }
        }()
        return out
    }

    // Build and run pipeline
    pipeline := stage3(ctx, stage2(ctx, stage1(ctx)))

    // Collect results
    var successful, failed int
    for result := range pipeline {
        if result.Error != nil {
            fmt.Printf("Pipeline error: %v\n", result.Error)
            failed++
        } else {
            fmt.Printf("Pipeline result: %d\n", result.Value)
            successful++
        }
    }

    fmt.Printf("Pipeline completed: %d successful, %d failed\n", successful, failed)
}

// Adaptive Worker Pool with dynamic scaling
func adaptiveWorkerPoolExample() {
    fmt.Println("=== Adaptive Worker Pool Example ===")

    type Job struct {
        ID       int
        Priority int
        Data     interface{}
    }

    type WorkerPool struct {
        jobs        chan Job
        results     chan interface{}
        workers     int
        maxWorkers  int
        minWorkers  int
        workersLock sync.RWMutex
        ctx         context.Context
        cancel      context.CancelFunc
    }

    pool := &WorkerPool{
        jobs:       make(chan Job, 100),
        results:    make(chan interface{}, 100),
        minWorkers: 2,
        maxWorkers: 10,
        workers:    2,
    }
    pool.ctx, pool.cancel = context.WithCancel(context.Background())

    // Start initial workers
    for i := 0; i < pool.minWorkers; i++ {
        go pool.worker(i)
    }

    // Monitor and scale workers based on queue length
    go func() {
        ticker := time.NewTicker(500 * time.Millisecond)
        defer ticker.Stop()

        for {
            select {
            case <-ticker.C:
                queueLen := len(pool.jobs)
                pool.workersLock.RLock()
                currentWorkers := pool.workers
                pool.workersLock.RUnlock()

                // Scale up if queue is getting full
                if queueLen > 50 && currentWorkers < pool.maxWorkers {
                    pool.workersLock.Lock()
                    pool.workers++
                    workerID := pool.workers
                    pool.workersLock.Unlock()

                    go pool.worker(workerID)
                    fmt.Printf("Scaled up to %d workers\n", workerID)
                }

                // Scale down if queue is mostly empty
                if queueLen < 10 && currentWorkers > pool.minWorkers {
                    // Workers will self-terminate when they detect scaling down
                    fmt.Printf("Scaling down from %d workers\n", currentWorkers)
                }

            case <-pool.ctx.Done():
                return
            }
        }
    }()

    // Submit jobs with different priorities
    go func() {
        for i := 1; i <= 50; i++ {
            job := Job{
                ID:       i,
                Priority: rand.Intn(3), // 0=high, 1=medium, 2=low
                Data:     i * 10,
            }
            pool.jobs <- job
            time.Sleep(100 * time.Millisecond)
        }
        close(pool.jobs)
    }()

    // Collect results
    go func() {
        for result := range pool.results {
            fmt.Printf("Result: %v\n", result)
        }
    }()

    time.Sleep(10 * time.Second)
    pool.cancel()
    close(pool.results)
}

func (p *WorkerPool) worker(id int) {
    fmt.Printf("Worker %d started\n", id)
    defer fmt.Printf("Worker %d stopped\n", id)

    for {
        select {
        case job, ok := <-p.jobs:
            if !ok {
                return // Jobs channel closed
            }

            // Check if we should scale down
            p.workersLock.RLock()
            shouldScaleDown := len(p.jobs) < 10 && p.workers > p.minWorkers && id > p.minWorkers
            if shouldScaleDown {
                p.workersLock.RUnlock()
                p.workersLock.Lock()
                p.workers--
                p.workersLock.Unlock()
                return
            }
            p.workersLock.RUnlock()

            // Process job based on priority
            processingTime := time.Duration(100*(job.Priority+1)) * time.Millisecond
            time.Sleep(processingTime)

            result := fmt.Sprintf("Job %d (priority %d) processed by worker %d",
                                  job.ID, job.Priority, id)

            select {
            case p.results <- result:
            case <-p.ctx.Done():
                return
            }

        case <-p.ctx.Done():
            return
        }
    }
}
```

## Context and Cancellation

### Advanced Context Patterns

```go
package main

import (
    "context"
    "fmt"
    "sync"
    "time"
)

// Context with timeout
func contextTimeoutExample() {
    fmt.Println("=== Context Timeout Example ===")

    // Create context with 2 second timeout
    ctx, cancel := context.WithTimeout(context.Background(), 2*time.Second)
    defer cancel()

    var wg sync.WaitGroup

    // Start multiple workers with context
    for i := 1; i <= 3; i++ {
        wg.Add(1)
        go func(workerID int) {
            defer wg.Done()

            select {
            case <-time.After(time.Duration(workerID) * time.Second):
                fmt.Printf("Worker %d completed work\n", workerID)
            case <-ctx.Done():
                fmt.Printf("Worker %d cancelled: %v\n", workerID, ctx.Err())
            }
        }(i)
    }

    wg.Wait()
}

// Context with cancellation
func contextCancellationExample() {
    fmt.Println("=== Context Cancellation Example ===")

    ctx, cancel := context.WithCancel(context.Background())

    var wg sync.WaitGroup

    // Start long-running workers
    for i := 1; i <= 3; i++ {
        wg.Add(1)
        go longRunningWorker(ctx, &wg, i)
    }

    // Cancel after 3 seconds
    time.Sleep(3 * time.Second)
    fmt.Println("Cancelling all workers...")
    cancel()

    wg.Wait()
    fmt.Println("All workers stopped")
}

func longRunningWorker(ctx context.Context, wg *sync.WaitGroup, id int) {
    defer wg.Done()

    ticker := time.NewTicker(time.Second)
    defer ticker.Stop()

    for {
        select {
        case <-ticker.C:
            fmt.Printf("Worker %d is working...\n", id)
        case <-ctx.Done():
            fmt.Printf("Worker %d stopping: %v\n", id, ctx.Err())
            return
        }
    }
}

// Context with values (for request tracing)
func contextWithValuesExample() {
    fmt.Println("=== Context With Values Example ===")

    // Create context with request ID
    ctx := context.WithValue(context.Background(), "requestID", "req-123")
    ctx = context.WithValue(ctx, "userID", "user-456")

    processRequest(ctx)
}

func processRequest(ctx context.Context) {
    requestID := ctx.Value("requestID").(string)
    userID := ctx.Value("userID").(string)

    fmt.Printf("Processing request %s for user %s\n", requestID, userID)

    // Pass context to other functions
    authenticateUser(ctx)
    processPayment(ctx)
}

func authenticateUser(ctx context.Context) {
    requestID := ctx.Value("requestID").(string)
    userID := ctx.Value("userID").(string)

    fmt.Printf("[%s] Authenticating user %s\n", requestID, userID)
}

func processPayment(ctx context.Context) {
    requestID := ctx.Value("requestID").(string)
    userID := ctx.Value("userID").(string)

    fmt.Printf("[%s] Processing payment for user %s\n", requestID, userID)
}
```

## Complex Synchronization

### Advanced Rate Limiting and Backpressure

```go
package main

import (
    "context"
    "fmt"
    "golang.org/x/time/rate"
    "sync"
    "time"
)

// Multi-tier rate limiter with different limits per client
type MultiTierRateLimiter struct {
    limiters map[string]*rate.Limiter
    tiers    map[string]TierConfig
    mu       sync.RWMutex
}

type TierConfig struct {
    RequestsPerSecond rate.Limit
    BurstSize         int
}

func NewMultiTierRateLimiter() *MultiTierRateLimiter {
    return &MultiTierRateLimiter{
        limiters: make(map[string]*rate.Limiter),
        tiers: map[string]TierConfig{
            "free":     {rate.Limit(1), 5},
            "premium":  {rate.Limit(10), 20},
            "enterprise": {rate.Limit(100), 200},
        },
    }
}

func (mtrl *MultiTierRateLimiter) Allow(clientID, tier string) bool {
    mtrl.mu.RLock()
    limiter, exists := mtrl.limiters[clientID]
    mtrl.mu.RUnlock()

    if !exists {
        mtrl.mu.Lock()
        // Double-check after acquiring write lock
        if limiter, exists = mtrl.limiters[clientID]; !exists {
            config, ok := mtrl.tiers[tier]
            if !ok {
                config = mtrl.tiers["free"] // Default to free tier
            }
            limiter = rate.NewLimiter(config.RequestsPerSecond, config.BurstSize)
            mtrl.limiters[clientID] = limiter
        }
        mtrl.mu.Unlock()
    }

    return limiter.Allow()
}

// Backpressure handling with circuit breaker
type BackpressureHandler struct {
    queue       chan Request
    maxQueue    int
    processing  int64
    maxConcurrent int64
    circuitBreaker *CircuitBreaker
}

type Request struct {
    ID       string
    Data     interface{}
    Response chan Response
}

type Response struct {
    Result interface{}
    Error  error
}

type CircuitBreaker struct {
    maxFailures int
    timeout     time.Duration
    failures    int64
    lastFailure time.Time
    state       int32 // 0=closed, 1=open, 2=half-open
    mu          sync.RWMutex
}

func NewBackpressureHandler(maxQueue int, maxConcurrent int64) *BackpressureHandler {
    return &BackpressureHandler{
        queue:          make(chan Request, maxQueue),
        maxQueue:       maxQueue,
        maxConcurrent:  maxConcurrent,
        circuitBreaker: &CircuitBreaker{maxFailures: 5, timeout: 30 * time.Second},
    }
}

func (bh *BackpressureHandler) Submit(req Request) error {
    // Check circuit breaker
    if !bh.circuitBreaker.allowRequest() {
        return fmt.Errorf("circuit breaker is open")
    }

    // Check if we're at capacity
    if atomic.LoadInt64(&bh.processing) >= bh.maxConcurrent {
        return fmt.Errorf("system at capacity")
    }

    // Try to queue the request
    select {
    case bh.queue <- req:
        return nil
    default:
        return fmt.Errorf("queue is full")
    }
}

func (bh *BackpressureHandler) Start(ctx context.Context, numWorkers int) {
    for i := 0; i < numWorkers; i++ {
        go bh.worker(ctx, i)
    }
}

func (bh *BackpressureHandler) worker(ctx context.Context, id int) {
    for {
        select {
        case req := <-bh.queue:
            atomic.AddInt64(&bh.processing, 1)

            // Simulate processing
            err := bh.processRequest(req)

            bh.circuitBreaker.recordResult(err)

            response := Response{Result: "processed", Error: err}
            select {
            case req.Response <- response:
            default:
                // Response channel might be closed
            }

            atomic.AddInt64(&bh.processing, -1)

        case <-ctx.Done():
            return
        }
    }
}

func (bh *BackpressureHandler) processRequest(req Request) error {
    // Simulate work that might fail
    time.Sleep(100 * time.Millisecond)
    if rand.Float32() < 0.1 { // 10% failure rate
        return fmt.Errorf("processing failed for request %s", req.ID)
    }
    return nil
}

func (cb *CircuitBreaker) allowRequest() bool {
    cb.mu.RLock()
    defer cb.mu.RUnlock()

    state := atomic.LoadInt32(&cb.state)

    if state == 1 { // open
        if time.Since(cb.lastFailure) > cb.timeout {
            atomic.StoreInt32(&cb.state, 2) // half-open
            return true
        }
        return false
    }

    return true // closed or half-open
}

func (cb *CircuitBreaker) recordResult(err error) {
    cb.mu.Lock()
    defer cb.mu.Unlock()

    if err != nil {
        cb.failures++
        cb.lastFailure = time.Now()

        if cb.failures >= int64(cb.maxFailures) {
            atomic.StoreInt32(&cb.state, 1) // open
        }
    } else {
        cb.failures = 0
        atomic.StoreInt32(&cb.state, 0) // closed
    }
}
```

## Performance Optimization

### Lock-Free Data Structures and Optimization

```go
// Lock-free stack using atomic operations
type LockFreeStack struct {
    head unsafe.Pointer
}

type stackNode struct {
    data interface{}
    next unsafe.Pointer
}

func (s *LockFreeStack) Push(data interface{}) {
    newNode := &stackNode{data: data}

    for {
        head := atomic.LoadPointer(&s.head)
        newNode.next = head

        if atomic.CompareAndSwapPointer(&s.head, head, unsafe.Pointer(newNode)) {
            break
        }
        // Retry on failure (ABA problem mitigation could be added)
    }
}

func (s *LockFreeStack) Pop() (interface{}, bool) {
    for {
        head := atomic.LoadPointer(&s.head)
        if head == nil {
            return nil, false
        }

        node := (*stackNode)(head)
        next := atomic.LoadPointer(&node.next)

        if atomic.CompareAndSwapPointer(&s.head, head, next) {
            return node.data, true
        }
        // Retry on failure
    }
}

// Memory pool for reducing GC pressure
type MemoryPool struct {
    pool sync.Pool
    size int
}

func NewMemoryPool(size int) *MemoryPool {
    return &MemoryPool{
        pool: sync.Pool{
            New: func() interface{} {
                return make([]byte, size)
            },
        },
        size: size,
    }
}

func (mp *MemoryPool) Get() []byte {
    return mp.pool.Get().([]byte)
}

func (mp *MemoryPool) Put(b []byte) {
    if len(b) == mp.size {
        // Reset slice but keep capacity
        b = b[:0]
        mp.pool.Put(b)
    }
}

// High-performance channel alternative using ring buffer
type RingBuffer struct {
    buffer []interface{}
    head   uint64
    tail   uint64
    mask   uint64
    mu     sync.Mutex
    notEmpty *sync.Cond
    notFull  *sync.Cond
}

func NewRingBuffer(size int) *RingBuffer {
    // Ensure size is power of 2 for efficient modulo
    if size&(size-1) != 0 {
        panic("size must be power of 2")
    }

    rb := &RingBuffer{
        buffer: make([]interface{}, size),
        mask:   uint64(size - 1),
    }
    rb.notEmpty = sync.NewCond(&rb.mu)
    rb.notFull = sync.NewCond(&rb.mu)

    return rb
}

func (rb *RingBuffer) Put(item interface{}) {
    rb.mu.Lock()
    defer rb.mu.Unlock()

    for rb.tail-rb.head == uint64(len(rb.buffer)) {
        rb.notFull.Wait()
    }

    rb.buffer[rb.tail&rb.mask] = item
    rb.tail++
    rb.notEmpty.Signal()
}

func (rb *RingBuffer) Get() interface{} {
    rb.mu.Lock()
    defer rb.mu.Unlock()

    for rb.head == rb.tail {
        rb.notEmpty.Wait()
    }

    item := rb.buffer[rb.head&rb.mask]
    rb.head++
    rb.notFull.Signal()

    return item
}
```

## External Services and Message Queues

### HTTP Client with Concurrency

```go
package main

import (
    "context"
    "encoding/json"
    "fmt"
    "net/http"
    "sync"
    "time"
)

// ExternalService represents an external API
type ExternalService struct {
    baseURL string
    client  *http.Client
}

type APIResponse struct {
    ID     string      `json:"id"`
    Data   interface{} `json:"data"`
    Status string      `json:"status"`
}

func NewExternalService(baseURL string) *ExternalService {
    return &ExternalService{
        baseURL: baseURL,
        client: &http.Client{
            Timeout: 10 * time.Second,
        },
    }
}

func (es *ExternalService) FetchData(ctx context.Context, endpoint string) (*APIResponse, error) {
    url := es.baseURL + endpoint

    req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
    if err != nil {
        return nil, fmt.Errorf("creating request: %w", err)
    }

    resp, err := es.client.Do(req)
    if err != nil {
        return nil, fmt.Errorf("making request: %w", err)
    }
    defer resp.Body.Close()

    if resp.StatusCode != http.StatusOK {
        return nil, fmt.Errorf("unexpected status: %d", resp.StatusCode)
    }

    var apiResp APIResponse
    if err := json.NewDecoder(resp.Body).Decode(&apiResp); err != nil {
        return nil, fmt.Errorf("decoding response: %w", err)
    }

    return &apiResp, nil
}

// Concurrent API calls with worker pool
func concurrentAPICallsExample() {
    fmt.Println("=== Concurrent API Calls Example ===")

    service := NewExternalService("https://jsonplaceholder.typicode.com")

    endpoints := []string{
        "/posts/1",
        "/posts/2",
        "/posts/3",
        "/users/1",
        "/users/2",
    }

    ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
    defer cancel()

    // Worker pool pattern for API calls
    jobs := make(chan string, len(endpoints))
    results := make(chan *APIResponse, len(endpoints))
    errors := make(chan error, len(endpoints))

    // Start workers
    const numWorkers = 3
    var wg sync.WaitGroup

    for i := 0; i < numWorkers; i++ {
        wg.Add(1)
        go func(workerID int) {
            defer wg.Done()
            for endpoint := range jobs {
                fmt.Printf("Worker %d fetching %s\n", workerID, endpoint)

                resp, err := service.FetchData(ctx, endpoint)
                if err != nil {
                    errors <- fmt.Errorf("endpoint %s: %w", endpoint, err)
                } else {
                    results <- resp
                }
            }
        }(i)
    }

    // Send jobs
    for _, endpoint := range endpoints {
        jobs <- endpoint
    }
    close(jobs)

    // Wait for workers to finish
    wg.Wait()
    close(results)
    close(errors)

    // Collect results
    fmt.Println("Results:")
    for result := range results {
        fmt.Printf("- ID: %s, Status: %s\n", result.ID, result.Status)
    }

    // Check errors
    for err := range errors {
        fmt.Printf("Error: %v\n", err)
    }
}

// Circuit breaker pattern
type CircuitBreaker struct {
    maxFailures int
    timeout     time.Duration
    failures    int
    lastFailure time.Time
    state       string // "closed", "open", "half-open"
    mu          sync.RWMutex
}

func NewCircuitBreaker(maxFailures int, timeout time.Duration) *CircuitBreaker {
    return &CircuitBreaker{
        maxFailures: maxFailures,
        timeout:     timeout,
        state:       "closed",
    }
}

func (cb *CircuitBreaker) Call(fn func() error) error {
    cb.mu.RLock()
    state := cb.state
    failures := cb.failures
    lastFailure := cb.lastFailure
    cb.mu.RUnlock()

    if state == "open" {
        if time.Since(lastFailure) > cb.timeout {
            cb.mu.Lock()
            cb.state = "half-open"
            cb.mu.Unlock()
        } else {
            return fmt.Errorf("circuit breaker is open")
        }
    }

    err := fn()

    cb.mu.Lock()
    defer cb.mu.Unlock()

    if err != nil {
        cb.failures++
        cb.lastFailure = time.Now()
        if cb.failures >= cb.maxFailures {
            cb.state = "open"
        }
        return err
    }

    // Success
    cb.failures = 0
    cb.state = "closed"
    return nil
}
```

### Message Queue Integration

```go
package main

import (
    "context"
    "encoding/json"
    "fmt"
    "sync"
    "time"
)

// Message represents a message in the queue
type Message struct {
    ID        string                 `json:"id"`
    Type      string                 `json:"type"`
    Payload   map[string]interface{} `json:"payload"`
    Timestamp time.Time              `json:"timestamp"`
    Retries   int                    `json:"retries"`
}

// MessageQueue simulates a message queue
type MessageQueue struct {
    messages chan Message
    workers  int
    handlers map[string]MessageHandler
    mu       sync.RWMutex
}

type MessageHandler func(ctx context.Context, msg Message) error

func NewMessageQueue(bufferSize, workers int) *MessageQueue {
    return &MessageQueue{
        messages: make(chan Message, bufferSize),
        workers:  workers,
        handlers: make(map[string]MessageHandler),
    }
}

func (mq *MessageQueue) RegisterHandler(messageType string, handler MessageHandler) {
    mq.mu.Lock()
    defer mq.mu.Unlock()
    mq.handlers[messageType] = handler
}

func (mq *MessageQueue) Publish(msg Message) error {
    select {
    case mq.messages <- msg:
        return nil
    default:
        return fmt.Errorf("queue is full")
    }
}

func (mq *MessageQueue) Start(ctx context.Context) {
    var wg sync.WaitGroup

    // Start worker goroutines
    for i := 0; i < mq.workers; i++ {
        wg.Add(1)
        go func(workerID int) {
            defer wg.Done()
            mq.worker(ctx, workerID)
        }(i)
    }

    wg.Wait()
}

func (mq *MessageQueue) worker(ctx context.Context, workerID int) {
    for {
        select {
        case msg := <-mq.messages:
            mq.processMessage(ctx, workerID, msg)
        case <-ctx.Done():
            fmt.Printf("Worker %d shutting down\n", workerID)
            return
        }
    }
}

func (mq *MessageQueue) processMessage(ctx context.Context, workerID int, msg Message) {
    mq.mu.RLock()
    handler, exists := mq.handlers[msg.Type]
    mq.mu.RUnlock()

    if !exists {
        fmt.Printf("Worker %d: No handler for message type %s\n", workerID, msg.Type)
        return
    }

    fmt.Printf("Worker %d processing message %s (type: %s)\n",
               workerID, msg.ID, msg.Type)

    err := handler(ctx, msg)
    if err != nil {
        fmt.Printf("Worker %d: Error processing message %s: %v\n",
                   workerID, msg.ID, err)

        // Retry logic
        if msg.Retries < 3 {
            msg.Retries++
            time.Sleep(time.Second * time.Duration(msg.Retries))
            select {
            case mq.messages <- msg:
                fmt.Printf("Worker %d: Retrying message %s (attempt %d)\n",
                           workerID, msg.ID, msg.Retries)
            default:
                fmt.Printf("Worker %d: Failed to requeue message %s\n",
                           workerID, msg.ID)
            }
        }
    } else {
        fmt.Printf("Worker %d: Successfully processed message %s\n",
                   workerID, msg.ID)
    }
}

// Message handlers
func paymentHandler(ctx context.Context, msg Message) error {
    // Simulate payment processing
    amount, ok := msg.Payload["amount"].(float64)
    if !ok {
        return fmt.Errorf("invalid amount in payment message")
    }

    // Simulate processing time
    time.Sleep(500 * time.Millisecond)

    if amount > 10000 {
        return fmt.Errorf("amount too large: %.2f", amount)
    }

    fmt.Printf("Payment processed: $%.2f\n", amount)
    return nil
}

func emailHandler(ctx context.Context, msg Message) error {
    // Simulate email sending
    recipient, ok := msg.Payload["recipient"].(string)
    if !ok {
        return fmt.Errorf("invalid recipient in email message")
    }

    time.Sleep(200 * time.Millisecond)
    fmt.Printf("Email sent to: %s\n", recipient)
    return nil
}

func auditHandler(ctx context.Context, msg Message) error {
    // Simulate audit logging
    action, ok := msg.Payload["action"].(string)
    if !ok {
        return fmt.Errorf("invalid action in audit message")
    }

    time.Sleep(100 * time.Millisecond)
    fmt.Printf("Audit log: %s at %s\n", action, msg.Timestamp.Format(time.RFC3339))
    return nil
}

// Example usage
func messageQueueExample() {
    fmt.Println("=== Message Queue Example ===")

    // Create message queue with 3 workers
    mq := NewMessageQueue(100, 3)

    // Register handlers
    mq.RegisterHandler("payment", paymentHandler)
    mq.RegisterHandler("email", emailHandler)
    mq.RegisterHandler("audit", auditHandler)

    ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
    defer cancel()

    // Start the message queue
    go mq.Start(ctx)

    // Publish messages
    messages := []Message{
        {
            ID:   "msg-1",
            Type: "payment",
            Payload: map[string]interface{}{
                "amount":    1500.00,
                "account":   "ACC-001",
            },
            Timestamp: time.Now(),
        },
        {
            ID:   "msg-2",
            Type: "email",
            Payload: map[string]interface{}{
                "recipient": "user@example.com",
                "subject":   "Payment Confirmation",
            },
            Timestamp: time.Now(),
        },
        {
            ID:   "msg-3",
            Type: "audit",
            Payload: map[string]interface{}{
                "action": "payment_processed",
                "user":   "user-123",
            },
            Timestamp: time.Now(),
        },
        {
            ID:   "msg-4",
            Type: "payment",
            Payload: map[string]interface{}{
                "amount":  15000.00, // This will fail
                "account": "ACC-002",
            },
            Timestamp: time.Now(),
        },
    }

    for _, msg := range messages {
        err := mq.Publish(msg)
        if err != nil {
            fmt.Printf("Failed to publish message %s: %v\n", msg.ID, err)
        }
    }

    // Wait for processing
    time.Sleep(5 * time.Second)
}

func main() {
    concurrentAPICallsExample()
    fmt.Println()
    messageQueueExample()
}
```

[⬆️ Back to Top](#table-of-contents)

**Note:** This guide focuses on advanced concurrency patterns. For basic goroutines, simple channels, and fundamental concepts, see GOLANG_INTERVIEW_COMPREHENSIVE.md. For complete web applications and database integration examples, see GO_WEB_DATABASE.md.

[⬆️ Back to Top](#table-of-contents)