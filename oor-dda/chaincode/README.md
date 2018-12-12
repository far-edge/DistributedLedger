#Opaque Object Registry - Distributed Data Analytics Chaincode

## Open APIs

### Analytics

* POST @ /analytics-instances   =  **editAnalyticsInstance**
* PUT @ /analytics-instances/:id/specification = **editAnalyticsInstance**
* DELETE @ /analytics-instances/:id = **delateAnalyticsInstances**
* GET @ /analytics-instances/:id = **getAnalyticsInstanceById**
* GET @ /analytics-instances/:id/specification = **getAnalyticsInstancesBySpecification**
* POST @ /analytics-instances/discover = **discoverAnalyticsInstances**

### Data Sources

* POST @ /data-sources = **createDataSource**
* DELETE @ /data-sources/:id = **deleteDataSource**
* POST  @ /data-sources/discover = **discoverDataSources**

### Edge Gateways

* POST @ /edge-gateways = **editEdgeGateway**
* PUT @ /edge-gateways/:id = **editEdgeGateway**
* GET @ /edge-gateways/:id = **getEdgeGateway**
* DELETE @ /edge-gateways/:id = **deleteEdgeGateway**
* POST @ /edge-gateways/discover = **discoverEdgeGateways**
