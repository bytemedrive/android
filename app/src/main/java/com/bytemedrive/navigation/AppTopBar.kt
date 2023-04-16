import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bytemedrive.R
import com.bytemedrive.signin.SignInManager
import org.koin.androidx.compose.get

@Composable
fun AppTopBar(signInManager: SignInManager = get()) {
    TopAppBar(
        title = { Text(text = "Top App Bar", color = Color.White) },
        navigationIcon = {
            IconButton(onClick = {}) {
                Icon(Icons.Filled.ArrowBack, stringResource(id = R.string.common_go_back), tint = Color.White)
            }
        },
        actions = {
            IconButton(onClick = { signInManager.signOut() }) {
                Icon(Icons.Filled.Logout, stringResource(id = R.string.common_sign_out), tint = Color.White)
            }
        },
        backgroundColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        elevation = 10.dp
    )
}