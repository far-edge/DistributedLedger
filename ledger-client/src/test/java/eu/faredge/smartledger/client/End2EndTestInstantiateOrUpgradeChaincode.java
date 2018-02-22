/**
 * @author ascatox
 */
package eu.faredge.smartledger.client;

import eu.faredge.smartledger.client.base.ISmartLedgerClient;
import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class End2EndTestInstantiateOrUpgradeChaincode {
/* IS_UPGRADE false => fa una INSTANTIATE nel primo giro dopo la INSTALL */
/* IS_UPGRADE true  => fa una UPGRADE appunto, dopo eventuali modifiche al chaincode */
    public static final boolean IS_UPGRADE = true;
    static ISmartLedgerClient client = null;

    @BeforeClass
    public static void begin() {
        client = new SmartLedgerClient();
        try {
            SmartLedgerClient smartLedgerClient = (SmartLedgerClient) client;
            smartLedgerClient.instantiateOrUpgradeChaincode(IS_UPGRADE);
        } catch (SmartLedgerClientException e) {
            assertFalse(e.getMessage(), true);
        }
    }


    @Test
    public void test() {

    }

}
