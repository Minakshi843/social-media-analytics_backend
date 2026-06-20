package com.social.analytics.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "analytics", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"city_id", "date", "platform"})
})
public class Analytics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, length = 50)
    private String platform; // INSTAGRAM, FACEBOOK, LINKEDIN, X

    @Column(name = "static_count", nullable = false)
    private int staticCount = 0;

    @Column(name = "carousel_count", nullable = false)
    private int carouselCount = 0;

    @Column(name = "reel_count", nullable = false)
    private int reelCount = 0;

    @Column(name = "post_count", nullable = false)
    private int postCount = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Analytics() {
    }

    public Analytics(City city, LocalDate date, String platform, int staticCount, int carouselCount, int reelCount, int postCount) {
        this.city = city;
        this.date = date;
        this.platform = platform;
        this.staticCount = staticCount;
        this.carouselCount = carouselCount;
        this.reelCount = reelCount;
        this.postCount = postCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public int getStaticCount() {
        return staticCount;
    }

    public void setStaticCount(int staticCount) {
        this.staticCount = staticCount;
    }

    public int getCarouselCount() {
        return carouselCount;
    }

    public void setCarouselCount(int carouselCount) {
        this.carouselCount = carouselCount;
    }

    public int getReelCount() {
        return reelCount;
    }

    public void setReelCount(int reelCount) {
        this.reelCount = reelCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
