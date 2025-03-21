package se.sundsvall.checklist.service.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
public class Employee {

	private String lastname;

	private String givenname;

	private String middlename;

	private String personId; // UUID in Employee

	private String legalId;

	private String loginname;

	private String emailAddress;

	private Employment mainEmployment;

	private Boolean isClassified;
}
