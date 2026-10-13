package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.architecture

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.w3c.dom.Element
import org.w3c.dom.Node
import javax.xml.parsers.DocumentBuilderFactory

/**
 * PRD section 6 (Security): no cleartext traffic, no auto backup of history, and no exported component except the
 * launcher activity. Reads `app/src/main/AndroidManifest.xml`.
 */
class ManifestSecurityTest {
    private val android = "http://schemas.android.com/apk/res/android"

    private val application: Element by lazy {
        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val document = factory.newDocumentBuilder().parse(SourceTree.manifest.toFile())
        document.documentElement.getElementsByTagName("application").item(0) as Element
    }

    private fun Element.androidAttr(name: String): String? = if (hasAttributeNS(android, name)) getAttributeNS(android, name) else null

    private val components: List<Element> by lazy {
        val tags = setOf("activity", "activity-alias", "service", "receiver", "provider")
        generateSequence(application.firstChild) { it.nextSibling }
            .filter { it.nodeType == Node.ELEMENT_NODE }
            .map { it as Element }
            .filter { it.tagName in tags }
            .toList()
    }

    private fun Element.name(): String = androidAttr("name") ?: "?"

    private fun Element.children(tag: String): List<Element> =
        generateSequence(firstChild) { it.nextSibling }.filterIsInstance<Element>().filter { it.tagName == tag }.toList()

    private fun Element.isLauncher(): Boolean =
        children("intent-filter").any { filter ->
            val actions = filter.children("action").map { it.androidAttr("name") }
            val categories = filter.children("category").map { it.androidAttr("name") }
            "android.intent.action.MAIN" in actions && "android.intent.category.LAUNCHER" in categories
        }

    @Test
    fun theManifestDeclaresComponents() {
        assertTrue(components.isNotEmpty(), "no components found in ${SourceTree.manifest}")
    }

    @Test
    fun onlyTheLauncherActivityIsExported() {
        val exported = components.filter { it.androidAttr("exported") == "true" }
        val unexpected = exported.filterNot { it.tagName == "activity" && it.isLauncher() }
        ArchitectureBaseline.assertNoNewViolations(
            Rules.MANIFEST,
            "components exported besides the launcher activity",
            unexpected.map { "${it.tagName} ${it.name()}" },
        )
        assertEquals(1, exported.count { it.isLauncher() }, "exactly one exported launcher activity expected")
    }

    @Test
    fun everyComponentWithAnIntentFilterDeclaresExportedExplicitly() {
        val implicit = components.filter { it.children("intent-filter").isNotEmpty() && it.androidAttr("exported") == null }
        ArchitectureBaseline.assertNoNewViolations(
            Rules.MANIFEST,
            "components with an intent filter must say android:exported explicitly (required from Android 12)",
            implicit.map { "${it.tagName} ${it.name()}" },
        )
    }

    @Test
    fun cleartextTrafficIsDisabledExplicitly() {
        assertEquals("false", application.androidAttr("usesCleartextTraffic"), "android:usesCleartextTraffic must be \"false\"")
    }

    @Test
    fun backupIsNotAllowed() {
        assertNotNull(application.androidAttr("allowBackup"), "declare android:allowBackup=\"false\" explicitly")
        assertEquals("false", application.androidAttr("allowBackup"), "android:allowBackup must not be \"true\"")
    }

    @Test
    fun releaseManifestIsNotDebuggable() {
        assertTrue(application.androidAttr("debuggable") != "true", "android:debuggable must not be set in the main manifest")
    }
}
