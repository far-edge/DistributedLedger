package main

import (
	"encoding/json"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

//                ______________________
//
//                EdgeGateways API
//                ______________________

func (t *DistributedDataAnalyticsWorkflow) discoverEdgeGateways(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	logger.Info("discoverEdgeGateways() start...\n")
	var qString string
	if len(args) != 4 {
		logger.Error("discoverEdgeGateways ERROR: Expect exactly 4 args!! \n")
		return shim.Error("discoverEdgeGateways ERROR: Expect exatcly 4 args!! ")
	}
	id := args[0]
	name := args[1]
	namespace := args[2]
	macAddress := args[3]

	queryString :=
		OPEN_BRACKET +
			SELECTOR +
			OPEN_BRACKET

	if len(id) > 0 {
		qString = addParameterToCouchDBQuery(queryString, "id", id)
	}
	if len(name) > 0 {
		qString = addParameterToCouchDBQuery(queryString, "name", name)
	}
	if len(namespace) > 0 {
		qString = addParameterToCouchDBQuery(queryString, "namespace", namespace)
	}
	if len(macAddress) > 0 {
		qString = addParameterToCouchDBQuery(queryString, "macAddress", macAddress)
	}
	qString += COMMA +
		addTypeToCouchDBQuery("type", EDGE_GATEWAY) +
		CLOSE_BRACKET +
		CLOSE_BRACKET

	buffer, err := getQueryResultForQueryString(stub, qString)
	if err != nil {
		return shim.Error(err.Error())
	}
	logger.Info("Discover edgeGatewayInstances Result", string(buffer))
	return shim.Success(buffer)
}

func (t *DistributedDataAnalyticsWorkflow) editEdgeGateway(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	var edgeGateway EdgeGateway

	logger.Info("editEdgeGateways() start...\n")

	if len(args) != 5 {
		logger.Error("editEdgeGateway() ERROR: Expect exactly 5 args!!\n")
		return shim.Error("editEdgeGateway() ERROR: Expect exactly 5 args!")
	}

	for i := range args {
		if len(args[i]) == 0 {
			logger.Error("editEdgeGateway() ERROR: empty args!\n")
			return shim.Error("editEdgeGateway() ERROR: empty args!")
		}
	}
	edgeGateway.Id = args[0]
	edgeGateway.Name = args[1]
	edgeGateway.Namespace = args[2]
	edgeGateway.MacAddress = args[3]
	edgeGateway.Payload = args[4]
	edgeGateway.Type = EDGE_GATEWAY

	edgeKey, err := getEdgeKey(stub, edgeGateway.Id)
	if err != nil {
		logger.Error("CreateCompositeKey() ERROR\n")
		return shim.Error(err.Error())
	}

	edgeGatewayByte, err := json.Marshal(&edgeGateway)
	if err != nil {
		logger.Error("json.Marshal() ERROR!\n")
		return shim.Error(err.Error())
	}

	err = stub.PutState(edgeKey, edgeGatewayByte)
	if err != nil {
		logger.Error("PutState() ERROR!\n")
		return shim.Error(err.Error())
	}

	err = setEdgeEvent(stub, edgeGatewayByte)
	if err != nil {
		logger.Error("SetEvent() ERROR!\n")
		return shim.Error(err.Error())
	}

	logger.Debug("editEdgeGateways EVENT: payload= ", string(edgeGatewayByte))
	return shim.Success(nil)
}

func (t *DistributedDataAnalyticsWorkflow) getEdgeGateway(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	logger.Info("getEdgeGateway start...\n")

	if len(args) != 1 {
		logger.Error("getEdgeGateway() ERROR: Expect exactly 1 arg!!\n")
		return shim.Error("getEdgeGateway() ERROR: Expect exactly 1 arg!")
	}
	edgeKey, err := getEdgeKey(stub, args[0])
	if err != nil {
		logger.Error("CreateCompositeKey() ERROR\n")
		return shim.Error("CreateCompositeKey() ERROR")
	}
	edgeGatewayAnalitycs, err1 := stub.GetState(edgeKey)
	if err1 != nil {
		logger.Error("GetState() ERROR\n")
		return shim.Error("GetState() ERROR")
	}
	logger.Info("EdgeGateway retrived: ", string(edgeGatewayAnalitycs))
	return shim.Success(edgeGatewayAnalitycs)
}

func (t *DistributedDataAnalyticsWorkflow) deleteEdgeGateway(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	logger.Info("deleteEdgeGateway() start...\n")

	if len(args) != 1 {
		logger.Error("deleteEdgeGateway() ERROR: Expect exactly 1 arg!!\n")
		return shim.Error("deleteEdgeGateway() ERROR: Expect exactly 1 arg!")
	}

	edgeGatewayKey, err := getEdgeKey(stub, args[0])
	if err != nil {
		logger.Error("CreateCompositeKey() ERROR\n")
		return shim.Error(err.Error())
	}

	err = stub.DelState(edgeGatewayKey)
	if err != nil {
		logger.Error("DelState() ERROR\n")
		return shim.Error(err.Error())
	}
	err = setEdgeEvent(stub, []byte("stub.Delete() success EdgeGateway with key: "+edgeGatewayKey))
	if err != nil {
		logger.Error("SetEvent() ERROR\n")
		return shim.Error(err.Error())
	}
	logger.Info("deleteEdgeGateway() completed!\n")
	return shim.Success(nil)
}
