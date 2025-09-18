import React, { useReducer, useState } from 'react';

interface Todo {
  id: number;
  text: string;
  completed: boolean;
}

interface TodoState {
  todos: Todo[];
  filter: 'all' | 'active' | 'completed';
}

type TodoAction =
  | { type: 'ADD_TODO'; payload: string }
  | { type: 'TOGGLE_TODO'; payload: number }
  | { type: 'DELETE_TODO'; payload: number }
  | { type: 'SET_FILTER'; payload: 'all' | 'active' | 'completed' }
  | { type: 'CLEAR_COMPLETED' }
  | { type: 'TOGGLE_ALL' };

const todoReducer = (state: TodoState, action: TodoAction): TodoState => {
  switch (action.type) {
    case 'ADD_TODO':
      return {
        ...state,
        todos: [
          ...state.todos,
          {
            id: Date.now(),
            text: action.payload,
            completed: false
          }
        ]
      };

    case 'TOGGLE_TODO':
      return {
        ...state,
        todos: state.todos.map(todo =>
          todo.id === action.payload
            ? { ...todo, completed: !todo.completed }
            : todo
        )
      };

    case 'DELETE_TODO':
      return {
        ...state,
        todos: state.todos.filter(todo => todo.id !== action.payload)
      };

    case 'SET_FILTER':
      return {
        ...state,
        filter: action.payload
      };

    case 'CLEAR_COMPLETED':
      return {
        ...state,
        todos: state.todos.filter(todo => !todo.completed)
      };

    case 'TOGGLE_ALL':
      const allCompleted = state.todos.every(todo => todo.completed);
      return {
        ...state,
        todos: state.todos.map(todo => ({
          ...todo,
          completed: !allCompleted
        }))
      };

    default:
      return state;
  }
};

interface CounterState {
  count: number;
  step: number;
}

type CounterAction =
  | { type: 'INCREMENT' }
  | { type: 'DECREMENT' }
  | { type: 'RESET' }
  | { type: 'SET_STEP'; payload: number };

const counterReducer = (state: CounterState, action: CounterAction): CounterState => {
  switch (action.type) {
    case 'INCREMENT':
      return { ...state, count: state.count + state.step };
    case 'DECREMENT':
      return { ...state, count: state.count - state.step };
    case 'RESET':
      return { ...state, count: 0 };
    case 'SET_STEP':
      return { ...state, step: action.payload };
    default:
      return state;
  }
};

function ReducerExample() {
  return (
    <div className="reducer-example">
      <h2>useReducer Hook Examples</h2>
      <CounterReducerExample />
      <TodoReducerExample />
    </div>
  );
}

function CounterReducerExample() {
  const [state, dispatch] = useReducer(counterReducer, { count: 0, step: 1 });

  return (
    <div className="example">
      <h3>Counter with useReducer</h3>
      <p>Count: {state.count}</p>
      <p>Step: {state.step}</p>

      <div>
        <button onClick={() => dispatch({ type: 'INCREMENT' })}>
          Increment by {state.step}
        </button>
        <button onClick={() => dispatch({ type: 'DECREMENT' })}>
          Decrement by {state.step}
        </button>
        <button onClick={() => dispatch({ type: 'RESET' })}>
          Reset
        </button>
      </div>

      <div>
        <label>
          Step size:
          <input
            type="number"
            value={state.step}
            onChange={(e) => dispatch({
              type: 'SET_STEP',
              payload: parseInt(e.target.value) || 1
            })}
            min="1"
          />
        </label>
      </div>

      <p><em>useReducer is better for complex state logic than useState</em></p>
    </div>
  );
}

function TodoReducerExample() {
  const [state, dispatch] = useReducer(todoReducer, {
    todos: [
      { id: 1, text: 'Learn React hooks', completed: true },
      { id: 2, text: 'Master useReducer', completed: false },
      { id: 3, text: 'Build awesome apps', completed: false }
    ],
    filter: 'all'
  });

  const [inputValue, setInputValue] = useState('');

  const addTodo = (e: React.FormEvent) => {
    e.preventDefault();
    if (inputValue.trim()) {
      dispatch({ type: 'ADD_TODO', payload: inputValue.trim() });
      setInputValue('');
    }
  };

  const filteredTodos = state.todos.filter(todo => {
    switch (state.filter) {
      case 'active':
        return !todo.completed;
      case 'completed':
        return todo.completed;
      default:
        return true;
    }
  });

  const activeTodoCount = state.todos.filter(todo => !todo.completed).length;
  const completedTodoCount = state.todos.filter(todo => todo.completed).length;

  return (
    <div className="example">
      <h3>Todo App with useReducer</h3>

      <form onSubmit={addTodo}>
        <input
          type="text"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          placeholder="Add a new todo..."
        />
        <button type="submit">Add Todo</button>
      </form>

      <div className="todo-controls">
        <button
          onClick={() => dispatch({ type: 'TOGGLE_ALL' })}
          disabled={state.todos.length === 0}
        >
          Toggle All
        </button>

        <div className="filters">
          <button
            onClick={() => dispatch({ type: 'SET_FILTER', payload: 'all' })}
            style={{ fontWeight: state.filter === 'all' ? 'bold' : 'normal' }}
          >
            All ({state.todos.length})
          </button>
          <button
            onClick={() => dispatch({ type: 'SET_FILTER', payload: 'active' })}
            style={{ fontWeight: state.filter === 'active' ? 'bold' : 'normal' }}
          >
            Active ({activeTodoCount})
          </button>
          <button
            onClick={() => dispatch({ type: 'SET_FILTER', payload: 'completed' })}
            style={{ fontWeight: state.filter === 'completed' ? 'bold' : 'normal' }}
          >
            Completed ({completedTodoCount})
          </button>
        </div>

        {completedTodoCount > 0 && (
          <button onClick={() => dispatch({ type: 'CLEAR_COMPLETED' })}>
            Clear Completed
          </button>
        )}
      </div>

      <ul className="todo-list">
        {filteredTodos.map(todo => (
          <li key={todo.id} className="todo-item">
            <input
              type="checkbox"
              checked={todo.completed}
              onChange={() => dispatch({ type: 'TOGGLE_TODO', payload: todo.id })}
            />
            <span
              style={{
                textDecoration: todo.completed ? 'line-through' : 'none',
                opacity: todo.completed ? 0.6 : 1
              }}
            >
              {todo.text}
            </span>
            <button
              onClick={() => dispatch({ type: 'DELETE_TODO', payload: todo.id })}
              style={{ marginLeft: '10px', color: 'red' }}
            >
              Delete
            </button>
          </li>
        ))}
      </ul>

      {filteredTodos.length === 0 && (
        <p><em>No todos match the current filter</em></p>
      )}

      <div className="reducer-benefits">
        <h4>useReducer Benefits:</h4>
        <ul>
          <li>Predictable state updates</li>
          <li>Complex state logic organization</li>
          <li>Better for testing (pure functions)</li>
          <li>Performance optimization opportunities</li>
          <li>Action-based state management</li>
        </ul>
      </div>
    </div>
  );
}

export default ReducerExample;