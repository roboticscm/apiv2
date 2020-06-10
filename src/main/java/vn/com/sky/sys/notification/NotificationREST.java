package vn.com.sky.sys.notification;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.com.sky.base.GenericREST;
import vn.com.sky.sys.model.PartNotification;
import vn.com.sky.util.MyServerResponse;
import vn.com.sky.util.StringUtil;

@Configuration
public class NotificationREST extends GenericREST {
	@Autowired
    private CustomNotificationRepo customRepo;
    @Autowired
    private NotificationRepo mainRepo;
    @Autowired
    private JavaMailSender javaMailSender;
    
    @Value("${suntech.frontend.domain}")
	private String frontendDomain;
    
    @Bean
    public RouterFunction<?> notificationRoutes() {
        return route(GET(buildURL("notification", this::findNotifications)), this::findNotifications)
        	.andRoute(PUT(buildURL("notification", this::update)), this::update)
            .andRoute(POST(buildURL("notification", this::save)), this::save);
    }

    private Mono<ServerResponse> findNotifications(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE
        var textSearch = getParam(request, "textSearch");
        var type = getParam(request, "type");
        return customRepo
                .findNotifications(getUserId(request), type, textSearch)
                .flatMap(json -> {
                	return ok(json);
                	});
    }

  
    private Mono<ServerResponse> save(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        return request
            .bodyToMono(PartNotification.class)
            .flatMap(req -> {
            	return Flux.fromIterable(req.getToHumanListIds())
            		.flatMap(toHumanId -> {
            			
            			sendEmail(request, "roboticscm2018@gmail.com", req);
            			
            			req.setToHumanId(toHumanId);
            			return saveEntity(mainRepo, req, getUserId(request));//.then(sendEmail(request, "roboticscm2018@gmail.com", req));
            		}).collectList().flatMap(res -> ok(res, List.class));
            	
            });
    }
    
    
    
    public void sendEmail(ServerRequest request, String toEmail, PartNotification notify) {
    	try {
    		var thread = new Thread() {
        		public void run(){
        	        var url = frontendDomain + "/" + notify.getMenuPath().replaceAll("/", "--");
        	        
        	        System.out.println("xxxx: " + url);
        	        
        	        try {
        	        	var msg = new SimpleMailMessage();
        	            msg.setTo(toEmail);
        	            msg.setSubject(StringUtil.html2Text(notify.getTitle()));
        	            msg.setText(
        	                "For detail, click the link below:\n" + url + "?depId=" + notify.getDepartmentId() + "&id=" + notify.getTargetId()
        	            );

        	            javaMailSender.send(msg);
        	        } catch (MailException e) {
        	            e.printStackTrace();
        	        }
        		}
        	};
        	
        	thread.start();
    	} catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    }
    
    
    private Mono<ServerResponse> update(ServerRequest request) {
        // SYSTEM BLOCK CODE
        // PLEASE DO NOT EDIT
        if (request == null) {
            String methodName = new Object() {}.getClass().getEnclosingMethod().getName();
            return Mono.just(new MyServerResponse(methodName));
        }
        // END SYSTEM BLOCK CODE

        return request
            .bodyToMono(PartNotification.class)
            .flatMap(req -> {
            	return mainRepo.findById(req.getId())
            			.flatMap(found -> {
            				if(req.getIsRead() != null) {
            					found.setIsRead(req.getIsRead());
            				}
            				
            				if(req.getIsFinished() != null) {
            					found.setIsFinished(req.getIsFinished());
            				}
            				return updateEntity(mainRepo, found, getUserId(request)).flatMap(res -> ok(res, PartNotification.class));
            			});
            	
            });
    }
}
