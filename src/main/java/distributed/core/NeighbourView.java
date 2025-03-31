package distributed.core;

import lombok.Data;


@Data
public class NeighbourView {
    int id;
    int adjectnum;
    int distance;

    int[] hostUsers;

    int[] constraintUsers;

    public NeighbourView(int id, int adjectnum){
        this.id = id;
        this.adjectnum = adjectnum;



    }
}
