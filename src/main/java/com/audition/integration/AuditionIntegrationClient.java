package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class AuditionIntegrationClient {


    @Autowired
    private RestTemplate restTemplate;

    /**
     * fetch all posts
     *
     * @return List of AuditionPost
     */
    public List<AuditionPost> getPosts() {
        try {
            ResponseEntity<List<AuditionPost>> response = restTemplate.exchange(
                "https://jsonplaceholder.typicode.com/posts",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<AuditionPost>>() {
                });
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new SystemException("Failed to retrieve posts", e.getStatusCode().value(), e);
        } catch (Exception e) {
            throw new SystemException("An unexpected error occurred while retrieving posts", e);
        }
    }

    /**
     * fetch post by a dedicated id
     *
     * @param id used to fetch post
     * @return AuditionPost
     */
    public AuditionPost getPostById(int id) {
        try {
            return restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/" + id, AuditionPost.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SystemException("Cannot find a Post with id " + id, "Resource Not Found", 404);
            } else {
                throw new SystemException(e.getResponseBodyAsString(), e.getStatusCode().value(), e);
            }
        } catch (Exception e) {
            throw new SystemException("An unexpected error occurred while retrieving the post", e);
        }
    }

    /**
     * fetch post by id and filled with comments
     *
     * @param postId used to fetch post and comments of this post
     * @return AuditionPost filled with comments
     */
    public AuditionPost getPostWithCommentsById(int postId) {
        try {
            // Fetch the post
            AuditionPost post = restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/" + postId,
                AuditionPost.class);

            // Fetch the comments for the post
            if (post != null) {
                List<Comment> comments = getCommentsByPostId(post.getId());
                // Set comments to the post
                post.setComments(comments);
            }
            return post;
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SystemException("Cannot find a Post with id " + postId, "Resource Not Found", 404);
            } else {
                throw new SystemException(e.getResponseBodyAsString(), e.getStatusCode().value(), e);
            }
        } catch (Exception e) {
            throw new SystemException("An unexpected error occurred while retrieving the post with comments", e);
        }
    }

    /**
     * fetch a comment list of a dedicated post by id
     *
     * @param postId used to fetch comments of a dedicated post by id
     * @return List of comment of a post
     */
    public List<Comment> getCommentsByPostId(int postId) {

        try {
            ResponseEntity<List<Comment>> response = restTemplate.exchange(
                "https://jsonplaceholder.typicode.com/posts/" + postId + "/comments",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Comment>>() {
                });
            return response.getBody();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new SystemException("Cannot find comments for Post with id " + postId, "Resource Not Found", 404);
            } else {
                throw new SystemException(e.getResponseBodyAsString(), e.getStatusCode().value(), e);
            }
        } catch (Exception e) {
            throw new SystemException("An unexpected error occurred while retrieving comments", e);
        }
    }
}
