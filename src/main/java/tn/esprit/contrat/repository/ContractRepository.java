package tn.esprit.contrat.repository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tn.esprit.contrat.entity.Contract;
import tn.esprit.contrat.entity.ContractStatus;

import java.util.List;

@Repository
public interface ContractRepository extends JpaRepository<Contract, Long> {

    // ─── Vérifier si un contrat existe déjà pour une proposal ────────────────
    boolean existsByProposalId(Long proposalId);

    // ─── Par projet ───────────────────────────────────────────────────────────
    List<Contract> findByProjectId(Long projectId);

    // ─── Par statut ───────────────────────────────────────────────────────────
    List<Contract> findByStatus(ContractStatus status);

    // ─── Contrats du client (userId) ─────────────────────────────────────────
    List<Contract> findByClientId(Long clientId);

    // ─── Contrats du freelancer ───────────────────────────────────────────────
    List<Contract> findByFreelancerId(Long freelancerId);

    // ─── Contrats d'un utilisateur (client OU freelancer) ────────────────────
    @Query("SELECT c FROM Contract c WHERE c.clientId = :userId OR c.freelancerId = :userId")
    List<Contract> findByUserId(@Param("userId") Long userId);

    // ─── Récupérer un contrat avec ses clauses seulement (pas de MultipleBagFetchException) ──
    @Query("SELECT DISTINCT c FROM Contract c LEFT JOIN FETCH c.clauses WHERE c.id = :id")
    Optional<Contract> findByIdWithClauses(@Param("id") Long id);

    // ─── Récupérer tous les contrats avec clauses seulement ──────────────────────
    // NOTE: on ne fetch pas milestones ici pour éviter MultipleBagFetchException
    // Les milestones sont chargés séparément via findByIdWithMilestones
    @Query("SELECT DISTINCT c FROM Contract c LEFT JOIN FETCH c.clauses ORDER BY c.id")
    List<Contract> findAllWithClauses();

    // ─── Récupérer tous les contrats avec milestones seulement ───────────────────
    @Query("SELECT DISTINCT c FROM Contract c LEFT JOIN FETCH c.milestones ORDER BY c.id")
    List<Contract> findAllWithMilestones();

    // ─── Récupérer un contrat avec milestones seulement ──────────────────────────
    @Query("SELECT DISTINCT c FROM Contract c LEFT JOIN FETCH c.milestones WHERE c.id = :id")
    Optional<Contract> findByIdWithMilestones(@Param("id") Long id);

    // ─── Find by DocuSign envelope ID ──────────────────────────────────────────
    Optional<Contract> findByEnvelopeId(String envelopeId);
}