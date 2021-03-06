package org.springsource.examples.spring31.web.config;

import org.springframework.context.annotation.*;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.vote.AuthenticatedVoter;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenEndpointFilter;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;
import org.springframework.security.oauth2.provider.error.OAuth2AuthenticationEntryPoint;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.InMemoryTokenStore;
import org.springframework.security.oauth2.provider.vote.ScopeVoter;
import org.springsource.examples.spring31.web.security.RoleAwareOAuthTokenServicesUserApprovalHandler;

import java.util.ArrayList;
import java.util.List;


/**
 * class for all the security aware parts of the system like form-based login and OAuth
 *
 * @author Josh long
 */
@Configuration
@ImportResource({"classpath:/security/security.xml"})
public class SecurityConfiguration {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public TextEncryptor textEncryptor() {
        return Encryptors.noOpText();
    }

    @Bean
    public InMemoryTokenStore tokenStore() {
        return new InMemoryTokenStore();
    }

    @Bean
    public UnanimousBased accessDecisionManager() {
        List<AccessDecisionVoter> decisionVoters = new ArrayList<AccessDecisionVoter>();
        decisionVoters.add(new ScopeVoter());
        decisionVoters.add(new RoleVoter());
        decisionVoters.add(new AuthenticatedVoter());
        return new UnanimousBased(decisionVoters);
    }

    @Bean
    public DefaultTokenServices tokenServices(InMemoryTokenStore tokenStore, ClientDetailsService jpaUserCredentialsService) {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore);
        defaultTokenServices.setSupportRefreshToken(true);
        defaultTokenServices.setClientDetailsService(jpaUserCredentialsService);
        return defaultTokenServices;
    }

    @Bean
    public OAuth2AuthenticationEntryPoint oauthAuthenticationEntryPoint() {
        OAuth2AuthenticationEntryPoint oAuth2AuthenticationEntryPoint = new OAuth2AuthenticationEntryPoint();
        oAuth2AuthenticationEntryPoint.setRealmName("crm");
        return oAuth2AuthenticationEntryPoint;
    }

    @Bean
    public OAuth2AccessDeniedHandler oauthAccessDeniedHandler() {
        return new OAuth2AccessDeniedHandler();
    }

    @Bean
    @Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
    public ClientCredentialsTokenEndpointFilter clientCredentialsTokenEndpointFilter(AuthenticationManager authenticationManager) {
        ClientCredentialsTokenEndpointFilter endpointFilter = new ClientCredentialsTokenEndpointFilter() {
        };
        // todo I had to subclass this because it's package private by default
        // todo NB this will be fixed in a later version of Spring Security OAuth
        endpointFilter.setAuthenticationManager(authenticationManager);
        return endpointFilter;

    }

    @Bean
    public RoleAwareOAuthTokenServicesUserApprovalHandler oauthTokenServicesUserApprovalHandler(AuthorizationServerTokenServices tokenServices) {
        RoleAwareOAuthTokenServicesUserApprovalHandler approvalHandler = new RoleAwareOAuthTokenServicesUserApprovalHandler();
        approvalHandler.setUseTokenServices(true);
        approvalHandler.setTokenServices(tokenServices);
        return approvalHandler;
    }

}



