// API service for handling all backend communications
const API_BASE_URL = 'http://localhost:8080/api';

const apiService = {
  // User authentication endpoints
  login: async (credentials) => {
    const response = await fetch(`${API_BASE_URL}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(credentials),
    });

    if (!response.ok) {
      const errorData = await response.text();
      throw new Error(errorData || 'Tên đăng nhập hoặc mật khẩu không đúng');
    }

    const data = await response.json();
    
    // Debug: Log response từ backend
    console.log('Login response:', data);
    
    // Lưu token vào localStorage
    if (data.token) {
      localStorage.setItem('authToken', data.token);
      console.log('Token saved:', data.token);
    }
    
    // Xác định user data (có thể ở data.user hoặc trực tiếp trong data)
    let userData = data.user || data;
    
    // Nếu có username/email thì coi đó là user data
    if (userData && (userData.username || userData.email || userData.name)) {
      console.log('User data:', userData);
      localStorage.setItem('userData', JSON.stringify(userData));
      localStorage.setItem('user', JSON.stringify(userData));
      localStorage.setItem('isLoggedIn', 'true');
      
      // Lưu userID riêng nếu có
      if (userData.id || userData.userID) {
        localStorage.setItem('userID', userData.id || userData.userID);
      }
    } else {
      console.warn('No user data found in response');
    }

    return data;
  },

  register: async (userData) => {
    const response = await fetch(`${API_BASE_URL}/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData),
    });

    if (!response.ok) {
      const errorData = await response.text();
      throw new Error(errorData || 'Đăng ký thất bại');
    }

    // Debug: Log response từ backend
    console.log('Register response status:', response.status);
    
    // Parse response - backend có thể trả JSON hoặc text
    const contentType = response.headers.get('content-type');
    let data;
    
    if (contentType && contentType.includes('application/json')) {
      data = await response.json();
      console.log('Register response (JSON):', data);
    } else {
      data = await response.text();
      console.log('Register response (Text):', data);
      // Nếu backend trả text, wrap thành object
      data = { success: true, message: data };
    }

    return data;
  },

  // User profile endpoints
  getUserProfile: async (userId, token) => {
    const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    });

    if (!response.ok) {
      throw new Error('Failed to fetch user profile');
    }

    return response.json();
  },

  updateUserProfile: async (userId, userData, token) => {
    const response = await fetch(`${API_BASE_URL}/users/${userId}`, {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(userData),
    });

    if (!response.ok) {
      throw new Error('Failed to update user profile');
    }

    return response.json();
  },

  // Logout
  logout: async () => {
    try {
      const token = localStorage.getItem('authToken');
      
      // Gọi API logout nếu có token
      if (token) {
        const response = await fetch(`${API_BASE_URL}/auth/logout`, {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
          },
        });
        
        console.log('Logout response:', response.status);
        
        // Không cần kiểm tra response.ok vì dù API fail vẫn phải clear localStorage
      }
    } catch (error) {
      console.error('Logout API error:', error);
      // Tiếp tục clear localStorage dù API lỗi
    } finally {
      // Luôn luôn xóa localStorage
      localStorage.removeItem('authToken');
      localStorage.removeItem('userData');
      localStorage.removeItem('user');
      localStorage.removeItem('isLoggedIn');
      localStorage.removeItem('userID');
      
      console.log('Local storage cleared');
    }
  },

  // Utility methods
  setAuthToken: (token) => {
    localStorage.setItem('authToken', token);
  },

  getAuthToken: () => {
    return localStorage.getItem('authToken');
  },

  clearAuthToken: () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('userData');
    localStorage.removeItem('user');
    localStorage.removeItem('isLoggedIn');
    localStorage.removeItem('userID');
  },

  isAuthenticated: () => {
    const token = localStorage.getItem('authToken');
    const userData = localStorage.getItem('userData');
    return !!(token && userData);
  }
};

export default apiService;