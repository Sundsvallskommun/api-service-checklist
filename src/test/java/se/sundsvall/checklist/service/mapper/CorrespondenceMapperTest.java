package se.sundsvall.checklist.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import generated.se.sundsvall.messaging.DeliveryResult;
import generated.se.sundsvall.messaging.MessageStatus;
import java.time.OffsetDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import se.sundsvall.checklist.integration.db.model.CorrespondenceEntity;
import se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel;
import se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus;

class CorrespondenceMapperTest {

	@Test
	void toCorrespondenceEntity() {
		// Arrange
		final var communicationChannel = CommunicationChannel.EMAIL;
		final var recipient = "recipient";

		// Act
		final var entity = CorrespondenceMapper.toCorrespondenceEntity(communicationChannel, recipient);

		// Assert
		assertThat(entity.getCommunicationChannel()).isEqualTo(communicationChannel);
		assertThat(entity.getCorrespondenceStatus()).isNull();
		assertThat(entity.getId()).isNull();
		assertThat(entity.getMessageId()).isNull();
		assertThat(entity.getRecipient()).isEqualTo(recipient);
		assertThat(entity.getAttempts()).isZero();
		assertThat(entity.getSent()).isNull();
	}

	@Test
	void toCorrespondenceEntityFromNull() {
		// Act
		final var entity = CorrespondenceMapper.toCorrespondenceEntity(null, null);

		// Assert
		assertThat(entity).hasAllNullFieldsOrPropertiesExcept("attempts");
		assertThat(entity.getAttempts()).isZero();

	}

	@Test
	void toCorrespondenceStatus() {
		// Act and assert
		assertThat(CorrespondenceMapper.toCorrespondenceStatus(null)).isEqualTo(CorrespondenceStatus.ERROR);
		assertThat(CorrespondenceMapper.toCorrespondenceStatus(List.of(new DeliveryResult().status(MessageStatus.AWAITING_FEEDBACK)))).isEqualTo(CorrespondenceStatus.ERROR);
		assertThat(CorrespondenceMapper.toCorrespondenceStatus(List.of(new DeliveryResult().status(MessageStatus.FAILED)))).isEqualTo(CorrespondenceStatus.ERROR);
		assertThat(CorrespondenceMapper.toCorrespondenceStatus(List.of(new DeliveryResult().status(MessageStatus.NO_CONTACT_SETTINGS_FOUND)))).isEqualTo(CorrespondenceStatus.ERROR);
		assertThat(CorrespondenceMapper.toCorrespondenceStatus(List.of(new DeliveryResult().status(MessageStatus.NO_CONTACT_WANTED)))).isEqualTo(CorrespondenceStatus.WILL_NOT_SEND);
		assertThat(CorrespondenceMapper.toCorrespondenceStatus(List.of(new DeliveryResult().status(MessageStatus.PENDING)))).isEqualTo(CorrespondenceStatus.NOT_SENT);
		assertThat(CorrespondenceMapper.toCorrespondenceStatus(List.of(new DeliveryResult().status(MessageStatus.SENT)))).isEqualTo(CorrespondenceStatus.SENT);
	}

	@Test
	void toCorrespondence() {
		// Arrange
		final var communciationChannel = CommunicationChannel.EMAIL;
		final var correspondenceStatus = CorrespondenceStatus.SENT;
		final var messageId = "messageId";
		final var reciptient = "recipient";
		final var attempts = 12;
		final var sent = OffsetDateTime.now();

		final var entity = CorrespondenceEntity.builder()
			.withCommunicationChannel(communciationChannel)
			.withCorrespondenceStatus(correspondenceStatus)
			.withMessageId(messageId)
			.withRecipient(reciptient)
			.withAttempts(attempts)
			.withSent(sent)
			.build();

		// Act
		final var bean = CorrespondenceMapper.toCorrespondence(entity);

		// Assert
		assertThat(bean.getCommunicationChannel()).isEqualTo(communciationChannel);
		assertThat(bean.getCorrespondenceStatus()).isEqualTo(correspondenceStatus);
		assertThat(bean.getMessageId()).isEqualTo(messageId);
		assertThat(bean.getRecipient()).isEqualTo(reciptient);
		assertThat(bean.getAttempts()).isEqualTo(attempts);
		assertThat(bean.getSent()).isEqualTo(sent);
	}

	@Test
	void toCorrespondenceFromNull() {
		assertThat(CorrespondenceMapper.toCorrespondence(null)).isNull();
	}
}
