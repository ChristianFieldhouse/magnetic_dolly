package com.example.magnetic_dolly_12

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.magnetic_dolly_12.databinding.ActivityFullscreenBinding
import java.io.File
import kotlin.math.pow
import kotlin.random.Random


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullscreenBinding
    private lateinit var fullscreenContent: TextView
    private lateinit var fullscreenContentControls: LinearLayout
    private val hideHandler = Handler()

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar
        if (Build.VERSION.SDK_INT >= 30) {
            fullscreenContent.windowInsetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     * */
    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
                Log.i("hi", "from touch event")
            }
            MotionEvent.ACTION_UP -> view.performClick()
            else -> {
            }
        }
        false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        StrictMode.setVmPolicy(
            VmPolicy.Builder(StrictMode.getVmPolicy())
                .detectLeakedClosableObjects()
                .build()
        )
        binding = ActivityFullscreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        isFullscreen = true

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent = binding.fullscreenContent
        fullscreenContent.setOnTouchListener( binding.customView.magnetControlListener )
        //fullscreenContent.setOnClickListener { binding.customView.magneticTouch() }

        fullscreenContentControls = binding.fullscreenContentControls

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        binding.dummyButton.setOnTouchListener(delayHideTouchListener)
        //binding.customView.magnetControlListener
        //binding.customView.setOnTouchListener(binding.customView.magnetControlListener)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun touchInput() {
        if (isFullscreen) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreenContentControls.visibility = View.GONE
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        if (Build.VERSION.SDK_INT >= 30) {
            fullscreenContent.windowInsetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            fullscreenContent.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
        isFullscreen = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}

class CustomView @JvmOverloads constructor(context: Context,
                                           attrs: AttributeSet? = null, defStyleAttr: Int = 0)
    : View(context, attrs, defStyleAttr) {
    // Called when the view should render its content.
    //private var fileName: String? = "@drawable/dronepic"
    //private var file: File = File(fileName)
    //private var splash = BitmapFactory.decodeFile(file.absolutePath)

    private lateinit var binding: ActivityFullscreenBinding
    private val mpaint = Paint()
    private val mrect = RectF(0.toFloat(), 0.toFloat(), 1000.toFloat(), 1000.toFloat())
    private val ballcol = ContextCompat.getColor(context, R.color.ball_default);
    private val usercol = ContextCompat.getColor(context, R.color.user_col);
    private val magnets : MutableList<Magnet> = mutableListOf(
        Magnet(x = Random.nextFloat() * 1000, y = Random.nextFloat() * 2000, xv = 3F, yv = 0F, radius = 20F, strength=20F, colour = usercol),
        Magnet(x = Random.nextFloat() * 1000, y = Random.nextFloat() * 2000, xv = 0F, yv = 0F, still=true, radius = 20F, strength=1F, colour = ballcol),
        Magnet(x = Random.nextFloat() * 1000, y = Random.nextFloat() * 2000, xv = 0F, yv = 0F, still=true, radius = 20F, strength=1F, colour = ballcol),
        Magnet(x = Random.nextFloat() * 1000, y = Random.nextFloat() * 2000, xv = 0F, yv = 0F, still=true, radius = 20F, strength=1F, colour = ballcol),
        Magnet(x = Random.nextFloat() * 1000, y = Random.nextFloat() * 2000, xv = 0F, yv = 0F, still=true, radius = 20F, strength=1F, colour = ballcol),
        Magnet(x = Random.nextFloat() * 1000, y = Random.nextFloat() * 2000, xv = 0F, yv = 0F, still=true, radius = 20F, strength=1F, colour = ballcol),
        Magnet(x = Random.nextFloat() * 1000, y = Random.nextFloat() * 2000, xv = 0F, yv = 0F, still=true, radius = 20F, strength=1F, colour = ballcol),
    )

    val magnetControlListener = View.OnTouchListener { view, motionEvent ->
        magnets[0].setPos(motionEvent.x, motionEvent.y)
        view.performClick()
        false
    }

    fun magneticTouch(){
        magnets[0].setPos(0F, 0F)
    }

    private var trail : ArrayDeque<Pair<Float, Float>> = ArrayDeque()
    private var frameno : Int = 0;

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        frameno += 1
        // DRAW STUFF HERE
        //canvas?.drawBitmap(splash, null, mrect, mpaint)
        //canvas?.drawRect(0.toFloat(), 0.toFloat(), 100.toFloat(), 100.toFloat(), mpaint)
        if (frameno % 20 == 0){
            trail.addFirst(magnets[0].getPos())
            if (trail.size > 15){
                trail.removeLast()
            }
        }
        trail.forEachIndexed { i, element ->
            val (x, y) = element;
            mpaint.setColor(Color.argb((255 * (1 - i.toFloat()/trail.size)).toInt(), 255, 255, 255))
            canvas?.drawCircle(x, y, 10F, mpaint)
        }
        for (mag in magnets){
            mag.setCenter(width.toFloat()/2, height.toFloat()/2) // todo
            mag.step(other_magnets = magnets)
            mag.drawOn(canvas, mpaint)
        }
        invalidate()
    }
}

class Magnet(
    private var x: Float = 0F,
    private var y: Float = 0F,
    private var xv: Float = 0F,
    private var yv: Float = 0F,
    private val radius: Float = 10F,
    private val strength: Float = 1F,
    private val mass: Float = 1e10F,
    private val still: Boolean = false,
    private var x_center: Float = 500F,
    private var y_center: Float = 1000F,
    private val colour: Int = Color.argb(255, 0, 0, 0),
){

    private var elasticity = 1F/10000

    fun drawOn(canvas : Canvas?, paint: Paint){
        paint.setColor(colour)
        canvas?.drawCircle(x, y, radius, paint)
    }

    fun setCenter(xc: Float, yc: Float){
        x_center = xc
        y_center = yc
    }

    fun getPos() : Pair<Float, Float>{
        return Pair<Float, Float>(x, y)
    }

    fun setPos(to_x : Float, to_y : Float){
        x = to_x
        y = to_y
    }

    fun step(other_magnets : MutableList<Magnet>){
        if (!still){
            var fx = (x_center - x) * elasticity
            var fy = (y_center - y) * elasticity
            for (other_mag in other_magnets){
                if (other_mag.getPos() == Pair<Float, Float>(x, y)){
                    continue;
                }
                val (nexx, nexy) = force(other_mag)
                fx += nexx
                fy += nexy
            }
            //Log.i("force: ", "$fx")
            xv = (xv + fx) * 0.999F
            yv = (yv + fy) * 0.999F
            x += xv
            y += yv
        }
    }

    fun force(magnet: Magnet) : Pair<Float, Float>{
        val xs = (magnet.x - x)
        val ys = (magnet.y - y)
        return Pair<Float, Float>(
            xs * strength * magnet.strength * (xs*xs + ys*ys).pow(-3/2),
            ys * strength * magnet.strength * (xs*xs + ys*ys).pow(-3/2),
        )
    }
}