package com.acme.json.rx;

import java.io.OutputStream;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;

import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;

abstract class JsonGeneratorFactoryDelegate implements JsonGeneratorFactory {

    private final JsonGeneratorFactory delegate;

    JsonGeneratorFactoryDelegate(JsonGeneratorFactory delegate) {
        this.delegate = delegate;
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
    public JsonGenerator createGenerator(OutputStream out, Charset charset) {
        return delegate.createGenerator(out, charset);
    }

    @Override
    public Map<String, ?> getConfigInUse() {
        return delegate.getConfigInUse();
    }
}
