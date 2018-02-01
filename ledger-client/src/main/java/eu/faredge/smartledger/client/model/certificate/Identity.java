package eu.faredge.smartledger.client.model.certificate;


public class Identity {

    private String certificate;


    public Identity() {

    }

    public Identity(String certificate) {
        this.certificate = certificate;
    }

    public String getCertificate() {
        return this.certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }


}
