# Go Fundamentals - Basic Syntax and Core Concepts

**Scope:** This guide covers only the fundamental Go language syntax, basic data types, control structures, and simple functions. For banking domain examples, see GOLANG_INTERVIEW_COMPREHENSIVE.md. For advanced topics like interfaces, concurrency, web development, and testing, see the specialized guides.

## Table of Contents
1. [Introduction to Go](#introduction-to-go)
2. [Basic Syntax and Data Types](#basic-syntax-and-data-types)
3. [Functions Basics](#functions-basics)
4. [Structs Basics](#structs-basics)
5. [Package Basics](#package-basics)

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
type Status string

const (
    Active   Status = "active"
    Inactive Status = "inactive"
)

func main() {
    // Basic types
    var (
        // Integers
        id       UserID = 12345
        count    int64  = 1000

        // Floats
        rate     float32 = 0.025
        amount   float64 = 1500.75

        // Strings
        name     string = "example"

        // Booleans
        isValid  bool = true

        // Arrays and Slices
        numbers  [5]int
        items    []string

        // Maps
        data     map[string]int

        // Pointers
        countPtr *int64
    )

    // Initialize collections
    numbers = [5]int{1, 2, 3, 4, 5}
    items = make([]string, 0, 10)
    data = make(map[string]int)

    // Pointer operations
    countPtr = &count

    fmt.Printf("ID: %d\n", id)
    fmt.Printf("Count: %d\n", count)
    fmt.Printf("Rate: %.3f\n", rate)
    fmt.Printf("Pointer to count: %v, Value: %d\n", countPtr, *countPtr)
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
    score := 85
    grade := "B"

    // If-else statements
    if score >= 90 {
        fmt.Println("Excellent!")
    } else if score >= 80 {
        fmt.Println("Good job!")
    } else {
        fmt.Println("Keep trying!")
    }

    // Switch statements
    switch grade {
    case "A":
        fmt.Println("Outstanding")
    case "B":
        fmt.Println("Good work")
    case "C":
        fmt.Println("Satisfactory")
    default:
        fmt.Println("Keep improving")
    }

    // For loops
    numbers := []int{1, 2, 3, 4, 5}

    // Traditional for loop
    for i := 0; i < len(numbers); i++ {
        fmt.Printf("Number %d: %d\n", i+1, numbers[i])
    }

    // Range loop
    for index, value := range numbers {
        fmt.Printf("Index %d: Value %d\n", index, value)
    }

    // While-like loop
    counter := 0
    maxCount := 3

    for counter < maxCount {
        fmt.Printf("Count: %d\n", counter)
        counter++
    }
}
```

[⬆️ Back to Top](#table-of-contents)

## Functions Basics

### Function Basics

```go
package main

import (
    "errors"
    "fmt"
    "math"
)

// Simple function
func add(a, b int) int {
    return a + b
}

// Multiple return values
func divide(a, b float64) (float64, error) {
    if b == 0 {
        return 0, errors.New("division by zero")
    }
    return a / b, nil
}

// Named return values
func calculate(a, b int) (sum, product int) {
    sum = a + b
    product = a * b
    return // naked return
}

// Variadic functions
func sum(numbers ...int) int {
    var total int
    for _, num := range numbers {
        total += num
    }
    return total
}

func main() {
    // Simple function call
    result := add(5, 3)
    fmt.Printf("5 + 3 = %d\n", result)

    // Multiple return values
    quotient, err := divide(10, 2)
    if err != nil {
        fmt.Printf("Error: %v\n", err)
    } else {
        fmt.Printf("10 / 2 = %.2f\n", quotient)
    }

    // Named returns
    s, p := calculate(4, 5)
    fmt.Printf("Sum: %d, Product: %d\n", s, p)

    // Variadic function
    total := sum(1, 2, 3, 4, 5)
    fmt.Printf("Sum of 1,2,3,4,5: %d\n", total)
}
```

## Structs Basics

```go
package main

import (
    "fmt"
    "time"
)

// Simple struct
type Person struct {
    Name string
    Age  int
}

// Struct with different field types
type Book struct {
    Title     string
    Author    string
    Pages     int
    Published bool
}

// Method with value receiver (doesn't modify original)
func (p Person) GetInfo() string {
    return fmt.Sprintf("%s is %d years old", p.Name, p.Age)
}

// Method with pointer receiver (can modify original)
func (p *Person) HaveBirthday() {
    p.Age++
}

// Method on Book
func (b Book) IsLongBook() bool {
    return b.Pages > 300
}

func main() {
    // Create struct instances
    person1 := Person{
        Name: "Alice",
        Age:  25,
    }

    person2 := Person{"Bob", 30} // Positional syntax

    book := Book{
        Title:     "Go Programming",
        Author:    "John Doe",
        Pages:     400,
        Published: true,
    }

    // Call methods
    fmt.Println(person1.GetInfo())
    fmt.Println(person2.GetInfo())

    // Modify through pointer receiver
    person1.HaveBirthday()
    fmt.Printf("After birthday: %s\n", person1.GetInfo())

    // Call method on book
    if book.IsLongBook() {
        fmt.Printf("%s is a long book\n", book.Title)
    }
}
```

## Package Basics

### Go Modules and Package Basics

Go uses modules for dependency management. Here are the basic commands:

```bash
# Initialize a new module
go mod init example.com/myproject

# Add dependencies
go get github.com/some/package

# Remove unused dependencies
go mod tidy
```

### Basic Package Structure

```
myproject/
├── go.mod
├── main.go
├── utils/
│   └── math.go
└── models/
    └── user.go
```

### Simple Package Example

#### Math Utilities (utils/math.go)

```go
package utils

// Add adds two integers
func Add(a, b int) int {
    return a + b
}

// Multiply multiplies two integers
func Multiply(a, b int) int {
    return a * b
}

// max is not exported (lowercase)
func max(a, b int) int {
    if a > b {
        return a
    }
    return b
}
```

#### Using the package (main.go)

```go
package main

import (
    "fmt"
    "example.com/myproject/utils"
)

func main() {
    result := utils.Add(5, 3)
    fmt.Printf("5 + 3 = %d\n", result)

    product := utils.Multiply(4, 7)
    fmt.Printf("4 * 7 = %d\n", product)

    // This would cause an error - max is not exported
    // utils.max(5, 3)
}
```

**Note:** For complete examples with interfaces, advanced methods, concurrency, error handling, testing, and real-world applications, see the comprehensive guide (GOLANG_INTERVIEW_COMPREHENSIVE.md) and specialized topic guides.

[⬆️ Back to Top](#table-of-contents)