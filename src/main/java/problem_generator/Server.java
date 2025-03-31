package problem_generator;

import lombok.Data;

import java.util.*;

@Data
public class Server {
    private Integer AgentID;
    private Set<Integer> adjacent;
    private List<Integer> userList;
    private Double latitude;
    private Double longtitude;
    private List<Integer> hostedUserList;
    private Integer id;
    List<Map.Entry<Integer,Double>> distanceList = new ArrayList<>();


    public Server(int index, String latitude_str, String longtitude_str) {
        id = index;
        latitude = Double.valueOf(latitude_str);
        longtitude = Double.valueOf(longtitude_str);
        this.userList=new ArrayList<>();
        this.hostedUserList = new ArrayList<>();
    }

    public void setDistanceList(List<Map.Entry<Integer, Double>> distanceList) {
        this.distanceList = new ArrayList<>(distanceList);
    }

    public void setAdjacent(Set<Integer> adjacent) {
        this.adjacent = new HashSet<>(adjacent);
    }
}
