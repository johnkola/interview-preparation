# Go Web Development and Database - Advanced Patterns

**Scope:** This guide covers advanced web development patterns including middleware, WebSockets, gRPC, GraphQL, and sophisticated database techniques like connection pooling, transactions, and migrations. For basic HTTP server examples, see GOLANG_INTERVIEW_COMPREHENSIVE.md. For concurrency patterns, see GO_CONCURRENCY_CHANNELS.md.

## Table of Contents
1. [Advanced Middleware Patterns](#advanced-middleware-patterns)
2. [WebSocket Implementation](#websocket-implementation)
3. [gRPC Services](#grpc-services)
4. [GraphQL Integration](#graphql-integration)
5. [Advanced Database Patterns](#advanced-database-patterns)
6. [Connection Pooling and Transactions](#connection-pooling-and-transactions)

## Advanced Middleware Patterns

### Chain-able Middleware System

```go
package main

import (
    "context"
    "fmt"
    "log"
    "net/http"
    "runtime"
    "strings"
    "time"
)

// Middleware represents a middleware function
type Middleware func(http.Handler) http.Handler

// MiddlewareChain represents a chain of middleware
type MiddlewareChain struct {
    middlewares []Middleware
}

// NewMiddlewareChain creates a new middleware chain
func NewMiddlewareChain(middlewares ...Middleware) *MiddlewareChain {
    return &MiddlewareChain{middlewares: middlewares}
}

// Add adds a middleware to the chain
func (mc *MiddlewareChain) Add(middleware Middleware) *MiddlewareChain {
    mc.middlewares = append(mc.middlewares, middleware)
    return mc
}

// Apply applies all middlewares to a handler
func (mc *MiddlewareChain) Apply(handler http.Handler) http.Handler {
    for i := len(mc.middlewares) - 1; i >= 0; i-- {
        handler = mc.middlewares[i](handler)
    }
    return handler
}

// Advanced logging middleware with request tracking
func RequestTrackingMiddleware() Middleware {
    return func(next http.Handler) http.Handler {
        return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
            requestID := generateRequestID()

            // Add request ID to context
            ctx := context.WithValue(r.Context(), "requestID", requestID)
            r = r.WithContext(ctx)

            // Add request ID to response headers
            w.Header().Set("X-Request-ID", requestID)

            // Wrap response writer to capture status code
            wrapper := &responseWriter{
                ResponseWriter: w,
                statusCode:     http.StatusOK,
            }

            start := time.Now()

            defer func() {
                duration := time.Since(start)
                log.Printf("[%s] %s %s %d %v",
                    requestID, r.Method, r.URL.Path, wrapper.statusCode, duration)
            }()

            next.ServeHTTP(wrapper, r)
        })
    }
}

type responseWriter struct {
    http.ResponseWriter
    statusCode int
}

func (rw *responseWriter) WriteHeader(code int) {
    rw.statusCode = code
    rw.ResponseWriter.WriteHeader(code)
}

// Rate limiting middleware with different strategies
type RateLimitStrategy interface {
    Allow(key string) bool
    Reset(key string)
}

// Token bucket rate limiter
type TokenBucketLimiter struct {
    buckets     map[string]*tokenBucket
    rate        int           // tokens per second
    capacity    int           // bucket capacity
    cleanupTime time.Duration
    mu          sync.RWMutex
}

type tokenBucket struct {
    tokens    int
    lastToken time.Time
    mu        sync.Mutex
}

func NewTokenBucketLimiter(rate, capacity int) *TokenBucketLimiter {
    limiter := &TokenBucketLimiter{
        buckets:     make(map[string]*tokenBucket),
        rate:        rate,
        capacity:    capacity,
        cleanupTime: 10 * time.Minute,
    }

    // Start cleanup goroutine
    go limiter.cleanup()

    return limiter
}

func (tbl *TokenBucketLimiter) Allow(key string) bool {
    tbl.mu.RLock()
    bucket, exists := tbl.buckets[key]
    tbl.mu.RUnlock()

    if !exists {
        tbl.mu.Lock()
        if bucket, exists = tbl.buckets[key]; !exists {
            bucket = &tokenBucket{
                tokens:    tbl.capacity,
                lastToken: time.Now(),
            }
            tbl.buckets[key] = bucket
        }
        tbl.mu.Unlock()
    }

    bucket.mu.Lock()
    defer bucket.mu.Unlock()

    now := time.Now()
    elapsed := now.Sub(bucket.lastToken)

    // Add tokens based on elapsed time
    tokensToAdd := int(elapsed.Seconds()) * tbl.rate
    bucket.tokens = min(bucket.tokens+tokensToAdd, tbl.capacity)
    bucket.lastToken = now

    if bucket.tokens > 0 {
        bucket.tokens--
        return true
    }

    return false
}

func (tbl *TokenBucketLimiter) Reset(key string) {
    tbl.mu.Lock()
    defer tbl.mu.Unlock()
    delete(tbl.buckets, key)
}

func (tbl *TokenBucketLimiter) cleanup() {
    ticker := time.NewTicker(tbl.cleanupTime)
    defer ticker.Stop()

    for range ticker.C {
        tbl.mu.Lock()
        now := time.Now()
        for key, bucket := range tbl.buckets {
            bucket.mu.Lock()
            if now.Sub(bucket.lastToken) > tbl.cleanupTime {
                delete(tbl.buckets, key)
            }
            bucket.mu.Unlock()
        }
        tbl.mu.Unlock()
    }
}

func RateLimitMiddleware(strategy RateLimitStrategy, keyFunc func(*http.Request) string) Middleware {
    return func(next http.Handler) http.Handler {
        return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
            key := keyFunc(r)

            if !strategy.Allow(key) {
                http.Error(w, "Rate limit exceeded", http.StatusTooManyRequests)
                return
            }

            next.ServeHTTP(w, r)
        })
    }
}

// Circuit breaker middleware
type CircuitBreakerMiddleware struct {
    maxFailures int
    timeout     time.Duration
    failures    int64
    lastFailure time.Time
    state       int32 // 0=closed, 1=open, 2=half-open
    mu          sync.RWMutex
}

func NewCircuitBreaker(maxFailures int, timeout time.Duration) *CircuitBreakerMiddleware {
    return &CircuitBreakerMiddleware{
        maxFailures: maxFailures,
        timeout:     timeout,
    }
}

func (cb *CircuitBreakerMiddleware) Middleware() Middleware {
    return func(next http.Handler) http.Handler {
        return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
            if !cb.allowRequest() {
                http.Error(w, "Service unavailable", http.StatusServiceUnavailable)
                return
            }

            wrapper := &responseWriter{ResponseWriter: w, statusCode: http.StatusOK}
            next.ServeHTTP(wrapper, r)

            cb.recordResult(wrapper.statusCode >= 500)
        })
    }
}

func (cb *CircuitBreakerMiddleware) allowRequest() bool {
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

func (cb *CircuitBreakerMiddleware) recordResult(failed bool) {
    cb.mu.Lock()
    defer cb.mu.Unlock()

    if failed {
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

// Distributed tracing middleware
type TracingMiddleware struct {
    serviceName string
}

func NewTracingMiddleware(serviceName string) *TracingMiddleware {
    return &TracingMiddleware{serviceName: serviceName}
}

func (tm *TracingMiddleware) Middleware() Middleware {
    return func(next http.Handler) http.Handler {
        return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
            span := tm.startSpan(r)
            defer span.finish()

            // Add span context to request
            ctx := context.WithValue(r.Context(), "span", span)
            r = r.WithContext(ctx)

            // Add trace headers to response
            w.Header().Set("X-Trace-ID", span.traceID)
            w.Header().Set("X-Span-ID", span.spanID)

            next.ServeHTTP(w, r)
        })
    }
}

type Span struct {
    traceID     string
    spanID      string
    parentID    string
    operationName string
    startTime   time.Time
    tags        map[string]interface{}
}

func (tm *TracingMiddleware) startSpan(r *http.Request) *Span {
    traceID := r.Header.Get("X-Trace-ID")
    if traceID == "" {
        traceID = generateTraceID()
    }

    parentID := r.Header.Get("X-Span-ID")
    spanID := generateSpanID()

    return &Span{
        traceID:       traceID,
        spanID:        spanID,
        parentID:      parentID,
        operationName: fmt.Sprintf("%s %s", r.Method, r.URL.Path),
        startTime:     time.Now(),
        tags:          make(map[string]interface{}),
    }
}

func (s *Span) finish() {
    duration := time.Since(s.startTime)
    log.Printf("Span: %s-%s (parent: %s) %s took %v",
        s.traceID, s.spanID, s.parentID, s.operationName, duration)
}

// Helper functions
func generateRequestID() string {
    return fmt.Sprintf("req_%d_%d", time.Now().UnixNano(), runtime.NumGoroutine())
}

func generateTraceID() string {
    return fmt.Sprintf("trace_%d", time.Now().UnixNano())
}

func generateSpanID() string {
    return fmt.Sprintf("span_%d", time.Now().UnixNano())
}

func min(a, b int) int {
    if a < b {
        return a
    }
    return b
}
```

[⬆️ Back to Top](#table-of-contents)

## WebSocket Implementation

### Real-time Communication Hub

```go
package main

import (
    "context"
    "encoding/json"
    "log"
    "net/http"
    "sync"
    "time"

    "github.com/gorilla/websocket"
)

// WebSocket message types
type MessageType string

const (
    MessageTypeJoin      MessageType = "join"
    MessageTypeLeave     MessageType = "leave"
    MessageTypeBroadcast MessageType = "broadcast"
    MessageTypePrivate   MessageType = "private"
    MessageTypeTyping    MessageType = "typing"
    MessageTypeError     MessageType = "error"
)

// Message represents a WebSocket message
type Message struct {
    Type      MessageType `json:"type"`
    From      string      `json:"from"`
    To        string      `json:"to,omitempty"`
    Content   string      `json:"content"`
    RoomID    string      `json:"room_id,omitempty"`
    Timestamp time.Time   `json:"timestamp"`
    Metadata  map[string]interface{} `json:"metadata,omitempty"`
}

// Client represents a WebSocket client
type Client struct {
    ID     string
    UserID string
    Conn   *websocket.Conn
    Send   chan *Message
    Hub    *Hub
    Rooms  map[string]bool
    mu     sync.RWMutex
}

// Hub manages WebSocket connections and message routing
type Hub struct {
    clients     map[string]*Client
    rooms       map[string]map[string]*Client
    join        chan *Client
    leave       chan *Client
    broadcast   chan *Message
    private     chan *Message
    roomMessage chan *Message
    mu          sync.RWMutex
}

// NewHub creates a new WebSocket hub
func NewHub() *Hub {
    return &Hub{
        clients:     make(map[string]*Client),
        rooms:       make(map[string]map[string]*Client),
        join:        make(chan *Client),
        leave:       make(chan *Client),
        broadcast:   make(chan *Message),
        private:     make(chan *Message),
        roomMessage: make(chan *Message),
    }
}

// Run starts the hub's message processing
func (h *Hub) Run(ctx context.Context) {
    defer func() {
        close(h.join)
        close(h.leave)
        close(h.broadcast)
        close(h.private)
        close(h.roomMessage)
    }()

    for {
        select {
        case client := <-h.join:
            h.handleJoin(client)

        case client := <-h.leave:
            h.handleLeave(client)

        case message := <-h.broadcast:
            h.handleBroadcast(message)

        case message := <-h.private:
            h.handlePrivateMessage(message)

        case message := <-h.roomMessage:
            h.handleRoomMessage(message)

        case <-ctx.Done():
            h.closeAllConnections()
            return
        }
    }
}

func (h *Hub) handleJoin(client *Client) {
    h.mu.Lock()
    h.clients[client.ID] = client
    h.mu.Unlock()

    // Send welcome message
    welcome := &Message{
        Type:      MessageTypeJoin,
        From:      "system",
        To:        client.ID,
        Content:   "Welcome to the chat!",
        Timestamp: time.Now(),
    }

    select {
    case client.Send <- welcome:
    default:
        close(client.Send)
        h.mu.Lock()
        delete(h.clients, client.ID)
        h.mu.Unlock()
    }

    log.Printf("Client %s joined", client.ID)
}

func (h *Hub) handleLeave(client *Client) {
    h.mu.Lock()
    if _, ok := h.clients[client.ID]; ok {
        delete(h.clients, client.ID)
        close(client.Send)

        // Remove from all rooms
        for roomID := range client.Rooms {
            if room, exists := h.rooms[roomID]; exists {
                delete(room, client.ID)
                if len(room) == 0 {
                    delete(h.rooms, roomID)
                }
            }
        }
    }
    h.mu.Unlock()

    log.Printf("Client %s left", client.ID)
}

func (h *Hub) handleBroadcast(message *Message) {
    h.mu.RLock()
    defer h.mu.RUnlock()

    for _, client := range h.clients {
        select {
        case client.Send <- message:
        default:
            h.removeClient(client)
        }
    }
}

func (h *Hub) handlePrivateMessage(message *Message) {
    h.mu.RLock()
    defer h.mu.RUnlock()

    if targetClient, exists := h.clients[message.To]; exists {
        select {
        case targetClient.Send <- message:
        default:
            h.removeClient(targetClient)
        }
    }
}

func (h *Hub) handleRoomMessage(message *Message) {
    h.mu.RLock()
    defer h.mu.RUnlock()

    if room, exists := h.rooms[message.RoomID]; exists {
        for _, client := range room {
            if client.ID != message.From { // Don't send to sender
                select {
                case client.Send <- message:
                default:
                    h.removeClient(client)
                }
            }
        }
    }
}

func (h *Hub) removeClient(client *Client) {
    if _, ok := h.clients[client.ID]; ok {
        delete(h.clients, client.ID)
        close(client.Send)
    }
}

func (h *Hub) closeAllConnections() {
    h.mu.Lock()
    defer h.mu.Unlock()

    for _, client := range h.clients {
        close(client.Send)
        client.Conn.Close()
    }
}

// JoinRoom adds a client to a room
func (h *Hub) JoinRoom(clientID, roomID string) error {
    h.mu.Lock()
    defer h.mu.Unlock()

    client, exists := h.clients[clientID]
    if !exists {
        return fmt.Errorf("client not found")
    }

    if h.rooms[roomID] == nil {
        h.rooms[roomID] = make(map[string]*Client)
    }

    h.rooms[roomID][clientID] = client

    client.mu.Lock()
    client.Rooms[roomID] = true
    client.mu.Unlock()

    return nil
}

// LeaveRoom removes a client from a room
func (h *Hub) LeaveRoom(clientID, roomID string) error {
    h.mu.Lock()
    defer h.mu.Unlock()

    client, exists := h.clients[clientID]
    if !exists {
        return fmt.Errorf("client not found")
    }

    if room, exists := h.rooms[roomID]; exists {
        delete(room, clientID)
        if len(room) == 0 {
            delete(h.rooms, roomID)
        }
    }

    client.mu.Lock()
    delete(client.Rooms, roomID)
    client.mu.Unlock()

    return nil
}

// Client methods
func (c *Client) readPump() {
    defer func() {
        c.Hub.leave <- c
        c.Conn.Close()
    }()

    c.Conn.SetReadLimit(512)
    c.Conn.SetReadDeadline(time.Now().Add(60 * time.Second))
    c.Conn.SetPongHandler(func(string) error {
        c.Conn.SetReadDeadline(time.Now().Add(60 * time.Second))
        return nil
    })

    for {
        var message Message
        err := c.Conn.ReadJSON(&message)
        if err != nil {
            if websocket.IsUnexpectedCloseError(err, websocket.CloseGoingAway, websocket.CloseAbnormalClosure) {
                log.Printf("WebSocket error: %v", err)
            }
            break
        }

        message.From = c.ID
        message.Timestamp = time.Now()

        // Route message based on type
        switch message.Type {
        case MessageTypeBroadcast:
            c.Hub.broadcast <- &message
        case MessageTypePrivate:
            c.Hub.private <- &message
        case MessageTypeJoin:
            if message.RoomID != "" {
                c.Hub.JoinRoom(c.ID, message.RoomID)
            }
        case MessageTypeLeave:
            if message.RoomID != "" {
                c.Hub.LeaveRoom(c.ID, message.RoomID)
            }
        default:
            if message.RoomID != "" {
                c.Hub.roomMessage <- &message
            }
        }
    }
}

func (c *Client) writePump() {
    ticker := time.NewTicker(54 * time.Second)
    defer func() {
        ticker.Stop()
        c.Conn.Close()
    }()

    for {
        select {
        case message, ok := <-c.Send:
            c.Conn.SetWriteDeadline(time.Now().Add(10 * time.Second))
            if !ok {
                c.Conn.WriteMessage(websocket.CloseMessage, []byte{})
                return
            }

            if err := c.Conn.WriteJSON(message); err != nil {
                log.Printf("WebSocket write error: %v", err)
                return
            }

        case <-ticker.C:
            c.Conn.SetWriteDeadline(time.Now().Add(10 * time.Second))
            if err := c.Conn.WriteMessage(websocket.PingMessage, nil); err != nil {
                return
            }
        }
    }
}

// WebSocket upgrader
var upgrader = websocket.Upgrader{
    ReadBufferSize:  1024,
    WriteBufferSize: 1024,
    CheckOrigin: func(r *http.Request) bool {
        // Implement proper origin checking in production
        return true
    },
}

// HTTP handler for WebSocket connections
func handleWebSocket(hub *Hub) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        conn, err := upgrader.Upgrade(w, r, nil)
        if err != nil {
            log.Printf("WebSocket upgrade error: %v", err)
            return
        }

        clientID := generateClientID()
        userID := r.URL.Query().Get("user_id")

        client := &Client{
            ID:     clientID,
            UserID: userID,
            Conn:   conn,
            Send:   make(chan *Message, 256),
            Hub:    hub,
            Rooms:  make(map[string]bool),
        }

        hub.join <- client

        // Start goroutines for reading and writing
        go client.writePump()
        go client.readPump()
    }
}

func generateClientID() string {
    return fmt.Sprintf("client_%d", time.Now().UnixNano())
}
```

[⬆️ Back to Top](#table-of-contents)

## gRPC Services

### High-Performance gRPC Server

```proto
// user_service.proto
syntax = "proto3";

package userservice;
option go_package = "./userservice";

import "google/protobuf/timestamp.proto";
import "google/protobuf/empty.proto";

service UserService {
    rpc CreateUser(CreateUserRequest) returns (User);
    rpc GetUser(GetUserRequest) returns (User);
    rpc UpdateUser(UpdateUserRequest) returns (User);
    rpc DeleteUser(DeleteUserRequest) returns (google.protobuf.Empty);
    rpc ListUsers(ListUsersRequest) returns (ListUsersResponse);
    rpc StreamUsers(StreamUsersRequest) returns (stream User);
    rpc BulkCreateUsers(stream CreateUserRequest) returns (BulkCreateUsersResponse);
}

message User {
    string id = 1;
    string name = 2;
    string email = 3;
    google.protobuf.Timestamp created_at = 4;
    google.protobuf.Timestamp updated_at = 5;
    map<string, string> metadata = 6;
}

message CreateUserRequest {
    string name = 1;
    string email = 2;
    map<string, string> metadata = 3;
}

message GetUserRequest {
    string id = 1;
}

message UpdateUserRequest {
    string id = 1;
    string name = 2;
    string email = 3;
    map<string, string> metadata = 4;
}

message DeleteUserRequest {
    string id = 1;
}

message ListUsersRequest {
    int32 page_size = 1;
    string page_token = 2;
    string filter = 3;
}

message ListUsersResponse {
    repeated User users = 1;
    string next_page_token = 2;
    int32 total_count = 3;
}

message StreamUsersRequest {
    string filter = 1;
}

message BulkCreateUsersResponse {
    repeated User users = 1;
    int32 created_count = 2;
    repeated string errors = 3;
}
```

```go
// gRPC server implementation
package main

import (
    "context"
    "fmt"
    "io"
    "log"
    "net"
    "sync"
    "time"

    "google.golang.org/grpc"
    "google.golang.org/grpc/codes"
    "google.golang.org/grpc/status"
    "google.golang.org/grpc/metadata"
    "google.golang.org/grpc/peer"
    "google.golang.org/protobuf/types/known/emptypb"
    "google.golang.org/protobuf/types/known/timestamppb"

    pb "path/to/your/userservice"
)

// UserServiceServer implements the gRPC UserService
type UserServiceServer struct {
    pb.UnimplementedUserServiceServer
    users  map[string]*pb.User
    mu     sync.RWMutex
    stream map[string]chan *pb.User
}

// NewUserServiceServer creates a new user service server
func NewUserServiceServer() *UserServiceServer {
    return &UserServiceServer{
        users:  make(map[string]*pb.User),
        stream: make(map[string]chan *pb.User),
    }
}

// CreateUser creates a new user
func (s *UserServiceServer) CreateUser(ctx context.Context, req *pb.CreateUserRequest) (*pb.User, error) {
    // Extract metadata from context
    md, ok := metadata.FromIncomingContext(ctx)
    if !ok {
        return nil, status.Error(codes.InvalidArgument, "missing metadata")
    }

    // Validate request
    if req.Name == "" || req.Email == "" {
        return nil, status.Error(codes.InvalidArgument, "name and email are required")
    }

    // Check for duplicate email
    s.mu.RLock()
    for _, user := range s.users {
        if user.Email == req.Email {
            s.mu.RUnlock()
            return nil, status.Error(codes.AlreadyExists, "email already exists")
        }
    }
    s.mu.RUnlock()

    // Create user
    user := &pb.User{
        Id:        generateUserID(),
        Name:      req.Name,
        Email:     req.Email,
        CreatedAt: timestamppb.Now(),
        UpdatedAt: timestamppb.Now(),
        Metadata:  req.Metadata,
    }

    s.mu.Lock()
    s.users[user.Id] = user
    s.mu.Unlock()

    // Notify streaming clients
    s.notifyStreamClients(user)

    // Log request details
    clientIP := getClientIP(ctx)
    requestID := getRequestID(md)
    log.Printf("Created user %s for client %s (request: %s)", user.Id, clientIP, requestID)

    return user, nil
}

// GetUser retrieves a user by ID
func (s *UserServiceServer) GetUser(ctx context.Context, req *pb.GetUserRequest) (*pb.User, error) {
    if req.Id == "" {
        return nil, status.Error(codes.InvalidArgument, "user ID is required")
    }

    s.mu.RLock()
    user, exists := s.users[req.Id]
    s.mu.RUnlock()

    if !exists {
        return nil, status.Error(codes.NotFound, "user not found")
    }

    return user, nil
}

// UpdateUser updates an existing user
func (s *UserServiceServer) UpdateUser(ctx context.Context, req *pb.UpdateUserRequest) (*pb.User, error) {
    if req.Id == "" {
        return nil, status.Error(codes.InvalidArgument, "user ID is required")
    }

    s.mu.Lock()
    defer s.mu.Unlock()

    user, exists := s.users[req.Id]
    if !exists {
        return nil, status.Error(codes.NotFound, "user not found")
    }

    // Update fields
    if req.Name != "" {
        user.Name = req.Name
    }
    if req.Email != "" {
        user.Email = req.Email
    }
    if req.Metadata != nil {
        user.Metadata = req.Metadata
    }
    user.UpdatedAt = timestamppb.Now()

    s.users[req.Id] = user

    return user, nil
}

// DeleteUser deletes a user
func (s *UserServiceServer) DeleteUser(ctx context.Context, req *pb.DeleteUserRequest) (*emptypb.Empty, error) {
    if req.Id == "" {
        return nil, status.Error(codes.InvalidArgument, "user ID is required")
    }

    s.mu.Lock()
    defer s.mu.Unlock()

    if _, exists := s.users[req.Id]; !exists {
        return nil, status.Error(codes.NotFound, "user not found")
    }

    delete(s.users, req.Id)

    return &emptypb.Empty{}, nil
}

// ListUsers returns a paginated list of users
func (s *UserServiceServer) ListUsers(ctx context.Context, req *pb.ListUsersRequest) (*pb.ListUsersResponse, error) {
    s.mu.RLock()
    defer s.mu.RUnlock()

    var users []*pb.User
    for _, user := range s.users {
        // Apply filter if provided
        if req.Filter != "" && !matchesFilter(user, req.Filter) {
            continue
        }
        users = append(users, user)
    }

    // Apply pagination
    pageSize := int(req.PageSize)
    if pageSize <= 0 {
        pageSize = 10
    }

    startIndex := 0
    if req.PageToken != "" {
        startIndex = parsePageToken(req.PageToken)
    }

    endIndex := startIndex + pageSize
    if endIndex > len(users) {
        endIndex = len(users)
    }

    var paginatedUsers []*pb.User
    var nextPageToken string

    if startIndex < len(users) {
        paginatedUsers = users[startIndex:endIndex]
        if endIndex < len(users) {
            nextPageToken = generatePageToken(endIndex)
        }
    }

    return &pb.ListUsersResponse{
        Users:         paginatedUsers,
        NextPageToken: nextPageToken,
        TotalCount:    int32(len(users)),
    }, nil
}

// StreamUsers streams users to the client
func (s *UserServiceServer) StreamUsers(req *pb.StreamUsersRequest, stream pb.UserService_StreamUsersServer) error {
    streamID := generateStreamID()
    streamChan := make(chan *pb.User, 100)

    // Register stream
    s.mu.Lock()
    s.stream[streamID] = streamChan
    s.mu.Unlock()

    // Cleanup on exit
    defer func() {
        s.mu.Lock()
        delete(s.stream, streamID)
        close(streamChan)
        s.mu.Unlock()
    }()

    // Send existing users first
    s.mu.RLock()
    for _, user := range s.users {
        if req.Filter == "" || matchesFilter(user, req.Filter) {
            if err := stream.Send(user); err != nil {
                s.mu.RUnlock()
                return err
            }
        }
    }
    s.mu.RUnlock()

    // Stream new users
    for {
        select {
        case user := <-streamChan:
            if req.Filter == "" || matchesFilter(user, req.Filter) {
                if err := stream.Send(user); err != nil {
                    return err
                }
            }
        case <-stream.Context().Done():
            return stream.Context().Err()
        }
    }
}

// BulkCreateUsers handles bulk user creation
func (s *UserServiceServer) BulkCreateUsers(stream pb.UserService_BulkCreateUsersServer) error {
    var users []*pb.User
    var errors []string

    for {
        req, err := stream.Recv()
        if err == io.EOF {
            // End of stream, send response
            return stream.SendAndClose(&pb.BulkCreateUsersResponse{
                Users:        users,
                CreatedCount: int32(len(users)),
                Errors:       errors,
            })
        }
        if err != nil {
            return err
        }

        // Validate and create user
        if req.Name == "" || req.Email == "" {
            errors = append(errors, "name and email are required")
            continue
        }

        user := &pb.User{
            Id:        generateUserID(),
            Name:      req.Name,
            Email:     req.Email,
            CreatedAt: timestamppb.Now(),
            UpdatedAt: timestamppb.Now(),
            Metadata:  req.Metadata,
        }

        s.mu.Lock()
        s.users[user.Id] = user
        s.mu.Unlock()

        users = append(users, user)

        // Notify streaming clients
        s.notifyStreamClients(user)
    }
}

// Helper methods
func (s *UserServiceServer) notifyStreamClients(user *pb.User) {
    s.mu.RLock()
    defer s.mu.RUnlock()

    for _, streamChan := range s.stream {
        select {
        case streamChan <- user:
        default:
            // Channel is full, skip
        }
    }
}

func generateUserID() string {
    return fmt.Sprintf("user_%d", time.Now().UnixNano())
}

func generateStreamID() string {
    return fmt.Sprintf("stream_%d", time.Now().UnixNano())
}

func getClientIP(ctx context.Context) string {
    if peer, ok := peer.FromContext(ctx); ok {
        return peer.Addr.String()
    }
    return "unknown"
}

func getRequestID(md metadata.MD) string {
    if values := md.Get("request-id"); len(values) > 0 {
        return values[0]
    }
    return "unknown"
}

func matchesFilter(user *pb.User, filter string) bool {
    // Implement your filtering logic here
    return strings.Contains(user.Name, filter) || strings.Contains(user.Email, filter)
}

func parsePageToken(token string) int {
    // Implement page token parsing
    return 0
}

func generatePageToken(index int) string {
    return fmt.Sprintf("token_%d", index)
}

// gRPC interceptors for authentication and logging
func AuthInterceptor(ctx context.Context, req interface{}, info *grpc.UnaryServerInfo, handler grpc.UnaryHandler) (interface{}, error) {
    md, ok := metadata.FromIncomingContext(ctx)
    if !ok {
        return nil, status.Error(codes.Unauthenticated, "missing metadata")
    }

    tokens := md.Get("authorization")
    if len(tokens) == 0 {
        return nil, status.Error(codes.Unauthenticated, "missing authorization token")
    }

    // Validate token (implement your auth logic)
    if !isValidToken(tokens[0]) {
        return nil, status.Error(codes.Unauthenticated, "invalid token")
    }

    return handler(ctx, req)
}

func LoggingInterceptor(ctx context.Context, req interface{}, info *grpc.UnaryServerInfo, handler grpc.UnaryHandler) (interface{}, error) {
    start := time.Now()

    resp, err := handler(ctx, req)

    duration := time.Since(start)

    status := "OK"
    if err != nil {
        status = "ERROR"
    }

    log.Printf("gRPC %s %s %v %s", info.FullMethod, status, duration, getClientIP(ctx))

    return resp, err
}

func isValidToken(token string) bool {
    // Implement your token validation logic
    return token != ""
}

// Main server setup
func main() {
    lis, err := net.Listen("tcp", ":50051")
    if err != nil {
        log.Fatalf("Failed to listen: %v", err)
    }

    s := grpc.NewServer(
        grpc.UnaryInterceptor(grpc.ChainUnaryInterceptor(
            LoggingInterceptor,
            AuthInterceptor,
        )),
    )

    userService := NewUserServiceServer()
    pb.RegisterUserServiceServer(s, userService)

    log.Println("gRPC server listening on :50051")
    if err := s.Serve(lis); err != nil {
        log.Fatalf("Failed to serve: %v", err)
    }
}
```

[⬆️ Back to Top](#table-of-contents)

## GraphQL Integration

### GraphQL Server with Advanced Features

```go
package main

import (
    "context"
    "encoding/json"
    "fmt"
    "log"
    "net/http"
    "strconv"
    "strings"
    "sync"
    "time"

    "github.com/graphql-go/graphql"
    "github.com/graphql-go/handler"
)

// Domain models
type User struct {
    ID        string    `json:"id"`
    Name      string    `json:"name"`
    Email     string    `json:"email"`
    Age       int       `json:"age"`
    Posts     []*Post   `json:"posts"`
    CreatedAt time.Time `json:"created_at"`
}

type Post struct {
    ID       string    `json:"id"`
    Title    string    `json:"title"`
    Content  string    `json:"content"`
    AuthorID string    `json:"author_id"`
    Author   *User     `json:"author"`
    Tags     []string  `json:"tags"`
    Published bool     `json:"published"`
    CreatedAt time.Time `json:"created_at"`
}

// Data store (use database in production)
type DataStore struct {
    users map[string]*User
    posts map[string]*Post
    mu    sync.RWMutex
}

func NewDataStore() *DataStore {
    return &DataStore{
        users: make(map[string]*User),
        posts: make(map[string]*Post),
    }
}

func (ds *DataStore) CreateUser(name, email string, age int) *User {
    ds.mu.Lock()
    defer ds.mu.Unlock()

    user := &User{
        ID:        generateID(),
        Name:      name,
        Email:     email,
        Age:       age,
        Posts:     []*Post{},
        CreatedAt: time.Now(),
    }

    ds.users[user.ID] = user
    return user
}

func (ds *DataStore) GetUser(id string) (*User, error) {
    ds.mu.RLock()
    defer ds.mu.RUnlock()

    user, exists := ds.users[id]
    if !exists {
        return nil, fmt.Errorf("user not found")
    }
    return user, nil
}

func (ds *DataStore) GetUsers(limit, offset int, filter string) []*User {
    ds.mu.RLock()
    defer ds.mu.RUnlock()

    var users []*User
    for _, user := range ds.users {
        if filter == "" || strings.Contains(strings.ToLower(user.Name), strings.ToLower(filter)) {
            users = append(users, user)
        }
    }

    // Apply pagination
    start := offset
    end := offset + limit
    if start > len(users) {
        return []*User{}
    }
    if end > len(users) {
        end = len(users)
    }

    return users[start:end]
}

func (ds *DataStore) CreatePost(title, content, authorID string, tags []string) (*Post, error) {
    ds.mu.Lock()
    defer ds.mu.Unlock()

    // Verify author exists
    if _, exists := ds.users[authorID]; !exists {
        return nil, fmt.Errorf("author not found")
    }

    post := &Post{
        ID:        generateID(),
        Title:     title,
        Content:   content,
        AuthorID:  authorID,
        Tags:      tags,
        Published: false,
        CreatedAt: time.Now(),
    }

    ds.posts[post.ID] = post

    // Add to user's posts
    ds.users[authorID].Posts = append(ds.users[authorID].Posts, post)

    return post, nil
}

func (ds *DataStore) GetPost(id string) (*Post, error) {
    ds.mu.RLock()
    defer ds.mu.RUnlock()

    post, exists := ds.posts[id]
    if !exists {
        return nil, fmt.Errorf("post not found")
    }
    return post, nil
}

func (ds *DataStore) GetPostsByAuthor(authorID string) []*Post {
    ds.mu.RLock()
    defer ds.mu.RUnlock()

    var posts []*Post
    for _, post := range ds.posts {
        if post.AuthorID == authorID {
            posts = append(posts, post)
        }
    }
    return posts
}

// GraphQL Schema Definition
func createSchema(dataStore *DataStore) (graphql.Schema, error) {
    // User type
    userType := graphql.NewObject(graphql.ObjectConfig{
        Name: "User",
        Fields: graphql.Fields{
            "id": &graphql.Field{
                Type: graphql.String,
            },
            "name": &graphql.Field{
                Type: graphql.String,
            },
            "email": &graphql.Field{
                Type: graphql.String,
            },
            "age": &graphql.Field{
                Type: graphql.Int,
            },
            "posts": &graphql.Field{
                Type: graphql.NewList(postType),
                Resolve: func(p graphql.ResolveParams) (interface{}, error) {
                    user := p.Source.(*User)
                    return dataStore.GetPostsByAuthor(user.ID), nil
                },
            },
            "createdAt": &graphql.Field{
                Type: graphql.String,
                Resolve: func(p graphql.ResolveParams) (interface{}, error) {
                    user := p.Source.(*User)
                    return user.CreatedAt.Format(time.RFC3339), nil
                },
            },
        },
    })

    // Post type (forward declaration)
    postType := graphql.NewObject(graphql.ObjectConfig{
        Name: "Post",
        Fields: graphql.Fields{
            "id": &graphql.Field{
                Type: graphql.String,
            },
            "title": &graphql.Field{
                Type: graphql.String,
            },
            "content": &graphql.Field{
                Type: graphql.String,
            },
            "author": &graphql.Field{
                Type: userType,
                Resolve: func(p graphql.ResolveParams) (interface{}, error) {
                    post := p.Source.(*Post)
                    return dataStore.GetUser(post.AuthorID)
                },
            },
            "tags": &graphql.Field{
                Type: graphql.NewList(graphql.String),
            },
            "published": &graphql.Field{
                Type: graphql.Boolean,
            },
            "createdAt": &graphql.Field{
                Type: graphql.String,
                Resolve: func(p graphql.ResolveParams) (interface{}, error) {
                    post := p.Source.(*Post)
                    return post.CreatedAt.Format(time.RFC3339), nil
                },
            },
        },
    })

    // Input types for mutations
    userInputType := graphql.NewInputObject(graphql.InputObjectConfig{
        Name: "UserInput",
        Fields: graphql.InputObjectConfigFieldMap{
            "name": &graphql.InputObjectFieldConfig{
                Type: graphql.NewNonNull(graphql.String),
            },
            "email": &graphql.InputObjectFieldConfig{
                Type: graphql.NewNonNull(graphql.String),
            },
            "age": &graphql.InputObjectFieldConfig{
                Type: graphql.NewNonNull(graphql.Int),
            },
        },
    })

    postInputType := graphql.NewInputObject(graphql.InputObjectConfig{
        Name: "PostInput",
        Fields: graphql.InputObjectConfigFieldMap{
            "title": &graphql.InputObjectFieldConfig{
                Type: graphql.NewNonNull(graphql.String),
            },
            "content": &graphql.InputObjectFieldConfig{
                Type: graphql.NewNonNull(graphql.String),
            },
            "authorId": &graphql.InputObjectFieldConfig{
                Type: graphql.NewNonNull(graphql.String),
            },
            "tags": &graphql.InputObjectFieldConfig{
                Type: graphql.NewList(graphql.String),
            },
        },
    })

    // Query root
    queryType := graphql.NewObject(graphql.ObjectConfig{
        Name: "Query",
        Fields: graphql.Fields{
            "user": &graphql.Field{
                Type: userType,
                Args: graphql.FieldConfigArgument{
                    "id": &graphql.ArgumentConfig{
                        Type: graphql.NewNonNull(graphql.String),
                    },
                },
                Resolve: func(p graphql.ResolveParams) (interface{}, error) {
                    id := p.Args["id"].(string)
                    return dataStore.GetUser(id)
                },
            },
            "users": &graphql.Field{
                Type: graphql.NewList(userType),
                Args: graphql.FieldConfigArgument{
                    "limit": &graphql.ArgumentConfig{
                        Type:         graphql.Int,
                        DefaultValue: 10,
                    },
                    "offset": &graphql.ArgumentConfig{
                        Type:         graphql.Int,
                        DefaultValue: 0,
                    },
                    "filter": &graphql.ArgumentConfig{
                        Type:         graphql.String,
                        DefaultValue: "",
                    },
                },
                Resolve: func(p graphql.ResolveParams) (interface{}, error) {
                    limit := p.Args["limit"].(int)
                    offset := p.Args["offset"].(int)
                    filter := p.Args["filter"].(string)
                    return dataStore.GetUsers(limit, offset, filter), nil
                },
            },
            "post": &graphql.Field{
                Type: postType,
                Args: graphql.FieldConfigArgument{
                    "id": &graphql.ArgumentConfig{
                        Type: graphql.NewNonNull(graphql.String),
                    },
                },
                Resolve: func(p graphql.ResolveParams) (interface{}, error) {
                    id := p.Args["id"].(string)
                    return dataStore.GetPost(id)
                },
            },
        },
    })

    // Mutation root
    mutationType := graphql.NewObject(graphql.ObjectConfig{
        Name: "Mutation",
        Fields: graphql.Fields{
            "createUser": &graphql.Field{
                Type: userType,
                Args: graphql.FieldConfigArgument{
                    "input": &graphql.ArgumentConfig{
                        Type: userInputType,
                    },
                },
                Resolve: func(p graphql.ResolveParams) (interface{}, error) {
                    input := p.Args["input"].(map[string]interface{})
                    name := input["name"].(string)
                    email := input["email"].(string)
                    age := input["age"].(int)

                    return dataStore.CreateUser(name, email, age), nil
                },
            },
            "createPost": &graphql.Field{
                Type: postType,
                Args: graphql.FieldConfigArgument{
                    "input": &graphql.ArgumentConfig{
                        Type: postInputType,
                    },
                },
                Resolve: func(p graphql.ResolveParams) (interface{}, error) {
                    input := p.Args["input"].(map[string]interface{})
                    title := input["title"].(string)
                    content := input["content"].(string)
                    authorID := input["authorId"].(string)

                    var tags []string
                    if tagList, ok := input["tags"].([]interface{}); ok {
                        for _, tag := range tagList {
                            tags = append(tags, tag.(string))
                        }
                    }

                    return dataStore.CreatePost(title, content, authorID, tags)
                },
            },
        },
    })

    // Subscription root (for real-time updates)
    subscriptionType := graphql.NewObject(graphql.ObjectConfig{
        Name: "Subscription",
        Fields: graphql.Fields{
            "userCreated": &graphql.Field{
                Type: userType,
                Resolve: func(p graphql.ResolveParams) (interface{}, error) {
                    // This would typically connect to a message broker
                    // or use channels for real-time updates
                    return nil, nil
                },
            },
            "postCreated": &graphql.Field{
                Type: postType,
                Resolve: func(p graphql.ResolveParams) (interface{}, error) {
                    return nil, nil
                },
            },
        },
    })

    return graphql.NewSchema(graphql.SchemaConfig{
        Query:        queryType,
        Mutation:     mutationType,
        Subscription: subscriptionType,
    })
}

// Custom GraphQL middleware
func graphqlMiddleware(next http.Handler) http.Handler {
    return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
        // Add CORS headers
        w.Header().Set("Access-Control-Allow-Origin", "*")
        w.Header().Set("Access-Control-Allow-Methods", "POST, OPTIONS")
        w.Header().Set("Access-Control-Allow-Headers", "Content-Type, Authorization")

        if r.Method == "OPTIONS" {
            w.WriteHeader(http.StatusOK)
            return
        }

        // Add request context
        ctx := context.WithValue(r.Context(), "requestTime", time.Now())
        r = r.WithContext(ctx)

        next.ServeHTTP(w, r)
    })
}

// Query complexity analysis
func analyzeComplexity(query string) (int, error) {
    // Simple complexity analysis based on query depth and fields
    complexity := strings.Count(query, "{") * 2
    complexity += strings.Count(query, "user") * 3
    complexity += strings.Count(query, "posts") * 5

    maxComplexity := 100
    if complexity > maxComplexity {
        return complexity, fmt.Errorf("query too complex: %d (max: %d)", complexity, maxComplexity)
    }

    return complexity, nil
}

// GraphQL handler with custom logic
func createGraphQLHandler(schema graphql.Schema) http.HandlerFunc {
    return func(w http.ResponseWriter, r *http.Request) {
        var requestBody struct {
            Query     string                 `json:"query"`
            Variables map[string]interface{} `json:"variables"`
        }

        if err := json.NewDecoder(r.Body).Decode(&requestBody); err != nil {
            http.Error(w, "Invalid JSON", http.StatusBadRequest)
            return
        }

        // Analyze query complexity
        if complexity, err := analyzeComplexity(requestBody.Query); err != nil {
            http.Error(w, err.Error(), http.StatusBadRequest)
            return
        } else {
            log.Printf("Query complexity: %d", complexity)
        }

        // Execute GraphQL query
        result := graphql.Do(graphql.Params{
            Schema:         schema,
            RequestString:  requestBody.Query,
            VariableValues: requestBody.Variables,
            Context:        r.Context(),
        })

        // Log execution time
        if requestTime, ok := r.Context().Value("requestTime").(time.Time); ok {
            duration := time.Since(requestTime)
            log.Printf("GraphQL query executed in %v", duration)
        }

        w.Header().Set("Content-Type", "application/json")
        json.NewEncoder(w).Encode(result)
    }
}

func generateID() string {
    return fmt.Sprintf("%d", time.Now().UnixNano())
}

func main() {
    // Initialize data store
    dataStore := NewDataStore()

    // Create some sample data
    user1 := dataStore.CreateUser("John Doe", "john@example.com", 30)
    user2 := dataStore.CreateUser("Jane Smith", "jane@example.com", 25)

    dataStore.CreatePost("GraphQL Introduction", "Learning GraphQL with Go", user1.ID, []string{"graphql", "go"})
    dataStore.CreatePost("Advanced Go Patterns", "Deep dive into Go programming", user2.ID, []string{"go", "patterns"})

    // Create GraphQL schema
    schema, err := createSchema(dataStore)
    if err != nil {
        log.Fatal(err)
    }

    // Setup HTTP handlers
    http.Handle("/graphql", graphqlMiddleware(createGraphQLHandler(schema)))

    // GraphQL Playground (for development)
    http.Handle("/playground", handler.New(&handler.Config{
        Schema:     &schema,
        Pretty:     true,
        Playground: true,
    }))

    log.Println("GraphQL server running on :8080")
    log.Println("Playground available at http://localhost:8080/playground")

    log.Fatal(http.ListenAndServe(":8080", nil))
}
```

[⬆️ Back to Top](#table-of-contents)

## Advanced Database Patterns

### Connection Pooling and Advanced Queries

```go
package main

import (
    "context"
    "database/sql"
    "fmt"
    "log"
    "sync"
    "time"

    _ "github.com/lib/pq"
    "github.com/jmoiron/sqlx"
)

// Database configuration
type DatabaseConfig struct {
    Host            string
    Port            int
    User            string
    Password        string
    DBName          string
    SSLMode         string
    MaxOpenConns    int
    MaxIdleConns    int
    ConnMaxLifetime time.Duration
    ConnMaxIdleTime time.Duration
}

// Advanced database client
type DatabaseClient struct {
    db     *sqlx.DB
    config *DatabaseConfig
    stats  *DBStats
    mu     sync.RWMutex
}

type DBStats struct {
    TotalQueries    int64
    SlowQueries     int64
    FailedQueries   int64
    AvgQueryTime    time.Duration
    LastError       string
    ConnectionStats sql.DBStats
    mu              sync.RWMutex
}

// NewDatabaseClient creates a new database client with connection pooling
func NewDatabaseClient(config *DatabaseConfig) (*DatabaseClient, error) {
    dsn := fmt.Sprintf("host=%s port=%d user=%s password=%s dbname=%s sslmode=%s",
        config.Host, config.Port, config.User, config.Password, config.DBName, config.SSLMode)

    db, err := sqlx.Connect("postgres", dsn)
    if err != nil {
        return nil, fmt.Errorf("failed to connect to database: %w", err)
    }

    // Configure connection pool
    db.SetMaxOpenConns(config.MaxOpenConns)
    db.SetMaxIdleConns(config.MaxIdleConns)
    db.SetConnMaxLifetime(config.ConnMaxLifetime)
    db.SetConnMaxIdleTime(config.ConnMaxIdleTime)

    client := &DatabaseClient{
        db:     db,
        config: config,
        stats:  &DBStats{},
    }

    // Test connection
    if err := client.Ping(); err != nil {
        return nil, fmt.Errorf("failed to ping database: %w", err)
    }

    // Start stats collection
    go client.collectStats()

    return client, nil
}

func (dc *DatabaseClient) Ping() error {
    ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
    defer cancel()
    return dc.db.PingContext(ctx)
}

func (dc *DatabaseClient) Close() error {
    return dc.db.Close()
}

// Transaction management
type TransactionFunc func(*sqlx.Tx) error

func (dc *DatabaseClient) WithTransaction(ctx context.Context, fn TransactionFunc) error {
    tx, err := dc.db.BeginTxx(ctx, nil)
    if err != nil {
        return fmt.Errorf("failed to begin transaction: %w", err)
    }

    defer func() {
        if r := recover(); r != nil {
            tx.Rollback()
            panic(r)
        }
    }()

    if err := fn(tx); err != nil {
        if rbErr := tx.Rollback(); rbErr != nil {
            return fmt.Errorf("transaction error: %v, rollback error: %v", err, rbErr)
        }
        return err
    }

    return tx.Commit()
}

// Query execution with monitoring
func (dc *DatabaseClient) Query(ctx context.Context, query string, args ...interface{}) (*sqlx.Rows, error) {
    start := time.Now()

    rows, err := dc.db.QueryxContext(ctx, query, args...)

    duration := time.Since(start)
    dc.recordQuery(duration, err)

    if duration > 1*time.Second {
        log.Printf("Slow query detected (%v): %s", duration, query)
    }

    return rows, err
}

func (dc *DatabaseClient) QueryRow(ctx context.Context, query string, args ...interface{}) *sqlx.Row {
    start := time.Now()

    row := dc.db.QueryRowxContext(ctx, query, args...)

    duration := time.Since(start)
    dc.recordQuery(duration, nil)

    return row
}

func (dc *DatabaseClient) Exec(ctx context.Context, query string, args ...interface{}) (sql.Result, error) {
    start := time.Now()

    result, err := dc.db.ExecContext(ctx, query, args...)

    duration := time.Since(start)
    dc.recordQuery(duration, err)

    return result, err
}

// Advanced query builder
type QueryBuilder struct {
    query  strings.Builder
    args   []interface{}
    argPos int
}

func NewQueryBuilder() *QueryBuilder {
    return &QueryBuilder{
        args:   make([]interface{}, 0),
        argPos: 1,
    }
}

func (qb *QueryBuilder) Select(columns ...string) *QueryBuilder {
    qb.query.WriteString("SELECT ")
    qb.query.WriteString(strings.Join(columns, ", "))
    return qb
}

func (qb *QueryBuilder) From(table string) *QueryBuilder {
    qb.query.WriteString(" FROM ")
    qb.query.WriteString(table)
    return qb
}

func (qb *QueryBuilder) Where(condition string, args ...interface{}) *QueryBuilder {
    qb.query.WriteString(" WHERE ")
    qb.query.WriteString(condition)
    qb.args = append(qb.args, args...)
    return qb
}

func (qb *QueryBuilder) And(condition string, args ...interface{}) *QueryBuilder {
    qb.query.WriteString(" AND ")
    qb.query.WriteString(condition)
    qb.args = append(qb.args, args...)
    return qb
}

func (qb *QueryBuilder) Or(condition string, args ...interface{}) *QueryBuilder {
    qb.query.WriteString(" OR ")
    qb.query.WriteString(condition)
    qb.args = append(qb.args, args...)
    return qb
}

func (qb *QueryBuilder) OrderBy(column, direction string) *QueryBuilder {
    qb.query.WriteString(" ORDER BY ")
    qb.query.WriteString(column)
    qb.query.WriteString(" ")
    qb.query.WriteString(direction)
    return qb
}

func (qb *QueryBuilder) Limit(limit int) *QueryBuilder {
    qb.query.WriteString(" LIMIT ")
    qb.query.WriteString(fmt.Sprintf("%d", limit))
    return qb
}

func (qb *QueryBuilder) Offset(offset int) *QueryBuilder {
    qb.query.WriteString(" OFFSET ")
    qb.query.WriteString(fmt.Sprintf("%d", offset))
    return qb
}

func (qb *QueryBuilder) Build() (string, []interface{}) {
    return qb.query.String(), qb.args
}

// Repository pattern with caching
type UserRepository struct {
    db    *DatabaseClient
    cache *sync.Map // Simple in-memory cache
}

type User struct {
    ID        int       `db:"id" json:"id"`
    Name      string    `db:"name" json:"name"`
    Email     string    `db:"email" json:"email"`
    CreatedAt time.Time `db:"created_at" json:"created_at"`
    UpdatedAt time.Time `db:"updated_at" json:"updated_at"`
}

func NewUserRepository(db *DatabaseClient) *UserRepository {
    return &UserRepository{
        db:    db,
        cache: &sync.Map{},
    }
}

func (ur *UserRepository) Create(ctx context.Context, user *User) error {
    query := `
        INSERT INTO users (name, email, created_at, updated_at)
        VALUES ($1, $2, $3, $4)
        RETURNING id
    `

    now := time.Now()
    user.CreatedAt = now
    user.UpdatedAt = now

    row := ur.db.QueryRow(ctx, query, user.Name, user.Email, user.CreatedAt, user.UpdatedAt)
    return row.Scan(&user.ID)
}

func (ur *UserRepository) GetByID(ctx context.Context, id int) (*User, error) {
    // Check cache first
    if cached, ok := ur.cache.Load(fmt.Sprintf("user_%d", id)); ok {
        return cached.(*User), nil
    }

    query := `
        SELECT id, name, email, created_at, updated_at
        FROM users
        WHERE id = $1
    `

    var user User
    row := ur.db.QueryRow(ctx, query, id)
    err := row.StructScan(&user)
    if err != nil {
        return nil, err
    }

    // Cache the result
    ur.cache.Store(fmt.Sprintf("user_%d", id), &user)

    return &user, nil
}

func (ur *UserRepository) GetByEmail(ctx context.Context, email string) (*User, error) {
    query := `
        SELECT id, name, email, created_at, updated_at
        FROM users
        WHERE email = $1
    `

    var user User
    row := ur.db.QueryRow(ctx, query, email)
    err := row.StructScan(&user)
    return &user, err
}

func (ur *UserRepository) Update(ctx context.Context, user *User) error {
    query := `
        UPDATE users
        SET name = $1, email = $2, updated_at = $3
        WHERE id = $4
    `

    user.UpdatedAt = time.Now()

    _, err := ur.db.Exec(ctx, query, user.Name, user.Email, user.UpdatedAt, user.ID)
    if err != nil {
        return err
    }

    // Invalidate cache
    ur.cache.Delete(fmt.Sprintf("user_%d", user.ID))

    return nil
}

func (ur *UserRepository) Delete(ctx context.Context, id int) error {
    query := "DELETE FROM users WHERE id = $1"

    _, err := ur.db.Exec(ctx, query, id)
    if err != nil {
        return err
    }

    // Invalidate cache
    ur.cache.Delete(fmt.Sprintf("user_%d", id))

    return nil
}

func (ur *UserRepository) List(ctx context.Context, limit, offset int, filter string) ([]*User, error) {
    qb := NewQueryBuilder().
        Select("id", "name", "email", "created_at", "updated_at").
        From("users")

    if filter != "" {
        qb.Where("name ILIKE $1 OR email ILIKE $1", "%"+filter+"%")
    }

    qb.OrderBy("created_at", "DESC").
        Limit(limit).
        Offset(offset)

    query, args := qb.Build()

    rows, err := ur.db.Query(ctx, query, args...)
    if err != nil {
        return nil, err
    }
    defer rows.Close()

    var users []*User
    for rows.Next() {
        var user User
        if err := rows.StructScan(&user); err != nil {
            return nil, err
        }
        users = append(users, &user)
    }

    return users, rows.Err()
}

// Bulk operations
func (ur *UserRepository) BulkCreate(ctx context.Context, users []*User) error {
    return ur.db.WithTransaction(ctx, func(tx *sqlx.Tx) error {
        query := `
            INSERT INTO users (name, email, created_at, updated_at)
            VALUES ($1, $2, $3, $4)
            RETURNING id
        `

        now := time.Now()
        for _, user := range users {
            user.CreatedAt = now
            user.UpdatedAt = now

            row := tx.QueryRowx(query, user.Name, user.Email, user.CreatedAt, user.UpdatedAt)
            if err := row.Scan(&user.ID); err != nil {
                return err
            }
        }

        return nil
    })
}

// Database migration system
type Migration struct {
    Version int
    Name    string
    Up      string
    Down    string
}

type MigrationRunner struct {
    db         *DatabaseClient
    migrations []Migration
}

func NewMigrationRunner(db *DatabaseClient) *MigrationRunner {
    return &MigrationRunner{
        db:         db,
        migrations: []Migration{},
    }
}

func (mr *MigrationRunner) AddMigration(migration Migration) {
    mr.migrations = append(mr.migrations, migration)
}

func (mr *MigrationRunner) Run(ctx context.Context) error {
    // Create migrations table if it doesn't exist
    createTable := `
        CREATE TABLE IF NOT EXISTS schema_migrations (
            version INTEGER PRIMARY KEY,
            applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )
    `

    if _, err := mr.db.Exec(ctx, createTable); err != nil {
        return fmt.Errorf("failed to create migrations table: %w", err)
    }

    // Get applied migrations
    query := "SELECT version FROM schema_migrations ORDER BY version"
    rows, err := mr.db.Query(ctx, query)
    if err != nil {
        return fmt.Errorf("failed to get applied migrations: %w", err)
    }
    defer rows.Close()

    appliedVersions := make(map[int]bool)
    for rows.Next() {
        var version int
        if err := rows.Scan(&version); err != nil {
            return err
        }
        appliedVersions[version] = true
    }

    // Apply pending migrations
    for _, migration := range mr.migrations {
        if appliedVersions[migration.Version] {
            continue
        }

        log.Printf("Applying migration %d: %s", migration.Version, migration.Name)

        err := mr.db.WithTransaction(ctx, func(tx *sqlx.Tx) error {
            // Execute migration
            if _, err := tx.Exec(migration.Up); err != nil {
                return fmt.Errorf("failed to execute migration %d: %w", migration.Version, err)
            }

            // Record migration
            insertMigration := "INSERT INTO schema_migrations (version) VALUES ($1)"
            if _, err := tx.Exec(insertMigration, migration.Version); err != nil {
                return fmt.Errorf("failed to record migration %d: %w", migration.Version, err)
            }

            return nil
        })

        if err != nil {
            return err
        }

        log.Printf("Migration %d applied successfully", migration.Version)
    }

    return nil
}

// Stats collection
func (dc *DatabaseClient) recordQuery(duration time.Duration, err error) {
    dc.stats.mu.Lock()
    defer dc.stats.mu.Unlock()

    dc.stats.TotalQueries++

    if err != nil {
        dc.stats.FailedQueries++
        dc.stats.LastError = err.Error()
    }

    if duration > 1*time.Second {
        dc.stats.SlowQueries++
    }

    // Update average query time
    totalTime := time.Duration(dc.stats.TotalQueries-1) * dc.stats.AvgQueryTime
    dc.stats.AvgQueryTime = (totalTime + duration) / time.Duration(dc.stats.TotalQueries)
}

func (dc *DatabaseClient) collectStats() {
    ticker := time.NewTicker(30 * time.Second)
    defer ticker.Stop()

    for range ticker.C {
        dc.stats.mu.Lock()
        dc.stats.ConnectionStats = dc.db.Stats()
        dc.stats.mu.Unlock()
    }
}

func (dc *DatabaseClient) GetStats() DBStats {
    dc.stats.mu.RLock()
    defer dc.stats.mu.RUnlock()
    return *dc.stats
}
```

[⬆️ Back to Top](#table-of-contents)

## Connection Pooling and Transactions

### Advanced Transaction Patterns

```go
package main

import (
    "context"
    "database/sql"
    "fmt"
    "log"
    "sync"
    "time"

    "github.com/jmoiron/sqlx"
)

// Distributed transaction coordinator
type DistributedTxManager struct {
    coordinators map[string]*TxCoordinator
    mu           sync.RWMutex
}

type TxCoordinator struct {
    id           string
    participants []*TxParticipant
    state        TxState
    mu           sync.RWMutex
}

type TxParticipant struct {
    id       string
    db       *sqlx.DB
    prepared bool
    tx       *sqlx.Tx
}

type TxState int

const (
    TxStateActive TxState = iota
    TxStatePreparing
    TxStatePrepared
    TxStateCommitting
    TxStateCommitted
    TxStateAborting
    TxStateAborted
)

func NewDistributedTxManager() *DistributedTxManager {
    return &DistributedTxManager{
        coordinators: make(map[string]*TxCoordinator),
    }
}

func (dtm *DistributedTxManager) BeginDistributedTx(ctx context.Context, participants []*TxParticipant) (*TxCoordinator, error) {
    txID := generateTxID()

    coordinator := &TxCoordinator{
        id:           txID,
        participants: participants,
        state:        TxStateActive,
    }

    // Begin transaction on all participants
    for _, participant := range participants {
        tx, err := participant.db.BeginTxx(ctx, &sql.TxOptions{
            Isolation: sql.LevelSerializable,
        })
        if err != nil {
            // Rollback already started transactions
            dtm.abortDistributedTx(ctx, coordinator)
            return nil, fmt.Errorf("failed to begin transaction on participant %s: %w", participant.id, err)
        }
        participant.tx = tx
    }

    dtm.mu.Lock()
    dtm.coordinators[txID] = coordinator
    dtm.mu.Unlock()

    return coordinator, nil
}

func (dtm *DistributedTxManager) CommitDistributedTx(ctx context.Context, coordinator *TxCoordinator) error {
    coordinator.mu.Lock()
    defer coordinator.mu.Unlock()

    if coordinator.state != TxStateActive {
        return fmt.Errorf("transaction not in active state")
    }

    // Phase 1: Prepare
    coordinator.state = TxStatePreparing

    for _, participant := range coordinator.participants {
        // In a real implementation, this would send a PREPARE message
        // to each participant database
        participant.prepared = true
    }

    coordinator.state = TxStatePrepared

    // Phase 2: Commit
    coordinator.state = TxStateCommitting

    var commitErrors []error
    for _, participant := range coordinator.participants {
        if err := participant.tx.Commit(); err != nil {
            commitErrors = append(commitErrors, err)
        }
    }

    if len(commitErrors) > 0 {
        coordinator.state = TxStateAborted
        return fmt.Errorf("commit failed on some participants: %v", commitErrors)
    }

    coordinator.state = TxStateCommitted

    // Cleanup
    dtm.mu.Lock()
    delete(dtm.coordinators, coordinator.id)
    dtm.mu.Unlock()

    return nil
}

func (dtm *DistributedTxManager) abortDistributedTx(ctx context.Context, coordinator *TxCoordinator) {
    coordinator.mu.Lock()
    defer coordinator.mu.Unlock()

    coordinator.state = TxStateAborting

    for _, participant := range coordinator.participants {
        if participant.tx != nil {
            participant.tx.Rollback()
        }
    }

    coordinator.state = TxStateAborted

    dtm.mu.Lock()
    delete(dtm.coordinators, coordinator.id)
    dtm.mu.Unlock()
}

// Saga pattern implementation
type Saga struct {
    steps       []*SagaStep
    currentStep int
    context     map[string]interface{}
    mu          sync.RWMutex
}

type SagaStep struct {
    name         string
    action       SagaAction
    compensation SagaAction
}

type SagaAction func(ctx context.Context, sagaCtx map[string]interface{}) error

func NewSaga() *Saga {
    return &Saga{
        steps:   make([]*SagaStep, 0),
        context: make(map[string]interface{}),
    }
}

func (s *Saga) AddStep(name string, action, compensation SagaAction) {
    step := &SagaStep{
        name:         name,
        action:       action,
        compensation: compensation,
    }
    s.steps = append(s.steps, step)
}

func (s *Saga) Execute(ctx context.Context) error {
    s.mu.Lock()
    defer s.mu.Unlock()

    // Execute steps in order
    for i, step := range s.steps {
        s.currentStep = i

        log.Printf("Executing saga step: %s", step.name)

        if err := step.action(ctx, s.context); err != nil {
            log.Printf("Saga step %s failed: %v", step.name, err)

            // Execute compensations in reverse order
            if compErr := s.compensate(ctx); compErr != nil {
                return fmt.Errorf("step %s failed: %v, compensation failed: %v", step.name, err, compErr)
            }

            return fmt.Errorf("saga failed at step %s: %v", step.name, err)
        }

        log.Printf("Saga step %s completed successfully", step.name)
    }

    log.Println("Saga completed successfully")
    return nil
}

func (s *Saga) compensate(ctx context.Context) error {
    log.Println("Starting saga compensation")

    // Execute compensations in reverse order for completed steps
    for i := s.currentStep; i >= 0; i-- {
        step := s.steps[i]

        if step.compensation == nil {
            continue
        }

        log.Printf("Executing compensation for step: %s", step.name)

        if err := step.compensation(ctx, s.context); err != nil {
            log.Printf("Compensation failed for step %s: %v", step.name, err)
            return err
        }

        log.Printf("Compensation completed for step: %s", step.name)
    }

    log.Println("Saga compensation completed")
    return nil
}

// Connection health monitoring
type ConnectionHealthMonitor struct {
    db              *sqlx.DB
    healthCheckSQL  string
    checkInterval   time.Duration
    timeout         time.Duration
    unhealthyCount  int
    maxUnhealthy    int
    isHealthy       bool
    lastCheck       time.Time
    mu              sync.RWMutex
    stopChan        chan struct{}
}

func NewConnectionHealthMonitor(db *sqlx.DB) *ConnectionHealthMonitor {
    return &ConnectionHealthMonitor{
        db:             db,
        healthCheckSQL: "SELECT 1",
        checkInterval:  30 * time.Second,
        timeout:        5 * time.Second,
        maxUnhealthy:   3,
        isHealthy:      true,
        stopChan:       make(chan struct{}),
    }
}

func (chm *ConnectionHealthMonitor) Start() {
    go chm.healthCheckLoop()
}

func (chm *ConnectionHealthMonitor) Stop() {
    close(chm.stopChan)
}

func (chm *ConnectionHealthMonitor) IsHealthy() bool {
    chm.mu.RLock()
    defer chm.mu.RUnlock()
    return chm.isHealthy
}

func (chm *ConnectionHealthMonitor) healthCheckLoop() {
    ticker := time.NewTicker(chm.checkInterval)
    defer ticker.Stop()

    for {
        select {
        case <-ticker.C:
            chm.performHealthCheck()
        case <-chm.stopChan:
            return
        }
    }
}

func (chm *ConnectionHealthMonitor) performHealthCheck() {
    ctx, cancel := context.WithTimeout(context.Background(), chm.timeout)
    defer cancel()

    chm.mu.Lock()
    defer chm.mu.Unlock()

    chm.lastCheck = time.Now()

    if err := chm.db.PingContext(ctx); err != nil {
        chm.unhealthyCount++
        log.Printf("Database health check failed: %v (count: %d)", err, chm.unhealthyCount)

        if chm.unhealthyCount >= chm.maxUnhealthy {
            chm.isHealthy = false
            log.Println("Database marked as unhealthy")
        }
    } else {
        if !chm.isHealthy {
            log.Println("Database health restored")
        }
        chm.unhealthyCount = 0
        chm.isHealthy = true
    }
}

// Database sharding utilities
type ShardManager struct {
    shards    map[string]*sqlx.DB
    shardFunc func(key string) string
    mu        sync.RWMutex
}

func NewShardManager(shardFunc func(key string) string) *ShardManager {
    return &ShardManager{
        shards:    make(map[string]*sqlx.DB),
        shardFunc: shardFunc,
    }
}

func (sm *ShardManager) AddShard(name string, db *sqlx.DB) {
    sm.mu.Lock()
    defer sm.mu.Unlock()
    sm.shards[name] = db
}

func (sm *ShardManager) GetShard(key string) (*sqlx.DB, error) {
    shardName := sm.shardFunc(key)

    sm.mu.RLock()
    defer sm.mu.RUnlock()

    shard, exists := sm.shards[shardName]
    if !exists {
        return nil, fmt.Errorf("shard %s not found", shardName)
    }

    return shard, nil
}

func (sm *ShardManager) ExecuteOnAllShards(ctx context.Context, query string, args ...interface{}) error {
    sm.mu.RLock()
    shards := make([]*sqlx.DB, 0, len(sm.shards))
    for _, shard := range sm.shards {
        shards = append(shards, shard)
    }
    sm.mu.RUnlock()

    var wg sync.WaitGroup
    errorChan := make(chan error, len(shards))

    for _, shard := range shards {
        wg.Add(1)
        go func(db *sqlx.DB) {
            defer wg.Done()
            if _, err := db.ExecContext(ctx, query, args...); err != nil {
                errorChan <- err
            }
        }(shard)
    }

    wg.Wait()
    close(errorChan)

    var errors []error
    for err := range errorChan {
        errors = append(errors, err)
    }

    if len(errors) > 0 {
        return fmt.Errorf("errors on %d shards: %v", len(errors), errors)
    }

    return nil
}

// Utility functions
func generateTxID() string {
    return fmt.Sprintf("tx_%d", time.Now().UnixNano())
}

// Example usage
func main() {
    // Database configuration
    config := &DatabaseConfig{
        Host:            "localhost",
        Port:            5432,
        User:            "postgres",
        Password:        "password",
        DBName:          "testdb",
        SSLMode:         "disable",
        MaxOpenConns:    25,
        MaxIdleConns:    10,
        ConnMaxLifetime: time.Hour,
        ConnMaxIdleTime: 30 * time.Minute,
    }

    // Create database client
    db, err := NewDatabaseClient(config)
    if err != nil {
        log.Fatal(err)
    }
    defer db.Close()

    // Setup migrations
    migrationRunner := NewMigrationRunner(db)
    migrationRunner.AddMigration(Migration{
        Version: 1,
        Name:    "create_users_table",
        Up: `
            CREATE TABLE users (
                id SERIAL PRIMARY KEY,
                name VARCHAR(255) NOT NULL,
                email VARCHAR(255) UNIQUE NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        `,
        Down: "DROP TABLE users",
    })

    ctx := context.Background()
    if err := migrationRunner.Run(ctx); err != nil {
        log.Fatal(err)
    }

    // Create repository
    userRepo := NewUserRepository(db)

    // Example usage
    user := &User{
        Name:  "John Doe",
        Email: "john@example.com",
    }

    if err := userRepo.Create(ctx, user); err != nil {
        log.Printf("Failed to create user: %v", err)
    } else {
        log.Printf("Created user with ID: %d", user.ID)
    }

    // Print database stats
    stats := db.GetStats()
    log.Printf("Database stats: %+v", stats)
}
```

**Note:** This guide focuses on advanced web and database patterns. For basic HTTP server examples and simple database operations, see GOLANG_INTERVIEW_COMPREHENSIVE.md. For concurrency patterns, see GO_CONCURRENCY_CHANNELS.md. For advanced language features, see GO_ADVANCED_TOPICS.md.

[⬆️ Back to Top](#table-of-contents)