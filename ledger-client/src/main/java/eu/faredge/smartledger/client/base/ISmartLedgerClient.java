package eu.faredge.smartledger.client.base;

import eu.faredge.dm.dcd.DCD;
import eu.faredge.dm.dcm.DCM;
import eu.faredge.dm.dsm.DSM;
import eu.faredge.smartledger.client.exception.SmartLedgerClientException;

import java.util.List;

public interface ISmartLedgerClient {


    /**
     * This method is used to create a DSM object in the Ledger
     *
     * @param dsm
     * @return id
     * @throws SmartLedgerClientException
     */
    String registerDSM(DSM dsm) throws SmartLedgerClientException;

    /**
     * This method performs a modify in already created DSM
     *
     * @param dsm
     * @throws SmartLedgerClientException
     */
    void editRegisteredDSM(DSM dsm) throws SmartLedgerClientException;

    /**
     * This method performs a modify in already created DCM
     *
     * @param dcm
     * @throws SmartLedgerClientException
     */
    void editRegisteredDCM(DCM dcm) throws SmartLedgerClientException;

    /**
     * This method is used to create a DCM object in the Ledger
     *
     * @param dcm
     * @return id
     * @throws SmartLedgerClientException
     */
    String registerDCM(DCM dcm) throws SmartLedgerClientException;

    /**
     * This method gives us a DSM object if present in the ledger, it uses the ID, a unique key of DSM
     * It gives us an empty object if the object is not present
     *
     * @param id
     * @return
     * @throws SmartLedgerClientException
     */

    DSM getDataSourceManifestById(String id) throws SmartLedgerClientException;

    /**
     * This method gives us a DSM object if present in the ledger, it uses the MACAddress, a unique key of DSM
     * It gives us an empty object if the object is not present
     *
     * @param macAddress
     * @return
     * @throws SmartLedgerClientException
     */
    DSM getDataSourceManifestByMacAddress(String macAddress) throws SmartLedgerClientException;

    /**
     * This method gives us a DSM object if present in the ledger, it uses the DSD, a unique key of DSM
     * It gives us an empty object if the object is not present
     *
     * @param dsdId
     * @return
     * @throws SmartLedgerClientException
     */
    DSM getDataSourceManifestByDSD(String dsdId) throws SmartLedgerClientException;

    /**
     * This method gives us a List of DCD objects if present in the ledger.
     *
     * @return
     * @throws SmartLedgerClientException
     */
    List<DCD> getAllDataChannelDescriptors() throws SmartLedgerClientException;


    /**
     * This method gives us a DCM object if present in the ledger, it uses the MACAddress, a unique key of DCM
     * It gives us an empty object if the object is not present
     *
     * @param macAddress
     * @return
     * @throws SmartLedgerClientException
     */
    DCM getDataConsumerManifestByMacAddress(String macAddress) throws SmartLedgerClientException;

    /**
     * This method gives us a DCM object if present in the ledger, it uses the ID, a unique key of DCM
     * It gives us an empty object if the object is not present
     *
     * @param id
     * @return
     * @throws SmartLedgerClientException
     */
    DCM getDataConsumerManifestById(String id) throws SmartLedgerClientException;


    /**
     * This method gives us a List of DSM objects if present in the ledger.
     *
     * @return
     * @throws SmartLedgerClientException
     */
    List<DSM> getAllDataSourceManifests() throws SmartLedgerClientException;

    /**
     * This is an administration method, needed to **install** a chaincode in all peers defined in Config class
     * It's possible to **instantiate** or **upgrade** a chaincode using the boolean flags
     *
     * @param instantiate
     * @param upgrade
     * @throws SmartLedgerClientException
     */


    /**
     * This method gives us a List of DSM objects compatible and of interest for the given DCM using definitions
     * It gives us an empty object if the objects are not present
     *
     * @param dcm
     * @return
     * @throws SmartLedgerClientException
     */
    List<DSM> getCompatibleDSM(DCM dcm) throws SmartLedgerClientException;


    //void installChaincode(boolean instantiate, boolean upgrade) throws SmartLedgerClientException;

    /**
     * This is an administration method, needed to **instantiate** or **upgrade** a chaincode in all peers defined in
     * Config class
     *
     * @param isUpgrade
     * @throws SmartLedgerClientException
     */
    //void instantiateOrUpgradeChaincode(boolean isUpgrade) throws SmartLedgerClientException;

    /**
     * This method gives us a List of DCM objects if present in the ledger.
     *
     * @return
     * @throws SmartLedgerClientException
     */

    List<DCM> getAllDataConsumerManifests() throws SmartLedgerClientException;

    /**
     * This method allows to remove a DSM.
     *
     * @param id
     * @throws SmartLedgerClientException
     */
    void removeDSM(String id) throws SmartLedgerClientException;

    /**
     * This method allows to remove a DCM.
     *
     * @param id
     * @throws SmartLedgerClientException
     */
    void removeDCM(String id) throws SmartLedgerClientException;

    /**
     * This method allows to create A DCD Data Channel Descriptor
     *
     * @param dcd
     * @return
     * @throws SmartLedgerClientException
     */
    String registerDCD(DCD dcd) throws SmartLedgerClientException;

    /**
     * This method allows to remove A DCD
     *
     * @param id
     * @throws SmartLedgerClientException
     */
    void removeDCD(String id) throws SmartLedgerClientException;

    /**
     * his method gives us a DCD object if present in the ledger, it uses the ID, a unique key of DCD
     * It gives us an empty object if the object is not present
     *
     * @param id
     * @return
     * @throws SmartLedgerClientException
     */
    DCD getDataChannelDescriptorById(String id) throws SmartLedgerClientException;

}
