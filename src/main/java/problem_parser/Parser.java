package problem_parser;

import distributed.core.Problem;
import org.jdom2.Element;

import java.util.*;

public class Parser {
    protected Element rootElement;
    protected Problem problem;
    protected Map<String,int[]> domains;
    protected Map<String,Integer> agentNameId;
    protected Map<String,AgentPair> constraintInfo;


    protected Map<Integer,Set<Integer>> agentNeighborsId;


    public Parser(Element rootElement, Problem problem) {
        this.rootElement = rootElement;
        this.problem = problem;
        domains = new HashMap<>();
        agentNameId = new HashMap<>();
        constraintInfo = new HashMap<>();
        agentNeighborsId = new HashMap<>();
    }


    public void parseContent(){
        parsePresentation();
        parseAgents();
        parseData();
        parseDivice();
        parseConstraints();
    }


    protected void parsePresentation(){
        Element presentation = rootElement.getChild("presentation");
        String timeSlots = presentation.getAttributeValue("timeSlots");
        problem.timeSlots=Integer.parseInt(timeSlots);

        String maxDistance = presentation.getAttributeValue("maxDistance");
        problem.maxDistance=Integer.parseInt(maxDistance);

        String serverDataLimit = presentation.getAttributeValue("serverDataLimit");
        problem.serverDataLimit=Integer.parseInt(serverDataLimit);
    }

    protected void parseAgents(){
        List<Element> agentElements = rootElement.getChild("agents").getChildren("agent");
        problem.allAgentId = new int[agentElements.size()];
        int index = 0;
        for (Element agentElement : agentElements){
            int id = Integer.parseInt(agentElement.getAttributeValue("id"));
            String name = agentElement.getAttributeValue("name");
            processhostUsers(agentElement.getChild("users").getText(),id);
            processNeighboursUnderLimit(agentElement.getChildren("neighbours"),id);
            problem.allAgentId[index++] = id;
            agentNameId.put(name,id);
        }
        problem.setUserHosts();
    }

    private void processhostUsers(String hostedUser, Integer agentId) {
        String[] hostedUsers = hostedUser.split("\\|");
        int[] userarr = new int[hostedUsers.length];
        for(int i=0;i<hostedUsers.length;i++){
            int user = Integer.parseInt(hostedUsers[i]);
            userarr[i] = user;
            if(problem.getUserHostList().get(user) ==null){
                ArrayList<Integer> hosts = new ArrayList<>();
                hosts.add(agentId);
                problem.getUserHostList().put(user,hosts);
            }else {
                List<Integer> hosts = problem.getUserHostList().get(user);
                hosts.add(agentId);
            }
        }
        problem.hostUsers.put(agentId,userarr);
    }

    private void processNeighboursUnderLimit(List<Element> neighbours, int id) {
        HashMap<Integer, int[]> NeighboursByDisMap = new HashMap<>();
        for(Element neighboursElement : neighbours){
            int dis = Integer.parseInt(neighboursElement.getAttributeValue("dis"));
            if(neighboursElement.getText().length()>0){
                String[] NeighboursByDis = neighboursElement.getText().split("\\|");
                int[] NeighboursByDisArr = new  int[NeighboursByDis.length];
                for(int i=0;i<NeighboursByDis.length;i++){
//                    System.out.println(neighboursElement.getText());
                    int neighbour = Integer.parseInt(NeighboursByDis[i]);
                    NeighboursByDisArr[i] = neighbour;
                }
                NeighboursByDisMap.put(dis,NeighboursByDisArr);
            }

        }
        problem.neighboursUnderLimit.put(id,NeighboursByDisMap);

        problem.neighbours.put(id,NeighboursByDisMap.get(1));
    }


    protected void parseData(){
        List<Element> dataElements = rootElement.getChild("Datas").getChildren("data");
        problem.allDataId = new int[dataElements.size()];
        int index = 0;
        for (Element dataElement : dataElements){
            int id = Integer.parseInt(dataElement.getAttributeValue("id"));
            problem.allDataId[index++] = id;
        }
    }


    protected void parseDivice(){
        List<Element> diviceElements = rootElement.getChild("Divices").getChildren("device");
        problem.allUserId = new int[diviceElements.size()];
        int index = 0;
        for (Element diviceElement : diviceElements){
            int diviceId = Integer.parseInt(diviceElement.getAttributeValue("id"));
            String name = diviceElement.getAttributeValue("name");
            problem.allUserId[index++] = diviceId;
            int[][] requireData = new int[problem.timeSlots][problem.allDataId.length];
            problem.userRequireData.put(diviceId,requireData);
            parseRequireData(diviceElement,diviceId);
        }
    }

    private void parseRequireData(Element diviceElement, Integer diviceId) {
        List<Element> timeSlotList = diviceElement.getChildren("timeSlot");
        for (Element timeSlot : timeSlotList){
            int time = Integer.parseInt(timeSlot.getAttributeValue("id"));
            processUserRequireData(timeSlot.getText(),diviceId,time);
        }
    }

    private void processUserRequireData(String tuple, Integer diviceId, Integer timeId) {
//        System.out.println("time:"+timeId+" divice:"+diviceId);
        String[] tuples = tuple.split("\\|");
        int[][] requireData = problem.userRequireData.get(diviceId);
        for (String t : tuples){
            String[] info = t.split(":");
            int dataIndex = Integer.parseInt(info[0]) - 1;
            int dataRequire = Integer.parseInt(info[1]);
            requireData[timeId][dataIndex] = dataRequire;
        }
    }


    protected void parseConstraints(){
        problem.neighbours = new HashMap<>();
        List<Element> constraintElements = rootElement.getChild("constraints").getChildren("constraint");
        for (Element constraintElement : constraintElements){
            String constraintName = constraintElement.getAttributeValue("reference");
            AgentPair agentPair = new AgentPair(constraintElement.getAttributeValue("scope"));
            int former = agentPair.former;
            int latter = agentPair.latter;

            if(agentNeighborsId.get(former) == null){
                Set<Integer> neighbour = new HashSet<Integer>();
                neighbour.add(latter);
                agentNeighborsId.put(former,neighbour);
            }else {
                Set<Integer> neighbour = agentNeighborsId.get(former);
                neighbour.add(latter);
            }

            if(agentNeighborsId.get(latter) == null){
                Set<Integer> neighbour = new HashSet<Integer>();
                neighbour.add(former);
                agentNeighborsId.put(latter,neighbour);
            }else {
                Set<Integer> neighbour = agentNeighborsId.get(latter);
                neighbour.add(former);
            }


        }

        for (int agentId : problem.allAgentId){
            Set<Integer> neighbours = agentNeighborsId.get(agentId);
            int[] neighbourArray = new int[neighbours.size()];
            int index = 0;
            for (int neighbourId : neighbours){
                neighbourArray[index++] = neighbourId;
            }
            problem.neighbours.put(agentId,neighbourArray);
        }
    }



    protected class AgentPair{
        int former;
        int latter;

        public AgentPair(String scope){
            String[] ids = scope.split(" ");
            if (ids.length != 2){
                throw new IllegalArgumentException();
            }
            former = agentNameId.get(ids[0]);
            latter = agentNameId.get(ids[1]);
        }
    }
}
