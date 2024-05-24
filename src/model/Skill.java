package model;

public class Skill {
    private String skillName;

    public Skill(String skillName) {
        this.skillName = skillName;
    }

    public static String getTableDefinition() {
        return "CREATE TABLE IF NOT EXISTS habilidades (\n"
                + "	id integer PRIMARY KEY AUTOINCREMENT,\n"
                + "	habilidade text NOT NULL,\n"
                + "	experiencia text NOT NULL\n"
                + ");";
    }

    public String getSkillName() {
        return this.skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }
}

