package se.sundsvall.checklist.api.model;

import static io.swagger.v3.oas.annotations.media.Schema.AccessMode.READ_ONLY;
import static org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import se.sundsvall.checklist.integration.db.model.enums.CommunicationChannel;
import se.sundsvall.checklist.integration.db.model.enums.CorrespondenceStatus;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@Builder(setterPrefix = "with")
@Schema(description = "Model for correspondence")
public class Correspondence {

	@Schema(description = "Id for message", examples = "3fa85f64-5717-4562-b3fc-2c963f66afa6", accessMode = READ_ONLY)
	private String messageId;

	@Schema(description = "Message recipient", examples = "email.address@noreply.com", accessMode = READ_ONLY)
	private String recipient;

	@Schema(description = "Attempt count", examples = "1", accessMode = READ_ONLY)
	private int attempts;

	@Schema(description = "Status for correspondence", accessMode = READ_ONLY)
	private CorrespondenceStatus correspondenceStatus;

	@Schema(description = "Communicationschannel used for message", accessMode = READ_ONLY)
	private CommunicationChannel communicationChannel;

	@Schema(description = "Timestamp when message was sent", examples = "2023-11-22T15:30:00+02:00", accessMode = READ_ONLY)
	@DateTimeFormat(iso = DATE_TIME)
	private OffsetDateTime sent;
}
