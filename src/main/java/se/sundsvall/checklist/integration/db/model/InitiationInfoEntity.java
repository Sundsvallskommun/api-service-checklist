package se.sundsvall.checklist.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

@Getter
@Setter
@Builder(setterPrefix = "with")
@Entity
@Table(name = "initiation_info")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class InitiationInfoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(name = "log_id")
	private String logId;

	@Column(name = "information")
	private String information;

	@Column(name = "status")
	private String status;

	@Column(name = "created")
	private OffsetDateTime created;

	@PrePersist
	void prePersist() {
		created = OffsetDateTime.now();
	}

}
