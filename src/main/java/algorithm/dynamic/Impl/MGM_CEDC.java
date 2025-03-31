package algorithm.dynamic.Impl;

import algorithm.local.local_message.*;
import distributed.core.*;
import distributed.result.ResultCycle;

import java.util.*;

public class MGM_CEDC extends DynamicSyncAgent {
    private final static int MSG_INITIAL_BROADCAST = 100001;

    private final static int MSG_RESPONSE = 100003;
    private final static int MSG_SYNC_DATA = 100004;
    private final static int MSG_VALUE = 100005;
    private final static int MSG_GAIN = 100006;

    private final static int MSG_NEW = 100007;
    private final static int MSG_CHECK = 100008;
    private final static int MSG_CHANGE = 100009;


    private final static int MAX_TIME = 10;
    private int iteration_stage = 0;
    private Map<Integer,BroadcastMessage> lastSendMessage;
    private Map<Integer,Integer> neighbourValueIndexMap;
    private int acceptTag;
    private Map<Integer,Integer> localAcceptTag;
    private Map<Integer, AskMessage> localAcceptAsk;
    private Map<Integer, ValueMessage> localAcceptValue;
    private Map<Integer, GainMessage> localAcceptGain;
    private Map<Integer, ResponseMessage> localAcceptRes;

    private Map<Integer, NewvalueMessage> localAcceptNewvalue;

    private Map<Integer, CheckMessage> localAcceptCheck;
    private Map<Integer, ChangeMessage> localAcceptChange;
    int count = 1 ;
    private int minvalueIndex;
    private int bestValueIndex;
    private int passibleValueIndex;
    private boolean isAbleTochangeValue;
    private boolean isSatisfy;
    private boolean isChange;

    private int notOKStep;
    protected DataDelayView dataDelayViewOld;

    private double gama;
    private double costCE;
    private double costEE;

    private Map<Integer,double[]> gain_ub;
    private Map<Integer, Boolean> spaceNotAssign;

    public MGM_CEDC(DynamicEDCAgentDTO agentDTO, DynamicSyncMailer mailer, double gama, double costCE, double costEE) {
        super(agentDTO, mailer);
        iteration_stage = 0;
        this.lastSendMessage = new HashMap<>();
        for(int neighbour : allneighbours){
            lastSendMessage.put(neighbour,new BroadcastMessage() );
        }
        neighbourValueIndexMap = new HashMap<>();
        acceptTag = 0;
        localAcceptTag = new HashMap<>();
        localAcceptAsk = new HashMap<>();
        localAcceptRes = new HashMap<>();
        localAcceptGain = new HashMap<>();
        localAcceptValue = new HashMap<>();
        localAcceptNewvalue = new HashMap<>();

        localAcceptCheck = new HashMap<>();
        localAcceptChange = new HashMap<>();

        this.gama=gama;
        this.costCE = costCE;
        this.costEE = costEE;

        initGainUBAndSpace(costCE);
        dataDelayViewOld = new DataDelayView(id,data.length ,allneighbours,neighboursUnderLimit);
        notOKStep=0;
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

    public void broadcastDataDelayMsg(int acceptTag) {
        for (int i : allneighbours) {
            BroadcastMessage brodcastMessage = new BroadcastMessage(id,valueIndex, dataDelayView.sendDataDelay(i),acceptTag);
            Print("BroadcastMessage: Send"+id +"->"+"Receive"+i+" DataDelay: "+ Arrays.toString(brodcastMessage.getSendDataDelay())+" acceptTag:"+acceptTag);
            if(!localAcceptAsk.keySet().contains(i)){
                sendMessage(new Message(id, i, MSG_INITIAL_BROADCAST, brodcastMessage));
            }
        }
    }

    @Override
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
                    localAcceptTag.put(sendId,0);
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
            case MSG_VALUE:{
                ValueMessage valueMessage = (ValueMessage) message.getValue();
                int sendId = valueMessage.getSendId();
                localAcceptValue.put(sendId,valueMessage);
                checkAndSetGainMessage();
                break;
            }
            case MSG_GAIN:{
                GainMessage gainMessage = (GainMessage) message.getValue();
                int sendId = gainMessage.getSendId();
                localAcceptGain.put(sendId,gainMessage);
                checkAndChangeValue();
                break;
            }
            case MSG_NEW:{
                NewvalueMessage newvalueMessage = (NewvalueMessage) message.getValue();
                int sendId = newvalueMessage.getSendId();
                localAcceptNewvalue.put(sendId,newvalueMessage);
                checkAndSetCheckMessage();
                break;
            }
            case MSG_CHECK:{
                CheckMessage checkMessage = (CheckMessage) message.getValue();
                int sendId = checkMessage.getSendId();
                localAcceptCheck.put(sendId,checkMessage);
                checkAndSetChangeMessage();
                break;
            }
            case MSG_CHANGE:{
                ChangeMessage changeMessage = (ChangeMessage) message.getValue();
                int sendId = changeMessage.getSendId();
                localAcceptChange.put(sendId,changeMessage);
                checkAndChange();
                break;
            }

        }
    }
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
            if(notOKStep>10){
                ArrayList<Integer> notOKIndex = new ArrayList<>();
                for(int index :InvalidDataIndex){
                    if(data[index]==0){
                        notOKIndex.add(index);
                    }
                }
                broadcastSyncDataMessage(notOKIndex);
            }
//            else if(notOKStep>20){
//                acceptTag = 1 ;
//                iteration_stage = 1;
//                broadcastDataDelayMsg(acceptTag);
//            }
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
//                minvalueIndex = selectBestValue();
//                double oldCost = getLocalCost(valueIndex);
//                double newCost = getLocalCost(minvalueIndex);
//                double gain = newCost-oldCost;
//                StringBuilder stringBuilder = new StringBuilder();
//                for(int index : feasibleValueIndex){
//                    if(isAvailableValueIndex(index)){
//                        stringBuilder.append("     "+Arrays.toString(transFormValueIndexToData(index))+"+"+PrintLocalCost(index));
//                    }
//                }
//                Print(stringBuilder.toString());
//                Print("-------------------------Gain"+newCost+"   "+oldCost+"       "+gain+"-------------------------");
//                Print("valueIndexChange"+Arrays.toString(transFormValueIndexToData(valueIndex))+"->"+Arrays.toString(transFormValueIndexToData(minvalueIndex)));
                int[] newDataDelay = dataDelayView.getLocalDataDelay(transFormValueIndexToData(minvalueIndex));
                ValueMessage valueMessage = new ValueMessage(id,valueIndex,newDataDelay);
                broadcastValueMsg(valueIndex,newDataDelay);
                iteration_stage =1;
                broadcastTime++;
            }
        }
    }

    private void checkAndSetCheckMessage() {
        if (localAcceptNewvalue.keySet().size() == allneighbours.length) {
            boolean[] check = new boolean[data.length];
            Arrays.fill(check, false);

            int[] tmpData1 = transFormValueIndexToData(passibleValueIndex);
            for (int dataIndex = 0; dataIndex < tmpData1.length; dataIndex++) {
                if (tmpData1[dataIndex] == 1) {
                    check[dataIndex] = true;
                }
            }
            for (Integer agentID : localAcceptNewvalue.keySet()) {
                NewvalueMessage newvalueMessage = localAcceptNewvalue.get(agentID);
                int valueIndex = newvalueMessage.getValueIndex();
                int[] tmpData = transFormValueIndexToData(valueIndex);
                for (int dataIndex = 0; dataIndex < tmpData.length; dataIndex++) {
                    if (tmpData[dataIndex] == 1) {
                        check[dataIndex] = true;
                    }
                }
            }
            if(checkIsTrue(check)){
                isSatisfy = true;
            }else{
                isSatisfy = false;
            }
            localAcceptNewvalue.clear();
            CheckMessage checkMessage = new CheckMessage(id, isSatisfy);
            localAcceptCheck.put(this.id,checkMessage);
            broadcastCheckMsg(isSatisfy);
        }
    }

    private void checkAndSetChangeMessage() {
        if (localAcceptCheck.keySet().size() == allneighbours.length+1) {
            boolean isAbleTochangeValue = true;
            for (Integer agentID : localAcceptCheck.keySet()) {
                CheckMessage checkMessage = localAcceptCheck.get(agentID);
                if (checkMessage.isSatisfy() == false) {
                    isAbleTochangeValue = false;
                    break;
                }
            }
            ChangeMessage changeMessage = new ChangeMessage(id, isAbleTochangeValue);
            localAcceptChange.put(this.id, changeMessage);
            broadcastChangeMsg(isAbleTochangeValue);
            localAcceptCheck.clear();
        }
    }

    private void checkAndChange() {
        if (localAcceptChange.keySet().size() == allneighbours.length+1) {
            isChange = true;
            for (Integer agentID : localAcceptChange.keySet()) {
                ChangeMessage changeMessage = localAcceptChange.get(agentID);
                if (!changeMessage.isAbleTochangeValue()) {
                    isChange = false;
                    break;
                }
            }
            if(isChange){
                valueIndex=passibleValueIndex;
                data = transFormValueIndexToData(valueIndex);
                initLocalDataDelay();
                UpdateLocalDataDelay();
            }
            localAcceptChange.clear();
            count++;
            acceptTag = 0;
            iteration_stage=0;
            broadcastDataDelayMsg(acceptTag);
        }
    }
    private void broadcastSyncDataMessage(ArrayList<Integer> notOKIndex) {
        for (int i : allneighbours) {
            SyncDataMessage syncDataMessage = new SyncDataMessage(id,valueIndex, dataDelayView.sendDataDelay(i),notOKIndex);
            Print("SyncDataMessage: Send"+id +"->"+"Receive"+i+" DataDelay: "+ Arrays.toString(syncDataMessage.getSendDataDelay())+" notOKIndex:"+notOKIndex.toString());
            sendMessage(new Message(id, i, MSG_SYNC_DATA, syncDataMessage));
        }
    }

    private void UpdateLocalDataDelay() {
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

    private int selectBestValue() {
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

    private boolean isAvailableValueIndex(Integer index) {
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
        return true;
    }
    public void broadcastGainMsg(int minvalueIndex, double gain) {
        for (int i : allneighbours) {
            GainMessage gainMessage = new GainMessage(id,minvalueIndex,gain);
            Print("GainMessage: Send"+id +"->"+"Receive"+i+" Gain: "+gain);
            sendMessage(new Message(id, i, MSG_GAIN, gainMessage));
        }
    }
    public void broadcastNewvalueMsg(int valueIndex) {
        for (int i : allneighbours) {
            NewvalueMessage newvalueMessage = new NewvalueMessage(id,valueIndex);
            sendMessage(new Message(id, i, MSG_NEW, newvalueMessage));
        }
    }

    public void broadcastCheckMsg(boolean isSatisfy) {
        for (int i : allneighbours) {
            CheckMessage checkMessage = new CheckMessage(id,isSatisfy);
            sendMessage(new Message(id, i, MSG_CHECK, checkMessage));
        }
    }

    public void broadcastChangeMsg(boolean isAbleTochangeValue) {
        for (int i : allneighbours) {
            ChangeMessage changeMessage = new ChangeMessage(id,isAbleTochangeValue);
            sendMessage(new Message(id, i, MSG_CHANGE, changeMessage));
        }
    }

    private void checkAndSetGainMessage() {
        if(localAcceptValue.keySet().size() == allneighbours.length){ //收到所有邻居的Ask 消息
            calculateGainMessage();
        }
    }

    private void calculateGainMessage() {
        minvalueIndex = selectBestValue();
        double oldCost = getLocalCost(valueIndex);
        double newCost = getLocalCost(minvalueIndex);
        double gain = newCost - oldCost;
        StringBuilder stringBuilder = new StringBuilder();
        for (int index : feasibleValueIndex) {
            if (isAvailableValueIndex(index)) {
                stringBuilder.append("     " + Arrays.toString(transFormValueIndexToData(index)) + "+" + PrintLocalCost(index));
            }
        }
        Print(stringBuilder.toString());
        Print("-------------------------Gain" + newCost + "   " + oldCost + "       " + gain + "-------------------------");
        Print("valueIndexChange" + Arrays.toString(transFormValueIndexToData(valueIndex)) + "->" + Arrays.toString(transFormValueIndexToData(minvalueIndex)));
        GainMessage gainMessage = new GainMessage(id, minvalueIndex, gain);
        localAcceptGain.put(id,gainMessage);
        broadcastGainMsg(minvalueIndex, gain);
    }
    private void checkAndChangeValue(){
        if(localAcceptGain.size() == allneighbours.length+1){
            DataDelayView tempDataDelay = this.dataDelayView.deepCopy();
            double maxGain = 0;
            HashSet<Integer> maxAgentIDSet = new HashSet<>();
            boolean[] check = new boolean[data.length];
            Arrays.fill(check,false);
            for(Integer agentID: localAcceptGain.keySet()){
                GainMessage gainMessage = localAcceptGain.get(agentID);
                double gain = gainMessage.getGain();
                int valueIndex = gainMessage.getValueIndex();
                int[] tmpData = transFormValueIndexToData(valueIndex);
                for(int dataIndex=0;dataIndex<tmpData.length;dataIndex++){
                    if(tmpData[dataIndex] == 1){
                        check[dataIndex] = true;
                    }
                }
                if(maxGain < gain ){
                    maxGain = gain;
                    maxAgentIDSet.clear();
                    maxAgentIDSet.add(agentID);
                }
                else if(maxGain == gain){
                    maxAgentIDSet.add(agentID);
                }
                if(gain<0){
                    maxAgentIDSet.add(agentID);
//                System.out.println("#######################################################################################################"+gain);
                }
            }
            ArrayList<Integer> list = new ArrayList(maxAgentIDSet);
            Collections.sort(list);
            Integer maxAgentID = list.get(0);
            if (checkIsTrue(check) || maxAgentID == id){
                isAbleTochangeValue = true;
                valueIndex = minvalueIndex;
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
            localAcceptValue.clear();
            localAcceptGain.clear();
        }
    }

    private boolean checkIsTrue(boolean[] check) {
        for(boolean bool : check){
            if(!bool)
                return false;
        }
        return true;
    }

    private void broadcastResponseMsg(boolean check, int maxAgentID, double maxGain) {
        ResponseMessage responseMessage;
        for (int i : allneighbours) {
            responseMessage = new ResponseMessage(id,check, maxAgentID, maxGain);
            Print("ResponseMessage: Send"+id +"->"+"Receive"+i+" Check:"+check +" MaxAgentID: "+maxAgentID+" MaxGain: "+maxGain);
            sendMessage(new Message(id,i,MSG_RESPONSE,responseMessage));
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

    private double getLocalCost(Integer index) {
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

    private String PrintLocalCost(Integer index) {
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


    private void initGainUBAndSpace(double costCE) {
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
        timeStep++;
        broadcastDataDelayMsg(acceptTag);
        mailer.agentDone(this.id);
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
                        if(userRequire[dataIndex] != 0) {
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

    @Override
    public void runFinished() {

    }

    public void broadcastValueMsg(int valueIndex, int[] newDataDelay) {
        for (int i : allneighbours) {
            ValueMessage valueMessage = new ValueMessage(id,valueIndex,newDataDelay);
            Print("ValueMessage: Send"+id +"->"+"Receive"+i+" newDataDelay: "+Arrays.toString(newDataDelay));
            sendMessage(new Message(id, i, MSG_VALUE, valueMessage));
        }
    }
}
