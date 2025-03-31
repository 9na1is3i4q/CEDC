package distributed.core;


import problem_parser.AgentParser;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class AgentManager {

    private static final String METHOD_ASYNC = "ASYNC";
    private static final String METHOD_SYNC = "SYNC";

    private static final String CONFIG_KEY_PRINT_CYCLE = "PRINTCYCLE";
    private static final String CONFIG_KEY_SUPPRESS_OUTPUT = "SUPPRESSOUTPUT";

    private List<Agent> agents;
    private SyncMailer syncMailer;
    private static Map<String, AgentDescriptor> agentDescriptors;

    public AgentManager(String agentDescriptorPath, String agentType, Problem problem, FinishedListener listener, boolean showPesudoTreeGraph) {
        agents = new LinkedList<>();
        AgentParser agentParser = new AgentParser(agentDescriptorPath);
        agentDescriptors = agentParser.parse();
        if (agentDescriptors.size() == 0){
            throw new RuntimeException("No agent is defined in manifest");
        }
        Map<String,String> configurations = agentParser.parseConfigurations();
        AgentDescriptor descriptor = agentDescriptors.get(agentType.toUpperCase());
        boolean suppressOutput = false;


        syncMailer = new SyncMailer(listener);

        if (configurations.containsKey(CONFIG_KEY_PRINT_CYCLE)){
            if (configurations.get(CONFIG_KEY_PRINT_CYCLE).equals("TRUE")){
                syncMailer.setPrintCycle(true);
            }
        }
        if (configurations.containsKey(CONFIG_KEY_SUPPRESS_OUTPUT)){
            if (configurations.get(CONFIG_KEY_SUPPRESS_OUTPUT).equals("TRUE")){
                suppressOutput = true;
            }
        }


        for (int id : problem.allAgentId){
            Agent agent = null;
            try {
                Class clazz = Class.forName(descriptor.className); //  反射
                Constructor constructor = clazz.getConstructors()[0];

                int[] data = new int[problem.allDataId.length];


                EDCAgentDTO agentDTO = new EDCAgentDTO(id, data, problem.getHostUsers().get(id), problem.getRelatedUsers(id),problem.getNeighbours().get(id),problem.getNeighboursUnderLimit().get(id), problem.getUserRequireDataByAgentAndTime(id, 0), problem.getUserHostsByAgent(id), problem.getNeighbourAdject(id),problem.getNeighbourHostUsers(id), problem.serverDataLimit, problem.maxDistance);
                agent = (Agent) constructor.newInstance(agentDTO,
                        syncMailer);



            } catch (Exception e) {
                throw new RuntimeException("init exception");
            }
            agent.setSuppressOutput(suppressOutput);
            agents.add(agent);
        }
    }



    public void addAgentIteratedOverListener(AgentIteratedOverListener listener){
        syncMailer.registerAgentIteratedOverListener(listener);
    }

    public void startAgents(){
        for (Agent agent : agents){
            agent.startProcess();
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (syncMailer != null){
            syncMailer.startProcess();
        }
    }


}
