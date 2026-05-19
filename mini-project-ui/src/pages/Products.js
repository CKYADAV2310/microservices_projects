import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { fetchAllProducts, addToCart } from '../services/api';

const Products = () => {
    const [products, setProducts] = useState([]);
    const [loading, setLoading] = useState(true);
    const [errorMsg, setErrorMsg] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        fetchAllProducts()
            .then(res => {
                // UNPACKING THE SHELL: If your Product-Service returns an ApiResponse wrapper, 
                // extract res.data.data. If it returns a direct array, use res.data.
                const productList = res.data && res.data.data ? res.data.data : res.data;
                
                if (Array.isArray(productList)) {
                    setProducts(productList);
                } else {
                    console.error("Expected an array but received:", productList);
                }
                setLoading(false);
            })
            .catch(err => {
                console.error("Error fetching products:", err);
                setErrorMsg("Failed to load catalog inventory. Please try again later.");
                setLoading(false);
            });
    }, []);

    const handleAddToCart = async (product) => {
        try {
            // Payloads are mapped to match your CartItem entity structure perfectly
            const payload = { 
                productId: product.id, 
                quantity: 1 
            };
            
            await addToCart(payload);
            alert(`🎉 ${product.name} successfully added to your cart!`);
        } catch (err) {
            console.error("Add to cart failed:", err);
            alert("Your session may have expired or the Cart Service is temporarily unreachable. Redirecting to login...");
            localStorage.clear();
            navigate('/');
        }
    };

    const handleLogout = () => {
        localStorage.clear();
        navigate('/');
    };

    if (loading) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh', fontSize: '20px' }}>
                🚀 Loading Storefront Inventory...
            </div>
        );
    }

    return (
        <div style={{ backgroundColor: '#f8f9fa', minHeight: '100vh' }}>
            <header style={headerStyle}>
                <div style={{ fontSize: '24px', fontWeight: '700', cursor: 'pointer' }} onClick={() => navigate('/customer-home')}>
                    🎬 Movie Store
                </div>
                <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
                    <button onClick={() => navigate('/cart')} style={cartBtn}>🛒 View Cart</button>
                    <button onClick={handleLogout} style={logoutBtn}>Logout</button>
                </div>
            </header>

            <main style={{ padding: '40px' }}>
                {errorMsg ? (
                    <div style={{ color: '#d93025', textAlign: 'center', fontSize: '18px', marginTop: '20px' }}>{errorMsg}</div>
                ) : products.length === 0 ? (
                    <div style={{ textAlign: 'center', fontSize: '18px', marginTop: '20px', color: '#5f6368' }}>No movies available in the catalog right now.</div>
                ) : (
                    <div style={gridStyle}>
                        {products.map(p => (
                            <div key={p.id} style={productCard}>
                                <img 
                                    src={p.imageUrl || 'https://via.placeholder.com/250x350?text=No+Poster+Available'} 
                                    alt={p.name} 
                                    style={imgStyle} 
                                />
                                <div style={{ padding: '15px', display: 'flex', flexDirection: 'column', gap: '8px' }}>
                                    <h3 style={{ margin: '0', fontSize: '18px', fontWeight: '600', color: '#202124' }}>{p.name}</h3>
                                    <p style={{ margin: '0', fontSize: '14px', color: '#5f6368', minHeight: '40px' }}>{p.description}</p>
                                    <p style={{ margin: '0', color: '#1a73e8', fontWeight: 'bold', fontSize: '20px' }}>₹{p.price.toFixed(2)}</p>
                                    <button onClick={() => handleAddToCart(p)} style={addBtn}>Add to Cart</button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </main>
        </div>
    );
};

// Layout Modifications and Button Styling Updates
const headerStyle = { display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '15px 40px', backgroundColor: '#fff', boxShadow: '0 2px 4px rgba(0,0,0,0.05)' };
const cartBtn = { backgroundColor: '#34a853', color: '#fff', border: 'none', padding: '8px 16px', borderRadius: '6px', cursor: 'pointer', fontWeight: '500' };
const logoutBtn = { background: 'none', border: '1px solid #d93025', color: '#d93025', padding: '8px 16px', borderRadius: '6px', cursor: 'pointer', fontWeight: '500' };
const gridStyle = { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(260px, 1fr))', gap: '30px' };
const productCard = { backgroundColor: '#fff', borderRadius: '12px', overflow: 'hidden', boxShadow: '0 4px 12px rgba(0,0,0,0.05)', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' };
const imgStyle = { width: '100%', height: '320px', objectFit: 'cover' };
const addBtn = { width: '100%', padding: '10px', backgroundColor: '#1a73e8', color: '#fff', border: 'none', borderRadius: '6px', cursor: 'pointer', fontWeight: '500', transition: 'background 0.2s' };

export default Products;