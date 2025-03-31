package distributed.core;

import distributed.result.Result;
import distributed.result.ResultCycle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface DynamicFinishedListener {
    void onFinished(Map<Integer, ResultCycle> resultMapByTime);
}
