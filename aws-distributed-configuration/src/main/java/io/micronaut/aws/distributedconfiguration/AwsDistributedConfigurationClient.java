package io.micronaut.aws.distributedconfiguration;

import io.micronaut.context.env.Environment;
import io.micronaut.context.env.MapPropertySource;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.discovery.config.ConfigurationClient;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static io.micronaut.aws.distributedconfiguration.AwsDistributedConfigurationProperties.STARTS_WITH;

/**
 * Client used to fetchSecrets. Currently not supporting ENV loading.
 * Should introduce load by currently active profile and respect load precedence.
 * For each application-env AwsDistributedConfigurationProperties.secrets should be used and AWS SDK called.
 * https://docs.micronaut.io/1.3.0.M1/guide/index.html#_included_propertysource_loaders
 *
 * This class could be abstracted to have List<KeyValueFetcherByPath> beans and resolve keys/paths by calling it.
 * Parsing and resolving String secret from properties should be handed to KeyValueFetcherByPath implementation.
 * With handing parsing and resolving we could then say if KeyValueFetcherByPath should resolve path/key or it should be other KeyValueFetcherByPath implementation.
 * With this approach we could reuse parameter and secrets manager integration.
 *
 * @author Sergio del Amo
 * @author Matej Nedic
 * @since ?
 */
public abstract class AwsDistributedConfigurationClient implements ConfigurationClient {

    private static final Logger LOG = LoggerFactory.getLogger(AwsDistributedConfigurationClient.class);
    private static final String SEMI_COLON = ";";
    private final KeyValueFetcherByPath keyValueFetcherByPath;
    private final AwsDistributedConfigurationProperties configurationPath;

    public AwsDistributedConfigurationClient(KeyValueFetcherByPath keyValueFetcherByPath,
                                             @Nullable AwsDistributedConfigurationProperties configurationPath) {
        this.keyValueFetcherByPath = keyValueFetcherByPath;
        this.configurationPath = configurationPath;
    }


    @Override
    public Publisher<PropertySource> getPropertySources(Environment environment) {
        List<String> secrets = parseSecrets(configurationPath.getSecrets());
        List<SecretRepresentation> secretRepresentations = getRepresentation(secrets);
        Map<String, String> configurationResolutionPrefixesValues = new HashMap<>();

        for (SecretRepresentation secretRepresentation : secretRepresentations) {
            Optional<Map<String,String>> keyValuesOptional = keyValueFetcherByPath.keyValuesByPrefix(secretRepresentation.getKey(), secretRepresentation.getVersionId());
            keyValuesOptional.ifPresent(stringStringMap -> stringStringMap.forEach(configurationResolutionPrefixesValues::put));
        }
        String propertySourceName = getPropertySourceName();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Property source {} with #{} items", propertySourceName, configurationResolutionPrefixesValues.size());
        }
        if (LOG.isTraceEnabled()) {
            for (String k : configurationResolutionPrefixesValues.keySet()) {
                LOG.trace("property {} resolved", k);
            }
        }
        return Publishers.just(new MapPropertySource(propertySourceName, configurationResolutionPrefixesValues));
    }

    @NonNull
    protected abstract String getPropertySourceName();

    private List<SecretRepresentation> getRepresentation(List<String> secrets) {
        List<SecretRepresentation> secretRepresentations = new ArrayList<>();
        secrets.parallelStream().forEach(
                secret -> {
                    if(secret.contains(":")) {
                        int location = secret.charAt(':');
                        secretRepresentations.add(new SecretRepresentation(secret.substring(0, location), secret.substring(location+1)));
                    } else {
                        secretRepresentations.add(new SecretRepresentation(secret, null));
                    }
                }
        );
        return secretRepresentations;
    }


    private List<String> parseSecrets(String secret) {
        if (secret.contains(STARTS_WITH)) {
            return Arrays.asList(secret.replaceFirst(STARTS_WITH, "").split(SEMI_COLON));
        } else {
            //Should be removed after validation is set
            throw new UnsupportedOperationException("Failed application since prefix is not found! Please include " + STARTS_WITH + " as prefix");
        }
    }

}
