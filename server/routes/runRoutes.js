const express = require('express');
const router = express.Router();
const runService = require('../services/runService');

// Get all runs
router.get('/', (req, res) => {
  try {
    const runs = runService.getAllRuns();
    res.json(runs);
  } catch (error) {
    console.error('Error getting all runs:', error);
    res.status(500).json({ error: 'Failed to get runs' });
  }
});

// Get a specific run by ID
router.get('/:id', (req, res) => {
  try {
    const run = runService.getRun(req.params.id);
    
    if (!run) {
      return res.status(404).json({ error: 'Run not found' });
    }
    
    res.json(run);
  } catch (error) {
    console.error(`Error getting run ${req.params.id}:`, error);
    res.status(500).json({ error: 'Failed to get run' });
  }
});

// Create a new run
router.post('/', (req, res) => {
  try {
    const newRun = runService.createRun(req.body);
    res.status(201).json(newRun);
  } catch (error) {
    console.error('Error creating run:', error);
    res.status(500).json({ error: 'Failed to create run' });
  }
});

// Pause a run
router.put('/:id/pause', (req, res) => {
  try {
    const updatedRun = runService.pauseRun(req.params.id, req.body);
    
    if (!updatedRun) {
      return res.status(404).json({ error: 'Run not found' });
    }
    
    res.json(updatedRun);
  } catch (error) {
    console.error(`Error pausing run ${req.params.id}:`, error);
    res.status(500).json({ error: 'Failed to pause run' });
  }
});

// Resume a run
router.put('/:id/resume', (req, res) => {
  try {
    const updatedRun = runService.resumeRun(req.params.id, req.body);
    
    if (!updatedRun) {
      return res.status(404).json({ error: 'Run not found' });
    }
    
    res.json(updatedRun);
  } catch (error) {
    console.error(`Error resuming run ${req.params.id}:`, error);
    res.status(500).json({ error: 'Failed to resume run' });
  }
});

// Stop a run
router.put('/:id/stop', (req, res) => {
  try {
    const completedRun = runService.stopRun(req.params.id, req.body);
    
    if (!completedRun) {
      return res.status(404).json({ error: 'Run not found' });
    }
    
    res.json(completedRun);
  } catch (error) {
    console.error(`Error stopping run ${req.params.id}:`, error);
    res.status(500).json({ error: 'Failed to stop run' });
  }
});

module.exports = router;
