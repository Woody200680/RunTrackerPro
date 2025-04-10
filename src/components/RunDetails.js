import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import RunMap from './RunMap';
import { getRun } from '../services/runService';

const RunDetails = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [run, setRun] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchRun = async () => {
      try {
        setLoading(true);
        const data = await getRun(id);
        setRun(data);
        setLoading(false);
      } catch (err) {
        console.error('Error fetching run details:', err);
        setError('Failed to load run details. This run might not exist.');
        setLoading(false);
      }
    };

    if (id) {
      fetchRun();
    }
  }, [id]);

  const formatDate = (timestamp) => {
    const date = new Date(timestamp);
    const options = { 
      weekday: 'long', 
      year: 'numeric', 
      month: 'long', 
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    };
    return date.toLocaleDateString(undefined, options);
  };

  const formatDuration = (seconds) => {
    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);
    
    return `${hours > 0 ? `${hours}h ` : ''}${minutes}m ${secs}s`;
  };

  const handleGoBack = () => {
    navigate('/history');
  };

  if (loading) {
    return <div className="loading">Loading run details...</div>;
  }

  if (error || !run) {
    return (
      <div className="error-container">
        <div className="error">{error || 'Run not found'}</div>
        <button className="back-btn" onClick={handleGoBack}>
          <i className="fas fa-arrow-left"></i> Back to History
        </button>
      </div>
    );
  }

  return (
    <div className="run-details">
      <div className="detail-header">
        <button className="back-btn" onClick={handleGoBack}>
          <i className="fas fa-arrow-left"></i>
        </button>
        <h1>Run Details</h1>
      </div>
      
      <div className="detail-date">
        {formatDate(run.startTime)}
      </div>
      
      <div className="detail-stats">
        <div className="stat-box">
          <span className="stat-value">{run.distance.toFixed(2)}</span>
          <span className="stat-label">Distance (km)</span>
        </div>
        <div className="stat-box">
          <span className="stat-value">{formatDuration(run.duration)}</span>
          <span className="stat-label">Duration</span>
        </div>
        <div className="stat-box">
          <span className="stat-value">
            {run.pace > 0 
              ? `${Math.floor(run.pace)}:${String(Math.floor((run.pace % 1) * 60)).padStart(2, '0')}` 
              : '0:00'}
          </span>
          <span className="stat-label">Pace (min/km)</span>
        </div>
      </div>
      
      <div className="map-container">
        <h2>Route Map</h2>
        <RunMap route={run.route} isStatic={true} />
      </div>
      
      {run.elevationGain && (
        <div className="additional-stat">
          <span className="stat-label">Elevation Gain</span>
          <span className="stat-value">{run.elevationGain}m</span>
        </div>
      )}
      
      {run.avgHeartRate && (
        <div className="additional-stat">
          <span className="stat-label">Avg. Heart Rate</span>
          <span className="stat-value">{run.avgHeartRate} bpm</span>
        </div>
      )}
    </div>
  );
};

export default RunDetails;
