import React, { useState, useEffect, useRef } from 'react';
import RunMap from './RunMap';
import Timer from './Timer';
import { trackLocation, stopTracking } from '../services/locationService';
import { startRun, pauseRun, resumeRun, stopRun } from '../services/runService';
import { calculateDistance, calculatePace } from '../utils/calculations';

const RunTracker = ({ activeRun, setActiveRun }) => {
  const [status, setStatus] = useState('idle'); // idle, running, paused, finished
  const [metrics, setMetrics] = useState({
    distance: 0,
    pace: 0,
    time: 0,
  });
  const [route, setRoute] = useState([]);
  const timerRef = useRef(null);
  const locationUpdateInterval = useRef(null);
  
  useEffect(() => {
    // Clean up on component unmount
    return () => {
      if (locationUpdateInterval.current) {
        clearInterval(locationUpdateInterval.current);
      }
      stopTracking();
    };
  }, []);

  useEffect(() => {
    // If there's an active run and we're in running state, update metrics
    if (status === 'running' && route.length > 0) {
      setMetrics(prevMetrics => ({
        ...prevMetrics,
        distance: calculateDistance(route),
        pace: calculatePace(calculateDistance(route), metrics.time)
      }));
    }
  }, [route, metrics.time, status]);

  const handleStartRun = async () => {
    try {
      // Check if geolocation is available
      if (!navigator.geolocation) {
        alert('Geolocation is not supported by your browser');
        return;
      }
      
      // Request permission for location
      const permission = await navigator.permissions.query({ name: 'geolocation' });
      if (permission.state === 'denied') {
        alert('Location permission is required for tracking runs');
        return;
      }
      
      // Create a new run
      const newRun = await startRun();
      setActiveRun(newRun);
      setStatus('running');
      setRoute([]);
      setMetrics({
        distance: 0,
        pace: 0,
        time: 0,
      });
      
      // Start location tracking
      locationUpdateInterval.current = trackLocation((position) => {
        const newPoint = {
          lat: position.coords.latitude,
          lng: position.coords.longitude,
          timestamp: new Date().getTime()
        };
        setRoute(prevRoute => [...prevRoute, newPoint]);
      });
    } catch (error) {
      console.error('Error starting run:', error);
      alert('Failed to start run. Please try again.');
    }
  };

  const handlePauseRun = async () => {
    if (status === 'running') {
      await pauseRun(activeRun.id);
      setStatus('paused');
      stopTracking();
      if (locationUpdateInterval.current) {
        clearInterval(locationUpdateInterval.current);
        locationUpdateInterval.current = null;
      }
    } else if (status === 'paused') {
      await resumeRun(activeRun.id);
      setStatus('running');
      // Resume location tracking
      locationUpdateInterval.current = trackLocation((position) => {
        const newPoint = {
          lat: position.coords.latitude,
          lng: position.coords.longitude,
          timestamp: new Date().getTime()
        };
        setRoute(prevRoute => [...prevRoute, newPoint]);
      });
    }
  };

  const handleStopRun = async () => {
    try {
      if (activeRun) {
        await stopRun(activeRun.id, {
          distance: metrics.distance,
          duration: metrics.time,
          pace: metrics.pace,
          route: route
        });
        
        setStatus('finished');
        setActiveRun(null);
        stopTracking();
        
        if (locationUpdateInterval.current) {
          clearInterval(locationUpdateInterval.current);
          locationUpdateInterval.current = null;
        }
      }
    } catch (error) {
      console.error('Error stopping run:', error);
      alert('Failed to stop run. Please try again.');
    }
  };

  const handleTimeUpdate = (time) => {
    setMetrics(prevMetrics => ({
      ...prevMetrics,
      time,
      pace: calculatePace(prevMetrics.distance, time)
    }));
  };

  return (
    <div className="run-tracker">
      <div className="metrics-container">
        <div className="metric">
          <span className="metric-label">Distance</span>
          <span className="metric-value">{metrics.distance.toFixed(2)} km</span>
        </div>
        <div className="metric">
          <span className="metric-label">Pace</span>
          <span className="metric-value">
            {metrics.pace > 0 
              ? `${Math.floor(metrics.pace)}:${String(Math.floor((metrics.pace % 1) * 60)).padStart(2, '0')}/km` 
              : '0:00/km'}
          </span>
        </div>
        <div className="metric">
          <span className="metric-label">Time</span>
          <Timer 
            running={status === 'running'} 
            onTimeUpdate={handleTimeUpdate}
            ref={timerRef}
          />
        </div>
      </div>
      
      <RunMap route={route} />
      
      <div className="controls">
        {status === 'idle' || status === 'finished' ? (
          <button className="start-btn" onClick={handleStartRun}>
            <i className="fas fa-play"></i> Start Run
          </button>
        ) : (
          <>
            <button className="control-btn pause-btn" onClick={handlePauseRun}>
              {status === 'paused' ? <><i className="fas fa-play"></i> Resume</> : <><i className="fas fa-pause"></i> Pause</>}
            </button>
            <button className="control-btn stop-btn" onClick={handleStopRun}>
              <i className="fas fa-stop"></i> Stop
            </button>
          </>
        )}
      </div>
      
      {status === 'finished' && (
        <div className="run-complete">
          <h3>Run Complete!</h3>
          <p>Your run has been saved to your history.</p>
        </div>
      )}
    </div>
  );
};

export default RunTracker;
