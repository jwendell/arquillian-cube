package org.arquillian.cube.kubernetes.impl.await;

import io.fabric8.kubernetes.api.model.ContainerState;
import io.fabric8.kubernetes.api.model.ContainerStateWaiting;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodCondition;
import io.fabric8.kubernetes.api.model.PodStatus;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.arquillian.cube.kubernetes.api.Session;

import java.util.List;
import java.util.concurrent.Callable;

public class SessionPodsAreReady implements Callable<Boolean> {

    private static final String RUNNING_PHASE = "Running";
    private static final String READY_CONDITION = "Ready";

    private final Session session;
    private final KubernetesClient kubernetesClient;

    public SessionPodsAreReady(KubernetesClient kubernetesClient, Session session) {
        this.session = session;
        this.kubernetesClient = kubernetesClient;
    }

    @Override
    public Boolean call() throws Exception {
        boolean result = true;
        List<Pod> pods = kubernetesClient.pods().inNamespace(session.getNamespace()).list().getItems();

        if (pods.isEmpty()) {
            result = false;
            session.getLogger().warn("No pods are available yet, waiting...");
        }

        for (Pod pod : pods) {
            if (!isPodReady(pod)) {
                result = false;
                PodStatus podStatus = pod.getStatus();
                if (podStatus != null) {
                    List<ContainerStatus> containerStatuses = podStatus.getContainerStatuses();
                    for (ContainerStatus containerStatus : containerStatuses) {
                        ContainerState state = containerStatus.getState();
                        if (state != null) {
                            ContainerStateWaiting waiting = state.getWaiting();
                            String containerName = containerStatus.getName();
                            if (waiting != null) {
                                session.getLogger().warn("Waiting for container: " + containerName + ". Reason: " + waiting.getReason());
                            } else {
                                 if (!isConditionReady(pod)) {
                                     session.getLogger().warn("Waiting for container: " + containerName + ". Reason: Waiting for readiness");
                                 } else {
                                     session.getLogger().warn("Waiting for container: " + containerName + ".");
                                 }
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private static boolean isPodReady(Pod pod) {
        if (pod.getStatus() == null) {
            return false;
        }

        String phase = pod.getStatus().getPhase();
        if (!RUNNING_PHASE.equalsIgnoreCase(phase)) {
            return false;
        }

        return isConditionReady(pod);
    }

    private static boolean isConditionReady(Pod pod) {
        for (PodCondition condition : pod.getStatus().getConditions()) {
            if (condition.getType().equalsIgnoreCase(READY_CONDITION)) {
                if (Boolean.valueOf(condition.getStatus())) {
                    return true;
                }
            }
        }

        return false;
    }
}
