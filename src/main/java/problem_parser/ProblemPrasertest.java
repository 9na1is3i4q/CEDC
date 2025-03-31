package problem_parser;

import distributed.core.Problem;


public class ProblemPrasertest {
    public static void main(String[] args) {
        String problemPath = "problem\\edc_dcop\\10\\diviceNum200\\EDC_DCOP_10_200_4_0.220.xml";
        ProblemParser parser = new ProblemParser(problemPath);
        Problem problem = parser.parse();
        problem.Print();
    }
}
