package distributed.core;

import distributed.result.Result;

public interface FinishedListener {
    void onFinished(Result result);
}
