package com.groww.infra.temporal.opentelemetry;

import com.groww.infra.temporal.opentelemetry.internal.ContextAccessor;
import com.groww.infra.temporal.opentelemetry.internal.OpenTelemetryActivityInboundCallsInterceptor;
import com.groww.infra.temporal.opentelemetry.internal.OpenTelemetryWorkflowInboundCallsInterceptor;
import com.groww.infra.temporal.opentelemetry.internal.SpanFactory;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptor;
import io.temporal.common.interceptors.WorkerInterceptor;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptor;

public class OpenTelemetryWorkerInterceptor implements WorkerInterceptor {
  private final OpenTelemetryOptions options;
  private final SpanFactory spanFactory;
  private final ContextAccessor contextAccessor;

  public OpenTelemetryWorkerInterceptor() {
    this(OpenTelemetryOptions.getDefaultInstance());
  }

  public OpenTelemetryWorkerInterceptor(OpenTelemetryOptions options) {
    this.options = options;
    this.spanFactory = new SpanFactory(options);
    this.contextAccessor = new ContextAccessor(options);
  }

  @Override
  public WorkflowInboundCallsInterceptor interceptWorkflow(WorkflowInboundCallsInterceptor next) {
    return new OpenTelemetryWorkflowInboundCallsInterceptor(
        next, options, spanFactory, contextAccessor);
  }

  @Override
  public ActivityInboundCallsInterceptor interceptActivity(ActivityInboundCallsInterceptor next) {
    return new OpenTelemetryActivityInboundCallsInterceptor(
        next, options, spanFactory, contextAccessor);
  }
}
