package io.remedymatch.engine;

import io.remedymatch.properties.RmBackendProperties;
import lombok.AllArgsConstructor;
import lombok.val;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@AllArgsConstructor
@Component
public class EngineClient {

    private final RmBackendProperties properties;

    public TaskDTO ladeTask(String taskId, String institutionId) {
        val restTemplate = new RestTemplate();
        ResponseEntity<TaskDTO> taskResponse = restTemplate.getForEntity(properties.getEngineUrl() + "/restapi/task/" + institutionId + "/" + taskId, TaskDTO.class);

        if (taskResponse.getStatusCode().isError()) {
            throw new RuntimeException("Beim laden des Task ist etwas fehlgeschlagen");
        }

        val task = taskResponse.getBody();
        return task;
    }

    public List<TaskDTO> ladeAlleTask(String institutionId) {
        val restTemplate = new RestTemplate();
        ResponseEntity<TaskDTO[]> taskResponse = restTemplate.getForEntity(properties.getEngineUrl() + "/restapi/task/institution/" + institutionId, TaskDTO[].class);
        if (taskResponse.getStatusCode().isError()) {
            throw new RuntimeException("Beim laden der Task ist etwas fehlgeschlagen");
        }
        val task = taskResponse.getBody();
        return Arrays.asList(task);
    }

    public void taskAbschliessen(String task, boolean angenommen) {

        val request = TaskAbschliessenRequest.builder().angenommen(angenommen).build();
        val restTemplate = new RestTemplate();
        ResponseEntity<Void> response = restTemplate.postForEntity(properties.getEngineUrl() + "/restapi/task/" + task, request, Void.class);

        if (response.getStatusCode().isError()) {
            throw new RuntimeException("Beim abschliessen ist etwas fehlgeschlagen");
        }
    }

    public String prozessStarten(String prozessKey, String typ, String anfrageId, String institutionId) {

        val request = ProzessStartRequest.builder()
                .anfrageId(anfrageId)
                .institutionId(institutionId)
                .prozessKey(prozessKey)
                .build();

        val restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.postForEntity(properties.getEngineUrl() + "/restapi/prozess/start/", request, String.class);

        if (response.getStatusCode().isError()) {
            throw new RuntimeException("Beim Starten des Prozesses ist etwas fehlgeschlagen");
        }

        return response.getBody();
    }
}
