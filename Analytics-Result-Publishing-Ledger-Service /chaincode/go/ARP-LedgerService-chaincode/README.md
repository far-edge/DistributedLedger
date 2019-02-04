# Analytics Result Publishing Ledger Service - Chaincode

```go
func (t *ARPLedgerService) Invoke(stub shim.ChaincodeStubInterface) pb.Response {

logger.Debug("Chaincode Interface - Invoke()\n")
	function, args := stub.GetFunctionAndParameters()

	if function == "post" {
		return t.edit(stub, args)
	} else if function == "put" {
		return t.edit(stub, args)
	} else if function == "delete" {
		return t.delete(stub, args)
	} else if function == "get" {
		return t.get(stub, args)
	} else {
		return shim.Error("Invalid invoke function name")
	}
```
For all methods we create specific events:

-  **POST** and **PUT** `eventName := "ARPLedgerService_EDIT"`
-  **GET** `eventName := "ARPLedgerService_GET"`
-  **DELETE** `eventName := "ARPLedgerService_DELETE"`
