import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zach on 10/19/15.
 */
public class People {
    public static void main(String[] args) {
        ArrayList<Person> people = new ArrayList();

        String fileContent = readFile("people.csv");
        String[] lines = fileContent.split("\n");

        for (String line : lines) {
            if (line == lines[0])
                continue;

            String[] columns = line.split(",");
            Person person = new Person(Integer.valueOf(columns[0]), columns[1], columns[2], columns[3], columns[4], columns[5]);
            people.add(person);
        }

        Spark.get(
                "/",
                ((request, response) -> {
                    String counter = request.queryParams("counter");
                    int countNum = 0;

                    if (counter == null) {
                        countNum = 0;
                    }
                    else if (counter != null) {
                        countNum = Integer.valueOf(counter);
                    }
                    if (countNum >= people.size() || countNum < 0) {
                        Spark.halt(403);
                    }

                    ArrayList<Person> list = new ArrayList<Person>(people.subList(countNum, countNum + 20));

                    HashMap m = new HashMap();
                    m.put("list", list);
                    m.put("prevCounter", countNum - 20);
                    m.put("counter", countNum + 20);

                    return new ModelAndView(m, "people.html");
                }),
                new MustacheTemplateEngine()
        );
        Spark.get(
                "/person",
                ((request, response) -> {
                    HashMap p = new HashMap();
                    String id = request.queryParams("id");

                    try{
                        int idNum = Integer.valueOf(id);
                        Person person = people.get(idNum - 1);
                        p.put("person", person);
                    } catch (Exception e) {

                    }
                    return new ModelAndView(p, "person.html");
                }),
                new MustacheTemplateEngine()
        );
    }

    static String readFile(String fileName) {
        File f = new File(fileName);
        try {
            FileReader fr = new FileReader(f);
            int fileSize = (int) f.length();
            char[] fileContent = new char[fileSize];
            fr.read(fileContent);
            return new String(fileContent);
        } catch (Exception e) {
            return null;
        }
    }
}
