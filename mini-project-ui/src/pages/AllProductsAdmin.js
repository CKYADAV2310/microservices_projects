import React, { useEffect, useState } from 'react';
import API from '../services/api';

function AllProductsAdmin() {
  const [products, setProducts] = useState([]);

  useEffect(() => {
    fetchAdminCatalog();
  }, []);

  const fetchAdminCatalog = async () => {
    try {
      const response = await API.get('/product/all');
      setProducts(response.data);
    } catch (error) {
      console.error("Error pulling product list", error);
    }
  };

  const handleDelete = async (productId) => {
    if (window.confirm("Are you absolutely sure you want to delete this product? This will purge all active user carts!")) {
      try {
        // Hits your secured admin delete routing endpoint directly
        await API.delete(`/product/admin/delete/${productId}`);
        alert("Product permanently deleted. Kafka broadcast issued successfully.");
        fetchAdminCatalog(); // Instantly update active rendering state tables
      } catch (error) {
        alert("Action failed: Administrative clearances required.");
      }
    }
  };

  return (
    <div className="container" style={{ width: '90%' }}>
      <h2>All Products (Admin Control Console)</h2>
      
      <div id="productList" style={{ marginTop: '20px' }}>
        {products.length === 0 ? (
          <p>No inventory found inside the catalog database.</p>
        ) : (
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
            <thead>
              <tr style={{ borderBottom: '2px solid #333' }}>
                <th>ID</th>
                <th>Name</th>
                <th>Category</th>
                <th>Price</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map(prod => (
                <tr key={prod.id} style={{ borderBottom: '1px solid #eee', height: '40px' }}>
                  <td>{prod.id}</td>
                  <td>{prod.name}</td>
                  <td>{prod.category}</td>
                  <td>₹{prod.price}</td>
                  <td>
                    <button 
                      style={{ backgroundColor: '#d9534f', color: '#fff', border: 'none', padding: '5px 10px', borderRadius: '3px', cursor: 'pointer' }}
                      onClick={() => handleDelete(prod.id)}
                    >
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

export default AllProductsAdmin;