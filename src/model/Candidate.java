package model;

public class Candidate {
    private int candidateId;
    private String fullName;
    private String nationalId;
    private String password;  // NOT passwordHash
    private String party;
    private String manifesto;
    private String position;
    private boolean approved;
    private String nationalIdPath;
    private String clearanceCertificatePath;
    private String academicCertPath;
    private String photoPath;

    // Getters and Setters
    public int getCandidateId() { return candidateId; }
    public void setCandidateId(int candidateId) { this.candidateId = candidateId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getParty() { return party; }
    public void setParty(String party) { this.party = party; }

    public String getManifesto() { return manifesto; }
    public void setManifesto(String manifesto) { this.manifesto = manifesto; }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public String getNationalIdPath() { return nationalIdPath; }
    public void setNationalIdPath(String nationalIdPath) { this.nationalIdPath = nationalIdPath; }

    public String getClearanceCertificatePath() { return clearanceCertificatePath; }
    public void setClearanceCertificatePath(String clearanceCertificatePath) { this.clearanceCertificatePath = clearanceCertificatePath; }

    public String getAcademicCertPath() { return academicCertPath; }
    public void setAcademicCertPath(String academicCertPath) { this.academicCertPath = academicCertPath; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
}
