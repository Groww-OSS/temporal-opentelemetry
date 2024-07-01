package com.groww.infra.temporal.opentelemetry.internal;

import com.groww.infra.temporal.opentelemetry.OpenTelemetryOptions;
import io.opentelemetry.api.internal.ImmutableSpanContext;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptor;
import io.temporal.common.interceptors.WorkflowInboundCallsInterceptorBase;
import io.temporal.common.interceptors.WorkflowOutboundCallsInterceptor;
import io.temporal.internal.sync.DestroyWorkflowThreadError;
import io.temporal.workflow.Workflow;

public class OpenTelemetryWorkflowInboundCallsInterceptor
    extends WorkflowInboundCallsInterceptorBase {
  private final OpenTelemetryOptions options;
  private final SpanFactory spanFactory;
  private final ContextAccessor contextAccessor;

  public OpenTelemetryWorkflowInboundCallsInterceptor(
      WorkflowInboundCallsInterceptor next,
      OpenTelemetryOptions options,
      SpanFactory spanFactory,
      ContextAccessor contextAccessor) {
    super(next);
    this.options = options;
    this.spanFactory = spanFactory;
    this.contextAccessor = contextAccessor;
  }

  @Override
  public void init(WorkflowOutboundCallsInterceptor outboundCalls) {
    super.init(
        new OpenTelemetryWorkflowOutboundCallsInterceptor(
            outboundCalls, options, spanFactory, contextAccessor));
  }

  @Override
  public WorkflowOutput execute(WorkflowInput input) {
    Tracer tracer = options.getTracer();
    TextMapPropagator propagator = options.getPropagator();
    Context rootSpanContext =
        contextAccessor.readSpanContextFromHeader(input.getHeader(), propagator);
    Span workflowRunSpan =
        spanFactory
            .createWorkflowRunSpan(
                tracer,
                Workflow.getInfo().getWorkflowType(),
                Workflow.getInfo().getWorkflowId(),
                Workflow.getInfo().getRunId(),
                rootSpanContext)
            .startSpan();
    try (Scope ignored = rootSpanContext.with(workflowRunSpan).makeCurrent()) {
      return super.execute(input);
    } catch (Throwable t) {
      if (t instanceof DestroyWorkflowThreadError) {
        spanFactory.logEviction(workflowRunSpan);
      } else {
        spanFactory.logFail(workflowRunSpan, t);
      }
      throw t;
    } finally {
      workflowRunSpan.end();
    }
  }

  @Override
  public void handleSignal(SignalInput input) {
    Tracer tracer = options.getTracer();
    TextMapPropagator propagator = options.getPropagator();
    Context rootSpanContext =
        contextAccessor.readSpanContextFromHeader(input.getHeader(), propagator);
    Span workflowSignalSpan =
        spanFactory
            .createWorkflowHandleSignalSpan(
                tracer,
                input.getSignalName(),
                Workflow.getInfo().getWorkflowId(),
                Workflow.getInfo().getRunId(),
                rootSpanContext)
            .startSpan();
    try (Scope ignored = rootSpanContext.with(workflowSignalSpan).makeCurrent()) {
      super.handleSignal(input);
    } catch (Throwable t) {
      if (t instanceof DestroyWorkflowThreadError) {
        spanFactory.logEviction(workflowSignalSpan);
      } else {
        spanFactory.logFail(workflowSignalSpan, t);
      }
      throw t;
    } finally {
      workflowSignalSpan.end();
    }
  }

  @Override
  public QueryOutput handleQuery(QueryInput input) {
    Tracer tracer = options.getTracer();
    TextMapPropagator propagator = options.getPropagator();
    Context rootSpanContext =
        contextAccessor.readSpanContextFromHeader(input.getHeader(), propagator);
    Span workflowQuerySpan =
        spanFactory
            .createWorkflowHandleQuerySpan(tracer, input.getQueryName(), rootSpanContext)
            .startSpan();
    try (Scope ignored = rootSpanContext.with(workflowQuerySpan).makeCurrent()) {
      return super.handleQuery(input);
    } catch (Throwable t) {
      if (t instanceof DestroyWorkflowThreadError) {
        spanFactory.logEviction(workflowQuerySpan);
      } else {
        spanFactory.logFail(workflowQuerySpan, t);
      }
      throw t;
    } finally {
      workflowQuerySpan.end();
    }
  }

  @Override
  public UpdateOutput executeUpdate(UpdateInput input) {
    Tracer tracer = options.getTracer();
    TextMapPropagator propagator = options.getPropagator();
    Context rootSpanContext =
        contextAccessor.readSpanContextFromHeader(input.getHeader(), propagator);
    Span workflowSignalSpan =
        spanFactory
            .createWorkflowExecuteUpdateSpan(
                tracer,
                input.getUpdateName(),
                Workflow.getInfo().getWorkflowId(),
                Workflow.getInfo().getRunId(),
                rootSpanContext)
            .startSpan();
    try (Scope ignored = rootSpanContext.with(workflowSignalSpan).makeCurrent()) {
      return super.executeUpdate(input);
    } catch (Throwable t) {
      if (t instanceof DestroyWorkflowThreadError) {
        spanFactory.logEviction(workflowSignalSpan);
      } else {
        spanFactory.logFail(workflowSignalSpan, t);
      }
      throw t;
    } finally {
      workflowSignalSpan.end();
    }
  }
}