/*
 * Copyright 2017-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license.
 */

package kotlinx.serialization.cbor

import com.upokecenter.cbor.*
import kotlinx.serialization.*
import kotlinx.serialization.builtins.*
import org.junit.Test
import kotlin.test.*

class CborCompatibilityTest {

    @Serializable
    data class SomeClass(val prop: Int = 0)

    @Serializable
    data class WithMap(val map: Map<Long, Long>)
    @Serializable
    data class IntData(val intV: Int)
    @Serializable
    data class StringData(val data: String)
    @Serializable
    data class FloatData(val field: Float)
    @Serializable
    data class DoubleData(val field: Double)

    @Serializable
    data class SomeComplexClass<T>(
        val boxed: T,
        val otherClass: StringData,
        val primitive: Int,
        val map: Map<String, IntData>
    )

    private inline fun <reified T> compare(obj: T, serializer: KSerializer<T>) {
        val bytes = CBORObject.FromObject(obj).EncodeToBytes()
        assertEquals(obj, Cbor.decodeFromByteArray(serializer, bytes))
    }

    @Test
    fun basicClassFromAnotherLibrary() {
        compare(SomeClass(), SomeClass.serializer())
    }

    @Test
    fun basicListFromAnotherLibrary() {
        compare(
            listOf(
                SomeClass(1),
                SomeClass(2),
                SomeClass(3)
            ), ListSerializer(SomeClass.serializer())
        )
    }

    @Test
    fun withMap() {
        compare(WithMap(mapOf()), WithMap.serializer())
        compare(WithMap(mapOf(10L to 10L)), WithMap.serializer())
        compare(
            WithMap(
                mapOf(
                    10L to 10L,
                    20L to 20L
                )
            ), WithMap.serializer())
    }

    @Test
    fun someComplexClass() {
        val obj = SomeComplexClass(
            listOf(10),
            StringData("20"),
            30,
            mapOf("40" to IntData(40), "50" to IntData(50))
        )
        val serial = SomeComplexClass.serializer(ListSerializer(Int.serializer()))
        compare(obj, serial)
    }

    @Test
    fun testFloat() {
        compare(FloatData(Float.NaN), FloatData.serializer())
        compare(FloatData(Float.POSITIVE_INFINITY), FloatData.serializer())
        compare(FloatData(Float.NEGATIVE_INFINITY), FloatData.serializer())
        compare(FloatData(Float.MAX_VALUE), FloatData.serializer())
        compare(FloatData(Float.MIN_VALUE), FloatData.serializer())
        compare(FloatData(0.0f), FloatData.serializer())
        compare(FloatData(-0.0f), FloatData.serializer())
        compare(FloatData(-1.0f), FloatData.serializer())
        compare(FloatData(1.0f), FloatData.serializer())
        compare(FloatData(123.56f), FloatData.serializer())
        compare(FloatData(123.0f), FloatData.serializer())
        // minimal denormalized value in half-precision
        compare(FloatData(5.9604645E-8f), FloatData.serializer())
        // maximal denormalized value in half-precision
        compare(FloatData(0.000060975552f), FloatData.serializer())
    }

    @Test
    fun testDouble() {
        compare(DoubleData(Double.NaN), DoubleData.serializer())
        compare(DoubleData(Double.POSITIVE_INFINITY), DoubleData.serializer())
        compare(DoubleData(Double.NEGATIVE_INFINITY), DoubleData.serializer())
        compare(DoubleData(Double.MAX_VALUE), DoubleData.serializer())
        compare(DoubleData(Double.MIN_VALUE), DoubleData.serializer())
        compare(DoubleData(0.0), DoubleData.serializer())
        compare(DoubleData(-0.0), DoubleData.serializer())
        compare(DoubleData(-1.0), DoubleData.serializer())
        compare(DoubleData(1.0), DoubleData.serializer())
        compare(DoubleData(123.56), DoubleData.serializer())
        compare(DoubleData(123.0), DoubleData.serializer())
        // minimal denormalized value in half-precision
        compare(DoubleData(5.9604644775390625E-8), DoubleData.serializer())
        // maximal denormalized value in half-precision
        compare(DoubleData(0.00006097555160522461), DoubleData.serializer())
    }
}
