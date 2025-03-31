package algorithm.local.local_message;

import lombok.Data;

@Data
public class AskMessage {
    int sendId;
    int valueIndex;
    double gain;
    int[] newDataDelay;

    public AskMessage(int sendId, int valueIndex, double gain, int[] newDataDelay){
        this.sendId = sendId;
        this.valueIndex = valueIndex;
        this.gain = gain;
        this.newDataDelay = newDataDelay;

    }
}
