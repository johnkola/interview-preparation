import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import './App.css';
import BasicHooks from './components/BasicHooks';
import CustomHooks from './components/CustomHooks';
import ContextExample from './components/ContextExample';
import ReducerExample from './components/ReducerExample';
import CompositionPatterns from './components/CompositionPatterns';
import AdvancedHooks from './components/AdvancedHooks';
import AllHooksExample from './components/AllHooksExample';

function App() {
  return (
    <Router>
      <div className="App">
        <nav className="navbar">
          <h1>React Hooks & Patterns Learning 🎯</h1>
          <div className="nav-links">
            <Link to="/all-hooks">🚀 All Hooks</Link>
            <Link to="/basic-hooks">Basic Hooks</Link>
            <Link to="/custom-hooks">Custom Hooks</Link>
            <Link to="/advanced-hooks">Advanced Hooks</Link>
            <Link to="/context">Context API</Link>
            <Link to="/reducer">useReducer</Link>
            <Link to="/composition">Composition</Link>
          </div>
        </nav>

        <main className="main-content">
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/all-hooks" element={<AllHooksExample />} />
            <Route path="/basic-hooks" element={<BasicHooks />} />
            <Route path="/custom-hooks" element={<CustomHooks />} />
            <Route path="/advanced-hooks" element={<AdvancedHooks />} />
            <Route path="/context" element={<ContextExample />} />
            <Route path="/reducer" element={<ReducerExample />} />
            <Route path="/composition" element={<CompositionPatterns />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

function Home() {
  return (
    <div className="home">
      <h2>Welcome to React Learning Clone</h2>
      <p>This project demonstrates various React hooks and patterns:</p>

      <div style={{ padding: '20px', backgroundColor: '#e3f2fd', borderRadius: '8px', margin: '20px 0', border: '2px solid #2196f3' }}>
        <h3>🚀 NEW: All Hooks in One Place!</h3>
        <p>Check out the <strong>"🚀 All Hooks"</strong> section to see every React hook working together in a single comprehensive example!</p>
      </div>

      <ul>
        <li><strong>🚀 All Hooks:</strong> Every React hook demonstrated together in one component</li>
        <li><strong>Basic Hooks:</strong> useState, useEffect, useRef</li>
        <li><strong>Custom Hooks:</strong> Creating reusable stateful logic</li>
        <li><strong>Advanced Hooks:</strong> useCallback, useMemo, useImperativeHandle, useLayoutEffect, useDebugValue</li>
        <li><strong>Context API:</strong> Global state management</li>
        <li><strong>useReducer:</strong> Complex state management</li>
        <li><strong>Composition:</strong> Higher-order components and render props</li>
      </ul>
      <p>Navigate through the examples using the menu above!</p>
    </div>
  );
}

export default App;
