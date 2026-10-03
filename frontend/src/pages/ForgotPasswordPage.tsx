import { useEffect, useRef, useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ApiError } from '../lib/api';
import { authApi, type UserRole } from '../lib/auth';

type Step = 1 | 2 | 3;

const ROLES: UserRole[] = ['USER', 'SUPPORT', 'ADMIN'];

// Mirrors ChangePasswordRequest @Pattern: upper, lower, digit and special (@#$%^&+=), min 8 chars.
const PASSWORD_RULE =
  /^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=]).{8,100}$/;

export default function ForgotPasswordPage() {
  const navigate = useNavigate();

  const [step, setStep] = useState<Step>(1);
  const [email, setEmail] = useState('');
  const [role, setRole] = useState<UserRole>('USER');
  const [otp, setOtp] = useState('');
  const [token, setToken] = useState<string | null>(null);

  const [newPassword, setNewPassword] = useState('');
  const [repeatNewPassword, setRepeatNewPassword] = useState('');

  const [error, setError] = useState<string | null>(null);
  const [info, setInfo] = useState<string | null>(null);
  const [submitting, setSubmitting] = useState(false);
  const [resending, setResending] = useState(false);

  const infoTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const errorTimer = useRef<ReturnType<typeof setTimeout> | null>(null);
  useEffect(() => {
    if (info) {
      if (infoTimer.current) clearTimeout(infoTimer.current);
      infoTimer.current = setTimeout(() => setInfo(null), 4000);
    }
    if (error) {
      if (errorTimer.current) clearTimeout(errorTimer.current);
      errorTimer.current = setTimeout(() => setError(null), 4000);
    }
    return () => {
      if (infoTimer.current) clearTimeout(infoTimer.current);
      if (errorTimer.current) clearTimeout(errorTimer.current);
    };
  }, [info, error]);

  const toError = (err: unknown): string => {
    if (err instanceof ApiError) {
      return err.message;
    }
    return 'Cannot reach the server. Is the backend running on port 8081?';
  };

  const handleSendCode = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!email.trim()) {
      setError('Please enter your email address.');
      return;
    }

    setSubmitting(true);
    try {
      const res = await authApi.forgetPasswordSendCode(email.trim(), role);
      setInfo(res.message);
      setStep(2);
    } catch (err) {
      setError(toError(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleResend = async () => {
    setError(null);
    setResending(true);
    try {
      const res = await authApi.forgetPasswordSendCode(email.trim(), role);
      setInfo(res.message);
    } catch (err) {
      setError(toError(err));
    } finally {
      setResending(false);
    }
  };

  const handleVerify = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!otp.trim()) {
      setError('Please enter the 6-digit code from your email.');
      return;
    }

    setSubmitting(true);
    try {
      const res = await authApi.forgetPasswordVerifyCode(email.trim(), otp.trim());
      setToken(res.token);
      setInfo(null);
      setStep(3);
    } catch (err) {
      setError(toError(err));
    } finally {
      setSubmitting(false);
    }
  };

  const handleSetPassword = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);

    if (!PASSWORD_RULE.test(newPassword)) {
      setError(
        'Password needs 8+ characters with an uppercase letter, a lowercase letter, a digit and one of @#$%^&+=.'
      );
      return;
    }
    if (newPassword !== repeatNewPassword) {
      setError('Passwords do not match.');
      return;
    }

    setSubmitting(true);
    try {
      const res = await authApi.forgetPasswordSetPassword({
        token: token!,
        email: email.trim(),
        userRole: role,
        newPassword,
        repeatNewPassword,
      });
      setInfo(null);
      // Success — send the user to log in with the new password.
      navigate('/login', { state: { message: res.message } });
    } catch (err) {
      setError(toError(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth-card">
      <h1 className="auth-title">Reset your password</h1>
      <p className="auth-subtitle">
        Three quick steps: email, code, new password.
      </p>

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
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          </label>

          <label className="field">
            <span className="field-label">Role</span>
            <select value={role} onChange={(e) => setRole(e.target.value as UserRole)}>
              {ROLES.map((r) => (
                <option key={r} value={r}>
                  {r}
                </option>
              ))}
            </select>
            <span className="field-hint">
              Must match the role your account was created with.
            </span>
          </label>

          <button className="btn" type="submit" disabled={submitting}>
            {submitting ? 'Sending…' : 'Send verification code'}
          </button>
        </form>
      )}

      {step === 2 && (
        <form onSubmit={handleVerify} noValidate>
          <p className="auth-subtitle">
            We sent a code to <strong>{email}</strong>.
          </p>
          <label className="field">
            <span className="field-label">Verification code</span>
            <div className="code-row">
              <input
                inputMode="numeric"
                autoComplete="one-time-code"
                placeholder="••••••"
                value={otp}
                onChange={(e) => setOtp(e.target.value)}
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
            <span className="field-hint">The code expires after 2 minutes.</span>
          </label>
          <button className="btn" type="submit" disabled={submitting}>
            {submitting ? 'Verifying…' : 'Verify code'}
          </button>
        </form>
      )}

      {step === 3 && (
        <form onSubmit={handleSetPassword} noValidate>
          <label className="field">
            <span className="field-label">New password</span>
            <input
              type="password"
              autoComplete="new-password"
              placeholder="8+ chars, Aa, 1, special"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
            />
            <span className="field-hint">
              At least 8 characters with uppercase, lowercase, a digit and one of
              @#$%^&amp;+=
            </span>
          </label>

          <label className="field">
            <span className="field-label">Repeat new password</span>
            <input
              type="password"
              autoComplete="new-password"
              placeholder="Repeat your new password"
              value={repeatNewPassword}
              onChange={(e) => setRepeatNewPassword(e.target.value)}
            />
          </label>

          <button className="btn" type="submit" disabled={submitting}>
            {submitting ? 'Saving…' : 'Set new password'}
          </button>
        </form>
      )}

      <p className="auth-footer">
        Remembered it? <Link to="/login">Log in</Link>
      </p>
    </div>
  );
}
