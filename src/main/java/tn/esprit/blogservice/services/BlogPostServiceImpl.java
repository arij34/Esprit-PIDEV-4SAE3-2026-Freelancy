package tn.esprit.blogservice.services;

import java.util.List;

import org.springframework.stereotype.Service;

import tn.esprit.blogservice.entities.BlogPost;
import tn.esprit.blogservice.entities.BlogStatus;
import tn.esprit.blogservice.repositories.BlogPostRepository;

@Service
public class BlogPostServiceImpl implements IBlogPostService {

    private final BlogPostRepository blogPostRepository;

    public BlogPostServiceImpl(BlogPostRepository blogPostRepository) {
        this.blogPostRepository = blogPostRepository;
    }

    @Override
    public BlogPost addPost(BlogPost post) {
        if (post.getStatus() == null) {
            post.setStatus(BlogStatus.APPROVED);
        }
        if (post.getLikes() == null) {
            post.setLikes(0);
        }
        if (post.getDislikes() == null) {
            post.setDislikes(0);
        }
        return blogPostRepository.save(post);
    }

    @Override
    public List<BlogPost> addAllPosts(List<BlogPost> posts) {
        return (List<BlogPost>) blogPostRepository.saveAll(posts);
    }

    @Override
    public BlogPost getPost(Long id) {
        return blogPostRepository.findById(id).orElse(null);
    }

    @Override
    public List<BlogPost> getAllPosts() {
        return (List<BlogPost>) blogPostRepository.findAll();
    }

    @Override
    public BlogPost updatePost(BlogPost post) {

        BlogPost existingPost = blogPostRepository.findById(post.getIdPost())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        existingPost.setTitle(post.getTitle());
        existingPost.setContent(post.getContent());
        existingPost.setAuthor(post.getAuthor());
        if (post.getStatus() != null) {
            existingPost.setStatus(post.getStatus());
        }
        if (post.getLikes() != null) {
            existingPost.setLikes(post.getLikes());
        }
        if (post.getDislikes() != null) {
            existingPost.setDislikes(post.getDislikes());
        }

        return blogPostRepository.save(existingPost);

    }

    @Override
    public void deletePost(Long id) {
        blogPostRepository.deleteById(id);
    }
}
