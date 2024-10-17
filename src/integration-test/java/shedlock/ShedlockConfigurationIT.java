package shedlock;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;

import net.javacrumbs.shedlock.core.LockConfiguration;
import net.javacrumbs.shedlock.core.LockProvider;
import se.sundsvall.checklist.Application;

@ActiveProfiles("it")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT, classes = Application.class, properties = {
	"spring.main.banner-mode=off",
	"logging.level.se.sundsvall.dept44.payload=OFF",
	"wiremock.server.port=10101"
})
class ShedlockConfigurationIT { // Needs to be runned in IT-phase as we need flyway to be enabled to verify shedlock mechanism

	@Autowired
	private LockProvider lockProvider;

	@Test
	void testAutowiring() {
		assertThat(lockProvider).isNotNull();
	}

	@Test
	void testLockMechanism() {
		final var lockConfiguration = new LockConfiguration(Instant.now(), "lockName", Duration.ofSeconds(30), Duration.ofSeconds(0));
		final var lock = lockProvider.lock(lockConfiguration);

		assertThat(lock).isPresent();
		assertThat(lockProvider.lock(lockConfiguration)).isNotPresent();

		lock.get().unlock();
	}
}
