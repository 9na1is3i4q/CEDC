package Test;

import distributed.core.Agent;
import distributed.core.FinishedListener;
import distributed.core.Solver;
import distributed.result.Result;
import distributed.result.ResultCycle;
import distributed.result.ResultWithPrivacy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class Main1 {
    public static void main(String[] args){
        Solver solver = new Solver();
        String[] algs =  new String[100];

        algs[0]= "MGM1";
        algs[1]= "MGM2";
        algs[2] = "MAXSUM";
        int ind =2 ;
        String algo = algs[2];

//        String problemPath = "problem\\edc_dcop\\40\\EDC_DCOP_40_300_4_0.0260.xml";
//        String problemPath = "problem\\edc_dcop\\20\\EDC_DCOP_20_300_4_0.10.xml";
//        String problemPath =  "problem\\edc_dcop\\7\\EDC_DCOP_7_100_1_0.10.xml";
        String problemPath =  "problem\\edc_dcop\\10\\EDC_DCOP_10_500_4_0.227.xml";
//        String problemPath =  "problem\\edc_dcop\\50\\EDC_DCOP_50_300_1_0.0250.xml";
//        String problemPath =  "problem\\edc_dcop\\7\\EDC_DCOP_7_300_1_0.19.xml";
//        String problemPath =  "problem\\edc_dcop\\40\\EDC_DCOP_40_500_1_0.0265.xml";
//        String problemPath =  "problem\\edc_dcop\\40\\EDC_DCOP_40_500_1_0.02612.xml";
//        String problemPath =  "problem\\edc_dcop\\40\\EDC_DCOP_40_500_1_0.02618.xml";
//        String problemPath =  "problem\\edc_dcop\\4\\EDC_DCOP_4_15_4_0.81.xml";


        boolean showGraphic = true;
        boolean showPseduoTree = false;


        solver.solve("problem/am.xml", algo, problemPath, new FinishedListener() {
            @Override
            public void onFinished(Result result) {
                int num=1;
                ResultCycle resultCycle1 = (ResultCycle) result;
                double[] res = resultCycle1.costInCycle;
                double[] benefitInCycle = resultCycle1.benefitInCycle;
                double[] dataSumInCycle = resultCycle1.dataSumInCycle;


                StringBuilder str = new StringBuilder();
                double max_val = Double.MIN_VALUE;
                for (int i = 0; i < res.length; ++i) {
                    System.out.println("cylce " + i + " : " + res[i]+"\tdatasum:"+dataSumInCycle[i]+"\tbenefit:"+benefitInCycle[i]);
                    max_val = Double.max(max_val, res[i]);
                    str.append(res[i]+"\n");
                }
                Map<Integer, Map<Integer, int[]>> agentDisNeighbourMap = resultCycle1.getAgentDisNeighbourMap();
                Map<Integer, int[]> agentUsers = resultCycle1.getAgentUsers();
                Map<Integer, Map<Integer, int[]>> agentUserRequireData = resultCycle1.getAgentUserRequireData();
                int cost =0;
                for(int agentId =1 ;agentId <= agentDisNeighbourMap.size();agentId++){
                    int[] data = resultCycle1.DataAns.get(agentId);
                    System.out.println("Agent:"+ agentId+": "+Arrays.toString(data));
                    for(int d : data){
                        cost += d;
                    }
                }
                HashMap<Integer, double[]> userBenefitRecord = new HashMap<Integer, double[]>();
                double benefit =0;
                for(int agentId =1 ;agentId <= agentDisNeighbourMap.size();agentId++){
                    int[] localData = resultCycle1.DataAns.get(agentId);
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
                                    int[] neighoburData = resultCycle1.DataAns.get(neighbourId);
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


                System.out.println("Cost: " + cost);;
                System.out.println("Benefit: " + benefit);;
                System.out.println("max_val: " + max_val);;


                //ResultWithPrivacy resultCycle = (ResultWithPrivacy) result;
//                  ResultCycle resultCycle = (ResultCycle) result;
//                  for (double i : resultCycle.costInCycle){
//                      System.out.println(i+"   "+num);
//                      num++;
//                  }
//                ResultWithPrivacy resultCycle = (ResultWithPrivacy) result;
//                System.out.println("计算得到cost:"+resultCycle.getUb());
//                System.out.println("计算得到Time:"+resultCycle.getTotalTime());
//                System.out.println("计算得到Ncccs:"+resultCycle.getNcccs());
//                System.out.println("计算得到MessageQuantity:"+resultCycle.getMessageQuantity());
//                System.out.println("计算得到size:"+resultCycle.getMessageSizeCount());

            }
        },showGraphic,showPseduoTree);
    }

}
