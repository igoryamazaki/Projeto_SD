package model;

public class Candidate {
    private String name;
    private String email;
    private String password;
    // Outros campos relevantes...

    public Candidate(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }
    public static String getTableDefinition() {
        return "CREATE TABLE IF NOT EXISTS candidatos (\n"
                + "	id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "	nome text NOT NULL,\n"
                + "	email text NOT NULL UNIQUE,\n"
                + "	senha text NOT NULL\n"
                + ");";
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        return this.email;
    }

    public String getPassword() {
        return this.password;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
