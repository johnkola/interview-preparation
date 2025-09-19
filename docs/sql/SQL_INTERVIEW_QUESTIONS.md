# SQL Interview Questions for Banking Applications

## Table of Contents
1. [Basic SQL Concepts](#basic-sql-concepts)
2. [Joins and Relationships](#joins-and-relationships)
3. [Aggregations and Grouping](#aggregations-and-grouping)
4. [Window Functions](#window-functions)
5. [Performance Optimization](#performance-optimization)
6. [Transaction Management](#transaction-management)
7. [Banking-Specific Scenarios](#banking-specific-scenarios)
8. [Advanced Topics](#advanced-topics)

---

## Basic SQL Concepts

### 1. Find All Active Accounts with Balance Greater Than $10,000

**Question:** Write a query to find all active accounts with a balance greater than $10,000.

```sql
SELECT
    account_number,
    customer_id,
    account_type,
    balance,
    currency_code
FROM accounts
WHERE status = 'ACTIVE'
    AND balance > 10000
ORDER BY balance DESC;
```

### 2. Update Account Status Based on Last Transaction Date

**Question:** Update account status to 'DORMANT' if no transaction in last 6 months.

```sql
UPDATE accounts
SET status = 'DORMANT',
    updated_at = GETDATE()
WHERE account_id IN (
    SELECT a.account_id
    FROM accounts a
    LEFT JOIN (
        SELECT account_id, MAX(transaction_date) as last_transaction
        FROM transactions
        GROUP BY account_id
    ) t ON a.account_id = t.account_id
    WHERE a.status = 'ACTIVE'
        AND (t.last_transaction < DATEADD(MONTH, -6, GETDATE())
             OR t.last_transaction IS NULL)
);
```

### 3. Delete Duplicate Records

**Question:** Remove duplicate customer records keeping only the most recent.

```sql
WITH CTE AS (
    SELECT
        customer_id,
        email,
        created_at,
        ROW_NUMBER() OVER (
            PARTITION BY email
            ORDER BY created_at DESC
        ) as rn
    FROM customers
)
DELETE FROM CTE WHERE rn > 1;

-- Alternative using self-join
DELETE c1
FROM customers c1
INNER JOIN customers c2
WHERE c1.email = c2.email
    AND c1.customer_id < c2.customer_id;
```

---

## Joins and Relationships

### 4. Customer Account Summary

**Question:** Get all customers with their account details and total balance across all accounts.

```sql
SELECT
    c.customer_id,
    c.first_name,
    c.last_name,
    c.email,
    COUNT(DISTINCT a.account_id) as total_accounts,
    SUM(a.balance) as total_balance,
    STRING_AGG(a.account_type, ', ') as account_types
FROM customers c
LEFT JOIN accounts a ON c.customer_id = a.customer_id
WHERE a.status = 'ACTIVE' OR a.status IS NULL
GROUP BY c.customer_id, c.first_name, c.last_name, c.email
ORDER BY total_balance DESC;
```

### 5. Find Customers Without Any Accounts

**Question:** List all customers who don't have any accounts.

```sql
-- Using LEFT JOIN
SELECT
    c.customer_id,
    c.first_name,
    c.last_name,
    c.registration_date
FROM customers c
LEFT JOIN accounts a ON c.customer_id = a.customer_id
WHERE a.account_id IS NULL;

-- Using NOT EXISTS
SELECT
    c.customer_id,
    c.first_name,
    c.last_name,
    c.registration_date
FROM customers c
WHERE NOT EXISTS (
    SELECT 1
    FROM accounts a
    WHERE a.customer_id = c.customer_id
);

-- Using NOT IN (careful with NULLs)
SELECT
    customer_id,
    first_name,
    last_name,
    registration_date
FROM customers
WHERE customer_id NOT IN (
    SELECT DISTINCT customer_id
    FROM accounts
    WHERE customer_id IS NOT NULL
);
```

### 6. Complex Join - Transaction History with Account and Customer Details

**Question:** Get transaction details with customer and account information.

```sql
SELECT
    t.transaction_id,
    t.transaction_date,
    c.customer_name,
    a.account_number,
    a.account_type,
    t.transaction_type,
    t.amount,
    t.balance_after,
    t.description,
    CASE
        WHEN t.transaction_type = 'DEBIT' THEN -t.amount
        ELSE t.amount
    END as signed_amount
FROM transactions t
INNER JOIN accounts a ON t.account_id = a.account_id
INNER JOIN customers c ON a.customer_id = c.customer_id
WHERE t.transaction_date >= DATEADD(DAY, -30, GETDATE())
ORDER BY t.transaction_date DESC;
```

---

## Aggregations and Grouping

### 7. Monthly Transaction Summary

**Question:** Calculate monthly transaction volumes and amounts by transaction type.

```sql
SELECT
    YEAR(transaction_date) as year,
    MONTH(transaction_date) as month,
    transaction_type,
    COUNT(*) as transaction_count,
    SUM(amount) as total_amount,
    AVG(amount) as avg_amount,
    MIN(amount) as min_amount,
    MAX(amount) as max_amount
FROM transactions
WHERE transaction_date >= DATEADD(YEAR, -1, GETDATE())
GROUP BY
    YEAR(transaction_date),
    MONTH(transaction_date),
    transaction_type
HAVING COUNT(*) > 10  -- Only show months with significant activity
ORDER BY year DESC, month DESC, transaction_type;
```

### 8. Top 10 Customers by Transaction Volume

**Question:** Find top 10 customers by transaction volume in last quarter.

```sql
SELECT TOP 10
    c.customer_id,
    c.customer_name,
    COUNT(DISTINCT a.account_id) as active_accounts,
    COUNT(t.transaction_id) as transaction_count,
    SUM(t.amount) as total_volume,
    AVG(t.amount) as avg_transaction_amount
FROM customers c
INNER JOIN accounts a ON c.customer_id = a.customer_id
INNER JOIN transactions t ON a.account_id = t.account_id
WHERE t.transaction_date >= DATEADD(QUARTER, -1, GETDATE())
GROUP BY c.customer_id, c.customer_name
ORDER BY total_volume DESC;

-- Using CTE for better readability
WITH CustomerTransactions AS (
    SELECT
        c.customer_id,
        c.customer_name,
        t.transaction_id,
        t.amount,
        a.account_id
    FROM customers c
    INNER JOIN accounts a ON c.customer_id = a.customer_id
    INNER JOIN transactions t ON a.account_id = t.account_id
    WHERE t.transaction_date >= DATEADD(QUARTER, -1, GETDATE())
)
SELECT TOP 10
    customer_id,
    customer_name,
    COUNT(DISTINCT account_id) as active_accounts,
    COUNT(transaction_id) as transaction_count,
    SUM(amount) as total_volume
FROM CustomerTransactions
GROUP BY customer_id, customer_name
ORDER BY total_volume DESC;
```

### 9. Account Balance Brackets

**Question:** Categorize accounts by balance brackets and count them.

```sql
SELECT
    CASE
        WHEN balance < 1000 THEN 'Low (< $1K)'
        WHEN balance BETWEEN 1000 AND 9999 THEN 'Medium ($1K-$10K)'
        WHEN balance BETWEEN 10000 AND 99999 THEN 'High ($10K-$100K)'
        WHEN balance >= 100000 THEN 'Premium (>= $100K)'
    END as balance_bracket,
    COUNT(*) as account_count,
    SUM(balance) as total_balance,
    AVG(balance) as avg_balance
FROM accounts
WHERE status = 'ACTIVE'
GROUP BY
    CASE
        WHEN balance < 1000 THEN 'Low (< $1K)'
        WHEN balance BETWEEN 1000 AND 9999 THEN 'Medium ($1K-$10K)'
        WHEN balance BETWEEN 10000 AND 99999 THEN 'High ($10K-$100K)'
        WHEN balance >= 100000 THEN 'Premium (>= $100K)'
    END
ORDER BY
    CASE balance_bracket
        WHEN 'Low (< $1K)' THEN 1
        WHEN 'Medium ($1K-$10K)' THEN 2
        WHEN 'High ($10K-$100K)' THEN 3
        WHEN 'Premium (>= $100K)' THEN 4
    END;
```

---

## Window Functions

### 10. Running Balance Calculation

**Question:** Calculate running balance for each account's transactions.

```sql
SELECT
    account_id,
    transaction_id,
    transaction_date,
    transaction_type,
    amount,
    SUM(CASE
        WHEN transaction_type = 'CREDIT' THEN amount
        WHEN transaction_type = 'DEBIT' THEN -amount
    END) OVER (
        PARTITION BY account_id
        ORDER BY transaction_date, transaction_id
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) as running_balance
FROM transactions
ORDER BY account_id, transaction_date;
```

### 11. Rank Customers by Balance Within Each Account Type

**Question:** Rank customers by their account balance within each account type.

```sql
SELECT
    c.customer_name,
    a.account_type,
    a.balance,
    RANK() OVER (
        PARTITION BY a.account_type
        ORDER BY a.balance DESC
    ) as rank_in_type,
    DENSE_RANK() OVER (
        PARTITION BY a.account_type
        ORDER BY a.balance DESC
    ) as dense_rank_in_type,
    ROW_NUMBER() OVER (
        PARTITION BY a.account_type
        ORDER BY a.balance DESC
    ) as row_number_in_type,
    PERCENT_RANK() OVER (
        PARTITION BY a.account_type
        ORDER BY a.balance DESC
    ) as percentile_rank
FROM accounts a
INNER JOIN customers c ON a.customer_id = c.customer_id
WHERE a.status = 'ACTIVE';
```

### 12. Compare Current Month vs Previous Month Transactions

**Question:** Compare each account's current month transactions with previous month.

```sql
WITH MonthlyStats AS (
    SELECT
        account_id,
        YEAR(transaction_date) as year,
        MONTH(transaction_date) as month,
        COUNT(*) as transaction_count,
        SUM(amount) as total_amount
    FROM transactions
    GROUP BY
        account_id,
        YEAR(transaction_date),
        MONTH(transaction_date)
)
SELECT
    account_id,
    year,
    month,
    transaction_count,
    total_amount,
    LAG(transaction_count, 1) OVER (
        PARTITION BY account_id
        ORDER BY year, month
    ) as prev_month_count,
    LAG(total_amount, 1) OVER (
        PARTITION BY account_id
        ORDER BY year, month
    ) as prev_month_amount,
    transaction_count - LAG(transaction_count, 1) OVER (
        PARTITION BY account_id
        ORDER BY year, month
    ) as count_change,
    ROUND(
        100.0 * (total_amount - LAG(total_amount, 1) OVER (
            PARTITION BY account_id
            ORDER BY year, month
        )) / NULLIF(LAG(total_amount, 1) OVER (
            PARTITION BY account_id
            ORDER BY year, month
        ), 0),
        2
    ) as amount_change_percentage
FROM MonthlyStats
ORDER BY account_id, year DESC, month DESC;
```

### 13. Find Top N Transactions per Account

**Question:** Get top 3 largest transactions for each account.

```sql
WITH RankedTransactions AS (
    SELECT
        account_id,
        transaction_id,
        transaction_date,
        amount,
        description,
        ROW_NUMBER() OVER (
            PARTITION BY account_id
            ORDER BY amount DESC
        ) as rn
    FROM transactions
    WHERE transaction_date >= DATEADD(MONTH, -3, GETDATE())
)
SELECT
    account_id,
    transaction_id,
    transaction_date,
    amount,
    description
FROM RankedTransactions
WHERE rn <= 3
ORDER BY account_id, rn;
```

---

## Performance Optimization

### 14. Optimizing Slow Queries with Indexes

**Question:** How would you optimize this slow query?

```sql
-- Original slow query
SELECT
    c.customer_name,
    SUM(t.amount) as total_spent
FROM customers c
JOIN accounts a ON c.customer_id = a.customer_id
JOIN transactions t ON a.account_id = t.account_id
WHERE t.transaction_date BETWEEN '2024-01-01' AND '2024-12-31'
    AND t.transaction_type = 'DEBIT'
    AND c.customer_type = 'PREMIUM'
GROUP BY c.customer_id, c.customer_name
HAVING SUM(t.amount) > 100000;

-- Optimization steps:

-- 1. Create appropriate indexes
CREATE INDEX IX_transactions_date_type
ON transactions(transaction_date, transaction_type)
INCLUDE (account_id, amount);

CREATE INDEX IX_customers_type
ON customers(customer_type)
INCLUDE (customer_id, customer_name);

CREATE INDEX IX_accounts_customer
ON accounts(customer_id, account_id);

-- 2. Rewrite query with better filtering
WITH PremiumCustomers AS (
    SELECT customer_id, customer_name
    FROM customers
    WHERE customer_type = 'PREMIUM'
),
RelevantTransactions AS (
    SELECT
        t.account_id,
        SUM(t.amount) as total_amount
    FROM transactions t WITH (INDEX(IX_transactions_date_type))
    WHERE t.transaction_date BETWEEN '2024-01-01' AND '2024-12-31'
        AND t.transaction_type = 'DEBIT'
    GROUP BY t.account_id
    HAVING SUM(t.amount) > 100000
)
SELECT
    pc.customer_name,
    SUM(rt.total_amount) as total_spent
FROM PremiumCustomers pc
JOIN accounts a ON pc.customer_id = a.customer_id
JOIN RelevantTransactions rt ON a.account_id = rt.account_id
GROUP BY pc.customer_id, pc.customer_name;

-- 3. Check execution plan
SET STATISTICS IO ON;
SET STATISTICS TIME ON;
-- Run query
SET STATISTICS IO OFF;
SET STATISTICS TIME OFF;
```

### 15. Query Plan Analysis

**Question:** How to analyze and improve query performance?

```sql
-- Enable execution plan
SET SHOWPLAN_ALL ON;
GO
-- Your query here
SET SHOWPLAN_ALL OFF;
GO

-- Or use graphical execution plan in SSMS

-- Check for missing indexes
SELECT
    migs.avg_total_user_cost * (migs.avg_user_impact / 100.0) *
    (migs.user_seeks + migs.user_scans) AS improvement_measure,
    'CREATE INDEX [IX_' + CONVERT(VARCHAR, mig.index_group_handle) + '_' +
    CONVERT(VARCHAR, mid.index_handle) + '_' +
    LEFT(PARSENAME(mid.statement, 1), 32) + ']' +
    ' ON ' + mid.statement +
    ' (' + ISNULL(mid.equality_columns, '') +
    CASE WHEN mid.equality_columns IS NOT NULL AND
              mid.inequality_columns IS NOT NULL THEN ',' ELSE '' END +
    ISNULL(mid.inequality_columns, '') + ')' +
    ISNULL(' INCLUDE (' + mid.included_columns + ')', '') AS create_index_statement,
    migs.*,
    mid.database_id,
    mid.[object_id]
FROM sys.dm_db_missing_index_groups mig
INNER JOIN sys.dm_db_missing_index_group_stats migs
    ON migs.group_handle = mig.index_group_handle
INNER JOIN sys.dm_db_missing_index_details mid
    ON mig.index_handle = mid.index_handle
WHERE migs.avg_total_user_cost * (migs.avg_user_impact / 100.0) *
      (migs.user_seeks + migs.user_scans) > 10
ORDER BY improvement_measure DESC;
```

---

## Transaction Management

### 16. Implementing Safe Money Transfer

**Question:** Write a stored procedure for safe money transfer between accounts.

```sql
CREATE PROCEDURE sp_TransferMoney
    @FromAccountId INT,
    @ToAccountId INT,
    @Amount DECIMAL(15,2),
    @Description NVARCHAR(500),
    @TransactionId VARCHAR(50) OUTPUT,
    @ErrorMessage NVARCHAR(500) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    DECLARE @FromBalance DECIMAL(15,2);
    DECLARE @ToBalance DECIMAL(15,2);

    BEGIN TRY
        BEGIN TRANSACTION;

        -- Lock both accounts to prevent concurrent modifications
        SELECT @FromBalance = balance
        FROM accounts WITH (UPDLOCK, ROWLOCK)
        WHERE account_id = @FromAccountId
            AND status = 'ACTIVE';

        SELECT @ToBalance = balance
        FROM accounts WITH (UPDLOCK, ROWLOCK)
        WHERE account_id = @ToAccountId
            AND status = 'ACTIVE';

        -- Validations
        IF @FromBalance IS NULL
        BEGIN
            RAISERROR('Source account not found or inactive', 16, 1);
        END

        IF @ToBalance IS NULL
        BEGIN
            RAISERROR('Destination account not found or inactive', 16, 1);
        END

        IF @FromBalance < @Amount
        BEGIN
            RAISERROR('Insufficient funds', 16, 1);
        END

        IF @Amount <= 0
        BEGIN
            RAISERROR('Transfer amount must be positive', 16, 1);
        END

        -- Generate transaction ID
        SET @TransactionId = CONCAT('TXN', FORMAT(GETDATE(), 'yyyyMMddHHmmss'),
                                    RIGHT(NEWID(), 8));

        -- Update balances
        UPDATE accounts
        SET balance = balance - @Amount,
            updated_at = GETDATE()
        WHERE account_id = @FromAccountId;

        UPDATE accounts
        SET balance = balance + @Amount,
            updated_at = GETDATE()
        WHERE account_id = @ToAccountId;

        -- Record transactions
        INSERT INTO transactions (
            transaction_id, account_id, transaction_type,
            amount, balance_after, description, transaction_date
        )
        VALUES
            (@TransactionId + '_D', @FromAccountId, 'DEBIT',
             @Amount, @FromBalance - @Amount, @Description, GETDATE()),
            (@TransactionId + '_C', @ToAccountId, 'CREDIT',
             @Amount, @ToBalance + @Amount, @Description, GETDATE());

        -- Audit log
        INSERT INTO audit_log (
            action, entity_type, entity_id, user_id,
            action_date, details
        )
        VALUES (
            'TRANSFER', 'TRANSACTION', @TransactionId,
            SUSER_SNAME(), GETDATE(),
            CONCAT('Transfer $', @Amount, ' from account ',
                   @FromAccountId, ' to ', @ToAccountId)
        );

        COMMIT TRANSACTION;
        SET @ErrorMessage = NULL;

    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0
            ROLLBACK TRANSACTION;

        SET @TransactionId = NULL;
        SET @ErrorMessage = ERROR_MESSAGE();

        -- Log error
        INSERT INTO error_log (
            error_date, error_message, error_severity,
            error_state, error_procedure, error_line
        )
        VALUES (
            GETDATE(), ERROR_MESSAGE(), ERROR_SEVERITY(),
            ERROR_STATE(), ERROR_PROCEDURE(), ERROR_LINE()
        );

        THROW;
    END CATCH
END;
GO
```

### 17. Deadlock Prevention and Handling

**Question:** How to prevent and handle deadlocks in banking transactions?

```sql
-- 1. Always access tables in the same order
-- 2. Keep transactions short
-- 3. Use appropriate isolation levels

-- Example: Deadlock-resistant update pattern
CREATE PROCEDURE sp_UpdateAccountBalanceSafe
    @AccountId INT,
    @Amount DECIMAL(15,2),
    @TransactionType VARCHAR(10)
AS
BEGIN
    SET NOCOUNT ON;
    DECLARE @RetryCount INT = 0;
    DECLARE @MaxRetries INT = 3;

    WHILE @RetryCount < @MaxRetries
    BEGIN
        BEGIN TRY
            BEGIN TRANSACTION;

            -- Use ROWLOCK hint to minimize lock scope
            UPDATE accounts WITH (ROWLOCK)
            SET balance = CASE
                    WHEN @TransactionType = 'CREDIT' THEN balance + @Amount
                    WHEN @TransactionType = 'DEBIT' THEN balance - @Amount
                  END,
                updated_at = GETDATE()
            WHERE account_id = @AccountId
                AND status = 'ACTIVE'
                AND (@TransactionType = 'CREDIT' OR balance >= @Amount);

            IF @@ROWCOUNT = 0
            BEGIN
                ROLLBACK TRANSACTION;
                RAISERROR('Update failed: Account not found or insufficient funds', 16, 1);
                RETURN;
            END

            COMMIT TRANSACTION;
            RETURN; -- Success, exit procedure

        END TRY
        BEGIN CATCH
            IF @@TRANCOUNT > 0
                ROLLBACK TRANSACTION;

            -- Check if it's a deadlock
            IF ERROR_NUMBER() = 1205 -- Deadlock error
            BEGIN
                SET @RetryCount = @RetryCount + 1;
                IF @RetryCount < @MaxRetries
                BEGIN
                    -- Exponential backoff
                    WAITFOR DELAY '00:00:01';
                    CONTINUE; -- Retry
                END
            END

            -- If not a deadlock or max retries reached, throw error
            THROW;
        END CATCH
    END
END;
GO

-- Monitor deadlocks
SELECT
    xed.value('@timestamp', 'datetime2') as deadlock_time,
    xed.query('.') as deadlock_xml
FROM (
    SELECT CAST(target_data as XML) as target_data
    FROM sys.dm_xe_sessions s
    JOIN sys.dm_xe_session_targets t
        ON s.address = t.event_session_address
    WHERE s.name = 'system_health'
        AND t.target_name = 'ring_buffer'
) AS x
CROSS APPLY target_data.nodes('RingBufferTarget/event[@name="xml_deadlock_report"]') AS xed(xed)
ORDER BY deadlock_time DESC;
```

---

## Banking-Specific Scenarios

### 18. Calculate Interest for Savings Accounts

**Question:** Calculate compound interest for all savings accounts.

```sql
-- Daily compound interest calculation
CREATE PROCEDURE sp_CalculateDailyInterest
    @Date DATE = NULL
AS
BEGIN
    SET NOCOUNT ON;

    IF @Date IS NULL
        SET @Date = CAST(GETDATE() AS DATE);

    -- Get interest rates by account type
    WITH InterestRates AS (
        SELECT
            account_type,
            annual_interest_rate
        FROM account_types
        WHERE effective_date <= @Date
            AND (expiry_date IS NULL OR expiry_date > @Date)
    ),
    DailyInterest AS (
        SELECT
            a.account_id,
            a.balance,
            ir.annual_interest_rate,
            -- Daily interest = Principal * (Annual Rate / 365)
            ROUND(a.balance * (ir.annual_interest_rate / 100.0 / 365.0), 2) as interest_amount
        FROM accounts a
        INNER JOIN InterestRates ir ON a.account_type = ir.account_type
        WHERE a.status = 'ACTIVE'
            AND a.account_type IN ('SAVINGS', 'HIGH_YIELD_SAVINGS')
            AND a.balance > 0
    )
    INSERT INTO interest_transactions (
        account_id,
        transaction_date,
        interest_amount,
        balance_before,
        interest_rate,
        created_at
    )
    SELECT
        account_id,
        @Date,
        interest_amount,
        balance,
        annual_interest_rate,
        GETDATE()
    FROM DailyInterest
    WHERE interest_amount > 0;

    -- Update account balances
    UPDATE a
    SET balance = balance + di.interest_amount,
        updated_at = GETDATE()
    FROM accounts a
    INNER JOIN (
        SELECT account_id, SUM(interest_amount) as interest_amount
        FROM interest_transactions
        WHERE transaction_date = @Date
        GROUP BY account_id
    ) di ON a.account_id = di.account_id;
END;
GO
```

### 19. Fraud Detection Query

**Question:** Identify potentially fraudulent transactions.

```sql
-- Detect suspicious transaction patterns
WITH TransactionPatterns AS (
    SELECT
        t.transaction_id,
        t.account_id,
        t.amount,
        t.transaction_date,
        t.merchant_category,
        t.location,
        -- Calculate statistics for the account
        AVG(t.amount) OVER (
            PARTITION BY t.account_id
            ORDER BY t.transaction_date
            ROWS BETWEEN 30 PRECEDING AND 1 PRECEDING
        ) as avg_amount_30d,
        STDEV(t.amount) OVER (
            PARTITION BY t.account_id
            ORDER BY t.transaction_date
            ROWS BETWEEN 30 PRECEDING AND 1 PRECEDING
        ) as stdev_amount_30d,
        COUNT(*) OVER (
            PARTITION BY t.account_id, CAST(t.transaction_date AS DATE)
        ) as daily_transaction_count,
        LAG(t.location, 1) OVER (
            PARTITION BY t.account_id
            ORDER BY t.transaction_date
        ) as previous_location,
        DATEDIFF(MINUTE,
            LAG(t.transaction_date, 1) OVER (
                PARTITION BY t.account_id
                ORDER BY t.transaction_date
            ),
            t.transaction_date
        ) as minutes_since_last
    FROM transactions t
    WHERE t.transaction_type = 'DEBIT'
        AND t.transaction_date >= DATEADD(DAY, -1, GETDATE())
)
SELECT
    tp.*,
    CASE
        -- Flag 1: Unusually large amount (> 3 standard deviations)
        WHEN tp.amount > (tp.avg_amount_30d + 3 * ISNULL(tp.stdev_amount_30d, 0))
        THEN 'UNUSUAL_AMOUNT'

        -- Flag 2: Rapid successive transactions
        WHEN tp.minutes_since_last < 5 AND tp.minutes_since_last IS NOT NULL
        THEN 'RAPID_SUCCESSION'

        -- Flag 3: Multiple transactions in a day
        WHEN tp.daily_transaction_count > 10
        THEN 'HIGH_FREQUENCY'

        -- Flag 4: Geographic impossibility
        WHEN tp.previous_location IS NOT NULL
            AND tp.location != tp.previous_location
            AND tp.minutes_since_last < 60
            AND dbo.fn_CalculateDistance(tp.previous_location, tp.location) > 500
        THEN 'IMPOSSIBLE_TRAVEL'

        -- Flag 5: First transaction after dormancy
        WHEN tp.minutes_since_last > 43200 -- 30 days in minutes
        THEN 'DORMANT_ACCOUNT_ACTIVITY'

        ELSE 'NORMAL'
    END as fraud_flag,
    c.customer_name,
    c.phone_number,
    c.email
FROM TransactionPatterns tp
INNER JOIN accounts a ON tp.account_id = a.account_id
INNER JOIN customers c ON a.customer_id = c.customer_id
WHERE tp.amount > 1000 -- Focus on significant transactions
    OR tp.daily_transaction_count > 5
ORDER BY
    CASE
        WHEN tp.amount > (tp.avg_amount_30d + 3 * ISNULL(tp.stdev_amount_30d, 0)) THEN 1
        WHEN tp.minutes_since_last < 5 THEN 2
        ELSE 3
    END,
    tp.transaction_date DESC;
```

### 20. Customer Lifetime Value Calculation

**Question:** Calculate customer lifetime value based on banking activities.

```sql
WITH CustomerMetrics AS (
    SELECT
        c.customer_id,
        c.customer_name,
        c.registration_date,
        DATEDIFF(MONTH, c.registration_date, GETDATE()) as months_as_customer,
        COUNT(DISTINCT a.account_id) as total_accounts,
        COUNT(DISTINCT CASE WHEN a.status = 'ACTIVE' THEN a.account_id END) as active_accounts,
        SUM(CASE WHEN a.status = 'ACTIVE' THEN a.balance ELSE 0 END) as total_balance,
        COUNT(DISTINCT l.loan_id) as total_loans,
        SUM(l.loan_amount) as total_loan_amount,
        SUM(l.interest_earned) as total_interest_earned
    FROM customers c
    LEFT JOIN accounts a ON c.customer_id = a.customer_id
    LEFT JOIN loans l ON c.customer_id = l.customer_id
    GROUP BY c.customer_id, c.customer_name, c.registration_date
),
TransactionMetrics AS (
    SELECT
        c.customer_id,
        COUNT(t.transaction_id) as total_transactions,
        SUM(CASE WHEN t.transaction_type = 'FEE' THEN t.amount ELSE 0 END) as total_fees,
        AVG(t.amount) as avg_transaction_amount,
        COUNT(DISTINCT YEAR(t.transaction_date) * 100 + MONTH(t.transaction_date)) as active_months
    FROM customers c
    LEFT JOIN accounts a ON c.customer_id = a.customer_id
    LEFT JOIN transactions t ON a.account_id = t.account_id
    GROUP BY c.customer_id
)
SELECT
    cm.customer_id,
    cm.customer_name,
    cm.months_as_customer,
    cm.total_accounts,
    cm.active_accounts,
    cm.total_balance,
    cm.total_loans,
    cm.total_loan_amount,
    cm.total_interest_earned,
    tm.total_transactions,
    tm.total_fees,
    tm.avg_transaction_amount,
    tm.active_months,
    -- Calculate Customer Lifetime Value
    (
        ISNULL(cm.total_interest_earned, 0) +  -- Interest from loans
        ISNULL(tm.total_fees, 0) +              -- Transaction fees
        (cm.total_balance * 0.002 * cm.months_as_customer) -- Estimated profit from deposits
    ) as current_clv,
    -- Projected CLV (next 5 years)
    (
        ISNULL(cm.total_interest_earned, 0) +
        ISNULL(tm.total_fees, 0) +
        (cm.total_balance * 0.002 * cm.months_as_customer)
    ) * (1 + (60.0 / NULLIF(cm.months_as_customer, 0))) as projected_clv_5y,
    -- Customer Segment
    CASE
        WHEN cm.total_balance > 100000 AND cm.total_loans > 0 THEN 'PLATINUM'
        WHEN cm.total_balance > 50000 OR cm.total_loan_amount > 100000 THEN 'GOLD'
        WHEN cm.total_balance > 10000 THEN 'SILVER'
        ELSE 'BRONZE'
    END as customer_segment,
    -- Churn Risk Score
    CASE
        WHEN tm.active_months < 3 AND cm.months_as_customer > 6 THEN 'HIGH'
        WHEN cm.active_accounts = 0 THEN 'HIGH'
        WHEN tm.total_transactions < 5 AND cm.months_as_customer > 3 THEN 'MEDIUM'
        ELSE 'LOW'
    END as churn_risk
FROM CustomerMetrics cm
LEFT JOIN TransactionMetrics tm ON cm.customer_id = tm.customer_id
ORDER BY current_clv DESC;
```

---

## Advanced Topics

### 21. Recursive CTE for Account Hierarchy

**Question:** Find all related accounts in a corporate structure.

```sql
WITH RECURSIVE AccountHierarchy AS (
    -- Anchor: Start with parent company accounts
    SELECT
        account_id,
        parent_account_id,
        account_name,
        account_type,
        0 as level,
        CAST(account_id AS VARCHAR(MAX)) as path
    FROM corporate_accounts
    WHERE parent_account_id IS NULL

    UNION ALL

    -- Recursive: Find all child accounts
    SELECT
        ca.account_id,
        ca.parent_account_id,
        ca.account_name,
        ca.account_type,
        ah.level + 1,
        ah.path + ' -> ' + CAST(ca.account_id AS VARCHAR(20))
    FROM corporate_accounts ca
    INNER JOIN AccountHierarchy ah ON ca.parent_account_id = ah.account_id
    WHERE ah.level < 10 -- Prevent infinite recursion
)
SELECT
    REPLICATE('  ', level) + account_name as hierarchical_name,
    account_type,
    level,
    path,
    (SELECT SUM(balance)
     FROM accounts
     WHERE corporate_account_id = ah.account_id) as account_balance,
    (SELECT COUNT(*)
     FROM corporate_accounts
     WHERE parent_account_id = ah.account_id) as child_count
FROM AccountHierarchy ah
ORDER BY path;
```

### 22. Pivot for Monthly Revenue Report

**Question:** Create a monthly revenue pivot report.

```sql
-- Dynamic pivot for monthly revenue by product
DECLARE @columns NVARCHAR(MAX), @sql NVARCHAR(MAX);

-- Get column list
SELECT @columns = COALESCE(@columns + ',', '') +
    QUOTENAME(FORMAT(transaction_date, 'yyyy-MM'))
FROM (
    SELECT DISTINCT
        DATEADD(MONTH, DATEDIFF(MONTH, 0, transaction_date), 0) as transaction_date
    FROM transactions
    WHERE transaction_date >= DATEADD(MONTH, -12, GETDATE())
) t
ORDER BY transaction_date;

-- Build dynamic SQL
SET @sql = N'
SELECT
    product_type,
    ' + @columns + ',
    total_revenue
FROM (
    SELECT
        p.product_type,
        FORMAT(t.transaction_date, ''yyyy-MM'') as month,
        t.amount,
        SUM(t.amount) OVER (PARTITION BY p.product_type) as total_revenue
    FROM transactions t
    INNER JOIN products p ON t.product_id = p.product_id
    WHERE t.transaction_type = ''FEE''
        AND t.transaction_date >= DATEADD(MONTH, -12, GETDATE())
) source
PIVOT (
    SUM(amount)
    FOR month IN (' + @columns + ')
) pivoted
ORDER BY total_revenue DESC';

EXEC sp_executesql @sql;
```

### 23. JSON Operations (SQL Server 2016+)

**Question:** Store and query transaction metadata in JSON format.

```sql
-- Create table with JSON column
CREATE TABLE transaction_metadata (
    transaction_id VARCHAR(50) PRIMARY KEY,
    metadata NVARCHAR(MAX) CHECK (ISJSON(metadata) = 1),
    created_at DATETIME2 DEFAULT GETDATE()
);

-- Insert JSON data
INSERT INTO transaction_metadata (transaction_id, metadata)
VALUES
    ('TXN001', N'{
        "device": {"type": "mobile", "os": "iOS", "version": "15.0"},
        "location": {"lat": 40.7128, "lon": -74.0060, "city": "New York"},
        "merchant": {"id": "M123", "name": "Store ABC", "category": "Retail"},
        "authentication": {"method": "biometric", "factor": 2},
        "riskScore": 0.15
    }'),
    ('TXN002', N'{
        "device": {"type": "web", "browser": "Chrome", "version": "96.0"},
        "location": {"lat": 34.0522, "lon": -118.2437, "city": "Los Angeles"},
        "merchant": {"id": "M456", "name": "Online Shop", "category": "E-commerce"},
        "authentication": {"method": "password", "factor": 1},
        "riskScore": 0.45
    }');

-- Query JSON data
SELECT
    transaction_id,
    JSON_VALUE(metadata, '$.device.type') as device_type,
    JSON_VALUE(metadata, '$.location.city') as city,
    JSON_VALUE(metadata, '$.merchant.name') as merchant_name,
    JSON_VALUE(metadata, '$.riskScore') as risk_score,
    JSON_QUERY(metadata, '$.authentication') as auth_details
FROM transaction_metadata
WHERE CAST(JSON_VALUE(metadata, '$.riskScore') AS FLOAT) > 0.3;

-- Update JSON property
UPDATE transaction_metadata
SET metadata = JSON_MODIFY(metadata, '$.riskScore', 0.75)
WHERE transaction_id = 'TXN002';

-- Extract all merchants from JSON
SELECT DISTINCT
    merchant.merchant_id,
    merchant.merchant_name,
    merchant.category
FROM transaction_metadata
CROSS APPLY OPENJSON(metadata, '$.merchant')
    WITH (
        merchant_id VARCHAR(10) '$.id',
        merchant_name VARCHAR(100) '$.name',
        category VARCHAR(50) '$.category'
    ) as merchant;
```

### 24. Temporal Tables (SQL Server 2016+)

**Question:** Implement audit trail using temporal tables.

```sql
-- Create temporal table for account history
CREATE TABLE accounts_temporal (
    account_id INT PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    customer_id INT NOT NULL,
    balance DECIMAL(15,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    modified_by VARCHAR(100) NOT NULL,
    -- Period columns for temporal feature
    valid_from DATETIME2 GENERATED ALWAYS AS ROW START NOT NULL,
    valid_to DATETIME2 GENERATED ALWAYS AS ROW END NOT NULL,
    PERIOD FOR SYSTEM_TIME (valid_from, valid_to)
)
WITH (SYSTEM_VERSIONING = ON (HISTORY_TABLE = dbo.accounts_history));

-- Query current data
SELECT * FROM accounts_temporal;

-- Query historical data at specific point in time
SELECT * FROM accounts_temporal
FOR SYSTEM_TIME AS OF '2024-01-15 10:00:00';

-- Query all historical changes for an account
SELECT
    account_id,
    balance,
    status,
    modified_by,
    valid_from,
    valid_to,
    CASE
        WHEN valid_to = '9999-12-31 23:59:59.9999999' THEN 'Current'
        ELSE 'Historical'
    END as record_status
FROM accounts_temporal
FOR SYSTEM_TIME ALL
WHERE account_id = 12345
ORDER BY valid_from DESC;

-- Find who made changes in specific time range
SELECT
    account_id,
    modified_by,
    COUNT(*) as change_count,
    MIN(valid_from) as first_change,
    MAX(valid_from) as last_change
FROM accounts_temporal
FOR SYSTEM_TIME BETWEEN '2024-01-01' AND '2024-01-31'
GROUP BY account_id, modified_by
HAVING COUNT(*) > 1
ORDER BY change_count DESC;
```

### 25. Common Table Expressions (CTEs) vs Subqueries vs Temp Tables

**Question:** When to use CTEs, subqueries, or temp tables?

```sql
-- 1. CTE - Best for readability and recursive queries
WITH CustomerSummary AS (
    SELECT
        customer_id,
        COUNT(*) as account_count,
        SUM(balance) as total_balance
    FROM accounts
    GROUP BY customer_id
),
HighValueCustomers AS (
    SELECT customer_id
    FROM CustomerSummary
    WHERE total_balance > 100000
)
SELECT c.*, cs.account_count, cs.total_balance
FROM customers c
INNER JOIN CustomerSummary cs ON c.customer_id = cs.customer_id
WHERE c.customer_id IN (SELECT customer_id FROM HighValueCustomers);

-- 2. Subquery - Good for simple, one-time use
SELECT
    c.*,
    (SELECT COUNT(*) FROM accounts a WHERE a.customer_id = c.customer_id) as account_count,
    (SELECT SUM(balance) FROM accounts a WHERE a.customer_id = c.customer_id) as total_balance
FROM customers c
WHERE EXISTS (
    SELECT 1
    FROM accounts a
    WHERE a.customer_id = c.customer_id
        AND a.balance > 100000
);

-- 3. Temp Table - Best for complex queries with reuse
CREATE TABLE #CustomerMetrics (
    customer_id INT PRIMARY KEY,
    account_count INT,
    total_balance DECIMAL(15,2),
    last_transaction_date DATE,
    INDEX IX_Balance (total_balance)
);

INSERT INTO #CustomerMetrics
SELECT
    c.customer_id,
    COUNT(DISTINCT a.account_id),
    SUM(a.balance),
    MAX(t.transaction_date)
FROM customers c
LEFT JOIN accounts a ON c.customer_id = a.customer_id
LEFT JOIN transactions t ON a.account_id = t.account_id
GROUP BY c.customer_id;

-- Can reuse temp table multiple times
SELECT * FROM #CustomerMetrics WHERE total_balance > 100000;
SELECT AVG(total_balance) FROM #CustomerMetrics;
SELECT COUNT(*) FROM #CustomerMetrics WHERE last_transaction_date < DATEADD(DAY, -30, GETDATE());

DROP TABLE #CustomerMetrics;

-- Performance comparison example
SET STATISTICS TIME ON;
SET STATISTICS IO ON;

-- Run each version and compare execution stats
```

---

## Interview Tips for SQL Questions

### Common Mistakes to Avoid:
1. **Forgetting NULL handling** - Always consider NULL values in WHERE clauses and JOINs
2. **Incorrect GROUP BY** - Include all non-aggregate columns in GROUP BY
3. **Using SELECT *** - Be specific about columns needed
4. **Not considering indexes** - Think about performance implications
5. **Ignoring transaction isolation** - Consider concurrent access scenarios

### Best Practices to Demonstrate:
1. **Use meaningful aliases** - Makes queries readable
2. **Format queries properly** - Shows attention to detail
3. **Comment complex logic** - Demonstrates communication skills
4. **Consider edge cases** - Shows thorough thinking
5. **Mention performance considerations** - Shows production experience

### When Asked to Optimize:
1. Check for missing indexes
2. Review JOIN order and types
3. Consider using EXISTS vs IN
4. Look for unnecessary DISTINCT
5. Evaluate if denormalization would help
6. Consider partitioning for large tables
7. Use appropriate isolation levels

### Banking-Specific Considerations:
1. **ACID compliance** - Transactions must be atomic
2. **Audit requirements** - Keep history of all changes
3. **Data consistency** - Double-entry bookkeeping
4. **Performance** - Sub-second response times
5. **Security** - Data encryption, access control
6. **Compliance** - Regulatory requirements (PCI, SOX)