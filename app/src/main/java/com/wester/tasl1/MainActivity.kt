package com.wester.tasl1

import android.app.NotificationManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.wester.tasl1.service.FibonacciService
import com.wester.tasl1.ui.theme.Tasl1Theme
import android.provider.Settings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Tasl1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissions(
                            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                            1)
                    }
                    MainScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}



@Composable
fun Fibonacci(){
    var numberFibonacci by remember { mutableStateOf("") }
    val colorScheme = MaterialTheme.colorScheme
    val context = LocalContext.current
    OutlinedTextField(
        value = numberFibonacci,
        onValueChange = { numberFibonacci = it },
        label = { Text(text = "Число", color = colorScheme.onPrimaryContainer) },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 16.dp),
        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 16.sp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )

    Row(modifier = Modifier.fillMaxWidth()
        .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
        Button(onClick = {
            val number = numberFibonacci.toIntOrNull() ?: 0
            if (number > 0) {
                val intent = Intent(context, FibonacciService::class.java).apply {
                    action = FibonacciService.ACTION_START
                    putExtra("number", number)
                }
                context.startForegroundService(intent)
            }
        }, ) {
            Text(text = "Посчитать Фибоначчи")
        }
        Button(onClick = {
            val intent = Intent(context, FibonacciService::class.java).apply {
                action = FibonacciService.ACTION_STOP
            }
            context.startService(intent)
        }) {
            Text(text = "Стоп")
        }
    }
    Row(modifier = Modifier.fillMaxWidth()
        .padding(16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ){
        Text(text= FibonacciService.RESULT.value, fontSize = 20.sp)
    }
}

@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(8.dp, 16.dp, 8.dp, 8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "",
            modifier = modifier
        )
        Fibonacci()
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Tasl1Theme {
        MainScreen()
    }
}