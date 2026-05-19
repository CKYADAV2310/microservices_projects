import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import axios from 'axios';

function Register() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [message, setMessage] = useState('');
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    setMessage('');
  
    try {
      // 🟢 Key names now match UserCredential.java variables EXACTLY
      await axios.post('http://localhost:8085/auth/register', {
        username: name,     // Sends the "name" state variable to the backend's "username" column
        email: email,       // Maps cleanly to your unique email column
        password: password, // Maps cleanly to your password column
        role: "ROLE_USER"   // Maps cleanly to your role column
      });
  
      setMessage('Registration Successful! Redirecting to login page...');
      
      setTimeout(() => {
        navigate('/');
      }, 2000);
  
    } catch (error) {
      console.error(error);
      if (error.response && error.response.data) {
        setMessage(`Registration Failed: ${error.response.data.message || 'Data validation error.'}`);
      } else {
        setMessage('Network Error: Gateway could not process registration at this time.');
      }
    }
  };

  return (
    // Added auth-card to perfectly inherit your layout's box styling
    <div className="container auth-card">
      <h2>Customer Sign Up</h2>
      <form onSubmit={handleRegister}>
        <input 
          type="text" 
          placeholder="Full Name" 
          value={name} 
          onChange={(e) => setName(e.target.value)} 
          required 
        />
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
        <button type="submit" className="btn-primary">Register</button>
      </form>

      {message && <p id="message" style={{ marginTop: '15px', fontWeight: 'bold' }}>{message}</p>}
      
      <p style={{ marginTop: '20px', fontSize: '14px', textAlign: 'center' }}>
        Already have an account? <Link to="/">Login here</Link>
      </p>
    </div>
  );
}

export default Register;