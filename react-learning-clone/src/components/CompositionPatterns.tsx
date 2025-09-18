import React, { Component, ReactNode, useState } from 'react';

interface WithLoadingProps {
  isLoading: boolean;
}

function withLoading<P extends object>(
  WrappedComponent: React.ComponentType<P>
) {
  return function WithLoadingComponent(props: P & WithLoadingProps) {
    const { isLoading, ...rest } = props;

    if (isLoading) {
      return <div>Loading...</div>;
    }

    return <WrappedComponent {...(rest as P)} />;
  };
}

interface User {
  id: number;
  name: string;
  email: string;
}

function UserProfile({ user }: { user: User }) {
  return (
    <div style={{ border: '1px solid #ccc', padding: '10px', margin: '10px 0' }}>
      <h4>{user.name}</h4>
      <p>Email: {user.email}</p>
      <p>ID: {user.id}</p>
    </div>
  );
}

const UserProfileWithLoading = withLoading(UserProfile);

interface RenderPropsComponentProps {
  children: (data: { count: number; increment: () => void; decrement: () => void }) => ReactNode;
}

function CounterRenderProps({ children }: RenderPropsComponentProps) {
  const [count, setCount] = useState(0);

  const increment = () => setCount(prev => prev + 1);
  const decrement = () => setCount(prev => prev - 1);

  return <>{children({ count, increment, decrement })}</>;
}

interface MouseTrackerProps {
  children: (mousePosition: { x: number; y: number }) => ReactNode;
}

function MouseTracker({ children }: MouseTrackerProps) {
  const [position, setPosition] = useState({ x: 0, y: 0 });

  const handleMouseMove = (e: React.MouseEvent) => {
    setPosition({ x: e.clientX, y: e.clientY });
  };

  return (
    <div onMouseMove={handleMouseMove} style={{ height: '200px', border: '2px dashed #ccc', position: 'relative' }}>
      {children(position)}
    </div>
  );
}

interface CardProps {
  title?: string;
  children: ReactNode;
  footer?: ReactNode;
}

function Card({ title, children, footer }: CardProps) {
  return (
    <div style={{ border: '1px solid #ddd', borderRadius: '8px', overflow: 'hidden', margin: '10px 0' }}>
      {title && (
        <div style={{ backgroundColor: '#f8f9fa', padding: '15px', borderBottom: '1px solid #ddd' }}>
          <h3 style={{ margin: 0 }}>{title}</h3>
        </div>
      )}
      <div style={{ padding: '15px' }}>
        {children}
      </div>
      {footer && (
        <div style={{ backgroundColor: '#f8f9fa', padding: '15px', borderTop: '1px solid #ddd' }}>
          {footer}
        </div>
      )}
    </div>
  );
}

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  children: ReactNode;
}

function Modal({ isOpen, onClose, children }: ModalProps) {
  if (!isOpen) return null;

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0,0,0,0.5)',
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      zIndex: 1000
    }}>
      <div style={{
        backgroundColor: 'white',
        padding: '20px',
        borderRadius: '8px',
        maxWidth: '500px',
        width: '90%',
        position: 'relative'
      }}>
        <button
          onClick={onClose}
          style={{
            position: 'absolute',
            top: '10px',
            right: '15px',
            border: 'none',
            background: 'none',
            fontSize: '20px',
            cursor: 'pointer'
          }}
        >
          ×
        </button>
        {children}
      </div>
    </div>
  );
}

interface ErrorBoundaryState {
  hasError: boolean;
  error: Error | null;
}

class ErrorBoundary extends Component<{ children: ReactNode }, ErrorBoundaryState> {
  constructor(props: { children: ReactNode }) {
    super(props);
    this.state = { hasError: false, error: null };
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error };
  }

  componentDidCatch(error: Error, errorInfo: React.ErrorInfo) {
    console.error('Error caught by boundary:', error, errorInfo);
  }

  render() {
    if (this.state.hasError) {
      return (
        <div style={{ padding: '20px', border: '2px solid red', borderRadius: '8px', margin: '10px 0' }}>
          <h3>Something went wrong!</h3>
          <p>Error: {this.state.error?.message}</p>
          <button onClick={() => this.setState({ hasError: false, error: null })}>
            Try Again
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}

function BuggyComponent() {
  const [shouldError, setShouldError] = useState(false);

  if (shouldError) {
    throw new Error('This is a simulated error!');
  }

  return (
    <div>
      <p>This component can throw an error</p>
      <button onClick={() => setShouldError(true)}>
        Throw Error
      </button>
    </div>
  );
}

function CompositionPatterns() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const sampleUser: User = {
    id: 1,
    name: 'John Doe',
    email: 'john@example.com'
  };

  const simulateLoading = () => {
    setIsLoading(true);
    setTimeout(() => setIsLoading(false), 2000);
  };

  return (
    <div className="composition-patterns">
      <h2>Component Composition Patterns</h2>

      <div className="example">
        <h3>Higher-Order Component (HOC)</h3>
        <p>A HOC is a function that takes a component and returns a new component with enhanced functionality.</p>

        <button onClick={simulateLoading} disabled={isLoading}>
          {isLoading ? 'Loading...' : 'Load User Profile'}
        </button>

        <UserProfileWithLoading user={sampleUser} isLoading={isLoading} />
      </div>

      <div className="example">
        <h3>Render Props Pattern</h3>
        <p>A component that provides data through a function as children.</p>

        <Card title="Counter Render Props">
          <CounterRenderProps>
            {({ count, increment, decrement }) => (
              <div>
                <p>Count: {count}</p>
                <button onClick={increment}>+</button>
                <button onClick={decrement}>-</button>
              </div>
            )}
          </CounterRenderProps>
        </Card>

        <Card title="Mouse Tracker">
          <MouseTracker>
            {({ x, y }) => (
              <div>
                <p>Mouse position: ({x}, {y})</p>
                <div
                  style={{
                    position: 'absolute',
                    left: x - 5,
                    top: y - 5,
                    width: '10px',
                    height: '10px',
                    backgroundColor: 'red',
                    borderRadius: '50%',
                    pointerEvents: 'none'
                  }}
                />
                <p><em>Move your mouse over this area!</em></p>
              </div>
            )}
          </MouseTracker>
        </Card>
      </div>

      <div className="example">
        <h3>Compound Components</h3>
        <p>Components that work together to form a complete UI.</p>

        <Card
          title="User Information"
          footer={
            <div>
              <button onClick={() => setIsModalOpen(true)}>
                View Details
              </button>
            </div>
          }
        >
          <p>This is a card component with flexible slots for title, content, and footer.</p>
          <ul>
            <li>Flexible and reusable</li>
            <li>Clean separation of concerns</li>
            <li>Easy to customize</li>
          </ul>
        </Card>

        <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)}>
          <h3>Modal Example</h3>
          <p>This is a modal component that can contain any content.</p>
          <p>It demonstrates the compound component pattern where the Modal manages the overlay and positioning, while the content is completely flexible.</p>
          <button onClick={() => setIsModalOpen(false)}>Close</button>
        </Modal>
      </div>

      <div className="example">
        <h3>Error Boundaries</h3>
        <p>Components that catch JavaScript errors anywhere in their child component tree.</p>

        <ErrorBoundary>
          <Card title="Error Boundary Demo">
            <BuggyComponent />
          </Card>
        </ErrorBoundary>
      </div>

      <div className="example">
        <h3>Composition Benefits</h3>
        <ul>
          <li><strong>Reusability:</strong> Components can be composed in different ways</li>
          <li><strong>Flexibility:</strong> Easy to extend and modify behavior</li>
          <li><strong>Separation of Concerns:</strong> Each component has a single responsibility</li>
          <li><strong>Testability:</strong> Easier to test individual pieces</li>
          <li><strong>Code Organization:</strong> Clear structure and hierarchy</li>
        </ul>
      </div>
    </div>
  );
}

export default CompositionPatterns;