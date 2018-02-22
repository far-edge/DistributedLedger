/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * The sample smart contract for documentation topic:
 * Writing Your First Blockchain Application
 */

package main

/* Imports
 * 4 utility libraries for formatting, handling bytes, reading and writing JSON, and string manipulation
 * 2 specific Hyperledger Fabric specific libraries for Smart Contracts
 */
import (
	"bytes"
	"encoding/json"
	"fmt"
	"os"
	"strconv"
	"strings"
	"github.com/hyperledger/fabric/core/chaincode/shim"
	sc "github.com/hyperledger/fabric/protos/peer"
	"github.com/rs/xid"
)

var logger = shim.NewLogger("smartfactory")

type Device struct{}

type DSM struct {
	Id                   string `json:"id"`
	MACAddress           string `json:"macAddress"`
	DSD                  string `json:"dsd"`
	DSMParameters        string `json:"dsmParameters"`
	Type                 string `json:"type"`

}

type DCM struct {
	Id               string `json:"id"`
	MACAddress       string `json:"macAddress"`
	DSDs             string `json:"dsds"`
	Type             string `json:"type"`
}

type DCD struct {
	ExpirationDateTime	string `json:"expirationDateTime"`
	ValidFrom       	string `json:"validFrom"`
	DSMId             	string `json:"dsmId"`
	DCMId				string `json:"dcmId"`
	Id					string `json:"id"`
	Type             	string `json:"type"`
}



/*
 * The Init method is called when the Smart Contract "fabcar" is instantiated by the blockchain network
 * Best practice is to have any Ledger initialization in separate function -- see initLedger()
 */

func (d *Device) Init(APIstub shim.ChaincodeStubInterface) sc.Response {
	/* Generator of keys */
	guid := xid.New()
	DSMs := []DSM{
		DSM{
			Id:                   "",
			MACAddress:           "123:456:789",
			DSD:                  "DSDkey1",	
			DSMParameters:        "{'key':'connectionParameters','value':'host:faredge.eu;port:8080'}",  
			Type:                 "DSM",
		},
		DSM{
			Id:                   "",
			MACAddress:           "789:654:321",
			DSD:                  "DSDkey2",	
			DSMParameters:        "{'key':'connectionParameters','value':'host:faredge.eu;port:8080'}",  
			Type:                 "DSM",
		},
	}

	/* Loop for n DSM in Init */
	j := 0
	for j < len(DSMs) {
		
		fmt.Println("j is ", j)
		DSMs[j].Id = "DSM_" + guid.String()		/* The guid previously generated */
		DSMAsBytes, _ := json.Marshal(DSMs[j])
		APIstub.PutState("DSM_" + guid.String(), DSMAsBytes)
		fmt.Println("Added", DSMs[j])
		j = j + 1
		guid = xid.New() 				/* The guid for the next item (if there is) */
	}

	DCMs := []DCM{
		DCM{
			Id:               "",
			MACAddress:       "321:654:987",
			DSDs:             "DSDkeyOne",
			Type:             "DCM",
		},
		DCM{
			Id:               "",
			MACAddress:       "987:654:321",
			DSDs:             "DSDkeyTwo",
			Type:             "DCM",
		},
	}
	k := 0
	for k < len(DCMs) {
		fmt.Println("k is ", k)
		DCMs[k].Id = "DCM_" + guid.String()		/* The guid previously generated */
		DCMAsBytes, _ := json.Marshal(DCMs[k])
		APIstub.PutState("DCM_" + guid.String(), DCMAsBytes)
		fmt.Println("Added", DCMs[k])
		k = k + 1
		guid = xid.New() 				/* The guid for the next item (if there is) */
	}
DCDs := []DCD{
		DCD{
			ExpirationDateTime:		"11/31/2099 23:59 PM CET",
			ValidFrom:       		"02/01/2000 00:01 PM CET",
			DSMId:                  "DSM_",
			DCMId:					"DCM_",
			Id:						"",
			Type:             		"DCD",
		},
	}
	l := 0
	for l < len(DCDs) {
		fmt.Println("l is ", l)
		DCDs[l].Id = "DCD_" + guid.String()		/* The guid previously generated */
		DCDAsBytes, _ := json.Marshal(DCDs[l])
		APIstub.PutState("DCD_" + guid.String(), DCDAsBytes)
		fmt.Println("Added", DCDs[l])
		l = l + 1
		guid = xid.New() 				/* The guid for the next item (if there is) */
	}


	return shim.Success(nil)
}

/*
 * The queryDSM method is called to extract a DSM with Key in args[0]
 */

func (d *Device) queryDSM(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}

	dsmAsBytes, _ := APIstub.GetState(args[0])
	return shim.Success(dsmAsBytes)
}


/*
 * The queryDCM method is called to extract a DCM with Key in args[0]
 */

func (d *Device) queryDCM(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}
	dcmAsBytes, _ := APIstub.GetState(args[0])
	return shim.Success(dcmAsBytes)
}


/*
 * The queryDCD method is called to extract a DSM with Key in args[0]
 */

func (d *Device) queryDCD(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting 1")
	}
	dcdAsBytes, _ := APIstub.GetState(args[0])
	return shim.Success(dcdAsBytes)
}


/*
 * The editDSM method is called to update/create the DSM in args[] (if exists or not)
 */

func (d *Device) editDSM(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {
	fmt.Println("Entering inside editDSM")

	if len(args) != 4 {
		lenght := strconv.Itoa(len(args))
		str := "Incorrect number of arguments. Expecting 4!" + "received: " + lenght
		return shim.Error(str)
	}

	guid := xid.New() 						/* new id generated */
	id := args[0]							/* id from parameters in args[] */
	idFound,_ := APIstub.GetState(args[0])	 
	if (args[0] == "" || idFound == nil) {		
		id = "DSM_" + guid.String()
	} 

	dsm := DSM{
		Id:                   id,
		MACAddress:           args[1],
		DSD:                  args[2],
		DSMParameters:		  args[3],
		Type:                 "DSM",
	}
	// Convert keys to compound ke */
	dsmAsBytes, _ := json.Marshal(dsm)
	// Id Primary Key is the same, Key and id
	APIstub.PutState(id, dsmAsBytes)
	
	
	return shim.Success(dsmAsBytes)
}

/*
 * The editDCM method is called to update/create the DSM in args[] (if exists or not)
 */

func (d *Device) editDCM(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 3 {
		return shim.Error("Incorrect number of arguments. Expecting 3")
	}

	guid := xid.New() 						/* new id generated */
	id := args[0]							/* id from parameters in args[] */
	idFound,_ := APIstub.GetState(args[0])
	if (args[0] == "" || idFound == nil) {		
		
		id = "DCM_" + guid.String()
	} 
	dcm := DCM{
		Id:               id,
		MACAddress:       args[1],
		DSDs:             args[2],
		Type:             "DCM",
	}

	dcmAsBytes, _ := json.Marshal(dcm)
	// Id Primary Key
	APIstub.PutState(id, dcmAsBytes)

	return shim.Success(dcmAsBytes)
}

/*
 * The editDCD method is called to update/create the DSM in args[] (if exists or not)
 */

func (d *Device) editDCD(APIstub shim.ChaincodeStubInterface, args []string) sc.Response {

	if len(args) != 5 {
		return shim.Error("Incorrect number of arguments. Expecting 5")
	}
	guid := xid.New() 						/* new id generated */
	id := args[4]							/* id from parameters in args[] */
	idFound,_ := APIstub.GetState(args[4])
	if (args[4] == "" || idFound == nil) {		
		id = "DCD_" + guid.String()
	} 
	dcd := DCD{
		ExpirationDateTime:	args[0],
		ValidFrom:       	args[1],
		DSMId:             	args[2],
		DCMId:				args[3],
		Id:					id,
		Type:             	"DCD",
	}

	dcdAsBytes, _ := json.Marshal(dcd)
	// Id Primary Key
	APIstub.PutState(id, dcdAsBytes)

	return shim.Success(dcdAsBytes)
}

/*
 * The queryAllDSMs method is called to extract all the DSM
 */

func (d *Device) queryAllDSMs(APIstub shim.ChaincodeStubInterface) sc.Response {
	fmt.Println("Entering inside queryAllDSMs")
	queryString :=
		"{" +
			"\"selector\": {" +
			"	\"type\": \"DSM\"" +
			" }" +
			" }"
	buffer, error := getQueryResultForQueryString(APIstub, queryString)
	if error != nil {
		fmt.Printf("Error querying all DSM: %s", error)
		return shim.Error("Error querying all DSM") //TODO Error
	}
	return shim.Success(buffer)
}

/*
 * The queryAllDSMsByDSDs method is called to extract all the DSM that have inside them the DSD in the array DSDs
 */

func (d *Device) queryAllDSMsByDSDs(APIstub shim.ChaincodeStubInterface, dsds []string) sc.Response {
	fmt.Println("Entering inside queryAllDSMsBySDSs: ", len(dsds))
	logger.Info("Entering in queryAllDSMsBySDSs")
	if (len(dsds)) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting dsds (array)")
	}
	isNotFirstElement := false 
	var buffer bytes.Buffer
	buffer.WriteString("[") 	   
	for i:=0;i<len(dsds);i++ {
	
		queryString :=
			"{" +
			"\"selector\": {" +
			"	\"type\": \"DSM\"" +
			" ," +
			" \"dsd\": " +
			" \"" + dsds[i] +
			"\"} " +
			" }"

		fmt.Println("querDSMByDSDs - queryString:", queryString)
		fmt.Println("querDSMByDSDs - queryIndex:", i)
		queryResponse, error := iteratorQueryResultForQueryString(APIstub, queryString)			
		if error != nil {
			fmt.Printf("Error querying DSM by DSD: %s", error)
			return shim.Error("Error querying DSM by dsd " + dsds[i]) //TODO Error
		}
		
		if (isNotFirstElement) {            
			buffer.WriteString(",") 	    
		}
	 
		buffer.WriteString(queryResponse)
		
		isNotFirstElement = true
	}
	buffer.WriteString("]")  
	return shim.Success(buffer.Bytes())
	
}

/*
 * The querDSMByDSD method is called to extract all the DSM that have inside them the DSDId in the parameter 
 */
func (d *Device) querDSMByDSD(APIstub shim.ChaincodeStubInterface, dsdId string) sc.Response {
	fmt.Println("Entering inside querDSMByDSD")
	//	logger.Info("Entering in queryDSMByDSD")
	if (len(strings.TrimSpace(dsdId))) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting DSD")
	}
	queryString :=
		"{" +
			"\"selector\": {" +
			"	\"type\": \"DSM\"" +
			" ," +
			" \"dsd\": " +
			" \"" + dsdId +
			"\"} " +
			" }"
	buffer, error := getQueryResultForQueryString(APIstub, queryString)
	if error != nil {
		fmt.Printf("Error querying DSM by DSD: %s", error)
		return shim.Error("Error querying DSM by DSD " + dsdId) //TODO Error
	}
	return shim.Success(buffer)
}

/*
 * The querDSMById method is called to extract the DSM that have inside them the Id in the parameter 
 */
func (d *Device) querDSMById(APIstub shim.ChaincodeStubInterface, id string) sc.Response {
	fmt.Println("Entering inside querDSMById")
	//	logger.Info("Entering in queryDSMById")
	if (len(strings.TrimSpace(id))) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting Id")
	}
	queryString :=
		"{" +
			"\"selector\": {" +
			"	\"type\": \"DSM\"" +
			" ," +
			" \"id\": " +
			" \"" + id +
			"\"} " +
			" }"
	buffer, error := getQueryResultForQueryString(APIstub, queryString)
	if error != nil {
		fmt.Printf("Error querying DSM by Id: %s", error)
		return shim.Error("Error querying DSM by Id " + id) //TODO Error
	}
	return shim.Success(buffer)
}

/*
 * The querDSMByMacAdd method is called to extract the DSM that have inside them the MacAdd in the parameter 
 */
func (d *Device) querDSMByMacAdd(APIstub shim.ChaincodeStubInterface, macAdd string) sc.Response {
	fmt.Println("Entering inside querDSMByMacAdd")
	//	logger.Info("Entering in queryDSMByMacAdd")
	if (len(strings.TrimSpace(macAdd))) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting macAdd")
	}
	queryString :=
		"{" +
			"\"selector\": {" +
			"	\"type\": \"DSM\"" +
			" ," +
			" \"macAddress\": " +
			" \"" + macAdd +
			"\"} " +
			" }"
	buffer, error := getQueryResultForQueryString(APIstub, queryString)
	if error != nil {
		fmt.Printf("Error querying DSM by MACAddress: %s", error)
		return shim.Error("Error querying DSM by MACAddress " + macAdd) //TODO Error
	}
	return shim.Success(buffer)
}

/*
 * The queryAllDCMs method is called to extract all the DCM
 */
func (d *Device) queryAllDCMs(APIstub shim.ChaincodeStubInterface) sc.Response {
	queryString :=
		"{" +
			"\"selector\": {" +
			"	\"type\": \"DCM\"" +
			" }" +
			" }"
	buffer, error := getQueryResultForQueryString(APIstub, queryString)
	if error != nil {
		fmt.Printf("Error querying all DCM: %s", error)
		return shim.Error("Error querying all DCM")
	}
	return shim.Success(buffer)
}

/*
 * The querDCMById method is called to extract the DCM that have inside them the Id in the parameter 
 */
func (d *Device) querDCMById(APIstub shim.ChaincodeStubInterface, id string) sc.Response {
	//	logger.Info("Entering in queryDCMById")
	if (len(strings.TrimSpace(id))) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting Id")
	}
	queryString :=
		"{" +
			"\"selector\": {" +
			"	\"type\": \"DCM\"" +
			" ," +
			" \"id\": " +
			" \"" + id +
			"\"} " +
			" }"
	buffer, error := getQueryResultForQueryString(APIstub, queryString)
	if error != nil {
		fmt.Printf("Error querying DCM by Id: %c", error)
		return shim.Error("Error querying DCM by Id " + id) //TODO Error
	}
	return shim.Success(buffer)
}

/*
 * The querDCMByMacAdd method is called to extract the DCM that have inside them the MacAdd in the parameter 
 */
func (d *Device) querDCMByMacAdd(APIstub shim.ChaincodeStubInterface, macAdd string) sc.Response {
	//	logger.Info("Entering in queryDCMByMacAdd")
	if (len(strings.TrimSpace(macAdd))) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting MACAddress")
	}
	queryString :=
		"{" +
			"\"selector\": {" +
			"	\"type\": \"DCM\"" +
			" ," +
			" \"macAddress\": " +
			" \"" + macAdd +
			"\"} " +
			" }"
	buffer, error := getQueryResultForQueryString(APIstub, queryString)
	if error != nil {
		fmt.Printf("Error querying DCM by MACAddress: %c", error)
		return shim.Error("Error querying DCM by MACAddress " + macAdd) //TODO Error
	}
	return shim.Success(buffer)
}

/*
 * The queryAllDCDs method is called to extract all the DCD
 */
func (d *Device) queryAllDCDs(APIstub shim.ChaincodeStubInterface) sc.Response {
	queryString :=
		"{" +
			"\"selector\": {" +
			"	\"type\": \"DCD\"" +
			" }" +
			" }"
	buffer, error := getQueryResultForQueryString(APIstub, queryString)
	if error != nil {
		fmt.Printf("Error querying all DCD: %s", error)
		return shim.Error("Error querying all DCD")
	}
	return shim.Success(buffer)
}	

/*
 * The querDCDById method is called to extract the DCD that have inside them the Id in the parameter 
 */
func (d *Device) querDCDById(APIstub shim.ChaincodeStubInterface, id string) sc.Response {
	//	logger.Info("Entering in queryDCDById")
	if (len(strings.TrimSpace(id))) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting Id")
	}
	queryString :=
		"{" +
			"\"selector\": {" +
			"	\"type\": \"DCD\"" +
			" ," +
			" \"id\": " +
			" \"" + id +
			"\"} " +
			" }"
	buffer, error := getQueryResultForQueryString(APIstub, queryString)
	if error != nil {
		fmt.Printf("Error querying DCD by Id: %c", error)
		return shim.Error("Error querying DCD by Id " + id) //TODO Error
	}
	return shim.Success(buffer)
}

/*
 * The removeDSM method is called to remove the DSM with the key in the parameters
 */
func (d *Device) removeDSM(stub shim.ChaincodeStubInterface, key string) sc.Response {
	if (len(strings.TrimSpace(key))) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting DSM key")
	}
	dsmAsBytes, _ := stub.GetState(key)
	if dsmAsBytes == nil {
		return shim.Error("Error DSM object with key: " + key + " not found")
	}

	var dsm DSM
	err := json.Unmarshal(dsmAsBytes, &dsm)
	if err != nil {
		return shim.Error("Error removing DSM with key: " + key)
	}
	if dsm.Type != "DSM" {
		return shim.Error("Error removing DSM type of the object for the key: " + key + " is incorrect (type: " + dsm.Type + ")")
	}
	fmt.Printf("Trying to delete DSM Object with key: " + key)
	error := stub.DelState(key)
	if error != nil {
		return shim.Error("Error removing DSM with key: " + key)
	}
	return shim.Success(nil)
}

/*
 * The removeDCM method is called to remove the DCM with the key in the parameters
 */
func (d *Device) removeDCM(stub shim.ChaincodeStubInterface, key string) sc.Response {
	if (len(strings.TrimSpace(key))) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting DCM key")
	}
	dcmAsBytes, _ := stub.GetState(key)
	if dcmAsBytes == nil {
		return shim.Error("Error DCM object with key: " + key + " not found")
	}

	var dcm DCM
	err := json.Unmarshal(dcmAsBytes, &dcm)
	if err != nil {
		return shim.Error("Error removing DCM with key: " + key)
	}
	if dcm.Type != "DCM" {
		return shim.Error("Error removing DCM type of the object for the key: " + key + " is incorrect (type: " + dcm.Type + ")")
	}
	fmt.Printf("Trying to delete DCM Object with key: " + key)
	error := stub.DelState(key)
	if error != nil {
		return shim.Error("Error removing DCM with key: " + key)
	}
	return shim.Success(nil)
}

/*
 * The removeDCD method is called to remove the DCD with the key in the parameters
 */

func (d *Device) removeDCD(stub shim.ChaincodeStubInterface, key string) sc.Response {
	if (len(strings.TrimSpace(key))) == 0 {
		return shim.Error("Incorrect number of arguments. Expecting DCD key")
	}
	dcdAsBytes, _ := stub.GetState(key)
	if dcdAsBytes == nil {
		return shim.Error("Error DCD object with key: " + key + " not found")
	}

	var dcd DCD
	err := json.Unmarshal(dcdAsBytes, &dcd)
	if err != nil {
		return shim.Error("Error removing DCD with key: " + key)
	}
	if dcd.Type != "DCD" {
		return shim.Error("Error removing DCD type of the object for the key: " + key + " is incorrect (type: " + dcd.Type + ")")
	}
	fmt.Printf("Trying to delete DCD Object with key: " + key)
	error := stub.DelState(key)
	if error != nil {
		return shim.Error("Error removing DCD with key: " + key)
	}
	return shim.Success(nil)
}

/*
 * The getQueryResultForQueryString method is called to applicate the query string condition
 */
func getQueryResultForQueryString(stub shim.ChaincodeStubInterface, queryString string) ([]byte, error) {
	fmt.Printf("- getQueryResultForQueryString queryString:\n%s\n", queryString)
	resultsIterator, err := stub.GetQueryResult(queryString)
	defer resultsIterator.Close()
	if err != nil {
		return nil, err
	}
	// buffer is a JSON array containing QueryRecords
	var buffer bytes.Buffer
	buffer.WriteString("[")
	bArrayMemberAlreadyWritten := false
	for resultsIterator.HasNext() {
		queryResponse,
			err := resultsIterator.Next()
		if err != nil {
			return nil, err
		}
		// Add a comma before array members, suppress it for the first array member
		if bArrayMemberAlreadyWritten == true {
			buffer.WriteString(",")
		}
		buffer.WriteString("{\"Key\":")
		buffer.WriteString("\"")
		buffer.WriteString(queryResponse.Key)
		buffer.WriteString("\"")
		buffer.WriteString(", \"Record\":")
		// Record is a JSON object, so we write as-is
		buffer.WriteString(string(queryResponse.Value))
		buffer.WriteString("}")
		bArrayMemberAlreadyWritten = true
	}
	buffer.WriteString("]")	
	fmt.Printf("- getQueryResultForQueryString queryResult:\n%s\n", buffer.String())
	return buffer.Bytes(), nil
}

/*
 * The iteratorQueryResultForQueryString method is called to applicate the query string condition in a loop for
 */
func iteratorQueryResultForQueryString(stub shim.ChaincodeStubInterface, queryString string) (string, error) {
	resultsIterator, err := stub.GetQueryResult(queryString)
	defer resultsIterator.Close()
	if err != nil {
		return "", err
	}
	// buffer is a JSON array containing QueryRecords
	var buffer bytes.Buffer
	/* buffer.WriteString("[") */
	bArrayMemberAlreadyWritten := false
	for resultsIterator.HasNext() {
		queryResponse,
			err := resultsIterator.Next()
		if err != nil {
			return "", err
		}
		// Add a comma before array members, suppress it for the first array member
		if bArrayMemberAlreadyWritten == true {
			buffer.WriteString(",")
		}
		buffer.WriteString("{\"Key\":")
		buffer.WriteString("\"")
		buffer.WriteString(queryResponse.Key)
		buffer.WriteString("\"")
		buffer.WriteString(", \"Record\":")
		// Record is a JSON object, so we write as-is
		buffer.WriteString(string(queryResponse.Value))
		buffer.WriteString("}")
		bArrayMemberAlreadyWritten = true
	}
	/* buffer.WriteString("]") */
	return buffer.String(),nil
}

/*
 * The Invoke method is called to invoke the method in args[0], with the parameters in args[n] 
 */
func (d *Device) Invoke(stub shim.ChaincodeStubInterface) sc.Response {
	function, args := stub.GetFunctionAndParameters()
	fmt.Printf("CALL function: " + function)

	switch function {
	// DSM Type
	// Query DSM
	case "qGetAllDSMs":
		return d.queryAllDSMs(stub)
	case "qGetAllDSMsByDSDs":
		return d.queryAllDSMsByDSDs(stub, args)
	case "qGetDSMById":
		return d.querDSMById(stub, args[0])
	case "qGetDSMByMacAdd":
		return d.querDSMByMacAdd(stub, args[0])
	case "qGetDSMByDSD":
		return d.querDSMByDSD(stub, args[0])
	// Edit and Remove DSM
	case "iEditDSM":
		return d.editDSM(stub, args)
	case "iRemoveDSM":
		return d.removeDSM(stub, args[0])
	// DCM Type
	// Query DCM
	case "qGetAllDCMs":
		return d.queryAllDCMs(stub)
	case "qGetDCMById":
		return d.querDCMById(stub, args[0])
	case "qGetDCMByMacAdd":
		return d.querDCMByMacAdd(stub, args[0])
	// Edit and Remove DCM
	case "iEditDCM":
		return d.editDCM(stub, args)
	case "iRemoveDCM":
		return d.removeDCM(stub, args[0])
	// DCD Type
		// Query DCM
	case "qGetAllDCDs":
		return d.queryAllDCDs(stub)
	case "qGetDCDById":
		return d.querDCDById(stub, args[0])
	// Edit and Remove DCD
	case "iEditDCD":
		return d.editDCD(stub, args)
	case "iRemoveDCD":
		return d.removeDCD(stub, args[0])
	}
	return shim.Success(nil)
}

func main() {
	fmt.Printf("Main Function")
	logger.SetLevel(shim.LogInfo)
	logLevel, _ := shim.LogLevel(os.Getenv("SHIM_LOGGING_LEVEL"))
	shim.SetLoggingLevel(logLevel)
	// Edit a new Smart Contract
	err := shim.Start(new(Device))
	fmt.Printf("Edit a new generic Device Type")
	if err != nil {
		fmt.Printf("Error creating new generic Device: %s", err)
	}
}
