import React, { useState, useEffect } from 'react';

function useCounter(initialValue: number = 0) {
  const [count, setCount] = useState(initialValue);

  const increment = () => setCount(prev => prev + 1);
  const decrement = () => setCount(prev => prev - 1);
  const reset = () => setCount(initialValue);

  return { count, increment, decrement, reset };
}

function useLocalStorage<T>(key: string, initialValue: T) {
  const [storedValue, setStoredValue] = useState<T>(() => {
    try {
      const item = window.localStorage.getItem(key);
      return item ? JSON.parse(item) : initialValue;
    } catch (error) {
      console.error(`Error reading localStorage key "${key}":`, error);
      return initialValue;
    }
  });

  const setValue = (value: T | ((val: T) => T)) => {
    try {
      const valueToStore = value instanceof Function ? value(storedValue) : value;
      setStoredValue(valueToStore);
      window.localStorage.setItem(key, JSON.stringify(valueToStore));
    } catch (error) {
      console.error(`Error setting localStorage key "${key}":`, error);
    }
  };

  return [storedValue, setValue] as const;
}

function useFetch<T>(url: string) {
  const [data, setData] = useState<T | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        setError(null);
        const response = await fetch(url);

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();
        setData(result);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'An error occurred');
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [url]);

  return { data, loading, error };
}

function CustomHooks() {
  return (
    <div className="custom-hooks">
      <h2>Custom Hooks Examples</h2>
      <CounterHookExample />
      <LocalStorageExample />
      <FetchExample />
    </div>
  );
}

function CounterHookExample() {
  const counter1 = useCounter(0);
  const counter2 = useCounter(10);

  return (
    <div className="example">
      <h3>useCounter Custom Hook</h3>
      <div>
        <h4>Counter 1 (starts at 0)</h4>
        <p>Count: {counter1.count}</p>
        <button onClick={counter1.increment}>+</button>
        <button onClick={counter1.decrement}>-</button>
        <button onClick={counter1.reset}>Reset</button>
      </div>

      <div>
        <h4>Counter 2 (starts at 10)</h4>
        <p>Count: {counter2.count}</p>
        <button onClick={counter2.increment}>+</button>
        <button onClick={counter2.decrement}>-</button>
        <button onClick={counter2.reset}>Reset</button>
      </div>
    </div>
  );
}

function LocalStorageExample() {
  const [name, setName] = useLocalStorage('userName', '');
  const [preferences, setPreferences] = useLocalStorage('userPrefs', {
    theme: 'light',
    notifications: true
  });

  return (
    <div className="example">
      <h3>useLocalStorage Custom Hook</h3>
      <div>
        <input
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Your name (saved to localStorage)"
        />
        <p>Stored name: {name || 'None'}</p>
      </div>

      <div>
        <h4>Preferences:</h4>
        <label>
          <input
            type="checkbox"
            checked={preferences.notifications}
            onChange={(e) => setPreferences(prev => ({
              ...prev,
              notifications: e.target.checked
            }))}
          />
          Enable notifications
        </label>

        <select
          value={preferences.theme}
          onChange={(e) => setPreferences(prev => ({
            ...prev,
            theme: e.target.value
          }))}
        >
          <option value="light">Light</option>
          <option value="dark">Dark</option>
        </select>

        <p>Current preferences: {JSON.stringify(preferences)}</p>
      </div>
    </div>
  );
}

interface Post {
  id: number;
  title: string;
  body: string;
}

function FetchExample() {
  const { data: posts, loading, error } = useFetch<Post[]>('https://jsonplaceholder.typicode.com/posts?_limit=3');

  return (
    <div className="example">
      <h3>useFetch Custom Hook</h3>

      {loading && <p>Loading posts...</p>}
      {error && <p style={{ color: 'red' }}>Error: {error}</p>}

      {posts && (
        <div>
          <h4>Sample Posts:</h4>
          {posts.map(post => (
            <div key={post.id} style={{ border: '1px solid #ccc', margin: '10px 0', padding: '10px' }}>
              <h5>{post.title}</h5>
              <p>{post.body}</p>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

export default CustomHooks;