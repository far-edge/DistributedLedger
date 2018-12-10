
package eu.faredge.smartledger.client.utils;

import eu.faredge.dm.dcd.DCD;
import eu.faredge.dm.dcm.DCM;
import eu.faredge.dm.dsm.DSM;
import eu.faredge.smartledger.client.SmartLedgerClient;
import eu.faredge.smartledger.client.exception.SmartLedgerClientException;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import java.util.GregorianCalendar;
import java.util.Set;

public class Validator {

    private javax.validation.Validator validator;
    public static final String ID_CANNOT_BE_EMPTY_MESSAGE = "Data Model and definition/s cannot be empty";
    public static final String DSM_CANNOT_BE_EMPTY_MESSAGE = "Data Source Manifest cannot be empty";
    public static final String DCM_CANNOT_BE_EMPTY_MESSAGE = "Data Consumer Manifest cannot be empty";
    public static final String DATA_CHANNEL_DESCRIPTOR_EXPIRED_AT = "Data Channel Descriptor expired at: ";
    public static final String DATA_CHANNEL_DESCRIPTOR_WILL_BE_VALID_FROM = "Data Channel Descriptor will be valid from: ";


    public Validator() {

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }


    public void validateBean(DSM dsm) throws SmartLedgerClientException {
        if (null == dsm || StringUtils.isEmpty(dsm.getDataSourceDefinitionID()))
            throw new SmartLedgerClientException(ID_CANNOT_BE_EMPTY_MESSAGE);
    }


    public void validateBean(DCM dcm) throws SmartLedgerClientException {
        if (null == dcm || null == dcm.getDataSourceDefinitionsIDs() || dcm
                .getDataSourceDefinitionsIDs().isEmpty())
            throw new SmartLedgerClientException(ID_CANNOT_BE_EMPTY_MESSAGE);
    }

    public void validateBean(DCD dcd) throws SmartLedgerClientException {
        if (null == dcd)
            throw new SmartLedgerClientException(ID_CANNOT_BE_EMPTY_MESSAGE);
        if (null == dcd.getDataConsumerManifestID() || StringUtils.isEmpty(dcd.getDataConsumerManifestID()))
            throw new SmartLedgerClientException(DCM_CANNOT_BE_EMPTY_MESSAGE);
        if (null == dcd.getDataSourceManifestID() || StringUtils.isEmpty(dcd.getDataSourceManifestID()))
            throw new SmartLedgerClientException(DSM_CANNOT_BE_EMPTY_MESSAGE);

        if (null != dcd.getExpirationDateTime()) {
            GregorianCalendar expirationTime = dcd.getExpirationDateTime().toGregorianCalendar();
            if (expirationTime.before(new GregorianCalendar()))
                throw new SmartLedgerClientException(DATA_CHANNEL_DESCRIPTOR_EXPIRED_AT + expirationTime.toString());
        }
        if (null != dcd.getValidFrom()) {
            GregorianCalendar validFrom = dcd.getValidFrom().toGregorianCalendar();
            if (validFrom.after(new GregorianCalendar()))
                Utils.out(DATA_CHANNEL_DESCRIPTOR_WILL_BE_VALID_FROM + validFrom.toString());
        }

    }


}

