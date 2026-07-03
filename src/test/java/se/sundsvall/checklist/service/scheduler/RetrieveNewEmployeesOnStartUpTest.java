package se.sundsvall.checklist.service.scheduler;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RetrieveNewEmployeesOnStartUpTest {

	@Mock
	private RetrieveNewEmployeesScheduler retriveNewEmployeesSchedulerMock;

	@Test
	void testOnStartup() {
		new RetrieveNewEmployeesOnStartUp(retriveNewEmployeesSchedulerMock, Duration.ofSeconds(1));

		await()
			.atMost(Duration.ofSeconds(3))
			.with()
			.pollInterval(Duration.ofMillis(100))
			.ignoreExceptions()
			.until(() -> {
				verify(retriveNewEmployeesSchedulerMock).execute();
				return true;
			});
	}

	@Test
	void testOnStartupWhenExecuteThrows() {
		// The scheduled task must catch and log any exception rather than let it escape unobserved into the executor.
		doThrow(new RuntimeException("boom")).when(retriveNewEmployeesSchedulerMock).execute();

		new RetrieveNewEmployeesOnStartUp(retriveNewEmployeesSchedulerMock, Duration.ofSeconds(1));

		await()
			.atMost(Duration.ofSeconds(3))
			.with()
			.pollInterval(Duration.ofMillis(100))
			.ignoreExceptions()
			.until(() -> {
				verify(retriveNewEmployeesSchedulerMock).execute();
				return true;
			});
	}
}
