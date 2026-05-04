package backend.notification_app_be.entity;

public class Notification {
    
    private String id;
    private String title;
    private String message;
    private String type;
    private Boolean read;
    private String timestamp;
    private Integer priority;
    
    public Notification() {
    }
    
    public Notification(String id, String title, String message, String type, Boolean read, String timestamp, Integer priority) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = read;
        this.timestamp = timestamp;
        this.priority = priority;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public Boolean getRead() {
        return read;
    }
    
    public void setRead(Boolean read) {
        this.read = read;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
