package ch.post.strm.poc.services.config;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.transport.SenderContext;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Objects;

public class ProducerRecordObservationHandler implements ObservationHandler<SenderContext<ProducerRecord<?, ?>>> {

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof SenderContext;
    }

    @Override
    public void onStart(SenderContext<ProducerRecord<?, ?>> context) {
        context.setName("send");
        context.setRemoteServiceAddress(Objects.requireNonNull(context.getCarrier()).topic());
        context.addLowCardinalityKeyValues(
                KeyValues
                        .of(KeyValue.of("messaging.destination", context.getCarrier().topic()))
                        .and(KeyValue.of("messaging.destination_kind", "topic"))
                        .and(KeyValue.of("messaging.system", "kafka"))
        );
    }

    @Override
    public void onError(SenderContext<ProducerRecord<?, ?>> context) {
        ObservationHandler.super.onError(context);
    }

    @Override
    public void onEvent(Observation.Event event, SenderContext<ProducerRecord<?, ?>> context) {
        ObservationHandler.super.onEvent(event, context);
    }

    @Override
    public void onScopeOpened(SenderContext<ProducerRecord<?, ?>> context) {
        ObservationHandler.super.onScopeOpened(context);
    }

    @Override
    public void onScopeClosed(SenderContext<ProducerRecord<?, ?>> context) {
        ObservationHandler.super.onScopeClosed(context);
    }

    @Override
    public void onScopeReset(SenderContext<ProducerRecord<?, ?>> context) {
        ObservationHandler.super.onScopeReset(context);
    }

    @Override
    public void onStop(SenderContext<ProducerRecord<?, ?>> context) {
        ObservationHandler.super.onStop(context);
    }
}
