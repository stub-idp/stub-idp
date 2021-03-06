/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Derived from EvaluableX509CertSelectorCredentialCriteria
 */

package stubidp.saml.security;

import org.opensaml.security.credential.Credential;
import org.opensaml.security.credential.criteria.impl.EvaluableCredentialCriterion;
import org.opensaml.security.x509.X509Credential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.utils.security.security.verification.CertificateChainValidator;
import stubidp.utils.security.security.verification.CertificateValidity;
import stubidp.utils.security.security.verification.exceptions.CertificateChainValidationException;

import java.security.KeyStore;
import java.security.cert.CertPathValidatorException;
import java.security.cert.X509Certificate;
import java.util.Objects;
import java.util.Optional;

/**
 * Instance of evaluable credential criteria for evaluating whether a credential's certificate has a valid
 * certificate chain according to {@link CertificateChainValidator}.
 *
 */
public class CertificateChainEvaluableCriterion implements EvaluableCredentialCriterion {

    private final CertificateChainValidator certificateChainValidator;
    private final KeyStore keyStore;
    private static final Logger log = LoggerFactory.getLogger(CertificateChainEvaluableCriterion.class);


    public CertificateChainEvaluableCriterion(CertificateChainValidator certificateChainValidator, KeyStore keyStore) {
        this.certificateChainValidator = certificateChainValidator;
        this.keyStore = keyStore;
    }

    @Override
    public boolean test(Credential target) {
        if (target == null) {
            log.error("Credential target was null");
            return Boolean.FALSE;
        }
        if (!(target instanceof X509Credential x509Cred)) {
            log.info("Credential is not an X509Credential, can not evaluate X509CertSelector criteria");
            return Boolean.FALSE;
        }

        X509Certificate entityCert = x509Cred.getEntityCertificate();
        if (Objects.isNull(entityCert)) {
            log.info("X509Credential did not contain an entity certificate, can not evaluate X509CertSelector criteria");
            return Boolean.FALSE;
        }

        try {
            CertificateValidity result = certificateChainValidator.validate(entityCert, keyStore);
            if (result.isValid()) {
                return Boolean.TRUE;
            }
            Optional<CertPathValidatorException> exception = result.getException();
            exception.ifPresent(e -> log.info(e.getMessage()));
            return Boolean.FALSE;
        }
        catch (CertificateChainValidationException ex) {
            log.info(ex.getMessage());
            return Boolean.FALSE;
        }
    }
}
