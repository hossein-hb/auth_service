import { Link } from 'react-router-dom';

export default function Home() {
  const accessToken = localStorage.getItem('accessToken');

  const displayToken = accessToken ? accessToken.slice(0, 12) + '…' : '';

  return (
    <div className="auth-card">
      {accessToken ? (
        <>
          <h1 className="auth-title">You are signed in ✅</h1>
          <p className="auth-subtitle">
            Your access token is stored in localStorage. The refresh token lives
            in an HttpOnly cookie scoped to <code>/api/auth/refresh</code>.
          </p>
          <div className="token-box">{displayToken}</div>
        </>
      ) : (
        <>
          <h1 className="auth-title">Auth Service</h1>
          <p className="auth-subtitle">
            Log in or create an account to get started.
          </p>
          <Link className="btn" to="/login">
            Log in
          </Link>
          <p className="auth-footer">
            <Link to="/signup">Create an account</Link>
          </p>
        </>
      )}
    </div>
  );
}
