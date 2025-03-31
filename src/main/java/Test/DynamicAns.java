package Test;

import java.util.Arrays;


public class DynamicAns {
    int problemNums;
    int timeSteps;
    long[][] caculateTime;
    double[][] allCost;
    double[][] allBenefit;
    double[][] allRevenue;

    public DynamicAns(int problemNums, int timeSteps) {
        this.problemNums = problemNums;
        this.timeSteps = timeSteps;
        caculateTime = new long[problemNums][timeSteps];
        allCost = new double[problemNums][timeSteps];
        allBenefit = new double[problemNums][timeSteps];
        allRevenue = new double[problemNums][timeSteps];
    }

    public void setCaculateTime(int problemNum,int timeStep,long caculateTimeNow) {
        this.caculateTime[problemNum][timeStep] = caculateTimeNow;
    }

    public void setAllCost(int problemNum,int timeStep,double costNow) {
        this.allCost[problemNum][timeStep] = costNow;
    }

    public void setAllBenefit(int problemNum,int timeStep,double benefitNow) {
        this.allBenefit[problemNum][timeStep]  = benefitNow;
    }

    public void setAllRevenue(int problemNum,int timeStep,double revenueNow) {
        this.allRevenue[problemNum][timeStep]  = revenueNow;
    }



   public double[] GetAvgByProblems(double[][] input){
       double[] avgAns = new double[problemNums];
       double[] allAns = new double[problemNums];
       Arrays.fill(avgAns,0);
       Arrays.fill(allAns,0);
       for(int i=0;i<problemNums;i++){
           for(int j=0;j<timeSteps;j++){
               allAns[i] += input[i][j];
           }
       }
       for(int i=0;i<problemNums;i++){
           avgAns[i] = allAns[i]/timeSteps;
       }
       return avgAns;
   }

    public double[] GetAvgByProblems(long[][] input){
        double[] avgAns = new double[problemNums];
        long[] allAns = new long[problemNums];
        Arrays.fill(avgAns,0);
        Arrays.fill(allAns,0);
        for(int i=0;i<problemNums;i++){
            for(int j=0;j<timeSteps;j++){
                allAns[i] += input[i][j];
            }
        }
        for(int i=0;i<problemNums;i++){
            avgAns[i] = (double)allAns[i]/timeSteps;
        }
        return avgAns;
    }

    public double[] GetAvgByTimes(double[][] input){
        double[] avgAns = new double[timeSteps];
        double[] allAns = new double[timeSteps];
        Arrays.fill(avgAns,0);
        Arrays.fill(allAns,0);

        for(int j=0;j<timeSteps;j++){
            for(int i=0;i<problemNums;i++){
                allAns[j] += input[i][j];
            }
        }

        for(int j=0;j<timeSteps;j++){
            avgAns[j] = allAns[j]/(double)problemNums;
        }
        return avgAns;
    }


    public double[] GetAvgByTimes(long[][] input){
        double[] avgAns = new double[timeSteps];
        long[] allAns = new long[timeSteps];
        Arrays.fill(avgAns,0);
        Arrays.fill(allAns,0);

        for(int j=0;j<timeSteps;j++){
            for(int i=0;i<problemNums;i++){
                allAns[j] += input[i][j];
            }
        }

        for(int j=0;j<timeSteps;j++){
            avgAns[j] = allAns[j]/(double)problemNums;
        }
        return avgAns;
    }

    public void Print() {
        System.out.println("-------------------------------------------time--------------------------------------------");
        for(int i=0;i<problemNums;i++){
            System.out.println("Problem"+i+Arrays.toString(caculateTime[i]));
        }
        System.out.println("-------------------------------------------cost--------------------------------------------");
        for(int i=0;i<problemNums;i++){
            System.out.println("Problem"+i+Arrays.toString(allCost[i]));
        }
        System.out.println("-------------------------------------------benefit--------------------------------------------");
        for(int i=0;i<problemNums;i++){
            System.out.println("Problem"+i+Arrays.toString(allBenefit[i]));
        }
        System.out.println("-------------------------------------------Revenue--------------------------------------------");
        for(int i=0;i<problemNums;i++){
            System.out.println("Problem"+i+Arrays.toString(allRevenue[i]));
        }
    }


    public void WriteAnsTable1(String Dir) {
        StringBuilder AnswerTableByProblem = new StringBuilder();
        AnswerTableByProblem.append(",").append("Cost").append(",").append("Benefit").append(",").append("Revenue").append(",").append("CaculateTime").append("\n");
        double[] avgCost = GetAvgByProblems(allCost);
        double[] avgBenefit = GetAvgByProblems(allBenefit);
        double[] avgRevenue= GetAvgByProblems(allRevenue);
        double[] avgCaculateTime = GetAvgByProblems(caculateTime);

        double allCost=0;
        double allBenefit=0;
        double allRevenue=0;
        double allCaculateTime=0;

        for(int i=0;i< problemNums;i++){
            AnswerTableByProblem.append("Problem ").append(i)
                    .append(",").append(avgCost[i])
                    .append(",").append(avgBenefit[i])
                    .append(",").append(avgRevenue[i])
                    .append(",").append(avgCaculateTime[i])
                    .append("\n");
            allCost += avgCost[i];
            allBenefit += avgBenefit[i];
            allRevenue += avgRevenue[i];
            allCaculateTime += avgCaculateTime[i];
        }
        AnswerTableByProblem.append("AverageAnswer ")
                .append(",").append(allCost/problemNums)
                .append(",").append(allBenefit/problemNums)
                .append(",").append(allRevenue/problemNums)
                .append(",").append(allCaculateTime/problemNums)
                .append("\n");

        FileUtils.writeStringToFile(AnswerTableByProblem.toString(),Dir);
    }

    public void WriteAnsTable2(String Dir) {
        StringBuilder AnswerTableByTime= new StringBuilder();
        AnswerTableByTime.append(",").append("Cost").append(",").append("Benefit").append(",").append("Revenue").append(",").append("CaculateTime").append("\n");
        double[] avgCost = GetAvgByTimes(allCost);
        double[] avgBenefit = GetAvgByTimes(allBenefit);
        double[] avgRevenue= GetAvgByTimes(allRevenue);
        double[] avgCaculateTime = GetAvgByTimes(caculateTime);
        double allCost=0;
        double allBenefit=0;
        double allRevenue=0;
        double allCaculateTime=0;
        for(int i=0;i< timeSteps;i++){
            AnswerTableByTime.append("TimeSlot ").append(i)
                    .append(",").append(avgCost[i])
                    .append(",").append(avgBenefit[i])
                    .append(",").append(avgRevenue[i])
                    .append(",").append(avgCaculateTime[i])
                    .append("\n");
            allCost += avgCost[i];
            allBenefit += avgBenefit[i];
            allRevenue += avgRevenue[i];
            allCaculateTime += avgCaculateTime[i];
        }
        AnswerTableByTime.append("AverageAnswer ")
                .append(",").append(allCost/timeSteps)
                .append(",").append(allBenefit/timeSteps)
                .append(",").append(allRevenue/timeSteps)
                .append(",").append(allCaculateTime/timeSteps)
                .append("\n");
        FileUtils.writeStringToFile(AnswerTableByTime.toString(),Dir);
    }
}
