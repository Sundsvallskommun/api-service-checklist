package se.sundsvall.checklist.api.model;

import io.swagger.v3.oas.annotations.Hidden;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.With;
import se.sundsvall.dept44.models.api.paging.AbstractParameterPagingAndSortingBase;

@Getter
@Setter
@With
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OngoingEmployeeChecklistParameters extends AbstractParameterPagingAndSortingBase {

	private String employeeName;

	@Hidden // Only used internally, shouldn't be visible in the API specification
	private String municipalityId;

}
