package model;

public class Voter {
    private int voterId;
    private String fullName;
    private String nationalId;
    private String password; // hashed
    private String idCardPath;
    private String voterCardPath;
    private boolean hasVoted;

    // Constructors
    public Voter() {}

    public Voter(int voterId, String fullName, String nationalId, String password,
                 String idCardPath, String voterCardPath, boolean hasVoted) {
        this.voterId = voterId;
        this.fullName = fullName;
        this.nationalId = nationalId;
        this.password = password;
        this.idCardPath = idCardPath;
        this.voterCardPath = voterCardPath;
        this.hasVoted = hasVoted;
    }

    // Getters and Setters
    public int getVoterId() {
        return voterId;
    }

    public void setVoterId(int voterId) {
        this.voterId = voterId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNationalId() {
        return nationalId;
    }

    public void setNationalId(String nationalId) {
        this.nationalId = nationalId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIdCardPath() {
        return idCardPath;
    }

    public void setIdCardPath(String idCardPath) {
        this.idCardPath = idCardPath;
    }

    public String getVoterCardPath() {
        return voterCardPath;
    }

    public void setVoterCardPath(String voterCardPath) {
        this.voterCardPath = voterCardPath;
    }

    public boolean isHasVoted() {
        return hasVoted;
    }

    public void setHasVoted(boolean hasVoted) {
        this.hasVoted = hasVoted;
    }

    @Override
    public String toString() {
        return "Voter{" +
                "voterId=" + voterId +
                ", fullName='" + fullName + '\'' +
                ", nationalId='" + nationalId + '\'' +
                ", hasVoted=" + hasVoted +
                '}';
    }
}
