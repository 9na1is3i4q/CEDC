package problem_generator;

import lombok.Data;

import java.util.*;


@Data
public class UserGenerator {

    private static final int DISTANCE_CHANGE = 10000;
    private List<User> userList ;
    private int userNum;
    private int seversNum;
    private List<Server> agentList ;
    private int diviceNum;
    private List<User> DiviceList;
    protected Random random = new Random();
    private double singleHostRate;


    public UserGenerator(List<Server> agentList, List<User> userList, int diviceNum,double singleHostRate) {
        this.agentList = new ArrayList<Server>(agentList);
        this.userList = new ArrayList<User>(userList) ;
        this.userNum = userList.size();
        this.seversNum = agentList.size();
        this.diviceNum = diviceNum;
        this.DiviceList = new ArrayList<>();
        this.singleHostRate = singleHostRate;
    }

    private void generateDistanceList(){
        for (int i = 0; i < userNum; i++){
            User start = userList.get(i);
            Map<Integer,Double> distanceMap = new HashMap<Integer,Double>();
            for(int j=1;j<seversNum+1;j++){
                Server end = agentList.get(j-1);
                double distance = Math.sqrt(Math.pow(start.getLatitude()-end.getLatitude(),2)+Math.pow(start.getLongtitude()-end.getLongtitude(),2))*DISTANCE_CHANGE;
                distanceMap.put(j,distance);

            }
            List<Map.Entry<Integer,Double>> distanceList = new ArrayList<>(distanceMap.entrySet());
            distanceList.sort(new Comparator<Map.Entry<Integer, Double>>() {
                @Override
                public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                    if(o1.getValue()-o2.getValue()<0)
                        return -1;
                    else if(o1.getValue()-o2.getValue()== 0)
                        return 0;
                    else
                        return 1;
                }
            });
            start.setHost(distanceList.get(0).getKey());
            Server host = agentList.get(distanceList.get(0).getKey()-1);
            host.getHostedUserList().add(start.getId());
            start.setDistanceList(distanceList);
        }
    }

    private void generateDistanceListForAgent(){
        for (int i = 0; i < seversNum; i++){
            Server start = agentList.get(i);
            Map<Integer,Double> distanceMap = new HashMap<>();
            for(int j=1;j<userNum+1;j++){
                User end = userList.get(j-1);
                double distance = Math.sqrt(Math.pow(start.getLatitude()-end.getLatitude(),2)+Math.pow(start.getLongtitude()-end.getLongtitude(),2))*DISTANCE_CHANGE;
                distanceMap.put(j,distance);
            }
            List<Map.Entry<Integer,Double>> distanceList = new ArrayList<>(distanceMap.entrySet());
            distanceList.sort(new Comparator<Map.Entry<Integer, Double>>() {
                @Override
                public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                    if(o1.getValue()-o2.getValue()<0)
                        return -1;
                    else if(o1.getValue()-o2.getValue()== 0)
                        return 0;
                    else
                        return 1;
                }
            });
            List<Integer> hostedUserList = start.getHostedUserList();
            if(hostedUserList.size()<5){
                int count =0;
                while (hostedUserList.size()<5){
                    User user = userList.get(distanceList.get(count).getKey()-1);
                    if(hostedUserList.contains(user.getId())){
                        count++;
                        continue;
                    }
                    hostedUserList.add(user.getId());
                }
            }
        }

        for (int i = 0; i < userNum; i++){
            User start = userList.get(i);
            Map<Integer,Double> distanceMap = new HashMap<Integer,Double>();
            for(int j=1;j<seversNum+1;j++){
                Server end = agentList.get(j-1);
                double distance = Math.sqrt(Math.pow(start.getLatitude()-end.getLatitude(),2)+Math.pow(start.getLongtitude()-end.getLongtitude(),2))*DISTANCE_CHANGE;
                distanceMap.put(j,distance);
            }
            List<Map.Entry<Integer,Double>> distanceList = new ArrayList<>(distanceMap.entrySet());
            distanceList.sort(new Comparator<Map.Entry<Integer, Double>>() {
                @Override
                public int compare(Map.Entry<Integer, Double> o1, Map.Entry<Integer, Double> o2) {
                    if(o1.getValue()-o2.getValue()<0)
                        return -1;
                    else if(o1.getValue()-o2.getValue()== 0)
                        return 0;
                    else
                        return 1;
                }
            });
            start.setHost(distanceList.get(0).getKey());
            Server host = agentList.get(distanceList.get(0).getKey()-1);
//            host.getHostedUserList().add(start.getId());
            start.setDistanceList(distanceList);
        }
    }

    private void CreateDivice(){
        Set<Integer> visited = new HashSet<>();

        for(int i=0;i<diviceNum;i++){
            List<Integer> hostList = new ArrayList<>();
            User user;
            int dividenum = (int) (diviceNum*singleHostRate);
            int agentnum = agentList.size();
            int partnum = dividenum/agentnum;

            int count=0;
            int agent=0;
            while(true){
                Server hostAgent ;
                if(i<partnum*agentnum){
                    hostAgent = agentList.get(i/partnum);
                    if(i/partnum != agent){
                        agent = i/partnum;
                        count=0;
                    }
                    List<Integer> hostedUserList = hostAgent.getHostedUserList();

                    if(count<100){
                        if(hostedUserList.size()<=0){
                            count++;
                            continue;
                        }
                        int index = random.nextInt(hostedUserList.size());
                        int userPoint = hostedUserList.get(index);
                        user = userList.get(userPoint-1);

                        hostList.add(hostAgent.getAgentID());

                    }
                    else{
                        int userPoint = random.nextInt(userNum) + 1;
                        user = userList.get(userPoint-1);
                        List<Map.Entry<Integer,Double>> distanceList = user.getDistanceList() ;

                        hostList = new ArrayList<>();

                        hostList.add(hostAgent.getAgentID());

                    }
                    count++;
                }
                else {
                    int userPoint = random.nextInt(userNum) + 1;
                    user = userList.get(userPoint-1);
                    List<Map.Entry<Integer,Double>> distanceList = user.getDistanceList() ;
                    hostList = new ArrayList<>();
                    Integer host =  user.getHost();
                    hostAgent = agentList.get(host-1);
                    hostList.add(host);
                    int index=1;
                    while(index < distanceList.size()){
                        Integer server =   distanceList.get(index).getKey();
                        if(hostAgent.getAdjacent().contains(server) && hostList.size()<3){
                            hostList.add(server);
                        }else {
                            break;
                        }
                        index++;
                    }
                }

                if( !visited.contains(user.getId())){
                    break;
                };
            }

//            System.out.println("------------user:"+user.getId()+"  Host"+hostList.toString()+"---------------");
            visited.add(user.getId());
            user.setDiviceID(i+1);
            user.setHostList(hostList);

//            System.out.println(i+1+": "+user.getHostList().toString());
            DiviceList.add(user);
        }


        for(User user : DiviceList){
            List<Integer> hostlist = user.getHostList();
            for(Integer i : hostlist){
                Server agent = agentList.get(i-1);
                agent.getUserList().add(user.getDiviceID());
            }
        }


    }

    public void generateUser() {
        generateDistanceList();
        generateDistanceListForAgent();
        CreateDivice();
    }
}
