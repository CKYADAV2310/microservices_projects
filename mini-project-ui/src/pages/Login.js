import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { login, register } from '../services/api'; // Using unified api import from previous steps

const Login = () => {
    const [credentials, setCredentials] = useState({ username: '', password: '' });
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        try {
            const response = await login(credentials.username, credentials.password);
            localStorage.setItem('token', response.data.token);
            navigate('/products');
        } catch (error) {
            alert("Login Failed: " + (error.response?.data?.message || error.message));
        }
    };

    return (
        <div style={pageStyle}>
            <div style={cardStyle}>
                <h1 style={titleStyle}>Welcome Back</h1>
                <p style={subtitleStyle}>Sign in to access your Movie Store</p>
                
                <form onSubmit={handleLogin} style={formStyle}>
                    <input 
                        type="text" 
                        placeholder="Username" 
                        style={inputStyle} 
                        required
                        onChange={(e) => setCredentials({...credentials, username: e.target.value})} 
                    />
                    <input 
                        type="password" 
                        placeholder="Password" 
                        style={inputStyle} 
                        required
                        onChange={(e) => setCredentials({...credentials, password: e.target.value})} 
                    />
                    <button type="submit" style={primaryButtonStyle}>
                        Sign In
                    </button>
                </form>
                
                <div style={dividerStyle}>
                    <span>OR</span>
                </div>
                
                <button 
                    onClick={() => navigate('/register')}
                    style={secondaryButtonStyle}>
                    Create New Account
                </button>
            </div>
        </div>
    );
};

/* Unified Register Component for Visual Comparison */
export const Register = () => {
    const [userData, setUserData] = useState({ username: '', password: '', role: 'USER' });
    const navigate = useNavigate();

    const handleRegister = async (e) => {
        e.preventDefault();
        try {
            await register(userData); // Assuming 'register' from your api.js
            alert("Registration Successful!");
            navigate('/');
        } catch (error) {
            alert("Registration Failed.");
        }
    };

    return (
        <div style={pageStyle}>
            <div style={cardStyle}>
                <h1 style={titleStyle}>Create Account</h1>
                <p style={subtitleStyle}>Join the Movie Store network</p>
                
                <form onSubmit={handleRegister} style={formStyle}>
                    <input type="text" placeholder="Desired Username" style={inputStyle} onChange={(e) => setUserData({...userData, username: e.target.value})} required/>
                    <input type="password" placeholder="Password" style={inputStyle} onChange={(e) => setUserData({...userData, password: e.target.value})} required/>
                    <select style={inputStyle} onChange={(e) => setUserData({...userData, role: e.target.value})}>
                        <option value="USER">ROLE_USER</option>
                        <option value="ADMIN">ROLE_ADMIN</option>
                    </select>
                    <button type="submit" style={{...primaryButtonStyle, backgroundColor: '#28a745'}}>
                        Register
                    </button>
                </form>
                
                <p style={footerLinkStyle}>Already have an account? <span onClick={() => navigate('/')} style={linkSpanStyle}>Sign in</span></p>
            </div>
        </div>
    );
};

/* --- SHARED STYLES FOR AUTH PAGES --- */
const pageStyle = {
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    minHeight: '100vh',
    padding: '20px'
};

const cardStyle = {
    backgroundColor: '#ffffff',
    padding: '40px',
    borderRadius: '16px',
    boxShadow: '0 10px 25px rgba(0,0,0,0.05)',
    width: '100%',
    maxWidth: '400px',
    textAlign: 'center'
};

const titleStyle = {
    fontSize: '32px',
    fontWeight: '700',
    color: '#1a1a1a',
    marginBottom: '8px'
};

const subtitleStyle = {
    fontSize: '16px',
    color: '#666',
    marginBottom: '32px'
};

const formStyle = {
    display: 'flex',
    flexDirection: 'column',
    gap: '15px'
};

const inputStyle = {
    padding: '12px 16px',
    fontSize: '16px',
    border: '1px solid #ddd',
    borderRadius: '8px',
    outline: 'none',
    transition: 'border-color 0.2s',
    '&:focus': {
        borderColor: '#1a73e8'
    }
};

const primaryButtonStyle = {
    padding: '12px 16px',
    fontSize: '16px',
    fontWeight: '600',
    color: 'white',
    backgroundColor: '#1a73e8', // Professional blue
    border: 'none',
    borderRadius: '8px',
    cursor: 'pointer',
    transition: 'background-color 0.2s'
};

const secondaryButtonStyle = {
    ...primaryButtonStyle,
    backgroundColor: 'white',
    color: '#1a73e8',
    border: '2px solid #e0e0e0',
    marginTop: '10px',
    '&:hover': {
        backgroundColor: '#f8f9fa'
    }
};

const dividerStyle = {
    display: 'flex',
    alignItems: 'center',
    textAlign: 'center',
    color: '#999',
    margin: '25px 0'
};

const footerLinkStyle = {
    fontSize: '14px',
    color: '#666',
    marginTop: '25px'
};

const linkSpanStyle = {
    color: '#1a73e8',
    cursor: 'pointer',
    fontWeight: '600'
};

export default Login;