import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import Register from './pages/Register';
import CustomerHome from './pages/CustomerHome';
import Cart from './pages/Cart'; 

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Register />} />
        
        {/* If your product dashboard is mapped to /products, keep it here */}
        <Route path="/products" element={<CustomerHome />} /> 
        
        {/* 🟢 2. FIXED PATH ROUTE: This handles the navigate('/cart') action without kicking you out! */}
        <Route path="/cart" element={<Cart />} />
      </Routes>
    </Router>
  );
}

export default App;