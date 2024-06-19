package model;

public class CandidateJob {
    private int candidateId;
    private int jobId;
    private int vagaId;
    private int habilidadeId;

    public CandidateJob(int candidateId, int recrutadorId, int vagaId) {
        this.candidateId = candidateId;
        this.jobId = recrutadorId;
        this.vagaId = vagaId;

    }

    public static String getTableDefinition() {
        return "CREATE TABLE IF NOT EXISTS candidato_vagas (\n"
                + " recrutador_id INTEGER NOT NULL,\n"
                + " candidato_id INTEGER NOT NULL,\n"
                + " FOREIGN KEY (recrutador_id) REFERENCES recrutador(id),\n"
                + " FOREIGN KEY (candidato_id) REFERENCES candidatos(id)\n"
                + ");";
    }
}
