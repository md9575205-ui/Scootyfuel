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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
fun AddServiceDialog(
    initialOdometerKm: Double,
    preselectedCategory: String = "Engine Oil (10W-30)",
    onDismiss: () -> Unit,
    onConfirm: (
        odometerKm: Double,
        category: String,
        cost: Double,
        garageName: String,
        parts: String,
        notes: String
    ) -> Unit
) {
    var odoText by remember { mutableStateOf(initialOdometerKm.toInt().toString()) }
    var selectedCategory by remember { mutableStateOf(preselectedCategory) }
    var costText by remember { mutableStateOf("450") }
    var garageName by remember { mutableStateOf("Authorized Honda Service Centre") }
    var partsText by remember { mutableStateOf("Honda 10W-30 4T Oil (800ml)") }
    var notes by remember { mutableStateOf("") }

    val categories = listOf(
        "Engine Oil (10W-30)",
        "Air Filter",
        "Spark Plug",
        "Brake Shoes",
        "Drive Belt & CVT",
        "Gear Oil",
        "Battery Check",
        "PUCC (Pollution)",
        "General Service"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CockpitBackground,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Build,
                    contentDescription = null,
                    tint = ActivaOrange,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log Maintenance Service",
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
                    text = "Record maintenance for WB 06 K5136",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Service category picker
                Text(text = "Service Category:", color = TextMuted, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categories.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowItems.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ActivaOrange.copy(alpha = 0.25f) else CockpitCard)
                                        .clickable {
                                            selectedCategory = cat
                                            if (cat.contains("Air Filter")) {
                                                partsText = "Genuine Viscous Air Filter"
                                                costText = "280"
                                            } else if (cat.contains("Brake")) {
                                                partsText = "Front & Rear Brake Shoes"
                                                costText = "550"
                                            } else if (cat.contains("PUCC")) {
                                                partsText = "Emission Test Certificate"
                                                costText = "100"
                                            } else if (cat.contains("Belt")) {
                                                partsText = "CVT V-Belt & Roller Weights"
                                                costText = "750"
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        color = if (isSelected) ActivaOrange else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Odometer & Cost
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = odoText,
                        onValueChange = { odoText = it },
                        label = { Text("Odo (KM)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ActivaOrange,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("service_odo_input")
                    )

                    OutlinedTextField(
                        value = costText,
                        onValueChange = { costText = it },
                        label = { Text("Cost (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ActivaOrange,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("service_cost_input")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Garage
                OutlinedTextField(
                    value = garageName,
                    onValueChange = { garageName = it },
                    label = { Text("Garage / Workshop") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ActivaOrange,
                        unfocusedBorderColor = CockpitCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Parts replaced
                OutlinedTextField(
                    value = partsText,
                    onValueChange = { partsText = it },
                    label = { Text("Parts Replaced / Details") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ActivaOrange,
                        unfocusedBorderColor = CockpitCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Technician Notes (optional)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ActivaOrange,
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
                    val cost = costText.toDoubleOrNull() ?: 0.0
                    onConfirm(odo, selectedCategory, cost, garageName, partsText, notes)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActivaOrange,
                    contentColor = CockpitBackground
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_service_button")
            ) {
                Text("Save Record", fontWeight = FontWeight.Bold)
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
