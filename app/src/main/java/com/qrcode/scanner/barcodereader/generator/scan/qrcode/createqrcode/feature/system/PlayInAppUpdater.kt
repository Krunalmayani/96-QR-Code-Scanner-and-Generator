package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.system

import android.app.Activity
import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/** [InAppUpdater] backed by Play In-App Updates (flexible). Reports nothing on debug builds. */
@Singleton
class PlayInAppUpdater @Inject constructor(
    @ApplicationContext private val context: Context,
) : InAppUpdater {

    private val mutableStatus = MutableStateFlow(UpdateStatus.NONE)
    override val status: StateFlow<UpdateStatus> = mutableStatus.asStateFlow()

    private val manager: AppUpdateManager by lazy { AppUpdateManagerFactory.create(context) }
    private var latestInfo: AppUpdateInfo? = null
    private var listening = false

    private val installListener = InstallStateUpdatedListener { state -> onInstallStatus(state.installStatus()) }

    override fun check() {
        if (BuildConfig.DEBUG) return
        try {
            manager.appUpdateInfo
                .addOnSuccessListener { info -> onUpdateInfo(info) }
                .addOnFailureListener { error -> Timber.w(error, "In-app update check failed") }
        } catch (error: RuntimeException) {
            Timber.w(error, "In-app update check could not start")
        }
    }

    override fun start(activity: Activity) {
        val info = latestInfo ?: return
        try {
            val options = AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
            startListening()
            manager.startUpdateFlow(info, activity, options)
                .addOnSuccessListener { resultCode -> onFlowResult(resultCode) }
                .addOnFailureListener { error -> onFlowFailed(error) }
        } catch (error: RuntimeException) {
            onFlowFailed(error)
        }
    }

    override fun complete() {
        try {
            manager.completeUpdate()
        } catch (error: RuntimeException) {
            Timber.w(error, "In-app update could not be completed")
        }
    }

    private fun onUpdateInfo(info: AppUpdateInfo) {
        latestInfo = info
        val installStatus = info.installStatus()
        mutableStatus.value = when {
            installStatus == InstallStatus.DOWNLOADED -> UpdateStatus.DOWNLOADED
            installStatus == InstallStatus.DOWNLOADING || installStatus == InstallStatus.PENDING -> UpdateStatus.DOWNLOADING
            info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) -> UpdateStatus.AVAILABLE
            else -> UpdateStatus.NONE
        }
        if (mutableStatus.value == UpdateStatus.DOWNLOADING) startListening()
    }

    private fun onFlowResult(resultCode: Int) {
        mutableStatus.value = if (resultCode == Activity.RESULT_OK) UpdateStatus.DOWNLOADING else UpdateStatus.NONE
        if (mutableStatus.value == UpdateStatus.NONE) stopListening()
    }

    private fun onFlowFailed(error: Throwable) {
        Timber.w(error, "In-app update flow failed")
        mutableStatus.value = UpdateStatus.NONE
        stopListening()
    }

    private fun onInstallStatus(installStatus: Int) {
        when (installStatus) {
            InstallStatus.PENDING, InstallStatus.DOWNLOADING -> mutableStatus.value = UpdateStatus.DOWNLOADING
            InstallStatus.DOWNLOADED -> mutableStatus.value = UpdateStatus.DOWNLOADED
            InstallStatus.FAILED, InstallStatus.CANCELED, InstallStatus.INSTALLED -> {
                mutableStatus.value = UpdateStatus.NONE
                stopListening()
            }
            else -> Unit
        }
    }

    private fun startListening() {
        if (listening) return
        listening = true
        manager.registerListener(installListener)
    }

    private fun stopListening() {
        if (!listening) return
        listening = false
        manager.unregisterListener(installListener)
    }
}
