import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by alhanger on 11/4/15.
 */
public class PeopleTest {

    public Connection startConnection() throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:./test");
        People.createTables(conn);
        return conn;
    }

    public void endConnection(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();
        stmt.execute("DROP TABLE people");
        conn.close();
    }

    @Test
    public void testPerson() throws SQLException {
        Connection conn = startConnection();
        People.insertPerson(conn, "Alex", "Hanger", "alex@theironyard.com", "USA", "1.2.3.4.5");
        Person person = People.selectPerson(conn, 1);
        endConnection(conn);

        assertTrue(person != null);
    }

    @Test
    public void testSelectPeople() throws SQLException {
        Connection conn = startConnection();
        People.insertPerson(conn, "Alex", "Hanger", "alex@theironyard.com", "USA", "1.2.3.4.5");
        People.insertPerson(conn, "Anna", "Williams", "anna@theironyard.com", "USA", "1.2.3.4.5");
        People.insertPerson(conn, "Edward", "Hanger", "edward@theironyard.com", "USA", "1.2.3.4.5");
        ArrayList<Person> people = People.selectPeople(conn);
        endConnection(conn);

        assertTrue(people.size() == 3);
    }
}