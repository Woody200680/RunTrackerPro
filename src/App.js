import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import Header from './components/Header';
import RunTracker from './components/RunTracker';
import RunHistory from './components/RunHistory';
import RunDetails from './components/RunDetails';

function App() {
  const [activeRun, setActiveRun] = useState(null);

  return (
    <Router>
      <div className="app-container">
        <Header activeRun={activeRun} />
        <main className="content">
          <Routes>
            <Route path="/" element={<RunTracker activeRun={activeRun} setActiveRun={setActiveRun} />} />
            <Route path="/history" element={<RunHistory />} />
            <Route path="/run/:id" element={<RunDetails />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;
