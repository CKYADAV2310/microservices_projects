/**
 * @file CustomerHome.js
 * @description Core storefront catalog dashboard component. Implements client-side 
 * pagination algorithms, dynamic category dropdown filtering, and an event-driven 
 * modal overlay architecture to render customer service contact metadata. Interacts 
 * with the Product Microservice for catalog data fetching and the Cart Microservice 
 * for persistence operations.
 * * @requires react
 * @requires react-router-dom
 * @requires ../services/api
 */

import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import API from '../services/api';

function CustomerHome() {
  /** * @type {[Array<Object>, Function]} 
   * @description Internal state holding the master collection of unfiltered product entities fetched from the database.
   */
  const [products, setProducts] = useState([]);
  
  /** * @type {[string, Function]} 
   * @description Tracks the active category selection string used for client-side evaluation rules. Defaults to 'ALL'.
   */
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  
  /** * @type {[Array<string>, Function]} 
   * @description Collection of distinct category strings dynamically derived from the product response payload.
   */
  const [categoriesList, setCategoriesList] = useState(['ALL']);

  /** @type {[string, Function]} Payload message text string for the custom notification toast. */
  const [toastMessage, setToastMessage] = useState('');
  
  /** @type {[boolean, Function]} Visibility visibility flag for the custom notification toast overlay. */
  const [showToast, setShowToast] = useState(false);
  
  /** @type {[string, Function]} Contextual style variant descriptor for toast feedback layers ('success' | 'error'). */
  const [toastType, setToastType] = useState('success');

  /** * @type {[number, Function]} 
   * @description Tracks the current active pagination page sequence index.
   */
  const [currentPage, setCurrentPage] = useState(1);
  
  /** * @type {[number]} 
   * @description Immutable block scalar defining the maximum quantity of product cards allowed per viewport slice.
   */
  const [productsPerPage] = useState(4);

  /** * @type {[boolean, Function]} 
   * @description Visibility toggle flag governing the execution and rendering of the corporate contact metadata modal layer.
   */
  const [showHelpModal, setShowHelpModal] = useState(false);

  /** @type {import('react-router-dom').NavigateFunction} Spatial client-side route redirection interface. */
  const navigate = useNavigate();

  /**
   * @effect
   * @description Post-mount initialization lifecycle hook. Executes an asynchronous HTTP GET 
   * request targeting the Product microservice resource endpoints to hydrate the catalog view tree.
   */
  useEffect(() => {
    API.get('/product/all')
      .then(response => {
        const data = response.data || [];
        setProducts(data);
        
        // Asynchronous structural data normalization to extract distinct category names for the dropdown
        const uniqueCategories = ['ALL', ...new Set(data.map(p => p.category).filter(Boolean))];
        setCategoriesList(uniqueCategories);
      })
      .catch(err => console.error("Critical Runtime Error: Unable to fetch store data schema ->", err));
  }, []);

  /**
   * @function triggerToast
   * @description Dispatches a temporary status notification toast box fixed above the lower boundary viewport.
   * Invokes a garbage collection frame macro-task loop to automatically strip the visibility layout flag.
   * * @param {string} msg - The notification string message body payload.
   * @param {string} [type='success'] - Functional context variation string mapping ('success' | 'error').
   * @returns {void}
   */
  const triggerToast = (msg, type = 'success') => {
    setToastMessage(msg);
    setToastType(type);
    setShowToast(true);
    setTimeout(() => setShowToast(false), 3000);
  };

  /**
   * @function addToCart
   * @async
   * @description Dispatches a synchronous HTTP POST mutation payload targeting the Cart microservice cluster gateway.
   * Leverages catch wrappers to capture and handle underlying microservice network state timeouts.
   * * @param {number|string} productId - The primary key relational entity identifier matching the product choice.
   * @throws {Error} Logs trace errors directly back to the terminal debug console on transport drops.
   * @returns {Promise<void>}
   */
  const addToCart = async (productId) => {
    try {
      await API.post('/cart/add', {
        productId: productId,
        quantity: 1
      });
      triggerToast("Item added to cart successfully!", 'success');
    } catch (error) {
      console.error("Cart Service Endpoint Ingress Transaction Failure:", error);
      triggerToast("Error adding item to cart. Please try again.", 'error');
    }
  };

  /**
   * @function handleHomeClick
   * @description Event callback bound to the primary dashboard navigation vector. Resets both filtering parameters 
   * and current pagination boundaries to baseline defaults, rendering the full master product grid array.
   * * @returns {void}
   */
  const handleHomeClick = () => {
    setSelectedCategory('ALL');
    setCurrentPage(1);
  };

  /**
   * @function handleCategoryChange
   * @description Ingress event capture method bound to the dropdown select element node. Updates filtering constraints
   * and resets pagination pointers to index page 1 to prevent empty viewport offsets on small item arrays.
   * * @param {React.ChangeEvent<HTMLSelectElement>} e - DOM modification change event container object.
   * @returns {void}
   */
  const handleCategoryChange = (e) => {
    setSelectedCategory(e.target.value);
    setCurrentPage(1); 
  };

  /**
   * @function logout
   * @description Purges active JSON Web Token (JWT) credentials from persistent local storage maps
   * and forces window location backwards to the entry authentication security portal.
   * * @returns {void}
   */
  const logout = () => {
    localStorage.removeItem('token');
    navigate('/');
  };

  // ==========================================
  // CLIENT-SIDE PAGINATION CALCULATIONS ENGINE
  // ==========================================
  const filteredProducts = selectedCategory === 'ALL' 
    ? products 
    : products.filter(p => p.category && p.category.toUpperCase() === selectedCategory.toUpperCase());

  const indexOfLastProduct = currentPage * productsPerPage;
  const indexOfFirstProduct = indexOfLastProduct - productsPerPage;
  
  /** @type {Array<Object>} Subset slice array comprising exclusively the records allocated to the active page index. */
  const currentPagedProducts = filteredProducts.slice(indexOfFirstProduct, indexOfLastProduct);
  
  /** @type {number} Computed upper limit page step index necessary to capture the entire active dataset. */
  const totalPages = Math.ceil(filteredProducts.length / productsPerPage);

  /** @type {Array<number>} Sequence tracking array utilized to iterate and output page selection elements. */
  const pageNumbers = [];
  for (let i = 1; i <= totalPages; i++) {
    pageNumbers.push(i);
  }

  return (
    <div style={styles.dashboardContainer}>
      
      {/* Toast Notification Box Overlay */}
      <div style={{
        ...styles.toast,
        opacity: showToast ? 1 : 0,
        transform: showToast ? 'translateY(0)' : 'translateY(20px)',
        pointerEvents: showToast ? 'auto' : 'none',
        backgroundColor: toastType === 'success' ? '#28a745' : '#dc3545'
      }}>
        {toastType === 'success' ? '✅ ' : '❌ '} {toastMessage}
      </div>

      {/* Embedded Support Metadata Modal Overlay */}
      {showHelpModal && (
        <div style={styles.modalOverlay} onClick={() => setShowHelpModal(false)}>
          <div style={styles.modalCard} onClick={(e) => e.stopPropagation()}>
            <div style={styles.modalHeader}>
              <h3 style={{ margin: 0, fontSize: '18px', fontWeight: '600' }}>Customer Support Help Desk</h3>
              <button style={styles.closeModalX} onClick={() => setShowHelpModal(false)}>×</button>
            </div>
            <div style={styles.modalBody}>
              <p style={{ margin: '0 0 12px 0', fontSize: '14px', color: '#475569', lineHeight: '1.5' }}>
                For technical inquiries or checkout route validation support, please contact our helpline:
              </p>
              <div style={styles.contactItem}><strong>Phone Support:</strong> +91 98765 43210</div>
              <div style={styles.contactItem}><strong>Toll-Free Helpline:</strong> 1800-123-4567</div>
              <div style={styles.contactItem}><strong>Email Desk:</strong> support@techstore.com</div>
              <p style={styles.supportAvailability}>Operating Hours: Mon - Sat | 9:00 AM - 7:00 PM IST</p>
            </div>
            <button style={styles.closeModalBtn} onClick={() => setShowHelpModal(false)}>Close View</button>
          </div>
        </div>
      )}

      {/* Primary Application Shell Bar Header */}
      <header style={styles.header}>
        <div>
          <h1 style={styles.mainTitle}>Customer Dashboard</h1>
          <p style={styles.subtitle}>Welcome back! Explore our exclusive tech catalog.</p>
        </div>
        
        <div style={styles.navButtons}>
          <button style={styles.navBtn} onClick={handleHomeClick}>Home</button>
          <button style={styles.primaryBtn} onClick={() => navigate('/cart')}>View Cart 🛒</button>
          <button style={styles.navBtn} onClick={() => setShowHelpModal(true)}>Help</button>
          <button style={styles.navBtn} onClick={() => alert("📞 Contact Us: Toll-Free 1800-123-4567")}>Contact Us</button>
          <button style={styles.dangerBtn} onClick={logout}>Logout</button>
        </div>
      </header>

      {/* Select Component Category Filter Toolbar Panel */}
      <div style={styles.filterBarContainer}>
        <label htmlFor="categorySelector" style={styles.filterBarLabel}>Choose Department:</label>
        <select
          id="categorySelector"
          value={selectedCategory}
          onChange={handleCategoryChange}
          style={styles.dropdownSelector}
        >
          {categoriesList.map(cat => (
            <option key={cat} value={cat}>
              {cat === 'ALL' ? "✨ View All Products" : cat.toUpperCase()}
            </option>
          ))}
        </select>
      </div>

      {/* Product Grid Processing Matrix */}
      <section style={styles.catalogSection}>
        <h2 style={styles.sectionTitle}>
          {selectedCategory === 'ALL' ? "Available Store Catalog" : `${selectedCategory} Selection`} ({filteredProducts.length})
        </h2>
        
        {currentPagedProducts.length === 0 ? (
          <div style={styles.noItemsFallback}>No hardware products found under this specific category segment.</div>
        ) : (
          <>
            <div style={styles.productGrid}>
              {currentPagedProducts.map(product => {
                
                // Fallback Asset Image Routing System
                let imageSource = product.imageUrl || 'https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=500&q=80';
                const prodName = product.name || "";

                if (prodName.toLowerCase().includes("odyssey") || prodName.toLowerCase().includes("samsung")) {
                  imageSource = "https://images.unsplash.com/photo-1616763355548-1b606f439f86?w=500&q=80";
                } else if (prodName.toLowerCase().includes("zephyrus") || prodName.toLowerCase().includes("asus")) {
                  imageSource = "https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=500&q=80";
                } else if (prodName.toLowerCase().includes("ipad") || prodName.toLowerCase().includes("m4")) {
                  imageSource = "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=500&q=80";
                } else if (prodName.toLowerCase().includes("surface") || prodName.toLowerCase().includes("microsoft")) {
                  imageSource = "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=500&q=80";
                } else if (prodName.toLowerCase().includes("nvidia") || prodName.toLowerCase().includes("rtx")) {
                  imageSource = "https://images.unsplash.com/photo-1591488320449-011701bb6704?w=500&q=80";
                }

                return (
                  <div key={product.id} style={styles.productCard}>
                    <div style={styles.imageWrapper}>
                      <img 
                        src={imageSource} 
                        alt={product.name} 
                        style={styles.productImage} 
                        onError={(e) => { e.target.src = 'https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=500&q=80'; }}
                      />
                    </div>
                    <div style={styles.cardBody}>
                      <div style={styles.categoryBadge}>{product.category || 'Tech'}</div>
                      <h3 style={styles.productName}>{product.name}</h3>
                      <p style={styles.productDescription}>{product.description}</p>
                      <div style={styles.cardFooter}>
                        <span style={styles.productPrice}>₹{product.price.toLocaleString('en-IN')}</span>
                        <button style={styles.addToCartBtn} onClick={() => addToCart(product.id)}>
                          Add To Cart
                        </button>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Pagination Controls Footer Navigation Segment */}
            {totalPages > 1 && (
              <div style={styles.paginationRowContainer}>
                <button 
                  style={styles.pageArrowBtn} 
                  disabled={currentPage === 1}
                  onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                >
                  « Prev
                </button>
                
                {pageNumbers.map(number => (
                  <button
                    key={number}
                    onClick={() => setCurrentPage(number)}
                    style={{
                      ...styles.pageNumberBtn,
                      backgroundColor: currentPage === number ? '#2874f0' : '#fff',
                      color: currentPage === number ? '#fff' : '#212529',
                      borderColor: currentPage === number ? '#2874f0' : '#cbd5e1'
                    }}
                  >
                    {number}
                  </button>
                ))}

                <button 
                  style={styles.pageArrowBtn} 
                  disabled={currentPage === totalPages}
                  onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                >
                  Next »
                </button>
              </div>
            )}
          </>
        )}
      </section>
    </div>
  );
}

const styles = {
  dashboardContainer: { padding: '30px max(4%, 20px)', fontFamily: "'Segoe UI', Roboto, sans-serif", backgroundColor: '#f8f9fa', minHeight: '100vh', position: 'relative' },
  toast: { position: 'fixed', bottom: '30px', right: '30px', color: '#fff', padding: '14px 24px', borderRadius: '8px', boxShadow: '0 4px 12px rgba(0,0,0,0.15)', zIndex: 2000, fontWeight: '600', fontSize: '15px', transition: 'all 0.3s ease', display: 'flex', alignItems: 'center', gap: '8px' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: '20px', paddingBottom: '25px', borderBottom: '1px solid #e9ecef', marginBottom: '20px' },
  mainTitle: { margin: 0, fontSize: '30px', color: '#212529', fontWeight: '700' },
  subtitle: { margin: '5px 0 0 0', color: '#6c757d', fontSize: '15px' },
  navButtons: { display: 'flex', gap: '10px', flexWrap: 'wrap' },
  navBtn: { backgroundColor: '#fff', color: '#495057', border: '1px solid #ced4da', padding: '10px 18px', borderRadius: '6px', cursor: 'pointer', fontWeight: '600', transition: 'all 0.15s ease' },
  primaryBtn: { backgroundColor: '#2874f0', color: '#fff', border: 'none', padding: '10px 20px', borderRadius: '6px', cursor: 'pointer', fontWeight: '600', transition: 'all 0.15s ease' },
  dangerBtn: { backgroundColor: '#dc3545', color: '#fff', border: 'none', padding: '10px 18px', borderRadius: '6px', cursor: 'pointer', fontWeight: '600' },
  filterBarContainer: { display: 'flex', alignItems: 'center', gap: '12px', backgroundColor: '#fff', padding: '14px 20px', borderRadius: '8px', border: '1px solid #e9ecef', marginBottom: '30px', maxWidth: '380px', boxShadow: '0 2px 4px rgba(0,0,0,0.02)' },
  filterBarLabel: { fontSize: '13px', fontWeight: '700', color: '#495057', textTransform: 'uppercase', letterSpacing: '0.5px' },
  dropdownSelector: { padding: '9px 14px', borderRadius: '6px', border: '1px solid #cbd5e1', fontSize: '14px', fontWeight: '600', color: '#334155', backgroundColor: '#fff', outline: 'none', cursor: 'pointer', flexGrow: 1, boxShadow: '0 1px 2px rgba(0,0,0,0.05)' },
  catalogSection: { marginTop: '10px' },
  sectionTitle: { fontSize: '22px', color: '#343a40', marginBottom: '25px', fontWeight: '600' },
  productGrid: { display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '30px' },
  noItemsFallback: { padding: '40px', backgroundColor: '#fff', borderRadius: '8px', textAlign: 'center', color: '#6c757d', border: '1px solid #e9ecef', fontWeight: '500' },
  productCard: { backgroundColor: '#fff', borderRadius: '12px', overflow: 'hidden', boxShadow: '0 4px 15px rgba(0,0,0,0.03)', display: 'flex', flexDirection: 'column', transition: 'transform 0.2s', border: '1px solid #e9ecef' },
  imageWrapper: { width: '100%', height: '180px', backgroundColor: '#f1f3f5', overflow: 'hidden' },
  productImage: { width: '100%', height: '100%', objectFit: 'cover' },
  cardBody: { padding: '20px', display: 'flex', flexDirection: 'column', flexGrow: 1 },
  categoryBadge: { alignSelf: 'flex-start', backgroundColor: '#e8f0fe', color: '#1a73e8', fontSize: '11px', fontWeight: '700', padding: '4px 8px', borderRadius: '4px', textTransform: 'uppercase', marginBottom: '10px' },
  productName: { margin: '0 0 8px 0', fontSize: '17px', color: '#212529', fontWeight: '600' },
  productDescription: { margin: '0 0 20px 0', fontSize: '14px', color: '#6c757d', lineHeight: '1.4', flexGrow: 1 },
  cardFooter: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: 'auto' },
  productPrice: { fontSize: '19px', fontWeight: '700', color: '#212529' },
  addToCartBtn: { backgroundColor: '#249746', color: '#fff', border: 'none', padding: '10px 16px', borderRadius: '6px', cursor: 'pointer', fontWeight: '600', fontSize: '13px' },
  paginationRowContainer: { display: 'flex', justifyContent: 'center', alignItems: 'center', gap: '8px', marginTop: '40px', paddingBottom: '20px' },
  pageArrowBtn: { padding: '9px 16px', border: '1px solid #cbd5e1', backgroundColor: '#fff', color: '#334155', borderRadius: '6px', cursor: 'pointer', fontSize: '14px', fontWeight: '600', transition: 'all 0.2s' },
  pageNumberBtn: { width: '38px', height: '38px', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '1px solid', borderRadius: '6px', fontSize: '14px', fontWeight: '700', cursor: 'pointer', transition: 'all 0.15s ease' },
  modalOverlay: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(15, 23, 42, 0.6)', backdropFilter: 'blur(4px)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 3000 },
  modalCard: { backgroundColor: '#fff', width: '92%', maxWidth: '440px', borderRadius: '12px', padding: '24px', boxShadow: '0 20px 25px -5px rgba(0,0,0,0.1), 0 10px 10px -5px rgba(0,0,0,0.04)', display: 'flex', flexDirection: 'column', gap: '16px' },
  modalHeader: { display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #e2e8f0', paddingBottom: '12px', color: '#1e293b' },
  closeModalX: { background: 'none', border: 'none', fontSize: '24px', cursor: 'pointer', color: '#94a3b8', lineHeight: '1' },
  modalBody: { display: 'flex', flexDirection: 'column', gap: '12px' },
  contactItem: { fontSize: '14px', color: '#334155', backgroundColor: '#f8fafc', padding: '11px 14px', borderRadius: '6px', border: '1px solid #f1f5f9', fontWeight: '500' },
  supportAvailability: { margin: '4px 0 0 0', fontSize: '12px', color: '#166534', fontWeight: '600', textAlign: 'center', backgroundColor: '#f0fdf4', padding: '6px', borderRadius: '4px' },
  closeModalBtn: { backgroundColor: '#1e293b', color: '#fff', border: 'none', padding: '12px 0', fontSize: '14px', fontWeight: '600', borderRadius: '6px', cursor: 'pointer', width: '100%', marginTop: '5px' }
};

export default CustomerHome;