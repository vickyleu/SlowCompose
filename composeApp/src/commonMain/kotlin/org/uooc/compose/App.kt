package org.uooc.compose

//import org.uooc.compose.ui.home.LocalOrbital
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Constraints
import coil3.annotation.ExperimentalCoilApi
import com.dokar.sonner.ToasterState
import org.uooc.compose.utils.CommonControllerConfiguration

public val LocalAppDelegate: ProvidableCompositionLocal<org.uooc.compose.base.AppDelegate> =
    staticCompositionLocalOf {
        error("No AppDelegate provided")
    }


@OptIn(ExperimentalCoilApi::class)
@Composable
fun App(
    globalToaster: ToasterState,
    restoreConfig: CommonControllerConfiguration,
) {


    Scaffold(Modifier.fillMaxSize().background(Color.White)) {
        BoxWithConstraints() {
            AppContent(this.constraints, restoreConfig = restoreConfig)
        }
    }
}

@Composable
fun AppContent(
    constraints: Constraints,
    restoreConfig: CommonControllerConfiguration,
) {
}
