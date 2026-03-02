package se.sundsvall.checklist.configuration;

import java.io.Serial;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.StdSerializer;

@Component
public class HttpStatusCodeJacksonModule extends SimpleModule {

	@Serial
	private static final long serialVersionUID = 1L;

	public HttpStatusCodeJacksonModule() {
		addSerializer(HttpStatusCode.class, new HttpStatusCodeSerializer());
		addDeserializer(HttpStatusCode.class, new HttpStatusCodeDeserializer());
	}

	private static class HttpStatusCodeSerializer extends StdSerializer<HttpStatusCode> {

		@Serial
		private static final long serialVersionUID = 1L;

		HttpStatusCodeSerializer() {
			super(HttpStatusCode.class);
		}

		@Override
		public void serialize(HttpStatusCode value, JsonGenerator gen, SerializationContext ctxt) {
			gen.writeNumber(value.value());
		}
	}

	private static class HttpStatusCodeDeserializer extends StdDeserializer<HttpStatusCode> {

		@Serial
		private static final long serialVersionUID = 1L;

		HttpStatusCodeDeserializer() {
			super(HttpStatusCode.class);
		}

		@Override
		public HttpStatusCode deserialize(JsonParser p, DeserializationContext ctxt) {
			return HttpStatus.valueOf(p.getIntValue());
		}
	}
}
