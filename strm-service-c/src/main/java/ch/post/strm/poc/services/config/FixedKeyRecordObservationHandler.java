package ch.post.strm.poc.services.config;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.transport.ReceiverContext;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;

public class FixedKeyRecordObservationHandler implements ObservationHandler<ReceiverContext<FixedKeyRecord<?, ?>>> {

    @Override
    public boolean supportsContext(Observation.Context context) {
        return context instanceof ReceiverContext;
    }

    @Override
    public void onStart(ReceiverContext<FixedKeyRecord<?, ?>> context) {
        context.setName("process");
        context.addLowCardinalityKeyValues(
                KeyValues
                        .of(KeyValue.of("messaging.destination_kind", "topic"))
                        .and(KeyValue.of("messaging.system", "kafka"))
        );
    }

    @Override
    public void onError(ReceiverContext<FixedKeyRecord<?, ?>> context) {
        ObservationHandler.super.onError(context);
    }

    @Override
    public void onEvent(Observation.Event event, ReceiverContext<FixedKeyRecord<?, ?>> context) {
        ObservationHandler.super.onEvent(event, context);
    }

    @Override
    public void onScopeOpened(ReceiverContext<FixedKeyRecord<?, ?>> context) {
        ObservationHandler.super.onScopeOpened(context);
    }

    @Override
    public void onScopeClosed(ReceiverContext<FixedKeyRecord<?, ?>> context) {
        ObservationHandler.super.onScopeClosed(context);
    }

    @Override
    public void onScopeReset(ReceiverContext<FixedKeyRecord<?, ?>> context) {
        ObservationHandler.super.onScopeReset(context);
    }

    @Override
    public void onStop(ReceiverContext<FixedKeyRecord<?, ?>> context) {
        ObservationHandler.super.onStop(context);
    }

}
