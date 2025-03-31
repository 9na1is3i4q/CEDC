package problem_generator;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class User {
    private Integer diviceID;
    private Integer host;
    private int[][]requestData;
    private List<Integer> hostList;
    private Double latitude;
    private Double longtitude;
    private List<Map.Entry<Integer,Double>> distanceList;
    private Integer id;

    public User(int index, String latitude_str, String longtitude_str,int dataNum,int timeSlot) {
        id = index;
        latitude = Double.valueOf(latitude_str);
        longtitude = Double.valueOf(longtitude_str);
        this.requestData = new int[timeSlot][dataNum];

    }

    public void setHostList(List<Integer> hostList) {
        this.hostList = new ArrayList<>(hostList);
    }

    public void setDistanceList(List<Map.Entry<Integer, Double>> distanceList) {
        this.distanceList = new ArrayList<>(distanceList);
    }
}
