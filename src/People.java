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
            person.id = results.getInt("id");
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
            String firstName = columns[1];
            String lastName = columns[2];
            String email = columns[3];
            String country = columns[4];
            String ip = columns[5];

            insertPerson(conn, firstName, lastName, email, country, ip);
        }
    }

    public static ArrayList<Person> selectPeople(Connection conn, int offset) throws SQLException{
        ArrayList<Person> people = new ArrayList<>();
        PreparedStatement stmt = conn.prepareStatement("SELECT * FROM people LIMIT 20 OFFSET ?");
        stmt.setInt(1, offset);
        ResultSet results = stmt.executeQuery();
        while (results.next()) {
            Person person = new Person();
            person.id = results.getInt("id");
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

        Spark.get(
                "/",
                ((request, response) -> {
                    String offset = request.queryParams("offset");
                    int offsetNum = 0;

                    try{
                        offsetNum = Integer.valueOf(offset);
                    } catch (Exception e) {

                    }

                    ArrayList<Person> list = selectPeople(conn, offsetNum);

                    HashMap m = new HashMap();
                    m.put("list", list);
                    m.put("prevCounter", offsetNum - SHOW_COUNT);
                    m.put("counter", offsetNum + SHOW_COUNT);

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
                        Person person = selectPerson(conn, idNum);
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
