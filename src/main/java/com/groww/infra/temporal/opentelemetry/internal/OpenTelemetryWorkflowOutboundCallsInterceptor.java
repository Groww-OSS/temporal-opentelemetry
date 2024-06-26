package com.groww.infra.temporal.opentelemetry.internal;

import com.google.common.base.MoreObjects;
import com.groww.infra.temporal.opentelemetry.OpenTelemetryOptions;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.temporal.common.interceptors.WorkflowOutboundCallsInterceptor;
import io.temporal.common.interceptors.WorkflowOutboundCallsInterceptorBase;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInfo;
import io.temporal.workflow.unsafe.WorkflowUnsafe;

public class OpenTelemetryWorkflowOutboundCallsInterceptor
    extends WorkflowOutboundCallsInterceptorBase {
  private final SpanFactory spanFactory;
  private final Tracer tracer;
  private final TextMapPropagator propagator;
  private final ContextAccessor contextAccessor;

  public OpenTelemetryWorkflowOutboundCallsInterceptor(
      WorkflowOutboundCallsInterceptor next,
      OpenTelemetryOptions options,
      SpanFactory spanFactory,
      ContextAccessor contextAccessor) {
    super(next);
    this.spanFactory = spanFactory;
    this.tracer = options.getTracer();
    this.propagator = options.getPropagator();
    this.contextAccessor = contextAccessor;
  }

  @Override
  public <R> ActivityOutput<R> executeActivity(ActivityInput<R> input) {
    if (!WorkflowUnsafe.isReplaying()) {
      Span activityStartSpan =
          contextAccessor.writeSpanContextToHeader(
              () -> createActivityStartSpanBuilder(input.getActivityName()).startSpan(),
              input.getHeader(),
              propagator);
      try (Scope ignored = activityStartSpan.makeCurrent()) {
        return super.executeActivity(input);
      } finally {
        activityStartSpan.end();
      }
    } else {
      return super.executeActivity(input);
    }
  }

  @Override
  public <R> LocalActivityOutput<R> executeLocalActivity(LocalActivityInput<R> input) {
    if (!WorkflowUnsafe.isReplaying()) {
      Span activityStartSpan =
          contextAccessor.writeSpanContextToHeader(
              () -> createActivityStartSpanBuilder(input.getActivityName()).startSpan(),
              input.getHeader(),
              propagator);
      try (Scope ignored = activityStartSpan.makeCurrent()) {
        return super.executeLocalActivity(input);
      } finally {
        activityStartSpan.end();
      }
    } else {
      return super.executeLocalActivity(input);
    }
  }

  @Override
  public <R> ChildWorkflowOutput<R> executeChildWorkflow(ChildWorkflowInput<R> input) {
    if (!WorkflowUnsafe.isReplaying()) {
      Span childWorkflowStartSpan =
          contextAccessor.writeSpanContextToHeader(
              () -> createChildWorkflowStartSpanBuilder(input).startSpan(), input.getHeader(), propagator);
      try (Scope ignored = childWorkflowStartSpan.makeCurrent()) {
        return super.executeChildWorkflow(input);
      } finally {
        childWorkflowStartSpan.end();
      }
    } else {
      return super.executeChildWorkflow(input);
    }
  }

  @Override
  public SignalExternalOutput signalExternalWorkflow(SignalExternalInput input) {
    if (!WorkflowUnsafe.isReplaying()) {
      WorkflowInfo workflowInfo = Workflow.getInfo();
      Span childWorkflowStartSpan =
          contextAccessor.writeSpanContextToHeader(
              () ->
                  spanFactory
                      .createExternalWorkflowSignalSpan(
                          tracer,
                          input.getSignalName(),
                          workflowInfo.getWorkflowId(),
                          workflowInfo.getRunId())
                      .startSpan(),
              input.getHeader(),
              propagator);
      try (Scope ignored = childWorkflowStartSpan.makeCurrent()) {
        return super.signalExternalWorkflow(input);
      } finally {
        childWorkflowStartSpan.end();
      }
    } else {
      return super.signalExternalWorkflow(input);
    }
  }

  @Override
  public void continueAsNew(ContinueAsNewInput input) {
    if (!WorkflowUnsafe.isReplaying()) {
      Span continueAsNewStartSpan =
          contextAccessor.writeSpanContextToHeader(
              () -> createContinueAsNewWorkflowStartSpanBuilder(input).startSpan(),
              input.getHeader(),
              propagator);
      try (Scope ignored = continueAsNewStartSpan.makeCurrent()) {
        super.continueAsNew(input);
      } finally {
        continueAsNewStartSpan.end();
      }
    } else {
      super.continueAsNew(input);
    }
  }

  @Override
  public Object newChildThread(Runnable runnable, boolean detached, String name) {
    Span activeSpan = Span.current();
    Runnable wrappedRunnable =
        activeSpan != null
            ? () -> {
              // transfer the existing active span into another thread
              try (Scope ignored = activeSpan.makeCurrent()) {
                runnable.run();
              }
            }
            : runnable;
    return super.newChildThread(wrappedRunnable, detached, name);
  }

  private SpanBuilder createActivityStartSpanBuilder(String activityName) {
    WorkflowInfo workflowInfo = Workflow.getInfo();
    return spanFactory.createActivityStartSpan(
        tracer, activityName, workflowInfo.getWorkflowId(), workflowInfo.getRunId());
  }

  private <R> SpanBuilder createChildWorkflowStartSpanBuilder(ChildWorkflowInput<R> input) {
    WorkflowInfo parentWorkflowInfo = Workflow.getInfo();
    return spanFactory.createChildWorkflowStartSpan(
        tracer,
        input.getWorkflowType(),
        input.getWorkflowId(),
        Workflow.currentTimeMillis(),
        parentWorkflowInfo.getWorkflowId(),
        parentWorkflowInfo.getRunId());
  }

  private SpanBuilder createContinueAsNewWorkflowStartSpanBuilder(ContinueAsNewInput input) {
    WorkflowInfo continuedWorkflowInfo = Workflow.getInfo();
    return spanFactory.createContinueAsNewWorkflowStartSpan(
        tracer,
        MoreObjects.firstNonNull(input.getWorkflowType(), continuedWorkflowInfo.getWorkflowType()),
        continuedWorkflowInfo.getWorkflowId(),
        continuedWorkflowInfo.getRunId());
  }
}