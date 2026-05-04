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
            System.out.println("Trying to fetch: " + externalApiUrl);
            
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");
            
           
            if (apiToken != null && !apiToken.isEmpty()) {
                headers.set("Authorization", "Bearer " + apiToken);
                System.out.println("Using Authorization token");
            } else {
                System.out.println("Warning");
            }
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<Map> response = restTemplate.exchange(externalApiUrl, HttpMethod.GET, entity, Map.class);
            
            System.out.println("API  Response: " + response.getStatusCode());
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                System.out.println("Response keys: " + body.keySet());
                
             
                List<?> notifList = (List<?>) body.get("notifications");
                if (notifList == null) {
                    notifList = (List<?>) body.get("data");
                }
                if (notifList == null) {
                    notifList = (List<?>) body.get("results");
                }
                
                if (notifList != null && !notifList.isEmpty()) {
                    notificationsCache.clear();
                    System.out.println("Found " + notifList.size() + " notifications");
                    
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
                    
                    System.out.println("SUCCESS   Synced " + notificationsCache.size() + " real notifications");
                    return;
                }
            }
            
            System.out.println("Warning-> No data from API");
            
        } catch (Exception e) {
            System.err.println("ERROR - API: " + e.getMessage());
            System.out.println("Paste the token in the Yaml file");
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
