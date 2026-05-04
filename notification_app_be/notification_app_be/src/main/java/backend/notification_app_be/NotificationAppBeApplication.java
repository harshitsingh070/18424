package backend.notification_app_be;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import backend.notification_app_be.service.NotificationService;

@SpringBootApplication
public class NotificationAppBeApplication {

	public static void main(String[] args) {
		ApplicationContext context = SpringApplication.run(NotificationAppBeApplication.class, args);
		
		NotificationService notificationService = context.getBean(NotificationService.class);
		System.out.println("Starting");
		notificationService.syncNotifications();
		System.out.println("Notification are listed ");
	}

}
