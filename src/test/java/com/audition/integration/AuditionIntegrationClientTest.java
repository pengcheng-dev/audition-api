package com.audition.integration;

import com.audition.common.exception.SystemException;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import java.lang.reflect.Field;
import java.util.ArrayList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditionIntegrationClientTest {

    private RestTemplate restTemplate;

    private AuditionIntegrationClient auditionIntegrationClient;

    @BeforeEach
    void setUp() throws Exception{
        restTemplate = Mockito.mock(RestTemplate.class);
        auditionIntegrationClient = new AuditionIntegrationClient();

        // Use reflection to set the restTemplate field
        Field restTemplateField = AuditionIntegrationClient.class.getDeclaredField("restTemplate");
        restTemplateField.setAccessible(true);
        restTemplateField.set(auditionIntegrationClient, restTemplate);
    }

    @Test
    void testGetPosts() {
        List<AuditionPost> mockPosts = List.of(new AuditionPost());
        Mockito.when(restTemplate.exchange(
            eq("https://jsonplaceholder.typicode.com/posts"),
            eq(HttpMethod.GET),
            isNull(),
            any(ParameterizedTypeReference.class))
        ).thenReturn(ResponseEntity.ok(mockPosts));

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
        ).thenThrow(new HttpClientErrorException(HttpStatusCode.valueOf(404), "test exception"));

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
            .thenThrow(new HttpClientErrorException(HttpStatusCode.valueOf(404), "test exception"));

        assertThrows(SystemException.class, () -> auditionIntegrationClient.getPostById(1));
    }

    @Test
    void testGetPostWithCommentsById() {
        AuditionPost mockPost = new AuditionPost();
        Comment comment = new Comment();
        comment.setId(1);
        comment.setPostId(1);
        comment.setBody("test body");
        List<Comment> mockComments = new ArrayList<>();
        mockComments.add(comment);
        ResponseEntity<List<Comment>> mockResponseEntity = ResponseEntity.ok(mockComments);
        System.out.println(mockResponseEntity.getBody());

        Mockito.when(restTemplate.getForObject("https://jsonplaceholder.typicode.com/posts/1", AuditionPost.class))
            .thenReturn(mockPost);

        // Mock RestTemplate call
        Mockito.when(restTemplate.exchange(
            eq("https://jsonplaceholder.typicode.com/posts/1/comments"),
            eq(HttpMethod.GET),
            eq(null),
            any(ParameterizedTypeReference.class))
        ).thenReturn(mockResponseEntity);

        // Call the method under test
        List<Comment> comments = auditionIntegrationClient.getCommentsByPostId(1);
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
        ).thenThrow(new HttpClientErrorException(HttpStatusCode.valueOf(404), "test exception"));

        assertThrows(SystemException.class, () -> auditionIntegrationClient.getCommentsByPostId(1));
    }
}
