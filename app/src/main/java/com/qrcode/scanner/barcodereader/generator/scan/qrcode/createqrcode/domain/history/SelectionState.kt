package com.qrcode.scanner.barcodereader.generator.scan.qrcode.createqrcode.domain.history

/**
 * Multi-select reducer. Immutable: every operation returns the next state. Selection mode stays on until
 * [exit] is called (close button, back press, or after an action), even when nothing is selected.
 */
data class SelectionState(
    val active: Boolean = false,
    val ids: Set<Long> = emptySet(),
) {
    val count: Int get() = ids.size

    fun isSelected(id: Long): Boolean = id in ids

    /** Long-press: enters selection mode with [id] selected (toggles it when already in selection mode). */
    fun longPress(id: Long): SelectionState = if (active) toggle(id) else SelectionState(active = true, ids = setOf(id))

    /** "Select items" button: selection mode with nothing selected. */
    fun begin(): SelectionState = if (active) this else SelectionState(active = true)

    fun toggle(id: Long): SelectionState {
        if (!active) return this
        return copy(ids = if (id in ids) ids - id else ids + id)
    }

    /** Selects every visible row, or clears the selection when all of them are already selected. */
    fun toggleAll(visible: Set<Long>): SelectionState {
        if (!active) return this
        val allSelected = visible.isNotEmpty() && ids.containsAll(visible)
        return copy(ids = if (allSelected) emptySet() else visible)
    }

    /** Drops selected ids that are no longer visible (deleted elsewhere, filtered out). */
    fun retain(visible: Set<Long>): SelectionState {
        if (!active) return this
        val kept = ids.intersect(visible)
        return if (kept.size == ids.size) this else copy(ids = kept)
    }

    fun exit(): SelectionState = SelectionState()
}
