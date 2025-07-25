package model;

public class Candidate {
    private String fullName;
    private String nationalId;
    private String party;
    private String position;
    private String manifesto;
    private String password;
    private String nationalIdPath;
    private String clearanceCertificatePath;
    private String academicCertPath;
    private String photoPath;

    // Default constructor (important for forms, modals, etc.)
    public Candidate() {}

    // Full constructor
    public Candidate(String fullName, String nationalId, String party, String position,
                     String manifesto, String password,
                     String nationalIdPath, String clearanceCertificatePath,
                     String academicCertPath, String photoPath) {
        this.fullName = fullName;
        this.nationalId = nationalId;
        this.party = party;
        this.position = position;
        this.manifesto = manifesto;
        this.password = password;
        this.nationalIdPath = nationalIdPath;
        this.clearanceCertificatePath = clearanceCertificatePath;
        this.academicCertPath = academicCertPath;
        this.photoPath = photoPath;
    }

    // Getters
    public String getFullName() { return fullName; }
    public String getNationalId() { return nationalId; }
    public String getParty() { return party; }
    public String getPosition() { return position; }
    public String getManifesto() { return manifesto; }
    public String getPassword() { return password; }
    public String getNationalIdPath() { return nationalIdPath; }
    public String getClearanceCertificatePath() { return clearanceCertificatePath; }
    public String getAcademicCertPath() { return academicCertPath; }
    public String getPhotoPath() { return photoPath; }

    // Setters
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public void setParty(String party) { this.party = party; }
    public void setPosition(String position) { this.position = position; }
    public void setManifesto(String manifesto) { this.manifesto = manifesto; }
    public void setPassword(String password) { this.password = password; }
    public void setNationalIdPath(String nationalIdPath) { this.nationalIdPath = nationalIdPath; }
    public void setClearanceCertificatePath(String clearanceCertificatePath) { this.clearanceCertificatePath = clearanceCertificatePath; }
    public void setAcademicCertPath(String academicCertPath) { this.academicCertPath = academicCertPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    // ToString - useful for display in ListView or ComboBox
    @Override
    public String toString() {
        return fullName + " (" + position + ")";
    }
}
