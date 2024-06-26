package com.groww.infra.temporal.opentelemetry;

public enum SpanOperationType {
  START_WORKFLOW("StartWorkflow"),
  SIGNAL_WITH_START_WORKFLOW("SignalWithStartWorkflow"),
  RUN_WORKFLOW("RunWorkflow"),
  START_CHILD_WORKFLOW("StartChildWorkflow"),
  START_CONTINUE_AS_NEW_WORKFLOW("StartContinueAsNewWorkflow"),
  START_ACTIVITY("StartActivity"),
  RUN_ACTIVITY("RunActivity"),
  SIGNAL_EXTERNAL_WORKFLOW("SignalExternalWorkflow"),
  QUERY_WORKFLOW("QueryWorkflow"),
  SIGNAL_WORKFLOW("SignalWorkflow"),
  UPDATE_WORKFLOW("UpdateWorkflow"),
  HANDLE_QUERY("HandleQuery"),
  HANDLE_SIGNAL("HandleSignal"),
  HANDLE_UPDATE("HandleUpdate");

  private final String defaultPrefix;

  SpanOperationType(String defaultPrefix) {
    this.defaultPrefix = defaultPrefix;
  }

  public String getDefaultPrefix() {
    return defaultPrefix;
  }
}