package stubidp.utils.security.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import org.junit.jupiter.api.Test;
import stubidp.utils.security.configuration.EncodedPrivateKeyConfiguration;
import stubidp.utils.security.configuration.PrivateKeyConfiguration;
import stubidp.utils.security.configuration.PrivateKeyFileConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

public class PrivateKeyConfigurationTest {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void shouldDefaultToFileType() throws Exception {
        String path = getClass().getClassLoader().getResource("private_key.pk8").getPath();
        PrivateKeyConfiguration config = objectMapper.readValue("{\"key\": \"" + path + "\"}", PrivateKeyConfiguration.class);
        assertThat(config.getClass()).isEqualTo(PrivateKeyFileConfiguration.class);
    }

    @Test
    public void shouldUseFileTypeWhenSpecified() throws Exception {
        String path = getClass().getClassLoader().getResource("private_key.pk8").getPath();
        PrivateKeyConfiguration config = objectMapper.readValue("{\"type\": \"file\", \"key\": \"" + path + "\"}", PrivateKeyConfiguration.class);
        assertThat(config.getClass()).isEqualTo(PrivateKeyFileConfiguration.class);
    }

    @Test
    public void shouldUseEncodedTypeWhenSpecified() throws Exception {
        PrivateKeyConfiguration config = objectMapper.readValue("{\"type\": \"encoded\", \"key\": \"" + getKeyAsBase64() + "\"}", PrivateKeyConfiguration.class);
        assertThat(config.getClass()).isEqualTo(EncodedPrivateKeyConfiguration.class);
    }

    private String getKeyAsBase64() throws IOException {
        String path = Resources.getResource("private_key.pk8").getFile();
        byte[] key = Files.readAllBytes(new File(path).toPath());
        return Base64.getEncoder().encodeToString(key);
    }
}