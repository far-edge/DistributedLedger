package main

import (
	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

var logger = shim.NewLogger("ARP-Ledger-Service-chaincode-log")

type ARPLedgerService struct {
	testMode bool
}

func (t *ARPLedgerService) Init(stub shim.ChaincodeStubInterface) pb.Response {

	logger.Info("Chaincode Interface - Init()\n")
	logger.SetLevel(shim.LogDebug)
	return shim.Success(nil)
}

func (t *ARPLedgerService) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	logger.Debug("Chaincode Interface - Invoke()\n")
	function, args := stub.GetFunctionAndParameters()

	if function == "postResult" {
		return t.editResult(stub, args)
	} else if function == "putResult" {
		return t.editResult(stub, args)
	} else if function == "deleteResult" {
		return t.deleteResult(stub, args)
	} else if function == "getResult" {
		return t.getResult(stub, args)
	} else {
		return shim.Error("Invalid invoke function name")
	}
}

func (t *ARPLedgerService) editResult(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	logger.Info("Chaincode Interface - put()")

	if len(args) != 2 {
		logger.Error("put() ERROR: Expect exactly 2 arguments!!\n")
		return shim.Error("put() ERROR: Expect exactly 2 arguments!!")
	}

	key, err := stub.CreateCompositeKey("", []string{args[0]})
	if err != nil {
		logger.Error("CreateCompositeKey() Error!")
		return shim.Error(err.Error())
	}

	err = stub.PutState(key, []byte(args[1]))
	if err != nil {
		logger.Error("PutState() Error!")
		return shim.Error(err.Error())
	}
	eventName := "ARPLedgerService_EDIT"
	err = createEvent(stub, eventName, []byte(args[1]))
	if err != nil {
		return shim.Error(err.Error())
	}
	logger.Info("Operation Success!\n")
	return shim.Success(nil)
}

func (t *ARPLedgerService) getResult(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	logger.Info("Chaincode Interface - get()")

	if len(args) != 1 {
		logger.Error("get ERROR: Expect exaxtly one args!!\n")
		return shim.Error("get ERROR: Expect exaxtly one args!!")
	}

	key, err := stub.CreateCompositeKey("", []string{args[0]})
	if err != nil {
		logger.Error("CreateCompositeKey() ERROR!")
		return shim.Error(err.Error())
	}
	byteReturn, err := stub.GetState(key)
	if err != nil {
		logger.Error("GetState() ERROR!!")
		return shim.Error(err.Error())
	}
	eventName := "ARPLedgerService_GET"
	err = createEvent(stub, eventName, byteReturn)
	if err != nil {
		return shim.Error(err.Error())
	}
	logger.Debug("get() success!!")
	return shim.Success(byteReturn)
}

func (t *ARPLedgerService) deleteResult(stub shim.ChaincodeStubInterface, args []string) pb.Response {

	logger.Info("Chaincode Interface - delete()")

	if len(args) != 1 {
		logger.Error("delete() ERROR: Expect exaxtly one args!!\n")
		return shim.Error("delete() ERROR: Expect exaxtly one args!!")
	}

	key, err := stub.CreateCompositeKey("", []string{args[0]})
	if err != nil {
		logger.Error("CreateCompositeKey() ERROR!")
		return shim.Error(err.Error())
	}
	err = stub.DelState(key)
	if err != nil {
		logger.Error("DelState() ERROR!!")
		return shim.Error(err.Error())
	}
	eventName := "ARPLedgerService_DELETE"
	err = createEvent(stub, eventName, []byte("object deleted!"))
	if err != nil {
		return shim.Error(err.Error())
	}
	logger.Debug("delete() success!!")
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
	twc := new(ARPLedgerService)
	twc.testMode = true
	err := shim.Start(twc)
	if err != nil {
		logger.Error("Error starting Analytics-Results-Publishing-Ledger-Service chaincode: ", err)
	}
}
