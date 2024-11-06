package se.sundsvall.checklist.integration.db.model;

import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import java.time.OffsetDateTime;

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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.sundsvall.checklist.integration.db.model.enums.FulfilmentStatus;

@Getter
@Setter
@Builder(setterPrefix = "with")
@Entity
@Table(name = "custom_fulfilment")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomFulfilmentEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Enumerated(EnumType.STRING)
	@Column(name = "completed")
	private FulfilmentStatus completed;

	@Column(name = "response_text")
	private String responseText;

	@Column(name = "updated")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime updated;

	@ManyToOne(cascade = {
		CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH
	})
	@JoinColumn(name = "employee_checklist_id", foreignKey = @ForeignKey(name = "fk_custom_fulfilment_employee_checklist"))
	private EmployeeChecklistEntity employeeChecklist;

	@ManyToOne(cascade = {
		CascadeType.DETACH, CascadeType.MERGE, CascadeType.REFRESH
	})
	@JoinColumn(name = "custom_task_id", foreignKey = @ForeignKey(name = "fk_custom_task_fulfilment_task"))
	private CustomTaskEntity customTask;

	@Column(name = "last_saved_by")
	private String lastSavedBy;

	@PrePersist
	@PreUpdate
	void preUpdate() {
		this.updated = OffsetDateTime.now();
	}
}
