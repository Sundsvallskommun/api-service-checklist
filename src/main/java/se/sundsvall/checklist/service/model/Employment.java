package se.sundsvall.checklist.service.model;

import java.util.Date;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
public class Employment {

	private Integer companyId;

	private Integer topOrgId;

	private String topOrgName;

	private Integer orgId;

	private String orgName;

	private Integer benefitGroupId;

	private Integer employmentType;

	private Boolean isManual;

	private String title;

	private Boolean isMainEmployment;

	private Boolean isManager;

	private Manager manager;

	private String managerCode;

	private Date startDate;

	private Date endDate;

	private String formOfEmploymentId;

	private String eventType;

}
