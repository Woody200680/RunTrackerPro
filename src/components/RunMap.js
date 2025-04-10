import React, { useEffect, useRef, useState } from 'react';

const RunMap = ({ route = [], isStatic = false }) => {
  const mapRef = useRef(null);
  const leafletMapRef = useRef(null);
  const polylineRef = useRef(null);
  const [leafletLoaded, setLeafletLoaded] = useState(false);

  // Check if Leaflet is available
  useEffect(() => {
    const checkLeaflet = () => {
      if (window.L) {
        setLeafletLoaded(true);
        return true;
      }
      return false;
    };
    
    // Check if Leaflet is already loaded
    if (!checkLeaflet()) {
      // If not, set up an interval to keep checking
      const interval = setInterval(() => {
        if (checkLeaflet()) {
          clearInterval(interval);
        }
      }, 100);
      
      // Clean up interval
      return () => clearInterval(interval);
    }
  }, []);

  // Initialize map once Leaflet is loaded
  useEffect(() => {
    if (!leafletLoaded || !mapRef.current || leafletMapRef.current) return;
    
    try {
      // Initialize map
      leafletMapRef.current = window.L.map(mapRef.current).setView([0, 0], 13);
      
      window.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
      }).addTo(leafletMapRef.current);
      
      polylineRef.current = window.L.polyline([], { 
        color: '#007BFF', 
        weight: 5, 
        opacity: 0.8 
      }).addTo(leafletMapRef.current);
      
      // Return a cleanup function to destroy the map when the component unmounts
      return () => {
        if (leafletMapRef.current) {
          leafletMapRef.current.remove();
          leafletMapRef.current = null;
        }
      };
    } catch (error) {
      console.error('Error initializing map:', error);
    }
  }, [leafletLoaded]);

  // Update route on the map
  useEffect(() => {
    if (!leafletLoaded || !leafletMapRef.current || !route.length) return;
    
    try {
      // Update the polyline with new route data
      const points = route.map(point => [point.lat, point.lng]);
      polylineRef.current.setLatLngs(points);
      
      // Add start and finish markers if there are points
      if (points.length > 0) {
        // Remove old markers
        leafletMapRef.current.eachLayer(layer => {
          if (layer instanceof window.L.Marker) {
            leafletMapRef.current.removeLayer(layer);
          }
        });
        
        // Add start marker
        const startIcon = window.L.divIcon({
          html: '<i class="fas fa-play-circle" style="color: green; font-size: 24px;"></i>',
          className: 'start-marker-icon',
          iconSize: [24, 24],
          iconAnchor: [12, 12]
        });
        
        window.L.marker(points[0], { icon: startIcon }).addTo(leafletMapRef.current);
        
        // Add end marker if run is completed (for static view) or has multiple points
        if (isStatic || points.length > 1) {
          const endIcon = window.L.divIcon({
            html: '<i class="fas fa-flag-checkered" style="color: red; font-size: 24px;"></i>',
            className: 'end-marker-icon',
            iconSize: [24, 24],
            iconAnchor: [12, 12]
          });
          
          window.L.marker(points[points.length - 1], { icon: endIcon }).addTo(leafletMapRef.current);
        }
      }
      
      // Center and zoom the map to show all points
      if (isStatic && points.length > 1) {
        const bounds = window.L.latLngBounds(points);
        leafletMapRef.current.fitBounds(bounds, {
          padding: [30, 30],
          maxZoom: 16
        });
      } else if (points.length > 0) {
        // For live tracking, center on the latest point
        leafletMapRef.current.setView(points[points.length - 1], 16);
      }
    } catch (error) {
      console.error('Error updating map route:', error);
    }
  }, [route, isStatic, leafletLoaded]);

  return (
    <div 
      ref={mapRef} 
      className={`run-map ${isStatic ? 'static-map' : 'active-map'}`}
      aria-label="Map showing your run route"
    >
      {!leafletLoaded && (
        <div className="map-loading">
          <p>Loading map...</p>
        </div>
      )}
    </div>
  );
};

export default RunMap;
