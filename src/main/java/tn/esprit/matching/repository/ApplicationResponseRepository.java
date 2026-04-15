package tn.esprit.matching.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.matching.entity.ApplicationResponse;

import java.util.Optional;

public interface ApplicationResponseRepository extends JpaRepository<ApplicationResponse, Long> {
    ApplicationResponse findFirstByInvitationId(Long invitationId);
    Optional<ApplicationResponse> findByInvitationId(Long invitationId);


}
