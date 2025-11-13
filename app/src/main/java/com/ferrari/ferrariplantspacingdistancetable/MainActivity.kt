package com.ferrari.ferrariplantspacingdistancetable

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ferrari.ferrariplantspacingdistancetable.AppDatabase
import com.ferrari.ferrariplantspacingdistancetable.ui.WheelPicker
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        setContent {
            FerrariApp(db)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FerrariApp(db: AppDatabase) {
    var tabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Old Model", "New Model")

    var selectedA by rememberSaveable { mutableStateOf<Int?>(null) }
    var selectedB by rememberSaveable { mutableStateOf<Int?>(null) }

    val allGears by produceState(initialValue = emptyList<Int>()) {
        while (AppDatabase.allGearsList.isEmpty()) {
            delay(100)
        }
        value = AppDatabase.allGearsList
    }

    Scaffold(
        topBar = {
            TabRow(selectedTabIndex = tabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = tabIndex == index,
                        onClick = { tabIndex = index },
                        text = { Text(stringResource(if (index == 0) R.string.tab_old else R.string.tab_new)) }
                    )
                }
            }
        }
    ) { padding ->
        when (tabIndex) {
            0 -> ModelScreen(
                db = db,
                model = "old",
                padding = padding,
                allGears = allGears,
                selectedA = selectedA,
                selectedB = selectedB,
                onAChanged = { selectedA = it },
                onBChanged = { selectedB = it }
            )
            1 -> ModelScreen(
                db = db,
                model = "new",
                padding = padding,
                allGears = allGears,
                selectedA = selectedA,
                selectedB = selectedB,
                onAChanged = { selectedA = it },
                onBChanged = { selectedB = it }
            )        }
    }
}

@Composable
fun ModelScreen(
    db: AppDatabase,
    model: String,
    padding: PaddingValues,
    allGears: List<Int>,
    selectedA: Int?,
    selectedB: Int?,
    onAChanged: (Int) -> Unit,
    onBChanged: (Int) -> Unit
) {
    val dao = db.distanzaDao()
    var distance by remember { mutableStateOf<Double?>(null) }
    var isFallback by remember { mutableStateOf(false) }

    val k = if (model == "old") 30.0 else 29.0

    LaunchedEffect(selectedA, selectedB, model) {
        if (selectedA != null && selectedB != null) {
            val fromDb = dao.find(selectedA, selectedB, model)
            if (fromDb != null) {
                distance = fromDb.distance
                isFallback = false
            } else {
                // Fallback: wzór k * B / A
                distance = k * selectedB / selectedA
                isFallback = true
            }
        } else {
            distance = null
            isFallback = false
        }
    }

    if (allGears.isEmpty()) {
        // ŁADOWANIE – CZEKAJ NA DANE
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .padding(padding)
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        Dropdown(label = "Gear A", items = allGears, selected = selectedA, onSelected = onAChanged)
//        Dropdown(label = "Gear B", items = allGears, selected = selectedB, onSelected = onBChanged)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.gear_a), style = MaterialTheme.typography.labelLarge)
                WheelPicker(
                    items = allGears,
                    selected = selectedA,
                    onSelected = onAChanged,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(stringResource(R.string.gear_b), style = MaterialTheme.typography.labelLarge)
                WheelPicker(
                    items = allGears,
                    selected = selectedB,
                    onSelected = onBChanged,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        distance?.let { dist ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isFallback)
                        MaterialTheme.colorScheme.tertiaryContainer
                    else
                        MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.distance, dist),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    if (isFallback) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.outside_table, k),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dropdown(label: String, items: List<Int>, selected: Int?, onSelected: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
        OutlinedTextField(
            value = selected?.toString() ?: "",
            onValueChange = {},
            label = { Text(label) },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.toString()) },
                    onClick = {
                        onSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}
