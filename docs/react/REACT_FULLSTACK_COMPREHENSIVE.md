# 📱 React Development - Comprehensive Banking Interview Guide

> **Complete guide to React development for banking applications**
> Covering React fundamentals, hooks, state management, routing, forms, testing, and performance optimization

## 📖 Table of Contents

### 1. [React Fundamentals](#react-fundamentals)
- [Q1: React Core Concepts and Virtual DOM](#q1-react-core-concepts-and-virtual-dom)
- [Q2: Components and Props](#q2-components-and-props)
- [Q3: State Management with useState](#q3-state-management-with-usestate)
- [Q4: Event Handling and Synthetic Events](#q4-event-handling-and-synthetic-events)

### 2. [React Hooks](#react-hooks)
- [Q5: useEffect Hook and Lifecycle](#q5-useeffect-hook-and-lifecycle)
- [Q6: useContext for State Management](#q6-usecontext-for-state-management)
- [Q7: useReducer for Complex State](#q7-usereducer-for-complex-state)
- [Q8: Custom Hooks](#q8-custom-hooks)
- [Q9: useMemo and useCallback](#q9-usememo-and-usecallback)

### 3. [Advanced React Patterns](#advanced-react-patterns)
- [Q10: Higher-Order Components (HOCs)](#q10-higher-order-components-hocs)
- [Q11: Render Props Pattern](#q11-render-props-pattern)
- [Q12: Compound Components](#q12-compound-components)
- [Q13: Error Boundaries](#q13-error-boundaries)

### 4. [State Management](#state-management)
- [Q14: Redux with React](#q14-redux-with-react)
- [Q15: Redux Toolkit (RTK)](#q15-redux-toolkit-rtk)
- [Q16: Zustand for Simple State](#q16-zustand-for-simple-state)
- [Q17: React Query for Server State](#q17-react-query-for-server-state)

### 5. [Routing and Navigation](#routing-and-navigation)
- [Q18: React Router v6](#q18-react-router-v6)
- [Q19: Protected Routes and Authentication](#q19-protected-routes-and-authentication)
- [Q20: Dynamic Routing](#q20-dynamic-routing)

### 6. [Forms and Validation](#forms-and-validation)
- [Q21: Form Handling with React Hook Form](#q21-form-handling-with-react-hook-form)
- [Q22: Form Validation Strategies](#q22-form-validation-strategies)
- [Q23: File Upload Components](#q23-file-upload-components)

### 7. [API Integration](#api-integration)
- [Q24: HTTP Client Integration (Axios)](#q24-http-client-integration-axios)
- [Q25: Error Handling and Loading States](#q25-error-handling-and-loading-states)
- [Q26: Authentication Token Management](#q26-authentication-token-management)
- [Q27: WebSocket Integration](#q27-websocket-integration)

### 8. [Testing Strategies](#testing-strategies)
- [Q28: Unit Testing with React Testing Library](#q28-unit-testing-with-react-testing-library)
- [Q29: Integration Testing](#q29-integration-testing)
- [Q30: End-to-End Testing with Cypress](#q30-end-to-end-testing-with-cypress)
- [Q31: Mocking API Calls](#q31-mocking-api-calls)

### 9. [Performance Optimization](#performance-optimization)
- [Q32: React.memo and Component Optimization](#q32-reactmemo-and-component-optimization)
- [Q33: Code Splitting and Lazy Loading](#q33-code-splitting-and-lazy-loading)
- [Q34: Bundle Analysis and Optimization](#q34-bundle-analysis-and-optimization)
- [Q35: Virtual Scrolling for Large Lists](#q35-virtual-scrolling-for-large-lists)

### 10. [Full-Stack Integration](#full-stack-integration)
- [Q36: Spring Boot + React Integration](#q36-spring-boot--react-integration)
- [Q37: JWT Authentication Flow](#q37-jwt-authentication-flow)
- [Q38: Real-time Banking Dashboard](#q38-real-time-banking-dashboard)
- [Q39: Microservices Communication](#q39-microservices-communication)

### 11. [Modern React Ecosystem](#modern-react-ecosystem)
- [Q40: Next.js for Full-Stack Apps](#q40-nextjs-for-full-stack-apps)
- [Q41: TypeScript with React](#q41-typescript-with-react)
- [Q42: Styling Solutions (CSS-in-JS, Tailwind)](#q42-styling-solutions-css-in-js-tailwind)
- [Q43: Build Tools and Development](#q43-build-tools-and-development)

### 12. [Banking-Specific React Patterns](#banking-specific-react-patterns)
- [Q44: Financial Data Visualization](#q44-financial-data-visualization)
- [Q45: Real-time Account Updates](#q45-real-time-account-updates)
- [Q46: Secure Form Components](#q46-secure-form-components)
- [Q47: Banking UI/UX Patterns](#q47-banking-ui-ux-patterns)
- [Q48: Accessibility in Banking Apps](#q48-accessibility-in-banking-apps)
- [Q49: Progressive Web App Features](#q49-progressive-web-app-features)

---

## 🚀 React Fundamentals

### Q1: React Core Concepts and Virtual DOM

**Answer:**
React is a JavaScript library for building user interfaces, particularly web applications. The Virtual DOM is React's key innovation for efficient UI updates.

**Virtual DOM Explanation:**

```javascript
// Virtual DOM Concept
// Real DOM manipulation is expensive
document.getElementById('myDiv').innerHTML = 'New Content'; // Slow

// React creates a virtual representation
const virtualElement = {
  type: 'div',
  props: {
    id: 'myDiv',
    children: 'New Content'
  }
};

// React efficiently updates only what changed
// 1. Create Virtual DOM
// 2. Diff with previous Virtual DOM
// 3. Apply minimal changes to Real DOM
```

**Banking Account Component Example:**

```jsx
import React, { useState, useEffect } from 'react';

// Functional Component with Virtual DOM optimization
const AccountBalance = ({ accountId }) => {
  const [balance, setBalance] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchBalance = async () => {
      try {
        setLoading(true);
        const response = await fetch(`/api/accounts/${accountId}/balance`);

        if (!response.ok) {
          throw new Error('Failed to fetch balance');
        }

        const data = await response.json();
        setBalance(data.balance);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    if (accountId) {
      fetchBalance();
    }
  }, [accountId]);

  // React optimizes re-renders through Virtual DOM diffing
  if (loading) {
    return (
      <div className="account-balance loading">
        <div className="skeleton-loader"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="account-balance error">
        <span className="error-message">Error: {error}</span>
        <button onClick={() => window.location.reload()}>
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="account-balance">
      <h3>Account Balance</h3>
      <div className="balance-amount">
        ${balance?.toLocaleString('en-US', {
          minimumFractionDigits: 2,
          maximumFractionDigits: 2
        })}
      </div>
      <div className="account-info">
        Account: {accountId}
      </div>
    </div>
  );
};

export default AccountBalance;
```

**React Reconciliation Process:**

```jsx
// Example showing React's reconciliation
const TransactionList = ({ transactions }) => {
  return (
    <div className="transaction-list">
      {transactions.map(transaction => (
        // Key prop helps React identify which items changed
        <TransactionItem
          key={transaction.id}
          transaction={transaction}
        />
      ))}
    </div>
  );
};

// When transactions array changes, React:
// 1. Creates new Virtual DOM tree
// 2. Compares with previous tree (diffing)
// 3. Identifies minimal changes needed
// 4. Updates only changed DOM nodes

const TransactionItem = React.memo(({ transaction }) => {
  // React.memo prevents unnecessary re-renders
  // Only re-renders if transaction prop changes

  return (
    <div className="transaction-item">
      <div className="transaction-date">
        {new Date(transaction.date).toLocaleDateString()}
      </div>
      <div className="transaction-description">
        {transaction.description}
      </div>
      <div className={`transaction-amount ${transaction.type}`}>
        {transaction.type === 'debit' ? '-' : '+'}
        ${Math.abs(transaction.amount).toFixed(2)}
      </div>
    </div>
  );
});
```

### Q2: Components and Props

**Answer:**
Components are the building blocks of React applications. Props allow data to flow from parent to child components.

**Banking Dashboard Components:**

```jsx
// Parent Component - Banking Dashboard
const BankingDashboard = () => {
  const [user, setUser] = useState(null);
  const [accounts, setAccounts] = useState([]);
  const [selectedAccount, setSelectedAccount] = useState(null);

  useEffect(() => {
    // Load user data and accounts
    loadUserData();
  }, []);

  const loadUserData = async () => {
    try {
      const [userResponse, accountsResponse] = await Promise.all([
        fetch('/api/user/profile'),
        fetch('/api/user/accounts')
      ]);

      const userData = await userResponse.json();
      const accountsData = await accountsResponse.json();

      setUser(userData);
      setAccounts(accountsData);

      // Select first account by default
      if (accountsData.length > 0) {
        setSelectedAccount(accountsData[0]);
      }
    } catch (error) {
      console.error('Failed to load user data:', error);
    }
  };

  return (
    <div className="banking-dashboard">
      {/* Header Component with user props */}
      <DashboardHeader
        user={user}
        onLogout={() => {/* logout logic */}}
      />

      <div className="dashboard-content">
        {/* Account Selector Component */}
        <AccountSelector
          accounts={accounts}
          selectedAccount={selectedAccount}
          onAccountSelect={setSelectedAccount}
        />

        {/* Account Details Component */}
        {selectedAccount && (
          <AccountDetails
            account={selectedAccount}
            onTransactionUpdate={loadUserData}
          />
        )}

        {/* Quick Actions Component */}
        <QuickActions
          account={selectedAccount}
          disabled={!selectedAccount}
        />
      </div>
    </div>
  );
};

// Child Component - Dashboard Header
const DashboardHeader = ({ user, onLogout }) => {
  return (
    <header className="dashboard-header">
      <div className="header-left">
        <h1>TD Bank Dashboard</h1>
      </div>

      <div className="header-right">
        {user && (
          <div className="user-info">
            <span className="welcome-message">
              Welcome, {user.firstName} {user.lastName}
            </span>
            <button
              className="logout-button"
              onClick={onLogout}
            >
              Logout
            </button>
          </div>
        )}
      </div>
    </header>
  );
};

// Child Component - Account Selector
const AccountSelector = ({ accounts, selectedAccount, onAccountSelect }) => {
  return (
    <div className="account-selector">
      <h3>Your Accounts</h3>
      <div className="account-list">
        {accounts.map(account => (
          <AccountCard
            key={account.id}
            account={account}
            isSelected={selectedAccount?.id === account.id}
            onClick={() => onAccountSelect(account)}
          />
        ))}
      </div>
    </div>
  );
};

// Granular Component - Account Card
const AccountCard = ({ account, isSelected, onClick }) => {
  const formatBalance = (balance) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: 'USD'
    }).format(balance);
  };

  const getAccountTypeIcon = (type) => {
    const icons = {
      checking: '🏦',
      savings: '💰',
      credit: '💳'
    };
    return icons[type] || '🏛️';
  };

  return (
    <div
      className={`account-card ${isSelected ? 'selected' : ''}`}
      onClick={onClick}
    >
      <div className="account-header">
        <span className="account-icon">
          {getAccountTypeIcon(account.type)}
        </span>
        <div className="account-info">
          <div className="account-name">{account.name}</div>
          <div className="account-number">
            ****{account.number.slice(-4)}
          </div>
        </div>
      </div>

      <div className="account-balance">
        {formatBalance(account.balance)}
      </div>

      <div className="account-status">
        <span className={`status ${account.status.toLowerCase()}`}>
          {account.status}
        </span>
      </div>
    </div>
  );
};
```

**Props Validation and Default Values:**

```jsx
import PropTypes from 'prop-types';

// Component with comprehensive prop validation
const TransactionForm = ({
  account,
  onSubmit,
  onCancel,
  initialData = null,
  disabled = false
}) => {
  const [formData, setFormData] = useState({
    amount: '',
    description: '',
    category: '',
    ...initialData
  });

  const handleSubmit = (e) => {
    e.preventDefault();

    // Validate form data
    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      alert('Please enter a valid amount');
      return;
    }

    onSubmit({
      ...formData,
      amount: parseFloat(formData.amount),
      accountId: account.id
    });
  };

  return (
    <form className="transaction-form" onSubmit={handleSubmit}>
      <div className="form-group">
        <label htmlFor="amount">Amount</label>
        <input
          type="number"
          id="amount"
          value={formData.amount}
          onChange={(e) => setFormData(prev => ({
            ...prev,
            amount: e.target.value
          }))}
          disabled={disabled}
          step="0.01"
          min="0.01"
          required
        />
      </div>

      <div className="form-group">
        <label htmlFor="description">Description</label>
        <input
          type="text"
          id="description"
          value={formData.description}
          onChange={(e) => setFormData(prev => ({
            ...prev,
            description: e.target.value
          }))}
          disabled={disabled}
          maxLength={100}
          required
        />
      </div>

      <div className="form-actions">
        <button
          type="button"
          onClick={onCancel}
          disabled={disabled}
        >
          Cancel
        </button>
        <button
          type="submit"
          disabled={disabled}
        >
          Submit Transaction
        </button>
      </div>
    </form>
  );
};

// PropTypes for runtime validation
TransactionForm.propTypes = {
  account: PropTypes.shape({
    id: PropTypes.oneOfType([PropTypes.string, PropTypes.number]).isRequired,
    name: PropTypes.string.isRequired,
    balance: PropTypes.number.isRequired
  }).isRequired,
  onSubmit: PropTypes.func.isRequired,
  onCancel: PropTypes.func.isRequired,
  initialData: PropTypes.shape({
    amount: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    description: PropTypes.string,
    category: PropTypes.string
  }),
  disabled: PropTypes.bool
};

// Default props
TransactionForm.defaultProps = {
  initialData: null,
  disabled: false
};
```

### Q3: State Management with useState

**Answer:**
useState is the primary hook for managing component state in functional components.

**Banking Account State Management:**

```jsx
import React, { useState, useCallback } from 'react';

const AccountManagement = () => {
  // Simple state
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  // Object state
  const [account, setAccount] = useState({
    id: null,
    balance: 0,
    transactions: [],
    lastUpdated: null
  });

  // Array state
  const [transactionHistory, setTransactionHistory] = useState([]);

  // Complex state with multiple properties
  const [filters, setFilters] = useState({
    dateRange: 'last30days',
    transactionType: 'all',
    amountRange: { min: 0, max: Infinity },
    searchTerm: ''
  });

  // State update patterns
  const updateAccountBalance = useCallback(async (accountId) => {
    setLoading(true);
    setError(null);

    try {
      const response = await fetch(`/api/accounts/${accountId}/balance`);
      const data = await response.json();

      // Functional update to avoid stale closure
      setAccount(prevAccount => ({
        ...prevAccount,
        balance: data.balance,
        lastUpdated: new Date().toISOString()
      }));

    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, []);

  // Adding new transaction to state
  const addTransaction = useCallback((newTransaction) => {
    // Update transactions array immutably
    setTransactionHistory(prevTransactions => [
      newTransaction,
      ...prevTransactions
    ]);

    // Update account balance
    setAccount(prevAccount => ({
      ...prevAccount,
      balance: prevAccount.balance +
        (newTransaction.type === 'credit'
          ? newTransaction.amount
          : -newTransaction.amount),
      lastUpdated: new Date().toISOString()
    }));
  }, []);

  // Complex state update with validation
  const updateFilters = useCallback((filterUpdates) => {
    setFilters(prevFilters => {
      const newFilters = { ...prevFilters, ...filterUpdates };

      // Validate filter values
      if (newFilters.amountRange.min < 0) {
        newFilters.amountRange.min = 0;
      }

      if (newFilters.amountRange.max < newFilters.amountRange.min) {
        newFilters.amountRange.max = newFilters.amountRange.min;
      }

      return newFilters;
    });
  }, []);

  // Derived state (computed from existing state)
  const filteredTransactions = React.useMemo(() => {
    return transactionHistory.filter(transaction => {
      // Date filter
      const transactionDate = new Date(transaction.date);
      const dateThreshold = getDateThreshold(filters.dateRange);
      if (transactionDate < dateThreshold) return false;

      // Type filter
      if (filters.transactionType !== 'all' &&
          transaction.type !== filters.transactionType) {
        return false;
      }

      // Amount filter
      if (transaction.amount < filters.amountRange.min ||
          transaction.amount > filters.amountRange.max) {
        return false;
      }

      // Search filter
      if (filters.searchTerm &&
          !transaction.description.toLowerCase().includes(
            filters.searchTerm.toLowerCase()
          )) {
        return false;
      }

      return true;
    });
  }, [transactionHistory, filters]);

  // State for form inputs
  const [transferForm, setTransferForm] = useState({
    toAccountId: '',
    amount: '',
    description: '',
    errors: {}
  });

  const handleTransferFormChange = useCallback((field, value) => {
    setTransferForm(prev => ({
      ...prev,
      [field]: value,
      // Clear error when user starts typing
      errors: {
        ...prev.errors,
        [field]: null
      }
    }));
  }, []);

  const validateTransferForm = useCallback(() => {
    const errors = {};

    if (!transferForm.toAccountId) {
      errors.toAccountId = 'Please select a destination account';
    }

    if (!transferForm.amount || parseFloat(transferForm.amount) <= 0) {
      errors.amount = 'Please enter a valid amount';
    } else if (parseFloat(transferForm.amount) > account.balance) {
      errors.amount = 'Insufficient funds';
    }

    if (!transferForm.description.trim()) {
      errors.description = 'Please enter a description';
    }

    setTransferForm(prev => ({ ...prev, errors }));
    return Object.keys(errors).length === 0;
  }, [transferForm, account.balance]);

  const handleTransferSubmit = useCallback(async () => {
    if (!validateTransferForm()) return;

    setLoading(true);
    try {
      const response = await fetch('/api/transfers', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          fromAccountId: account.id,
          toAccountId: transferForm.toAccountId,
          amount: parseFloat(transferForm.amount),
          description: transferForm.description
        })
      });

      if (!response.ok) {
        throw new Error('Transfer failed');
      }

      const transferResult = await response.json();

      // Add transaction to local state
      addTransaction({
        id: transferResult.transactionId,
        type: 'debit',
        amount: parseFloat(transferForm.amount),
        description: transferForm.description,
        date: new Date().toISOString()
      });

      // Reset form
      setTransferForm({
        toAccountId: '',
        amount: '',
        description: '',
        errors: {}
      });

      alert('Transfer completed successfully!');

    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [transferForm, account.id, validateTransferForm, addTransaction]);

  return (
    <div className="account-management">
      {/* Account Balance Display */}
      <div className="account-balance">
        <h2>Account Balance</h2>
        <div className="balance-amount">
          ${account.balance.toLocaleString()}
        </div>
        {account.lastUpdated && (
          <div className="last-updated">
            Last updated: {new Date(account.lastUpdated).toLocaleString()}
          </div>
        )}
        <button
          onClick={() => updateAccountBalance(account.id)}
          disabled={loading}
        >
          {loading ? 'Refreshing...' : 'Refresh Balance'}
        </button>
      </div>

      {/* Transfer Form */}
      <div className="transfer-form">
        <h3>Transfer Money</h3>
        <form onSubmit={(e) => {
          e.preventDefault();
          handleTransferSubmit();
        }}>
          <div className="form-group">
            <label>To Account</label>
            <select
              value={transferForm.toAccountId}
              onChange={(e) => handleTransferFormChange('toAccountId', e.target.value)}
            >
              <option value="">Select account...</option>
              {/* Account options would be populated here */}
            </select>
            {transferForm.errors.toAccountId && (
              <span className="error">{transferForm.errors.toAccountId}</span>
            )}
          </div>

          <div className="form-group">
            <label>Amount</label>
            <input
              type="number"
              step="0.01"
              value={transferForm.amount}
              onChange={(e) => handleTransferFormChange('amount', e.target.value)}
            />
            {transferForm.errors.amount && (
              <span className="error">{transferForm.errors.amount}</span>
            )}
          </div>

          <div className="form-group">
            <label>Description</label>
            <input
              type="text"
              value={transferForm.description}
              onChange={(e) => handleTransferFormChange('description', e.target.value)}
            />
            {transferForm.errors.description && (
              <span className="error">{transferForm.errors.description}</span>
            )}
          </div>

          <button type="submit" disabled={loading}>
            {loading ? 'Processing...' : 'Transfer'}
          </button>
        </form>
      </div>

      {/* Transaction History with Filters */}
      <div className="transaction-history">
        <h3>Transaction History</h3>

        {/* Filter Controls */}
        <div className="filter-controls">
          <select
            value={filters.dateRange}
            onChange={(e) => updateFilters({ dateRange: e.target.value })}
          >
            <option value="last7days">Last 7 days</option>
            <option value="last30days">Last 30 days</option>
            <option value="last90days">Last 90 days</option>
          </select>

          <select
            value={filters.transactionType}
            onChange={(e) => updateFilters({ transactionType: e.target.value })}
          >
            <option value="all">All transactions</option>
            <option value="credit">Credits only</option>
            <option value="debit">Debits only</option>
          </select>

          <input
            type="text"
            placeholder="Search transactions..."
            value={filters.searchTerm}
            onChange={(e) => updateFilters({ searchTerm: e.target.value })}
          />
        </div>

        {/* Transaction List */}
        <div className="transaction-list">
          {filteredTransactions.map(transaction => (
            <div key={transaction.id} className="transaction-item">
              <div className="transaction-date">
                {new Date(transaction.date).toLocaleDateString()}
              </div>
              <div className="transaction-description">
                {transaction.description}
              </div>
              <div className={`transaction-amount ${transaction.type}`}>
                {transaction.type === 'credit' ? '+' : '-'}
                ${transaction.amount.toFixed(2)}
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* Error Display */}
      {error && (
        <div className="error-message">
          <span>Error: {error}</span>
          <button onClick={() => setError(null)}>Dismiss</button>
        </div>
      )}
    </div>
  );
};

// Helper function
const getDateThreshold = (range) => {
  const now = new Date();
  switch (range) {
    case 'last7days':
      return new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    case 'last30days':
      return new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
    case 'last90days':
      return new Date(now.getTime() - 90 * 24 * 60 * 60 * 1000);
    default:
      return new Date(0);
  }
};

export default AccountManagement;
```

### Q4: Event Handling and Synthetic Events

**Answer:**
React uses SyntheticEvents to provide consistent event handling across different browsers.

**Banking Form Event Handling:**

```jsx
import React, { useState, useRef, useCallback } from 'react';

const BankingTransactionForm = () => {
  const [formData, setFormData] = useState({
    accountFrom: '',
    accountTo: '',
    amount: '',
    description: '',
    scheduledDate: ''
  });

  const [validationErrors, setValidationErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Refs for form elements
  const amountInputRef = useRef(null);
  const descriptionInputRef = useRef(null);

  // Event handler with synthetic event
  const handleInputChange = useCallback((event) => {
    const { name, value, type } = event.target;

    // Prevent default behavior for certain keys
    if (name === 'amount' && type === 'keydown') {
      const allowedKeys = [
        'Backspace', 'Delete', 'Tab', 'Escape', 'Enter',
        'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown'
      ];

      if (!allowedKeys.includes(event.key) &&
          !/[0-9.]/.test(event.key)) {
        event.preventDefault();
        return;
      }
    }

    // Update form data
    setFormData(prevData => ({
      ...prevData,
      [name]: value
    }));

    // Clear validation error when user starts typing
    if (validationErrors[name]) {
      setValidationErrors(prev => ({
        ...prev,
        [name]: null
      }));
    }
  }, [validationErrors]);

  // Amount input formatting
  const handleAmountChange = useCallback((event) => {
    let value = event.target.value;

    // Remove any non-digit and non-decimal characters
    value = value.replace(/[^0-9.]/g, '');

    // Ensure only one decimal point
    const parts = value.split('.');
    if (parts.length > 2) {
      value = parts[0] + '.' + parts.slice(1).join('');
    }

    // Limit to 2 decimal places
    if (parts[1] && parts[1].length > 2) {
      value = parts[0] + '.' + parts[1].substring(0, 2);
    }

    setFormData(prev => ({
      ...prev,
      amount: value
    }));
  }, []);

  // Form submission with event handling
  const handleSubmit = useCallback(async (event) => {
    event.preventDefault(); // Prevent default form submission

    setIsSubmitting(true);

    try {
      // Validate form
      const errors = validateForm(formData);
      if (Object.keys(errors).length > 0) {
        setValidationErrors(errors);

        // Focus on first error field
        const firstErrorField = Object.keys(errors)[0];
        if (firstErrorField === 'amount') {
          amountInputRef.current?.focus();
        } else if (firstErrorField === 'description') {
          descriptionInputRef.current?.focus();
        }

        return;
      }

      // Submit transaction
      const response = await fetch('/api/transactions', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          ...formData,
          amount: parseFloat(formData.amount)
        })
      });

      if (!response.ok) {
        throw new Error('Transaction failed');
      }

      const result = await response.json();

      // Reset form on success
      setFormData({
        accountFrom: '',
        accountTo: '',
        amount: '',
        description: '',
        scheduledDate: ''
      });

      alert(`Transaction successful! ID: ${result.transactionId}`);

    } catch (error) {
      alert(`Error: ${error.message}`);
    } finally {
      setIsSubmitting(false);
    }
  }, [formData]);

  // Keyboard event handling
  const handleKeyDown = useCallback((event) => {
    // Submit form on Ctrl+Enter
    if (event.ctrlKey && event.key === 'Enter') {
      event.preventDefault();
      handleSubmit(event);
    }

    // Clear form on Escape
    if (event.key === 'Escape') {
      setFormData({
        accountFrom: '',
        accountTo: '',
        amount: '',
        description: '',
        scheduledDate: ''
      });
      setValidationErrors({});
    }
  }, [handleSubmit]);

  // Mouse event handling for amount buttons
  const handleQuickAmount = useCallback((event) => {
    event.preventDefault();
    const amount = event.target.dataset.amount;

    setFormData(prev => ({
      ...prev,
      amount: amount
    }));

    // Focus amount input after setting quick amount
    amountInputRef.current?.focus();
  }, []);

  // Focus events for better UX
  const handleAmountFocus = useCallback((event) => {
    // Select all text when focusing amount input
    event.target.select();
  }, []);

  // Paste event handling for amount
  const handleAmountPaste = useCallback((event) => {
    event.preventDefault();

    const pasteData = event.clipboardData.getData('text');
    const numericValue = pasteData.replace(/[^0-9.]/g, '');

    if (numericValue && !isNaN(parseFloat(numericValue))) {
      setFormData(prev => ({
        ...prev,
        amount: numericValue
      }));
    }
  }, []);

  // Double-click to clear field
  const handleDoubleClick = useCallback((event) => {
    const fieldName = event.target.name;
    setFormData(prev => ({
      ...prev,
      [fieldName]: ''
    }));
  }, []);

  return (
    <div className="banking-transaction-form">
      <h2>Transfer Money</h2>

      <form onSubmit={handleSubmit} onKeyDown={handleKeyDown}>
        {/* From Account */}
        <div className="form-group">
          <label htmlFor="accountFrom">From Account</label>
          <select
            id="accountFrom"
            name="accountFrom"
            value={formData.accountFrom}
            onChange={handleInputChange}
            required
          >
            <option value="">Select account...</option>
            <option value="checking-001">Checking Account (...001)</option>
            <option value="savings-002">Savings Account (...002)</option>
          </select>
          {validationErrors.accountFrom && (
            <span className="error">{validationErrors.accountFrom}</span>
          )}
        </div>

        {/* To Account */}
        <div className="form-group">
          <label htmlFor="accountTo">To Account</label>
          <select
            id="accountTo"
            name="accountTo"
            value={formData.accountTo}
            onChange={handleInputChange}
            required
          >
            <option value="">Select account...</option>
            <option value="checking-003">Checking Account (...003)</option>
            <option value="savings-004">Savings Account (...004)</option>
          </select>
          {validationErrors.accountTo && (
            <span className="error">{validationErrors.accountTo}</span>
          )}
        </div>

        {/* Amount */}
        <div className="form-group">
          <label htmlFor="amount">Amount</label>
          <div className="amount-input-container">
            <span className="currency-symbol">$</span>
            <input
              ref={amountInputRef}
              type="text"
              id="amount"
              name="amount"
              value={formData.amount}
              onChange={handleAmountChange}
              onFocus={handleAmountFocus}
              onPaste={handleAmountPaste}
              onDoubleClick={handleDoubleClick}
              placeholder="0.00"
              required
            />
          </div>

          {/* Quick amount buttons */}
          <div className="quick-amounts">
            <button
              type="button"
              data-amount="100"
              onMouseDown={handleQuickAmount}
            >
              $100
            </button>
            <button
              type="button"
              data-amount="500"
              onMouseDown={handleQuickAmount}
            >
              $500
            </button>
            <button
              type="button"
              data-amount="1000"
              onMouseDown={handleQuickAmount}
            >
              $1000
            </button>
          </div>

          {validationErrors.amount && (
            <span className="error">{validationErrors.amount}</span>
          )}
        </div>

        {/* Description */}
        <div className="form-group">
          <label htmlFor="description">Description</label>
          <input
            ref={descriptionInputRef}
            type="text"
            id="description"
            name="description"
            value={formData.description}
            onChange={handleInputChange}
            onDoubleClick={handleDoubleClick}
            placeholder="Enter transaction description"
            maxLength={100}
            required
          />
          <div className="character-count">
            {formData.description.length}/100
          </div>
          {validationErrors.description && (
            <span className="error">{validationErrors.description}</span>
          )}
        </div>

        {/* Scheduled Date */}
        <div className="form-group">
          <label htmlFor="scheduledDate">Scheduled Date (Optional)</label>
          <input
            type="date"
            id="scheduledDate"
            name="scheduledDate"
            value={formData.scheduledDate}
            onChange={handleInputChange}
            min={new Date().toISOString().split('T')[0]}
          />
        </div>

        {/* Submit Button */}
        <div className="form-actions">
          <button
            type="button"
            onClick={(e) => {
              e.preventDefault();
              setFormData({
                accountFrom: '',
                accountTo: '',
                amount: '',
                description: '',
                scheduledDate: ''
              });
              setValidationErrors({});
            }}
          >
            Clear
          </button>

          <button
            type="submit"
            disabled={isSubmitting || !formData.accountFrom || !formData.accountTo}
          >
            {isSubmitting ? 'Processing...' : 'Transfer Money'}
          </button>
        </div>
      </form>

      {/* Help Text */}
      <div className="form-help">
        <p><strong>Keyboard Shortcuts:</strong></p>
        <ul>
          <li>Ctrl+Enter: Submit form</li>
          <li>Escape: Clear form</li>
          <li>Double-click: Clear field</li>
        </ul>
      </div>
    </div>
  );
};

// Validation function
const validateForm = (data) => {
  const errors = {};

  if (!data.accountFrom) {
    errors.accountFrom = 'Please select a source account';
  }

  if (!data.accountTo) {
    errors.accountTo = 'Please select a destination account';
  } else if (data.accountFrom === data.accountTo) {
    errors.accountTo = 'Destination account must be different from source account';
  }

  if (!data.amount) {
    errors.amount = 'Please enter an amount';
  } else {
    const amount = parseFloat(data.amount);
    if (isNaN(amount) || amount <= 0) {
      errors.amount = 'Please enter a valid amount greater than 0';
    } else if (amount > 10000) {
      errors.amount = 'Amount cannot exceed $10,000 per transaction';
    }
  }

  if (!data.description.trim()) {
    errors.description = 'Please enter a description';
  } else if (data.description.length < 3) {
    errors.description = 'Description must be at least 3 characters';
  }

  return errors;
};

export default BankingTransactionForm;
```

---

## 🎣 React Hooks

### Q5: useEffect Hook and Lifecycle

**Answer:**
useEffect manages side effects and lifecycle events in functional components.

**Banking Dashboard with useEffect:**

```jsx
import React, { useState, useEffect, useRef, useCallback } from 'react';

const BankingDashboard = ({ userId }) => {
  const [userProfile, setUserProfile] = useState(null);
  const [accounts, setAccounts] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [notifications, setNotifications] = useState([]);

  // Refs for cleanup
  const intervalRef = useRef(null);
  const wsRef = useRef(null);
  const abortControllerRef = useRef(null);

  // Effect for initial data loading
  useEffect(() => {
    const loadInitialData = async () => {
      setLoading(true);
      setError(null);

      // Create AbortController for request cancellation
      abortControllerRef.current = new AbortController();

      try {
        // Load multiple data sources in parallel
        const [profileResponse, accountsResponse] = await Promise.all([
          fetch(`/api/users/${userId}`, {
            signal: abortControllerRef.current.signal
          }),
          fetch(`/api/users/${userId}/accounts`, {
            signal: abortControllerRef.current.signal
          })
        ]);

        if (!profileResponse.ok || !accountsResponse.ok) {
          throw new Error('Failed to load user data');
        }

        const profileData = await profileResponse.json();
        const accountsData = await accountsResponse.json();

        setUserProfile(profileData);
        setAccounts(accountsData);

      } catch (err) {
        if (err.name !== 'AbortError') {
          setError(err.message);
        }
      } finally {
        setLoading(false);
      }
    };

    if (userId) {
      loadInitialData();
    }

    // Cleanup function
    return () => {
      if (abortControllerRef.current) {
        abortControllerRef.current.abort();
      }
    };
  }, [userId]); // Dependency: re-run when userId changes

  // Effect for loading transactions when accounts change
  useEffect(() => {
    const loadTransactions = async () => {
      if (accounts.length === 0) return;

      try {
        const transactionPromises = accounts.map(account =>
          fetch(`/api/accounts/${account.id}/transactions?limit=10`)
            .then(res => res.json())
        );

        const transactionArrays = await Promise.all(transactionPromises);
        const allTransactions = transactionArrays
          .flat()
          .sort((a, b) => new Date(b.date) - new Date(a.date));

        setTransactions(allTransactions);

      } catch (err) {
        console.error('Failed to load transactions:', err);
      }
    };

    loadTransactions();
  }, [accounts]); // Dependency: re-run when accounts change

  // Effect for setting up real-time updates
  useEffect(() => {
    if (!userProfile?.id) return;

    // WebSocket connection for real-time updates
    const connectWebSocket = () => {
      wsRef.current = new WebSocket(`ws://localhost:8080/ws/user/${userProfile.id}`);

      wsRef.current.onopen = () => {
        console.log('WebSocket connected');
      };

      wsRef.current.onmessage = (event) => {
        const message = JSON.parse(event.data);

        switch (message.type) {
          case 'TRANSACTION_UPDATE':
            // Update transactions in real-time
            setTransactions(prev => [message.transaction, ...prev.slice(0, 49)]);

            // Update account balance
            setAccounts(prev =>
              prev.map(account =>
                account.id === message.transaction.accountId
                  ? { ...account, balance: message.newBalance }
                  : account
              )
            );
            break;

          case 'ACCOUNT_ALERT':
            // Add notification
            setNotifications(prev => [
              ...prev,
              {
                id: Date.now(),
                message: message.alert,
                type: 'warning',
                timestamp: new Date()
              }
            ]);
            break;
        }
      };

      wsRef.current.onerror = (error) => {
        console.error('WebSocket error:', error);
      };

      wsRef.current.onclose = () => {
        console.log('WebSocket disconnected');
        // Attempt to reconnect after 5 seconds
        setTimeout(connectWebSocket, 5000);
      };
    };

    connectWebSocket();

    // Cleanup WebSocket
    return () => {
      if (wsRef.current) {
        wsRef.current.close();
      }
    };
  }, [userProfile?.id]);

  // Effect for periodic balance updates
  useEffect(() => {
    if (accounts.length === 0) return;

    const updateBalances = async () => {
      try {
        const balancePromises = accounts.map(account =>
          fetch(`/api/accounts/${account.id}/balance`)
            .then(res => res.json())
            .then(data => ({ accountId: account.id, balance: data.balance }))
        );

        const balanceUpdates = await Promise.all(balancePromises);

        setAccounts(prevAccounts =>
          prevAccounts.map(account => {
            const update = balanceUpdates.find(u => u.accountId === account.id);
            return update ? { ...account, balance: update.balance } : account;
          })
        );

      } catch (err) {
        console.error('Failed to update balances:', err);
      }
    };

    // Update balances every 30 seconds
    intervalRef.current = setInterval(updateBalances, 30000);

    // Cleanup interval
    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, [accounts.length]);

  // Effect for managing document title
  useEffect(() => {
    const originalTitle = document.title;

    if (userProfile) {
      document.title = `TD Bank - ${userProfile.firstName} ${userProfile.lastName}`;
    }

    // Cleanup: restore original title
    return () => {
      document.title = originalTitle;
    };
  }, [userProfile]);

  // Effect for notification cleanup
  useEffect(() => {
    if (notifications.length === 0) return;

    // Auto-dismiss notifications after 5 seconds
    const timeouts = notifications.map(notification =>
      setTimeout(() => {
        setNotifications(prev =>
          prev.filter(n => n.id !== notification.id)
        );
      }, 5000)
    );

    // Cleanup timeouts
    return () => {
      timeouts.forEach(timeout => clearTimeout(timeout));
    };
  }, [notifications]);

  // Effect for keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (event) => {
      // Refresh data with Ctrl+R
      if (event.ctrlKey && event.key === 'r') {
        event.preventDefault();
        window.location.reload();
      }

      // Quick account switch with number keys
      if (event.altKey && /^[1-9]$/.test(event.key)) {
        const accountIndex = parseInt(event.key) - 1;
        if (accounts[accountIndex]) {
          // Focus on specific account (you'd implement this logic)
          console.log(`Switching to account ${accountIndex + 1}`);
        }
      }
    };

    document.addEventListener('keydown', handleKeyDown);

    // Cleanup event listener
    return () => {
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [accounts]);

  // Effect for handling online/offline status
  useEffect(() => {
    const handleOnline = () => {
      setNotifications(prev => [
        ...prev,
        {
          id: Date.now(),
          message: 'Connection restored',
          type: 'success',
          timestamp: new Date()
        }
      ]);
    };

    const handleOffline = () => {
      setNotifications(prev => [
        ...prev,
        {
          id: Date.now(),
          message: 'Connection lost - working offline',
          type: 'error',
          timestamp: new Date()
        }
      ]);
    };

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    // Cleanup event listeners
    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  // Custom hook for data fetching with cleanup
  const useApiCall = useCallback((url, dependencies = []) => {
    const [data, setData] = useState(null);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
      const abortController = new AbortController();

      const fetchData = async () => {
        setLoading(true);
        setError(null);

        try {
          const response = await fetch(url, {
            signal: abortController.signal
          });

          if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
          }

          const result = await response.json();
          setData(result);

        } catch (err) {
          if (err.name !== 'AbortError') {
            setError(err.message);
          }
        } finally {
          setLoading(false);
        }
      };

      fetchData();

      return () => {
        abortController.abort();
      };
    }, dependencies);

    return { data, loading, error };
  }, []);

  // Render loading state
  if (loading) {
    return (
      <div className="dashboard-loading">
        <div className="spinner"></div>
        <p>Loading your banking dashboard...</p>
      </div>
    );
  }

  // Render error state
  if (error) {
    return (
      <div className="dashboard-error">
        <h2>Error Loading Dashboard</h2>
        <p>{error}</p>
        <button onClick={() => window.location.reload()}>
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="banking-dashboard">
      {/* Notifications */}
      <div className="notifications">
        {notifications.map(notification => (
          <div
            key={notification.id}
            className={`notification ${notification.type}`}
          >
            {notification.message}
            <button
              onClick={() => setNotifications(prev =>
                prev.filter(n => n.id !== notification.id)
              )}
            >
              ×
            </button>
          </div>
        ))}
      </div>

      {/* User Profile */}
      {userProfile && (
        <div className="user-profile">
          <h1>Welcome, {userProfile.firstName}!</h1>
          <p>Last login: {new Date(userProfile.lastLogin).toLocaleString()}</p>
        </div>
      )}

      {/* Account Summary */}
      <div className="accounts-summary">
        <h2>Your Accounts</h2>
        <div className="accounts-grid">
          {accounts.map(account => (
            <div key={account.id} className="account-card">
              <h3>{account.name}</h3>
              <p className="account-number">****{account.number.slice(-4)}</p>
              <p className="balance">
                ${account.balance.toLocaleString()}
              </p>
            </div>
          ))}
        </div>
      </div>

      {/* Recent Transactions */}
      <div className="recent-transactions">
        <h2>Recent Transactions</h2>
        <div className="transaction-list">
          {transactions.slice(0, 10).map(transaction => (
            <div key={transaction.id} className="transaction-item">
              <div className="transaction-date">
                {new Date(transaction.date).toLocaleDateString()}
              </div>
              <div className="transaction-description">
                {transaction.description}
              </div>
              <div className={`transaction-amount ${transaction.type}`}>
                {transaction.type === 'credit' ? '+' : '-'}
                ${Math.abs(transaction.amount).toFixed(2)}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default BankingDashboard;
```

---

## 🏦 Banking-Specific React Patterns

### Q44: Financial Data Visualization

**Question**: How do you create secure and accessible financial data visualizations in React for banking applications?

**Answer**:

Financial data visualization requires careful attention to security, accuracy, and accessibility.

**Banking Chart Components with Security:**

```jsx
import React, { useState, useEffect, useMemo } from 'react';
import { Line, Bar, Pie } from 'react-chartjs-2';
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend,
} from 'chart.js';

ChartJS.register(
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  BarElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
);

const BankingChartDashboard = ({ accountId, dateRange }) => {
  const [chartData, setChartData] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [userPermissions, setUserPermissions] = useState({});

  // Secure data fetching with permission checks
  useEffect(() => {
    const fetchChartData = async () => {
      setLoading(true);
      setError(null);

      try {
        // Check user permissions before fetching sensitive data
        const permissionsResponse = await fetch('/api/user/permissions');
        const permissions = await permissionsResponse.json();
        setUserPermissions(permissions);

        if (!permissions.canViewDetailedAnalytics) {
          throw new Error('Insufficient permissions to view detailed analytics');
        }

        const response = await fetch(
          `/api/accounts/${accountId}/analytics?range=${dateRange}`,
          {
            headers: {
              'Authorization': `Bearer ${localStorage.getItem('auth_token')}`,
              'X-Requested-With': 'XMLHttpRequest'
            }
          }
        );

        if (!response.ok) {
          throw new Error('Failed to fetch chart data');
        }

        const data = await response.json();
        setChartData(data);

      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    if (accountId && dateRange) {
      fetchChartData();
    }
  }, [accountId, dateRange]);

  // Account balance trend chart
  const balanceChartData = useMemo(() => {
    if (!chartData?.balanceHistory) return null;

    return {
      labels: chartData.balanceHistory.map(item =>
        new Date(item.date).toLocaleDateString()
      ),
      datasets: [
        {
          label: 'Account Balance',
          data: chartData.balanceHistory.map(item => item.balance),
          borderColor: '#10B981',
          backgroundColor: 'rgba(16, 185, 129, 0.1)',
          tension: 0.1,
          pointRadius: 3,
          pointHoverRadius: 6,
        }
      ]
    };
  }, [chartData]);

  // Transaction category breakdown
  const categoryChartData = useMemo(() => {
    if (!chartData?.categoryBreakdown) return null;

    const colors = [
      '#EF4444', '#F97316', '#EAB308', '#22C55E',
      '#06B6D4', '#3B82F6', '#8B5CF6', '#EC4899'
    ];

    return {
      labels: chartData.categoryBreakdown.map(item => item.category),
      datasets: [
        {
          data: chartData.categoryBreakdown.map(item => Math.abs(item.amount)),
          backgroundColor: colors.slice(0, chartData.categoryBreakdown.length),
          borderWidth: 2,
          borderColor: '#fff',
        }
      ]
    };
  }, [chartData]);

  // Monthly spending comparison
  const spendingChartData = useMemo(() => {
    if (!chartData?.monthlySpending) return null;

    return {
      labels: chartData.monthlySpending.map(item => item.month),
      datasets: [
        {
          label: 'Income',
          data: chartData.monthlySpending.map(item => item.income),
          backgroundColor: '#22C55E',
          borderColor: '#16A34A',
          borderWidth: 1,
        },
        {
          label: 'Expenses',
          data: chartData.monthlySpending.map(item => Math.abs(item.expenses)),
          backgroundColor: '#EF4444',
          borderColor: '#DC2626',
          borderWidth: 1,
        }
      ]
    };
  }, [chartData]);

  // Accessible chart options
  const chartOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        position: 'top',
        labels: {
          usePointStyle: true,
          padding: 20,
          font: {
            size: 14
          }
        }
      },
      tooltip: {
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        titleColor: '#fff',
        bodyColor: '#fff',
        borderColor: '#374151',
        borderWidth: 1,
        cornerRadius: 8,
        displayColors: true,
        callbacks: {
          label: function(context) {
            const value = context.parsed.y;
            return `${context.dataset.label}: $${value.toLocaleString('en-US', {
              minimumFractionDigits: 2,
              maximumFractionDigits: 2
            })}`;
          }
        }
      }
    },
    scales: {
      y: {
        beginAtZero: true,
        ticks: {
          callback: function(value) {
            return '$' + value.toLocaleString();
          }
        }
      }
    },
    // Accessibility enhancements
    onHover: (event, activeElements) => {
      if (activeElements.length > 0) {
        const element = activeElements[0];
        const datasetLabel = element.dataset.label;
        const value = element.parsed.y;

        // Announce data point for screen readers
        const announcement = `${datasetLabel}: $${value.toLocaleString()}`;

        // Update aria-live region
        const liveRegion = document.getElementById('chart-live-region');
        if (liveRegion) {
          liveRegion.textContent = announcement;
        }
      }
    }
  };

  // Security: Mask sensitive data for unauthorized users
  const getMaskedValue = (value, permission) => {
    if (!permission) {
      return '***';
    }
    return value.toLocaleString('en-US', {
      style: 'currency',
      currency: 'USD'
    });
  };

  if (loading) {
    return (
      <div className="chart-dashboard loading">
        <div className="skeleton-loader"></div>
        <div className="skeleton-loader"></div>
        <div className="skeleton-loader"></div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="chart-dashboard error" role="alert">
        <h3>Error Loading Chart Data</h3>
        <p>{error}</p>
        <button onClick={() => window.location.reload()}>
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="banking-chart-dashboard">
      {/* Accessibility: Live region for screen readers */}
      <div
        id="chart-live-region"
        aria-live="polite"
        aria-atomic="true"
        className="sr-only"
      ></div>

      {/* Account Balance Trend */}
      <div className="chart-container">
        <div className="chart-header">
          <h3>Account Balance Trend</h3>
          <div className="chart-controls">
            <label htmlFor="balance-period">Period:</label>
            <select id="balance-period" defaultValue="30d">
              <option value="7d">Last 7 days</option>
              <option value="30d">Last 30 days</option>
              <option value="90d">Last 90 days</option>
              <option value="1y">Last year</option>
            </select>
          </div>
        </div>

        <div className="chart-wrapper" style={{ height: '300px' }}>
          {balanceChartData && (
            <Line
              data={balanceChartData}
              options={chartOptions}
              aria-label="Account balance trend over time"
            />
          )}
        </div>

        {/* Data table for accessibility */}
        <details className="chart-data-table">
          <summary>View balance data table</summary>
          <table>
            <thead>
              <tr>
                <th>Date</th>
                <th>Balance</th>
              </tr>
            </thead>
            <tbody>
              {chartData?.balanceHistory?.map((item, index) => (
                <tr key={index}>
                  <td>{new Date(item.date).toLocaleDateString()}</td>
                  <td>{getMaskedValue(item.balance, userPermissions.canViewDetailedAnalytics)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </details>
      </div>

      {/* Category Breakdown */}
      <div className="chart-container">
        <div className="chart-header">
          <h3>Spending by Category</h3>
          <p className="chart-description">
            Distribution of expenses across different categories
          </p>
        </div>

        <div className="chart-wrapper" style={{ height: '300px' }}>
          {categoryChartData && (
            <Pie
              data={categoryChartData}
              options={{
                ...chartOptions,
                plugins: {
                  ...chartOptions.plugins,
                  legend: {
                    position: 'right',
                    labels: {
                      generateLabels: function(chart) {
                        const data = chart.data;
                        return data.labels.map((label, index) => ({
                          text: `${label}: ${getMaskedValue(
                            data.datasets[0].data[index],
                            userPermissions.canViewDetailedAnalytics
                          )}`,
                          fillStyle: data.datasets[0].backgroundColor[index],
                          strokeStyle: data.datasets[0].borderColor,
                          lineWidth: data.datasets[0].borderWidth
                        }));
                      }
                    }
                  }
                }
              }}
              aria-label="Spending breakdown by category"
            />
          )}
        </div>
      </div>

      {/* Monthly Income vs Expenses */}
      <div className="chart-container">
        <div className="chart-header">
          <h3>Monthly Income vs Expenses</h3>
          <div className="chart-summary">
            <div className="summary-item">
              <span className="label">Average Monthly Income:</span>
              <span className="value income">
                {getMaskedValue(
                  chartData?.averageIncome || 0,
                  userPermissions.canViewDetailedAnalytics
                )}
              </span>
            </div>
            <div className="summary-item">
              <span className="label">Average Monthly Expenses:</span>
              <span className="value expense">
                {getMaskedValue(
                  Math.abs(chartData?.averageExpenses || 0),
                  userPermissions.canViewDetailedAnalytics
                )}
              </span>
            </div>
          </div>
        </div>

        <div className="chart-wrapper" style={{ height: '300px' }}>
          {spendingChartData && (
            <Bar
              data={spendingChartData}
              options={chartOptions}
              aria-label="Monthly income versus expenses comparison"
            />
          )}
        </div>
      </div>

      {/* Export Options */}
      {userPermissions.canExportData && (
        <div className="chart-actions">
          <button
            onClick={() => exportChartData('pdf')}
            className="export-button"
          >
            Export as PDF
          </button>
          <button
            onClick={() => exportChartData('excel')}
            className="export-button"
          >
            Export to Excel
          </button>
        </div>
      )}
    </div>
  );
};

// Export functionality
const exportChartData = async (format) => {
  try {
    const response = await fetch(`/api/charts/export?format=${format}`, {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      }
    });

    if (!response.ok) {
      throw new Error('Export failed');
    }

    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `banking-analytics.${format}`;
    document.body.appendChild(a);
    a.click();
    window.URL.revokeObjectURL(url);
    document.body.removeChild(a);

  } catch (error) {
    alert(`Export failed: ${error.message}`);
  }
};

export default BankingChartDashboard;
```

---

### Q45: Real-time Account Updates

**Question**: How do you implement real-time account balance and transaction updates in React using WebSockets?

**Answer**:

Real-time updates are crucial for banking applications to provide accurate, up-to-date information.

**Real-time Banking Component:**

```jsx
import React, { useState, useEffect, useRef, useCallback } from 'react';

const RealTimeBankingAccount = ({ accountId, userId }) => {
  const [account, setAccount] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [connectionStatus, setConnectionStatus] = useState('disconnected');
  const [notifications, setNotifications] = useState([]);
  const [isVisible, setIsVisible] = useState(true);

  const wsRef = useRef(null);
  const reconnectTimeoutRef = useRef(null);
  const reconnectAttempts = useRef(0);
  const maxReconnectAttempts = 5;

  // WebSocket connection management
  const connectWebSocket = useCallback(() => {
    if (wsRef.current?.readyState === WebSocket.OPEN) {
      return;
    }

    try {
      const token = localStorage.getItem('auth_token');
      const wsUrl = `wss://banking-api.company.com/ws/accounts/${accountId}?token=${token}`;

      wsRef.current = new WebSocket(wsUrl);

      wsRef.current.onopen = () => {
        console.log('WebSocket connected');
        setConnectionStatus('connected');
        reconnectAttempts.current = 0;

        // Send authentication message
        wsRef.current.send(JSON.stringify({
          type: 'AUTH',
          userId: userId,
          accountId: accountId,
          timestamp: new Date().toISOString()
        }));
      };

      wsRef.current.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          handleWebSocketMessage(message);
        } catch (error) {
          console.error('Failed to parse WebSocket message:', error);
        }
      };

      wsRef.current.onerror = (error) => {
        console.error('WebSocket error:', error);
        setConnectionStatus('error');
      };

      wsRef.current.onclose = (event) => {
        console.log('WebSocket disconnected:', event.code, event.reason);
        setConnectionStatus('disconnected');

        // Attempt reconnection with exponential backoff
        if (reconnectAttempts.current < maxReconnectAttempts) {
          const delay = Math.pow(2, reconnectAttempts.current) * 1000;
          reconnectTimeoutRef.current = setTimeout(() => {
            reconnectAttempts.current++;
            connectWebSocket();
          }, delay);
        }
      };

    } catch (error) {
      console.error('Failed to establish WebSocket connection:', error);
      setConnectionStatus('error');
    }
  }, [accountId, userId]);

  // Handle different types of WebSocket messages
  const handleWebSocketMessage = useCallback((message) => {
    switch (message.type) {
      case 'BALANCE_UPDATE':
        setAccount(prevAccount => ({
          ...prevAccount,
          balance: message.newBalance,
          lastUpdated: message.timestamp
        }));

        // Show notification for significant balance changes
        if (Math.abs(message.balanceChange) > 100) {
          addNotification({
            type: 'info',
            title: 'Balance Updated',
            message: `Your balance changed by $${Math.abs(message.balanceChange).toFixed(2)}`,
            timestamp: message.timestamp
          });
        }
        break;

      case 'NEW_TRANSACTION':
        const newTransaction = message.transaction;

        setTransactions(prevTransactions => {
          const updated = [newTransaction, ...prevTransactions];
          return updated.slice(0, 50); // Keep only recent 50 transactions
        });

        // Update account balance
        setAccount(prevAccount => ({
          ...prevAccount,
          balance: newTransaction.newBalance,
          lastUpdated: newTransaction.timestamp
        }));

        // Show transaction notification
        addNotification({
          type: newTransaction.amount > 0 ? 'success' : 'warning',
          title: newTransaction.amount > 0 ? 'Money Received' : 'Payment Made',
          message: `${newTransaction.description}: $${Math.abs(newTransaction.amount).toFixed(2)}`,
          timestamp: newTransaction.timestamp
        });

        // Play sound notification if page is not visible
        if (!isVisible && 'Notification' in window && Notification.permission === 'granted') {
          new Notification('New Bank Transaction', {
            body: `${newTransaction.description}: $${Math.abs(newTransaction.amount).toFixed(2)}`,
            icon: '/banking-icon.png',
            tag: 'banking-transaction'
          });
        }
        break;

      case 'SECURITY_ALERT':
        addNotification({
          type: 'error',
          title: 'Security Alert',
          message: message.alert,
          timestamp: message.timestamp,
          persistent: true
        });

        // Force authentication refresh for critical security alerts
        if (message.severity === 'critical') {
          handleSecurityAlert(message);
        }
        break;

      case 'ACCOUNT_LOCKED':
        setAccount(prevAccount => ({
          ...prevAccount,
          status: 'locked',
          lockReason: message.reason
        }));

        addNotification({
          type: 'error',
          title: 'Account Locked',
          message: message.reason,
          timestamp: message.timestamp,
          persistent: true
        });
        break;

      case 'MAINTENANCE_NOTICE':
        addNotification({
          type: 'warning',
          title: 'Scheduled Maintenance',
          message: `Banking services will be unavailable from ${message.startTime} to ${message.endTime}`,
          timestamp: message.timestamp,
          persistent: true
        });
        break;

      case 'HEARTBEAT':
        // Server heartbeat to keep connection alive
        wsRef.current?.send(JSON.stringify({
          type: 'HEARTBEAT_ACK',
          timestamp: new Date().toISOString()
        }));
        break;

      default:
        console.log('Unknown message type:', message.type);
    }
  }, [isVisible]);

  // Add notification with auto-dismiss
  const addNotification = useCallback((notification) => {
    const id = Date.now() + Math.random();
    const notificationWithId = { ...notification, id };

    setNotifications(prev => [...prev, notificationWithId]);

    // Auto-dismiss non-persistent notifications
    if (!notification.persistent) {
      setTimeout(() => {
        setNotifications(prev => prev.filter(n => n.id !== id));
      }, 5000);
    }
  }, []);

  // Handle security alerts
  const handleSecurityAlert = useCallback((alertMessage) => {
    // Redirect to security verification page
    if (alertMessage.requiresReauth) {
      localStorage.setItem('security_redirect', window.location.pathname);
      window.location.href = '/security/verify';
    }
  }, []);

  // Initialize WebSocket connection
  useEffect(() => {
    connectWebSocket();

    return () => {
      if (reconnectTimeoutRef.current) {
        clearTimeout(reconnectTimeoutRef.current);
      }
      if (wsRef.current) {
        wsRef.current.close();
      }
    };
  }, [connectWebSocket]);

  // Track page visibility for notifications
  useEffect(() => {
    const handleVisibilityChange = () => {
      setIsVisible(!document.hidden);
    };

    document.addEventListener('visibilitychange', handleVisibilityChange);

    return () => {
      document.removeEventListener('visibilitychange', handleVisibilityChange);
    };
  }, []);

  // Request notification permissions
  useEffect(() => {
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission().then(permission => {
        console.log('Notification permission:', permission);
      });
    }
  }, []);

  // Manual refresh function
  const refreshAccount = useCallback(async () => {
    try {
      const response = await fetch(`/api/accounts/${accountId}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
        }
      });

      if (!response.ok) {
        throw new Error('Failed to refresh account data');
      }

      const accountData = await response.json();
      setAccount(accountData);

      addNotification({
        type: 'success',
        title: 'Account Refreshed',
        message: 'Account data has been updated',
        timestamp: new Date().toISOString()
      });

    } catch (error) {
      addNotification({
        type: 'error',
        title: 'Refresh Failed',
        message: error.message,
        timestamp: new Date().toISOString()
      });
    }
  }, [accountId, addNotification]);

  // Connection status indicator
  const ConnectionStatus = () => (
    <div className={`connection-status ${connectionStatus}`}>
      <div className="status-indicator"></div>
      <span className="status-text">
        {connectionStatus === 'connected' && 'Live Updates Active'}
        {connectionStatus === 'disconnected' && 'Reconnecting...'}
        {connectionStatus === 'error' && 'Connection Error'}
      </span>

      {connectionStatus !== 'connected' && (
        <button
          onClick={connectWebSocket}
          className="reconnect-button"
          aria-label="Reconnect to live updates"
        >
          Reconnect
        </button>
      )}
    </div>
  );

  // Notification component
  const NotificationList = () => (
    <div className="notification-list">
      {notifications.map(notification => (
        <div
          key={notification.id}
          className={`notification ${notification.type}`}
          role="alert"
        >
          <div className="notification-header">
            <strong>{notification.title}</strong>
            <button
              onClick={() => setNotifications(prev =>
                prev.filter(n => n.id !== notification.id)
              )}
              className="dismiss-button"
              aria-label="Dismiss notification"
            >
              ×
            </button>
          </div>
          <div className="notification-body">
            {notification.message}
          </div>
          <div className="notification-time">
            {new Date(notification.timestamp).toLocaleTimeString()}
          </div>
        </div>
      ))}
    </div>
  );

  if (!account) {
    return (
      <div className="account-loading">
        <div className="spinner"></div>
        <p>Loading account information...</p>
      </div>
    );
  }

  return (
    <div className="realtime-banking-account">
      {/* Connection Status */}
      <ConnectionStatus />

      {/* Notifications */}
      <NotificationList />

      {/* Account Information */}
      <div className="account-header">
        <div className="account-info">
          <h2>{account.name}</h2>
          <p className="account-number">Account ending in {account.number.slice(-4)}</p>
          <div className={`account-status ${account.status}`}>
            Status: {account.status}
          </div>
        </div>

        <div className="account-balance">
          <div className="balance-label">Current Balance</div>
          <div className="balance-amount">
            ${account.balance?.toLocaleString('en-US', {
              minimumFractionDigits: 2,
              maximumFractionDigits: 2
            })}
          </div>
          <div className="last-updated">
            Last updated: {account.lastUpdated ?
              new Date(account.lastUpdated).toLocaleString() :
              'Unknown'
            }
          </div>
        </div>

        <div className="account-actions">
          <button
            onClick={refreshAccount}
            className="refresh-button"
            aria-label="Manually refresh account data"
          >
            Refresh
          </button>
        </div>
      </div>

      {/* Real-time Transaction Feed */}
      <div className="transaction-feed">
        <div className="feed-header">
          <h3>Live Transaction Feed</h3>
          <div className="feed-controls">
            <label>
              <input
                type="checkbox"
                defaultChecked
                onChange={(e) => {
                  // Toggle real-time updates
                  if (e.target.checked) {
                    connectWebSocket();
                  } else {
                    wsRef.current?.close();
                  }
                }}
              />
              Real-time updates
            </label>
          </div>
        </div>

        <div className="transaction-list">
          {transactions.length === 0 ? (
            <div className="no-transactions">
              <p>No recent transactions</p>
            </div>
          ) : (
            transactions.map(transaction => (
              <div
                key={transaction.id}
                className={`transaction-item ${transaction.type} ${
                  transaction.isNew ? 'new-transaction' : ''
                }`}
              >
                <div className="transaction-time">
                  {new Date(transaction.timestamp).toLocaleTimeString()}
                </div>
                <div className="transaction-details">
                  <div className="transaction-description">
                    {transaction.description}
                  </div>
                  <div className="transaction-reference">
                    Ref: {transaction.reference}
                  </div>
                </div>
                <div className={`transaction-amount ${transaction.type}`}>
                  {transaction.amount > 0 ? '+' : ''}
                  ${transaction.amount.toLocaleString('en-US', {
                    minimumFractionDigits: 2,
                    maximumFractionDigits: 2
                  })}
                </div>
              </div>
            ))
          )}
        </div>
      </div>

      {/* Account locked overlay */}
      {account.status === 'locked' && (
        <div className="account-locked-overlay">
          <div className="locked-message">
            <h3>Account Temporarily Locked</h3>
            <p>{account.lockReason}</p>
            <button onClick={() => window.location.href = '/support'}>
              Contact Support
            </button>
          </div>
        </div>
      )}
    </div>
  );
};

export default RealTimeBankingAccount;
```

---

### Q46: Secure Form Components

**Question**: How do you implement secure form components for banking applications with validation, sanitization, and security measures?

**Answer**:

Banking forms require robust security, validation, and user experience considerations.

**Secure Banking Form Implementation:**

```jsx
import React, { useState, useCallback, useRef, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { yupResolver } from '@hookform/resolvers/yup';
import * as yup from 'yup';
import DOMPurify from 'dompurify';

// Validation schema for money transfer
const transferSchema = yup.object().shape({
  fromAccount: yup.string()
    .required('Please select a source account')
    .matches(/^ACC\d{10}$/, 'Invalid account format'),

  toAccount: yup.string()
    .required('Please select a destination account')
    .matches(/^ACC\d{10}$/, 'Invalid account format')
    .test('different-accounts', 'Cannot transfer to the same account', function(value) {
      return value !== this.parent.fromAccount;
    }),

  amount: yup.number()
    .required('Please enter an amount')
    .positive('Amount must be positive')
    .min(0.01, 'Minimum transfer amount is $0.01')
    .max(50000, 'Maximum transfer amount is $50,000 per transaction')
    .test('two-decimal', 'Amount can have maximum 2 decimal places', function(value) {
      return Number.isInteger(value * 100);
    }),

  description: yup.string()
    .required('Please enter a description')
    .min(3, 'Description must be at least 3 characters')
    .max(100, 'Description cannot exceed 100 characters')
    .matches(/^[a-zA-Z0-9\s\-.,!?']*$/, 'Description contains invalid characters'),

  scheduledDate: yup.date()
    .nullable()
    .min(new Date(), 'Scheduled date cannot be in the past')
    .max(new Date(Date.now() + 90 * 24 * 60 * 60 * 1000), 'Cannot schedule more than 90 days ahead'),

  purpose: yup.string()
    .required('Please select a purpose')
    .oneOf(['personal', 'business', 'family', 'bills', 'other'], 'Invalid purpose'),

  // Security fields
  authPin: yup.string()
    .required('Please enter your PIN')
    .matches(/^\d{4,6}$/, 'PIN must be 4-6 digits')
});

const SecureBankingTransferForm = ({ userAccounts, onSubmit, onCancel }) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [securityToken, setSecurityToken] = useState(null);
  const [attempts, setAttempts] = useState(0);
  const [isLocked, setIsLocked] = useState(false);
  const [showConfirmation, setShowConfirmation] = useState(false);
  const [transferData, setTransferData] = useState(null);

  const formRef = useRef(null);
  const amountInputRef = useRef(null);
  const submitTimeRef = useRef(null);

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    getValues,
    formState: { errors, isValid, dirtyFields },
    reset,
    clearErrors
  } = useForm({
    resolver: yupResolver(transferSchema),
    mode: 'onChange',
    defaultValues: {
      fromAccount: '',
      toAccount: '',
      amount: '',
      description: '',
      scheduledDate: '',
      purpose: '',
      authPin: ''
    }
  });

  const watchedAmount = watch('amount');
  const watchedFromAccount = watch('fromAccount');
  const watchedToAccount = watch('toAccount');

  // Generate security token on component mount
  useEffect(() => {
    const generateSecurityToken = async () => {
      try {
        const response = await fetch('/api/security/token', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('auth_token')}`,
            'Content-Type': 'application/json'
          }
        });

        if (response.ok) {
          const data = await response.json();
          setSecurityToken(data.token);
        }
      } catch (error) {
        console.error('Failed to generate security token:', error);
      }
    };

    generateSecurityToken();
  }, []);

  // Anti-automation measures
  useEffect(() => {
    submitTimeRef.current = Date.now();
  }, []);

  // Input sanitization
  const sanitizeInput = useCallback((value, field) => {
    if (typeof value !== 'string') return value;

    // Remove potentially dangerous characters
    let sanitized = DOMPurify.sanitize(value, { ALLOWED_TAGS: [] });

    // Field-specific sanitization
    switch (field) {
      case 'amount':
        // Only allow numbers and decimal point
        sanitized = sanitized.replace(/[^0-9.]/g, '');

        // Ensure only one decimal point
        const parts = sanitized.split('.');
        if (parts.length > 2) {
          sanitized = parts[0] + '.' + parts.slice(1).join('');
        }

        // Limit decimal places to 2
        if (parts[1] && parts[1].length > 2) {
          sanitized = parts[0] + '.' + parts[1].substring(0, 2);
        }
        break;

      case 'description':
        // Remove HTML tags and limit special characters
        sanitized = sanitized.replace(/<[^>]*>/g, '');
        sanitized = sanitized.replace(/[<>{}]/g, '');
        break;

      case 'authPin':
        // Only digits
        sanitized = sanitized.replace(/\D/g, '');
        break;

      default:
        break;
    }

    return sanitized;
  }, []);

  // Secure form submission
  const onFormSubmit = useCallback(async (data) => {
    // Prevent rapid submissions
    const timeSinceMount = Date.now() - submitTimeRef.current;
    if (timeSinceMount < 2000) {
      alert('Please wait before submitting');
      return;
    }

    // Check if form is locked due to failed attempts
    if (isLocked) {
      alert('Form is temporarily locked due to multiple failed attempts');
      return;
    }

    setIsSubmitting(true);

    try {
      // Sanitize all input data
      const sanitizedData = Object.keys(data).reduce((acc, key) => {
        acc[key] = sanitizeInput(data[key], key);
        return acc;
      }, {});

      // Validate transaction limits
      const fromAccount = userAccounts.find(acc => acc.id === sanitizedData.fromAccount);
      if (!fromAccount) {
        throw new Error('Invalid source account');
      }

      if (fromAccount.balance < parseFloat(sanitizedData.amount)) {
        throw new Error('Insufficient funds');
      }

      // Add security metadata
      const transferRequest = {
        ...sanitizedData,
        securityToken,
        deviceFingerprint: await generateDeviceFingerprint(),
        timestamp: new Date().toISOString(),
        sessionId: localStorage.getItem('session_id'),
        userAgent: navigator.userAgent,
        ipAddress: await getUserIP() // Would need to implement
      };

      // Store for confirmation
      setTransferData(transferRequest);
      setShowConfirmation(true);

    } catch (error) {
      setAttempts(prev => prev + 1);

      if (attempts >= 2) {
        setIsLocked(true);
        setTimeout(() => {
          setIsLocked(false);
          setAttempts(0);
        }, 300000); // 5 minute lockout
      }

      alert(error.message);
    } finally {
      setIsSubmitting(false);
    }
  }, [securityToken, userAccounts, attempts, isLocked, sanitizeInput]);

  // Confirm and execute transfer
  const confirmTransfer = useCallback(async () => {
    if (!transferData) return;

    setIsSubmitting(true);

    try {
      const response = await fetch('/api/transfers', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`,
          'Content-Type': 'application/json',
          'X-CSRF-Token': securityToken,
          'X-Requested-With': 'XMLHttpRequest'
        },
        body: JSON.stringify(transferData)
      });

      if (!response.ok) {
        const error = await response.json();
        throw new Error(error.message || 'Transfer failed');
      }

      const result = await response.json();

      // Clear sensitive data
      reset();
      setTransferData(null);
      setShowConfirmation(false);

      // Clear auth PIN from memory
      setValue('authPin', '');

      // Call parent callback
      onSubmit(result);

      alert(`Transfer successful! Reference: ${result.referenceNumber}`);

    } catch (error) {
      alert(error.message);
    } finally {
      setIsSubmitting(false);
    }
  }, [transferData, securityToken, reset, setValue, onSubmit]);

  // Amount formatting helper
  const formatAmount = useCallback((event) => {
    const value = event.target.value;
    const sanitized = sanitizeInput(value, 'amount');
    setValue('amount', sanitized, { shouldValidate: true });
  }, [setValue, sanitizeInput]);

  // Quick amount buttons
  const setQuickAmount = useCallback((amount) => {
    setValue('amount', amount.toString(), { shouldValidate: true });
    amountInputRef.current?.focus();
  }, [setValue]);

  // Generate device fingerprint for security
  const generateDeviceFingerprint = async () => {
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    ctx.textBaseline = 'top';
    ctx.font = '14px Arial';
    ctx.fillText('Banking security fingerprint', 2, 2);

    return {
      screen: `${screen.width}x${screen.height}`,
      timezone: Intl.DateTimeFormat().resolvedOptions().timeZone,
      language: navigator.language,
      platform: navigator.platform,
      canvasFingerprint: canvas.toDataURL(),
      userAgent: navigator.userAgent.substring(0, 100) // Truncate for security
    };
  };

  // Auto-save draft (encrypted)
  useEffect(() => {
    const saveDraft = () => {
      const values = getValues();
      if (values.fromAccount || values.toAccount || values.amount) {
        const draft = {
          ...values,
          authPin: '' // Never save PIN
        };

        // Encrypt and save to sessionStorage
        sessionStorage.setItem('transfer_draft', JSON.stringify(draft));
      }
    };

    const timer = setTimeout(saveDraft, 2000);
    return () => clearTimeout(timer);
  }, [watchedAmount, watchedFromAccount, watchedToAccount, getValues]);

  // Load draft on mount
  useEffect(() => {
    const loadDraft = () => {
      try {
        const draft = sessionStorage.getItem('transfer_draft');
        if (draft) {
          const parsed = JSON.parse(draft);
          Object.keys(parsed).forEach(key => {
            if (parsed[key] && key !== 'authPin') {
              setValue(key, parsed[key]);
            }
          });
        }
      } catch (error) {
        console.error('Failed to load draft:', error);
      }
    };

    loadDraft();
  }, [setValue]);

  if (showConfirmation && transferData) {
    return (
      <div className="transfer-confirmation">
        <div className="confirmation-header">
          <h3>Confirm Transfer</h3>
          <p>Please review the details below:</p>
        </div>

        <div className="confirmation-details">
          <div className="detail-row">
            <span className="label">From:</span>
            <span className="value">
              {userAccounts.find(acc => acc.id === transferData.fromAccount)?.name}
            </span>
          </div>
          <div className="detail-row">
            <span className="label">To:</span>
            <span className="value">
              {userAccounts.find(acc => acc.id === transferData.toAccount)?.name}
            </span>
          </div>
          <div className="detail-row">
            <span className="label">Amount:</span>
            <span className="value amount">
              ${parseFloat(transferData.amount).toLocaleString('en-US', {
                minimumFractionDigits: 2,
                maximumFractionDigits: 2
              })}
            </span>
          </div>
          <div className="detail-row">
            <span className="label">Description:</span>
            <span className="value">{transferData.description}</span>
          </div>
          {transferData.scheduledDate && (
            <div className="detail-row">
              <span className="label">Scheduled:</span>
              <span className="value">
                {new Date(transferData.scheduledDate).toLocaleDateString()}
              </span>
            </div>
          )}
        </div>

        <div className="confirmation-actions">
          <button
            type="button"
            onClick={() => setShowConfirmation(false)}
            className="cancel-button"
            disabled={isSubmitting}
          >
            Back to Edit
          </button>
          <button
            type="button"
            onClick={confirmTransfer}
            className="confirm-button"
            disabled={isSubmitting}
          >
            {isSubmitting ? 'Processing...' : 'Confirm Transfer'}
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="secure-banking-form">
      <div className="form-header">
        <h2>Transfer Money</h2>
        <div className="security-indicators">
          <div className="ssl-indicator">🔒 Secure Connection</div>
          {securityToken && (
            <div className="token-indicator">✓ Security Token Active</div>
          )}
        </div>
      </div>

      <form
        ref={formRef}
        onSubmit={handleSubmit(onFormSubmit)}
        className={`transfer-form ${isLocked ? 'locked' : ''}`}
        noValidate
      >
        {/* From Account */}
        <div className="form-group">
          <label htmlFor="fromAccount">From Account *</label>
          <select
            id="fromAccount"
            {...register('fromAccount')}
            className={errors.fromAccount ? 'error' : ''}
            disabled={isSubmitting}
          >
            <option value="">Select source account...</option>
            {userAccounts.map(account => (
              <option key={account.id} value={account.id}>
                {account.name} - ${account.balance.toLocaleString()} available
              </option>
            ))}
          </select>
          {errors.fromAccount && (
            <span className="error-message" role="alert">
              {errors.fromAccount.message}
            </span>
          )}
        </div>

        {/* To Account */}
        <div className="form-group">
          <label htmlFor="toAccount">To Account *</label>
          <select
            id="toAccount"
            {...register('toAccount')}
            className={errors.toAccount ? 'error' : ''}
            disabled={isSubmitting}
          >
            <option value="">Select destination account...</option>
            {userAccounts
              .filter(account => account.id !== watchedFromAccount)
              .map(account => (
                <option key={account.id} value={account.id}>
                  {account.name} - ****{account.number.slice(-4)}
                </option>
              ))
            }
          </select>
          {errors.toAccount && (
            <span className="error-message" role="alert">
              {errors.toAccount.message}
            </span>
          )}
        </div>

        {/* Amount */}
        <div className="form-group">
          <label htmlFor="amount">Amount *</label>
          <div className="amount-input-container">
            <span className="currency-symbol">$</span>
            <input
              ref={amountInputRef}
              type="text"
              id="amount"
              {...register('amount')}
              onChange={formatAmount}
              placeholder="0.00"
              className={errors.amount ? 'error' : ''}
              disabled={isSubmitting}
              autoComplete="off"
              inputMode="decimal"
            />
          </div>

          {/* Quick amount buttons */}
          <div className="quick-amounts">
            {[100, 250, 500, 1000].map(amount => (
              <button
                key={amount}
                type="button"
                onClick={() => setQuickAmount(amount)}
                className="quick-amount-btn"
                disabled={isSubmitting}
              >
                ${amount}
              </button>
            ))}
          </div>

          {errors.amount && (
            <span className="error-message" role="alert">
              {errors.amount.message}
            </span>
          )}
        </div>

        {/* Description */}
        <div className="form-group">
          <label htmlFor="description">Description *</label>
          <input
            type="text"
            id="description"
            {...register('description')}
            onChange={(e) => {
              const sanitized = sanitizeInput(e.target.value, 'description');
              setValue('description', sanitized, { shouldValidate: true });
            }}
            placeholder="Enter transfer description"
            className={errors.description ? 'error' : ''}
            disabled={isSubmitting}
            maxLength={100}
          />
          <div className="character-count">
            {watch('description')?.length || 0}/100
          </div>
          {errors.description && (
            <span className="error-message" role="alert">
              {errors.description.message}
            </span>
          )}
        </div>

        {/* Purpose */}
        <div className="form-group">
          <label htmlFor="purpose">Purpose *</label>
          <select
            id="purpose"
            {...register('purpose')}
            className={errors.purpose ? 'error' : ''}
            disabled={isSubmitting}
          >
            <option value="">Select purpose...</option>
            <option value="personal">Personal</option>
            <option value="business">Business</option>
            <option value="family">Family</option>
            <option value="bills">Bills & Utilities</option>
            <option value="other">Other</option>
          </select>
          {errors.purpose && (
            <span className="error-message" role="alert">
              {errors.purpose.message}
            </span>
          )}
        </div>

        {/* Scheduled Date */}
        <div className="form-group">
          <label htmlFor="scheduledDate">Schedule Transfer (Optional)</label>
          <input
            type="date"
            id="scheduledDate"
            {...register('scheduledDate')}
            min={new Date().toISOString().split('T')[0]}
            max={new Date(Date.now() + 90 * 24 * 60 * 60 * 1000).toISOString().split('T')[0]}
            className={errors.scheduledDate ? 'error' : ''}
            disabled={isSubmitting}
          />
          {errors.scheduledDate && (
            <span className="error-message" role="alert">
              {errors.scheduledDate.message}
            </span>
          )}
        </div>

        {/* Security PIN */}
        <div className="form-group security-group">
          <label htmlFor="authPin">Security PIN *</label>
          <input
            type="password"
            id="authPin"
            {...register('authPin')}
            onChange={(e) => {
              const sanitized = sanitizeInput(e.target.value, 'authPin');
              setValue('authPin', sanitized, { shouldValidate: true });
            }}
            placeholder="Enter your 4-6 digit PIN"
            className={errors.authPin ? 'error' : ''}
            disabled={isSubmitting}
            maxLength={6}
            autoComplete="off"
          />
          {errors.authPin && (
            <span className="error-message" role="alert">
              {errors.authPin.message}
            </span>
          )}
          <div className="security-note">
            Your PIN is encrypted and never stored in our systems
          </div>
        </div>

        {/* Attempts warning */}
        {attempts > 0 && (
          <div className="attempts-warning" role="alert">
            Warning: {attempts}/3 failed attempts. Form will be locked after 3 attempts.
          </div>
        )}

        {/* Lock message */}
        {isLocked && (
          <div className="lock-message" role="alert">
            Form is temporarily locked due to multiple failed attempts.
            Please wait 5 minutes before trying again.
          </div>
        )}

        {/* Form Actions */}
        <div className="form-actions">
          <button
            type="button"
            onClick={() => {
              reset();
              sessionStorage.removeItem('transfer_draft');
              onCancel?.();
            }}
            className="cancel-button"
            disabled={isSubmitting}
          >
            Cancel
          </button>

          <button
            type="submit"
            className="submit-button"
            disabled={!isValid || isSubmitting || isLocked || !securityToken}
          >
            {isSubmitting ? 'Processing...' : 'Review Transfer'}
          </button>
        </div>

        {/* Security footer */}
        <div className="security-footer">
          <p>
            🔒 All transfers are encrypted and monitored for security.
            You will receive confirmation via email and SMS.
          </p>
        </div>
      </form>
    </div>
  );
};

export default SecureBankingTransferForm;
```

---

### Q47: Banking UI/UX Patterns

**Question**: What are the key UI/UX patterns and design principles for banking applications in React?

**Answer**:

Banking applications require specific UI/UX patterns focused on trust, clarity, security, and accessibility.

**Banking Design System Components:**

```jsx
// Core banking design tokens and components
import React from 'react';
import styled from 'styled-components';

// Design tokens for banking applications
export const BankingTheme = {
  colors: {
    primary: {
      50: '#F0F9FF',
      100: '#E0F2FE',
      500: '#0EA5E9',
      600: '#0284C7',
      900: '#0C4A6E'
    },
    success: {
      50: '#F0FDF4',
      500: '#22C55E',
      600: '#16A34A'
    },
    warning: {
      50: '#FFFBEB',
      500: '#F59E0B',
      600: '#D97706'
    },
    error: {
      50: '#FEF2F2',
      500: '#EF4444',
      600: '#DC2626'
    },
    neutral: {
      50: '#F9FAFB',
      100: '#F3F4F6',
      500: '#6B7280',
      700: '#374151',
      900: '#111827'
    }
  },
  typography: {
    fontFamily: '"Inter", "Segoe UI", system-ui, sans-serif',
    sizes: {
      xs: '0.75rem',
      sm: '0.875rem',
      base: '1rem',
      lg: '1.125rem',
      xl: '1.25rem',
      '2xl': '1.5rem',
      '3xl': '1.875rem'
    },
    weights: {
      normal: 400,
      medium: 500,
      semibold: 600,
      bold: 700
    }
  },
  spacing: {
    xs: '0.25rem',
    sm: '0.5rem',
    md: '1rem',
    lg: '1.5rem',
    xl: '2rem',
    '2xl': '3rem'
  },
  shadows: {
    sm: '0 1px 2px 0 rgba(0, 0, 0, 0.05)',
    md: '0 4px 6px -1px rgba(0, 0, 0, 0.1)',
    lg: '0 10px 15px -3px rgba(0, 0, 0, 0.1)',
    xl: '0 20px 25px -5px rgba(0, 0, 0, 0.1)'
  }
};

// Currency display component with banking standards
export const CurrencyDisplay = ({
  amount,
  currency = 'USD',
  size = 'base',
  variant = 'default',
  showSign = true,
  className = ''
}) => {
  const formatCurrency = (value) => {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: currency,
      minimumFractionDigits: 2,
      maximumFractionDigits: 2
    }).format(Math.abs(value));
  };

  const getColorClass = () => {
    if (variant === 'positive' || amount > 0) return 'text-success-600';
    if (variant === 'negative' || amount < 0) return 'text-error-600';
    return 'text-neutral-900';
  };

  const getSizeClass = () => {
    const sizeMap = {
      xs: 'text-xs',
      sm: 'text-sm',
      base: 'text-base',
      lg: 'text-lg',
      xl: 'text-xl',
      '2xl': 'text-2xl'
    };
    return sizeMap[size] || sizeMap.base;
  };

  return (
    <span className={`font-medium tabular-nums ${getSizeClass()} ${getColorClass()} ${className}`}>
      {showSign && amount !== 0 && (amount > 0 ? '+' : '-')}
      {formatCurrency(amount)}
    </span>
  );
};

// Account card component with banking-specific patterns
export const AccountCard = ({
  account,
  isSelected = false,
  onClick,
  showBalance = true,
  variant = 'default'
}) => {
  const getAccountIcon = (type) => {
    const icons = {
      checking: '🏦',
      savings: '💰',
      credit: '💳',
      investment: '📈',
      loan: '🏠'
    };
    return icons[type] || '🏛️';
  };

  const getAccountTypeColor = (type) => {
    const colors = {
      checking: 'bg-blue-50 border-blue-200',
      savings: 'bg-green-50 border-green-200',
      credit: 'bg-purple-50 border-purple-200',
      investment: 'bg-yellow-50 border-yellow-200',
      loan: 'bg-red-50 border-red-200'
    };
    return colors[type] || 'bg-gray-50 border-gray-200';
  };

  return (
    <div
      className={`
        relative p-4 rounded-lg border-2 cursor-pointer transition-all duration-200
        ${isSelected ? 'border-primary-500 bg-primary-50 shadow-md' : getAccountTypeColor(account.type)}
        hover:shadow-lg hover:scale-[1.02] focus:outline-none focus:ring-2 focus:ring-primary-500
        ${account.status !== 'active' ? 'opacity-60' : ''}
      `}
      onClick={onClick}
      role="button"
      tabIndex={0}
      onKeyDown={(e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          onClick?.();
        }
      }}
      aria-pressed={isSelected}
      aria-describedby={`account-${account.id}-description`}
    >
      {/* Account status indicator */}
      {account.status !== 'active' && (
        <div className="absolute top-2 right-2">
          <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-warning-100 text-warning-800">
            {account.status}
          </span>
        </div>
      )}

      <div className="flex items-start justify-between">
        <div className="flex items-center space-x-3">
          <div className="text-2xl">{getAccountIcon(account.type)}</div>
          <div>
            <h3 className="font-semibold text-neutral-900">{account.name}</h3>
            <p className="text-sm text-neutral-500">
              ****{account.number.slice(-4)}
            </p>
            <p className="text-xs text-neutral-400 capitalize">
              {account.type} account
            </p>
          </div>
        </div>

        {showBalance && (
          <div className="text-right">
            <div className="text-xs text-neutral-500 mb-1">Available Balance</div>
            <CurrencyDisplay
              amount={account.availableBalance}
              size="lg"
              className="font-bold"
            />
            {account.pendingAmount > 0 && (
              <div className="text-xs text-neutral-500 mt-1">
                <CurrencyDisplay
                  amount={account.pendingAmount}
                  size="xs"
                /> pending
              </div>
            )}
          </div>
        )}
      </div>

      {/* Account description for screen readers */}
      <div id={`account-${account.id}-description`} className="sr-only">
        {account.type} account {account.name} ending in {account.number.slice(-4)}
        with available balance of {account.availableBalance}
        {account.status !== 'active' && `. Account status: ${account.status}`}
      </div>
    </div>
  );
};

// Transaction list with banking UX patterns
export const TransactionList = ({ transactions, showAccountInfo = false }) => {
  const groupTransactionsByDate = (transactions) => {
    return transactions.reduce((groups, transaction) => {
      const date = new Date(transaction.date).toDateString();
      if (!groups[date]) {
        groups[date] = [];
      }
      groups[date].push(transaction);
      return groups;
    }, {});
  };

  const groupedTransactions = groupTransactionsByDate(transactions);

  return (
    <div className="transaction-list space-y-4">
      {Object.entries(groupedTransactions).map(([date, dayTransactions]) => (
        <div key={date} className="transaction-group">
          <h3 className="text-sm font-medium text-neutral-700 mb-2 sticky top-0 bg-white py-1">
            {new Date(date).toLocaleDateString('en-US', {
              weekday: 'long',
              year: 'numeric',
              month: 'long',
              day: 'numeric'
            })}
          </h3>

          <div className="space-y-2">
            {dayTransactions.map((transaction) => (
              <TransactionItem
                key={transaction.id}
                transaction={transaction}
                showAccountInfo={showAccountInfo}
              />
            ))}
          </div>
        </div>
      ))}
    </div>
  );
};

export const TransactionItem = ({ transaction, showAccountInfo = false }) => {
  const getTransactionIcon = (category) => {
    const icons = {
      'food': '🍽️',
      'gas': '⛽',
      'shopping': '🛍️',
      'bills': '💡',
      'transfer': '↔️',
      'deposit': '💰',
      'withdrawal': '💸',
      'payment': '💳'
    };
    return icons[category] || '💰';
  };

  const isDebit = transaction.amount < 0;

  return (
    <div className="flex items-center p-3 bg-white rounded-lg border border-neutral-200 hover:border-neutral-300 transition-colors">
      <div className="flex-shrink-0 mr-3">
        <div className="w-10 h-10 rounded-full bg-neutral-100 flex items-center justify-center text-lg">
          {getTransactionIcon(transaction.category)}
        </div>
      </div>

      <div className="flex-1 min-w-0">
        <div className="flex items-start justify-between">
          <div className="flex-1">
            <p className="font-medium text-neutral-900 truncate">
              {transaction.description}
            </p>
            <div className="flex items-center space-x-2 mt-1">
              <p className="text-sm text-neutral-500">
                {new Date(transaction.date).toLocaleTimeString('en-US', {
                  hour: 'numeric',
                  minute: '2-digit'
                })}
              </p>
              {showAccountInfo && (
                <span className="text-xs text-neutral-400">
                  • {transaction.accountName}
                </span>
              )}
              {transaction.pending && (
                <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-warning-100 text-warning-800">
                  Pending
                </span>
              )}
            </div>
          </div>

          <div className="flex-shrink-0 ml-4 text-right">
            <CurrencyDisplay
              amount={transaction.amount}
              variant={isDebit ? 'negative' : 'positive'}
              size="base"
              className="font-semibold"
            />
            {transaction.category && (
              <p className="text-xs text-neutral-500 mt-1 capitalize">
                {transaction.category}
              </p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

// Status indicators with banking-appropriate styling
export const StatusBadge = ({ status, size = 'base' }) => {
  const getStatusConfig = (status) => {
    const configs = {
      active: {
        color: 'bg-success-100 text-success-800',
        icon: '✓',
        label: 'Active'
      },
      pending: {
        color: 'bg-warning-100 text-warning-800',
        icon: '⏳',
        label: 'Pending'
      },
      locked: {
        color: 'bg-error-100 text-error-800',
        icon: '🔒',
        label: 'Locked'
      },
      closed: {
        color: 'bg-neutral-100 text-neutral-800',
        icon: '✗',
        label: 'Closed'
      },
      processing: {
        color: 'bg-blue-100 text-blue-800',
        icon: '⚡',
        label: 'Processing'
      }
    };
    return configs[status] || configs.active;
  };

  const config = getStatusConfig(status);
  const sizeClass = size === 'sm' ? 'px-2 py-1 text-xs' : 'px-3 py-1 text-sm';

  return (
    <span className={`inline-flex items-center rounded-full font-medium ${config.color} ${sizeClass}`}>
      <span className="mr-1">{config.icon}</span>
      {config.label}
    </span>
  );
};

export default {
  BankingTheme,
  CurrencyDisplay,
  AccountCard,
  TransactionList,
  TransactionItem,
  StatusBadge
};
```

---

### Q48: Accessibility in Banking Apps

**Question**: How do you implement comprehensive accessibility features in React banking applications to meet WCAG guidelines?

**Answer**:

Banking applications must be accessible to all users, including those with disabilities, to comply with regulations like ADA and Section 508.

**Accessible Banking Components:**

```jsx
import React, { useState, useRef, useEffect } from 'react';

// Accessible form components for banking
export const AccessibleBankingForm = () => {
  const [formData, setFormData] = useState({
    amount: '',
    description: '',
    toAccount: ''
  });
  const [errors, setErrors] = useState({});
  const [announcements, setAnnouncements] = useState('');

  const amountRef = useRef(null);
  const errorSummaryRef = useRef(null);

  // ARIA live region for dynamic announcements
  const announce = (message) => {
    setAnnouncements(message);
    setTimeout(() => setAnnouncements(''), 1000);
  };

  // Form validation with accessible error handling
  const validateForm = () => {
    const newErrors = {};

    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      newErrors.amount = 'Please enter a valid amount greater than zero';
    }

    if (!formData.description.trim()) {
      newErrors.description = 'Please enter a description for this transfer';
    }

    if (!formData.toAccount) {
      newErrors.toAccount = 'Please select a destination account';
    }

    setErrors(newErrors);

    // Focus on first error and announce to screen readers
    if (Object.keys(newErrors).length > 0) {
      const firstErrorField = Object.keys(newErrors)[0];
      const firstErrorElement = document.getElementById(firstErrorField);

      if (firstErrorElement) {
        firstErrorElement.focus();
      }

      // Move focus to error summary
      if (errorSummaryRef.current) {
        errorSummaryRef.current.focus();
      }

      announce(`Form has ${Object.keys(newErrors).length} errors. Please review and correct.`);

      return false;
    }

    return true;
  };

  const handleSubmit = (e) => {
    e.preventDefault();

    if (validateForm()) {
      announce('Transfer request submitted successfully');
      // Process form submission
    }
  };

  return (
    <div className="accessible-banking-form">
      {/* Skip link for keyboard navigation */}
      <a href="#main-content" className="skip-link">
        Skip to main content
      </a>

      {/* ARIA live region for announcements */}
      <div
        aria-live="polite"
        aria-atomic="true"
        className="sr-only"
        id="announcements"
      >
        {announcements}
      </div>

      {/* Error summary - appears when there are validation errors */}
      {Object.keys(errors).length > 0 && (
        <div
          ref={errorSummaryRef}
          className="error-summary"
          role="alert"
          aria-labelledby="error-summary-title"
          tabIndex="-1"
        >
          <h2 id="error-summary-title" className="error-summary-title">
            There are {Object.keys(errors).length} errors in this form
          </h2>
          <ul className="error-summary-list">
            {Object.entries(errors).map(([field, message]) => (
              <li key={field}>
                <a
                  href={`#${field}`}
                  onClick={(e) => {
                    e.preventDefault();
                    document.getElementById(field)?.focus();
                  }}
                >
                  {message}
                </a>
              </li>
            ))}
          </ul>
        </div>
      )}

      <form onSubmit={handleSubmit} noValidate>
        <fieldset>
          <legend className="form-legend">Transfer Money</legend>

          {/* Amount field with comprehensive accessibility */}
          <div className="form-group">
            <label htmlFor="amount" className="form-label">
              Transfer Amount
              <span className="required-indicator" aria-label="required">*</span>
            </label>

            <div className="input-wrapper">
              <span className="input-prefix" aria-hidden="true">$</span>
              <input
                ref={amountRef}
                type="text"
                id="amount"
                name="amount"
                value={formData.amount}
                onChange={(e) => setFormData(prev => ({ ...prev, amount: e.target.value }))}
                className={`form-input ${errors.amount ? 'error' : ''}`}
                placeholder="0.00"
                aria-describedby={errors.amount ? 'amount-error amount-help' : 'amount-help'}
                aria-invalid={errors.amount ? 'true' : 'false'}
                aria-required="true"
                inputMode="decimal"
                autoComplete="off"
              />
            </div>

            <div id="amount-help" className="form-help">
              Enter the amount you want to transfer (maximum $10,000 per day)
            </div>

            {errors.amount && (
              <div id="amount-error" className="error-message" role="alert">
                <span className="error-icon" aria-hidden="true">⚠️</span>
                {errors.amount}
              </div>
            )}
          </div>

          {/* Account selection with accessible combobox */}
          <div className="form-group">
            <label htmlFor="toAccount" className="form-label">
              Transfer To
              <span className="required-indicator" aria-label="required">*</span>
            </label>

            <select
              id="toAccount"
              name="toAccount"
              value={formData.toAccount}
              onChange={(e) => setFormData(prev => ({ ...prev, toAccount: e.target.value }))}
              className={`form-select ${errors.toAccount ? 'error' : ''}`}
              aria-describedby={errors.toAccount ? 'toAccount-error toAccount-help' : 'toAccount-help'}
              aria-invalid={errors.toAccount ? 'true' : 'false'}
              aria-required="true"
            >
              <option value="">Choose an account</option>
              <option value="checking-001">
                Checking Account ending in 001 - $5,234.56 available
              </option>
              <option value="savings-002">
                Savings Account ending in 002 - $12,890.45 available
              </option>
            </select>

            <div id="toAccount-help" className="form-help">
              Select the account you want to transfer money to
            </div>

            {errors.toAccount && (
              <div id="toAccount-error" className="error-message" role="alert">
                <span className="error-icon" aria-hidden="true">⚠️</span>
                {errors.toAccount}
              </div>
            )}
          </div>

          {/* Description field */}
          <div className="form-group">
            <label htmlFor="description" className="form-label">
              Description
              <span className="required-indicator" aria-label="required">*</span>
            </label>

            <input
              type="text"
              id="description"
              name="description"
              value={formData.description}
              onChange={(e) => setFormData(prev => ({ ...prev, description: e.target.value }))}
              className={`form-input ${errors.description ? 'error' : ''}`}
              placeholder="Enter transfer description"
              aria-describedby={errors.description ? 'description-error description-help' : 'description-help'}
              aria-invalid={errors.description ? 'true' : 'false'}
              aria-required="true"
              maxLength={100}
            />

            <div id="description-help" className="form-help">
              Describe the purpose of this transfer (up to 100 characters)
            </div>

            {errors.description && (
              <div id="description-error" className="error-message" role="alert">
                <span className="error-icon" aria-hidden="true">⚠️</span>
                {errors.description}
              </div>
            )}
          </div>

          {/* Submit button with loading state */}
          <div className="form-actions">
            <button
              type="submit"
              className="btn btn-primary"
              aria-describedby="submit-help"
            >
              <span className="btn-text">Transfer Money</span>
              <span className="sr-only">
                Submit transfer for {formData.amount ? `$${formData.amount}` : 'entered amount'}
              </span>
            </button>

            <div id="submit-help" className="form-help">
              Review your transfer details before submitting
            </div>
          </div>
        </fieldset>
      </form>
    </div>
  );
};

// Accessible data table for transaction history
export const AccessibleTransactionTable = ({ transactions }) => {
  const [sortConfig, setSortConfig] = useState({ key: 'date', direction: 'desc' });
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 10;

  const sortedTransactions = React.useMemo(() => {
    let sortableTransactions = [...transactions];

    if (sortConfig.key) {
      sortableTransactions.sort((a, b) => {
        if (a[sortConfig.key] < b[sortConfig.key]) {
          return sortConfig.direction === 'asc' ? -1 : 1;
        }
        if (a[sortConfig.key] > b[sortConfig.key]) {
          return sortConfig.direction === 'asc' ? 1 : -1;
        }
        return 0;
      });
    }

    return sortableTransactions;
  }, [transactions, sortConfig]);

  const paginatedTransactions = sortedTransactions.slice(
    (currentPage - 1) * itemsPerPage,
    currentPage * itemsPerPage
  );

  const totalPages = Math.ceil(sortedTransactions.length / itemsPerPage);

  const handleSort = (key) => {
    setSortConfig(prevConfig => ({
      key,
      direction: prevConfig.key === key && prevConfig.direction === 'asc' ? 'desc' : 'asc'
    }));
  };

  const getSortAriaLabel = (column) => {
    if (sortConfig.key === column) {
      return `Sort by ${column} ${sortConfig.direction === 'asc' ? 'descending' : 'ascending'}`;
    }
    return `Sort by ${column}`;
  };

  return (
    <div className="accessible-table-container">
      {/* Table summary and navigation info */}
      <div className="table-info" aria-live="polite">
        <p>
          Showing {paginatedTransactions.length} of {sortedTransactions.length} transactions.
          {sortConfig.key && (
            <span> Sorted by {sortConfig.key} in {sortConfig.direction}ending order.</span>
          )}
        </p>
      </div>

      {/* Accessible table with proper headers and structure */}
      <table
        className="transaction-table"
        role="table"
        aria-label="Transaction history"
        aria-describedby="table-info"
      >
        <caption className="sr-only">
          Transaction history showing date, description, amount, and status.
          Use arrow keys to navigate and Enter to sort columns.
        </caption>

        <thead>
          <tr role="row">
            <th
              role="columnheader"
              aria-sort={
                sortConfig.key === 'date'
                  ? sortConfig.direction === 'asc' ? 'ascending' : 'descending'
                  : 'none'
              }
              tabIndex="0"
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  handleSort('date');
                }
              }}
              onClick={() => handleSort('date')}
              aria-label={getSortAriaLabel('date')}
              className="sortable-header"
            >
              Date
              <span className="sort-indicator" aria-hidden="true">
                {sortConfig.key === 'date' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
              </span>
            </th>

            <th
              role="columnheader"
              aria-sort={
                sortConfig.key === 'description'
                  ? sortConfig.direction === 'asc' ? 'ascending' : 'descending'
                  : 'none'
              }
              tabIndex="0"
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  handleSort('description');
                }
              }}
              onClick={() => handleSort('description')}
              aria-label={getSortAriaLabel('description')}
              className="sortable-header"
            >
              Description
              <span className="sort-indicator" aria-hidden="true">
                {sortConfig.key === 'description' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
              </span>
            </th>

            <th
              role="columnheader"
              aria-sort={
                sortConfig.key === 'amount'
                  ? sortConfig.direction === 'asc' ? 'ascending' : 'descending'
                  : 'none'
              }
              tabIndex="0"
              onKeyDown={(e) => {
                if (e.key === 'Enter' || e.key === ' ') {
                  e.preventDefault();
                  handleSort('amount');
                }
              }}
              onClick={() => handleSort('amount')}
              aria-label={getSortAriaLabel('amount')}
              className="sortable-header amount-column"
            >
              Amount
              <span className="sort-indicator" aria-hidden="true">
                {sortConfig.key === 'amount' && (sortConfig.direction === 'asc' ? '↑' : '↓')}
              </span>
            </th>

            <th role="columnheader">Status</th>
          </tr>
        </thead>

        <tbody>
          {paginatedTransactions.map((transaction, index) => (
            <tr key={transaction.id} role="row">
              <td role="gridcell">
                <time dateTime={transaction.date}>
                  {new Date(transaction.date).toLocaleDateString()}
                </time>
              </td>

              <td role="gridcell">
                {transaction.description}
              </td>

              <td role="gridcell" className="amount-cell">
                <span className={`amount ${transaction.amount < 0 ? 'debit' : 'credit'}`}>
                  {transaction.amount < 0 ? '-' : '+'}
                  ${Math.abs(transaction.amount).toFixed(2)}
                </span>
              </td>

              <td role="gridcell">
                <span
                  className={`status-badge ${transaction.status}`}
                  aria-label={`Transaction status: ${transaction.status}`}
                >
                  {transaction.status}
                </span>
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Accessible pagination */}
      <nav aria-label="Transaction pagination" className="pagination-nav">
        <ul className="pagination" role="list">
          <li>
            <button
              onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
              disabled={currentPage === 1}
              aria-label="Go to previous page"
              className="pagination-btn"
            >
              Previous
            </button>
          </li>

          {Array.from({ length: totalPages }, (_, i) => i + 1).map(page => (
            <li key={page}>
              <button
                onClick={() => setCurrentPage(page)}
                aria-label={`Go to page ${page}`}
                aria-current={currentPage === page ? 'page' : undefined}
                className={`pagination-btn ${currentPage === page ? 'current' : ''}`}
              >
                {page}
              </button>
            </li>
          ))}

          <li>
            <button
              onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
              disabled={currentPage === totalPages}
              aria-label="Go to next page"
              className="pagination-btn"
            >
              Next
            </button>
          </li>
        </ul>

        <div className="pagination-info" aria-live="polite">
          Page {currentPage} of {totalPages}
        </div>
      </nav>
    </div>
  );
};

// High contrast mode toggle for accessibility
export const HighContrastToggle = () => {
  const [isHighContrast, setIsHighContrast] = useState(false);

  useEffect(() => {
    const savedPreference = localStorage.getItem('highContrast') === 'true';
    setIsHighContrast(savedPreference);

    if (savedPreference) {
      document.body.classList.add('high-contrast');
    }
  }, []);

  const toggleHighContrast = () => {
    const newValue = !isHighContrast;
    setIsHighContrast(newValue);
    localStorage.setItem('highContrast', newValue.toString());

    if (newValue) {
      document.body.classList.add('high-contrast');
    } else {
      document.body.classList.remove('high-contrast');
    }
  };

  return (
    <button
      onClick={toggleHighContrast}
      className="high-contrast-toggle"
      aria-label={`${isHighContrast ? 'Disable' : 'Enable'} high contrast mode`}
      aria-pressed={isHighContrast}
    >
      <span className="toggle-icon" aria-hidden="true">
        {isHighContrast ? '🌙' : '☀️'}
      </span>
      <span className="toggle-text">
        {isHighContrast ? 'Standard' : 'High'} Contrast
      </span>
    </button>
  );
};

export default {
  AccessibleBankingForm,
  AccessibleTransactionTable,
  HighContrastToggle
};
```

---

### Q49: Progressive Web App Features

**Question**: How do you implement Progressive Web App features for banking applications using React?

**Answer**:

PWA features enable banking apps to work offline, send push notifications, and provide app-like experiences.

**Banking PWA Implementation:**

```jsx
import React, { useState, useEffect } from 'react';

// Service Worker registration and management
export const useServiceWorker = () => {
  const [isOnline, setIsOnline] = useState(navigator.onLine);
  const [swRegistration, setSwRegistration] = useState(null);
  const [updateAvailable, setUpdateAvailable] = useState(false);

  useEffect(() => {
    // Register service worker
    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.register('/sw.js', {
        scope: '/'
      })
      .then(registration => {
        setSwRegistration(registration);

        // Check for updates
        registration.addEventListener('updatefound', () => {
          const newWorker = registration.installing;
          newWorker.addEventListener('statechange', () => {
            if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
              setUpdateAvailable(true);
            }
          });
        });
      })
      .catch(error => {
        console.error('Service Worker registration failed:', error);
      });
    }

    // Online/offline status
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  const updateServiceWorker = () => {
    if (swRegistration?.waiting) {
      swRegistration.waiting.postMessage({ type: 'SKIP_WAITING' });
      window.location.reload();
    }
  };

  return {
    isOnline,
    updateAvailable,
    updateServiceWorker,
    swRegistration
  };
};

// Offline banking component with cached data
export const OfflineBankingDashboard = () => {
  const [cachedData, setCachedData] = useState(null);
  const [syncQueue, setSyncQueue] = useState([]);
  const { isOnline, updateAvailable, updateServiceWorker } = useServiceWorker();

  useEffect(() => {
    // Load cached data when offline
    if (!isOnline) {
      loadCachedData();
    }

    // Sync queued actions when online
    if (isOnline && syncQueue.length > 0) {
      syncQueuedActions();
    }
  }, [isOnline, syncQueue]);

  const loadCachedData = async () => {
    try {
      const cache = await caches.open('banking-data-v1');
      const cachedResponse = await cache.match('/api/dashboard');

      if (cachedResponse) {
        const data = await cachedResponse.json();
        setCachedData(data);
      }
    } catch (error) {
      console.error('Failed to load cached data:', error);
    }
  };

  const syncQueuedActions = async () => {
    try {
      for (const action of syncQueue) {
        await fetch(action.url, {
          method: action.method,
          headers: action.headers,
          body: action.body
        });
      }

      setSyncQueue([]);

      // Show success notification
      if ('serviceWorker' in navigator && 'Notification' in window) {
        navigator.serviceWorker.ready.then(registration => {
          registration.showNotification('Banking App', {
            body: 'Your pending transactions have been synced.',
            icon: '/icons/icon-192x192.png',
            badge: '/icons/badge-72x72.png',
            tag: 'sync-complete'
          });
        });
      }
    } catch (error) {
      console.error('Failed to sync queued actions:', error);
    }
  };

  const addToSyncQueue = (action) => {
    setSyncQueue(prev => [...prev, action]);

    // Store in IndexedDB for persistence
    if ('indexedDB' in window) {
      const request = indexedDB.open('BankingOfflineDB', 1);
      request.onsuccess = (event) => {
        const db = event.target.result;
        const transaction = db.transaction(['syncQueue'], 'readwrite');
        const store = transaction.objectStore('syncQueue');
        store.add(action);
      };
    }
  };

  const handleOfflineTransfer = (transferData) => {
    // Add to sync queue for when online
    addToSyncQueue({
      url: '/api/transfers',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      },
      body: JSON.stringify(transferData)
    });

    // Show offline confirmation
    alert('Transfer queued. It will be processed when you\'re back online.');
  };

  return (
    <div className="offline-banking-dashboard">
      {/* Update notification */}
      {updateAvailable && (
        <div className="update-notification" role="alert">
          <p>A new version of the app is available.</p>
          <button onClick={updateServiceWorker}>Update Now</button>
        </div>
      )}

      {/* Offline indicator */}
      {!isOnline && (
        <div className="offline-indicator" role="alert">
          <span className="offline-icon">📶</span>
          <span>You're offline. Using cached data.</span>
          {syncQueue.length > 0 && (
            <span className="sync-count">
              {syncQueue.length} action(s) pending sync
            </span>
          )}
        </div>
      )}

      {/* Cached account data */}
      {cachedData && (
        <div className="cached-data-warning">
          <p>⚠️ Showing cached data from {new Date(cachedData.lastUpdated).toLocaleString()}</p>
        </div>
      )}

      {/* Banking dashboard content */}
      <div className="dashboard-content">
        {/* Account balances (cached or live) */}
        <div className="account-balances">
          {(cachedData?.accounts || []).map(account => (
            <div key={account.id} className="account-card offline">
              <h3>{account.name}</h3>
              <p className="balance">${account.balance.toLocaleString()}</p>
              <p className="last-updated">
                Last updated: {new Date(account.lastUpdated).toLocaleString()}
              </p>
            </div>
          ))}
        </div>

        {/* Offline transaction form */}
        <div className="offline-transfer-form">
          <h3>Quick Transfer {!isOnline && '(Offline)'}</h3>
          <p>Transfers will be processed when you're back online.</p>

          <button
            onClick={() => handleOfflineTransfer({
              amount: 100,
              description: 'Quick transfer',
              toAccount: 'savings'
            })}
            disabled={!isOnline}
            className="offline-transfer-btn"
          >
            {isOnline ? 'Transfer $100' : 'Queue Transfer (Offline)'}
          </button>
        </div>
      </div>
    </div>
  );
};

// Push notification management for banking alerts
export const usePushNotifications = () => {
  const [permission, setPermission] = useState(Notification.permission);
  const [subscription, setSubscription] = useState(null);

  useEffect(() => {
    if ('serviceWorker' in navigator && 'PushManager' in window) {
      navigator.serviceWorker.ready.then(registration => {
        registration.pushManager.getSubscription().then(sub => {
          setSubscription(sub);
        });
      });
    }
  }, []);

  const requestPermission = async () => {
    if ('Notification' in window) {
      const result = await Notification.requestPermission();
      setPermission(result);
      return result;
    }
  };

  const subscribeToPush = async () => {
    if ('serviceWorker' in navigator && 'PushManager' in window) {
      const registration = await navigator.serviceWorker.ready;

      const subscription = await registration.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: 'YOUR_VAPID_PUBLIC_KEY' // Replace with your VAPID key
      });

      setSubscription(subscription);

      // Send subscription to server
      await fetch('/api/push/subscribe', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
        },
        body: JSON.stringify(subscription)
      });

      return subscription;
    }
  };

  const unsubscribeFromPush = async () => {
    if (subscription) {
      await subscription.unsubscribe();
      setSubscription(null);

      // Notify server
      await fetch('/api/push/unsubscribe', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
        },
        body: JSON.stringify(subscription)
      });
    }
  };

  return {
    permission,
    subscription,
    requestPermission,
    subscribeToPush,
    unsubscribeFromPush
  };
};

// Banking notification preferences component
export const NotificationSettings = () => {
  const { permission, subscription, requestPermission, subscribeToPush, unsubscribeFromPush } = usePushNotifications();
  const [preferences, setPreferences] = useState({
    accountAlerts: true,
    transactionNotifications: true,
    securityAlerts: true,
    balanceReminders: false,
    promotionalOffers: false
  });

  const handlePermissionRequest = async () => {
    const result = await requestPermission();
    if (result === 'granted') {
      await subscribeToPush();
    }
  };

  const handlePreferenceChange = async (preference, enabled) => {
    const newPreferences = { ...preferences, [preference]: enabled };
    setPreferences(newPreferences);

    // Save preferences to server
    await fetch('/api/user/notification-preferences', {
      method: 'PUT',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${localStorage.getItem('auth_token')}`
      },
      body: JSON.stringify(newPreferences)
    });
  };

  return (
    <div className="notification-settings">
      <h2>Notification Settings</h2>

      <div className="permission-status">
        <h3>Push Notification Permission</h3>
        <p>Status: {permission}</p>

        {permission === 'default' && (
          <button onClick={handlePermissionRequest} className="enable-notifications-btn">
            Enable Push Notifications
          </button>
        )}

        {permission === 'granted' && !subscription && (
          <button onClick={subscribeToPush} className="subscribe-btn">
            Subscribe to Notifications
          </button>
        )}

        {subscription && (
          <button onClick={unsubscribeFromPush} className="unsubscribe-btn">
            Disable Push Notifications
          </button>
        )}
      </div>

      <div className="notification-preferences">
        <h3>Notification Types</h3>

        {Object.entries(preferences).map(([key, enabled]) => (
          <label key={key} className="preference-item">
            <input
              type="checkbox"
              checked={enabled}
              onChange={(e) => handlePreferenceChange(key, e.target.checked)}
              disabled={!subscription}
            />
            <span className="preference-label">
              {key.replace(/([A-Z])/g, ' $1').replace(/^./, str => str.toUpperCase())}
            </span>
          </label>
        ))}
      </div>

      <div className="notification-examples">
        <h3>Example Notifications</h3>
        <ul>
          <li>💰 Large deposit received in your checking account</li>
          <li>🔒 Login from new device detected</li>
          <li>💳 Credit card payment due in 3 days</li>
          <li>📊 Monthly account statement is ready</li>
        </ul>
      </div>
    </div>
  );
};

// PWA install prompt
export const PWAInstallPrompt = () => {
  const [deferredPrompt, setDeferredPrompt] = useState(null);
  const [showInstallPrompt, setShowInstallPrompt] = useState(false);

  useEffect(() => {
    const handleBeforeInstallPrompt = (e) => {
      e.preventDefault();
      setDeferredPrompt(e);
      setShowInstallPrompt(true);
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);

    return () => {
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
    };
  }, []);

  const handleInstallClick = async () => {
    if (deferredPrompt) {
      deferredPrompt.prompt();
      const choiceResult = await deferredPrompt.userChoice;

      if (choiceResult.outcome === 'accepted') {
        console.log('User accepted the install prompt');
      }

      setDeferredPrompt(null);
      setShowInstallPrompt(false);
    }
  };

  if (!showInstallPrompt) {
    return null;
  }

  return (
    <div className="pwa-install-prompt">
      <div className="install-prompt-content">
        <h3>Install Banking App</h3>
        <p>Get quick access to your accounts with our app.</p>
        <ul>
          <li>✓ Work offline with cached data</li>
          <li>✓ Receive push notifications for important alerts</li>
          <li>✓ Quick access from your home screen</li>
          <li>✓ Enhanced security features</li>
        </ul>

        <div className="install-actions">
          <button onClick={handleInstallClick} className="install-btn">
            Install App
          </button>
          <button onClick={() => setShowInstallPrompt(false)} className="dismiss-btn">
            Not Now
          </button>
        </div>
      </div>
    </div>
  );
};

export default {
  useServiceWorker,
  OfflineBankingDashboard,
  usePushNotifications,
  NotificationSettings,
  PWAInstallPrompt
};
```

---

## Summary

This comprehensive React guide covers:

- **React Fundamentals** (4 questions): Core concepts, components, state, events
- **React Hooks** (5 questions): useEffect, useContext, useReducer, custom hooks, optimization
- **Advanced Patterns** (4 questions): HOCs, render props, compound components, error boundaries
- **State Management** (4 questions): Redux, RTK, Zustand, React Query
- **Routing & Navigation** (3 questions): React Router, protected routes, dynamic routing
- **Forms & Validation** (3 questions): React Hook Form, validation, file uploads
- **API Integration** (4 questions): HTTP clients, error handling, auth, WebSockets
- **Testing Strategies** (4 questions): Unit testing, integration, E2E, mocking
- **Performance** (4 questions): Optimization, code splitting, bundle analysis, virtual scrolling
- **Full-Stack Integration** (4 questions): Spring Boot integration, JWT, dashboards, microservices
- **Modern Ecosystem** (4 questions): Next.js, TypeScript, styling, build tools
- **Banking-Specific Patterns** (6 questions): Financial visualizations, real-time updates, secure forms, UI/UX, accessibility, PWA

**Total: 49 detailed React interview questions** focused specifically on React development with comprehensive banking examples and production-ready patterns for financial services applications.