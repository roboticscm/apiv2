//package vn.com.sky.redis;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
//import org.springframework.data.redis.core.ReactiveRedisOperations;
//import org.springframework.data.redis.core.ReactiveRedisTemplate;
//import org.springframework.data.redis.listener.ChannelTopic;
//import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
//import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.RedisSerializationContext;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
///**
// *
// */
//
///**
// * @author roboticscm2018@gmail.com (khai.lv)
// * Created date: Apr 3, 2019
// */
//@Configuration
////@EnableRedisWebSession
//public class RedisConfiguration {
//	@Bean
//    ChannelTopic channelTopic() {
//        return new ChannelTopic("skyone-redis:queue");
//    }
//
//    @Bean
//    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
//        return new ReactiveRedisTemplate<>(factory, RedisSerializationContext.string());
//    }
//
//    @Bean
//    ReactiveRedisMessageListenerContainer container(ReactiveRedisConnectionFactory factory) {
//
//        ReactiveRedisMessageListenerContainer container = new ReactiveRedisMessageListenerContainer(factory);
//        container.receive(channelTopic());
//
//        return container;
//    }
//
//    @Bean
//    ReactiveRedisOperations<String, LoginInfo> redisOperations(ReactiveRedisConnectionFactory factory) {
//        Jackson2JsonRedisSerializer<LoginInfo> serializer = new Jackson2JsonRedisSerializer<>(LoginInfo.class);
//
//        RedisSerializationContext.RedisSerializationContextBuilder<String, LoginInfo> builder =
//                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());
//
//        RedisSerializationContext<String, LoginInfo> context = builder.value(serializer).build();
//
//        return new ReactiveRedisTemplate<>(factory, context);
//    }
//
////    @Bean
////    public LettuceConnectionFactory redisConnectionFactory() {
////        return new LettuceConnectionFactory();
////    }
//}
