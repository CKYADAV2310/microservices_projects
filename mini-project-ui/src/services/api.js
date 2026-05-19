import axios from 'axios';

// 1. Point this to your Gateway (03FOR-GATEWAY) port.
// Since you mentioned 8085 in your YAML, we'll use that.
const API_BASE_URL = 'http://localhost:8085'; 

const api = axios.create({
    baseURL: API_BASE_URL
});

/**
 * 2. AXIOS INTERCEPTOR
 * This automatically adds the JWT token to the header of every request.
 * This is essential for your 05CART-SERVICE and 04PRODUCT-SERVICE 
 * to verify who is making the request.
 */
api.interceptors.request.use(config => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
}, error => {
    return Promise.reject(error);
});

/**
 * 3. AUTHENTICATION SERVICES (Routes to 02AUTH-SERVICE)
 */
export const login = (credentials) => {
    return api.post('/auth/login', credentials);
};

export const register = (userData) => {
    return api.post('/auth/register', userData);
};

/**
 * 4. PRODUCT SERVICES (Routes to 04PRODUCT-SERVICE via Gateway)
 */
export const fetchAllProducts = () => {
    // Matches your route Path=/product/** in the Gateway config
    return api.get('/product/all'); 
};

/**
 * 5. CART SERVICES (Routes to 05CART-SERVICE via Gateway)
 * This allows the UI to interact with your Redis-backed cart.
 */
export const addToCart = (cartItem) => {
    return api.post('/cart/add', cartItem);
};

export default api;