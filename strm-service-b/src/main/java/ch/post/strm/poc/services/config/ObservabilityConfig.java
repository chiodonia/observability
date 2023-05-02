package ch.post.strm.poc.services.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.observation.DefaultMeterObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import io.micrometer.tracing.otel.bridge.*;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

import static io.opentelemetry.sdk.trace.samplers.Sampler.alwaysOn;
import static java.util.Collections.emptyList;

@Configuration
public class ObservabilityConfig {

    static {
        Hooks.enableAutomaticContextPropagation();
    }

    private final String serviceName;
    private final String zipkinEndpoint;

    public ObservabilityConfig(
            @Value("${spring.application.name}") String serviceName,
            @Value("${management.zipkin.tracing.endpoint}") String zipkinEndpoint) {
        this.serviceName = serviceName;
        this.zipkinEndpoint = zipkinEndpoint;
    }

    //    @Bean
    public OpenTelemetry openTelemetry() {
        return OpenTelemetrySdk.builder()
                .setTracerProvider(
                        SdkTracerProvider.builder()
                                .addSpanProcessor(
                                        BatchSpanProcessor.builder(
                                                ZipkinSpanExporter.builder().setEndpoint(zipkinEndpoint).build()
                                        ).build()
                                )
                                .setSampler(alwaysOn())
                                .setResource(
                                        Resource.getDefault()
                                                .merge(Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, serviceName)))
                                )
                                .build()
                )
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .buildAndRegisterGlobal();
    }

    //    @Bean
    public OtelTracer tracer(OpenTelemetry openTelemetry) {
        OtelCurrentTraceContext currentTraceContext = new OtelCurrentTraceContext();
        Slf4JEventListener eventListener = new Slf4JEventListener();
        Slf4JBaggageEventListener baggageEventListener = new Slf4JBaggageEventListener(emptyList());

        return new OtelTracer(
                openTelemetry.getTracer("io.micrometer.micrometer-tracing"),
                currentTraceContext, (OtelTracer.EventPublisher) event -> {
            eventListener.onEvent(event);
            baggageEventListener.onEvent(event);
        },
                new OtelBaggageManager(currentTraceContext, emptyList(), emptyList())
        );
    }

    //    @Bean
    public ObservationRegistry observationRegistry(MeterRegistry meterRegistry, Tracer tracer) {
        ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig()
                .observationHandler(new DefaultMeterObservationHandler(meterRegistry))
                .observationHandler(new DefaultTracingObservationHandler(tracer));
        return registry;
    }

}
