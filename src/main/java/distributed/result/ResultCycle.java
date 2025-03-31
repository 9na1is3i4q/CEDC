package distributed.result;


import distributed.core.Agent;
import distributed.result.annotations.AverageField;

import java.util.HashMap;
import java.util.Map;

public class ResultCycle extends Result {
    @AverageField
    public double[] costInCycle;
    @AverageField
    public double[] dataSumInCycle;
    @AverageField
    public double[] benefitInCycle;

    public Map<Integer, int[]> DataAns;
    protected Map<Integer,Map<Integer,int[]>> agentDisNeighbourMap;
    protected Map<Integer,int[]> agentUsers;
    protected Map<Integer,Map<Integer,int[]>> agentUserRequireData;
    public Map<Integer, Map<Integer, int[]>> getAgentDisNeighbourMap() {
        return agentDisNeighbourMap;
    }
    public Map<Integer, int[]> getAgentUsers() {
        return agentUsers;
    }
    public Map<Integer, Map<Integer, int[]>> getAgentUserRequireData() {
        return agentUserRequireData;
    }



    public ResultCycle() {
        costInCycle = new double[0];
        dataSumInCycle = new double[0];
        benefitInCycle = new double[0];
        DataAns = new HashMap<Integer, int[]>();
        agentDisNeighbourMap = new HashMap<>();
        agentUsers = new HashMap<>();
        agentUserRequireData = new HashMap<>();
    }

    public void setCostInCycle(double[] costInCycle, int time){
       this.costInCycle = new double[time];
       for (int i = 0; i < time; i++){
           this.costInCycle[i] = costInCycle[i];
       }
    }

    public void setDataAns(HashMap<Integer, int[]> DataAns){
        this.DataAns = new HashMap<>(DataAns);

    }

    public void setDataSumInCycle(double[] dataSumInCycle, int time){
        this.dataSumInCycle = new double[time];
        for (int i = 0; i < time; i++){
            this.dataSumInCycle[i] = dataSumInCycle[i];
        }
    }

    public void setBenefitInCycle(double[] benefitInCycle, int time) {
        this.benefitInCycle = new double[time];
        for (int i = 0; i < time; i++){
            this.benefitInCycle[i] = benefitInCycle[i];
        }
    }

    public  void setAgentDisNeighbourMap(int agentId,Map<Integer, int[]> disNeighbourMap){
        agentDisNeighbourMap.put(agentId,disNeighbourMap);
    }


    public void setAgentUsers(int id, int[] users) {
        agentUsers.put(id,users);

    }

    public void setAgentUserRequireData(int id, HashMap<Integer,int[]> userRequireData) {
        agentUserRequireData.put(id,userRequireData);
    }
}
