package se.sundsvall.checklist.integration.templating.model;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(setterPrefix = "with")
public class EmployeeParameter {

	private String firstName;
	private String lastName;
	private LocalDate startDate;

}
