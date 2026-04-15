package tn.esprit.contrat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.contrat.entity.ContractHistory;

import java.util.List;

@Repository
public interface ContractHistoryRepository extends JpaRepository<ContractHistory, Long> {

    List<ContractHistory> findByContractIdOrderByPerformedAtDesc(Long contractId);
}
