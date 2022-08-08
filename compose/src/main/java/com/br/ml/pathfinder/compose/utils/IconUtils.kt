import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ImageVector.Icon(contentDescription: String? = null) = Icon(
    imageVector = this,
    contentDescription = contentDescription ?: name.split(".").lastOrNull()
)

