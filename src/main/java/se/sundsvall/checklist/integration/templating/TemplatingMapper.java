package se.sundsvall.checklist.integration.templating;

import generated.se.sundsvall.templating.RenderRequest;
import se.sundsvall.checklist.integration.db.model.EmployeeEntity;
import se.sundsvall.checklist.integration.templating.model.EmployeeParameter;
import se.sundsvall.checklist.integration.templating.model.ManagerParameter;

public final class TemplatingMapper {

	private TemplatingMapper() {
		// private constructor
	}

	/**
	 * Creates the request that is used to render templates.
	 *
	 * @param employee   the employee to use in rendering
	 * @param identifier the template identifier that is stored in templating-api
	 * @return RenderRequest that is used in templating integration.
	 */

	public static RenderRequest toRenderRequest(final EmployeeEntity employee, final String identifier) {
		final var renderRequest = new RenderRequest();
		final var employeeParameter = EmployeeParameter.builder()
			.withFirstName(employee.getFirstName())
			.withLastName(employee.getLastName())
			.withStartDate(employee.getStartDate())
			.build();
		final var managerParameter = ManagerParameter.builder()
			.withFirstName(employee.getManager().getFirstName())
			.withLastName(employee.getManager().getLastName())
			.build();

		renderRequest.setIdentifier(identifier);
		renderRequest.putParametersItem("employee", employeeParameter);
		renderRequest.putParametersItem("manager", managerParameter);
		return renderRequest;
	}
}
