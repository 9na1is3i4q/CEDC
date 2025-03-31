package Test.dynamic;

import Result.CentralizedResult;
import Test.DynamicAns;
import centralized_core.CentralizedSolver;
import distributed.core.DynamicFinishedListener;
import distributed.core.DynamicSolver;
import distributed.result.ResultCycle;

import java.io.File;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class DynamicMainMGM_CEDC1 {

    static double Gama = 0.004;
    static double Beta = 0.004;
    static double CostCE = 0.016 ;
    static  double CostEE = 0.006;

    public static void main(String[] args) throws InterruptedException {

//        String problemDir = "problem\\edc_dcop\\10200\\Simple4";
//        String outDir = "problem\\ans\\10200\\Simple4";

//        String problemDir = "problem\\edc_dcop\\10200\\diviceNum200";
//        String outDir = "problem\\ans\\myans\\10200";

//        String problemDir = "problem\\edc_dcop\\30200\\dataNum4";
//        String outDir = "problem\\ans\\myans\\30200";

//        String problemDir = "problem\\edc_dcop\\90200\\Simple4";
//        String outDir = "problem\\ans\\90200\\10200";

//        String problemDir = "problem\\edc_dcop\\10200\\density3";
//        String outDir = "problem\\ans\\myans\\10200\\density3";

//        String problemDir = "problem\\edc_dcop\\10200\\dataNum6";
//        String outDir = "problem\\ans\\myans\\10200\\dataNum6";

//        String problemDir = "problem\\edc_dcop\\10200\\serverDataLimit4";
//        String outDir = "problem\\ans\\myans\\10200\\serverDataLimit4";

        String problemDir = "problem\\edc_dcop\\90200\\dataNum4";
        String outDir = "problem\\ans\\myans\\90200";

//        String problemDir = "problem\\edc_dcop\\80200\\Simple4";
//        String outDir = "problem\\ans\\80200\\Simple4";

        File dir = new File(problemDir);
        List<File> acceptedProblemFiles = new LinkedList<>();
        for (File file : Objects.requireNonNull(dir.listFiles())){
            if (file.isDirectory()){
                continue;
            }
            if (file.getName().endsWith(".xml")){
                acceptedProblemFiles.add(file);
            }
        }
        String[] problemPaths = new String[acceptedProblemFiles.size()];

        String[] algs =  new String[100];
        algs[1]= "MGM_CEDC";

        String algo = algs[1];
        int timeSlots =50;
        int size = acceptedProblemFiles.size();
        DynamicAns dynamicAns = new DynamicAns(size,timeSlots);
        long startTime = new Date().getTime();
        for (int i = 0; i < acceptedProblemFiles.size(); i++){
            problemPaths[i] = acceptedProblemFiles.get(i).getAbsolutePath();
            DynamicSolver solver = new DynamicSolver();
            boolean showGraphic = true;
            boolean showPseduoTree = false;
            int finalI = i;


            CentralizedSolver centralizedSolver = new CentralizedSolver(problemPaths[i]);
            CentralizedResult centralizedResult = new CentralizedResult(centralizedSolver, Beta, CostCE, CostEE);



            CountDownLatch countDownLatch  = new CountDownLatch(1);
            solver.solve("problem/am.xml", algo,  problemPaths[i],Gama,CostCE,CostEE, new DynamicFinishedListener() {

                StringBuilder problemAnswerPerTimeStep = new StringBuilder();
                @Override
                public void onFinished(Map<Integer, ResultCycle> resultMapByTime) {
                    int timeSlots = resultMapByTime.size();
                    for(int t=0;t<timeSlots;t++){
                        System.out.println("--------------------------------------------"+"TimeStep:"+ t+"--------------------------------------------");
                        ResultCycle resultCycle1 = (ResultCycle) resultMapByTime.get(t);
                        Map<Integer, Map<Integer, int[]>> agentDisNeighbourMap = resultCycle1.getAgentDisNeighbourMap();
                        Map<Integer, int[]> agentUsers = resultCycle1.getAgentUsers();
                        Map<Integer, Map<Integer, int[]>> agentUserRequireData = resultCycle1.getAgentUserRequireData();
                        long caculateTime = resultCycle1.getTotalTime();
                        System.out.println("求解时间："+ caculateTime +"ms");
                        double cost = GetCost(resultMapByTime, t, resultCycle1, agentDisNeighbourMap, CostCE, CostEE);
                        double benefit =  GetBenefit(resultCycle1, agentDisNeighbourMap, agentUsers, agentUserRequireData,Beta);
                        double revenue = benefit-cost;
                        System.out.println("缓存收益revenue："+revenue);
                        for(int agentId =1 ;agentId <= agentDisNeighbourMap.size();agentId++){
                            int[] data = resultCycle1.getAgentData(agentId);
                            System.out.println("Agent:"+ agentId+": "+ Arrays.toString(data));
                        }
//                        dynamicAns.setAllCost(finalI,t,cost);
//                        dynamicAns.setAllBenefit(finalI,t,benefit);
//                        dynamicAns.setAllRevenue(finalI,t,revenue);
//                        dynamicAns.setCaculateTime(finalI,t,caculateTime);

                        for (int agentId =0 ;agentId < agentDisNeighbourMap.size();agentId++) {
                            for(int k=0;k< resultCycle1.getAgentData(agentId+1).length;k++){
                                centralizedResult.setAgentDataSet(t,agentId,k,resultCycle1.getAgentData(agentId+1)[k]);
                            }
                        }
                        centralizedResult.setCaculateTime(t, caculateTime);
                    }
                    countDownLatch.countDown();
                }
            },showGraphic,showPseduoTree);
            countDownLatch.await();

            double percent = i / (double) size*100;
            System.out.println(percent+"%");


            centralizedResult.setDataAns();

            for(int t=0;t<timeSlots;t++){
                dynamicAns.setAllCost(i,t,centralizedResult.costByTime[t]);
                dynamicAns.setAllBenefit(i,t,centralizedResult.benefitByTime[t]);
                dynamicAns.setAllRevenue(i,t,centralizedResult.revenueByTime[t]);
                dynamicAns.setCaculateTime(i,t,centralizedResult.caculateTimeByTime[t]);
            }

        }

        System.out.println("完成全部求解");
        dynamicAns.Print();
        dynamicAns.WriteAnsTable1(outDir + "/"+algo+Gama+"/AnswerTableByProblem.csv");
        dynamicAns.WriteAnsTable2(outDir + "/"+algo+Gama+"/AnswerTableByTime.csv");

        long totaltime = new Date().getTime() - startTime;

        System.out.println("总时间："+totaltime);
    }




    private static double GetBenefit(ResultCycle resultCycle1, Map<Integer, Map<Integer, int[]>> agentDisNeighbourMap, Map<Integer, int[]> agentUsers, Map<Integer, Map<Integer, int[]>> agentUserRequireData, double beta) {
        HashMap<Integer, double[]> userBenefitRecord = new HashMap<Integer, double[]>();
        double benefit =0;
        for(int agentId =1 ;agentId <= agentDisNeighbourMap.size();agentId++){
            int[] localData = resultCycle1.getAgentData(agentId);
            Map<Integer, int[]> disNeighbourMap = agentDisNeighbourMap.get(agentId);
            int[] users = agentUsers.get(agentId);
            Map<Integer, int[]> userRequireData = agentUserRequireData.get(agentId);
            for(int dataIndex = 0; dataIndex<localData.length;dataIndex++){
                double tmpLantecy = resultCycle1.maxDistance;
                if(localData[dataIndex] == 1){
                    tmpLantecy = 0;
                }
                else {
                    int lantecy = resultCycle1.maxDistance;
                    for(int dis =1 ;dis <= disNeighbourMap.size() ; dis++ ){
                        int[] neighbourByDis = disNeighbourMap.get(dis);
                        for(int neighbourId : neighbourByDis){
                            int[] neighoburData =resultCycle1.getAgentData(neighbourId);
                            if(neighoburData[dataIndex] == 1){
                                lantecy = dis;
                                break;
                            }
                        }
                        if(lantecy < resultCycle1.maxDistance){
                            break;
                        }
                    }
                    tmpLantecy = lantecy;
                }
                for(int userId : users){
                    if(userBenefitRecord.containsKey(userId)){
                        double[] tmpBenefit = userBenefitRecord.get(userId);
                        if(userRequireData.get(userId)[dataIndex]==1){
                            double benefitcheck = resultCycle1.maxDistance - tmpLantecy;
                            double benefitold= tmpBenefit[dataIndex];
                            if(benefitcheck>benefitold){
                                benefit += benefitcheck-benefitold;
                                tmpBenefit[dataIndex] = benefitcheck;
                            }

                        }
                    }else {
                        double[] tmpBenefit = new double[localData.length];
                        Arrays.fill(tmpBenefit,0);
                        if(userRequireData.get(userId)[dataIndex]==1){
                            benefit += resultCycle1.maxDistance-tmpLantecy;
                            tmpBenefit[dataIndex] = resultCycle1.maxDistance-tmpLantecy;
                        }
                        userBenefitRecord.put(userId,tmpBenefit);
                    }
                }
            }
        }
        System.out.println("缓存效益Benefit："+benefit*Beta);
        return benefit*Beta;
    }
    private static double GetCost(Map<Integer, ResultCycle> resultMapByTime, int t, ResultCycle resultCycle1, Map<Integer, Map<Integer, int[]>> agentDisNeighbourMap, double costCE, double costEE) {
        double cost =0;
        if(t==0){
            for(int agentId =1 ;agentId <= agentDisNeighbourMap.size();agentId++){
                int[] data = resultCycle1.getAgentData(agentId);
                Map<Integer, int[]> disNeighbourMap = agentDisNeighbourMap.get(agentId);
                for(int k=0;k<data.length;k++){
                    if( data[k]==1 ){
                        cost += costCE;
                    }
                }
            }
        }
        else {
            ResultCycle resultCycle0 = (ResultCycle) resultMapByTime.get(t-1);
            for(int agentId =1 ;agentId <= agentDisNeighbourMap.size();agentId++){
                int[] data = resultCycle1.getAgentData(agentId);
                Map<Integer, int[]> disNeighbourMap = agentDisNeighbourMap.get(agentId);
                for(int k=0;k<data.length;k++){
                    int[] data_local_old = resultCycle0.getAgentData(agentId);
                    if( data[k]==1 && data_local_old[k] ==0){
                        boolean fromEdge = false;
                        int DataDis = Integer.MAX_VALUE;
                        for(int dis =1 ;dis <= disNeighbourMap.size() ; dis++ ){
                            if(fromEdge){
                                break;
                            }
                            int[] neighbours = disNeighbourMap.get(dis);
                            for(int neighbourID : neighbours){
                                int[] data_neighbour = resultCycle0.getAgentData(neighbourID);
                                if(data_neighbour[k]==1){
                                    DataDis = dis;
                                    fromEdge =true;
                                    break;
                                }
                            }
                        }
                        if(fromEdge){
                            cost += DataDis* costEE;
                        }else {
                            cost += costCE;
                        }
                    }
                }
            }
        }
        System.out.println("数据缓存成本Cost："+cost);
        return cost;
    }

}
