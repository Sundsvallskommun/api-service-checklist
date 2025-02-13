package se.sundsvall.checklist.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import se.sundsvall.dept44.models.api.paging.PagingMetaData;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Model for a paginated list of events")
@EqualsAndHashCode(callSuper = true)
public class Events extends PagingMetaData {

	private List<Event> eventList;
}
