package se.sundsvall.checklist.integration.db.model;

import static org.hibernate.annotations.TimeZoneStorageType.NORMALIZE;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hibernate.annotations.TimeZoneStorage;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
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
import se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel;

@Getter
@Setter
@Builder(setterPrefix = "with")
@Entity
@Table(name = "organization", indexes = {
	@Index(name = "organization_number_municipality_id_idx", columnList = "organization_number, municipality_id")
}, uniqueConstraints = {
	@UniqueConstraint(name = "uk_organization_organization_number_municipality_id", columnNames = { "organization_number", "municipality_id" })
})
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrganizationEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(name = "organization_name")
	private String organizationName;

	@Column(name = "organization_number")
	private int organizationNumber;

	@Column(name = "municipality_id")
	private String municipalityId;

	@Column(name = "created")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime created;

	@Column(name = "updated")
	@TimeZoneStorage(NORMALIZE)
	private OffsetDateTime updated;

	@Builder.Default
	@OneToMany(cascade = { CascadeType.REFRESH, CascadeType.PERSIST, CascadeType.MERGE }, orphanRemoval = true)
	@JoinColumn(name = "organization_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_organization_checklist"))
	private List<ChecklistEntity> checklists = new ArrayList<>();

	@ElementCollection(targetClass = CommunicationChannel.class)
	@CollectionTable(name = "organization_communication_channel", joinColumns = @JoinColumn(name = "organization_id"), foreignKey = @ForeignKey(name = "fk_organization_communication_channel_organization"))
	@Column(name = "communication_channel", nullable = false)
	@Enumerated(EnumType.STRING)
	private Set<CommunicationChannel> communicationChannels;

	@PrePersist
	void prePersist() {
		this.created = OffsetDateTime.now();
	}

	@PreUpdate
	void preUpdate() {
		this.updated = OffsetDateTime.now();
	}

}
