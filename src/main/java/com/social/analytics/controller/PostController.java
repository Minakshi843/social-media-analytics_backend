package com.social.analytics.controller;

import com.social.analytics.model.Post;
import com.social.analytics.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostRepository postRepository;

    @Autowired
    public PostController(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @GetMapping("/city/{cityId}")
    public ResponseEntity<List<Post>> getPostsByCity(@PathVariable Long cityId) {
        // Fetch posts for the specified city and sort them
        List<Post> posts = postRepository.findByCityId(cityId);
        
        // Sort chronologically in descending order
        posts.sort((p1, p2) -> {
            int dateCompare = p2.getPostDate().compareTo(p1.getPostDate());
            if (dateCompare != 0) {
                return dateCompare;
            }
            return p2.getPostTime().compareTo(p1.getPostTime());
        });

        return ResponseEntity.ok(posts);
    }
}
