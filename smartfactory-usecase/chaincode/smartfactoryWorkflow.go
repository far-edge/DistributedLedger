package main

import (
	"bytes"
	"encoding/json"
	"strings"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

var logger = shim.NewLogger("smartfactory-chaincode-log")

type SmartFactoryWorkflow struct {
	testMode bool
}

func (t *SmartFactoryWorkflow) Init(stub shim.ChaincodeStubInterface) pb.Response {

	logger.Info("Chaincode   Interface - Init()\n")
	logger.SetLevel(shim.LogDebug)
	return shim.Success(nil)
}

func (t *SmartFactoryWorkflow) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	logger.Info("Chaincode Interface - Invoke()\n")

	function, args := stub.GetFunctionAndParameters()
	logger.Info("Function name:"+function, " args: "+strings.Join(args, ","))

	if function == "storeOrderCommand" {
		return t.storeOrderCommand(stub, args)
	} else if function == "getAllOrderCommand" {
		return t.getAllOrderCommand(stub, args)
	} else if function == "getOrderCommand" {
		return t.getOrderCommand(stub, args)
	} else if function == "getOrderCommandByOrder" {
		return t.getOrderCommandByQueryResult(stub, args, true)
	} else if function == "getOrderCommandByCustomer" {
		return t.getOrderCommandByQueryResult(stub, args, false)
	}
	return shim.Error("Invalid invoke function name")
}

func (t *SmartFactoryWorkflow) getOrderCommandByQueryResult(stub shim.ChaincodeStubInterface, args []string, isOrderer bool) pb.Response {
	logger.Info("Chaincode Interface - getOrderCommandByOrder")

	var typeOrderCommand string

	if len(args) == 0 {
		logger.Error("getOrderCommandByQueryResult ERROR: input argument is empty!\n")
		return shim.Error("getOrderCommandByQueryResult ERROR: input argument is empty!")
	}
	if isOrderer {
		typeOrderCommand = "order"
	} else {
		typeOrderCommand = "customer"
	}
	logger.Debug(typeOrderCommand)
	queryString :=
		"{" +
			" \"selector\": {" +
			" \"" + typeOrderCommand + "\": \"" + args[0] + "\"" +
			" ," +
			" \"type\": \"orderCommand\"" +
			" }" +
			" }"

	logger.Debug("Query on DB starting...")

	stateQueryIteratorInterface, err := stub.GetQueryResult(queryString)
	defer stateQueryIteratorInterface.Close()
	if err != nil {
		logger.Error("getOrderCommandByQueryResult ERROR: GetQueryResult()")
		return shim.Error(err.Error())
	}
	var buffer bytes.Buffer
	buffer.WriteString("[")
	if !stateQueryIteratorInterface.HasNext() {
		logger.Debug("Never orderCommand in ledger ")
		return shim.Success(nil)
	}
	for stateQueryIteratorInterface.HasNext() {
		orderCommandInterface, err := stateQueryIteratorInterface.Next()
		if err != nil {
			logger.Error("stateQueryIteratorInterface.Next() ERROR")
			return shim.Error(err.Error())
		}
		logger.Debug("Signle OrderCommand extract :", string(orderCommandInterface.Value))
		buffer.WriteString(string(orderCommandInterface.Value))
		buffer.WriteString(",")
	}
	jsonResp := buffer.String()
	subString := jsonResp[0 : len(jsonResp)-1]
	jsonResponse := subString + "]"
	eventName := "SMARTFACTORY_USECASE_EVENT"
	err = createEvent(stub, eventName, []byte(jsonResponse))
	if err != nil {
		return shim.Error(err.Error())
	}
	return shim.Success([]byte(jsonResponse))
}

func (t *SmartFactoryWorkflow) getOrderCommand(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	logger.Info("Chaincode Interface - getOrderCommand()")

	if len(args) != 2 {
		logger.Error("getOrderCommand ERROR: Expect exaxtly two args!!\n")
		return shim.Error("getOrderCommand ERROR: Expect exaxtly two args!!")
	}

	logger.Debug("Input args: ", args[0]+args[1])

	orderCommandKey, err := stub.CreateCompositeKey("FE_SMARTFACTORY_USECASE", []string{args[0], args[1]})
	if err != nil {
		logger.Error("getOrderCommand ERROR: CreateCompositeKey()")
		return shim.Error(err.Error())
	}
	logger.Debug("KEY: ", orderCommandKey)
	byteOrderCommand, err := stub.GetState(orderCommandKey)
	if err != nil || len(byteOrderCommand) == 0 {
		logger.Error("GetState() ERROR!!")
		return shim.Error(err.Error())
	}

	var orderCommand *OrderCommand
	err = json.Unmarshal(byteOrderCommand, &orderCommand)
	if err != nil {
		logger.Error("getOrderCommand ERROR: json.Unmarshal()")
		return shim.Error(err.Error())
	}
	orderCommand.Operation = "getOrderCommand"
	byteOC, err := json.Marshal(&orderCommand)
	if err != nil {
		logger.Error("getOrderCommand ERROR: json.Unmarshal()")
		return shim.Error(err.Error())
	}

	eventName := "SMARTFACTORY_USECASE_EVENT"
	err = createEvent(stub, eventName, byteOC)
	if err != nil {
		return shim.Error(err.Error())
	}
	logger.Debug("getOrderCommand() success!!")
	return shim.Success(byteOrderCommand)
}

func (t *SmartFactoryWorkflow) getAllOrderCommand(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	logger.Info("Chaincode Interface - storeOrderCommand()")
	//var orderCommand OrderCommand
	stateQueryIteratorInterface, err := stub.GetStateByPartialCompositeKey("FE_SMARTFACTORY_USECASE", []string{})
	if err != nil {
		logger.Error("getAllaOrderCommand ERROR: GetStateByPartialCompositeKey()")
		return shim.Error(err.Error())
	}
	var buffer bytes.Buffer
	buffer.WriteString("[")
	for stateQueryIteratorInterface.HasNext() {
		orderCommandInterface, err := stateQueryIteratorInterface.Next()
		if err != nil {
			logger.Error("stateQueryIteratorInterface.Next() ERROR")
			return shim.Error(err.Error())
		}
		logger.Debug("Signle OrderCommand extract :", string(orderCommandInterface.Value))
		buffer.WriteString(string(orderCommandInterface.Value))
		buffer.WriteString(",")
	}
	jsonResp := buffer.String()
	subString := jsonResp[0 : len(jsonResp)-1]
	jsonResponse := subString + "]"
	//logger.Debug("Query Response:\n" + jsonResponse)

	eventName := "SMARTFACTORY_USECASE_EVENT"
	err = createEvent(stub, eventName, []byte(jsonResponse))
	if err != nil {
		return shim.Error(err.Error())
	}

	return shim.Success([]byte(jsonResponse))
}

func (t *SmartFactoryWorkflow) storeOrderCommand(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	logger.Info("Chaincode Interface - storeOrderCommand()")

	var orderCommand OrderCommand

	if len(args) != 1 {
		logger.Error("storeOrderCommand ERROR: Expect exactly 1 argument!!\n")
		return shim.Error("storeOrderCommand ERROR: Expect exactly 1 argument!!")
	}

	err := json.Unmarshal([]byte(args[0]), &orderCommand)
	if err != nil {
		logger.Error("storeOrderCommand ERROR: json.Unmashal()")
		return shim.Error(err.Error())
	}
	orderCommand.Type = "orderCommand"
	orderCommandKey, err := stub.CreateCompositeKey("FE_SMARTFACTORY_USECASE", []string{orderCommand.Customer, orderCommand.Order})
	if err != nil {
		logger.Error("storeOrderCommand ERROR: json.Unmashal()")
		return shim.Error(err.Error())
	}
	logger.Debug("KEY: ", orderCommandKey)
	byteOrderCommand, err := json.Marshal(orderCommand)
	if err != nil {
		logger.Error("storeOrderCommand ERROR: json.Mashal()")
		return shim.Error(err.Error())
	}
	orderCommandString := string(byteOrderCommand[:])
	logger.Debug("OrderCommand: " + orderCommandString)
	err = stub.PutState(orderCommandKey, byteOrderCommand)
	if err != nil {
		logger.Error("storeOrderCommand ERROR: PutState()\n")
		return shim.Error(err.Error())
	}
	eventName := "SMARTFACTORY_USECASE_EVENT"
	err = createEvent(stub, eventName, byteOrderCommand)
	if err != nil {
		return shim.Error(err.Error())
	}
	logger.Debug("storeOrderCommand success\n")
	return shim.Success(nil)
}

func createEvent(stub shim.ChaincodeStubInterface, eventName string, payload []byte) error {
	var err error
	if len(payload) == 0 {
		logger.Error("createEvent() ERROR: []byte input empty\n")
	}

	err = stub.SetEvent(eventName, payload)
	if err != nil {
		logger.Error("SetEvent() ERROR\n")
		return err
	}
	logger.Debug("Event create whit name:", eventName+" & payload :"+string(payload))
	return nil

}

func main() {
	twc := new(SmartFactoryWorkflow)
	twc.testMode = true
	err := shim.Start(twc)
	if err != nil {
		logger.Error("Error starting smartfactory-usecace-chaincode: ", err)
	}
}
