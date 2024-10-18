package se.sundsvall.checklist.api.specification;

import org.springframework.data.jpa.domain.Specification;

import net.kaczmarzyk.spring.data.jpa.domain.LikeIgnoreCase;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Join;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Or;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import se.sundsvall.checklist.integration.db.model.EmployeeChecklistEntity;

@Join(path = "delegates", alias = "d")
@Or(value = {
	@Spec(path = "employee.firstName", params = "searchString", spec = LikeIgnoreCase.class),
	@Spec(path = "employee.lastName", params = "searchString", spec = LikeIgnoreCase.class),
	@Spec(path = "employee.username", params = "searchString", spec = LikeIgnoreCase.class),
	@Spec(path = "employee.company.organizationName", params = "searchString", spec = LikeIgnoreCase.class),
	@Spec(path = "employee.manager.firstName", params = "searchString", spec = LikeIgnoreCase.class),
	@Spec(path = "employee.manager.lastName", params = "searchString", spec = LikeIgnoreCase.class),
	@Spec(path = "d.firstName", params = "searchString", spec = LikeIgnoreCase.class),
	@Spec(path = "d.lastName", params = "searchString", spec = LikeIgnoreCase.class) })
public interface EmployeeCheclistFilterSpecification extends Specification<EmployeeChecklistEntity> {}
