# Analytics Result Publishing Ledger Service - Chaincode

### APIs

```go
func (t *AECLedgerService) Invoke(stub shim.ChaincodeStubInterface) pb.Response {
	logger.Debug("Chaincode Interface - Invoke()\n")
	function, args := stub.GetFunctionAndParameters()

	if function == "postConfiguration" {
		return t.editConfiguration(stub, args)
	} else if function == "putConfiguration" {
		return t.editConfiguration(stub, args)
	} else if function == "deleteConfiguration" {
		return t.deleteConfiguration(stub, args)
	} else if function == "getConfiguration" {
		return t.getConfiguration(stub, args)
	} else {
		return shim.Error("Invalid invoke function name")
	}
}
```
For all methods we create specific events:

-  **POST** and **PUT** `eventName := "AECLedgerService_EDIT"`
-  **GET** `eventName := "AECLedgerService_GET"`
-  **DELETE** `eventName := "AECLedgerService_DELETE"`
