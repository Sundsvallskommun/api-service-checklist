package se.sundsvall.checklist.service.mapper;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;

import generated.se.sundsvall.messaging.DeliveryResult;
import generated.se.sundsvall.messaging.MessageStatus;
import java.util.List;
import java.util.Optional;
import se.sundsvall.checklist.api.model.Correspondence;
import se.sundsvall.checklist.integration.db.model.CorrespondenceEntity;
import se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel;
import se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus;

public class CorrespondenceMapper {
	private CorrespondenceMapper() {}

	// -----------------------------
	// Entity mappings
	// -----------------------------

	public static CorrespondenceEntity toCorrespondenceEntity(final CommunicationChannel communicationChannel, final String recipient) {
		return CorrespondenceEntity.builder()
			.withCommunicationChannel(communicationChannel)
			.withRecipient(recipient)
			.build();
	}

	public static CorrespondenceStatus toCorrespondenceStatus(final List<DeliveryResult> results) {
		final var status = Optional.ofNullable(results).orElse(emptyList()).stream()
			.map(DeliveryResult::getStatus)
			.findFirst()
			.orElse(MessageStatus.FAILED);

		return switch (status) {
			case SENT -> CorrespondenceStatus.SENT;
			case PENDING -> CorrespondenceStatus.NOT_SENT;
			case NO_CONTACT_WANTED -> CorrespondenceStatus.WILL_NOT_SEND;
			default -> CorrespondenceStatus.ERROR;
		};
	}

	// -----------------------------
	// API mappings
	// -----------------------------

	public static Correspondence toCorrespondence(final CorrespondenceEntity correspondenceEntity) {
		return ofNullable(correspondenceEntity)
			.map(entity -> Correspondence.builder()
				.withCommunicationChannel(entity.getCommunicationChannel())
				.withCorrespondenceStatus(entity.getCorrespondenceStatus())
				.withMessageId(entity.getMessageId())
				.withRecipient(entity.getRecipient())
				.withAttempts(entity.getAttempts())
				.withSent(entity.getSent())
				.build())
			.orElse(null);
	}
}
