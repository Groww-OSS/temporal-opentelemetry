package com.groww.infra.temporal.opentelemetry.internal;

import com.google.common.base.Throwables;
import com.groww.infra.temporal.opentelemetry.OpenTelemetryOptions;
import com.groww.infra.temporal.opentelemetry.SpanCreationContext;
import com.groww.infra.temporal.opentelemetry.SpanOperationType;
import com.groww.infra.temporal.opentelemetry.StandardTagNames;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

public class SpanFactory {

  private static final Logger logger = LoggerFactory.getLogger(SpanFactory.class);
  private final OpenTelemetryOptions options;

  public SpanFactory(OpenTelemetryOptions options) {
    this.options = options;
  }

  public SpanBuilder createWorkflowStartSpan(
      Tracer tracer, SpanOperationType operationType, String workflowType, String workflowId) {

    SpanCreationContext context =
        SpanCreationContext.newBuilder()
            .setSpanOperationType(operationType)
            .setActionName(workflowType)
            .setWorkflowId(workflowId)
            .build();

    return createSpan(context, tracer, null, SpanKind.CLIENT);
  }

  public SpanBuilder createChildWorkflowStartSpan(
      Tracer tracer,
      String childWorkflowType,
      String childWorkflowId,
      long startTimeMs,
      String parentWorkflowId,
      String parentRunId) {
    SpanCreationContext context =
        SpanCreationContext.newBuilder()
            .setSpanOperationType(SpanOperationType.START_CHILD_WORKFLOW)
            .setActionName(childWorkflowType)
            .setWorkflowId(childWorkflowId)
            .setParentWorkflowId(parentWorkflowId)
            .setParentRunId(parentRunId)
            .build();
    return createSpan(context, tracer, null, SpanKind.CLIENT);
  }

  public SpanBuilder createExternalWorkflowSignalSpan(
      Tracer tracer, String signalName, String workflowId, String runId) {
    SpanCreationContext context =
        SpanCreationContext.newBuilder()
            .setSpanOperationType(SpanOperationType.SIGNAL_EXTERNAL_WORKFLOW)
            .setActionName(signalName)
            .setWorkflowId(workflowId)
            .setRunId(runId)
            .build();
    return createSpan(context, tracer, null, SpanKind.CLIENT);
  }

  public SpanBuilder createWorkflowSignalSpan(
      Tracer tracer, String signalName, String workflowId, String runId) {
    SpanCreationContext context =
        SpanCreationContext.newBuilder()
            .setSpanOperationType(SpanOperationType.SIGNAL_WORKFLOW)
            .setActionName(signalName)
            .setWorkflowId(workflowId)
            .setRunId(runId)
            .build();
    return createSpan(context, tracer, null, SpanKind.CLIENT);
  }

  public SpanBuilder createWorkflowHandleSignalSpan(
      Tracer tracer,
      String signalName,
      String workflowId,
      String runId,
      Context workflowSignalSpanContext) {
    SpanCreationContext context =
        SpanCreationContext.newBuilder()
            .setSpanOperationType(SpanOperationType.HANDLE_SIGNAL)
            .setActionName(signalName)
            .setWorkflowId(workflowId)
            .setRunId(runId)
            .build();
    return createSpan(context, tracer, workflowSignalSpanContext, SpanKind.CLIENT);
  }

  public SpanBuilder createContinueAsNewWorkflowStartSpan(
      Tracer tracer, String continueAsNewWorkflowType, String workflowId, String parentRunId) {
    SpanCreationContext context =
        SpanCreationContext.newBuilder()
            .setSpanOperationType(SpanOperationType.START_CONTINUE_AS_NEW_WORKFLOW)
            .setActionName(continueAsNewWorkflowType)
            .setWorkflowId(workflowId)
            .setParentRunId(parentRunId)
            .build();
    return createSpan(context, tracer, null, SpanKind.CLIENT);
  }

  public SpanBuilder createWorkflowRunSpan(
      Tracer tracer,
      String workflowType,
      String workflowId,
      String runId,
      Context workflowStartSpanContext) {
    SpanCreationContext context =
        SpanCreationContext.newBuilder()
            .setSpanOperationType(SpanOperationType.RUN_WORKFLOW)
            .setActionName(workflowType)
            .setWorkflowId(workflowId)
            .setRunId(runId)
            .build();
    return createSpan(context, tracer, workflowStartSpanContext, SpanKind.CLIENT);
  }

  public SpanBuilder createActivityStartSpan(
      Tracer tracer, String activityType, String workflowId, String runId) {
    SpanCreationContext context =
        SpanCreationContext.newBuilder()
            .setSpanOperationType(SpanOperationType.START_ACTIVITY)
            .setActionName(activityType)
            .setWorkflowId(workflowId)
            .setRunId(runId)
            .build();
    return createSpan(context, tracer, null, SpanKind.CLIENT);
  }

  public SpanBuilder createActivityRunSpan(
      Tracer tracer,
      String activityType,
      String workflowId,
      String runId,
      Context activityStartSpanContext) {
    SpanCreationContext context =
        SpanCreationContext.newBuilder()
            .setSpanOperationType(SpanOperationType.RUN_ACTIVITY)
            .setActionName(activityType)
            .setWorkflowId(workflowId)
            .setRunId(runId)
            .build();
    return createSpan(context, tracer, activityStartSpanContext, SpanKind.CLIENT);
  }

  public SpanBuilder createWorkflowStartUpdateSpan(
      Tracer tracer, String updateName, String workflowId, String runId) {
    SpanCreationContext context =
        SpanCreationContext.newBuilder()
            .setSpanOperationType(SpanOperationType.UPDATE_WORKFLOW)
            .setActionName(updateName)
            .setWorkflowId(workflowId)
            .setRunId(runId)
            .build();
    return createSpan(context, tracer, null, SpanKind.CLIENT);
  }

  public SpanBuilder createWorkflowExecuteUpdateSpan(
      Tracer tracer,
      String updateName,
      String workflowId,
      String runId,
      Context workflowUpdateSpanContext) {
    SpanCreationContext context =
        SpanCreationContext.newBuilder()
            .setSpanOperationType(SpanOperationType.HANDLE_UPDATE)
            .setActionName(updateName)
            .setWorkflowId(workflowId)
            .setRunId(runId)
            .build();
    return createSpan(context, tracer, workflowUpdateSpanContext, SpanKind.CLIENT);
  }

  public SpanBuilder createWorkflowQuerySpan(
      Tracer tracer, String updateName, String workflowId, String runId) {
    SpanCreationContext context =
        SpanCreationContext.newBuilder()
            .setSpanOperationType(SpanOperationType.QUERY_WORKFLOW)
            .setActionName(updateName)
            .setWorkflowId(workflowId)
            .setRunId(runId)
            .build();
    return createSpan(context, tracer, null, SpanKind.CLIENT);
  }

  public SpanBuilder createWorkflowHandleQuerySpan(
      Tracer tracer, String queryName, Context workflowQuerySpanContext) {
    SpanCreationContext context =
        SpanCreationContext.newBuilder()
            .setSpanOperationType(SpanOperationType.HANDLE_QUERY)
            .setActionName(queryName)
            .build();
    return createSpan(context, tracer, workflowQuerySpanContext, SpanKind.CLIENT);
  }

  public void logFail(Span toSpan, Throwable failReason) {

    LoggingEventBuilder log = logger.atError();
    log.addKeyValue(StandardTagNames.FAILED, true);
    log.addKeyValue("Error", options.getIsErrorPredicate().test(failReason));

    Map<String, Object> logPayload = new HashMap<>();
    log.addKeyValue("EVENT", "error");
    log.addKeyValue("ERROR_KIND", failReason.getClass().getName());
    log.addKeyValue("ERROR_OBJECT", failReason);
    log.addKeyValue("STACK", Throwables.getStackTraceAsString(failReason));

    String message = failReason.getMessage();
    if (message != null) {
      log.addKeyValue("MESSAGE", message);
    }

    log.log();
  }

  public void logEviction(Span toSpan) {
    toSpan.setAttribute(StandardTagNames.EVICTED, true);
  }


  private SpanBuilder createSpan(
      SpanCreationContext context,
      Tracer tracer,
      @Nullable Context parentSpanContext,
      @Nullable SpanKind kind) {
    Context parent;

    Span activeSpan = Span.fromContextOrNull(Context.current());
    if (activeSpan != null) {
      parent = Context.current().with(activeSpan);
    } else {
      parent = parentSpanContext;
    }


    SpanBuilder builder = options.getSpanBuilderProvider().createSpanBuilder(tracer, context);

    if (kind != null) {
      builder.setSpanKind(kind);
    }

    if (parent != null) {
      builder.setParent(parent);
    }

    return builder;
  }
}

