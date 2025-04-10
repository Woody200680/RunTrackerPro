// Run Tracker Application

// API Service
const API = {
  baseUrl: '/api',
  
  async getStatus() {
    try {
      const response = await fetch(`${this.baseUrl}/status`);
      return await response.json();
    } catch (error) {
      console.error('Error fetching API status:', error);
      return { status: 'error', error: error.message };
    }
  },
  
  async getRuns() {
    try {
      const response = await fetch(`${this.baseUrl}/runs`);
      return await response.json();
    } catch (error) {
      console.error('Error fetching runs:', error);
      return [];
    }
  },
  
  async getRun(id) {
    try {
      const response = await fetch(`${this.baseUrl}/runs/${id}`);
      return await response.json();
    } catch (error) {
      console.error(`Error fetching run ${id}:`, error);
      return null;
    }
  },
  
  async startRun() {
    try {
      const response = await fetch(`${this.baseUrl}/runs`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({})
      });
      return await response.json();
    } catch (error) {
      console.error('Error starting run:', error);
      return null;
    }
  },
  
  async pauseRun(id) {
    try {
      const response = await fetch(`${this.baseUrl}/runs/${id}/pause`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ pauseTime: Date.now() })
      });
      return await response.json();
    } catch (error) {
      console.error(`Error pausing run ${id}:`, error);
      return null;
    }
  },
  
  async resumeRun(id) {
    try {
      const response = await fetch(`${this.baseUrl}/runs/${id}/resume`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ resumeTime: Date.now() })
      });
      return await response.json();
    } catch (error) {
      console.error(`Error resuming run ${id}:`, error);
      return null;
    }
  },
  
  async stopRun(id, data) {
    try {
      const response = await fetch(`${this.baseUrl}/runs/${id}/stop`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ ...data, endTime: Date.now() })
      });
      return await response.json();
    } catch (error) {
      console.error(`Error stopping run ${id}:`, error);
      return null;
    }
  }
};

// Utility functions
const Utils = {
  formatDate(timestamp) {
    const date = new Date(timestamp);
    return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  },
  
  formatDuration(seconds) {
    const hrs = Math.floor(seconds / 3600);
    const mins = Math.floor((seconds % 3600) / 60);
    const secs = Math.floor(seconds % 60);
    
    const parts = [];
    if (hrs > 0) parts.push(`${hrs}h`);
    if (mins > 0 || hrs > 0) parts.push(`${mins}m`);
    parts.push(`${secs}s`);
    
    return parts.join(' ');
  },
  
  formatDistance(km) {
    return km.toFixed(2) + ' km';
  },
  
  formatPace(pace) {
    if (!pace) return '0:00 /km';
    const minutes = Math.floor(pace);
    const seconds = Math.floor((pace - minutes) * 60);
    return `${minutes}:${seconds.toString().padStart(2, '0')} /km`;
  }
};

// Application State
const AppState = {
  currentPage: 'track', // 'track', 'history', 'details'
  activeRun: null,
  selectedRunId: null,
  runs: [],
  
  init() {
    this.loadRuns();
    this.handleRouting();
    window.addEventListener('popstate', () => this.handleRouting());
  },
  
  async loadRuns() {
    this.runs = await API.getRuns();
    this.renderCurrentPage();
  },
  
  handleRouting() {
    const path = window.location.pathname;
    
    if (path.startsWith('/run/')) {
      const id = path.split('/')[2];
      this.selectedRunId = id;
      this.currentPage = 'details';
    } else if (path === '/history') {
      this.currentPage = 'history';
    } else {
      this.currentPage = 'track';
    }
    
    this.renderCurrentPage();
  },
  
  navigate(path) {
    window.history.pushState({}, '', path);
    this.handleRouting();
  },
  
  renderCurrentPage() {
    const contentEl = document.querySelector('.content');
    
    // Update navigation active state
    document.querySelectorAll('.nav-links a').forEach(link => {
      link.classList.remove('active');
      if ((this.currentPage === 'track' && link.getAttribute('href') === '/') || 
          (this.currentPage === 'history' && link.getAttribute('href') === '/history') ||
          (this.currentPage === 'details' && link.getAttribute('href') === '/history')) {
        link.classList.add('active');
      }
    });
    
    // Render appropriate content
    if (this.currentPage === 'track') {
      contentEl.innerHTML = this.renderTrackPage();
      this.initTrackPage();
    } else if (this.currentPage === 'history') {
      contentEl.innerHTML = this.renderHistoryPage();
      this.initHistoryPage();
    } else if (this.currentPage === 'details') {
      this.loadRunDetails(this.selectedRunId);
    }
  },
  
  async loadRunDetails(runId) {
    const contentEl = document.querySelector('.content');
    contentEl.innerHTML = '<div class="loading"><i class="fas fa-running"></i><p>Loading run details...</p></div>';
    
    const run = await API.getRun(runId);
    
    if (run) {
      contentEl.innerHTML = this.renderRunDetails(run);
      this.initRunDetailsPage(run);
    } else {
      contentEl.innerHTML = `
        <div class="error-container">
          <h2>Run Not Found</h2>
          <p>Sorry, we couldn't find the run you're looking for.</p>
          <button class="btn btn-primary" id="back-to-history">
            <i class="fas fa-arrow-left"></i> Back to History
          </button>
        </div>
      `;
      
      document.getElementById('back-to-history').addEventListener('click', () => {
        this.navigate('/history');
      });
    }
  },
  
  renderTrackPage() {
    return `
      <div class="run-tracker">
        <h2>Track Your Run</h2>
        
        <div class="metrics-container">
          <div class="metric">
            <div class="metric-label">Distance</div>
            <div class="metric-value" id="distance">0.00 km</div>
          </div>
          <div class="metric">
            <div class="metric-label">Pace</div>
            <div class="metric-value" id="pace">0:00 /km</div>
          </div>
          <div class="metric">
            <div class="metric-label">Time</div>
            <div class="metric-value" id="time">00:00:00</div>
          </div>
        </div>
        
        <div class="map-container" id="map">
          <div class="map-loading">Loading map...</div>
        </div>
        
        <div class="run-controls">
          ${!this.activeRun ? `
            <button class="btn btn-success" id="start-run">
              <i class="fas fa-play"></i> Start Run
            </button>
          ` : `
            <button class="btn ${this.activeRun.status === 'paused' ? 'btn-primary' : 'btn-warning'}" id="pause-resume-run">
              <i class="fas ${this.activeRun.status === 'paused' ? 'fa-play' : 'fa-pause'}"></i>
              ${this.activeRun.status === 'paused' ? 'Resume' : 'Pause'}
            </button>
            <button class="btn btn-danger" id="stop-run">
              <i class="fas fa-stop"></i> Stop
            </button>
          `}
        </div>
        
        ${this.activeRun ? `
          <div class="active-run-indicator">
            <span class="pulse-dot"></span>
            <span>Recording Run</span>
          </div>
        ` : ''}
      </div>
    `;
  },
  
  renderHistoryPage() {
    return `
      <div class="run-history">
        <h2>Your Run History</h2>
        
        ${this.runs.length > 0 ? `
          <div class="run-list">
            ${this.runs.map(run => `
              <div class="run-item" data-id="${run.id}">
                <div class="run-info">
                  <div class="run-date">${Utils.formatDate(run.startTime)}</div>
                  <div class="run-stats">
                    <span><i class="fas fa-route"></i> ${Utils.formatDistance(run.distance)}</span>
                    <span><i class="fas fa-stopwatch"></i> ${Utils.formatDuration(run.duration)}</span>
                    <span><i class="fas fa-tachometer-alt"></i> ${Utils.formatPace(run.pace)}</span>
                  </div>
                </div>
                <div class="run-actions">
                  <button class="btn btn-primary btn-sm view-run">
                    <i class="fas fa-eye"></i> View
                  </button>
                </div>
              </div>
            `).join('')}
          </div>
        ` : `
          <div class="no-runs">
            <i class="fas fa-running"></i>
            <p>You haven't logged any runs yet. Start tracking to see your history!</p>
            <button class="btn btn-primary" id="go-to-track">
              <i class="fas fa-play"></i> Start Tracking
            </button>
          </div>
        `}
      </div>
    `;
  },
  
  renderRunDetails(run) {
    return `
      <div class="run-details">
        <div class="run-details-header">
          <button class="btn btn-link back-button" id="back-to-history">
            <i class="fas fa-arrow-left"></i> Back to History
          </button>
          <h2>Run Details</h2>
        </div>
        
        <div class="run-info-card">
          <div class="run-date">${Utils.formatDate(run.startTime)}</div>
          
          <div class="run-details-stats">
            <div class="detail-card">
              <div class="detail-label">Distance</div>
              <div class="detail-value">${Utils.formatDistance(run.distance)}</div>
            </div>
            <div class="detail-card">
              <div class="detail-label">Duration</div>
              <div class="detail-value">${Utils.formatDuration(run.duration)}</div>
            </div>
            <div class="detail-card">
              <div class="detail-label">Pace</div>
              <div class="detail-value">${Utils.formatPace(run.pace)}</div>
            </div>
          </div>
        </div>
        
        <div class="map-container" id="detail-map">
          <div class="map-loading">Loading route map...</div>
        </div>
      </div>
    `;
  },
  
  initTrackPage() {
    // Initialize map if needed
    this.initMap();
    
    // Set up event listeners
    const startButton = document.getElementById('start-run');
    if (startButton) {
      startButton.addEventListener('click', () => this.startRun());
    }
    
    const pauseResumeButton = document.getElementById('pause-resume-run');
    if (pauseResumeButton) {
      pauseResumeButton.addEventListener('click', () => this.pauseResumeRun());
    }
    
    const stopButton = document.getElementById('stop-run');
    if (stopButton) {
      stopButton.addEventListener('click', () => this.stopRun());
    }
  },
  
  initHistoryPage() {
    // Add click handlers to run items
    document.querySelectorAll('.run-item').forEach(item => {
      item.addEventListener('click', () => {
        const id = item.getAttribute('data-id');
        this.navigate(`/run/${id}`);
      });
    });
    
    // Go to track button for empty state
    const goToTrackButton = document.getElementById('go-to-track');
    if (goToTrackButton) {
      goToTrackButton.addEventListener('click', () => {
        this.navigate('/');
      });
    }
  },
  
  initRunDetailsPage(run) {
    // Back button
    document.getElementById('back-to-history').addEventListener('click', () => {
      this.navigate('/history');
    });
    
    // Initialize map with route
    this.initDetailMap(run);
  },
  
  initMap() {
    // Initialize Leaflet map
    if (window.L && document.getElementById('map')) {
      const map = window.L.map('map').setView([0, 0], 13);
      
      window.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
      }).addTo(map);
      
      // Use geolocation to center the map if available
      if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
          (position) => {
            const { latitude, longitude } = position.coords;
            map.setView([latitude, longitude], 15);
          },
          (error) => {
            console.error('Geolocation error:', error);
          }
        );
      }
      
      this.map = map;
      this.routeLine = window.L.polyline([], { color: '#4285f4', weight: 4 }).addTo(map);
    }
  },
  
  initDetailMap(run) {
    if (window.L && document.getElementById('detail-map') && run.route && run.route.length > 0) {
      const map = window.L.map('detail-map');
      
      window.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
      }).addTo(map);
      
      const points = run.route.map(point => [point.lat, point.lng]);
      const routeLine = window.L.polyline(points, { color: '#4285f4', weight: 4 }).addTo(map);
      
      // Add start marker
      if (points.length > 0) {
        const startIcon = window.L.divIcon({
          html: '<i class="fas fa-play-circle" style="color: green; font-size: 24px;"></i>',
          className: 'start-marker-icon',
          iconSize: [24, 24],
          iconAnchor: [12, 12]
        });
        
        window.L.marker(points[0], { icon: startIcon }).addTo(map);
        
        // Add end marker
        const endIcon = window.L.divIcon({
          html: '<i class="fas fa-flag-checkered" style="color: red; font-size: 24px;"></i>',
          className: 'end-marker-icon',
          iconSize: [24, 24],
          iconAnchor: [12, 12]
        });
        
        window.L.marker(points[points.length - 1], { icon: endIcon }).addTo(map);
      }
      
      // Zoom to show the entire route
      if (points.length > 1) {
        const bounds = window.L.latLngBounds(points);
        map.fitBounds(bounds, { padding: [30, 30] });
      } else if (points.length === 1) {
        map.setView(points[0], 15);
      }
    }
  },
  
  async startRun() {
    try {
      const run = await API.startRun();
      if (run) {
        this.activeRun = run;
        this.startTracking();
        this.renderCurrentPage();
      }
    } catch (error) {
      console.error('Error starting run:', error);
      alert('Failed to start run. Please try again.');
    }
  },
  
  async pauseResumeRun() {
    if (!this.activeRun) return;
    
    try {
      if (this.activeRun.status === 'active') {
        // Pause the run
        const updatedRun = await API.pauseRun(this.activeRun.id);
        if (updatedRun) {
          this.activeRun = updatedRun;
          this.pauseTracking();
          this.renderCurrentPage();
        }
      } else if (this.activeRun.status === 'paused') {
        // Resume the run
        const updatedRun = await API.resumeRun(this.activeRun.id);
        if (updatedRun) {
          this.activeRun = updatedRun;
          this.resumeTracking();
          this.renderCurrentPage();
        }
      }
    } catch (error) {
      console.error('Error pausing/resuming run:', error);
      alert('Failed to update run. Please try again.');
    }
  },
  
  async stopRun() {
    if (!this.activeRun) return;
    
    if (confirm('Are you sure you want to stop this run?')) {
      try {
        const completedRun = await API.stopRun(this.activeRun.id, {
          distance: this.distance,
          duration: this.time,
          pace: this.pace,
          route: this.route
        });
        
        if (completedRun) {
          this.stopTracking();
          this.activeRun = null;
          await this.loadRuns();
          this.navigate('/history');
        }
      } catch (error) {
        console.error('Error stopping run:', error);
        alert('Failed to stop run. Please try again.');
      }
    }
  },
  
  // Tracking state
  route: [],
  distance: 0,
  time: 0,
  pace: 0,
  timerInterval: null,
  locationWatchId: null,
  
  startTracking() {
    this.route = [];
    this.distance = 0;
    this.time = 0;
    this.pace = 0;
    
    // Start timer
    this.startTimer();
    
    // Start location tracking
    this.startLocationTracking();
  },
  
  pauseTracking() {
    // Pause timer
    clearInterval(this.timerInterval);
    
    // Pause location tracking
    this.stopLocationTracking();
  },
  
  resumeTracking() {
    // Resume timer
    this.startTimer();
    
    // Resume location tracking
    this.startLocationTracking();
  },
  
  stopTracking() {
    // Stop timer
    clearInterval(this.timerInterval);
    
    // Stop location tracking
    this.stopLocationTracking();
    
    // Reset tracking state
    this.route = [];
    this.distance = 0;
    this.time = 0;
    this.pace = 0;
  },
  
  startTimer() {
    this.timerInterval = setInterval(() => {
      this.time++;
      this.updateTimerDisplay();
      
      // Update pace calculation
      if (this.distance > 0) {
        this.pace = this.time / 60 / this.distance;
        this.updatePaceDisplay();
      }
    }, 1000);
  },
  
  startLocationTracking() {
    if (!navigator.geolocation) {
      console.error('Geolocation is not supported by this browser.');
      return;
    }
    
    this.locationWatchId = navigator.geolocation.watchPosition(
      (position) => {
        const { latitude, longitude } = position.coords;
        const newPoint = { lat: latitude, lng: longitude, timestamp: Date.now() };
        
        // Add point to route
        if (this.route.length > 0) {
          // Calculate distance from last point
          const lastPoint = this.route[this.route.length - 1];
          const segmentDistance = this.calculateDistance(
            lastPoint.lat, lastPoint.lng, 
            newPoint.lat, newPoint.lng
          );
          
          // Only add point if it's a meaningful distance away
          if (segmentDistance > 0.001) { // 1 meter minimum
            this.route.push(newPoint);
            this.distance += segmentDistance;
            this.updateDistanceDisplay();
            
            // Update the map
            if (this.map && this.routeLine) {
              this.routeLine.addLatLng([latitude, longitude]);
              this.map.setView([latitude, longitude], 15);
            }
          }
        } else {
          // First point
          this.route.push(newPoint);
          
          // Center map on first point
          if (this.map) {
            this.map.setView([latitude, longitude], 15);
          }
        }
      },
      (error) => {
        console.error('Error getting location:', error);
      },
      {
        enableHighAccuracy: true,
        timeout: 5000,
        maximumAge: 0
      }
    );
  },
  
  stopLocationTracking() {
    if (this.locationWatchId !== null) {
      navigator.geolocation.clearWatch(this.locationWatchId);
      this.locationWatchId = null;
    }
  },
  
  updateTimerDisplay() {
    const timeElement = document.getElementById('time');
    if (timeElement) {
      const hours = Math.floor(this.time / 3600);
      const minutes = Math.floor((this.time % 3600) / 60);
      const seconds = this.time % 60;
      
      timeElement.textContent = `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
    }
  },
  
  updateDistanceDisplay() {
    const distanceElement = document.getElementById('distance');
    if (distanceElement) {
      distanceElement.textContent = `${this.distance.toFixed(2)} km`;
    }
  },
  
  updatePaceDisplay() {
    const paceElement = document.getElementById('pace');
    if (paceElement) {
      const minutes = Math.floor(this.pace);
      const seconds = Math.floor((this.pace - minutes) * 60);
      paceElement.textContent = `${minutes}:${seconds.toString().padStart(2, '0')} /km`;
    }
  },
  
  calculateDistance(lat1, lon1, lat2, lon2) {
    // Haversine formula to calculate distance between two coordinates
    const R = 6371; // Radius of the earth in km
    const dLat = this.deg2rad(lat2 - lat1);
    const dLon = this.deg2rad(lon2 - lon1);
    const a = 
      Math.sin(dLat/2) * Math.sin(dLat/2) +
      Math.cos(this.deg2rad(lat1)) * Math.cos(this.deg2rad(lat2)) * 
      Math.sin(dLon/2) * Math.sin(dLon/2)
    ; 
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
    const distance = R * c; // Distance in km
    return distance;
  },
  
  deg2rad(deg) {
    return deg * (Math.PI/180);
  }
};

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
  console.log('Run Tracker App loaded successfully!');
  
  // Set up navigation
  document.querySelectorAll('.nav-links a').forEach(link => {
    link.addEventListener('click', (e) => {
      e.preventDefault();
      AppState.navigate(link.getAttribute('href'));
    });
  });
  
  // Initialize app state
  AppState.init();
  
  // Check API connection
  API.getStatus()
    .then(data => {
      console.log('API Status:', data);
    })
    .catch(error => {
      console.error('API Error:', error);
    });
});