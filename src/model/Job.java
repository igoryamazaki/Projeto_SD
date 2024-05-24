package model;

public class Job {
    private String skill;
    private String experience;

    public Job(String skill, String experience) {
        this.skill = skill;
        this.experience = experience;
    }

    public static String getTableDefinition() {
        return "CREATE TABLE IF NOT EXISTS vagas (\n"
                + "	id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "	habilidade text NOT NULL,\n"
                + "	experiencia text NOT NULL\n"
                + ");";
    }
}
