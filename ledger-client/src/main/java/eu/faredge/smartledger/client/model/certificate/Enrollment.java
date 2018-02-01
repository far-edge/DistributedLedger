package eu.faredge.smartledger.client.model.certificate;


public class Enrollment {

    private String signingIdentity;
    private Identity identity;


    public Enrollment() {

    }

    public Enrollment(String signingIdentity, Identity identity) {
        this.signingIdentity = signingIdentity;
        this.identity = identity;
    }

    public String getSigningIdentity() {
        return this.signingIdentity;
    }

    public void setSigningIdentity(String signingIdentity) {
        this.signingIdentity = signingIdentity;
    }

    public Identity getIdentity() {
        return this.identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }


}
