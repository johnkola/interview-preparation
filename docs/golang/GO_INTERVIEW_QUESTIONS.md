# Go Interview Questions - Pure Q&A Reference

**Scope:** This guide contains only interview questions and answers in Q&A format. All code implementations and detailed explanations have been moved to specialized guides. See cross-references for detailed examples and implementations.

## Quick Reference to Detailed Guides
- **Basic Syntax & Concepts**: See [GO_FUNDAMENTALS.md](./GO_FUNDAMENTALS.md)
- **Comprehensive Examples**: See [GOLANG_INTERVIEW_COMPREHENSIVE.md](./GOLANG_INTERVIEW_COMPREHENSIVE.md)
- **Concurrency Patterns**: See [GO_CONCURRENCY_CHANNELS.md](./GO_CONCURRENCY_CHANNELS.md)
- **Advanced Topics**: See [GO_ADVANCED_TOPICS.md](./GO_ADVANCED_TOPICS.md)
- **Web & Database**: See [GO_WEB_DATABASE.md](./GO_WEB_DATABASE.md)

## Table of Contents
1. [Beginner Level Questions](#beginner-level-questions)
2. [Intermediate Level Questions](#intermediate-level-questions)
3. [Advanced Level Questions](#advanced-level-questions)
4. [System Design Questions](#system-design-questions)
5. [Performance and Optimization Questions](#performance-and-optimization-questions)

## Beginner Level Questions

### Q1: What is Go and what are its key features?

**Answer:** Go (Golang) is a statically typed, compiled programming language designed at Google. Key features include:
- Fast compilation
- Built-in concurrency support with goroutines
- Garbage collection
- Strong typing
- Simple syntax
- Cross-platform compilation
- Excellent standard library

### Q2: Explain the difference between goroutines and threads.

**Answer:**
- **Goroutines** are lightweight, managed by Go runtime, have dynamic stack size (starts at 2KB), and are multiplexed onto OS threads
- **OS Threads** are heavier (usually 1-2MB stack), managed by OS kernel, and have direct 1:1 mapping with kernel threads
- Go can run thousands of goroutines on a few OS threads

*For detailed concurrency examples, see [GO_CONCURRENCY_CHANNELS.md](./GO_CONCURRENCY_CHANNELS.md)*

### Q3: How do you handle errors in Go?

**Answer:** Go uses explicit error handling with multiple return values. Functions return an error as the last return value, and you check if it's nil.

*For detailed error handling patterns, see [GOLANG_INTERVIEW_COMPREHENSIVE.md](./GOLANG_INTERVIEW_COMPREHENSIVE.md)*

### Q4: What is a channel in Go?

**Answer:** Channels are Go's way of communicating between goroutines. They provide synchronization and data exchange. Channels can be buffered or unbuffered, and support directional operations.

*For advanced channel patterns, see [GO_CONCURRENCY_CHANNELS.md](./GO_CONCURRENCY_CHANNELS.md)*

### Q5: Explain the difference between `make` and `new` in Go.

**Answer:**
- **`new(T)`** allocates memory, zeros it, and returns a pointer to the zero value (`*T`)
- **`make(T, args)`** creates slices, maps, and channels, initializes them, and returns the value (not a pointer)
- `new` works with any type, `make` only works with slices, maps, and channels

*For detailed examples, see [GO_FUNDAMENTALS.md](./GO_FUNDAMENTALS.md)*

### Q6: What are interfaces in Go and how do they work?

**Answer:** Interfaces define method signatures. Go uses implicit interface satisfaction - any type that implements the required methods automatically satisfies the interface. This enables powerful polymorphism and loose coupling.

*For comprehensive interface examples, see [GOLANG_INTERVIEW_COMPREHENSIVE.md](./GOLANG_INTERVIEW_COMPREHENSIVE.md)*

### Q7: Explain Go's package system.

**Answer:**
- Packages organize code into reusable units
- Package name should match directory name
- `main` package creates executables
- Exported identifiers start with capital letters
- Use `go mod` for dependency management

*For package examples, see [GO_FUNDAMENTALS.md](./GO_FUNDAMENTALS.md)*

### Q8: What is the zero value in Go?

**Answer:** Every type in Go has a zero value:
- `0` for numeric types
- `false` for boolean
- `""` for strings
- `nil` for pointers, slices, maps, channels, functions, and interfaces

### Q9: How do you create and use slices in Go?

**Answer:** Slices are dynamic arrays with three components: pointer, length, and capacity. You can create them with `make()`, slice literals, or by slicing existing arrays/slices. The `append()` function adds elements and may reallocate if capacity is exceeded.

*For detailed slice examples, see [GO_FUNDAMENTALS.md](./GO_FUNDAMENTALS.md)*

### Q10: What is a pointer in Go and how do you use it?

**Answer:** Pointers hold memory addresses. Use `&` to get the address of a variable and `*` to dereference a pointer. Go has pointer arithmetic restrictions for safety.

*For pointer examples, see [GO_FUNDAMENTALS.md](./GO_FUNDAMENTALS.md)*

[⬆️ Back to Top](#table-of-contents)

## Intermediate Level Questions

### Q11: Explain the difference between buffered and unbuffered channels.

**Answer:**
- **Unbuffered channels** (synchronous): Send blocks until another goroutine receives
- **Buffered channels** (asynchronous): Send only blocks when buffer is full
- Buffered channels allow asynchronous communication and can improve performance in producer-consumer scenarios

*For advanced channel patterns and examples, see [GO_CONCURRENCY_CHANNELS.md](./GO_CONCURRENCY_CHANNELS.md)*

### Q12: What is the select statement and how is it used?

**Answer:** Select allows a goroutine to wait on multiple channel operations. It's similar to switch but for channels. It blocks until one of its cases can proceed, and if multiple are ready, it chooses one randomly.

*For complex select patterns and examples, see [GO_CONCURRENCY_CHANNELS.md](./GO_CONCURRENCY_CHANNELS.md)*

### Q13: How do you prevent goroutine leaks?

**Answer:** Use context for cancellation, ensure goroutines have exit conditions, close channels when done, and use proper synchronization. Always provide a way for goroutines to terminate gracefully.

*For advanced context patterns and goroutine management, see [GO_CONCURRENCY_CHANNELS.md](./GO_CONCURRENCY_CHANNELS.md)*

### Q14: Explain the sync package and its common types.

**Answer:** The sync package provides synchronization primitives:
- **Mutex**: Mutual exclusion lock for protecting shared resources
- **RWMutex**: Read-write mutex allowing multiple readers or one writer
- **WaitGroup**: Waits for a collection of goroutines to finish
- **Once**: Ensures a function is executed only once
- **Pool**: Thread-safe object pool for reusing objects

*For detailed synchronization examples, see [GOLANG_INTERVIEW_COMPREHENSIVE.md](./GOLANG_INTERVIEW_COMPREHENSIVE.md)*

### Q15: How do you implement a worker pool pattern?

**Answer:** Worker pools limit the number of concurrent operations by creating a fixed number of workers that process tasks from a shared channel. This pattern helps control resource usage and prevent system overload.

*For worker pool implementations, see [GO_CONCURRENCY_CHANNELS.md](./GO_CONCURRENCY_CHANNELS.md)*

### Q16: What are method receivers and when would you use pointer vs value receivers?

**Answer:**
- **Value receivers** operate on a copy; use for immutable operations and small structs
- **Pointer receivers** operate on the original; use when modifying the receiver or for large structs
- **Consistency rule**: If any method uses pointer receiver, use it for all methods on that type

*For detailed struct and method examples, see [GO_FUNDAMENTALS.md](./GO_FUNDAMENTALS.md)*

### Q17: How do you handle panics and recovery in Go?

**Answer:** Use `defer` and `recover()` to handle panics gracefully. Panics should be rare and typically indicate programming errors. Recovery allows you to prevent crashes and log errors appropriately.

*For panic/recovery patterns, see [GOLANG_INTERVIEW_COMPREHENSIVE.md](./GOLANG_INTERVIEW_COMPREHENSIVE.md)*

### Q18: Explain the context package and its use cases.

**Answer:** Context provides cancellation, timeouts, and request-scoped values across API boundaries. It's essential for controlling goroutine lifetimes and handling timeouts in distributed systems.

*For advanced context patterns, see [GO_CONCURRENCY_CHANNELS.md](./GO_CONCURRENCY_CHANNELS.md)*

### Q19: How would you implement rate limiting in Go?

**Answer:** Rate limiting can be implemented using token bucket algorithms, sliding window approaches, or simple channel-based throttling. The choice depends on the specific requirements for burst handling and precision.

*For rate limiting implementations, see [GO_WEB_DATABASE.md](./GO_WEB_DATABASE.md)*

### Q20: What is the difference between concurrent and parallel execution?

**Answer:**
- **Concurrent**: Tasks start, run, and complete in overlapping time periods (dealing with multiple things at once)
- **Parallel**: Tasks literally run at the same time on multiple cores (doing multiple things at once)
- Go's goroutines enable concurrency, and the runtime can execute them in parallel if multiple CPU cores are available

*For detailed concurrency vs parallelism examples, see [GO_CONCURRENCY_CHANNELS.md](./GO_CONCURRENCY_CHANNELS.md)*

[⬆️ Back to Top](#table-of-contents)

## Advanced Level Questions

### Q21: Explain Go's memory model and happens-before relationship.

**Answer:** Go's memory model specifies when reads of a variable in one goroutine can observe writes to the same variable in another goroutine. Key principles:
- Programs with data races have undefined behavior
- Synchronization primitives establish happens-before relationships
- Operations within a single goroutine appear to execute in program order
- Without proper synchronization, memory operations can be reordered

*For advanced synchronization patterns, see [GO_CONCURRENCY_CHANNELS.md](./GO_CONCURRENCY_CHANNELS.md)*

### Q22: How does Go's garbage collector work?

**Answer:** Go uses a concurrent, tri-color mark-and-sweep garbage collector:
- **Mark phase**: Traces all reachable objects from roots
- **Sweep phase**: Frees unmarked objects
- **Concurrent**: Runs alongside program execution with minimal stop-the-world pauses
- **Low-latency**: Designed for consistent performance

*For performance optimization and profiling techniques, see [GO_ADVANCED_TOPICS.md](./GO_ADVANCED_TOPICS.md)*

### Q23: Explain Go's scheduler and the GMP model.

**Answer:** Go's scheduler implements the GMP model:
- **G (Goroutine)**: Lightweight thread
- **M (Machine)**: OS thread
- **P (Processor)**: Logical processor with local run queue

Key features include work-stealing, preemptive scheduling, and efficient system call handling.

*For detailed concurrency implementation patterns, see [GO_CONCURRENCY_CHANNELS.md](./GO_CONCURRENCY_CHANNELS.md)*

### Q24: How would you implement a circuit breaker pattern?

**Answer:** Circuit breakers prevent cascading failures by monitoring error rates and temporarily blocking requests when a service is unhealthy. They have three states: closed (normal), open (blocking), and half-open (testing recovery).

*For circuit breaker implementations, see [GO_WEB_DATABASE.md](./GO_WEB_DATABASE.md)*

### Q25: How do you handle graceful shutdown in a Go application?

**Answer:** Graceful shutdown involves catching system signals, stopping new request acceptance, completing in-flight requests, and cleaning up resources. Use context cancellation and timeouts to ensure shutdown completes within acceptable time limits.

*For graceful shutdown patterns, see [GO_WEB_DATABASE.md](./GO_WEB_DATABASE.md)*

### Q26: Explain reflection in Go and when you might use it.

**Answer:** Reflection allows programs to examine and modify objects at runtime. Common use cases include JSON marshaling/unmarshaling, ORM libraries, dependency injection, and testing frameworks. Use sparingly due to performance overhead and reduced type safety.

*For reflection examples and patterns, see [GO_ADVANCED_TOPICS.md](./GO_ADVANCED_TOPICS.md)*

### Q27: How do you implement custom unmarshaling in Go?

**Answer:** Implement the `json.Unmarshaler` interface with an `UnmarshalJSON` method. This allows custom logic for parsing JSON into your types, handling special formats, validation, or field transformations.

*For JSON processing examples, see [GOLANG_INTERVIEW_COMPREHENSIVE.md](./GOLANG_INTERVIEW_COMPREHENSIVE.md)*

### Q28: How would you implement a thread-safe cache with TTL?

**Answer:** Combine `sync.RWMutex` for thread safety with timestamps for TTL. Include a cleanup goroutine to remove expired entries. Consider using sync.Map for read-heavy scenarios or implementing LRU eviction policies.

*For caching implementations, see [GO_ADVANCED_TOPICS.md](./GO_ADVANCED_TOPICS.md)*

### Q29: Explain the differences between sync.Map and regular map with mutex.

**Answer:**
- **Regular map with mutex**: Simple, single lock, better for small maps or write-heavy workloads
- **sync.Map**: Optimized for read-heavy workloads, lock-free reads when possible, more complex implementation

*For concurrent data structure examples, see [GO_CONCURRENCY_CHANNELS.md](./GO_CONCURRENCY_CHANNELS.md)*

### Q30: How do you profile Go applications for performance optimization?

**Answer:** Use Go's built-in profiling tools: `go tool pprof` for CPU and memory profiling, `go tool trace` for execution tracing, and the `/debug/pprof/` HTTP endpoints for live profiling. Always profile before optimizing.

*For profiling techniques and examples, see [GO_ADVANCED_TOPICS.md](./GO_ADVANCED_TOPICS.md)*

[⬆️ Back to Top](#table-of-contents)

## System Design Questions

### Q31: Design a distributed rate limiter using Go.

**Answer:** Use Redis with Lua scripts for atomic operations, implement sliding window or token bucket algorithms, handle network failures gracefully, and consider consistency vs availability trade-offs.

*For distributed system patterns, see [GO_WEB_DATABASE.md](./GO_WEB_DATABASE.md)*

### Q32: Design a load balancer in Go.

**Answer:** Implement health checking, choose appropriate load balancing algorithms (round-robin, weighted, least connections), handle backend failures, and provide circuit breaking functionality.

*For load balancer implementations, see [GO_WEB_DATABASE.md](./GO_WEB_DATABASE.md)*

### Q33: Design a cache system with different eviction policies.

**Answer:** Support multiple eviction policies (LRU, LFU, TTL), provide thread-safe operations, implement size limits, and consider distributed caching for scalability.

*For cache system designs, see [GO_ADVANCED_TOPICS.md](./GO_ADVANCED_TOPICS.md)*

### Q34: Design a message queue system.

**Answer:** Implement persistent storage, support multiple consumers, provide message acknowledgment, handle dead letter queues, and ensure message ordering when required.

*For message queue patterns, see [GO_WEB_DATABASE.md](./GO_WEB_DATABASE.md)*

### Q35: Design a distributed lock service.

**Answer:** Use consensus algorithms or distributed databases, implement lock leasing with renewal, handle network partitions, and provide deadlock detection and recovery.

*For distributed lock patterns, see [GO_CONCURRENCY_CHANNELS.md](./GO_CONCURRENCY_CHANNELS.md)*

[⬆️ Back to Top](#table-of-contents)

## Performance and Optimization Questions

### Q36: How do you implement thread-safe operations in Go?

**Answer:** Use synchronization primitives like mutexes, channels, or atomic operations. Key considerations:
- Use `sync.RWMutex` for read-heavy scenarios
- Implement consistent locking order to prevent deadlocks
- Consider using channels for communication instead of shared memory
- Use `sync/atomic` for simple atomic operations

*For complete thread-safe implementations, see [GOLANG_INTERVIEW_COMPREHENSIVE.md](./GOLANG_INTERVIEW_COMPREHENSIVE.md)*

### Q37: How do you optimize Go application performance?

**Answer:** Key optimization strategies:
- **Profiling**: Use `go tool pprof` for CPU and memory profiling
- **Benchmarking**: Write benchmarks with `testing.B`
- **Memory management**: Use object pools, avoid unnecessary allocations
- **Concurrency**: Optimize goroutine usage and synchronization
- **Algorithm optimization**: Choose efficient data structures and algorithms

*For detailed profiling and optimization techniques, see [GO_ADVANCED_TOPICS.md](./GO_ADVANCED_TOPICS.md)*

### Q38: What are Go's profiling tools and how do you use them?

**Answer:** Go provides several profiling tools:
- **CPU profiling**: `go tool pprof` for identifying hot code paths
- **Memory profiling**: Heap profiling to find memory leaks
- **Trace profiling**: `go tool trace` for analyzing goroutine behavior
- **HTTP endpoints**: `/debug/pprof/` for live profiling
- **Build tags**: `-race` flag for race condition detection

*For hands-on profiling examples, see [GO_ADVANCED_TOPICS.md](./GO_ADVANCED_TOPICS.md)*

### Q39: How do you handle high-concurrency scenarios in Go?

**Answer:** Strategies for high-concurrency:
- **Worker pools**: Limit concurrent operations with worker patterns
- **Rate limiting**: Use token bucket or sliding window algorithms
- **Connection pooling**: Reuse database connections efficiently
- **Buffered channels**: Reduce blocking in producer-consumer scenarios
- **Context cancellation**: Proper resource cleanup and timeout handling

*For advanced concurrency patterns, see [GO_CONCURRENCY_CHANNELS.md](./GO_CONCURRENCY_CHANNELS.md)*

### Q40: What are common Go performance pitfalls?

**Answer:** Common performance issues:
- **Unnecessary goroutines**: Creating too many goroutines
- **Memory leaks**: Not closing channels or canceling contexts
- **String concatenation**: Using `+` instead of `strings.Builder`
- **Interface boxing**: Unnecessary interface{} usage
- **Inefficient JSON parsing**: Not using streaming for large payloads
- **Missing pprof**: Not profiling before optimizing

*For optimization examples and best practices, see [GO_ADVANCED_TOPICS.md](./GO_ADVANCED_TOPICS.md)*

[⬆️ Back to Top](#table-of-contents)