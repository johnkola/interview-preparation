# React Full-Stack Developer Comprehensive Guide

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

### 12. [Full-Stack Developer Responsibilities](#full-stack-developer-responsibilities)
- [Q44: System Architecture Design](#q44-system-architecture-design)
- [Q45: Frontend-Backend Integration](#q45-frontend-backend-integration)
- [Q46: Database Design and Management](#q46-database-design-and-management)
- [Q47: DevOps and Infrastructure](#q47-devops-and-infrastructure)
- [Q48: Code Quality and Standards](#q48-code-quality-and-standards)
- [Q49: Team Leadership and Mentoring](#q49-team-leadership-and-mentoring)

### 13. [Agile Development Methods](#agile-development-methods)
- [Q50: Scrum Framework Implementation](#q50-scrum-framework-implementation)
- [Q51: Sprint Planning and Estimation](#q51-sprint-planning-and-estimation)
- [Q52: User Story Writing and Backlog Management](#q52-user-story-writing-and-backlog-management)
- [Q53: Daily Standups and Communication](#q53-daily-standups-and-communication)
- [Q54: Code Reviews and Pair Programming](#q54-code-reviews-and-pair-programming)
- [Q55: Testing in Agile Environment](#q55-testing-in-agile-environment)
- [Q56: Continuous Integration/Deployment](#q56-continuous-integrationdeployment)
- [Q57: Retrospectives and Process Improvement](#q57-retrospectives-and-process-improvement)

### 14. [Production Deployment](#production-deployment)
- [Q58: Docker Containerization](#q58-docker-containerization)
- [Q59: CI/CD Pipelines](#q59-cicd-pipelines)
- [Q60: Environment Configuration](#q60-environment-configuration)
- [Q61: Monitoring and Analytics](#q61-monitoring-and-analytics)

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

## 🏗️ Full-Stack Developer Responsibilities

### Q44: System Architecture Design

**Answer:**
As a full-stack developer/architect, you're responsible for designing scalable, maintainable systems that serve business needs effectively.

**Banking System Architecture Design:**

```javascript
// System Architecture Overview for Banking Application

/**
 * BANKING SYSTEM ARCHITECTURE
 *
 * Frontend (React)
 * ├── Presentation Layer
 * │   ├── Components (UI)
 * │   ├── State Management (Redux/Context)
 * │   ├── Routing (React Router)
 * │   └── API Integration
 *
 * Backend (Spring Boot)
 * ├── API Gateway Layer
 * │   ├── Authentication/Authorization
 * │   ├── Rate Limiting
 * │   ├── Load Balancing
 * │   └── Request Routing
 * ├── Business Logic Layer
 * │   ├── Account Management Service
 * │   ├── Transaction Service
 * │   ├── Fraud Detection Service
 * │   └── Notification Service
 * ├── Data Access Layer
 * │   ├── Repository Pattern
 * │   ├── ORM (JPA/Hibernate)
 * │   └── Caching (Redis)
 *
 * Database Layer
 * ├── Primary Database (PostgreSQL)
 * ├── Cache Layer (Redis)
 * ├── Search Engine (Elasticsearch)
 * └── Message Queue (RabbitMQ/Kafka)
 *
 * Infrastructure
 * ├── Container Orchestration (Kubernetes)
 * ├── CI/CD Pipeline (Jenkins/GitHub Actions)
 * ├── Monitoring (Prometheus/Grafana)
 * └── Cloud Services (AWS/Azure)
 */

// Architecture Decision Record (ADR) Template
const architectureDecision = {
  title: "State Management Strategy for Banking Application",
  status: "Accepted",
  date: "2024-01-15",

  context: `
    Banking application requires complex state management for:
    - User authentication and sessions
    - Account data synchronization
    - Real-time transaction updates
    - Offline capabilities
    - Form state management
  `,

  decision: `
    Use Redux Toolkit for global state management with:
    - RTK Query for server state
    - React Context for theme/UI state
    - React Hook Form for form state
    - Local storage for user preferences
  `,

  consequences: {
    positive: [
      "Predictable state updates",
      "Time-travel debugging",
      "Excellent dev tools",
      "Clear separation of concerns"
    ],
    negative: [
      "Learning curve for team",
      "Additional boilerplate",
      "Bundle size increase"
    ]
  },

  alternatives: [
    "Zustand - simpler but less tooling",
    "React Context only - would cause performance issues",
    "SWR/React Query - good for server state but not global client state"
  ]
};

// System Design Patterns Implementation
class BankingSystemArchitect {

  // Microservices Architecture Pattern
  designMicroservices() {
    return {
      services: [
        {
          name: "user-service",
          responsibilities: ["Authentication", "User Profile", "Preferences"],
          database: "user_db",
          dependencies: ["notification-service"]
        },
        {
          name: "account-service",
          responsibilities: ["Account Management", "Balance Tracking"],
          database: "account_db",
          dependencies: ["user-service", "transaction-service"]
        },
        {
          name: "transaction-service",
          responsibilities: ["Transaction Processing", "Transfer Operations"],
          database: "transaction_db",
          dependencies: ["account-service", "fraud-service"]
        },
        {
          name: "fraud-service",
          responsibilities: ["Fraud Detection", "Risk Analysis"],
          database: "fraud_db",
          dependencies: ["transaction-service", "notification-service"]
        },
        {
          name: "notification-service",
          responsibilities: ["Email/SMS", "Push Notifications", "Alerts"],
          database: "notification_db",
          dependencies: []
        }
      ],

      communicationPatterns: {
        synchronous: "REST APIs for real-time operations",
        asynchronous: "Message queues for event-driven updates",
        dataSync: "Event sourcing for audit trail"
      }
    };
  }

  // Database Design Strategy
  designDatabase() {
    return {
      strategy: "Database per Service",

      databases: {
        userDatabase: {
          type: "PostgreSQL",
          schema: {
            users: ["id", "username", "email", "created_at"],
            user_profiles: ["user_id", "first_name", "last_name", "phone"],
            user_preferences: ["user_id", "theme", "notifications", "language"]
          }
        },

        accountDatabase: {
          type: "PostgreSQL",
          schema: {
            accounts: ["id", "user_id", "account_number", "type", "balance"],
            account_history: ["id", "account_id", "balance", "timestamp"]
          }
        },

        transactionDatabase: {
          type: "PostgreSQL",
          partitioning: "by_date", // Monthly partitions
          schema: {
            transactions: ["id", "from_account", "to_account", "amount", "timestamp"],
            transaction_events: ["id", "transaction_id", "event_type", "timestamp"]
          }
        },

        cacheLayer: {
          type: "Redis",
          usage: ["Session storage", "Account balances", "User preferences"]
        },

        searchEngine: {
          type: "Elasticsearch",
          usage: ["Transaction search", "Fraud pattern detection"]
        }
      }
    };
  }

  // API Design Guidelines
  designAPIs() {
    return {
      restfulPrinciples: {
        resourceBased: true,
        httpMethods: ["GET", "POST", "PUT", "DELETE", "PATCH"],
        statusCodes: [200, 201, 400, 401, 403, 404, 500],

        examples: {
          accounts: {
            "GET /api/accounts": "List user accounts",
            "GET /api/accounts/{id}": "Get specific account",
            "POST /api/accounts": "Create new account",
            "PUT /api/accounts/{id}": "Update account",
            "DELETE /api/accounts/{id}": "Close account"
          },

          transactions: {
            "GET /api/accounts/{id}/transactions": "Get account transactions",
            "POST /api/transactions/transfer": "Create money transfer",
            "GET /api/transactions/{id}": "Get transaction details"
          }
        }
      },

      apiVersioning: {
        strategy: "URL versioning",
        format: "/api/v1/accounts",
        deprecationPolicy: "Support N-1 versions"
      },

      documentation: {
        tool: "OpenAPI/Swagger",
        includeExamples: true,
        generateSDKs: true
      }
    };
  }

  // Security Architecture
  designSecurity() {
    return {
      authentication: {
        strategy: "JWT with refresh tokens",
        provider: "OAuth 2.0 / OpenID Connect",
        mfa: "TOTP/SMS verification"
      },

      authorization: {
        model: "RBAC (Role-Based Access Control)",
        roles: ["CUSTOMER", "ADMIN", "SUPPORT", "AUDITOR"],
        permissions: "Fine-grained resource permissions"
      },

      dataProtection: {
        encryption: {
          atRest: "AES-256",
          inTransit: "TLS 1.3",
          applicationLevel: "Field-level encryption for PII"
        },
        compliance: ["PCI DSS", "GDPR", "SOX"]
      },

      apiSecurity: {
        rateLimiting: "100 requests/minute per user",
        cors: "Strict origin policies",
        headers: ["HSTS", "CSP", "X-Frame-Options"]
      }
    };
  }

  // Performance Requirements
  designPerformance() {
    return {
      requirements: {
        responseTime: {
          accountBalance: "< 200ms",
          transactionHistory: "< 500ms",
          moneyTransfer: "< 1s"
        },

        throughput: {
          concurrentUsers: "10,000+",
          transactionsPerSecond: "1,000+",
          dailyTransactions: "1M+"
        },

        availability: {
          uptime: "99.9%",
          plannedDowntime: "< 4 hours/month",
          disasterRecovery: "RTO: 1 hour, RPO: 15 minutes"
        }
      },

      optimizationStrategies: {
        caching: {
          browser: "Static assets, API responses",
          cdn: "Global content distribution",
          application: "Redis for frequently accessed data",
          database: "Query result caching"
        },

        scaling: {
          horizontal: "Auto-scaling based on load",
          vertical: "Performance monitoring and optimization",
          database: "Read replicas, connection pooling"
        }
      }
    };
  }
}

// Frontend Architecture Patterns
const frontendArchitecture = {

  componentArchitecture: {
    structure: `
      src/
      ├── components/           # Reusable UI components
      │   ├── atoms/           # Basic building blocks
      │   ├── molecules/       # Component combinations
      │   ├── organisms/       # Complex components
      │   └── templates/       # Page layouts
      ├── pages/               # Route components
      ├── hooks/               # Custom React hooks
      ├── services/            # API integration
      ├── store/               # State management
      ├── utils/               # Helper functions
      ├── types/               # TypeScript definitions
      └── constants/           # Application constants
    `,

    designPrinciples: [
      "Single Responsibility",
      "Composition over Inheritance",
      "Props down, Events up",
      "Separation of Concerns"
    ]
  },

  stateManagement: {
    global: "Redux Toolkit for application state",
    server: "RTK Query for server state",
    local: "useState/useReducer for component state",
    forms: "React Hook Form for form state",
    url: "React Router for navigation state"
  },

  codeOrganization: {
    featureSlicing: "Group by business domain",
    sharedComponents: "Reusable across features",
    utilities: "Pure functions, no side effects",
    constants: "Environment and configuration"
  }
};

export { BankingSystemArchitect, frontendArchitecture };
```

### Q45: Frontend-Backend Integration

**Answer:**
Seamless integration between React frontend and Spring Boot backend requires careful API design, error handling, and state synchronization.

**Complete Integration Strategy:**

```javascript
// API Service Layer for Frontend-Backend Integration

class BankingAPIService {
  constructor() {
    this.baseURL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';
    this.authToken = null;
    this.refreshToken = null;

    // Configure Axios instance
    this.client = axios.create({
      baseURL: this.baseURL,
      timeout: 10000,
      headers: {
        'Content-Type': 'application/json'
      }
    });

    this.setupInterceptors();
  }

  setupInterceptors() {
    // Request interceptor for auth tokens
    this.client.interceptors.request.use(
      (config) => {
        if (this.authToken) {
          config.headers.Authorization = `Bearer ${this.authToken}`;
        }

        // Add request ID for tracing
        config.headers['X-Request-ID'] = this.generateRequestId();

        return config;
      },
      (error) => Promise.reject(error)
    );

    // Response interceptor for token refresh
    this.client.interceptors.response.use(
      (response) => response,
      async (error) => {
        const originalRequest = error.config;

        if (error.response?.status === 401 && !originalRequest._retry) {
          originalRequest._retry = true;

          try {
            await this.refreshAuthToken();
            originalRequest.headers.Authorization = `Bearer ${this.authToken}`;
            return this.client(originalRequest);
          } catch (refreshError) {
            this.handleAuthFailure();
            return Promise.reject(refreshError);
          }
        }

        return Promise.reject(this.transformError(error));
      }
    );
  }

  // Authentication Methods
  async login(credentials) {
    try {
      const response = await this.client.post('/auth/login', credentials);

      this.authToken = response.data.accessToken;
      this.refreshToken = response.data.refreshToken;

      // Store tokens securely
      this.storeTokens(response.data);

      return {
        success: true,
        user: response.data.user,
        tokens: response.data
      };
    } catch (error) {
      throw this.transformError(error);
    }
  }

  async refreshAuthToken() {
    if (!this.refreshToken) {
      throw new Error('No refresh token available');
    }

    try {
      const response = await this.client.post('/auth/refresh', {
        refreshToken: this.refreshToken
      });

      this.authToken = response.data.accessToken;
      this.storeTokens(response.data);

      return response.data;
    } catch (error) {
      this.clearTokens();
      throw error;
    }
  }

  // Account Management APIs
  async getAccounts() {
    try {
      const response = await this.client.get('/accounts');
      return response.data;
    } catch (error) {
      throw this.transformError(error);
    }
  }

  async getAccountDetails(accountId) {
    try {
      const response = await this.client.get(`/accounts/${accountId}`);
      return response.data;
    } catch (error) {
      throw this.transformError(error);
    }
  }

  async getAccountBalance(accountId) {
    try {
      const response = await this.client.get(`/accounts/${accountId}/balance`);
      return response.data;
    } catch (error) {
      throw this.transformError(error);
    }
  }

  // Transaction APIs
  async getTransactions(accountId, params = {}) {
    try {
      const queryParams = new URLSearchParams({
        page: params.page || 0,
        size: params.size || 20,
        sort: params.sort || 'date,desc',
        ...params.filters
      });

      const response = await this.client.get(
        `/accounts/${accountId}/transactions?${queryParams}`
      );

      return {
        transactions: response.data.content,
        pagination: {
          page: response.data.number,
          size: response.data.size,
          totalElements: response.data.totalElements,
          totalPages: response.data.totalPages,
          hasNext: !response.data.last,
          hasPrevious: !response.data.first
        }
      };
    } catch (error) {
      throw this.transformError(error);
    }
  }

  async createTransfer(transferData) {
    try {
      // Validate transfer data before sending
      this.validateTransferData(transferData);

      const response = await this.client.post('/transactions/transfer', {
        fromAccountId: transferData.fromAccount,
        toAccountId: transferData.toAccount,
        amount: parseFloat(transferData.amount),
        description: transferData.description,
        scheduledDate: transferData.scheduledDate || null
      });

      return response.data;
    } catch (error) {
      throw this.transformError(error);
    }
  }

  // WebSocket Integration for Real-time Updates
  connectWebSocket(userId) {
    return new Promise((resolve, reject) => {
      const wsUrl = `${this.baseURL.replace('http', 'ws')}/ws/user/${userId}`;

      this.websocket = new WebSocket(wsUrl, [], {
        headers: {
          Authorization: `Bearer ${this.authToken}`
        }
      });

      this.websocket.onopen = () => {
        console.log('WebSocket connected');
        resolve(this.websocket);
      };

      this.websocket.onerror = (error) => {
        console.error('WebSocket error:', error);
        reject(error);
      };

      this.websocket.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data);
          this.handleWebSocketMessage(message);
        } catch (error) {
          console.error('Failed to parse WebSocket message:', error);
        }
      };

      this.websocket.onclose = () => {
        console.log('WebSocket disconnected');
        // Attempt to reconnect after 5 seconds
        setTimeout(() => {
          if (this.authToken) {
            this.connectWebSocket(userId);
          }
        }, 5000);
      };
    });
  }

  // Error Handling and Transformation
  transformError(error) {
    if (error.response) {
      // Server responded with error status
      const { status, data } = error.response;

      switch (status) {
        case 400:
          return {
            type: 'VALIDATION_ERROR',
            message: data.message || 'Invalid request data',
            details: data.errors || [],
            status
          };
        case 401:
          return {
            type: 'AUTHENTICATION_ERROR',
            message: 'Authentication failed',
            status
          };
        case 403:
          return {
            type: 'AUTHORIZATION_ERROR',
            message: 'Access denied',
            status
          };
        case 404:
          return {
            type: 'NOT_FOUND_ERROR',
            message: data.message || 'Resource not found',
            status
          };
        case 409:
          return {
            type: 'CONFLICT_ERROR',
            message: data.message || 'Resource conflict',
            status
          };
        case 500:
          return {
            type: 'SERVER_ERROR',
            message: 'Internal server error',
            status
          };
        default:
          return {
            type: 'API_ERROR',
            message: data.message || 'An error occurred',
            status
          };
      }
    } else if (error.request) {
      // Network error
      return {
        type: 'NETWORK_ERROR',
        message: 'Network connection failed',
        details: 'Please check your internet connection'
      };
    } else {
      // Other error
      return {
        type: 'UNKNOWN_ERROR',
        message: error.message || 'An unexpected error occurred'
      };
    }
  }

  // Utility Methods
  validateTransferData(data) {
    const errors = [];

    if (!data.fromAccount) errors.push('Source account is required');
    if (!data.toAccount) errors.push('Destination account is required');
    if (data.fromAccount === data.toAccount) errors.push('Source and destination accounts must be different');

    const amount = parseFloat(data.amount);
    if (!amount || amount <= 0) errors.push('Amount must be greater than 0');
    if (amount > 10000) errors.push('Amount cannot exceed $10,000 per transaction');

    if (!data.description?.trim()) errors.push('Description is required');

    if (errors.length > 0) {
      throw {
        type: 'VALIDATION_ERROR',
        message: 'Invalid transfer data',
        details: errors
      };
    }
  }

  generateRequestId() {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
  }

  storeTokens(tokens) {
    // Use secure storage (consider encryption for sensitive data)
    localStorage.setItem('banking_access_token', tokens.accessToken);
    localStorage.setItem('banking_refresh_token', tokens.refreshToken);
    localStorage.setItem('banking_token_expiry', tokens.expiresAt);
  }

  clearTokens() {
    this.authToken = null;
    this.refreshToken = null;
    localStorage.removeItem('banking_access_token');
    localStorage.removeItem('banking_refresh_token');
    localStorage.removeItem('banking_token_expiry');
  }

  handleAuthFailure() {
    this.clearTokens();
    window.location.href = '/login';
  }

  handleWebSocketMessage(message) {
    // Dispatch events that components can listen to
    const event = new CustomEvent('banking-websocket-message', {
      detail: message
    });
    window.dispatchEvent(event);
  }
}

// React Hook for API Integration
export const useApiService = () => {
  const apiService = useRef(new BankingAPIService()).current;

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const executeRequest = useCallback(async (apiCall) => {
    setLoading(true);
    setError(null);

    try {
      const result = await apiCall();
      return result;
    } catch (err) {
      setError(err);
      throw err;
    } finally {
      setLoading(false);
    }
  }, []);

  return {
    apiService,
    loading,
    error,
    executeRequest
  };
};

// Integration with React Components
const AccountDashboard = () => {
  const { apiService, loading, error, executeRequest } = useApiService();
  const [accounts, setAccounts] = useState([]);
  const [selectedAccount, setSelectedAccount] = useState(null);
  const [transactions, setTransactions] = useState([]);

  // Load initial data
  useEffect(() => {
    const loadData = async () => {
      try {
        const accountsData = await executeRequest(() => apiService.getAccounts());
        setAccounts(accountsData);

        if (accountsData.length > 0) {
          setSelectedAccount(accountsData[0]);
        }
      } catch (err) {
        console.error('Failed to load accounts:', err);
      }
    };

    loadData();
  }, [apiService, executeRequest]);

  // Load transactions when account changes
  useEffect(() => {
    if (!selectedAccount) return;

    const loadTransactions = async () => {
      try {
        const transactionData = await executeRequest(() =>
          apiService.getTransactions(selectedAccount.id, { size: 50 })
        );
        setTransactions(transactionData.transactions);
      } catch (err) {
        console.error('Failed to load transactions:', err);
      }
    };

    loadTransactions();
  }, [selectedAccount, apiService, executeRequest]);

  // WebSocket for real-time updates
  useEffect(() => {
    const connectWebSocket = async () => {
      try {
        await apiService.connectWebSocket('current-user-id');

        // Listen for WebSocket messages
        const handleMessage = (event) => {
          const message = event.detail;

          if (message.type === 'TRANSACTION_UPDATE') {
            // Update transactions in real-time
            setTransactions(prev => [message.transaction, ...prev]);

            // Update account balance
            setAccounts(prev =>
              prev.map(account =>
                account.id === message.transaction.accountId
                  ? { ...account, balance: message.newBalance }
                  : account
              )
            );
          }
        };

        window.addEventListener('banking-websocket-message', handleMessage);

        return () => {
          window.removeEventListener('banking-websocket-message', handleMessage);
        };
      } catch (err) {
        console.error('WebSocket connection failed:', err);
      }
    };

    connectWebSocket();
  }, [apiService]);

  if (loading && accounts.length === 0) {
    return <div>Loading accounts...</div>;
  }

  if (error) {
    return (
      <div className="error-container">
        <h3>Error: {error.message}</h3>
        {error.details && (
          <ul>
            {error.details.map((detail, index) => (
              <li key={index}>{detail}</li>
            ))}
          </ul>
        )}
        <button onClick={() => window.location.reload()}>
          Retry
        </button>
      </div>
    );
  }

  return (
    <div className="account-dashboard">
      {/* Account selection and transaction display */}
    </div>
  );
};

export default AccountDashboard;
```

---

## 🔄 Agile Development Methods

### Q50: Scrum Framework Implementation

**Answer:**
Implementing Scrum effectively requires understanding roles, ceremonies, and artifacts while adapting to team needs.

**Complete Scrum Implementation for Banking Team:**

```javascript
// Scrum Framework Implementation for Banking Development Team

class ScrumFramework {
  constructor(teamConfig) {
    this.team = teamConfig;
    this.currentSprint = null;
    this.productBacklog = [];
    this.sprintBacklog = [];
    this.burndownChart = [];
  }

  // Scrum Roles Definition
  defineRoles() {
    return {
      productOwner: {
        name: "Sarah Johnson",
        responsibilities: [
          "Define product vision and roadmap",
          "Prioritize product backlog",
          "Write and refine user stories",
          "Accept/reject sprint deliverables",
          "Stakeholder communication",
          "ROI maximization"
        ],
        bankingFocus: [
          "Customer experience optimization",
          "Regulatory compliance requirements",
          "Business value prioritization",
          "Market competitive analysis"
        ]
      },

      scrumMaster: {
        name: "Mike Chen",
        responsibilities: [
          "Facilitate scrum ceremonies",
          "Remove team impediments",
          "Coach team on agile practices",
          "Protect team from distractions",
          "Metrics tracking and reporting",
          "Continuous improvement facilitation"
        ],
        bankingFocus: [
          "Risk management processes",
          "Security compliance coaching",
          "Cross-team coordination",
          "Regulatory change management"
        ]
      },

      developmentTeam: {
        members: [
          {
            name: "Alex Rodriguez",
            role: "Full-Stack Developer",
            specialization: "React/Spring Boot",
            capacity: 40, // hours per sprint
            currentVelocity: 13 // story points per sprint
          },
          {
            name: "Lisa Wang",
            role: "Frontend Developer",
            specialization: "React/TypeScript",
            capacity: 40,
            currentVelocity: 15
          },
          {
            name: "David Kim",
            role: "Backend Developer",
            specialization: "Spring Boot/Microservices",
            capacity: 40,
            currentVelocity: 12
          },
          {
            name: "Emma Thompson",
            role: "QA Engineer",
            specialization: "Test Automation",
            capacity: 40,
            currentVelocity: 10
          }
        ],
        collectiveResponsibilities: [
          "Sprint planning participation",
          "Daily standup attendance",
          "Sprint goal achievement",
          "Code quality maintenance",
          "Knowledge sharing",
          "Cross-functional collaboration"
        ]
      }
    };
  }

  // Sprint Planning Implementation
  planSprint(sprintNumber, sprintDuration = 14) {
    const sprintGoal = this.defineSprintGoal(sprintNumber);
    const teamCapacity = this.calculateTeamCapacity();
    const prioritizedBacklog = this.prioritizeBacklog();

    const sprint = {
      number: sprintNumber,
      goal: sprintGoal,
      duration: sprintDuration, // days
      startDate: new Date(),
      endDate: new Date(Date.now() + sprintDuration * 24 * 60 * 60 * 1000),
      capacity: teamCapacity,
      selectedStories: [],
      commitmentPoints: 0
    };

    // Sprint Planning Meeting Part 1: What can we deliver?
    const part1Planning = this.conductSprintPlanningPart1(
      prioritizedBacklog,
      teamCapacity
    );

    // Sprint Planning Meeting Part 2: How will we deliver it?
    const part2Planning = this.conductSprintPlanningPart2(
      part1Planning.selectedStories
    );

    sprint.selectedStories = part2Planning.storiesWithTasks;
    sprint.commitmentPoints = part2Planning.totalPoints;

    this.currentSprint = sprint;
    return sprint;
  }

  defineSprintGoal(sprintNumber) {
    // Banking-specific sprint goals
    const sprintGoals = {
      1: "Implement secure user authentication and account overview",
      2: "Complete transaction history and filtering functionality",
      3: "Build money transfer feature with fraud detection",
      4: "Add account management and statement generation",
      5: "Implement real-time notifications and alerts"
    };

    return sprintGoals[sprintNumber] || "Continue product development";
  }

  calculateTeamCapacity() {
    const team = this.defineRoles().developmentTeam;

    return team.members.reduce((total, member) => {
      // Account for planned time off, meetings, etc.
      const availableHours = member.capacity * 0.8; // 80% availability
      return total + availableHours;
    }, 0);
  }

  conductSprintPlanningPart1(backlog, capacity) {
    const selectedStories = [];
    let totalPoints = 0;
    let remainingCapacity = capacity;

    for (const story of backlog) {
      if (story.points <= remainingCapacity && totalPoints + story.points <= 50) {
        selectedStories.push(story);
        totalPoints += story.points;
        remainingCapacity -= story.points;
      }
    }

    return { selectedStories, totalPoints };
  }

  conductSprintPlanningPart2(stories) {
    const storiesWithTasks = stories.map(story => ({
      ...story,
      tasks: this.breakDownIntoTasks(story),
      owner: this.assignStoryOwner(story),
      acceptanceCriteria: this.refineAcceptanceCriteria(story)
    }));

    const totalPoints = storiesWithTasks.reduce(
      (sum, story) => sum + story.points, 0
    );

    return { storiesWithTasks, totalPoints };
  }

  breakDownIntoTasks(story) {
    // Example task breakdown for banking story
    const commonTasks = {
      "User Login Feature": [
        "Design login form UI component (4h)",
        "Implement form validation (3h)",
        "Integrate with authentication API (5h)",
        "Add error handling and loading states (3h)",
        "Write unit tests for login component (4h)",
        "Create integration tests (3h)",
        "Security review and penetration testing (6h)"
      ],

      "Account Balance Display": [
        "Create balance component design (2h)",
        "Implement real-time balance updates (6h)",
        "Add balance history tracking (4h)",
        "Format currency display properly (2h)",
        "Implement error states for balance fetch (3h)",
        "Add accessibility features (3h)",
        "Performance optimization (4h)"
      ],

      "Money Transfer Feature": [
        "Design transfer form UI (6h)",
        "Implement amount validation (4h)",
        "Add account selection logic (5h)",
        "Integrate with transfer API (8h)",
        "Add confirmation flow (4h)",
        "Implement fraud detection integration (6h)",
        "Create transaction receipt (3h)",
        "Add comprehensive error handling (5h)"
      ]
    };

    return commonTasks[story.title] || [
      "Analysis and design (2h)",
      "Implementation (8h)",
      "Testing (4h)",
      "Code review (2h)"
    ];
  }

  // Daily Standup Structure
  conductDailyStandup() {
    const standupStructure = {
      timeBox: "15 minutes maximum",
      participants: "Development team members only",
      format: "Round-robin, each member shares",

      questions: [
        "What did you accomplish yesterday?",
        "What will you work on today?",
        "What impediments are blocking you?"
      ],

      scrumMasterNotes: [
        "Note any impediments mentioned",
        "Identify stories at risk",
        "Schedule follow-up conversations",
        "Update burndown chart"
      ],

      bankingSpecificTopics: [
        "Security review status",
        "Compliance checkpoint updates",
        "Integration testing progress",
        "Production deployment readiness"
      ]
    };

    // Sample standup responses
    const standupResponses = {
      "Alex Rodriguez": {
        yesterday: "Completed user authentication API integration, resolved CORS issues",
        today: "Will work on account balance real-time updates, pair with Lisa on WebSocket implementation",
        impediments: "Waiting for security team approval on JWT token configuration"
      },

      "Lisa Wang": {
        yesterday: "Finished login form UI, added form validation with React Hook Form",
        today: "Will implement WebSocket connection for real-time balance updates",
        impediments: "None currently"
      },

      "David Kim": {
        yesterday: "Implemented transfer API endpoints, added fraud detection hooks",
        today: "Will work on transaction history pagination and database optimization",
        impediments: "Database migration taking longer than expected, may need DBA consultation"
      },

      "Emma Thompson": {
        yesterday: "Set up automated testing pipeline, created test data fixtures",
        today: "Will write integration tests for authentication flow",
        impediments: "Test environment is unstable, coordinating with DevOps team"
      }
    };

    return { standupStructure, standupResponses };
  }

  // Sprint Review Implementation
  conductSprintReview() {
    const reviewStructure = {
      timeBox: "2 hours for 2-week sprint",
      participants: [
        "Development team",
        "Product owner",
        "Stakeholders",
        "End users (when possible)"
      ],

      agenda: [
        {
          item: "Sprint goal review",
          duration: "10 minutes",
          owner: "Product Owner"
        },
        {
          item: "Demonstration of completed features",
          duration: "60 minutes",
          owner: "Development Team"
        },
        {
          item: "Stakeholder feedback collection",
          duration: "30 minutes",
          owner: "Product Owner"
        },
        {
          item: "Product backlog updates",
          duration: "20 minutes",
          owner: "Product Owner"
        }
      ],

      demonstrationScript: {
        "User Authentication": {
          scenario: "New customer onboarding",
          steps: [
            "Navigate to registration page",
            "Enter customer information",
            "Verify email/SMS confirmation",
            "Set up security questions",
            "Complete first login"
          ],
          successCriteria: [
            "Registration completes without errors",
            "Security validations work correctly",
            "User can successfully log in",
            "Session management functions properly"
          ]
        },

        "Account Dashboard": {
          scenario: "Existing customer checking accounts",
          steps: [
            "Log in to application",
            "View account summary",
            "Check real-time balances",
            "Review recent transactions"
          ],
          successCriteria: [
            "All accounts display correctly",
            "Balances are accurate and current",
            "Transaction history loads quickly",
            "UI is responsive and accessible"
          ]
        }
      }
    };

    return reviewStructure;
  }

  // Sprint Retrospective Implementation
  conductSprintRetrospective() {
    const retrospectiveFormat = {
      timeBox: "1.5 hours for 2-week sprint",
      participants: "Development team + Scrum Master",

      activities: [
        {
          name: "Check-in",
          duration: "10 minutes",
          description: "Team mood and energy level assessment"
        },
        {
          name: "Data gathering",
          duration: "30 minutes",
          technique: "Start-Stop-Continue",
          description: "Collect facts about the sprint"
        },
        {
          name: "Generate insights",
          duration: "20 minutes",
          technique: "5 Whys analysis",
          description: "Understand root causes"
        },
        {
          name: "Decide what to do",
          duration: "20 minutes",
          description: "Create action items with owners"
        },
        {
          name: "Close retrospective",
          duration: "10 minutes",
          description: "Commit to action items"
        }
      ],

      startStopContinueExample: {
        start: [
          "Daily code reviews for critical banking features",
          "Security-focused peer programming sessions",
          "Regular stakeholder check-ins during development",
          "Automated security testing in CI/CD pipeline"
        ],

        stop: [
          "Last-minute requirement changes without impact analysis",
          "Deploying to production without proper testing",
          "Skipping documentation for complex business logic",
          "Working in isolation on shared components"
        ],

        continue: [
          "Pair programming for complex features",
          "Daily standups with focused updates",
          "Test-driven development practices",
          "Regular refactoring and code cleanup"
        ]
      },

      actionItems: [
        {
          action: "Implement pre-commit hooks for security scanning",
          owner: "David Kim",
          dueDate: "Next sprint planning",
          success_criteria: "All commits automatically scanned for security issues"
        },
        {
          action: "Create shared component library documentation",
          owner: "Lisa Wang",
          dueDate: "End of next sprint",
          success_criteria: "All components have usage examples and props documentation"
        },
        {
          action: "Set up automated performance testing",
          owner: "Emma Thompson",
          dueDate: "Next sprint",
          success_criteria: "Performance tests run on every deployment"
        }
      ]
    };

    return retrospectiveFormat;
  }

  // Metrics and Tracking
  trackSprintMetrics() {
    return {
      velocityTracking: {
        lastSprints: [
          { sprint: 1, plannedPoints: 45, completedPoints: 42, velocity: 42 },
          { sprint: 2, plannedPoints: 48, completedPoints: 50, velocity: 50 },
          { sprint: 3, plannedPoints: 50, completedPoints: 47, velocity: 47 },
          { sprint: 4, plannedPoints: 49, completedPoints: 51, velocity: 51 }
        ],
        averageVelocity: 47.5,
        velocityTrend: "stable"
      },

      burndownChart: {
        sprintLength: 10, // working days
        totalPoints: 50,
        dailyProgress: [
          { day: 1, remaining: 50, ideal: 45 },
          { day: 2, remaining: 47, ideal: 40 },
          { day: 3, remaining: 43, ideal: 35 },
          { day: 4, remaining: 38, ideal: 30 },
          { day: 5, remaining: 35, ideal: 25 },
          { day: 6, remaining: 28, ideal: 20 },
          { day: 7, remaining: 22, ideal: 15 },
          { day: 8, remaining: 15, ideal: 10 },
          { day: 9, remaining: 8, ideal: 5 },
          { day: 10, remaining: 0, ideal: 0 }
        ]
      },

      qualityMetrics: {
        bugRatePerStory: 0.8,
        testCoverage: 85,
        codeReviewParticipation: 100,
        customerSatisfactionScore: 4.2
      }
    };
  }
}

// Banking-Specific Agile Practices
const bankingAgilePractices = {

  complianceIntegration: {
    definitionOfDone: [
      "Code complete and peer reviewed",
      "Unit tests written and passing (>90% coverage)",
      "Integration tests passing",
      "Security review completed",
      "Accessibility compliance verified",
      "Performance benchmarks met",
      "Documentation updated",
      "Product owner acceptance",
      "Compliance checklist completed",
      "Security penetration test passed"
    ],

    regulatoryConsiderations: [
      "PCI DSS compliance for payment processing",
      "GDPR data protection requirements",
      "SOX financial reporting controls",
      "AML (Anti-Money Laundering) checks",
      "KYC (Know Your Customer) validation"
    ]
  },

  riskManagement: {
    riskCategories: [
      "Security vulnerabilities",
      "Data privacy breaches",
      "Regulatory non-compliance",
      "System availability issues",
      "Performance degradation"
    ],

    mitigationStrategies: [
      "Regular security audits",
      "Automated compliance testing",
      "Disaster recovery testing",
      "Load testing and monitoring",
      "Code quality gates"
    ]
  }
};

export { ScrumFramework, bankingAgilePractices };
```

### Q51: Sprint Planning and Estimation

**Answer:**
Effective sprint planning requires proper estimation techniques, capacity planning, and collaborative decision-making.

**Comprehensive Sprint Planning Process:**

```javascript
// Sprint Planning and Estimation Framework

class SprintPlanningFramework {
  constructor() {
    this.estimationTechniques = this.initializeEstimationTechniques();
    this.planningPokerCards = [1, 2, 3, 5, 8, 13, 21, 34, 55, 89];
    this.teamCapacity = {};
    this.historicalVelocity = [];
  }

  // Story Point Estimation using Planning Poker
  conductPlanningPoker(userStory) {
    const planningSession = {
      story: userStory,
      participants: [
        "Alex (Full-Stack Dev)",
        "Lisa (Frontend Dev)",
        "David (Backend Dev)",
        "Emma (QA Engineer)"
      ],

      rounds: [
        {
          roundNumber: 1,
          estimates: {
            "Alex": 8,
            "Lisa": 5,
            "David": 13,
            "Emma": 8
          },
          discussion: [
            "David: This involves complex microservice integration",
            "Lisa: Frontend seems straightforward, mostly form handling",
            "Alex: Database changes might be more complex than expected",
            "Emma: Testing will require multiple integration scenarios"
          ]
        },
        {
          roundNumber: 2,
          estimates: {
            "Alex": 8,
            "Lisa": 8,
            "David": 8,
            "Emma": 8
          },
          finalEstimate: 8,
          confidence: "High - team consensus reached"
        }
      ]
    };

    return planningSession;
  }

  // Banking User Stories with Detailed Estimation
  estimateBankingUserStories() {
    const userStories = [
      {
        id: "US-001",
        title: "Account Balance Real-time Updates",
        description: "As a customer, I want to see my account balance update in real-time so that I have accurate financial information",

        acceptanceCriteria: [
          "Balance updates immediately after transaction",
          "WebSocket connection handles disconnections gracefully",
          "Balance display shows loading state during updates",
          "Historical balance changes are tracked",
          "Works across multiple browser tabs"
        ],

        tasks: [
          "Implement WebSocket connection management (5h)",
          "Create balance update event handlers (3h)",
          "Add loading states and error handling (4h)",
          "Write unit tests for WebSocket integration (4h)",
          "Implement connection retry logic (3h)",
          "Add integration tests (4h)"
        ],

        estimationFactors: {
          complexity: "Medium - WebSocket integration",
          unknowns: "Low - team has WebSocket experience",
          dependencies: "Medium - requires backend WebSocket endpoint",
          riskLevel: "Medium - real-time features can be tricky"
        },

        storyPoints: 8,
        estimationReasoning: [
          "WebSocket integration is well-understood pattern",
          "Frontend state management is straightforward",
          "Good test coverage required for real-time features",
          "Similar to previous real-time features delivered"
        ]
      },

      {
        id: "US-002",
        title: "Money Transfer with Fraud Detection",
        description: "As a customer, I want to transfer money between accounts with automatic fraud detection to ensure secure transactions",

        acceptanceCriteria: [
          "Transfer form validates all required fields",
          "Fraud detection API is called before processing",
          "High-risk transfers require additional verification",
          "Transfer confirmation includes transaction ID",
          "Email notification sent for completed transfers",
          "Transaction history updated immediately"
        ],

        tasks: [
          "Design transfer form with validation (6h)",
          "Implement fraud detection API integration (8h)",
          "Add two-factor authentication for high-risk transfers (10h)",
          "Create transfer confirmation flow (4h)",
          "Implement email notification system (6h)",
          "Add comprehensive error handling (5h)",
          "Write unit and integration tests (8h)",
          "Security review and penetration testing (6h)"
        ],

        estimationFactors: {
          complexity: "High - Multiple system integrations",
          unknowns: "Medium - Fraud detection API requirements unclear",
          dependencies: "High - Requires fraud service, email service, auth service",
          riskLevel: "High - Financial transaction security critical"
        },

        storyPoints: 21,
        estimationReasoning: [
          "Complex integration with multiple external services",
          "Security requirements are extensive",
          "Two-factor authentication adds complexity",
          "Comprehensive testing required for financial features",
          "Similar features took 2-3 weeks in previous projects"
        ]
      },

      {
        id: "US-003",
        title: "Transaction History with Advanced Filtering",
        description: "As a customer, I want to filter and search my transaction history to easily find specific transactions",

        acceptanceCriteria: [
          "Filter by date range, amount range, transaction type",
          "Search by description or merchant name",
          "Sort by date, amount, or description",
          "Export filtered results to PDF/CSV",
          "Pagination for large result sets",
          "Save filter preferences"
        ],

        tasks: [
          "Create filter UI components (6h)",
          "Implement search functionality (4h)",
          "Add sorting and pagination (5h)",
          "Create PDF/CSV export feature (8h)",
          "Implement filter preference saving (4h)",
          "Optimize database queries for performance (6h)",
          "Add comprehensive testing (6h)"
        ],

        estimationFactors: {
          complexity: "Medium - Standard CRUD with search",
          unknowns: "Low - Well-understood requirements",
          dependencies: "Low - Mostly frontend work with existing APIs",
          riskLevel: "Low - Standard feature set"
        },

        storyPoints: 13,
        estimationReasoning: [
          "Advanced filtering requires careful UI design",
          "PDF/CSV export adds some complexity",
          "Database query optimization needed for performance",
          "Standard web application features with some extra polish"
        ]
      }
    ];

    return userStories;
  }

  // Capacity Planning for Team
  calculateTeamCapacity(sprintLength = 10) { // 10 working days
    const teamMembers = [
      {
        name: "Alex Rodriguez",
        role: "Full-Stack Developer",
        hoursPerDay: 8,
        availability: 0.85, // 85% (accounting for meetings, email, etc.)
        plannedTimeOff: 0, // days off during sprint
        commitments: {
          codeReviews: 4, // hours per sprint
          mentoring: 2,
          architecture: 3
        }
      },
      {
        name: "Lisa Wang",
        role: "Frontend Developer",
        hoursPerDay: 8,
        availability: 0.90,
        plannedTimeOff: 1, // 1 day off
        commitments: {
          codeReviews: 3,
          designSystem: 4,
          accessibility: 2
        }
      },
      {
        name: "David Kim",
        role: "Backend Developer",
        hoursPerDay: 8,
        availability: 0.80, // Lower due to production support
        plannedTimeOff: 0,
        commitments: {
          productionSupport: 8,
          codeReviews: 4,
          databaseOptimization: 3
        }
      },
      {
        name: "Emma Thompson",
        role: "QA Engineer",
        hoursPerDay: 8,
        availability: 0.85,
        plannedTimeOff: 2, // 2 days off
        commitments: {
          testAutomation: 6,
          bugTriage: 4,
          testPlanning: 3
        }
      }
    ];

    const capacityCalculation = teamMembers.map(member => {
      const workingDays = sprintLength - member.plannedTimeOff;
      const totalHours = workingDays * member.hoursPerDay * member.availability;
      const commitmentHours = Object.values(member.commitments).reduce((a, b) => a + b, 0);
      const availableHours = totalHours - commitmentHours;

      return {
        ...member,
        workingDays,
        totalHours: Math.round(totalHours),
        commitmentHours,
        availableHours: Math.round(availableHours),
        storyPointsCapacity: Math.round(availableHours / 6) // Assuming 6 hours per story point
      };
    });

    const totalCapacity = {
      totalHours: capacityCalculation.reduce((sum, member) => sum + member.availableHours, 0),
      totalStoryPoints: capacityCalculation.reduce((sum, member) => sum + member.storyPointsCapacity, 0)
    };

    return {
      individuals: capacityCalculation,
      team: totalCapacity
    };
  }

  // Velocity-Based Planning
  calculateVelocityBasedCommitment() {
    const historicalVelocity = [
      { sprint: 1, planned: 45, completed: 42 },
      { sprint: 2, planned: 48, completed: 50 },
      { sprint: 3, planned: 50, completed: 47 },
      { sprint: 4, planned: 49, completed: 51 },
      { sprint: 5, planned: 52, completed: 49 }
    ];

    const analysis = {
      averageVelocity: historicalVelocity.reduce((sum, sprint) => sum + sprint.completed, 0) / historicalVelocity.length,

      consistencyAnalysis: {
        standardDeviation: this.calculateStandardDeviation(historicalVelocity.map(s => s.completed)),
        stabilityRating: "Good", // Based on low standard deviation
      },

      trendAnalysis: {
        trend: "Stable with slight improvement",
        confidence: "High",
        recommendedCommitment: 48 // Conservative based on historical data
      },

      factorsAffectingVelocity: [
        "Team member vacation schedules",
        "Complexity of current sprint backlog",
        "Technical debt paydown efforts",
        "External dependencies and blockers",
        "New team member onboarding"
      ]
    };

    return analysis;
  }

  // Sprint Commitment Process
  finalizeSprintCommitment(stories, teamCapacity, velocityData) {
    const commitmentProcess = {
      step1_initialSelection: {
        description: "Select stories based on priority and estimated effort",
        selectedStories: stories.slice(0, 4), // Top 4 priority stories
        totalPoints: stories.slice(0, 4).reduce((sum, story) => sum + story.storyPoints, 0)
      },

      step2_capacityValidation: {
        description: "Validate selection against team capacity",
        teamCapacityPoints: teamCapacity.team.totalStoryPoints,
        selectionFitsCapacity: true,
        adjustmentsNeeded: false
      },

      step3_velocityCheck: {
        description: "Compare against historical velocity",
        historicalAverage: velocityData.averageVelocity,
        currentCommitment: 50,
        riskAssessment: "Medium - slightly above average",
        mitigationStrategies: [
          "Identify stories that could be descoped if needed",
          "Plan for pair programming on complex features",
          "Prepare contingency plan for technical blockers"
        ]
      },

      step4_finalCommitment: {
        sprintGoal: "Deliver core money transfer functionality with enhanced security",
        committedStories: [
          "US-001: Account Balance Real-time Updates (8 pts)",
          "US-002: Money Transfer with Fraud Detection (21 pts)",
          "US-003: Transaction History with Advanced Filtering (13 pts)",
          "US-004: Mobile App Performance Optimization (8 pts)"
        ],
        totalCommitment: 50,
        confidence: "Medium-High",
        riskFactors: [
          "Fraud detection API integration complexity",
          "Mobile performance testing requirements",
          "Potential scope creep on security features"
        ]
      }
    };

    return commitmentProcess;
  }

  // Sprint Planning Meeting Structure
  structureSprintPlanningMeeting() {
    return {
      duration: "4 hours for 2-week sprint",
      participants: ["Product Owner", "Scrum Master", "Development Team"],

      agenda: [
        {
          section: "Sprint Planning Part 1 - What?",
          duration: "2 hours",
          activities: [
            {
              activity: "Review sprint goal and product backlog",
              duration: "30 minutes",
              owner: "Product Owner",
              outcome: "Shared understanding of priorities"
            },
            {
              activity: "Estimate and select user stories",
              duration: "60 minutes",
              owner: "Development Team",
              outcome: "Initial story selection"
            },
            {
              activity: "Validate capacity and velocity",
              duration: "20 minutes",
              owner: "Scrum Master",
              outcome: "Realistic commitment level"
            },
            {
              activity: "Finalize sprint backlog",
              duration: "10 minutes",
              owner: "Product Owner + Team",
              outcome: "Committed sprint backlog"
            }
          ]
        },

        {
          section: "Sprint Planning Part 2 - How?",
          duration: "2 hours",
          activities: [
            {
              activity: "Break down stories into tasks",
              duration: "60 minutes",
              owner: "Development Team",
              outcome: "Detailed task breakdown"
            },
            {
              activity: "Identify dependencies and risks",
              duration: "30 minutes",
              owner: "Development Team",
              outcome: "Risk mitigation plan"
            },
            {
              activity: "Assign initial story ownership",
              duration: "20 minutes",
              owner: "Development Team",
              outcome: "Clear accountability"
            },
            {
              activity: "Confirm sprint commitment",
              duration: "10 minutes",
              owner: "Entire Team",
              outcome: "Team consensus on deliverables"
            }
          ]
        }
      ],

      outputs: [
        "Sprint goal statement",
        "Sprint backlog with estimated stories",
        "Task breakdown for each story",
        "Sprint capacity plan",
        "Risk and dependency identification",
        "Definition of Done confirmation"
      ]
    };
  }

  // Utility function for standard deviation
  calculateStandardDeviation(values) {
    const mean = values.reduce((sum, value) => sum + value, 0) / values.length;
    const squaredDifferences = values.map(value => Math.pow(value - mean, 2));
    const variance = squaredDifferences.reduce((sum, diff) => sum + diff, 0) / values.length;
    return Math.sqrt(variance);
  }
}

export { SprintPlanningFramework };
```

---