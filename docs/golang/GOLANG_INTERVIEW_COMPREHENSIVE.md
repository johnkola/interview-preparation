# Golang Interview Guide - Comprehensive Edition

## Table of Contents
1. [Introduction to Go](#introduction-to-go)
2. [Basic Syntax and Data Types](#basic-syntax-and-data-types)
3. [Functions and Methods](#functions-and-methods)
4. [Structs and Interfaces](#structs-and-interfaces)
5. [Goroutines and Concurrency](#goroutines-and-concurrency)
6. [Channels](#channels)
7. [Error Handling](#error-handling)
8. [Memory Management](#memory-management)
9. [Package Management](#package-management)
10. [Testing in Go](#testing-in-go)
11. [Web Development with Go](#web-development-with-go)
12. [Database Integration](#database-integration)
13. [Performance and Optimization](#performance-and-optimization)
14. [Design Patterns in Go](#design-patterns-in-go)
15. [Banking Domain Examples](#banking-domain-examples)
16. [Advanced Topics](#advanced-topics)
17. [Best Practices](#best-practices)
18. [Common Pitfalls](#common-pitfalls)
19. [Interview Questions by Difficulty](#interview-questions-by-difficulty)

## Introduction to Go

Go (Golang) is a statically typed, compiled programming language designed at Google. It combines the performance and security benefits of a compiled language with the ease of programming of an interpreted language.

### Key Features

**Design Principles**
- Simplicity and readability
- Strong typing and memory safety
- Built-in concurrency support
- Fast compilation
- Garbage collection

**Why Go?**
- Excellent for microservices and distributed systems
- Strong standard library
- Cross-platform compilation
- Efficient concurrency model
- Simple deployment (single binary)

### Hello World Example

```go
package main

import (
    "fmt"
)

func main() {
    fmt.Println("Hello, World!")
}
```

[⬆️ Back to Top](#table-of-contents)

## Basic Syntax and Data Types

### Variables and Constants

```go
package main

import "fmt"

func main() {
    // Variable declarations
    var name string = "John Doe"
    var age int = 30
    var salary float64 = 75000.50

    // Short variable declaration
    email := "john@example.com"
    isActive := true

    // Multiple variable declaration
    var (
        firstName string = "John"
        lastName  string = "Doe"
        balance   float64
    )

    // Constants
    const (
        MaxRetries = 3
        APIVersion = "v1"
        Pi         = 3.14159
    )

    fmt.Printf("Name: %s, Age: %d, Salary: %.2f\n", name, age, salary)
    fmt.Printf("Email: %s, Active: %v\n", email, isActive)
}
```

### Data Types

```go
package main

import (
    "fmt"
    "time"
)

// Custom types
type UserID int64
type AccountType string

const (
    Checking AccountType = "checking"
    Savings  AccountType = "savings"
)

func main() {
    // Basic types
    var (
        // Integers
        userID     UserID = 12345
        balance    int64  = 1000000 // cents

        // Floats
        interestRate float32 = 0.025
        amount       float64 = 1500.75

        // Strings
        accountNumber string = "ACC-001-789"

        // Booleans
        isVerified bool = true

        // Arrays and Slices
        recentTransactions [5]float64
        transactionHistory []float64

        // Maps
        userAccounts map[UserID][]string

        // Pointers
        balancePtr *int64
    )

    // Initialize collections
    recentTransactions = [5]float64{100.0, 250.0, 75.5, 300.0, 125.25}
    transactionHistory = make([]float64, 0, 100)
    userAccounts = make(map[UserID][]string)

    // Pointer operations
    balancePtr = &balance

    fmt.Printf("User ID: %d\n", userID)
    fmt.Printf("Balance: $%.2f\n", float64(balance)/100)
    fmt.Printf("Interest Rate: %.3f%%\n", interestRate*100)
    fmt.Printf("Pointer to balance: %v, Value: %d\n", balancePtr, *balancePtr)
}
```

### Control Flow

```go
package main

import (
    "fmt"
    "time"
)

func main() {
    balance := 1500.0
    accountType := "checking"

    // If-else statements
    if balance >= 1000 {
        fmt.Println("Premium account benefits available")
    } else if balance >= 500 {
        fmt.Println("Standard account")
    } else {
        fmt.Println("Basic account")
    }

    // Switch statements
    switch accountType {
    case "checking":
        fmt.Println("No monthly fees for checking")
    case "savings":
        fmt.Println("0.5% interest rate for savings")
    case "investment":
        fmt.Println("Variable returns based on portfolio")
    default:
        fmt.Println("Unknown account type")
    }

    // For loops
    transactions := []float64{100.0, -50.0, 200.0, -25.0, 150.0}

    // Traditional for loop
    for i := 0; i < len(transactions); i++ {
        fmt.Printf("Transaction %d: $%.2f\n", i+1, transactions[i])
    }

    // Range loop
    for index, amount := range transactions {
        if amount > 0 {
            fmt.Printf("Deposit %d: $%.2f\n", index+1, amount)
        } else {
            fmt.Printf("Withdrawal %d: $%.2f\n", index+1, -amount)
        }
    }

    // While-like loop
    attempts := 0
    maxAttempts := 3

    for attempts < maxAttempts {
        fmt.Printf("Authentication attempt %d\n", attempts+1)
        // Simulate authentication logic
        if authenticateUser() {
            fmt.Println("Authentication successful")
            break
        }
        attempts++
        time.Sleep(time.Second)
    }

    if attempts >= maxAttempts {
        fmt.Println("Account locked due to multiple failed attempts")
    }
}

func authenticateUser() bool {
    // Simulate authentication (returns true for demo)
    return true
}
```

[⬆️ Back to Top](#table-of-contents)

## Functions and Methods

### Function Basics

```go
package main

import (
    "errors"
    "fmt"
    "math"
)

// Simple function
func calculateInterest(principal, rate float64, years int) float64 {
    return principal * rate * float64(years)
}

// Multiple return values
func divide(a, b float64) (float64, error) {
    if b == 0 {
        return 0, errors.New("division by zero")
    }
    return a / b, nil
}

// Named return values
func calculateLoanPayment(principal, rate float64, months int) (monthlyPayment, totalInterest float64) {
    if months == 0 {
        return 0, 0
    }

    monthlyRate := rate / 12
    monthlyPayment = principal * (monthlyRate * math.Pow(1+monthlyRate, float64(months))) /
                    (math.Pow(1+monthlyRate, float64(months)) - 1)
    totalInterest = (monthlyPayment * float64(months)) - principal
    return // naked return
}

// Variadic functions
func sumTransactions(transactions ...float64) float64 {
    var total float64
    for _, amount := range transactions {
        total += amount
    }
    return total
}

// Higher-order functions
func applyToBalance(balance float64, operations ...func(float64) float64) float64 {
    for _, op := range operations {
        balance = op(balance)
    }
    return balance
}

// Closures
func createMultiplier(factor float64) func(float64) float64 {
    return func(value float64) float64 {
        return value * factor
    }
}

func main() {
    // Simple function call
    interest := calculateInterest(10000, 0.05, 3)
    fmt.Printf("Simple interest: $%.2f\n", interest)

    // Multiple return values
    result, err := divide(100, 5)
    if err != nil {
        fmt.Printf("Error: %v\n", err)
    } else {
        fmt.Printf("Division result: %.2f\n", result)
    }

    // Named returns
    payment, totalInt := calculateLoanPayment(250000, 0.04, 360)
    fmt.Printf("Monthly payment: $%.2f, Total interest: $%.2f\n", payment, totalInt)

    // Variadic function
    total := sumTransactions(100.0, -50.0, 200.0, -25.0)
    fmt.Printf("Total transactions: $%.2f\n", total)

    // Higher-order function
    balance := 1000.0
    addBonus := func(b float64) float64 { return b + 100 }
    applyTax := func(b float64) float64 { return b * 0.9 }

    finalBalance := applyToBalance(balance, addBonus, applyTax)
    fmt.Printf("Final balance: $%.2f\n", finalBalance)

    // Closure
    percentageCalculator := createMultiplier(0.01)
    onePercent := percentageCalculator(balance)
    fmt.Printf("1%% of balance: $%.2f\n", onePercent)
}
```

### Methods and Receivers

```go
package main

import (
    "fmt"
    "time"
)

// Custom types for banking domain
type AccountID string
type Currency string

const (
    USD Currency = "USD"
    EUR Currency = "EUR"
    GBP Currency = "GBP"
)

// Account struct
type Account struct {
    ID            AccountID
    HolderName    string
    Balance       float64
    Currency      Currency
    CreatedAt     time.Time
    IsActive      bool
    transactions  []Transaction // private field
}

// Transaction struct
type Transaction struct {
    ID          string
    AccountID   AccountID
    Amount      float64
    Type        string
    Description string
    Timestamp   time.Time
}

// Method with value receiver (doesn't modify original)
func (a Account) GetFormattedBalance() string {
    return fmt.Sprintf("%.2f %s", a.Balance, a.Currency)
}

// Method with pointer receiver (can modify original)
func (a *Account) Deposit(amount float64, description string) error {
    if amount <= 0 {
        return fmt.Errorf("deposit amount must be positive")
    }

    a.Balance += amount
    transaction := Transaction{
        ID:          generateTransactionID(),
        AccountID:   a.ID,
        Amount:      amount,
        Type:        "deposit",
        Description: description,
        Timestamp:   time.Now(),
    }

    a.transactions = append(a.transactions, transaction)
    return nil
}

func (a *Account) Withdraw(amount float64, description string) error {
    if amount <= 0 {
        return fmt.Errorf("withdrawal amount must be positive")
    }

    if a.Balance < amount {
        return fmt.Errorf("insufficient funds: balance %.2f, requested %.2f",
                         a.Balance, amount)
    }

    a.Balance -= amount
    transaction := Transaction{
        ID:          generateTransactionID(),
        AccountID:   a.ID,
        Amount:      -amount,
        Type:        "withdrawal",
        Description: description,
        Timestamp:   time.Now(),
    }

    a.transactions = append(a.transactions, transaction)
    return nil
}

func (a Account) GetTransactionHistory() []Transaction {
    // Return a copy to protect internal state
    history := make([]Transaction, len(a.transactions))
    copy(history, a.transactions)
    return history
}

func (a *Account) Transfer(to *Account, amount float64, description string) error {
    if amount <= 0 {
        return fmt.Errorf("transfer amount must be positive")
    }

    if a.Currency != to.Currency {
        return fmt.Errorf("currency mismatch: %s vs %s", a.Currency, to.Currency)
    }

    // Check sufficient funds
    if a.Balance < amount {
        return fmt.Errorf("insufficient funds for transfer")
    }

    // Perform the transfer
    err := a.Withdraw(amount, fmt.Sprintf("Transfer to %s: %s", to.ID, description))
    if err != nil {
        return err
    }

    err = to.Deposit(amount, fmt.Sprintf("Transfer from %s: %s", a.ID, description))
    if err != nil {
        // Rollback the withdrawal
        a.Deposit(amount, "Rollback failed transfer")
        return err
    }

    return nil
}

// Method for calculating account statistics
func (a Account) GetAccountStats() map[string]interface{} {
    stats := make(map[string]interface{})

    stats["account_id"] = a.ID
    stats["current_balance"] = a.Balance
    stats["transaction_count"] = len(a.transactions)
    stats["account_age_days"] = int(time.Since(a.CreatedAt).Hours() / 24)

    var totalDeposits, totalWithdrawals float64
    var depositCount, withdrawalCount int

    for _, tx := range a.transactions {
        if tx.Amount > 0 {
            totalDeposits += tx.Amount
            depositCount++
        } else {
            totalWithdrawals += -tx.Amount
            withdrawalCount++
        }
    }

    stats["total_deposits"] = totalDeposits
    stats["total_withdrawals"] = totalWithdrawals
    stats["deposit_count"] = depositCount
    stats["withdrawal_count"] = withdrawalCount

    if depositCount > 0 {
        stats["avg_deposit"] = totalDeposits / float64(depositCount)
    }

    if withdrawalCount > 0 {
        stats["avg_withdrawal"] = totalWithdrawals / float64(withdrawalCount)
    }

    return stats
}

func generateTransactionID() string {
    return fmt.Sprintf("TXN-%d", time.Now().UnixNano())
}

func main() {
    // Create accounts
    account1 := &Account{
        ID:         "ACC-001",
        HolderName: "Alice Johnson",
        Balance:    1000.0,
        Currency:   USD,
        CreatedAt:  time.Now().AddDate(0, -6, 0), // 6 months ago
        IsActive:   true,
    }

    account2 := &Account{
        ID:         "ACC-002",
        HolderName: "Bob Smith",
        Balance:    500.0,
        Currency:   USD,
        CreatedAt:  time.Now().AddDate(0, -3, 0), // 3 months ago
        IsActive:   true,
    }

    fmt.Printf("Account 1 initial balance: %s\n", account1.GetFormattedBalance())
    fmt.Printf("Account 2 initial balance: %s\n", account2.GetFormattedBalance())

    // Perform some transactions
    err := account1.Deposit(500.0, "Salary deposit")
    if err != nil {
        fmt.Printf("Deposit error: %v\n", err)
    }

    err = account1.Withdraw(200.0, "ATM withdrawal")
    if err != nil {
        fmt.Printf("Withdrawal error: %v\n", err)
    }

    // Transfer between accounts
    err = account1.Transfer(account2, 300.0, "Payment for services")
    if err != nil {
        fmt.Printf("Transfer error: %v\n", err)
    } else {
        fmt.Println("Transfer completed successfully")
    }

    fmt.Printf("Account 1 final balance: %s\n", account1.GetFormattedBalance())
    fmt.Printf("Account 2 final balance: %s\n", account2.GetFormattedBalance())

    // Get account statistics
    stats := account1.GetAccountStats()
    fmt.Printf("\nAccount 1 Statistics:\n")
    for key, value := range stats {
        fmt.Printf("%s: %v\n", key, value)
    }

    // Show transaction history
    fmt.Printf("\nAccount 1 Transaction History:\n")
    for _, tx := range account1.GetTransactionHistory() {
        fmt.Printf("%s: %.2f %s - %s (%s)\n",
                   tx.Timestamp.Format("2006-01-02 15:04:05"),
                   tx.Amount, account1.Currency, tx.Description, tx.Type)
    }
}
```

[⬆️ Back to Top](#table-of-contents)

## Structs and Interfaces

### Struct Embedding and Composition

```go
package main

import (
    "fmt"
    "time"
)

// Base entity with common fields
type BaseEntity struct {
    ID        string
    CreatedAt time.Time
    UpdatedAt time.Time
    Version   int
}

// Update the entity version and timestamp
func (e *BaseEntity) Touch() {
    e.UpdatedAt = time.Now()
    e.Version++
}

// Person represents a person with contact information
type Person struct {
    BaseEntity // Embedded struct
    FirstName  string
    LastName   string
    Email      string
    Phone      string
    Address    Address
}

func (p Person) FullName() string {
    return fmt.Sprintf("%s %s", p.FirstName, p.LastName)
}

// Address represents physical address
type Address struct {
    Street   string
    City     string
    State    string
    ZipCode  string
    Country  string
}

func (a Address) String() string {
    return fmt.Sprintf("%s, %s, %s %s, %s", a.Street, a.City, a.State, a.ZipCode, a.Country)
}

// Customer embeds Person and adds banking-specific fields
type Customer struct {
    Person                    // Embedded struct
    CustomerNumber   string
    AccountNumbers   []string
    CreditScore      int
    KYCStatus        string
    RiskLevel        string
    PreferredContact string
}

func (c *Customer) AddAccount(accountNumber string) {
    c.AccountNumbers = append(c.AccountNumbers, accountNumber)
    c.Touch() // Update timestamp from embedded BaseEntity
}

// Employee also embeds Person but adds employment fields
type Employee struct {
    Person          // Embedded struct
    EmployeeID      string
    Department      string
    Position        string
    HireDate        time.Time
    Salary          float64
    Manager         *Employee
    AccessLevel     int
    Permissions     []string
}

func (e Employee) YearsOfService() int {
    return int(time.Since(e.HireDate).Hours() / (24 * 365))
}

func (e *Employee) PromoteTo(newPosition string, newSalary float64) {
    e.Position = newPosition
    e.Salary = newSalary
    e.Touch()
}

// Account represents a bank account
type BankAccount struct {
    BaseEntity
    AccountNumber    string
    CustomerID       string
    AccountType      string
    Balance          float64
    Currency         string
    Status           string
    InterestRate     float64
    MinimumBalance   float64
    MonthlyFee       float64
    OverdraftLimit   float64
}

func (ba *BankAccount) CalculateMonthlyInterest() float64 {
    if ba.Balance <= 0 {
        return 0
    }
    return ba.Balance * (ba.InterestRate / 12)
}

func (ba *BankAccount) ApplyMonthlyFee() {
    if ba.Balance < ba.MinimumBalance && ba.MonthlyFee > 0 {
        ba.Balance -= ba.MonthlyFee
        ba.Touch()
    }
}

// CreditCard extends BankAccount with credit-specific features
type CreditCard struct {
    BankAccount          // Embedded struct
    CreditLimit   float64
    AvailableCredit float64
    APR           float64
    MinPayment    float64
    DueDate       time.Time
    LastPayment   time.Time
}

func (cc *CreditCard) MakePayment(amount float64) error {
    if amount <= 0 {
        return fmt.Errorf("payment amount must be positive")
    }

    // For credit cards, positive balance means credit available
    cc.Balance += amount
    cc.AvailableCredit = cc.CreditLimit - (-cc.Balance) // Balance is negative for debt
    cc.LastPayment = time.Now()
    cc.Touch()

    return nil
}

func (cc *CreditCard) MakePurchase(amount float64) error {
    if amount <= 0 {
        return fmt.Errorf("purchase amount must be positive")
    }

    if cc.AvailableCredit < amount {
        return fmt.Errorf("insufficient credit: available %.2f, requested %.2f",
                         cc.AvailableCredit, amount)
    }

    cc.Balance -= amount
    cc.AvailableCredit -= amount
    cc.Touch()

    return nil
}

func main() {
    // Create a customer
    customer := &Customer{
        Person: Person{
            BaseEntity: BaseEntity{
                ID:        "CUST-001",
                CreatedAt: time.Now(),
                UpdatedAt: time.Now(),
                Version:   1,
            },
            FirstName: "John",
            LastName:  "Doe",
            Email:     "john.doe@email.com",
            Phone:     "+1-555-0123",
            Address: Address{
                Street:  "123 Main St",
                City:    "Anytown",
                State:   "CA",
                ZipCode: "12345",
                Country: "USA",
            },
        },
        CustomerNumber:   "CN123456",
        CreditScore:      750,
        KYCStatus:        "Verified",
        RiskLevel:        "Low",
        PreferredContact: "Email",
    }

    // Create an employee
    employee := &Employee{
        Person: Person{
            BaseEntity: BaseEntity{
                ID:        "EMP-001",
                CreatedAt: time.Now(),
                UpdatedAt: time.Now(),
                Version:   1,
            },
            FirstName: "Jane",
            LastName:  "Smith",
            Email:     "jane.smith@bank.com",
            Phone:     "+1-555-0456",
            Address: Address{
                Street:  "456 Bank St",
                City:    "Financial District",
                State:   "NY",
                ZipCode: "10004",
                Country: "USA",
            },
        },
        EmployeeID:  "EMP123456",
        Department:  "Customer Service",
        Position:    "Senior Teller",
        HireDate:    time.Now().AddDate(-3, 0, 0), // Hired 3 years ago
        Salary:      55000,
        AccessLevel: 3,
        Permissions: []string{"view_accounts", "process_transactions", "customer_support"},
    }

    // Create a bank account
    account := &BankAccount{
        BaseEntity: BaseEntity{
            ID:        "ACC-001",
            CreatedAt: time.Now(),
            UpdatedAt: time.Now(),
            Version:   1,
        },
        AccountNumber:  "1234567890",
        CustomerID:     customer.ID,
        AccountType:    "Checking",
        Balance:        2500.0,
        Currency:       "USD",
        Status:         "Active",
        InterestRate:   0.01, // 1% annual
        MinimumBalance: 100.0,
        MonthlyFee:     5.0,
        OverdraftLimit: 500.0,
    }

    // Create a credit card
    creditCard := &CreditCard{
        BankAccount: BankAccount{
            BaseEntity: BaseEntity{
                ID:        "CC-001",
                CreatedAt: time.Now(),
                UpdatedAt: time.Now(),
                Version:   1,
            },
            AccountNumber: "4532123456789012",
            CustomerID:    customer.ID,
            AccountType:   "Credit Card",
            Balance:       -1200.0, // Negative balance indicates debt
            Currency:      "USD",
            Status:        "Active",
        },
        CreditLimit:     5000.0,
        AvailableCredit: 3800.0, // 5000 - 1200
        APR:             0.18,   // 18% annual
        MinPayment:      50.0,
        DueDate:         time.Now().AddDate(0, 0, 25), // Due in 25 days
    }

    // Add account to customer
    customer.AddAccount(account.AccountNumber)
    customer.AddAccount(creditCard.AccountNumber)

    // Display information
    fmt.Printf("Customer: %s\n", customer.FullName())
    fmt.Printf("Customer Number: %s\n", customer.CustomerNumber)
    fmt.Printf("Address: %s\n", customer.Address)
    fmt.Printf("Credit Score: %d\n", customer.CreditScore)
    fmt.Printf("Accounts: %v\n", customer.AccountNumbers)
    fmt.Printf("Customer Version: %d\n", customer.Version)

    fmt.Printf("\nEmployee: %s\n", employee.FullName())
    fmt.Printf("Position: %s\n", employee.Position)
    fmt.Printf("Years of Service: %d\n", employee.YearsOfService())
    fmt.Printf("Salary: $%.2f\n", employee.Salary)

    fmt.Printf("\nBank Account: %s\n", account.AccountNumber)
    fmt.Printf("Balance: $%.2f\n", account.Balance)
    fmt.Printf("Monthly Interest: $%.2f\n", account.CalculateMonthlyInterest())

    fmt.Printf("\nCredit Card: %s\n", creditCard.AccountNumber)
    fmt.Printf("Current Balance: $%.2f\n", creditCard.Balance)
    fmt.Printf("Available Credit: $%.2f\n", creditCard.AvailableCredit)

    // Demonstrate method calls
    err := creditCard.MakePayment(200.0)
    if err != nil {
        fmt.Printf("Payment error: %v\n", err)
    } else {
        fmt.Printf("After payment - Balance: $%.2f, Available Credit: $%.2f\n",
                   creditCard.Balance, creditCard.AvailableCredit)
    }

    employee.PromoteTo("Assistant Manager", 65000)
    fmt.Printf("After promotion - Position: %s, Salary: $%.2f\n",
               employee.Position, employee.Salary)
}
```

### Interfaces

```go
package main

import (
    "fmt"
    "math"
    "time"
)

// PaymentProcessor interface defines payment processing capabilities
type PaymentProcessor interface {
    ProcessPayment(amount float64, currency string) (*PaymentResult, error)
    GetSupportedCurrencies() []string
    GetProcessingFee(amount float64) float64
}

// AccountManager interface defines account management operations
type AccountManager interface {
    CreateAccount(customerID string, accountType string) (*Account, error)
    GetAccount(accountID string) (*Account, error)
    UpdateAccount(account *Account) error
    CloseAccount(accountID string) error
}

// TransactionLogger interface for logging transactions
type TransactionLogger interface {
    LogTransaction(transaction Transaction) error
    GetTransactionHistory(accountID string, limit int) ([]Transaction, error)
}

// NotificationService interface for sending notifications
type NotificationService interface {
    SendNotification(recipient string, message string, channel string) error
    GetSupportedChannels() []string
}

// Auditable interface for entities that can be audited
type Auditable interface {
    GetAuditInfo() AuditInfo
    UpdateAuditInfo(userID string)
}

// Common structs
type PaymentResult struct {
    TransactionID string
    Status        string
    ProcessedAt   time.Time
    Fee           float64
    Currency      string
}

type Account struct {
    ID           string
    CustomerID   string
    AccountType  string
    Balance      float64
    Currency     string
    Status       string
    CreatedAt    time.Time
    UpdatedAt    time.Time
    LastModifier string
}

type Transaction struct {
    ID          string
    AccountID   string
    Amount      float64
    Currency    string
    Type        string
    Description string
    ProcessedAt time.Time
    ProcessorID string
}

type AuditInfo struct {
    CreatedAt    time.Time
    CreatedBy    string
    UpdatedAt    time.Time
    UpdatedBy    string
    Version      int
}

// Implement Auditable interface for Account
func (a *Account) GetAuditInfo() AuditInfo {
    return AuditInfo{
        CreatedAt: a.CreatedAt,
        UpdatedAt: a.UpdatedAt,
        UpdatedBy: a.LastModifier,
        Version:   1, // Simplified for demo
    }
}

func (a *Account) UpdateAuditInfo(userID string) {
    a.UpdatedAt = time.Now()
    a.LastModifier = userID
}

// CreditCardProcessor implements PaymentProcessor interface
type CreditCardProcessor struct {
    ProcessorName    string
    SupportedCards   []string
    BaseFeePercent   float64
    MinFee           float64
    MaxFee           float64
    SupportedCurrencies []string
}

func (ccp CreditCardProcessor) ProcessPayment(amount float64, currency string) (*PaymentResult, error) {
    if amount <= 0 {
        return nil, fmt.Errorf("amount must be positive")
    }

    if !ccp.supportsCurrency(currency) {
        return nil, fmt.Errorf("currency %s not supported", currency)
    }

    // Simulate processing delay
    time.Sleep(time.Millisecond * 100)

    fee := ccp.GetProcessingFee(amount)

    return &PaymentResult{
        TransactionID: fmt.Sprintf("CC-%d", time.Now().UnixNano()),
        Status:        "approved",
        ProcessedAt:   time.Now(),
        Fee:           fee,
        Currency:      currency,
    }, nil
}

func (ccp CreditCardProcessor) GetSupportedCurrencies() []string {
    return ccp.SupportedCurrencies
}

func (ccp CreditCardProcessor) GetProcessingFee(amount float64) float64 {
    fee := amount * ccp.BaseFeePercent
    if fee < ccp.MinFee {
        fee = ccp.MinFee
    }
    if fee > ccp.MaxFee {
        fee = ccp.MaxFee
    }
    return fee
}

func (ccp CreditCardProcessor) supportsCurrency(currency string) bool {
    for _, c := range ccp.SupportedCurrencies {
        if c == currency {
            return true
        }
    }
    return false
}

// ACHProcessor implements PaymentProcessor interface
type ACHProcessor struct {
    ProcessorName     string
    FlatFee          float64
    ProcessingDays   int
    SupportedCurrencies []string
}

func (ach ACHProcessor) ProcessPayment(amount float64, currency string) (*PaymentResult, error) {
    if amount <= 0 {
        return nil, fmt.Errorf("amount must be positive")
    }

    if currency != "USD" {
        return nil, fmt.Errorf("ACH only supports USD")
    }

    // ACH has longer processing time
    time.Sleep(time.Millisecond * 200)

    return &PaymentResult{
        TransactionID: fmt.Sprintf("ACH-%d", time.Now().UnixNano()),
        Status:        "pending", // ACH typically starts as pending
        ProcessedAt:   time.Now(),
        Fee:           ach.FlatFee,
        Currency:      currency,
    }, nil
}

func (ach ACHProcessor) GetSupportedCurrencies() []string {
    return ach.SupportedCurrencies
}

func (ach ACHProcessor) GetProcessingFee(amount float64) float64 {
    return ach.FlatFee
}

// BankingService implements multiple interfaces
type BankingService struct {
    accounts     map[string]*Account
    transactions []Transaction
    processors   map[string]PaymentProcessor
    nextAccountID int
}

func NewBankingService() *BankingService {
    return &BankingService{
        accounts:     make(map[string]*Account),
        transactions: make([]Transaction, 0),
        processors:   make(map[string]PaymentProcessor),
        nextAccountID: 1000,
    }
}

// Implement AccountManager interface
func (bs *BankingService) CreateAccount(customerID string, accountType string) (*Account, error) {
    if customerID == "" {
        return nil, fmt.Errorf("customer ID cannot be empty")
    }

    accountID := fmt.Sprintf("ACC-%06d", bs.nextAccountID)
    bs.nextAccountID++

    account := &Account{
        ID:          accountID,
        CustomerID:  customerID,
        AccountType: accountType,
        Balance:     0.0,
        Currency:    "USD",
        Status:      "active",
        CreatedAt:   time.Now(),
        UpdatedAt:   time.Now(),
    }

    bs.accounts[accountID] = account
    return account, nil
}

func (bs *BankingService) GetAccount(accountID string) (*Account, error) {
    account, exists := bs.accounts[accountID]
    if !exists {
        return nil, fmt.Errorf("account %s not found", accountID)
    }
    return account, nil
}

func (bs *BankingService) UpdateAccount(account *Account) error {
    if account == nil {
        return fmt.Errorf("account cannot be nil")
    }

    _, exists := bs.accounts[account.ID]
    if !exists {
        return fmt.Errorf("account %s not found", account.ID)
    }

    account.UpdatedAt = time.Now()
    bs.accounts[account.ID] = account
    return nil
}

func (bs *BankingService) CloseAccount(accountID string) error {
    account, exists := bs.accounts[accountID]
    if !exists {
        return fmt.Errorf("account %s not found", accountID)
    }

    if account.Balance != 0 {
        return fmt.Errorf("cannot close account with non-zero balance: %.2f", account.Balance)
    }

    account.Status = "closed"
    account.UpdatedAt = time.Now()
    return nil
}

// Implement TransactionLogger interface
func (bs *BankingService) LogTransaction(transaction Transaction) error {
    transaction.ProcessedAt = time.Now()
    bs.transactions = append(bs.transactions, transaction)
    return nil
}

func (bs *BankingService) GetTransactionHistory(accountID string, limit int) ([]Transaction, error) {
    var accountTransactions []Transaction

    for _, tx := range bs.transactions {
        if tx.AccountID == accountID {
            accountTransactions = append(accountTransactions, tx)
        }
    }

    // Apply limit
    if limit > 0 && len(accountTransactions) > limit {
        accountTransactions = accountTransactions[:limit]
    }

    return accountTransactions, nil
}

// Add payment processor
func (bs *BankingService) AddPaymentProcessor(name string, processor PaymentProcessor) {
    bs.processors[name] = processor
}

// Process payment using specified processor
func (bs *BankingService) ProcessPaymentWithProcessor(processorName string, accountID string,
                                                     amount float64, currency string) (*PaymentResult, error) {
    processor, exists := bs.processors[processorName]
    if !exists {
        return nil, fmt.Errorf("payment processor %s not found", processorName)
    }

    account, err := bs.GetAccount(accountID)
    if err != nil {
        return nil, err
    }

    result, err := processor.ProcessPayment(amount, currency)
    if err != nil {
        return nil, err
    }

    // Update account balance
    account.Balance += amount
    account.UpdateAuditInfo("system")

    // Log transaction
    transaction := Transaction{
        ID:          result.TransactionID,
        AccountID:   accountID,
        Amount:      amount,
        Currency:    currency,
        Type:        "payment",
        Description: fmt.Sprintf("Payment processed via %s", processorName),
        ProcessorID: processorName,
    }

    bs.LogTransaction(transaction)

    return result, nil
}

// Interface demonstration functions
func demonstratePolymorphism(processors []PaymentProcessor, amount float64, currency string) {
    fmt.Printf("Processing payment of %.2f %s with different processors:\n", amount, currency)

    for i, processor := range processors {
        result, err := processor.ProcessPayment(amount, currency)
        if err != nil {
            fmt.Printf("Processor %d error: %v\n", i+1, err)
            continue
        }

        fmt.Printf("Processor %d: ID=%s, Status=%s, Fee=%.2f\n",
                   i+1, result.TransactionID, result.Status, result.Fee)
    }
}

func processAuditableEntities(entities []Auditable, userID string) {
    fmt.Printf("Processing auditable entities for user %s:\n", userID)

    for i, entity := range entities {
        auditInfo := entity.GetAuditInfo()
        fmt.Printf("Entity %d: Last updated %s by %s\n",
                   i+1, auditInfo.UpdatedAt.Format("2006-01-02 15:04:05"), auditInfo.UpdatedBy)

        entity.UpdateAuditInfo(userID)

        newAuditInfo := entity.GetAuditInfo()
        fmt.Printf("Entity %d: Now updated %s by %s\n",
                   i+1, newAuditInfo.UpdatedAt.Format("2006-01-02 15:04:05"), newAuditInfo.UpdatedBy)
    }
}

func main() {
    // Create banking service
    bankingService := NewBankingService()

    // Create payment processors
    creditCardProcessor := CreditCardProcessor{
        ProcessorName:       "VISA/MasterCard",
        SupportedCards:      []string{"VISA", "MasterCard", "AMEX"},
        BaseFeePercent:      0.029, // 2.9%
        MinFee:              0.30,
        MaxFee:              100.00,
        SupportedCurrencies: []string{"USD", "EUR", "GBP"},
    }

    achProcessor := ACHProcessor{
        ProcessorName:       "ACH Network",
        FlatFee:            0.25,
        ProcessingDays:     2,
        SupportedCurrencies: []string{"USD"},
    }

    // Add processors to banking service
    bankingService.AddPaymentProcessor("credit_card", creditCardProcessor)
    bankingService.AddPaymentProcessor("ach", achProcessor)

    // Create accounts
    account1, err := bankingService.CreateAccount("CUST-001", "checking")
    if err != nil {
        fmt.Printf("Error creating account: %v\n", err)
        return
    }

    account2, err := bankingService.CreateAccount("CUST-002", "savings")
    if err != nil {
        fmt.Printf("Error creating account: %v\n", err)
        return
    }

    fmt.Printf("Created accounts: %s and %s\n", account1.ID, account2.ID)

    // Demonstrate polymorphism with PaymentProcessor interface
    processors := []PaymentProcessor{creditCardProcessor, achProcessor}
    demonstratePolymorphism(processors, 100.0, "USD")

    fmt.Println()

    // Process payments through banking service
    result1, err := bankingService.ProcessPaymentWithProcessor("credit_card", account1.ID, 500.0, "USD")
    if err != nil {
        fmt.Printf("Payment error: %v\n", err)
    } else {
        fmt.Printf("Credit card payment: %s, Fee: %.2f\n", result1.TransactionID, result1.Fee)
    }

    result2, err := bankingService.ProcessPaymentWithProcessor("ach", account2.ID, 1000.0, "USD")
    if err != nil {
        fmt.Printf("Payment error: %v\n", err)
    } else {
        fmt.Printf("ACH payment: %s, Fee: %.2f\n", result2.TransactionID, result2.Fee)
    }

    // Check account balances
    updatedAccount1, _ := bankingService.GetAccount(account1.ID)
    updatedAccount2, _ := bankingService.GetAccount(account2.ID)

    fmt.Printf("Account %s balance: %.2f\n", updatedAccount1.ID, updatedAccount1.Balance)
    fmt.Printf("Account %s balance: %.2f\n", updatedAccount2.ID, updatedAccount2.Balance)

    // Demonstrate Auditable interface
    fmt.Println("\nAuditable entities demonstration:")
    auditableEntities := []Auditable{updatedAccount1, updatedAccount2}
    processAuditableEntities(auditableEntities, "admin-001")

    // Get transaction history
    history1, err := bankingService.GetTransactionHistory(account1.ID, 10)
    if err != nil {
        fmt.Printf("Error getting transaction history: %v\n", err)
    } else {
        fmt.Printf("\nTransaction history for %s:\n", account1.ID)
        for _, tx := range history1 {
            fmt.Printf("  %s: %.2f %s - %s\n", tx.ID, tx.Amount, tx.Currency, tx.Description)
        }
    }
}
```

[⬆️ Back to Top](#table-of-contents)

## Goroutines and Concurrency

### Go vs Java Concurrency Comparison

#### Fundamental Differences

| Aspect | Go (Goroutines) | Java (Threads) |
|--------|-----------------|----------------|
| **Creation Cost** | ~2KB initial stack | ~1-2MB per thread |
| **Memory Model** | CSP (Communicating Sequential Processes) | Shared Memory |
| **Primary Communication** | Channels | Shared variables + locks |
| **Runtime Management** | Go runtime manages M:N mapping | 1:1 mapping to OS threads |
| **Syntax Complexity** | Simple `go` keyword | Thread classes, Runnable interface |
| **Scalability** | Millions of goroutines | Hundreds to thousands of threads |

#### Code Comparison Examples

**Creating Concurrent Tasks:**

```go
// Go - Simple and lightweight
func main() {
    for i := 0; i < 10000; i++ {
        go func(id int) {
            fmt.Printf("Goroutine %d\n", id)
        }(i)
    }

    time.Sleep(time.Second) // Wait for completion
}
```

```java
// Java - More verbose and resource-intensive
public class JavaConcurrency {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(100);

        for (int i = 0; i < 10000; i++) {
            final int id = i;
            executor.submit(() -> {
                System.out.println("Thread " + id);
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
```

**Producer-Consumer Pattern:**

```go
// Go - Using channels (no explicit locks needed)
func goProducerConsumer() {
    dataChannel := make(chan int, 100)

    // Producer
    go func() {
        defer close(dataChannel)
        for i := 0; i < 1000; i++ {
            dataChannel <- i // Send to channel
        }
    }()

    // Consumer
    go func() {
        for data := range dataChannel { // Receive from channel
            processData(data)
        }
    }()
}
```

```java
// Java - Using BlockingQueue with explicit synchronization
public class JavaProducerConsumer {
    private BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(100);

    public void start() {
        // Producer
        new Thread(() -> {
            try {
                for (int i = 0; i < 1000; i++) {
                    queue.put(i); // Blocking put
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();

        // Consumer
        new Thread(() -> {
            try {
                while (true) {
                    Integer data = queue.take(); // Blocking take
                    processData(data);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}
```

**Synchronization Comparison:**

```go
// Go - Mutex for shared state (when channels aren't suitable)
type SafeCounter struct {
    mu    sync.Mutex
    value int
}

func (c *SafeCounter) Increment() {
    c.mu.Lock()
    defer c.mu.Unlock()
    c.value++
}

func (c *SafeCounter) Value() int {
    c.mu.Lock()
    defer c.mu.Unlock()
    return c.value
}

// Go - Channel-based synchronization (preferred)
type ChannelCounter struct {
    ch chan int
}

func NewChannelCounter() *ChannelCounter {
    cc := &ChannelCounter{ch: make(chan int)}
    go func() {
        count := 0
        for cmd := range cc.ch {
            if cmd == 1 {
                count++
            }
            // Send back current value
            cc.ch <- count
        }
    }()
    return cc
}

func (cc *ChannelCounter) Increment() {
    cc.ch <- 1
    <-cc.ch // Wait for response
}
```

```java
// Java - Synchronized methods
public class JavaSafeCounter {
    private int value = 0;

    public synchronized void increment() {
        value++;
    }

    public synchronized int getValue() {
        return value;
    }
}

// Java - AtomicInteger for better performance
public class JavaAtomicCounter {
    private AtomicInteger value = new AtomicInteger(0);

    public void increment() {
        value.incrementAndGet();
    }

    public int getValue() {
        return value.get();
    }
}

// Java - ReentrantLock for more control
public class JavaLockCounter {
    private final ReentrantLock lock = new ReentrantLock();
    private int value = 0;

    public void increment() {
        lock.lock();
        try {
            value++;
        } finally {
            lock.unlock();
        }
    }

    public int getValue() {
        lock.lock();
        try {
            return value;
        } finally {
            lock.unlock();
        }
    }
}
```

#### Banking Example Comparison

**Go - Channel-based Transaction Processing:**

```go
type GoTransactionProcessor struct {
    transactions chan Transaction
    results      chan TransactionResult
    workers      int
}

func NewGoTransactionProcessor(workers int) *GoTransactionProcessor {
    tp := &GoTransactionProcessor{
        transactions: make(chan Transaction, 1000),
        results:      make(chan TransactionResult, 1000),
        workers:      workers,
    }

    // Start worker goroutines
    for i := 0; i < workers; i++ {
        go tp.worker(i)
    }

    return tp
}

func (tp *GoTransactionProcessor) worker(id int) {
    for transaction := range tp.transactions {
        result := tp.processTransaction(transaction)
        result.WorkerID = id
        tp.results <- result
    }
}

func (tp *GoTransactionProcessor) ProcessAsync(tx Transaction) {
    tp.transactions <- tx
}

func (tp *GoTransactionProcessor) GetResult() TransactionResult {
    return <-tp.results
}

// Usage
func main() {
    processor := NewGoTransactionProcessor(10)

    // Send 1000 transactions
    for i := 0; i < 1000; i++ {
        tx := Transaction{ID: fmt.Sprintf("TXN-%d", i), Amount: float64(i * 100)}
        processor.ProcessAsync(tx)
    }

    // Collect results
    for i := 0; i < 1000; i++ {
        result := processor.GetResult()
        fmt.Printf("Processed: %s by worker %d\n", result.TransactionID, result.WorkerID)
    }
}
```

**Java - Thread Pool-based Transaction Processing:**

```java
public class JavaTransactionProcessor {
    private final ExecutorService executor;
    private final BlockingQueue<TransactionResult> results;

    public JavaTransactionProcessor(int workers) {
        this.executor = Executors.newFixedThreadPool(workers);
        this.results = new LinkedBlockingQueue<>();
    }

    public void processAsync(Transaction transaction) {
        executor.submit(() -> {
            try {
                TransactionResult result = processTransaction(transaction);
                results.put(result);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public TransactionResult getResult() throws InterruptedException {
        return results.take();
    }

    public void shutdown() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}

// Usage
public static void main(String[] args) throws InterruptedException {
    JavaTransactionProcessor processor = new JavaTransactionProcessor(10);

    // Send 1000 transactions
    for (int i = 0; i < 1000; i++) {
        Transaction tx = new Transaction("TXN-" + i, i * 100.0);
        processor.processAsync(tx);
    }

    // Collect results
    for (int i = 0; i < 1000; i++) {
        TransactionResult result = processor.getResult();
        System.out.println("Processed: " + result.getTransactionId());
    }

    processor.shutdown();
}
```

#### Performance and Scalability Analysis

**Memory Usage:**
```go
// Go - Can easily handle 100,000 goroutines
func memoryComparison() {
    var wg sync.WaitGroup

    // This uses approximately 200MB for 100,000 goroutines
    for i := 0; i < 100000; i++ {
        wg.Add(1)
        go func(id int) {
            defer wg.Done()
            time.Sleep(time.Second)
        }(i)
    }

    wg.Wait()
}
```

```java
// Java - 100,000 threads would use ~100-200GB of memory
// This is why Java uses thread pools instead
public void memoryComparison() {
    ExecutorService executor = Executors.newFixedThreadPool(1000); // Limited pool
    CountDownLatch latch = new CountDownLatch(100000);

    for (int i = 0; i < 100000; i++) {
        executor.submit(() -> {
            try {
                Thread.sleep(1000);
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
    }

    executor.shutdown();
}
```

#### When to Use Which Approach

**Use Go Goroutines When:**
- High concurrency requirements (thousands+ concurrent operations)
- I/O intensive operations
- Microservices with many external API calls
- Real-time data processing
- Simple concurrent logic
- Building from scratch

**Use Java Threads When:**
- CPU-intensive operations requiring fine-tuned thread control
- Integration with existing Java ecosystem
- Need for mature debugging tools
- Complex thread lifecycle management
- Enterprise applications with established Java infrastructure

#### Deadlock Prevention Comparison

**Go - Channel-based deadlock prevention:**
```go
// Go encourages designs that avoid deadlocks
func transferFunds(from, to *Account, amount float64) error {
    // Use select with timeout to avoid deadlocks
    timeout := time.After(5 * time.Second)

    select {
    case <-from.Lock():
        defer from.Unlock()
        select {
        case <-to.Lock():
            defer to.Unlock()
            return performTransfer(from, to, amount)
        case <-timeout:
            return errors.New("timeout acquiring destination account lock")
        }
    case <-timeout:
        return errors.New("timeout acquiring source account lock")
    }
}
```

**Java - Traditional deadlock prevention:**
```java
// Java requires careful lock ordering
public boolean transferFunds(Account from, Account to, double amount) {
    // Always acquire locks in consistent order to prevent deadlock
    Account firstLock = from.getId() < to.getId() ? from : to;
    Account secondLock = from.getId() < to.getId() ? to : from;

    synchronized(firstLock) {
        synchronized(secondLock) {
            return performTransfer(from, to, amount);
        }
    }
}

// Or using tryLock with timeout
public boolean transferFundsWithTimeout(Account from, Account to, double amount) {
    try {
        if (from.getLock().tryLock(5, TimeUnit.SECONDS)) {
            try {
                if (to.getLock().tryLock(5, TimeUnit.SECONDS)) {
                    try {
                        return performTransfer(from, to, amount);
                    } finally {
                        to.getLock().unlock();
                    }
                }
            } finally {
                from.getLock().unlock();
            }
        }
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
    return false;
}
```

#### Summary of Key Differences

1. **Philosophy**: Go promotes "Don't communicate by sharing memory; share memory by communicating" while Java traditionally uses shared memory with locks

2. **Ease of Use**: Go's goroutines are simpler to create and manage than Java's threads

3. **Performance**: Go can handle many more concurrent operations with less memory overhead

4. **Error Handling**: Go's explicit error handling vs Java's exception-based approach affects concurrent code design

5. **Debugging**: Java has more mature debugging tools, but Go's simpler model makes debugging easier

6. **Ecosystem**: Java has more mature concurrency libraries (java.util.concurrent), while Go's standard library provides most needed primitives

[⬆️ Back to Top](#table-of-contents)

### Basic Goroutines

```go
package main

import (
    "fmt"
    "math/rand"
    "sync"
    "time"
)

// TransactionProcessor simulates transaction processing
type TransactionProcessor struct {
    ID          int
    ProcessedTx int
    mu          sync.Mutex
}

func (tp *TransactionProcessor) ProcessTransaction(txID string, amount float64) {
    // Simulate processing time
    processingTime := time.Duration(rand.Intn(1000)) * time.Millisecond
    time.Sleep(processingTime)

    tp.mu.Lock()
    tp.ProcessedTx++
    fmt.Printf("Processor %d: Processed transaction %s (Amount: $%.2f) - Total processed: %d\n",
               tp.ID, txID, amount, tp.ProcessedTx)
    tp.mu.Unlock()
}

// Simple goroutine examples
func simpleGoroutineExample() {
    fmt.Println("=== Simple Goroutine Example ===")

    // Anonymous goroutine
    go func() {
        for i := 1; i <= 5; i++ {
            fmt.Printf("Goroutine: Count %d\n", i)
            time.Sleep(time.Millisecond * 500)
        }
    }()

    // Named function goroutine
    go countDown("Main")

    // Start multiple goroutines
    for i := 1; i <= 3; i++ {
        go func(id int) {
            fmt.Printf("Worker %d: Starting work\n", id)
            time.Sleep(time.Millisecond * time.Duration(rand.Intn(1000)))
            fmt.Printf("Worker %d: Finished work\n", id)
        }(i) // Pass i as parameter to avoid closure issue
    }

    // Wait for goroutines to complete
    time.Sleep(time.Second * 3)
    fmt.Println("Main function ending\n")
}

func countDown(name string) {
    for i := 3; i >= 1; i-- {
        fmt.Printf("%s: %d\n", name, i)
        time.Sleep(time.Millisecond * 300)
    }
    fmt.Printf("%s: Done!\n", name)
}

// WaitGroup example
func waitGroupExample() {
    fmt.Println("=== WaitGroup Example ===")

    var wg sync.WaitGroup

    // Create transaction processors
    processors := make([]*TransactionProcessor, 3)
    for i := 0; i < 3; i++ {
        processors[i] = &TransactionProcessor{ID: i + 1}
    }

    // Simulate processing multiple transactions concurrently
    transactions := []struct {
        ID     string
        Amount float64
    }{
        {"TXN-001", 150.75},
        {"TXN-002", 2500.00},
        {"TXN-003", 75.25},
        {"TXN-004", 1200.50},
        {"TXN-005", 300.00},
        {"TXN-006", 89.99},
    }

    for _, tx := range transactions {
        wg.Add(1) // Add to wait group

        go func(txID string, amount float64) {
            defer wg.Done() // Mark as done when function exits

            // Assign to random processor
            processor := processors[rand.Intn(len(processors))]
            processor.ProcessTransaction(txID, amount)
        }(tx.ID, tx.Amount)
    }

    wg.Wait() // Wait for all goroutines to complete

    // Print final statistics
    fmt.Println("\nFinal Processing Statistics:")
    for _, processor := range processors {
        fmt.Printf("Processor %d: %d transactions processed\n", processor.ID, processor.ProcessedTx)
    }
    fmt.Println()
}

// Worker pool pattern
func workerPoolExample() {
    fmt.Println("=== Worker Pool Example ===")

    const numWorkers = 4
    const numJobs = 15

    // Create channels
    jobs := make(chan TransactionJob, numJobs)
    results := make(chan TransactionResult, numJobs)

    // Start workers
    var wg sync.WaitGroup
    for w := 1; w <= numWorkers; w++ {
        wg.Add(1)
        go worker(w, jobs, results, &wg)
    }

    // Send jobs
    go func() {
        for j := 1; j <= numJobs; j++ {
            job := TransactionJob{
                ID:       fmt.Sprintf("JOB-%03d", j),
                Amount:   float64(rand.Intn(5000) + 100),
                Priority: rand.Intn(3) + 1, // Priority 1-3
            }
            jobs <- job
        }
        close(jobs)
    }()

    // Collect results
    go func() {
        wg.Wait()
        close(results)
    }()

    // Process results
    var totalProcessed int
    var totalAmount float64
    for result := range results {
        totalProcessed++
        totalAmount += result.Amount
        if result.Error != nil {
            fmt.Printf("Job %s failed: %v\n", result.JobID, result.Error)
        } else {
            fmt.Printf("Job %s processed by worker %d: $%.2f (%.2fs)\n",
                       result.JobID, result.WorkerID, result.Amount, result.ProcessingTime.Seconds())
        }
    }

    fmt.Printf("\nWorker Pool Summary:\n")
    fmt.Printf("Total jobs processed: %d\n", totalProcessed)
    fmt.Printf("Total amount processed: $%.2f\n", totalAmount)
    fmt.Println()
}

type TransactionJob struct {
    ID       string
    Amount   float64
    Priority int
}

type TransactionResult struct {
    JobID          string
    WorkerID       int
    Amount         float64
    ProcessingTime time.Duration
    Error          error
}

func worker(id int, jobs <-chan TransactionJob, results chan<- TransactionResult, wg *sync.WaitGroup) {
    defer wg.Done()

    for job := range jobs {
        start := time.Now()

        // Simulate processing based on priority
        var processingTime time.Duration
        switch job.Priority {
        case 1: // High priority - fast processing
            processingTime = time.Duration(rand.Intn(200)) * time.Millisecond
        case 2: // Medium priority
            processingTime = time.Duration(rand.Intn(500)) * time.Millisecond
        case 3: // Low priority - slower processing
            processingTime = time.Duration(rand.Intn(1000)) * time.Millisecond
        }

        time.Sleep(processingTime)

        result := TransactionResult{
            JobID:          job.ID,
            WorkerID:       id,
            Amount:         job.Amount,
            ProcessingTime: time.Since(start),
            Error:          nil,
        }

        // Simulate occasional errors
        if rand.Float32() < 0.1 { // 10% chance of error
            result.Error = fmt.Errorf("processing failed for amount %.2f", job.Amount)
        }

        results <- result
    }
}

// Race condition demonstration and fix
func raceConditionExample() {
    fmt.Println("=== Race Condition Example ===")

    // Without synchronization (race condition)
    fmt.Println("Without synchronization:")
    var counter1 int
    var wg1 sync.WaitGroup

    for i := 0; i < 10; i++ {
        wg1.Add(1)
        go func() {
            defer wg1.Done()
            for j := 0; j < 1000; j++ {
                counter1++ // Race condition!
            }
        }()
    }
    wg1.Wait()
    fmt.Printf("Expected: 10000, Actual: %d\n", counter1)

    // With mutex (fixed)
    fmt.Println("\nWith mutex synchronization:")
    var counter2 int
    var mu sync.Mutex
    var wg2 sync.WaitGroup

    for i := 0; i < 10; i++ {
        wg2.Add(1)
        go func() {
            defer wg2.Done()
            for j := 0; j < 1000; j++ {
                mu.Lock()
                counter2++
                mu.Unlock()
            }
        }()
    }
    wg2.Wait()
    fmt.Printf("Expected: 10000, Actual: %d\n", counter2)

    // With atomic operations
    fmt.Println("\nWith atomic operations:")
    var counter3 int64
    var wg3 sync.WaitGroup

    for i := 0; i < 10; i++ {
        wg3.Add(1)
        go func() {
            defer wg3.Done()
            for j := 0; j < 1000; j++ {
                // Note: import "sync/atomic" would be needed
                // atomic.AddInt64(&counter3, 1)
                // For demo, we'll use mutex
                mu.Lock()
                counter3++
                mu.Unlock()
            }
        }()
    }
    wg3.Wait()
    fmt.Printf("Expected: 10000, Actual: %d\n", counter3)
    fmt.Println()
}

// Concurrent bank account simulation
type BankAccount struct {
    ID      string
    Balance float64
    mu      sync.RWMutex
}

func (ba *BankAccount) Deposit(amount float64) {
    ba.mu.Lock()
    defer ba.mu.Unlock()

    ba.Balance += amount
    fmt.Printf("Account %s: Deposited $%.2f, New balance: $%.2f\n",
               ba.ID, amount, ba.Balance)
}

func (ba *BankAccount) Withdraw(amount float64) bool {
    ba.mu.Lock()
    defer ba.mu.Unlock()

    if ba.Balance >= amount {
        ba.Balance -= amount
        fmt.Printf("Account %s: Withdrew $%.2f, New balance: $%.2f\n",
                   ba.ID, amount, ba.Balance)
        return true
    }

    fmt.Printf("Account %s: Insufficient funds for withdrawal of $%.2f (Balance: $%.2f)\n",
               ba.ID, amount, ba.Balance)
    return false
}

func (ba *BankAccount) GetBalance() float64 {
    ba.mu.RLock()
    defer ba.mu.RUnlock()
    return ba.Balance
}

func concurrentBankingExample() {
    fmt.Println("=== Concurrent Banking Example ===")

    account := &BankAccount{
        ID:      "ACC-12345",
        Balance: 1000.0,
    }

    var wg sync.WaitGroup

    // Simulate concurrent deposits
    for i := 0; i < 5; i++ {
        wg.Add(1)
        go func(id int) {
            defer wg.Done()
            amount := float64(rand.Intn(200) + 50) // $50-250
            account.Deposit(amount)
        }(i)
    }

    // Simulate concurrent withdrawals
    for i := 0; i < 3; i++ {
        wg.Add(1)
        go func(id int) {
            defer wg.Done()
            amount := float64(rand.Intn(300) + 100) // $100-400
            account.Withdraw(amount)
        }(i)
    }

    // Simulate balance checks
    for i := 0; i < 2; i++ {
        wg.Add(1)
        go func(id int) {
            defer wg.Done()
            time.Sleep(time.Millisecond * time.Duration(rand.Intn(500)))
            balance := account.GetBalance()
            fmt.Printf("Balance check %d: $%.2f\n", id+1, balance)
        }(i)
    }

    wg.Wait()

    fmt.Printf("Final account balance: $%.2f\n", account.GetBalance())
    fmt.Println()
}

func main() {
    rand.Seed(time.Now().UnixNano())

    simpleGoroutineExample()
    waitGroupExample()
    workerPoolExample()
    raceConditionExample()
    concurrentBankingExample()

    fmt.Println("All goroutine examples completed!")
}
```

### Advanced Concurrency Patterns

```go
package main

import (
    "context"
    "fmt"
    "math/rand"
    "sync"
    "time"
)

// Fan-out/Fan-in pattern
func fanOutFanInExample() {
    fmt.Println("=== Fan-out/Fan-in Pattern ===")

    // Input data
    transactions := []Transaction{
        {ID: "TXN-001", Amount: 100.0, Type: "deposit"},
        {ID: "TXN-002", Amount: 250.0, Type: "deposit"},
        {ID: "TXN-003", Amount: -75.0, Type: "withdrawal"},
        {ID: "TXN-004", Amount: 500.0, Type: "deposit"},
        {ID: "TXN-005", Amount: -200.0, Type: "withdrawal"},
        {ID: "TXN-006", Amount: 1000.0, Type: "deposit"},
    }

    start := time.Now()

    // Fan-out: distribute work to multiple workers
    input := generateTransactions(transactions)

    // Start multiple workers (fan-out)
    const numWorkers = 3
    workers := make([]<-chan ProcessedTransaction, numWorkers)

    for i := 0; i < numWorkers; i++ {
        workers[i] = processTransactions(input)
    }

    // Fan-in: merge results from all workers
    results := mergeResults(workers...)

    // Collect results
    var processedCount int
    var totalAmount float64

    for result := range results {
        processedCount++
        totalAmount += result.Amount
        fmt.Printf("Processed: %s, Amount: $%.2f, Fee: $%.2f, Worker: %d\n",
                   result.ID, result.Amount, result.ProcessingFee, result.WorkerID)
    }

    duration := time.Since(start)
    fmt.Printf("Fan-out/Fan-in completed: %d transactions, Total: $%.2f in %v\n\n",
               processedCount, totalAmount, duration)
}

func generateTransactions(transactions []Transaction) <-chan Transaction {
    out := make(chan Transaction)
    go func() {
        defer close(out)
        for _, tx := range transactions {
            out <- tx
        }
    }()
    return out
}

func processTransactions(input <-chan Transaction) <-chan ProcessedTransaction {
    out := make(chan ProcessedTransaction)
    go func() {
        defer close(out)
        workerID := rand.Intn(100)

        for tx := range input {
            // Simulate processing time
            time.Sleep(time.Duration(rand.Intn(300)) * time.Millisecond)

            processed := ProcessedTransaction{
                Transaction:    tx,
                ProcessingFee:  calculateFee(tx.Amount),
                ProcessedAt:    time.Now(),
                WorkerID:      workerID,
            }

            out <- processed
        }
    }()
    return out
}

func mergeResults(channels ...<-chan ProcessedTransaction) <-chan ProcessedTransaction {
    out := make(chan ProcessedTransaction)
    var wg sync.WaitGroup

    // Start a goroutine for each input channel
    wg.Add(len(channels))
    for _, ch := range channels {
        go func(c <-chan ProcessedTransaction) {
            defer wg.Done()
            for result := range c {
                out <- result
            }
        }(ch)
    }

    // Close output when all input channels are closed
    go func() {
        wg.Wait()
        close(out)
    }()

    return out
}

// Pipeline pattern
func pipelineExample() {
    fmt.Println("=== Pipeline Pattern ===")

    start := time.Now()

    // Stage 1: Generate transaction requests
    requests := generateRequests(10)

    // Stage 2: Validate transactions
    validated := validateTransactions(requests)

    // Stage 3: Apply business rules
    processed := applyBusinessRules(validated)

    // Stage 4: Calculate fees
    withFees := calculateFees(processed)

    // Stage 5: Final processing
    results := finalProcessing(withFees)

    // Consume results
    var count int
    for result := range results {
        count++
        fmt.Printf("Pipeline result %d: %s - $%.2f (Fee: $%.2f)\n",
                   count, result.ID, result.Amount, result.ProcessingFee)
    }

    duration := time.Since(start)
    fmt.Printf("Pipeline completed: %d transactions in %v\n\n", count, duration)
}

func generateRequests(count int) <-chan TransactionRequest {
    out := make(chan TransactionRequest)
    go func() {
        defer close(out)
        for i := 1; i <= count; i++ {
            request := TransactionRequest{
                ID:        fmt.Sprintf("REQ-%03d", i),
                Amount:    float64(rand.Intn(1000) + 100),
                AccountID: fmt.Sprintf("ACC-%03d", rand.Intn(5)+1),
                Type:      []string{"deposit", "withdrawal", "transfer"}[rand.Intn(3)],
            }
            out <- request
            time.Sleep(time.Millisecond * 50) // Simulate request arrival rate
        }
    }()
    return out
}

func validateTransactions(input <-chan TransactionRequest) <-chan TransactionRequest {
    out := make(chan TransactionRequest)
    go func() {
        defer close(out)
        for req := range input {
            // Simulate validation logic
            time.Sleep(time.Millisecond * 20)

            // Only pass through valid transactions
            if req.Amount > 0 && req.AccountID != "" {
                out <- req
            } else {
                fmt.Printf("Validation failed for request %s\n", req.ID)
            }
        }
    }()
    return out
}

func applyBusinessRules(input <-chan TransactionRequest) <-chan TransactionRequest {
    out := make(chan TransactionRequest)
    go func() {
        defer close(out)
        for req := range input {
            // Simulate business rules processing
            time.Sleep(time.Millisecond * 30)

            // Apply daily limit check (example rule)
            if req.Amount <= 10000 {
                out <- req
            } else {
                fmt.Printf("Business rule violation for request %s (amount too high)\n", req.ID)
            }
        }
    }()
    return out
}

func calculateFees(input <-chan TransactionRequest) <-chan ProcessedTransaction {
    out := make(chan ProcessedTransaction)
    go func() {
        defer close(out)
        for req := range input {
            // Simulate fee calculation
            time.Sleep(time.Millisecond * 15)

            processed := ProcessedTransaction{
                Transaction: Transaction{
                    ID:     req.ID,
                    Amount: req.Amount,
                    Type:   req.Type,
                },
                ProcessingFee: calculateFee(req.Amount),
                ProcessedAt:   time.Now(),
                WorkerID:     0,
            }
            out <- processed
        }
    }()
    return out
}

func finalProcessing(input <-chan ProcessedTransaction) <-chan ProcessedTransaction {
    out := make(chan ProcessedTransaction)
    go func() {
        defer close(out)
        for tx := range input {
            // Simulate final processing
            time.Sleep(time.Millisecond * 25)
            out <- tx
        }
    }()
    return out
}

// Context cancellation example
func contextCancellationExample() {
    fmt.Println("=== Context Cancellation Example ===")

    // Create a context with timeout
    ctx, cancel := context.WithTimeout(context.Background(), 2*time.Second)
    defer cancel()

    // Start a long-running operation
    results := make(chan string, 1)

    go func() {
        select {
        case <-time.After(3 * time.Second): // This would normally take 3 seconds
            results <- "Operation completed successfully"
        case <-ctx.Done():
            results <- fmt.Sprintf("Operation cancelled: %v", ctx.Err())
        }
    }()

    // Wait for either completion or timeout
    result := <-results
    fmt.Printf("Result: %s\n", result)

    // Example with manual cancellation
    fmt.Println("\nManual cancellation example:")
    ctx2, cancel2 := context.WithCancel(context.Background())

    go func() {
        time.Sleep(500 * time.Millisecond)
        fmt.Println("Cancelling operation...")
        cancel2()
    }()

    go func() {
        select {
        case <-time.After(2 * time.Second):
            results <- "Long operation completed"
        case <-ctx2.Done():
            results <- fmt.Sprintf("Long operation cancelled: %v", ctx2.Err())
        }
    }()

    result2 := <-results
    fmt.Printf("Result: %s\n\n", result2)
}

// Rate limiting example
func rateLimitingExample() {
    fmt.Println("=== Rate Limiting Example ===")

    // Create a rate limiter (5 operations per second)
    limiter := time.NewTicker(200 * time.Millisecond)
    defer limiter.Stop()

    requests := make(chan int, 10)

    // Generate requests
    go func() {
        for i := 1; i <= 10; i++ {
            requests <- i
        }
        close(requests)
    }()

    // Process requests with rate limiting
    start := time.Now()
    for req := range requests {
        <-limiter.C // Wait for rate limiter
        fmt.Printf("Processing request %d at %v\n", req, time.Since(start).Round(time.Millisecond))
    }

    fmt.Printf("Rate limiting completed in %v\n\n", time.Since(start).Round(time.Millisecond))
}

// Worker pool with graceful shutdown
func gracefulShutdownExample() {
    fmt.Println("=== Graceful Shutdown Example ===")

    const numWorkers = 3
    jobs := make(chan Job, 20)
    results := make(chan Result, 20)

    // Context for graceful shutdown
    ctx, cancel := context.WithCancel(context.Background())
    defer cancel()

    var wg sync.WaitGroup

    // Start workers
    for i := 1; i <= numWorkers; i++ {
        wg.Add(1)
        go gracefulWorker(ctx, i, jobs, results, &wg)
    }

    // Send jobs
    go func() {
        defer close(jobs)
        for i := 1; i <= 15; i++ {
            select {
            case jobs <- Job{ID: i, Data: fmt.Sprintf("Job-%d", i)}:
            case <-ctx.Done():
                fmt.Println("Job generation cancelled")
                return
            }
        }
    }()

    // Simulate shutdown signal after 1 second
    go func() {
        time.Sleep(1 * time.Second)
        fmt.Println("Shutdown signal received, stopping workers...")
        cancel()
    }()

    // Collect results
    go func() {
        wg.Wait()
        close(results)
    }()

    // Process results
    var completed int
    for result := range results {
        completed++
        fmt.Printf("Completed: %s by worker %d\n", result.JobID, result.WorkerID)
    }

    fmt.Printf("Graceful shutdown completed: %d jobs processed\n\n", completed)
}

func gracefulWorker(ctx context.Context, id int, jobs <-chan Job, results chan<- Result, wg *sync.WaitGroup) {
    defer wg.Done()

    for {
        select {
        case job, ok := <-jobs:
            if !ok {
                fmt.Printf("Worker %d: No more jobs, shutting down\n", id)
                return
            }

            // Simulate work
            select {
            case <-time.After(time.Duration(rand.Intn(800)) * time.Millisecond):
                results <- Result{JobID: job.Data, WorkerID: id}
            case <-ctx.Done():
                fmt.Printf("Worker %d: Context cancelled, shutting down\n", id)
                return
            }

        case <-ctx.Done():
            fmt.Printf("Worker %d: Context cancelled, shutting down\n", id)
            return
        }
    }
}

// Supporting types
type Transaction struct {
    ID     string
    Amount float64
    Type   string
}

type ProcessedTransaction struct {
    Transaction
    ProcessingFee float64
    ProcessedAt   time.Time
    WorkerID      int
}

type TransactionRequest struct {
    ID        string
    Amount    float64
    AccountID string
    Type      string
}

type Job struct {
    ID   int
    Data string
}

type Result struct {
    JobID    string
    WorkerID int
}

func calculateFee(amount float64) float64 {
    if amount < 0 {
        amount = -amount // For withdrawals
    }

    if amount >= 1000 {
        return 5.0
    } else if amount >= 100 {
        return 2.5
    }
    return 1.0
}

func main() {
    rand.Seed(time.Now().UnixNano())

    fanOutFanInExample()
    pipelineExample()
    contextCancellationExample()
    rateLimitingExample()
    gracefulShutdownExample()

    fmt.Println("All advanced concurrency examples completed!")
}
```

[⬆️ Back to Top](#table-of-contents)

## Channels

### Channel Basics and Patterns

```go
package main

import (
    "fmt"
    "math/rand"
    "sync"
    "time"
)

// Basic channel operations
func basicChannelExample() {
    fmt.Println("=== Basic Channel Example ===")

    // Unbuffered channel
    ch := make(chan string)

    // Send in goroutine (non-blocking for main)
    go func() {
        time.Sleep(time.Second)
        ch <- "Hello from goroutine!"
    }()

    // Receive blocks until value is sent
    message := <-ch
    fmt.Printf("Received: %s\n", message)

    // Buffered channel
    bufferedCh := make(chan int, 3)

    // Can send up to buffer size without blocking
    bufferedCh <- 1
    bufferedCh <- 2
    bufferedCh <- 3

    // Receive values
    for i := 0; i < 3; i++ {
        value := <-bufferedCh
        fmt.Printf("Buffered channel value: %d\n", value)
    }

    fmt.Println()
}

// Select statement for non-blocking operations
func selectStatementExample() {
    fmt.Println("=== Select Statement Example ===")

    ch1 := make(chan string, 1)
    ch2 := make(chan string, 1)

    // Send to channels in random order
    go func() {
        time.Sleep(time.Duration(rand.Intn(1000)) * time.Millisecond)
        ch1 <- "from ch1"
    }()

    go func() {
        time.Sleep(time.Duration(rand.Intn(1000)) * time.Millisecond)
        ch2 <- "from ch2"
    }()

    // Select waits for first available channel
    for i := 0; i < 2; i++ {
        select {
        case msg1 := <-ch1:
            fmt.Printf("Received %s\n", msg1)
        case msg2 := <-ch2:
            fmt.Printf("Received %s\n", msg2)
        case <-time.After(2 * time.Second):
            fmt.Println("Timeout!")
        }
    }

    // Non-blocking select with default
    select {
    case ch1 <- "trying to send":
        fmt.Println("Sent to ch1")
    default:
        fmt.Println("Could not send to ch1")
    }

    fmt.Println()
}

// Channel direction restrictions
func channelDirectionExample() {
    fmt.Println("=== Channel Direction Example ===")

    // Bidirectional channel
    ch := make(chan int, 2)

    // Function that only receives
    go func(recv <-chan int) {
        for value := range recv {
            fmt.Printf("Received: %d\n", value)
        }
    }(ch)

    // Function that only sends
    go func(send chan<- int) {
        for i := 1; i <= 3; i++ {
            send <- i
        }
        close(send)
    }(ch)

    time.Sleep(time.Second)
    fmt.Println()
}

// Producer-Consumer pattern with channels
func producerConsumerExample() {
    fmt.Println("=== Producer-Consumer Example ===")

    const bufferSize = 5
    const numProducers = 2
    const numConsumers = 3
    const itemsPerProducer = 10

    // Channel for work items
    workCh := make(chan WorkItem, bufferSize)

    // Channel for results
    resultCh := make(chan WorkResult, bufferSize)

    var wg sync.WaitGroup

    // Start producers
    for p := 1; p <= numProducers; p++ {
        wg.Add(1)
        go producer(p, itemsPerProducer, workCh, &wg)
    }

    // Start consumers
    for c := 1; c <= numConsumers; c++ {
        wg.Add(1)
        go consumer(c, workCh, resultCh, &wg)
    }

    // Close workCh when all producers are done
    go func() {
        wg.Wait()
        close(workCh)
    }()

    // Start result collector
    var collectorWg sync.WaitGroup
    collectorWg.Add(1)
    go resultCollector(resultCh, &collectorWg)

    // Wait for all workers to finish
    wg.Wait()
    close(resultCh)
    collectorWg.Wait()

    fmt.Println()
}

type WorkItem struct {
    ID         int
    ProducerID int
    Data       string
    Priority   int
}

type WorkResult struct {
    ItemID     int
    ConsumerID int
    Result     string
    ProcessTime time.Duration
}

func producer(id int, itemCount int, workCh chan<- WorkItem, wg *sync.WaitGroup) {
    defer wg.Done()

    for i := 1; i <= itemCount; i++ {
        item := WorkItem{
            ID:         i,
            ProducerID: id,
            Data:       fmt.Sprintf("Item-%d-%d", id, i),
            Priority:   rand.Intn(3) + 1,
        }

        workCh <- item
        fmt.Printf("Producer %d: Created %s\n", id, item.Data)

        // Simulate production time
        time.Sleep(time.Duration(rand.Intn(200)) * time.Millisecond)
    }

    fmt.Printf("Producer %d: Finished\n", id)
}

func consumer(id int, workCh <-chan WorkItem, resultCh chan<- WorkResult, wg *sync.WaitGroup) {
    defer wg.Done()

    for item := range workCh {
        start := time.Now()

        // Simulate processing time based on priority
        processingTime := time.Duration(item.Priority*100) * time.Millisecond
        time.Sleep(processingTime)

        result := WorkResult{
            ItemID:      item.ID,
            ConsumerID:  id,
            Result:      fmt.Sprintf("Processed-%s", item.Data),
            ProcessTime: time.Since(start),
        }

        resultCh <- result
        fmt.Printf("Consumer %d: Processed %s (took %v)\n",
                   id, item.Data, result.ProcessTime)
    }

    fmt.Printf("Consumer %d: Finished\n", id)
}

func resultCollector(resultCh <-chan WorkResult, wg *sync.WaitGroup) {
    defer wg.Done()

    var totalItems int
    var totalTime time.Duration
    consumerStats := make(map[int]int)

    for result := range resultCh {
        totalItems++
        totalTime += result.ProcessTime
        consumerStats[result.ConsumerID]++
    }

    fmt.Printf("\nResult Summary:\n")
    fmt.Printf("Total items processed: %d\n", totalItems)
    fmt.Printf("Average processing time: %v\n", totalTime/time.Duration(totalItems))

    fmt.Printf("Consumer statistics:\n")
    for consumerID, count := range consumerStats {
        fmt.Printf("  Consumer %d: %d items\n", consumerID, count)
    }
}

// Banking transaction pipeline using channels
func bankingTransactionPipeline() {
    fmt.Println("=== Banking Transaction Pipeline ===")

    // Create pipeline stages
    requests := make(chan TransactionRequest, 10)
    validated := make(chan TransactionRequest, 10)
    authorized := make(chan TransactionRequest, 10)
    processed := make(chan TransactionResult, 10)

    var wg sync.WaitGroup

    // Stage 1: Request validation
    wg.Add(1)
    go validateRequests(requests, validated, &wg)

    // Stage 2: Authorization
    wg.Add(1)
    go authorizeTransactions(validated, authorized, &wg)

    // Stage 3: Processing
    wg.Add(1)
    go processTransactions(authorized, processed, &wg)

    // Stage 4: Result handling
    wg.Add(1)
    go handleResults(processed, &wg)

    // Generate sample requests
    sampleRequests := []TransactionRequest{
        {ID: "REQ-001", AccountID: "ACC-001", Amount: 150.00, Type: "withdrawal"},
        {ID: "REQ-002", AccountID: "ACC-002", Amount: 500.00, Type: "deposit"},
        {ID: "REQ-003", AccountID: "ACC-001", Amount: 2000.00, Type: "withdrawal"}, // Should fail authorization
        {ID: "REQ-004", AccountID: "ACC-003", Amount: 75.50, Type: "transfer"},
        {ID: "REQ-005", AccountID: "", Amount: 100.00, Type: "deposit"}, // Should fail validation
    }

    // Send requests
    go func() {
        defer close(requests)
        for _, req := range sampleRequests {
            requests <- req
            time.Sleep(time.Millisecond * 100)
        }
    }()

    // Wait for pipeline to complete
    wg.Wait()
    fmt.Println()
}

type TransactionRequest struct {
    ID        string
    AccountID string
    Amount    float64
    Type      string
}

type TransactionResult struct {
    RequestID string
    Status    string
    Message   string
    ProcessedAt time.Time
}

func validateRequests(input <-chan TransactionRequest, output chan<- TransactionRequest, wg *sync.WaitGroup) {
    defer wg.Done()
    defer close(output)

    for req := range input {
        fmt.Printf("Validating: %s\n", req.ID)

        // Basic validation
        if req.AccountID == "" || req.Amount <= 0 {
            fmt.Printf("Validation failed for %s: invalid data\n", req.ID)
            continue
        }

        time.Sleep(time.Millisecond * 50) // Simulate validation time
        output <- req
        fmt.Printf("Validation passed for %s\n", req.ID)
    }
}

func authorizeTransactions(input <-chan TransactionRequest, output chan<- TransactionRequest, wg *sync.WaitGroup) {
    defer wg.Done()
    defer close(output)

    // Mock account balances
    accountBalances := map[string]float64{
        "ACC-001": 1000.00,
        "ACC-002": 500.00,
        "ACC-003": 2500.00,
    }

    for req := range input {
        fmt.Printf("Authorizing: %s\n", req.ID)

        balance, exists := accountBalances[req.AccountID]
        if !exists {
            fmt.Printf("Authorization failed for %s: account not found\n", req.ID)
            continue
        }

        if req.Type == "withdrawal" && balance < req.Amount {
            fmt.Printf("Authorization failed for %s: insufficient funds\n", req.ID)
            continue
        }

        time.Sleep(time.Millisecond * 75) // Simulate authorization time
        output <- req
        fmt.Printf("Authorization passed for %s\n", req.ID)
    }
}

func processTransactions(input <-chan TransactionRequest, output chan<- TransactionResult, wg *sync.WaitGroup) {
    defer wg.Done()
    defer close(output)

    for req := range input {
        fmt.Printf("Processing: %s\n", req.ID)

        // Simulate processing time
        time.Sleep(time.Duration(rand.Intn(200)) * time.Millisecond)

        result := TransactionResult{
            RequestID:   req.ID,
            Status:      "completed",
            Message:     fmt.Sprintf("Transaction %s processed successfully", req.ID),
            ProcessedAt: time.Now(),
        }

        output <- result
        fmt.Printf("Processing completed for %s\n", req.ID)
    }
}

func handleResults(input <-chan TransactionResult, wg *sync.WaitGroup) {
    defer wg.Done()

    var successCount int

    for result := range input {
        successCount++
        fmt.Printf("Result: %s - %s (%s)\n",
                   result.RequestID, result.Status,
                   result.ProcessedAt.Format("15:04:05.000"))
    }

    fmt.Printf("Pipeline completed: %d transactions processed successfully\n", successCount)
}

// Timeout and cancellation with channels
func timeoutCancellationExample() {
    fmt.Println("=== Timeout and Cancellation Example ===")

    // Example 1: Timeout
    result := make(chan string, 1)

    go func() {
        time.Sleep(2 * time.Second) // Simulate long operation
        result <- "Operation completed"
    }()

    select {
    case res := <-result:
        fmt.Printf("Success: %s\n", res)
    case <-time.After(1 * time.Second):
        fmt.Println("Operation timed out")
    }

    // Example 2: Cancellation
    stop := make(chan bool, 1)
    done := make(chan string, 1)

    go func() {
        for i := 0; i < 10; i++ {
            select {
            case <-stop:
                done <- "Operation cancelled"
                return
            default:
                fmt.Printf("Working... step %d\n", i+1)
                time.Sleep(time.Millisecond * 200)
            }
        }
        done <- "Operation completed normally"
    }()

    // Cancel after 1 second
    go func() {
        time.Sleep(time.Second)
        stop <- true
    }()

    result2 := <-done
    fmt.Printf("Result: %s\n", result2)

    fmt.Println()
}

func main() {
    rand.Seed(time.Now().UnixNano())

    basicChannelExample()
    selectStatementExample()
    channelDirectionExample()
    producerConsumerExample()
    bankingTransactionPipeline()
    timeoutCancellationExample()

    fmt.Println("All channel examples completed!")
}
```

[⬆️ Back to Top](#table-of-contents)

## Error Handling

### Error Handling Patterns

```go
package main

import (
    "errors"
    "fmt"
    "log"
    "strconv"
    "time"
)

// Custom error types
type BankingError struct {
    Code      string
    Message   string
    Timestamp time.Time
    Details   map[string]interface{}
}

func (e BankingError) Error() string {
    return fmt.Sprintf("[%s] %s - %s", e.Code, e.Message, e.Timestamp.Format("2006-01-02 15:04:05"))
}

// Specific banking errors
type InsufficientFundsError struct {
    AccountID       string
    RequestedAmount float64
    AvailableAmount float64
}

func (e InsufficientFundsError) Error() string {
    return fmt.Sprintf("insufficient funds in account %s: requested %.2f, available %.2f",
                       e.AccountID, e.RequestedAmount, e.AvailableAmount)
}

type AccountNotFoundError struct {
    AccountID string
}

func (e AccountNotFoundError) Error() string {
    return fmt.Sprintf("account not found: %s", e.AccountID)
}

type ValidationError struct {
    Field   string
    Value   interface{}
    Message string
}

func (e ValidationError) Error() string {
    return fmt.Sprintf("validation error for field '%s' with value '%v': %s", e.Field, e.Value, e.Message)
}

// Basic error handling patterns
func basicErrorHandling() {
    fmt.Println("=== Basic Error Handling ===")

    // Simple error creation and handling
    result, err := divide(10, 0)
    if err != nil {
        fmt.Printf("Error: %v\n", err)
    } else {
        fmt.Printf("Result: %.2f\n", result)
    }

    // Multiple error types
    account := &Account{ID: "ACC-001", Balance: 500.0}

    err = processWithdrawal(account, 1000.0)
    if err != nil {
        // Type assertion to handle different error types
        switch e := err.(type) {
        case InsufficientFundsError:
            fmt.Printf("Insufficient funds error: %v\n", e)
            fmt.Printf("Shortfall: %.2f\n", e.RequestedAmount-e.AvailableAmount)
        case ValidationError:
            fmt.Printf("Validation error: %v\n", e)
        default:
            fmt.Printf("Unknown error: %v\n", e)
        }
    }

    // Error wrapping
    err = complexOperation("invalid-id")
    if err != nil {
        fmt.Printf("Wrapped error: %v\n", err)
    }

    fmt.Println()
}

func divide(a, b float64) (float64, error) {
    if b == 0 {
        return 0, errors.New("division by zero")
    }
    return a / b, nil
}

func processWithdrawal(account *Account, amount float64) error {
    if amount <= 0 {
        return ValidationError{
            Field:   "amount",
            Value:   amount,
            Message: "amount must be positive",
        }
    }

    if account.Balance < amount {
        return InsufficientFundsError{
            AccountID:       account.ID,
            RequestedAmount: amount,
            AvailableAmount: account.Balance,
        }
    }

    account.Balance -= amount
    return nil
}

func complexOperation(id string) error {
    if err := validateID(id); err != nil {
        return fmt.Errorf("complex operation failed: %w", err)
    }
    return nil
}

func validateID(id string) error {
    if id == "" {
        return errors.New("ID cannot be empty")
    }
    if id == "invalid-id" {
        return errors.New("invalid ID format")
    }
    return nil
}

// Error handling with multiple return values
func multipleReturnValues() {
    fmt.Println("=== Multiple Return Values ===")

    // Function with multiple return values including error
    account, transaction, err := processTransfer("ACC-001", "ACC-002", 250.0)
    if err != nil {
        fmt.Printf("Transfer failed: %v\n", err)
        return
    }

    fmt.Printf("Transfer successful:\n")
    fmt.Printf("  From Account: %s (New Balance: %.2f)\n", account.ID, account.Balance)
    fmt.Printf("  Transaction: %s (Amount: %.2f)\n", transaction.ID, transaction.Amount)

    // Named return values with defer
    result, err := calculateCompoundInterest(1000, 0.05, 10)
    if err != nil {
        fmt.Printf("Interest calculation failed: %v\n", err)
    } else {
        fmt.Printf("Compound interest result: %.2f\n", result)
    }

    fmt.Println()
}

func processTransfer(fromID, toID string, amount float64) (*Account, *Transaction, error) {
    // Mock account data
    accounts := map[string]*Account{
        "ACC-001": {ID: "ACC-001", Balance: 1000.0},
        "ACC-002": {ID: "ACC-002", Balance: 500.0},
    }

    fromAccount, exists := accounts[fromID]
    if !exists {
        return nil, nil, AccountNotFoundError{AccountID: fromID}
    }

    _, exists = accounts[toID]
    if !exists {
        return nil, nil, AccountNotFoundError{AccountID: toID}
    }

    if fromAccount.Balance < amount {
        return nil, nil, InsufficientFundsError{
            AccountID:       fromID,
            RequestedAmount: amount,
            AvailableAmount: fromAccount.Balance,
        }
    }

    fromAccount.Balance -= amount

    transaction := &Transaction{
        ID:     "TXN-" + strconv.FormatInt(time.Now().UnixNano(), 10),
        Amount: amount,
        Type:   "transfer",
    }

    return fromAccount, transaction, nil
}

func calculateCompoundInterest(principal float64, rate float64, years int) (finalAmount float64, err error) {
    // Named return values - can be modified in defer
    defer func() {
        if r := recover(); r != nil {
            err = fmt.Errorf("calculation panic: %v", r)
        }
    }()

    if principal <= 0 {
        err = ValidationError{Field: "principal", Value: principal, Message: "must be positive"}
        return
    }

    if rate < 0 {
        err = ValidationError{Field: "rate", Value: rate, Message: "cannot be negative"}
        return
    }

    if years < 0 {
        err = ValidationError{Field: "years", Value: years, Message: "cannot be negative"}
        return
    }

    finalAmount = principal
    for i := 0; i < years; i++ {
        finalAmount *= (1 + rate)
    }

    return
}

// Panic and recover
func panicRecoverExample() {
    fmt.Println("=== Panic and Recover ===")

    // Example 1: Recover from panic in same function
    func() {
        defer func() {
            if r := recover(); r != nil {
                fmt.Printf("Recovered from panic: %v\n", r)
            }
        }()

        // This will panic
        riskyOperation(0)
        fmt.Println("This line won't be reached")
    }()

    // Example 2: Function that handles panics
    result := safeOperation([]int{1, 2, 3}, 5)
    fmt.Printf("Safe operation result: %v\n", result)

    result = safeOperation([]int{1, 2, 3}, 1)
    fmt.Printf("Safe operation result: %v\n", result)

    // Example 3: Banking operation with panic handling
    err := safeBankingOperation("ACC-001", 1000000000000) // Very large amount
    if err != nil {
        fmt.Printf("Banking operation error: %v\n", err)
    }

    fmt.Println()
}

func riskyOperation(divisor int) int {
    if divisor == 0 {
        panic("cannot divide by zero")
    }
    return 100 / divisor
}

func safeOperation(slice []int, index int) (result int) {
    defer func() {
        if r := recover(); r != nil {
            fmt.Printf("Recovered from index out of bounds: %v\n", r)
            result = -1
        }
    }()

    return slice[index] // May panic if index is out of bounds
}

func safeBankingOperation(accountID string, amount float64) (err error) {
    defer func() {
        if r := recover(); r != nil {
            err = fmt.Errorf("banking operation panic: %v", r)
        }
    }()

    // Simulate operation that might panic with very large numbers
    if amount > 1000000000 {
        panic("amount too large for system to handle")
    }

    fmt.Printf("Processing transaction for account %s: $%.2f\n", accountID, amount)
    return nil
}

// Error aggregation and handling multiple errors
func multipleErrorHandling() {
    fmt.Println("=== Multiple Error Handling ===")

    // Collect multiple errors
    var errors []error

    // Validate multiple fields
    if err := validateAmount(-100); err != nil {
        errors = append(errors, err)
    }

    if err := validateAccountID(""); err != nil {
        errors = append(errors, err)
    }

    if err := validateCurrency("XYZ"); err != nil {
        errors = append(errors, err)
    }

    if len(errors) > 0 {
        fmt.Printf("Validation failed with %d errors:\n", len(errors))
        for i, err := range errors {
            fmt.Printf("  %d. %v\n", i+1, err)
        }
    }

    // Batch processing with error collection
    results := processBatchTransactions([]TransferRequest{
        {FromID: "ACC-001", ToID: "ACC-002", Amount: 100},
        {FromID: "ACC-999", ToID: "ACC-002", Amount: 200}, // Invalid account
        {FromID: "ACC-001", ToID: "ACC-002", Amount: -50},  // Invalid amount
        {FromID: "ACC-001", ToID: "ACC-002", Amount: 300},
    })

    fmt.Printf("\nBatch processing results:\n")
    for i, result := range results {
        if result.Error != nil {
            fmt.Printf("  Transaction %d failed: %v\n", i+1, result.Error)
        } else {
            fmt.Printf("  Transaction %d succeeded: %s\n", i+1, result.TransactionID)
        }
    }

    fmt.Println()
}

func validateAmount(amount float64) error {
    if amount <= 0 {
        return ValidationError{Field: "amount", Value: amount, Message: "must be positive"}
    }
    return nil
}

func validateAccountID(accountID string) error {
    if accountID == "" {
        return ValidationError{Field: "accountID", Value: accountID, Message: "cannot be empty"}
    }
    return nil
}

func validateCurrency(currency string) error {
    validCurrencies := []string{"USD", "EUR", "GBP", "JPY"}
    for _, valid := range validCurrencies {
        if currency == valid {
            return nil
        }
    }
    return ValidationError{Field: "currency", Value: currency, Message: "unsupported currency"}
}

type TransferRequest struct {
    FromID string
    ToID   string
    Amount float64
}

type TransferResult struct {
    TransactionID string
    Error         error
}

func processBatchTransactions(requests []TransferRequest) []TransferResult {
    results := make([]TransferResult, len(requests))

    for i, req := range requests {
        if err := validateAmount(req.Amount); err != nil {
            results[i] = TransferResult{Error: err}
            continue
        }

        if err := validateAccountID(req.FromID); err != nil {
            results[i] = TransferResult{Error: err}
            continue
        }

        if err := validateAccountID(req.ToID); err != nil {
            results[i] = TransferResult{Error: err}
            continue
        }

        // Simulate processing
        if req.FromID == "ACC-999" {
            results[i] = TransferResult{Error: AccountNotFoundError{AccountID: req.FromID}}
            continue
        }

        // Success case
        results[i] = TransferResult{
            TransactionID: fmt.Sprintf("TXN-%d", time.Now().UnixNano()),
            Error:         nil,
        }
    }

    return results
}

// Logging errors
func errorLogging() {
    fmt.Println("=== Error Logging ===")

    // Setup logger (in real application, this would be configured properly)
    logger := log.New(log.Writer(), "BANKING: ", log.LstdFlags|log.Lshortfile)

    // Different types of logging
    account := &Account{ID: "ACC-001", Balance: 100.0}

    err := processTransaction(account, "withdrawal", 500.0, logger)
    if err != nil {
        // Log error with context
        logger.Printf("Transaction failed for account %s: %v", account.ID, err)
    }

    // Structured logging simulation
    logStructuredError("TXN-001", "ACC-001", "withdrawal", 500.0, err)

    fmt.Println()
}

func processTransaction(account *Account, txType string, amount float64, logger *log.Logger) error {
    logger.Printf("Processing %s of %.2f for account %s", txType, amount, account.ID)

    if txType == "withdrawal" && account.Balance < amount {
        err := InsufficientFundsError{
            AccountID:       account.ID,
            RequestedAmount: amount,
            AvailableAmount: account.Balance,
        }
        logger.Printf("Transaction validation failed: %v", err)
        return err
    }

    // Simulate successful processing
    logger.Printf("Transaction completed successfully")
    return nil
}

func logStructuredError(txID, accountID, txType string, amount float64, err error) {
    if err != nil {
        // In a real application, you'd use a structured logging library
        fmt.Printf("ERROR: {\"transaction_id\":\"%s\", \"account_id\":\"%s\", \"type\":\"%s\", \"amount\":%.2f, \"error\":\"%v\", \"timestamp\":\"%s\"}\n",
                   txID, accountID, txType, amount, err, time.Now().Format(time.RFC3339))
    }
}

// Supporting types
type Account struct {
    ID      string
    Balance float64
}

type Transaction struct {
    ID     string
    Amount float64
    Type   string
}

func main() {
    basicErrorHandling()
    multipleReturnValues()
    panicRecoverExample()
    multipleErrorHandling()
    errorLogging()

    fmt.Println("All error handling examples completed!")
}
```

[⬆️ Back to Top](#table-of-contents)

## Memory Management

### Garbage Collection and Memory Optimization

```go
package main

import (
    "fmt"
    "runtime"
    "time"
)

// Memory allocation patterns
func memoryAllocationExample() {
    fmt.Println("=== Memory Allocation Example ===")

    // Print initial memory stats
    printMemStats("Initial")

    // Allocate large slice
    largeSlice := make([]byte, 100*1024*1024) // 100MB
    printMemStats("After large allocation")

    // Use the slice to prevent optimization
    largeSlice[0] = 1
    largeSlice[len(largeSlice)-1] = 1

    // Create many small allocations
    var smallSlices [][]byte
    for i := 0; i < 1000; i++ {
        smallSlices = append(smallSlices, make([]byte, 1024))
    }
    printMemStats("After many small allocations")

    // Force garbage collection
    runtime.GC()
    runtime.GC() // Call twice to ensure cleanup
    printMemStats("After forced GC")

    // Clear references
    largeSlice = nil
    smallSlices = nil

    runtime.GC()
    runtime.GC()
    printMemStats("After clearing references and GC")

    fmt.Println()
}

func printMemStats(label string) {
    var m runtime.MemStats
    runtime.ReadMemStats(&m)

    fmt.Printf("%s:\n", label)
    fmt.Printf("  Alloc: %d KB", m.Alloc/1024)
    fmt.Printf("  TotalAlloc: %d KB", m.TotalAlloc/1024)
    fmt.Printf("  Sys: %d KB", m.Sys/1024)
    fmt.Printf("  NumGC: %d\n", m.NumGC)
}

// Memory pools for object reuse
type ConnectionPool struct {
    pool chan *Connection
}

type Connection struct {
    ID       int
    IsActive bool
    LastUsed time.Time
}

func NewConnectionPool(size int) *ConnectionPool {
    return &ConnectionPool{
        pool: make(chan *Connection, size),
    }
}

func (cp *ConnectionPool) Get() *Connection {
    select {
    case conn := <-cp.pool:
        conn.IsActive = true
        conn.LastUsed = time.Now()
        return conn
    default:
        // Create new connection if pool is empty
        return &Connection{
            ID:       int(time.Now().UnixNano()),
            IsActive: true,
            LastUsed: time.Now(),
        }
    }
}

func (cp *ConnectionPool) Put(conn *Connection) {
    if conn == nil {
        return
    }

    // Reset connection state
    conn.IsActive = false

    select {
    case cp.pool <- conn:
        // Successfully returned to pool
    default:
        // Pool is full, let GC handle it
    }
}

func poolExample() {
    fmt.Println("=== Connection Pool Example ===")

    pool := NewConnectionPool(5)

    // Use connections
    var connections []*Connection

    for i := 0; i < 10; i++ {
        conn := pool.Get()
        fmt.Printf("Got connection %d\n", conn.ID)
        connections = append(connections, conn)
    }

    // Return connections to pool
    for _, conn := range connections {
        pool.Put(conn)
        fmt.Printf("Returned connection %d to pool\n", conn.ID)
    }

    // Reuse connections
    for i := 0; i < 3; i++ {
        conn := pool.Get()
        fmt.Printf("Reused connection %d\n", conn.ID)
        pool.Put(conn)
    }

    fmt.Println()
}

// Memory-efficient string operations
func stringOptimization() {
    fmt.Println("=== String Optimization Example ===")

    // Inefficient string concatenation
    start := time.Now()
    var result string
    for i := 0; i < 1000; i++ {
        result += fmt.Sprintf("item-%d,", i) // Creates new string each time
    }
    inefficientTime := time.Since(start)

    // Efficient string building
    start = time.Now()
    var builder strings.Builder
    builder.Grow(10000) // Pre-allocate capacity
    for i := 0; i < 1000; i++ {
        builder.WriteString(fmt.Sprintf("item-%d,", i))
    }
    result2 := builder.String()
    efficientTime := time.Since(start)

    fmt.Printf("Inefficient concatenation: %v\n", inefficientTime)
    fmt.Printf("Efficient building: %v\n", efficientTime)
    fmt.Printf("Speedup: %.2fx\n", float64(inefficientTime)/float64(efficientTime))
    fmt.Printf("Results equal: %v\n", len(result) == len(result2))

    fmt.Println()
}

// Slice memory management
func sliceMemoryManagement() {
    fmt.Println("=== Slice Memory Management ===")

    // Original large slice
    largeSlice := make([]int, 1000000)
    for i := range largeSlice {
        largeSlice[i] = i
    }

    printMemStats("After creating large slice")

    // Inefficient: keeps reference to large underlying array
    smallSliceInefficient := largeSlice[:10]

    // Efficient: creates new slice with only needed data
    smallSliceEfficient := make([]int, 10)
    copy(smallSliceEfficient, largeSlice[:10])

    // Clear reference to large slice
    largeSlice = nil

    runtime.GC()
    runtime.GC()
    printMemStats("After clearing large slice reference")

    // Use slices to prevent optimization
    fmt.Printf("Inefficient slice length: %d\n", len(smallSliceInefficient))
    fmt.Printf("Efficient slice length: %d\n", len(smallSliceEfficient))

    fmt.Println()
}

func main() {
    memoryAllocationExample()
    poolExample()
    stringOptimization()
    sliceMemoryManagement()
}
```

[⬆️ Back to Top](#table-of-contents)

## Package Management

### Go Modules and Package Organization

```go
// go.mod file example
module github.com/bank/core-banking

go 1.21

require (
    github.com/gin-gonic/gin v1.9.1
    github.com/golang-jwt/jwt/v5 v5.0.0
    github.com/google/uuid v1.3.0
    github.com/lib/pq v1.10.9
    github.com/stretchr/testify v1.8.4
    golang.org/x/crypto v0.14.0
)

require (
    github.com/bytedance/sonic v1.9.1 // indirect
    github.com/chenzhuoyu/base64x v0.0.0-20221115062448-fe3a3abad311 // indirect
    // ... other indirect dependencies
)
```

### Package Structure

```
banking-system/
├── cmd/
│   ├── api/
│   │   └── main.go
│   ├── cli/
│   │   └── main.go
│   └── worker/
│       └── main.go
├── internal/
│   ├── account/
│   │   ├── handler.go
│   │   ├── service.go
│   │   ├── repository.go
│   │   └── model.go
│   ├── transaction/
│   │   ├── handler.go
│   │   ├── service.go
│   │   └── repository.go
│   └── common/
│       ├── config/
│       ├── database/
│       └── middleware/
├── pkg/
│   ├── auth/
│   ├── validation/
│   └── logger/
├── api/
│   └── openapi.yaml
├── docs/
├── scripts/
├── deployments/
├── go.mod
├── go.sum
└── README.md
```

### Package Examples

```go
// pkg/logger/logger.go
package logger

import (
    "encoding/json"
    "fmt"
    "log"
    "os"
    "time"
)

type Level int

const (
    DEBUG Level = iota
    INFO
    WARN
    ERROR
    FATAL
)

func (l Level) String() string {
    switch l {
    case DEBUG:
        return "DEBUG"
    case INFO:
        return "INFO"
    case WARN:
        return "WARN"
    case ERROR:
        return "ERROR"
    case FATAL:
        return "FATAL"
    default:
        return "UNKNOWN"
    }
}

type Logger struct {
    level  Level
    logger *log.Logger
}

type LogEntry struct {
    Timestamp string                 `json:"timestamp"`
    Level     string                 `json:"level"`
    Message   string                 `json:"message"`
    Fields    map[string]interface{} `json:"fields,omitempty"`
}

func New(level Level) *Logger {
    return &Logger{
        level:  level,
        logger: log.New(os.Stdout, "", 0),
    }
}

func (l *Logger) log(level Level, message string, fields map[string]interface{}) {
    if level < l.level {
        return
    }

    entry := LogEntry{
        Timestamp: time.Now().Format(time.RFC3339),
        Level:     level.String(),
        Message:   message,
        Fields:    fields,
    }

    jsonBytes, _ := json.Marshal(entry)
    l.logger.Println(string(jsonBytes))
}

func (l *Logger) Debug(message string, fields ...map[string]interface{}) {
    l.log(DEBUG, message, mergeFields(fields...))
}

func (l *Logger) Info(message string, fields ...map[string]interface{}) {
    l.log(INFO, message, mergeFields(fields...))
}

func (l *Logger) Warn(message string, fields ...map[string]interface{}) {
    l.log(WARN, message, mergeFields(fields...))
}

func (l *Logger) Error(message string, fields ...map[string]interface{}) {
    l.log(ERROR, message, mergeFields(fields...))
}

func (l *Logger) Fatal(message string, fields ...map[string]interface{}) {
    l.log(FATAL, message, mergeFields(fields...))
    os.Exit(1)
}

func mergeFields(fields ...map[string]interface{}) map[string]interface{} {
    result := make(map[string]interface{})
    for _, fieldMap := range fields {
        for k, v := range fieldMap {
            result[k] = v
        }
    }
    return result
}

// pkg/auth/jwt.go
package auth

import (
    "errors"
    "time"

    "github.com/golang-jwt/jwt/v5"
)

type JWTManager struct {
    secretKey     string
    tokenDuration time.Duration
}

type Claims struct {
    UserID   string `json:"user_id"`
    Username string `json:"username"`
    Role     string `json:"role"`
    jwt.RegisteredClaims
}

func NewJWTManager(secretKey string, tokenDuration time.Duration) *JWTManager {
    return &JWTManager{
        secretKey:     secretKey,
        tokenDuration: tokenDuration,
    }
}

func (manager *JWTManager) GenerateToken(userID, username, role string) (string, error) {
    claims := Claims{
        UserID:   userID,
        Username: username,
        Role:     role,
        RegisteredClaims: jwt.RegisteredClaims{
            ExpiresAt: jwt.NewNumericDate(time.Now().Add(manager.tokenDuration)),
            IssuedAt:  jwt.NewNumericDate(time.Now()),
            NotBefore: jwt.NewNumericDate(time.Now()),
        },
    }

    token := jwt.NewWithClaims(jwt.SigningMethodHS256, claims)
    return token.SignedString([]byte(manager.secretKey))
}

func (manager *JWTManager) ValidateToken(tokenString string) (*Claims, error) {
    token, err := jwt.ParseWithClaims(
        tokenString,
        &Claims{},
        func(token *jwt.Token) (interface{}, error) {
            return []byte(manager.secretKey), nil
        },
    )

    if err != nil {
        return nil, err
    }

    claims, ok := token.Claims.(*Claims)
    if !ok {
        return nil, errors.New("invalid token claims")
    }

    return claims, nil
}

// internal/account/model.go
package account

import (
    "time"
)

type AccountType string

const (
    Checking    AccountType = "checking"
    Savings     AccountType = "savings"
    Investment  AccountType = "investment"
)

type Account struct {
    ID           string      `json:"id" db:"id"`
    CustomerID   string      `json:"customer_id" db:"customer_id"`
    AccountType  AccountType `json:"account_type" db:"account_type"`
    Balance      float64     `json:"balance" db:"balance"`
    Currency     string      `json:"currency" db:"currency"`
    Status       string      `json:"status" db:"status"`
    CreatedAt    time.Time   `json:"created_at" db:"created_at"`
    UpdatedAt    time.Time   `json:"updated_at" db:"updated_at"`
}

type CreateAccountRequest struct {
    CustomerID      string      `json:"customer_id" validate:"required"`
    AccountType     AccountType `json:"account_type" validate:"required"`
    InitialDeposit  float64     `json:"initial_deposit" validate:"min=0"`
    Currency        string      `json:"currency" validate:"required,len=3"`
}

type UpdateAccountRequest struct {
    Status string `json:"status" validate:"oneof=active inactive closed"`
}

// internal/account/repository.go
package account

import (
    "database/sql"
    "fmt"
)

type Repository interface {
    Create(account *Account) error
    GetByID(id string) (*Account, error)
    GetByCustomerID(customerID string) ([]*Account, error)
    Update(account *Account) error
    Delete(id string) error
}

type repository struct {
    db *sql.DB
}

func NewRepository(db *sql.DB) Repository {
    return &repository{db: db}
}

func (r *repository) Create(account *Account) error {
    query := `
        INSERT INTO accounts (id, customer_id, account_type, balance, currency, status, created_at, updated_at)
        VALUES ($1, $2, $3, $4, $5, $6, $7, $8)
    `

    _, err := r.db.Exec(query,
        account.ID,
        account.CustomerID,
        account.AccountType,
        account.Balance,
        account.Currency,
        account.Status,
        account.CreatedAt,
        account.UpdatedAt,
    )

    return err
}

func (r *repository) GetByID(id string) (*Account, error) {
    query := `
        SELECT id, customer_id, account_type, balance, currency, status, created_at, updated_at
        FROM accounts
        WHERE id = $1
    `

    account := &Account{}
    err := r.db.QueryRow(query, id).Scan(
        &account.ID,
        &account.CustomerID,
        &account.AccountType,
        &account.Balance,
        &account.Currency,
        &account.Status,
        &account.CreatedAt,
        &account.UpdatedAt,
    )

    if err != nil {
        if err == sql.ErrNoRows {
            return nil, fmt.Errorf("account not found: %s", id)
        }
        return nil, err
    }

    return account, nil
}

func (r *repository) GetByCustomerID(customerID string) ([]*Account, error) {
    query := `
        SELECT id, customer_id, account_type, balance, currency, status, created_at, updated_at
        FROM accounts
        WHERE customer_id = $1
        ORDER BY created_at DESC
    `

    rows, err := r.db.Query(query, customerID)
    if err != nil {
        return nil, err
    }
    defer rows.Close()

    var accounts []*Account
    for rows.Next() {
        account := &Account{}
        err := rows.Scan(
            &account.ID,
            &account.CustomerID,
            &account.AccountType,
            &account.Balance,
            &account.Currency,
            &account.Status,
            &account.CreatedAt,
            &account.UpdatedAt,
        )
        if err != nil {
            return nil, err
        }
        accounts = append(accounts, account)
    }

    return accounts, rows.Err()
}

func (r *repository) Update(account *Account) error {
    query := `
        UPDATE accounts
        SET account_type = $2, balance = $3, currency = $4, status = $5, updated_at = $6
        WHERE id = $1
    `

    result, err := r.db.Exec(query,
        account.ID,
        account.AccountType,
        account.Balance,
        account.Currency,
        account.Status,
        account.UpdatedAt,
    )

    if err != nil {
        return err
    }

    rowsAffected, err := result.RowsAffected()
    if err != nil {
        return err
    }

    if rowsAffected == 0 {
        return fmt.Errorf("account not found: %s", account.ID)
    }

    return nil
}

func (r *repository) Delete(id string) error {
    query := `DELETE FROM accounts WHERE id = $1`

    result, err := r.db.Exec(query, id)
    if err != nil {
        return err
    }

    rowsAffected, err := result.RowsAffected()
    if err != nil {
        return err
    }

    if rowsAffected == 0 {
        return fmt.Errorf("account not found: %s", id)
    }

    return nil
}

// internal/account/service.go
package account

import (
    "fmt"
    "time"

    "github.com/google/uuid"
)

type Service interface {
    CreateAccount(req *CreateAccountRequest) (*Account, error)
    GetAccount(id string) (*Account, error)
    GetCustomerAccounts(customerID string) ([]*Account, error)
    UpdateAccount(id string, req *UpdateAccountRequest) (*Account, error)
    DeleteAccount(id string) error
}

type service struct {
    repo Repository
}

func NewService(repo Repository) Service {
    return &service{repo: repo}
}

func (s *service) CreateAccount(req *CreateAccountRequest) (*Account, error) {
    // Validate request
    if err := s.validateCreateRequest(req); err != nil {
        return nil, err
    }

    account := &Account{
        ID:          uuid.New().String(),
        CustomerID:  req.CustomerID,
        AccountType: req.AccountType,
        Balance:     req.InitialDeposit,
        Currency:    req.Currency,
        Status:      "active",
        CreatedAt:   time.Now(),
        UpdatedAt:   time.Now(),
    }

    if err := s.repo.Create(account); err != nil {
        return nil, fmt.Errorf("failed to create account: %w", err)
    }

    return account, nil
}

func (s *service) GetAccount(id string) (*Account, error) {
    return s.repo.GetByID(id)
}

func (s *service) GetCustomerAccounts(customerID string) ([]*Account, error) {
    return s.repo.GetByCustomerID(customerID)
}

func (s *service) UpdateAccount(id string, req *UpdateAccountRequest) (*Account, error) {
    account, err := s.repo.GetByID(id)
    if err != nil {
        return nil, err
    }

    // Update fields
    if req.Status != "" {
        account.Status = req.Status
    }
    account.UpdatedAt = time.Now()

    if err := s.repo.Update(account); err != nil {
        return nil, fmt.Errorf("failed to update account: %w", err)
    }

    return account, nil
}

func (s *service) DeleteAccount(id string) error {
    account, err := s.repo.GetByID(id)
    if err != nil {
        return err
    }

    if account.Balance != 0 {
        return fmt.Errorf("cannot delete account with non-zero balance: %.2f", account.Balance)
    }

    return s.repo.Delete(id)
}

func (s *service) validateCreateRequest(req *CreateAccountRequest) error {
    if req.CustomerID == "" {
        return fmt.Errorf("customer ID is required")
    }

    if req.AccountType == "" {
        return fmt.Errorf("account type is required")
    }

    if req.InitialDeposit < 0 {
        return fmt.Errorf("initial deposit cannot be negative")
    }

    if len(req.Currency) != 3 {
        return fmt.Errorf("currency must be 3 characters")
    }

    return nil
}
```

[⬆️ Back to Top](#table-of-contents)

## Testing in Go

### Unit Testing, Benchmarks, and Test Organization

```go
// internal/account/service_test.go
package account

import (
    "errors"
    "testing"
    "time"

    "github.com/stretchr/testify/assert"
    "github.com/stretchr/testify/mock"
    "github.com/stretchr/testify/suite"
)

// Mock repository for testing
type MockRepository struct {
    mock.Mock
}

func (m *MockRepository) Create(account *Account) error {
    args := m.Called(account)
    return args.Error(0)
}

func (m *MockRepository) GetByID(id string) (*Account, error) {
    args := m.Called(id)
    return args.Get(0).(*Account), args.Error(1)
}

func (m *MockRepository) GetByCustomerID(customerID string) ([]*Account, error) {
    args := m.Called(customerID)
    return args.Get(0).([]*Account), args.Error(1)
}

func (m *MockRepository) Update(account *Account) error {
    args := m.Called(account)
    return args.Error(0)
}

func (m *MockRepository) Delete(id string) error {
    args := m.Called(id)
    return args.Error(0)
}

// Test suite using testify
type AccountServiceTestSuite struct {
    suite.Suite
    service  Service
    mockRepo *MockRepository
}

func (suite *AccountServiceTestSuite) SetupTest() {
    suite.mockRepo = new(MockRepository)
    suite.service = NewService(suite.mockRepo)
}

func (suite *AccountServiceTestSuite) TestCreateAccount_Success() {
    // Arrange
    req := &CreateAccountRequest{
        CustomerID:     "customer-123",
        AccountType:    Checking,
        InitialDeposit: 1000.0,
        Currency:       "USD",
    }

    suite.mockRepo.On("Create", mock.AnythingOfType("*account.Account")).Return(nil)

    // Act
    account, err := suite.service.CreateAccount(req)

    // Assert
    suite.NoError(err)
    suite.NotNil(account)
    suite.Equal(req.CustomerID, account.CustomerID)
    suite.Equal(req.AccountType, account.AccountType)
    suite.Equal(req.InitialDeposit, account.Balance)
    suite.Equal("active", account.Status)
    suite.mockRepo.AssertExpectations(suite.T())
}

func (suite *AccountServiceTestSuite) TestCreateAccount_ValidationError() {
    // Test cases for validation errors
    testCases := []struct {
        name        string
        request     *CreateAccountRequest
        expectedErr string
    }{
        {
            name: "empty customer ID",
            request: &CreateAccountRequest{
                CustomerID:     "",
                AccountType:    Checking,
                InitialDeposit: 1000.0,
                Currency:       "USD",
            },
            expectedErr: "customer ID is required",
        },
        {
            name: "empty account type",
            request: &CreateAccountRequest{
                CustomerID:     "customer-123",
                AccountType:    "",
                InitialDeposit: 1000.0,
                Currency:       "USD",
            },
            expectedErr: "account type is required",
        },
        {
            name: "negative initial deposit",
            request: &CreateAccountRequest{
                CustomerID:     "customer-123",
                AccountType:    Checking,
                InitialDeposit: -100.0,
                Currency:       "USD",
            },
            expectedErr: "initial deposit cannot be negative",
        },
        {
            name: "invalid currency",
            request: &CreateAccountRequest{
                CustomerID:     "customer-123",
                AccountType:    Checking,
                InitialDeposit: 1000.0,
                Currency:       "INVALID",
            },
            expectedErr: "currency must be 3 characters",
        },
    }

    for _, tc := range testCases {
        suite.T().Run(tc.name, func(t *testing.T) {
            // Act
            account, err := suite.service.CreateAccount(tc.request)

            // Assert
            suite.Error(err)
            suite.Nil(account)
            suite.Contains(err.Error(), tc.expectedErr)
        })
    }
}

func (suite *AccountServiceTestSuite) TestGetAccount_Success() {
    // Arrange
    expectedAccount := &Account{
        ID:          "account-123",
        CustomerID:  "customer-123",
        AccountType: Checking,
        Balance:     1000.0,
        Currency:    "USD",
        Status:      "active",
        CreatedAt:   time.Now(),
        UpdatedAt:   time.Now(),
    }

    suite.mockRepo.On("GetByID", "account-123").Return(expectedAccount, nil)

    // Act
    account, err := suite.service.GetAccount("account-123")

    // Assert
    suite.NoError(err)
    suite.Equal(expectedAccount, account)
    suite.mockRepo.AssertExpectations(suite.T())
}

func (suite *AccountServiceTestSuite) TestGetAccount_NotFound() {
    // Arrange
    suite.mockRepo.On("GetByID", "nonexistent").Return((*Account)(nil), errors.New("account not found"))

    // Act
    account, err := suite.service.GetAccount("nonexistent")

    // Assert
    suite.Error(err)
    suite.Nil(account)
    suite.mockRepo.AssertExpectations(suite.T())
}

func (suite *AccountServiceTestSuite) TestDeleteAccount_WithBalance() {
    // Arrange
    accountWithBalance := &Account{
        ID:      "account-123",
        Balance: 500.0,
    }

    suite.mockRepo.On("GetByID", "account-123").Return(accountWithBalance, nil)

    // Act
    err := suite.service.DeleteAccount("account-123")

    // Assert
    suite.Error(err)
    suite.Contains(err.Error(), "cannot delete account with non-zero balance")
    suite.mockRepo.AssertExpectations(suite.T())
}

func TestAccountServiceTestSuite(t *testing.T) {
    suite.Run(t, new(AccountServiceTestSuite))
}

// Table-driven tests
func TestValidateCreateRequest(t *testing.T) {
    service := &service{}

    tests := []struct {
        name    string
        request *CreateAccountRequest
        wantErr bool
        errMsg  string
    }{
        {
            name: "valid request",
            request: &CreateAccountRequest{
                CustomerID:     "customer-123",
                AccountType:    Checking,
                InitialDeposit: 1000.0,
                Currency:       "USD",
            },
            wantErr: false,
        },
        {
            name: "empty customer ID",
            request: &CreateAccountRequest{
                CustomerID:     "",
                AccountType:    Checking,
                InitialDeposit: 1000.0,
                Currency:       "USD",
            },
            wantErr: true,
            errMsg:  "customer ID is required",
        },
        {
            name: "invalid currency length",
            request: &CreateAccountRequest{
                CustomerID:     "customer-123",
                AccountType:    Checking,
                InitialDeposit: 1000.0,
                Currency:       "US",
            },
            wantErr: true,
            errMsg:  "currency must be 3 characters",
        },
    }

    for _, tt := range tests {
        t.Run(tt.name, func(t *testing.T) {
            err := service.validateCreateRequest(tt.request)

            if tt.wantErr {
                assert.Error(t, err)
                assert.Contains(t, err.Error(), tt.errMsg)
            } else {
                assert.NoError(t, err)
            }
        })
    }
}

// Benchmark tests
func BenchmarkAccountService_CreateAccount(b *testing.B) {
    mockRepo := new(MockRepository)
    service := NewService(mockRepo)

    req := &CreateAccountRequest{
        CustomerID:     "customer-123",
        AccountType:    Checking,
        InitialDeposit: 1000.0,
        Currency:       "USD",
    }

    mockRepo.On("Create", mock.AnythingOfType("*account.Account")).Return(nil)

    b.ResetTimer()
    for i := 0; i < b.N; i++ {
        _, err := service.CreateAccount(req)
        if err != nil {
            b.Fatal(err)
        }
    }
}

func BenchmarkAccountValidation(b *testing.B) {
    service := &service{}
    req := &CreateAccountRequest{
        CustomerID:     "customer-123",
        AccountType:    Checking,
        InitialDeposit: 1000.0,
        Currency:       "USD",
    }

    b.ResetTimer()
    for i := 0; i < b.N; i++ {
        err := service.validateCreateRequest(req)
        if err != nil {
            b.Fatal(err)
        }
    }
}

// Integration tests
func TestAccountIntegration(t *testing.T) {
    if testing.Short() {
        t.Skip("skipping integration test")
    }

    // Setup test database
    db := setupTestDB(t)
    defer cleanupTestDB(t, db)

    repo := NewRepository(db)
    service := NewService(repo)

    // Test creating and retrieving account
    req := &CreateAccountRequest{
        CustomerID:     "customer-123",
        AccountType:    Checking,
        InitialDeposit: 1000.0,
        Currency:       "USD",
    }

    // Create account
    account, err := service.CreateAccount(req)
    assert.NoError(t, err)
    assert.NotEmpty(t, account.ID)

    // Retrieve account
    retrievedAccount, err := service.GetAccount(account.ID)
    assert.NoError(t, err)
    assert.Equal(t, account.ID, retrievedAccount.ID)
    assert.Equal(t, account.CustomerID, retrievedAccount.CustomerID)
}

// Test helpers
func setupTestDB(t *testing.T) *sql.DB {
    // Setup test database connection
    // This would typically use a test database or in-memory database
    t.Helper()
    // Implementation would go here
    return nil
}

func cleanupTestDB(t *testing.T, db *sql.DB) {
    t.Helper()
    // Cleanup test database
    if db != nil {
        db.Close()
    }
}

// Example test with subtests
func TestAccountOperations(t *testing.T) {
    mockRepo := new(MockRepository)
    service := NewService(mockRepo)

    t.Run("CreateAccount", func(t *testing.T) {
        t.Run("Success", func(t *testing.T) {
            req := &CreateAccountRequest{
                CustomerID:     "customer-123",
                AccountType:    Checking,
                InitialDeposit: 1000.0,
                Currency:       "USD",
            }

            mockRepo.On("Create", mock.AnythingOfType("*account.Account")).Return(nil)

            account, err := service.CreateAccount(req)

            assert.NoError(t, err)
            assert.NotNil(t, account)
        })

        t.Run("ValidationError", func(t *testing.T) {
            req := &CreateAccountRequest{
                CustomerID:     "",
                AccountType:    Checking,
                InitialDeposit: 1000.0,
                Currency:       "USD",
            }

            account, err := service.CreateAccount(req)

            assert.Error(t, err)
            assert.Nil(t, account)
        })
    })

    t.Run("GetAccount", func(t *testing.T) {
        t.Run("Found", func(t *testing.T) {
            expectedAccount := &Account{ID: "account-123", CustomerID: "customer-123"}
            mockRepo.On("GetByID", "account-123").Return(expectedAccount, nil)

            account, err := service.GetAccount("account-123")

            assert.NoError(t, err)
            assert.Equal(t, expectedAccount, account)
        })

        t.Run("NotFound", func(t *testing.T) {
            mockRepo.On("GetByID", "nonexistent").Return((*Account)(nil), errors.New("not found"))

            account, err := service.GetAccount("nonexistent")

            assert.Error(t, err)
            assert.Nil(t, account)
        })
    })
}

// Fuzzing test (Go 1.18+)
func FuzzValidateCreateRequest(f *testing.F) {
    service := &service{}

    // Add seed inputs
    f.Add("customer-123", "checking", 1000.0, "USD")
    f.Add("", "savings", 500.0, "EUR")
    f.Add("customer-456", "", 0.0, "GBP")

    f.Fuzz(func(t *testing.T, customerID, accountType string, initialDeposit float64, currency string) {
        req := &CreateAccountRequest{
            CustomerID:     customerID,
            AccountType:    AccountType(accountType),
            InitialDeposit: initialDeposit,
            Currency:       currency,
        }

        // The function should not panic
        err := service.validateCreateRequest(req)

        // Basic validation - function should always return an error or nil
        if err != nil {
            // Error should be a string
            _ = err.Error()
        }
    })
}
```

[⬆️ Back to Top](#table-of-contents)

## Interview Questions by Difficulty

### Beginner Level Questions

**Q1: What is Go and what are its key features?**

**Answer:** Go (Golang) is a statically typed, compiled programming language designed at Google. Key features include:
- Fast compilation
- Built-in concurrency support with goroutines
- Garbage collection
- Strong typing
- Simple syntax
- Cross-platform compilation
- Excellent standard library

**Q2: Explain the difference between goroutines and threads.**

**Answer:**
- **Goroutines** are lightweight, managed by Go runtime, have dynamic stack size (starts at 2KB), and are multiplexed onto OS threads
- **OS Threads** are heavier (usually 1-2MB stack), managed by OS kernel, and have direct 1:1 mapping with kernel threads
- Go can run thousands of goroutines on a few OS threads

**Q3: How do you handle errors in Go?**

**Answer:** Go uses explicit error handling with multiple return values:

```go
result, err := someFunction()
if err != nil {
    // Handle error
    return err
}
// Use result
```

**Q4: What is a channel in Go?**

**Answer:** Channels are Go's way of communicating between goroutines. They provide synchronization and data exchange:

```go
ch := make(chan int)
go func() {
    ch <- 42  // Send
}()
value := <-ch  // Receive
```

### Intermediate Level Questions

**Q5: Explain the difference between buffered and unbuffered channels.**

**Answer:**
- **Unbuffered channels** (synchronous): Send blocks until another goroutine receives
- **Buffered channels** (asynchronous): Send only blocks when buffer is full

```go
unbuffered := make(chan int)     // Synchronous
buffered := make(chan int, 10)   // Can hold 10 values
```

**Q6: What is the select statement and how is it used?**

**Answer:** Select allows a goroutine to wait on multiple channel operations:

```go
select {
case msg1 := <-ch1:
    fmt.Println("Received from ch1:", msg1)
case msg2 := <-ch2:
    fmt.Println("Received from ch2:", msg2)
case <-timeout:
    fmt.Println("Timeout")
default:
    fmt.Println("No channels ready")
}
```

**Q7: Explain interfaces in Go with an example.**

**Answer:** Interfaces define method signatures. Go uses implicit interface satisfaction:

```go
type Writer interface {
    Write([]byte) (int, error)
}

type FileWriter struct{}

func (fw FileWriter) Write(data []byte) (int, error) {
    // Implementation
    return len(data), nil
}

// FileWriter automatically implements Writer interface
```

**Q8: How does Go's garbage collector work?**

**Answer:** Go uses a concurrent, tri-color mark-and-sweep garbage collector:
- Runs concurrently with program execution
- Uses write barriers to maintain consistency
- Aims for low latency (sub-millisecond pause times)
- Automatically manages memory allocation and deallocation

### Advanced Level Questions

**Q9: Explain the happens-before relationship in Go's memory model.**

**Answer:** The happens-before relationship defines when one memory operation is guaranteed to occur before another:
- Channel operations provide happens-before guarantees
- Mutex operations create happens-before relationships
- Once.Do() provides happens-before guarantees

```go
var a int
var done = make(chan bool)

func setup() {
    a = 42
    done <- true  // This happens-before the receive
}

func main() {
    go setup()
    <-done  // This happens-after the send
    fmt.Println(a)  // Guaranteed to print 42
}
```

**Q10: How would you implement a worker pool pattern?**

**Answer:**

```go
func workerPool(numWorkers int, jobs <-chan Job, results chan<- Result) {
    var wg sync.WaitGroup

    for i := 0; i < numWorkers; i++ {
        wg.Add(1)
        go func() {
            defer wg.Done()
            for job := range jobs {
                result := processJob(job)
                results <- result
            }
        }()
    }

    wg.Wait()
    close(results)
}
```

**Q11: Explain context package and its use cases.**

**Answer:** Context provides cancellation, timeouts, and request-scoped values:

```go
ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
defer cancel()

select {
case result := <-doWork(ctx):
    return result
case <-ctx.Done():
    return ctx.Err()  // timeout or cancellation
}
```

**Q12: How would you implement rate limiting in Go?**

**Answer:**

```go
type RateLimiter struct {
    limiter *rate.Limiter
}

func NewRateLimiter(r rate.Limit, b int) *RateLimiter {
    return &RateLimiter{
        limiter: rate.NewLimiter(r, b),
    }
}

func (rl *RateLimiter) Allow() bool {
    return rl.limiter.Allow()
}

func (rl *RateLimiter) Wait(ctx context.Context) error {
    return rl.limiter.Wait(ctx)
}
```

[⬆️ Back to Top](#table-of-contents)