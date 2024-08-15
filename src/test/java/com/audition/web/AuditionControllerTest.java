package com.audition.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.service.AuditionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


@SpringBootTest
class AuditionControllerTest {

    @MockBean
    private transient AuditionService auditionService;

    @Autowired
    private transient AuditionController auditionController;

    @BeforeEach
    void setUp() {
        final List<AuditionPost> mockPosts = List.of(new AuditionPost());
        Mockito.when(auditionService.getPosts()).thenReturn(mockPosts);
    }

    @Test
    void testGetPosts() {
        final List<AuditionPost> posts = auditionController.getPosts(null);
        assertNotNull(posts);
        assertEquals(1, posts.size());
    }

    @Test
    void testGetPost() {
        final AuditionPost mockPost = new AuditionPost();
        Mockito.when(auditionService.getPostById(1)).thenReturn(mockPost);

        final AuditionPost post = auditionController.getPost("1");
        assertNotNull(post);
    }

    @Test
    void testGetPostWithInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> auditionController.getPost("-1"));
        assertThrows(IllegalArgumentException.class, () -> auditionController.getPost("abc"));
    }

    @Test
    void testGetCommentsByPostId() {
        final List<Comment> mockComments = List.of(new Comment());
        Mockito.when(auditionService.getCommentsByPostId(1)).thenReturn(mockComments);

        final List<Comment> comments = auditionController.getCommentsByPostId("1");
        assertNotNull(comments);
        assertEquals(1, comments.size());
    }

    @Test
    void testGetCommentsByPostIdWithInvalidId() {
        assertThrows(IllegalArgumentException.class, () -> auditionController.getCommentsByPostId("-1"));
        assertThrows(IllegalArgumentException.class, () -> auditionController.getCommentsByPostId("abc"));
    }
}