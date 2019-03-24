package stubidp.stubidp;

import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import io.dropwizard.views.freemarker.FreemarkerViewRenderer;
import stubidp.saml.extensions.IdaSamlBootstrap;
import stubidp.stubidp.bundles.DatabaseMigrationBundle;
import stubidp.stubidp.configuration.StubIdpConfiguration;
import stubidp.stubidp.csrf.CSRFCheckProtectionFeature;
import stubidp.stubidp.csrf.CSRFViewRenderer;
import stubidp.stubidp.exceptions.mappers.CatchAllExceptionMapper;
import stubidp.stubidp.exceptions.mappers.FeatureNotEnabledExceptionMapper;
import stubidp.stubidp.exceptions.mappers.FileNotFoundExceptionMapper;
import stubidp.stubidp.exceptions.mappers.GenericStubIdpExceptionExceptionMapper;
import stubidp.stubidp.exceptions.mappers.IdpNotFoundExceptionMapper;
import stubidp.stubidp.exceptions.mappers.IdpUserNotFoundExceptionMapper;
import stubidp.stubidp.exceptions.mappers.SessionSerializationExceptionMapper;
import stubidp.stubidp.filters.NoCacheResponseFilter;
import stubidp.stubidp.filters.SecurityHeadersFilter;
import stubidp.stubidp.filters.SessionCookieValueMustExistAsASessionFeature;
import stubidp.stubidp.filters.StubIdpCacheControlFilter;
import stubidp.stubidp.healthcheck.DatabaseHealthCheck;
import stubidp.stubidp.healthcheck.StubIdpHealthCheck;
import stubidp.stubidp.resources.AuthnRequestReceiverResource;
import stubidp.stubidp.resources.GeneratePasswordResource;
import stubidp.stubidp.resources.UserResource;
import stubidp.stubidp.resources.eidas.EidasConsentResource;
import stubidp.stubidp.resources.eidas.EidasDebugPageResource;
import stubidp.stubidp.resources.eidas.EidasLoginPageResource;
import stubidp.stubidp.resources.eidas.EidasProxyNodeServiceMetadataResource;
import stubidp.stubidp.resources.eidas.EidasRegistrationPageResource;
import stubidp.stubidp.resources.idp.ConsentResource;
import stubidp.stubidp.resources.idp.DebugPageResource;
import stubidp.stubidp.resources.idp.HeadlessIdpResource;
import stubidp.stubidp.resources.idp.LoginPageResource;
import stubidp.stubidp.resources.idp.RegistrationPageResource;
import stubidp.stubidp.resources.idp.SecureLoginPageResource;
import stubidp.stubidp.resources.idp.SecureRegistrationPageResource;
import stubidp.stubidp.resources.singleidp.SingleIdpHomePageResource;
import stubidp.stubidp.resources.singleidp.SingleIdpLogoutPageResource;
import stubidp.stubidp.resources.singleidp.SingleIdpPreRegistrationResource;
import stubidp.stubidp.resources.singleidp.SingleIdpStartPromptPageResource;
import stubidp.utils.rest.bundles.LoggingBundle;
import stubidp.utils.rest.bundles.MonitoringBundle;
import stubidp.utils.rest.bundles.ServiceStatusBundle;
import stubidp.utils.rest.filters.AcceptLanguageFilter;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.Map;

import static java.util.Collections.singletonList;

public class StubIdpApplication extends Application<StubIdpConfiguration> {

    public static void main(String[] args) {
        try {
            if (args == null || args.length == 0) {
                String configFile = System.getenv("CONFIG_FILE");

                if (configFile == null) {
                    throw new RuntimeException("CONFIG_FILE environment variable should be set with path to configuration file");
                }

                new StubIdpApplication().run("server", configFile);
            } else {
                new StubIdpApplication().run(args);
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public String getName() {
        return "Stub Idp Service";
    }

    @Override
    public final void initialize(Bootstrap<StubIdpConfiguration> bootstrap) {

        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
            new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                new EnvironmentVariableSubstitutor(false)
            )
        );

        bootstrap.addBundle(new DatabaseMigrationBundle());

        bootstrap.addBundle(new ServiceStatusBundle());
        bootstrap.addBundle(new ViewBundle<StubIdpConfiguration>(singletonList(new CSRFViewRenderer())) {
            @Override
            public Map<String, Map<String, String>> getViewConfiguration(StubIdpConfiguration config) {
                // beware: this is to force enable escaping of unsanitised user input
                return ImmutableMap.of(new FreemarkerViewRenderer().getConfigurationKey(),
                    ImmutableMap.of(
                        "output_format", "HTMLOutputFormat"
                    ));
            }
        });
        bootstrap.addBundle(new LoggingBundle());
        bootstrap.addBundle(new MonitoringBundle());

        bootstrap.addBundle(new AssetsBundle("/assets/", "/assets/"));
        bootstrap.getObjectMapper().registerModule(new Jdk8Module());
    }

    @Override
    public final void run(StubIdpConfiguration configuration, Environment environment) {
        IdaSamlBootstrap.bootstrap();
        environment.servlets().addFilter("Cache Control", new StubIdpCacheControlFilter(configuration)).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/consent"+Urls.ROUTE_SUFFIX);
        environment.servlets().addFilter("Remove Accept-Language headers", AcceptLanguageFilter.class).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

        environment.jersey().register(SessionCookieValueMustExistAsASessionFeature.class);
        environment.jersey().register(CSRFCheckProtectionFeature.class);

        environment.getObjectMapper().setDateFormat(new ISO8601DateFormat());

        environment.jersey().register(new StubIdpBinder(configuration, environment));

        // idp resources
        environment.jersey().register(AuthnRequestReceiverResource.class);
        environment.jersey().register(DebugPageResource.class);
        environment.jersey().register(ConsentResource.class);

        // single idp resources
        if(configuration.isSingleIdpJourneyEnabled()) {
            environment.jersey().register(SingleIdpStartPromptPageResource.class);
            environment.jersey().register(SingleIdpLogoutPageResource.class);
            environment.jersey().register(SingleIdpHomePageResource.class);
            environment.jersey().register(SingleIdpPreRegistrationResource.class);

            environment.jersey().register(LoginPageResource.class);
            environment.jersey().register(RegistrationPageResource.class);
        } else {
            environment.jersey().register(SecureLoginPageResource.class);
            environment.jersey().register(SecureRegistrationPageResource.class);
        }

        // proxy node resources
        environment.jersey().register(EidasLoginPageResource.class);
        environment.jersey().register(EidasConsentResource.class);
        environment.jersey().register(EidasRegistrationPageResource.class);
        environment.jersey().register(EidasProxyNodeServiceMetadataResource.class);
        environment.jersey().register(EidasDebugPageResource.class);

        // other idp resources
        environment.jersey().register(UserResource.class);
        environment.jersey().register(HeadlessIdpResource.class);
        environment.jersey().register(GeneratePasswordResource.class);

        //exception mappers
        environment.jersey().register(IdpNotFoundExceptionMapper.class);
        environment.jersey().register(IdpUserNotFoundExceptionMapper.class);
        environment.jersey().register(FileNotFoundExceptionMapper.class);
        environment.jersey().register(SessionSerializationExceptionMapper.class);
        environment.jersey().register(FeatureNotEnabledExceptionMapper.class);
        environment.jersey().register(GenericStubIdpExceptionExceptionMapper.class);
        environment.jersey().register(CatchAllExceptionMapper.class);

        //filters
        environment.jersey().register(NoCacheResponseFilter.class);
        environment.jersey().register(SecurityHeadersFilter.class);

        //health checks
        StubIdpHealthCheck healthCheck = new StubIdpHealthCheck();
        environment.healthChecks().register(healthCheck.getName(), healthCheck);

        DatabaseHealthCheck dbHealthCheck = new DatabaseHealthCheck(configuration.getDatabaseConfiguration().getUrl());
        environment.healthChecks().register("database", dbHealthCheck);
    }
}
