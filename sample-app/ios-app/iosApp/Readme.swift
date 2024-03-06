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
                endpoints: Endpoints(
                    tokenEndpoint: "<tokenEndpoint>",
                    authorizationEndpoint: "<authorizationEndpoint>",
                    userInfoEndpoint: nil,
                    endSessionEndpoint: "<endSessionEndpoint>"
                ),
                clientId: "<clientId>",
                clientSecret: "<clientSecret>",
                scope: "openid profile",
                codeChallengeMethod: .s256,
                redirectUri: "<redirectUri>"
            )
        )
    }
    
    // Request access token using code auth flow:
    func _2() async {
        let flow = CodeAuthFlow(client: client)
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
