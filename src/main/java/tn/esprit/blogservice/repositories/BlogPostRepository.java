package tn.esprit.blogservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import tn.esprit.blogservice.entities.BlogPost;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
}