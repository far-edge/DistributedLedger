# Analytics-Result-Publishing-Ledger-Service - Client

### APIs
```java

public interface AnalyticsResultsPublishing {


    void postResult(String key, String payload) throws JLedgerClientException;

    void putResult(String key, String payload) throws JLedgerClientException;

    void deleteResult(String key) throws JLedgerClientException;

    String getResult(String key) throws JLedgerClientException;
}
```

For the event mechanism we use a specific event name:

-  **POST** and **PUT** `eventName := "ARPLedgerService_EDIT"`
-  **GET** `eventName := "ARPLedgerService_GET"`
-  **DELETE** `eventName := "ARPLedgerService_DELETE"`
