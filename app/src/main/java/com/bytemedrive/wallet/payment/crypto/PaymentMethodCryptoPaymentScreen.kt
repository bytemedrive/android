package com.bytemedrive.wallet.payment.crypto

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.bytemedrive.navigation.TopBarAppContentBack
import com.bytemedrive.store.AppState
import com.bytemedrive.ui.component.Loader
import kotlinx.coroutines.flow.update
import org.koin.androidx.compose.koinViewModel
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun PaymentMethodCryptoPaymentScreen(
    storageAmount: Int,
    paymentMethodCryptoPaymentViewModel: PaymentMethodCryptoPaymentViewModel = koinViewModel(),
) {
    val loading by paymentMethodCryptoPaymentViewModel.loading.collectAsState()

    LaunchedEffect(Unit) {
        AppState.title.update { "Monero payment" }
        AppState.topBarComposable.update { { TopBarAppContentBack() } }
        paymentMethodCryptoPaymentViewModel.init(storageAmount)
    }

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val cakeWalletPackage = "com.cakewallet.cake_wallet"

    val walletAddress by paymentMethodCryptoPaymentViewModel.walletAddress.collectAsState()
    val amount by paymentMethodCryptoPaymentViewModel.amount.collectAsState()
    val expirationAt by paymentMethodCryptoPaymentViewModel.expirationAt.collectAsState()
    val expiresIn by paymentMethodCryptoPaymentViewModel.expiresIn.collectAsState()

    if (loading) {
        Loader()
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(start = 24.dp, end = 24.dp),
        ) {
            Text(
                text = "Storage amount:",
                color = Color.Gray
            )
            Text(
                text = "$storageAmount GBM",
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = "Wallet address:",
                color = Color.Gray
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(10f),
                    text = walletAddress.orEmpty()
                )
                IconButton(
                    onClick = { clipboardManager.setText(AnnotatedString((walletAddress.orEmpty()))) },
                    modifier = Modifier.weight(1f).padding(start = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy wallet address",
                        tint = Color.Black,
                    )
                }
            }

            Text(
                text = "Price to pay:",
                color = Color.Gray
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "$amount XMR")
                IconButton(onClick = { clipboardManager.setText(AnnotatedString((amount.toString()))) }) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy amount",
                        tint = Color.Black,
                    )
                }
            }

            Text(
                text = "Payment expiration:",
                color = Color.Gray
            )
            Text(
                text = "${expirationAt?.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG))} (expires in $expiresIn)"
            )

            Row(
                modifier = Modifier.fillMaxSize().padding(bottom = 16.dp, start = 24.dp, end = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                Button(
                    onClick = {
                        var intent = context.packageManager.getLaunchIntentForPackage(cakeWalletPackage)

                        if (intent == null) {
                            intent = Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse("market://details?id=$cakeWalletPackage"));
                        }

                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        context.startActivity(intent)
                    }
                ) {
                    Text(text = "Open wallet")
                }
            }
        }
    }
}
