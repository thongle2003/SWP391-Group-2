import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import './Header.css'

function Header() {
  const navigate = useNavigate()
  const [searchQuery, setSearchQuery] = useState('')

  const handleSearch = (e) => {
    e.preventDefault()
    console.log('Tìm kiếm:', searchQuery)
  }

  return (
    <header className="home-header">
      <div className="container-fluid">
        <div className="header-content">
          {/* Logo/Site Name */}
          <h1 className="site-logo" onClick={() => navigate('/')}>EVMARKETPLAY.VN</h1>

          {/* Menu Navigation */}
          <nav className="main-nav">
            <a href="#" className="nav-link">Mua sản phẩm</a>
            <a href="#" className="nav-link">Bán sản phẩm</a>
            <a href="#" className="nav-link">Liên hệ</a>
            <a href="#" className="nav-link">Về chúng tôi</a>
          </nav>

          {/* Search Bar */}
          <form className="search-form" onSubmit={handleSearch}>
            <div className="search-container">
              <svg className="search-icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="11" cy="11" r="8"></circle>
                <path d="m21 21-4.35-4.35"></path>
              </svg>
              <input
                type="text"
                className="search-input"
                placeholder="Tìm kiếm"
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
          </form>

          {/* Auth Buttons - Right */}
          <div className="auth-buttons">
            <button className="btn-login" onClick={() => navigate('/login')}>
              Đăng nhập
            </button>
            <button className="btn-register" onClick={() => navigate('/register')}>
              Đăng ký
            </button>
          </div>
        </div>
      </div>
    </header>
  )
}

export default Header

