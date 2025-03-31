package algorithm.local.local_message;

import lombok.Data;


@Data
public class ValueMessage {
    int sendId;
    int valueIndex;
    int[] newDataDelay;

    public ValueMessage(int sendId, int valueIndex, int[] newDataDelay){
        this.sendId = sendId;
        this.valueIndex = valueIndex;
        this.newDataDelay = newDataDelay;

    }
}
