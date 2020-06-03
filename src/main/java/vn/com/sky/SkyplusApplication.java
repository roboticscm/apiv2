package vn.com.sky;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

//import springfox.documentation.swagger2.annotations.EnableSwagger2WebFlux;
import vn.com.sky.task.services.ReminderService;

@SpringBootApplication
//@EnableSwagger2WebFlux
public class SkyplusApplication {
	private static ApplicationContext applicationContext;

    public static void main(String[] args) {
    	applicationContext = SpringApplication.run(SkyplusApplication.class, args);
        var reminderService = applicationContext.getBean(ReminderService.class);
        reminderService.run();
    }
}
