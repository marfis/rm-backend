package io.remedymatch.bedarf.api;

import io.remedymatch.bedarf.domain.BedarfService;
import io.remedymatch.properties.RmBackendProperties;
import lombok.AllArgsConstructor;
import lombok.val;
import org.camunda.bpm.client.ExternalTaskClient;
import org.camunda.bpm.client.backoff.ExponentialBackoffStrategy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@AllArgsConstructor
@Component
public class BedarfExternalTaskClient {
    private final RmBackendProperties properties;
    private final BedarfService bedarfService;

    @PostConstruct
    public void doSubscribe() {

        ExternalTaskClient client = ExternalTaskClient.create()
                .baseUrl(properties.getEngineUrl() + "/rest")
                .backoffStrategy(new ExponentialBackoffStrategy(3000, 2, 3000))
                .build();

        client.subscribe("bedarfAnfrageAblehnen")
                .lockDuration(2000)
                .handler((externalTask, externalTaskService) -> {

                    val anfrageId = externalTask.getVariable("objektId").toString();
                    bedarfService.anfrageStornieren(anfrageId);

                    //hier eventuell E-Mail versand?

                    externalTaskService.complete(externalTask);
                }).open();
    }
}
