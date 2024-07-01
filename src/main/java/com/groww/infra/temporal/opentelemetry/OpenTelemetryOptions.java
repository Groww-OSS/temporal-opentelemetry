package com.groww.infra.temporal.opentelemetry;

import com.google.common.base.MoreObjects;
import com.groww.infra.temporal.opentelemetry.internal.ActionTypeAndNameSpanBuilderProvider;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.propagation.TextMapPropagator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Predicate;

public class OpenTelemetryOptions {
  private static final OpenTelemetryOptions DEFAULT_INSTANCE =
      OpenTelemetryOptions.newBuilder().build();

  private final Tracer tracer;
  private final TextMapPropagator propagator;
  private final SpanBuilderProvider spanBuilderProvider;
  private final OpenTelemetrySpanContextCodec spanContextCodec;
  private final Predicate<Throwable> isErrorPredicate;

  private OpenTelemetryOptions(
      Tracer tracer,
      TextMapPropagator propagator,
      SpanBuilderProvider spanBuilderProvider,
      OpenTelemetrySpanContextCodec spanContextCodec,
      Predicate<Throwable> isErrorPredicate) {
    if (tracer == null) throw new IllegalArgumentException("tracer shouldn't be null");
    if (propagator == null) throw new IllegalArgumentException("propagator shouldn't be null");
    this.tracer = tracer;
    this.propagator = propagator;
    this.spanBuilderProvider = spanBuilderProvider;
    this.spanContextCodec = spanContextCodec;
    this.isErrorPredicate = isErrorPredicate;
  }

  public static OpenTelemetryOptions getDefaultInstance() {
    return DEFAULT_INSTANCE;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  @Nonnull
  public Tracer getTracer() {
    return tracer;
  }

  @Nonnull
  public TextMapPropagator getPropagator() {
    return propagator;
  }

  @Nonnull
  public SpanBuilderProvider getSpanBuilderProvider() {
    return spanBuilderProvider;
  }

  @Nonnull
  public OpenTelemetrySpanContextCodec getSpanContextCodec() {
    return spanContextCodec;
  }

  @Nonnull
  public Predicate<Throwable> getIsErrorPredicate() {
    return isErrorPredicate;
  }

  public static final class Builder {
    private Tracer tracer;
    private TextMapPropagator propagator;
    private SpanBuilderProvider spanBuilderProvider = ActionTypeAndNameSpanBuilderProvider.INSTANCE;
    private OpenTelemetrySpanContextCodec spanContextCodec =
        OpenTelemetrySpanContextCodec.TEXT_MAP_INJECT_EXTRACT_CODEC;
    private Predicate<Throwable> isErrorPredicate = t -> true;

    private Builder() {
    }

    public Builder setTracer(@Nullable Tracer tracer) {
      this.tracer = tracer;
      return this;
    }

    public Builder setSpanBuilderProvider(@Nonnull SpanBuilderProvider spanBuilderProvider) {
      Objects.requireNonNull(spanBuilderProvider, "spanBuilderProvider can't be null");
      this.spanBuilderProvider = spanBuilderProvider;
      return this;
    }

    /**
     * @param spanContextCodec custom {@link OpenTelemetrySpanContextCodec}, allows for more control
     *                         over how SpanContext is encoded and decoded from Map
     * @return this
     */
    public Builder setSpanContextCodec(@Nonnull OpenTelemetrySpanContextCodec spanContextCodec) {
      Objects.requireNonNull(spanContextCodec, "spanContextCodec can't be null");
      this.spanContextCodec = spanContextCodec;
      return this;
    }

    public Builder setIsErrorPredicate(@Nonnull Predicate<Throwable> isErrorPredicate) {
      Objects.requireNonNull(isErrorPredicate, "isErrorPredicate can't be null");
      this.isErrorPredicate = isErrorPredicate;
      return this;
    }

    public OpenTelemetryOptions build() {
      return new OpenTelemetryOptions(
          MoreObjects.firstNonNull(tracer, GlobalOpenTelemetry.get().getTracerProvider().tracerBuilder("temporal").build()),
          MoreObjects.firstNonNull(propagator, GlobalOpenTelemetry.getPropagators().getTextMapPropagator()),
          spanBuilderProvider,
          spanContextCodec,
          isErrorPredicate);
    }
  }
}