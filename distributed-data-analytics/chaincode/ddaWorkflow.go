package main

import (
	"bytes"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

var logger = shim.NewLogger("dda-chaincode-log")

type DistributedDataAnalyticsWorkflow struct {
	testMode bool
}

func (t *DistributedDataAnalyticsWorkflow) Init(stub shim.ChaincodeStubInterface) pb.Response {

	logger.Info("Chaincode Interface - Init()\n")
	logger.SetLevel(shim.LogDebug)
	return shim.Success(nil)
}

func (t *DistributedDataAnalyticsWorkflow) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	logger.Debug("Chaincode Interface - Invoke()\n")
	function, args := stub.GetFunctionAndParameters()

	//Analytics
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

		//DataSources
	} else if function == "createDataSource" {
		return t.createDataSource(stub, args)
	} else if function == "deleteDataSource" {
		return t.deleteDataSource(stub, args)
	} else if function == "discoverDataSources" {
		return t.discoverDataSources(stub, args)

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
	}
}

// ===================================================================================================
// addParameterToCouchDBQuery add a new Paramter with a name (owner) and value (tom) to Couchdb Query
// {
//     "selector": {
//       "owner": "tom"
//    }
// }
// ===================================================================================================

func addParameterToCouchDBQuery(queryString string, parameterName string, paramaterValue string) string {
	queryString += DOUBLE_QUOTES + parameterName + DOUBLE_QUOTES + TWO_POINTS + DOUBLE_QUOTES + paramaterValue + DOUBLE_QUOTES + SPACE
	logger.Info("addParameterToCouchDBQuery query string: ", queryString)
	return queryString
}
func addTypeToCouchDBQuery(parameterName string, paramaterValue string) string {
	var queryString string
	queryString += DOUBLE_QUOTES + parameterName + DOUBLE_QUOTES + TWO_POINTS + DOUBLE_QUOTES + paramaterValue + DOUBLE_QUOTES + SPACE
	logger.Info("addTypeToCouchDBQuery query string: ", queryString)
	return queryString
}

// =========================================================================================
// getQueryResultForQueryString executes the passed in query string.
// Result set is built and returned as a byte array containing the JSON results.
// =========================================================================================
func getQueryResultForQueryString(stub shim.ChaincodeStubInterface, queryString string) ([]byte, error) {

	logger.Info("- getQueryResultForQueryString queryString:\n", queryString)
	resultsIterator, err := stub.GetQueryResult(queryString)
	if err != nil {
		logger.Error("GetQueryResult ERROR!")
		return nil, err
	}
	defer resultsIterator.Close()

	data, err := constructQueryResponseFromIterator(resultsIterator)
	if err != nil {
		return nil, err
	}
	logger.Info("- getQueryResultForQueryString queryResult:\n%s\n", data)
	return []byte(data), nil
}

// ===========================================================================================
// constructQueryResponseFromIterator constructs a JSON array containing query results from
// a given result iterator
// ===========================================================================================
func constructQueryResponseFromIterator(resultsIterator shim.StateQueryIteratorInterface) (string, error) {
	// buffer is a JSON array containing QueryResults
	var buffer bytes.Buffer
	if !resultsIterator.HasNext() {
		logger.Info("constructQueryResponseFromIterator Info: resultIterator null!")
		return "", nil
	}
	buffer.WriteString("[")

	for resultsIterator.HasNext() {
		orderCommandInterface, err := resultsIterator.Next()
		if err != nil {
			logger.Error("stateQueryIteratorInterface.Next() ERROR")
			return "", err
		}
		buffer.WriteString(string(orderCommandInterface.Value))
		buffer.WriteString(",")
	}
	jsonResp := buffer.String()
	subString := jsonResp[0 : len(jsonResp)-1]
	jsonResponse := subString + "]"
	logger.Debug("Query Response:\n" + jsonResponse)

	return jsonResponse, nil
}

func main() {
	twc := new(DistributedDataAnalyticsWorkflow)
	twc.testMode = true
	err := shim.Start(twc)
	if err != nil {
		logger.Error("Error starting Distributed-Data-Analytics chaincode: ", err)
	}
}
