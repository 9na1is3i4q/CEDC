package algorithm.local.local_message;

import lombok.Data;

@Data
public class GainMessage {
    int sendId;
    int valueIndex;
    double gain;

    public GainMessage(int sendId, int valueIndex, double gain){
        this.sendId = sendId;
        this.valueIndex = valueIndex;
        this.gain = gain;

    }
}
