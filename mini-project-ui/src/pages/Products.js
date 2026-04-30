import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchAllProducts } from '../services/api'; // Using unified api import

const Products = () => {
    const [products, setProducts] = useState([]);
    const navigate = useNavigate();

    useEffect(() => {
        fetchAllProducts()
            .then(res => setProducts(res.data))
            .catch(err => console.error("Could not load products", err));
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('token');
        navigate('/');
    };

    return (
        <div>
            {/* Elegant Header */}
            <header style={headerStyle}>
                <div style={brandStyle}>🎬 Movie Store (Gateway)</div>
                <div style={navGroupStyle}>
                    <span style={userGreetingStyle}>Welcome, CKYADAV</span>
                    <button onClick={handleLogout} style={logoutButtonStyle}>Logout</button>
                </div>
            </header>

            {/* Main Content Area (automatically uses global grey background) */}
            <main style={mainContentStyle}>
                <div style={gridStyle}>
                    {products.map(p => (
                        <div key={p.id} style={productCardStyle}>
                            <div style={imageContainerStyle}>
                                <img src={p.imageUrl} alt={p.name} style={imageStyle} />
                            </div>
                            <div style={cardDetailsStyle}>
                                <h3 style={productNameStyle}>{p.name}</h3>
                                <p style={productPriceStyle}>${p.price.toFixed(2)}</p>
                                <button style={viewButtonStyle}>View Details</button>
                            </div>
                        </div>
                    ))}
                </div>
            </main>
        </div>
    );
};

/* --- REDESIGNED PRODUCTS STYLES --- */

const headerStyle = {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: '16px 40px',
    backgroundColor: 'white',
    boxShadow: '0 2px 10px rgba(0,0,0,0.03)',
    position: 'sticky',
    top: 0,
    zIndex: 100
};

const brandStyle = {
    fontSize: '24px',
    fontWeight: '700',
    color: '#1a1a1a'
};

const navGroupStyle = {
    display: 'flex',
    alignItems: 'center',
    gap: '20px'
};

const userGreetingStyle = {
    fontSize: '14px',
    color: '#666',
    fontWeight: '500'
};

const logoutButtonStyle = {
    padding: '8px 16px',
    fontSize: '14px',
    color: '#d93025', // Soft red
    backgroundColor: 'white',
    border: '1px solid #f5c6cb',
    borderRadius: '6px',
    cursor: 'pointer',
    transition: 'background-color 0.2s'
};

const mainContentStyle = {
    padding: '40px',
};

const gridStyle = {
    display: 'grid',
    gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
    gap: '30px',
};

const productCardStyle = {
    backgroundColor: '#ffffff',
    borderRadius: '16px',
    overflow: 'hidden',
    boxShadow: '0 6px 15px rgba(0,0,0,0.03)',
    transition: 'transform 0.2s, box-shadow 0.2s',
    '&:hover': {
        transform: 'translateY(-5px)',
        boxShadow: '0 12px 25px rgba(0,0,0,0.07)'
    }
};

const imageContainerStyle = {
    width: '100%',
    height: '240px',
    backgroundColor: '#eaeaea',
    overflow: 'hidden'
};

const imageStyle = {
    width: '100%',
    height: '100%',
    objectFit: 'cover' // Crucial for responsive images
};

const cardDetailsStyle = {
    padding: '20px',
    textAlign: 'left',
};

const productNameStyle = {
    fontSize: '18px',
    fontWeight: '600',
    color: '#1a1a1a',
    marginBottom: '8px',
};

const productPriceStyle = {
    fontSize: '20px',
    fontWeight: '700',
    color: '#1a73e8', // Match primary color
    marginBottom: '16px',
};

const viewButtonStyle = {
    width: '100%',
    padding: '10px',
    fontSize: '14px',
    fontWeight: '600',
    color: '#1a73e8',
    backgroundColor: 'white',
    border: '2px solid #e0e0e0',
    borderRadius: '8px',
    cursor: 'pointer'
};

export default Products;