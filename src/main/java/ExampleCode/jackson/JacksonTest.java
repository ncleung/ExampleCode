package ExampleCode.jackson;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;

public class JacksonTest {

    public static interface IntegerInterface {
        Integer getValue();
        void setValue(Integer val);
    }

    public static class Number implements IntegerInterface {
        private Integer value;

        public Number() {
            value = 0;
        }
        public Number(int v) {
            value = v;
        }
        @Override
        public String toString() {
            return value.toString();
        }
        @Override
        public Integer getValue() { return value; }
        @Override
        public void setValue(Integer val) { value = val; }
    }

    public static class IntegerInterfaceSerializer extends com.fasterxml.jackson.databind.JsonSerializer<IntegerInterface> {

        @Override
        public void serialize(IntegerInterface num, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeObjectField("type", num.getClass().getName());
            jsonGenerator.writeObjectField("my value is", num.getValue());
            jsonGenerator.writeEndObject();
        }

        @Override
        public Class<IntegerInterface> handledType() {
            return IntegerInterface.class;
        }
    }

    public static class IntegerInterfaceDeserializer extends JsonDeserializer<IntegerInterface> {
        @Override
        public IntegerInterface deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            ObjectMapper mapper = (ObjectMapper)jsonParser.getCodec();
            TreeNode node = mapper.readTree(jsonParser);
            String typeString = ((JsonNode)node.get("type")).asText();
            IntegerInterface newInstance;
            try {
                newInstance = (IntegerInterface)Class.forName(typeString).newInstance();
            } catch (Exception e) {
                return null;
            }
            newInstance.setValue(((JsonNode)node.get("my value is")).asInt());
            return newInstance;
        }
    }

    public static void main(String args[]) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        SimpleModule module = new SimpleModule();
        module.addSerializer(IntegerInterface.class, new IntegerInterfaceSerializer());
        module.addDeserializer(IntegerInterface.class, new IntegerInterfaceDeserializer());
        mapper.registerModule(module);

        Number number = new Number(1234);

        String numberString = mapper.writerWithType(Number.class).writeValueAsString(number);
        System.out.println("Number string: " + numberString);
        IntegerInterface deserNumber0 = mapper.readValue(numberString, IntegerInterface.class);
        System.out.println("Deserialized Number (IntegerInterface.class): " + deserNumber0);
        IntegerInterface deserNumber1 = mapper.readValue(numberString, Number.class);
        System.out.println("Deserialized Number (Number.class): " + deserNumber1);
    }
}

