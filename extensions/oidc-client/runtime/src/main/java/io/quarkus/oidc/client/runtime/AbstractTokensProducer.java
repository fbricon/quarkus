package io.quarkus.oidc.client.runtime;

import java.util.Objects;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.quarkus.arc.Arc;
import io.quarkus.oidc.client.OidcClient;
import io.quarkus.oidc.client.OidcClients;
import io.quarkus.oidc.client.Tokens;
import io.smallrye.mutiny.Uni;

public abstract class AbstractTokensProducer {
    private OidcClient oidcClient;

    @Inject
    @ConfigProperty(name = "quarkus.oidc-client.early-tokens-acquisition")
    public boolean earlyTokenAcquisition;

    final TokensHelper tokensHelper = new TokensHelper();

    @PostConstruct
    public void initTokens() {
        OidcClients oidcClients = Arc.container().instance(OidcClients.class).get();
        oidcClient = Objects.requireNonNull(clientId(), "clientId must not be null")
                .map(oidcClient -> Objects.requireNonNull(oidcClients.getClient(oidcClient), "Unknown client"))
                .orElseGet(oidcClients::getClient);

        if (earlyTokenAcquisition) {
            tokensHelper.initTokens(oidcClient);
        }
    }

    public Uni<Tokens> getTokens() {
        return tokensHelper.getTokens(oidcClient);
    }

    public Tokens awaitTokens() {
        return getTokens().await().indefinitely();
    }

    /**
     * @return optional ID of OIDC client to use for token acquisition.
     *         Defaults to default OIDC client when {@link Optional#empty() empty}.
     */
    protected Optional<String> clientId() {
        return Optional.empty();
    }
}
