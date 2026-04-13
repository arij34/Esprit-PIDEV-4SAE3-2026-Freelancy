package tn.esprit.planningg.repositories;

import org.springframework.data.repository.CrudRepository;
import tn.esprit.planningg.entities.Planning;
import org.springframework.stereotype.Repository;

public interface PlanningRepository extends CrudRepository<Planning, Long> {
}
