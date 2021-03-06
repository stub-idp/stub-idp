package stubidp.saml.metadata;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opensaml.core.config.InitializationException;
import org.opensaml.core.config.InitializationService;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.soap.wsaddressing.impl.AddressBuilder;
import org.opensaml.xmlsec.signature.support.SignatureException;
import stubidp.saml.test.metadata.EntityDescriptorFactory;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static stubidp.saml.test.builders.EntitiesDescriptorBuilder.anEntitiesDescriptor;

public class EntitiesDescriptorNamePredicateTest {

    @BeforeEach
    void setUp() throws InitializationException {
        InitializationService.initialize();
    }

    @Test
    void shouldApplyForEntityWithExpectedParent() throws MarshallingException, SignatureException {
        String entitiesName = "collection of entities";

        EntityDescriptor entityDescriptor = new EntityDescriptorFactory().idpEntityDescriptor("an idp");
        anEntitiesDescriptor()
                .withEntityDescriptors(Collections.singletonList(entityDescriptor))
                .withName(entitiesName)
                .build();

        EntitiesDescriptorNamePredicate entitiesDescriptorNamePredicate = new EntitiesDescriptorNamePredicate(
                new EntitiesDescriptorNameCriterion(entitiesName));

        assertThat(entitiesDescriptorNamePredicate.test(entityDescriptor)).isTrue();

    }

    @Test
    void shouldNotApplyForEntityWithWrongParent() throws MarshallingException, SignatureException {
        EntityDescriptor entityDescriptor = new EntityDescriptorFactory().idpEntityDescriptor("an idp");
        anEntitiesDescriptor()
                .withEntityDescriptors(Collections.singletonList(entityDescriptor))
                .withName("collection of entities")
                .build();

        EntitiesDescriptorNamePredicate entitiesDescriptorNamePredicate = new EntitiesDescriptorNamePredicate(
                new EntitiesDescriptorNameCriterion("some other parent"));

        assertThat(entitiesDescriptorNamePredicate.test(entityDescriptor)).isFalse();
    }

    @Test
    void shouldNotApplyForEntityWithNoParent() {
        EntityDescriptor entityDescriptor = new EntityDescriptorFactory().idpEntityDescriptor("an idp");

        EntitiesDescriptorNamePredicate entitiesDescriptorNamePredicate = new EntitiesDescriptorNamePredicate(
                new EntitiesDescriptorNameCriterion("some other parent"));

        assertThat(entitiesDescriptorNamePredicate.test(entityDescriptor)).isFalse();
    }

    @Test
    void shouldNotApplyForEntityWithNamelessParent() throws MarshallingException, SignatureException {
        EntityDescriptor entityDescriptor = new EntityDescriptorFactory().idpEntityDescriptor("an idp");
        anEntitiesDescriptor()
                .withEntityDescriptors(Collections.singletonList(entityDescriptor))
                .withName(null)
                .build();

        EntitiesDescriptorNamePredicate entitiesDescriptorNamePredicate = new EntitiesDescriptorNamePredicate(
                new EntitiesDescriptorNameCriterion("some other parent"));

        assertThat(entitiesDescriptorNamePredicate.test(entityDescriptor)).isFalse();
    }

    @Test
    void shouldNotApplyForEntityWithWrongParentType() {
        EntityDescriptor entityDescriptor = new EntityDescriptorFactory().idpEntityDescriptor("an idp");
        entityDescriptor.setParent(new AddressBuilder().buildObject("Some", "Other", "Type"));

        EntitiesDescriptorNamePredicate entitiesDescriptorNamePredicate = new EntitiesDescriptorNamePredicate(
                new EntitiesDescriptorNameCriterion("some other parent"));

        assertThat(entitiesDescriptorNamePredicate.test(entityDescriptor)).isFalse();
    }

}
