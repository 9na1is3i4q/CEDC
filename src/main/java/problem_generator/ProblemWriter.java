package problem_generator;

import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

public class ProblemWriter {
    private int nbInstance;
    private String dirPath;

    private int agentNum = 6 ;
    private double density = 0.1;
    private int diviceNum = 10;
    private double singleHostRate = 0.6;
    private int dataNum=4;
    private  int timeSlots=1;
    private  int maxDistance=1;
    private  int serverDataLimit=1;
    private double requireRate=-1.0;
    private String problemType;


    public ProblemWriter(int nbInstance, String dirPath, int agentNum, double density, int diviceNum, double singleHostRate, int dataNum, int timeSlots, int maxDistance, int serverDataLimit, String problemType, double requireRate) {
        this.nbInstance = nbInstance;
        this.dirPath = dirPath;
        this.agentNum = agentNum;
        this.density = density;
        this.diviceNum = diviceNum;
        this.singleHostRate = singleHostRate;
        this.dataNum = dataNum;
        this.timeSlots = timeSlots;
        this.maxDistance = maxDistance;
        this.serverDataLimit = serverDataLimit;
        this.problemType = problemType;
        this.requireRate = requireRate;

    }

    public void generate() throws Exception{
        Format format = Format.getPrettyFormat();
        format.setEncoding("UTF-8");
        XMLOutputter outputter = new XMLOutputter(format);
        int base = 0;
        File f=new File(dirPath);
        if(!f.exists())
        {
            f.mkdirs();
        }
        String filenameBase =dirPath + "\\" + problemType + "_" + agentNum + "_" + diviceNum + "_"+ dataNum + "_" + density ;
        while (true){
            String fileName = filenameBase + base + ".xml";
            if (!new File(fileName).exists())
                break;
            base++;
        }

        for (int i = 0; i < nbInstance; i++){
            FileOutputStream stream = new FileOutputStream(filenameBase+ (base + i) + ".xml");
            Element root = new Element("instance");
            EDCGraph problemGenerator = null;
            problemGenerator = new ProblemGenerator(agentNum,density,diviceNum,singleHostRate,dataNum,timeSlots,maxDistance,serverDataLimit,requireRate);
            problemGenerator.setName("Instance"+i);
            ((ProblemGenerator) problemGenerator).Print();
            root.addContent(problemGenerator.getPresentation());
            root.addContent(problemGenerator.getAgents());
            root.addContent(problemGenerator.getDatas());
            root.addContent(problemGenerator.getDivices());
            root.addContent(problemGenerator.getVariables());
            root.addContent(problemGenerator.getConstraints());
            root.addContent(problemGenerator.getGuiPresentation());
            outputter.output(root,stream);
            stream.close();
        }

    }





    public static void main(String[] args) throws Exception {
         Map<String,Object> para = new HashMap<String, Object>();

        int agentNum = 10;
        //1.0
        double density = 0.222;
        //2.2
//        double density = 0.4888;
        //2.4
//        double density = 0.5333;
        //2.6
//        double density = 0.5777;
        //2.8
//        double density = 0.6222;


//        int agentNum = 20 ;
//        double density = 0.105;

//        int agentNum = 30 ;
//        double density = 0.068;

//        int agentNum = 40 ;
//        double density = 0.051;

//
//        int agentNum = 50;
//        double density = 0.0408;

//        int agentNum = 60;
//        double density = 0.0339;

//        int agentNum = 70;
//        double density = 0.0290;

//        int agentNum = 80;
//        double density = 0.0253;

//        int agentNum = 90;
//        double density = 0.0225;

//        int agentNum = 100;
//        double density = 0.0203;

//        int agentNum = 110;
//        double density = 0.0184;

//        int agentNum = 120 ;
//        double density = 0.0169;






//        int agentNum = 80 ;
//        double density = 0.025;



//        int agentNum = 40 ;
//        //1.0
//        double density = 0.051;

//        double density = 0.102;





        int maxDistance= 2;
        int serverDataLimit = 3;
        int dataNum=4;
        int timeSlots = 50;
        int diviceNum = 200;


        double requireRate = 0.1;
        double singleHostRate = 0.7;


        String problemType = "EDC_DCOP";
//        String path = "problem/edc_dcop/" + String.valueOf(agentNum)+"/dataNum"+ String.valueOf(agentNum);
//        String path = "problem/edc_dcop/" + String.valueOf(agentNum)+diviceNum +"/Simple"+ String.valueOf(dataNum);
//        String path = "problem/edc_dcop/" + String.valueOf(agentNum)+diviceNum +"/dataNum"+ String.valueOf(dataNum);
        String path = "problem/edc_dcop/" + String.valueOf(agentNum)+diviceNum +"/serverDataLimit"+ String.valueOf(serverDataLimit);
//        String path = "problem/edc_dcop/" + String.valueOf(agentNum)+diviceNum +"/density"+ String.valueOf(density);

        ProblemWriter writer = new ProblemWriter(30,path,agentNum,density,diviceNum,singleHostRate,dataNum,timeSlots,maxDistance,serverDataLimit,problemType,requireRate);
         writer.generate();
    }



}
