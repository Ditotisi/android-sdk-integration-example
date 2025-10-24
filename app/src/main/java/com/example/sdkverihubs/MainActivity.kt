package com.example.sdkverihubs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sdkverihubs.ui.theme.SDKVerihubsTheme
import com.verihubs.layout.LicenseCallback
import com.verihubs.layout.Verihubs
import com.verihubs.layout.constants.VerihubsEnum
import com.verihubs.layout.VerihubsType.LIVENESS_CODE
import com.verihubs.layout.VerihubsType.NO_LIVENESS_CODE
import com.verihubs.layout.VerihubsType.ANOMALY_CODE
import com.verihubs.layout.VerihubsType.ENCRYPTION_FAILED
import org.json.JSONObject

class MainActivity : AppCompatActivity(),LicenseCallback {
    private lateinit var sdk: Verihubs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sdk = Verihubs(this)
        findViewById<Button>(R.id.startButton).setOnClickListener {
            startLivenessFlow()
        }
    }

    private fun startLivenessFlow() {
        val licenseId = BuildConfig.LICENSE_ID
        val licenseKey = BuildConfig.LICENSE_KEY
        val proxyBaseURL = "https://myproxy.example.com"
        sdk.debugDevModeDetection(false)
        sdk.setProxy("$proxyBaseURL/proxy/face/liveness", "$proxyBaseURL/proxy/license/${licenseId}/check", "$proxyBaseURL/proxy/encryption/generate-key")
        sdk.deepfakeDetection(true, false)
        sdk.setDeepfakeProxy("$proxyBaseURL/proxy/face/deepfake")
        sdk.useVoiceInstruction(false, false)
        sdk.showInstructions(true)
        sdk.licenseCheck(licenseId, licenseKey, this)
    }
    override fun licenseCheck(licenseCondition: VerihubsEnum) {
        println("License result = ${licenseCondition.name}")
        if(licenseCondition === VerihubsEnum.LICENSE_CHECK_SUCCESS) {
            sdk.verifyLiveness()
            println("Start Liveness")
        } else if (licenseCondition === VerihubsEnum.LICENSE_CHECK_INVALID){
            println("License Invalid")
        } else if(licenseCondition === VerihubsEnum.LICENSE_CHECK_UNDEFINED_ERROR) {
            println("Undefined error on license Check")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (data == null) return

        when (requestCode) {
            ANOMALY_CODE -> {
                sdk.dismissSlider()
                val status = data.getStringExtra("status") ?: return
                if (status == VerihubsEnum.PROCESS_FAILED_3.name) {
                    Toast.makeText(this, "Status: $status", Toast.LENGTH_SHORT).show()
                }
            }

            LIVENESS_CODE -> {
                if (resultCode != RESULT_OK) return

                val status = data.getStringExtra("status") ?: return
                val logs = data.getStringExtra("logs")
                val sp = getSharedPreferences("verihubs-storage", MODE_PRIVATE)
                val base64 = sp.getString("base64_image", "") ?: ""
                Log.d("LivenessResult", base64)
                Log.d("WatchHere", logs ?: "Empty Logs")
                when (status) {
                    VerihubsEnum.PROCESS_FAILED_2.name -> {
                        // Deepfake detected
                        val responseApiDeepfake = data.getStringExtra("responseApiDeepfake")
                        // Handle deepfake result here
                        sdk.clean()
                        return
                    }

                    VerihubsEnum.PROCESS_SUCCESS.name,
                    VerihubsEnum.PROCESS_FAILED.name -> {
                        val responseApi = data.getStringExtra("responseApi")
                        val responseApiDeepfake = data.getStringExtra("responseApiDeepfake")
                        // Handle success/failed result here
                        Log.d("WatchHere", "==========Liveness Response Start==========")
                        Log.d("WatchHere", responseApi ?: "no Response API LIVENESS")
                        Log.d("WatchHere", "==========Liveness Response End==========")
                        Log.d("WatchHere", "==========Deepfake Response Start==========")
                        Log.d("WatchHere", responseApiDeepfake ?: "no Response API Deepfake")
                        Log.d("WatchHere", "==========Deepfake Response End==========")
                        sdk.clean()

                        try {
                            if (!responseApi.isNullOrEmpty()) {
                                val jsonResult = JSONObject(responseApi)
                                val liveness = jsonResult.getJSONObject("liveness")
                                val isValid  = liveness.getBoolean("status")

                                Toast.makeText(this, "Liveness $isValid", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this, "Invalid JSON format", Toast.LENGTH_LONG).show()
                            }
                        }
                        catch (e: Exception){
                            Log.e("WatchHere", "Failed Parse Liveness")
                        }
                        try {
                            if (!responseApiDeepfake.isNullOrEmpty()) {
                                val jsonResult = JSONObject(responseApiDeepfake)
                                val deepfake = jsonResult.getBoolean("deepfake")

                                Toast.makeText(this, "Deepfake $deepfake", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(this, "Invalid JSON format", Toast.LENGTH_LONG).show()
                            }
                        }
                        catch (e: Exception){
                            Log.e("WatchHere", "Failed Parse Deepfake Result")
                        }
                    }
                }
            }
        }
    }



}

@Composable
fun MyApp(onButtonClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center // center everything
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Liveness Provider Demo",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 24.sp
            )

            Button(onClick = onButtonClick) {
                Text(text = "Start Liveness")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SDKVerihubsTheme {
    }
}