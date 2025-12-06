package id.viasco.compassqstile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Icon
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import kotlin.math.roundToInt

class CompassTileService : TileService(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var magnetometer: Sensor? = null

    private val gravity = FloatArray(3)
    private val geomagnetic = FloatArray(3)
    private var hasGravity = false
    private var hasGeomagnetic = false

    private var lastAzimuth = 0f
    private val MIN_CHANGE_DEGREE = 5f // Only update if changed > 5 degrees
    
    private val PREFS_NAME = "compass_prefs"
    private val KEY_IS_ACTIVE = "is_active"

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        
        val isActive = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).getBoolean(KEY_IS_ACTIVE, false)
        
        if (isActive) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = "Loading..."
            startSensors()
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Compass"
            tile.icon = Icon.createWithResource(this, R.drawable.ic_compass)
        }
        tile.updateTile()
    }

    override fun onStopListening() {
        super.onStopListening()
        stopSensors()
    }
    
    override fun onClick() {
        super.onClick()
        val tile = qsTile ?: return
        
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val newState = !prefs.getBoolean(KEY_IS_ACTIVE, false)
        
        prefs.edit().putBoolean(KEY_IS_ACTIVE, newState).apply()
        
        if (newState) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = "Loading..."
            startSensors()
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = "Compass"
            tile.icon = Icon.createWithResource(this, R.drawable.ic_compass)
            stopSensors()
        }
        tile.updateTile()
    }
    
    private fun startSensors() {
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        magnetometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }
    
    private fun stopSensors() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return

        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, gravity, 0, 3)
            hasGravity = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, geomagnetic, 0, 3)
            hasGeomagnetic = true
        }

        if (hasGravity && hasGeomagnetic) {
            val R = FloatArray(9)
            val I = FloatArray(9)
            if (SensorManager.getRotationMatrix(R, I, gravity, geomagnetic)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)
                
                // Convert radians to degrees
                var azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat()
                // Normalize to 0-360
                azimuth = (azimuth + 360) % 360

                // Check if change is significant enough to update UI
                if (Math.abs(azimuth - lastAzimuth) > MIN_CHANGE_DEGREE) {
                    lastAzimuth = azimuth
                    updateTile(azimuth)
                }
            }
        }
    }

    private fun updateTile(azimuth: Float) {
        val tile = qsTile ?: return
        
        // 1. Update Label Text (Cardinal Direction)
        val direction = getDirectionLabel(azimuth)
        tile.label = "${azimuth.roundToInt()}° $direction"
        
        // 2. Update Icon (Bitmap Rotation)
        // We redraw the icon rotated according to the azimuth.
        // Note: Icon rotation is counter-clockwise because we want the needle 
        // to point North relative to the device.
        tile.icon = Icon.createWithBitmap(getRotatedIconBitmap(-azimuth))
        
        tile.state = Tile.STATE_ACTIVE
        tile.updateTile()
    }

    private fun getDirectionLabel(azimuth: Float): String {
        return when {
            azimuth >= 337.5 || azimuth < 22.5 -> "N"
            azimuth >= 22.5 && azimuth < 67.5 -> "NE"
            azimuth >= 67.5 && azimuth < 112.5 -> "E"
            azimuth >= 112.5 && azimuth < 157.5 -> "SE"
            azimuth >= 157.5 && azimuth < 202.5 -> "S"
            azimuth >= 202.5 && azimuth < 247.5 -> "SW"
            azimuth >= 247.5 && azimuth < 292.5 -> "W"
            azimuth >= 292.5 && azimuth < 337.5 -> "NW"
            else -> ""
        }
    }

    private fun getRotatedIconBitmap(degrees: Float): Bitmap {
        // Get base drawable
        val drawable = getDrawable(R.drawable.ic_compass) ?: return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        
        // Create bitmap for drawing
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Set icon color to white (QS Tile standard)
        drawable.colorFilter = PorterDuffColorFilter(0xFFFFFFFF.toInt(), PorterDuff.Mode.SRC_IN)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        
        // Rotate canvas
        canvas.save()
        canvas.rotate(degrees, width / 2f, height / 2f)
        drawable.draw(canvas)
        canvas.restore()
        
        return bitmap
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No detailed implementation needed for this case
    }
}