package tn.esprit.planningg.repositories;

import org.springframework.data.repository.CrudRepository;
import tn.esprit.planningg.entities.Task;
import tn.esprit.planningg.entities.Statut;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository  extends CrudRepository<Task, Long> {
	List<Task> findByDateFinBeforeAndStatutNot(LocalDateTime dateFin, Statut statut);
	List<Task> findByPlanning_Id(Long planningId);
}
