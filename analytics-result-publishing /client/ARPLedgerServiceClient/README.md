# Analytics-Result-Publishing-Ledger-Service - Client

### APIs
```java

public interface AnalyticsResultsPublishing {


    void post(String key, String payload) throws JLedgerClientException;

    void put(String key, String payload) throws JLedgerClientException;

    void delete(String key) throws JLedgerClientException;

    String get(String key) throws JLedgerClientException;
}
```

For the event mechanism we use a specific event name:

-  **POST** and **PUT** `eventName := "ARPLedgerService_EDIT"`
-  **GET** `eventName := "ARPLedgerService_GET"`
-  **DELETE** `eventName := "ARPLedgerService_DELETE"`
