package se.sundsvall.checklist.integration.db.model;

import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.TimeZoneStorage;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

@Getter
@Setter
@Builder(setterPrefix = "with")
@Entity
@Table(name = "manager")
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ManagerEntity {

	@Id
	@Column(name = "id")
	private String personId;

	@Column(name = "first_name")
	private String firstName;

	@Column(name = "last_name")
	private String lastName;

	@Column(name = "user_name")
	private String userName;

	@Column(name = "email")
	private String email;

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Column(name = "updated")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime updated;

	@Builder.Default
	@OneToMany(cascade = { CascadeType.MERGE, CascadeType.PERSIST, CascadeType.DETACH, CascadeType.REFRESH }, mappedBy = "manager")
	private List<EmployeeEntity> employees = new ArrayList<>();

	@PrePersist
	void prePersist() {
		this.created = OffsetDateTime.now();
	}

	@PreUpdate
	void preUpdate() {
		this.updated = OffsetDateTime.now();
	}

}
