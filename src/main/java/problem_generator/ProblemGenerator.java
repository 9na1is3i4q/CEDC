package problem_generator;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;


@EqualsAndHashCode(callSuper = true)
@Data
public class ProblemGenerator extends EDCGraph{

    protected static Random rand = new Random();
    private double density;
    private double singleHostRate;
    private int[][] requestDiviceCount;
//    private int[][] agentDistanceList ;
    private int[][] diviceDistanceList ;


    private double requireRate;


    protected Random random = new Random();


    public ProblemGenerator(int agentNum, double density, int diviceNum, double singleHostRate, int dataNum, int timeSlots, int maxDistance, int serverDataLimit, double requireRate) {
        this.agentNum = agentNum;
        this.density = density;
        this.diviceNum = diviceNum;
        this.singleHostRate = singleHostRate;
        this.dataNum = dataNum;
        this.timeSlots =timeSlots;
        this.maxDistance = maxDistance;
        this.serverDataLimit = serverDataLimit;
        this.variableNum = agentNum*dataNum;
        this.agentDistanceList = new int[agentNum][agentNum];
        this.requestDiviceCount = new int[timeSlots][dataNum];

        this.diviceDistanceList = new int[diviceNum][agentNum];
        for(int [] tmp : diviceDistanceList){
            Arrays.fill(tmp,Integer.MAX_VALUE);
        }
        CreateGraph();
        SetData(dataNum,timeSlots,requireRate);
        SetAgentDistance();
        SetDiviceDistance();
    }

    private  void CreateGraph(){
        List<Server> serverList = Reader.GetServerList();
        List<User> userList = Reader.GetUserList(dataNum,timeSlots);
        int seversNum = serverList.size();
        agentList = new ArrayList<Server>();
        for(int i=0;i<agentNum;i++){
            Server s =  serverList.get(rand.nextInt(seversNum)) ;
            while (agentList.contains(s)){
                s =  serverList.get(rand.nextInt(seversNum));
            }
            s.setAgentID(i+1);
            agentList.add(s);
        }
        GraphGenerator  generator = new GraphGenerator(agentList,density);
        generator.generateConstraint();
        this.constraintNum = generator.nbConstraint;
        this.source = generator.source;
        this.dest = generator.dest;
        UserGenerator userGenerator = new UserGenerator(agentList,userList,diviceNum,singleHostRate);
        userGenerator.generateUser();
        diviceList = new ArrayList<>(userGenerator.getDiviceList());
    }

    private void SetData(int dataNum,int timeSlots,double requireRate){
        if(requireRate <0.0 || requireRate>1.0){
            for(int t=0;t<timeSlots;t++){
                for(int i=0;i<dataNum;i++){
                    int requestDiviceNum = (int) NormalDistribution((double) diviceNum / 2, (double) diviceNum / 4);

                    while (requestDiviceNum< 0 || requestDiviceNum > diviceNum){
                        requestDiviceNum = (int) NormalDistribution((double) diviceNum / 2, (double) diviceNum / 4);
                    }

                    requestDiviceCount[t][i] = requestDiviceNum;
                    int count=0;
                    while (count<requestDiviceNum){
                        int diviceIndex = random.nextInt(diviceList.size());
                        User divice = diviceList.get(diviceIndex);
                        int[] requestData = divice.getRequestData()[t];
                        if(requestData[i] == 0){
                            requestData[i] = 1;
                            count++;
                        }
                    }
                }
            }
        }
        else {
            for(int t=0;t<timeSlots;t++){
                for(int i=0;i<dataNum;i++){
                    int requestDiviceNum = (int) (diviceNum*requireRate);
                    requestDiviceCount[t][i] = requestDiviceNum;
                    int count=0;
                    ArrayList<User> tmpList = new ArrayList<>(diviceList);
                    while (count<requestDiviceNum){
                        int diviceIndex = random.nextInt(tmpList.size());
                        User divice = tmpList.get(diviceIndex);
                        tmpList.remove(diviceIndex);
                        int[] requestData = divice.getRequestData()[t];
                        if(requestData[i] == 0){
                            requestData[i] = 1;
                            count++;
                        }
                    }
                }
            }


        }
    }

    private void SetAgentDistance() {
        for(Server agent : agentList){
            Map<Integer,Integer> distanceMap = new HashMap<Integer,Integer>();
            int distance = 0;
            setDistanceMap(agent , distance ,distanceMap);
            for(int i=0;i<agentNum;i++){
                agentDistanceList[agent.getAgentID()-1][i] = distanceMap.get(i+1);
            }
        }
    }

    private void SetDiviceDistance(){
        for (int i = 0; i<diviceNum; i++){
            User divice = diviceList.get(i);
            for(Integer host : divice.getHostList()){
                int hostIndex = host-1;
                for(int j=0;j<agentNum;j++){
                    diviceDistanceList[i][j] = Math.min(agentDistanceList[hostIndex][j],diviceDistanceList[i][j]);
                }
            }

        }

    }


    private void setDistanceMap(Server agent, Integer distance, Map<Integer, Integer> distanceMap) {
        Queue<Integer> stack = new LinkedList<>() ;
        Queue<Integer> stackB = new LinkedList<>();
        stack.add(agent.getAgentID());
        stackB.add(0);
        while (stack.size()>0){
            Integer agentIndex = stack.poll();
            Integer dis = stackB.poll();

            for(Integer neighbourIndex : agentList.get(agentIndex-1).getAdjacent()){
                if(!distanceMap.containsKey(agentIndex)){
                    stack.add(neighbourIndex);
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


    public static double NormalDistribution(double u,double v){
        Random random = new Random();
        return v*random.nextGaussian()+u;
    }

    public void Print(){
        for(int i=0;i<1;i++){
            for(Server s:agentList ){
                System.out.println(s.toString());
            }

        }
    }



    @Override
    protected String getHostUsers(List<Integer> userList) {
        StringBuilder stringBuilder = new StringBuilder();
        for(Integer user : userList){
            stringBuilder.append(user).append("|");
        }

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();

    }


    @Override
    protected String getNeighboursList(int[] neighbourDis, int dis_count) {
        StringBuilder stringBuilder = new StringBuilder();
        for(int agentID=1;agentID<=agentNum;agentID++){
            if(dis_count == neighbourDis[agentID-1]){
                stringBuilder.append(agentID).append("|");
            }
        }
        if(stringBuilder.length()>1){
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }

    @Override
    protected String getRequire(int diviceIndex, int timeIndex) {
        User user = diviceList.get(diviceIndex);
        int[] requestData = user.getRequestData()[timeIndex];
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 1; i <= dataNum; i++){
            stringBuilder.append(i).append(":");
            stringBuilder.append(requestData[i - 1]).append("|");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return stringBuilder.toString();
    }


    public static void main(String[] args) {
        for(int i=0;i<1;i++){
            ProblemGenerator problemGenerator = new ProblemGenerator(10,0.26,500,0.6,4,1,2, 3, 1.0);
            for(Server s:problemGenerator.agentList ){
                System.out.println(s.toString());
            }
            int length = problemGenerator.agentDistanceList.length;

            for(int j =0 ;j<length;j++)
            {
                for(int k=0;k<length;k++){
                    System.out.print(problemGenerator.agentDistanceList[j][k]+" ");
                }
                System.out.println();
            }

        }


    }

}
