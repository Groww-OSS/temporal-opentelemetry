package com.groww.infra.temporal.opentelemetry.internal;

import com.groww.infra.temporal.opentelemetry.OpenTelemetryOptions;
import com.groww.infra.temporal.opentelemetry.SpanOperationType;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.temporal.common.interceptors.WorkflowClientCallsInterceptor;
import io.temporal.common.interceptors.WorkflowClientCallsInterceptorBase;

public class OpenTelemetryWorkflowClientCallsInterceptor extends WorkflowClientCallsInterceptorBase {
  private final SpanFactory spanFactory;
  private final Tracer tracer;
  private final TextMapPropagator propagator;
  private final ContextAccessor contextAccessor;

  public OpenTelemetryWorkflowClientCallsInterceptor(
      WorkflowClientCallsInterceptor next,
      OpenTelemetryOptions options,
      SpanFactory spanFactory,
      ContextAccessor contextAccessor) {
    super(next);
    this.spanFactory = spanFactory;
    this.propagator = options.getPropagator();
    this.tracer = options.getTracer();
    this.contextAccessor = contextAccessor;
  }

  @Override
  public WorkflowStartOutput start(WorkflowStartInput input) {
    Span workflowStartSpan =
        contextAccessor.writeSpanContextToHeader(
            () -> createWorkflowStartSpanBuilder(input, SpanOperationType.START_WORKFLOW).startSpan(),
            input.getHeader(),
            propagator);
    try (Scope ignored = workflowStartSpan.makeCurrent()) {
      return super.start(input);
    } finally {
      workflowStartSpan.end();
    }
  }

  @Override
  public WorkflowSignalOutput signal(WorkflowClientCallsInterceptor.WorkflowSignalInput input) {
    Span workflowSignalSpan =
        contextAccessor.writeSpanContextToHeader(
            () ->
                spanFactory
                    .createWorkflowSignalSpan(
                        tracer,
                        input.getSignalName(),
                        input.getWorkflowExecution().getWorkflowId(),
                        input.getWorkflowExecution().getRunId())
                    .startSpan(),
            input.getHeader(),
            propagator);
    try (Scope ignored = workflowSignalSpan.makeCurrent()) {
      return super.signal(input);
    } finally {
      workflowSignalSpan.end();
    }
  }

  @Override
  public WorkflowSignalWithStartOutput signalWithStart(WorkflowSignalWithStartInput input) {
    WorkflowStartInput workflowStartInput = input.getWorkflowStartInput();
    Span workflowStartSpan =
        contextAccessor.writeSpanContextToHeader(
            () ->
                createWorkflowStartSpanBuilder(
                    workflowStartInput, SpanOperationType.SIGNAL_WITH_START_WORKFLOW)
                    .startSpan(),
            workflowStartInput.getHeader(),
            propagator);
    try (Scope ignored = workflowStartSpan.makeCurrent()) {
      return super.signalWithStart(input);
    } finally {
      workflowStartSpan.end();
    }
  }

  @Override
  public <R> QueryOutput<R> query(QueryInput<R> input) {
    Span workflowQuerySpan =
        contextAccessor.writeSpanContextToHeader(
            () ->
                spanFactory
                    .createWorkflowQuerySpan(
                        tracer,
                        input.getQueryType(),
                        input.getWorkflowExecution().getWorkflowId(),
                        input.getWorkflowExecution().getRunId())
                    .startSpan(),
            input.getHeader(),
            propagator);
    try (Scope ignored = workflowQuerySpan.makeCurrent()) {
      return super.query(input);
    } finally {
      workflowQuerySpan.end();
    }
  }

  @Override
  public <R> StartUpdateOutput<R> startUpdate(StartUpdateInput<R> input) {
    Span workflowStartUpdateSpan =
        contextAccessor.writeSpanContextToHeader(
            () ->
                spanFactory
                    .createWorkflowStartUpdateSpan(
                        tracer,
                        input.getUpdateName(),
                        input.getWorkflowExecution().getWorkflowId(),
                        input.getWorkflowExecution().getRunId())
                    .startSpan(),
            input.getHeader(),
            propagator);
    try (Scope ignored = workflowStartUpdateSpan.makeCurrent()) {
      return super.startUpdate(input);
    } finally {
      workflowStartUpdateSpan.end();
    }
  }

  private SpanBuilder createWorkflowStartSpanBuilder(
      WorkflowStartInput input, SpanOperationType operationType) {
    return spanFactory.createWorkflowStartSpan(
        tracer, operationType, input.getWorkflowType(), input.getWorkflowId());
  }
}
