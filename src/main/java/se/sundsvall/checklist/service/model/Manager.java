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
public class Manager {

	private String loginname;

	private String emailAddress;

	private String lastname;

	private String givenname;

	private String personId;

}
