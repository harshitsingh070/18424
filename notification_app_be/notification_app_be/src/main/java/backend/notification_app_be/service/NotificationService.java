package backend.notification_app_be.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import backend.notification_app_be.entity.Notification;

@Service
public class NotificationService {
    
    private final RestTemplate restTemplate;
    
    @Value("${api.external.url}")
    private String externalApiUrl;
    
    @Value("${api.external.token:}")
    private String apiToken;
    
    private final List<Notification> notificationsCache = new ArrayList<>();
    
    public NotificationService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    
    private int calculatePriority(String type) {
        return switch (type.toUpperCase()) {
            case "PLACEMENT" -> 3;
            case "RESULT" -> 2;
            case "EVENT" -> 1;
            default -> 0;
        };
    }
    
   
    public void syncNotifications() {
        try {
            System.out.println("Starting sync from: " + externalApiUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            headers.set("Accept", "application/json");
            
            if (apiToken != null && !apiToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + apiToken);
                System.out.println("Token added");
            }
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            System.out.println("Making request...");
            ResponseEntity<Map> response = restTemplate.exchange(externalApiUrl, HttpMethod.GET, entity, Map.class);
            
            System.out.println("Response code: " + response.getStatusCode());
            Map<String, Object> body = response.getBody();
            
            if (body == null) {
                System.out.println("Response body is null");
                return;
            }
            
            System.out.println("Response body keys: " + body.keySet());
            
            List<?> notifList = null;
            if (body.containsKey("data")) {
                Object data = body.get("data");
                System.out.println("Found 'data' key, type: " + (data != null ? data.getClass().getName() : "null"));
                if (data instanceof List<?>) {
                    notifList = (List<?>) data;
                }
            }
            
            if (notifList == null) {
                System.out.println("Trying alternative keys...");
                notifList = (List<?>) body.get("notifications");
            }
            if (notifList == null) {
                notifList = (List<?>) body.get("results");
            }
            
            if (notifList != null && !notifList.isEmpty()) {
                System.out.println("Processing " + notifList.size() + " notifications");
                notificationsCache.clear();
                
                for (Object notifObj : notifList) {
                    if (notifObj instanceof Map<?, ?>) {
                        Map<?, ?> notifMap = (Map<?, ?>) notifObj;
                        String id = String.valueOf(notifMap.get("ID") != null ? notifMap.get("ID") : notifMap.get("id"));
                        String message = String.valueOf(notifMap.get("Message") != null ? notifMap.get("Message") : notifMap.get("message"));
                        String type = String.valueOf(notifMap.get("Type") != null ? notifMap.get("Type") : notifMap.get("type"));
                        String timestamp = String.valueOf(notifMap.get("Timestamp") != null ? notifMap.get("Timestamp") : notifMap.get("timestamp"));
                        
                        Notification notif = new Notification();
                        notif.setId(id);
                        notif.setTitle(message);
                        notif.setMessage(message);
                        notif.setType(type);
                        notif.setTimestamp(timestamp);
                        notif.setPriority(calculatePriority(type));
                        notif.setRead(false);
                        
                        notificationsCache.add(notif);
                    }
                }
                
                notificationsCache.sort((a, b) -> {
                    int priorityCompare = b.getPriority().compareTo(a.getPriority());
                    if (priorityCompare != 0) return priorityCompare;
                    return b.getTimestamp().compareTo(a.getTimestamp());
                });
                
                System.out.println("Synced " + notificationsCache.size() + " notifications");
            } else {
                System.out.println("No notifications found in response");
            }
            
        } catch (Exception e) {
            System.out.println("Sync error: " + e.getMessage());
            e.printStackTrace();
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
    
  
    public Notification markAsRead(String id) {
        return notificationsCache.stream()
                .filter(n -> n.getId().equals(id))
                .findFirst()
                .map(n -> {
                    n.setRead(true);
                    return n;
                })
                .orElseThrow(() -> new RuntimeException("Nothng is found: " + id));
    }
    
    
    public void deleteNotification(String id) {
        notificationsCache.removeIf(n -> n.getId().equals(id));
    }
}
