package distributed.core;


import distributed.result.Result;

public interface ProgressChangedListener {
    void onProgressChanged(double percentage, Result result);
    void interrupted(String reason);
}
