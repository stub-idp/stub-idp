package stubidp.saml.extensions.extensions.eidas.impl;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.opensaml.core.xml.XMLObject;
import org.opensaml.core.xml.schema.impl.XSAnyImpl;
import stubidp.saml.extensions.extensions.eidas.DateOfBirth;

import java.time.Instant;
import java.util.List;

public class DateOfBirthImpl extends XSAnyImpl implements DateOfBirth {

    /** String to hold the date of birth. */
    private Instant dateOfBirth;

    /**
     * Constructor.
     *
     * @param namespaceURI the namespace the element is in
     * @param elementLocalName the local name of the XML element this Object represents
     * @param namespacePrefix the prefix for the given namespace
     */
    protected DateOfBirthImpl(String namespaceURI, String elementLocalName, String namespacePrefix) {
        super(namespaceURI, elementLocalName, namespacePrefix);
    }

    /** {@inheritDoc} */
    public Instant getDateOfBirth() {
        return dateOfBirth;
    }

    /** {@inheritDoc} */
    public void setDateOfBirth(Instant dob) {
        dateOfBirth = prepareForAssignment(dateOfBirth, dob);
    }

    @Nullable
    @Override
    public List<XMLObject> getOrderedChildren() {
        return null;
    }
}
