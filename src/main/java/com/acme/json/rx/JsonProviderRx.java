package com.acme.json.rx;

import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import jakarta.json.spi.JsonProvider;
import jakarta.json.stream.JsonGenerator;
import jakarta.json.stream.JsonGeneratorFactory;

public final class JsonProviderRx extends JsonProviderDelegate {

    private JsonProviderRx(JsonProvider delegate) {
        super(delegate);
    }

    public static JsonProviderRx create(JsonProvider delegate) {
        return new JsonProviderRx(delegate);
    }

    @Override
    public JsonGenerator createGenerator(OutputStream os) {
        if (os instanceof JsonMultiOutputStream stream) {
            return new JsonGeneratorRx(stream, delegate::createGenerator);
        }
        throw new IllegalArgumentException("Not an instance of " + JsonMultiOutputStream.class);
    }

    @Override
    public JsonGeneratorFactory createGeneratorFactory(Map<String, ?> config) {
        JsonGeneratorFactory delegate = super.createGeneratorFactory(config);
        return new JsonGeneratorFactoryRx(delegate);
    }

    private static final class JsonGeneratorFactoryRx extends JsonGeneratorFactoryDelegate {

        JsonGeneratorFactoryRx(JsonGeneratorFactory delegate) {
            super(delegate);
        }

        @Override
        public JsonGenerator createGenerator(OutputStream os) {
            if (os instanceof JsonMultiOutputStream stream) {
                return new JsonGeneratorRx(stream, super::createGenerator);
            }
            throw new IllegalArgumentException("Not an instance of " + JsonMultiOutputStream.class);
        }

        @Override
        public JsonGenerator createGenerator(OutputStream os, Charset ignored) {
            return createGenerator(os);
        }
    }
}
