package tn.freelancy.skillmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.freelancy.skillmanagement.entity.PendingSkill;
import tn.freelancy.skillmanagement.entity.Status;

import java.util.List;

public interface PendingSkillRepository extends JpaRepository<PendingSkill, Long> {

    List<PendingSkill> findByStatus(Status status);

    boolean existsByNormalizedNameAndStatus(String normalizedName, Status status);
}