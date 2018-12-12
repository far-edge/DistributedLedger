# Opaque Object Registry - Distributed Data Analytics

## Analytics

- POST @ /analytics-instances = **createAnalyticsInstance**
- PUT @ /analytics-instances/:id/specification = **updateAnalyticsInstance**
- DELETE @ /analytics-instances/:id = **delateAnalyticsInstances**
- GET @ /analytics-instances/:id = **getAnalyticsInstanceById**
- GET @ /analytics-instances/:id/specification = **getAnalyticsInstancesBySpecification**
- POST @ /analytics-instances/discover = **discoverAnalyticsInstances**

## Data Sources

- POST @ /data-sources = **createDataSource**
- DELETE @ /data-sources/:id = **deleteDataSource**
- POST @ /data-sources/discover = **discoverDataSources**

## Edge Gateways

- POST @ /edge-gateways = **createEdgeGateway**
- PUT @ /edge-gateways/:id = **updateEdgeGateway**
- GET @ /edge-gateways/:id = **getEdgeGateway**
- DELETE @ /edge-gateways/:id = **deleteEdgeGateway**
- POST @ /edge-gateways/discover = **discoverEdgeGateways**

## DDALedgerClient Interface

```java
    void createAnalyticsInstance(String id, String name, String edgeGatewayReferenceID, String payload) throws JLedgerClientException;
    void updateAnalyticsInstance(String id, String name, String edgeGatewayReferenceID, String payload) throws JLedgerClientException;
    void deleteAnalyticsInstance(String id, String edgeGatewayReferenceID) throws JLedgerClientException;
    String getAnalyticsInstanceById(String id) throws JLedgerClientException;
    String getAnalyticsInstancesBySpecification(String id, String edgeGatewayReferenceID) throws JLedgerClientException;
    String discoverAnalyticsInstances(String id, String name, String edgeGatewayReferenceID) throws JLedgerClientException;

    void createDataSource(String id, String name, String edgeGatewayReferenceID, String dataSourceDefinitionReferenceID, String payload) throws JLedgerClientException;
    void deleteDataSource(String id, String edgeGatewayReferenceID) throws JLedgerClientException;
    String discoverDataSources(String id, String name, String edgeGatewayReferenceID, String dataSourceDefinitionReferenceID) throws JLedgerClientException;

    void createEdgeGateway(String id, String name, String namespace, String macAddress, String payload) throws JLedgerClientException;
    void updateEdgeGateway(String id, String name, String namespace, String macAddress, String payload) throws JLedgerClientException;
    void deleteEdgeGateway(String id) throws JLedgerClientException;
    String getEdgeGateway(String id) throws JLedgerClientException;
    String discoverEdgeGateways(String id, String name, String namespace, String macAddress) throws JLedgerClientException;
```

## Usage

In the test folder there are an example of how to use this library.

First you need to instantiate a `DDALedgerClientImpl` object and pass to the constructor 3 file read as **InputStream**:

- config-fabric-network.json
- ca-cert.pem
- keystore

```java
InputStream config = ClassLoader.getSystemResourceAsStream("./config-fabric-network.json");
InputStream cert = ClassLoader.getSystemResourceAsStream("./ca-cert.pem");
InputStream keystore = ClassLoader.getSystemResourceAsStream("./keystore");
ddaLedgerClient = new DDALedgerClientImpl(config, cert, keystore);
```

### Event

The library provides a special API called "doRegisterEvent" to manage events.

This method needs two arguments: a string called **eventName** and an object **ChaincodeEventListener**.

In this use case we have 3 different _eventName_:

- **FE_Analytics_Instances_Event**
- **FE_EgeGateway_Event**
- **FE_DataSource_Event**

Example:

```java
static String DDAEvent1;
static String DDAEvent2;
static String DDAEvent3;
static ChaincodeEventListener chaincodeEventListener;
chaincodeEventListener = new ChaincodeEventListener() {
    @Override
    public void received(String handle, BlockEvent blockEvent, ChaincodeEvent chaincodeEvent) {
        String payload = new String(chaincodeEvent.getPayload());
        System.out.println("Event from chaincode: " +       chaincodeEvent.getEventName() + " " + payload);
                }
            };
DDAEvent1 = ddaLedgerClient.doRegisterEvent("FE_Analytics_Instances_Event", chaincodeEventListener);
DDAEvent2 = ddaLedgerClient.doRegisterEvent("FE_DataSource_Event", chaincodeEventListener);
DDAEvent3 = ddaLedgerClient.doRegisterEvent("FE_EgeGateway_Event", chaincodeEventListener);
```
