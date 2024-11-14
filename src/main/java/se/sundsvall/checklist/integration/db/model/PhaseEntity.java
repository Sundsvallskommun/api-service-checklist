package se.sundsvall.checklist.integration.db.model;

import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.TimeZoneStorage;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.sundsvall.checklist.integration.db.model.enums.Permission;

@Getter
@Setter
@Builder(setterPrefix = "with")
@Entity
@Table(name = "phase")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PhaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

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

	@Builder.Default
	@OneToMany(cascade = {
		CascadeType.REFRESH, CascadeType.PERSIST, CascadeType.MERGE
	}, orphanRemoval = true)
	@JoinColumn(name = "phase_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_phase_task"))
	private List<TaskEntity> tasks = new ArrayList<>();

	@PrePersist
	void prePersist() {
		this.created = OffsetDateTime.now();
	}

	@PreUpdate
	void preUpdate() {
		this.updated = OffsetDateTime.now();
	}
}
