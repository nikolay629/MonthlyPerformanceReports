import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CalculateScore {
    private static List<String[]> employeesList;

    private static int topPerformersThreshold;
    private static boolean useExperienceMultiplier;
    private static int periodLimit;

    public static void main(String[] args) {

        JSONParser jsonParser = new JSONParser();
        ClassLoader classLoader = CalculateScore.class.getClassLoader();

        File data = new File(Objects.requireNonNull(classLoader.getResource("Data.json")).getFile());
        File reportDefinition = new File(Objects.requireNonNull(classLoader.getResource("ReportDefinition.json")).getFile());

        employeesList = new ArrayList<>();
        employeesList.add(new String[] {"Name    ","Score"});

        try {
            BufferedReader readerReportDefinition = new BufferedReader(new FileReader(reportDefinition));
            BufferedReader readerData = new BufferedReader(new FileReader(data));

            Object objectDefinition = jsonParser.parse(readerReportDefinition);
            JSONObject definition = (JSONObject) objectDefinition;

            topPerformersThreshold = Integer.parseInt(definition.get("topPerformersThreshold").toString());
            useExperienceMultiplier = Boolean.parseBoolean(definition.get("useExperienceMultiplier").toString());
            periodLimit = Integer.parseInt(definition.get("periodLimit").toString());


            Object objectData = jsonParser.parse(readerData);
            JSONArray employees = (JSONArray) objectData;

            employees.forEach(employee -> parseEmployee((JSONObject) employee));
            printCSVFile();

        } catch (ParseException | IOException e){
            e.printStackTrace();
        }
    }

    public static void parseEmployee(JSONObject object){
        String name = (String) object.get("name");
        int salesPeriod = Integer.parseInt(object.get("salesPeriod").toString());
        int totalSales = Integer.parseInt(object.get("totalSales").toString());
        float experienceMultiplier = Float.parseFloat(object.get("experienceMultiplier").toString());

        float score;

        if(useExperienceMultiplier)
            score = (totalSales/salesPeriod) * experienceMultiplier;
        else
            score = totalSales/salesPeriod;

        if(salesPeriod <= periodLimit && score >= topPerformersThreshold){
            employeesList.add(new String[] {"\n" + name, String.valueOf(score)});
        }
    }

    public static void printCSVFile() throws IOException{
        Path path = Paths.get("src","main", "resources");
        String absolutePath = path.toFile().getAbsolutePath();

        PrintWriter printWriter = new PrintWriter(new File(absolutePath,"Output.csv"));

        String line;

        for (String[] employee : employeesList) {
            line = employee[0] + ", " + employee[1];
            printWriter.write(line);
        }
        printWriter.close();
    }
}
