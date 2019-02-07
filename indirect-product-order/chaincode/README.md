# The Product Order Ledger Service Chaincode

```go
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
```
