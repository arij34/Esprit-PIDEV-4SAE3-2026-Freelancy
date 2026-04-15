package tn.esprit.contrat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.contrat.entity.ContractClause;
import java.util.List;

public interface ContractClauseRepository extends JpaRepository<ContractClause, Long> {
    List<ContractClause> findByContractId(Long contractId);
}
