import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';

function Login() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setMessage('Connecting to authentication gateway...'); // Immediate feedback on click
  
    try {
      const response = await axios.post('http://localhost:8085/auth/login', {
        email: email,
        password: password
      }, {
        // 🟢 Force Axios to accept plain text data smoothly without crashing the parser
        responseType: 'text' 
      });
  
      // Capture the string token directly
      const token = response.data;
      
      if (token && token.trim() !== "") {
        localStorage.setItem('token', token);
        setMessage('Login Successful! Redirecting...');
  
        // Base64 Decode the payload of the JWT to check user role
        const payloadBase64 = token.split('.')[1];
        const decodedPayload = JSON.parse(atob(payloadBase64));
        const userRole = decodedPayload.role || ''; 
  
        setTimeout(() => {
          if (userRole.toUpperCase() === 'ROLE_ADMIN') {
            navigate('/admin/products');
          } else {
            navigate('/products');
          }
        }, 1000);
      } else {
        setMessage('Authentication failed: Received empty validation token from server.');
      }
    } catch (error) {
      console.error("Login catch block caught an error:", error);
      
      if (error.response) {
        if (error.response.status === 401) {
          setMessage('Access Denied: Invalid Email or Password.');
        } else {
          setMessage(`Server Error Status: ${error.response.status}. Check backend logs.`);
        }
      } else if (error.request) {
        setMessage('System Error: Gateway port 8085 is not responding.');
      } else {
        setMessage(`Unexpected error layout: ${error.message}`);
      }
    }
  };

  return (
    <div className="container auth-card">
      <h2>Account Sign In</h2>
      <form onSubmit={handleLogin}>
        <input 
          type="email" 
          placeholder="Email Address" 
          value={email} 
          onChange={(e) => setEmail(e.target.value)} 
          required 
        />
        <input 
          type="password" 
          placeholder="Password" 
          value={password} 
          onChange={(e) => setPassword(e.target.value)} 
          required 
        />
        <button type="submit" className="btn-primary">Login</button>
      </form>
      
      {message && <p id="message" style={{ marginTop: '15px', fontWeight: 'bold' }}>{message}</p>}
      
      <p style={{ marginTop: '20px', fontSize: '14px', textAlign: 'center' }}>
        New to the store? <Link to="/register">Sign up here</Link>
      </p>
    </div>
  );
}

export default Login;