package stubidp.saml.hub.domain;

import stubidp.saml.domain.assertions.AuthnContext;

import java.util.Arrays;

public enum LevelOfAssurance implements Comparable<LevelOfAssurance> {

    LOW("http://eidas.europa.eu/LoA/low"),

    SUBSTANTIAL("http://eidas.europa.eu/LoA/substantial"),

    HIGH("http://eidas.europa.eu/LoA/high");

    private final String value;

    LevelOfAssurance(String value) {
        this.value = value;
    }

    public static LevelOfAssurance fromString(String levelOfAssurance) {
        return Arrays.stream(values())
            .filter(x -> x.value.equals(levelOfAssurance))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Unknown level of Assurance"));
    }

    public AuthnContext toVerifyLevelOfAssurance() {
        return switch (this) {
            case LOW -> AuthnContext.LEVEL_1;
            case SUBSTANTIAL -> AuthnContext.LEVEL_2;
            case HIGH -> AuthnContext.LEVEL_2; // Verify doesn't support LoA 3 yet, so return LoA 2
            default -> throw new IllegalStateException("Unknown level of assurance from requested AuthnContext : " + this.value);
        };
    }

    @Override
    public String toString() {
        return value;
    }
}
