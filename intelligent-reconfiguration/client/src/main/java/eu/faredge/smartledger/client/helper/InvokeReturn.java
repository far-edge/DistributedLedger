package eu.faredge.smartledger.client.helper;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.hyperledger.fabric.sdk.BlockEvent;

import java.util.concurrent.CompletableFuture;

public class InvokeReturn {

    private CompletableFuture<BlockEvent.TransactionEvent> completableFuture;
    private String payload;
    private JsonParser parser = new JsonParser();

    public CompletableFuture<BlockEvent.TransactionEvent> getCompletableFuture() {
        return completableFuture;
    }

    public void setCompletableFuture(CompletableFuture<BlockEvent.TransactionEvent> completableFuture) {
        this.completableFuture = completableFuture;
    }

    public InvokeReturn(CompletableFuture<BlockEvent.TransactionEvent> completableFuture, String payload) {
        this.completableFuture = completableFuture;
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }

    public String getId() {
        JsonObject record = parser.parse(getPayload()).getAsJsonObject();
        String id = record.get("id").getAsString();
        return id;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
