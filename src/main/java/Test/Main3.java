package Test;

import distributed.core.FinishedListener;
import distributed.core.Solver;
import distributed.result.Result;
import distributed.result.ResultCycle;


public class Main3 {
    public static void main(String[] args){
        Solver solver = new Solver();
        String[] algs =  new String[100];

        algs[0]= "MGM1";
        algs[1]= "MGM2";
        int ind =1 ;
        String algo = algs[1];

//        String problemPath = "problem\\edc_dcop\\40\\EDC_DCOP_40_300_4_0.0260.xml";
//        String problemPath =  "problem\\edc_dcop\\40\\EDC_DCOP_40_500_1_0.02618.xml";
        String problemPath =  "problem\\edc_dcop\\40\\EDC_DCOP_40_500_4_0.0265.xml";
//        String problemPath = "problem\\edc_dcop\\20\\EDC_DCOP_20_300_4_0.10.xml";

//        String problemPath =  "problem\\edc_dcop\\120\\EDC_DCOP_120_500_1_0.032.xml";
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
                double min_val = Double.MAX_VALUE;
                for (int i = 0; i < res.length; ++i) {
                    System.out.println("cylce " + i + " : " + res[i]+"\tdatasum:"+dataSumInCycle[i]+"\tbenefit:"+benefitInCycle[i]);
                    min_val = Double.min(min_val, res[i]);
                    str.append(res[i]+"\n");
                }
                System.out.println("min_val: " + min_val);;


            }
        },showGraphic,showPseduoTree);
    }

}
