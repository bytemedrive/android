package com.bytemedrive.authentication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TermsAndConditionsScreen() {
    val scrollState = rememberScrollState()


    Column(
        modifier = Modifier.padding(16.dp).verticalScroll(
            state = scrollState,

            ),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(text = "ByteMe Drive Terms and Conditions", fontSize = 20.sp, fontWeight = FontWeight(500))
        Text(text = "By downloading or using the app, these terms will automatically apply to you – you should make sure therefore that you read them carefully before using the app. You’re not allowed to copy or modify the app, any part of the app, or our trademarks in any way. You’re not allowed to attempt to extract the source code of the app, and you also shouldn’t try to translate the app into other languages or make derivative versions. The app itself, and all the trademarks, copyright, database rights, and other intellectual property rights related to it, still belong to Osomahe s.r.o..")
        Text(text = "Osomahe s.r.o. is committed to ensuring that the app is as useful and efficient as possible. For that reason, we reserve the right to make changes to the app or to charge for its services, at any time and for any reason. We will never charge you for the app or its services without making it very clear to you exactly what you’re paying for.")
        Text(text = "The ByteMe Drive app stores and processes personal data that you have provided to us, to provide our Service. It’s your responsibility to keep your phone and access to the app secure. We therefore recommend that you do not jailbreak or root your phone, which is the process of removing software restrictions and limitations imposed by the official operating system of your device. It could make your phone vulnerable to malware/viruses/malicious programs, compromise your phone’s security features and it could mean that the ByteMe Drive app won’t work properly or at all.")
        Text(text = "The app does use third-party services that declare their Terms and Conditions.")
        Text(text = "Link to Terms and Conditions of third-party service providers used by the app")
        TextButton(onClick = { /* https://policies.google.com/terms */ }) {
            Text(text = "Google Play Services")
        }
        Text(text = "You should be aware that there are certain things that Osomahe s.r.o. will not take responsibility for. Certain functions of the app will require the app to have an active internet connection. The connection can be Wi-Fi or provided by your mobile network provider, but Osomahe s.r.o. cannot take responsibility for the app not working at full functionality if you don’t have access to Wi-Fi, and you don’t have any of your data allowance left.")
        Text(text = "If you’re using the app outside of an area with Wi-Fi, you should remember that the terms of the agreement with your mobile network provider will still apply. As a result, you may be charged by your mobile provider for the cost of data for the duration of the connection while accessing the app, or other third-party charges. In using the app, you’re accepting responsibility for any such charges, including roaming data charges if you use the app outside of your home territory (i.e. region or country) without turning off data roaming. If you are not the bill payer for the device on which you’re using the app, please be aware that we assume that you have received permission from the bill payer for using the app.")
        Text(text = "Along the same lines, Osomahe s.r.o. cannot always take responsibility for the way you use the app i.e. You need to make sure that your device stays charged – if it runs out of battery and you can’t turn it on to avail the Service, Osomahe s.r.o. cannot accept responsibility.")
        Text(text = "With respect to Osomahe s.r.o.’s responsibility for your use of the app, when you’re using the app, it’s important to bear in mind that although we endeavor to ensure that it is updated and correct at all times, we do rely on third parties to provide information to us so that we can make it available to you. Osomahe s.r.o. accepts no liability for any loss, direct or indirect, you experience as a result of relying wholly on this functionality of the app.")
        Text(text = "At some point, we may wish to update the app. The app is currently available on Android – the requirements for the system(and for any additional systems we decide to extend the availability of the app to) may change, and you’ll need to download the updates if you want to keep using the app. Osomahe s.r.o. does not promise that it will always update the app so that it is relevant to you and/or works with the Android version that you have installed on your device. However, you promise to always accept updates to the application when offered to you, We may also wish to stop providing the app, and may terminate use of it at any time without giving notice of termination to you. Unless we tell you otherwise, upon any termination, (a) the rights and licenses granted to you in these terms will end; (b) you must stop using the app, and (if needed) delete it from your device.")
        Text(text = "Changes to This Terms and Conditions")
        Text(text = "We may update our Terms and Conditions from time to time. Thus, you are advised to review this page periodically for any changes. We will notify you of any changes by posting the new Terms and Conditions on this page.")
        Text(text = "These terms and conditions are effective as of 2023-03-19.")
        Text(text = "Contact Us")
        Text(text = "If you have any questions or suggestions about our Terms and Conditions, do not hesitate to contact us at support@bytemedrive.com.")
    }
}