package se.sundsvall.checklist.configuration;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfiguration {

	public static final String EMPLOYEE_CACHE = "employee";
	public static final String COMPANY_CACHE = "company";

	@Value("${cache.employee.expire-after-write:PT12H}")
	private Duration employeeExpireAfterWriteDuration;

	@Value("${cache.mdviewer.expire-after-write:PT12H}")
	private Duration companyExpireAfterWriteDuration;

	@Bean
	@ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
	CacheManager cacheManager() {
		final var cacheManager = new SimpleCacheManager();

		cacheManager.setCaches(List.of(
			buildCache(EMPLOYEE_CACHE, employeeExpireAfterWriteDuration, 100),
			buildCache(COMPANY_CACHE, companyExpireAfterWriteDuration, 500)));

		return cacheManager;
	}

	private CaffeineCache buildCache(final String name, final Duration expireAfterWriteDuration, final int maximumSize) {
		return new CaffeineCache(name, Caffeine.newBuilder()
			.expireAfterWrite(expireAfterWriteDuration)
			.maximumSize(maximumSize)
			.build());
	}
}
