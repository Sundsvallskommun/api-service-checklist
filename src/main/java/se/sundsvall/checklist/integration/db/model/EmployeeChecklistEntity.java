package se.sundsvall.checklist.integration.db.model;

import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.TimeZoneStorage;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "employee_checklist", indexes = {
	@Index(name = "employee_checklist_expiration_date_locked_idx", columnList = "expiration_date, locked")
}, uniqueConstraints = {
	@UniqueConstraint(name = "uk_correspondence_id", columnNames = "correspondence_id"),
	@UniqueConstraint(name = "uk_employee_id", columnNames = "employee_id")
})
@Getter
@Setter
@Builder(setterPrefix = "with")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmployeeChecklistEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Column(name = "updated")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime updated;

	@Column(name = "start_date")
	private LocalDate startDate;

	@Column(name = "end_date")
	private LocalDate endDate;

	@Column(name = "expiration_date")
	private LocalDate expirationDate;

	@Column(name = "locked")
	private boolean locked;

	@OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinColumn(name = "employee_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_employee_checklist_employee"))
	private EmployeeEntity employee;

	@OneToOne(cascade = {
		CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH
	}, orphanRemoval = true)
	@JoinColumn(name = "correspondence_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_employee_checklist_correspondence"))
	private CorrespondenceEntity correspondence;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "referred_checklist",
		joinColumns = {
			@JoinColumn(name = "employee_checklist_id")
		},
		foreignKey = @ForeignKey(name = "fk_referred_checklist_employee_checklist"),
		inverseJoinColumns = {
			@JoinColumn(name = "checklist_id")
		},
		inverseForeignKey = @ForeignKey(name = "fk_referred_checklist_checklist"),
		uniqueConstraints = {
			@UniqueConstraint(name = "uk_employee_checklist_id_checklist_id", columnNames = {
				"employee_checklist_id", "checklist_id"
			})
		})
	private List<ChecklistEntity> checklists;

	@Builder.Default
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "employeeChecklist")
	private List<FulfilmentEntity> fulfilments = new ArrayList<>();

	@Builder.Default
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "employeeChecklist")
	private List<CustomFulfilmentEntity> customFulfilments = new ArrayList<>();

	@Builder.Default
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "employeeChecklist")
	private List<CustomTaskEntity> customTasks = new ArrayList<>();

	@Builder.Default
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true, mappedBy = "employeeChecklist")
	private List<DelegateEntity> delegates = new ArrayList<>();

	@Embedded
	private MentorEntity mentor;

	@PrePersist
	void prePersist() {
		this.created = OffsetDateTime.now();
	}

	@PreUpdate
	void preUpdate() {
		this.updated = OffsetDateTime.now();
	}
}
