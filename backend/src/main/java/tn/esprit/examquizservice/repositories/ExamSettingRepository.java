package tn.esprit.examquizservice.repositories;

import tn.esprit.examquizservice.entities.ExamSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExamSettingRepository extends JpaRepository<ExamSetting, Long> {
	Optional<ExamSetting> findByExam_Id(Long examId);
}
