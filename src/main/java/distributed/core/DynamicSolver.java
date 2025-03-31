package distributed.core;

import problem_parser.ProblemParser;

import java.util.concurrent.atomic.AtomicBoolean;


public class DynamicSolver {
    private AtomicBoolean isSolving;
    private Thread threadMonitor;

    public DynamicSolver(){
        isSolving = new AtomicBoolean(false);
    }

    public void solve(String agentDescriptorPath,String agentType,String problemPath,DynamicFinishedListener listener,boolean showConstraintGraph,boolean showPesudoTree){
        ProblemParser parser = new ProblemParser(problemPath);
        Problem problem = parser.parse();

        problem.Print();
        DynamicAgentManager manager = new DynamicAgentManager(agentDescriptorPath,agentType,problem,listener,showPesudoTree);
        manager.startAgents();
    }

    public void solve(String agentDescriptorPath,String agentType,String problemPath, double Gama, double CostCE, double CostEE, DynamicFinishedListener listener,boolean showConstraintGraph,boolean showPesudoTree) {
        ProblemParser parser = new ProblemParser(problemPath);
        Problem problem = parser.parse();

        problem.Print();
        DynamicAgentManager manager = new DynamicAgentManager(agentDescriptorPath,agentType,problem,listener,showPesudoTree, Gama,  CostCE,  CostEE);  //初始化服务器
        manager.startAgents();
    }
}
