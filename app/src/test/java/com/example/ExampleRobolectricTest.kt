package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.MaintenanceScheduleItem
import com.example.data.model.VehicleInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Activa Pilot", appName)
  }

  @Test
  fun `vehicle info defaults are configured for Activa 2014 WB 06 K5136`() {
    val vehicle = VehicleInfo()
    assertEquals("WB 06 K5136", vehicle.registrationNumber)
    assertEquals("Honda Activa (2014)", vehicle.makeModel)
    assertEquals(5.3, vehicle.fuelTankCapacityLitres, 0.01)
    assertEquals(1.3, vehicle.reserveTankCapacityLitres, 0.01)
    assertFalse(vehicle.isLowFuel)
  }

  @Test
  fun `reserve fuel alert triggers when fuel is at or below 1_3L`() {
    val lowFuelVehicle = VehicleInfo(currentEstimatedFuelLitres = 1.1)
    assertTrue(lowFuelVehicle.isLowFuel)

    val normalFuelVehicle = VehicleInfo(currentEstimatedFuelLitres = 3.2)
    assertFalse(normalFuelVehicle.isLowFuel)
  }

  @Test
  fun `maintenance schedule due calculation`() {
    val item = MaintenanceScheduleItem(
      id = "engine_oil",
      title = "Engine Oil Replacement",
      description = "Change 10W-30 oil",
      intervalKm = 3500.0,
      intervalMonths = 4,
      lastDoneOdometerKm = 24000.0
    )

    assertTrue(item.isDue(28000.0))
    assertEquals(-500.0, item.getKmRemaining(28000.0), 0.01)

    assertFalse(item.isDue(27300.0))
    assertTrue(item.isDueSoon(27300.0))
  }
}
