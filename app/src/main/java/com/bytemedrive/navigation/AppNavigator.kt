package com.bytemedrive.navigation

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class AppNavigator {

    private val _sharedFlow = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val sharedFlow = _sharedFlow.asSharedFlow()

    fun navigateTo(navTarget: NavTarget, arguments: Map<String, String?> = mapOf()) {
        var target = navTarget.label

        arguments.forEach { target = target.replace("{${it.key}}", it.value.orEmpty()) }

        _sharedFlow.tryEmit(target)
    }

    enum class NavTarget(val label: String) {

        ADD_CREDIT_CODE("addCreditCode"),

        ADD_CREDIT_METHOD("addCreditMethod"),

        BACK("back"),

        BIN("bin"),

        FILE("file?folderId={folderId}"),

        FILE_BOTTOM_SHEET_CONTEXT_FILE("fileBottomSheetContextFile/{id}"),

        FILE_BOTTOM_SHEET_CONTEXT_FOLDER("fileBottomSheetContextFolder/{id}"),

        FILE_BOTTOM_SHEET_CREATE("fileBottomSheetCreate?folderId={folderId}"),

        SETTINGS("settings"),

        SIGN_IN("signIn"),

        SIGN_UP("signUp"),

        STARRED("starred"),

        STARRED_BOTTOM_SHEET_CONTEXT_FILE("starredBottomSheetContextFile/{id}"),

        STARRED_BOTTOM_SHEET_CONTEXT_FOLDER("starredBottomSheetContextFolder/{id}"),

        TERMS_AND_CONDITIONS("termsAndConditions"),
    }
}