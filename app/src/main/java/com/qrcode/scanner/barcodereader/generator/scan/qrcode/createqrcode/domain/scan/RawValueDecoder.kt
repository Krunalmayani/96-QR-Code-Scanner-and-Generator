package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.scan

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction

/** Chooses the text of a scanned code when the decoder gives a string, bytes, or both. */
object RawValueDecoder {
    /** Strict UTF-8 first (most QR codes), ISO-8859-1 otherwise (never fails). Null for null or empty input. */
    fun decodeBytes(bytes: ByteArray?): String? {
        if (bytes == null || bytes.isEmpty()) return null
        return try {
            Charsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes))
                .toString()
        } catch (e: CharacterCodingException) {
            String(bytes, Charsets.ISO_8859_1)
        }
    }

    /** The decoder's own string when it has one, else the decoded bytes. Null when neither carries data. */
    fun resolve(rawValue: String?, rawBytes: ByteArray?): String? =
        if (!rawValue.isNullOrEmpty()) rawValue else decodeBytes(rawBytes)
}
