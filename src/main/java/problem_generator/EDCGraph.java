package problem_generator;

import org.jdom2.Element;

import java.util.ArrayList;
import java.util.List;

public abstract class EDCGraph {
    protected String name;
    protected String type = "EDC_DCOP";
    protected String benchmark = "RandomDCOP";

    protected String constraintModel = "TKC";

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected String format = "XDisCSP 1.0";

    protected int dataNum;
    protected int agentNum;
    protected int diviceNum;
    protected int variableNum;
    protected int constraintNum;
    protected int timeSlots;
    protected int maxDistance;
    protected int serverDataLimit;
    protected List<Server> agentList;
    protected List<User> diviceList;


    protected List<Integer> source;
    protected List<Integer> dest;

    protected int[][] agentDistanceList ;

    //获取代理的 XML 元素
    public Element getAgents(){
        Element agentRootElement = new Element("agents");
        agentRootElement.setAttribute("AgentNum",String.valueOf(agentNum));
        for (int i = 1; i <= agentNum; i++){
            Element agent = new Element("agent");
            agent.setAttribute("name","A" + i);
            agent.setAttribute("id",String.valueOf(i));
            agent.setAttribute("description","Agent " + i);


            Element user_list = new Element("users");
            List<Integer> userList = agentList.get(i - 1).getUserList();
            user_list.addContent(getHostUsers(userList));
            agent.addContent(user_list);

            for(int dis_count=1; dis_count<=maxDistance;dis_count++){
                Element neighbours = new Element("neighbours");
                neighbours.setAttribute("dis",String.valueOf(dis_count));
                List<Integer> neighborsList = new ArrayList<>();
                int[] neighbourDis = agentDistanceList[i - 1];
                neighbours.addContent(getNeighboursList(neighbourDis,dis_count));
                agent.addContent(neighbours);
            }
            agentRootElement.addContent(agent);
        }
        return agentRootElement;
    }

    protected abstract String getNeighboursList(int[] neighbourDis, int dis_count);

    protected abstract String getHostUsers(List<Integer> userList);

    public Element getDatas(){
        Element dataRootElement = new Element("Datas");
        dataRootElement.setAttribute("DataNum",String.valueOf(dataNum));
        for (int i = 0; i < dataNum; i++){
            Element data = new Element("data");
            data.setAttribute("name","D" + i);
            data.setAttribute("id",String.valueOf(i));
            data.setAttribute("description","Data_" + i);
            dataRootElement.addContent(data);
        }
        return dataRootElement;
    }


    public Element getDivices(){
        Element Divices = new Element("Divices");
        Divices.setAttribute("diviceNum",String.valueOf(diviceNum));
        for (int i = 0; i < diviceNum; i++){
            Element device = new Element("device");
            device.setAttribute("name","U" + i);
            device.setAttribute("id",String.valueOf(i+1));
            device.setAttribute("host","X"+String.valueOf(diviceList.get(i).getHost()));
            for (int j = 0; j < timeSlots; j++){
                Element timeSlot = new Element("timeSlot");
                timeSlot.setAttribute("name","T" + j);
                timeSlot.setAttribute("id",String.valueOf(j));
                timeSlot.addContent(getRequire(i,j));
                device.addContent(timeSlot);
            }
            Divices.addContent(device);
        }
        return Divices;
    }

    protected abstract String getRequire(int diviceIndex,int timeIndex);


    public Element getVariables(){
        if (variableNum != agentNum*dataNum)
            throw new IllegalArgumentException("variables not equals with Agents!");
        Element variableRootElement = new Element("variables");
        variableRootElement.setAttribute("nbVariables",String.valueOf(variableNum));
        for (int i = 0; i < agentNum; i++){
            for (int j = 0; j < dataNum; j++){
                Element variable = new Element("variable");
                variable.setAttribute("agent","A" + (i + 1));
                variable.setAttribute("name","X" + (i + 1) + "_D"+ (j + 1) );
                variable.setAttribute("id",String.valueOf(i*dataNum+j+1));
                variable.setAttribute("description","variable " + (i + 1) + "."+(j+1));
            }
        }
        return variableRootElement;
    }

    public Element getConstraints(){
        Element allConstraint = new Element("constraints");
        allConstraint.setAttribute("nbConstraints",String.valueOf(constraintNum));
        for (int i = 0; i < constraintNum; i++){
            Element constaint = new Element("constraint");
            constaint.setAttribute("name","C" + i);
            constaint.setAttribute("model","TKC");
            constaint.setAttribute("arity","2");
            constaint.setAttribute("scope","A" + source.get(i) + " A" + dest.get(i));
            allConstraint.addContent(constaint);
        }
        return allConstraint;
    }


    public Element getPresentation(){
        Element presentation = new Element("presentation");
        presentation.setAttribute("name",name);
        presentation.setAttribute("type",type);
        presentation.setAttribute("benchmark",benchmark);
        presentation.setAttribute("constraintModel",constraintModel);
        presentation.setAttribute("timeSlots", String.valueOf(timeSlots));
        presentation.setAttribute("maxDistance", String.valueOf(maxDistance));
        presentation.setAttribute("serverDataLimit", String.valueOf(serverDataLimit));
        presentation.setAttribute("format",format);
        return presentation;
    }

    public Element getGuiPresentation(){
        Element guiPresentation = new Element("GuiPresentation");
        guiPresentation.setAttribute("type",type);
        guiPresentation.setAttribute("benchmark",benchmark);
        guiPresentation.setAttribute("name",name);
        guiPresentation.setAttribute("model","TKC");
        guiPresentation.setAttribute("nbAgents",String.valueOf(agentNum));
        guiPresentation.setAttribute("nbConstraints",String.valueOf(constraintNum));
        return guiPresentation;
    }


}
