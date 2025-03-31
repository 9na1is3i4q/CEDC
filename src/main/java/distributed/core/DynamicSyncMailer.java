package distributed.core;

import distributed.result.ResultCycle;
import distributed.result.annotations.NotRecordCostInCycle;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


public class DynamicSyncMailer extends Process {

    public static final int PHASE_AGENT = 1;
    public static final int PHASE_MAILER = 2;
    public static final int PHASE_NEXTSTEP= 3;

    public   int maxTimeSteps;

    private int timeStep;

    private Map<Integer, DynamicSyncAgent> agents;
    private Queue<Message> messageQueue;
    private int cycleCount;
    private int messageCount;
    private Set<Integer> agentReady;
    private AtomicInteger phase;
    private Map<Integer,ResultCycle> resultCycleByTime;


    private double[] costInCycle;
    private  double[] dataSumInCycle;
    private double[] benefitInCycle;
    private double[] bestCostInCyle;
    private int tail;

    private long startTime;
    private DynamicFinishedListener listener;
    private Set<Agent> stoppedAgents;

    private Set<DynamicSyncAgent> waitingAgents;

    private boolean printCycle;
    private List<SyncMailer.CycleListener> cycleListeners;
    private List<AgentIteratedOverListener> agentIteratedOverListeners;

    private double sumCost = 0;
    private double sumData =0;
    private double sumBenefit=0;
    private Map<Integer,int[]> DataAns;
    private Map<Integer,Map<Integer,Double>> AgentCostByTime;
    private Map<Integer,Map<Integer,Double>> AgentDataSumByTime;
    private Map<Integer,Map<Integer,Double>> AgentBenefitByTime;

    private Map<Integer,int[]> DataDelayOld;


    private int t;
    private int ncccs;
    private boolean recordCostInCycle;
    private int maxDistance;


    public DynamicSyncMailer(){
        super("mailer");
        agents = new HashMap<>();
        messageQueue = new LinkedList<>();
        agentReady = new HashSet<>();
        phase = new AtomicInteger(PHASE_AGENT);

        DataAns = new HashMap<>();
        DataDelayOld = new HashMap<>();

        stoppedAgents = new HashSet<>();
        waitingAgents = new HashSet<>();
        cycleListeners = new LinkedList<>();
        agentIteratedOverListeners = new LinkedList<>();
        recordCostInCycle = true;
        costInCycle = new double[2];
        bestCostInCyle = new double[2];
        benefitInCycle = new double[2];
        dataSumInCycle = new double[2];
        AgentCostByTime = new HashMap<>();
        AgentDataSumByTime = new HashMap<>();
        AgentBenefitByTime = new HashMap<>();
        timeStep=0;


      resultCycleByTime = new HashMap<>();
    }

    public DynamicSyncMailer(DynamicFinishedListener finishedListener,int maxTimeSteps,int maxDistance){
        this();
        listener = finishedListener;
        this.maxTimeSteps = maxTimeSteps;
        this.maxDistance = maxDistance;
    }


    public void setPrintCycle(boolean printCycle) {
        this.printCycle = printCycle;
    }
    public void registerCycleListener(SyncMailer.CycleListener listener){
        cycleListeners.add(listener);
    }

    public void registerAgentIteratedOverListener(AgentIteratedOverListener listener){
        agentIteratedOverListeners.add(listener);
    }


    public void register(DynamicSyncAgent agent){
        if (recordCostInCycle){
            recordCostInCycle = !agent.getClass().isAnnotationPresent(NotRecordCostInCycle.class);
        }
        agents.put(agent.id,agent);
    }

    public void addMessage(Message message) {
        synchronized (phase){
            while (phase.get() == PHASE_MAILER);
            synchronized (messageQueue){
                messageQueue.add(message);
            }
        }
    }

    private void expand(){
        double[] tmpCostInCycle = new double[costInCycle.length * 2];
        double[] tmpBestCostInCycle = new double[bestCostInCyle.length * 2];
        double[] tmpBenefitInCycle = new double[benefitInCycle.length * 2];
        double[] tmpDataSumInCycle = new double[dataSumInCycle.length * 2];
        for (int i = 0 ; i < costInCycle.length; i++){
            tmpCostInCycle[i] = costInCycle[i];
            tmpBestCostInCycle[i] = bestCostInCyle[i];
            tmpDataSumInCycle[i] = dataSumInCycle[i];
            tmpBenefitInCycle[i] = benefitInCycle[i];
        }
        costInCycle = tmpCostInCycle;
        bestCostInCyle = tmpBestCostInCycle;
        dataSumInCycle = tmpDataSumInCycle;
        benefitInCycle = tmpBenefitInCycle;

    }

    @Override
    public void preExecution() {
        startTime = new Date().getTime();
    }



    @Override
    public void execution() {
        if (phase.get() == PHASE_MAILER){
            HashSet<DynamicSyncAgent> awakeAgents = new HashSet<>();
            boolean isRest = false;
            synchronized (messageQueue){
                while (!messageQueue.isEmpty()){
                    Message message = messageQueue.poll();
                    messageCount++;
                    if (agents.get(message.getIdReceiver()).isRunning()){
                        agents.get(message.getIdReceiver()).addMessage(message);
                    }
                }
                boolean canWaiting = true;
                for (DynamicSyncAgent syncAgent : agents.values()){
                    if (syncAgent.getPhase() == DynamicSyncAgent.PHASE_RUNNING){
                        canWaiting = false;
                    }
                    else {
                        waitingAgents.add(syncAgent);
                    }
                }

                if (recordCostInCycle) {
                    if (tail == costInCycle.length - 1){
                        expand();
                    }
                    for (DynamicSyncAgent agent : waitingAgents) {
                        sumCost += agent.getLocalCost();
//                        System.out.println("------------------"+agent.id+Arrays.toString(agent.data));
                        DataAns.put(agent.id,agent.data);
                        int []  dayaDelayOld = new int[agent.dataDelayOld.length];
                        System.arraycopy(agent.dataDelayOld,0,dayaDelayOld,0,agent.dataDelayOld.length);
                        DataDelayOld.put(agent.id,dayaDelayOld);
                    }
                    costInCycle[tail] = sumCost/2;
                    dataSumInCycle[tail] = sumData;
                    benefitInCycle[tail] = sumBenefit;
                }
                tail++;
                sumCost = 0;
                sumBenefit =0;
                sumData = 0;
                printCycle = true;
                if (printCycle){
                    if(tail <10){
//                        System.out.println("-----------TimeStep:"+timeStep+"--cycle:" + tail+"-------------------------");
                    }
                }

                if (canWaiting){

                    if(timeStep >= maxTimeSteps){
                        stopProcess();
                    }
                    else{
                        awakeAgents = new HashSet<>(waitingAgents);

                        isRest=true;
                    }
                }
                else {
                    cycleCount++;
                    agentReady.clear();
                    phase.set(PHASE_AGENT);
                }
            }

            if (isRest){
                waitingAgents.clear();
                agentReady.clear();
                messageQueue.clear();
                phase.set(PHASE_NEXTSTEP);

                DataAns = new HashMap<>();
                timeStep++;
                tail=0;

                double percent = (double) timeStep / (double) maxTimeSteps *100;

                System.out.println("运行进度:"+percent+"%");

                if(timeStep >= maxTimeSteps){
                    stopProcess();
                }else {
                    for(DynamicSyncAgent agent : awakeAgents){
                        agent.agentAwake();
                    }
                }
            }
        }

    }

    @Override
    public void postExecution() {

    }


    public synchronized void agentDone(int id){
        synchronized (agentReady){
            agentReady.add(id);
            sumCost += agents.get(id).getLocalCost();
            sumBenefit += agents.get(id).getBenefit();
            sumData += agents.get(id).getDataSum();
            if (agentReady.size() == agents.size() - waitingAgents.size()){
                phase.set(PHASE_MAILER);
            }
        }
    }

    public synchronized boolean isDone(int id){
        return agentReady.contains(id);
    }

    public synchronized int getPhase() {
        return phase.get();
    }

    public Map<Integer, int[]> getDataDelayOld() {
        return DataDelayOld;
    }

    public synchronized void setResultCycle(int id, ResultCycle resultCycle){
        ResultCycle resultCycleTmp = resultCycleByTime.getOrDefault(timeStep,new ResultCycle());
        if (resultCycleTmp == null){
            resultCycleTmp = resultCycle;
            resultCycleTmp.setAgentDisNeighbourMap(id,resultCycle.disNeighbourMap);
            resultCycleTmp.setAgentUsers(id,resultCycle.users);
            resultCycleTmp.setAgentUserRequireData(id,resultCycle.userRequireData);
        }
        else {
            resultCycleTmp.add(resultCycle);
            resultCycleTmp.setAgentValues(id,resultCycle.getAgentValue(id));
            resultCycleTmp.setAgentData(id,resultCycle.getAgentData(id));
            resultCycleTmp.setAgentDisNeighbourMap(id,resultCycle.disNeighbourMap);
            resultCycleTmp.setAgentUsers(id,resultCycle.users);
            resultCycleTmp.setAgentUserRequireData(id,resultCycle.userRequireData);
        }
        if (resultCycleTmp.getAgents().size() == agents.size()){
            resultCycleTmp.setTotalTime(new Date().getTime() - startTime);
            startTime = new Date().getTime();
            resultCycleTmp.setMessageQuantity(messageCount);
            resultCycleTmp.setMaxDistance(maxDistance);
            if (recordCostInCycle){
                resultCycleTmp.setCostInCycle(costInCycle,tail);
                resultCycleTmp.setDataSumInCycle(dataSumInCycle,tail);
                resultCycleTmp.setBenefitInCycle(benefitInCycle,tail);
                resultCycleTmp.setDataAns((HashMap<Integer, int[]>) DataAns);
            }
            resultCycleTmp.setNcccs(ncccs);
        }
        resultCycleByTime.put(timeStep,resultCycleTmp);
        if (resultCycleTmp.getAgents().size() == agents.size()){
            if (timeStep >= maxTimeSteps-1 &&   listener != null){
                listener.onFinished(resultCycleByTime);
            }
        }
        
    }

}
