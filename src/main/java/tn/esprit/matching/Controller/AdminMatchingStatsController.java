package tn.esprit.matching.Controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.matching.dto.AdminMatchingStatsDTO;
import tn.esprit.matching.service.AdminMatchingStatsService;

@RestController
@RequestMapping("/admin/matching")
public class AdminMatchingStatsController {

    private final AdminMatchingStatsService statsService;

    public AdminMatchingStatsController(AdminMatchingStatsService statsService) {
        this.statsService = statsService;
    }

    // 🔹 GET /admin/matching/stats
    @GetMapping("/stats")
    public AdminMatchingStatsDTO getGlobalStats() {
        return statsService.getGlobalStats();
    }
}