package com.example.weatherappncjc.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherappncjc.R
import com.example.weatherappncjc.data.WeatherModel
import com.example.weatherappncjc.ui.theme.BlueLight
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.pagerTabIndicatorOffset
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject


@Composable
fun MainCard(currentDay: MutableState<WeatherModel>, onClickSync: () -> Unit, onClickSearch: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(5.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(0.dp),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = BlueLight)
        )
        {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            )
            {
                Row(
                    modifier = Modifier
                        //.background(BlueLightExtra)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier
                            .padding(top = 8.dp, start = 8.dp),
                        text = currentDay.value.time,
                        style = TextStyle(
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    )
                    AsyncImage(
                        model = "https:${currentDay.value.icon}",
                        contentDescription = null,
                        modifier = Modifier
                            .size(35.dp)
                            .padding(top = 3.dp, end = 8.dp)
                    )
                }

                Text(
                    modifier = Modifier,
                    text = currentDay.value.city,
                    style = TextStyle(
                        fontSize = 24.sp,
                        color = Color.White
                    )
                )
                Text(
                    modifier = Modifier,
                    text = if (currentDay.value.currentTemp.isNotEmpty()) "${currentDay.value.currentTemp.toFloat().toInt()}°C" else "${currentDay.value.maxTemp.toFloat().toInt()}°C | ${currentDay.value.minTemp.toFloat().toInt()}°C",
                    style = TextStyle(
                        fontSize = 65.sp,
                        color = Color.White
                    )
                )
                Text(
                    modifier = Modifier,
                    text = currentDay.value.condition,
                    style = TextStyle(
                        fontSize = 16.sp,
                        color = Color.White
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        onClickSearch.invoke()
                    }

                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_search),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    Text(
                        modifier = Modifier,

                        text = if (currentDay.value.currentTemp.isNotEmpty()) "${currentDay.value.maxTemp.toFloat().toInt()}°C | ${currentDay.value.minTemp.toFloat().toInt()}°C" else "Sunrise: ${currentDay.value.sunrise} | Sunset: ${currentDay.value.sunset}"
                    ,
                        style = TextStyle(
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    )

                    IconButton(onClick = {
                        onClickSync.invoke()
                    }

                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sync),
                            contentDescription = null,
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun TabLayout(daysList: MutableState<List<WeatherModel>>, currentDay: MutableState<WeatherModel>) {
    val tabList = listOf("HOURS", "DAYS")
    val pagerState = rememberPagerState{ tabList.size }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .padding(start = 5.dp, end = 5.dp)
            .clip(RoundedCornerShape(5.dp)
            )
    ) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            indicator = { tabPositions->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(currentTabPosition = tabPositions[pagerState.currentPage])
                )
            },
            containerColor = BlueLight,
            contentColor = Color.White

        ) {
            tabList.forEachIndexed { index, text ->
                Tab(
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = {
                        Text(text = text)
                    }
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
        ) { pageIndex ->
            val list = when (pageIndex) {
                0 -> getWeatherByHours(currentDay.value.hours)
                1 -> daysList.value
                else -> daysList.value
            }
            MainList(list, currentDay)
        }
    }
}

private fun getWeatherByHours(response: String): List<WeatherModel> {
    if (response.isEmpty()) return emptyList()

    val hoursArray = JSONArray(response)
    val list = ArrayList<WeatherModel>()

    for (i in 0 until hoursArray.length()) {
        val item = hoursArray[i] as JSONObject
        list.add(
            WeatherModel(
                "",
                item.getString("time"),
                item.getString("temp_c").toFloat().toInt().toString(),
                item.getJSONObject("condition").getString("text"),
                item.getJSONObject("condition").getString("icon"),
                "",
                "",
                "",
                "",
                ""
            )
        )
    }
    return list
}