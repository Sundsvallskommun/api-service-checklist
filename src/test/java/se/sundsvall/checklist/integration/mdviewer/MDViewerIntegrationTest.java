package se.sundsvall.checklist.integration.mdviewer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.zalando.problem.Status.I_AM_A_TEAPOT;

import generated.se.sundsvall.mdviewer.Organization;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.Cacheable;
import org.zalando.problem.Problem;
import org.zalando.problem.ThrowableProblem;
import wiremock.org.apache.commons.lang3.RandomUtils;

@ExtendWith(MockitoExtension.class)
class MDViewerIntegrationTest {

	@Mock
	private MDViewerClient mdViewerClientMock;

	@InjectMocks
	private MDViewerIntegration mdViewerIntegration;

	@AfterEach
	void verifyNoMoreMockInteractions() {
		verifyNoMoreInteractions(mdViewerClientMock);
	}

	@Test
	void verifyCacheAnnotations() throws NoSuchMethodException {
		assertThat(MDViewerIntegration.class.getMethod("getCompanies").getAnnotation(Cacheable.class).value()).containsExactly("mdviewer");
		assertThat(MDViewerIntegration.class.getMethod("getOrganizationsForCompany", int.class).getAnnotation(Cacheable.class).value()).containsExactly("mdviewer");
	}

	@Test
	void getCompanies() {
		// Arrange
		final var companies = List.of(new Organization().companyId(RandomUtils.nextInt()));
		when(mdViewerClientMock.getCompanies()).thenReturn(companies);

		// Act
		final var result = mdViewerIntegration.getCompanies();

		// Assert and verify
		assertThat(result).isSameAs(companies);
		verify(mdViewerClientMock).getCompanies();
	}

	@Test
	void getCompaniesThrowsException() {
		// Arrange
		when(mdViewerClientMock.getCompanies()).thenThrow(Problem.valueOf(I_AM_A_TEAPOT, "Big and stout"));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> mdViewerIntegration.getCompanies());

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(I_AM_A_TEAPOT);
		assertThat(e.getDetail()).isEqualTo("Big and stout");
		verify(mdViewerClientMock).getCompanies();
	}

	@Test
	void getOrganizationsForCompany() {
		// Arrange
		final var companyId = RandomUtils.nextInt();
		final var companies = List.of(new Organization().companyId(companyId).organizationId(UUID.randomUUID()));
		when(mdViewerClientMock.getOrganizationsForCompany(companyId)).thenReturn(companies);

		// Act
		final var result = mdViewerIntegration.getOrganizationsForCompany(companyId);

		// Assert and verify
		assertThat(result).isSameAs(companies);
		verify(mdViewerClientMock).getOrganizationsForCompany(companyId);
	}

	@Test
	void getOrganizationsForCompanyThrowsException() {
		// Arrange
		final var companyId = RandomUtils.nextInt();
		when(mdViewerClientMock.getOrganizationsForCompany(anyInt())).thenThrow(Problem.valueOf(I_AM_A_TEAPOT, "Big and stout"));

		// Act
		final var e = assertThrows(ThrowableProblem.class, () -> mdViewerIntegration.getOrganizationsForCompany(companyId));

		// Assert and verify
		assertThat(e.getStatus()).isEqualTo(I_AM_A_TEAPOT);
		assertThat(e.getDetail()).isEqualTo("Big and stout");
		verify(mdViewerClientMock).getOrganizationsForCompany(companyId);
	}
}
