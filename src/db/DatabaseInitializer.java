package db;

import model.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
public class DatabaseInitializer {
    public static void initialize() {
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:meubanco.db");
             Statement stmt = conn.createStatement()) {

            String sql = Candidate.getTableDefinition();
            stmt.execute(sql);

            sql = Recruiter.getTableDefinition();
            stmt.execute(sql);

            sql = CandidateSkill.getTableDefinition();
            stmt.execute(sql);

            sql = Skill.getTableDefinition();
            stmt.execute(sql);

            sql = RecruiterJob.getTableDefinition();
            stmt.execute(sql);

            sql = Job.getTableDefinition();
            stmt.execute(sql);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
