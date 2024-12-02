package se.sundsvall.checklist.service.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static se.sundsvall.checklist.TestObjectFactory.generateSortorderRequest;
import static se.sundsvall.checklist.integration.db.model.enums.ComponentType.PHASE;
import static se.sundsvall.checklist.integration.db.model.enums.ComponentType.TASK;

import org.junit.jupiter.api.Test;

import se.sundsvall.checklist.api.model.SortorderRequest;

class SortorderMapperTest {
	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final int ORGANIZATION_NUMBER = 987;

	@Test
	void toSortorderEntities() {
		// Arrange
		final var request = generateSortorderRequest();

		// Act
		final var entities = SortorderMapper.toSortorderEntities(MUNICIPALITY_ID, ORGANIZATION_NUMBER, request);

		// Assert
		assertThat(entities).hasSize(6);

		assertThat(entities.stream()
			.filter(entity -> PHASE == entity.getComponentType())
			.toList())
			.satisfiesExactlyInAnyOrder(entity -> {
				assertThat(entity.getComponentId()).isEqualTo(request.getPhaseOrder().getFirst().getId());
				assertThat(entity.getPosition()).isEqualTo(request.getPhaseOrder().getFirst().getPosition());
			}, entity -> {
				assertThat(entity.getComponentId()).isEqualTo(request.getPhaseOrder().getLast().getId());
				assertThat(entity.getPosition()).isEqualTo(request.getPhaseOrder().getLast().getPosition());
			});

		assertThat(entities.stream()
			.filter(entity -> TASK == entity.getComponentType())
			.toList())
			.satisfiesExactlyInAnyOrder(entity -> {
				assertThat(entity.getComponentId()).isEqualTo(request.getPhaseOrder().getFirst().getTaskOrder().getFirst().getId());
				assertThat(entity.getPosition()).isEqualTo(request.getPhaseOrder().getFirst().getTaskOrder().getFirst().getPosition());
			}, entity -> {
				assertThat(entity.getComponentId()).isEqualTo(request.getPhaseOrder().getFirst().getTaskOrder().getLast().getId());
				assertThat(entity.getPosition()).isEqualTo(request.getPhaseOrder().getFirst().getTaskOrder().getLast().getPosition());
			}, entity -> {
				assertThat(entity.getComponentId()).isEqualTo(request.getPhaseOrder().getLast().getTaskOrder().getFirst().getId());
				assertThat(entity.getPosition()).isEqualTo(request.getPhaseOrder().getLast().getTaskOrder().getFirst().getPosition());
			}, entity -> {
				assertThat(entity.getComponentId()).isEqualTo(request.getPhaseOrder().getLast().getTaskOrder().getLast().getId());
				assertThat(entity.getPosition()).isEqualTo(request.getPhaseOrder().getLast().getTaskOrder().getLast().getPosition());
			});
	}

	@Test
	void toSortorderEntitiesWhenNoTasksPresent() {
		// Arrange
		final var request = generateSortorderRequest();
		request.getPhaseOrder().forEach(phase -> phase.setTaskOrder(null));

		// Act
		final var entities = SortorderMapper.toSortorderEntities(MUNICIPALITY_ID, ORGANIZATION_NUMBER, request);

		// Assert
		assertThat(entities).hasSize(2).satisfiesExactlyInAnyOrder(entity -> {
			assertThat(entity.getComponentId()).isEqualTo(request.getPhaseOrder().getFirst().getId());
			assertThat(entity.getPosition()).isEqualTo(request.getPhaseOrder().getFirst().getPosition());
		}, entity -> {
			assertThat(entity.getComponentId()).isEqualTo(request.getPhaseOrder().getLast().getId());
			assertThat(entity.getPosition()).isEqualTo(request.getPhaseOrder().getLast().getPosition());
		});
	}

	@Test
	void toSortorderEntitiesWhenNoPhasesPresent() {
		// Arrange
		final var request = SortorderRequest.builder().build();

		// Act
		final var entities = SortorderMapper.toSortorderEntities(MUNICIPALITY_ID, ORGANIZATION_NUMBER, request);

		// Assert
		assertThat(entities).isEmpty();
	}

	@Test
	void toSortorderEntitiesFromNull() {
		assertThat(SortorderMapper.toSortorderEntities(null, null, null)).isNull();
	}

}
