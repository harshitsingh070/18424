package backend.notification_app_be.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import backend.notification_app_be.entity.Notification;
import backend.notification_app_be.service.NotificationService;



@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    private final NotificationService service;
    
    public NotificationController(NotificationService service) {
        this.service = service;
    }
    


    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications(
            @RequestParam(name = "notification_type", required = false) String notificationType,
            @RequestParam(name = "type", required = false) String typeAlt,
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int page) {
        
        // Support both notification_type and type parameter
        String typeFilter = notificationType != null ? notificationType : typeAlt;
        
        List<Notification> notifications;
        
        if (typeFilter != null) {
            notifications = service.getByType(typeFilter);
        } else if (read != null) {
            notifications = service.getByReadStatus(read);
        } else {
            notifications = service.getAllNotifications();
        }
        
        // Pagination logic: page 0 = first limit items, page 1 = next limit items, etc
        int skip = page * limit;
        return ResponseEntity.ok(notifications.stream()
                .skip(skip)
                .limit(limit)
                .toList());
    }
    
    @GetMapping("/top/{limit}")
    public ResponseEntity<List<Notification>> getTopNotifications(@PathVariable int limit) {
        return ResponseEntity.ok(service.getTopNotifications(limit));
    }
    
    @PatchMapping("/{id}/read")
    public ResponseEntity<Notification> markRead(@PathVariable String id) {
        return ResponseEntity.ok(service.markAsRead(id));
    }



    @PostMapping("/sync")
    public ResponseEntity<String> syncNotifications() {
        try {
            service.syncNotifications();
            int count = service.getAllNotifications().size();
            return ResponseEntity.ok("Synced " + count + " notifications");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Sync failed: " + e.getMessage());
        }
    }





    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String id) {
        service.deleteNotification(id);
        return ResponseEntity.noContent().build();
    }
    
    
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Running");
    }
    
    @GetMapping("/debug/sync")
    public ResponseEntity<String> debugSync() {
        return ResponseEntity.ok("Total notifications: " + service.getAllNotifications().size());
    }
}
