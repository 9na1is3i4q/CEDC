package distributed.core;

import java.util.*;


public abstract class DynamicAgent extends Process{
    protected final static int MAX_VALUE = Integer.MAX_VALUE;


    protected int id;
    protected int[] data;


    protected int[] dataOld;
    protected int timeStep;

    protected int[] users;
    protected int domainLength;
    protected int valueIndex;
    protected int serverDataLimit;
    protected int maxDistance;

    protected List<Integer> feasibleValueIndex;

    protected int[] neighbours;
    protected int[] allneighbours;
    protected Map<Integer, int[]> neighboursUnderLimit;
    protected Map<Integer, Map<Integer, int[]>> userRequireData;
    protected Map<Integer,int[]> userHosts;
    protected Map<Integer, int[]> neighboursHostUsers;
    protected DataDelayView dataDelayView;

    protected int[] dataDelayOld;



    private Set<Integer> relatedUsers;

    protected Map<Integer,int[]> minDelayByUser;

    protected Map<Integer,int[]> minDelayByUserCheck;

    private Map<Integer,NeighbourView> localView;


    public DynamicAgent(DynamicEDCAgentDTO agentDTO) {
        super("Agent " + agentDTO.getId());
        this.id = agentDTO.getId();
        this.data = agentDTO.getData();


        this.dataOld = new int[data.length];
        Arrays.fill(dataOld,0);

        timeStep=0;


        dataDelayOld = new int[data.length];


        this.domainLength = (int) Math.pow(2,data.length);
        this.users = agentDTO.getDivices();
        this.relatedUsers = agentDTO.getRelatedUsers();
        this.neighbours = agentDTO.getNeighbours();
        this.neighboursUnderLimit = agentDTO.getNeighboursUnderLimit();
        setAllNeighbours(neighboursUnderLimit);
        this.userRequireData = agentDTO.getUserRequireData();
        this.userHosts = agentDTO.getUserHosts();
        this.serverDataLimit = agentDTO.getServerDataLimit();
        this.neighboursHostUsers = agentDTO.getNeighboursHostUsers();
        this.maxDistance = agentDTO.getMaxDistance();
        this.feasibleValueIndex = new ArrayList<Integer>();
        setFeasibleValueIndex(serverDataLimit);
        this.localView = new HashMap<>();
        Map<Integer, Integer> neighboursAdject = agentDTO.getNeighboursAdject();
        for (int neighbourId : neighbours){
            localView.put(neighbourId,new NeighbourView(neighbourId, neighboursAdject.get(neighbourId)));
        }
        this.dataDelayView = new DataDelayView(id,data.length ,allneighbours,neighboursUnderLimit);
        InitalMinDelayByUser();




    }

    private void setAllNeighbours(Map<Integer,int[]> neighboursUnderLimit) {
        Set<Integer> objects = new HashSet<Integer>();
        for(int[] arr : neighboursUnderLimit.values()){
            for(int id : arr){
                objects.add(id);
            }
        }
        int[] arr = new int[objects.size()];
        int i=0;
        for(int id : objects){
            arr[i] = id;
            i++;
        }
        this.allneighbours = arr;
    }


    public void InitalMinDelayByUser() {
        //初始化延迟记录
        this.minDelayByUser = new HashMap<>();
        this.minDelayByUserCheck = new HashMap<>();
        for(Integer user : relatedUsers){
            int[] minDelay = new int[data.length];
            int[] minDelayCheck = new int[data.length];
            Arrays.fill(minDelay,maxDistance);
            Arrays.fill(minDelayCheck,maxDistance);
            minDelayByUser.put(user,minDelay);
            minDelayByUserCheck.put(user,minDelayCheck);
        }
    }



    protected abstract double getLocalCost();
    protected abstract double getBenefit();
    protected abstract double getDataSum();

    private void setFeasibleValueIndex(int serverDataLimit) {
        for(int index=0; index<domainLength;index++){
            if(IsFeasible(index)){
                feasibleValueIndex.add(index);
            };
        }
    }


    private boolean IsFeasible(int index) {
        int count = 0;
        for(int i = data.length;i >= 0; i--){
            if(count > serverDataLimit){
                return false;
            }
            int tmp = index >>> i & 1;
            if(tmp == 1){
                count++;
            }
        }
        return count <= serverDataLimit;
    }

    public void setValueIndexAndData(int valueIndex){
        this.valueIndex = valueIndex;
        this.data = transFormValueIndexToData(valueIndex);
    }


    public int getValueIndex(){
        valueIndex = transFormDataToValueIndex(data);
        return valueIndex;
    }


    public static int[] transFormValueIndexToData(int index,int dataLength){
        int[] resultdata =new int[dataLength];
        for(int j = resultdata.length-1 ;j>= 0 ;j--){
            int tmp = (int) Math.pow(2,j);
            resultdata[j] = index/tmp;
            index = index%tmp;
        }
        return  resultdata;
    }



    public  int[] transFormValueIndexToData(int index){
        return  transFormValueIndexToData(index,data.length);
    }

    public static int transFormDataToValueIndex(int[] inputData){
        int value = 0;
        for(int i=0;i<inputData.length;i++){
            value += (int) Math.pow(2,i)*inputData[i];
        }
        return value;
    }






    @Override
    public void preExecution() {
        initRun();
        postInit();
    }


    protected abstract void initRun();

    protected void postInit(){
    }

    public abstract void runFinished();

    @Override
    public void postExecution() {
        runFinished();
    }

    public abstract void sendMessage(Message message);


    public abstract void disposeMessage(Message message);



    protected  void  Stop(){
        try {
            Thread.sleep( id*500 );
        } catch (Exception e){
            System.exit( 0 );
        }
    }

    protected void Print(String s) {

            System.out.println("Agent"+id+": "+ s);
    }

}
