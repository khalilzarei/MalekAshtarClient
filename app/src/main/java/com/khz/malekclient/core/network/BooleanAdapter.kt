package com.khz.malekclient.core.network

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * TypeAdapter سفارشی برای Boolean که چندین فرمت سرور را می‌پذیرد:
 *
 *  - true / false (بولی استاندارد)
 *  - 1 / 0 (عدد — که سرور شما برای must_change_password می‌فرستد)
 *  - "1" / "0" / "true" / "false" (رشته)
 *  - null → null
 *
 * چون سرور در بعضی فیلدها (مثل must_change_password)
 * عدد می‌فرستد و در بعضی Boolean، این adapter ایمنی لازم را فراهم می‌کند.
 */
class BooleanAdapter : TypeAdapter<Boolean?>() {

    override fun write(out: JsonWriter, value: Boolean?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(reader: JsonReader): Boolean? {
        return when (reader.peek()) {
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            JsonToken.BOOLEAN -> reader.nextBoolean()
            JsonToken.NUMBER -> {
                // عدد: 0 → false، هر چیز دیگر → true
                reader.nextInt() != 0
            }
            JsonToken.STRING -> {
                // رشته: "true"/"1" → true، بقیه → false
                when (reader.nextString()?.lowercase()) {
                    "true", "1", "yes", "بله" -> true
                    else -> false
                }
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }
}
