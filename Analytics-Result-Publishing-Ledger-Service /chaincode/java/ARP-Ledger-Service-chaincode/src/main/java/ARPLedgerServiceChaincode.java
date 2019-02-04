
import java.io.Serializable;
import java.util.List;

import model.Data;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hyperledger.fabric.shim.ChaincodeBase;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import utilis.JsonConverter;

public class ARPLedgerServiceChaincode extends ChaincodeBase {

    private static Log _logger = LogFactory.getLog(ARPLedgerServiceChaincode.class);

    @Override
    public Response init(ChaincodeStub stub) {
        try {
            _logger.info("Init AECLedgerService Chaincode");
            String func = stub.getFunction();
            if (!func.equals("init")) {
                return newErrorResponse("function other than init is not supported");
            }
            List<String> args = stub.getParameters();
            if (args.size() == 0) {
                newErrorResponse("init() Error...empty args");
            }
            // Initialize the chaincode
            //String account1Key = args.get(0);
            //int account1Value = Integer.parseInt(args.get(1));
            //String account2Key = args.get(2);
            //int account2Value = Integer.parseInt(args.get(3));

            //_logger.info(String.format("account %s, value = %s; account %s, value %s", account1Key, account1Value, account2Key, account2Value));
            //stub.putStringState(account1Key, args.get(1));
            //stub.putStringState(account2Key, args.get(3));

            return newSuccessResponse();
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    @Override
    public Response invoke(ChaincodeStub stub) {
        try {
            _logger.info("Invoke AECLedgerService Chaincode");
            String func = stub.getFunction();
            List<String> params = stub.getParameters();
            if (func.equals("putData")) {
                return putEntity(stub, params);
            }
            if (func.equals("deleteData")) {
                return deleteEntity(stub, params);
            }
            /*if (func.equals("updateEntity")) {
                return updateEntity(stub, params);
            }*/
            if (func.equals("getData")) {
                return getEntity(stub, params);
            }
            return newErrorResponse("Invalid invoke function name. Expecting one of: [\"putEntity\", \"deleteEntity\", \", \"updateEntity\"\" , \"getEntity\"]");
        } catch (Throwable e) {
            return newErrorResponse(e);
        }
    }

    private Response putData(ChaincodeStub stub, List<String> args) throws Throwable {

        if (args.size() != 1) {
            return newErrorResponse("Incorrect number of arguments. Expecting 1");
        }
        try {
            Data data;
            data = (Data) JsonConverter.convertFromJson(args.get(0), Data.class, false);
            CompositeKey dataKEY = stub.createCompositeKey("", data.getKey());

            _logger.debug("dataKEY :" + dataKEY.toString());

            byte[] dataByte = SerializationUtils.serialize((Serializable) data);
            stub.putState(dataKEY.toString(), dataByte);
            stub.setEvent("PUT_DATA_EVENT", dataByte);
            _logger.info("Put OK!");
            return newSuccessResponse("Put completed!!");
        } catch (Throwable e) {
            _logger.error(e);
            return newErrorResponse(e);
        }


        /*String accountFromKey = args.get(0);
        String accountToKey = args.get(1);
        String accountFromValueStr = stub.getStringState(accountFromKey);
        if (accountFromValueStr == null) {
            return newErrorResponse(String.format("Entity %s not found", accountFromKey));
        }
        int accountFromValue = Integer.parseInt(accountFromValueStr);
        String accountToValueStr = stub.getStringState(accountToKey);
        if (accountToValueStr == null) {
            return newErrorResponse(String.format("Entity %s not found", accountToKey));
        }
        int accountToValue = Integer.parseInt(accountToValueStr);
        int amount = Integer.parseInt(args.get(2));

        if (amount > accountFromValue) {
            return newErrorResponse(String.format("not enough money in account %s", accountFromKey));
        }

        accountFromValue -= amount;
        accountToValue += amount;
        _logger.info(String.format("new value of A: %s", accountFromValue));
        _logger.info(String.format("new value of B: %s", accountToValue));
        stub.putStringState(accountFromKey, Integer.toString(accountFromValue));
        stub.putStringState(accountToKey, Integer.toString(accountToValue));
        _logger.info("Transfer complete");
        */
    }
    // query callback representing the query of a chaincode
    private Response getData(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1) {
            return newErrorResponse("Incorrect number of arguments. Expecting name of the person to query");
        }

        CompositeKey key = stub.createCompositeKey("", args.get(0));
        //byte[] stateBytes
        String val = stub.getStringState(key.toString());

        if (val == null) {
            return newErrorResponse(String.format("Error: state for %s is null", key));
        }
        _logger.info(String.format("Query Response:\nName: %s, Amount: %s\n", key, val));
        return newSuccessResponse(val);
    }


    // Deletes an entity from state
    private Response deleteData(ChaincodeStub stub, List<String> args) {
        if (args.size() != 1) {
            return newErrorResponse("Incorrect number of arguments. Expecting 1");
        }
        try {
            CompositeKey key = stub.createCompositeKey("", args.get(0));
            // Delete the key from the state in ledger
            stub.delState(key.toString());
            return newSuccessResponse();
        }catch (Throwable e){
            _logger.error(e);
            return newErrorResponse(e);
        }

    }

    public static void main(String[] args) {

        new ARPLedgerServiceChaincode().start(args);
    }
}