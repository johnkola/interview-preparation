import React, { useState, useEffect, useRef } from 'react';

function BasicHooks() {
  return (
    <div className="basic-hooks">
      <h2>Basic Hooks Examples</h2>
      <CounterExample />
      <EffectExample />
      <RefExample />
    </div>
  );
}

function CounterExample() {
  // useState Hook: Manages state in functional components
  // Syntax: const [stateVariable, setStateFunction] = useState(initialValue)
  // Returns: A pair - current state value and a function to update it
  const [count, setCount] = useState(0);
  const [name, setName] = useState('');

  return (
    <div className="example">
      <h3>useState Hook</h3>
      <p>Count: {count}</p>
      {/* Three ways to update state:
          1. Direct value: setCount(count + 1)
          2. Functional update: setCount(prev => prev + 1) - better for async updates
          3. Replace entirely: setCount(0) */}
      <button onClick={() => setCount(count + 1)}>Increment</button>
      <button onClick={() => setCount(count - 1)}>Decrement</button>
      <button onClick={() => setCount(0)}>Reset</button>

      <div>
        {/* Controlled input: value comes from state, onChange updates state */}
        <input
          type="text"
          value={name}
          onChange={(e) => setName(e.target.value)}
          placeholder="Enter your name"
        />
        <p>Hello, {name || 'Anonymous'}!</p>
      </div>
    </div>
  );
}

function EffectExample() {
  const [seconds, setSeconds] = useState(0);
  const [isRunning, setIsRunning] = useState(false);

  // useEffect Hook #1: Perform side effects in functional components
  // This effect updates the document title whenever 'seconds' changes
  useEffect(() => {
    // Effect function: Runs after render when dependencies change
    document.title = `Timer: ${seconds}s`;

    // Cleanup function (return value): CRITICAL CONCEPT
    // WHY RETURN A FUNCTION?
    // 1. Prevents memory leaks by cleaning up subscriptions, timers, etc.
    // 2. Runs before component unmounts OR before effect runs again
    // 3. Ensures no stale closures or orphaned operations
    return () => {
      // This cleanup resets the title when:
      // - Component unmounts
      // - Before the effect runs again (when seconds changes)
      document.title = 'React Learning Clone';
    };
  }, [seconds]); // Dependency array: Effect only runs when 'seconds' changes

  // useEffect Hook #2: Managing a timer with cleanup
  useEffect(() => {
    let interval: NodeJS.Timeout | null = null;

    if (isRunning) {
      // Start interval when running
      interval = setInterval(() => {
        // Using functional update to avoid stale closure issues
        // setSeconds(s => s + 1) is safer than setSeconds(seconds + 1)
        setSeconds(s => s + 1);
      }, 1000);
    } else if (!isRunning && seconds !== 0) {
      clearInterval(interval!);
    }

    // CLEANUP FUNCTION - WHY IT'S ESSENTIAL HERE:
    // Without this return statement:
    // 1. Multiple intervals would stack up (memory leak)
    // 2. Timer would continue after component unmounts
    // 3. Old intervals would interfere with new ones
    return () => {
      // This cleanup ensures:
      // - Only one interval exists at a time
      // - Timer stops when component unmounts
      // - No memory leaks from orphaned intervals
      if (interval) clearInterval(interval);
    };
  }, [isRunning, seconds]); // Re-run effect when these deps change

  return (
    <div className="example">
      <h3>useEffect Hook</h3>
      <p>Timer: {seconds} seconds</p>
      <button onClick={() => setIsRunning(!isRunning)}>
        {isRunning ? 'Pause' : 'Start'}
      </button>
      <button onClick={() => setSeconds(0)}>Reset</button>
      <p><em>Check the browser tab title - it updates with the timer!</em></p>
    </div>
  );
}

function RefExample() {
  // ====================================================================
  // useRef Hook: Creates a mutable reference that persists across renders
  // ====================================================================

  // KEY CONCEPTS:
  // 1. Does NOT cause re-renders when changed (unlike useState)
  // 2. Persists value between renders (unlike regular variables)
  // 3. Common uses: DOM references, storing previous values, timers/intervals

  // SYNTAX: const refContainer = useRef(initialValue)
  // - initialValue: The initial value for .current property
  // - Returns: An object with a .current property { current: initialValue }

  // Example 1: DOM Reference (most common use case)
  const inputRef = useRef<HTMLInputElement>(null);
  const [value, setValue] = useState('');

  // Example 2: Storing mutable values (commented examples)
  // const renderCount = useRef(0); // Tracks renders without causing re-render
  // const previousValue = useRef(''); // Stores previous state value
  // const timerIdRef = useRef<NodeJS.Timeout>(); // Stores timer ID for cleanup

  // ====================================================================
  // PRACTICAL EXAMPLES OF useRef USAGE
  // ====================================================================

  // Pattern 1: Direct DOM manipulation
  const focusInput = () => {
    // .current holds the actual DOM element after component mounts
    // Initially null, becomes HTMLInputElement after first render
    // Optional chaining (?.) prevents errors if ref isn't attached yet
    inputRef.current?.focus();

    // Other DOM methods you could call:
    // inputRef.current?.select(); // Select all text
    // inputRef.current?.blur(); // Remove focus
    // inputRef.current?.scrollIntoView(); // Scroll to element
  };

  // Pattern 2: Combining state updates with DOM manipulation
  const clearAndFocus = () => {
    setValue(''); // State change causes re-render and updates UI
    inputRef.current?.focus(); // Direct DOM manipulation, no re-render

    // This demonstrates the key difference:
    // - setValue('') → Triggers re-render → React updates DOM
    // - inputRef.current.focus() → Direct DOM call → No re-render
  };

  // ====================================================================
  // COMMON useRef PATTERNS AND USE CASES
  // ====================================================================

  // Pattern 3: Storing previous values (useful for comparisons)
  // const prevValueRef = useRef();
  // useEffect(() => {
  //   prevValueRef.current = value; // Store current value for next render
  // });

  // Pattern 4: Storing interval/timeout IDs for cleanup
  // const intervalRef = useRef<NodeJS.Timeout>();
  // useEffect(() => {
  //   intervalRef.current = setInterval(() => {}, 1000);
  //   return () => clearInterval(intervalRef.current);
  // }, []);

  // Pattern 5: Tracking component mount status
  // const isMountedRef = useRef(true);
  // useEffect(() => {
  //   return () => { isMountedRef.current = false; };
  // }, []);

  // ====================================================================
  // useRef vs useState - WHEN TO USE WHICH?
  // ====================================================================

  // USE useState WHEN:
  // - Value changes should trigger re-render
  // - Value is displayed in UI
  // - Value affects component appearance
  // Example: const [count, setCount] = useState(0);

  // USE useRef WHEN:
  // - Need to store value without re-rendering
  // - Accessing DOM elements directly
  // - Storing previous values for comparison
  // - Storing timer/interval IDs
  // - Tracking mount/unmount status
  // Example: const timerRef = useRef();

  // ====================================================================
  // IMPORTANT GOTCHAS AND BEST PRACTICES
  // ====================================================================

  // GOTCHA 1: ref.current is mutable - changes don't trigger re-renders
  // Wrong: {inputRef.current?.value} // Won't update in UI
  // Right: {value} // Use state for UI values

  // GOTCHA 2: Don't read ref.current during rendering
  // Wrong: const val = inputRef.current?.value; // May be null/stale
  // Right: Read in event handlers or effects

  // GOTCHA 3: Initial render has null for DOM refs
  // Always check if ref.current exists before using

  return (
    <div className="example">
      <h3>useRef Hook</h3>

      {/* The ref prop connects this DOM element to our useRef hook */}
      {/* After mount: inputRef.current === this input element */}
      <input
        ref={inputRef}
        type="text"
        value={value}
        onChange={(e) => setValue(e.target.value)}
        placeholder="Click buttons to interact"
      />

      <div>
        <button onClick={focusInput}>Focus Input</button>
        <button onClick={clearAndFocus}>Clear & Focus</button>
      </div>

      <p>useRef allows direct DOM manipulation without re-renders</p>
      <p><small>Tip: useRef is perfect for storing values that shouldn't trigger re-renders</small></p>

      {/* Additional explanation for learning */}
      <div style={{ fontSize: '12px', marginTop: '10px', color: '#666' }}>
        <strong>Try this:</strong> Type something, click "Clear & Focus" - notice the input
        clears (state change) AND stays focused (ref manipulation) in one action!
      </div>
    </div>
  );
}

export default BasicHooks;