/*
 * Copyright (C) 2022 Temporal Technologies, Inc. All Rights Reserved.
 *
 * Copyright (C) 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this material except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.groww.infra.temporal.opentelemetry.internal;

import com.groww.infra.temporal.opentelemetry.OpenTelemetryOptions;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.activity.ActivityInfo;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptor;
import io.temporal.common.interceptors.ActivityInboundCallsInterceptorBase;

public class OpenTelemetryActivityInboundCallsInterceptor
    extends ActivityInboundCallsInterceptorBase {
  private final OpenTelemetryOptions options;
  private final SpanFactory spanFactory;
  private final Tracer tracer;
  private final TextMapPropagator propagator;
  private final ContextAccessor contextAccessor;

  public OpenTelemetryActivityInboundCallsInterceptor(
      ActivityInboundCallsInterceptor next,
      OpenTelemetryOptions options,
      SpanFactory spanFactory,
      ContextAccessor contextAccessor) {
    super(next);
    this.options = options;
    this.spanFactory = spanFactory;
    this.tracer = options.getTracer();
    this.propagator = options.getPropagator();
    this.contextAccessor = contextAccessor;
  }

  private ActivityExecutionContext activityExecutionContext;

  @Override
  public void init(ActivityExecutionContext context) {
    // Workflow Interceptors have access to Workflow.getInfo methods,
    // but Activity Interceptors don't have access to Activity.getExecutionContext().getInfo()
    // This is inconsistent and should be addressed, but this is a workaround.
    this.activityExecutionContext = context;
    super.init(context);
  }

  @Override
  public ActivityOutput execute(ActivityInput input) {
    SpanContext rootSpanContext =
        contextAccessor.readSpanContextFromHeader(input.getHeader(), propagator);
    ActivityInfo activityInfo = activityExecutionContext.getInfo();
    Span activityRunSpan =
        spanFactory
            .createActivityRunSpan(
                tracer,
                activityInfo.getActivityType(),
                activityInfo.getWorkflowId(),
                activityInfo.getRunId(),
                rootSpanContext)
            .startSpan();
    try (Scope scope = activityRunSpan.makeCurrent()) {
      return super.execute(input);
    } catch (Throwable t) {
      spanFactory.logFail(activityRunSpan, t);
      throw t;
    } finally {
      activityRunSpan.end();
    }
  }
}
