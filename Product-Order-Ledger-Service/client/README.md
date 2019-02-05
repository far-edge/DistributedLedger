# The Product Order Ledger Service


## Library API 


 ```java
void storeOrderCommand(OrderCommand orderCommand) throws JLedgerClientException;

Collection<OrderCommand> getAllOrderCommand() throws JLedgerClientException;

Collection<OrderCommand> getOrderCommandByOrder(String order) throws JLedgerClientException;

Collection<OrderCommand> getOrderCommandByCustomer(String customer) throws JLedgerClientException;

OrderCommand getOrderCommand(String order, String customer) throws JLedgerClientException;
```
## Usage

In the test folder there are an example of how to use this library.

First you need to instantiate a `SmartFactoryLedgerClientImpl` object and pass to the constructor 3 file read as **InputStream**:

- config-fabric-network.json
- ca-cert.pem
- keystore

```java
InputStream config = ClassLoader.getSystemResourceAsStream("./config-fabric-network.json");
InputStream cert = ClassLoader.getSystemResourceAsStream("./ca-cert.pem");
InputStream keystore = ClassLoader.getSystemResourceAsStream("./keystore");
smartFactoryLedgerClient = new SmartFactoryLedgerClientImpl(config, cert, keystore);
```

The library provides a special API called `doRegisterEvent` to manage events. <br>
This method needs two arguments: a string called eventName and an object ChaincodeEventListener.<br>
The eventName in this usecase is `SMARTFACTORY_USECASE_EVENT`.
<br>
For the second argument you need to instantiate a new ChaincodeEventListener.

Example:
```java
 ChaincodeEventListener chaincodeEventListener;
 String smartfactoryUsecaseEvent;
 SmartFactoryLedgerClientImpl smartFactoryLedgerClient;
    
chaincodeEventListener = new ChaincodeEventListener() {
    @Override
    public void received(String handle, BlockEvent blockEvent, ChaincodeEvent chaincodeEvent) {
        String payload = new String(chaincodeEvent.getPayload());
        System.out.println("Event from chaincode: " + chaincodeEvent.getEventName() + " " + payload);
        }
    };
smartfactoryUsecaseEvent = smartFactoryLedgerClient.doRegisterEvent("SMARTFACTORY_USECASE_EVENT", chaincodeEventListener);
```
