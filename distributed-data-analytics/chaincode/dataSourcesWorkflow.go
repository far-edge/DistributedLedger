package main

import (
	"encoding/json"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

//                ______________________
//
//                Data Sources API
//                ______________________
//
func (t *DistributedDataAnalyticsWorkflow) deleteDataSource(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	logger.Info("deleteDataSource start...")

	if len(args) != 2 {
		logger.Error("deleteDataSource() ERROR: Expect exactly 2 args!!\n")
		return shim.Error("deleteDataSource() ERROR: Expect exactly 2 args!")
	}

	dataKey, err := getDataKey(stub, args[0], args[1])
	if err != nil {
		logger.Error("getDataKey ERROR")
		return shim.Error(err.Error())
	}

	err = stub.DelState(dataKey)
	if err != nil {
		logger.Error("DelState ERROR")
		return shim.Error(err.Error())
	}
	logger.Info("DataSourse deleted!")

	err = setDataEvent(stub, []byte("stub.Delete() success DataSource with key: "+dataKey))
	if err != nil {
		logger.Error("SetEvent() ERROR\n")
		return shim.Error(err.Error())
	}
	logger.Info("deleteDataSource completed!")
	return shim.Success(nil)

}

func (t *DistributedDataAnalyticsWorkflow) createDataSource(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	logger.Info("createDataSource() start...")

	/*
		Id                       string `json:"id"`
		Name					 string  `json:"name"`
		EdgeGatewayReferenceID	 string `json:"edgeGatewayReferenceID"`
		DataSourceDefinitionReferenceID	 string `json:"dataSourceDefinitionReferenceID"`
		Payload                  string `json:"payload"`
	*/
	var dataSource DataSource

	if len(args) != 5 {
		logger.Error("createDataSource() ERROR: Expect exactly 5 args!!\n")
		return shim.Error("createDataSource() ERROR: Expect exactly 5 args!")
	}

	for i := range args {
		if len(args[i]) == 0 {
			logger.Error("createDataSource() ERROR: empty args!\n")
			return shim.Error("createDataSource() ERROR: empty args!")
		}
	}
	dataSource.Id = args[0]
	dataSource.Name = args[1]
	dataSource.EdgeGatewayReferenceID = args[2]
	dataSource.DataSourceDefinitionReferenceID = args[3]
	dataSource.Payload = args[4]
	dataSource.Type = DATA_SOURCE

	dataKey, err := getDataKey(stub, dataSource.Id, dataSource.EdgeGatewayReferenceID)
	if err != nil {
		logger.Error("getDataKey() ERROR\n")
		logger.Error(err.Error())
	}
	byteData, err1 := json.Marshal(dataSource)
	if err1 != nil {
		logger.Error("json.Marshal() ERROR!\n")
		return shim.Error(err1.Error())
	}
	err = stub.PutState(dataKey, byteData)
	if err != nil {
		logger.Error("PutState() ERROR!\n")
		return shim.Error(err.Error())
	}

	//          TODO   add SET EVENT()
	err = setDataEvent(stub, byteData)
	if err != nil {
		logger.Error("SetEvent() ERROR!\n")
		return shim.Error(err.Error())
	}

	logger.Debug("createDataSource EVENT: payload= ", string(byteData))
	return shim.Success(nil)
}

func (t *DistributedDataAnalyticsWorkflow) discoverDataSources(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	logger.Info("discoverDataSources() start...\n")
	var qString string

	if len(args) != 4 {
		logger.Error("discoverDataSources ERROR: Expect exactly 4 args!! \n")
		return shim.Error("discoverDataSources ERROR: Expect exactly 4 args!! ")
	}
	egid := args[2]
	id := args[0]
	name := args[1]
	dsdId := args[3]

	queryString :=
		OPEN_BRACKET +
			SELECTOR +
			OPEN_BRACKET

	if len(egid) > 0 {
		qString = addParameterToCouchDBQuery(queryString, "edgeGatewayReferenceID", egid)
	}
	if len(id) > 0 {
		qString = addParameterToCouchDBQuery(queryString, "id", id)
	}
	if len(name) > 0 {
		qString = addParameterToCouchDBQuery(queryString, "name", name)
	}
	if len(dsdId) > 0 {
		qString = addParameterToCouchDBQuery(queryString, "dataSourceDefinitionReferenceID", dsdId)
	}
	qString += COMMA +
		addTypeToCouchDBQuery("type", DATA_SOURCE) +
		CLOSE_BRACKET +
		CLOSE_BRACKET

	buffer, err := getQueryResultForQueryString(stub, qString)
	if err != nil {
		return shim.Error(err.Error())
	}
	logger.Info("Discover DataSources Result", string(buffer))
	return shim.Success(buffer)
}
