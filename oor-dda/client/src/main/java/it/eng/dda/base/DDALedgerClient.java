package it.eng.dda.base;

import it.eng.jledgerclient.exception.JLedgerClientException;

public interface DDALedgerClient {
/*
    	if function == "createAnalyticsInstance" {
        return t.editAnalyticsInstance(stub, args)
    } else if function == "updateAnalyticsInstance" {
        return t.editAnalyticsInstance(stub, args)
    } else if function == "deleteAnalyticsInstance" {
        return t.deleteAnalyticsInstance(stub, args)
    } else if function == "getAnalyticsInstanceById" {
        return t.getAnalyticsInstanceById(stub, args)
    } else if function == "getAnalyticsInstancesBySpecification" {
        return t.getAnalyticsInstancesBySpecification(stub, args)
    } else if function == "discoverAnalyticsInstances" {
        return t.discoverAnalyticsInstances(stub, args)

    */

    void createAnalyticsInstance(String id, String name, String edgeGatewayReferenceID, String payload) throws JLedgerClientException;

    void updateAnalyticsInstance(String id, String name, String edgeGatewayReferenceID, String payload) throws JLedgerClientException;

    void deleteAnalyticsInstance(String id, String edgeGatewayReferenceID) throws JLedgerClientException;

    String getAnalyticsInstanceById(String id) throws JLedgerClientException;

    String getAnalyticsInstancesBySpecification(String id, String edgeGatewayReferenceID) throws JLedgerClientException;

    String discoverAnalyticsInstances(String id, String name, String edgeGatewayReferenceID) throws JLedgerClientException;


/*
        //DataSources
    } else if function == "createDataSource" {
        return t.createDataSource(stub, args)
    } else if function == "deleteDataSource" {
        return t.deleteDataSource(stub, args)
    } else if function == "discoverDataSources" {
        return t.discoverDataSources(stub, args)
*/

    void createDataSource(String id, String name, String edgeGatewayReferenceID, String dataSourceDefinitionReferenceID, String payload) throws JLedgerClientException;

    void deleteDataSource(String id, String edgeGatewayReferenceID) throws JLedgerClientException;

    String discoverDataSources(String id, String name, String edgeGatewayReferenceID, String dataSourceDefinitionReferenceID) throws JLedgerClientException;


/*
        //EdgeGateways
    } else if function == "createEdgeGateway" {
        return t.editEdgeGateway(stub, args)
    } else if function == "updateEdgeGateway" {
        return t.editEdgeGateway(stub, args)
    } else if function == "discoverEdgeGateways" {
        return t.discoverEdgeGateways(stub, args)
    } else if function == "getEdgeGateway" {
        return t.getEdgeGateway(stub, args)
    } else if function == "deleteEdgeGateway" {
        return t.deleteEdgeGateway(stub, args)
    } else {
        return shim.Error("Invalid invoke function name")
    }*/

    void createEdgeGateway(String id, String name, String namespace, String macAddress, String payload) throws JLedgerClientException;

    void updateEdgeGateway(String id, String name, String namespace, String macAddress, String payload) throws JLedgerClientException;

    void deleteEdgeGateway(String id) throws JLedgerClientException;

    String getEdgeGateway(String id) throws JLedgerClientException;

    String discoverEdgeGateways(String id, String name, String namespace, String macAddress) throws JLedgerClientException;

}
