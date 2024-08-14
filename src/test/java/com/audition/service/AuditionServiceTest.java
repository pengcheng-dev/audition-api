package com.audition.service;

import com.audition.integration.AuditionIntegrationClient;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class AuditionServiceTest {

    @MockBean
    private AuditionIntegrationClient auditionIntegrationClient;

    @Autowired
    private AuditionService auditionService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetPosts() {
        List<AuditionPost> mockPosts = List.of(new AuditionPost());
        when(auditionIntegrationClient.getPosts()).thenReturn(mockPosts);

        List<AuditionPost> posts = auditionService.getPosts();
        assertNotNull(posts);
        assertEquals(1, posts.size());
    }

    @Test
    void testGetPostById() {
        AuditionPost mockPost = new AuditionPost();
        when(auditionIntegrationClient.getPostById(1)).thenReturn(mockPost);

        AuditionPost post = auditionService.getPostById(1);
        assertNotNull(post);
    }

    @Test
    void testGetCommentsByPostId() {
        List<Comment> mockComments = List.of(new Comment());
        when(auditionIntegrationClient.getCommentsByPostId(1)).thenReturn(mockComments);

        List<Comment> comments = auditionService.getCommentsByPostId(1);
        assertNotNull(comments);
        assertEquals(1, comments.size());
    }

    @Test
    void testGetPostWithCommentsById_ValidId() {
        // Arrange
        AuditionPost mockPost = new AuditionPost();
        mockPost.setId(1);

        Comment mockComment = new Comment();
        mockComment.setPostId(1);
        mockComment.setId(1);
        mockComment.setBody("Test comment");

        List<Comment> mockComments = List.of(mockComment);

        when(auditionIntegrationClient.getPostById(1)).thenReturn(mockPost);
        when(auditionIntegrationClient.getCommentsByPostId(1)).thenReturn(mockComments);

        // Act
        AuditionPost result = auditionService.getPostWithCommentsById(1);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertNotNull(result.getComments());
        assertEquals(1, result.getComments().size());
        assertEquals("Test comment", result.getComments().get(0).getBody());

        // Verify that the client methods were called
        verify(auditionIntegrationClient, times(1)).getPostById(1);
        verify(auditionIntegrationClient, times(1)).getCommentsByPostId(1);
    }

    @Test
    void testGetPostWithCommentsById_NegativeId() {
        // Arrange & Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            auditionService.getPostWithCommentsById(-1);
        });

        assertEquals("Post ID must be positive", exception.getMessage());

        // Verify that no client methods were called
        verify(auditionIntegrationClient, never()).getPostById(anyInt());
        verify(auditionIntegrationClient, never()).getCommentsByPostId(anyInt());
    }

    @Test
    void testGetPostWithCommentsById_PostNotFound() {
        // Arrange
        when(auditionIntegrationClient.getPostById(1)).thenReturn(null);

        // Act
        AuditionPost result = auditionService.getPostWithCommentsById(1);

        // Assert
        assertNull(result);

        // Verify that the client method was called once
        verify(auditionIntegrationClient, times(1)).getPostById(1);

        // Verify that getCommentsByPostId was never called
        verify(auditionIntegrationClient, never()).getCommentsByPostId(anyInt());
    }

}
