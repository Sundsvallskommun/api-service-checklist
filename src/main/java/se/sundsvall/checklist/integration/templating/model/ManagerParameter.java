package se.sundsvall.checklist.integration.templating.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(setterPrefix = "with")
public class ManagerParameter {

	private String firstName;
	private String lastName;
}
