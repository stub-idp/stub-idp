package stubidp.stubidp.configuration;

import io.dropwizard.configuration.ConfigurationSourceProvider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class StringConfigurationSourceProvider implements ConfigurationSourceProvider {
    private final String configuration;

    public StringConfigurationSourceProvider(String configuration) {
        this.configuration = configuration;
    }

    @Override
    public InputStream open(String path) {
        return new ByteArrayInputStream(this.configuration.getBytes());
    }
}