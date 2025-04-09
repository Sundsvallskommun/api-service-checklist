package se.sundsvall.checklist.service.scheduler;

import static java.time.temporal.ChronoUnit.DAYS;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.checklist.integration.db.repository.InitiationRepository;

@ExtendWith(MockitoExtension.class)
class PurgeOldInitiationInfoSchedulerTest {

	private static final int THRESHOLD_IN_DAYS = 123;

	@Mock
	private InitiationRepository initiationRepositoryMock;

	private PurgeOldInitiationInfoScheduler scheduler;

	@BeforeEach
	void setup() {
		scheduler = new PurgeOldInitiationInfoScheduler(initiationRepositoryMock, THRESHOLD_IN_DAYS);
	}

	@Test
	void execute() {
		// Act
		scheduler.execute();

		// Assert and verify
		verify(initiationRepositoryMock).deleteAllByCreatedBefore(OffsetDateTime.now().truncatedTo(DAYS).minusDays(THRESHOLD_IN_DAYS));
		verifyNoMoreInteractions(initiationRepositoryMock);
	}
}
