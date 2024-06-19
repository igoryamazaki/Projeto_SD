package model;

public class Job {
    private String skill;
    private String experience;
    private int available;
    private int searchable;

    public Job(String skill, String experience, int available, int searchable) {
        this.skill = skill;
        this.experience = experience;
        this.available = available;
        this.searchable = searchable;
    }

    public static String getTableDefinition() {
        return "CREATE TABLE IF NOT EXISTS vagas (\n"
                + "	id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "	habilidade text NOT NULL,\n"
                + "	experiencia text NOT NULL,\n"
                + "	disponivel integer NOT NULL,\n"
                + "	divulgavel integer NOT NULL\n"
                + ");";
    }
}
