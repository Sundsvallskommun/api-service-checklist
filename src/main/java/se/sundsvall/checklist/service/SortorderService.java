package se.sundsvall.checklist.service;

import static se.sundsvall.checklist.service.mapper.SortorderMapper.toSortorderEntities;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import se.sundsvall.checklist.api.model.SortorderRequest;
import se.sundsvall.checklist.integration.db.repository.SortorderRepository;

@Service
public class SortorderService {

	private final SortorderRepository sortorderRepository;

	public SortorderService(final SortorderRepository sortorderRepository) {
		this.sortorderRepository = sortorderRepository;
	}

	@Transactional
	public void saveSortorder(final String municipalityId, final Integer organizationNumber, final SortorderRequest request) {
		sortorderRepository.deleteAllInBatch(sortorderRepository.findAllByMunicipalityIdAndOrganizationNumber(municipalityId, organizationNumber));
		sortorderRepository.saveAll(toSortorderEntities(municipalityId, organizationNumber, request));
	}
}
