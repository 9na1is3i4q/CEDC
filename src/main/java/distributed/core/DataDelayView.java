package distributed.core;

import lombok.Data;

import java.util.*;

@Data
public class DataDelayView {
    private Integer localAgentId;


    private Map<Integer,int[]> dataDelay = new HashMap<>();
    private Map<Integer,Integer> agentDis = new HashMap<>();


    public DataDelayView(Integer agentId, int datanum, int[] allneighbours, Map<Integer, int[]> neighboursUnderLimit) {
        this.localAgentId = agentId;
        int[] agentLatency = new int[datanum];
        Arrays.fill(agentLatency,Integer.MAX_VALUE);
        this.dataDelay.put(agentId,agentLatency);
        for(int neighbourId : allneighbours){
            int[] neighbourLatency = new int[datanum];
            Arrays.fill(neighbourLatency,Integer.MAX_VALUE);
            this.dataDelay.put(neighbourId,neighbourLatency);
        }
        for(Integer dis : neighboursUnderLimit.keySet()){
            for(int agent : neighboursUnderLimit.get(dis)){
                this.agentDis.put(agent,dis);
            }
        }
    }

    private DataDelayView(Integer localAgentId, HashMap<Integer,int[]> newDataDelay, HashMap<Integer, Integer> newAgentDis) {
        this.localAgentId = localAgentId;
        this.dataDelay = newDataDelay;
        this.agentDis = newAgentDis;
    }




    public DataDelayView deepCopy(){
        HashMap<Integer,int[]> newDataDelay = new HashMap<>();
        HashMap<Integer,Integer> newAgentDis = new HashMap<>();
        for(Integer ID : dataDelay.keySet()){
            int[] oldLatency = dataDelay.get(ID);
            int[] agentLatency = new int[oldLatency.length];
            System.arraycopy(oldLatency, 0, agentLatency, 0, oldLatency.length);
            newDataDelay.put(ID,agentLatency);
        }
        for(Integer ID : agentDis.keySet()){
           newAgentDis.put(ID,agentDis.get(ID));
        }

        DataDelayView dataDelayView = new DataDelayView(localAgentId,newDataDelay,newAgentDis);

        return dataDelayView;
    }


    public void updateDataLatencyView(Integer fromAgentId, int[] dataDelay){
        int[] dataDelaylocal =  Arrays.copyOf(dataDelay,dataDelay.length);

        this.dataDelay.put(fromAgentId,dataDelaylocal);
    }


    public int[] getDataLatencyView(Integer agentId){
        return dataDelay.get(agentId);
    }


    public int[] getDataLatencyView(int[] agentData, int neighbourId) {
        int[] delay = new int[agentData.length];
        for(int i=0; i<agentData.length;i++){
            delay[i] = dataDelay.get(neighbourId)[i];
        }
        for(int valueIndex =0 ; valueIndex<agentData.length;valueIndex++){
            int value = agentData[valueIndex];
            if (value != 0 && agentDis.get(neighbourId) <delay[valueIndex]) {
              delay[valueIndex] = agentDis.get(neighbourId);
            }
        }
        return  delay;
    }


    public int[] allNeighbourDataDelay() {
        int length = dataDelay.get(localAgentId).length;
        int[] allNeighbourDataDelay = new int[length];
        Arrays.fill(allNeighbourDataDelay,Integer.MAX_VALUE);


        for(int neighbour : dataDelay.keySet()){
            if(neighbour != localAgentId){
                int[] neighbourDataDelay = dataDelay.get(neighbour);
                for(int dataId =0;dataId<length;dataId++){
                    if(neighbourDataDelay[dataId] != Integer.MAX_VALUE && allNeighbourDataDelay[dataId] > neighbourDataDelay[dataId]+ agentDis.get(neighbour) ){
                        allNeighbourDataDelay[dataId] = neighbourDataDelay[dataId]+agentDis.get(neighbour);
                    }
                }
            }
        }
        return allNeighbourDataDelay;
    }

    public int[] sendDataDelay(int receivedAgentId) {

        int[] localDataDelay = dataDelay.get(localAgentId);
        int[] sendDataDelay = new int[localDataDelay.length];
        Arrays.fill(sendDataDelay,Integer.MAX_VALUE);
        int length = localDataDelay.length;
        for(int i=0;i<length;i++){
            if(localDataDelay[i] == 0){
                sendDataDelay[i] = 0;
            }
        }
        for(int neighbour : dataDelay.keySet()){
            if(neighbour != receivedAgentId && neighbour != localAgentId){
                int[] neighbourDataDelay = dataDelay.get(neighbour);
                for(int dataId =0;dataId<length;dataId++){
                    if(neighbourDataDelay[dataId] != Integer.MAX_VALUE && sendDataDelay[dataId] > neighbourDataDelay[dataId]+ agentDis.get(neighbour) ){
                        sendDataDelay[dataId] = neighbourDataDelay[dataId]+agentDis.get(neighbour);
                    }
                }
            }
        }
        return sendDataDelay;
    }


    public int[] getLocalDataDelay(int[] agentData) {

        int length = agentData.length;
        int[] localDataDelay = new int[length] ;

        for(int index =0;index < length;index++){
            if(agentData[index] == 1){
                localDataDelay[index] = 0;
            }else {
                localDataDelay[index] = Integer.MAX_VALUE;
            }
        }

        int[] sendDataDelay = new int[length];
        Arrays.fill(sendDataDelay,Integer.MAX_VALUE);
        for(int i=0;i<length;i++){
            if(localDataDelay[i] == 0){
                sendDataDelay[i] = 0;
            }
        }

        for(int neighbour : dataDelay.keySet()){
            if(neighbour != localAgentId){
                int[] neighbourDataDelay = dataDelay.get(neighbour);
                for(int dataId =0;dataId<length;dataId++){

                    if(neighbourDataDelay[dataId] != Integer.MAX_VALUE && sendDataDelay[dataId] > neighbourDataDelay[dataId]+ agentDis.get(neighbour) ){
                        sendDataDelay[dataId] = neighbourDataDelay[dataId]+agentDis.get(neighbour);
                    }
                }
            }
        }
        return sendDataDelay;
    }


    public List<Integer> InvalidDataIndex(int maxDistance){  //获取延迟超过最大距离的数据索引  //局部硬约束？
        int[] localDataDelay = dataDelay.get(localAgentId);
        ArrayList<Integer> ans = new ArrayList<Integer>();
        for(int i=0;i<localDataDelay.length;i++){
            if(localDataDelay[i] > maxDistance){
                ans.add(i);
            }
        }
        return ans;
    }



    public void Print(){
        System.out.println("——DataDelayView： Agent"+localAgentId+" ------------------------------");
        System.out.println("    LocalDelay: "+Arrays.toString(dataDelay.get(localAgentId)));
        for(Map.Entry<Integer,int[]> set : dataDelay.entrySet()){
            Integer id = set.getKey();
            int[] neighbourDataDelay = set.getValue();
            if(id != localAgentId){
                System.out.println("    "+"Agent"+id+": "+Arrays.toString(neighbourDataDelay));
            }
        }
        System.out.println("----------------------------------------------------------");
    }


}
