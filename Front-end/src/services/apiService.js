// API service for handling all backend communications

const API_BASE_URL = "http://localhost:8080/api";

const apiService = {
  // thêm export của API_BASE_URL để các module khác có thể dùng trực tiếp
  API_BASE_URL,
  // thiết lập các phương thức api ở đây

  //profile controller
  profile_user_by_id: (userId) => `${API_BASE_URL}/profiles/${userId}`,
  /**
   * Cập nhật thông tin profile của người dùng (PUT).
   * @param {number} userId - ID của người dùng cần cập nhật.
   */
  update_profile_by_id: (userId) => `${API_BASE_URL}/profiles/${userId}`,

  //api complaint controller (feedback)
  /**
   * @param {number} complaintId - ID của khiếu nại cần giải quyết.
   */
  resolve_complaint: (complaintId) =>
    `${API_BASE_URL}/complaints/${complaintId}/resolve`,

  /**
   * Lấy danh sách khiếu nại, có thể lọc theo trạng thái.
   * @param {object} params - Đối tượng chứa tham số lọc. Ví dụ: { status: 'PENDING' }
   */
  get_all_complaints: (params = {}) => {
    // Tái sử dụng logic URLSearchParams đã dùng trước đây
    const query = new URLSearchParams(params).toString();
    const queryString = query ? `?${query}` : "";

    return `${API_BASE_URL}/complaints${queryString}`;
  },

  create_new_complaint: `${API_BASE_URL}/complaints`,

  //review controller
  create_new_review: `${API_BASE_URL}/reviews`,
  get_reviews_for_listing: (listingId) =>
    `${API_BASE_URL}/reviews/listing/${listingId}`,

  //favorite controller
  /**
   * Thêm một bài đăng vào danh sách yêu thích (POST).
   * @param {number} listingId - ID của bài đăng cần thêm.
   */
  add_favorite: (listingId) =>
    `${API_BASE_URL}/favorites/listings/${listingId}`,

  /**
   * Xóa một bài đăng khỏi danh sách yêu thích (DELETE).
   * @param {number} listingId - ID của bài đăng cần xóa.
   */
  remove_favorite: (listingId) =>
    `${API_BASE_URL}/favorites/listings/${listingId}`,

  /**
   * Lấy danh sách tất cả các bài đăng yêu thích của người dùng (GET).
   */
  get_my_favorites: `${API_BASE_URL}/favorites/listings`,

  // User authentication endpoints
  login: async (credentials) => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(credentials),
    });

    if (!response.ok) {
      const errorData = await response.text();
      throw new Error(errorData || "Tên đăng nhập hoặc mật khẩu không đúng");
    }

    const data = await response.json();

    // Debug: Log response từ backend
    console.log("✅ Login API Response:", data);

    // Lưu token vào localStorage
    if (data.token) {
      localStorage.setItem("authToken", data.token);
      localStorage.setItem("tokenType", data.tokenType || "Bearer ");
      console.log("Token saved:", data.token);
    }

    // Lưu user data (data ở root level từ API)
    const userData = {
      id: data.userID,
      userID: data.userID,
      username: data.username,
      email: data.email,
      roles: [data.role && data.role.toUpperCase()], // chuyển role thành mảng, ví dụ ["ADMIN"]
    };

    localStorage.setItem("userData", JSON.stringify(userData));
    localStorage.setItem("user", JSON.stringify(userData));
    localStorage.setItem("isLoggedIn", "true");
    localStorage.setItem("userID", data.userID);
    localStorage.setItem("username", data.username);

    // ===== LẤY ATTRIBUTE ROLE TỪ API =====
    const roleAttribute = data.role; // getAttribute từ response

    // Chuyển đổi role string sang role ID
    let roleId = 3; // Default: Member
    if (roleAttribute === "Admin") {
      roleId = 1;
    } else if (roleAttribute === "Moderator") {
      roleId = 2;
    } else if (roleAttribute === "Member") {
      roleId = 3;
    }

    // Lưu role vào localStorage
    localStorage.setItem("role", roleId.toString());
    localStorage.setItem("roleId", roleId.toString());
    localStorage.setItem("roleName", roleAttribute);

    console.log("📌 Role getAttribute:", roleAttribute, "→ ID:", roleId);

    // ===== THÔNG BÁO THEO ROLE =====
    if (roleAttribute === "Admin") {
      alert(
        `✅ Đã đăng nhập là ADMIN\n\nUser: ${data.username}\nEmail: ${data.email}`
      );
    }

    return data;
  },

  register: async (userData) => {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(userData),
    });

    if (!response.ok) {
      const errorData = await response.text();
      throw new Error(errorData || "Đăng ký thất bại");
    }

    // Debug: Log response từ backend
    console.log("Register response status:", response.status);

    // Parse response - backend có thể trả JSON hoặc text
    const contentType = response.headers.get("content-type");
    let data;

    if (contentType && contentType.includes("application/json")) {
      data = await response.json();
      console.log("Register response (JSON):", data);
    } else {
      data = await response.text();
      console.log("Register response (Text):", data);
      // Nếu backend trả text, wrap thành object
      data = { success: true, message: data };
    }

    return data;
  },

  // Logout
  logout: async () => {
    try {
      const token = localStorage.getItem("authToken");

      // Gọi API logout nếu có token
      if (token) {
        const response = await fetch(`${API_BASE_URL}/auth/logout`, {
          method: "POST",
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });

        console.log("Logout response:", response.status);

        // Không cần kiểm tra response.ok vì dù API fail vẫn phải clear localStorage
      }
    } catch (error) {
      console.error("Logout API error:", error);
      // Tiếp tục clear localStorage dù API lỗi
    } finally {
      // Luôn luôn xóa localStorage
      localStorage.removeItem("authToken");
      localStorage.removeItem("userData");
      localStorage.removeItem("user");
      localStorage.removeItem("isLoggedIn");
      localStorage.removeItem("userID");

      console.log("Local storage cleared");
    }
  },

  // Utility methods
  setAuthToken: (token) => {
    localStorage.setItem("authToken", token);
  },

  getAuthToken: () => {
    return localStorage.getItem("authToken");
  },

  clearAuthToken: () => {
    localStorage.removeItem("authToken");
    localStorage.removeItem("userData");
    localStorage.removeItem("user");
    localStorage.removeItem("isLoggedIn");
    localStorage.removeItem("userID");
  },

  isAuthenticated: () => {
    const token = localStorage.getItem("authToken");
    const userData = localStorage.getItem("userData");
    return !!(token && userData);
  },

  getTokenPayload: function () {
    const token = this.getAuthToken();
    if (!token) return null;
    try {
      const payload = JSON.parse(atob(token.split(".")[1]));
      return payload;
    } catch {
      return null;
    }
  },

  isTokenExpired: function () {
    const payload = this.getTokenPayload();
    if (!payload || !payload.exp) return true;
    // exp là số giây, Date.now() là ms
    return Date.now() / 1000 > payload.exp;
  },

  // Social login (Google OAuth || Facebook OAuth)
  socialLogin: async function ({ provider, accessToken }) {
    const res = await fetch(`${API_BASE_URL}/auth/social`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "*/*",
      },
      body: JSON.stringify({ provider, accessToken }),
    });
    if (!res.ok) throw new Error("Social login failed");
    return await res.json();
  },

  // apiService.js
  googleCodeLogin: async (code) => {
    const res = await fetch(`${API_BASE_URL}/auth/google/code`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ code }),
    });
    if (!res.ok) throw new Error("Google login failed");
    return await res.json();
  },

  ///////////////////////////////////////////////////////////////////////////////
  // API TẠO BÀI ĐĂNG SẢN PHẨM
  // Lấy danh sách brand (public, không cần token)
  getBrands: async function () {
    const res = await fetch(`${API_BASE_URL}/brands`, {
      headers: {
        Accept: "*/*",
      },
    });
    if (!res.ok) throw new Error("Không thể tải danh sách hãng xe");
    return await res.json();
  },

  // Lấy danh sách category (public, không cần token)
  getCategories: async function () {
    const res = await fetch(`${API_BASE_URL}/categories`, {
      headers: {
        Accept: "*/*",
      },
    });
    if (!res.ok) throw new Error("Không thể tải danh sách danh mục xe");
    return await res.json();
  },

  // API VỀ BÀI ĐĂNG SẢN PHẨM
  // Tạo bài đăng sản phẩm mới với hình ảnh
  createProductPost: async function (listingObj, images) {
    const token = this.getAuthToken();
    const form = new FormData();

    // Đưa toàn bộ thông tin bài đăng vào field 'listing' dưới dạng JSON string
    form.append("listing", JSON.stringify(listingObj));

    // Đưa từng ảnh vào field 'images'
    if (images && Array.isArray(images)) {
      images.forEach((file) => {
        form.append("images", file);
      });
    }

    const res = await fetch(`${API_BASE_URL}/listings`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        // KHÔNG đặt Content-Type, để browser tự set boundary
      },
      body: form,
    });

    if (!res.ok) throw new Error("Không thể tạo bài đăng sản phẩm");
    return await res.json();
  },

  // Tìm kiếm bài đăng sản phẩm (public, không cần token)
  searchProductPosts: async function (searchParams) {
    const query = new URLSearchParams(searchParams).toString();
    const res = await fetch(`${API_BASE_URL}/listings/search?${query}`, {
      headers: {
        Accept: "*/*",
      },
    });
    if (!res.ok) throw new Error("Không thể tìm kiếm bài đăng sản phẩm");
    return await res.json();
  },

  // Lấy chi tiết bài đăng sản phẩm theo ID (public, không cần token)
  getProductPostById: async function (listingId) {
    const res = await fetch(`${API_BASE_URL}/listings/${listingId}`, {
      headers: {
        Accept: "*/*",
      },
    });
    if (!res.ok) throw new Error("Không thể tải chi tiết bài đăng sản phẩm");
    return await res.json();
  },

  ///////////////////////////////////////////////////////////////////////////////

  // API ĐẶT ĐƠN HÀNG
  // Tạo đơn hàng mới
  createOrder: async function (orderObj) {
    const token = this.getAuthToken();
    const res = await fetch(`${API_BASE_URL}/orders`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
        Accept: "*/*",
      },
      body: JSON.stringify(orderObj),
    });
    if (!res.ok) throw new Error("Không thể tạo đơn hàng");
    return await res.json();
  },
  //////////////////////////////////////////////////////////////////////////////////

  // API GIAO DỊCH VÀ THANH TOÁN
  // Lấy danh sách giao dịch của user hiện tại
  getTransactions: async function (navigate) {
    const token = this.getAuthToken();
    const res = await fetch(`${API_BASE_URL}/transactions`, {
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: "*/*",
      },
    });
    if (res.status === 401 || res.status === 403) {
      // Token hết hạn hoặc không hợp lệ, chuyển về login
      if (navigate) navigate("/login");
      throw new Error("Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.");
    }
    if (!res.ok) throw new Error("Không thể tải giao dịch");
    return await res.json();
  },

  // Tạo thanh toán cho một giao dịch
  createPayment: async function ({ transactionId, amount }) {
    const token = this.getAuthToken();
    const body = {
      transactionId,
      amount,
      paymentMethod: "VNPAY",
      paymentProvider: "VNPAY",
    };
    const res = await fetch(`${API_BASE_URL}/payments`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        "Content-Type": "application/json",
        Accept: "*/*",
      },
      body: JSON.stringify(body),
    });
    if (!res.ok) throw new Error("Không tạo được thanh toán");
    return await res.json();
  },
  ////////////////////////////////////////////////////////////////////////////////
  // API CHO ADMIN VÀ MODERATOR
  // Lấy danh sách tất cả người dùng (chỉ dành cho Admin)
  getAllUsers: async function () {
    const token = this.getAuthToken();
    const res = await fetch(`${API_BASE_URL}/users`, {
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: "*/*",
      },
    });
    if (!res.ok) throw new Error("Không thể tải danh sách người dùng");
    return await res.json();
  },

  // Khóa hoặc mở khóa người dùng (chỉ dành cho Admin)
  disableUser: async function (userId) {
    const token = this.getAuthToken();
    const res = await fetch(`${API_BASE_URL}/users/${userId}/disable`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: "*/*",
      },
    });
    if (!res.ok) throw new Error("Không thể khóa người dùng");
    return await res.json();
  },

  enableUser: async function (userId) {
    const token = this.getAuthToken();
    const res = await fetch(`${API_BASE_URL}/users/${userId}/enable`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: "*/*",
      },
    });
    if (!res.ok) throw new Error("Không thể mở khóa người dùng");
    return await res.json();
  },

  // Lấy danh sach tất cả bài đăng (chỉ dành cho Moderator và Admin)
  getAllListings: async function () {
    const token = this.getAuthToken();
    const res = await fetch(`${API_BASE_URL}/listings`, {
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: "*/*",
      },
    });
    if (!res.ok) throw new Error("Không thể tải danh sách bài đăng");
    return await res.json();
  },

  // Lấy danh sách tất cả bài đăng status PENDING (chỉ dành cho Moderator và Admin)
  getListingsPending: async function () {
    const token = this.getAuthToken();
    const url = new URL(`${API_BASE_URL}/listings`);
    url.searchParams.append("status", "PENDING");
    const res = await fetch(url, {
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: "*/*",
      },
    });
    if (!res.ok) throw new Error("Không thể tải danh sách bài đăng");
    return await res.json();
  },

  // Phê duyệt bài đăng (chỉ dành cho Moderator và Admin)
  approveListing: async function (listingId) {
    const token = this.getAuthToken();
    const res = await fetch(`${API_BASE_URL}/listings/${listingId}/approve`, {
      method: "POST",
      headers: {
        Authorization: `Bearer ${token}`,
        Accept: "*/*",
      },
    });
    if (!res.ok) throw new Error("Không thể phê duyệt bài đăng");
    return await res.json();
  },

  // Từ chối bài đăng (chỉ dành cho Moderator và Admin)
  rejectListing: async function (id, reason) {
    const res = await fetch(
      `${API_BASE_URL}/listings/${id}/reject?reason=${encodeURIComponent(
        reason
      )}`,
      {
        method: "POST",
        headers: {
          Authorization: `Bearer ${this.getAuthToken()}`,
          Accept: "*/*",
        },
      }
    );
    if (!res.ok) throw new Error("Từ chối bài đăng thất bại");
    return await res.json();
  },
};

export default apiService;
