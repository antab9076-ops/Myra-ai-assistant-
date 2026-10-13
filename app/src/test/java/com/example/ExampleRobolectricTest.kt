package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.actions.DeviceActionResult
import com.example.actions.MyraDeviceActions
import com.example.data.model.AssistantState
import com.example.data.model.ChatMessage
import com.example.data.model.FemaleVoiceProfiles
import com.example.data.model.Persona
import com.example.data.repository.MyraRepository
import com.example.service.MyraVoiceForegroundService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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
    assertEquals("MYRA AI", appName)
  }

  @Test
  fun `repository loads initial conversations`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = MyraRepository(context)
    val conversations = repository.getSavedConversations()
    assertTrue(conversations.isNotEmpty())
    assertNotNull(conversations.first().title)
  }

  @Test
  fun `assistant state and personas are well-defined`() {
    assertEquals(AssistantState.IDLE, AssistantState.valueOf("IDLE"))
    assertEquals(AssistantState.LISTENING, AssistantState.valueOf("LISTENING"))
    assertEquals(AssistantState.THINKING, AssistantState.valueOf("THINKING"))
    assertEquals(AssistantState.SPEAKING, AssistantState.valueOf("SPEAKING"))
    assertTrue(Persona.entries.isNotEmpty())
  }

  @Test
  fun `female voice profiles are configured with sweet soft tones`() {
    assertTrue(FemaleVoiceProfiles.isNotEmpty())
    val defaultProfile = FemaleVoiceProfiles.first()
    assertEquals("sweet_soft", defaultProfile.id)
    assertTrue(defaultProfile.pitch > 1.0f) // elevated sweetness/softness
    assertTrue(defaultProfile.sampleText.isNotBlank())
  }

  @Test
  fun `persona instructions include multilingual support for Assamese Bengali Hindi and English`() {
    val prompt = Persona.HELPFUL.promptInstruction
    assertTrue(prompt.contains("Hindi") || prompt.contains("हिन्दी"))
    assertTrue(prompt.contains("Bengali") || prompt.contains("বাংলা"))
    assertTrue(prompt.contains("Assamese") || prompt.contains("অসমীয়া"))
    assertTrue(prompt.contains("English"))
  }

  @Test
  fun `device action parses camera command`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val result = MyraDeviceActions.parseAndExecuteCommand(context, "open camera")
    assertNotNull(result)
    assertTrue(result is DeviceActionResult.Executed || result is DeviceActionResult.Error)
  }

  @Test
  fun `device action parses alarm command`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val result = MyraDeviceActions.parseAndExecuteCommand(context, "set alarm for 7:30 AM")
    assertNotNull(result)
    assertTrue(result is DeviceActionResult.Executed)
  }

  @Test
  fun `device action passes through standard conversation queries`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val result = MyraDeviceActions.parseAndExecuteCommand(context, "Explain quantum physics to me")
    assertNull(result)
  }

  @Test
  fun `foreground service constants and wake word toggle`() {
    assertEquals("myra_voice_channel", MyraVoiceForegroundService.CHANNEL_ID)
    MyraVoiceForegroundService.setWakeWordEnabled(true)
    assertTrue(MyraVoiceForegroundService.isWakeWordEnabled.value)
    MyraVoiceForegroundService.setWakeWordEnabled(false)
    assertTrue(!MyraVoiceForegroundService.isWakeWordEnabled.value)
    MyraVoiceForegroundService.setWakeWordEnabled(true)
  }

  @Test
  fun `device action executes time and stop listening commands`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val timeResult = MyraDeviceActions.parseAndExecuteCommand(context, "tell me the time")
    assertNotNull(timeResult)
    assertTrue(timeResult is DeviceActionResult.Executed)
    assertTrue((timeResult as DeviceActionResult.Executed).message.contains(":"))

    val stopResult = MyraDeviceActions.parseAndExecuteCommand(context, "stop listening")
    assertNotNull(stopResult)
    assertTrue(stopResult is DeviceActionResult.Executed)
    assertTrue((stopResult as DeviceActionResult.Executed).message.contains("Stopping listening"))
  }

  @Test
  fun `firebase manager has correct project configuration`() {
    assertEquals("myra-ai-assistant-58d3f", com.example.data.firebase.FirebaseManager.PROJECT_ID)
    assertEquals("393231947903", com.example.data.firebase.FirebaseManager.MESSAGING_SENDER_ID)
    assertEquals("https://myra-ai-assistant-58d3f-default-rtdb.firebaseio.com", com.example.data.firebase.FirebaseManager.DATABASE_URL)
  }
}
