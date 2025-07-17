package model;

public class Voter {
    private int voterId;
    private String nationalId;
    private String fullName;
    private String password; // hashed
    private String idCardPath;
    private String voterCardPath;
    private boolean hasVoted;

    public Voter(String nationalId, String fullName, String password,
                 String idCardPath, String voterCardPath) {
        this.nationalId = nationalId;
        this.fullName = fullName;
        this.password = password;
        this.idCardPath = idCardPath;
        this.voterCardPath = voterCardPath;
        this.hasVoted = false;
    }

    // Getters and setters
    public String getNationalId() { return nationalId; }
    public String getFullName() { return fullName; }
    public String getPassword() { return password; }
    public String getIdCardPath() { return idCardPath; }
    public String getVoterCardPath() { return voterCardPath; }
    public boolean isHasVoted() { return hasVoted; }
}
