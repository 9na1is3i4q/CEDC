package algorithm.local.local_message;

import lombok.Data;

@Data
public class NewvalueMessage {
    int sendId;
    int valueIndex;

    public NewvalueMessage(int sendId, int valueIndex){
        this.sendId = sendId;
        this.valueIndex = valueIndex;
    }
}
