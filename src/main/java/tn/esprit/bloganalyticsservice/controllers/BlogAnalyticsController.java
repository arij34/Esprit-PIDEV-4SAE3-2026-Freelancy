package tn.esprit.bloganalyticsservice.controllers;

import org.springframework.web.bind.annotation.*;
import tn.esprit.bloganalyticsservice.entities.BlogAnalytics;
import tn.esprit.bloganalyticsservice.dto.BlogStat;
import tn.esprit.bloganalyticsservice.services.IBlogAnalyticsService;

import java.util.List;

@RestController
@RequestMapping("/analytics")
@CrossOrigin(origins = "http://localhost:4200")
public class BlogAnalyticsController {

    private final IBlogAnalyticsService service;

    public BlogAnalyticsController(IBlogAnalyticsService service) {
        this.service = service;
    }

    @PostMapping("/upsert")
    public BlogAnalytics upsert(@RequestParam String metric, @RequestParam Long value) {
        return service.upsert(metric, value);
    }

    @GetMapping("/all")
    public List<BlogAnalytics> getAll() {
        return service.getAll();
    }

    @GetMapping("/stat/{metric}")
    public BlogStat getStat(@PathVariable String metric) {
        return service.getStat(metric);
    }
}