package se.sundsvall.checklist.service.scheduler;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.time.temporal.ChronoUnit.DAYS;

import java.time.OffsetDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.checklist.integration.db.repository.InitiationRepository;
import se.sundsvall.dept44.scheduling.Dept44Scheduled;

/**
 * Scheduler for job to remove initiation information rows older than X days (configurable by properties) from the
 * initiation_info table
 */
@Component
public class PurgeOldInitiationInfoScheduler {

	private static final Logger LOGGER = LoggerFactory.getLogger(PurgeOldInitiationInfoScheduler.class);
	private static final String LOG_PURGE_STARTED = "Purging rows older than {} days from initiation_info table (i.e. rows with created date before {})";

	private final InitiationRepository initiationRepository;
	private final int maxLifetimeInDaysThreshold;

	public PurgeOldInitiationInfoScheduler(InitiationRepository initiationRepository,
		@Value("${checklist.purge-old-initiation-info.maximum-lifespan-in-days}") int maxLifetimeInDaysThreshold) {
		this.initiationRepository = initiationRepository;
		this.maxLifetimeInDaysThreshold = maxLifetimeInDaysThreshold;
	}

	/**
	 * Removes initiation information rows with created date older than threshold from table.
	 */
	@Transactional
	@Dept44Scheduled(
		name = "${checklist.purge-old-initiation-info.name}",
		cron = "${checklist.purge-old-initiation-info.cron}",
		lockAtMostFor = "${checklist.purge-old-initiation-info.lockAtMostFor}",
		maximumExecutionTime = "${checklist.purge-old-initiation-info.maximumExecutionTime}")
	public void execute() {
		final var oldestCreatedDate = OffsetDateTime.now().truncatedTo(DAYS).minusDays(maxLifetimeInDaysThreshold);
		final var formattedDateTime = toReadableFormat(oldestCreatedDate);
		LOGGER.info(LOG_PURGE_STARTED, maxLifetimeInDaysThreshold, formattedDateTime);

		initiationRepository.deleteAllByCreatedBefore(oldestCreatedDate);
	}

	private String toReadableFormat(OffsetDateTime dateTime) {
		return dateTime.format(ISO_LOCAL_DATE_TIME);
	}
}
