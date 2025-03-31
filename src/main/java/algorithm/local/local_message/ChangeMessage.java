package algorithm.local.local_message;

import lombok.Data;

@Data
public class ChangeMessage {
    int sendId;
    boolean isAbleTochangeValue;

    public ChangeMessage(int sendId, boolean isAbleTochangeValue){
        this.sendId = sendId;
        this.isAbleTochangeValue = isAbleTochangeValue;
    }
}
