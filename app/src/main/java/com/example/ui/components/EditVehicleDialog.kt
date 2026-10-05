package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VehicleInfo
import com.example.ui.theme.ActivaNeonCyan
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun EditVehicleDialog(
    vehicle: VehicleInfo,
    onDismiss: () -> Unit,
    onConfirm: (VehicleInfo) -> Unit
) {
    var regNumber by remember { mutableStateOf(vehicle.registrationNumber) }
    var modelName by remember { mutableStateOf(vehicle.makeModel) }
    var odoText by remember { mutableStateOf(vehicle.currentOdometerKm.toInt().toString()) }
    var mileageText by remember { mutableStateOf(vehicle.baseMileageKmPerL.toString()) }
    var petrolPriceText by remember { mutableStateOf(vehicle.petrolPricePerLitre.toString()) }
    var fuelInTankText by remember { mutableStateOf(String.format("%.2f", vehicle.currentEstimatedFuelLitres)) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CockpitBackground,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = null,
                    tint = ActivaNeonCyan
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Scooter Profile Settings",
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
                OutlinedTextField(
                    value = regNumber,
                    onValueChange = { regNumber = it },
                    label = { Text("Registration Number") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ActivaNeonCyan,
                        unfocusedBorderColor = CockpitCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = modelName,
                    onValueChange = { modelName = it },
                    label = { Text("Model & Year") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ActivaNeonCyan,
                        unfocusedBorderColor = CockpitCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

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
                            focusedBorderColor = ActivaNeonCyan,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = mileageText,
                        onValueChange = { mileageText = it },
                        label = { Text("Base km/L") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ActivaNeonCyan,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = petrolPriceText,
                        onValueChange = { petrolPriceText = it },
                        label = { Text("Petrol (₹/L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ActivaNeonCyan,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = fuelInTankText,
                        onValueChange = { fuelInTankText = it },
                        label = { Text("Fuel Now (L)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ActivaNeonCyan,
                            unfocusedBorderColor = CockpitCardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = vehicle.copy(
                        registrationNumber = regNumber,
                        makeModel = modelName,
                        currentOdometerKm = odoText.toDoubleOrNull() ?: vehicle.currentOdometerKm,
                        baseMileageKmPerL = mileageText.toDoubleOrNull() ?: vehicle.baseMileageKmPerL,
                        petrolPricePerLitre = petrolPriceText.toDoubleOrNull() ?: vehicle.petrolPricePerLitre,
                        currentEstimatedFuelLitres = (fuelInTankText.toDoubleOrNull() ?: vehicle.currentEstimatedFuelLitres)
                            .coerceIn(0.0, vehicle.fuelTankCapacityLitres)
                    )
                    onConfirm(updated)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ActivaNeonCyan,
                    contentColor = CockpitBackground
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_vehicle_settings_button")
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
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
