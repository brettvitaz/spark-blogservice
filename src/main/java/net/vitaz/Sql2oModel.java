package net.vitaz;

import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Sql2oModel implements Model {

    private Sql2o sql2o;
    private UuidGenerator uuidGenerator;

    public Sql2oModel(Sql2o sql2o) {
        this.sql2o = sql2o;
        uuidGenerator = new RandomUuidGenerator();
    }

    @Override
    public UUID createPost(String title, String content, List categories) {
        try (Connection conn = sql2o.beginTransaction()) {
            UUID postUuid = uuidGenerator.generate();
            conn.createQuery("INSERT INTO posts(post_uuid, title, content, publishing_date) VALUES (:post_uuid, :title, :content, :date)")
                    .addParameter("post_uuid", postUuid)
                    .addParameter("title", title)
                    .addParameter("content", content)
                    .addParameter("date", new Date())
                    .executeUpdate();
            categories.forEach((category) ->
                    conn.createQuery("insert into posts_categories(post_uuid, category) VALUES (:post_uuid, :category)")
                            .addParameter("post_uuid", postUuid)
                            .addParameter("category", category)
                            .executeUpdate());
            conn.commit();
            return postUuid;
        }
    }

    @Override
    public UUID createComment(UUID post, String author, String content) {
        try (Connection conn = sql2o.beginTransaction()) {
            UUID contentUuid = uuidGenerator.generate();
            conn.createQuery("INSERT INTO comments(comment_uuid, post_uuid, author, content, approved, submission_date) VALUES (:comment_uuid, :post_uuid, :author, :content, :approved, :submission_date)")
                    .addParameter("comment_uuid", contentUuid)
                    .addParameter("post_uuid", post)
                    .addParameter("author", author)
                    .addParameter("content", content)
                    .addParameter("approved", false)
                    .addParameter("submission_date", new Date())
                    .executeUpdate();
            conn.commit();
            return contentUuid;
        }
    }


    @Override
    public List getAllPosts() {
        try (Connection conn = sql2o.open()) {
            List<Post> posts = conn.createQuery("SELECT * FROM posts").executeAndFetch(Post.class);
            posts.forEach((post) -> post.setCategories(getCategories(conn, post.getPost_uuid())));
            return posts;
        }
    }

    private List getCategories(Connection conn, UUID post_uuid) {
        return conn.createQuery("SELECT category FROM posts_categories WHERE post_uuid = :post_uuid")
                .addParameter("post_uuid", post_uuid)
                .executeAndFetch(String.class);
    }

    @Override
    public List getAllCommentsOn(UUID post) {
        try (Connection conn = sql2o.open()) {
            return conn.createQuery("SELECT * FROM comments WHERE post_uuid = :post_uuid")
                    .addParameter("post_uuid", post)
                    .executeAndFetch(Comment.class);
        }
    }

    @Override
    public boolean existsPost(UUID post) {
        try (Connection conn = sql2o.open()) {
            List<Post> posts = conn.createQuery("SELECT * FROM posts WHERE post_uuid = :post_uuid")
                    .addParameter("post_uuid", post)
                    .executeAndFetch(Post.class);
            return posts.size() > 0;
        }
    }
}
