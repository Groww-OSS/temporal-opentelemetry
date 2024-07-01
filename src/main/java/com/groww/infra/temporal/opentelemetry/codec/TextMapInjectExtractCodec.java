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

package com.groww.infra.temporal.opentelemetry.codec;

import com.groww.infra.temporal.opentelemetry.OpenTelemetrySpanContextCodec;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapPropagator;

import java.util.HashMap;
import java.util.Map;

public class TextMapInjectExtractCodec implements OpenTelemetrySpanContextCodec {
  public static final TextMapInjectExtractCodec INSTANCE = new TextMapInjectExtractCodec();

  private static final TemporalHeaderSetter SETTER = TemporalHeaderSetter.INSTANCE;
  private static final TemporalHeaderGetter GETTER = TemporalHeaderGetter.INSTANCE;


  @Override
  public Map<String, String> encode(Context context, TextMapPropagator propagator) {
    Map<String, String> serialized = new HashMap<>();
    propagator.inject(context, serialized, SETTER);
    return serialized;
  }

  @Override
  public Context decode(Map<String, String> serializedSpanContext, TextMapPropagator propagator) {
    return propagator.extract(Context.current(), serializedSpanContext, TemporalHeaderGetter.INSTANCE);
  }
}
