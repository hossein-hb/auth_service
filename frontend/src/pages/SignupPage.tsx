import { useEffect, useRef, useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ApiError } from '../lib/api';
import {
  authApi,
  type CompleteSignUpPayload,
  type TokenResponse,
} from '../lib/auth';
import GoogleSignInButton
  from '../components/GoogleSignInButton';

type Step = 1 | 2 | 3;

interface FormData {
  email: string;
  otp: string;
  firstName: string;
  lastName: string;
  password: string;
  repeatPassword: string;
}

const INITIAL: FormData = {
  email: '',
  otp: '',
  firstName: '',
  lastName: '',
  password: '',
  repeatPassword: '',
};

// Mirrors SignUpInfos @Pattern: upper, lower, digit and special (@#$%^&+=), min 8 chars.
const PASSWORD_RULE =
  /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,100}$/;

export default function SignupPage() {
  const navigate = useNavigate();
  const [step, setStep] = useState<Step>(1);
  const [form, setForm] = useState<FormData>(INITIAL);
  const [signUpToken, setSignUpToken] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [info, setInfo] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [resending, setResending] = useState(false);

  const [googleSubmitting, setGoogleSubmitting] = useState(false);

  const infoTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const errorTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    return () => {
      if (infoTimer.current) clearTimeout(infoTimer.current);
      if (errorTimer.current) clearTimeout(errorTimer.current);
    };
  }, []);

  const set = (patch: Partial<FormData>) => setForm((f) => ({ ...f, ...patch }));

  const showInfo = (message: string) => {
    setInfo(message);
    if (infoTimer.current) clearTimeout(infoTimer.current);
    infoTimer.current = setTimeout(() => setInfo(null), 4000);
  };

  const showError = (message: string) => {
    setError(message);
    if (errorTimer.current) clearTimeout(errorTimer.current);
    errorTimer.current = setTimeout(() => setError(null), 4000);
  };

  const handleSendCode = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!form.email.trim()) {
      showError('Please enter your email address.');
      return;
    }

    setSubmitting(true);
    try {
      const res = await authApi.sendVerificationCode(form.email.trim());
      showInfo(res.message);
      setStep(2);
    } catch (err) {
      if (err instanceof ApiError) {
        showError(err.message);
      } else {
        showError('Cannot reach the server. Is the backend running on port 8081?');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleVerify = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!form.otp.trim()) {
      showError('Please enter the 6-digit code from your email.');
      return;
    }

    setSubmitting(true);
    try {
      const res: TokenResponse = await authApi.verifyCode(
        form.email.trim(),
        form.otp.trim()
      );
      setSignUpToken(res.token);
      setStep(3);
    } catch (err) {
      if (err instanceof ApiError) {
        showError(err.message);
      } else {
        showError('Cannot reach the server. Is the backend running on port 8081?');
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleGoogleSuccess = async (idToken: string) => {
    setGoogleSubmitting(true);

    try {
      const response = await authApi.googleSignIn(idToken);

      localStorage.setItem(
        'accessToken',
        response.accessToken
      );

      navigate('/');
    } catch (error) {
      console.error(error);

      setError(
        'Google sign-up failed. Please try again.'
      );
    } finally {
      setGoogleSubmitting(false);
    }
  };

  const handleResend = async () => {
    setError(null);
    setResending(true);
    try {
      const res = await authApi.sendVerificationCode(form.email.trim());
      showInfo(res.message);
    } catch (err) {
      if (err instanceof ApiError) {
        showError(err.message);
      } else {
        showError('Cannot reach the server. Is the backend running on port 8081?');
      }
    } finally {
      setResending(false);
    }
  };

  const handleComplete = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!form.firstName.trim() || !form.lastName.trim()) {
      showError('Please enter your first and last name.');
      return;
    }
    if (!PASSWORD_RULE.test(form.password)) {
      showError(
        'Password needs 8+ characters with an uppercase letter, a lowercase letter, a digit and one of @#$%^&+=.'
      );
      return;
    }
    if (form.password !== form.repeatPassword) {
      showError('Passwords do not match.');
      return;
    }

    setSubmitting(true);
    try {
      const payload: CompleteSignUpPayload = {
        signUpToken: signUpToken!,
        firstName: form.firstName.trim(),
        lastName: form.lastName.trim(),
        email: form.email.trim(),
        password: form.password,
        repeatPassword: form.repeatPassword,
      };
      const res = await authApi.completeUserSignUp(payload);
      localStorage.setItem('accessToken', res.accessToken);
      localStorage.setItem('firstName', payload.firstName);
      navigate('/');
    } catch (err) {
      if (err instanceof ApiError) {
        showError(err.message);
      } else {
        showError('Cannot reach the server. Is the backend running on port 8081?');
      }
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-card">
      <h1 className="auth-title">Create your account</h1>
      <p className="auth-subtitle">Three quick steps: email, code, profile.</p>

      <GoogleSignInButton
        text="signup_with"
        onSuccess={handleGoogleSuccess}
        onError={() =>
          setError('Unable to initialize Google Sign-In.')
        }
      />

      {googleSubmitting && (
        <p className="auth-subtitle">
          Creating your account with Google...
        </p>
      )}

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

        <span>OR</span>

        <div
          style={{
            flex: 1,
            height: '1px',
            background: '#ddd',
          }}
        />
      </div>

      <div className="steps" aria-label={`Step ${step} of 3`}>
        {[1, 2, 3].map((n) => (
          <div
            key={n}
            className={
              'step' + (n === step ? ' active' : '') + (n < step ? ' done' : '')
            }
          >
            {n}
          </div>
        ))}
      </div>

      {error && <div className="alert alert-error">{error}</div>}
      {info && <div className="alert alert-success">{info}</div>}

      {step === 1 && (
        <form onSubmit={handleSendCode} noValidate>
          <label className="field">
            <span className="field-label">Email</span>
            <input
              type="email"
              autoComplete="email"
              placeholder="you@example.com"
              value={form.email}
              onChange={(e) => set({ email: e.target.value })}
            />
          </label>
          <button className="btn" type="submit" disabled={submitting}>
            {submitting ? 'Sending…' : 'Send verification code'}
          </button>
        </form>
      )}

      {step === 2 && (
        <form onSubmit={handleVerify} noValidate>
          <p className="auth-subtitle">
            We sent a code to <strong>{form.email}</strong>.
          </p>
          <label className="field">
            <span className="field-label">Verification code</span>
            <div className="code-row">
              <input
                inputMode="numeric"
                autoComplete="one-time-code"
                placeholder="••••••"
                value={form.otp}
                onChange={(e) => set({ otp: e.target.value })}
              />
              <button
                className="btn btn-ghost"
                type="button"
                onClick={handleResend}
                disabled={resending}
              >
                {resending ? '…' : 'Resend'}
              </button>
            </div>
            <span className="field-hint">
              Didn’t get it? Resend a new code — the old one becomes invalid.
            </span>
          </label>
          <button className="btn" type="submit" disabled={submitting}>
            {submitting ? 'Verifying…' : 'Verify code'}
          </button>
        </form>
      )}

      {step === 3 && (
        <form onSubmit={handleComplete} noValidate>
          <div className="row-2">
            <label className="field">
              <span className="field-label">First name</span>
              <input
                autoComplete="given-name"
                placeholder="Ada"
                value={form.firstName}
                onChange={(e) => set({ firstName: e.target.value })}
              />
            </label>
            <label className="field">
              <span className="field-label">Last name</span>
              <input
                autoComplete="family-name"
                placeholder="Lovelace"
                value={form.lastName}
                onChange={(e) => set({ lastName: e.target.value })}
              />
            </label>
          </div>

          <label className="field">
            <span className="field-label">Email</span>
            <input type="email" value={form.email} disabled />
          </label>

          <label className="field">
            <span className="field-label">Password</span>
            <input
              type="password"
              autoComplete="new-password"
              placeholder="8+ chars, Aa, 1, special"
              value={form.password}
              onChange={(e) => set({ password: e.target.value })}
            />
            <span className="field-hint">
              At least 8 characters with uppercase, lowercase, a digit and one of
              @#$%^&amp;+=
            </span>
          </label>

          <label className="field">
            <span className="field-label">Repeat password</span>
            <input
              type="password"
              autoComplete="new-password"
              placeholder="Repeat your password"
              value={form.repeatPassword}
              onChange={(e) => set({ repeatPassword: e.target.value })}
            />
          </label>

          <button className="btn" type="submit" disabled={submitting}>
            {submitting ? 'Creating account…' : 'Create account'}
          </button>
        </form>
      )}

      <p className="auth-footer">
        Already have an account? <Link to="/login">Log in</Link>
      </p>
    </div>
  );
}
