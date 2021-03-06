package stubidp.stubidp.services;

import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.stubidp.domain.DatabaseEidasUser;
import stubidp.stubidp.domain.EidasScheme;
import stubidp.stubidp.domain.EidasUser;
import stubidp.stubidp.exceptions.IncompleteRegistrationException;
import stubidp.stubidp.exceptions.InvalidSessionIdException;
import stubidp.stubidp.exceptions.InvalidUsernameOrPasswordException;
import stubidp.stubidp.exceptions.UsernameAlreadyTakenException;
import stubidp.stubidp.repositories.EidasSession;
import stubidp.stubidp.repositories.EidasSessionRepository;
import stubidp.stubidp.repositories.StubCountry;
import stubidp.stubidp.repositories.StubCountryRepository;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public class StubCountryService {

    private final StubCountryRepository stubCountryRepository;
    private final EidasSessionRepository sessionRepository;

    @Inject
    public StubCountryService(StubCountryRepository stubCountryRepository, EidasSessionRepository sessionRepository) {
        this.stubCountryRepository = stubCountryRepository;
        this.sessionRepository = sessionRepository;
    }

    public void attachStubCountryToSession(EidasScheme eidasScheme, String username, String password, boolean signAssertions, EidasSession session) throws InvalidUsernameOrPasswordException, InvalidSessionIdException {
        StubCountry stubCountry = stubCountryRepository.getStubCountryWithFriendlyId(eidasScheme);
        Optional<DatabaseEidasUser> user = stubCountry.getUser(username, password);
        session.setSignAssertions(signAssertions);
        attachEidasUserToSession(user, session);
    }

    public void createAndAttachIdpUserToSession(EidasScheme eidasScheme,
                                                String username, String password,
                                                EidasSession idpSessionId,
                                                String firstName,
                                                String nonLatinFirstname,
                                                String surname,
                                                String nonLatinSurname,
                                                String dob,
                                                AuthnContext levelOfAssurance) throws InvalidSessionIdException, InvalidUsernameOrPasswordException, IncompleteRegistrationException, UsernameAlreadyTakenException {
        StubCountry stubCountry = stubCountryRepository.getStubCountryWithFriendlyId(eidasScheme);
        DatabaseEidasUser user = createEidasUserInStubCountry(
                username, password, stubCountry, firstName, nonLatinFirstname,
                surname, nonLatinSurname, dob, levelOfAssurance
        );
        attachEidasUserToSession(Optional.of(user), idpSessionId);
    }

    private DatabaseEidasUser createEidasUserInStubCountry(String username,
                                                           String password,
                                                           StubCountry stubCountry,
                                                           String firstName,
                                                           String nonLatinFirstname,
                                                           String surname,
                                                           String nonLatinSurname,
                                                           String dob,
                                                           AuthnContext levelOfAssurance)
            throws IncompleteRegistrationException, UsernameAlreadyTakenException {

        if (!isMandatoryDataPresent(firstName, surname, dob, username, password)) {
            throw new IncompleteRegistrationException();
        }

        LocalDate parsedDateOfBirth = LocalDate.parse(dob);

        boolean usernameAlreadyTaken = stubCountry.userExists(username);
        if (usernameAlreadyTaken) {
            throw new UsernameAlreadyTakenException();
        }

        return stubCountry.createUser(
                username, password,
                createMdsValue(firstName), createOptionalMdsValue(nonLatinFirstname),
                createMdsValue(surname), createOptionalMdsValue(nonLatinSurname),
                createMdsValue(parsedDateOfBirth),
                levelOfAssurance);
    }

    private void attachEidasUserToSession(Optional<DatabaseEidasUser> user, EidasSession session) throws InvalidUsernameOrPasswordException, InvalidSessionIdException {
        if (user.isEmpty()) {
            throw new InvalidUsernameOrPasswordException();
        }
        EidasUser eidasUser = createEidasUser(user);

        session.setEidasUser(eidasUser);

        if (session.getEidasUser().isEmpty()) {
            throw new InvalidSessionIdException();
        }
        sessionRepository.updateSession(session.getSessionId(), session);
    }

    private EidasUser createEidasUser(Optional<DatabaseEidasUser> optionalUser) {

        DatabaseEidasUser user = optionalUser.get();

        return new EidasUser(
                user.getFirstname().getValue(),
                getOptionalValue(user.getNonLatinFirstname()),
                user.getSurname().getValue(),
                getOptionalValue(user.getNonLatinSurname()),
                user.getPersistentId(),
                user.getDateOfBirth().getValue(),
                Optional.empty(),
                Optional.empty()
        );
    }

    private Optional<String> getOptionalValue(Optional<SimpleMdsValue<String>> fieldValue) {
        return fieldValue.map(SimpleMdsValue::getValue);
    }

    private <T> SimpleMdsValue<T> createMdsValue(T value) {
        return new SimpleMdsValue<>(value, null, null, true);
    }

    private Optional<SimpleMdsValue<String>> createOptionalMdsValue(String value) {
        if (Objects.isNull(value) || value.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new SimpleMdsValue<>(value, null, null, true));
    }

    private boolean isMandatoryDataPresent(String... args) {
        for (String arg : args) {
            if (arg == null || arg.trim().isEmpty()) {
                return false;
            }
        }

        return true;
    }
}
