package com.wire.backups.exports.android.database.loaders

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

internal class ParsingIssueTest {

    private data class H(val h: String)

    private fun H.toJson() = "[{\"h\": \"$h\"}]"

    private inline fun <reified T> parseJson(json: String) =
        jacksonObjectMapper().readValue<T>(json)

    @Test
    fun `working example`() {
        val expected = H("hello world")
        val actual = parseJson<List<H>>(expected.toJson())

        assertEquals(expected.h, actual.single().h)
    }

    private inline fun <reified T> parseListJson(json: String) =
        jacksonObjectMapper().readValue<List<T>>(json)

    @Test
    fun `failing on class cast exception`() {
        val expected = H("hello again!")
        val actual = parseListJson<H>(expected.toJson())

        val ex = assertFailsWith<ClassCastException> {
            actual.single().h
        }
        assertTrue {
            ex.message?.contains("java.util.LinkedHashMap cannot be cast") ?: false
        }
    }
}
