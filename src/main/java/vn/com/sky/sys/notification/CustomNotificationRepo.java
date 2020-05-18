package vn.com.sky.sys.notification;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import vn.com.sky.base.BaseR2dbcRepository;

@Repository
public class CustomNotificationRepo extends BaseR2dbcRepository {

    /*
	-- Module: System (sys)
	-- Section: Notification
	-- Function Description: Find notifications list by user id and filter
	-- Params:
	--  userId
	--  textSearch
	*/
    public Mono<String> findNotifications(Long userId, String textSearch) {
        String methodName = new Object() {}.getClass().getEnclosingMethod().getName();

        var ret =
            this.databaseClient()
                .execute(genSql(methodName, "userId", "textSearch"))
                .bind("userId", userId);

        if (textSearch == null)
        	ret = ret.bindNull("textSearch", String.class);
        else 
        	ret = ret.bind("textSearch", textSearch);

        return ret.as(String.class).fetch().first();
    }
}
