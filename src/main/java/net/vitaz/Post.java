package net.vitaz;

import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
public class Post {
    private UUID post_uuid;
    private String title;
    private String content;
    Date publishing_date;
    private List categories;
}
