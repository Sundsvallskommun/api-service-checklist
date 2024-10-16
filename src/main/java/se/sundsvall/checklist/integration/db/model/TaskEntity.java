package se.sundsvall.checklist.integration.db.model;

import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import java.time.OffsetDateTime;

import org.hibernate.annotations.TimeZoneStorage;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Column(name = "updated")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime updated;

	@PrePersist
	void prePersist() {
		this.created = OffsetDateTime.now();
	}

	@PreUpdate
	void preUpdate() {
		this.updated = OffsetDateTime.now();
	}

}
