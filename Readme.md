# Temporal [OpenTelemetry](https://opentelemetry.io/) module

This module provides a set of Interceptors that adds support for OpenTelemetry Span Context propagation to Temporal.

## Usage

You want to register two interceptors - one on the Temporal client side, another on the worker side:

1. Client configuration:
    ```java
    import com.groww.infra.temporal.opentelemetry.OpenTelemetryClientInterceptor;
       //...
       .setInterceptors(new OpenTelemetryClientInterceptor())
       .build();
    ```
2. Worker configuration:
    ```java
    import com.groww.infra.temporal.opentelemetry.OpenTelemetryWorkerInterceptor;
       //...
       .setWorkerInterceptors(new OpenTelemetryWorkerInterceptor())
       .build();
    ```




