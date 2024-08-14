package com.audition.service;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * business logic for query posts and comments
 */
@Service
public class AuditionService {

    @Autowired
    private AuditionIntegrationClient auditionIntegrationClient;

    public List<AuditionPost> getPosts() {
        return auditionIntegrationClient.getPosts();
    }

    public AuditionPost getPostById(int postId) {
        if(postId <= 0){
            throw new IllegalArgumentException("Post ID must be positive");
        }
        return auditionIntegrationClient.getPostById(postId);
    }

    public List<Comment> getCommentsByPostId(int postId) {
        if(postId <= 0){
            throw new IllegalArgumentException("Post ID must be positive");
        }
        return auditionIntegrationClient.getCommentsByPostId(postId);
    }
}
