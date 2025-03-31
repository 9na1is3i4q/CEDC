package distributed.core;



import distributed.result.ResultCycle;
import distributed.result.annotations.NotRecordCostInCycle;

import javax.xml.crypto.Data;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncMailer extends Process{

    public static final int PHASE_AGENT = 1;
    public static final int PHASE_MAILER = 2;

    private Map<Integer,SyncAgent> agents;
    private Queue<Message> messageQueue;
    private int cycleCount;
    private int messageCount;
    private Set<Integer> agentReady;
    private AtomicInteger phase;

    private double[] costInCycle;
    private  double[] dataSumInCycle;
    private double[] benefitInCycle;



    private double[] bestCostInCyle;
    private int tail;

    private int time;

    private ResultCycle resultCycle;
    private long startTime;
    private FinishedListener listener;
    private Set<Agent> stoppedAgents;
    private boolean printCycle;
    private List<CycleListener> cycleListeners;
    private List<AgentIteratedOverListener> agentIteratedOverListeners;
    private double sumCost = 0;
    private double sumData =0;
    private double sumBenefit=0;
    private Map<Integer,int[]> DataAns;

    private Map<Integer,Map<Integer,Double>> AgentCostByTime;
    private Map<Integer,Map<Integer,Double>> AgentDataSumByTime;
    private Map<Integer,Map<Integer,Double>> AgentBenefitByTime;




    private int t;
    private int ncccs;
    private boolean recordCostInCycle;
    private int maxDistance;

    public SyncMailer(){
        super("mailer");
        agents = new HashMap<>();
        messageQueue = new LinkedList<>();
        agentReady = new HashSet<>();
        phase = new AtomicInteger(PHASE_AGENT);
        costInCycle = new double[2];
        bestCostInCyle = new double[2];
        benefitInCycle = new double[2];
        dataSumInCycle = new double[2];
        DataAns = new HashMap<>();
        stoppedAgents = new HashSet<>();
        cycleListeners = new LinkedList<>();
        agentIteratedOverListeners = new LinkedList<>();
        recordCostInCycle = true;

        AgentCostByTime = new HashMap<>();
        AgentDataSumByTime = new HashMap<>();
        AgentBenefitByTime = new HashMap<>();
    }

    public SyncMailer(FinishedListener finishedListener){
        this();
        listener = finishedListener;
    }


    public void setPrintCycle(boolean printCycle) {
        this.printCycle = printCycle;
    }

    public void registerCycleListener(CycleListener listener){
        cycleListeners.add(listener);
    }

    public void registerAgentIteratedOverListener(AgentIteratedOverListener listener){
        agentIteratedOverListeners.add(listener);
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





    public void register(SyncAgent agent){
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

    @Override
    public void preExecution() {
        startTime = new Date().getTime();
    }

    @Override
    public void execution() {
        if (phase.get() == PHASE_MAILER){
            synchronized (messageQueue){
                while (!messageQueue.isEmpty()){
                    Message message = messageQueue.poll();
                    messageCount++;
                    if (agents.get(message.getIdReceiver()).isRunning()){
                        agents.get(message.getIdReceiver()).addMessage(message);
//                        int senderNccc = agents.get(message.getIdSender()).ncccs;
//                        int receiverNcccc = agents.get(message.getIdReceiver()).ncccs;
//                        ncccs = Integer.max(ncccs,senderNccc);
//                        ncccs = Integer.max(ncccs,receiverNcccc);
//                        System.out.println(message + "   nccc:" + Integer.max(senderNccc+t, receiverNcccc));
//                        agents.get(message.getIdReceiver()).ncccs = Integer.max(senderNccc + t,receiverNcccc);
                    }
                }

                boolean canTerminate = true;
                for (SyncAgent syncAgent : agents.values()){
                    if (syncAgent.isRunning()){
                        canTerminate = false;
                    }
                    else {
                        stoppedAgents.add(syncAgent);
                    }
                }


                if (recordCostInCycle) {
                    if (tail == costInCycle.length - 1){
                        expand();
                    }
                    for (Agent agent : stoppedAgents) {
                        sumCost += agent.getLocalCost();
                        System.out.println("------------------"+agent.id+Arrays.toString(agent.data));
                        DataAns.put(agent.id,agent.data);
                        maxDistance = agent.maxDistance;
                    }
                    costInCycle[tail] = sumCost/2;
                    dataSumInCycle[tail] = sumData;
                    benefitInCycle[tail] = sumBenefit;

                }

                sumCost = 0;
                sumBenefit =0;
                sumData = 0;
                if (recordCostInCycle) {
                    Agent rootAgent = agents.get(1);

                }



                tail++;


                for (CycleListener listener : cycleListeners){
                    listener.onCycleChanged(tail);
                }
                for (AgentIteratedOverListener listener : agentIteratedOverListeners){
                    listener.agentIteratedOver(agents);
                }


                printCycle = true;
                if (printCycle){
                    if(tail <10){
                        System.out.println("--------------cycle -------------------------" + tail);
                    }
                }
                if (canTerminate){
                    stopProcess();
                }
                else {
                    cycleCount++;
                    agentReady.clear();
                    phase.set(PHASE_AGENT);
                }
            }
        }
    }

    public int getMessageCount() {
        return messageCount;
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

            if (agentReady.size() == agents.size() - stoppedAgents.size()){
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

    public ResultCycle getResultCycle() {
        return resultCycle;
    }



    public synchronized void setResultCycle(int id,ResultCycle resultCycle){

        if (this.resultCycle == null){
            this.resultCycle = resultCycle;
            this.resultCycle.setAgentDisNeighbourMap(id,resultCycle.disNeighbourMap);
            this.resultCycle.setAgentUsers(id,resultCycle.users);
            this.resultCycle.setAgentUserRequireData(id,resultCycle.userRequireData);

        }
        else {
            this.resultCycle.add(resultCycle);
            this.resultCycle.setAgentValues(id,resultCycle.getAgentValue(id));
            this.resultCycle.setAgentDisNeighbourMap(id,resultCycle.disNeighbourMap);
            this.resultCycle.setAgentUsers(id,resultCycle.users);
            this.resultCycle.setAgentUserRequireData(id,resultCycle.userRequireData);
        }
        if (this.resultCycle.getAgents().size() == agents.size()){
            this.resultCycle.setTotalTime(new Date().getTime() - startTime);
            this.resultCycle.setMessageQuantity(messageCount);
            this.resultCycle.setMaxDistance(maxDistance);
            if (recordCostInCycle){
                this.resultCycle.setCostInCycle(costInCycle,tail);
                this.resultCycle.setDataSumInCycle(dataSumInCycle,tail);
                this.resultCycle.setBenefitInCycle(benefitInCycle,tail);
                this.resultCycle.setDataAns((HashMap<Integer, int[]>) DataAns);
            }
            this.resultCycle.setNcccs(ncccs);
            if (listener != null){
                listener.onFinished(this.resultCycle);
            }
        }
    }

    public interface CycleListener{
        void onCycleChanged(int cycle);
    }
}
