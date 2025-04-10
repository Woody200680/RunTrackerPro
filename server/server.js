const express = require('express');
const cors = require('cors');
const path = require('path');
const runRoutes = require('./routes/runRoutes');

const app = express();
const PORT = 5000; // Changed to 5000 to match the exposed port

// Middleware
app.use(cors());
app.use(express.json());

// API status endpoint
app.get('/api/status', (req, res) => {
  res.json({ status: 'Server is running!', time: new Date().toISOString() });
});

// API Routes
app.use('/api/runs', runRoutes);

// Serve static files from public directory
app.use(express.static(path.join(__dirname, '../public')));

// Fallback route for SPA
app.get('/', (req, res) => {
  res.sendFile(path.join(__dirname, '../public/index.html'));
});

app.get('/history', (req, res) => {
  res.sendFile(path.join(__dirname, '../public/index.html'));
});

app.get('/run/:runId', (req, res) => {
  res.sendFile(path.join(__dirname, '../public/index.html'));
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('Server error:', err.stack);
  res.status(500).json({
    error: 'Something went wrong on the server',
    message: err.message
  });
});

// Start server
app.listen(PORT, '0.0.0.0', () => {
  console.log(`Server running on http://0.0.0.0:${PORT}`);
});
