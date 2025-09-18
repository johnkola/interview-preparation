import React, { createContext, useContext, useState, ReactNode } from 'react';

interface User {
  id: number;
  name: string;
  email: string;
}

interface Theme {
  primary: string;
  secondary: string;
  background: string;
  text: string;
}

const themes: Record<string, Theme> = {
  light: {
    primary: '#007bff',
    secondary: '#6c757d',
    background: '#ffffff',
    text: '#212529'
  },
  dark: {
    primary: '#0d6efd',
    secondary: '#6c757d',
    background: '#212529',
    text: '#ffffff'
  }
};

interface UserContextType {
  user: User | null;
  login: (user: User) => void;
  logout: () => void;
}

interface ThemeContextType {
  theme: Theme;
  themeName: string;
  toggleTheme: () => void;
}

const UserContext = createContext<UserContextType | undefined>(undefined);
const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

function UserProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);

  const login = (userData: User) => {
    setUser(userData);
  };

  const logout = () => {
    setUser(null);
  };

  return (
    <UserContext.Provider value={{ user, login, logout }}>
      {children}
    </UserContext.Provider>
  );
}

function ThemeProvider({ children }: { children: ReactNode }) {
  const [themeName, setThemeName] = useState<string>('light');

  const toggleTheme = () => {
    setThemeName(prev => prev === 'light' ? 'dark' : 'light');
  };

  const theme = themes[themeName];

  return (
    <ThemeContext.Provider value={{ theme, themeName, toggleTheme }}>
      {children}
    </ThemeContext.Provider>
  );
}

function useUser() {
  const context = useContext(UserContext);
  if (context === undefined) {
    throw new Error('useUser must be used within a UserProvider');
  }
  return context;
}

function useTheme() {
  const context = useContext(ThemeContext);
  if (context === undefined) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
}

function ContextExample() {
  return (
    <UserProvider>
      <ThemeProvider>
        <ThemedApp />
      </ThemeProvider>
    </UserProvider>
  );
}

function ThemedApp() {
  const { theme, themeName, toggleTheme } = useTheme();

  return (
    <div
      className="context-example"
      style={{
        backgroundColor: theme.background,
        color: theme.text,
        padding: '20px',
        minHeight: '400px'
      }}
    >
      <h2>Context API Example</h2>
      <div style={{ marginBottom: '20px' }}>
        <button
          onClick={toggleTheme}
          style={{
            backgroundColor: theme.primary,
            color: 'white',
            border: 'none',
            padding: '10px 20px',
            borderRadius: '4px'
          }}
        >
          Switch to {themeName === 'light' ? 'Dark' : 'Light'} Theme
        </button>
      </div>

      <UserSection />
      <ProfileCard />
      <UserStats />
    </div>
  );
}

function UserSection() {
  const { user, login, logout } = useUser();
  const { theme } = useTheme();

  const handleLogin = () => {
    login({
      id: 1,
      name: 'John Doe',
      email: 'john@example.com'
    });
  };

  return (
    <div
      className="example"
      style={{
        border: `1px solid ${theme.secondary}`,
        padding: '15px',
        marginBottom: '20px',
        borderRadius: '4px'
      }}
    >
      <h3>User Authentication</h3>
      {user ? (
        <div>
          <p>Welcome, {user.name}!</p>
          <button
            onClick={logout}
            style={{
              backgroundColor: theme.secondary,
              color: 'white',
              border: 'none',
              padding: '8px 16px',
              borderRadius: '4px'
            }}
          >
            Logout
          </button>
        </div>
      ) : (
        <div>
          <p>Please log in to continue</p>
          <button
            onClick={handleLogin}
            style={{
              backgroundColor: theme.primary,
              color: 'white',
              border: 'none',
              padding: '8px 16px',
              borderRadius: '4px'
            }}
          >
            Login as John Doe
          </button>
        </div>
      )}
    </div>
  );
}

function ProfileCard() {
  const { user } = useUser();
  const { theme } = useTheme();

  if (!user) {
    return (
      <div
        className="example"
        style={{
          border: `1px solid ${theme.secondary}`,
          padding: '15px',
          marginBottom: '20px',
          borderRadius: '4px'
        }}
      >
        <h3>Profile Card</h3>
        <p>No user logged in</p>
      </div>
    );
  }

  return (
    <div
      className="example"
      style={{
        border: `1px solid ${theme.primary}`,
        padding: '15px',
        marginBottom: '20px',
        borderRadius: '4px'
      }}
    >
      <h3>Profile Card</h3>
      <div>
        <p><strong>ID:</strong> {user.id}</p>
        <p><strong>Name:</strong> {user.name}</p>
        <p><strong>Email:</strong> {user.email}</p>
      </div>
    </div>
  );
}

function UserStats() {
  const { user } = useUser();
  const { theme } = useTheme();

  return (
    <div
      className="example"
      style={{
        border: `1px solid ${theme.secondary}`,
        padding: '15px',
        borderRadius: '4px'
      }}
    >
      <h3>User Statistics</h3>
      <p>Total Users: 1</p>
      <p>Currently Logged In: {user ? 'Yes' : 'No'}</p>
      <p>Current Theme: {user ? 'Available' : 'Not Available'}</p>

      <div style={{ marginTop: '15px' }}>
        <h4>Context Benefits:</h4>
        <ul>
          <li>No prop drilling needed</li>
          <li>Global state management</li>
          <li>Clean separation of concerns</li>
          <li>Easy to test and maintain</li>
        </ul>
      </div>
    </div>
  );
}

export default ContextExample;