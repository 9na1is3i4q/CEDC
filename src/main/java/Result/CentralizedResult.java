package Result;

import Test.DynamicAns;
import centralized_core.CentralizedSolver;
import lombok.Data;

import java.util.Arrays;

@Data
public class CentralizedResult {

    private int timeSlots;
    private int dataNum;
    private int agentNum;  //agent
    private int diviceNum;

    private int agentDataSet[][][];
    private double resultTargetOne[];
    private double resultTargetTwo[];


    private int[][] diviceDistanceList;
    private int[][][] timeUserRequireData;
    private int[][] agentDistanceList;

    private double CostCE;
    private double CostEE;
    private double BETA;
    private int maxDistance;

    public double[] costByTime;
    public double[] benefitByTime;
    public double[] revenueByTime;
    public long[] caculateTimeByTime;


    public CentralizedResult(int timeSlots, int dataNum, int agentNum, int diviceNum) {
        this.timeSlots = timeSlots;
        this.dataNum = dataNum;
        this.agentNum = agentNum;
        this.diviceNum = diviceNum;
        this.agentDataSet = new int[timeSlots][agentNum][dataNum];
        this.resultTargetOne = new double[timeSlots];
        this.resultTargetTwo = new double[timeSlots];

    }

    public CentralizedResult(CentralizedSolver centralizedSolver,double BETA,double costCE, double costEE) {
        this.timeSlots = centralizedSolver.getTimeSlots();
        this.dataNum = centralizedSolver.getDataNum();
        this.agentNum = centralizedSolver.getAgentNum();
        this.diviceNum = centralizedSolver.getDiviceNum();
        this.agentDataSet = new int[timeSlots][agentNum][dataNum];
        this.resultTargetOne = new double[timeSlots];
        this.resultTargetTwo = new double[timeSlots];


        this.diviceDistanceList = centralizedSolver.getDiviceDistanceList();
        this.timeUserRequireData = centralizedSolver.getTimeUserRequireData();
        this.agentDistanceList = centralizedSolver.getAgentDistanceList();
        this.maxDistance = centralizedSolver.getMaxDistance();

        this.BETA = BETA;
        this.CostCE = costCE;
        this.CostEE =costEE;


        this.costByTime = new double[timeSlots];
        this.benefitByTime = new double[timeSlots];
        this.revenueByTime = new double[timeSlots];
        this.caculateTimeByTime = new long[timeSlots];

    }

    public void setDataAns() {
        //计算Cost
        int[][] distanceOld = new int[agentNum][dataNum];
        double[][] cost = new double[agentNum][dataNum];
        int[][] distanceNew= new int[agentNum][dataNum];
        for(int j=0;j<agentNum;j++){
            Arrays.fill(distanceOld[j],Integer.MAX_VALUE);
        }
        for(int t=0;t<timeSlots;t++){
            costByTime[t]=0;
            benefitByTime[t]=0;
            for(int j=0;j<agentNum;j++){
                Arrays.fill(distanceNew[j],Integer.MAX_VALUE);
            }
            for(int i=0;i<agentNum;i++){
                for(int k=0;k<dataNum;k++){
                    if(t==0){
                        for(int j=0;j<agentNum;j++){
                            Arrays.fill(distanceOld[j],Integer.MAX_VALUE);
                        }
                    }else {
                        int[][] lambdaOld = agentDataSet[t - 1];
                        distanceOld=distanceNew;
                    }
                    SetCost(distanceOld, cost, i, k);
                    if(t==0){
                        costByTime[t]+=cost[i][k]*agentDataSet[t][i][k];
                    }else {
                        costByTime[t]+=cost[i][k]*(1-agentDataSet[t-1][i][k])*agentDataSet[t][i][k];
                    }
                }
            }

            for(int i=0;i<agentNum;i++){
                for(int k=0;k<dataNum;k++){
                    int[][] lambdaNew = agentDataSet[t];
                    SetDistance(distanceNew, i, k, lambdaNew);
                }
            }

            for(int q=0;q<diviceNum;q++){
                for(int k=0;k<dataNum;k++){
                    double benfit_q_k = 0;
                    double minDis_q_K = maxDistance;
                    for(int i=0;i<agentNum;i++){
                        if(distanceNew[i][k] < maxDistance){
                            int dis_q_i_k = diviceDistanceList[q][i] + distanceNew[i][k];
                            if(minDis_q_K> dis_q_i_k){
                                minDis_q_K= dis_q_i_k;
                            }
                        }
                    }
                    benfit_q_k = timeUserRequireData[t][q][k]*(maxDistance-minDis_q_K);
                    benefitByTime[t]+=benfit_q_k*BETA;
                }
            }
            revenueByTime[t] = benefitByTime[t] - costByTime[t];

        }

    }
    public void setAgentDataSet(int time,int agentIndex, int data, int set) {
        this.agentDataSet[time][agentIndex][data] = set;
    }
    public void setResultTargetOne(int time,double tar) {
        this.resultTargetOne[time] = tar;
    }
    public void setResultTargetTwo(int time,double tar) {
        this.resultTargetTwo[time] = tar;
    }




    public void Print(){
        System.out.println("-------------------------Problem---------------------");
        System.out.println("timeSlots: "+ timeSlots);
        System.out.println("Agents: "+ agentNum);
        System.out.println("Users:  "+ diviceNum);
        System.out.println("Datas:  "+ dataNum);

        int[][] distanceOld = new int[agentNum][dataNum];
        double[][] cost = new double[agentNum][dataNum];
        int[][] distanceNew= new int[agentNum][dataNum];

        for(int j=0;j<agentNum;j++){
            Arrays.fill(distanceOld[j],Integer.MAX_VALUE);
        }


        for(int t=0;t<timeSlots;t++){
            System.out.println("*************************Time"+t+"***********************");
            System.out.println("ResultTargetOne"+resultTargetOne[t]);
            System.out.println("ResultTargetTwo"+resultTargetTwo[t]);
            double costByTime=0;
            double benefitByTime=0;

            for(int j=0;j<agentNum;j++){
                Arrays.fill(distanceNew[j],Integer.MAX_VALUE);
            }

            for(int i=0;i<agentNum;i++){
                for(int k=0;k<dataNum;k++){

                    if(t==0){
                        for(int j=0;j<agentNum;j++){
                            Arrays.fill(distanceOld[j],Integer.MAX_VALUE);
                        }
                    }else {
                        int[][] lambdaOld = agentDataSet[t - 1];
                        distanceOld=distanceNew;
                    }

                    SetCost(distanceOld, cost, i, k);
                    if(t==0){
                        costByTime+=cost[i][k]*agentDataSet[t][i][k];
                    }else {
                        costByTime+=cost[i][k]*(1-agentDataSet[t-1][i][k])*agentDataSet[t][i][k];
                    }
                }
            }


            for(int i=0;i<agentNum;i++){
                for(int k=0;k<dataNum;k++){
                    int[][] lambdaNew = agentDataSet[t];
                    SetDistance(distanceNew, i, k, lambdaNew);
                }
            }

            for(int q=0;q<diviceNum;q++){
                for(int k=0;k<dataNum;k++){
                    double benfit_q_k = 0;
                    double minDis_q_K = maxDistance;
                    for(int i=0;i<agentNum;i++){
                        int dis_q_i_k = diviceDistanceList[q][i] + distanceNew[i][k];
                        if(minDis_q_K> dis_q_i_k){
                            minDis_q_K= dis_q_i_k;
                        }
                    }
                    benfit_q_k = timeUserRequireData[t][q][k]*(maxDistance-minDis_q_K);
                    benefitByTime+=benfit_q_k*BETA;
                }
            }

            System.out.println("CostByTime"+costByTime);
            System.out.println("benefitByTime"+benefitByTime);


            for(int agentIndex=0;agentIndex<agentNum;agentIndex++){
                int agent = agentIndex + 1;
                System.out.println("Agent"+agent+":"+Arrays.toString(agentDataSet[t][agentIndex]));
            }
        }

        System.out.println("-----------------------------------------------------");

    }

    private void SetDistance(int[][] distanceOld, int i, int k, int[][] lambdaOld) {
        if(lambdaOld[i][k]==1){
            distanceOld[i][k]=0;
        }else {
            for(int j=0;j<agentNum;j++){
                if(lambdaOld[j][k]==1){
                    if(distanceOld[i][k]>agentDistanceList[i][j]){
                        distanceOld[i][k]=agentDistanceList[i][j];
                    }
                }
            }
        }
    }

    private void SetCost(int[][] distanceOld, double[][] cost, int i, int k) {
        if (distanceOld[i][k] == Integer.MAX_VALUE || distanceOld[i][k] > maxDistance) {
            cost[i][k] = CostCE;
        } else {
            cost[i][k] = CostEE * distanceOld[i][k];
        }
    }


    public void setCaculateTime(int time, long passTime) {
        caculateTimeByTime[time] = passTime;
    }
}
