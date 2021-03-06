package stubidp.utils.security.security.verification;


import stubidp.utils.security.security.verification.exceptions.CertificateChainValidationException;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.PKIXParameters;

public class PKIXParametersProvider {

    public PKIXParametersProvider() {
    }

    public PKIXParameters getPkixParameters(KeyStore keyStore) {
        PKIXParameters certPathParameters;

        try {
            certPathParameters = new PKIXParameters(keyStore);
        } catch (KeyStoreException | InvalidAlgorithmParameterException e) {
            throw new CertificateChainValidationException("There was an error reading from the trust store.", e);
        }
        certPathParameters.setRevocationEnabled(false);
        return certPathParameters;
    }
}
