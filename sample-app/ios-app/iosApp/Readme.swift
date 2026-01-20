import shared
//import OpenIdConnectClient


struct Readme {
    let client: OpenIdConnectClient
    let tokens: AccessTokenResponse
    let tokenstore: KeychainTokenStore
    let oldAccessToken: String
    
    // Create OpenID config and client:
    func _1() {
        let client = OpenIdConnectClient(
            config: OpenIdConnectClientConfig(
                discoveryUri: "<discovery url>",
                endpoints: Endpoints( // pass nil if you only want to use discovery
                    tokenEndpoint: "<tokenEndpoint>",
                    authorizationEndpoint: "<authorizationEndpoint>",
                    userInfoEndpoint: nil,
                    endSessionEndpoint: "<endSessionEndpoint>",
                    revocationEndpoint: "<revocationEndpoint>"
                ),
                clientId: "<clientId>",
                clientSecret: "<clientSecret>",
                scope: "openid profile",
                codeChallengeMethod: .s256,
                redirectUri: "<redirectUri>",
                postLogoutRedirectUri: "<postLogoutRedirectUri>",
                disableNonce: false
            )
        )
    }
    
    // Create OpenID client with custom http client configuration:
    func _1b() {
        let client = OpenIdConnectClient(
            httpClient: OpenIdConnectClient.companion.DefaultHttpClient.config(block: { config in
                config.installClientPlugin(
                    name: "customheader",
                    onRequest: { requestBuilder, content in
                        requestBuilder.headers.append(name: "User-Agent", value: "oidcclient")
                    },
                    onResponse: {_ in},
                    onClose: {}
                )
            }),
            config: OpenIdConnectClientConfig(
                discoveryUri: "<discovery url>",
                endpoints: nil,
                clientId: "<clientId>",
                clientSecret: "<clientSecret>",
                scope: "openid profile",
                codeChallengeMethod: .s256,
                redirectUri: "<redirectUri>",
                postLogoutRedirectUri: "<postLogoutRedirectUri>",
                disableNonce: false
            )
            
        )
    }
    
    // Request access token using code auth flow:
    func _2() async {
        let factory = CodeAuthFlowFactory(ephemeralBrowserSession: false)
        let flow = factory.createAuthFlow(client: client)
        do {
            let tokens = try await flow.getAccessToken()
        } catch {
            print(error)
        }
    }
    
    // Perform refresh or endSession:
    func _3() async throws {
        try await client.refreshToken(refreshToken: tokens.refresh_token!)
        try await client.endSession(idToken: tokens.id_token!)
    }
    
//     customize endSession request:
    func _3a() async throws {
        try await client.endSession(idToken: "") { requestBuilder in
            requestBuilder.headers.append(name: "X-CUSTOM-HEADER", value: "value")
            requestBuilder.url.parameters.append(name: "custom_parameter", value: "value")
        }
        // endSession with Web flow (opens browser and handles post_logout_redirect_uri redirect)
        let factory = CodeAuthFlowFactory(ephemeralBrowserSession: false)
        let flow = factory.createAuthFlow(client: client)
        try await flow.endSession(idToken: "<idToken>", configureEndSessionUrl: { urlBuilder in
        })
        
    }
    
//     customize getAccessToken request:
    func _3b() async throws {
        let factory = CodeAuthFlowFactory(ephemeralBrowserSession: false)
        let flow = factory.createAuthFlow(client: client)
        try await flow.getAccessToken(
            configureAuthUrl: { urlBuilder in
                urlBuilder.parameters.append(name: "prompt", value: "login")
            },
            configureTokenExchange: { requestBuilder in
                requestBuilder.headers.append(name: "additionalHeaderField", value: "value")
            }
        )
    }
    
    // We provide simple JWT parsing:
    func _4() {
        let jwt = tokens.id_token.map { try! JwtParser.shared.parse(from: $0) }
        print(jwt?.payload.aud) // print audience
        print(jwt?.payload.iss) // print issuer
        print(jwt?.payload.additionalClaims["email"]) // get claim
    }
    
    // TokenStore
    func _5() async throws {
        let tokenstore = KeychainTokenStore()
        try await tokenstore.saveTokens(tokens: tokens)
    }
    
    // RefreshHandler
    func _6() async throws {
        let refreshHandler = TokenRefreshHandler(tokenStore: tokenstore)
        try await refreshHandler.refreshAndSaveToken(client: client, oldAccessToken: oldAccessToken)  // thread-safe refresh and save new tokens to store
    }
}
