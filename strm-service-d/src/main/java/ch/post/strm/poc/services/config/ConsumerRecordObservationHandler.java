package ch.post.strm.poc.services.config;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.transport.ReceiverContext;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public class ConsumerRecordObservationHandler implements ObservationHandler<ReceiverContext<ConsumerRecord<?, ?>>> {

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof ReceiverContext;
    }

    @Override
    public void onStart(ReceiverContext<ConsumerRecord<?, ?>> context) {
        context.setName("receive");
        context.setRemoteServiceAddress(context.getCarrier().topic());
        context.addLowCardinalityKeyValues(
                KeyValues
                        .of(KeyValue.of("messaging.destination", context.getCarrier().topic()))
                        .and(KeyValue.of("messaging.destination_kind", "topic"))
                        .and(KeyValue.of("messaging.system", "kafka"))
        );
    }

    @Override
    public void onError(ReceiverContext<ConsumerRecord<?, ?>> context) {
        ObservationHandler.super.onError(context);
    }

    @Override
    public void onEvent(Observation.Event event, ReceiverContext<ConsumerRecord<?, ?>> context) {
        ObservationHandler.super.onEvent(event, context);
    }

    @Override
    public void onScopeOpened(ReceiverContext<ConsumerRecord<?, ?>> context) {
        ObservationHandler.super.onScopeOpened(context);
    }

    @Override
    public void onScopeClosed(ReceiverContext<ConsumerRecord<?, ?>> context) {
        ObservationHandler.super.onScopeClosed(context);
    }

    @Override
    public void onScopeReset(ReceiverContext<ConsumerRecord<?, ?>> context) {
        ObservationHandler.super.onScopeReset(context);
    }

    @Override
    public void onStop(ReceiverContext<ConsumerRecord<?, ?>> context) {
        ObservationHandler.super.onStop(context);
    }

}
