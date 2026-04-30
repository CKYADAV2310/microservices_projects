import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
// Change this to match the 'register' export in your api.js
import { register } from '../services/api'; 

const Register = () => {
    const [user, setUser] = useState({ username: '', email: '', password: '' });
    const navigate = useNavigate();

    const handleRegister = async (e) => {
        e.preventDefault();
        try {
            // Update the function call here as well
            await register(user);
            alert("Registration Successful!");
            navigate('/');
        } catch (err) {
            console.error(err);
            alert("Registration Failed.");
        }
    };

    return (
        <div style={containerStyle}>
            <h2>Register</h2>
            <form onSubmit={handleRegister}>
                <input placeholder="Username" style={inputStyle} onChange={e => setUser({...user, username: e.target.value})} /><br/>
                <input placeholder="Email" style={inputStyle} onChange={e => setUser({...user, email: e.target.value})} /><br/>
                <input type="password" placeholder="Password" style={inputStyle} onChange={e => setUser({...user, password: e.target.value})} /><br/><br/>
                <button type="submit" style={regBtnStyle}>Register</button>
            </form>
            <p>Already have an account? <Link to="/">Login here</Link></p>
        </div>
    );
};

const containerStyle = { textAlign: 'center', marginTop: '100px', fontFamily: 'Arial' };
const inputStyle = { padding: '10px', margin: '10px', width: '250px', borderRadius: '5px', border: '1px solid #ccc' };
const regBtnStyle = { padding: '10px 20px', backgroundColor: '#28a745', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' };

export default Register;