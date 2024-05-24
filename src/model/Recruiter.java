package model;

public class Recruiter {
    private String name;
    private String email;
    private String password;
    private String industry;
    private String description;

    public Recruiter(String name, String email, String password, String industry, String description) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.industry = industry;
        this.description = description;
    }
    public static String getTableDefinition(){
        return "CREATE TABLE IF NOT EXISTS recrutador (\n"
                + "	id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "	nome text NOT NULL,\n"
                + "	email text NOT NULL UNIQUE,\n"
                + "	senha text NOT NULL,\n"
                + " industria text NOT NULL,\n"
                + " descricao text NOT NULL\n"
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

    public String getIndustry() {
        return this.industry;
    }

    public String getDescription() {
        return this.description;
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

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
