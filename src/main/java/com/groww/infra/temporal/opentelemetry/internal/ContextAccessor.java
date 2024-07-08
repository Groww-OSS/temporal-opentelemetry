package com.groww.infra.temporal.opentelemetry.internal;

import com.google.common.reflect.TypeToken;
import com.groww.infra.temporal.opentelemetry.OpenTelemetryOptions;
import com.groww.infra.temporal.opentelemetry.OpenTelemetrySpanContextCodec;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.temporal.api.common.v1.Payload;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.common.converter.StdConverterBackwardsCompatAdapter;
import io.temporal.common.interceptors.Header;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ContextAccessor {
  private static final String TRACER_HEADER_KEY = "_tracer-data";
  private static final Type HASH_MAP_STRING_STRING_TYPE =
      new TypeToken<HashMap<String, String>>() {
      }.getType();

  private final OpenTelemetrySpanContextCodec codec;

  public ContextAccessor(OpenTelemetryOptions options) {
    this.codec = options.getSpanContextCodec();
  }

  public Span writeSpanContextToHeader(
      Supplier<Span> spanSupplier, Header toHeader, TextMapPropagator propagator) {
    Span span = spanSupplier.get();
    writeSpanContextToHeader(Context.current().with(span), toHeader, propagator);
    return span;
  }

  public void writeSpanContextToHeader(Context context, Header header, TextMapPropagator propagator) {
    Map<String, String> serializedSpanContext = codec.encode(context, propagator);
    Optional<Payload> payload =
        DefaultDataConverter.STANDARD_INSTANCE.toPayload(serializedSpanContext);
    header.getValues().put(TRACER_HEADER_KEY, payload.get());
  }

  public Context readSpanContextFromHeader(Header header, TextMapPropagator propagator) {
    Payload payload = header.getValues().get(TRACER_HEADER_KEY);
    if (payload == null) {
      return Context.current();
    }
    @SuppressWarnings("unchecked")
    Map<String, String> serializedSpanContext =
        StdConverterBackwardsCompatAdapter.fromPayload(
            payload, HashMap.class, HASH_MAP_STRING_STRING_TYPE);
    return codec.decode(serializedSpanContext, propagator);
  }
}
