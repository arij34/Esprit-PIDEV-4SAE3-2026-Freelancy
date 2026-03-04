package tn.freelancy.skillmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.freelancy.skillmanagement.entity.Experience;
import tn.freelancy.skillmanagement.entity.User;

import java.util.List;

@Repository
public interface ExperienceRepository extends JpaRepository<Experience, Long> {
    List<Experience> findByUser_Id(Long userId);

    void deleteByUser(User user);
}
