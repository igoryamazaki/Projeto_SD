package model;

public class CandidateSkill {
    private int candidateId;
    private int skillId;

    public CandidateSkill(int candidateId, int skillId) {
        this.candidateId = candidateId;
        this.skillId = skillId;
    }

    public static String getTableDefinition() {
        return "CREATE TABLE IF NOT EXISTS candidato_habilidades (\n"
                + "	candidato_id integer NOT NULL,\n"
                + "	habilidade_id integer NOT NULL,\n"
                + "	PRIMARY KEY (candidato_id, habilidade_id),\n"
                + "	FOREIGN KEY(candidato_id) REFERENCES candidatos(id),\n"
                + "	FOREIGN KEY(habilidade_id) REFERENCES habilidades(id)\n"
                + ");";
    }

    public int getCandidateId() {
        return this.candidateId;
    }

    public int getSkillId() {
        return this.skillId;
    }

    public void setCandidateId(int candidateId) {
        this.candidateId = candidateId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }
}

