package tn.esprit.contrat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.contrat.entity.ContractMilestone;

@Repository
public interface ContractMilestoneRepository extends JpaRepository<ContractMilestone, Long> {
}
