import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import defaultImage from "../assets/VinFast_VF5_Plus.jpg";
import Header from "../components/Header";
import apiService from "../services/apiService";
import "./ProductDetail.css";

function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [item, setItem] = useState(null);
  const [mainImage, setMainImage] = useState(defaultImage);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [orderLoading, setOrderLoading] = useState(false);
  const [orderError, setOrderError] = useState("");
  const [orderSuccess, setOrderSuccess] = useState("");

  // Hiển thị thông báo trên đầu trang
  const [topMessage, setTopMessage] = useState("");

  const fetchDetail = async () => {
    setLoading(true);
    setError(null);
    try {
      const payload = await apiService.getProductPostById(id);
      const images = Array.isArray(payload.images) ? payload.images : [];
      const primary =
        payload.primaryImageUrl || images.find((i) => i.isPrimary)?.url;
      const display = primary || images[0]?.url || defaultImage;
      setItem({ ...payload, images, display });
      setMainImage(display);
    } catch (err) {
      setError(err.message || "Lỗi khi tải chi tiết");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDetail();
    // eslint-disable-next-line
  }, [id]);

  const fmtPrice = (v) =>
    v == null ? "—" : new Intl.NumberFormat("vi-VN").format(Number(v)) + " đ";
  const fmtDate = (s) => (s ? new Date(s).toLocaleDateString("vi-VN") : "—");

  const handleOrder = async () => {
    setOrderLoading(true);
    setOrderError("");
    setOrderSuccess("");
    setTopMessage("");
    const token = apiService.getAuthToken();
    if (!token) {
      navigate("/login");
      setOrderLoading(false);
      return;
    }

    try {
      await apiService.createOrder({ listingId: Number(id), quantity: 1 });
      setOrderSuccess("Đặt đơn hàng thành công!");
      setTopMessage("Đặt đơn hàng thành công!");
      // Reload lại chi tiết sản phẩm để cập nhật trạng thái
      await fetchDetail();
    } catch (err) {
      setOrderError(err.message || "Đặt đơn hàng thất bại");
      setTopMessage(err.message || "Đặt đơn hàng thất bại");
    } finally {
      setOrderLoading(false);
    }
  };

  if (loading) return <div className="pd-loading">Đang tải...</div>;
  if (error) return <div className="pd-error">{error}</div>;
  if (!item) return <div className="pd-empty">Không tìm thấy sản phẩm</div>;

  const product = item.product ?? {};
  const seller = item.seller ?? {};

  // Ẩn nút đặt đơn hàng nếu status là PROCESSING hoặc SOLD
  const hideOrderBtn =
    String(item.status).toUpperCase() === "PROCESSING" ||
    String(item.status).toUpperCase() === "SOLD";

  return (
    <>
      <Header />
      <div className="pd-wrapper">
        {/* Hiển thị thông báo trên đầu trang */}
        {topMessage && (
          <div
            className={`pd-top-message ${
              orderSuccess ? "success" : "error"
            }`}
            style={{
              marginBottom: "16px",
              padding: "12px",
              background: orderSuccess ? "#e6ffe6" : "#ffe6e6",
              color: orderSuccess ? "#2e7d32" : "#c62828",
              borderRadius: "6px",
              textAlign: "center",
              fontWeight: "bold",
            }}
          >
            {topMessage}
          </div>
        )}

        <div className="pd-toolbar">
          <button className="pd-back" onClick={() => navigate(-1)}>
            ← Quay lại
          </button>
        </div>

        <div className="pd-card">
          <div className="pd-left">
            <img className="pd-main" src={mainImage} alt={item.title} />
            <div className="pd-thumbs">
              {(item.images && item.images.length
                ? item.images
                : [{ url: item.display }]
              ).map((img, idx) => (
                <img
                  key={idx}
                  src={img?.url || defaultImage}
                  alt={`thumb-${idx}`}
                  className={`pd-thumb ${
                    img?.url === mainImage ? "active" : ""
                  }`}
                  onClick={() => setMainImage(img?.url || defaultImage)}
                />
              ))}
            </div>
          </div>

          <div className="pd-right">
            <div className="pd-header">
              <h1 className="pd-title">{item.title || "—"}</h1>
              <div className="pd-badges">
                <span className="pd-badge category">
                  {item.categoryName || "—"}
                </span>
                <span className="pd-badge brand">{item.brandName || "—"}</span>
                <span
                  className={`pd-badge status ${String(
                    item.status || ""
                  ).toLowerCase()}`}
                >
                  {item.status || "—"}
                </span>
              </div>
            </div>

            <div className="pd-price">{fmtPrice(item.price)}</div>

            <div className="pd-meta">
              <div>Đăng: {fmtDate(item.createdAt)}</div>
              <div>
                Hiệu lực: {fmtDate(item.startDate)} → {fmtDate(item.expiryDate)}
              </div>
            </div>

            <div className="pd-info">
              <h3>Thông số</h3>
              <ul className="pd-list">
                {product.model && (
                  <li>
                    <strong>Model:</strong> {product.model}
                  </li>
                )}
                {product.year && (
                  <li>
                    <strong>Năm:</strong> {product.year}
                  </li>
                )}
                {product.color && (
                  <li>
                    <strong>Màu:</strong> {product.color}
                  </li>
                )}
                {product.condition && (
                  <li>
                    <strong>Tình trạng:</strong> {product.condition}
                  </li>
                )}
                {product.price && (
                  <li>
                    <strong>Giá gốc:</strong> {fmtPrice(product.price)}
                  </li>
                )}
              </ul>
            </div>

            <div className="pd-desc">
              <h3>Mô tả</h3>
              <p>{item.description || "Không có mô tả."}</p>
            </div>

            <div className="pd-seller">
              <h3>Người bán</h3>
              <div>
                <strong>{seller.username || "—"}</strong>
              </div>
              <div className="pd-seller-meta">{seller.email || "—"}</div>
              <div className="pd-actions">
                {!hideOrderBtn && (
                  <button
                    className="pd-contact"
                    onClick={handleOrder}
                    disabled={orderLoading}
                  >
                    {orderLoading ? "Đang đặt..." : "Đặt đơn hàng"}
                  </button>
                )}
                {/* Các thông báo lỗi/thành công đã được hiển thị ở trên */}
              </div>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}

export default ProductDetail;
