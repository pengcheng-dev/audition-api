package com.audition.model;

import java.util.List;
import lombok.Data;

@Data
public class AuditionPost {

    private int userId;
    private int id;
    private String title;
    private String body;

    // Add comments field
    private List<Comment> comments;
}
