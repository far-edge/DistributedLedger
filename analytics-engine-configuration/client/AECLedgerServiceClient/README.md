# Analytics-Result-Publishing-Ledger-Service - Client

### APIs
```java

public interface AnalyticsEngineConfiguration {


    void postConfiguration(String key, String payload) throws JLedgerClientException;

    void putConfiguration(String key, String payload) throws JLedgerClientException;

    void deleteConfiguration(String key) throws JLedgerClientException;

    String getConfiguration(String key) throws JLedgerClientException;
}
```

For the event mechanism we use a specific event name:

-  **POST** and **PUT** `eventName := "AECLedgerService_EDIT"`
-  **GET** `eventName := "AECLedgerService_GET"`
-  **DELETE** `eventName := "AECLedgerService_DELETE"`
