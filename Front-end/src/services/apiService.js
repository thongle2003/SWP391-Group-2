// API service for handling all backend communications
const API_BASE_URL = 'http://localhost:8081/api'; //Cái này thay đổi tùy theo port của be 80 hay 81

const apiService = {
  // thêm export của API_BASE_URL để các module khác có thể dùng trực tiếp
  API_BASE_URL,
  // thiết lập các phương thức api ở đây


  //api để quản lý bài đăng sản phẩm
  get_detail_product_post: (productId) => `${API_BASE_URL}/listings/${productId}`,
  update_product_post: (productId) => `${API_BASE_URL}/listings/${productId}`,
  delete_product_post: (productId) => `${API_BASE_URL}/listings/${productId}`,
    /**
   * Lấy danh sách bài đăng với các tham số tùy chọn để lọc, phân trang và sắp xếp.
   * Khớp chính xác với endpoint GET /api/listings trên backend.
   * @param {object} params - Một đối tượng chứa các tham số truy vấn.
   * Ví dụ: { status: 'PENDING', page: 0, size: 20, sortBy: 'price' }
   */
  getAll_products_post: (params = {}) => {
    // URLSearchParams sẽ tự động bỏ qua các giá trị undefined hoặc null
    // và mã hóa (encode) các giá trị một cách chính xác.
    const query = new URLSearchParams(params).toString();
    
    // Nếu query không rỗng, thêm '?' vào trước, ngược lại trả về chuỗi rỗng.
    const queryString = query ? `?${query}` : '';

    return `${API_BASE_URL}/listings${queryString}`;
  },

  create_product_post:`${API_BASE_URL}/listings`,

  /**
   * Từ chối một bài đăng.
   * @param {number} productId - ID của bài đăng, sẽ được chèn vào đường dẫn.
   * @param {string} reason - Lý do từ chối, sẽ được thêm làm query parameter.
   */
  reject_product_post: (productId, reason) => {
    // 1. Tạo URL cơ sở với path variable
    const url = `${API_BASE_URL}/listings/${productId}/reject`;

    // 2. Dùng URLSearchParams để thêm 'reason' một cách an toàn
    // (tự động mã hóa ký tự đặc biệt như dấu cách, tiếng Việt)
    const params = new URLSearchParams();
    if (reason) { // Chỉ thêm 'reason' nếu nó tồn tại
      params.append('reason', reason);
    }

    const queryString = params.toString();
    
    // 3. Kết hợp lại
    return `${url}${queryString ? `?${queryString}` : ''}`;
  },

  approve_product_post: (productId) => `${API_BASE_URL}/listings/${productId}/approve`,
  
  /**
   * (SỬA LẠI HÀM NÀY)
   * Lấy danh sách các bài đăng đang chờ phê duyệt (pending).
   * Hỗ trợ phân trang và sắp xếp.
   * @param {object} params - Đối tượng chứa tham số phân trang/sắp xếp.
   * Ví dụ: { page: 1, size: 5, sortBy: 'price' }
   */
  get_pending_products_post: (params = {}) => {
    // Tái sử dụng logic của URLSearchParams
    const query = new URLSearchParams(params).toString();
    const queryString = query ? `?${query}` : '';

    return `${API_BASE_URL}/listings/pending${queryString}`;
  },
  


  //api quản lý thương hiệu
  create_new_brand:`${API_BASE_URL}/brands`,
  get_all_brands: `${API_BASE_URL}/brands`,
  //api quản lý danh mục
  create_new_category:`${API_BASE_URL}/categories`,
  get_all_categories: `${API_BASE_URL}/categories`, // <-- Thêm dòng này


  //api để quản lý đơn hàng và thanh toán
  create_new_payment:`${API_BASE_URL}/payments`,
  get_all_oders:`${API_BASE_URL}/orders`,
  create_new_oder:`${API_BASE_URL}/orders`,
  get_history_payment: (transactionId) => `${API_BASE_URL}/transactions/${transactionId}payments`,
  get_detail_oder: (orderId) => `${API_BASE_URL}/orders/${orderId}`,


  //api user controller
  get_user_by_id: (userId) => `${API_BASE_URL}/users/${userId}`,
  update_user_by_id: (userId) => `${API_BASE_URL}/users/${userId}`,
  delete_user_by_id: (userId) => `${API_BASE_URL}/users/${userId}`,
  get_all_users: `${API_BASE_URL}/users`,
  create_new_user: `${API_BASE_URL}/users`,
  disable_user_by_id: (userId) => `${API_BASE_URL}/users/${userId}/disable`,
  approve_user_by_id: (userId) => `${API_BASE_URL}/users/${userId}/approve`,

  



};

export default apiService;