package se.sundsvall.checklist.integration.db.model;

import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TimeZoneStorage;
import se.sundsvall.checklist.integration.db.model.enums.Permission;

@Getter
@Setter
@Builder(setterPrefix = "with")
@Entity
@Table(name = "phase", indexes = {
	@Index(name = "phase_municipality_id_idx", columnList = "municipality_id")
})
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PhaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "name")
	private String name;

	@Column(name = "body_text", columnDefinition = "varchar(2048)")
	private String bodyText;

	@Column(name = "time_to_complete")
	private String timeToComplete;

	@Enumerated(EnumType.STRING)
	@Column(name = "permission")
	private Permission permission;

	@Column(name = "sort_order")
	private int sortOrder;

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Column(name = "updated")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime updated;

	@Column(name = "last_saved_by", nullable = false)
	private String lastSavedBy;

	@PrePersist
	void prePersist() {
		this.created = OffsetDateTime.now();
	}

	@PreUpdate
	void preUpdate() {
		this.updated = OffsetDateTime.now();
	}
}
