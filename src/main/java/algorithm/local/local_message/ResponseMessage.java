package algorithm.local.local_message;

import lombok.Data;

import java.util.HashSet;


@Data
public class ResponseMessage {
    int sendId;
    boolean check;
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


