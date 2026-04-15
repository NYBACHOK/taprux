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

fun <K, V> Map<K, V>.serialize(
    serializer: Serializer,
    serializeEntry: Serializer.(K, V) -> Unit,
) {
    serializer.serialize_len(size.toLong())
    forEach { (key, value) ->
        serializer.serializeEntry(key, value)
    }
}

fun <K, V> Deserializer.deserializeMapOf(deserializeEntry: (Deserializer) -> Pair<K, V>): Map<K, V> {
    val length = deserialize_len()
    val map = mutableMapOf<K, V>()
    repeat(length.toInt()) {
        val (key, value) = deserializeEntry(this)
        map[key] = value
    }
    return map
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

data class ApplicationSettings(
    val appVersion: String,
    val weekStartDay: com.ghuba.taprux.core.WeekDay,
    val showTrackableNames: Boolean,
    val homeTimezone: String,
    val deviceTimezone: String,
    val notifInsightsReports: Boolean,
    val notifHealthHerstory: Boolean,
    val isInsightsActivated: Boolean,
    val hasAccess: Boolean,
    val isTrial: Boolean,
    val statusMessage: String? = null,
) {
    fun serialize(serializer: Serializer) {
        serializer.increase_container_depth()
        serializer.serialize_str(appVersion)
        weekStartDay.serialize(serializer)
        serializer.serialize_bool(showTrackableNames)
        serializer.serialize_str(homeTimezone)
        serializer.serialize_str(deviceTimezone)
        serializer.serialize_bool(notifInsightsReports)
        serializer.serialize_bool(notifHealthHerstory)
        serializer.serialize_bool(isInsightsActivated)
        serializer.serialize_bool(hasAccess)
        serializer.serialize_bool(isTrial)
        statusMessage.serializeOptionOf(serializer) {
            serializer.serialize_str(it)
        }
        serializer.decrease_container_depth()
    }

    fun bincodeSerialize(): ByteArray {
        val serializer = BincodeSerializer()
        serialize(serializer)
        return serializer.get_bytes()
    }

    companion object {
        fun deserialize(deserializer: Deserializer): ApplicationSettings {
            deserializer.increase_container_depth()
            val appVersion = deserializer.deserialize_str()
            val weekStartDay = com.ghuba.taprux.core.WeekDay.deserialize(deserializer)
            val showTrackableNames = deserializer.deserialize_bool()
            val homeTimezone = deserializer.deserialize_str()
            val deviceTimezone = deserializer.deserialize_str()
            val notifInsightsReports = deserializer.deserialize_bool()
            val notifHealthHerstory = deserializer.deserialize_bool()
            val isInsightsActivated = deserializer.deserialize_bool()
            val hasAccess = deserializer.deserialize_bool()
            val isTrial = deserializer.deserialize_bool()
            val statusMessage =
                deserializer.deserializeOptionOf {
                    deserializer.deserialize_str()
                }
            deserializer.decrease_container_depth()
            return ApplicationSettings(appVersion, weekStartDay, showTrackableNames, homeTimezone, deviceTimezone, notifInsightsReports, notifHealthHerstory, isInsightsActivated, hasAccess, isTrial, statusMessage)
        }

        @Throws(DeserializationError::class)
        fun bincodeDeserialize(input: ByteArray?): ApplicationSettings {
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

enum class AppliedChanges {
    USERTRACKABLE;

    fun serialize(serializer: Serializer) {
        serializer.increase_container_depth()
        serializer.serialize_variant_index(ordinal)
        serializer.decrease_container_depth()
    }

    fun bincodeSerialize(): ByteArray {
        val serializer = BincodeSerializer()
        serialize(serializer)
        return serializer.get_bytes()
    }

    companion object {
        @Throws(DeserializationError::class)
        fun deserialize(deserializer: Deserializer): AppliedChanges {
            deserializer.increase_container_depth()
            val index = deserializer.deserialize_variant_index()
            deserializer.decrease_container_depth()
            return when (index) {
                0 -> USERTRACKABLE
                else -> throw DeserializationError("Unknown variant index for AppliedChanges: $index")
            }
        }

        @Throws(DeserializationError::class)
        fun bincodeDeserialize(input: ByteArray?): AppliedChanges {
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

    data class Changes(
        val value: com.ghuba.taprux.core.AppliedChanges,
    ) : Effect {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(1)
            value.serialize(serializer)
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): Changes {
                deserializer.increase_container_depth()
                val value = com.ghuba.taprux.core.AppliedChanges.deserialize(deserializer)
                deserializer.decrease_container_depth()
                return Changes(value)
            }
        }
    }

    data class Query(
        val value: com.ghuba.taprux.core.QueryRequest,
    ) : Effect {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(2)
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
                1 -> Changes.deserialize(deserializer)
                2 -> Query.deserialize(deserializer)
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

    /// Load all resources on first load
    data object Initialize: Event {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(0)
            serializer.decrease_container_depth()
        }

        fun deserialize(deserializer: Deserializer): Initialize {
            return Initialize
        }
    }

    data class Query(
        val value: com.ghuba.taprux.core.QueryRequest,
    ) : Event {
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
        fun deserialize(deserializer: Deserializer): Event {
            val index = deserializer.deserialize_variant_index()
            return when (index) {
                0 -> Initialize.deserialize(deserializer)
                1 -> Query.deserialize(deserializer)
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

    data object AllTrackables: QueryRequest {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(0)
            serializer.decrease_container_depth()
        }

        fun deserialize(deserializer: Deserializer): AllTrackables {
            return AllTrackables
        }
    }

    data object UserTrackables: QueryRequest {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(1)
            serializer.decrease_container_depth()
        }

        fun deserialize(deserializer: Deserializer): UserTrackables {
            return UserTrackables
        }
    }

    data class AddUserTrackable(
        val value: UInt,
    ) : QueryRequest {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(2)
            serializer.serialize_u32(value)
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): AddUserTrackable {
                deserializer.increase_container_depth()
                val value = deserializer.deserialize_u32()
                deserializer.decrease_container_depth()
                return AddUserTrackable(value)
            }
        }
    }

    data class AddOccurrence(
        val value: UInt,
    ) : QueryRequest {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(3)
            serializer.serialize_u32(value)
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): AddOccurrence {
                deserializer.increase_container_depth()
                val value = deserializer.deserialize_u32()
                deserializer.decrease_container_depth()
                return AddOccurrence(value)
            }
        }
    }

    data class DeleteOccurrence(
        val value: UInt,
    ) : QueryRequest {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(4)
            serializer.serialize_u32(value)
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): DeleteOccurrence {
                deserializer.increase_container_depth()
                val value = deserializer.deserialize_u32()
                deserializer.decrease_container_depth()
                return DeleteOccurrence(value)
            }
        }
    }

    data object Occurrences: QueryRequest {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(5)
            serializer.decrease_container_depth()
        }

        fun deserialize(deserializer: Deserializer): Occurrences {
            return Occurrences
        }
    }

    data class Details(
        val value: UInt,
    ) : QueryRequest {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(6)
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

    data object Settings: QueryRequest {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(7)
            serializer.decrease_container_depth()
        }

        fun deserialize(deserializer: Deserializer): Settings {
            return Settings
        }
    }

    data class UpdateSettings(
        val value: com.ghuba.taprux.core.ApplicationSettings,
    ) : QueryRequest {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(8)
            value.serialize(serializer)
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): UpdateSettings {
                deserializer.increase_container_depth()
                val value = com.ghuba.taprux.core.ApplicationSettings.deserialize(deserializer)
                deserializer.decrease_container_depth()
                return UpdateSettings(value)
            }
        }
    }

    companion object {
        @Throws(DeserializationError::class)
        fun deserialize(deserializer: Deserializer): QueryRequest {
            val index = deserializer.deserialize_variant_index()
            return when (index) {
                0 -> AllTrackables.deserialize(deserializer)
                1 -> UserTrackables.deserialize(deserializer)
                2 -> AddUserTrackable.deserialize(deserializer)
                3 -> AddOccurrence.deserialize(deserializer)
                4 -> DeleteOccurrence.deserialize(deserializer)
                5 -> Occurrences.deserialize(deserializer)
                6 -> Details.deserialize(deserializer)
                7 -> Settings.deserialize(deserializer)
                8 -> UpdateSettings.deserialize(deserializer)
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

    data class AllTrackables(
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
            fun deserialize(deserializer: Deserializer): AllTrackables {
                deserializer.increase_container_depth()
                val value =
                    deserializer.deserializeListOf {
                        com.ghuba.taprux.core.TrackableModel.deserialize(deserializer)
                    }
                deserializer.decrease_container_depth()
                return AllTrackables(value)
            }
        }
    }

    data class UserTrackables(
        val value: List<com.ghuba.taprux.core.TrackableModel>,
    ) : QueryResponse {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(2)
            value.serialize(serializer) {
                it.serialize(serializer)
            }
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): UserTrackables {
                deserializer.increase_container_depth()
                val value =
                    deserializer.deserializeListOf {
                        com.ghuba.taprux.core.TrackableModel.deserialize(deserializer)
                    }
                deserializer.decrease_container_depth()
                return UserTrackables(value)
            }
        }
    }

    data class Occurrences(
        val value: Map<UInt, UInt>,
    ) : QueryResponse {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(3)
            value.serialize(serializer) { key, value ->
                serializer.serialize_u32(key)
                serializer.serialize_u32(value)
            }
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): Occurrences {
                deserializer.increase_container_depth()
                val value =
                    deserializer.deserializeMapOf {
                        val key = deserializer.deserialize_u32()
                        val value = deserializer.deserialize_u32()
                        Pair(key, value)
                    }
                deserializer.decrease_container_depth()
                return Occurrences(value)
            }
        }
    }

    data class Clicked(
        val value: UInt,
    ) : QueryResponse {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(4)
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

    data class DeletedOccurrence(
        val value: UInt,
    ) : QueryResponse {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(5)
            serializer.serialize_u32(value)
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): DeletedOccurrence {
                deserializer.increase_container_depth()
                val value = deserializer.deserialize_u32()
                deserializer.decrease_container_depth()
                return DeletedOccurrence(value)
            }
        }
    }

    data object AddedUserTrackable: QueryResponse {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(6)
            serializer.decrease_container_depth()
        }

        fun deserialize(deserializer: Deserializer): AddedUserTrackable {
            return AddedUserTrackable
        }
    }

    data class Details(
        val value: com.ghuba.taprux.core.TrackableWithChildrenModel,
    ) : QueryResponse {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(7)
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

    data class Settings(
        val value: com.ghuba.taprux.core.ApplicationSettings,
    ) : QueryResponse {
        override fun serialize(serializer: Serializer) {
            serializer.increase_container_depth()
            serializer.serialize_variant_index(8)
            value.serialize(serializer)
            serializer.decrease_container_depth()
        }

        companion object {
            fun deserialize(deserializer: Deserializer): Settings {
                deserializer.increase_container_depth()
                val value = com.ghuba.taprux.core.ApplicationSettings.deserialize(deserializer)
                deserializer.decrease_container_depth()
                return Settings(value)
            }
        }
    }

    companion object {
        @Throws(DeserializationError::class)
        fun deserialize(deserializer: Deserializer): QueryResponse {
            val index = deserializer.deserialize_variant_index()
            return when (index) {
                0 -> None.deserialize(deserializer)
                1 -> AllTrackables.deserialize(deserializer)
                2 -> UserTrackables.deserialize(deserializer)
                3 -> Occurrences.deserialize(deserializer)
                4 -> Clicked.deserialize(deserializer)
                5 -> DeletedOccurrence.deserialize(deserializer)
                6 -> AddedUserTrackable.deserialize(deserializer)
                7 -> Details.deserialize(deserializer)
                8 -> Settings.deserialize(deserializer)
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
    val orderKey: UInt,
    val name: String,
    val svgIcon: List<UByte>,
    val hasSubEvents: Boolean,
) {
    fun serialize(serializer: Serializer) {
        serializer.increase_container_depth()
        serializer.serialize_u32(id)
        serializer.serialize_u32(orderKey)
        serializer.serialize_str(name)
        svgIcon.serialize(serializer) {
            serializer.serialize_u8(it)
        }
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
            val orderKey = deserializer.deserialize_u32()
            val name = deserializer.deserialize_str()
            val svgIcon =
                deserializer.deserializeListOf {
                    deserializer.deserialize_u8()
                }
            val hasSubEvents = deserializer.deserialize_bool()
            deserializer.decrease_container_depth()
            return TrackableModel(id, orderKey, name, svgIcon, hasSubEvents)
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
    val allTrackables: List<com.ghuba.taprux.core.TrackableModel>,
    val userTrackables: List<com.ghuba.taprux.core.TrackableModel>,
    val occurrences: Map<UInt, UInt>,
    val settings: com.ghuba.taprux.core.ApplicationSettings,
) {
    fun serialize(serializer: Serializer) {
        serializer.increase_container_depth()
        error.serializeOptionOf(serializer) {
            it.serialize(serializer)
        }
        details.serializeOptionOf(serializer) {
            it.serialize(serializer)
        }
        allTrackables.serialize(serializer) {
            it.serialize(serializer)
        }
        userTrackables.serialize(serializer) {
            it.serialize(serializer)
        }
        occurrences.serialize(serializer) { key, value ->
            serializer.serialize_u32(key)
            serializer.serialize_u32(value)
        }
        settings.serialize(serializer)
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
            val allTrackables =
                deserializer.deserializeListOf {
                    com.ghuba.taprux.core.TrackableModel.deserialize(deserializer)
                }
            val userTrackables =
                deserializer.deserializeListOf {
                    com.ghuba.taprux.core.TrackableModel.deserialize(deserializer)
                }
            val occurrences =
                deserializer.deserializeMapOf {
                    val key = deserializer.deserialize_u32()
                    val value = deserializer.deserialize_u32()
                    Pair(key, value)
                }
            val settings = com.ghuba.taprux.core.ApplicationSettings.deserialize(deserializer)
            deserializer.decrease_container_depth()
            return ViewModel(error, details, allTrackables, userTrackables, occurrences, settings)
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

enum class WeekDay {
    SUNDAY,
    MONDAY;

    fun serialize(serializer: Serializer) {
        serializer.increase_container_depth()
        serializer.serialize_variant_index(ordinal)
        serializer.decrease_container_depth()
    }

    fun bincodeSerialize(): ByteArray {
        val serializer = BincodeSerializer()
        serialize(serializer)
        return serializer.get_bytes()
    }

    companion object {
        @Throws(DeserializationError::class)
        fun deserialize(deserializer: Deserializer): WeekDay {
            deserializer.increase_container_depth()
            val index = deserializer.deserialize_variant_index()
            deserializer.decrease_container_depth()
            return when (index) {
                0 -> SUNDAY
                1 -> MONDAY
                else -> throw DeserializationError("Unknown variant index for WeekDay: $index")
            }
        }

        @Throws(DeserializationError::class)
        fun bincodeDeserialize(input: ByteArray?): WeekDay {
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
