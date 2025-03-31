package distributed.core;

import lombok.Data;

import java.util.*;

@Data
public class Problem {
    public int[] allAgentId;
    public int[] allUserId;
    public int[] allDataId;
    public Map<Integer,int[]> hostUsers;
    public Map<Integer,int[]> neighbours;

    public Map<Integer,Map<Integer,int[]>> neighboursUnderLimit;



    public Map<Integer,int[][]> userRequireData;
    public Map<Integer, int[]> userHosts;

    private Map<Integer, List<Integer>> userHostList;


    public int timeSlots;
    public int maxDistance;
    public int serverDataLimit;

    public Problem() {
        this.hostUsers = new HashMap<>();
        this.neighbours = new HashMap<>();
        this.userRequireData = new HashMap<>();
        this.userHostList = new HashMap<>();
        this.userHosts = new HashMap<>();
        this.neighboursUnderLimit = new HashMap<>();



    }

    public void setUserHosts() {
        for(Map.Entry<Integer, List<Integer>> set : userHostList.entrySet()){
            Integer user = set.getKey();
            List<Integer> hostList = set.getValue();
            int hosts[] = new int[hostList.size()];
            for(int i=0;i<hostList.size();i++){
                hosts[i] = hostList.get(i);
            }
            userHosts.put(user,hosts);
        }
    }


    public Set<Integer> getRelatedUsers(int AgentId){
        Set<Integer> UserIdSet = new HashSet<>();
        Map<Integer, int[]> neighbourDisMap = neighboursUnderLimit.get(AgentId);
        Map<Integer,int[]> neighbourHostUsers = new HashMap<>();
        for(int[] neighboursId : neighbourDisMap.values()){
            for (int neighbour : neighboursId) {
                for(int userId : hostUsers.get(neighbour)){
                    UserIdSet.add(userId);
                }
            }
        }
        for(int userId : hostUsers.get(AgentId)){
            UserIdSet.add(userId);
        }
        return  UserIdSet;
    }


    public Map<Integer, Map<Integer, int[]>> getUserRequireDataByAgent(int AgentId){
        Map<Integer, Map<Integer, int[]>> userRequireDataByAgent = new HashMap<>();
        for(int time=0;time<timeSlots;time++){
            Set<Integer> UserIdSet = getRelatedUsers(AgentId);
            Map<Integer,int[]> userRequireDataByAgentAndTime = new HashMap<>();
            for (Integer user : UserIdSet) {
                int[][] requireByDivice = userRequireData.get(user);
                int[] require = Arrays.copyOf(requireByDivice[time], requireByDivice[time].length);
                userRequireDataByAgentAndTime.put(user,require);
            }
            userRequireDataByAgent.put(time,userRequireDataByAgentAndTime);
        }
        return userRequireDataByAgent;
    }





    public Map<Integer, int[]> getUserRequireDataByAgentAndTime(int AgentId, int time){
        Set<Integer> UserIdSet = getRelatedUsers(AgentId);
        Map<Integer,int[]> userRequireDataByAgentAndTime = new HashMap<>();
        for (Integer user : UserIdSet) {
            int[][] requireByDivice = userRequireData.get(user);
            int[] require = Arrays.copyOf(requireByDivice[time], requireByDivice[time].length);
            userRequireDataByAgentAndTime.put(user,require);
        }
        return userRequireDataByAgentAndTime;
    }


    public Map<Integer, int[]> getUserHostsByAgent(int AgentId){
        Set<Integer> UserIdSet = getRelatedUsers(AgentId);
        Map<Integer,int[]> userHostsByAgent= new HashMap<>();
        for (Integer user : UserIdSet) {
            userHostsByAgent.put(user,userHosts.get(user));
        }
        return userHostsByAgent;
    }



    public Map<Integer,int[]> getNeighbourHostUsers(int AgentId){
        Map<Integer, int[]> neighbourDisMap = neighboursUnderLimit.get(AgentId);
        Map<Integer,int[]> neighbourHostUsers = new HashMap<>();
        for(int[] neighboursId : neighbourDisMap.values()){
            for (int neighbour : neighboursId) {
                neighbourHostUsers.put(neighbour,hostUsers.get(neighbour));
            }
        }
        return neighbourHostUsers;
    }


    public Map<Integer,Integer> getNeighbourAdject(int AgentId){
        int[] neighboursId = neighbours.get(AgentId);
        Map<Integer,Integer> neighbourAdjectNum= new HashMap<>();
        for (int neighbour : neighboursId) {
            neighbourAdjectNum.put(neighbour,neighbours.get(neighbour).length);
        }
        return neighbourAdjectNum;
    }




    public void Print(){
        System.out.println("-------------------------Problem---------------------");
        System.out.println("Agents: "+ Arrays.toString(allAgentId));
        System.out.println("Users:  "+ Arrays.toString(allUserId));
        System.out.println("Datas:  "+ Arrays.toString(allDataId));
        System.out.println("neighbours:  ");
        for(Map.Entry<Integer,int[]> set : neighbours.entrySet()){
            System.out.println("    Agent"+set.getKey()+": "+Arrays.toString(set.getValue()));
        }
        System.out.println("neighboursUnderLimit:  ");
        for(Map.Entry<Integer,Map<Integer,int[]>> set : neighboursUnderLimit.entrySet()){
            System.out.print("    Agent"+set.getKey()+":{ ");
            Map<Integer, int[]> neighbourDisMap = set.getValue();
            for(Map.Entry<Integer,int[]> subset : neighbourDisMap.entrySet()){
                System.out.print("Agent"+subset.getKey()+": "+Arrays.toString(subset.getValue())+",");
            }
            System.out.println("}");


        }



        System.out.println("hostUsers:  ");
        for(Map.Entry<Integer,int[]> set : hostUsers.entrySet()){
            System.out.println("    Aegnt"+set.getKey()+": "+Arrays.toString(set.getValue()));
        }

//        System.out.println("userHosts:  ");
//        for(Map.Entry<Integer,int[]> set : userHosts.entrySet()){
//            System.out.println("    User"+set.getKey()+": "+Arrays.toString(set.getValue()));
//        }
//


        System.out.println("TimeSlot："+timeSlots);
        System.out.println("maxDistance："+maxDistance);
        System.out.println("serverDataLimit："+serverDataLimit);
//        System.out.println("userRequireData:  ");
//        for(Map.Entry<Integer,int[][]> set : userRequireData.entrySet()){
//            int[][] timerequires = set.getValue();
//            System.out.println("    User"+set.getKey()+": ");
//            for(int t=0;t<timerequires.length;t++){
//                System.out.print("        Time"+t+": Require:");
//                for(int d=1;d<=timerequires[0].length;d++){
//                    System.out.print(" D"+d+": "+timerequires[t][d-1]);
//                    if(d<timerequires[0].length){
//                        System.out.print(",");
//                    }
//                }
//                System.out.println();
//            }
//        }
        System.out.println("-----------------------------------------------------");
    }
}
