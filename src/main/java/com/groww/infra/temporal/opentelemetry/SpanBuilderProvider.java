package com.groww.infra.temporal.opentelemetry;

import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;

public interface SpanBuilderProvider {
  SpanBuilder createSpanBuilder(Tracer tracer, SpanCreationContext spanCreationContext);
}