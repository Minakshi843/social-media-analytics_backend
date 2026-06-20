package com.social.analytics.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "targets", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"city_id", "platform"})
})
public class Target {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(nullable = false, length = 50)
    private String platform; // INSTAGRAM, FACEBOOK, LINKEDIN, X

    @Column(name = "daily_static_target", nullable = false)
    private int dailyStaticTarget = 0;

    @Column(name = "daily_carousel_target", nullable = false)
    private int dailyCarouselTarget = 0;

    @Column(name = "daily_reel_target", nullable = false)
    private int dailyReelTarget = 0;

    @Column(name = "daily_post_target", nullable = false)
    private int dailyPostTarget = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Target() {
    }

    public Target(City city, String platform, int dailyStaticTarget, int dailyCarouselTarget, int dailyReelTarget, int dailyPostTarget) {
        this.city = city;
        this.platform = platform;
        this.dailyStaticTarget = dailyStaticTarget;
        this.dailyCarouselTarget = dailyCarouselTarget;
        this.dailyReelTarget = dailyReelTarget;
        this.dailyPostTarget = dailyPostTarget;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
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

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public int getDailyStaticTarget() {
        return dailyStaticTarget;
    }

    public void setDailyStaticTarget(int dailyStaticTarget) {
        this.dailyStaticTarget = dailyStaticTarget;
    }

    public int getDailyCarouselTarget() {
        return dailyCarouselTarget;
    }

    public void setDailyCarouselTarget(int dailyCarouselTarget) {
        this.dailyCarouselTarget = dailyCarouselTarget;
    }

    public int getDailyReelTarget() {
        return dailyReelTarget;
    }

    public void setDailyReelTarget(int dailyReelTarget) {
        this.dailyReelTarget = dailyReelTarget;
    }

    public int getDailyPostTarget() {
        return dailyPostTarget;
    }

    public void setDailyPostTarget(int dailyPostTarget) {
        this.dailyPostTarget = dailyPostTarget;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
