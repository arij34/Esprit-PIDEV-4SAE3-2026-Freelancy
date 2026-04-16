package tn.esprit.blogservice.services;

import java.util.List;

import org.springframework.stereotype.Service;

import tn.esprit.blogservice.entities.Blog;
import tn.esprit.blogservice.repositories.BlogRepository;

@Service
public class BlogServiceImpl implements IBlogService {

    private final BlogRepository blogRepository;

    public BlogServiceImpl(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    @Override
    public Blog addBlog(Blog blog) {
        return blogRepository.save(blog);
    }

    @Override
    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    @Override
    public Blog getBlogById(Long id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog not found"));
    }

    @Override
    public Blog updateBlog(Long id, Blog blog) {
        Blog existing = getBlogById(id);
        existing.setTitle(blog.getTitle());
        existing.setContent(blog.getContent());
        existing.setAuthor(blog.getAuthor());
        return blogRepository.save(existing);
    }

    @Override
    public void deleteBlog(Long id) {
        blogRepository.deleteById(id);
    }
}