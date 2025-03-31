package distributed.core;

import java.util.Map;

public interface AgentIteratedOverListener {
    void agentIteratedOver(Map<Integer, SyncAgent> agents);
}
