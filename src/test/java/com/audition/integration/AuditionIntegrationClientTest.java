package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class AuditionIntegrationClientTest {

    @MockBean
    private RestTemplate restTemplate;

    @Autowired
    private AuditionIntegrationClient auditionIntegrationClient;

    @BeforeEach
    void setUp() {
        List<AuditionPost> mockPosts = List.of(new AuditionPost());
        Mockito.when(restTemplate.exchange(
            eq("https://jsonplaceholder.typicode.com/posts"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class))
        ).thenReturn(ResponseEntity.ok(mockPosts));
    }

    @Test
    void testGetPosts() {
        List<AuditionPost> posts = auditionIntegrationClient.getPosts();
        assertNotNull(posts);
        assertEquals(1, posts.size());
    }

    @Test
    void testGetPostsHttpClientErrorException() {
        Mockito.when(restTemplate.exchange(
            eq("https://jsonplaceholder.typicode.com/posts"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class))
        ).thenThrow(HttpClientErrorException.class);

        assertThrows(SystemException.class, () -> auditionIntegrationClient.getPosts());
    }

    @Test
    void testGetPostById() {
        AuditionPost mockPost = new AuditionPost();
        Mockito.when(restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/1", AuditionPost.class))
            .thenReturn(mockPost);

        AuditionPost post = auditionIntegrationClient.getPostById(1);
        assertNotNull(post);
    }

    @Test
    void testGetPostByIdHttpClientErrorException() {
        Mockito.when(restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/1", AuditionPost.class))
            .thenThrow(HttpClientErrorException.class);

        assertThrows(SystemException.class, () -> auditionIntegrationClient.getPostById(1));
    }

    @Test
    void testGetPostWithCommentsById() {
        AuditionPost mockPost = new AuditionPost();
        List<Comment> mockComments = List.of(new Comment());
        Mockito.when(restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/1", AuditionPost.class))
            .thenReturn(mockPost);
        Mockito.when(restTemplate.exchange(
            eq("https://jsonplaceholder.typicode.com/posts/1/comments"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class))
        ).thenReturn(ResponseEntity.ok(mockComments));

        AuditionPost post = auditionIntegrationClient.getPostWithCommentsById(1);
        assertNotNull(post);
        assertEquals(1, post.getComments().size());
    }

    @Test
    void testGetCommentsByPostId() {
        List<Comment> mockComments = List.of(new Comment());
        ResponseEntity<List<Comment>> responseEntity = ResponseEntity.ok(mockComments);

        Mockito.when(restTemplate.exchange(
            eq("https://jsonplaceholder.typicode.com/posts/1/comments"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class))
        ).thenReturn(responseEntity);

        List<Comment> comments = auditionIntegrationClient.getCommentsByPostId(1);
        assertNotNull(comments);
        assertEquals(1, comments.size());
    }

    @Test
    void testGetCommentsByPostIdHttpClientErrorException() {
        Mockito.when(restTemplate.exchange(
            eq("https://jsonplaceholder.typicode.com/posts/1/comments"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class))
        ).thenThrow(HttpClientErrorException.class);

        assertThrows(SystemException.class, () -> auditionIntegrationClient.getCommentsByPostId(1));
    }
}
