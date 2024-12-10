package se.sundsvall.checklist.integration.db.model;

import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

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
import jakarta.persistence.ManyToOne;
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
import se.sundsvall.checklist.integration.db.model.enums.QuestionType;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

@Getter
@Setter
@Builder(setterPrefix = "with")
@Entity
@Table(name = "task")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TaskEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(name = "heading")
	private String heading;

	@Column(name = "text", columnDefinition = "varchar(2048)")
	private String text;

	@Column(name = "sort_order")
	private int sortOrder;

	@Enumerated(EnumType.STRING)
	@Column(name = "role_type")
	private RoleType roleType;

	@Enumerated(EnumType.STRING)
	@Column(name = "question_type")
	private QuestionType questionType;

	@Enumerated(EnumType.STRING)
	@Column(name = "permission")
	private Permission permission;

	@ManyToOne(cascade = {
		CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH
	})
	@JoinColumn(name = "phase_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_task_phase"))
	private PhaseEntity phase;

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
