package com.github.gianlucampos.auth;

import com.google.auth.oauth2.UserCredentials;
import java.io.IOException;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GmailOAuth {

    private final String clientId;
    private final String clientSecret;
    private final String refreshToken;

    public String getAccessToken() throws IOException {
        UserCredentials creds = UserCredentials.newBuilder()
            .setClientId(clientId)
            .setClientSecret(clientSecret)
            .setRefreshToken(refreshToken)
            .build();
        return creds.refreshAccessToken().getTokenValue();
    }
}
