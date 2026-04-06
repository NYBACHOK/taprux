package com.ghuba.taprux.core

import com.novi.bincode.BincodeDeserializer
import com.novi.bincode.BincodeSerializer
import com.novi.serde.DeserializationError
import com.novi.serde.Deserializer
import com.novi.serde.Serializer

fun <T> List<T>.serialize(
    serializer: Serializer,
    serializeElement: Serializer.(T) -> Unit,
) {
    serializer.serialize_len(size.toLong())
    forEach { element ->
        serializer.serializeElement(element)
    }
}

fun <T> Deserializer.deserializeListOf(deserializeElement: (Deserializer) -> T): List<T> {
    val length = deserialize_len()
    val list = mutableListOf<T>()
    repeat(length.toInt()) {
        list.add(deserializeElement(this))
    }
    return list
}

fun <T> T?.serializeOptionOf(
    serializer: Serializer,
    serializeElement: Serializer.(T) -> Unit,
) {
    if (this != null) {
        serializer.serialize_option_tag(true)
        serializer.serializeElement(this)
    } else {
        serializer.serialize_option_tag(false)
    }
}

fun <T> Deserializer.deserializeOptionOf(deserializeElement: (Deserializer) -> T): T? {
    val tag = deserialize_option_tag()
    return if (tag) {
        deserializeElement(this)
    } else {
        null
    }
}

sealed interface Effect {
    fun serialize(serializer: Serializer)

    fun bincodeSerialize(): ByteArray {
        val serializer = BincodeSerializer()
        serialize(serializer)
        return serializer.get_bytes()
    }

    data class Render(
        val value: com.ghuba.taprux.core.RenderOperation,
    ) : Effect {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(0)
            value.serialize(serializer)
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): Render {
                deserializer.increase_container_depth()
                val value = com.ghuba.taprux.core.RenderOperation.deserialize(deserializer)
                deserializer.decrease_container_depth()
                return Render(value)
            }
        }
    }

    data class Query(
        val value: com.ghuba.taprux.core.QueryRequest,
    ) : Effect {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(1)
            value.serialize(serializer)
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): Query {
                deserializer.increase_container_depth()
                val value = com.ghuba.taprux.core.QueryRequest.deserialize(deserializer)
                deserializer.decrease_container_depth()
                return Query(value)
            }
        }
    }

    companion object {
        @Throws(DeserializationError::class)
        fun deserialize(deserializer: Deserializer): Effect {
            val index = deserializer.deserialize_variant_index()
            return when (index) {
                0 -> Render.deserialize(deserializer)
                1 -> Query.deserialize(deserializer)
                else -> throw DeserializationError("Unknown variant index for Effect: $index")
            }
        }

        @Throws(DeserializationError::class)
        fun bincodeDeserialize(input: ByteArray?): Effect {
            if (input == null) {
                throw DeserializationError("Cannot deserialize null array")
            }
            val deserializer = BincodeDeserializer(input)
            val value = deserialize(deserializer)
            if (deserializer.get_buffer_offset() < input.size) {
                throw DeserializationError("Some input bytes were not read")
            }
            return value
        }
    }
}

data class ErrorModel(
    val isCritical: Boolean,
    val description: String,
) {
    fun serialize(serializer: Serializer) {
        serializer.increase_container_depth()
        serializer.serialize_bool(isCritical)
        serializer.serialize_str(description)
        serializer.decrease_container_depth()
    }

    fun bincodeSerialize(): ByteArray {
        val serializer = BincodeSerializer()
        serialize(serializer)
        return serializer.get_bytes()
    }

    companion object {
        fun deserialize(deserializer: Deserializer): ErrorModel {
            deserializer.increase_container_depth()
            val isCritical = deserializer.deserialize_bool()
            val description = deserializer.deserialize_str()
            deserializer.decrease_container_depth()
            return ErrorModel(isCritical, description)
        }

        @Throws(DeserializationError::class)
        fun bincodeDeserialize(input: ByteArray?): ErrorModel {
            if (input == null) {
                throw DeserializationError("Cannot deserialize null array")
            }
            val deserializer = BincodeDeserializer(input)
            val value = deserialize(deserializer)
            if (deserializer.get_buffer_offset() < input.size) {
                throw DeserializationError("Some input bytes were not read")
            }
            return value
        }
    }
}

sealed interface Event {
    fun serialize(serializer: Serializer)

    fun bincodeSerialize(): ByteArray {
        val serializer = BincodeSerializer()
        serialize(serializer)
        return serializer.get_bytes()
    }

    data class Query(
        val value: com.ghuba.taprux.core.QueryRequest,
    ) : Event {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(0)
            value.serialize(serializer)
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): Query {
                deserializer.increase_container_depth()
                val value = com.ghuba.taprux.core.QueryRequest.deserialize(deserializer)
                deserializer.decrease_container_depth()
                return Query(value)
            }
        }
    }

    companion object {
        @Throws(DeserializationError::class)
        fun deserialize(deserializer: Deserializer): Event {
            val index = deserializer.deserialize_variant_index()
            return when (index) {
                0 -> Query.deserialize(deserializer)
                else -> throw DeserializationError("Unknown variant index for Event: $index")
            }
        }

        @Throws(DeserializationError::class)
        fun bincodeDeserialize(input: ByteArray?): Event {
            if (input == null) {
                throw DeserializationError("Cannot deserialize null array")
            }
            val deserializer = BincodeDeserializer(input)
            val value = deserialize(deserializer)
            if (deserializer.get_buffer_offset() < input.size) {
                throw DeserializationError("Some input bytes were not read")
            }
            return value
        }
    }
}

sealed interface QueryRequest {
    fun serialize(serializer: Serializer)

    fun bincodeSerialize(): ByteArray {
        val serializer = BincodeSerializer()
        serialize(serializer)
        return serializer.get_bytes()
    }

    data object List: QueryRequest {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(0)
            serializer.decrease_container_depth()
        }

        fun deserialize(deserializer: Deserializer): List {
            return List
        }
    }

    data class Clicked(
        val value: UInt,
    ) : QueryRequest {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(1)
            serializer.serialize_u32(value)
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): Clicked {
                deserializer.increase_container_depth()
                val value = deserializer.deserialize_u32()
                deserializer.decrease_container_depth()
                return Clicked(value)
            }
        }
    }

    data class Details(
        val value: UInt,
    ) : QueryRequest {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(2)
            serializer.serialize_u32(value)
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): Details {
                deserializer.increase_container_depth()
                val value = deserializer.deserialize_u32()
                deserializer.decrease_container_depth()
                return Details(value)
            }
        }
    }

    companion object {
        @Throws(DeserializationError::class)
        fun deserialize(deserializer: Deserializer): QueryRequest {
            val index = deserializer.deserialize_variant_index()
            return when (index) {
                0 -> List.deserialize(deserializer)
                1 -> Clicked.deserialize(deserializer)
                2 -> Details.deserialize(deserializer)
                else -> throw DeserializationError("Unknown variant index for QueryRequest: $index")
            }
        }

        @Throws(DeserializationError::class)
        fun bincodeDeserialize(input: ByteArray?): QueryRequest {
            if (input == null) {
                throw DeserializationError("Cannot deserialize null array")
            }
            val deserializer = BincodeDeserializer(input)
            val value = deserialize(deserializer)
            if (deserializer.get_buffer_offset() < input.size) {
                throw DeserializationError("Some input bytes were not read")
            }
            return value
        }
    }
}

sealed interface QueryResponse {
    fun serialize(serializer: Serializer)

    fun bincodeSerialize(): ByteArray {
        val serializer = BincodeSerializer()
        serialize(serializer)
        return serializer.get_bytes()
    }

    data object None: QueryResponse {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(0)
            serializer.decrease_container_depth()
        }

        fun deserialize(deserializer: Deserializer): None {
            return None
        }
    }

    data class Trackables(
        val value: List<com.ghuba.taprux.core.TrackableModel>,
    ) : QueryResponse {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(1)
            value.serialize(serializer) {
                it.serialize(serializer)
            }
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): Trackables {
                deserializer.increase_container_depth()
                val value =
                    deserializer.deserializeListOf {
                        com.ghuba.taprux.core.TrackableModel.deserialize(deserializer)
                    }
                deserializer.decrease_container_depth()
                return Trackables(value)
            }
        }
    }

    data class Clicked(
        val value: UInt,
    ) : QueryResponse {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(2)
            serializer.serialize_u32(value)
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): Clicked {
                deserializer.increase_container_depth()
                val value = deserializer.deserialize_u32()
                deserializer.decrease_container_depth()
                return Clicked(value)
            }
        }
    }

    data class Details(
        val value: com.ghuba.taprux.core.TrackableWithChildrenModel,
    ) : QueryResponse {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(3)
            value.serialize(serializer)
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): Details {
                deserializer.increase_container_depth()
                val value = com.ghuba.taprux.core.TrackableWithChildrenModel.deserialize(deserializer)
                deserializer.decrease_container_depth()
                return Details(value)
            }
        }
    }

    companion object {
        @Throws(DeserializationError::class)
        fun deserialize(deserializer: Deserializer): QueryResponse {
            val index = deserializer.deserialize_variant_index()
            return when (index) {
                0 -> None.deserialize(deserializer)
                1 -> Trackables.deserialize(deserializer)
                2 -> Clicked.deserialize(deserializer)
                3 -> Details.deserialize(deserializer)
                else -> throw DeserializationError("Unknown variant index for QueryResponse: $index")
            }
        }

        @Throws(DeserializationError::class)
        fun bincodeDeserialize(input: ByteArray?): QueryResponse {
            if (input == null) {
                throw DeserializationError("Cannot deserialize null array")
            }
            val deserializer = BincodeDeserializer(input)
            val value = deserialize(deserializer)
            if (deserializer.get_buffer_offset() < input.size) {
                throw DeserializationError("Some input bytes were not read")
            }
            return value
        }
    }
}

sealed interface QueryResult {
    fun serialize(serializer: Serializer)

    fun bincodeSerialize(): ByteArray {
        val serializer = BincodeSerializer()
        serialize(serializer)
        return serializer.get_bytes()
    }

    data class Response(
        val value: com.ghuba.taprux.core.QueryResponse,
    ) : QueryResult {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(0)
            value.serialize(serializer)
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): Response {
                deserializer.increase_container_depth()
                val value = com.ghuba.taprux.core.QueryResponse.deserialize(deserializer)
                deserializer.decrease_container_depth()
                return Response(value)
            }
        }
    }

    data class Err(
        val value: String,
    ) : QueryResult {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(1)
            serializer.serialize_str(value)
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): Err {
                deserializer.increase_container_depth()
                val value = deserializer.deserialize_str()
                deserializer.decrease_container_depth()
                return Err(value)
            }
        }
    }

    companion object {
        @Throws(DeserializationError::class)
        fun deserialize(deserializer: Deserializer): QueryResult {
            val index = deserializer.deserialize_variant_index()
            return when (index) {
                0 -> Response.deserialize(deserializer)
                1 -> Err.deserialize(deserializer)
                else -> throw DeserializationError("Unknown variant index for QueryResult: $index")
            }
        }

        @Throws(DeserializationError::class)
        fun bincodeDeserialize(input: ByteArray?): QueryResult {
            if (input == null) {
                throw DeserializationError("Cannot deserialize null array")
            }
            val deserializer = BincodeDeserializer(input)
            val value = deserialize(deserializer)
            if (deserializer.get_buffer_offset() < input.size) {
                throw DeserializationError("Some input bytes were not read")
            }
            return value
        }
    }
}

/// The single operation `Render` implements.
data object RenderOperation {
    fun serialize(serializer: Serializer) {}

    fun bincodeSerialize(): ByteArray {
        val serializer = BincodeSerializer()
        serialize(serializer)
        return serializer.get_bytes()
    }

    fun deserialize(deserializer: Deserializer): RenderOperation {
        return RenderOperation
    }

    @Throws(DeserializationError::class)
    fun bincodeDeserialize(input: ByteArray?): RenderOperation {
        if (input == null) {
            throw DeserializationError("Cannot deserialize null array")
        }
        val deserializer = BincodeDeserializer(input)
        val value = deserialize(deserializer)
        if (deserializer.get_buffer_offset() < input.size) {
            throw DeserializationError("Some input bytes were not read")
        }
        return value
    }
}

/// Request for a side-effect passed from the Core to the Shell. The `EffectId` links
/// the `Request` with the corresponding call to [`Core::resolve`] to pass the data back
/// to the [`App::update`] function (wrapped in the event provided to the capability originating the effect).
data class Request(
    val id: UInt,
    val effect: com.ghuba.taprux.core.Effect,
) {
    fun serialize(serializer: Serializer) {
        serializer.increase_container_depth()
        serializer.serialize_u32(id)
        effect.serialize(serializer)
        serializer.decrease_container_depth()
    }

    fun bincodeSerialize(): ByteArray {
        val serializer = BincodeSerializer()
        serialize(serializer)
        return serializer.get_bytes()
    }

    companion object {
        fun deserialize(deserializer: Deserializer): Request {
            deserializer.increase_container_depth()
            val id = deserializer.deserialize_u32()
            val effect = com.ghuba.taprux.core.Effect.deserialize(deserializer)
            deserializer.decrease_container_depth()
            return Request(id, effect)
        }

        @Throws(DeserializationError::class)
        fun bincodeDeserialize(input: ByteArray?): Request {
            if (input == null) {
                throw DeserializationError("Cannot deserialize null array")
            }
            val deserializer = BincodeDeserializer(input)
            val value = deserialize(deserializer)
            if (deserializer.get_buffer_offset() < input.size) {
                throw DeserializationError("Some input bytes were not read")
            }
            return value
        }
    }
}

data class TrackableModel(
    val id: UInt,
    val name: String,
    val svgIcon: List<UByte>,
    val eventOccurrence: UInt,
    val hasSubEvents: Boolean,
) {
    fun serialize(serializer: Serializer) {
        serializer.increase_container_depth()
        serializer.serialize_u32(id)
        serializer.serialize_str(name)
        svgIcon.serialize(serializer) {
            serializer.serialize_u8(it)
        }
        serializer.serialize_u32(eventOccurrence)
        serializer.serialize_bool(hasSubEvents)
        serializer.decrease_container_depth()
    }

    fun bincodeSerialize(): ByteArray {
        val serializer = BincodeSerializer()
        serialize(serializer)
        return serializer.get_bytes()
    }

    companion object {
        fun deserialize(deserializer: Deserializer): TrackableModel {
            deserializer.increase_container_depth()
            val id = deserializer.deserialize_u32()
            val name = deserializer.deserialize_str()
            val svgIcon =
                deserializer.deserializeListOf {
                    deserializer.deserialize_u8()
                }
            val eventOccurrence = deserializer.deserialize_u32()
            val hasSubEvents = deserializer.deserialize_bool()
            deserializer.decrease_container_depth()
            return TrackableModel(id, name, svgIcon, eventOccurrence, hasSubEvents)
        }

        @Throws(DeserializationError::class)
        fun bincodeDeserialize(input: ByteArray?): TrackableModel {
            if (input == null) {
                throw DeserializationError("Cannot deserialize null array")
            }
            val deserializer = BincodeDeserializer(input)
            val value = deserialize(deserializer)
            if (deserializer.get_buffer_offset() < input.size) {
                throw DeserializationError("Some input bytes were not read")
            }
            return value
        }
    }
}

data class TrackableWithChildrenModel(
    val id: UInt,
    val name: String,
    val svgIcon: List<UByte>,
    val subEvents: List<com.ghuba.taprux.core.TrackableModel>,
) {
    fun serialize(serializer: Serializer) {
        serializer.increase_container_depth()
        serializer.serialize_u32(id)
        serializer.serialize_str(name)
        svgIcon.serialize(serializer) {
            serializer.serialize_u8(it)
        }
        subEvents.serialize(serializer) {
            it.serialize(serializer)
        }
        serializer.decrease_container_depth()
    }

    fun bincodeSerialize(): ByteArray {
        val serializer = BincodeSerializer()
        serialize(serializer)
        return serializer.get_bytes()
    }

    companion object {
        fun deserialize(deserializer: Deserializer): TrackableWithChildrenModel {
            deserializer.increase_container_depth()
            val id = deserializer.deserialize_u32()
            val name = deserializer.deserialize_str()
            val svgIcon =
                deserializer.deserializeListOf {
                    deserializer.deserialize_u8()
                }
            val subEvents =
                deserializer.deserializeListOf {
                    com.ghuba.taprux.core.TrackableModel.deserialize(deserializer)
                }
            deserializer.decrease_container_depth()
            return TrackableWithChildrenModel(id, name, svgIcon, subEvents)
        }

        @Throws(DeserializationError::class)
        fun bincodeDeserialize(input: ByteArray?): TrackableWithChildrenModel {
            if (input == null) {
                throw DeserializationError("Cannot deserialize null array")
            }
            val deserializer = BincodeDeserializer(input)
            val value = deserialize(deserializer)
            if (deserializer.get_buffer_offset() < input.size) {
                throw DeserializationError("Some input bytes were not read")
            }
            return value
        }
    }
}

data class ViewModel(
    val error: com.ghuba.taprux.core.ErrorModel? = null,
    val details: com.ghuba.taprux.core.TrackableWithChildrenModel? = null,
    val trackables: List<com.ghuba.taprux.core.TrackableModel>,
) {
    fun serialize(serializer: Serializer) {
        serializer.increase_container_depth()
        error.serializeOptionOf(serializer) {
            it.serialize(serializer)
        }
        details.serializeOptionOf(serializer) {
            it.serialize(serializer)
        }
        trackables.serialize(serializer) {
            it.serialize(serializer)
        }
        serializer.decrease_container_depth()
    }

    fun bincodeSerialize(): ByteArray {
        val serializer = BincodeSerializer()
        serialize(serializer)
        return serializer.get_bytes()
    }

    companion object {
        fun deserialize(deserializer: Deserializer): ViewModel {
            deserializer.increase_container_depth()
            val error =
                deserializer.deserializeOptionOf {
                    com.ghuba.taprux.core.ErrorModel.deserialize(deserializer)
                }
            val details =
                deserializer.deserializeOptionOf {
                    com.ghuba.taprux.core.TrackableWithChildrenModel.deserialize(deserializer)
                }
            val trackables =
                deserializer.deserializeListOf {
                    com.ghuba.taprux.core.TrackableModel.deserialize(deserializer)
                }
            deserializer.decrease_container_depth()
            return ViewModel(error, details, trackables)
        }

        @Throws(DeserializationError::class)
        fun bincodeDeserialize(input: ByteArray?): ViewModel {
            if (input == null) {
                throw DeserializationError("Cannot deserialize null array")
            }
            val deserializer = BincodeDeserializer(input)
            val value = deserialize(deserializer)
            if (deserializer.get_buffer_offset() < input.size) {
                throw DeserializationError("Some input bytes were not read")
            }
            return value
        }
    }
}
