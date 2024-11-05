package se.sundsvall.checklist.integration.db.model;

import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.hibernate.annotations.TimeZoneStorage;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.sundsvall.checklist.integration.db.model.enums.RoleType;

@Getter
@Setter
@Builder(setterPrefix = "with")
@Entity
@Table(name = "employee", indexes = {
	@Index(name = "idx_employee_username", columnList = "username")
})
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class EmployeeEntity {

	@Id
	@Column(name = "id")
	private String id;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "email")
	private String email;

	@Column(name = "username")
	private String username;

	@Column(name = "title")
	private String title;

	@Enumerated(EnumType.STRING)
	@Column(name = "role_type")
	private RoleType roleType;

	@Column(name = "start_date")
	private LocalDate startDate;

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Column(name = "updated")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime updated;

	@OneToOne(cascade = {
		CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH
	}, mappedBy = "employee", orphanRemoval = true)
	private EmployeeChecklistEntity employeeChecklist;

	@ManyToOne(cascade = {
		CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH
	})
	@JoinColumn(name = "organization_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_employee_company"))
	private OrganizationEntity company;

	@ManyToOne(cascade = {
		CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH
	})
	@JoinColumn(name = "department_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_employee_department"))
	private OrganizationEntity department;

	@ManyToOne(cascade = {
		CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH
	})
	@JoinColumn(name = "manager_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_employee_manager"))
	private ManagerEntity manager;

	@PrePersist
	void prePersist() {
		this.created = OffsetDateTime.now();
	}

	@PreUpdate
	void preUpdate() {
		this.updated = OffsetDateTime.now();
	}

}
