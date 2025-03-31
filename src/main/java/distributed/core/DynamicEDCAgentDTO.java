package distributed.core;

import lombok.Data;

import java.util.Map;
import java.util.Set;


@Data
public class DynamicEDCAgentDTO {
    public DynamicEDCAgentDTO(int id, int[] data, int[] divices, Set<Integer> relatedUsers, int[] neighbours, Map<Integer, int[]> neighboursUnderLimit, Map<Integer, Map<Integer, int[]>> userRequireData, Map<Integer, int[]> userHosts, Map<Integer, Integer> neighboursAdject, Map<Integer, int[]> neighbourHostUsers, int serverDataLimit, int maxDistance) {
        this.id = id;
        this.data = data;
        this.divices = divices;
        this.relatedUsers = relatedUsers;
        this.neighbours = neighbours;
        this.neighboursUnderLimit = neighboursUnderLimit;

        this.userRequireData = userRequireData;
        this.userHosts = userHosts;
        this.neighboursAdject = neighboursAdject;
        this.neighboursHostUsers = neighbourHostUsers;
        this.serverDataLimit = serverDataLimit;
        this.maxDistance = maxDistance;
    }
    private  int id;
    private int[] data;
    private  int[] divices;
    private int[] neighbours;



    private  Map<Integer,int[]> neighboursUnderLimit;
    private  Map<Integer, Map<Integer, int[]>> userRequireData;
    private  Map<Integer, int[]> userHosts;
    private  Map<Integer,Integer> neighboursAdject;
    private  Map<Integer, int[]>  neighboursHostUsers;
    private Set<Integer> relatedUsers;
    private int serverDataLimit;
    private int maxDistance;

}
