package distributed.core;

import java.util.LinkedList;
import java.util.Queue;


public abstract class SyncAgent extends Agent {

    private Queue<Message> messageQueue;
    protected SyncMailer mailer;
    private int pendingValueIndex;

    protected int  broadcastTime = 0 ;




    public SyncAgent(EDCAgentDTO agentDTO, SyncMailer mailer) {
        super(agentDTO);
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

    @Override
    public void execution() {
//        if (mailer.getPhase() == SyncMailer.PHASE_AGENT)
//        {
//            System.out.println(id + " execution. " + mailer.isDone(id));
//        }
        if (mailer.getPhase() == SyncMailer.PHASE_AGENT && !mailer.isDone(this.id)){
//            System.out.println(id + " execution. " + mailer.isDone(id));
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
    }

}
