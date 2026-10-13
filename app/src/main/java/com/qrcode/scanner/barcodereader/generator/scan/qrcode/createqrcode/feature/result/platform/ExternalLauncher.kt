package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.feature.result.platform

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PersistableBundle
import android.provider.CalendarContract
import android.provider.ContactsContract
import androidx.browser.customtabs.CustomTabsIntent
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.ContactInsertData
import com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.result.ExternalAction

/**
 * Turns the pure [ExternalAction] descriptions into real intents and starts them. Nothing here runs by itself: every
 * call is triggered by a tap. All functions return false when no app could handle the request, so the caller can show
 * a message instead of crashing.
 */
object ExternalLauncher {
    fun launch(context: Context, action: ExternalAction): Boolean = start(context, intentFor(action))

    fun openCustomTab(context: Context, url: String, dark: Boolean): Boolean {
        val scheme = if (dark) CustomTabsIntent.COLOR_SCHEME_DARK else CustomTabsIntent.COLOR_SCHEME_LIGHT
        val tab =
            CustomTabsIntent
                .Builder()
                .setShowTitle(true)
                .setColorScheme(scheme)
                .build()
        if (context !is Activity) tab.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            tab.launchUrl(context, Uri.parse(url))
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: SecurityException) {
            false
        }
    }

    fun shareText(context: Context, text: String): Boolean {
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        return start(context, Intent.createChooser(send, null))
    }

    /** Copies [text]; [sensitive] (passwords) asks Android 13+ to hide the value in its clipboard preview. */
    fun copy(context: Context, text: String, sensitive: Boolean): Boolean {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return false
        val clip = ClipData.newPlainText("", text)
        if (sensitive && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val extras = PersistableBundle()
            extras.putBoolean(ClipDescription.EXTRA_IS_SENSITIVE, true)
            clip.description.extras = extras
        }
        return try {
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: SecurityException) {
            false
        }
    }

    private fun start(context: Context, intent: Intent): Boolean {
        if (context !is Activity) intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        } catch (e: SecurityException) {
            false
        }
    }

    private fun intentFor(action: ExternalAction): Intent =
        when (action) {
            is ExternalAction.Dial -> Intent(Intent.ACTION_DIAL, Uri.parse(action.uri))
            is ExternalAction.ComposeEmail -> emailIntent(action)
            is ExternalAction.ComposeSms -> smsIntent(action)
            is ExternalAction.OpenMap -> Intent(Intent.ACTION_VIEW, Uri.parse(action.uri))
            is ExternalAction.AddToCalendar -> calendarIntent(action)
            is ExternalAction.AddToContacts -> contactIntent(action.contact)
        }

    private fun emailIntent(action: ExternalAction.ComposeEmail): Intent {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(action.uri))
        if (action.address.isNotBlank()) intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(action.address))
        if (action.subject.isNotBlank()) intent.putExtra(Intent.EXTRA_SUBJECT, action.subject)
        if (action.body.isNotBlank()) intent.putExtra(Intent.EXTRA_TEXT, action.body)
        return intent
    }

    private fun smsIntent(action: ExternalAction.ComposeSms): Intent {
        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse(action.uri))
        if (action.body.isNotBlank()) intent.putExtra("sms_body", action.body)
        return intent
    }

    private fun calendarIntent(action: ExternalAction.AddToCalendar): Intent {
        val intent = Intent(Intent.ACTION_INSERT, CalendarContract.Events.CONTENT_URI)
        intent.putExtra(CalendarContract.Events.TITLE, action.title)
        action.beginMillis?.let { intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, it) }
        action.endMillis?.let { intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, it) }
        intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, action.allDay)
        if (action.location.isNotBlank()) intent.putExtra(CalendarContract.Events.EVENT_LOCATION, action.location)
        if (action.description.isNotBlank()) intent.putExtra(CalendarContract.Events.DESCRIPTION, action.description)
        return intent
    }

    private fun contactIntent(contact: ContactInsertData): Intent {
        val intent = Intent(ContactsContract.Intents.Insert.ACTION)
        intent.type = ContactsContract.Contacts.CONTENT_TYPE
        putIfNotBlank(intent, ContactsContract.Intents.Insert.NAME, contact.name)
        putIfNotBlank(intent, ContactsContract.Intents.Insert.COMPANY, contact.company)
        putIfNotBlank(intent, ContactsContract.Intents.Insert.JOB_TITLE, contact.jobTitle)
        putIfNotBlank(intent, ContactsContract.Intents.Insert.PHONE, contact.phone)
        putIfNotBlank(intent, ContactsContract.Intents.Insert.SECONDARY_PHONE, contact.secondaryPhone)
        putIfNotBlank(intent, ContactsContract.Intents.Insert.EMAIL, contact.email)
        putIfNotBlank(intent, ContactsContract.Intents.Insert.SECONDARY_EMAIL, contact.secondaryEmail)
        putIfNotBlank(intent, ContactsContract.Intents.Insert.POSTAL, contact.postal)
        putIfNotBlank(intent, ContactsContract.Intents.Insert.NOTES, contact.notes)
        return intent
    }

    private fun putIfNotBlank(intent: Intent, key: String, value: String) {
        if (value.isNotBlank()) intent.putExtra(key, value)
    }
}
