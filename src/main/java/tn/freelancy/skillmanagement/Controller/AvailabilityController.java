package tn.freelancy.skillmanagement.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tn.freelancy.skillmanagement.entity.Availability;
import tn.freelancy.skillmanagement.service.AvailabilityService;

import java.util.List;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    @Autowired
    private AvailabilityService availabilityService;

    @PostMapping("/user/{userId}")
    public Availability create
            (
            @PathVariable Long userId,
            @RequestBody Availability availability) {
        return availabilityService.createAvailability(userId,availability);
    }

    @GetMapping
    public List<Availability> getAll() {
        return availabilityService.getAllAvailabilities();
    }

    @GetMapping("/{id}")
    public Availability getById(@PathVariable Long id) {
        return availabilityService.getAvailabilityById(id);
    }

    @PutMapping("/{id}")                                    // ← ajouter /{id}
    public Availability update(@PathVariable Long id ,@RequestBody Availability availability) {
        return availabilityService.updateAvailability(id,availability);
    }

    @PostMapping("/preview")
    public Availability preview(@RequestBody Availability availability) {
        return availabilityService.calculatePreview(availability);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        availabilityService.deleteAvailability(id);
    }
}
