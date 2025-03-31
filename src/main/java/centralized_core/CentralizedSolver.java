package centralized_core;

import distributed.core.Problem;
import lombok.Data;
import problem_parser.ProblemParser;

import java.util.*;


@Data
public class CentralizedSolver {
    private int agentNum;
    private int diviceNum;
    private int maxDistance;
    private int dataNum;
    private int serverDataLimit;

    private int[][] agentDistanceList;      //agentA-agentB | distance
    private int[][] diviceDistanceList;     //divice-agent | distance
    private Map<Integer, int[]> hostUsers;
    private Map<Integer, int[]> neighbours;
    private  int timeSlots;

    private  int[][][] timeUserRequireData; // time-divice-data | require

    private Problem problem;



    public CentralizedSolver(String problemPath){
        ProblemParser parser = new ProblemParser(problemPath);
        problem = parser.parse();
        this.agentNum = problem.allAgentId.length;
        this.diviceNum = problem.allUserId.length;
        this.dataNum = problem.allDataId.length;
        this.maxDistance = problem.maxDistance;
        this.serverDataLimit = problem.serverDataLimit;
        this.agentDistanceList = new int[agentNum][agentNum];
        this.diviceDistanceList = new int[diviceNum][agentNum];

        this.hostUsers = problem.hostUsers;
        this.neighbours = problem.neighbours;


        this.timeSlots = problem.timeSlots;
        this.timeUserRequireData = new int[timeSlots][diviceNum][dataNum];


        for(int [] tmp : diviceDistanceList){
            Arrays.fill(tmp,Integer.MAX_VALUE);
        }
        SetAgentDistance();
        SetDiviceDistance();
        setTimeUserRequireData();
    }


    public void setTimeUserRequireData() {
        Map<Integer, int[][]> userRequireData = problem.getUserRequireData();
        for(int time=0;time<problem.timeSlots;time++){
            for(int diviceIndex = 0;diviceIndex<diviceNum;diviceIndex++){
                int diviceID = diviceIndex+1;
                for(int data=0;data<dataNum;data++){
                    timeUserRequireData[time][diviceIndex][data] = userRequireData.get(diviceID)[time][data];
                }
            }
        }
    }

    private void SetAgentDistance() {
        Map<Integer, int[]> adject = problem.neighbours;
        for (Map.Entry<Integer, int[]> set : adject.entrySet()){
            Integer agentId = set.getKey();
            Map<Integer,Integer> distanceMap = new HashMap<Integer,Integer>();
            int distance = 0;
            setDistanceMap(agentId , distance ,  distanceMap,adject);
            for(int i=0;i<agentNum;i++){
                agentDistanceList[agentId-1][i] = distanceMap.get(i+1);
            }
        }
    }


    private void setDistanceMap(int agentId, Integer distance, Map<Integer, Integer> distanceMap, Map<Integer, int[]> adject) {
        Queue<Integer> stack = new LinkedList<>() ;
        Queue<Integer> stackB = new LinkedList<>();
        stack.add(agentId);
        stackB.add(0);
        while (stack.size()>0){
            Integer agentIndex = stack.poll();
            Integer dis = stackB.poll();

            for(Integer neighbourId: adject.get(agentIndex)){
                if(!distanceMap.containsKey(agentIndex)){
                    stack.add(neighbourId);
                    stackB.add(dis+1);
                }
            }
            if(!distanceMap.containsKey(agentIndex)){
                distanceMap.put(agentIndex,dis);
            }

            if(distanceMap.size() >= agentNum){
                break;
            }
        }
    }


    private void SetDiviceDistance(){
        for (int diviceIndex = 0; diviceIndex < diviceNum; diviceIndex++){
            for(Integer host : problem.userHosts.get(diviceIndex+1)){
                int hostIndex = host-1;
                for(int j=0;j<agentNum;j++){
                    diviceDistanceList[diviceIndex][j] = Math.min(agentDistanceList[hostIndex][j],diviceDistanceList[diviceIndex][j]);
                }
            }
        }
    }

    public void Print(){
        problem.Print();


        System.out.println("agentDistanceList:  ");
        for(int i=0;i<agentDistanceList.length;i++){
            int tmp = i+1;
            System.out.println("    Aegnt"+tmp+": "+ Arrays.toString(agentDistanceList[i]));
        }

        System.out.println("diviceDistanceList:  ");
        for(int i=0;i<diviceDistanceList.length;i++){
            int tmp = i+1;
            System.out.println("    Divice"+tmp+": "+ Arrays.toString(diviceDistanceList[i]));
        }

        System.out.println("timeUserRequireData:   ");
        for(int time=0;time<timeUserRequireData.length;time++){
            System.out.println("    Time"+time+":");
            for(int divice=0;divice<timeUserRequireData[0].length;divice++){
                int tmp = divice+1;
                System.out.println("    Divice"+tmp+": "+ Arrays.toString(timeUserRequireData[time][divice]));
            }

        }

    }
}
