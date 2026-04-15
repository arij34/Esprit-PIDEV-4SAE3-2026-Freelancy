package tn.esprit.matching.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional; // ✅ ajouter
import tn.esprit.matching.entity.Matching;

import java.util.List;

public interface MatchingRepository extends JpaRepository<Matching, Long> {

    List<Matching> findByFreelancerId(Long freelancerId);

    List<Matching> findByProjectId(Long projectId);

    Matching findByFreelancerIdAndProjectId(Long freelancerId, Long projectId);
    List<Matching> findAllByOrderByProjectIdAscFreelancerIdAsc();



}