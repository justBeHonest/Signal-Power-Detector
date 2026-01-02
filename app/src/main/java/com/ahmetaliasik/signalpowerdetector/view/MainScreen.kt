package com.ahmetaliasik.signalpowerdetector.view
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.ahmetaliasik.signalpowerdetector.core.telephonyManager.SignalStrengthObserver
import com.ahmetaliasik.signalpowerdetector.ui.theme.SignalpowerdetectorTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current

    var value by remember { mutableStateOf(0) }

    DisposableEffect(Unit) {
        val observer = SignalStrengthObserver(context) {
            value = it
        }
        observer.start()

        onDispose {
            observer.stop()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Signal Power Detector") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "%$value")
            VuMeter(value = value, count = 21)
        }
    }
}

fun vuColor(progress: Float): Color {
    return Color(
        red = 1f - progress,
        green = progress,
        blue = 0f
    )
}
@Composable
fun VuMeter(
    value: Int,
    count: Int
) {
    val animatedValue by animateIntAsState(
        targetValue = value,
        animationSpec = tween(120),
        label = "vu"
    )

    val activeBars = (animatedValue / 5f).toInt()

    LazyColumn(
        reverseLayout = true,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(count) { it ->
            val progress = (it + 1) / count.toFloat()
            val activeColor = vuColor(progress)

            val color = if (it <= activeBars) {
                activeColor
            } else {
                activeColor.copy(alpha = 0.25f)
            }

            Card(
                modifier = Modifier
                    .width(100.dp)
                    .height(20.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                colors = CardDefaults.cardColors(containerColor = color)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${it * 5}",
                        fontSize = 10.sp,
                        color = lerp(color, Color.White, 0.7f)
                    )
                }
            }
        }
    }
}


/*
@Composable
fun VuMeter(
    value: Int,
    count : Int
) {
    val activeBars = (value / 5f).toInt()
    LazyColumn(reverseLayout = true, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(count) {
            val progress = (it + 1) / count.toFloat()
            val activeColor = vuColor(progress)
            val color = if (it <= activeBars) {
                activeColor
            } else {
                activeColor.copy(alpha = 0.25f) // passive
            }
                Card(modifier = Modifier.width(100.dp).height(20.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = color
                    )
                )
                { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center){
                    Text((it*5) .toString(), fontSize = 10.sp, color = lerp(color, Color.White, 0.7f))
                }

                }
        }
    }
}*/

@Preview(showBackground = true)
@Composable
fun MainScreenPreview()
{
    SignalpowerdetectorTheme {
        MainScreen()
    }

}