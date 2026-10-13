package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CameraPermissionStatusTest {
    @Test
    fun `granted wins over everything`() {
        assertEquals(CameraPermissionStatus.GRANTED, resolveCameraPermission(true, false, false))
        assertEquals(CameraPermissionStatus.GRANTED, resolveCameraPermission(true, true, true))
    }

    @Test
    fun `never asked shows the rationale first`() {
        assertEquals(CameraPermissionStatus.NOT_ASKED, resolveCameraPermission(false, false, false))
    }

    @Test
    fun `denied once can be requested again`() {
        assertEquals(CameraPermissionStatus.DENIED, resolveCameraPermission(false, true, true))
        assertEquals(CameraPermissionStatus.DENIED, resolveCameraPermission(false, true, false))
    }

    @Test
    fun `answered prompt without a rationale means permanently denied`() {
        assertEquals(CameraPermissionStatus.PERMANENTLY_DENIED, resolveCameraPermission(false, false, true))
    }
}
