package com.acme.json.rx;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonGenerator;

@SuppressWarnings("resource")
final class JsonGeneratorRx implements JsonGenerator {

    private static final Event START_OBJECT = new Event(EventType.START_OBJECT, null, null);
    private static final Event START_ARRAY = new Event(EventType.START_ARRAY, null, null);
    private static final Event END = new Event(EventType.END, null, null);
    private static final Event TRUE = new Event(EventType.TRUE, null, null);
    private static final Event FALSE = new Event(EventType.FALSE, null, null);
    private static final Event NULL = new Event(EventType.NULL, null, null);

    enum EventType {
        START_OBJECT,
        NAMED_START_OBJECT,
        NAMED_KEY,
        START_ARRAY,
        NAMED_START_ARRAY,
        NAMED_STRING,
        NAMED_BIG_INTEGER,
        NAMED_BIG_DECIMAL,
        NAMED_INT,
        NAMED_LONG,
        NAMED_DOUBLE,
        NAMED_BOOLEAN,
        NAMED_NULL,
        END,
        STRING,
        BIG_DECIMAL,
        BIG_INTEGER,
        INT,
        LONG,
        DOUBLE,
        NULL,
        TRUE,
        FALSE
    }

    private record Event(EventType type, String name, Object value) {
    }

    private final Queue<Event> queue = new ArrayDeque<>();
    private final JsonMultiOutputStream stream;
    private final JsonGenerator generator;

    JsonGeneratorRx(JsonMultiOutputStream stream, Function<OutputStream, JsonGenerator> factory) {
        this.generator = factory.apply(stream);
        this.stream = stream.onRequest((n, demand) -> onRequest());
    }

    private void onRequest() {
        stream.drain();
        while (stream.ready() && !queue.isEmpty()) {
            Event event = queue.poll();
            try {
                switch (event.type) {
                    case START_OBJECT -> generator.writeStartObject();
                    case START_ARRAY -> generator.writeStartArray();
                    case END -> generator.writeEnd();
                    case NULL -> generator.writeNull();
                    case TRUE -> generator.write(true);
                    case FALSE -> generator.write(false);
                    case INT -> generator.write((int) event.value);
                    case DOUBLE -> generator.write((double) event.value);
                    case LONG -> generator.write((long) event.value);
                    case STRING -> generator.write((String) event.value);
                    case BIG_DECIMAL -> generator.write((BigDecimal) event.value);
                    case BIG_INTEGER -> generator.write((BigInteger) event.value);
                    case NAMED_START_OBJECT -> generator.writeStartObject(event.name);
                    case NAMED_START_ARRAY -> generator.writeStartArray(event.name);
                    case NAMED_NULL -> generator.writeNull(event.name);
                    case NAMED_BOOLEAN -> generator.write(event.name, (boolean) event.value);
                    case NAMED_INT -> generator.write(event.name, (int) event.value);
                    case NAMED_DOUBLE -> generator.write(event.name, (double) event.value);
                    case NAMED_LONG -> generator.write(event.name, (long) event.value);
                    case NAMED_STRING -> generator.write(event.name, (String) event.value);
                    case NAMED_BIG_DECIMAL -> generator.write(event.name, (BigDecimal) event.value);
                    case NAMED_BIG_INTEGER -> generator.write(event.name, (BigInteger) event.value);
                    case NAMED_KEY -> generator.writeKey(event.name);
                }
            } catch (JsonException ex) {
                stream.fail(ex);
            }
        }
        if (queue.isEmpty()) {
            if (stream.ready()) {
                generator.close();
            }
            if (stream.closed()) {
                stream.complete();
            }
        }
    }

    private JsonGenerator event(Event event) {
        queue.add(event);
        return this;
    }

    private JsonGenerator event(EventType eventType, String name, Object value) {
        return event(new Event(eventType, name, value));
    }

    @Override
    public void close() {
        // no-op
    }

    @Override
    public void flush() {
        // no-op
    }

    @Override
    public JsonGenerator write(String name, JsonValue value) {
        // This method expands complex values into simple values (events)
        // The goal of this class is to avoid storing the serialized bytes in memory,
        // however the current approach stores the whole entity as events, which isn't better
        // TODO expand just-in-time
        switch (value.getValueType()) {
            case ARRAY:
                JsonArray array = (JsonArray) value;
                writeStartArray(name);
                for (JsonValue child : array) {
                    write(child);
                }
                writeEnd();
                break;
            case OBJECT:
                JsonObject object = (JsonObject) value;
                writeStartObject(name);
                for (Map.Entry<String, JsonValue> member : object.entrySet()) {
                    write(member.getKey(), member.getValue());
                }
                writeEnd();
                break;
            case STRING:
                write(name, ((JsonString) value).getString());
                break;
            case NUMBER:
                write(name, value.toString());
                break;
            case TRUE:
                write(name, true);
                break;
            case FALSE:
                write(name, false);
                break;
            case NULL:
                writeNull(name);
                break;
        }
        return this;
    }

    @Override
    public JsonGenerator write(JsonValue value) {
        switch (value.getValueType()) {
            case ARRAY:
                JsonArray array = (JsonArray) value;
                writeStartArray();
                for (JsonValue child : array) {
                    write(child);
                }
                writeEnd();
                break;
            case OBJECT:
                JsonObject object = (JsonObject) value;
                writeStartObject();
                for (Map.Entry<String, JsonValue> member : object.entrySet()) {
                    write(member.getKey(), member.getValue());
                }
                writeEnd();
                break;
            case STRING:
                JsonString str = (JsonString) value;
                write(str.getString());
                break;
            case NUMBER:
                JsonNumber number = (JsonNumber) value;
                write(number.toString());
                break;
            case TRUE:
                write(true);
                break;
            case FALSE:
                write(false);
                break;
            case NULL:
                writeNull();
                break;
        }
        return this;
    }

    @Override
    public JsonGenerator writeStartObject() {
        return event(START_OBJECT);
    }

    @Override
    public JsonGenerator writeStartObject(String name) {
        return event(EventType.NAMED_START_OBJECT, name, null);
    }

    @Override
    public JsonGenerator writeKey(String name) {
        return event(EventType.NAMED_KEY, name, null);
    }

    @Override
    public JsonGenerator writeStartArray() {
        return event(START_ARRAY);
    }

    @Override
    public JsonGenerator writeStartArray(String name) {
        return event(EventType.NAMED_START_ARRAY, name, null);
    }

    @Override
    public JsonGenerator write(String name, String value) {
        return event(EventType.NAMED_STRING, name, value);
    }

    @Override
    public JsonGenerator write(String name, BigInteger value) {
        return event(EventType.NAMED_BIG_INTEGER, name, value);
    }

    @Override
    public JsonGenerator write(String name, BigDecimal value) {
        return event(EventType.NAMED_BIG_DECIMAL, name, value);
    }

    @Override
    public JsonGenerator write(String name, int value) {
        return event(EventType.NAMED_INT, name, value);
    }

    @Override
    public JsonGenerator write(String name, long value) {
        return event(EventType.NAMED_LONG, name, value);
    }

    @Override
    public JsonGenerator write(String name, double value) {
        return event(EventType.NAMED_DOUBLE, name, value);
    }

    @Override
    public JsonGenerator write(String name, boolean value) {
        return event(EventType.NAMED_BOOLEAN, name, value);
    }

    @Override
    public JsonGenerator writeNull(String name) {
        return event(EventType.NAMED_NULL, name, null);
    }

    @Override
    public JsonGenerator writeEnd() {
        return event(END);
    }

    @Override
    public JsonGenerator write(String value) {
        return event(EventType.STRING, null, value);
    }

    @Override
    public JsonGenerator write(BigDecimal value) {
        return event(EventType.BIG_DECIMAL, null, value);
    }

    @Override
    public JsonGenerator write(BigInteger value) {
        return event(EventType.BIG_INTEGER, null, value);
    }

    @Override
    public JsonGenerator write(int value) {
        return event(EventType.INT, null, value);
    }

    @Override
    public JsonGenerator write(long value) {
        return event(EventType.LONG, null, value);
    }

    @Override
    public JsonGenerator write(double value) {
        return event(EventType.DOUBLE, null, value);
    }

    @Override
    public JsonGenerator write(boolean value) {
        return event(value ? TRUE : FALSE);
    }

    @Override
    public JsonGenerator writeNull() {
        return event(NULL);
    }
}
