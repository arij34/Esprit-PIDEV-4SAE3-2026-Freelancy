package tn.freelancy.skillmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.freelancy.skillmanagement.entity.FreelancerSkill;
import tn.freelancy.skillmanagement.entity.Skill;
import tn.freelancy.skillmanagement.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
public interface FreelancerSkillRepository extends JpaRepository<FreelancerSkill, Long> {

    List<FreelancerSkill> findByCustomSkillName(String suggestedName);
    void deleteByUser(User user);

    @Query("SELECT fs FROM FreelancerSkill fs WHERE fs.user.id = :userId")
    List<FreelancerSkill> findByUserId(@Param("userId") Long userId);

    // Version directe pour éviter le stream dans CvService
    @Query("SELECT fs.skill.idS FROM FreelancerSkill fs WHERE fs.user.id = :userId")
    Set<Long> findSkillIdsByUserId(@Param("userId") Long userId);

    @Query("SELECT fs.skill.idS, COUNT(fs) FROM FreelancerSkill fs WHERE fs.user.id = :freelancerId GROUP BY fs.skill.idS HAVING COUNT(fs) > 1")
    List<Object[]> findDuplicateSkillIds(Long freelancerId);

    List<FreelancerSkill> findByUser_IdAndSkill_IdSIn(
            Long userId,
            List<Long> skillIdS
    );

    boolean existsByUserIdAndSkillIdS(Long userId, Long skillId);

    boolean existsByUserIdAndCustomSkillNameIgnoreCase(Long userId, String customSkillName);
}

