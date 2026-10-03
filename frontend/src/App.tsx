import { NavLink, Outlet, useNavigate } from 'react-router-dom';

export default function App() {
  const navigate = useNavigate();

  const accessToken = localStorage.getItem('accessToken');
  const firstName = localStorage.getItem('firstName') ?? '';

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('firstName');
    navigate('/login');
  };

  return (
    <div className="app-shell">
      <header className="topbar">
        <span className="brand">Auth Service</span>
        <nav>
          {accessToken ? (
            <>
              <span className="welcome">Hi, {firstName || 'user'}</span>
              <button className="btn btn-link" onClick={handleLogout}>
                Log out
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className="nav-link">
                Log in
              </NavLink>
              <NavLink to="/signup" className="nav-link">
                Sign up
              </NavLink>
            </>
          )}
        </nav>
      </header>
      <main className="page">
        <Outlet />
      </main>
    </div>
  );
}
