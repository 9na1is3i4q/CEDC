package distributed.core;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DynamicSyncAgent extends DynamicAgent {

    public static final int PHASE_RUNNING = 1;
    public static final int PHASE_WAITING = 2;
    private AtomicInteger phase;

    private Queue<Message> messageQueue;
    protected DynamicSyncMailer mailer;
    private int pendingValueIndex;

    protected int  broadcastTime = 0 ;


    public DynamicSyncAgent(DynamicEDCAgentDTO agentDTO, DynamicSyncMailer mailer) {
        super(agentDTO);
        phase = new AtomicInteger(PHASE_RUNNING);
        messageQueue = new LinkedList<>();
        this.mailer = mailer;
        mailer.register(this);
        pendingValueIndex = -1;
    }

    public void assignValueIndex(int pendingValueIndex){
        this.pendingValueIndex = pendingValueIndex;
    }


    public int getBroadcastTime() {
        return broadcastTime;
    }

    @Override
    protected void postInit() {
        super.postInit();
        mailer.agentDone(this.id);
    }

    public void addMessage(Message message){
        messageQueue.add(message);
    }

    @Override
    public void sendMessage(Message message) {
        mailer.addMessage(message);
    }


    public void allMessageDisposed(){
    }

    public synchronized int getPhase() {
        return phase.get();
    }



    protected void agentWaiting(){
        phase.set(PHASE_WAITING);
    }

    //开始下一阶段
    protected void agentAwake(){
        phase.set(PHASE_RUNNING);
    }


    @Override
    public void execution() {
        if(mailer.getPhase() == DynamicSyncMailer.PHASE_AGENT && !mailer.isDone(this.id)){
            if(phase.get() == PHASE_RUNNING){
                while (!messageQueue.isEmpty()){
                    disposeMessage(messageQueue.poll());
                }
                allMessageDisposed();
                mailer.agentDone(this.id);
                if (pendingValueIndex >= 0){
                    setValueIndexAndData(pendingValueIndex);
                    pendingValueIndex = -1;
                }
            }
            else {

            }
        }

    }

}
