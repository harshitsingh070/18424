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
@CrossOrigin(origins = "http://localhost:3000")
public class NotificationController {
    
    private final NotificationService service;
    
    public NotificationController(NotificationService service) {
        this.service = service;
    }
    


    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Boolean read,
            @RequestParam(defaultValue = "100") int limit) {
        
        List<Notification> notifications;
        
        if (type != null) {
            notifications = service.getByType(type);
        } else if (read != null) {
            notifications = service.getByReadStatus(read);
        } else {
            notifications = service.getAllNotifications();
        }
        
        return ResponseEntity.ok(notifications.stream().limit(limit).toList());
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
        service.syncNotifications();
        return ResponseEntity.ok("Sync Completed");
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
}
