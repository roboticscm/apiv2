///**
// *
// */
//package vn.com.sky.redis;
//
//import org.springframework.data.redis.core.ReactiveRedisOperations;
//import org.springframework.stereotype.Service;
//
//import lombok.AllArgsConstructor;
//import reactor.core.publisher.Flux;
//
///**
// * @author roboticscm2018@gmail.com (khai.lv)
// * Created date: Apr 3, 2019
// */
//
//@Service
//@AllArgsConstructor
//public class LoginInfoService {
//	private final ReactiveRedisOperations<String, LoginInfo> loginInfoOps;
//
//    public void save(LoginInfo loginInfo) {
//    	loginInfoOps.opsForValue().set(loginInfo.getToken(), loginInfo).subscribe();
//    }
//
//    public Flux<LoginInfo> getCurrent(String key) {
//    	return loginInfoOps.keys(key).flatMap(loginInfoOps.opsForValue()::get);
//    }
//}
