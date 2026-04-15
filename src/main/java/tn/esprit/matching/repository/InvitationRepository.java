package tn.esprit.matching.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.matching.entity.Invitation;
import tn.esprit.matching.entity.InvitationStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    List<Invitation> findByFreelancerId(Long freelancerId);
    List<Invitation> findByFreelancerIdAndStatus(Long freelancerId, InvitationStatus status);
    long countByFreelancerIdAndStatus(Long freelancerId, InvitationStatus status);
    boolean existsByProjectIdAndFreelancerId(Long projectId, Long freelancerId);
    List<Invitation> findByStatusAndTrashedAtBefore(InvitationStatus status, LocalDateTime date);
    List<Invitation> findAllByOrderByCreatedAtDesc();
    List<Invitation> findByProjectId(Long projectId);

}