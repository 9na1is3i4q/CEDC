package algorithm.local.local_message;

import lombok.Data;

import java.util.ArrayList;

/**
 * @ClassName: SyncDataMessage
 * @Description:
 * @author: hjh
 * @date: 2023/3/22 10:43
 */
@Data
public class SyncDataMessage {
    private  ArrayList<Integer> syncDataIndex;
    int sendId;
    int valueIndex;
    int syncValueIndex; //推荐当前节点更新的赋值 //建议邻居更改的第几个数据的决策的索引
    int[] sendDataDelay;  //sendId 对于Data 的Delay








    public SyncDataMessage(int sendId, int valueIndex, int[] sendDataDelay, ArrayList<Integer> syncDataIndex) {
        this.sendId = sendId;
        this.valueIndex = valueIndex;
        this.sendDataDelay = sendDataDelay;
        this.syncDataIndex = syncDataIndex;
    }












}
