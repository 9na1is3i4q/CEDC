package algorithm.local.local_message;

import lombok.Data;

import java.util.ArrayList;

@Data
public class SyncDataMessage {
    private  ArrayList<Integer> syncDataIndex;
    int sendId;
    int valueIndex;
    int syncValueIndex;
    int[] sendDataDelay;








    public SyncDataMessage(int sendId, int valueIndex, int[] sendDataDelay, ArrayList<Integer> syncDataIndex) {
        this.sendId = sendId;
        this.valueIndex = valueIndex;
        this.sendDataDelay = sendDataDelay;
        this.syncDataIndex = syncDataIndex;
    }












}
