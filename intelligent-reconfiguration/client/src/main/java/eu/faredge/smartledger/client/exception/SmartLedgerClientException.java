package eu.faredge.smartledger.client.exception;

//TODO Modify Test to use this exception
// create some status codes to manage events coming from Fabric, but even possible event happening
// like the absence of a Fabric instance at the moment.
public class SmartLedgerClientException extends Exception {
    public SmartLedgerClientException() {
        super();
    }

    public SmartLedgerClientException(String message) {
        super(message);
    }

    public SmartLedgerClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public SmartLedgerClientException(Throwable cause) {
        super(cause);
    }

    protected SmartLedgerClientException(String message, Throwable cause, boolean enableSuppression, boolean
            writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
