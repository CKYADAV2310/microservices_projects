import axios from 'axios';

// Change 8080 to 8085 to match your YAML config
const API_BASE_URL = 'http://localhost:8085'; 

export const login = (credentials) => {
    return axios.post(`${API_BASE_URL}/auth/login`, credentials);
};

export const register = (userData) => {
    return axios.post(`${API_BASE_URL}/auth/register`, userData);
};

export const fetchAllProducts = () => {
    return axios.get(`${API_BASE_URL}/product/all`); // Ensure path matches your route Path=/product/**
};