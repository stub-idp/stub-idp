package stubidp.utils.security.configuration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import stubidp.utils.security.security.PrivateKeyFactory;

import java.security.PrivateKey;

/* There is a bug being tracked here - https://github.com/FasterXML/jackson-databind/issues/1358
preventing us from deserializing directly to one of the JsonSubTypes when there is a defaultImpl
defined. This can be avoided by only ever to deserialize to the super type (PrivateKeyConfiguration) */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
        property = "type",
        defaultImpl = PrivateKeyFileConfiguration.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = PrivateKeyFileConfiguration.class, name = "file"),
        @JsonSubTypes.Type(value = EncodedPrivateKeyConfiguration.class, name = "encoded")
})
public abstract class PrivateKeyConfiguration {

    public PrivateKeyConfiguration() {
    }

    public abstract PrivateKey getPrivateKey();

    protected PrivateKey getPrivateKeyFromBytes(byte[] privateKey) {
        PrivateKeyFactory privateKeyFactory = new PrivateKeyFactory();
        return privateKeyFactory.createPrivateKey(privateKey);
    }
}
