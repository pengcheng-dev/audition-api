package com.audition.web;

import com.audition.common.logging.AuditionLogger;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.service.AuditionService;
import com.audition.web.advice.ExceptionControllerAdvice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class AuditionControllerAdviceTest {

    @MockBean
    private AuditionService auditionService;

    @MockBean
    private AuditionLogger logger;

    @Autowired
    private AuditionController auditionController;

    @BeforeEach
    void setUp() {
        List<AuditionPost> mockPosts = List.of(new AuditionPost());
        Mockito.when(auditionService.getPosts()).thenReturn(mockPosts);
    }

    @Test
    void testGetPosts() {
        List<AuditionPost> posts = auditionController.getPosts(null);
        assertNotNull(posts);
        assertEquals(1, posts.size());
    }

    @Test
    void testGetPost() {
        AuditionPost mockPost = new AuditionPost();
        Mockito.when(auditionService.getPostById(1)).thenReturn(mockPost);

        AuditionPost post = auditionController.getPost("1");
        assertNotNull(post);
    }

    @Test
    void testGetPostWithInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> auditionController.getPost("-1"));
        assertThrows(IllegalArgumentException.class, () -> auditionController.getPost("abc"));
    }

    @Test
    void testGetCommentsByPostId() {
        List<Comment> mockComments = List.of(new Comment());
        Mockito.when(auditionService.getCommentsByPostId(1)).thenReturn(mockComments);

        List<Comment> comments = auditionController.getCommentsByPostId("1");
        assertNotNull(comments);
        assertEquals(1, comments.size());
    }

    @Test
    void testGetCommentsByPostIdWithInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> auditionController.getCommentsByPostId("-1"));
        assertThrows(IllegalArgumentException.class, () -> auditionController.getCommentsByPostId("abc"));
    }
}