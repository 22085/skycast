package com.skycast.weather.ui.search

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.skycast.weather.data.repository.CitySuggestion
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val repo = (application as com.skycast.weather.SkycastApplication)
            .container.weatherRepository

        setContent {
            MaterialTheme {
                var query by remember { mutableStateOf("") }
                var results by remember { mutableStateOf<List<CitySuggestion>>(emptyList()) }
                val scope = rememberCoroutineScope()
                var searchJob by remember { mutableStateOf<Job?>(null) }

                fun doSearch(q: String) {
                    searchJob?.cancel()
                    if (q.length < 2) {
                        results = emptyList()
                        return
                    }
                    searchJob = scope.launch {
                        delay(300) // debounce
                        repo.searchCity(q)
                            .onSuccess { results = it }
                            .onFailure { results = emptyList() }
                    }
                }

                @OptIn(ExperimentalMaterial3Api::class)
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("搜索城市") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                                }
                            },
                        )
                    },
                ) { padding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(horizontal = 16.dp),
                    ) {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { q ->
                                query = q
                                doSearch(q)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("输入城市名称") },
                            singleLine = true,
                        )

                        if (results.isEmpty() && query.length >= 2) {
                            Text(
                                text = "没有找到相关城市",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            items(results, key = { it.latitude to it.longitude }) { city ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val prefs = com.skycast.weather.data.prefs.SavedLocation(
                                                name = city.name,
                                                region = city.country,
                                                latitude = city.latitude,
                                                longitude = city.longitude,
                                                isCurrent = false,
                                            )
                                            scope.launch {
                                                (application as com.skycast.weather.SkycastApplication)
                                                    .container.settingsStore.saveLocation(prefs)
                                            }
                                            startActivity(
                                                androidx.content.Intent(
                                                    this@SearchActivity,
                                                    com.skycast.weather.MainActivity::class.java,
                                                ).apply { flags = androidx.content.Intent.FLAG_ACTIVITY_CLEAR_TOP }
                                            )
                                            finish()
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        Icons.Default.Place,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 12.dp),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(city.name, style = MaterialTheme.typography.bodyLarge)
                                        city.country?.let {
                                            Text(
                                                it,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                    }
                                    Text(
                                        "${city.latitude},${city.longitude}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
