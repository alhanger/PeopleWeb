import spark.ModelAndView;
import spark.Spark;
import spark.template.mustache.MustacheTemplateEngine;

import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by zach on 10/19/15.
 */
public class People {
    static final int SHOW_COUNT = 20;

    public static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE IF EXISTS people");
        stmt.execute("CREATE TABLE people " +
                "(id IDENTITY, first_name VARCHAR, last_name VARCHAR, email VARCHAR, country VARCHAR, ip VARCHAR)");
    }

    public static void insertPerson(Connection conn, String firstName, String lastName, String email, String country, String ip) throws SQLException {
        PreparedStatement stmt = conn.prepareStatement("INSERT INTO people VALUES (NULL, ?, ?, ?, ?, ?)");
        stmt.setString(1, firstName);
        stmt.setString(2, lastName);
        stmt.setString(3, email);
        stmt.setString(4, country);
        stmt.setString(5, ip);
        stmt.execute();
    }

    public static Person selectPerson(Connection conn, int id) throws SQLException {
        Person person = null;
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM people WHERE id = ?");
        stmt.setInt(1, id);
        ResultSet results = stmt.executeQuery();
        if (results.next()) {
            person = new Person();
            person.firstName = results.getString("first_name");
            person.lastName = results.getString("last_name");
            person.email = results.getString("email");
            person.country = results.getString("country");
            person.ip = results.getString("ip");
        }
        return person;
    }

    public static void populateDatabase(Connection conn, String fileName) throws SQLException {
        String fileContent = readFile(fileName);
        String[] lines = fileContent.split("\n");
        for (String line : lines) {
            if (line == lines[0])
                continue;

            String[] columns = line.split(",");
            String firstName = columns[0];
            String lastName = columns[1];
            String email = columns[2];
            String country = columns[3];
            String ip = columns[4];

            insertPerson(conn, firstName, lastName, email, country, ip);
        }
    }

    public static ArrayList<Person> selectPeople(Connection conn) throws SQLException{
        ArrayList<Person> people = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM people");
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            Person person = new Person();
            person.firstName = results.getString("first_name");
            person.lastName = results.getString("last_name");
            person.email = results.getString("email");
            person.country = results.getString("country");
            person.ip = results.getString("ip");
            people.add(person);
        }
        return people;
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./main");
        createTables(conn);

        populateDatabase(conn, "people.csv");

        ArrayList<Person> people = new ArrayList();

//        String fileContent = readFile("people.csv");
//        String[] lines = fileContent.split("\n");
//
//        for (String line : lines) {
//            if (line == lines[0])
//                continue;
//
//            String[] columns = line.split(",");
//            Person person = new Person(Integer.valueOf(columns[0]), columns[1], columns[2], columns[3], columns[4], columns[5]);
//            people.add(person);
//        }

        Spark.get(
                "/",
                ((request, response) -> {
                    String counter = request.queryParams("counter");
                    int countNum;

                    if (counter == null) {
                        countNum = 0;
                    }
                    else {
                        countNum = Integer.valueOf(counter);
                    }
                    if (countNum >= people.size() || countNum < 0) {
                        Spark.halt(403);
                    }

                    ArrayList<Person> list = new ArrayList<Person>(people.subList(
                            Math.max(0, Math.min(people.size(), countNum)),
                            Math.max(0, Math.min(people.size(), countNum + SHOW_COUNT))
                    ));

                    HashMap m = new HashMap();
                    m.put("list", list);
                    m.put("prevCounter", countNum - SHOW_COUNT);
                    m.put("counter", countNum + SHOW_COUNT);

                    boolean showNext = countNum + SHOW_COUNT < people.size();
                    boolean showPrev = countNum > 0;
                    m.put("showNext", showNext);
                    m.put("showPrev", showPrev);

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
