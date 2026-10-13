package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan

/** Where the camera permission flow is, from the Scan screen's point of view. */
enum class CameraPermissionStatus {
    GRANTED,

    /** Never asked: show the rationale dialog, then the system prompt. */
    NOT_ASKED,

    /** Denied once; the system prompt can still be shown ("Request again"). */
    DENIED,

    /** Denied with "don't ask again" (or by policy): only the system settings can grant it ("Open settings"). */
    PERMANENTLY_DENIED,
}

/**
 * Resolves the flow state from what Android tells us.
 *
 * @param granted checkSelfPermission == GRANTED.
 * @param shouldShowRationale ActivityCompat.shouldShowRequestPermissionRationale: true after one denial.
 * @param requestedThisSession the system prompt was shown (and answered) since this screen was created.
 */
fun resolveCameraPermission(
    granted: Boolean,
    shouldShowRationale: Boolean,
    requestedThisSession: Boolean,
): CameraPermissionStatus =
    when {
        granted -> CameraPermissionStatus.GRANTED
        shouldShowRationale -> CameraPermissionStatus.DENIED
        requestedThisSession -> CameraPermissionStatus.PERMANENTLY_DENIED
        else -> CameraPermissionStatus.NOT_ASKED
    }
