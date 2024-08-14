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
import static org.mockito.Mockito.*;

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
        Mockito.when(auditionIntegrationClient.getPosts()).thenReturn(mockPosts);

        List<AuditionPost> posts = auditionService.getPosts();
        assertNotNull(posts);
        assertEquals(1, posts.size());
    }

    @Test
    void testGetPostById() {
        AuditionPost mockPost = new AuditionPost();
        Mockito.when(auditionIntegrationClient.getPostById(1)).thenReturn(mockPost);

        AuditionPost post = auditionService.getPostById(1);
        assertNotNull(post);
    }

    @Test
    void testGetCommentsByPostId() {
        List<Comment> mockComments = List.of(new Comment());
        Mockito.when(auditionIntegrationClient.getCommentsByPostId(1)).thenReturn(mockComments);

        List<Comment> comments = auditionService.getCommentsByPostId(1);
        assertNotNull(comments);
        assertEquals(1, comments.size());
    }
}
