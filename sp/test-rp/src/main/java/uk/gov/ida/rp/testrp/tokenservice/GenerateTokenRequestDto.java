package uk.gov.ida.rp.testrp.tokenservice;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.joda.time.DateTime;

import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GenerateTokenRequestDto {
    private Instant validUntil;
    private String issueTo;

    // all of these annotations are required, otherwise an error won't be thrown for missing fields
    @JsonCreator
    public GenerateTokenRequestDto(@JsonProperty(value = "validUntil", required = true) Instant validUntil,
                                   @JsonProperty(value = "issueTo", required = true) String issueTo) {
        this.validUntil = validUntil;
        this.issueTo = issueTo;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public String getIssueTo() {
        return issueTo;
    }
}
