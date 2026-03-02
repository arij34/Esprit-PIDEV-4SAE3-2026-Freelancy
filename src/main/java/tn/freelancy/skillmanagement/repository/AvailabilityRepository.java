package tn.freelancy.skillmanagement.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.freelancy.skillmanagement.entity.Availability;
@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
}
