package se.sundsvall.checklist.integration.employee;

import static org.zalando.problem.Status.INTERNAL_SERVER_ERROR;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zalando.problem.Problem;

/**
 * Builder for creating a query string for the employee service. Booleans are Boolean objects to allow for null values.
 */
@Getter
@Setter
public class EmployeeFilterBuilder {

	private static final Logger LOG = LoggerFactory.getLogger(EmployeeFilterBuilder.class);

	@JsonIgnore
	private ObjectMapper objectMapper;

	public EmployeeFilterBuilder() {
		objectMapper = new ObjectMapper()
			.registerModule(new JavaTimeModule()) // Parsing LocalDate
			.setSerializationInclusion(JsonInclude.Include.NON_NULL); // Not including null values
	}

	@JsonProperty("CompanyId")
	private List<Integer> companyIds;

	@JsonProperty("HireDateFrom")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate hireDateFrom;

	@JsonProperty("HireDateTo")
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
	private LocalDate hireDateTo;

	@JsonProperty("IsManual")
	private Boolean isManual;

	@JsonProperty("ShowOnlyNewEmployees")
	private Boolean showOnlyNewEmployees;

	@JsonProperty("PersonId")
	private String personId;

	@JsonProperty("EventInfo")
	private List<String> eventInfo;

	public EmployeeFilterBuilder withCompanyId(Integer companyId) {
		if (this.companyIds == null) {
			this.companyIds = new ArrayList<>();
		}
		this.companyIds.add(companyId);
		return this;
	}

	public EmployeeFilterBuilder withCompanyIds(List<Integer> companyIds) {
		this.companyIds = companyIds;
		return this;
	}

	public EmployeeFilterBuilder withHireDateFrom(LocalDate hireDateFrom) {
		this.hireDateFrom = hireDateFrom;
		return this;
	}

	public EmployeeFilterBuilder withHireDateTo(LocalDate hireDateTo) {
		this.hireDateTo = hireDateTo;
		return this;
	}

	public EmployeeFilterBuilder withManual(Boolean manual) {
		isManual = manual;
		return this;
	}

	public EmployeeFilterBuilder withShowOnlyNewEmployees(Boolean showOnlyNewEmployees) {
		this.showOnlyNewEmployees = showOnlyNewEmployees;
		return this;
	}

	public EmployeeFilterBuilder withPersonId(String personId) {
		this.personId = personId;
		return this;
	}

	public EmployeeFilterBuilder withEventInfo(String... eventInfo) {
		this.eventInfo = Arrays.stream(eventInfo).toList();
		return this;
	}

	/**
	 * Construct a custom JSON filter string.
	 *
	 * @return The filter string.
	 */
	public String build() {
		try {
			return objectMapper.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			LOG.error("Couldn't build filter string for Employee", e);
			throw Problem.valueOf(INTERNAL_SERVER_ERROR, "Couldn't build filter string for Employee");
		}
	}

	/**
	 * Default filter for fetching new employees.
	 *
	 * @return default filter string.
	 */
	public static String buildDefaultNewEmployeeFilter() {
		return getDefaultNewEmployeeFilterBuilder()
			.withHireDateFrom(LocalDate.now().minusDays(30))
			.build();
	}

	/**
	 * Filter for fetching a specific employee by uuid regardless if the employee is new or employed since the beginning of
	 * time.
	 *
	 * @param  uuid the unique id of the employee to fetch
	 * @return      a filter string for provided uuid
	 */
	public static String buildUuidEmployeeFilter(String uuid) {
		return getDefaultNewEmployeeFilterBuilder()
			.withPersonId(uuid)
			.withShowOnlyNewEmployees(false)
			// This is to extend the filter with more employment events
			.withEventInfo("Mover", "Corporate", "Company", "Rehire,Corporate") // Yes, it should be "Rehire,Corporate"
			.build();
	}

	/**
	 * Get default Builder to be able to add more filters.
	 *
	 * @return
	 */
	public static EmployeeFilterBuilder getDefaultNewEmployeeFilterBuilder() {
		return new EmployeeFilterBuilder();
	}
}
