# Analytics Result Publishing Ledger Service - Chaincode

### APIs

```go
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
```

For all methods we create specific events:

- **POST** and **PUT** `eventName := "ARPLedgerService_EDIT"`
- **GET** `eventName := "ARPLedgerService_GET"`
- **DELETE** `eventName := "ARPLedgerService_DELETE"`
