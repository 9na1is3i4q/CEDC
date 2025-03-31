package problem_generator;

import java.util.*;

public class GraphGenerator {
    private static final int DISTANCE_CHANGE = 10000;

    private int seversNum;
    private double density;
    private Map<Integer, Set<Integer>> adjacentTable;

    protected Random random = new Random();;
    protected List<Integer> source;
    protected List<Integer> dest;
    protected int nbConstraint;
    protected int nbRelation;
    private List<Server> agentList ;

    public GraphGenerator(List<Server> agentList, double density) {

        this.density = density;
        this.seversNum = agentList.size();
        adjacentTable = new HashMap<>();
        this.source = new ArrayList<Integer>();
        this.dest = new ArrayList<Integer>();
        this.agentList = new ArrayList<Server>(agentList);
    }

    private void generateDistanceList(){
        for (int i = 0; i < seversNum; i++){
            int startPoint=i+1;
            Server start = agentList.get(i);
            Map<Integer,Double> distanceMap = new HashMap<Integer,Double>();
            for(int j=1;j<seversNum+1;j++){
                if(j != startPoint){
                    Server end = agentList.get(j-1);
                    double distance = Math.sqrt(Math.pow(start.getLatitude()-end.getLatitude(),2)+Math.pow(start.getLongtitude()-end.getLongtitude(),2))*DISTANCE_CHANGE;
                    distanceMap.put(j,distance);
                }
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
            start.setDistanceList(distanceList);
        }
    }

    private void generateInitConnectedGraph(){
        for (int i = 0; i < seversNum - 1; i++){
            int endPoint;
            int startPoint;
            while (true) {
                startPoint = random.nextInt(seversNum) + 1;
                Server start = agentList.get(startPoint-1);
                List<Map.Entry<Integer,Double>> distanceList = new ArrayList<>(start.getDistanceList());  //首先随机选择一个起始服务器 startPoint，然后获取该服务器的距离列表


                int index = 0;
                endPoint = distanceList.get(index).getKey();
                Set<Integer> visited = new HashSet<Integer>();
                while (!isValidate(endPoint,startPoint,visited)){  //不断尝试寻找可行的 endPoint，直到找到一个可以连接的节点为止
                    index++;
                    if(index>distanceList.size()){
                        break;
                    }
                    endPoint = distanceList.get(index).getKey();
                    visited = new HashSet<Integer>();
                }
                if(index <= distanceList.size()){
                    break;
                }
            }
            source.add(Integer.min(startPoint,endPoint));
            dest.add(Integer.max(startPoint,endPoint));
            nbConstraint++;
            nbRelation++;
            Set<Integer> adjacent = adjacentTable.get(startPoint);
            if (adjacent == null){
                adjacent = new HashSet<>();
                adjacentTable.put(startPoint,adjacent);
            }
            adjacent.add(endPoint);
            adjacent = adjacentTable.get(endPoint);
            if (adjacent == null){
                adjacent = new HashSet<>();
                adjacentTable.put(endPoint,adjacent);
            }
            adjacent.add(startPoint);
        }
    }

    private boolean isValidate(int nextPoint, int target, Set<Integer> visited){
        if (nextPoint == target)
            return false;
        visited.add(nextPoint);
        Set<Integer> adjacent = adjacentTable.get(nextPoint);
        if (adjacent == null)
            return true;
        for (int adj : adjacent){
            if (visited.contains(adj))
                continue;
            if (!isValidate(adj,target,visited)){
                return false;
            }
        }
        return true;
    }

    public void generateConstraint() {
        generateDistanceList();
        generateInitConnectedGraph();
        int maxEdges = (int) (seversNum * (seversNum - 1) * density / 2);
        for (int i = seversNum - 1; i < maxEdges; i++){
            int startPoint = random.nextInt(seversNum) + 1;
            int count =0;
            while (random.nextBoolean()&&count<seversNum-2){
                count++;
            }
            List<Map.Entry<Integer,Double>> distanceList = agentList.get(startPoint-1).getDistanceList();

            int endPoint = distanceList.get(count).getKey();
            while (startPoint == endPoint || adjacentTable.get(startPoint).contains(endPoint)){
                startPoint = random.nextInt(seversNum) + 1;
                count =0;
                while (random.nextBoolean()&&count<seversNum-2){
                    count++;
                }
                distanceList = agentList.get(startPoint-1).getDistanceList();
                endPoint = distanceList.get(count).getKey();
            }
            adjacentTable.get(startPoint).add(endPoint);
            adjacentTable.get(endPoint).add(startPoint);
            source.add(Integer.min(startPoint,endPoint));
            dest.add(Integer.max(startPoint,endPoint));
            nbConstraint++;
            nbRelation++;
        }

        for(int j=0;j<seversNum;j++){
            int ServerPoint = j+1;
            Server agent = agentList.get(j);
            agent.setAdjacent(adjacentTable.get(ServerPoint));
        }

    }

}
