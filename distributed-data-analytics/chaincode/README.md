# DDA Open API  

## Analytics

* POST @ /analytics-instances   =  **createAnalyticsInstance**
* PUT @ /analytics-instances/:id/specification = **updateAnalyticsInstance**
* DELETE @ /analytics-instances/:id = **delateAnalyticsInstances**
* GET @ /analytics-instances/:id = **getAnalyticsInstanceById**
* GET @ /analytics-instances/:id/specification = **getAnalyticsInstancesBySpecification**
* POST @ /analytics-instances/discover = **discoverAnalyticsInstances**

## Data Sources

* POST @ /data-sources = **createDataSource**
* DELETE @ /data-sources/:id = **deleteDataSource**
* POST  @ /data-sources/discover = **discoverDataSources**

## Edge Gateways

* POST @ /edge-gateways = **createEdgeGateway**
* PUT @ /edge-gateways/:id = **updateEdgeGateway**
* GET @ /edge-gateways/:id = **getEdgeGateway**
* DELETE @ /edge-gateways/:id = **deleteEdgeGateway**
* POST @ /edge-gateways/discover = **discoverEdgeGateways**
* 

```java
    
//Analytics


    void createAnalyticsInstance(String id, String name, String edgeGatewayReferenceID, String payload) throws JLedgerClientException;

    void updateAnalyticsInstance(String id, String name, String edgeGatewayReferenceID, String payload) throws JLedgerClientException;

    void deleteAnalyticsInstance(String id, String edgeGatewayReferenceID) throws JLedgerClientException;

    String getAnalyticsInstanceById(String id) throws JLedgerClientException;

    String getAnalyticsInstancesBySpecification(String id, String edgeGatewayReferenceID) throws JLedgerClientException;

    String discoverAnalyticsInstances(String id, String name, String edgeGatewayReferenceID) throws JLedgerClientException;

//DataSources


    void createDataSource(String id, String name, String edgeGatewayReferenceID, String dataSourceDefinitionReferenceID, String payload) throws JLedgerClientException;

    void deleteDataSource(String id, String edgeGatewayReferenceID) throws JLedgerClientException;

    String discoverDataSources(String id, String name, String edgeGatewayReferenceID, String dataSourceDefinitionReferenceID) throws JLedgerClientException;


//EdgeGateways


    void createEdgeGateway(String id, String name, String namespace, String macAddress, String payload) throws JLedgerClientException;

    void updateEdgeGateway(String id, String name, String namespace, String macAddress, String payload) throws JLedgerClientException;

    void deleteEdgeGateway(String id) throws JLedgerClientException;

    String getEdgeGateway(String id) throws JLedgerClientException;

    String discoverEdgeGateways(String id, String name, String namespace, String macAddress) throws JLedgerClientException;

```
