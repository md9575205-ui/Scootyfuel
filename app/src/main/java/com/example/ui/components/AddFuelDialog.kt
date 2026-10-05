package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ActivaNeonCyan
import com.example.ui.theme.ActivaOrange
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitCard
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AddFuelDialog(
    initialOdometerKm: Double,
    defaultPricePerLitre: Double = 103.50,
    onDismiss: () -> Unit,
    onConfirm: (
        odometerKm: Double,
        litres: Double,
        pricePerLitre: Double,
        totalCost: Double,
        isFullTank: Boolean,
        stationName: String,
        notes: String
    ) -> Unit
) {
    var odoText by remember { mutableStateOf(initialOdometerKm.toInt().toString()) }
    var litresText by remember { mutableStateOf("4.5") }
    var priceText by remember { mutableStateOf(defaultPricePerLitre.toString()) }
    var totalCostText by remember {
        mutableStateOf(String.format("%.2f", 4.5 * defaultPricePerLitre))
    }
    var isFullTank by remember { mutableStateOf(true) }
    var stationName by remember { mutableStateOf("Indian Oil (IOCL)") }
    var notes by remember { mutableStateOf("") }

    val quickStations = listOf("Indian Oil (IOCL)", "Bharat Petroleum (BPCL)", "Hindustan Petroleum (HP)", "Shell", "Nayara")

    fun recomputeTotal(litresStr: String, priceStr: String) {
        val l = litresStr.toDoubleOrNull() ?: 0.0
        val p = priceStr.toDoubleOrNull() ?: 0.0
        totalCostText = String.format("%.2f", l * p)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CockpitBackground,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = null,
                    tint = ActivaNeonCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log Petrol Refill",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Record fuel for Honda Activa (WB 06 K5136)",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Odometer
                OutlinedTextField(
                    value = odoText,
                    onValueChange = { odoText = it },
                    label = { Text("Odometer Reading (KM)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ActivaNeonCyan,
                        unfocusedBorderColor = CockpitCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fuel_odo_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Litres & Price Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = litresText,
                        onValueChange = {
                            litresText = it
                            recomputeTotal(it, priceText)
                        },
                        label = { Text("Litres (L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ActivaNeonCyan,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("fuel_litres_input")
                    )

                    OutlinedTextField(
                        value = priceText,
                        onValueChange = {
                            priceText = it
                            recomputeTotal(litresText, it)
                        },
                        label = { Text("Price (₹/L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ActivaNeonCyan,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("fuel_price_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Total Cost
                OutlinedTextField(
                    value = totalCostText,
                    onValueChange = { totalCostText = it },
                    label = { Text("Total Cost (₹)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ActivaNeonCyan,
                        unfocusedBorderColor = CockpitCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("fuel_total_cost_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Full tank checkbox
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFullTank = !isFullTank }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isFullTank,
                        onCheckedChange = { isFullTank = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = ActivaNeonCyan,
                            checkmarkColor = CockpitBackground
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Full Tank Filled (Auto resets 5.3L gauge)",
                        color = TextPrimary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Station quick chips
                Text(text = "Fuel Station:", color = TextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickStations.take(3).forEach { st ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (stationName == st) ActivaNeonCyan.copy(alpha = 0.25f) else CockpitCard)
                                .clickable { stationName = st }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = st.split(" ").first(),
                                color = if (stationName == st) ActivaNeonCyan else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    placeholder = { Text("e.g. Speed 97 petrol, highway ride") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ActivaNeonCyan,
                        unfocusedBorderColor = CockpitCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val odo = odoText.toDoubleOrNull() ?: initialOdometerKm
                    val l = litresText.toDoubleOrNull() ?: 4.5
                    val p = priceText.toDoubleOrNull() ?: defaultPricePerLitre
                    val total = totalCostText.toDoubleOrNull() ?: (l * p)
                    onConfirm(odo, l, p, total, isFullTank, stationName, notes)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActivaNeonCyan,
                    contentColor = CockpitBackground
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_fuel_button")
            ) {
                Text("Save Refill", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
