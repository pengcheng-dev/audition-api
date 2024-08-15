package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

class AuditionIntegrationClientTest {

    private transient RestTemplate restTemplate;

    private transient AuditionIntegrationClient auditionIntegrationClient;

    @BeforeEach
    void setUp() throws Exception {
        restTemplate = Mockito.mock(RestTemplate.class);
        auditionIntegrationClient = new AuditionIntegrationClient();

        // Set the private field 'restTemplate' using ReflectionTestUtils
        ReflectionTestUtils.setField(auditionIntegrationClient, "restTemplate", restTemplate);
    }

    @Test
    void testGetPosts() {
        final List<AuditionPost> mockPosts = List.of(new AuditionPost());
        final ParameterizedTypeReference<List<AuditionPost>> responseType = new ParameterizedTypeReference<List<AuditionPost>>() {};
        Mockito.when(restTemplate.exchange(
            eq("https://jsonplaceholder.typicode.com/posts"),
            eq(HttpMethod.GET),
            isNull(),
            eq(responseType))
        ).thenReturn(ResponseEntity.ok(mockPosts));

        final List<AuditionPost> posts = auditionIntegrationClient.getPosts();
        assertNotNull(posts);
        assertEquals(1, posts.size());
    }

    @Test
    void testGetPostsHttpClientErrorException() {
        final ParameterizedTypeReference<List<AuditionPost>> responseType = new ParameterizedTypeReference<List<AuditionPost>>() {};
        Mockito.when(restTemplate.exchange(
            eq("https://jsonplaceholder.typicode.com/posts"),
            eq(HttpMethod.GET),
            isNull(),
            eq(responseType))
        ).thenThrow(new HttpClientErrorException(HttpStatusCode.valueOf(404), "test exception"));

        assertThrows(SystemException.class, () -> auditionIntegrationClient.getPosts());
    }

    @Test
    void testGetPostById() {
        final AuditionPost mockPost = new AuditionPost();
        Mockito.when(restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/1", AuditionPost.class))
            .thenReturn(mockPost);

        final AuditionPost post = auditionIntegrationClient.getPostById(1);
        assertNotNull(post);
    }

    @Test
    void testGetPostByIdHttpClientErrorException() {
        Mockito.when(restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/1", AuditionPost.class))
            .thenThrow(new HttpClientErrorException(HttpStatusCode.valueOf(404), "test exception"));

        assertThrows(SystemException.class, () -> auditionIntegrationClient.getPostById(1));
    }

    @Test
    @SuppressWarnings("deprecation")
    void testGetPostWithCommentsById() {
        final Comment comment = new Comment();
        comment.setId(1);
        comment.setPostId(1);
        comment.setBody("test body");
        final List<Comment> mockComments = new ArrayList<>();
        mockComments.add(comment);
        final ResponseEntity<List<Comment>> mockResponseEntity = ResponseEntity.ok(mockComments);

        final AuditionPost post = new AuditionPost();
        post.setId(1);
        post.setTitle("test title");
        post.setBody("any test body");

        Mockito.when(restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/1", AuditionPost.class))
            .thenReturn(post);

        // Mock RestTemplate call
        final ParameterizedTypeReference<List<Comment>> responseType = new ParameterizedTypeReference<List<Comment>>() {};
        Mockito.when(restTemplate.exchange(
            eq("https://jsonplaceholder.typicode.com/posts/1/comments"),
            eq(HttpMethod.GET),
            eq(null),
            eq(responseType))
        ).thenReturn(mockResponseEntity);

        // Call the method under test
        final AuditionPost resltPost = auditionIntegrationClient.getPostWithCommentsById(1);
        assertNotNull(resltPost);
        assertEquals(1, resltPost.getComments().size());
        assertEquals(1, resltPost.getComments().get(0).getId());
    }

    @Test
    void testGetCommentsByPostId() {
        final List<Comment> mockComments = List.of(new Comment());
        final ResponseEntity<List<Comment>> responseEntity = ResponseEntity.ok(mockComments);

        final ParameterizedTypeReference<List<Comment>> responseType = new ParameterizedTypeReference<List<Comment>>() {};
        Mockito.when(restTemplate.exchange(
            eq("https://jsonplaceholder.typicode.com/posts/1/comments"),
            eq(HttpMethod.GET),
            isNull(),
            eq(responseType))
        ).thenReturn(responseEntity);

        final List<Comment> comments = auditionIntegrationClient.getCommentsByPostId(1);
        assertNotNull(comments);
        assertEquals(1, comments.size());
    }

    @Test
    void testGetCommentsByPostIdHttpClientErrorException() {
        final ParameterizedTypeReference<List<Comment>> responseType = new ParameterizedTypeReference<List<Comment>>() {};
        Mockito.when(restTemplate.exchange(
            eq("https://jsonplaceholder.typicode.com/posts/1/comments"),
            eq(HttpMethod.GET),
            isNull(),
            eq(responseType))
        ).thenThrow(new HttpClientErrorException(HttpStatusCode.valueOf(404), "test exception"));

        assertThrows(SystemException.class, () -> auditionIntegrationClient.getCommentsByPostId(1));
    }
}
