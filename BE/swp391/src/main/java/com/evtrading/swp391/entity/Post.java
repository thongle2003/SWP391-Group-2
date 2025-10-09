package com.evtrading.swp391.entity;


import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Table(name = "posts")
public class Post {
@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
private Long id;

@Column(nullable = false, length = 255)
private String title;

@Column(columnDefinition = "NVARCHAR(MAX)")
private String description;

@Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
private PostStatus status = PostStatus.DRAF;

@Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
private Visibility visibility = Visibility.PUBLIC;

@Column(precision = 18, scale = 2)
private BigDecimal price;

@Column(nullable = false)
private Boolean deleted = false;

@Column(name ="author_id")
private Long authorId;

@Column(name = "category_id")
private Long categoryId;

@Column(nullable = false)
Instant createdAt = Instant.now();
private Instant updatedAt;

// Getters and Setters
public Long getId() {
    return id;
}
public void setId(Long id) {
    this.id = id;
}
public String getTitle() {
    return title;
}
public void setTitle(String title) {
    this.title = title;
}
public String getDescription() {
    return description;
}
public void setDescription(String description) {
    this.description = description;
}
public PostStatus getStatus() {
    return status;
}
public void setStatus(PostStatus status) {
    this.status = status;
}
public Visibility getVisibility() {
    return visibility;
}
public void setVisibility(Visibility visibility) {
    this.visibility = visibility;
}
public BigDecimal getPrice() {
    return price;
}
public void setPrice(BigDecimal price) {
    this.price = price;
}
public Boolean getDeleted() {
    return deleted;
}
public void setDeleted(Boolean deleted) {
    this.deleted = deleted;
}
public Long getAuthorId() {
    return authorId;
}
public void setAuthorId(Long authorId) {
    this.authorId = authorId;
}
public Long getCategoryId() {
    return categoryId;
}
public void setCategoryId(Long categoryId) {
    this.categoryId = categoryId;
}
public Instant getCreatedAt() {
    return createdAt;
}
public void setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
}
public Instant getUpdatedAt() {
    return updatedAt;
}
public void setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
}




}
