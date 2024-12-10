package se.sundsvall.checklist.integration.db.model;

import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TimeZoneStorage;
import se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel;
import se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus;

@Getter
@Setter
@Builder(setterPrefix = "with")
@Entity
@Table(name = "correspondence")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CorrespondenceEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(name = "message_id")
	private String messageId;

	@Column(name = "recipient", nullable = false)
	private String recipient;

	@Column(name = "attempts")
	private int attempts;

	@Enumerated(EnumType.STRING)
	@Column(name = "correspondence_status")
	private CorrespondenceStatus correspondenceStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "communication_channel")
	private CommunicationChannel communicationChannel;

	@Column(name = "sent")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime sent;

	@PrePersist
	void prePersist() {
		this.sent = OffsetDateTime.now();
	}

}
