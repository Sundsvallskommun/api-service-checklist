package se.sundsvall.checklist.integration.company;

import generated.se.sundsvall.company.Organization;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.Cacheable;
import se.sundsvall.dept44.problem.Problem;
import se.sundsvall.dept44.problem.ThrowableProblem;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ExtendWith(MockitoExtension.class)
class CompanyIntegrationTest {

	@Mock
	private CompanyClient companyClientMock;

	@InjectMocks
	private CompanyIntegration companyIntegration;

	@AfterEach
	void verifyNoMoreMockInteractions() {
		verifyNoMoreInteractions(companyClientMock);
	}

	@Test
	void verifyCacheAnnotations() throws NoSuchMethodException {
		assertThat(CompanyIntegration.class.getMethod("getCompanies", String.class).getAnnotation(Cacheable.class).value()).containsExactly("company");
		assertThat(CompanyIntegration.class.getMethod("getOrganizationsForCompany", String.class, int.class).getAnnotation(Cacheable.class).value()).containsExactly("company");
	}

	@Test
	void getCompanies() {
		// Arrange
		final var municipalityId = "2281";
		final var companies = List.of(new Organization().companyId(ThreadLocalRandom.current().nextInt()));
		when(companyClientMock.getCompanies(municipalityId)).thenReturn(companies);

		// Act
		final var result = companyIntegration.getCompanies(municipalityId);

		// Assert and verify
		assertThat(result).isSameAs(companies);
		verify(companyClientMock).getCompanies(municipalityId);
	}

	@Test
	void getCompaniesThrowsException() {
		// Arrange
		final var municipalityId = "2281";
		when(companyClientMock.getCompanies(municipalityId)).thenThrow(Problem.valueOf(BAD_REQUEST, "Big and stout"));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> companyIntegration.getCompanies(municipalityId));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(e.getDetail()).isEqualTo("Big and stout");
		verify(companyClientMock).getCompanies(municipalityId);
	}

	@Test
	void getOrganizationsForCompany() {
		// Arrange
		final var municipalityId = "2281";
		final var companyId = ThreadLocalRandom.current().nextInt();
		final var companies = List.of(new Organization().companyId(companyId).orgId(12));
		when(companyClientMock.getOrganizationsForCompany(municipalityId, companyId)).thenReturn(companies);

		// Act
		final var result = companyIntegration.getOrganizationsForCompany(municipalityId, companyId);

		// Assert and verify
		assertThat(result).isSameAs(companies);
		verify(companyClientMock).getOrganizationsForCompany(municipalityId, companyId);
	}

	@Test
	void getOrganizationsForCompanyThrowsException() {
		// Arrange
		final var municipalityId = "2281";
		final var companyId = ThreadLocalRandom.current().nextInt();
		when(companyClientMock.getOrganizationsForCompany(anyString(), anyInt())).thenThrow(Problem.valueOf(BAD_REQUEST, "Big and stout"));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> companyIntegration.getOrganizationsForCompany(municipalityId, companyId));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(BAD_REQUEST);
		assertThat(e.getDetail()).isEqualTo("Big and stout");
		verify(companyClientMock).getOrganizationsForCompany(municipalityId, companyId);
	}
}
