# The Product Order Ledger Service

The OrderCommand class is a data transfer object that is structured as follows: 
```java
public class OrderCommand { 

String firstName; 
String lastName; 
String title; 
String company; 
String email; 
String phone; 
String internalOrder; 
String internalCustomer; 
String order; 
String customer; 
Integer color; 
Integer simulate; 
Integer giveaway; 
Integer priority; 
String type; 
String operation; 
} 
```
### Library API 


 ```java
public interface IndirectProductOrder { 

// issue new product order 
void storeOrderCommand(OrderCommand orderCommand) throws JLedgerClientException; 

// retrieve all existing product orders 

Collection<OrderCommand> getAllOrderCommand() throws JLedgerClientException; 

// retrieve all existing product orders by order id 

Collection<OrderCommand> getOrderCommandByOrder(String order) throws JLedgerClientException; 

// retrieve all existing product orders by customer id 

Collection<OrderCommand> getOrderCommandByCustomer(String customer) throws JLedgerClientException; 

// retrieve existing product order 

OrderCommand getOrderCommand(String order, String customer) throws JLedgerClientException; 

} 
```
### Usage

In the test folder there are an example of how to use this library.

First you need to instantiate a `IndirectProductOrderImpl` object and pass to the constructor 3 file read as **InputStream**:

- config-fabric-network.json
- ca-cert.pem
- keystore

```java
InputStream config = ClassLoader.getSystemResourceAsStream("./config-fabric-network.json");
InputStream cert = ClassLoader.getSystemResourceAsStream("./ca-cert.pem");
InputStream keystore = ClassLoader.getSystemResourceAsStream("./keystore");
IndirectProductOrderImpl client = new IndirectProductOrderImpl(config, cert, keystore); ```

The library provides a special API called `doRegisterEvent` to manage events. <br>
This method needs two arguments: a string called eventName and an object ChaincodeEventListener.<br>

<br>
For the second argument you need to instantiate a new ChaincodeEventListener.

Example:
```java
 ChaincodeEventListener chaincodeEventListener;
 String Event;
 IndirectProductOrderImpl client;
    
chaincodeEventListener = new ChaincodeEventListener() {
    @Override
    public void received(String handle, BlockEvent blockEvent, ChaincodeEvent chaincodeEvent) {
        String payload = new String(chaincodeEvent.getPayload());
        System.out.println("Event from chaincode: " + chaincodeEvent.getEventName() + " " + payload);
        }
    };
event = client.doRegisterEvent("<event_name>", chaincodeEventListener);
```
