//package com.a404.duckonback.config;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.redis.connection.RedisConnectionFactory;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
//import org.springframework.data.redis.serializer.StringRedisSerializer;
//
//@Configuration
//public class RedisConfig {
//
//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
//
//        RedisTemplate<String, Object> template = new RedisTemplate<>();
//        template.setConnectionFactory(connectionFactory);
//
//        template.setKeySerializer(new StringRedisSerializer());
//        template.setHashKeySerializer(new StringRedisSerializer());
//
//        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer();
//
//        template.setValueSerializer(serializer);
//        template.setHashValueSerializer(serializer);
//
//        template.afterPropertiesSet();
//        return template;
//    }
//}
//

package com.a404.duckonback.config;

import com.a404.duckonback.dto.LiveRoomDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    // 방 상태 전용: room:{roomId} -> LiveRoomDTO (단일 값)
    @Bean
    public RedisTemplate<String, LiveRoomDTO> roomTemplate(
            RedisConnectionFactory connectionFactory,
            ObjectMapper objectMapper   // 전역 objectMapper 주입
    ) {
        var valueSer = new Jackson2JsonRedisSerializer<>(objectMapper, LiveRoomDTO.class);

        var template = new RedisTemplate<String, LiveRoomDTO>();
        template.setConnectionFactory(connectionFactory);

        var stringSer = new StringRedisSerializer();
        template.setKeySerializer(stringSer);
        template.setValueSerializer(valueSer);
        template.setHashKeySerializer(stringSer);
        template.setHashValueSerializer(valueSer);

        template.afterPropertiesSet();
        return template;
    }
}
