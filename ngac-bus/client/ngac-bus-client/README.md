
# NgacBusClient


## NgacBus Interface

```java
    void post(String key, String payload) throws JLedgerClientException;

    void put(String key, String payload) throws JLedgerClientException;

    void delete(String key) throws JLedgerClientException;

    String get(String key) throws JLedgerClientException;
```

## Usage

In the test folder there are an example of how to use this library.

## Event

The library provides a special API called "doRegisterEvent" to manage events.

This method needs two arguments: a string called **eventName** and an object **ChaincodeEventListener**.

In this use case we have only one _eventName_:

- **ngacBusEvent_EDIT**

Example:

```java
static String ngacBusEvent;
static ChaincodeEventListener chaincodeEventListener;
chaincodeEventListener = new ChaincodeEventListener() {
    @Override
    public void received(String handle, BlockEvent blockEvent, ChaincodeEvent chaincodeEvent) {
        String payload = new String(chaincodeEvent.getPayload());
        System.out.println("Event from chaincode: " +       chaincodeEvent.getEventName() + " " + payload);
                }
            };
ngacBusEvent = ngacBus.doRegisterEvent("ngacBusEvent_EDIT", chaincodeEventListener);
```

## HLFLedgerClient

In your project create a class that `extends` HLFLedgerClient. <br>
Instantiated a `new HLFLedgerClient` <br>

`HLFLedgerClient hlfledgerclient = new HFLedgerClient`

Now you can view a method to working with chaincode: <br>
  ```doRegisterEvent(String eventName, ChaincodeEventListener chaincodeEventListener)```


### doRegisterEvent

```
 public String doRegisterEvent(String eventName, ChaincodeEventListener chaincodeEventListener) throws JLedgerClientException {
        return ledgerInteractionHelper.getEventHandler().register(eventName, chaincodeEventListener);
    }
 ```
 
 To record an event sent by the chaincode you need to instantiate a **ChaincodeEventListener**. <br>
 
 ``` ChaincodeEventListener chaincodeEventListener = new ChaincodeEventListener() {
                @Override
                public void received(String handle, BlockEvent blockEvent, ChaincodeEvent chaincodeEvent) {
                    String payload = new String(chaincodeEvent.getPayload());
                    System.out.println("Event from chaincode: " + chaincodeEvent.getEventName() + " " + payload);

                }
            };
```
Now you can call **doRegisterEvent** <br>

**Example** <br>

`ngacBusClient.doRegisterEvent("EVENT", chaincodeEventListener);` <br>

**Watch out!!!** The string *eventName* must be the same as the event created in the chaincode!!!
 



