package ro.snapify.service

import org.junit.Assert.assertEquals
import org.junit.Test

class OverlayServiceTest {
    @Test
    fun `preview candidates preserve uri-first order and remove duplicates`() {
        assertEquals(
            listOf("content://media/42", "/tmp/capture.png"),
            overlayPreviewCandidates("content://media/42", "/tmp/capture.png"),
        )
        assertEquals(
            listOf("content://media/42"),
            overlayPreviewCandidates("content://media/42", "content://media/42"),
        )
    }

    @Test
    fun `preview candidates omit blank sources`() {
        assertEquals(
            listOf("/tmp/capture.png"),
            overlayPreviewCandidates("", "/tmp/capture.png"),
        )
        assertEquals(emptyList<String>(), overlayPreviewCandidates(null, ""))
    }
}
