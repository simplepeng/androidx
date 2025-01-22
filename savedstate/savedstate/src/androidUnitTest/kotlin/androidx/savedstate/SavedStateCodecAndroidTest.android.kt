/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.savedstate

import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Parcel
import android.os.Parcelable
import android.util.Size
import android.util.SizeF
import android.util.SparseArray
import androidx.core.os.bundleOf
import androidx.kruth.assertThat
import androidx.kruth.assertThrows
import androidx.savedstate.SavedStateCodecTestUtils.encodeDecode
import androidx.savedstate.serialization.decodeFromSavedState
import androidx.savedstate.serialization.encodeToSavedState
import androidx.savedstate.serialization.serializers.CharSequenceArraySerializer
import androidx.savedstate.serialization.serializers.CharSequenceListSerializer
import androidx.savedstate.serialization.serializers.CharSequenceSerializer
import androidx.savedstate.serialization.serializers.IBinderSerializer
import androidx.savedstate.serialization.serializers.JavaSerializableSerializer
import androidx.savedstate.serialization.serializers.ParcelableArraySerializer
import androidx.savedstate.serialization.serializers.ParcelableListSerializer
import androidx.savedstate.serialization.serializers.ParcelableSerializer
import androidx.savedstate.serialization.serializers.SavedStateSerializer
import androidx.savedstate.serialization.serializers.SizeFSerializer
import androidx.savedstate.serialization.serializers.SizeSerializer
import androidx.savedstate.serialization.serializers.SparseParcelableArraySerializer
import java.util.UUID
import kotlin.test.Test
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure

@ExperimentalSerializationApi
internal class SavedStateCodecAndroidTest : RobolectricTest() {
    @Test
    fun customSerializers() {
        val uuid = UUID.randomUUID()

        uuid.encodeDecode(MyUUIDSerializer()) {
            assertThat(size()).isEqualTo(1)
            assertThat(getString("")).isEqualTo(uuid.toString())
        }
        Size(128, 256).encodeDecode(MySizeSerializer()) {
            assertThat(size()).isEqualTo(2)
            assertThat(getInt("width")).isEqualTo(128)
            assertThat(getInt("height")).isEqualTo(256)
        }

        @Serializable
        data class MyModel(
            @Serializable(with = MyUUIDSerializer::class) val uuid: UUID,
            @Serializable(with = MySizeSerializer::class) val size: Size
        )
        val uuid2 = UUID.randomUUID()
        MyModel(uuid2, Size(3, 5)).encodeDecode {
            assertThat(size()).isEqualTo(2)
            assertThat(getString("uuid")).isEqualTo(uuid2.toString())
            getSavedState("size").read {
                assertThat(size()).isEqualTo(2)
                assertThat(getInt("width")).isEqualTo(3)
                assertThat(getInt("height")).isEqualTo(5)
            }
        }
    }

    @Test
    fun bundle() {
        @Suppress(
            "SERIALIZER_TYPE_INCOMPATIBLE"
        ) // The lint warning does not show up for external users.
        @Serializable
        class MyClass(@Serializable(with = SavedStateSerializer::class) val s: Bundle)
        MyClass(
                bundleOf(
                    "i" to 1,
                    "s" to "foo",
                    "a" to intArrayOf(1, 3, 5),
                    "ss" to bundleOf("s" to "bar")
                )
            )
            .encodeDecode(
                checkDecoded = { decoded, original ->
                    assertThat(decoded.s.read { contentDeepEquals(original.s) }).isTrue()
                },
                checkEncoded = {
                    assertThat(size()).isEqualTo(1)
                    getSavedState("s").read {
                        assertThat(size()).isEqualTo(4)
                        assertThat(getInt("i")).isEqualTo(1)
                        assertThat(getString("s")).isEqualTo("foo")
                        assertThat(getIntArray("a")).isEqualTo(intArrayOf(1, 3, 5))
                        getSavedState("ss").read {
                            assertThat(size()).isEqualTo(1)
                            assertThat(getString("s")).isEqualTo("bar")
                        }
                    }
                }
            )

        // Bundle at root.
        val origin = bundleOf("i" to 3, "s" to "foo", "d" to 3.14)
        val restored =
            decodeFromSavedState(
                SavedStateSerializer(),
                encodeToSavedState(SavedStateSerializer(), origin).read {
                    assertThat(size()).isEqualTo(3)
                    assertThat(getInt("i")).isEqualTo(3)
                    assertThat(getString("s")).isEqualTo("foo")
                    assertThat(getDouble("d")).isEqualTo(3.14)
                    source
                }
            )
        // Bundle's `equals` doesn't compare contents.
        assertThat(restored.read { contentDeepEquals(origin) }).isTrue()
        assertThat(restored).isNotSameInstanceAs(origin)
    }

    @Test
    fun sizeAndSizeF() {
        @Serializable
        data class MyModel(
            @Serializable(with = SizeSerializer::class) val size: Size,
            @Serializable(with = SizeFSerializer::class) val sizeF: SizeF
        )

        MyModel(Size(128, 256), SizeF(1.23f, 4.56f)).encodeDecode {
            assertThat(size()).isEqualTo(2)
            assertThat(getSize("size")).isEqualTo(Size(128, 256))
            assertThat(getSizeF("sizeF")).isEqualTo(SizeF(1.23f, 4.56f))
        }
    }

    @Test
    fun interfaceTypes() {
        @Serializable
        data class CharSequenceContainer(
            @Serializable(with = CharSequenceSerializer::class) val value: CharSequence
        )
        CharSequenceContainer("foo").encodeDecode {
            assertThat(size()).isEqualTo(1)
            assertThat(getCharSequence("value")).isEqualTo("foo")
        }

        @Serializable
        data class SerializableContainer(
            @Serializable(with = CustomJavaSerializableSerializer::class)
            val value: java.io.Serializable
        )
        val myJavaSerializable = MyJavaSerializable(3, "foo", 3.14)
        SerializableContainer(myJavaSerializable).encodeDecode {
            assertThat(size()).isEqualTo(1)
            assertThat(getJavaSerializable<MyJavaSerializable>("value"))
                .isEqualTo(myJavaSerializable)
        }

        @Serializable
        data class ParcelableContainer(
            @Serializable(with = CustomParcelableSerializer::class) val value: Parcelable
        )
        val myParcelable = MyParcelable(3, "foo", 3.14)
        ParcelableContainer(myParcelable).encodeDecode {
            assertThat(size()).isEqualTo(1)
            assertThat(getParcelable<MyParcelable>("value")).isEqualTo(myParcelable)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            @Serializable
            data class IBinderContainer(
                @Serializable(with = IBinderSerializer::class) val value: IBinder
            )
            val binder = Binder("foo")
            IBinderContainer(binder).encodeDecode {
                assertThat(size()).isEqualTo(1)
                assertThat(getBinder("value")).isEqualTo(binder)
            }
        } else {
            error("VERSION.SDK_INT < Q")
        }
    }

    @Test
    fun interfaceTypesWithoutAnnotation() {
        @Serializable data class CharSequenceContainer(val value: CharSequence)
        assertThrows<SerializationException> {
                CharSequenceContainer("value" as CharSequence).encodeDecode {}
            }
            .hasMessageThat()
            .contains(
                "Serializer for subclass 'String' is not found in the polymorphic scope of 'CharSequence'"
            )

        @Serializable data class SerializableContainer(val value: java.io.Serializable)
        assertThrows<SerializationException> {
                SerializableContainer(MyJavaSerializable(3, "foo", 3.14) as java.io.Serializable)
                    .encodeDecode {}
            }
            .hasMessageThat()
            .contains(
                "Serializer for subclass 'MyJavaSerializable' is not found in the polymorphic scope of 'Serializable'"
            )

        @Serializable data class ParcelableContainer(val value: Parcelable)
        assertThrows<SerializationException> {
                ParcelableContainer(MyParcelable(3, "foo", 3.14) as Parcelable).encodeDecode {}
            }
            .hasMessageThat()
            .contains(
                "Serializer for subclass 'MyParcelable' is not found in the polymorphic scope of 'Parcelable'"
            )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            @Serializable data class IBinderContainer(val value: IBinder)
            assertThrows<SerializationException> {
                    IBinderContainer(Binder("foo") as IBinder).encodeDecode {}
                }
                .hasMessageThat()
                .contains(
                    "Serializer for subclass 'Binder' is not found in the polymorphic scope of 'IBinder'"
                )
        } else {
            error("VERSION.SDK_INT < Q")
        }

        @Suppress("ArrayInDataClass")
        @Serializable
        data class CharSequenceArrayContainer(val value: Array<out CharSequence>)
        assertThrows<SerializationException> {
                CharSequenceArrayContainer(arrayOf("foo", "bar")).encodeDecode {}
            }
            .hasMessageThat()
            .contains(
                "Serializer for subclass 'String' is not found in the polymorphic scope of 'CharSequence'."
            )
    }

    @Test
    fun concreteTypesInsteadOfInterfaceTypes() {
        @Serializable
        data class SerializableContainer(
            @Serializable(with = MyJavaSerializableAsJavaSerializableSerializer::class)
            val value: MyJavaSerializable
        )
        val myJavaSerializable = MyJavaSerializable(3, "foo", 3.14)
        SerializableContainer(myJavaSerializable).encodeDecode {
            assertThat(size()).isEqualTo(1)
            assertThat(getJavaSerializable<MyJavaSerializable>("value"))
                .isEqualTo(myJavaSerializable)
        }

        @Serializable
        data class ParcelableContainer(
            @Serializable(with = MyParcelableAsParcelableSerializer::class) val value: MyParcelable
        )
        val myParcelable = MyParcelable(3, "foo", 3.14)
        ParcelableContainer(myParcelable).encodeDecode {
            assertThat(size()).isEqualTo(1)
            assertThat(getParcelable<MyParcelable>("value")).isEqualTo(myParcelable)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            @Serializable
            data class IBinderContainer(
                @Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
                @Serializable(with = IBinderSerializer::class)
                val value: Binder
            )
            val binder = Binder("foo")
            IBinderContainer(binder).encodeDecode {
                assertThat(size()).isEqualTo(1)
                assertThat(getBinder("value")).isEqualTo(binder)
            }
        } else {
            error("VERSION.SDK_INT < Q")
        }
    }

    @Test
    fun collectionTypes() {
        @Serializable
        @Suppress("ArrayInDataClass")
        data class CharSequenceArrayContainer(
            @Serializable(with = CharSequenceArraySerializer::class)
            val value: Array<out CharSequence>
        )
        val myCharSequenceArray = arrayOf(StringBuilder("foo"), StringBuilder("bar"))
        CharSequenceArrayContainer(myCharSequenceArray)
            .encodeDecode(
                checkDecoded = { decoded, original -> decoded.value.contentEquals(original.value) },
                checkEncoded = {
                    assertThat(size()).isEqualTo(1)
                    assertThat(getCharSequenceArray("value")).isEqualTo(myCharSequenceArray)
                }
            )

        @Serializable
        @Suppress("ArrayInDataClass")
        data class ParcelableArrayContainer(
            @Serializable(with = ParcelableArraySerializer::class) val value: Array<out Parcelable>
        )
        val myParcelableArray = arrayOf(MyParcelable(3, "foo", 3.14), MyParcelable(4, "bar", 1.73))
        ParcelableArrayContainer(myParcelableArray)
            .encodeDecode(
                checkDecoded = { decoded, original -> decoded.value.contentEquals(original.value) },
                checkEncoded = {
                    assertThat(size()).isEqualTo(1)
                    assertThat(getParcelableArray<MyParcelable>("value"))
                        .isEqualTo(myParcelableArray)
                }
            )

        @Serializable
        data class CharSequenceListContainer(
            @Serializable(with = CharSequenceListSerializer::class) val value: List<CharSequence>
        )
        val myCharSequenceList = arrayListOf("foo", "bar")
        CharSequenceListContainer(myCharSequenceList).encodeDecode {
            assertThat(size()).isEqualTo(1)
            assertThat(getCharSequenceList("value")).isEqualTo(myCharSequenceList)
        }

        @Serializable
        data class ParcelableListContainer(
            @Serializable(with = ParcelableListSerializer::class) val value: List<Parcelable>
        )
        val myParcelableList =
            arrayListOf(MyParcelable(3, "foo", 3.14), MyParcelable(4, "bar", 1.73))
        ParcelableListContainer(myParcelableList).encodeDecode {
            assertThat(size()).isEqualTo(1)
            assertThat(getParcelableList<MyParcelable>("value")).isEqualTo(myParcelableList)
        }

        @Serializable
        data class SparseParcelableArrayContainer(
            @Serializable(with = SparseParcelableArraySerializer::class)
            val value: SparseArray<out Parcelable>
        )
        val mySparseParcelableArray =
            SparseArray<MyParcelable>().apply {
                append(1, MyParcelable(3, "foo", 3.14))
                append(3, MyParcelable(4, "bar", 1.73))
            }
        SparseParcelableArrayContainer(mySparseParcelableArray)
            .encodeDecode(
                checkDecoded = { decoded, original ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        decoded.value.contentEquals(original.value)
                    } else {
                        error("VERSION.SDK_INT < S")
                    }
                },
                checkEncoded = {
                    assertThat(size()).isEqualTo(1)
                    assertThat(getSparseParcelableArray<Parcelable>("value"))
                        .isEqualTo(mySparseParcelableArray)
                }
            )
    }

    @Test
    fun collectionTypesWithConcreteElement() {
        @Suppress("ArrayInDataClass")
        @Serializable
        data class CharSequenceArrayContainer(
            @Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
            @Serializable(with = CharSequenceArraySerializer::class)
            val value: Array<@Serializable(with = CharSequenceSerializer::class) StringBuilder>
        )
        val myCharSequenceArray = arrayOf<StringBuilder>(StringBuilder("foo"), StringBuilder("bar"))
        // `Bundle.getCharSequenceArray()` returns a `CharSequence[]` and the actual element type
        // is not being retained after parcel/unparcel so the plugin-generated serializer will
        // get `ClassCastException` when trying to cast it back to `Array<StringBuilder>`.
        assertThrows(ClassCastException::class) {
            CharSequenceArrayContainer(myCharSequenceArray).encodeDecode {
                assertThat(size()).isEqualTo(1)
                assertThat(getCharSequenceArray("value")).isEqualTo(myCharSequenceArray)
            }
        }

        @Suppress("ArrayInDataClass")
        @Serializable
        data class ParcelableArrayContainer(
            @Serializable(with = ParcelableArraySerializer::class)
            // Here the serializer for the element is actually not used, but leaving it out leads
            // to SERIALIZER_NOT_FOUND compile error.
            val value:
                Array<@Serializable(with = MyParcelableAsParcelableSerializer::class) MyParcelable>
        )
        val myParcelableArray = arrayOf(MyParcelable(3, "foo", 3.14), MyParcelable(4, "bar", 1.73))
        // Even though `Bundle` does retain the actual `Parcelable` type there's no way for us to
        // specify this `Parcelable` element type for the array, so the restored array is still of
        // type `Array<Parcelable>` and the plugin-generated serializer will get
        // `ClassCastException` when trying to cast it back to `Array<MyParcelable>`.
        assertThrows(ClassCastException::class) {
            ParcelableArrayContainer(myParcelableArray).encodeDecode {
                assertThat(size()).isEqualTo(1)
                assertThat(getParcelableArray<MyParcelable>("value")).isEqualTo(myParcelableArray)
            }
        }

        @Serializable
        data class CharSequenceListContainer(
            @Serializable(with = CharSequenceListSerializer::class)
            @Suppress("SERIALIZER_TYPE_INCOMPATIBLE")
            val value: List<@Serializable(with = CharSequenceSerializer::class) StringBuilder>
        )
        val myCharSequenceList = arrayListOf(StringBuilder("foo"), StringBuilder("bar"))

        CharSequenceListContainer(myCharSequenceList)
            .encodeDecode(
                checkDecoded = { decoded, original ->
                    assertThat(original.value[0]::class).isEqualTo(StringBuilder::class)
                    // This is similar to the `CharSequenceArray` case where the element type of the
                    // restored List after parcel/unparcel is of `String` instead of
                    // `StringBuilder`. However, since the element type of Lists is erased no
                    // `CastCastException` is thrown when the plugin-generated serializer tried to
                    // assign the restored list back to `List<StringBuilder>`.
                    assertThat(decoded.value[0]::class).isEqualTo(String::class)
                },
                checkEncoded = {
                    assertThat(size()).isEqualTo(1)
                    assertThat(getCharSequenceList("value")).isEqualTo(myCharSequenceList)
                }
            )

        @Serializable
        data class ParcelableListContainer(
            // Unlike arrays this works as `List`s can be down-casted, e.g.
            // a `List<Parcelable>` can be casted to `List<MyParcelable>`.
            @Serializable(with = ParcelableListSerializer::class)
            val value:
                List<@Serializable(with = MyParcelableAsParcelableSerializer::class) MyParcelable>
        )
        val myParcelableList =
            arrayListOf(MyParcelable(3, "foo", 3.14), MyParcelable(4, "bar", 1.73))
        ParcelableListContainer(myParcelableList).encodeDecode {
            assertThat(size()).isEqualTo(1)
            assertThat(getParcelableList<MyParcelable>("value")).isEqualTo(myParcelableList)
        }

        @Serializable
        data class SparseParcelableArrayContainer(
            // Unlike arrays this works as `SparseArray`s can be down-casted, e.g.
            // a `SparseArray<Parcelable>` can be casted to `SparseArray<MyParcelable>`.
            @Serializable(with = SparseParcelableArraySerializer::class)
            val value:
                SparseArray<
                    @Serializable(with = MyParcelableAsParcelableSerializer::class)
                    MyParcelable
                >
        )
        val mySparseParcelableArray =
            SparseArray<MyParcelable>().apply {
                append(1, MyParcelable(3, "foo", 3.14))
                append(3, MyParcelable(4, "bar", 1.73))
            }
        SparseParcelableArrayContainer(mySparseParcelableArray)
            .encodeDecode(
                checkDecoded = { decoded, original ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        assertThat(decoded.value.contentEquals(original.value))
                    } else {
                        error("VERSION.SDK_INT < S")
                    }
                },
                checkEncoded = {
                    assertThat(size()).isEqualTo(1)
                    assertThat(getSparseParcelableArray<Parcelable>("value"))
                        .isEqualTo(mySparseParcelableArray)
                }
            )
    }
}

private class MyUUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor
        get() = PrimitiveSerialDescriptor("UUIDSerializer", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}

private class MySizeSerializer : KSerializer<Size> {
    override val descriptor: SerialDescriptor
        get() =
            buildClassSerialDescriptor("SizeDescriptor") {
                element("width", PrimitiveSerialDescriptor("width", PrimitiveKind.INT))
                element("height", PrimitiveSerialDescriptor("height", PrimitiveKind.INT))
            }

    override fun deserialize(decoder: Decoder): Size {
        return decoder.decodeStructure(descriptor) {
            var width = 0
            var height = 0
            while (true) {
                when (decodeElementIndex(descriptor)) {
                    CompositeDecoder.DECODE_DONE -> break
                    0 -> width = decodeIntElement(descriptor, 0)
                    1 -> height = decodeIntElement(descriptor, 1)
                    else -> error("what?")
                }
            }
            Size(width, height)
        }
    }

    override fun serialize(encoder: Encoder, value: Size) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.width)
            encodeIntElement(descriptor, 1, value.height)
        }
    }
}

private data class MyJavaSerializable(val i: Int, val s: String, val d: Double) :
    java.io.Serializable

private data class MyParcelable(val i: Int, val s: String, val d: Double) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readInt(), parcel.readString()!!, parcel.readDouble())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(i)
        parcel.writeString(s)
        parcel.writeDouble(d)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<MyParcelable> {
        override fun createFromParcel(parcel: Parcel): MyParcelable {
            return MyParcelable(parcel)
        }

        override fun newArray(size: Int): Array<MyParcelable?> {
            return arrayOfNulls(size)
        }
    }
}

private class MyJavaSerializableAsJavaSerializableSerializer :
    JavaSerializableSerializer<MyJavaSerializable>()

private class MyParcelableAsParcelableSerializer : ParcelableSerializer<MyParcelable>()

private class CustomJavaSerializableSerializer : JavaSerializableSerializer<java.io.Serializable>()

private class CustomParcelableSerializer : ParcelableSerializer<Parcelable>()
