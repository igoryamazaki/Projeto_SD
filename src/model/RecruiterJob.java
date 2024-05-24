package model;

public class RecruiterJob {
    private int recruiterId;
    private int jobId;

    public RecruiterJob(int recruiterId, int jobId) {
        this.recruiterId = recruiterId;
        this.jobId = jobId;
    }

    public static String getTableDefinition() {
        return "CREATE TABLE IF NOT EXISTS recrutador_vagas (\n"
                + "	recrutador_id integer NOT NULL,\n"
                + "	vaga_id integer NOT NULL,\n"
                + "	PRIMARY KEY (recrutador_id, vaga_id),\n"
                + "	FOREIGN KEY(recrutador_id) REFERENCES recrutador(id),\n"
                + "	FOREIGN KEY(vaga_id) REFERENCES vagas(id)\n"
                + ");";
    }
}
