package net.vitaz;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

import static spark.Spark.get;
import static spark.Spark.post;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.sql2o.Sql2o;
//import spark.Request;
//import spark.Response;
//import spark.Route;

import java.io.IOException;
import java.io.StringWriter;
import java.util.UUID;

public class BlogService {
    public static void main(String[] args) {
//        CommandLineOptions options = new CommandLineOptions();
//        new JCommander(options, args);

        Model model = new Sql2oModel(new Sql2o("jdbc:postgresql://192.168.99.100:5432/blog", "blog_owner", "sparkforthewin"));

        get("/hello", (req, res) -> "Hello World!");

        post("/posts", (req, res) -> {
            try {
                ObjectMapper mapper = new ObjectMapper();
                Post post = mapper.readValue(req.body(), Post.class);

/*
                if (!post.isValid()) {
                    res.status(HTTP_BAD_REQUEST);
                    return "";
                }
*/

                UUID id = model.createPost(post.getTitle(), post.getContent(), post.getCategories());
                res.status(HTTP_OK);
//                res.type("application/text");
                return id;
            } catch (JsonParseException jpe) {
                res.status(HTTP_BAD_REQUEST);
                return "";
            }

        });

        get("/posts", (req, res) -> {
            res.status(HTTP_OK);
            res.type("application/json");
            return dataToJson(model.getAllPosts());
        });

        post("/posts/:uuid/comments", (request, response) -> {
            ObjectMapper mapper = new ObjectMapper();
            Comment comment = mapper.readValue(request.body(), Comment.class);
            UUID post = UUID.fromString(request.params(":uuid"));
            if (!model.existsPost(post)) {
                response.status(HTTP_BAD_REQUEST);
                return "";
            }
            UUID id = model.createComment(post, comment.getAuthor(), comment.getContent());
            response.status(HTTP_OK);
            return id;
        });

        get("/posts/:uuid/comments", (request, response) -> {
            UUID post = UUID.fromString(request.params(":uuid"));
            if (!model.existsPost(post)) {
                response.status(HTTP_BAD_REQUEST);
                return "";
            }
            response.status(HTTP_OK);
            response.type("application/json");
            return dataToJson(model.getAllCommentsOn(post));
        });
    }

    private static String dataToJson(Object data) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(SerializationFeature.INDENT_OUTPUT);
            StringWriter sw = new StringWriter();
            mapper.writeValue(sw, data);
            return sw.toString();
        } catch (IOException e) {
            throw new RuntimeException("IOException in StringWriter? Why?!?");
        }
    }
}
