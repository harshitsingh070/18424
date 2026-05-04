package backend.notification_app_be.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import backend.notification_app_be.entity.Notification;

@Service
public class NotificationService {
    
    private final RestTemplate restTemplate;
    private static final String EXTERNAL_API_URL = "http://20.207.122.201/evaluation-service/notifications";
    private final List<Notification> notificationsCache = new ArrayList<>();
    
    public NotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
  
    private int calculatePriority(String type) {
        return switch (type.toUpperCase()) {
            case "PLACEMENTT" -> 3;
            case "RESULT" -> 2;
            case "EVENT" -> 1;
            default -> 0;
        };
    }
    

    public void syncNotifications() {
        try {
            Map<String, Object> response = restTemplate.getForObject(EXTERNAL_API_URL, Map.class);
            if (response != null && response.get("notification") instanceof List<?> notifList) {
                notificationsCache.clear();
                
                for (Object notifObj : notifList) {
                    if (notifObj instanceof Map<?, ?> notifMap) {
                        String id = (String) notifMap.get("ID");
                        String message = (String) notifMap.get("Message");
                        String type = (String) notifMap.get("Type");
                        String timestamp = (String) notifMap.get("Timestamp");
                        
                        Notification notif = new Notification();
                        notif.setId(id);
                        notif.setTitle(message != null ? message : "No Title");
                        notif.setMessage(message);
                        notif.setType(type);
                        notif.setTimestamp(timestamp);
                        notif.setPriority(calculatePriority(type));
                        notif.setRead(false);
                        
                        notificationsCache.add(notif);
                        System.out.println("Loaded: " + notif.getTitle());
                    }
                }
                
              
                notificationsCache.sort((a, b) -> {
                    int priorityCompare = b.getPriority().compareTo(a.getPriority());
                    if (priorityCompare != 0) return priorityCompare;
                    return b.getTimestamp().compareTo(a.getTimestamp());
                });
            }
            System.out.println("Synced " + notificationsCache.size() + " notifications");
        } catch (Exception e) {
            System.err.println("Sync Failed: " + e.getMessage());
        }
    }
    

    public List<Notification> getAllNotifications() {
        return new ArrayList<>(notificationsCache);
    }
    
    public List<Notification> getTopNotifications(int limit) {
        return notificationsCache.stream()
                .filter(n -> !n.getRead())
                .limit(limit)
                .collect(Collectors.toList());
    }
    

    public List<Notification> getByType(String type) {
        return notificationsCache.stream()
                .filter(n -> n.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }
    
    public List<Notification> getByReadStatus(Boolean read) {
        return notificationsCache.stream()
                .filter(n -> n.getRead().equals(read))
                .collect(Collectors.toList());
    }
    
    public Notification markRead(String id) {
        return notificationsCache.stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .map(n -> {
                    n.setRead(true);
                    return n;
                })
                .orElseThrow(() -> new RuntimeException("No Notifiaction found: " + id));
    }
    
    public void deleteNotification(String id) {
        notificationsCache.removeIf(n -> n.getId().equals(id));
    }
}
