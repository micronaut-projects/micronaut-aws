package io.micronaut.function.aws.proxy;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.config.SecurityConfiguration;
import io.micronaut.security.config.SecurityConfigurationProperties;
import io.micronaut.security.rules.AbstractSecurityRule;
import io.micronaut.security.rules.IpPatternsRule;
import io.micronaut.security.rules.SecuredAnnotationRule;
import io.micronaut.security.rules.SecurityRuleResult;
import io.micronaut.security.token.RolesFinder;
import io.micronaut.web.router.RouteMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//TODO delete when https://github.com/micronaut-projects/micronaut-security/issues/219 fix is merged and released
@Replaces(IpPatternsRule.class)
@Singleton
public class IpPatternsRuleWorkaround extends AbstractSecurityRule {

    /**
     * The order of the rule.
     */
    public static final Integer ORDER = SecuredAnnotationRule.ORDER - 100;

    private static final Logger LOG = LoggerFactory.getLogger(IpPatternsRuleWorkaround.class);

    private final List<Pattern> patternList;

    /**
     * @param rolesFinder Roles Parser
     * @param securityConfiguration Security Configuration
     */
    @Inject
    public IpPatternsRuleWorkaround(RolesFinder rolesFinder,
                          SecurityConfiguration securityConfiguration) {
        super(rolesFinder);
        this.patternList = securityConfiguration.getIpPatterns()
                .stream()
                .map(Pattern::compile)
                .collect(Collectors.toList());
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public SecurityRuleResult check(HttpRequest<?> request, @Nullable RouteMatch<?> routeMatch, @Nullable Map<String, Object> claims) {

        if (patternList.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No IP patterns provided. Skipping host address check.");
            }
            return SecurityRuleResult.UNKNOWN;
        } else {
            InetSocketAddress socketAddress = request.getRemoteAddress();
            //noinspection ConstantConditions https://github.com/micronaut-projects/micronaut-security/issues/186
            if (socketAddress != null) {
                if (socketAddress.getAddress() != null) {
                    String hostAddress = socketAddress.getAddress().getHostAddress();
                    if (patternList.stream().anyMatch(pattern ->
                            pattern.pattern().equals(SecurityConfigurationProperties.ANYWHERE) ||
                                    pattern.matcher(hostAddress).matches())) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("One or more of the IP patterns matched the host address [{}]. Continuing request processing.", hostAddress);
                        }
                        return SecurityRuleResult.UNKNOWN;
                    } else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("None of the IP patterns [{}] matched the host address [{}]. Rejecting the request.", patternList.stream().map(Pattern::pattern).collect(Collectors.toList()), hostAddress);
                        }
                        return SecurityRuleResult.REJECTED;
                    }
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Could not resolve the {@code InetAddress}. Continuing request processing.");
                    }
                    return SecurityRuleResult.UNKNOWN;
                }
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Request remote address was not found. Continuing request processing.");
                }
                return SecurityRuleResult.UNKNOWN;
            }
        }
    }

}
