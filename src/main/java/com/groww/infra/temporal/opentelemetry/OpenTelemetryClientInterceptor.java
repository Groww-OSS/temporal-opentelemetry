package com.groww.infra.temporal.opentelemetry;

import com.groww.infra.temporal.opentelemetry.internal.ContextAccessor;
import com.groww.infra.temporal.opentelemetry.internal.OpenTelemetryWorkflowClientCallsInterceptor;
import com.groww.infra.temporal.opentelemetry.internal.SpanFactory;
import io.temporal.common.interceptors.WorkflowClientCallsInterceptor;
import io.temporal.common.interceptors.WorkflowClientInterceptorBase;

public class OpenTelemetryClientInterceptor extends WorkflowClientInterceptorBase {
  private final OpenTelemetryOptions options;
  private final SpanFactory spanFactory;
  private final ContextAccessor contextAccessor;

  public OpenTelemetryClientInterceptor() {
    this(OpenTelemetryOptions.getDefaultInstance());
  }

  public OpenTelemetryClientInterceptor(OpenTelemetryOptions options) {
    this.options = options;
    this.spanFactory = new SpanFactory(options);
    this.contextAccessor = new ContextAccessor(options);
  }

  @Override
  public WorkflowClientCallsInterceptor workflowClientCallsInterceptor(
      WorkflowClientCallsInterceptor next) {
    return new OpenTelemetryWorkflowClientCallsInterceptor(
        next, options, spanFactory, contextAccessor);
  }
}
