package se.sundsvall.checklist.integration.db.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "delegate", indexes = {
	@Index(name = "idx_delegate_username", columnList = "username"),
	@Index(name = "idx_delegate_first_name", columnList = "first_name"),
	@Index(name = "idx_delegate_last_name", columnList = "last_name"),
	@Index(name = "idx_delegate_email", columnList = "email") })
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class DelegateEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(name = "party_id", nullable = false)
	private String partyId;

	@Column(name = "username", nullable = false)
	private String username;

	@Column(name = "first_name", nullable = false)
	private String firstName;

	@Column(name = "last_name", nullable = false)
	private String lastName;

	@Column(name = "email", nullable = false)
	private String email;

	@ManyToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH })
	@JoinColumn(name = "manager_id", foreignKey = @ForeignKey(name = "fk_delegate_manager"))
	private ManagerEntity delegatedBy;

	@ManyToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH })
	@JoinColumn(name = "employee_checklist_id", foreignKey = @ForeignKey(name = "fk_delegate_employee_checklist"))
	private EmployeeChecklistEntity employeeChecklist;

}
