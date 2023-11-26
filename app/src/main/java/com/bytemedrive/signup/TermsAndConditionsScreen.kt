package com.bytemedrive.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
        Text(text = "Terms of Service", fontSize = 20.sp, fontWeight = FontWeight.Bold)


        Text(text = "Introduction and Definitions", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = "By downloading or using the app, these terms will automatically apply – the user should make sure therefore that they read them carefully before using the app.")
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Service:") }
                append(" an anonymous online storage platform that allows Users to securely upload, store, and manage their data while maintaining their anonymity.")
            })

        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Service Provider:") }
                append(" the entity or organization responsible for operating and providing the anonymous online storage service to Users, ensuring the functionality, security, and maintenance of the platform. The service provider is Osomahe, s.r.o.")
            })

        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("User:") }
                append(" any individual or entity that anonymously creates an account by entering a username and password to access the online storage service. The User can then utilize the service to securely upload, store, and manage their data as per the service’s guidelines and obligations.")
            })


        Text(text = "Confidentiality and Data Security", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Anonymity and security:") }
                append(" The online storage service allows users to anonymously upload, store, and download data. All data stored on the service is encrypted on the user’s device to ensure privacy and data security. The service provider and any unauthorized parties do not have any rights or access to the data stored by users, including information about which data belongs to which user or the amount of data stored.")
            })


        Text(text = "Price Calculation and Charges", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("The price calculation process") }
                append(" is conducted through a one-way process that does not reveal any information about the user’s data or space usage.")
            })

        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Monthly charges:") }
                append(" Users are charged on a monthly basis based on the amount of data they store during the previous month. The service provider may provide detailed information on pricing and payment methods separately, which users should review and comply with.")
            })


        Text(text = "User Obligations", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Acceptable Use:") }
                append(" Users must agree to use the online storage service in a lawful manner, refraining from uploading or sharing any content that is illegal, infringing, harmful, or violates the rights of others.")
            })

        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Content Responsibility:") }
                append(" Users are solely responsible for the content they upload or share through the service, including ensuring that they have the necessary rights, permissions, or licenses to do so, and respecting the privacy and intellectual property rights of others.")
            })

        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Prohibited Activities:") }
                append(" Users must not engage in activities that disrupt or harm the service or its users, such as attempting unauthorized access or engaging in any form of unauthorized data mining or scraping.")
            })


        Text(text = "Termination and Data Deletion", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("User-Requested Account Termination:") }
                append(" Users have the option to request the termination of their account at any time. Upon account termination request, all associated data will be permanently deleted from the service. Should user require the data deletion and/or account termination, it can be requested in-app. The files are deleted immediately and user loses access to any remaining credit.")
            })

        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Non-Payment and Data Deletion:") }
                append("In the event that a user fails to make payment for the storage space utilized, the service provider reserves the right to delete the data associated with that user’s account after notification.")
            })


        Text(text = "Governing Law and Jurisdiction", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(
            buildAnnotatedString {
                withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) { append("Governing law:") }
                append(" These terms and conditions and any disputes arising from or relating to the online storage service shall be governed by the laws of the Czech Republic, and any legal action or proceeding shall be exclusively brought in the courts of the Czech Republic. Users consent to the personal jurisdiction of such courts and waive any objection to the convenience or appropriateness of the venue./or account termination, it can be requested in-app. The files are deleted immediately and user loses access to any remaining credit.")
            })


        Text(text = "Modifications to the Terms", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = "These terms and conditions are effective as of 2023-11-27. Terms version 23.11.1.")
        Text(text = "The service provider reserves the right to modify the terms and conditions. The changes and their effective date will be published on the service’s website.")
    }
}