package tn.esprit.contrat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.contrat.entity.ContractPayment;

public interface ContractPaymentRepository extends JpaRepository<ContractPayment, Long> {
}
