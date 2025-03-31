package algorithm.local.local_message;

import lombok.Data;

import java.util.Arrays;
import java.util.Objects;

@Data
public class BroadcastMessage {
    int sendId;
    int valueIndex;
    int[] sendDataDelay;
    int acceptTag;




    public BroadcastMessage(int sendId, int valueIndex, int[] sendDataDelay) {
        this.sendId = sendId;
        this.valueIndex = valueIndex;
        this.sendDataDelay = sendDataDelay;
        this.acceptTag = 0;
    }


    public BroadcastMessage(int sendId, int valueIndex, int[] sendDataDelay,int acceptTag ) {
        this.sendId = sendId;
        this.valueIndex = valueIndex;
        this.sendDataDelay = sendDataDelay;
        this.acceptTag = acceptTag;
    }



    public BroadcastMessage(){

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BroadcastMessage)) return false;
        BroadcastMessage that = (BroadcastMessage) o;
        return sendId == that.sendId &&
                valueIndex == that.valueIndex &&
                acceptTag == that.acceptTag &&
                Arrays.equals(sendDataDelay, that.sendDataDelay);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(sendId, valueIndex, acceptTag);
        result = 31 * result + Arrays.hashCode(sendDataDelay);
        return result;
    }
}
