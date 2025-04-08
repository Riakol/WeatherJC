package com.example.weatherappncjc

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherappncjc.data.WeatherModel
import com.example.weatherappncjc.screens.DialogSearch
import com.example.weatherappncjc.screens.MainCard
import com.example.weatherappncjc.screens.TabLayout
import org.json.JSONObject

const val API = "XXX"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val daysList = remember {
                mutableStateOf(listOf<WeatherModel>())
            }
            val dialogState = remember {
                mutableStateOf(false)
            }

            val currentCity = remember {
                mutableStateOf("Oskemen")
            }

            val currentDay = remember {
                mutableStateOf(
                    WeatherModel(
                    "",
                    "",
                    "0.0",
                    "",
                    "",
                    "0.0",
                    "0.0",
                    "",
                        "",
                        ""
                    )
                )
            }

            if (dialogState.value) {
                DialogSearch(dialogState,
                    onSubmit = {
                        getData(it, this, daysList, currentDay)
                    },
                    currentCity = {
                        currentCity.value = it
                    })
            }

            getData(currentCity.value, this, daysList, currentDay)
            Image(
                painter = painterResource(R.drawable.mainbackground),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.7f),
                contentScale = ContentScale.FillBounds
            )

            Column {
                MainCard(currentDay, onClickSync = {
                    getData(currentCity.value, this@MainActivity, daysList, currentDay)
                },
                    onClickSearch = {
                        dialogState.value = true
                    })
                TabLayout(daysList, currentDay)
            }
        }
    }
}

private fun getData(city: String,
                    context: Context,
                    daysList: MutableState<List<WeatherModel>>,
                    currentDay: MutableState<WeatherModel>
) {
    val url = "https://api.weatherapi.com/v1/forecast.json?" +
            "key=$API&" +
            "q=$city&" +
            "days=3&" +
            "aqi=no&" +
            "alerts=no"

    val queue = Volley.newRequestQueue(context)
    val stringRequest = StringRequest(
        Request.Method.GET, url,
        { response ->
            val list = getWeatherByDays(response)
            daysList.value = list.subList(1, list.size)
            currentDay.value = list[0]

        },
        { error ->

        }
    )
    queue.add(stringRequest)
}

private fun getWeatherByDays(response: String): List<WeatherModel> {
    if (response.isEmpty()) return emptyList()
    val mainObj = JSONObject(response)
    val list = ArrayList<WeatherModel>()
    val city = mainObj.getJSONObject("location").getString("name")
    val days = mainObj.getJSONObject("forecast").getJSONArray("forecastday")

    for (i in 0 until days.length()) {
        val item = days[i] as JSONObject
        list.add(
            WeatherModel(
                city,
                item.getString("date"),
                "",
                item.getJSONObject("day").getJSONObject("condition").getString("text"),
                item.getJSONObject("day").getJSONObject("condition").getString("icon"),
                item.getJSONObject("day").getString("maxtemp_c"),
                item.getJSONObject("day").getString("mintemp_c"),
                item.getJSONArray("hour").toString(),
                item.getJSONObject("astro").getString("sunrise"),
                item.getJSONObject("astro").getString("sunset"),
            )
        )
    }
    list[0] = list[0].copy(
        time = mainObj.getJSONObject("current").getString("last_updated"),
        currentTemp = mainObj.getJSONObject("current").getString("temp_c").toString()
    )
    return list
}
