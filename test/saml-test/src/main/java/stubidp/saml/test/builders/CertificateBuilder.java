package stubidp.saml.test.builders;

import stubidp.utils.security.security.Certificate;
import stubidp.test.devpki.TestCertificateStrings;

public class CertificateBuilder {
    private boolean useCertificateOfIssuerId = true;
    private String issuerId = TestCertificateStrings.TEST_ENTITY_ID;
    private String certificate = null;
    private Certificate.KeyUse keyUse = Certificate.KeyUse.Signing;

    private CertificateBuilder() {}

    public static CertificateBuilder aCertificate() {
        return new CertificateBuilder();
    }

    public Certificate build() {
        if (useCertificateOfIssuerId) {
            if (keyUse == Certificate.KeyUse.Signing) {
                certificate = TestCertificateStrings.PUBLIC_SIGNING_CERTS.get(issuerId);
            } else {
                certificate = TestCertificateStrings.getPrimaryPublicEncryptionCert(issuerId);
            }
            if (certificate == null) {
                certificate = "Some certificate";
            }
        }
        return new Certificate(issuerId, certificate, keyUse);
    }

    public CertificateBuilder withIssuerId(String issuerID) {
        this.issuerId = issuerID;
        return this;
    }

    public CertificateBuilder withCertificate(String certificate) {
        useCertificateOfIssuerId = false;
        this.certificate = certificate;
        return this;
    }

    public CertificateBuilder withKeyUse(Certificate.KeyUse keyUse) {
        this.keyUse = keyUse;
        return this;
    }
}
