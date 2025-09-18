import React, { useState, useCallback, useMemo, useContext, createContext, useImperativeHandle, forwardRef, useLayoutEffect, useRef, useDebugValue } from 'react';

function AdvancedHooks() {
  return (
    <div className="advanced-hooks">
      <h2>Advanced & Performance Hooks</h2>
      <CallbackExample />
      <MemoExample />
      <ContextExample />
      <ImperativeHandleExample />
      <LayoutEffectExample />
      <DebugValueExample />
    </div>
  );
}

function CallbackExample() {
  const [count, setCount] = useState(0);
  const [todos, setTodos] = useState<string[]>([]);
  const [inputValue, setInputValue] = useState('');

  const expensiveCalculation = useCallback((num: number) => {
    console.log('Expensive calculation running...');
    let result = 0;
    for (let i = 0; i < 1000000; i++) {
      result += num;
    }
    return result;
  }, []);

  const addTodo = useCallback(() => {
    if (inputValue.trim()) {
      setTodos(prev => [...prev, inputValue.trim()]);
      setInputValue('');
    }
  }, [inputValue]);

  const removeTodo = useCallback((index: number) => {
    setTodos(prev => prev.filter((_, i) => i !== index));
  }, []);

  return (
    <div className="example">
      <h3>useCallback Hook</h3>
      <p>Prevents unnecessary re-creation of functions on every render</p>

      <div>
        <p>Count: {count}</p>
        <button onClick={() => setCount(c => c + 1)}>Increment Count</button>
        <p>Expensive calculation result: {expensiveCalculation(count)}</p>
      </div>

      <div>
        <h4>Todo List with useCallback</h4>
        <input
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          placeholder="Add todo..."
          onKeyPress={(e) => e.key === 'Enter' && addTodo()}
        />
        <button onClick={addTodo}>Add Todo</button>

        <ul>
          {todos.map((todo, index) => (
            <li key={index}>
              {todo}
              <button onClick={() => removeTodo(index)} style={{ marginLeft: '10px' }}>
                Remove
              </button>
            </li>
          ))}
        </ul>
        <p><em>Check console - expensive calculation only runs when count changes!</em></p>
      </div>
    </div>
  );
}

function MemoExample() {
  const [count, setCount] = useState(0);
  const [items] = useState(() => Array.from({ length: 10000 }, (_, i) => i + 1));
  const [filter, setFilter] = useState('');

  const expensiveValue = useMemo(() => {
    console.log('Computing expensive value...');
    return items.reduce((sum, item) => sum + item, 0);
  }, [items]);

  const filteredItems = useMemo(() => {
    console.log('Filtering items...');
    if (!filter) return items.slice(0, 100);
    return items.filter(item => item.toString().includes(filter)).slice(0, 100);
  }, [items, filter]);

  const averageValue = useMemo(() => {
    console.log('Computing average...');
    return filteredItems.length > 0 ?
      filteredItems.reduce((sum, item) => sum + item, 0) / filteredItems.length : 0;
  }, [filteredItems]);

  return (
    <div className="example">
      <h3>useMemo Hook</h3>
      <p>Memoizes expensive calculations to avoid unnecessary recalculations</p>

      <div>
        <p>Count: {count} (doesn't affect expensive calculations)</p>
        <button onClick={() => setCount(c => c + 1)}>Increment Count</button>
      </div>

      <div>
        <p>Sum of all items: {expensiveValue.toLocaleString()}</p>

        <input
          value={filter}
          onChange={(e) => setFilter(e.target.value)}
          placeholder="Filter items..."
        />

        <p>Filtered items count: {filteredItems.length}</p>
        <p>Average of filtered items: {averageValue.toFixed(2)}</p>

        <div style={{ maxHeight: '200px', overflowY: 'auto', border: '1px solid #ccc', padding: '10px' }}>
          {filteredItems.map(item => (
            <span key={item} style={{ marginRight: '5px' }}>{item}</span>
          ))}
        </div>
        <p><em>Check console - calculations only run when dependencies change!</em></p>
      </div>
    </div>
  );
}

const SimpleContext = createContext<{
  message: string;
  updateMessage: (msg: string) => void;
} | null>(null);

function SimpleContextProvider({ children }: { children: React.ReactNode }) {
  const [message, setMessage] = useState('Hello from useContext!');

  const updateMessage = useCallback((msg: string) => {
    setMessage(msg);
  }, []);

  return (
    <SimpleContext.Provider value={{ message, updateMessage }}>
      {children}
    </SimpleContext.Provider>
  );
}

function ContextChild() {
  const context = useContext(SimpleContext);

  if (!context) {
    throw new Error('ContextChild must be used within SimpleContextProvider');
  }

  const { message, updateMessage } = context;

  return (
    <div style={{ border: '1px solid #ddd', padding: '10px', margin: '10px 0' }}>
      <p>Message from context: <strong>{message}</strong></p>
      <input
        type="text"
        onChange={(e) => updateMessage(e.target.value)}
        placeholder="Update context message"
      />
    </div>
  );
}

function ContextExample() {
  return (
    <div className="example">
      <h3>useContext Hook (Standalone)</h3>
      <p>Directly consumes context values without Consumer component</p>

      <SimpleContextProvider>
        <ContextChild />
        <ContextChild />
        <p><em>Both components share the same context state!</em></p>
      </SimpleContextProvider>
    </div>
  );
}

interface InputHandle {
  focus: () => void;
  clear: () => void;
  getValue: () => string;
}

const FancyInput = forwardRef<InputHandle, { label: string }>((props, ref) => {
  const inputRef = useRef<HTMLInputElement>(null);
  const [value, setValue] = useState('');

  useImperativeHandle(ref, () => ({
    focus: () => {
      inputRef.current?.focus();
    },
    clear: () => {
      setValue('');
      inputRef.current?.focus();
    },
    getValue: () => {
      return value;
    }
  }));

  return (
    <div>
      <label>{props.label}</label>
      <input
        ref={inputRef}
        value={value}
        onChange={(e) => setValue(e.target.value)}
        style={{ marginLeft: '10px', padding: '5px' }}
      />
    </div>
  );
});

function ImperativeHandleExample() {
  const inputRef = useRef<InputHandle>(null);
  const [output, setOutput] = useState('');

  const focusInput = () => inputRef.current?.focus();
  const clearInput = () => inputRef.current?.clear();
  const getValue = () => {
    const value = inputRef.current?.getValue();
    setOutput(value || '');
  };

  return (
    <div className="example">
      <h3>useImperativeHandle Hook</h3>
      <p>Customizes the instance value exposed to parent components when using ref</p>

      <FancyInput ref={inputRef} label="Fancy Input:" />

      <div style={{ marginTop: '10px' }}>
        <button onClick={focusInput}>Focus Input</button>
        <button onClick={clearInput}>Clear Input</button>
        <button onClick={getValue}>Get Value</button>
      </div>

      {output && <p>Current value: <strong>{output}</strong></p>}
      <p><em>Parent component can call custom methods on child!</em></p>
    </div>
  );
}

function LayoutEffectExample() {
  const [width, setWidth] = useState(0);
  const [height, setHeight] = useState(0);
  const boxRef = useRef<HTMLDivElement>(null);
  const [color, setColor] = useState('lightblue');

  useLayoutEffect(() => {
    if (boxRef.current) {
      const rect = boxRef.current.getBoundingClientRect();
      setWidth(rect.width);
      setHeight(rect.height);
    }
  }, []); // Empty dependency array to run only once

  const changeColor = () => {
    const colors = ['lightblue', 'lightgreen', 'lightcoral', 'lightyellow', 'lightpink'];
    const currentIndex = colors.indexOf(color);
    const nextIndex = (currentIndex + 1) % colors.length;
    setColor(colors[nextIndex]);
  };

  return (
    <div className="example">
      <h3>useLayoutEffect Hook</h3>
      <p>Runs synchronously after all DOM mutations but before browser paint</p>

      <div
        ref={boxRef}
        style={{
          backgroundColor: color,
          padding: '20px',
          margin: '10px 0',
          border: '2px solid #333',
          borderRadius: '8px',
          transition: 'background-color 0.3s'
        }}
      >
        <p>This box changes color and measures itself</p>
        <p>Width: {width.toFixed(2)}px</p>
        <p>Height: {height.toFixed(2)}px</p>
      </div>

      <button onClick={changeColor}>Change Color</button>
      <p><em>useLayoutEffect ensures measurements happen before paint</em></p>
    </div>
  );
}

function useCounter(initialValue = 0, step = 1) {
  const [count, setCount] = useState(initialValue);

  useDebugValue(count > 10 ? 'High' : 'Low');

  const increment = useCallback(() => setCount(c => c + step), [step]);
  const decrement = useCallback(() => setCount(c => c - step), [step]);
  const reset = useCallback(() => setCount(initialValue), [initialValue]);

  return { count, increment, decrement, reset };
}

function DebugValueExample() {
  const counter1 = useCounter(0, 1);
  const counter2 = useCounter(5, 2);

  return (
    <div className="example">
      <h3>useDebugValue Hook</h3>
      <p>Used to display a label for custom hooks in React DevTools</p>

      <div>
        <h4>Counter 1 (step: 1)</h4>
        <p>Count: {counter1.count}</p>
        <button onClick={counter1.increment}>+</button>
        <button onClick={counter1.decrement}>-</button>
        <button onClick={counter1.reset}>Reset</button>
      </div>

      <div>
        <h4>Counter 2 (step: 2)</h4>
        <p>Count: {counter2.count}</p>
        <button onClick={counter2.increment}>+</button>
        <button onClick={counter2.decrement}>-</button>
        <button onClick={counter2.reset}>Reset</button>
      </div>

      <p><em>Open React DevTools to see useDebugValue labels!</em></p>
    </div>
  );
}

export default AdvancedHooks;