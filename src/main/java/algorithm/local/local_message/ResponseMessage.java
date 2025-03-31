package algorithm.local.local_message;

import lombok.Data;

import java.util.HashSet;

/**
 * @ClassName: ResponseMessage
 * @Description: ResponseMessage
 * @author: hjh
 * @date: 2023/2/15 19:48
 */

@Data
public class ResponseMessage {
    int sendId;
    boolean check;  //表示代理节点是否接受了请求
    int acceptAgentID;


    double acceptGain;


    public ResponseMessage(int sendId, boolean check) {
        this.sendId = sendId;
        this.check = check;
    }

    public ResponseMessage(int sendId, boolean check, int acceptAgentID) {
        this.sendId = sendId;
        this.check = check;
        this.acceptAgentID = acceptAgentID;
    }

    public ResponseMessage(int sendId, boolean check, int acceptAgentID, double acceptGain) {
        this.sendId = sendId;
        this.check = check;
        this.acceptAgentID = acceptAgentID;
        this.acceptGain = acceptGain;
    }


}


