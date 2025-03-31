package problem_parser;

import distributed.core.Problem;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

import java.io.File;

public class ProblemParser {

    protected Element rootElement;

    public ProblemParser(String path){
        try {
            rootElement = new SAXBuilder().build(new File(path)).getRootElement();
        } catch (Exception e) {
            throw new RuntimeException("parse failed");
        }
    }
    public Problem parse(){
        Problem problem = new Problem();
        Parser parser = null;
        parser = new Parser(rootElement,problem);
        parser.parseContent();
        return problem;
    }
}
