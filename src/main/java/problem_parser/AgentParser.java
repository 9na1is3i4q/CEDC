package problem_parser;

import distributed.core.AgentDescriptor;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AgentParser {
    private String agentsPath;
    public AgentParser(String agentsPath) {
        this.agentsPath = agentsPath;
    }

    public Map<String, AgentDescriptor> parse(){
        Map<String,AgentDescriptor> map = new HashMap<>();
        try {
            Element root = new SAXBuilder().build(new File(agentsPath)).getRootElement();
            List<Element> agentList = root.getChildren("agents").get(0).getChildren("agent");
            for (Element agentElement : agentList){
                String name = agentElement.getAttributeValue("name").toUpperCase();
                AgentDescriptor agentDescriptor = new AgentDescriptor();
                agentDescriptor.className = agentElement.getAttributeValue("class");
                agentDescriptor.method = agentElement.getAttributeValue("method").toUpperCase();
                map.put(name.toUpperCase(),agentDescriptor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }


    public Map<String,String> parseConfigurations(){
        Map<String,String> configurations = new HashMap<>();
        Element root = null;
        try {
            root = new SAXBuilder().build(new File(agentsPath)).getRootElement();
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (root.getChildren("configurations").size() == 0){
            return configurations;
        }
        List<Element> configurationList = root.getChildren("configurations").get(0).getChildren("configuration");
        for (Element configuration : configurationList){
            configurations.put(configuration.getAttributeValue("name").toUpperCase(),configuration.getAttributeValue("value").toUpperCase());
        }
        return configurations;
    }

}
