package org.publicvalue.multiplatform.oidc

import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import org.publicvalue.multiplatform.oidc.types.Jwt
import kotlin.test.Test

class JwtTest {

    @Test
    @Suppress("MaximumLineLength")
    fun test() {
        val id_token = "eyJhbGciOiJSUzI1NiIsImtpZCI6IjFlOWdkazcifQ.ewogImlzcyI6ICJodHRwOi8vc2VydmVyLmV4YW1wbGUuY29tIiwKICJzdWIiOiAiMjQ4Mjg5NzYxMDAxIiwKICJhdWQiOiAiczZCaGRSa3F0MyIsCiAibm9uY2UiOiAibi0wUzZfV3pBMk1qIiwKICJleHAiOiAxMzExMjgxOTcwLAogImlhdCI6IDEzMTEyODA5NzAKfQ.ggW8hZ1EuVLuxNuuIJKX_V8a_OMXzR0EHR9R6jgdqrOOF4daGU96Sr_P6qJp6IcmD3HP99Obi1PRs-cwh3LO-p146waJ8IhehcwL7F09JdijmBqkvPeB2T9CJNqeGpe-gccMg4vfKjkM8FcGvnzZUN4_KSP0aAp1tOJ1zZwgjxqGByKHiOtX7TpdQyHE5lcMiKPXfEIQILVq0pc_E2DzL7emopWoaoZTF_m0_N0YzFC6g6EJbOEoRoSK5hoDalrcvRYLSrQAZZKflyuVCyixEoV9GfNQC3_osjzw2PAithfubEEBLuVVk4XUVrWOLrLl0nx7RkKU8NXNHq-rvKMzqg"
        val jwt = Jwt.parse(id_token)
        assertThat(jwt.header.alg).isEqualTo("RS256")
        assertThat(jwt.header.kid).isEqualTo("1e9gdk7")

        assertThat(jwt.payload.iss).isEqualTo("http://server.example.com")
        assertThat(jwt.payload.sub).isEqualTo("248289761001")
        assertThat(jwt.payload.aud?.first()).isEqualTo("s6BhdRkqt3")
        assertThat(jwt.payload.nonce).isEqualTo("n-0S6_WzA2Mj")
        assertThat(jwt.payload.exp).isEqualTo(1311281970)
        assertThat(jwt.payload.iat).isEqualTo(1311280970)
    }

    @Test
    @Suppress("MaximumLineLength")
    fun brokenToken() {
        var e: Exception? = null
        try {
            @Suppress("MaximumLineLength")
            val id_token = "eyJhxbGciOiJSUzI1NiIsImtpZCI6IjFlOWdkazcifQ.ewogImlzcyI6ICJodHRwOi8vc2VydmVyLmV4YW1wbGUuY29tIiwKICJzdWIiOiAiMjQ4Mjg5NzYxMDAxIiwKICJhdWQiOiAiczZCaGRSa3F0MyIsCiAibm9uY2UiOiAibi0wUzZfV3pBMk1qIiwKICJleHAiOiAxMzExMjgxOTcwLAogImlhdCI6IDEzMTEyODA5NzAKfQ.ggW8hZ1EuVLuxNuuIJKX_V8a_OMXzR0EHR9R6jgdqrOOF4daGU96Sr_P6qJp6IcmD3HP99Obi1PRs-cwh3LO-p146waJ8IhehcwL7F09JdijmBqkvPeB2T9CJNqeGpe-gccMg4vfKjkM8FcGvnzZUN4_KSP0aAp1tOJ1zZwgjxqGByKHiOtX7TpdQyHE5lcMiKPXfEIQILVq0pc_E2DzL7emopWoaoZTF_m0_N0YzFC6g6EJbOEoRoSK5hoDalrcvRYLSrQAZZKflyuVCyixEoV9GfNQC3_osjzw2PAithfubEEBLuVVk4XUVrWOLrLl0nx7RkKU8NXNHq-rvKMzqg"
            Jwt.parse(id_token)
        } catch (ex: Exception) {
            e = ex
        }
        assertThat(e!!).isInstanceOf<OpenIdConnectException.TechnicalFailure>()
    }

    @Test
    @Suppress("MaximumLineLength")
    fun multipleAudiences() {
        @Suppress("MaximumLineLength")
        val idToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3MDE2OTQ3MDYsImV4cCI6MTczMzIzMDcwNiwiYXVkIjpbInd3dy5leGFtcGxlLmNvbSIsInNlY29uZCBhdWRpZW5jZSJdLCJzdWIiOiJqcm9ja2V0QGV4YW1wbGUuY29tIiwiRW1haWwiOiJiZWVAZXhhbXBsZS5jb20ifQ.k140fPrDnsTsBUPwkIs8g4Hfu-DTKwNsv8KDkowOu9g"
        val jwt = Jwt.parse(idToken)

        assertThat(jwt.payload.aud!!).containsExactlyInAnyOrder("second audience", "www.example.com")
    }

    @Test
    fun multipleAmr() {
        @Suppress("MaximumLineLength")
        val idToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3MTg2MTczNzIsImV4cCI6MTc1MDE1MzM3MiwiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoianJvY2tldEBleGFtcGxlLmNvbSIsIkdpdmVuTmFtZSI6IkpvaG5ueSIsImFtciI6WyJzbXMiLCJtZmEiLCJwd2QiXX0.t86lfvm-eV3c2JJP4WQLtDGXm3rWSCKy_MD2colIzLA"
        val jwt = Jwt.parse(idToken)

        assertThat(jwt.payload.amr!!).containsExactlyInAnyOrder("sms", "mfa", "pwd")
    }

    @Test
    fun additionalClaims() {
        @Suppress("MaximumLineLength")
        val idToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJPbmxpbmUgSldUIEJ1aWxkZXIiLCJpYXQiOjE3MDE2OTQ3MDYsImV4cCI6MTczMzIzMDcwNiwiYXVkIjoid3d3LmV4YW1wbGUuY29tIiwic3ViIjoianJvY2tldEBleGFtcGxlLmNvbSIsIkVtYWlsIjoiYmVlQGV4YW1wbGUuY29tIiwiaHR0cDovL3NjaGVtYXMubWljcm9zb2Z0LmNvbS93cy8yMDA4LzA2L2lkZW50aXR5L2NsYWltcy9yb2xlIjoiTWFuYWdlciIsImZhbWlseV9uYW1lIjoiU29tZW9uZSJ9.mjk1il9Nrr5JouTZ9kRcWb1R84bw30vdrYaw0CVsv9A"
        val jwt = Jwt.parse(idToken)

        assertThat(jwt.payload.additionalClaims.get("Email")).isEqualTo("bee@example.com")
        assertThat(
            jwt.payload.additionalClaims.get("http://schemas.microsoft.com/ws/2008/06/identity/claims/role")
        ).isEqualTo("Manager")
        assertThat(jwt.payload.additionalClaims.get("family_name")).isEqualTo("Someone")
    }

    @Test
    fun jsonObjectsInClaims() {
        @Suppress("MaximumLineLength")
        val idToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.ewogICAgInN1YiI6ICJhYWFhYWFhYS1iYmJiLWNjY2MtZGRkZC1lZWVlZWVlZWVlZWUiLAogICAgImNvZ25pdG86Z3JvdXBzIjogWwogICAgICAgICJ0ZXN0LWdyb3VwLWEiLAogICAgICAgICJ0ZXN0LWdyb3VwLWIiLAogICAgICAgICJ0ZXN0LWdyb3VwLWMiCiAgICBdLAogICAgImVtYWlsX3ZlcmlmaWVkIjogdHJ1ZSwKICAgICJjb2duaXRvOnByZWZlcnJlZF9yb2xlIjogImFybjphd3M6aWFtOjoxMTExMjIyMjMzMzM6cm9sZS9teS10ZXN0LXJvbGUiLAogICAgImlzcyI6ICJodHRwczovL2NvZ25pdG8taWRwLnVzLXdlc3QtMi5hbWF6b25hd3MuY29tL3VzLXdlc3QtMl9leGFtcGxlIiwKICAgICJjb2duaXRvOnVzZXJuYW1lIjogIm15LXRlc3QtdXNlciIsCiAgICAibWlkZGxlX25hbWUiOiAiSmFuZSIsCiAgICAibm9uY2UiOiAiYWJjZGVmZyIsCiAgICAib3JpZ2luX2p0aSI6ICJhYWFhYWFhYS1iYmJiLWNjY2MtZGRkZC1lZWVlZWVlZWVlZWUiLAogICAgImNvZ25pdG86cm9sZXMiOiBbCiAgICAgICAgImFybjphd3M6aWFtOjoxMTExMjIyMjMzMzM6cm9sZS9teS10ZXN0LXJvbGUiCiAgICBdLAogICAgImF1ZCI6ICJ4eHh4eHh4eHh4eHhleGFtcGxlIiwKICAgICJpZGVudGl0aWVzIjogWwogICAgICAgIHsKICAgICAgICAgICAgInVzZXJJZCI6ICJhbXpuMS5hY2NvdW50LkVYQU1QTEUiLAogICAgICAgICAgICAicHJvdmlkZXJOYW1lIjogIkxvZ2luV2l0aEFtYXpvbiIsCiAgICAgICAgICAgICJwcm92aWRlclR5cGUiOiAiTG9naW5XaXRoQW1hem9uIiwKICAgICAgICAgICAgImlzc3VlciI6IG51bGwsCiAgICAgICAgICAgICJwcmltYXJ5IjogInRydWUiLAogICAgICAgICAgICAiZGF0ZUNyZWF0ZWQiOiAiMTY0MjY5OTExNzI3MyIKICAgICAgICB9CiAgICBdLAogICAgImV2ZW50X2lkIjogIjY0ZjUxM2JlLTMyZGItNDJiMC1iNzhlLWIwMjEyN2I0ZjQ2MyIsCiAgICAidG9rZW5fdXNlIjogImlkIiwKICAgICJhdXRoX3RpbWUiOiAxNjc2MzEyNzc3LAogICAgImV4cCI6IDE2NzYzMTYzNzcsCiAgICAiaWF0IjogMTY3NjMxMjc3NywKICAgICJqdGkiOiAiYWFhYWFhYWEtYmJiYi1jY2NjLWRkZGQtZWVlZWVlZWVlZWVlIiwKICAgICJlbWFpbCI6ICJteS10ZXN0LXVzZXJAZXhhbXBsZS5jb20iCn0K.mjk1il9Nrr5JouTZ9kRcWb1R84bw30vdrYaw0CVsv9A"
        val jwt = Jwt.parse(idToken)

        assertThat(jwt.payload.additionalClaims.get("identities")).isNotNull()
        assertThat(jwt.payload.additionalClaims.get("identities") is List<*>)
        val identities = jwt.payload.additionalClaims.get("identities") as List<*>
        assertThat(identities.size).isEqualTo(1)
        assertThat(identities[0] is Map<*, *>)
        val identitiesMap = identities[0] as Map<*, *>

        assertThat(identitiesMap["userId"]).isEqualTo("amzn1.account.EXAMPLE")
        assertThat(identitiesMap["providerName"]).isEqualTo("LoginWithAmazon")
    }
}
