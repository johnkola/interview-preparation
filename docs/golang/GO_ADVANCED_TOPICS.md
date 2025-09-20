# Go Advanced Topics - Reflection, Generics, Performance & Testing

**Scope:** This guide covers advanced Go features including reflection, generics, performance optimization, profiling, and advanced testing strategies. For basic interfaces and error handling examples, see GOLANG_INTERVIEW_COMPREHENSIVE.md. For concurrency patterns, see GO_CONCURRENCY_CHANNELS.md.

## Table of Contents
1. [Reflection and Runtime Inspection](#reflection-and-runtime-inspection)
2. [Generics and Type Parameters](#generics-and-type-parameters)
3. [Performance Optimization and Profiling](#performance-optimization-and-profiling)
4. [Advanced Testing Strategies](#advanced-testing-strategies)
5. [Build Constraints and Code Generation](#build-constraints-and-code-generation)
6. [Unsafe Operations and CGO](#unsafe-operations-and-cgo)

## Reflection and Runtime Inspection

### Advanced Reflection Techniques

```go
package main

import (
    "fmt"
    "reflect"
    "strings"
    "time"
)

// Struct with reflection tags
type User struct {
    ID       int       `json:"id" db:"user_id" validate:"required"`
    Name     string    `json:"name" db:"full_name" validate:"required,min=2"`
    Email    string    `json:"email" db:"email_addr" validate:"required,email"`
    Age      int       `json:"age" db:"age" validate:"min=18,max=120"`
    Created  time.Time `json:"created_at" db:"created_at"`
    Active   bool      `json:"active" db:"is_active"`
    metadata map[string]interface{} `json:"-" db:"-"`
}

// Generic struct validator using reflection
type Validator struct {
    rules map[string][]ValidationRule
}

type ValidationRule func(interface{}) error

func NewValidator() *Validator {
    return &Validator{
        rules: map[string][]ValidationRule{
            "required": {validateRequired},
            "email":    {validateEmail},
            "min":      {validateMin},
            "max":      {validateMax},
        },
    }
}

func (v *Validator) Validate(s interface{}) []error {
    var errors []error
    val := reflect.ValueOf(s)
    typ := reflect.TypeOf(s)

    // Handle pointer
    if val.Kind() == reflect.Ptr {
        val = val.Elem()
        typ = typ.Elem()
    }

    if val.Kind() != reflect.Struct {
        return []error{fmt.Errorf("validation target must be a struct")}
    }

    for i := 0; i < val.NumField(); i++ {
        field := val.Field(i)
        fieldType := typ.Field(i)

        // Skip unexported fields
        if !field.CanInterface() {
            continue
        }

        validateTag := fieldType.Tag.Get("validate")
        if validateTag == "" {
            continue
        }

        rules := strings.Split(validateTag, ",")
        for _, rule := range rules {
            if err := v.validateField(field.Interface(), rule, fieldType.Name); err != nil {
                errors = append(errors, err)
            }
        }
    }

    return errors
}

func (v *Validator) validateField(value interface{}, rule, fieldName string) error {
    parts := strings.Split(rule, "=")
    ruleName := parts[0]

    validators, exists := v.rules[ruleName]
    if !exists {
        return fmt.Errorf("unknown validation rule: %s", ruleName)
    }

    for _, validator := range validators {
        if err := validator(value); err != nil {
            return fmt.Errorf("%s: %v", fieldName, err)
        }
    }

    return nil
}

// Validation rule implementations
func validateRequired(value interface{}) error {
    v := reflect.ValueOf(value)
    if v.Kind() == reflect.Ptr && v.IsNil() {
        return fmt.Errorf("is required")
    }

    switch v.Kind() {
    case reflect.String:
        if v.String() == "" {
            return fmt.Errorf("is required")
        }
    case reflect.Slice, reflect.Map, reflect.Array:
        if v.Len() == 0 {
            return fmt.Errorf("is required")
        }
    case reflect.Int, reflect.Int8, reflect.Int16, reflect.Int32, reflect.Int64:
        if v.Int() == 0 {
            return fmt.Errorf("is required")
        }
    }

    return nil
}

func validateEmail(value interface{}) error {
    str, ok := value.(string)
    if !ok {
        return fmt.Errorf("must be a string")
    }

    if !strings.Contains(str, "@") || !strings.Contains(str, ".") {
        return fmt.Errorf("must be a valid email")
    }

    return nil
}

func validateMin(value interface{}) error {
    // Simplified min validation
    return nil
}

func validateMax(value interface{}) error {
    // Simplified max validation
    return nil
}

// Dynamic struct creation
func createDynamicStruct(fields map[string]reflect.Type) reflect.Type {
    var structFields []reflect.StructField

    for name, typ := range fields {
        field := reflect.StructField{
            Name: strings.Title(name),
            Type: typ,
            Tag:  reflect.StructTag(fmt.Sprintf(`json:"%s"`, name)),
        }
        structFields = append(structFields, field)
    }

    return reflect.StructOf(structFields)
}

// Method invocation via reflection
func callMethodByName(obj interface{}, methodName string, args ...interface{}) ([]reflect.Value, error) {
    objValue := reflect.ValueOf(obj)
    method := objValue.MethodByName(methodName)

    if !method.IsValid() {
        return nil, fmt.Errorf("method %s not found", methodName)
    }

    // Convert arguments to reflect.Value
    argValues := make([]reflect.Value, len(args))
    for i, arg := range args {
        argValues[i] = reflect.ValueOf(arg)
    }

    return method.Call(argValues), nil
}

// Reflection-based deep copy
func deepCopy(src interface{}) interface{} {
    srcVal := reflect.ValueOf(src)
    return deepCopyValue(srcVal).Interface()
}

func deepCopyValue(src reflect.Value) reflect.Value {
    switch src.Kind() {
    case reflect.Ptr:
        if src.IsNil() {
            return reflect.Zero(src.Type())
        }
        dst := reflect.New(src.Type().Elem())
        dst.Elem().Set(deepCopyValue(src.Elem()))
        return dst

    case reflect.Struct:
        dst := reflect.New(src.Type()).Elem()
        for i := 0; i < src.NumField(); i++ {
            if dst.Field(i).CanSet() {
                dst.Field(i).Set(deepCopyValue(src.Field(i)))
            }
        }
        return dst

    case reflect.Slice:
        if src.IsNil() {
            return reflect.Zero(src.Type())
        }
        dst := reflect.MakeSlice(src.Type(), src.Len(), src.Cap())
        for i := 0; i < src.Len(); i++ {
            dst.Index(i).Set(deepCopyValue(src.Index(i)))
        }
        return dst

    case reflect.Map:
        if src.IsNil() {
            return reflect.Zero(src.Type())
        }
        dst := reflect.MakeMap(src.Type())
        for _, key := range src.MapKeys() {
            dst.SetMapIndex(key, deepCopyValue(src.MapIndex(key)))
        }
        return dst

    default:
        return src
    }
}
```

### Reflection-Based ORM Example

```go
// Simple ORM using reflection
type SimpleORM struct {
    tableName string
    fields    map[string]reflect.StructField
}

func NewSimpleORM(model interface{}) *SimpleORM {
    modelType := reflect.TypeOf(model)
    if modelType.Kind() == reflect.Ptr {
        modelType = modelType.Elem()
    }

    orm := &SimpleORM{
        tableName: strings.ToLower(modelType.Name()) + "s",
        fields:    make(map[string]reflect.StructField),
    }

    for i := 0; i < modelType.NumField(); i++ {
        field := modelType.Field(i)
        dbTag := field.Tag.Get("db")
        if dbTag != "" && dbTag != "-" {
            orm.fields[dbTag] = field
        }
    }

    return orm
}

func (orm *SimpleORM) GenerateSelectSQL() string {
    var columns []string
    for dbColumn := range orm.fields {
        columns = append(columns, dbColumn)
    }
    return fmt.Sprintf("SELECT %s FROM %s", strings.Join(columns, ", "), orm.tableName)
}

func (orm *SimpleORM) GenerateInsertSQL(model interface{}) (string, []interface{}) {
    modelValue := reflect.ValueOf(model)
    if modelValue.Kind() == reflect.Ptr {
        modelValue = modelValue.Elem()
    }

    var columns []string
    var placeholders []string
    var values []interface{}

    i := 1
    for dbColumn, field := range orm.fields {
        fieldValue := modelValue.FieldByName(field.Name)
        if fieldValue.IsValid() && fieldValue.CanInterface() {
            columns = append(columns, dbColumn)
            placeholders = append(placeholders, fmt.Sprintf("$%d", i))
            values = append(values, fieldValue.Interface())
            i++
        }
    }

    sql := fmt.Sprintf("INSERT INTO %s (%s) VALUES (%s)",
        orm.tableName,
        strings.Join(columns, ", "),
        strings.Join(placeholders, ", "))

    return sql, values
}

func (orm *SimpleORM) ScanRow(rows interface{}, dest interface{}) error {
    // This would implement row scanning using reflection
    // Simplified for brevity
    return nil
}
```

[⬆️ Back to Top](#table-of-contents)

## Generics and Type Parameters

### Advanced Generic Patterns

```go
package main

import (
    "fmt"
    "constraints"
    "sort"
)

// Generic constraint interfaces
type Numeric interface {
    constraints.Integer | constraints.Float
}

type Comparable[T any] interface {
    Compare(T) int
}

// Generic data structures

// Generic Stack
type Stack[T any] struct {
    items []T
}

func NewStack[T any]() *Stack[T] {
    return &Stack[T]{items: make([]T, 0)}
}

func (s *Stack[T]) Push(item T) {
    s.items = append(s.items, item)
}

func (s *Stack[T]) Pop() (T, bool) {
    if len(s.items) == 0 {
        var zero T
        return zero, false
    }
    index := len(s.items) - 1
    item := s.items[index]
    s.items = s.items[:index]
    return item, true
}

func (s *Stack[T]) Peek() (T, bool) {
    if len(s.items) == 0 {
        var zero T
        return zero, false
    }
    return s.items[len(s.items)-1], true
}

func (s *Stack[T]) Size() int {
    return len(s.items)
}

// Generic Binary Tree
type TreeNode[T Comparable[T]] struct {
    Value T
    Left  *TreeNode[T]
    Right *TreeNode[T]
}

type BinaryTree[T Comparable[T]] struct {
    root *TreeNode[T]
    size int
}

func NewBinaryTree[T Comparable[T]]() *BinaryTree[T] {
    return &BinaryTree[T]{}
}

func (bt *BinaryTree[T]) Insert(value T) {
    bt.root = bt.insertNode(bt.root, value)
    bt.size++
}

func (bt *BinaryTree[T]) insertNode(node *TreeNode[T], value T) *TreeNode[T] {
    if node == nil {
        return &TreeNode[T]{Value: value}
    }

    if value.Compare(node.Value) < 0 {
        node.Left = bt.insertNode(node.Left, value)
    } else {
        node.Right = bt.insertNode(node.Right, value)
    }

    return node
}

func (bt *BinaryTree[T]) Search(value T) bool {
    return bt.searchNode(bt.root, value)
}

func (bt *BinaryTree[T]) searchNode(node *TreeNode[T], value T) bool {
    if node == nil {
        return false
    }

    comparison := value.Compare(node.Value)
    if comparison == 0 {
        return true
    } else if comparison < 0 {
        return bt.searchNode(node.Left, value)
    } else {
        return bt.searchNode(node.Right, value)
    }
}

// Generic Map-Reduce functions
func Map[T, U any](slice []T, mapper func(T) U) []U {
    result := make([]U, len(slice))
    for i, item := range slice {
        result[i] = mapper(item)
    }
    return result
}

func Filter[T any](slice []T, predicate func(T) bool) []T {
    var result []T
    for _, item := range slice {
        if predicate(item) {
            result = append(result, item)
        }
    }
    return result
}

func Reduce[T, U any](slice []T, initial U, reducer func(U, T) U) U {
    result := initial
    for _, item := range slice {
        result = reducer(result, item)
    }
    return result
}

// Generic Optional type
type Optional[T any] struct {
    value *T
}

func Some[T any](value T) Optional[T] {
    return Optional[T]{value: &value}
}

func None[T any]() Optional[T] {
    return Optional[T]{value: nil}
}

func (o Optional[T]) IsPresent() bool {
    return o.value != nil
}

func (o Optional[T]) Get() T {
    if o.value == nil {
        panic("attempting to get value from empty Optional")
    }
    return *o.value
}

func (o Optional[T]) GetOrElse(defaultValue T) T {
    if o.value == nil {
        return defaultValue
    }
    return *o.value
}

func (o Optional[T]) Map(mapper func(T) T) Optional[T] {
    if o.value == nil {
        return None[T]()
    }
    return Some(mapper(*o.value))
}

// Generic Result type for error handling
type Result[T any] struct {
    value T
    err   error
}

func Ok[T any](value T) Result[T] {
    return Result[T]{value: value}
}

func Err[T any](err error) Result[T] {
    var zero T
    return Result[T]{value: zero, err: err}
}

func (r Result[T]) IsOk() bool {
    return r.err == nil
}

func (r Result[T]) IsErr() bool {
    return r.err != nil
}

func (r Result[T]) Unwrap() T {
    if r.err != nil {
        panic(fmt.Sprintf("called Unwrap on an Err value: %v", r.err))
    }
    return r.value
}

func (r Result[T]) UnwrapOr(defaultValue T) T {
    if r.err != nil {
        return defaultValue
    }
    return r.value
}

func (r Result[T]) Map(mapper func(T) T) Result[T] {
    if r.err != nil {
        return Err[T](r.err)
    }
    return Ok(mapper(r.value))
}

// Generic cache with type constraints
type Cache[K comparable, V any] struct {
    data     map[K]V
    maxSize  int
    accessed map[K]int64
    counter  int64
}

func NewCache[K comparable, V any](maxSize int) *Cache[K, V] {
    return &Cache[K, V]{
        data:     make(map[K]V),
        maxSize:  maxSize,
        accessed: make(map[K]int64),
        counter:  0,
    }
}

func (c *Cache[K, V]) Put(key K, value V) {
    if len(c.data) >= c.maxSize {
        c.evictLRU()
    }

    c.data[key] = value
    c.counter++
    c.accessed[key] = c.counter
}

func (c *Cache[K, V]) Get(key K) (V, bool) {
    value, exists := c.data[key]
    if exists {
        c.counter++
        c.accessed[key] = c.counter
    }
    return value, exists
}

func (c *Cache[K, V]) evictLRU() {
    var oldestKey K
    var oldestTime int64 = c.counter + 1

    for key, time := range c.accessed {
        if time < oldestTime {
            oldestTime = time
            oldestKey = key
        }
    }

    delete(c.data, oldestKey)
    delete(c.accessed, oldestKey)
}

// Example type implementing Comparable
type IntValue int

func (iv IntValue) Compare(other IntValue) int {
    if iv < other {
        return -1
    } else if iv > other {
        return 1
    }
    return 0
}
```

[⬆️ Back to Top](#table-of-contents)

## Performance Optimization and Profiling

### CPU and Memory Profiling

```go
package main

import (
    "context"
    "fmt"
    "log"
    "net/http"
    _ "net/http/pprof"
    "os"
    "runtime"
    "runtime/pprof"
    "sync"
    "time"
)

// Performance monitoring utilities
type PerformanceMonitor struct {
    metrics map[string]*Metric
    mu      sync.RWMutex
}

type Metric struct {
    Count    int64
    Duration time.Duration
    MinTime  time.Duration
    MaxTime  time.Duration
    mu       sync.Mutex
}

func NewPerformanceMonitor() *PerformanceMonitor {
    return &PerformanceMonitor{
        metrics: make(map[string]*Metric),
    }
}

func (pm *PerformanceMonitor) Time(name string, fn func()) {
    start := time.Now()
    fn()
    duration := time.Since(start)

    pm.recordMetric(name, duration)
}

func (pm *PerformanceMonitor) recordMetric(name string, duration time.Duration) {
    pm.mu.RLock()
    metric, exists := pm.metrics[name]
    pm.mu.RUnlock()

    if !exists {
        pm.mu.Lock()
        if metric, exists = pm.metrics[name]; !exists {
            metric = &Metric{
                MinTime: duration,
                MaxTime: duration,
            }
            pm.metrics[name] = metric
        }
        pm.mu.Unlock()
    }

    metric.mu.Lock()
    metric.Count++
    metric.Duration += duration
    if duration < metric.MinTime {
        metric.MinTime = duration
    }
    if duration > metric.MaxTime {
        metric.MaxTime = duration
    }
    metric.mu.Unlock()
}

func (pm *PerformanceMonitor) Report() {
    pm.mu.RLock()
    defer pm.mu.RUnlock()

    fmt.Println("Performance Report:")
    fmt.Println("==================")

    for name, metric := range pm.metrics {
        metric.mu.Lock()
        avgTime := metric.Duration / time.Duration(metric.Count)
        fmt.Printf("%s:\n", name)
        fmt.Printf("  Count: %d\n", metric.Count)
        fmt.Printf("  Total: %v\n", metric.Duration)
        fmt.Printf("  Average: %v\n", avgTime)
        fmt.Printf("  Min: %v\n", metric.MinTime)
        fmt.Printf("  Max: %v\n", metric.MaxTime)
        fmt.Println()
        metric.mu.Unlock()
    }
}

// Memory pool for reducing garbage collection pressure
type BytePool struct {
    pools map[int]*sync.Pool
    sizes []int
}

func NewBytePool(sizes []int) *BytePool {
    bp := &BytePool{
        pools: make(map[int]*sync.Pool),
        sizes: sizes,
    }

    for _, size := range sizes {
        size := size // capture loop variable
        bp.pools[size] = &sync.Pool{
            New: func() interface{} {
                return make([]byte, size)
            },
        }
    }

    return bp
}

func (bp *BytePool) Get(size int) []byte {
    // Find the smallest pool that can accommodate the size
    for _, poolSize := range bp.sizes {
        if poolSize >= size {
            buf := bp.pools[poolSize].Get().([]byte)
            return buf[:size] // Return slice with requested size
        }
    }

    // If no pool is large enough, allocate directly
    return make([]byte, size)
}

func (bp *BytePool) Put(buf []byte) {
    capacity := cap(buf)

    // Only return to pool if it matches a pool size
    if pool, exists := bp.pools[capacity]; exists {
        // Reset the slice
        buf = buf[:capacity]
        for i := range buf {
            buf[i] = 0
        }
        pool.Put(buf)
    }
}

// CPU profiling functions
func StartCPUProfile(filename string) func() {
    f, err := os.Create(filename)
    if err != nil {
        log.Fatal("could not create CPU profile: ", err)
    }

    if err := pprof.StartCPUProfile(f); err != nil {
        log.Fatal("could not start CPU profile: ", err)
    }

    return func() {
        pprof.StopCPUProfile()
        f.Close()
    }
}

func WriteMemProfile(filename string) {
    f, err := os.Create(filename)
    if err != nil {
        log.Fatal("could not create memory profile: ", err)
    }
    defer f.Close()

    runtime.GC() // get up-to-date statistics
    if err := pprof.WriteHeapProfile(f); err != nil {
        log.Fatal("could not write memory profile: ", err)
    }
}

// Benchmark utilities
type Benchmark struct {
    name string
    fn   func()
}

func NewBenchmark(name string, fn func()) *Benchmark {
    return &Benchmark{name: name, fn: fn}
}

func (b *Benchmark) Run(iterations int) {
    fmt.Printf("Running benchmark: %s\n", b.name)

    // Warmup
    for i := 0; i < iterations/10; i++ {
        b.fn()
    }

    // Force GC before actual benchmark
    runtime.GC()

    var memBefore, memAfter runtime.MemStats
    runtime.ReadMemStats(&memBefore)

    start := time.Now()
    for i := 0; i < iterations; i++ {
        b.fn()
    }
    duration := time.Since(start)

    runtime.ReadMemStats(&memAfter)

    fmt.Printf("Iterations: %d\n", iterations)
    fmt.Printf("Total time: %v\n", duration)
    fmt.Printf("Average time: %v\n", duration/time.Duration(iterations))
    fmt.Printf("Operations/second: %.2f\n", float64(iterations)/duration.Seconds())
    fmt.Printf("Memory allocated: %d bytes\n", memAfter.TotalAlloc-memBefore.TotalAlloc)
    fmt.Printf("GC runs: %d\n", memAfter.NumGC-memBefore.NumGC)
    fmt.Println()
}

// HTTP profiling setup
func setupProfiling() {
    go func() {
        log.Println("Starting profiling server on :6060")
        log.Println(http.ListenAndServe("localhost:6060", nil))
    }()
}

// Example of performance optimization
func optimizedStringConcatenation(strings []string) string {
    // Calculate total length first
    totalLen := 0
    for _, s := range strings {
        totalLen += len(s)
    }

    // Pre-allocate builder with known capacity
    var builder strings.Builder
    builder.Grow(totalLen)

    for _, s := range strings {
        builder.WriteString(s)
    }

    return builder.String()
}
```

[⬆️ Back to Top](#table-of-contents)

## Advanced Testing Strategies

### Property-Based Testing

```go
package main

import (
    "fmt"
    "math/rand"
    "reflect"
    "testing"
    "time"
)

// Property-based testing framework (simplified)
type Property struct {
    name        string
    testFunc    interface{}
    numTests    int
    maxSize     int
    generators  []Generator
}

type Generator func(rand *rand.Rand, size int) interface{}

func NewProperty(name string, testFunc interface{}) *Property {
    return &Property{
        name:     name,
        testFunc: testFunc,
        numTests: 100,
        maxSize:  100,
    }
}

func (p *Property) WithGenerators(generators ...Generator) *Property {
    p.generators = generators
    return p
}

func (p *Property) WithNumTests(num int) *Property {
    p.numTests = num
    return p
}

func (p *Property) Check(t *testing.T) {
    funcValue := reflect.ValueOf(p.testFunc)
    funcType := funcValue.Type()

    if len(p.generators) != funcType.NumIn() {
        t.Fatalf("Number of generators (%d) doesn't match function parameters (%d)",
                 len(p.generators), funcType.NumIn())
    }

    rng := rand.New(rand.NewSource(time.Now().UnixNano()))

    for i := 0; i < p.numTests; i++ {
        // Generate inputs
        inputs := make([]reflect.Value, len(p.generators))
        for j, gen := range p.generators {
            value := gen(rng, p.maxSize)
            inputs[j] = reflect.ValueOf(value)
        }

        // Call function
        results := funcValue.Call(inputs)

        // Check result (assumes function returns bool)
        if len(results) > 0 && results[0].Kind() == reflect.Bool {
            if !results[0].Bool() {
                t.Errorf("Property '%s' failed with inputs: %v", p.name, inputs)
            }
        }
    }
}

// Generators for common types
func IntGenerator(min, max int) Generator {
    return func(rng *rand.Rand, size int) interface{} {
        return rng.Intn(max-min+1) + min
    }
}

func StringGenerator(charset string, maxLen int) Generator {
    return func(rng *rand.Rand, size int) interface{} {
        length := rng.Intn(maxLen + 1)
        result := make([]byte, length)
        for i := range result {
            result[i] = charset[rng.Intn(len(charset))]
        }
        return string(result)
    }
}

func SliceGenerator(elemGen Generator, maxLen int) Generator {
    return func(rng *rand.Rand, size int) interface{} {
        length := rng.Intn(maxLen + 1)
        result := make([]interface{}, length)
        for i := range result {
            result[i] = elemGen(rng, size)
        }
        return result
    }
}

// Example property tests
func TestReverseProperty(t *testing.T) {
    reverseIsInvolution := func(s string) bool {
        return reverse(reverse(s)) == s
    }

    prop := NewProperty("reverse is involution", reverseIsInvolution).
        WithGenerators(StringGenerator("abcdefghijklmnopqrstuvwxyz", 50)).
        WithNumTests(1000)

    prop.Check(t)
}

func TestSortProperty(t *testing.T) {
    sortIsIdempotent := func(slice []int) bool {
        sorted1 := sortCopy(slice)
        sorted2 := sortCopy(sorted1)
        return reflect.DeepEqual(sorted1, sorted2)
    }

    prop := NewProperty("sort is idempotent", sortIsIdempotent).
        WithGenerators(SliceIntGenerator(20)).
        WithNumTests(500)

    prop.Check(t)
}

func SliceIntGenerator(maxLen int) Generator {
    return func(rng *rand.Rand, size int) interface{} {
        length := rng.Intn(maxLen + 1)
        result := make([]int, length)
        for i := range result {
            result[i] = rng.Intn(1000)
        }
        return result
    }
}

// Fuzzing utilities
type FuzzTest struct {
    name     string
    testFunc func([]byte) error
    corpus   [][]byte
}

func NewFuzzTest(name string, testFunc func([]byte) error) *FuzzTest {
    return &FuzzTest{
        name:     name,
        testFunc: testFunc,
        corpus:   make([][]byte, 0),
    }
}

func (ft *FuzzTest) AddCorpus(data ...[]byte) *FuzzTest {
    ft.corpus = append(ft.corpus, data...)
    return ft
}

func (ft *FuzzTest) Run(t *testing.T, iterations int) {
    rng := rand.New(rand.NewSource(time.Now().UnixNano()))

    // Test with corpus first
    for i, data := range ft.corpus {
        if err := ft.testFunc(data); err != nil {
            t.Errorf("Fuzz test '%s' failed on corpus[%d]: %v", ft.name, i, err)
        }
    }

    // Generate random inputs
    for i := 0; i < iterations; i++ {
        data := make([]byte, rng.Intn(1000))
        rng.Read(data)

        if err := ft.testFunc(data); err != nil {
            t.Errorf("Fuzz test '%s' failed on iteration %d with input %x: %v",
                     ft.name, i, data, err)
        }
    }
}

// Test doubles and mocking
type MockHTTPClient struct {
    responses map[string]*HTTPResponse
    calls     []HTTPCall
}

type HTTPResponse struct {
    StatusCode int
    Body       []byte
    Error      error
}

type HTTPCall struct {
    URL    string
    Method string
    Body   []byte
}

func NewMockHTTPClient() *MockHTTPClient {
    return &MockHTTPClient{
        responses: make(map[string]*HTTPResponse),
        calls:     make([]HTTPCall, 0),
    }
}

func (m *MockHTTPClient) SetResponse(url string, response *HTTPResponse) {
    m.responses[url] = response
}

func (m *MockHTTPClient) Get(url string) (*HTTPResponse, error) {
    m.calls = append(m.calls, HTTPCall{URL: url, Method: "GET"})

    if response, exists := m.responses[url]; exists {
        return response, response.Error
    }

    return &HTTPResponse{StatusCode: 404}, fmt.Errorf("no mock response for %s", url)
}

func (m *MockHTTPClient) GetCalls() []HTTPCall {
    return m.calls
}

// Example usage of mock
func TestAPIClient(t *testing.T) {
    // Setup mock
    mock := NewMockHTTPClient()
    mock.SetResponse("https://api.example.com/users/123", &HTTPResponse{
        StatusCode: 200,
        Body:       []byte(`{"id": 123, "name": "John Doe"}`),
    })

    // Test with mock
    client := &APIClient{httpClient: mock}
    user, err := client.GetUser(123)

    if err != nil {
        t.Fatalf("Expected no error, got %v", err)
    }

    if user.ID != 123 {
        t.Errorf("Expected user ID 123, got %d", user.ID)
    }

    // Verify interactions
    calls := mock.GetCalls()
    if len(calls) != 1 {
        t.Errorf("Expected 1 HTTP call, got %d", len(calls))
    }
}

// Table-driven tests with subtests
func TestMathOperations(t *testing.T) {
    tests := []struct {
        name     string
        a, b     int
        expected int
        op       func(int, int) int
    }{
        {"add positive", 2, 3, 5, add},
        {"add negative", -2, 3, 1, add},
        {"add zero", 0, 5, 5, add},
        {"multiply positive", 3, 4, 12, multiply},
        {"multiply by zero", 5, 0, 0, multiply},
        {"multiply negative", -2, 3, -6, multiply},
    }

    for _, tt := range tests {
        t.Run(tt.name, func(t *testing.T) {
            result := tt.op(tt.a, tt.b)
            if result != tt.expected {
                t.Errorf("Expected %d, got %d", tt.expected, result)
            }
        })
    }
}

// Helper functions for examples
func reverse(s string) string {
    runes := []rune(s)
    for i, j := 0, len(runes)-1; i < j; i, j = i+1, j-1 {
        runes[i], runes[j] = runes[j], runes[i]
    }
    return string(runes)
}

func sortCopy(slice []int) []int {
    result := make([]int, len(slice))
    copy(result, slice)
    sort.Ints(result)
    return result
}

func add(a, b int) int { return a + b }
func multiply(a, b int) int { return a * b }

type APIClient struct {
    httpClient interface{ Get(string) (*HTTPResponse, error) }
}

type User struct {
    ID   int    `json:"id"`
    Name string `json:"name"`
}

func (c *APIClient) GetUser(id int) (*User, error) {
    url := fmt.Sprintf("https://api.example.com/users/%d", id)
    resp, err := c.httpClient.Get(url)
    if err != nil {
        return nil, err
    }

    // Parse JSON response (simplified)
    return &User{ID: id, Name: "John Doe"}, nil
}
```

[⬆️ Back to Top](#table-of-contents)

## Build Constraints and Code Generation

### Build Tags and Conditional Compilation

```go
// +build integration

package integration

// This file will only be compiled when building with the "integration" tag:
// go test -tags=integration

import (
    "database/sql"
    "testing"
)

func TestDatabaseIntegration(t *testing.T) {
    // Integration test code here
}
```

```go
// +build !integration

package unit

// This file will be compiled when NOT building with the "integration" tag

import "testing"

func TestUnitFunction(t *testing.T) {
    // Unit test code here
}
```

### Code Generation with go:generate

```go
//go:generate stringer -type=Status
//go:generate mockgen -source=interfaces.go -destination=mocks/mock_interfaces.go

package main

type Status int

const (
    Active Status = iota
    Inactive
    Pending
    Suspended
)

// This will generate a String() method for Status type when running:
// go generate
```

[⬆️ Back to Top](#table-of-contents)

## Unsafe Operations and CGO

### Unsafe Pointer Operations

```go
package main

import (
    "fmt"
    "unsafe"
)

// WARNING: These examples are for educational purposes only
// Unsafe operations can cause memory corruption and crashes

func unsafeStringToBytes() {
    str := "Hello, World!"

    // Convert string to []byte without allocation (unsafe!)
    strHeader := (*reflect.StringHeader)(unsafe.Pointer(&str))
    byteHeader := reflect.SliceHeader{
        Data: strHeader.Data,
        Len:  strHeader.Len,
        Cap:  strHeader.Len,
    }

    bytes := *(*[]byte)(unsafe.Pointer(&byteHeader))
    fmt.Printf("String as bytes: %v\n", bytes)

    // WARNING: Modifying this slice would corrupt the string!
}

func unsafeBytesToString() {
    bytes := []byte("Hello, unsafe world!")

    // Convert []byte to string without allocation (unsafe!)
    return *(*string)(unsafe.Pointer(&bytes))
}

// Atomic operations with unsafe pointers
type LockFreeQueue struct {
    head unsafe.Pointer
    tail unsafe.Pointer
}

type queueNode struct {
    data interface{}
    next unsafe.Pointer
}

// CGO example (requires C compiler)
/*
#include <stdlib.h>
#include <string.h>

char* reverseString(char* str) {
    int len = strlen(str);
    char* result = (char*)malloc(len + 1);
    for(int i = 0; i < len; i++) {
        result[i] = str[len - 1 - i];
    }
    result[len] = '\0';
    return result;
}
*/
import "C"

func cgoReverseString(s string) string {
    cstr := C.CString(s)
    defer C.free(unsafe.Pointer(cstr))

    result := C.reverseString(cstr)
    defer C.free(unsafe.Pointer(result))

    return C.GoString(result)
}
```

**Note:** This guide focuses on advanced Go features. For basic language syntax and concepts, see GO_FUNDAMENTALS.md. For web development and database patterns, see GO_WEB_DATABASE.md. For concurrency patterns, see GO_CONCURRENCY_CHANNELS.md.

[⬆️ Back to Top](#table-of-contents)