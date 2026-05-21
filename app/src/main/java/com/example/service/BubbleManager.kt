package com.example.service

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import com.example.R
import android.view.ViewGroup
import android.widget.FrameLayout
import android.graphics.drawable.GradientDrawable

object BubbleManager {
    private var windowManager: WindowManager? = null
    
    fun showBubble(context: Context, title: String, summary: String) {
        val appContext = context.applicationContext
        if (windowManager == null) {
            windowManager = appContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        }

        val windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        windowParams.gravity = Gravity.TOP or Gravity.START
        windowParams.x = 0
        windowParams.y = 100

        // Create the view manually since we don't have standard Android XML layouts 
        // avoiding complex theming issues
        val bubbleView = FrameLayout(appContext)
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(android.graphics.Color.parseColor("#4B0082")) // deep purple
        }
        bubbleView.background = drawable
        val params = FrameLayout.LayoutParams(120, 120)
        bubbleView.layoutParams = params
        
        val initials = TextView(appContext).apply {
            text = "RSS"
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
        bubbleView.addView(initials)

        // Details Card
        val detailsCard = LinearLayout(appContext).apply {
            orientation = LinearLayout.VERTICAL
            val cardBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f
                setColor(android.graphics.Color.WHITE)
                // Border for contrast
                setStroke(2, android.graphics.Color.LTGRAY)
            }
            background = cardBg
            val p = LinearLayout.LayoutParams(600, LinearLayout.LayoutParams.WRAP_CONTENT)
            layoutParams = p
            setPadding(32, 32, 32, 32)
            visibility = View.GONE
        }
        
        val titleText = TextView(appContext).apply {
            text = title
            textSize = 16f
            setTypeface(null, android.graphics.Typeface.BOLD)
            setTextColor(android.graphics.Color.BLACK)
        }
        
        val summaryText = TextView(appContext).apply {
            text = summary
            textSize = 14f
            setTextColor(android.graphics.Color.DKGRAY)
            setPadding(0, 16, 0, 16)
        }
        
        val closeButton = TextView(appContext).apply {
            text = "FECHAR"
            setTextColor(android.graphics.Color.RED)
            setTypeface(null, android.graphics.Typeface.BOLD)
            setPadding(0, 16, 0, 0)
            gravity = Gravity.END
        }
        
        detailsCard.addView(titleText)
        detailsCard.addView(summaryText)
        detailsCard.addView(closeButton)

        val rootView = FrameLayout(appContext)
        val rootParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        rootView.layoutParams = rootParams
        
        // Add layouts
        val detailsParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        detailsParams.setMargins(140, 0, 0, 0)
        rootView.addView(detailsCard, detailsParams)
        rootView.addView(bubbleView)

        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f

        bubbleView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = windowParams.x
                    initialY = windowParams.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_UP -> {
                    val diffX = (event.rawX - initialTouchX).toInt()
                    val diffY = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(diffX) < 10 && Math.abs(diffY) < 10) {
                        // It's a click
                        if (detailsCard.visibility == View.GONE) {
                            detailsCard.visibility = View.VISIBLE
                        } else {
                            detailsCard.visibility = View.GONE
                        }
                    }
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    windowParams.x = initialX + (event.rawX - initialTouchX).toInt()
                    windowParams.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(rootView, windowParams)
                    true
                }
                else -> false
            }
        }

        closeButton.setOnClickListener {
            try {
                windowManager?.removeView(rootView)
            } catch (e: Exception) {}
        }

        try {
            windowManager?.addView(rootView, windowParams)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
