/**
 * Calculate the distance of a route in kilometers
 * @param {Array} route Array of {lat, lng} points
 * @returns {number} Distance in kilometers
 */
export const calculateDistance = (route) => {
  if (!route || route.length < 2) {
    return 0;
  }

  let totalDistance = 0;
  
  for (let i = 1; i < route.length; i++) {
    const prevPoint = route[i - 1];
    const currentPoint = route[i];
    
    totalDistance += getDistanceBetweenPoints(
      prevPoint.lat, 
      prevPoint.lng, 
      currentPoint.lat, 
      currentPoint.lng
    );
  }
  
  return totalDistance;
};

/**
 * Calculate pace in minutes per kilometer
 * @param {number} distance Distance in kilometers
 * @param {number} time Time in seconds
 * @returns {number} Pace in minutes per kilometer
 */
export const calculatePace = (distance, time) => {
  if (distance <= 0 || time <= 0) {
    return 0;
  }
  
  // Convert seconds to minutes and divide by distance
  return (time / 60) / distance;
};

/**
 * Calculate the distance between two coordinates using the Haversine formula
 * @param {number} lat1 Latitude of first point
 * @param {number} lon1 Longitude of first point
 * @param {number} lat2 Latitude of second point
 * @param {number} lon2 Longitude of second point
 * @returns {number} Distance in kilometers
 */
const getDistanceBetweenPoints = (lat1, lon1, lat2, lon2) => {
  const R = 6371; // Radius of the Earth in km
  const dLat = deg2rad(lat2 - lat1);
  const dLon = deg2rad(lon2 - lon1);
  
  const a = 
    Math.sin(dLat/2) * Math.sin(dLat/2) +
    Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * 
    Math.sin(dLon/2) * Math.sin(dLon/2);
  
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  const distance = R * c; // Distance in km
  
  return distance;
};

/**
 * Convert degrees to radians
 * @param {number} deg Angle in degrees
 * @returns {number} Angle in radians
 */
const deg2rad = (deg) => {
  return deg * (Math.PI/180);
};
