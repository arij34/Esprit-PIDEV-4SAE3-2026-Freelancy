package tn.esprit.blogservice.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import tn.esprit.blogservice.entities.BlogPost;
import tn.esprit.blogservice.services.IBlogPostService;

@RestController
@RequestMapping("/posts")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = {"Content-Type", "Authorization"}, methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class BlogPostController {

    private final IBlogPostService blogPostService;

    public BlogPostController(IBlogPostService blogPostService) {
        this.blogPostService = blogPostService;
    }

    @PostMapping("/add")
    public BlogPost addPost(@RequestBody BlogPost post) {
        return blogPostService.addPost(post);
    }

    @PostMapping("/addAll")
    public List<BlogPost> addAllPosts(@RequestBody List<BlogPost> posts) {
        return blogPostService.addAllPosts(posts);
    }

    @GetMapping("/get/{id}")
    public BlogPost getPost(@PathVariable Long id) {
        return blogPostService.getPost(id);
    }

    @GetMapping("/all")
    public List<BlogPost> getAllPosts() {
        return blogPostService.getAllPosts();
    }

    @PutMapping("/update/{id}")
    public BlogPost updatePost(@PathVariable Long id, @RequestBody BlogPost post) {
        post.setIdPost(id);
        return blogPostService.updatePost(post);
    }

    @DeleteMapping("/delete/{id}")
    public String deletePost(@PathVariable Long id) {
        blogPostService.deletePost(id);
        return "Post deleted successfully";
    }
}