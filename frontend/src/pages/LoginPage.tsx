import {
  useEffect,
  useRef,
  useState,
  type FormEvent,
} from 'react';

import {
  Link,
  useNavigate,
} from 'react-router-dom';

import { ApiError } from '../lib/api';

import {
  authApi,
  type UserRole,
} from '../lib/auth';

import GoogleSignInButton
  from '../components/GoogleSignInButton';

const ROLES: UserRole[] = [
  'USER',
  'SUPPORT',
  'ADMIN',
];

export default function LoginPage() {

  const navigate = useNavigate();

  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');

  const [role, setRole] =
    useState<UserRole>('USER');

  const [error, setError] =
    useState<string | null>(null);

  const [submitting, setSubmitting] =
    useState(false);

  const [googleSubmitting, setGoogleSubmitting] =
    useState(false);

  const errorTimer =
    useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {

    if (error) {

      if (errorTimer.current) {
        clearTimeout(errorTimer.current);
      }

      errorTimer.current =
        setTimeout(() => setError(null), 4000);
    }

    return () => {

      if (errorTimer.current) {
        clearTimeout(errorTimer.current);
      }

    };

  }, [error]);

  const handleSubmit = async (
    e: FormEvent
  ) => {

    e.preventDefault();

    setError(null);

    if (!username.trim() || !password) {
      setError(
        'Please enter your username and password.'
      );
      return;
    }

    setSubmitting(true);

    try {

      const res =
        await authApi.login(
          username.trim(),
          password,
          role
        );

      localStorage.setItem(
        'accessToken',
        res.accessToken
      );

      localStorage.setItem(
        'firstName',
        username.trim()
      );

      navigate('/');

    } catch (err) {

      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError(
          'Cannot reach the server. Is the backend running on port 8081?'
        );
      }

    } finally {
      setSubmitting(false);
    }
  };

  const handleGoogleSuccess = async (
    idToken: string
  ) => {

    setError(null);
    setGoogleSubmitting(true);

    try {

      const res =
        await authApi.googleSignIn(idToken);

      localStorage.setItem(
        'accessToken',
        res.accessToken
      );

      navigate('/');

    } catch (err) {

      if (err instanceof ApiError) {
        setError(err.message);
      } else {
        setError(
          'Google sign-in failed. Please try again.'
        );
      }

    } finally {
      setGoogleSubmitting(false);
    }
  };

  const handleGoogleError = () => {

    setError(
      'Unable to initialize Google Sign-In.'
    );
  };

  return (
    <div className="auth-card">

      <h1 className="auth-title">
        Welcome back
      </h1>

      <p className="auth-subtitle">
        Log in to your account to continue.
      </p>

      {error && (
        <div className="alert alert-error">
          {error}
        </div>
      )}

      <form
        onSubmit={handleSubmit}
        noValidate
      >

        <label className="field">

          <span className="field-label">
            Username (email)
          </span>

          <input
            type="email"
            autoComplete="username"
            placeholder="you@example.com"
            value={username}
            onChange={(e) =>
              setUsername(e.target.value)
            }
          />

        </label>

        <label className="field">

          <span className="field-label">
            Password
          </span>

          <input
            type="password"
            autoComplete="current-password"
            placeholder="••••••••"
            value={password}
            onChange={(e) =>
              setPassword(e.target.value)
            }
          />

        </label>

        <label className="field">

          <span className="field-label">
            Role
          </span>

          <select
            value={role}
            onChange={(e) =>
              setRole(
                e.target.value as UserRole
              )
            }
          >

            {ROLES.map((r) => (
              <option
                key={r}
                value={r}
              >
                {r}
              </option>
            ))}

          </select>

        </label>

        <button
          className="btn"
          type="submit"
          disabled={
            submitting ||
            googleSubmitting
          }
        >
          {submitting
            ? 'Logging in…'
            : 'Log in'}
        </button>

      </form>

      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          margin: '20px 0',
        }}
      >

        <div
          style={{
            flex: 1,
            height: '1px',
            background: '#ddd',
          }}
        />

        <span>
          OR
        </span>

        <div
          style={{
            flex: 1,
            height: '1px',
            background: '#ddd',
          }}
        />

      </div>

      <GoogleSignInButton
        text="continue_with"
        onSuccess={handleGoogleSuccess}
        onError={handleGoogleError}
      />

      {googleSubmitting && (
        <p className="auth-subtitle">
          Signing in with Google…
        </p>
      )}

      <p className="auth-footer">

        No account yet?{' '}
        <Link to="/signup">
          Sign up
        </Link>

        <br />

        <Link
          to="/forgot-password"
          className="muted-link"
        >
          Forgot your password?
        </Link>

      </p>

    </div>
  );
}