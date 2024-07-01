package com.groww.infra.temporal.opentelemetry.internal;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.groww.infra.temporal.opentelemetry.SpanBuilderProvider;
import com.groww.infra.temporal.opentelemetry.SpanCreationContext;
import com.groww.infra.temporal.opentelemetry.SpanOperationType;
import com.groww.infra.temporal.opentelemetry.StandardTagNames;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;

import java.util.Map;

public class ActionTypeAndNameSpanBuilderProvider implements SpanBuilderProvider {
  public static final ActionTypeAndNameSpanBuilderProvider INSTANCE =
      new ActionTypeAndNameSpanBuilderProvider();

  private static final String PREFIX_DELIMITER = ":";

  public ActionTypeAndNameSpanBuilderProvider() {
  }

  public SpanBuilder createSpanBuilder(Tracer tracer, SpanCreationContext context) {
    SpanBuilder spanBuilder = tracer.spanBuilder(this.getSpanName(context));

    getSpanTags(context).forEach(spanBuilder::setAttribute);

    return spanBuilder;
  }

  /**
   * Generates the name of the span given the span context.
   *
   * @param context Span creation context
   * @return The span name
   */
  protected String getSpanName(SpanCreationContext context) {
    return context.getSpanOperationType().getDefaultPrefix()
        + PREFIX_DELIMITER
        + context.getActionName();
  }

  /**
   * Generates tags for the span given the span creation context
   *
   * @param context The span creation context
   * @return The map of tags for the span
   */
  protected Map<String, String> getSpanTags(SpanCreationContext context) {
    SpanOperationType operationType = context.getSpanOperationType();
    switch (operationType) {
      case START_WORKFLOW:
      case SIGNAL_WITH_START_WORKFLOW:
        return ImmutableMap.of(StandardTagNames.WORKFLOW_ID, context.getWorkflowId());
      case START_CHILD_WORKFLOW:
        return ImmutableMap.of(
            StandardTagNames.WORKFLOW_ID, context.getWorkflowId(),
            StandardTagNames.PARENT_WORKFLOW_ID, context.getParentWorkflowId(),
            StandardTagNames.PARENT_RUN_ID, context.getParentRunId());
      case START_CONTINUE_AS_NEW_WORKFLOW:
        return ImmutableMap.of(
            StandardTagNames.WORKFLOW_ID, context.getWorkflowId(),
            StandardTagNames.PARENT_RUN_ID, context.getParentRunId());
      case RUN_WORKFLOW:
      case START_ACTIVITY:
      case RUN_ACTIVITY:
      case SIGNAL_EXTERNAL_WORKFLOW:
      case SIGNAL_WORKFLOW:
      case UPDATE_WORKFLOW:
      case QUERY_WORKFLOW:
      case HANDLE_SIGNAL:
      case HANDLE_UPDATE:
        String runId = context.getRunId();
        Preconditions.checkNotNull(
            runId, "runId is expected to be not null for span operation type %s", operationType);
        return ImmutableMap.of(
            StandardTagNames.WORKFLOW_ID, context.getWorkflowId(),
            StandardTagNames.RUN_ID, context.getRunId());
      case HANDLE_QUERY:
        return ImmutableMap.of();
    }
    throw new IllegalArgumentException("Unknown span operation type provided");
  }
}