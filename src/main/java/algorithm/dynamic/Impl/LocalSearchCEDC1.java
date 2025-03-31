package algorithm.dynamic.Impl;

import algorithm.local.local_message.*;
import distributed.core.*;
import distributed.result.ResultCycle;

import java.util.*;

public abstract class LocalSearchCEDC1 extends DynamicSyncAgent {
    public final static int MSG_INITIAL_BROADCAST = 100001;
    public final static int MSG_ASK = 100002;
    public final static int MSG_RESPONSE = 100003;
    public final static int MSG_SYNC_DATA = 100004;
    public final static int MSG_VALUE = 100005;
    public final static int MAX_TIME = 10;

    public int iteration_stage = 0;
    public Map<Integer, BroadcastMessage> lastSendMessage;
    public Map<Integer,Integer> neighbourValueIndexMap;
    public int acceptTag;
    public Map<Integer,Integer> localAcceptTag;
    public Map<Integer, AskMessage> localAcceptAsk;
    //public Map<Integer, MyMessage> localAcceptValue;
    public Map<Integer, ValueMessage> localAcceptValue;
    public Map<Integer, ResponseMessage> localAcceptRes;
    int count = 1 ;
    public int ValueIndex;
    public boolean isAbleTochangeValue;


    public int notOKStep;

    public DataDelayView dataDelayViewOld;


    public double gama;
    public double costCE;
    public double costEE;

    public Map<Integer,double[]> gain_ub;
    public Map<Integer, Boolean> spaceNotAssign;

    public final static double P = 0.4;

    public LocalSearchCEDC1(DynamicEDCAgentDTO agentDTO, DynamicSyncMailer mailer) {
        super(agentDTO, mailer);
    }

    @Override
    protected void initRun() {
        int index = (int)(Math.random() * feasibleValueIndex.size());
        valueIndex = feasibleValueIndex.get(index);
        setValueIndexAndData(valueIndex);
        Print("Agent"+id+"  Data："+ Arrays.toString(data));
        initLocalDataDelay();
        broadcastDataDelayMsg(acceptTag);
    }

    private void initLocalDataDelay() {
        int[] localDataDelay = new int[data.length];
        for(int dataId=0;dataId<data.length;dataId++){
            if(data[dataId] == 1){
                localDataDelay[dataId] = 0;
            }else {
                localDataDelay[dataId] = MAX_VALUE;
            }
        }
        dataDelayView.updateDataLatencyView(id,localDataDelay);
    }

//    public void broadcastDataDelayMsg(int acceptTag) {
//        for (int i : allneighbours) {
//            BroadcastMessage brodcastMessage = new BroadcastMessage(id,valueIndex, dataDelayView.sendDataDelay(i),acceptTag);
//            Print("BroadcastMessage: Send"+id +"->"+"Receive"+i+" DataDelay: "+ Arrays.toString(brodcastMessage.getSendDataDelay())+" acceptTag:"+acceptTag);
//            if (!localAcceptValue.keySet().contains(i)) {
//                sendMessage(new Message(id, i, MSG_INITIAL_BROADCAST, brodcastMessage));
//            }
//        }
//    }


    public abstract void broadcastDataDelayMsg(int acceptTag);

    private void checkAndSetAskMessage() {
        UpdateLocalDataDelay();

        List<Integer> InvalidDataIndex = dataDelayView.InvalidDataIndex(maxDistance);
        if(InvalidDataIndex.size()>0 && notOKStep<20){
            acceptTag = 0;
            int oldValueIndex = transFormDataToValueIndex(data);
            int[] datatemp = new int[data.length];
            int count=0;
            System.arraycopy(data, 0, datatemp, 0, data.length);
            int assigncount =0;
            for(int datavalue : data){
                assigncount += datavalue;
            }
            while (assigncount<serverDataLimit){
                System.out.println(assigncount);
                for(int index :InvalidDataIndex){
                    int i = (int) (Math.random() * 2);
                    if(i == 1 ){
                        datatemp[index] = 1;
                        assigncount++;
                    }
                }
            }

            int chenckvalueindex =  transFormDataToValueIndex(datatemp);

            if(feasibleValueIndex.contains(chenckvalueindex)){
                valueIndex = chenckvalueindex;
                data = datatemp;
            }
            Print("Agent"+id+": "+ Arrays.toString(data));
            notOKStep++;
            if(notOKStep>10 && notOKStep<20){
                ArrayList<Integer> notOKIndex = new ArrayList<>();
                for(int index :InvalidDataIndex){
                    if(data[index]==0){
                        notOKIndex.add(index);
                    }
                }
                broadcastSyncDataMessage(notOKIndex);
            }
            else if(notOKStep>20){
                acceptTag = 1 ;
                iteration_stage = 1;
                broadcastDataDelayMsg(acceptTag);
            }
            else {
                if(valueIndex != oldValueIndex){
                    initLocalDataDelay();
                    UpdateLocalDataDelay();
                    broadcastDataDelayMsg(acceptTag);
                }else{
                    broadcastDataDelayMsg(acceptTag);
                }
            }

        }
        else {
            acceptTag =1;
            broadcastDataDelayMsg(acceptTag);
            boolean check = true;
            for(int neighbourAcceptTag : localAcceptTag.values()){
                if(neighbourAcceptTag == 0){
                    check =false;
                    break;
                }
            }
            if (check && broadcastTime<count){
                finishHardConstraint();
            }
        }
    }

    private void UpdateLocalDataDelay() {
        //更新LocalDataDelay
        int[] localDataDelay = dataDelayView.getDataLatencyView(id);
        for(int neighbour : allneighbours){
            int[] neighbourDataDelay = dataDelayView.getDataLatencyView(neighbour);
            for(int dataId =0;dataId<data.length;dataId++){
                if(neighbourDataDelay[dataId] !=MAX_VALUE && localDataDelay[dataId] > neighbourDataDelay[dataId]+ dataDelayView.getAgentDis().get(neighbour) ){
                    localDataDelay[dataId] = neighbourDataDelay[dataId]+ dataDelayView.getAgentDis().get(neighbour);
                }
            }
        }
    }

    private void broadcastSyncDataMessage(ArrayList<Integer> notOKIndex) {
        for (int i : allneighbours) {
            SyncDataMessage syncDataMessage = new SyncDataMessage(id,valueIndex, dataDelayView.sendDataDelay(i),notOKIndex);
            Print("SyncDataMessage: Send"+id +"->"+"Receive"+i+" DataDelay: "+ Arrays.toString(syncDataMessage.getSendDataDelay())+" notOKIndex:"+notOKIndex.toString());
            sendMessage(new Message(id, i, MSG_SYNC_DATA, syncDataMessage));
        }
    }

    public abstract void finishHardConstraint();


    public int selectBestValue() {
        double maxBenfit = (double) Integer.MIN_VALUE;
        int minValue = -1;
        for (Integer index : feasibleValueIndex) {
            if(isAvailableValueIndex(index)){
                double localCost = getLocalCost(index);
                if (maxBenfit < localCost) {
                    maxBenfit = localCost;
                    minValue = index;
                }
            }
        }
        if(minValue ==-1){
            minValue = valueIndex;
            maxBenfit = 0.0;
        }
        Print("oldValue"+valueIndex+" newValue "+minValue);
        return minValue;
    }

    public boolean isAvailableValueIndex(Integer index) {
        boolean[] check = new boolean[data.length];
        Arrays.fill(check,false);
        int[] tmpData = transFormValueIndexToData(index);

        for(int dataIndex=0;dataIndex<tmpData.length;dataIndex++){
            if(tmpData[dataIndex] == 1){
                check[dataIndex] = true;
            }
        }
        for(Integer agentID: neighbourValueIndexMap.keySet()){
            Integer neighbourValueIndex = neighbourValueIndexMap.get(agentID);
            int[] neighbourData = transFormValueIndexToData(neighbourValueIndex);
            for(int dataIndex=0;dataIndex<neighbourData.length;dataIndex++){
                if(neighbourData[dataIndex] == 1){
                    check[dataIndex] = true;
                }
            }
        }

        for(int i=0;i<data.length;i++){
            if(!check[i]){
                return false;
            }
        }
        return  true;
    }

    public abstract void checkAndSetResponseMessage();

    public abstract void calculateResponseMessage();

    public boolean checkIsTrue(boolean[] check) {
        for(boolean bool : check){
            if(!bool)
                return false;
        }
        return true;
    }

//    public abstract void broadcastResponseMsg(boolean check);

    public void checkResponse() {
        if(localAcceptRes.size() == allneighbours.length){
            if(isAbleTochangeValue){
                valueIndex=ValueIndex;
                data = transFormValueIndexToData(valueIndex);
                initLocalDataDelay();
                UpdateLocalDataDelay();
            }
            count++;
            acceptTag = 0;
            iteration_stage=0;
            Print("Agent"+id+"  Data："+ Arrays.toString(data));
            broadcastDataDelayMsg(acceptTag);
            localAcceptTag.clear();
            localAcceptAsk.clear();
            localAcceptValue.clear();
            localAcceptRes.clear();
            Print("---------------------------------------------stage:"+count+"-----------------------------------------------");
        }
    }

    @Override
    public void allMessageDisposed() {
        super.allMessageDisposed();
        if (count >= MAX_TIME) {
            System.arraycopy(data, 0, dataOld, 0, data.length);
            agentWaiting();
        }
    }

    public double getLocalCost(Integer index) {
        InitalMinDelayByUser();
        int benfit = 0;
        double cost = 0.0;
        initGainUBAndSpace(costCE);
        for (int nId : allneighbours) {
            benfit += calculateConstraintLatencyBenefit(nId,index);
            if(neighbourValueIndexMap.containsKey(nId)){
                cost += calculateConstraintBR(nId, index, neighbourValueIndexMap.get(nId));
            }else{
                return 0;
            }
        }
        return gama*benfit+cost;
    }


    private int calculateConstraintLatencyBenefit(int neighbourId, Integer agentValueIndex) {
        Set<Integer> allUsersUnderConstaraint = new HashSet<>();
        for(int user : neighboursHostUsers.get(neighbourId)){
            allUsersUnderConstaraint.add(user);
        }
        for(int user : users ){
            allUsersUnderConstaraint.add(user);
        }


        int[] agentData = transFormValueIndexToData(agentValueIndex);
        int[] localDelay = dataDelayView.getLocalDataDelay(agentData);
        int[] neighbourDelay = dataDelayView.getDataLatencyView(agentData,neighbourId);
        int benefit = 0;


        for(Integer user : allUsersUnderConstaraint){
            int[] userRequire = userRequireData.get(timeStep).get(user);
            if(userHosts.containsKey(user)){
                if(ArraysContains(userHosts.get(user),neighbourId)){
                    for(int dataIndex=0;dataIndex<this.data.length;dataIndex++){
                        if(userRequire[dataIndex] != 0) {
                            int minDelay = Math.min(localDelay[dataIndex], neighbourDelay[dataIndex]);
                            if(agentData[dataIndex] == 1 ){
                                minDelay = 0;
                            }
                            int[] minDelayCheck = minDelayByUserCheck.get(user);
                            Integer recoderedDelay = minDelayCheck[dataIndex];
                            if(minDelay < recoderedDelay){
                                benefit += recoderedDelay-minDelay;
                                minDelayCheck[dataIndex] = minDelay;
                            }
                        }
                    }
                }else{
                    for(int dataIndex=0;dataIndex<this.data.length;dataIndex++){
                        if(userRequire[dataIndex] != 0) { //请求该数据
                            int minDelay = localDelay[dataIndex];
                            if(agentData[dataIndex] == 1 ){
                                minDelay = 0;
                            }
                            int[] minDelayCheck = minDelayByUserCheck.get(user);
                            Integer recoderedDelay = minDelayCheck[dataIndex];
                            if(minDelay < recoderedDelay){
                                benefit += recoderedDelay-minDelay;
                                minDelayCheck[dataIndex] = minDelay;
                            }
                        }
                    }
                }
            }else {
                for(int dataIndex=0;dataIndex<this.data.length;dataIndex++){
                    if(userRequire[dataIndex] != 0) {
                        int minDelay = neighbourDelay[dataIndex];
                        int[] minDelayCheck = minDelayByUserCheck.get(user);
                        Integer recoderedDelay = minDelayCheck[dataIndex];
                        if(minDelay < recoderedDelay){
                            benefit += recoderedDelay-minDelay;
                            minDelayCheck[dataIndex] = minDelay;
                        }
                    }
                }
            }
        }
        return benefit;
    }

    public boolean ArraysContains(int[] arr,int num){
        for (int value : arr) {
            if (num == value) {
                return true;
            }
        }
        return false;
    }


    public double calculateConstraintBR(int neighbourId, int agentValueIndex, int neighbourValueIndex){
        int[] neighbourData =  transFormValueIndexToData(neighbourValueIndex);
        int[] agentData = transFormValueIndexToData(agentValueIndex);
        int len = agentData.length;
        double BR_local_neighbour=0.0;
        Map<Integer, int[]> dataDelayOld = dataDelayViewOld.getDataDelay();
        int[] localOldDataDelay = dataDelayOld.get(id);
        int[] neighboOldDataDelay = dataDelayOld.get(neighbourId);
        double[] gain_ub_local = gain_ub.get(id);
        double[] gain_ub_neighbour = gain_ub.get(neighbourId);
        int fullSpace = serverDataLimit * 2;
        int localAssignSpace =0;
        int neighbourAssignsSpace =0;
        for(int k=0;k<len;k++){
            if(gain_ub_local[k]>localOldDataDelay[k]*costEE){
                BR_local_neighbour += (gain_ub_local[k]-localOldDataDelay[k]*costEE)*agentData[k];
                gain_ub_local[k]=localOldDataDelay[k]*costEE;
            }
            localAssignSpace += agentData[k];
            if(gain_ub_neighbour[k]>neighboOldDataDelay[k]*costEE){
                BR_local_neighbour += (gain_ub_neighbour[k]-neighboOldDataDelay[k]*costEE)*neighbourData[k];
                gain_ub_neighbour[k]=neighboOldDataDelay[k]*costEE;
            }
            neighbourAssignsSpace += neighbourData[k];
        }
        if(spaceNotAssign.get(id)){
            fullSpace -= localAssignSpace;
            spaceNotAssign.put(id,false);
        }
        if(spaceNotAssign.get(neighbourId)){
            fullSpace -= neighbourAssignsSpace;
            spaceNotAssign.put(neighbourId,false);
        }
        BR_local_neighbour += costCE*fullSpace;
        return BR_local_neighbour;
    };

    public String PrintLocalCost(Integer index) {
        InitalMinDelayByUser();
        int benfit = 0;
        double cost = 0.0;
        initGainUBAndSpace(costCE);

        StringBuilder str = new StringBuilder();

        for (int nId : allneighbours) {
            benfit += calculateConstraintLatencyBenefit(nId,index);
            if(neighbourValueIndexMap.containsKey(nId)){
                cost += calculateConstraintBR(nId, index, neighbourValueIndexMap.get(nId));
            }else{
                str.append(0);
                return str.toString();
            }
        }
        str.append(gama*benfit).append("+").append(cost);
        return str.toString();
    }

    public void initGainUBAndSpace(double costCE) {
        gain_ub = new HashMap<>();
        spaceNotAssign = new HashMap<>();
        double[] gain_ub_local = new double[data.length];
        Arrays.fill(gain_ub_local, costCE);
        gain_ub.put(id,gain_ub_local);
        spaceNotAssign.put(id,true);
        for(int neighbour : allneighbours){
            double[] gain_ub_neigbour = new double[data.length];
            Arrays.fill(gain_ub_neigbour, costCE);
            gain_ub.put(neighbour,gain_ub_neigbour);
            spaceNotAssign.put(neighbour,true);
        }

    }

    @Override
    protected double getLocalCost() {
        return getAgentCost();
    }

    private double getAgentCost(){
        InitalMinDelayByUser();
        int benfit = 0;
        double cost = 0.0;
        int[] data = transFormValueIndexToData(valueIndex);
        for(int i : data){
            cost += 1-i ;
        }

        int[] agentData = transFormValueIndexToData(valueIndex);
        int[] localDelay = dataDelayView.getLocalDataDelay(agentData);

        for(int user : users ){
            int[] require = userRequireData.get(timeStep).get(user);
            for(int index =0 ;index<data.length;index++){
                benfit += (maxDistance - localDelay[index])*require[index];
            }
        }
        return benfit+cost*0.1;
    }

    @Override
    protected double getBenefit() {
        return 0;
    }

    @Override
    protected double getDataSum() {
        return 0;
    }


    @Override
    protected void agentWaiting() {
        super.agentWaiting();
        broadcastTime=0;
        count=1;
        if(timeStep < userRequireData.size()){
            Map<Integer, int[]> userRequireDataByTime = userRequireData.get(timeStep);
            Print("timeStep:"+timeStep+" "+userRequireDataByTime.toString());
        }

        dataDelayViewOld = dataDelayView.deepCopy();
        ResultCycle cycle = new ResultCycle();
        cycle.setTotalCost(getLocalCost(valueIndex) * 1.0 /2);
        cycle.setAgentValues(id,valueIndex);
        cycle.setAgentData(id,data);
        cycle.setDisNeighbour(neighboursUnderLimit);
        cycle.setUsers(users);
        cycle.setUserRequireData(userRequireData.get(timeStep));
        mailer.setResultCycle(id,cycle);

        if(timeStep == userRequireData.size() -1 ){
            stopProcess();
        }

    }

    @Override
    protected void agentAwake() {
        super.agentAwake();
        iteration_stage = 0;
        this.lastSendMessage = new HashMap<>();
        for(int neighbour : allneighbours){
            lastSendMessage.put(neighbour,new BroadcastMessage() );
        }
        neighbourValueIndexMap = new HashMap<>();
        localAcceptTag = new HashMap<>();
        localAcceptAsk = new HashMap<>();
        localAcceptRes = new HashMap<>();
        localAcceptValue = new HashMap<>();
        timeStep++;
        broadcastDataDelayMsg(acceptTag);
        mailer.agentDone(this.id);
    }

    public void disposeMessage(Message message) {
        switch (message.getType()){
            case MSG_INITIAL_BROADCAST:{
                if(iteration_stage == 0){
                    BroadcastMessage broadcastMessage = (BroadcastMessage) message.getValue();
                    int sendId = broadcastMessage.getSendId();
                    int receivedValueIndex = broadcastMessage.getValueIndex();
                    int[] receivedDataDelay = broadcastMessage.getSendDataDelay();
                    neighbourValueIndexMap.put(sendId,receivedValueIndex);
                    dataDelayView.updateDataLatencyView(sendId,receivedDataDelay);
                    localAcceptTag.put(sendId,broadcastMessage.getAcceptTag());
                    if(localAcceptTag.size() == allneighbours.length){
                        checkAndSetAskMessage();
                        // 清除已经收到的Tag数据，收到Ask消息的部分Tag为1
                        ArrayList<Integer> removeIDs = new ArrayList<>();
                        for(Integer id : localAcceptTag.keySet()){
                            if(localAcceptTag.get(id) == 0){
                                removeIDs.add(id);
                            }
                        }
                        for(Integer id : removeIDs){
                            localAcceptTag.remove(id);
                        }

                    }
                }
                break;
            }
            case MSG_SYNC_DATA:{
                SyncDataMessage syncDataMessage = (SyncDataMessage) message.getValue();
                int sendId = syncDataMessage.getSendId();
                int receivedValueIndex = syncDataMessage.getValueIndex();
                int[] receivedDataDelay = syncDataMessage.getSendDataDelay();
                ArrayList<Integer> syncDataIndex = syncDataMessage.getSyncDataIndex();
                neighbourValueIndexMap.put(sendId,receivedValueIndex);
                dataDelayView.updateDataLatencyView(sendId,receivedDataDelay);
                localAcceptTag.put(sendId,1);
                int[] datatemp =  new int[data.length];
                System.arraycopy(data, 0, datatemp, 0, data.length);
                int assigncount =0;
                for(int datavalue : data){
                    assigncount += datavalue;
                }
                while (assigncount<serverDataLimit){
                    System.out.println(assigncount);
                    for(int index :syncDataIndex){
                        int i = (int) (Math.random() * 2);
                        if(i == 1){
                            datatemp[index] = 1;
                            assigncount++;
                        }
                    }
                }
                int chenckvalueindex =  transFormDataToValueIndex(datatemp);
                if(feasibleValueIndex.contains(chenckvalueindex)){
                    valueIndex = chenckvalueindex;
                    data = datatemp;
                }
                Print("Agent"+id+": "+ Arrays.toString(data));
                initLocalDataDelay();
                UpdateLocalDataDelay();
                if(localAcceptTag.size() == allneighbours.length){
                    checkAndSetAskMessage();
                    // 清除已经收到的Tag数据，收到Ask消息的部分Tag为1
                    ArrayList<Integer> removeIDs = new ArrayList<>();
                    for(Integer id : localAcceptTag.keySet()){
                        if(localAcceptTag.get(id) == 0){
                            removeIDs.add(id);
                        }
                    }
                    for(Integer id : removeIDs){
                        localAcceptTag.remove(id);
                    }

                }
                break;
            }
        }
    }
}
