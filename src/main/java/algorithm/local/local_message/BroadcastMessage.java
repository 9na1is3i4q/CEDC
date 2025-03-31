package algorithm.local.local_message;

import lombok.Data;

import java.util.Arrays;
import java.util.Objects;

/**
 * @ClassName: BroadcastMessage
 * @Description: 广播消息模板
 * @author: hjh
 * @date: 2023/2/10 15:07
 */
@Data
public class BroadcastMessage {
    int sendId;
    int valueIndex;
    int[] sendDataDelay;  //sendId 对于Data 的Delay
    int acceptTag;        //表示当前Agent是否满足硬约束 所有DataDelay<maxDistance




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
