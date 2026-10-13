package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SelectionStateTest {
    @Test
    fun `starts inactive`() {
        val s = SelectionState()
        assertFalse(s.active)
        assertEquals(0, s.count)
    }

    @Test
    fun `long press enters selection with that row`() {
        val s = SelectionState().longPress(5)
        assertTrue(s.active)
        assertEquals(setOf(5L), s.ids)
        assertTrue(s.isSelected(5))
    }

    @Test
    fun `long press while active toggles`() {
        val s = SelectionState().longPress(5).longPress(6).longPress(5)
        assertEquals(setOf(6L), s.ids)
    }

    @Test
    fun `toggle is ignored while inactive`() {
        val s = SelectionState()
        assertSame(s, s.toggle(1))
    }

    @Test
    fun `toggling the last selected row keeps selection mode on`() {
        val s = SelectionState().longPress(1).toggle(1)
        assertTrue(s.active)
        assertEquals(emptySet<Long>(), s.ids)
    }

    @Test
    fun `begin starts empty and is idempotent`() {
        val s = SelectionState().begin()
        assertTrue(s.active)
        assertEquals(0, s.count)
        assertSame(s, s.begin())
    }

    @Test
    fun `toggleAll selects all then clears`() {
        val visible = setOf(1L, 2L, 3L)
        val all = SelectionState().longPress(1).toggleAll(visible)
        assertEquals(visible, all.ids)
        val none = all.toggleAll(visible)
        assertTrue(none.active)
        assertEquals(emptySet<Long>(), none.ids)
    }

    @Test
    fun `toggleAll with nothing visible stays empty`() {
        assertEquals(emptySet<Long>(), SelectionState().begin().toggleAll(emptySet()).ids)
    }

    @Test
    fun `retain drops ids that are no longer visible`() {
        val s =
            SelectionState()
                .longPress(1)
                .toggle(2)
                .toggle(3)
                .retain(setOf(2L, 3L, 4L))
        assertEquals(setOf(2L, 3L), s.ids)
        assertTrue(s.active)
    }

    @Test
    fun `retain returns the same instance when nothing changes`() {
        val s = SelectionState().longPress(1)
        assertSame(s, s.retain(setOf(1L, 2L)))
    }

    @Test
    fun `exit resets everything`() {
        assertEquals(SelectionState(), SelectionState().longPress(1).toggle(2).exit())
    }
}
