package main

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"
)

func setAnalyticsEvent(stub shim.ChaincodeStubInterface, eventPayload []byte) error {

	err := stub.SetEvent(ANALYTICS_EVENT, eventPayload)
	return err

}
func setEdgeEvent(stub shim.ChaincodeStubInterface, eventPayload []byte) error {

	err := stub.SetEvent(EDGE_GATEWAY_EVENT, eventPayload)
	return err

}

func setDataEvent(stub shim.ChaincodeStubInterface, eventPayload []byte) error {

	err := stub.SetEvent(DATA_SOURCE_EVENT, eventPayload)
	return err

}

func getAnalyticsKey(stub shim.ChaincodeStubInterface, id string, egid string) (string, error) {
	ddaKey, err := stub.CreateCompositeKey(ANALYTICS_KEY, []string{id, egid})
	if err != nil {
		return "", err
	} else {
		return ddaKey, nil
	}
}
func getDataKey(stub shim.ChaincodeStubInterface, id string, egid string) (string, error) {

	ddaKey, err := stub.CreateCompositeKey(DATA_SOURCE_KEY, []string{id, egid})
	if err != nil {
		return "", err
	} else {
		return ddaKey, nil
	}
}
func getEdgeKey(stub shim.ChaincodeStubInterface, egid string) (string, error) {

	ddaKey, err := stub.CreateCompositeKey(EDGE_GATEWAY_KEY, []string{egid})
	if err != nil {
		return "", err
	} else {
		return ddaKey, nil
	}
}

func BytesToString(data []byte) string {
	return string(data[:])
}
