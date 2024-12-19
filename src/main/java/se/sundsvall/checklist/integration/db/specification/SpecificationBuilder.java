package se.sundsvall.checklist.integration.db.specification;

import static java.util.Objects.nonNull;

import jakarta.persistence.criteria.Expression;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public class SpecificationBuilder<T> {

	/**
	 * Retrieves the given entities employee attributes firstname and lastname and concatenates them to a full name. Then it
	 * compares the full name to the given value using a like ignore case predicate.
	 *
	 * @param  value the value to compare the full name to
	 * @return       a specification that compares the full name to the given value
	 */
	public Specification<T> buildEmployeeNameFilter(final String value) {
		return (entity, cq, cb) -> {
			if (value == null) {
				return null;
			}

			Expression<String> firstNameExpression = entity.get("employee").get("firstName");
			Expression<String> lastNameExpression = entity.get("employee").get("lastName");

			var fullNameExpression = cb.concat(
				cb.concat(cb.lower(firstNameExpression), cb.literal(" ")),
				cb.lower(lastNameExpression));

			return cb.like(fullNameExpression, "%" + value.toLowerCase() + "%");
		};
	}

	/**
	 * Retrieves the given entities employee's company's municipality id and compares it to the given value using an equal
	 * predicate.
	 *
	 * @param  value the value to compare the municipality id to
	 * @return       a specification that compares the municipality id to the given value
	 */
	public Specification<T> buildMunicipalityIdFilter(final String value) {
		return (entity, cq, cb) -> {
			if (value == null) {
				return null;
			}
			return cb.equal(entity.get("employee").get("company").get("municipalityId"), value);
		};
	}

	public Specification<T> buildStartDateEqualOrBeforeFilter(final String attribute, final LocalDate value) {
		return (entity, cq, cb) -> nonNull(value) ? cb.lessThanOrEqualTo(entity.get(attribute), value) : cb.and();
	}

	public Specification<T> buildEndDateAfterFilter(final String attribute, final LocalDate value) {
		return (entity, cq, cb) -> nonNull(value) ? cb.greaterThanOrEqualTo(entity.get(attribute), value) : cb.and();
	}

	/**
	 * Creates a distinct specification, to avoid any duplicates in the result.
	 *
	 * @return a specification that makes the result distinct
	 */
	public Specification<T> distinct() {
		return (entity, cq, cb) -> {
			cq.distinct(true);
			return null;
		};
	}

}
