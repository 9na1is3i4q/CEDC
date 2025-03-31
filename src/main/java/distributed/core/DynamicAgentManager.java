package distributed.core;

import distributed.core.*;
import problem_parser.AgentParser;

import java.lang.reflect.Constructor;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


public class DynamicAgentManager {


    private static final String METHOD_ASYNC = "ASYNC";
    private static final String METHOD_SYNC = "SYNC";

    private static final String CONFIG_KEY_PRINT_CYCLE = "PRINTCYCLE";
    private static final String CONFIG_KEY_SUPPRESS_OUTPUT = "SUPPRESSOUTPUT";

    private List<DynamicAgent> agents;
    private DynamicSyncMailer dynamicSyncMailer;
    private static Map<String, AgentDescriptor> agentDescriptors;


    public DynamicAgentManager(String agentDescriptorPath, String agentType, Problem problem, DynamicFinishedListener listener, boolean showPesudoTreeGraph) {
        agents = new LinkedList<>();
        AgentParser agentParser = new AgentParser(agentDescriptorPath);
        agentDescriptors = agentParser.parse();
        if (agentDescriptors.size() == 0){
            throw new RuntimeException("No agent is defined in manifest");
        }
        Map<String,String> configurations = agentParser.parseConfigurations();
        AgentDescriptor descriptor = agentDescriptors.get(agentType.toUpperCase());
        boolean suppressOutput = false;

        dynamicSyncMailer = new DynamicSyncMailer(listener,problem.timeSlots,problem.maxDistance);

        if (configurations.containsKey(CONFIG_KEY_PRINT_CYCLE)){
            if (configurations.get(CONFIG_KEY_PRINT_CYCLE).equals("TRUE")){
                dynamicSyncMailer.setPrintCycle(true);
            }
        }
        if (configurations.containsKey(CONFIG_KEY_SUPPRESS_OUTPUT)){
            if (configurations.get(CONFIG_KEY_SUPPRESS_OUTPUT).equals("TRUE")){
                suppressOutput = true;
            }
        }


        for (int id : problem.allAgentId){
            DynamicAgent agent = null;
            try {
                Class clazz = Class.forName(descriptor.className);
                Constructor constructor = clazz.getConstructors()[0];
                int[] data = new int[problem.allDataId.length];
                DynamicEDCAgentDTO agentDTO = new DynamicEDCAgentDTO(id, data, problem.getHostUsers().get(id), problem.getRelatedUsers(id),problem.getNeighbours().get(id),problem.getNeighboursUnderLimit().get(id), problem.getUserRequireDataByAgent(id), problem.getUserHostsByAgent(id), problem.getNeighbourAdject(id),problem.getNeighbourHostUsers(id), problem.serverDataLimit, problem.maxDistance);
                agent = (DynamicAgent) constructor.newInstance(agentDTO,
                        dynamicSyncMailer);



            } catch (Exception e) {
                throw new RuntimeException("init exception");
            }
            agent.setSuppressOutput(suppressOutput);
            agents.add(agent);
        }
    }



    public DynamicAgentManager(String agentDescriptorPath, String agentType, Problem problem, DynamicFinishedListener listener, boolean showPesudoTree, double gama, double costCE, double costEE) {

        agents = new LinkedList<>();
        AgentParser agentParser = new AgentParser(agentDescriptorPath);
        agentDescriptors = agentParser.parse();
        if (agentDescriptors.size() == 0){
            throw new RuntimeException("No agent is defined in manifest");
        }
        Map<String,String> configurations = agentParser.parseConfigurations();
        AgentDescriptor descriptor = agentDescriptors.get(agentType.toUpperCase());
        boolean suppressOutput = false;

        dynamicSyncMailer = new DynamicSyncMailer(listener,problem.timeSlots,problem.maxDistance);

        if (configurations.containsKey(CONFIG_KEY_PRINT_CYCLE)){
            if (configurations.get(CONFIG_KEY_PRINT_CYCLE).equals("TRUE")){
                dynamicSyncMailer.setPrintCycle(true);
            }
        }
        if (configurations.containsKey(CONFIG_KEY_SUPPRESS_OUTPUT)){
            if (configurations.get(CONFIG_KEY_SUPPRESS_OUTPUT).equals("TRUE")){
                suppressOutput = true;
            }
        }

        for (int id : problem.allAgentId){
            DynamicAgent agent = null;
            try {
                Class clazz = Class.forName(descriptor.className);
                Constructor constructor = clazz.getConstructors()[0];
                int[] data = new int[problem.allDataId.length];
                DynamicEDCAgentDTO agentDTO = new DynamicEDCAgentDTO(id, data, problem.getHostUsers().get(id), problem.getRelatedUsers(id),problem.getNeighbours().get(id),problem.getNeighboursUnderLimit().get(id), problem.getUserRequireDataByAgent(id), problem.getUserHostsByAgent(id), problem.getNeighbourAdject(id),problem.getNeighbourHostUsers(id), problem.serverDataLimit, problem.maxDistance);
                agent = (DynamicAgent) constructor.newInstance(agentDTO,
                        dynamicSyncMailer,
                        gama,
                        costCE,
                        costEE
                        );

            } catch (Exception e) {
                throw new RuntimeException("init exception");
            }
            agent.setSuppressOutput(suppressOutput);
            agents.add(agent);
        }

    }


    public void addAgentIteratedOverListener(AgentIteratedOverListener listener){
        dynamicSyncMailer.registerAgentIteratedOverListener(listener);
    }

    public void startAgents(){
        for (DynamicAgent agent : agents){
            agent.startProcess();
        }
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (dynamicSyncMailer != null){
            dynamicSyncMailer.startProcess();
        }
    }
}
