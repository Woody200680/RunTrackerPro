// In-memory database for storing runs
const runs = [];

/**
 * Generate a unique ID for a new run
 * @returns {string} Unique ID
 */
const generateId = () => {
  return Date.now().toString(36) + Math.random().toString(36).substring(2);
};

/**
 * Get all runs
 * @returns {Array} Array of run objects
 */
const getAllRuns = () => {
  // Sort by start time descending (newest first)
  return [...runs].sort((a, b) => b.startTime - a.startTime);
};

/**
 * Get a specific run by ID
 * @param {string} id Run ID
 * @returns {Object|null} Run object or null if not found
 */
const getRun = (id) => {
  return runs.find(run => run.id === id) || null;
};

/**
 * Create a new run
 * @param {Object} data Initial run data
 * @returns {Object} New run object
 */
const createRun = (data) => {
  const newRun = {
    id: generateId(),
    startTime: data.startTime || Date.now(),
    status: 'active',
    route: [],
    distance: 0,
    duration: 0,
    pace: 0,
    pauseTimes: [],
    resumeTimes: []
  };
  
  runs.push(newRun);
  return newRun;
};

/**
 * Pause a run
 * @param {string} id Run ID
 * @param {Object} data Pause data
 * @returns {Object|null} Updated run object or null if not found
 */
const pauseRun = (id, data) => {
  const run = getRun(id);
  
  if (!run) {
    return null;
  }
  
  run.status = 'paused';
  run.pauseTimes.push(data.pauseTime || Date.now());
  
  return run;
};

/**
 * Resume a paused run
 * @param {string} id Run ID
 * @param {Object} data Resume data
 * @returns {Object|null} Updated run object or null if not found
 */
const resumeRun = (id, data) => {
  const run = getRun(id);
  
  if (!run) {
    return null;
  }
  
  run.status = 'active';
  run.resumeTimes.push(data.resumeTime || Date.now());
  
  return run;
};

/**
 * Stop and complete a run
 * @param {string} id Run ID
 * @param {Object} data Run completion data
 * @returns {Object|null} Completed run object or null if not found
 */
const stopRun = (id, data) => {
  const run = getRun(id);
  
  if (!run) {
    return null;
  }
  
  run.status = 'completed';
  run.endTime = data.endTime || Date.now();
  run.distance = data.distance || 0;
  run.duration = data.duration || 0;
  run.pace = data.pace || 0;
  run.route = data.route || [];
  
  // Calculate total time including pauses
  let totalDuration = run.duration;
  
  // If there are pauses, calculate actual duration
  if (run.pauseTimes.length > 0 && run.resumeTimes.length > 0) {
    let pausedTime = 0;
    for (let i = 0; i < Math.min(run.pauseTimes.length, run.resumeTimes.length); i++) {
      pausedTime += run.resumeTimes[i] - run.pauseTimes[i];
    }
    
    // If there's an extra pause without resume, add time until endTime
    if (run.pauseTimes.length > run.resumeTimes.length) {
      pausedTime += run.endTime - run.pauseTimes[run.pauseTimes.length - 1];
    }
    
    totalDuration = Math.round((run.endTime - run.startTime - pausedTime) / 1000);
    run.duration = totalDuration;
  }
  
  return run;
};

module.exports = {
  getAllRuns,
  getRun,
  createRun,
  pauseRun,
  resumeRun,
  stopRun
};
