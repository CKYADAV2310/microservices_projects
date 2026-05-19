import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css'; // Standard React base styling
import './App.css';   // <--- MAKE SURE THIS LINE IS ABSOLUTELY HERE!

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);