package stubidp.saml.domain.matching;

import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.MatchingDataset;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.saml.domain.assertions.TransliterableMdsValue;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MatchingDatasetBuilder {
    private final List<TransliterableMdsValue> firstnames = new ArrayList<>();
    private final List<SimpleMdsValue<String>> middlenames = new ArrayList<>();
    private final List<TransliterableMdsValue> surnames = new ArrayList<>();
    private Optional<SimpleMdsValue<Gender>> gender = Optional.empty();
    private final List<SimpleMdsValue<LocalDate>> dateOfBirths = new ArrayList<>();
    private final List<Address> currentAddresses = new ArrayList<>();
    private final List<Address> previousAddresses = new ArrayList<>();
    private String personalId;

    public MatchingDatasetBuilder() {
    }

    public void addFirstNames(List<TransliterableMdsValue> firstnames) {
        this.firstnames.addAll(firstnames);
    }

    public void addSurnames(List<TransliterableMdsValue> surnames) {
        this.surnames.addAll(surnames);
    }

    public void gender(SimpleMdsValue<Gender> gender) {
        this.gender = Optional.ofNullable(gender);
    }

    public void dateOfBirth(List<SimpleMdsValue<LocalDate>> dateOfBirths) {
        this.dateOfBirths.addAll(dateOfBirths);
    }

    public void addCurrentAddresses(List<Address> currentAddresses) {
        this.currentAddresses.addAll(currentAddresses);
    }

    public void personalId(String personalId) {
        this.personalId = personalId;
    }

    public void middlenames(List<SimpleMdsValue<String>> middlenames) {
        this.middlenames.addAll(middlenames);
    }

    public void addPreviousAddresses(List<Address> previousAddresses) {
        this.previousAddresses.addAll(previousAddresses);
    }

    public MatchingDataset build() {
        return new MatchingDataset(
                firstnames,
                middlenames,
                surnames,
                gender,
                dateOfBirths,
                currentAddresses,
                previousAddresses,
                personalId
        );
    }
}
