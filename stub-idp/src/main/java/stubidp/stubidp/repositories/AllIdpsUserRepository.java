package stubidp.stubidp.repositories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stubidp.saml.domain.assertions.Address;
import stubidp.saml.domain.assertions.AuthnContext;
import stubidp.saml.domain.assertions.Gender;
import stubidp.saml.domain.assertions.SimpleMdsValue;
import stubidp.stubidp.domain.DatabaseEidasUser;
import stubidp.stubidp.domain.DatabaseIdpUser;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static stubidp.stubidp.repositories.StubCountryRepository.STUB_COUNTRY_FRIENDLY_ID;

@Singleton
public class AllIdpsUserRepository {

    private final UserRepository userRepository;
    private static final Logger LOG = LoggerFactory.getLogger(AllIdpsUserRepository.class);

    @Inject
    public AllIdpsUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    void createHardcodedTestUsersForIdp(String idpFriendlyId, String assetId){
        LOG.debug("Creating hard coded users for IDP: " + idpFriendlyId);
        List<DatabaseIdpUser> sacredUsers = HardCodedTestUserList.getHardCodedTestUsers(assetId);

        for (DatabaseIdpUser sacredUser : sacredUsers) {
            addUserForIdp(idpFriendlyId, sacredUser);
        }
    }

    void createHardcodedTestUsersForCountries(String countryFriendlyId, String assetId){
        LOG.debug("Creating hard coded users for Country: " + countryFriendlyId);
        List<DatabaseEidasUser> sacredUsers = HardCodedTestUserList.getHardCodedCountryTestUsers(assetId);

        for (DatabaseEidasUser sacredUser : sacredUsers) {
            addUserForStubCountry(countryFriendlyId, sacredUser);
        }
    }

    DatabaseIdpUser createUserForIdp(String idpFriendlyName,
                                     String persistentId,
                                     List<SimpleMdsValue<String>> firstnames,
                                     List<SimpleMdsValue<String>> middleNames,
                                     List<SimpleMdsValue<String>> surnames,
                                     Optional<SimpleMdsValue<Gender>> gender,
                                     List<SimpleMdsValue<LocalDate>> dateOfBirths,
                                     List<Address> addresses,
                                     String username,
                                     String password,
                                     AuthnContext levelOfAssurance) {


        DatabaseIdpUser user = new DatabaseIdpUser(
                username,
                persistentId,
                password,
                firstnames,
                middleNames,
                surnames,
                gender,
                dateOfBirths,
                addresses,
                levelOfAssurance);
        user.hashPassword();

        addUserForIdp(idpFriendlyName, user);

        return user;
    }

    DatabaseEidasUser createUserForStubCountry(String countryFriendlyName,
                                               String persistentId,
                                               String username,
                                               String password,
                                               SimpleMdsValue<String> firstName,
                                               Optional<SimpleMdsValue<String>> nonLatinFirstName,
                                               SimpleMdsValue<String> surname,
                                               Optional<SimpleMdsValue<String>> nonLatinSurname,
                                               SimpleMdsValue<LocalDate> dob,
                                               AuthnContext levelOfAssurance){
        DatabaseEidasUser user = new DatabaseEidasUser(
                username, persistentId, password,
                firstName, nonLatinFirstName, surname, nonLatinSurname,
                dob, levelOfAssurance
        );
        user.hashPassword();

        addUserForStubCountry(countryFriendlyName, user);

        return user;
    }

    Collection<DatabaseIdpUser> getAllUsersForIdp(String idpFriendlyName) {
        return userRepository.getUsersForIdp(idpFriendlyName);
    }

    Optional<DatabaseIdpUser> getUserForIdp(String idpFriendlyName, String username) {
        return userRepository.getUsersForIdp(idpFriendlyName)
                .stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    Optional<DatabaseEidasUser> getUserForCountry(String countryFriendlyName, String username) {
        return userRepository.getUsersForCountry(STUB_COUNTRY_FRIENDLY_ID)
                .stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }

    boolean containsUserForIdp(String idpFriendlyName, String username) {
        return getUserForIdp(idpFriendlyName, username).isPresent();
    }

    private void addUserForIdp(String idpFriendlyName, DatabaseIdpUser user) {
        LOG.debug("Creating user " + user.getUsername() + " for IDP " + idpFriendlyName);
        userRepository.addOrUpdateUserForIdp(idpFriendlyName, user);
   }

   private void addUserForStubCountry(String stubCountryFriendlyName, DatabaseEidasUser user){
       LOG.debug("Creating user " + user.getUsername() + " for Stub Country " + stubCountryFriendlyName);
       userRepository.addOrUpdateEidasUserForStubCountry(stubCountryFriendlyName, user);
   }

    void deleteUserFromIdp(String idpFriendlyName, String username) {
        LOG.debug("Deleting user " + username + " from IDP " + idpFriendlyName);
        userRepository.deleteUserFromIdp(idpFriendlyName, username);
    }
}
