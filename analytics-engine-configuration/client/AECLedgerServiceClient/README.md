# Analytics-Result-Publishing - Client



This is the Java library that allows you to interact with <u>Analytics-Result-Publishing Chaincode</u>. We have provided the classic **CRUD** operations (create, read, update, delete) for manage a Analytics Result Publishing object. 

As for the chaincode this library does not maintain in any way this object, in fact the communication between the library and the chaincode takes place through arrays of strings.

#### Library APIs Interface

Below are the operations that our interface allows to perform:

```java
public interface AnalyticsEngineConfiguration {
    
void postConfiguration(String key, String payload) throws JLedgerClientException;
void putConfiguration(String key, String payload) throws JLedgerClientException;
void deleteConfiguration(String key) throws JLedgerClientException;
String getConfiguration(String key) throws JLedgerClientException;

}
```

###### Usage 

**(see the End2EndTest.java class inside the test folder)**

First you need to instantiate a `AnalyticsEngineConfigurationImpl` object, as you will see this class needs 3 input arguments:

1. **config-fabric-network.json** : It is a JSON file to generate the correct communication with the blockchain hosting the chaincode. In this file among the many information we need to write the name and the path where the chaincode is saved, the addresses of the peer, orderer and CA (Certificate Authority) and the name of the user that we operate on the blockchain (in the resource folder you can see an example of config-fabric-network.json)
2. **ca-cert.pem** and **keystore_sk** : these are the certificates that the CA provides to users of the blockchain. For more information see the official documentation ad this [link](https://hyperledger-fabric-ca.readthedocs.io/en/release-1.4/users-guide.html#).

Put all files into `resourse` folder in you Java project and read them as **InputStream** and pass them to the `AnalyticsEngineConfigurationImpl` .

Example:

```java
InputStream config = ClassLoader.getSystemResourceAsStream("./config-fabric-network.json");
InputStream cert = ClassLoader.getSystemResourceAsStream("./ca-cert.pem");
InputStream keystore = ClassLoader.getSystemResourceAsStream("./keystore_sk");

AnalyticsEngineConfigurationImpl aecClient = new AnalyticsEngineConfigurationImpl(config, cert, keystore);
```



The library provides a special API called *doRegisterEvent* to manage events. This method needs two arguments: a string called **eventName** and an object `ChaincodeEventListener`.
For the correct eventName see the README inside <u>Analytics-Result-Publishing - Chaincode</u> fodler. For the second argument you need to instantiate a new ChaincodeEventListener.

Example:

```java
ChaincodeEventListener chaincodeEventListener;
String event;
AnalyticsEngineConfigurationImpl aecClient;

chaincodeEventListener = new ChaincodeEventListener() {
    @Override
	public void received(String handle, BlockEvent blockEvent, ChaincodeEvent 			chaincodeEvent) {
		String payload = new String(chaincodeEvent.getPayload());
		System.out.println("Event from chaincode: " + chaincodeEvent.getEventName() + " 			" + payload);
		}
	};
event = aecClient.doRegisterEvent("SMARTFACTORY_USECASE_EVENT", chaincodeEventListener);
```

