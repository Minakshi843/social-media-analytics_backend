package com.social.analytics.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "posts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"platform", "post_id"})
})
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "post_id", nullable = false, length = 100)
    private String postId; // External post ID from platform API

    @Column(nullable = false, length = 50)
    private String platform; // INSTAGRAM, FACEBOOK, LINKEDIN, X

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "post_url", length = 255)
    private String postUrl;

    @Column(columnDefinition = "TEXT")
    private String caption;

    @Column(name = "post_date", nullable = false)
    private LocalDate postDate;

    @Column(name = "post_time", nullable = false)
    private LocalTime postTime;

    @Column(name = "post_type", nullable = false, length = 50)
    private String postType; // STATIC, REEL, CAROUSEL

    @Column(nullable = false)
    private int likes = 0;

    @Column(nullable = false)
    private int comments = 0;

    @Column(nullable = false)
    private int reach = 0;

    @Column(nullable = false)
    private int impressions = 0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Post() {
    }

    public Post(String postId, String platform, City city, String postUrl, String caption, LocalDate postDate, LocalTime postTime, String postType, int likes, int comments, int reach, int impressions) {
        this.postId = postId;
        this.platform = platform;
        this.city = city;
        this.postUrl = postUrl;
        this.caption = caption;
        this.postDate = postDate;
        this.postTime = postTime;
        this.postType = postType;
        this.likes = likes;
        this.comments = comments;
        this.reach = reach;
        this.impressions = impressions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public City getCity() {
        return city;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public LocalDate getPostDate() {
        return postDate;
    }

    public void setPostDate(LocalDate postDate) {
        this.postDate = postDate;
    }

    public LocalTime getPostTime() {
        return postTime;
    }

    public void setPostTime(LocalTime postTime) {
        this.postTime = postTime;
    }

    public String getPostType() {
        return postType;
    }

    public void setPostType(String postType) {
        this.postType = postType;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public int getReach() {
        return reach;
    }

    public void setReach(int reach) {
        this.reach = reach;
    }

    public int getImpressions() {
        return impressions;
    }

    public void setImpressions(int impressions) {
        this.impressions = impressions;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
