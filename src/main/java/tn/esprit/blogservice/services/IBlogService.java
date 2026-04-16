package tn.esprit.blogservice.services;

import tn.esprit.blogservice.entities.Blog;

import java.util.List;

public interface IBlogService {

    Blog addBlog(Blog blog);

    List<Blog> getAllBlogs();

    Blog getBlogById(Long id);

    Blog updateBlog(Long id, Blog blog);

    void deleteBlog(Long id);
}