package main

import (
	"encoding/json"
	"errors"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

//                ______________________
//
//                AnalitycsInstances API
//                ______________________

func (t *DistributedDataAnalyticsWorkflow) discoverAnalyticsInstances(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	logger.Info("discoverAnalyticsInstances() start...\n")
	var qString string
	if len(args) != 3 {
		logger.Error("discoverAnalyticsInstances ERROR: Expect exactly 3 args!! \n")
		return shim.Error("discoverAnalyticsInstances ERROR: Expect exatcly 3 args!! ")
	}
	id := args[0]
	name := args[1]
	egid := args[2]

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
	if len(egid) > 0 {
		qString = addParameterToCouchDBQuery(queryString, "edgeGatewayReferenceID", egid)
	}
	qString += COMMA +
		addTypeToCouchDBQuery("type", ANALYTICS) +
		CLOSE_BRACKET +
		CLOSE_BRACKET

	buffer, err := getQueryResultForQueryString(stub, qString)
	if err != nil {
		return shim.Error(err.Error())
	}
	logger.Info("Discover AnalyticsInstances Result", string(buffer))
	return shim.Success(buffer)
}

func goGetAnalyticsInstancesBySpecification(stub shim.ChaincodeStubInterface, args []string) ([]byte, error) {

	logger.Info("getAnalyticsInstancesBySpecification() start...\n")
	if len(args) != 2 {
		logger.Error("getAnalyticsInstancesBySpecification ERROR: Expect exatly 2 args!! \n")
		errDef := errors.New("getAnalyticsInstancesBySpecification ERROR: Expect exatly 2 args!!")
		return nil, errDef
	}
	analyticsKey, err := getAnalyticsKey(stub, args[0], args[1])
	if err != nil {
		logger.Error("CreateCompositeKey() ERROR\n")
		return nil, err
	}
	byteAnalitycs, err1 := stub.GetState(analyticsKey)
	if err1 != nil {
		logger.Error("GetState() ERROR\n")
		return nil, err1
	}
	logger.Info("AnalitycsInstances retrived: ", string(byteAnalitycs))
	return byteAnalitycs, nil
}

func (t *DistributedDataAnalyticsWorkflow) getAnalyticsInstancesBySpecification(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	buffer, err := goGetAnalyticsInstancesBySpecification(stub, args)
	if err != nil {
		logger.Error(err.Error())
		return shim.Error(err.Error())
	}
	return shim.Success(buffer)
}

func (t *DistributedDataAnalyticsWorkflow) getAnalyticsInstanceById(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	buffer, err := goGetAnalyticsInstancesById(stub, args)
	if err != nil {
		logger.Error(err.Error())
		return shim.Error(err.Error())
	}
	return shim.Success(buffer)
}

func goGetAnalyticsInstancesById(stub shim.ChaincodeStubInterface, args []string) ([]byte, error) {

	logger.Info("getAnalyticsInstancesById() start...\n")
	var errDef error

	if len(args) != 1 {
		logger.Error("getAnalyticsInstancesById() ERROR: Expect exactly 1 arg!!\n")
		errDef = errors.New("getAnalyticsInstancesById() ERROR: Expect exactly 1 arg!")
		return nil, errDef
	}

	/* stateQueryIteratorInterface, err := stub.GetStateByPartialCompositeKey("FE_Analytics_Instances", args)
	if err != nil {
		logger.Error("GetStateByPartialCompositeKey() ERROR")
		return nil, err
	} */

	queryString :=
		OPEN_BRACKET +
			SELECTOR +
			OPEN_BRACKET

	qString := addParameterToCouchDBQuery(queryString, "id", args[0])
	qString += CLOSE_BRACKET + CLOSE_BRACKET
	logger.Info(" Query String: ", qString)
	buffer, err := getQueryResultForQueryString(stub, qString)
	if err != nil {
		return nil, err
	}
	logger.Debug("getAnalitycsByID Result: ", string(buffer))
	err = setAnalyticsEvent(stub, buffer)
	if err != nil {
		logger.Error("SetEvent() ERROR!\n")
		return nil, err
	}
	return buffer, nil

	/* var analitycsStrinArray []string

	for stateQueryIteratorInterface.HasNext() {
		analitycsInterface, err := stateQueryIteratorInterface.Next()
		if err != nil {
			logger.Error("stateQueryIteratorInterface.Next() ERROR")
			return nil, err
		}
		logger.Debug("Single Analitycs discover: ", string(analitycsInterface.Value))
		analitycsStrinArray = append(analitycsStrinArray, string(analitycsInterface.Value))
	}
	jsonResp := strings.Join(analitycsStrinArray, "")
	logger.Debug("Query Response: " + jsonResp)

	err = setAnalyticsEvent(stub, []byte(jsonResp))
	if err != nil {
		logger.Error("SetEvent() ERROR!\n")
		return nil, err
	}
	return []byte(jsonResp), nil */
}

func (t *DistributedDataAnalyticsWorkflow) deleteAnalyticsInstance(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	logger.Info("deleteAnalyticsInstance() start...\n")

	if len(args) != 2 {
		logger.Error("deleteAnalyticsInstance() ERROR: Expect exactly 2 args!!\n")
		return shim.Error("deleteAnalyticsInstance() ERROR: Expect exactly 2 args!")
	}

	analyticsKey, err := getAnalyticsKey(stub, args[0], args[1])
	if err != nil {
		logger.Error("CreateCompositeKey() ERROR\n")
		return shim.Error(err.Error())
	}

	err = stub.DelState(analyticsKey)
	if err != nil {
		logger.Error("DelState() ERROR\n")
		return shim.Error(err.Error())
	}
	err = setAnalyticsEvent(stub, []byte("stub.Delete() success AnalyticsInstance with key: "+analyticsKey))
	if err != nil {
		logger.Error("SetEvent() ERROR\n")
		return shim.Error(err.Error())
	}
	logger.Info("deleteAnalyticsInstance completed!")
	return shim.Success(nil)
}

func (t *DistributedDataAnalyticsWorkflow) editAnalyticsInstance(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	var analytics AnalyticsInstances

	logger.Info("editAnalyticsInstances() start...\n")

	if len(args) != 4 {
		logger.Error("editAnalyticsInstances() ERROR: Expect exactly 4 args!!\n")
		return shim.Error("editAnalyticsInstances() ERROR: Expect exactly 4 args!")
	}

	for i := range args {
		if len(args[i]) == 0 {
			logger.Error("editAnalyticsInstances() ERROR: empty args!\n")
			return shim.Error("editAnalyticsInstances() ERROR: empty args!")
		}
	}
	analytics.Id = args[0]
	analytics.Name = args[1]
	analytics.EdgeGatewayReferenceID = args[2]
	analytics.Payload = args[3]
	analytics.Type = ANALYTICS

	analyticsKey, err := getAnalyticsKey(stub, analytics.Id, analytics.EdgeGatewayReferenceID)
	if err != nil {
		logger.Error("CreateCompositeKey() ERROR\n")
		return shim.Error(err.Error())
	}

	analyticsByte, err := json.Marshal(&analytics)
	if err != nil {
		logger.Error("json.Marshal() ERROR!\n")
		return shim.Error(err.Error())
	}

	err = stub.PutState(analyticsKey, analyticsByte)
	if err != nil {
		logger.Error("PutState() ERROR!\n")
		return shim.Error(err.Error())
	}

	err = setAnalyticsEvent(stub, analyticsByte)
	if err != nil {
		logger.Error("SetEvent() ERROR!\n")
		return shim.Error(err.Error())
	}

	logger.Debug("editAnalyticsInstances EVENT: payload= ", string(analyticsByte))
	return shim.Success(nil)

}
