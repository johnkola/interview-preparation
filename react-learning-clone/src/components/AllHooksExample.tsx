import React, { useState, useEffect, useRef, useCallback, useMemo, useContext, createContext, ReactNode } from 'react';

// Simple Context for useContext demonstration
const MessageContext = createContext<{
  message: string;
  setMessage: (msg: string) => void;
} | null>(null);

function MessageProvider({ children }: { children: ReactNode }) {
  const [message, setMessage] = useState('Hello from useContext!');
  return (
    <MessageContext.Provider value={{ message, setMessage }}>
      {children}
    </MessageContext.Provider>
  );
}

function UseContextDemo() {
  const context = useContext(MessageContext);

  if (!context) {
    return <div>Error: useContext must be used within MessageProvider</div>;
  }

  const { message, setMessage } = context;

  return (
    <div className="hook-card" style={{ border: '1px solid #e2e8f0', borderRadius: '8px', padding: '15px', backgroundColor: 'white', margin: '10px 0' }}>
      <h3>🎯 useContext Hook</h3>

      <div style={{ backgroundColor: '#fffbeb', padding: '8px', borderRadius: '4px', marginBottom: '10px', fontSize: '14px' }}>
        <strong>📝 Remember:</strong> Consumes context values directly. Avoids prop drilling. Must be used within a Provider.
      </div>

      <div style={{ backgroundColor: '#f0f0f0', padding: '10px', borderRadius: '4px', marginBottom: '10px' }}>
        <strong>Code:</strong>
        <br />
        <code style={{ fontSize: '12px' }}>
          const context = useContext(MessageContext);<br />
          const {`{message, setMessage}`} = context;
        </code>
      </div>

      <p><strong>Message from useContext:</strong> "{message}"</p>

      <input
        type="text"
        value={message}
        onChange={(e) => setMessage(e.target.value)}
        placeholder="Update context message..."
        style={{ padding: '8px', marginRight: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
      />

      <button onClick={() => setMessage('Hello from useContext!')}>
        Reset Message
      </button>

      <p style={{ fontSize: '14px', color: '#666' }}>
        <em>This demonstrates the useContext hook consuming context directly</em>
      </p>
    </div>
  );
}

function BasicHooksDemo() {
  const [count, setCount] = useState(0);
  const [text, setText] = useState('');
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    document.title = `All Hooks Demo - Count: ${count}`;
    return () => {
      document.title = 'React Learning Clone';
    };
  }, [count]);

  const focusInput = useCallback(() => {
    inputRef.current?.focus();
  }, []);

  const expensiveCalculation = useMemo(() => {
    console.log('Running expensive calculation...');
    return count * 1000;
  }, [count]);

  return (
    <div>
      <div className="hook-card" style={{ border: '1px solid #e2e8f0', borderRadius: '8px', padding: '15px', backgroundColor: 'white', margin: '10px 0' }}>
        <h3>🎯 useState Hook</h3>
        <div style={{ backgroundColor: '#fff5f5', padding: '8px', borderRadius: '4px', marginBottom: '10px', fontSize: '14px' }}>
          <strong>📝 Remember:</strong> Adds state to functional components. Returns [value, setter].
        </div>
        <p>Count: {count}</p>
        <button onClick={() => setCount(c => c + 1)}>Increment</button>
        <button onClick={() => setCount(c => c - 1)}>Decrement</button>
        <button onClick={() => setCount(0)}>Reset</button>
      </div>

      <div className="hook-card" style={{ border: '1px solid #e2e8f0', borderRadius: '8px', padding: '15px', backgroundColor: 'white', margin: '10px 0' }}>
        <h3>⏰ useEffect Hook</h3>
        <div style={{ backgroundColor: '#f0f9ff', padding: '8px', borderRadius: '4px', marginBottom: '10px', fontSize: '14px' }}>
          <strong>📝 Remember:</strong> Runs side effects after render. Like componentDidMount + componentDidUpdate + componentWillUnmount.
        </div>
        <p>Count: {count}</p>
        <p><small>Check browser title - it updates with the count!</small></p>
      </div>

      <div className="hook-card" style={{ border: '1px solid #e2e8f0', borderRadius: '8px', padding: '15px', backgroundColor: 'white', margin: '10px 0' }}>
        <h3>🎯 useRef Hook</h3>
        <div style={{ backgroundColor: '#f0fdf4', padding: '8px', borderRadius: '4px', marginBottom: '10px', fontSize: '14px' }}>
          <strong>📝 Remember:</strong> Access DOM elements directly. Persists values between renders without triggering re-render.
        </div>
        <input
          ref={inputRef}
          value={text}
          onChange={(e) => setText(e.target.value)}
          placeholder="Type here..."
          style={{ padding: '8px', marginRight: '8px', borderRadius: '4px', border: '1px solid #ccc' }}
        />
        <button onClick={focusInput}>Focus Input</button>
      </div>

      <div className="hook-card" style={{ border: '1px solid #e2e8f0', borderRadius: '8px', padding: '15px', backgroundColor: 'white', margin: '10px 0' }}>
        <h3>🚀 useCallback + useMemo Hooks</h3>
        <div style={{ backgroundColor: '#fdf4ff', padding: '8px', borderRadius: '4px', marginBottom: '10px', fontSize: '14px' }}>
          <strong>📝 Remember:</strong> useCallback memoizes functions, useMemo memoizes values. Both prevent unnecessary re-computations.
        </div>
        <p>Count: {count}</p>
        <p>Expensive calculation result: {expensiveCalculation}</p>
        <p><small>Check console - calculation only runs when count changes!</small></p>
      </div>
    </div>
  );
}

function AllHooksExample() {
  return (
    <MessageProvider>
      <div style={{ padding: '20px' }}>
        <h2>🚀 All React Hooks in One Place!</h2>
        <p>This page demonstrates various React hooks working together!</p>

        <BasicHooksDemo />
        <UseContextDemo />

        <div style={{ marginTop: '30px', padding: '20px', border: '2px solid #4299e1', borderRadius: '8px', backgroundColor: '#ebf8ff' }}>
          <h3>🎉 React Hooks Quick Reference</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(300px, 1fr))', gap: '15px' }}>
            <div style={{ backgroundColor: 'white', padding: '10px', borderRadius: '4px' }}>
              <strong>useState</strong> - Adds state to functional components
              <br />
              <code style={{ fontSize: '12px' }}>const [value, setValue] = useState(initial)</code>
            </div>
            <div style={{ backgroundColor: 'white', padding: '10px', borderRadius: '4px' }}>
              <strong>useEffect</strong> - Side effects after render
              <br />
              <code style={{ fontSize: '12px' }}>useEffect(() => effect, [deps])</code>
            </div>
            <div style={{ backgroundColor: 'white', padding: '10px', borderRadius: '4px' }}>
              <strong>useRef</strong> - DOM access without re-render
              <br />
              <code style={{ fontSize: '12px' }}>const ref = useRef(null)</code>
            </div>
            <div style={{ backgroundColor: 'white', padding: '10px', borderRadius: '4px' }}>
              <strong>useContext</strong> - Consume context directly
              <br />
              <code style={{ fontSize: '12px' }}>const value = useContext(Context)</code>
            </div>
            <div style={{ backgroundColor: 'white', padding: '10px', borderRadius: '4px' }}>
              <strong>useCallback</strong> - Memoize functions
              <br />
              <code style={{ fontSize: '12px' }}>const fn = useCallback(() => func, [deps])</code>
            </div>
            <div style={{ backgroundColor: 'white', padding: '10px', borderRadius: '4px' }}>
              <strong>useMemo</strong> - Memoize expensive calculations
              <br />
              <code style={{ fontSize: '12px' }}>const value = useMemo(() => calc(), [deps])</code>
            </div>
          </div>
          <p style={{ marginTop: '15px', textAlign: 'center' }}><em>💡 All hooks work together to build powerful React components!</em></p>
        </div>
      </div>
    </MessageProvider>
  );
}

export default AllHooksExample;