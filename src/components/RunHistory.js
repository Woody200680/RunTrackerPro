import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { getAllRuns } from '../services/runService';

const RunHistory = () => {
  const [runs, setRuns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchRuns = async () => {
      try {
        setLoading(true);
        const data = await getAllRuns();
        setRuns(data);
        setLoading(false);
      } catch (err) {
        console.error('Error fetching runs:', err);
        setError('Failed to load run history. Please try again later.');
        setLoading(false);
      }
    };

    fetchRuns();
  }, []);

  const formatDate = (timestamp) => {
    const date = new Date(timestamp);
    return date.toLocaleDateString();
  };

  const formatTime = (timestamp) => {
    const date = new Date(timestamp);
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  };

  const formatDuration = (seconds) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);
    
    return `${hours > 0 ? `${hours}h ` : ''}${minutes}m ${secs}s`;
  };

  if (loading) {
    return <div className="loading">Loading your run history...</div>;
  }

  if (error) {
    return <div className="error">{error}</div>;
  }

  if (runs.length === 0) {
    return (
      <div className="empty-state">
        <i className="fas fa-running fa-3x"></i>
        <h2>No runs yet</h2>
        <p>Start your first run to see it here!</p>
        <Link to="/" className="start-run-btn">Start a Run</Link>
      </div>
    );
  }

  return (
    <div className="run-history">
      <h1>Run History</h1>
      <div className="run-list">
        {runs.map(run => (
          <Link to={`/run/${run.id}`} key={run.id} className="run-card">
            <div className="run-date">
              <div className="date">{formatDate(run.startTime)}</div>
              <div className="time">{formatTime(run.startTime)}</div>
            </div>
            <div className="run-stats">
              <div className="stat">
                <span className="stat-value">{run.distance.toFixed(2)}</span>
                <span className="stat-label">km</span>
              </div>
              <div className="stat">
                <span className="stat-value">{formatDuration(run.duration)}</span>
                <span className="stat-label">time</span>
              </div>
              <div className="stat">
                <span className="stat-value">
                  {run.pace > 0 
                    ? `${Math.floor(run.pace)}:${String(Math.floor((run.pace % 1) * 60)).padStart(2, '0')}` 
                    : '0:00'}
                </span>
                <span className="stat-label">/km</span>
              </div>
            </div>
            <div className="run-arrow">
              <i className="fas fa-chevron-right"></i>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
};

export default RunHistory;
