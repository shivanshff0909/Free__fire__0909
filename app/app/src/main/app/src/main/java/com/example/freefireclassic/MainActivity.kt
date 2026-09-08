package com.example.freefireclassic

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import kotlin.math.*
import kotlin.random.Random

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(GameView(this))
    }

    class GameView(context: Context) : View(context) {
        private val paint = Paint().apply { isAntiAlias = true }
        private var playerX = 500f
        private var playerY = 800f
        private var joystickX = 200f
        private var joystickY = 1400f
        private var isMoving = false
        private var moveAngle = 0f
        private var health = 100
        private var score = 0
        private var bullets = mutableListOf<Bullet>()
        private var enemies = mutableListOf<Enemy>()
        private var lastShot = 0L
        private var gameOver = false

        data class Bullet(var x: Float, var y: Float, var angle: Float)
        data class Enemy(var x: Float, var y: Float, var hp: Int = 100)

        init {
            repeat(5) {
                enemies.add(Enemy(Random.nextInt(100, 1000).toFloat(), Random.nextInt(100, 600).toFloat()))
            }
            postDelayed({ gameLoop() }, 16)
        }

        fun gameLoop() {
            if (gameOver) return
            if (isMoving) {
                playerX += cos(moveAngle) * 12
                playerY += sin(moveAngle) * 12
                playerX = playerX.coerceIn(50f, 1000f)
                playerY = playerY.coerceIn(50f, 1400f)
            }
            bullets.forEach {
                it.x += cos(it.angle) * 20
                it.y += sin(it.angle) * 20
            }
            bullets.removeAll { it.x < 0 || it.x > 1080 || it.y < 0 || it.y > 1600 }

            val iter = enemies.iterator()
            while(iter.hasNext()){
                val e = iter.next()
                for(b in bullets){
                    if(hypot(e.x - b.x, e.y - b.y) < 60){
                        e.hp -= 50
                        if(e.hp <= 0){
                            iter.remove()
                            score += 100
                            enemies.add(Enemy(Random.nextInt(100, 1000).toFloat(), Random.nextInt(100, 600).toFloat()))
                        }
                        break
                    }
                }
                if(hypot(e.x - playerX, e.y - playerY) < 80){
                    health -= 1
                    if(health <= 0) gameOver = true
                }
            }
            invalidate()
            postDelayed({ gameLoop() }, 16)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            canvas.drawColor(Color.rgb(30, 100, 40))

            // Player
            paint.color = Color.BLUE
            canvas.drawCircle(playerX, playerY, 40f, paint)
            paint.color = Color.BLACK
            paint.textSize = 30f
            canvas.drawText("HP: $health SCORE: $score", 20f, 60f, paint)

            // Enemies
            paint.color = Color.RED
            enemies.forEach { canvas.drawCircle(it.x, it.y, 35f, paint) }

            // Bullets
            paint.color = Color.YELLOW
            bullets.forEach { canvas.drawCircle(it.x, it.y, 10f, paint) }

            // Joystick
            paint.color = Color.argb(100, 255, 255, 255)
            canvas.drawCircle(joystickX, joystickY, 150f, paint)
            paint.color = Color.argb(200, 255, 255, 255)
            if(isMoving){
                canvas.drawCircle(joystickX + cos(moveAngle)*70, joystickY + sin(moveAngle)*70, 60f, paint)
            } else {
                canvas.drawCircle(joystickX, joystickY, 60f, paint)
            }

            // Fire Button
            paint.color = Color.argb(150, 255, 0, 0)
            canvas.drawCircle(900f, 1400f, 100f, paint)
            paint.color = Color.WHITE
            paint.textSize = 40f
            canvas.drawText("FIRE", 840f, 1415f, paint)

            if(gameOver){
                paint.color = Color.BLACK
                paint.textSize = 80f
                canvas.drawText("GAME OVER", 300f, 800f, paint)
            }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val x = event.x
            val y = event.y
            when(event.action){
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    if(hypot(x - 900f, y - 1400f) < 120){
                        if(System.currentTimeMillis() - lastShot > 300){
                            val angle = if(enemies.isNotEmpty()) atan2(enemies[0].y - playerY, enemies[0].x - playerX) else 0f
                            bullets.add(Bullet(playerX, playerY, angle))
                            lastShot = System.currentTimeMillis()
                        }
                    } else if(hypot(x - joystickX, y - joystickY) < 200){
                        isMoving = true
                        moveAngle = atan2(y - joystickY, x - joystickX)
                    }
                }
                MotionEvent.ACTION_UP -> { isMoving = false }
            }
            return true
        }
    }
}
