package se.sundsvall.checklist.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static se.sundsvall.checklist.TestObjectFactory.generateSortorderRequest;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import se.sundsvall.checklist.integration.db.model.SortorderEntity;
import se.sundsvall.checklist.integration.db.repository.SortorderRepository;

@ExtendWith(MockitoExtension.class)
class SortorderServiceTest {

	private static final String MUNICIPALITY_ID = "municipalityId";
	private static final int ORGANIZATION_NUMBER = 123456;

	@Mock
	private SortorderRepository sortorderRepositoryMock;

	@InjectMocks
	private SortorderService service;

	@Captor
	private ArgumentCaptor<List<SortorderEntity>> saveAllCaptor;

	@Test
	void saveSortorder() {
		// Arrange
		final var sortorderRequest = generateSortorderRequest();
		final var sortorderEntities = List.of(SortorderEntity.builder().build(), SortorderEntity.builder().build());

		when(sortorderRepositoryMock.findAllByMunicipalityIdAndOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER)).thenReturn(sortorderEntities);

		// Act
		service.saveSortorder(MUNICIPALITY_ID, ORGANIZATION_NUMBER, sortorderRequest);

		// Assert and verify
		verify(sortorderRepositoryMock).findAllByMunicipalityIdAndOrganizationNumber(MUNICIPALITY_ID, ORGANIZATION_NUMBER);
		verify(sortorderRepositoryMock).deleteAllInBatch(sortorderEntities);
		verify(sortorderRepositoryMock).saveAll(saveAllCaptor.capture());
		verifyNoMoreInteractions(sortorderRepositoryMock);

		assertThat(saveAllCaptor.getValue()).hasSize(6);
	}
}
