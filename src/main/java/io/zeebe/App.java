package io.zeebe;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.camunda.zeebe.client.ZeebeClient;
import io.camunda.zeebe.client.api.response.DeploymentEvent;
import io.camunda.zeebe.client.api.response.ProcessInstanceEvent;

import java.util.Map;
import java.util.Scanner;

/**
 * Example application that connects to a cluster on Camunda Cloud, or a locally deployed cluster.
 *
 * <p>When connecting to a cluster in Camunda Cloud, this application assumes that the following
 * environment variables are set:
 *
 * <ul>
 *   <li>ZEEBE_ADDRESS
 *   <li>ZEEBE_CLIENT_ID (implicitly required by {@code ZeebeClient} if authorization is enabled)
 *   <li>ZEEBE_CLIENT_SECRET (implicitly required by {@code ZeebeClient} if authorization is enabled)
 *   <li>ZEEBE_AUTHORIZATION_SERVER_URL (implicitly required by {@code ZeebeClient} if authorization is enabled)
 * </ul>
 *
 * <p><strong>Hint:</strong> When you create client credentials in Camunda Cloud you have the option
 * to download a file with above lines filled out for you.
 *
 * <p>When connecting to a local cluster, you can specifiy that address either at the command line
 * or by setting {@code ZEEBE_ADDRESS}. This application also assumes that auhentication is disabled
 * for a locally deployed cluster
 */
public class App {

    public static void main(String[] args) throws JsonProcessingException {
        final String gatewayAddress = "localhost:26500"; // System.getenv("ZEEBE_ADDRESS");

        System.out.println("Connected");

        final ZeebeClient client = ZeebeClient.newClientBuilder().gatewayAddress(gatewayAddress).usePlaintext().build();

        final DeploymentEvent deployment = client.newDeployCommand().addResourceFromClasspath("order-process.bpmn").send().join();

        ObjectMapper objectMapper = new ObjectMapper();
        String json = "{\r\n" + "   \"name\": \"codezup\",\r\n" + " \"stream\": \"computer-science\"\r\n" + "}";
        Map<String, String> map = objectMapper.readValue(json, Map.class);

        System.out.println(map);

        final ProcessInstanceEvent wfInstance = client.newCreateInstanceCommand()
                .bpmnProcessId("order-process")
                .latestVersion()
                .variables(map)
                .send()
                .join();

        final long workflowInstanceKey = wfInstance.getProcessInstanceKey();
        System.out.println("Workflow instance created. Key: " + workflowInstanceKey);

        client.close();
        System.out.println("Closed.");
    }

}
