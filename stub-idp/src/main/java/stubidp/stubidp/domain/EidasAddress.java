package stubidp.stubidp.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.text.MessageFormat;
import java.util.Base64;
import java.util.Objects;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EidasAddress {
    private final String poBox;
    private final String locatorDesignator;
    private final String locatorName;
    private final String cvAddressArea;
    private final String thoroughfare;
    private final String postName;
    private final String adminunitFirstLine;
    private final String adminunitSecondLine;
    private final String postCode;

    @JsonCreator
    public EidasAddress(@JsonProperty("poBox") String poBox, @JsonProperty("locatorDesignator") String locatorDesignator, @JsonProperty("locatorName") String locatorName, @JsonProperty("cvAddressArea") String cvAddressArea, @JsonProperty("thoroughfare") String thoroughfare, @JsonProperty("postName") String postName, @JsonProperty("adminunitFirstLine") String adminunitFirstLine, @JsonProperty("adminunitSecondLine") String adminunitSecondLine, @JsonProperty("postCode") String postCode) {
        this.poBox = poBox;
        this.locatorDesignator = locatorDesignator;
        this.locatorName = locatorName;
        this.cvAddressArea = cvAddressArea;
        this.thoroughfare = thoroughfare;
        this.postName = postName;
        this.adminunitFirstLine = adminunitFirstLine;
        this.adminunitSecondLine = adminunitSecondLine;
        this.postCode = postCode;
    }

    public String getPoBox() {
        return poBox;
    }

    public String getLocatorDesignator() {
        return locatorDesignator;
    }

    public String getLocatorName() {
        return locatorName;
    }

    public String getCvAddressArea() {
        return cvAddressArea;
    }

    public String getThoroughfare() {
        return thoroughfare;
    }

    public String getPostName() {
        return postName;
    }

    public String getAdminunitFirstLine() {
        return adminunitFirstLine;
    }

    public String getAdminunitSecondLine() {
        return adminunitSecondLine;
    }

    public String getPostCode() {
        return postCode;
    }

    public String toBase64EncodedSaml() {
        String addressAsSamlString = getFieldAsSaml(poBox, "PoBox") +
                getFieldAsSaml(locatorDesignator, "LocatorDesignator") +
                getFieldAsSaml(locatorName, "LocatorName") +
                getFieldAsSaml(cvAddressArea, "CvaddressArea") +
                getFieldAsSaml(thoroughfare, "Thoroughfare") +
                getFieldAsSaml(postName, "PostName") +
                getFieldAsSaml(adminunitFirstLine, "AdminunitFirstline") +
                getFieldAsSaml(adminunitSecondLine, "AdminunitSecondline") +
                getFieldAsSaml(postCode, "PostCode");

        return Base64.getEncoder().encodeToString(addressAsSamlString.getBytes(UTF_8));
    }

    private String getFieldAsSaml(String value, String samlTag) {
        if(value != null && !value.isEmpty()) {
            return MessageFormat.format("<eidas:{0}>{1}</eidas:{0}>", samlTag, value);
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EidasAddress that = (EidasAddress) o;
        return Objects.equals(poBox, that.poBox) && Objects.equals(locatorDesignator, that.locatorDesignator) && Objects.equals(locatorName, that.locatorName) && Objects.equals(cvAddressArea, that.cvAddressArea) && Objects.equals(thoroughfare, that.thoroughfare) && Objects.equals(postName, that.postName) && Objects.equals(adminunitFirstLine, that.adminunitFirstLine) && Objects.equals(adminunitSecondLine, that.adminunitSecondLine) && Objects.equals(postCode, that.postCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(poBox, locatorDesignator, locatorName, cvAddressArea, thoroughfare, postName, adminunitFirstLine, adminunitSecondLine, postCode);
    }
}
