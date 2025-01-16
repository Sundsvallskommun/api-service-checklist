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
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.TimeZoneStorage;
import se.sundsvall.checklist.integration.db.model.enums.LifeCycle;

@Getter
@Setter
@Builder(setterPrefix = "with")
@Entity
@Table(name = "checklist", uniqueConstraints = {
	@UniqueConstraint(name = "uk_checklist_name_municipality_id_version", columnNames = {
		"name", "municipality_id", "version"
	})
})
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChecklistEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(name = "name")
	private String name;

	@Column(name = "display_name")
	private String displayName;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "version")
	private int version;

	@Enumerated(EnumType.STRING)
	@Column(name = "life_cycle")
	private LifeCycle lifeCycle;

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Column(name = "updated")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime updated;

	@Column(name = "last_saved_by", nullable = false)
	private String lastSavedBy;

	@ManyToOne(cascade = {
		CascadeType.REFRESH, CascadeType.PERSIST, CascadeType.MERGE
	})
	@JoinColumn(name = "organization_id")
	private OrganizationEntity organization;

	@Builder.Default
	@OneToMany(cascade = {
		CascadeType.REFRESH, CascadeType.PERSIST, CascadeType.MERGE
	}, orphanRemoval = true)
	@JoinColumn(name = "checklist_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_checklist_task"))
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
