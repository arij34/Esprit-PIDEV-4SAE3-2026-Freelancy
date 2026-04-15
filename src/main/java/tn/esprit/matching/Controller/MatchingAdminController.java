package tn.esprit.matching.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tn.esprit.matching.dto.AdminMatchingRowDTO;
import tn.esprit.matching.service.MatchingService;

import java.util.List;

@RestController
@RequestMapping("/admin/matching")
public class MatchingAdminController {

    @Autowired
    private MatchingService matchingService;

    @GetMapping
    public List<AdminMatchingRowDTO> getAllMatchings() {
        return matchingService.getAllMatchingsForAdmin();
    }
}