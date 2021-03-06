package stubsp.stubsp;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import stubidp.metrics.prometheus.bundle.PrometheusBundle;
import stubidp.saml.extensions.IdaSamlBootstrap;
import stubidp.saml.metadata.bundle.MetadataResolverBundle;
import stubidp.shared.csrf.CSRFViewRenderer;
import stubidp.utils.rest.bundles.LoggingBundle;
import stubidp.utils.rest.bundles.MonitoringBundle;
import stubidp.utils.rest.bundles.ServiceStatusBundle;
import stubidp.utils.rest.filters.AcceptLanguageFilter;
import stubsp.stubsp.configuration.StubSpConfiguration;
import stubsp.stubsp.filters.NoCacheResponseFilter;
import stubsp.stubsp.filters.RequireValidLoginFeature;
import stubsp.stubsp.filters.SecurityHeadersFilter;
import stubsp.stubsp.filters.StubSpCacheControlFilter;
import stubsp.stubsp.resources.AuthenticationFailedResource;
import stubsp.stubsp.resources.AvailableServicesResource;
import stubsp.stubsp.resources.InitiateSingleIdpJourneyResource;
import stubsp.stubsp.resources.RootResource;
import stubsp.stubsp.resources.SamlResponseResource;
import stubsp.stubsp.resources.SamlSpMetadataResource;
import stubsp.stubsp.resources.SecureResource;
import stubsp.stubsp.resources.SuccessResource;

import javax.servlet.DispatcherType;
import java.util.EnumSet;
import java.util.Locale;

import static java.util.Collections.singletonList;

public class StubSpApplication extends Application<StubSpConfiguration> {

    private MetadataResolverBundle<StubSpConfiguration> metadataResolverBundle;

    public static void main(String[] args) throws Exception {
        new StubSpApplication().run(args);
    }

    @Override
    public String getName() {
        return "Stub Sp Service";
    }

    @Override
    public final void initialize(Bootstrap<StubSpConfiguration> bootstrap) {

        // Enable variable substitution with environment variables
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );

        bootstrap.addBundle(new PrometheusBundle());
        bootstrap.addBundle(new ServiceStatusBundle<>());
        bootstrap.addBundle(new ViewBundle<>(singletonList(new CSRFViewRenderer())));
        bootstrap.addBundle(new LoggingBundle<>());
        bootstrap.addBundle(new MonitoringBundle());
        metadataResolverBundle = new MetadataResolverBundle<>(StubSpConfiguration::getMetadata, "idp-metadata");
        bootstrap.addBundle(metadataResolverBundle);

        bootstrap.addBundle(new AssetsBundle("/assets/", "/assets/"));

        bootstrap.getObjectMapper().enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Override
    public void run(StubSpConfiguration configuration, Environment environment) {
        IdaSamlBootstrap.bootstrap();

//        environment.servlets().addFilter("Cache Control", new StubSpCacheControlFilter(configuration)).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/consent"+Urls.ROUTE_SUFFIX);
        environment.servlets().addFilter("Remove Accept-Language headers", AcceptLanguageFilter.class).addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");

        environment.getObjectMapper().setDateFormat(new StdDateFormat().withLocale(Locale.UK));
        environment.jersey().register(new StubSpBinder(configuration, environment, metadataResolverBundle));

        // resources
        environment.jersey().register(RootResource.class);
        environment.jersey().register(SecureResource.class);
        environment.jersey().register(AuthenticationFailedResource.class);
        environment.jersey().register(SuccessResource.class);
        environment.jersey().register(AvailableServicesResource.class);
        environment.jersey().register(SamlResponseResource.class);
        environment.jersey().register(SamlSpMetadataResource.class);
        environment.jersey().register(InitiateSingleIdpJourneyResource.class);

        // filters
        environment.jersey().register(StubSpCacheControlFilter.class);
        environment.jersey().register(NoCacheResponseFilter.class);
        environment.jersey().register(SecurityHeadersFilter.class);
        environment.jersey().register(RequireValidLoginFeature.class);
    }
}