package algorithm.local.local_message;

import lombok.Data;

/**
 * @ClassName: AskMessage
 * @Description:
 * @author: hjh
 * @date: 2023/2/14 21:16
 */
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
