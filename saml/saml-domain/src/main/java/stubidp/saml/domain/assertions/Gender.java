package stubidp.saml.domain.assertions;

public enum Gender {
    FEMALE("Female"),
    MALE("Male"),
    NOT_SPECIFIED("Not Specified");

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Gender fromString(String string) {
        for (Gender gender : values()) {
            if (gender.getValue().equalsIgnoreCase(string)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Not a legal value for gender: " + string);
    }
}
