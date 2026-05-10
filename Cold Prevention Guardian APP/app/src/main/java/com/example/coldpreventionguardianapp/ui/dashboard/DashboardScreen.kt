package com.example.coldpreventionguardianapp.ui.dashboard

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.coldpreventionguardianapp.R
import com.example.coldpreventionguardianapp.data.model.TemperatureRecord
import com.example.coldpreventionguardianapp.viewmodel.DashboardViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen() {
    val viewModel: DashboardViewModel = viewModel()
    val records by viewModel.temperatureRecords.collectAsState()

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val context = LocalContext.current

    // Form state
    var selectedDate by remember { mutableStateOf(dateFormat.format(Date())) }
    var temperatureText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = stringResource(R.string.dashboard_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Latest temperature card
        val latest = records.firstOrNull()
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (latest != null && latest.temperature >= 37.5)
                    MaterialTheme.colorScheme.errorContainer
                else
                    MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.dashboard_latest_temp),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (latest != null) "${latest.temperature}°C" else "--",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (latest != null && latest.temperature >= 37.5)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
                if (latest != null && latest.temperature >= 37.5) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.dashboard_fever_warning),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Line chart
        Text(
            text = stringResource(R.string.dashboard_chart_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TemperatureLineChart(
            records = records.reversed(), // reversed for chronological order in chart
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Input form
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.dashboard_input_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Date picker
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.dashboard_date_label, selectedDate))
                }

                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState()
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    selectedDate = dateFormat.format(Date(millis))
                                }
                                showDatePicker = false
                            }) {
                                Text(stringResource(R.string.dashboard_confirm))
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text(stringResource(R.string.dashboard_cancel))
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Temperature input
                OutlinedTextField(
                    value = temperatureText,
                    onValueChange = { newValue ->
                        // Allow only valid decimal input
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d{1,2}(\\.\\d?)?$"))) {
                            temperatureText = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.dashboard_temp_label)) },
                    placeholder = { Text(stringResource(R.string.dashboard_temp_placeholder)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                // Error message
                errorMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = msg,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Submit button
                Button(
                    onClick = {
                        errorMessage = null
                        val temp = temperatureText.toDoubleOrNull()
                        when {
                            temp == null -> errorMessage = context.getString(R.string.dashboard_error_invalid_temp)
                            temp < 35.0 || temp > 42.0 -> errorMessage = context.getString(R.string.dashboard_error_temp_range)
                            else -> {
                                viewModel.submitTemperature(
                                    TemperatureRecord(
                                        date = selectedDate,
                                        temperature = temp,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                                temperatureText = ""
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.dashboard_submit))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Recent records list
        Text(
            text = stringResource(R.string.dashboard_recent_records),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (records.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.dashboard_no_records),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            records.forEach { record ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (record.temperature >= 37.5)
                            MaterialTheme.colorScheme.errorContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = record.date,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "${record.temperature}°C",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (record.temperature >= 37.5)
                                MaterialTheme.colorScheme.error
                            else
                                MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun TemperatureLineChart(
    records: List<TemperatureRecord>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(true)
                setPinchZoom(false)
                setScaleEnabled(false)
                setDrawGridBackground(false)

                // X Axis
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    textColor = Color.GRAY
                    textSize = 10f
                }

                // Y Axis (Left)
                axisLeft.apply {
                    axisMinimum = 35f
                    axisMaximum = 42f
                    granularity = 1f
                    textColor = Color.GRAY
                    textSize = 10f
                    setDrawGridLines(true)
                }

                // Y Axis (Right) — disable
                axisRight.isEnabled = false

                // Add horizontal reference line at 37.5
                val feverLine = com.github.mikephil.charting.components.LimitLine(
                    37.5f,
                    context.getString(R.string.dashboard_fever_line)
                ).apply {
                    lineColor = Color.RED
                    lineWidth = 1f
                    textColor = Color.RED
                    textSize = 9f
                }
                axisLeft.addLimitLine(feverLine)
            }
        },
        update = { chart ->
            if (records.isEmpty()) {
                chart.clear()
                chart.invalidate()
                return@AndroidView
            }

            val entries = records.mapIndexed { index, record ->
                Entry(index.toFloat(), record.temperature.toFloat())
            }

            val dataSet = LineDataSet(entries, "体温").apply {
                color = Color.BLUE
                valueTextSize = 10f
                valueTextColor = Color.DKGRAY
                lineWidth = 2f
                circleRadius = 5f
                setDrawValues(true)
                setDrawCircleHole(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER

                // Color each point: red if >= 37.5, else blue
                val colors = records.map { record ->
                    if (record.temperature >= 37.5) Color.RED else Color.BLUE
                }
                circleColors = colors
                setCircleColors(colors)
            }

            val lineData = LineData(dataSet)
            chart.data = lineData

            // X Axis labels
            val dateLabels = records.map { it.date }
            chart.xAxis.valueFormatter = IndexAxisValueFormatter(dateLabels)
            chart.xAxis.labelCount = dateLabels.size.coerceAtMost(7)

            chart.invalidate()
        },
        modifier = modifier
    )
}