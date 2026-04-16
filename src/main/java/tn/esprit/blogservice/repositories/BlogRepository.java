package tn.esprit.blogservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.blogservice.entities.Blog;

public interface BlogRepository extends JpaRepository<Blog, Long> {
}