package com.smartfreelance.backend.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartfreelance.backend.service.KeycloakAdminService;

/**
 * Admin-only statistics endpoints.
 * Protected by SecurityConfig: /api/admin/** requires ROLE_ADMIN.
 */
@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    @Autowired
    private KeycloakAdminService keycloak;

    /**
     * Returns user statistics for the back-office dashboard.
     */
    @GetMapping("/users")
    public Map<String, Object> usersStats() {
        // Keycloak is the source of truth for stats.

        // Total users
        long totalUsers;
        try {
            totalUsers = keycloak.countUsers();
        } catch (Exception e) {
            // fallback: count by listing a large page
            totalUsers = keycloak.listUsers(200).size();
        }

        // Counts by realm role (paginated)
        long clients = countByRole("CLIENT");
        long freelancers = countByRole("FREELANCER");
        long admins = countByRole("ADMIN");

        // Active + New this month based on Keycloak user fields
        long activeUsers = 0L;
        long newThisMonth = 0L;

        ZoneId zone = ZoneId.systemDefault();
        LocalDate firstDay = LocalDate.now(zone).withDayOfMonth(1);
        Instant firstDayInstant = ZonedDateTime.of(firstDay.getYear(), firstDay.getMonthValue(), 1, 0, 0, 0, 0, zone).toInstant();
        long firstDayEpochMs = firstDayInstant.toEpochMilli();

        final int pageSize = 100;
        int first = 0;
        while (true) {
            var page = keycloak.listUsersPage(first, pageSize);
            if (page == null || page.isEmpty()) break;

            for (var u : page) {
                boolean enabled = u.get("enabled") instanceof Boolean b ? b : true;
                if (enabled) activeUsers++;

                Object created = u.get("createdTimestamp");
                long createdMs = created instanceof Number n ? n.longValue() : -1L;
                if (createdMs >= firstDayEpochMs) {
                    newThisMonth++;
                }
            }

            if (page.size() < pageSize) break;
            first += pageSize;
        }

        return Map.of(
                "totalUsers", totalUsers,
                "clients", clients,
                "freelancers", freelancers,
                "admins", admins,
                "activeUsers", activeUsers,
                "newUsersThisMonth", newThisMonth
        );
    }

    private long countByRole(String roleName) {
        long count = 0L;
        final int pageSize = 100;
        int first = 0;
        while (true) {
            var page = keycloak.listUsersByRealmRole(roleName, first, pageSize);
            if (page == null || page.isEmpty()) break;
            count += page.size();
            if (page.size() < pageSize) break;
            first += pageSize;
        }
        return count;
    }
}
