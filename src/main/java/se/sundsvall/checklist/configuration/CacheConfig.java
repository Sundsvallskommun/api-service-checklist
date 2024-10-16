package se.sundsvall.checklist.configuration;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class CacheConfig {

	public static final String EMPLOYEE_CACHE = "employee";

	@Value("${cache.employee.expire-after-write:PT12H}")
	private Duration expireAfterWriteDuration;

	public Caffeine<Object, Object> caffeineConfig() {
		return Caffeine.newBuilder()
			.expireAfterWrite(expireAfterWriteDuration)
			.initialCapacity(100);
	}

	@Bean
	@ConditionalOnProperty(name = "cache.employee.enabled", havingValue = "true")
	public CacheManager cacheManager() {
		CaffeineCacheManager caffeineCacheManager = new CaffeineCacheManager();
		caffeineCacheManager.setCaffeine(caffeineConfig());
		caffeineCacheManager.setCacheNames(List.of(EMPLOYEE_CACHE));
		return caffeineCacheManager;
	}
}
