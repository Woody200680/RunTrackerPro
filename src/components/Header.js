import React from 'react';
import { Link, useLocation } from 'react-router-dom';

const Header = ({ activeRun }) => {
  const location = useLocation();
  
  return (
    <header className="app-header">
      <div className="logo">
        <i className="fas fa-running"></i>
        <span>Run Tracker</span>
      </div>
      
      {!activeRun && (
        <nav className="nav-links">
          <Link 
            to="/" 
            className={location.pathname === '/' ? 'active' : ''}
            aria-label="Go to Run Tracker"
          >
            <i className="fas fa-play"></i>
            <span>Track</span>
          </Link>
          <Link 
            to="/history" 
            className={location.pathname === '/history' || location.pathname.startsWith('/run/') ? 'active' : ''}
            aria-label="View Run History"
          >
            <i className="fas fa-history"></i>
            <span>History</span>
          </Link>
        </nav>
      )}
      
      {activeRun && (
        <div className="active-run-indicator">
          <span className="pulse-dot"></span>
          <span>Recording</span>
        </div>
      )}
    </header>
  );
};

export default Header;
