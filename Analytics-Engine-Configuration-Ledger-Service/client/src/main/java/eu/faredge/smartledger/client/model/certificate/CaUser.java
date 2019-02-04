package eu.faredge.smartledger.client.model.certificate;

import java.util.List;

public class CaUser {

    private Enrollment enrollment;
    private String mspid;
    private List<String> roles;
    private String name;
    private String affiliation;
    private String enrollmentSecret;

    public CaUser() {

    }

    public CaUser(Enrollment enrollment, String mspid, List<String> roles, String name, String affiliation, String
            enrollmentSecret) {
        this.enrollment = enrollment;
        this.mspid = mspid;
        this.roles = roles;
        this.name = name;
        this.affiliation = affiliation;
        this.enrollmentSecret = enrollmentSecret;
    }

    public Enrollment getEnrollment() {
        return this.enrollment;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public String getMspid() {
        return this.mspid;
    }

    public void setMspid(String mspid) {
        this.mspid = mspid;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAffiliation() {
        return this.affiliation;
    }

    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }

    public String getEnrollmentSecret() {
        return this.enrollmentSecret;
    }

    public void setEnrollmentSecret(String enrollmentSecret) {
        this.enrollmentSecret = enrollmentSecret;
    }


}
