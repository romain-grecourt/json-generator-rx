package com.acme.json.rx;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;

import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonMergePatch;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonPatch;
import jakarta.json.JsonPatchBuilder;
import jakarta.json.JsonPointer;
import jakarta.json.JsonReader;
import jakarta.json.JsonReaderFactory;
import jakarta.json.JsonString;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParserFactory;

abstract class JsonProviderDelegate extends JsonProvider {
    final JsonProvider delegate;

    protected JsonProviderDelegate(JsonProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public JsonParser createParser(Reader reader) {
        return delegate.createParser(reader);
    }

    @Override
    public JsonParser createParser(InputStream in) {
        return delegate.createParser(in);
    }

    @Override
    public JsonParserFactory createParserFactory(Map<String, ?> config) {
        return delegate.createParserFactory(config);
    }

    @Override
    public JsonGenerator createGenerator(Writer writer) {
        return delegate.createGenerator(writer);
    }

    @Override
    public JsonGenerator createGenerator(OutputStream out) {
        return delegate.createGenerator(out);
    }

    @Override
    public JsonGeneratorFactory createGeneratorFactory(Map<String, ?> config) {
        return delegate.createGeneratorFactory(config);
    }

    @Override
    public JsonReader createReader(Reader reader) {
        return delegate.createReader(reader);
    }

    @Override
    public JsonReader createReader(InputStream in) {
        return delegate.createReader(in);
    }

    @Override
    public JsonWriter createWriter(Writer writer) {
        return delegate.createWriter(writer);
    }

    @Override
    public JsonWriter createWriter(OutputStream out) {
        return delegate.createWriter(out);
    }

    @Override
    public JsonWriterFactory createWriterFactory(Map<String, ?> config) {
        return delegate.createWriterFactory(config);
    }

    @Override
    public JsonReaderFactory createReaderFactory(Map<String, ?> config) {
        return delegate.createReaderFactory(config);
    }

    @Override
    public JsonObjectBuilder createObjectBuilder() {
        return delegate.createObjectBuilder();
    }

    @Override
    public JsonObjectBuilder createObjectBuilder(JsonObject object) {
        return delegate.createObjectBuilder(object);
    }

    @Override
    public JsonObjectBuilder createObjectBuilder(Map<String, Object> map) {
        return delegate.createObjectBuilder(map);
    }

    @Override
    public JsonArrayBuilder createArrayBuilder() {
        return delegate.createArrayBuilder();
    }

    @Override
    public JsonArrayBuilder createArrayBuilder(JsonArray array) {
        return delegate.createArrayBuilder(array);
    }

    @Override
    public JsonPointer createPointer(String jsonPointer) {
        return delegate.createPointer(jsonPointer);
    }

    @Override
    public JsonPatchBuilder createPatchBuilder() {
        return delegate.createPatchBuilder();
    }

    @Override
    public JsonPatchBuilder createPatchBuilder(JsonArray array) {
        return delegate.createPatchBuilder(array);
    }

    @Override
    public JsonPatch createPatch(JsonArray array) {
        return delegate.createPatch(array);
    }

    @Override
    public JsonPatch createDiff(JsonStructure source, JsonStructure target) {
        return delegate.createDiff(source, target);
    }

    @Override
    public JsonMergePatch createMergePatch(JsonValue patch) {
        return delegate.createMergePatch(patch);
    }

    @Override
    public JsonMergePatch createMergeDiff(JsonValue source, JsonValue target) {
        return delegate.createMergeDiff(source, target);
    }

    @Override
    public JsonArrayBuilder createArrayBuilder(Collection<?> collection) {
        return delegate.createArrayBuilder(collection);
    }

    @Override
    public JsonBuilderFactory createBuilderFactory(Map<String, ?> config) {
        return delegate.createBuilderFactory(config);
    }

    @Override
    public JsonString createValue(String value) {
        return delegate.createValue(value);
    }

    @Override
    public JsonNumber createValue(int value) {
        return delegate.createValue(value);
    }

    @Override
    public JsonNumber createValue(long value) {
        return delegate.createValue(value);
    }

    @Override
    public JsonNumber createValue(double value) {
        return delegate.createValue(value);
    }

    @Override
    public JsonNumber createValue(BigDecimal value) {
        return delegate.createValue(value);
    }

    @Override
    public JsonNumber createValue(BigInteger value) {
        return delegate.createValue(value);
    }
}
