/**
 *
 */
package vn.com.sky.mail;

/**
 * @author roboticscm2018@gmail.com (khai.lv)
 * Created date: Apr 18, 2019
 */
import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

@Configuration
public class MailConfig {
	@Value("${suntech.mail.host}")
	private String host;
	
	@Value("${suntech.mail.port}")
	private int port;
	
	@Value("${suntech.mail.username}")
	private String username;
	
	@Value("${suntech.mail.password}")
	private String password;

    @Bean
    public JavaMailSender getJavaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        mailSender.setUsername(username);
        mailSender.setPassword(password);

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.debug", "true");

        
        return mailSender;
    }
}
