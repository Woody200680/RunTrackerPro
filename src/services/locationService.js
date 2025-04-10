let watchId = null;

/**
 * Start tracking the user's location
 * @param {Function} callback Function to call with each location update
 * @returns {number} Interval ID for clearing the tracking
 */
export const trackLocation = (callback) => {
  // Check if browser supports geolocation
  if (!navigator.geolocation) {
    console.error('Geolocation is not supported by your browser');
    return null;
  }

  // Clear any existing watch
  if (watchId) {
    stopTracking();
  }

  // Set up geolocation watching
  watchId = navigator.geolocation.watchPosition(
    (position) => {
      callback(position);
    },
    (error) => {
      console.error('Error getting location:', error);
      
      // Handle specific errors
      switch(error.code) {
        case error.PERMISSION_DENIED:
          alert('Location tracking denied. Please enable location permissions for this app.');
          break;
        case error.POSITION_UNAVAILABLE:
          alert('Location information is unavailable. Please check your device settings.');
          break;
        case error.TIMEOUT:
          alert('Location request timed out. Please try again.');
          break;
        default:
          alert('An unknown error occurred while tracking location.');
          break;
      }
    },
    {
      enableHighAccuracy: true,  // Use GPS if available
      maximumAge: 0,             // Don't use cached position
      timeout: 5000              // Time to wait for a position
    }
  );

  // Return the interval ID for tracking at a more consistent interval
  return setInterval(() => {
    navigator.geolocation.getCurrentPosition(
      (position) => {
        callback(position);
      },
      (error) => {
        console.error('Error getting interval location:', error);
      },
      {
        enableHighAccuracy: true,
        maximumAge: 0,
        timeout: 5000
      }
    );
  }, 10000); // Update every 10 seconds for consistent tracking
};

/**
 * Stop tracking the user's location
 */
export const stopTracking = () => {
  if (watchId) {
    navigator.geolocation.clearWatch(watchId);
    watchId = null;
  }
};
