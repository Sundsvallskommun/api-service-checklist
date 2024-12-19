package se.sundsvall.checklist.integration.db.repository.specification;

import static java.util.Objects.nonNull;
import static org.springframework.util.CollectionUtils.isEmpty;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.CriteriaBuilder.In;

public class SpecificationBuilder<T> {

	/**
	 * Method builds an equal filter if value is not null. If value is null, method returns
	 * an always-true predicate (meaning no filtering will be applied for sent in attribute)
	 *
	 * @param  attribute name that will be used in filter
	 * @param  value     value (or null) to compare against
	 * @return           Specification<T> matching sent in comparison
	 */
	public Specification<T> buildEqualFilter(String attribute, Object value) {
		return (entity, cq, cb) -> nonNull(value) ? cb.equal(entity.get(attribute), value) : cb.and();
	}

	/**
	 * Method builds an equal filter if value is not null. If value is null, method returns
	 * an always-true predicate (meaning no filtering will be applied for sent in attribute)
	 *
	 * @param  attribute name that will be used in filter
	 * @param  value     value (or null) to compare against
	 * @return           Specification<T> matching sent in comparison
	 */
	public Specification<T> buildLikeFilter(String attribute, String value) {
		return (entity, cq, cb) -> nonNull(value) ? cb.like(cb.lower(entity.get(attribute)), "%" + value + "%".toLowerCase()) : cb.and();
	}

	/**
	 * Method builds an equal filter if value is not null. If value is null, method returns
	 * an always-true predicate (meaning no filtering will be applied for sent in attribute)
	 *
	 * @param  level1Attribute  name that will be used for level 1 in filter
	 * @param  level2AAttribute name that will be used for level 2 in filter
	 * @param  value            value (or null) to compare against
	 * @return                  Specification<T> matching sent in comparison
	 */
	public Specification<T> buildLikeFilter(String level1Attribute, String level2Attribute, String value) {
		return (entity, cq, cb) -> nonNull(value) ? cb.like(cb.lower(entity.get(level1Attribute).get(level2Attribute)), "%" + value + "%".toLowerCase()) : cb.and();
	}

	/**
	 * Method builds an equal filter if value is not null. If value is null, method returns
	 * an always-true predicate (meaning no filtering will be applied for sent in attribute)
	 *
	 * @param  level1Attribute  name that will be used for level 1 in filter
	 * @param  level2AAttribute name that will be used for level 2 in filter
	 * @param  level3AAttribute name that will be used for level 2 in filter
	 * @param  value            value (or null) to compare against
	 * @return                  Specification<T> matching sent in comparison
	 */
	public Specification<T> buildLikeFilter(String level1Attribute, String level2Attribute, String level3Attribute, String value) {
		return (entity, cq, cb) -> nonNull(value) ? cb.like(cb.lower(entity.get(level1Attribute).get(level2Attribute).get(level3Attribute)), "%" + value + "%".toLowerCase()) : cb.and();
	}

	/**
	 * Method builds a filter depending on sent in time stamps. If both values are null, method returns
	 * an always-true predicate (meaning no filtering will be applied for sent in attribute)
	 *
	 * @param  attribute    name of attribute (of type date) that will be used in filter
	 * @param  dateTimeFrom date from (or null) to compare against
	 * @param  dateTimeTo   date tom (or null) to compare against
	 * @return              Specification<T> matching sent in comparison
	 */
	public Specification<T> buildDateFilter(String attribute, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
		return (entity, cq, cb) -> {

			if (nonNull(dateTimeFrom) && nonNull(dateTimeTo)) {
				return cb.between(entity.get(attribute), dateTimeFrom, dateTimeTo);
			}
			if (nonNull(dateTimeFrom)) {
				return cb.greaterThanOrEqualTo(entity.get(attribute), dateTimeFrom);
			}
			if (nonNull(dateTimeTo)) {
				return cb.lessThanOrEqualTo(entity.get(attribute), dateTimeTo);
			}

			// always-true predicate, meaning that if no dateFrom or to has been set, no filtering will be applied
			return cb.and();
		};
	}

	/**
	 * Method builds a filter depending on sent in dates. If both values are null, method returns
	 * an always-true predicate (meaning no filtering will be applied for sent in attribute)
	 *
	 * @param  attribute name of attribute (of type date) that will be used in filter
	 * @param  dateFrom  date from (or null) to compare against
	 * @param  dateTom   date tom (or null) to compare against
	 * @return           Specification<T> matching sent in comparison
	 */
	public Specification<T> buildDateFilter(String attribute, LocalDate dateFrom, LocalDate dateTom) {
		return (invoiceEntity, cq, cb) -> {

			if (nonNull(dateFrom) && nonNull(dateTom)) {
				return cb.between(invoiceEntity.get(attribute), dateFrom, dateTom);
			}
			if (nonNull(dateFrom)) {
				return cb.greaterThanOrEqualTo(invoiceEntity.get(attribute), dateFrom);
			}
			if (nonNull(dateTom)) {
				return cb.lessThanOrEqualTo(invoiceEntity.get(attribute), dateTom);
			}

			// always-true predicate, meaning that if no dateFrom or to has been set, no filtering will be applied
			return cb.and();
		};
	}

	/**
	 * Method builds a filter depending on sent in list of string. If values are null, method returns
	 * an always-true predicate (meaning no filtering will be applied for sent in attribute)
	 *
	 * @param  attribute name of attribute that will be used in filter
	 * @param  values    String-values (or null) to compare against
	 * @return           Specification<T> matching sent in comparison
	 */
	public Specification<T> buildInFilterForString(String attribute, List<String> values) {
		return (entity, cq, cb) -> isEmpty(values) ? cb.and() : addToInClauseForString(cb.in(entity.get(attribute)), values);
	}

	/**
	 * Method builds a filter depending on sent in list of Integer. If values are null, method returns
	 * an always-true predicate (meaning no filtering will be applied for sent in attribute)
	 *
	 * @param  attribute name of attribute that will be used in filter
	 * @param  values    Integer-values (or null) to compare against
	 * @return           Specification<T> matching sent in comparison
	 */
	public Specification<T> buildInFilterForInteger(String attribute, List<Integer> values) {
		return (entity, cq, cb) -> isEmpty(values) ? cb.and() : addToInClauseForInteger(cb.in(entity.get(attribute)), values);
	}

	private static In<String> addToInClauseForString(In<String> clause, List<String> values) {
		values.forEach(clause::value);

		return clause;
	}

	private static In<Integer> addToInClauseForInteger(In<Integer> clause, List<Integer> values) {
		values.forEach(clause::value);

		return clause;
	}
}
