@SuppressWarnings({"requires-automatic", "requires-transitive-automatic"})
module stubidp.saml.extensions {
    opens stubidp.saml.extensions.extensions.impl;
    opens stubidp.saml.extensions.extensions.eidas.impl;
    opens stubidp.saml.extensions.extensions.versioning;

    exports stubidp.saml.extensions;
    exports stubidp.saml.extensions.domain;
    exports stubidp.saml.extensions.extensions;
    exports stubidp.saml.extensions.extensions.eidas;
    exports stubidp.saml.extensions.extensions.eidas.impl;
    exports stubidp.saml.extensions.validation;
    exports stubidp.saml.extensions.validation.errors;
    exports stubidp.saml.extensions.extensions.versioning;
    exports stubidp.saml.extensions.extensions.versioning.application;
    exports stubidp.saml.extensions.extensions.impl;

    requires transitive java.xml;
    requires transitive net.shibboleth.utilities.java.support;
    requires transitive org.checkerframework.checker.qual;
    requires transitive org.opensaml.core;
    requires transitive org.opensaml.saml.impl;
    requires transitive org.opensaml.saml;
    requires transitive org.slf4j;

    requires org.apache.santuario.xmlsec;
    requires org.bouncycastle.provider;
    requires org.opensaml.xmlsec;
    requires org.opensaml.xmlsec.impl;
}