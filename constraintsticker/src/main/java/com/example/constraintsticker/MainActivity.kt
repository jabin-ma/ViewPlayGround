package com.example.constraintsticker

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : AppCompatActivity() {
    companion object {
        const val TAG = "Main"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        for (i in 0..360) {
            val result = calcDistanceToEdgePoint2(70.0,80.0, i.toDouble())
            Log.d(TAG, "onCreate:$i $result")
        }
    }

    fun calcDistanceToEdgePoint(centerX: Double, centerY: Double, theta: Double): Double {
        val step = PI / 4
        val absTheta = theta % step
        return when (theta) {
            in PI / 2..PI / 1 -> {
                Log.d(TAG, "calcDistanceToEdgePoint:4  ")
                centerX / sin(absTheta)
            }
            in PI / 3..PI / 2 -> {
                Log.d(TAG, "calcDistanceToEdgePoint:3  ")
                centerX / sin(step - absTheta)
            }
            in PI / 4..PI / 3 -> {
                Log.d(TAG, "calcDistanceToEdgePoint:2  ")
                centerY / cos(absTheta)
            }
            in 0.0..PI / 4 -> {
                Log.d(TAG, "calcDistanceToEdgePoint:1  ")
                centerY / cos(step - absTheta)
            }
            else -> -1.0
        }
    }

    fun calcDistanceToEdgePoint2(centerX: Double, centerY: Double, angle: Double): Double {
        val absAngle = angle % 45.0
        val loop = angle % 180.0
        return when(loop){
            in 135.0 .. 180.0 ->{
                centerY / cos(Math.toRadians(45.0 - absAngle))
            }
            in 90.0 .. 135.0 ->{
                centerX / cos(Math.toRadians(absAngle))
            }
            in 45.0 .. 90.0 ->{
                centerX / cos(Math.toRadians(45.0 - absAngle))
            }
            in 0.0 .. 45.0 ->{
                centerY / cos(Math.toRadians(absAngle))
            }
            else -> {
                -1.0
            }
        }
    }
}