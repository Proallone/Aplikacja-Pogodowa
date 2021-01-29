package com.example.pwiweatherapplication

import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.os.Handler
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_welcome_view.*

/*Klasa odopowiadająca za czynności związane z ładowaniem aplikacji */
class WelcomeView : AppCompatActivity() {

    /*Zmienna określająca długość ekranu ładowania*/
    private val SPLASH_TIME_OUT:Long = 3000 // w milisekundach

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_view)
        /*Animacja tła*/
        val layout: LinearLayout = findViewById(R.id.WelcomeLayout)
        val welcomeAnimation = WelcomeLayout.background as AnimationDrawable
        welcomeAnimation.setEnterFadeDuration(2000)
        welcomeAnimation.setExitFadeDuration(2000)
        welcomeAnimation.start()
        /*Animacja ikony aplikacji, efekt pojawiania się*/
        val FIanimation = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        Logo.startAnimation(FIanimation)
        Welcoming.startAnimation(FIanimation)
        Autorzy.startAnimation(FIanimation)
        /*Funkcja, po której można umieścić dalsze instrukcje, które mają wykonać się po załadowaniu*/
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            /*Zamknięcie activity*/
            finish()
        }, SPLASH_TIME_OUT)
    }
}