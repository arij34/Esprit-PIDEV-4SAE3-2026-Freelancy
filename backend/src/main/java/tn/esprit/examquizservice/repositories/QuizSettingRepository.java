package tn.esprit.examquizservice.repositories;

import tn.esprit.examquizservice.entities.QuizSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizSettingRepository extends JpaRepository<QuizSetting, Long> {
}
