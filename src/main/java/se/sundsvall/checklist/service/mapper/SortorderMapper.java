package se.sundsvall.checklist.service.mapper;

import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toCollection;
import static org.apache.commons.lang3.ObjectUtils.anyNull;
import static se.sundsvall.checklist.integration.db.model.enums.ComponentType.PHASE;
import static se.sundsvall.checklist.integration.db.model.enums.ComponentType.TASK;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import se.sundsvall.checklist.api.model.SortorderRequest;
import se.sundsvall.checklist.api.model.SortorderRequest.PhaseItem;
import se.sundsvall.checklist.api.model.SortorderRequest.TaskItem;
import se.sundsvall.checklist.integration.db.model.SortorderEntity;

public class SortorderMapper {

	private SortorderMapper() {}

	public static List<SortorderEntity> toSortorderEntities(final String municipalityId, final Integer organizationNumber, final SortorderRequest request) {
		if (anyNull(municipalityId, organizationNumber, request)) {
			return null;
		}

		// Start by converting all phases to to entities
		final var entities = ofNullable(request.getPhaseOrder()).orElse(emptyList()).stream()
			.map(item -> toSortorderEntity(municipalityId, organizationNumber, item))
			.collect(toCollection(ArrayList::new));

		// Then convert all tasks to to entities
		entities.addAll(
			ofNullable(request.getPhaseOrder()).orElse(emptyList()).stream()
				.map(PhaseItem::getTaskOrder)
				.filter(Objects::nonNull)
				.flatMap(List::stream)
				.map(item -> toSortorderEntity(municipalityId, organizationNumber, item))
				.toList());

		return entities;
	}

	private static SortorderEntity toSortorderEntity(final String municipalityId, final int organizationNumber, final PhaseItem item) {
		return SortorderEntity.builder()
			.withComponentId(item.getId())
			.withComponentType(PHASE)
			.withMunicipalityId(municipalityId)
			.withOrganizationNumber(organizationNumber)
			.withPosition(item.getPosition())
			.build();
	}

	private static SortorderEntity toSortorderEntity(final String municipalityId, final int organizationNumber, final TaskItem item) {
		return SortorderEntity.builder()
			.withComponentId(item.getId())
			.withComponentType(TASK)
			.withMunicipalityId(municipalityId)
			.withOrganizationNumber(organizationNumber)
			.withPosition(item.getPosition())
			.build();
	}

}
