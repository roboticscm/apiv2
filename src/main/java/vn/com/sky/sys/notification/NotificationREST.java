package vn.com.sky.sys.notification;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message.RecipientType;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.com.sky.base.GenericREST;
import vn.com.sky.sys.model.HumanOrOrg;
import vn.com.sky.sys.model.PartNotification;
import vn.com.sky.util.MyServerResponse;

@Configuration
@AllArgsConstructor
public class NotificationREST extends GenericREST {
    private CustomNotificationRepo customRepo;
    private NotificationRepo mainRepo;
    private JavaMailSender javaMailSender;
    
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
            	System.out.println(req);
            	return Flux.fromIterable(req.getToHumanListIds())
            		.flatMap(toHumanId -> {
            			try {
//							sendEmail(request, "roboticscm2018@gmail.com", req);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
            			req.setToHumanId(toHumanId);
            			return saveEntity(mainRepo, req, getUserId(request));//.then(sendEmail(request, "roboticscm2018@gmail.com", req));
            		}).collectList().flatMap(res -> ok(res, List.class));
            	
            });
    }
    
    
//    private Mono<Boolean> sendEmail(ServerRequest request, String email, PartNotification notify) throws Exception {
//        var appUrl = request.uri().toString();
//        var url = appUrl.replaceAll(request.uri().getPath(), "") + "/" + notify.getMenuPath().replaceAll("/", "--");
//        
//        System.out.println("xxxx: " + url);
//        
//        Properties props = new Properties();
//        
//        
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.debug", "true");
//        
//        props.put("mail.smtp.host", "smtp.mailtrap.io");
//        props.put("mail.smtp.port", "25");
//        
//        
//        Session session = Session.getInstance(props, new Authenticator() {
//        	@Override
//        	protected PasswordAuthentication getPasswordAuthentication() {
//        		return new PasswordAuthentication("7c661cf4ee24c8", "83503323dacd51");
//        	    }
//        });
//        
//        try {
//        	var msg = new MimeMessage(session);
//        	
//            msg.setFrom(new InternetAddress("roboticscm2018@gmail.com"));
//            msg.setRecipient(RecipientType.TO, new InternetAddress("roboticscm2018@gmail.com"));
//            
//            msg.setSubject(notify.getTitle());
//            msg.setText(
//                "For detail, click the link below:\n" + url + "?token="
//            );
//
//            Transport.send(msg);
//            
//            return Mono.just(true);
//        } catch (MailException e) {
//            e.printStackTrace();
//        }
//
//        return Mono.just(false);
//    }
    
    
    
//    private Mono<Boolean> sendEmail(ServerRequest request, String email, PartNotification notify) throws Exception {
//        var appUrl = request.uri().toString();
//        var url = appUrl.replaceAll(request.uri().getPath(), "") + "/" + notify.getMenuPath().replaceAll("/", "--");
//        
//        System.out.println("xxxx: " + url);
//        
//        Properties props = new Properties();
//        
//        
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.debug", "true");
//        props.put("mail.smtp.ssl.trust", "172.16.22.111");        
//        props.put("mail.smtp.host", "172.16.22.111");
//        props.put("mail.smtp.port", "25");
//        
//        
//        Session session = Session.getInstance(props, new Authenticator() {
//        	@Override
//        	protected PasswordAuthentication getPasswordAuthentication() {
//        		return new PasswordAuthentication("suntech\\khai.lv", "AaBb12345678@");
//        	    }
//        });
//        
//        try {
//        	var msg = new MimeMessage(session);
//        	
//            msg.setFrom(new InternetAddress("khai.lv@suntech.com.vn"));
//            msg.setRecipient(RecipientType.TO, new InternetAddress("roboticscm2018@gmail.com"));
//            
//            msg.setSubject(notify.getTitle());
//            msg.setText(
//                "For detail, click the link below:\n" + url + "?token="
//            );
//
//            Transport.send(msg);
//            
//            return Mono.just(true);
//        } catch (MailException e) {
//            e.printStackTrace();
//        }
//
//        return Mono.just(false);
//    }
    
    
//    private Mono<Boolean> sendEmail(ServerRequest request, String email, PartNotification notify) throws Exception {
//        var appUrl = request.uri().toString();
//        var url = appUrl.replaceAll(request.uri().getPath(), "") + "/" + notify.getMenuPath().replaceAll("/", "--");
//        
//        System.out.println("xxxx: " + url);
//        
//        Properties props = new Properties();
//        
//        
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.debug", "true");
//        
//        props.put("mail.smtp.host", "smtp.gmail.com");
//        props.put("mail.smtp.port", "587");
//        
//        
//        Session session = Session.getInstance(props, new Authenticator() {
//        	@Override
//        	protected PasswordAuthentication getPasswordAuthentication() {
//        		return new PasswordAuthentication("roboticscm2018@gmail.com", "damdoi2018");
//        	    }
//        });
//        
//        try {
//        	var msg = new MimeMessage(session);
//        	
//            msg.setFrom(new InternetAddress("roboticscm2018@gmail.com"));
//            msg.setRecipient(RecipientType.TO, new InternetAddress("roboticscm2018@gmail.com"));
//            
//            msg.setSubject(notify.getTitle());
//            msg.setText(
//                "For detail, click the link below:\n" + url + "?token="
//            );
//
//            Transport.send(msg);
//            
//            return Mono.just(true);
//        } catch (MailException e) {
//            e.printStackTrace();
//        }
//
//        return Mono.just(false);
//    }
    
    private Mono<Boolean> sendEmail(ServerRequest request, String email, PartNotification notify) {
        var appUrl = request.uri().toString();
        var url = appUrl.replaceAll(request.uri().getPath(), "") + "/" + notify.getMenuPath().replaceAll("/", "--");
        
        System.out.println("xxxx: " + url);
        
        try {
        	var msg = new SimpleMailMessage();
            msg.setTo(email);
            msg.setSubject(notify.getTitle());
            msg.setText(
                "For detail, click the link below:\n" + url + "?token="
            );

            javaMailSender.send(msg);
            return Mono.just(true);
        } catch (MailException e) {
            e.printStackTrace();
        }

        return Mono.just(false);
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
