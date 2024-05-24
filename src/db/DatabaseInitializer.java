package db;

import model.Candidate;
import model.Recruiter;

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
/*
            String sql = "CREATE TABLE IF NOT EXISTS candidatos (\n"
                    + "	id integer PRIMARY KEY AUTOINCREMENT,\n"
                    + "	nome text NOT NULL,\n"
                    + "	email text NOT NULL UNIQUE,\n"
                    + "	senha text NOT NULL\n"
                    + ");";
            stmt.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS recrutador (\n"
                    + "	id integer PRIMARY KEY AUTOINCREMENT,\n"
                    + "	nome text NOT NULL,\n"
                    + "	email text NOT NULL UNIQUE,\n"
                    + "	senha text NOT NULL\n"
                    + " industria text NOT NULL\n"
                    + " decricao text NOT NULL\n"
                    + ");";
            stmt.execute(sql);

            sql = "CREATE TABLE IF NOT EXISTS active_tokens (\n"
                    + "	user_id integer,\n"
                    + "	token text\n"
                    + ");";
            stmt.execute(sql);
*/
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
