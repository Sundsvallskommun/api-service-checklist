package se.sundsvall.checklist.integration.db.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import se.sundsvall.checklist.integration.db.model.enums.ComponentType;

@Getter
@Setter
@Builder(setterPrefix = "with")
@Entity
@Table(name = "custom_sortorder", indexes = {
	@Index(name = "idx_custom_sortorder_municipality_id_organization_number", columnList = "municipality_id, organization_number")
}, uniqueConstraints = {
	@UniqueConstraint(name = "uk_municipality_id_organization_number_component_id", columnNames = {
		"municipality_id", "organization_number", "component_id"
	})
})
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SortorderEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;

	@Column(name = "municipality_id", nullable = false)
	private String municipalityId;

	@Column(name = "organization_number", nullable = false)
	private int organizationNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "component_type", nullable = false)
	private ComponentType componentType;

	@Column(name = "component_id", nullable = false)
	private String componentId;

	@Column(name = "position", nullable = false)
	private int position;
}
