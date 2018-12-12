package model;


public class Data {

    private String key;
    private String payload;

    public Data() {
    }


    public Data(String payload, String key) {
        this.payload = payload;
        this.key = key;
    }


    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
