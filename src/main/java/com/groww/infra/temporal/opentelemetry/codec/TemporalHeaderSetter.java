package com.groww.infra.temporal.opentelemetry.codec;

import io.opentelemetry.context.propagation.TextMapSetter;

import java.util.Map;

public enum TemporalHeaderSetter implements TextMapSetter<Map<String, String>> {
  INSTANCE;

  @Override
  public void set(Map<String, String> map, String key, String value) {
    map.put(key, value);
  }
}
