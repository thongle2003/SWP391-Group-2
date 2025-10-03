import 'bootstrap/dist/css/bootstrap.min.css'
import React from 'react'
import { useNavigate } from 'react-router-dom'
import './Home.css'

function Home() {
  const navigate = useNavigate()

  return (
    <div className="home-page">
      {/* Header/Navbar */}
      <header className="home-header">
        <div className="container-fluid">
          <div className="row align-items-center py-3">
            <div className="col-md-3">
              <h1 className="site-logo">EVMARKETPLAY.VN</h1>
            </div>
            <div className="col-md-6">
              <nav className="main-nav">
                <a href="#" className="nav-link">Trang chủ</a>
                <a href="#" className="nav-link">Sản phẩm</a>
                <a href="#" className="nav-link">Giới thiệu</a>
                <a href="#" className="nav-link">Liên hệ</a>
              </nav>
            </div>
            <div className="col-md-3 text-end">
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

      {/* Hero Section */}
      <section className="hero-section">
        <div className="container">
          <div className="row align-items-center justify-content-center">
            <div className="col-md-8 text-center">
              <h1 className="hero-title">EVMARKETPLAY.VN</h1>
              <p className="hero-subtitle">Nền tảng mua bán xe điện hàng đầu Việt Nam</p>
              <p className="hero-description">
                Khám phá và sở hữu những mẫu xe điện hiện đại, thân thiện với môi trường. 
                Tương lai xanh bắt đầu từ đây.
              </p>
              <div className="hero-buttons">
                <button className="btn-primary-large" onClick={() => navigate('/register')}>
                  Bắt đầu ngay
                </button>
                <button className="btn-secondary-large">
                  Tìm hiểu thêm
                </button>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="features-section">
        <div className="container">
          <h2 className="section-title">Tại sao chọn chúng tôi?</h2>
          <div className="row mt-5">
            <div className="col-md-4">
              <div className="feature-card">
                <div className="feature-icon">🚗</div>
                <h3>Đa dạng xe điện</h3>
                <p>Hàng trăm mẫu xe điện từ các thương hiệu hàng đầu thế giới</p>
              </div>
            </div>
            <div className="col-md-4">
              <div className="feature-card">
                <div className="feature-icon">⚡</div>
                <h3>Giao dịch nhanh chóng</h3>
                <p>Quy trình mua bán đơn giản, minh bạch và an toàn</p>
              </div>
            </div>
            <div className="col-md-4">
              <div className="feature-card">
                <div className="feature-icon">🌱</div>
                <h3>Thân thiện môi trường</h3>
                <p>Đóng góp vào tương lai xanh, bảo vệ môi trường</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* CTA Section */}
      <section className="cta-section">
        <div className="container text-center">
          <h2 className="cta-title">Sẵn sàng trải nghiệm?</h2>
          <p className="cta-description">Đăng ký ngay hôm nay để khám phá thế giới xe điện</p>
          <button className="btn-cta" onClick={() => navigate('/register')}>
            Đăng ký miễn phí
          </button>
        </div>
      </section>

      {/* Footer */}
      <footer className="home-footer">
        <div className="container">
          <div className="row">
            <div className="col-md-4">
              <h3 className="footer-logo">EVMARKETPLAY.VN</h3>
              <p>Nền tảng mua bán xe điện hàng đầu Việt Nam</p>
            </div>
            <div className="col-md-4">
              <h4>Liên kết</h4>
              <ul className="footer-links">
                <li><a href="#">Về chúng tôi</a></li>
                <li><a href="#">Sản phẩm</a></li>
                <li><a href="#">Tin tức</a></li>
                <li><a href="#">Liên hệ</a></li>
              </ul>
            </div>
            <div className="col-md-4">
              <h4>Liên hệ</h4>
              <p>Email: contact@evmarketplay.vn</p>
              <p>Hotline: 1900 xxxx</p>
            </div>
          </div>
          <div className="row mt-4">
            <div className="col-12 text-center">
              <p className="copyright">© 2024 EVMARKETPLAY.VN. All rights reserved.</p>
            </div>
          </div>
        </div>
      </footer>
    </div>
  )
}

export default Home
