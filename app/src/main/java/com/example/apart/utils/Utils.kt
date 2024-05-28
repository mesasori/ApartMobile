package com.example.apart.utils

import android.content.res.Resources
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import com.yandex.mapkit.SpannableString
import com.yandex.mapkit.geometry.BoundingBox
import com.yandex.mapkit.map.VisibleRegion
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.retryWhen
import java.io.IOException

fun VisibleRegion.toBoundingBox() = BoundingBox(bottomLeft, topRight)

fun SpannableString.toSpannable(@ColorInt color: Int): Spannable {
    val spannableString = android.text.SpannableString(text)
    spans.forEach {
        spannableString.setSpan(
            ForegroundColorSpan(color),
            it.begin,
            it.end,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
    }
    return spannableString
}

fun <T> List<T>.takeIfNotEmpty(): List<T>? = takeIf { it.isNotEmpty() }

fun <T : View, V> T.goneOrRun(value: V?, block: T.(V) -> Unit) {
    this.isVisible = value != null
    if (value != null) {
        this.block(value)
    }
}

private const val RETRY_TIME_IN_MILLIS = 15_000L
private const val RETRY_ATTEMPT_COUNT = 3

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Error(val exception: Throwable? = null) : Result<Nothing>
    object Loading : Result<Nothing>
}

fun <T> Flow<T>.asResult(): Flow<Result<T>> {
    return this
        .map<T, Result<T>> {
            Result.Success(it)
        }
        .onStart { emit(Result.Loading) }
        .retryWhen { cause, attempt ->
            if (cause is IOException && attempt < RETRY_ATTEMPT_COUNT) {
                delay(RETRY_TIME_IN_MILLIS)
                true
            } else {
                false
            }
        }
        .catch { emit(Result.Error(it)) }
}

val Number.toPx
    get() = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this.toFloat(),
        Resources.getSystem().displayMetrics
    )

