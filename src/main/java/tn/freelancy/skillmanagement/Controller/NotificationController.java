package tn.freelancy.skillmanagement.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.freelancy.skillmanagement.dto.NotificationDTO;
import tn.freelancy.skillmanagement.service.NotificationService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ── ADMIN ──────────────────────────────────────────────────────

    /** Toutes les notifs admin */
    @GetMapping("/admin")
    public ResponseEntity<List<NotificationDTO>> getAdminNotifications() {
        return ResponseEntity.ok(notificationService.getAdminNotifications());
    }

    /** Badge compteur non lu admin */
    @GetMapping("/admin/unread-count")
    public ResponseEntity<Map<String, Long>> getAdminUnreadCount() {
        return ResponseEntity.ok(
                Map.of("count", notificationService.getAdminUnreadCount())
        );
    }

    /** Marquer toutes les notifs admin comme lues */
    @PutMapping("/admin/mark-all-read")
    public ResponseEntity<Void> markAdminAllRead() {
        notificationService.markAdminAllRead();
        return ResponseEntity.ok().build();
    }

    // ── FREELANCER (frontoffice) ───────────────────────────────────

    /** Toutes les notifs d'un freelancer */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(
            @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    /** Badge compteur non lu freelancer */
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Map<String, Long>> getUserUnreadCount(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                Map.of("count", notificationService.getUserUnreadCount(userId))
        );
    }

    /** Marquer toutes les notifs d'un freelancer comme lues */
    @PutMapping("/user/{userId}/mark-all-read")
    public ResponseEntity<Void> markUserAllRead(@PathVariable Long userId) {
        notificationService.markUserAllRead(userId);
        return ResponseEntity.ok().build();
    }

    // ── COMMUN ────────────────────────────────────────────────────

    /** Marquer une notification précise comme lue */
    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markOneAsRead(@PathVariable Long id) {
        notificationService.markOneAsRead(id);
        return ResponseEntity.ok().build();
    }
}