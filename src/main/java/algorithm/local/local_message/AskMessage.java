package algorithm.local.local_message;

import lombok.Data;

/**
 * @ClassName: AskMessage
 * @Description:
 * @author: hjh
 * @date: 2023/2/14 21:16
 */
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
