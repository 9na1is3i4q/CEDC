package problem_generator;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Reader {

    public static List<Server> GetServerList(){
        List<Server> serverList = new ArrayList<>();
        String csvFile = "src\\main\\java\\problem_generator\\site-optus-melbCBD.csv";
        String line = "";
        String cvsSplitBy = ",";

        int index =1;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine();
            while ((line = br.readLine()) != null) {
                // use comma as separator
                String[] line_str = line.split(cvsSplitBy);
                Server server = new Server(index,line_str[1],line_str[2]);
                serverList.add(server);
                index++;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serverList;
    }

    public static List<User> GetUserList(int dataNum,int timeSlot){
        List<User> userList = new ArrayList<>();
        String csvFile = "src\\main\\java\\problem_generator\\users-melbcbd-generated.csv";
        String line = "";
        String cvsSplitBy = ",";

        int index =1;
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] line_str = line.split(cvsSplitBy);
                User user = new User(index,line_str[0],line_str[1],dataNum,timeSlot);
                userList.add(user);
                index++;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return userList;
    }

}

