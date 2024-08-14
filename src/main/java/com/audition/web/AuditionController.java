package com.audition.web;

import com.audition.common.logging.AuditionLogger;
import com.audition.model.AuditionPost;
import com.audition.model.Comment;
import com.audition.service.AuditionService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuditionController {

    private static final Logger LOG = LoggerFactory.getLogger(AuditionController.class);
    @Autowired
    AuditionService auditionService;
    @Autowired
    private AuditionLogger logger;

    /**
     * @param filterString used to filter the posts either title or body contains this filter string
     * @return List of AuditionPost
     */
    @RequestMapping(value = "/posts", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<AuditionPost> getPosts(
        @RequestParam(value = "filterString", required = false) final String filterString) {
        List<AuditionPost> posts = auditionService.getPosts();

        if (filterString != null && !filterString.isEmpty()) {
            posts = posts.stream()
                .filter((post) -> post.getTitle().contains(filterString) || post.getBody().contains(filterString))
                .toList();
        }

        return posts;
    }

    /**
     * @param postId used to query post match this post ID
     * @return AuditionPost
     */
    @RequestMapping(value = "/posts/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody AuditionPost getPost(@PathVariable("id") final String postId) {

        try {
            int id = Integer.parseInt(postId);
            if (id <= 0) {
                throw new IllegalArgumentException("Post ID must be positive");
            }

            return auditionService.getPostById(id);
        } catch (NumberFormatException e) {
            logger.logErrorWithException(LOG, e.getMessage(), e);
            throw new IllegalArgumentException("Invalid Post ID format.");
        }
    }

    /**
     * @param postId used to query all comments of this post
     * @return AuditionPost
     */
    @RequestMapping(value = "/posts/{id}/comments", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<Comment> getCommentsByPostId(@PathVariable("id") final String postId) {
        try {
            int id = Integer.parseInt(postId);
            if (id <= 0) {
                throw new IllegalArgumentException("Post ID must be positive.");
            }
            return auditionService.getCommentsByPostId(id);
        } catch (NumberFormatException e) {
            logger.logErrorWithException(LOG, e.getMessage(), e);
            throw new IllegalArgumentException("Invalid Post ID format.");
        }
    }

}
