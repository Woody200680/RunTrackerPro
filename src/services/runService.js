import axios from 'axios';

const API_URL = 'http://localhost:8000/api/runs';

/**
 * Get all runs from the API
 * @returns {Promise<Array>} Array of run objects
 */
export const getAllRuns = async () => {
  try {
    const response = await axios.get(API_URL);
    return response.data;
  } catch (error) {
    console.error('Error fetching runs:', error);
    throw error;
  }
};

/**
 * Get a specific run by ID
 * @param {string} id Run ID
 * @returns {Promise<Object>} Run object
 */
export const getRun = async (id) => {
  try {
    const response = await axios.get(`${API_URL}/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching run ${id}:`, error);
    throw error;
  }
};

/**
 * Start a new run
 * @returns {Promise<Object>} New run object
 */
export const startRun = async () => {
  try {
    const response = await axios.post(API_URL, {
      startTime: new Date().getTime()
    });
    return response.data;
  } catch (error) {
    console.error('Error starting run:', error);
    throw error;
  }
};

/**
 * Pause a running run
 * @param {string} id Run ID
 * @returns {Promise<Object>} Updated run object
 */
export const pauseRun = async (id) => {
  try {
    const response = await axios.put(`${API_URL}/${id}/pause`, {
      pauseTime: new Date().getTime()
    });
    return response.data;
  } catch (error) {
    console.error(`Error pausing run ${id}:`, error);
    throw error;
  }
};

/**
 * Resume a paused run
 * @param {string} id Run ID
 * @returns {Promise<Object>} Updated run object
 */
export const resumeRun = async (id) => {
  try {
    const response = await axios.put(`${API_URL}/${id}/resume`, {
      resumeTime: new Date().getTime()
    });
    return response.data;
  } catch (error) {
    console.error(`Error resuming run ${id}:`, error);
    throw error;
  }
};

/**
 * Stop a run
 * @param {string} id Run ID
 * @param {Object} data Run data to save
 * @returns {Promise<Object>} Completed run object
 */
export const stopRun = async (id, data) => {
  try {
    const response = await axios.put(`${API_URL}/${id}/stop`, {
      endTime: new Date().getTime(),
      ...data
    });
    return response.data;
  } catch (error) {
    console.error(`Error stopping run ${id}:`, error);
    throw error;
  }
};
