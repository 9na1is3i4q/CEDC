package algorithm.local.local_message;

import lombok.Data;

@Data
public class CheckMessage {
    int sendId;
    boolean isSatisfy;

    public CheckMessage(int sendId, boolean isSatisfy){
        this.sendId = sendId;
        this.isSatisfy = isSatisfy;
    }
}
