package se.sundsvall.checklist.integration.templating;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import generated.se.sundsvall.templating.RenderResponse;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.db.model.ManagerEntity;

@ExtendWith(MockitoExtension.class)
class TemplatingIntegrationTest {

	private static final String MUNICIPALITY_ID = "municipalityId";

	@Mock
	private TemplatingClient templatingClientMock;

	@Mock
	private EmployeeEntity employeeEntityMock;

	@Mock
	private ManagerEntity managerEntityMock;

	@InjectMocks
	private TemplatingIntegration templatingIntegration;

	@Test
	void renderTemplateTest() {
		// Arrange
		final var identifier = "Identifier";
		final var string = "This is the return value";
		final var base64 = Base64.getEncoder().encodeToString(string.getBytes());
		when(employeeEntityMock.getManager()).thenReturn(managerEntityMock);
		when(templatingClientMock.render(eq(MUNICIPALITY_ID), any())).thenReturn(new RenderResponse().output(base64));

		// Act
		final var request = TemplatingMapper.toRenderRequest(employeeEntityMock, identifier);
		final var response = templatingIntegration.renderTemplate(MUNICIPALITY_ID, request);

		// Assert and verify
		assertThat(response).isPresent();
		assertThat(response.get().getOutput()).isEqualTo(base64);
		assertThat(Base64.getDecoder().decode(response.get().getOutput())).isEqualTo(string.getBytes());

		verify(templatingClientMock).render(MUNICIPALITY_ID, request);
		verify(employeeEntityMock).getFirstName();
		verify(employeeEntityMock).getLastName();
		verify(employeeEntityMock).getStartDate();
		verify(employeeEntityMock, times(2)).getManager();
		verify(managerEntityMock).getFirstName();
		verify(managerEntityMock).getLastName();
	}

	@Test
	void renderTemplateNotFoundTest() {
		// Arrange
		final var identifier = "No template with this identifier";
		when(employeeEntityMock.getManager()).thenReturn(managerEntityMock);

		// Act
		final var request = TemplatingMapper.toRenderRequest(employeeEntityMock, identifier);
		final var response = templatingIntegration.renderTemplate(MUNICIPALITY_ID, request);

		// Assert and verify
		assertThat(response).isEmpty();
		verify(templatingClientMock).render(MUNICIPALITY_ID, request);
		verify(employeeEntityMock).getFirstName();
		verify(employeeEntityMock).getLastName();
		verify(employeeEntityMock).getStartDate();
		verify(employeeEntityMock, times(2)).getManager();
		verify(managerEntityMock).getFirstName();
		verify(managerEntityMock).getLastName();
	}
}
