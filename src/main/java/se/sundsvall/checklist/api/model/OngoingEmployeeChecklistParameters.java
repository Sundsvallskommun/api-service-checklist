package se.sundsvall.checklist.api.model;

import lombok.AllArgsConstructor;
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
public class OngoingEmployeeChecklistParameters extends AbstractParameterPagingAndSortingBase {

	private String employeeName;

	private String municipalityId;

}
