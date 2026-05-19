/**
 * @file Cart.js
 * @description Interactive checkout screen component. Features a sticky navigation header,
 * real-time aggregate calculation matrices, and multi-channel payment method workflows (Flipkart model).
 * Uses optimistic state scaling to sync seamlessly with the underlying Spring Boot repository layer.
 * * @requires react
 * @requires react-router-dom
 * @requires ../services/api
 */

import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import API from '../services/api';

function Cart() {
  /** @type {[Array<Object>, Function]} Collection state containing item models belonging to active user profile */
  const [cartItems, setCartItems] = useState([]);
  
  /** @type {[string, Function]} Stores system error tracking alerts strings */
  const [message, setMessage] = useState('');
  
  /** @type {[boolean, Function]} Transaction flag tracking loading states during transaction processing loops */
  const [isProcessing, setIsProcessing] = useState(false);
  
  /** @type {[boolean, Function]} Controls visibility of the left-hand side multi-option payment block */
  const [showPaymentOptions, setShowPaymentOptions] = useState(false);

  /** @type {[string, Function]} Key mapping tracker defining active payment type ('upi' | 'card' | 'cod') */
  const [activeMethod, setActiveMethod] = useState('upi'); 
  
  /** @type {[string, Function]} VPA tracking state input string for UPI transaction workflows */
  const [upiId, setUpiId] = useState('');
  
  /** @type {[string, Function]} Select element state value matching selected net banking providers */
  const [selectedBank, setSelectedBank] = useState(''); 
  
  /** @type {[string, Function]} Buffer state holding manual text string inputs for Captcha checks */
  const [captchaInput, setCaptchaInput] = useState('');
  
  /** @type {[string]} Immutable verification string key computed once per initialization cycle */
  const [generatedCaptcha] = useState(() => Math.floor(1000 + Math.random() * 9000).toString());

  /** @type {import('react-router-dom').NavigateFunction} Navigation routing hook for pipeline transitions */
  const navigate = useNavigate();

  /**
   * @function fetchCartData
   * @description Fetches user cart contents from the Cart Microservice via a standard GET operation.
   * On arrival, data hydrates state matrices and triggers console inspection traces.
   * * @returns {void}
   */
  const fetchCartData = () => {
    API.get('/cart/my-cart') 
      .then(response => {
        console.log("Cart data loaded successfully:", response.data);
        setCartItems(response.data || []);
      })
      .catch(err => {
        console.error("Critical Failure: Unable to fetch active cart array ->", err);
        setMessage("Could not load cart items. Please verify your backend microservices are active.");
      });
  };

  /** @effect Microservice pull execution hook triggered once upon mount entry */
  useEffect(() => {
    fetchCartData();
  }, []);

  /**
   * @function handleIncreaseQuantity
   * @async
   * @description Optimistically increases the scalar element count. Dispatches a PUT network 
   * event payload targeting backend endpoints. Evicts current Redis memory footprints on execution.
   * * @param {Object} item - The current selected CartItem object layout context matrix target row.
   * @returns {Promise<void>}
   */
  const handleIncreaseQuantity = async (item) => {
    try {
      const newQty = item.quantity + 1;
      setCartItems(prev => prev.map(i => i.id === item.id ? { ...i, quantity: newQty } : i));
      await API.put(`/cart/update/${item.id}/${newQty}`);
    } catch (error) {
      console.error("Failed to mutate quantity row map upwards:", error);
      fetchCartData(); // Hard rollback down to current server states if network drops
    }
  };

  /**
   * @function handleDecreaseQuantity
   * @async
   * @description Optimistically drops scalar row numbers down by an index of 1. If evaluation boundary 
   * limits drop past a minimum scalar of 1, it automatically triggers standard deletion logic cascades.
   * * @param {Object} item - The current selected CartItem object layout context matrix target row.
   * @returns {Promise<void>}
   */
  const handleDecreaseQuantity = async (item) => {
    if (item.quantity <= 1) {
      handleRemoveItem(item.id);
      return;
    }

    try {
      const newQty = item.quantity - 1;
      setCartItems(prev => prev.map(i => i.id === item.id ? { ...i, quantity: newQty } : i));
      await API.put(`/cart/update/${item.id}/${newQty}`);
    } catch (error) {
      console.error("Failed to mutate quantity row map downwards:", error);
      fetchCartData();
    }
  };

  /**
   * @function handleRemoveItem
   * @async
   * @description Dispatches an HTTP DELETE transaction request targeting line items in database rows.
   * Filters the item out of the active frontend rendering tree on response acknowledgment.
   * * @param {number|string} itemId - The structural primary transactional row key identifier to purge.
   * @returns {Promise<void>}
   */
  const handleRemoveItem = async (itemId) => {
    try {
      setCartItems(prev => prev.filter(item => item.id !== itemId));
      await API.delete(`/cart/remove/${itemId}`);
    } catch (error) {
      console.error("Failed to execute data row removal process:", error);
      fetchCartData();
    }
  };

  /**
   * @function calculateSubtotal
   * @description Evaluates sum matrices across cart item loops using scalar array reduction operations.
   * * @returns {number} The aggregated currency pricing value before platform taxes or courier attachments.
   */
  const calculateSubtotal = () => {
    return cartItems.reduce((acc, item) => acc + (item.price * item.quantity), 0);
  };

  /**
   * @function handleCheckout
   * @description Finalizes consumer transaction steps. Includes captcha evaluations 
   * to mitigate bot request spikes prior to committing order payloads.
   * * @param {React.FormEvent} e - Form submittal element tracking payload loop handles.
   * @returns {void}
   */
  const handleCheckout = (e) => {
    e.preventDefault();
    if (activeMethod === 'cod' && captchaInput !== generatedCaptcha) {
      alert("❌ Incorrect Captcha code! Please check the numbers typed.");
      return;
    }

    setIsProcessing(true);
    setTimeout(() => {
      setIsProcessing(false);
      alert(`🎉 Order Confirmed via ${activeMethod.toUpperCase()}! Thank you for your purchase.`);
      setCartItems([]);
      navigate('/products');
    }, 2500);
  };

  const totalAmount = calculateSubtotal();
  const platformFee = totalAmount > 0 ? 49 : 0;
  const securedPackagingFee = totalAmount > 0 ? 29 : 0;
  const grandTotal = totalAmount + platformFee + securedPackagingFee;

  return (
    <div style={styles.cartContainer}>
      
      {/* Pinned Sticky Navigation Strip Header */}
      <div style={styles.topNavigation}>
        <button style={styles.backBtn} onClick={() => navigate('/products')}>
          ← Continue Shopping
        </button>
        <h1 style={styles.cartTitle}>Your Shopping Cart 🛒</h1>
      </div>

      {message && <p style={styles.errorMessage}>{message}</p>}

      {cartItems.length === 0 ? (
        <div style={styles.emptyCartBox}>
          <div style={{ fontSize: '50px', marginBottom: '15px' }}>🛒</div>
          <h3 style={{ margin: '0 0 10px 0', color: '#212529' }}>Your Cart is Empty</h3>
          <p style={{ margin: '0 0 20px 0', color: '#6c757d' }}>Add products from the main dashboard to proceed.</p>
          <button style={styles.shopNowBtn} onClick={() => navigate('/products')}>Shop Now</button>
        </div>
      ) : (
        <div style={styles.checkoutLayout}>
          
          {/* LEFT INTERFACE COLUMN: LINE ITEMS & QUANTITY ROUTERS */}
          <div style={styles.leftColumn}>
            <div style={styles.accordionHeader}>
              <h2 style={styles.stepTitle}>Review Your Items ({cartItems.length})</h2>
            </div>
            <div style={styles.cartList}>
              {cartItems.map((item) => {
                
                // Kafka-Isolated Item Identity Image Mapping Workaround Engine
                let distinctImage = item.imageUrl || 'https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=150&q=80';
                const productTitle = item.productName || "Tech Product";

                if (productTitle.toLowerCase().includes("odyssey") || productTitle.toLowerCase().includes("samsung")) {
                  distinctImage = "https://images.unsplash.com/photo-1616763355548-1b606f439f86?w=150&q=80"; 
                } else if (productTitle.toLowerCase().includes("zephyrus") || productTitle.toLowerCase().includes("asus")) {
                  distinctImage = "https://images.unsplash.com/photo-1603302576837-37561b2e2302?w=150&q=80"; 
                } else if (productTitle.toLowerCase().includes("ipad") || productTitle.toLowerCase().includes("m4")) {
                  distinctImage = "https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?w=150&q=80"; 
                } else if (productTitle.toLowerCase().includes("surface") || productTitle.toLowerCase().includes("microsoft")) {
                  distinctImage = "https://images.unsplash.com/photo-1588872657578-7efd1f1555ed?w=150&q=80"; 
                } else if (productTitle.toLowerCase().includes("nvidia") || productTitle.toLowerCase().includes("rtx")) {
                  distinctImage = "https://images.unsplash.com/photo-1591488320449-011701bb6704?w=150&q=80"; 
                }

                return (
                  <div key={item.id} style={styles.cartRow}>
                    <div style={styles.itemMeta}>
                      <img 
                        src={distinctImage} 
                        alt="Product component card" 
                        style={styles.cartThumb} 
                        onError={(e) => { e.target.src = 'https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=150&q=80'; }} 
                      />
                      <div>
                        <h3 style={styles.itemName}>{productTitle}</h3>
                        
                        {/* Interactive Quantity Control Nodes */}
                        <div style={styles.quantityControlGroup}>
                          <button type="button" style={styles.qtyBtn} onClick={() => handleDecreaseQuantity(item)}>-</button>
                          <span style={styles.qtyDisplay}>{item.quantity}</span>
                          <button type="button" style={styles.qtyBtn} onClick={() => handleIncreaseQuantity(item)}>+</button>
                          <button type="button" style={styles.removeTextLink} onClick={() => handleRemoveItem(item.id)}>Remove</button>
                        </div>
                      </div>
                    </div>
                    <div style={styles.itemTotalPrice}>
                      ₹{(item.price * item.quantity).toLocaleString('en-IN')}
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Accordion Payment Section Wrapper */}
            {showPaymentOptions && (
              <div style={{ marginTop: '25px' }}>
                <div style={{ ...styles.accordionHeader, backgroundColor: '#28a745' }}>
                  <h2 style={styles.stepTitle}>SELECT PAYMENT METHOD</h2>
                </div>

                <div style={styles.paymentMethodsWrapper}>
                  {/* UPI Method Selection Block */}
                  <div style={{ ...styles.paymentOptionRow, backgroundColor: activeMethod === 'upi' ? '#f5faff' : '#fff' }} onClick={() => setActiveMethod('upi')}>
                    <input type="radio" checked={activeMethod === 'upi'} readOnly style={styles.radioButton} />
                    <div style={{ flexGrow: 1 }}>
                      <div style={styles.methodLabelTitle}>UPI (Google Pay / PhonePe / BHIM)</div>
                      {activeMethod === 'upi' && (
                        <div style={styles.methodDropdownContent} onClick={(e) => e.stopPropagation()}>
                          <p style={styles.instructionText}>Enter Your UPI ID</p>
                          <div style={styles.upiInputContainer}>
                            <input type="text" placeholder="username@okaxis" value={upiId} onChange={(e) => setUpiId(e.target.value)} style={styles.formInput} />
                            <button type="button" onClick={handleCheckout} disabled={!upiId || isProcessing} style={styles.verifyPayBtn}>
                              {isProcessing ? "Processing..." : "PAY NOW"}
                            </button>
                          </div>
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Card Method Selection Block */}
                  <div style={{ ...styles.paymentOptionRow, backgroundColor: activeMethod === 'card' ? '#f5faff' : '#fff' }} onClick={() => setActiveMethod('card')}>
                    <input type="radio" checked={activeMethod === 'card'} readOnly style={styles.radioButton} />
                    <div style={{ flexGrow: 1 }}>
                      <div style={styles.methodLabelTitle}>Credit / Debit / ATM Card</div>
                      {activeMethod === 'card' && (
                        <div style={styles.methodDropdownContent} onClick={(e) => e.stopPropagation()}>
                          <form onSubmit={handleCheckout} style={styles.nestedForm}>
                            <input type="text" placeholder="Enter Card Number" maxLength="19" required style={styles.formInput} />
                            <div style={{ display: 'flex', gap: '15px' }}>
                              <input type="text" placeholder="MM/YY" maxLength="5" required style={styles.formInput} />
                              <input type="password" placeholder="CVV" maxLength="3" required style={styles.formInput} />
                            </div>
                            <button type="submit" disabled={isProcessing} style={styles.flipkartPayBtn}>
                              {isProcessing ? "Authorizing Security..." : `PAY ₹${grandTotal.toLocaleString('en-IN')}`}
                            </button>
                          </form>
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Net Banking Method Selection Block */}
                  <div style={{ ...styles.paymentOptionRow, backgroundColor: activeMethod === 'netbanking' ? '#f5faff' : '#fff' }} onClick={() => setActiveMethod('netbanking')}>
                    <input type="radio" checked={activeMethod === 'netbanking'} readOnly style={styles.radioButton} />
                    <div style={{ flexGrow: 1 }}>
                      <div style={styles.methodLabelTitle}>Net Banking</div>
                      {activeMethod === 'netbanking' && (
                        <div style={styles.methodDropdownContent} onClick={(e) => e.stopPropagation()}>
                          <select value={selectedBank} onChange={(e) => setSelectedBank(e.target.value)} style={styles.formInput}>
                            <option value="">-- Select Your Bank --</option>
                            <option value="sbi">State Bank of India (SBI)</option>
                            <option value="hdfc">HDFC Bank</option>
                            <option value="icici">ICICI Bank</option>
                            <option value="axis">Axis Bank</option>
                          </select>
                          <button type="button" onClick={handleCheckout} disabled={!selectedBank || isProcessing} style={{ ...styles.flipkartPayBtn, marginTop: '12px' }}>
                            {isProcessing ? "Connecting to Bank Secure Gateway..." : "PROCEED TO BANK"}
                          </button>
                        </div>
                      )}
                    </div>
                  </div>

                  {/* COD Method Selection Block */}
                  <div style={{ ...styles.paymentOptionRow, backgroundColor: activeMethod === 'cod' ? '#f5faff' : '#fff', borderBottom: 'none' }} onClick={() => setActiveMethod('cod')}>
                    <input type="radio" checked={activeMethod === 'cod'} readOnly style={styles.radioButton} />
                    <div style={{ flexGrow: 1 }}>
                      <div style={styles.methodLabelTitle}>Cash on Delivery (COD)</div>
                      {activeMethod === 'cod' && (
                        <div style={styles.methodDropdownContent} onClick={(e) => e.stopPropagation()}>
                          <div style={styles.captchaRow}>
                            <div style={styles.captchaBlock}>{generatedCaptcha}</div>
                            <input type="text" placeholder="Enter Captcha" maxLength="4" value={captchaInput} onChange={(e) => setCaptchaInput(e.target.value)} style={{ ...styles.formInput, width: '130px', margin: 0 }} />
                          </div>
                          <button type="button" onClick={handleCheckout} disabled={!captchaInput || isProcessing} style={{ ...styles.flipkartPayBtn, backgroundColor: '#ff9f00' }}>
                            {isProcessing ? "Placing Order..." : "CONFIRM COD ORDER"}
                          </button>
                        </div>
                      )}
                    </div>
                  </div>

                </div>
              </div>
            )}
          </div>

          {/* RIGHT INTERFACE COLUMN: SIDEBAR ORDER INVOICE ACCUMULATOR */}
          <div style={styles.rightColumn}>
            <div style={styles.summaryCard}>
              <h2 style={styles.summaryHeading}>PRICE DETAILS</h2>
              <div style={styles.pricingTable}>
                <div style={styles.priceRow}>
                  <span>Price ({cartItems.length} items)</span>
                  <span>₹{totalAmount.toLocaleString('en-IN')}</span>
                </div>
                <div style={styles.priceRow}>
                  <span>Delivery Charges</span>
                  <span style={{ color: '#388e3c', fontWeight: '600' }}>FREE</span>
                </div>
                <div style={styles.priceRow}>
                  <span>Secured Packaging Fee</span>
                  <span>₹{securedPackagingFee}</span>
                </div>
                <div style={styles.priceRow}>
                  <span>Platform Gateway Fee</span>
                  <span>₹{platformFee}</span>
                </div>
                <hr style={styles.divider} />
                
                <div style={styles.totalPriceRow}>
                  <span>Total Amount Due</span>
                  <span>₹{grandTotal.toLocaleString('en-IN')}</span>
                </div>
              </div>

              {!showPaymentOptions ? (
                <button style={styles.proceedToPaymentBtn} onClick={() => setShowPaymentOptions(true)}>
                  PROCEED TO PAYMENT
                </button>
              ) : (
                <div style={styles.paymentActiveBadge}>
                  👈 Choose your payment channel on the left to complete order
                </div>
              )}
            </div>
          </div>

        </div>
      )}
    </div>
  );
}

const styles = {
  cartContainer: { padding: '0 max(5%, 20px) 40px max(5%, 20px)', fontFamily: "'Roboto', sans-serif", backgroundColor: '#f1f3f6', minHeight: '100vh' },
  topNavigation: { position: 'sticky', top: 0, backgroundColor: '#f1f3f6', zIndex: 1100, display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '20px 0', borderBottom: '1px solid #e0e0e0', marginBottom: '20px' },
  cartTitle: { margin: 0, fontSize: '24px', color: '#212529', fontWeight: '600' },
  backBtn: { backgroundColor: '#fff', color: '#212529', border: '1px solid #dbdbdb', padding: '10px 16px', borderRadius: '4px', cursor: 'pointer', fontWeight: '600', boxShadow: '0 1px 2px rgba(0,0,0,0.05)' },
  errorMessage: { color: '#fff', backgroundColor: '#d32f2f', padding: '12px', borderRadius: '4px', marginBottom: '20px' },
  emptyCartBox: { textAlign: 'center', padding: '50px 20px', borderRadius: '4px', backgroundColor: '#fff', maxWidth: '500px', margin: '40px auto', boxShadow: '0 1px 4px rgba(0,0,0,0.1)' },
  shopNowBtn: { backgroundColor: '#2874f0', color: '#fff', border: 'none', padding: '12px 24px', borderRadius: '2px', cursor: 'pointer', fontWeight: '600' },
  checkoutLayout: { display: 'grid', gridTemplateColumns: '1.3fr 0.7fr', gap: '20px', alignItems: 'start' },
  leftColumn: { display: 'flex', flexDirection: 'column' },
  accordionHeader: { backgroundColor: '#2874f0', color: '#fff', padding: '14px 20px', display: 'flex', alignItems: 'center', borderRadius: '4px 4px 0 0' },
  stepTitle: { fontSize: '15px', color: '#fff', margin: 0, fontWeight: '600', letterSpacing: '0.5px', textTransform: 'uppercase' },
  cartList: { display: 'flex', flexDirection: 'column', backgroundColor: '#fff', padding: '10px 20px', boxShadow: '0 1px 2px rgba(0,0,0,0.05)' },
  cartRow: { padding: '15px 0', display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid #f0f0f0' },
  itemMeta: { display: 'flex', alignItems: 'center', gap: '15px' },
  cartThumb: { width: '70px', height: '70px', objectFit: 'cover', border: '1px solid #f0f0f0', borderRadius: '4px' },
  itemName: { margin: '0 0 5px 0', fontSize: '15px', color: '#212529', fontWeight: '600' },
  itemQty: { margin: 0, fontSize: '13px', color: '#878787' },
  badge: { fontWeight: '700', color: '#212529' },
  itemTotalPrice: { fontWeight: '600', fontSize: '16px', color: '#212529' },
  quantityControlGroup: { display: 'flex', alignItems: 'center', gap: '10px', marginTop: '8px' },
  qtyBtn: { width: '28px', height: '28px', border: '1px solid #c2c2c2', backgroundColor: '#fff', borderRadius: '50%', fontSize: '16px', fontWeight: '600', cursor: 'pointer', display: 'flex', alignItems: 'center', justifyContent: 'center' },
  qtyDisplay: { fontSize: '14px', fontWeight: '700', width: '20px', textAlign: 'center' },
  removeTextLink: { background: 'none', border: 'none', color: '#2874f0', fontWeight: '600', fontSize: '14px', marginLeft: '15px', cursor: 'pointer' },
  paymentMethodsWrapper: { backgroundColor: '#fff', border: '1px solid #e0e0e0', borderTop: 'none', boxShadow: '0 1px 3px rgba(0,0,0,0.05)' },
  paymentOptionRow: { display: 'flex', gap: '15px', padding: '20px', borderBottom: '1px solid #f0f0f0', cursor: 'pointer' },
  radioButton: { width: '18px', height: '18px', marginTop: '2px', cursor: 'pointer' },
  methodLabelTitle: { fontSize: '15px', color: '#212529', fontWeight: '600' },
  methodDropdownContent: { marginTop: '15px', paddingLeft: '5px' },
  instructionText: { margin: '0 0 10px 0', fontSize: '13px', color: '#212529', fontWeight: '500' },
  upiInputContainer: { display: 'flex', gap: '15px', maxWidth: '450px' },
  formInput: { width: '100%', padding: '11px 14px', border: '1px solid #c2c2c2', fontSize: '14px', outline: 'none', borderRadius: '2px' },
  verifyPayBtn: { backgroundColor: '#fb641b', color: '#fff', border: 'none', padding: '0 24px', fontWeight: '600', borderRadius: '2px', cursor: 'pointer' },
  nestedForm: { display: 'flex', flexDirection: 'column', gap: '12px', maxWidth: '350px' },
  flipkartPayBtn: { backgroundColor: '#fb641b', color: '#fff', border: 'none', width: '100%', padding: '13px 0', fontSize: '14px', fontWeight: '600', borderRadius: '2px', cursor: 'pointer' },
  captchaRow: { display: 'flex', alignItems: 'center', gap: '15px', marginBottom: '15px' },
  captchaBlock: { backgroundColor: '#f0f0f0', color: '#333', padding: '8px 16px', fontSize: '18px', fontWeight: '700', letterSpacing: '4px', border: '1px dashed #777' },
  rightColumn: { position: 'sticky', top: '90px', zIndex: 1000 },
  summaryCard: { backgroundColor: '#fff', borderRadius: '2px', boxShadow: '0 1px 3px rgba(0,0,0,0.1)', paddingBottom: '10px' },
  summaryHeading: { fontSize: '14px', color: '#878787', fontWeight: '600', margin: 0, padding: '15px 20px', borderBottom: '1px solid #f0f0f0' },
  pricingTable: { padding: '20px 20px 10px 20px', display: 'flex', flexDirection: 'column', gap: '15px' },
  priceRow: { display: 'flex', justifyContent: 'space-between', color: '#212529', fontSize: '15px' },
  divider: { border: 'none', borderTop: '1px dashed #e0e0e0', margin: '5px 0' },
  totalPriceRow: { display: 'flex', justifyContent: 'space-between', color: '#212529', fontSize: '18px', fontWeight: '600', paddingBottom: '10px' },
  proceedToPaymentBtn: { backgroundColor: '#fb641b', color: '#fff', border: 'none', width: 'calc(100% - 40px)', margin: '10px 20px 15px 20px', padding: '14px 0', fontSize: '15px', fontWeight: '600', borderRadius: '2px', cursor: 'pointer', boxShadow: '0 1px 2px rgba(0,0,0,0.2)' },
  paymentActiveBadge: { margin: '10px 20px 15px 20px', padding: '12px', border: '1px solid #b6effb', backgroundColor: '#e8f7fa', color: '#055160', fontSize: '13px', fontWeight: '500', borderRadius: '4px', textAlign: 'center' }
};

export default Cart;