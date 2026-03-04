package tn.freelancy.skillmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.freelancy.skillmanagement.entity.Education;
import tn.freelancy.skillmanagement.entity.User;

import java.util.List;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {

    Education findTopByUser_IdOrderByYearDesc(Long userId);

    void deleteByUser(User user);
}