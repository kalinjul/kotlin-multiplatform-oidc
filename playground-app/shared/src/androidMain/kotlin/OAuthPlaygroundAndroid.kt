import androidx.compose.runtime.Composable
import org.publicvalue.multiplatform.oauth.inject.CommonAndroidActivityComponent

@Composable
fun OauthPlaygroundMainView(component: CommonAndroidActivityComponent) {
    component.appContent(
        { url ->
            // do nothing at the moment
        }
    )
}