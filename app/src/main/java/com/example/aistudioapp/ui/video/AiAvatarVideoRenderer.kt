package com.example.aistudioapp.ui.video

import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.view.TextureView
import androidx.annotation.MainThread
import kotlinx.coroutines.cancel
import com.example.aistudioapp.data.model.AvatarSelection
import com.example.aistudioapp.di.ServiceLocator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlin.math.min

class AiAvatarVideoRenderer(context: Context) {

    private val avatarStore = ServiceLocator.provideAvatarStore(context)
    private val scope = CoroutineScope(Dispatchers.Main.immediate + Job())
    private var textureView: TextureView? = null

    @MainThread
    fun bind(view: TextureView) {
        textureView = view
        scope.launch {
            avatarStore.activeAvatar.collectLatest { selection ->
                render(selection)
            }
        }
    }

    private fun render(selection: AvatarSelection?) {
        val view = textureView ?: return
        if (!view.isAvailable) return
        val canvas = view.lockCanvas() ?: return
        drawPlaceholder(canvas, selection)
        view.unlockCanvasAndPost(canvas)
    }

    private fun drawPlaceholder(canvas: Canvas, selection: AvatarSelection?) {
        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()
        val bgPaint = Paint().apply {
            shader = LinearGradient(
                0f,
                0f,
                width,
                height,
                intArrayOf(0xFF0B132B.toInt(), 0xFF1F2547.toInt()),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x88FFFFFF.toInt()
            style = Paint.Style.STROKE
            strokeWidth = 12f
        }
        val radius = min(width, height) * 0.35f
        val centerX = width / 2f
        val centerY = height / 2f
        canvas.drawCircle(centerX, centerY, radius, ringPaint)

        val text = selection?.label ?: "No Avatar"
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFEAF4EF.toInt()
            textAlign = Paint.Align.CENTER
            textSize = min(width, height) * 0.08f
        }
        val textBounds = RectF(
            centerX - radius,
            centerY + radius + 40f,
            centerX + radius,
            centerY + radius + 160f
        )
        val baseline = textBounds.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2)
        canvas.drawText(text, textBounds.centerX(), baseline, textPaint)
    }

    fun release() {
        scope.cancel()
    }
}
