package com.groww.infra.temporal.opentelemetry.codec;

import io.opentelemetry.context.propagation.TextMapGetter;

import javax.annotation.Nullable;
import java.util.Map;

public enum TemporalHeaderGetter implements TextMapGetter<Map<String, String>> {
  INSTANCE;

  @Override
  public Iterable<String> keys(Map<String, String> map) {
    return map.keySet();
  }

  @Nullable
  @Override
  public String get(@Nullable Map<String, String> map, String key) {
    return map.get(key);
  }
}
