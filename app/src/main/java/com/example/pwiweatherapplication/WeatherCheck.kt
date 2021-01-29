package com.example.pwiweatherapplication

import android.content.Context
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.AsyncTask
import android.view.View
import android.widget.*
import kotlinx.android.synthetic.main.activity_weather_check.*
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class WeatherCheck : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather_check)
        /* ANIMACJA TLA */
        val layout: RelativeLayout = findViewById(R.id.layout_weather)
        val animationDrawable = layout.background as AnimationDrawable
        animationDrawable.setEnterFadeDuration(2000)
        animationDrawable.setExitFadeDuration(4000)
        animationDrawable.start()
        /*Załadowanie zapisanej przez użytkownika lokacji*/
        loadAddress()
        /*Wywołanie zapisu lokacji w momencie naciśnięcia przycisku zapisu*/
        save.setOnClickListener {
            saveAddress()
        }
    }
    /*Funkcja odpowiadająca na naciśnięcie przycisku SPRAWDŹ*/
    fun sendUserAddress (view: View){
        val userInput = findViewById<EditText>(R.id.userAddress)
        Constants.CITY = userAddress.text.toString()
        findViewById<TextView>(R.id.errorText).visibility = View.INVISIBLE
        weatherTask().execute()
    }
    /*Funkcja odpowiadająca za zapisanie wprowadzonej lokacji*/
    private fun saveAddress(){
        val savedAddress : String = userAddress.text.toString()
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply{
            putString("ADDRESS",savedAddress)
        }.apply()
        Toast.makeText(this,"Zapisano",Toast.LENGTH_LONG).show()
    }
    /*Funkcja odpowiadająca za odczyt zapisanej lokacji*/
    private fun loadAddress(){
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val savedAddress = sharedPreferences.getString("ADDRESS",null)
        Constants.CITY = savedAddress.toString()
        weatherTask().execute()
    }
    /*Właściwa klasa zajmująca się komunikacją z API OpenWeatherMap*/
    inner class weatherTask(): AsyncTask<String, Void, String>() {
        override fun onPreExecute() {
            super.onPreExecute()
            /*Schowanie paska ładowania*/
            findViewById<ProgressBar>(R.id.loader).visibility =View.GONE
        }
        /*Nadpisanie domyślnej aktywności w tle aplikacji, wysłanie requesta do API z analizą informacji zwrotnej*/
        override fun doInBackground(vararg params: String?): String? {
            var response:String?
            try{
                response = URL("https://api.openweathermap.org/data/2.5/weather?q=${Constants.CITY}&units=metric&lang=pl&appid=${Constants.WEATHER_API_KEY}").readText(Charsets.UTF_8)
            }
            catch (e: Exception){
                response = null
            }
            return response
        }
        /*Funkcja wywołana po otrzymaniu odpowiedzi z API*/
        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            try {
                /* Pobranie za pomocą JSON danych z API pogodowego*/
                val jsonObj = JSONObject(result)
                /*main, sys, wind itd to kategorie, pod którymi można znaleźć konkretne wartości np. temp
                * patrz więcej na https://openweathermap.org/current*/
                val main =  jsonObj.getJSONObject("main")
                val sys = jsonObj.getJSONObject("sys")
                val wind = jsonObj.getJSONObject("wind")
                val coord = jsonObj.getJSONObject("coord")
                val weather = jsonObj.getJSONArray("weather").getJSONObject(0)
                val updatedAt:Long = jsonObj.getLong("dt")
                val clouds = jsonObj.getJSONObject("clouds")
                /*Pobranie konkretnych wartości przy pomocy wcześniej zdefiniowanych kategorii,
                * konkretne nazwy pól również w linku wyżej w przykładowym API JSON Response*/
                val updatedAtText = "Stan na: " + SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH).format(Date(updatedAt*1000))
                /*Pola w kategorii main */
                val temp = main.getString("temp")+"°C"
                val tempMin = main.getString("temp_min") + "°C"
                val tempMax = main.getString("temp_max") + "°C"
                val pressure = main.getString("pressure") + " hPa"
                val humidity = main.getString("humidity") + " %"
                val feelsLike = main.getString("feels_like") + "°C"
                /*Pola w kategorii sys */
                val sunrise:Long = sys.getLong("sunrise")
                val sunset:Long = sys.getLong("sunset")
                /*Pola w kategorii wind */
                val windSpeed = wind.getString("speed") + " km/h"
                val windDeg = wind.getString("deg")
                /*Pola w kategorii weather */
                val weatherDescription = weather.getString("description")
                /*Pola w kategorii sys */
                val address = jsonObj.getString("name") + ", " + sys.getString("country")
                /*Pola w kategorii coord oraz weryfikacja kierunku geograficznego (API zwraca wartości ze znakiem + lub - )*/
                var lon = coord.getString("lon")
                    if (lon.toDouble()<0) { lon+=" W" } else { lon+=" E" }
                var lat = coord.getString("lat")
                    if (lat.toDouble()>0) { lat+=" N" } else { lat+=" S" }
                val coordinates = lon + " " + lat
                /*Pola w kategorii clouds */
                val cloudiness = clouds.getString("all") + " %"
                /*Pola w kategorii rain, niestety ale w większości sytuacji nie są dostępne */
                //val rain3h = rain.getString("rain.3h")
                /*Wpisanie danych pobranych z API do pól UI, wyświetlenie ich użytkownikowi */
                findViewById<TextView>(R.id.address).text= address
                findViewById<TextView>(R.id.updated_at).text = updatedAtText
                findViewById<TextView>(R.id.status).text = weatherDescription.capitalize()
                findViewById<TextView>(R.id.temp).text = temp
                findViewById<TextView>(R.id.temp_min).text = tempMin
                findViewById<TextView>(R.id.temp_max).text = tempMax
                findViewById<TextView>(R.id.sunrise).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunrise*1000))
                findViewById<TextView>(R.id.sunset).text = SimpleDateFormat("hh:mm a", Locale.ENGLISH).format(Date(sunset*1000))
                findViewById<TextView>(R.id.wind).text = windSpeed
                findViewById<TextView>(R.id.pressure).text = pressure
                findViewById<TextView>(R.id.humidity).text = humidity
                findViewById<TextView>(R.id.clouds).text = cloudiness
                findViewById<TextView>(R.id.feels_like).text = feelsLike
                findViewById<TextView>(R.id.coordinates).text = coordinates
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.mainContainer).visibility = View.VISIBLE
            }
            /*Error handler, wyświetla wiadomość gdy wprowadzona lokalizacja nie została odnaleziona w API*/
            catch (e: Exception) {
                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<TextView>(R.id.errorText).visibility = View.VISIBLE
            }
        }
    }
}