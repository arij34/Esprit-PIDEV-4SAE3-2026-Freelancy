package tn.esprit.blogservice.services;

import tn.esprit.blogservice.entities.BlogPost;

import java.util.List;

public interface IBlogPostService {

    BlogPost addPost(BlogPost post);

    List<BlogPost> addAllPosts(List<BlogPost> posts);

    BlogPost getPost(Long id);

    List<BlogPost> getAllPosts();

    BlogPost updatePost(BlogPost post);

    void deletePost(Long id);
}