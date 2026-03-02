package tn.freelancy.skillmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.freelancy.skillmanagement.entity.FreelancerSkill;
import tn.freelancy.skillmanagement.entity.Skill;

import java.util.List;

@Repository
public interface FreelancerSkillRepository extends JpaRepository<FreelancerSkill, Long> {

    List<FreelancerSkill> findByCustomSkillName(String suggestedName);
}
