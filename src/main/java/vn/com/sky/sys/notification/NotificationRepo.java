package vn.com.sky.sys.notification;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

import vn.com.sky.sys.model.PartNotification;

@Repository
public interface NotificationRepo extends ReactiveCrudRepository<PartNotification, Long> {
	
}
