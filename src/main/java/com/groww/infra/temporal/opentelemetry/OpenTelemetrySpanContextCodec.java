package com.groww.infra.temporal.opentelemetry;

import com.groww.infra.temporal.opentelemetry.codec.TextMapInjectExtractCodec;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;

import java.util.Map;

public interface OpenTelemetrySpanContextCodec {
  // default implementation
  OpenTelemetrySpanContextCodec TEXT_MAP_INJECT_EXTRACT_CODEC = TextMapInjectExtractCodec.INSTANCE;

  Map<String, String> encode(Context spanContext, TextMapPropagator propagator);

  Context decode(Map<String, String> serializedSpanContext, TextMapPropagator propagator);
}