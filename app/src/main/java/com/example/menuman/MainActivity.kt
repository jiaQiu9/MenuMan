package com.example.menuman

import android.os.Bundle
import androidx.compose.material3.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.menuman.ui.theme.MenuManTheme
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MenuManTheme {
                Scaffold(
                    topBar = {
                        CenterAlignedTopAppBar(

                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor =Color.Gray,
                                titleContentColor = Color.Black,
                            ),
                            title = {
                                Text("MenuMen")
                            }
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
                    Column(
                        modifier=Modifier.padding(innerPadding)
                    ){

                        Row(){
                            MainScreen()
                        }
//                        Row(){
//                            Box(
//                                contentAlignment = Alignment.Center
//                            ) {
//                                // call the function Timer
//                                // and pass the values
//                                // it is defined below.
//                                Timer(
//                                    totalTime = 10L * 1000L,
//                                    handleColor = Color.Green,
//                                    inactiveBarColor = Color.DarkGray,
//                                    activeBarColor = Color(0xFF37B900),
//                                    modifier = Modifier.size(100.dp)
//                                )
//                            }
//                        }
                    }


                }
            }
        }
    }
}
@Composable
fun MainScreen(){
    var randNum by remember{ mutableIntStateOf(0) }
    var numButtons = 10
    var changeLevel by remember{ mutableIntStateOf(0) }
    var currentRound by remember{ mutableIntStateOf(0) }
    if (changeLevel <=10){

    Row(modifier=Modifier.fillMaxWidth()){
//        Column(modifier=Modifier.weight(2f)){
//            Row(){
//                //buttonChange(2, 3)
//                StartButton( onRand= { randNum = (1..numButtons).random() })
//            }
//            Spacer(modifier=Modifier.height(10.dp))
//            Row(){
//                //buttonChange(2, 2)
//            }
//            Spacer(modifier=Modifier.height(10.dp))
//            Row(){
//                LazyWithButtonRand(numButtons,randNum)
//            }
//
//        }

        Column(modifier=Modifier.weight(1f).border(BorderStroke(2.dp, SolidColor(Color.Red))).fillMaxHeight()){
            Row(){
                StartButton(onRand={changeLevel=1})
            }
            Spacer(modifier=Modifier.height(10.dp))
            Row(){
                Text("change level ${changeLevel}")


            }
            Spacer(modifier=Modifier.height(10.dp))
            Row(){
                Text("Current round ${currentRound}")
            }

            Spacer(modifier=Modifier.height(10.dp))
            Row(){
                //LazyWithButtonRand(numButtons, changeLevel)

                LazyButtonFixed(numButtons, changeLevel, ChangeLevel={changeLevel++})
            }
        }
    }} else{
        Column(modifier=Modifier.fillMaxSize()){
            Row(){
                Text("Win, replace with a quotes from ZenQuotes")
            }
            Spacer(modifier=Modifier.height(10.dp))
            Row(){
                Button(onClick = {changeLevel=0; currentRound++}){
                    Text("Next Round?")
                }
            }

        }
    }

}


@Composable
fun StartButton(onRand: () -> Unit){
    Button(onClick = {
        onRand()
    }){
        Text("Start Game")
    }
}


@Composable
fun LazyWithButtonRand(buttonsCount: Int, RandNum: Int){
    val buttonData = List(buttonsCount){index->
        "Button ${index +1}"
    }
    LazyColumn(){
        items(buttonData.size){ index->
            val buttonId = index+1
            buttonChange(buttonId, RandNum)
        }
    }
}

@Composable
fun LazyButtonFixed(buttonsCount: Int, Level:Int, ChangeLevel:()->Unit){
    val buttonData = List(buttonsCount){index->
        "Button ${index +1}"
    }
    LazyColumn(){
        items(buttonData.size){ index->
            val buttonId = index+1
            buttonChangeLevelChange(buttonId, Level, ChangeLevel)
        }
    }
}


@Composable
fun buttonChange(Level: Int, requiredLevel: Int){
    var clicked by remember{ mutableIntStateOf(0) }
    if(Level == requiredLevel){
        Box{
           Button(onClick = {clicked++},
               colors = ButtonDefaults.buttonColors(
               containerColor = Color.Magenta, // Background color
               contentColor = Color.White
           ) ){
               Text("Win ${clicked}")
           }
           Icon(Icons.Filled.Check, contentDescription="checkMark", modifier= Modifier.align(Alignment.BottomEnd))
        }
    } else{
        Button(onClick = {}) {
            Text("Filled ${Level}")
        }
    }
}

@Composable
fun buttonChangeLevelChange(Level: Int, requiredLevel:Int, onFind:()->Unit){
    if(Level == requiredLevel){
        Box{
            Button(onClick = {onFind()},
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Magenta, // Background color
                    contentColor = Color.White
                ) ){
                Text("Win")
            }
            Icon(Icons.Filled.Check, contentDescription="checkMark", modifier= Modifier.align(Alignment.BottomEnd))
        }
    } else{
        Button(onClick = {}) {
            Text("Filled ${Level}")
        }
    }
}

@Composable
fun BoxWithButtonAndIcon() {
    Box(
        modifier = Modifier
            .background(Color.Gray) // Optional background color for the Box
    ) {
        // Button at the center of the Box (or wherever you want)
        Button(
            onClick = { /* Handle button click */ },
            modifier = Modifier.align(Alignment.Center) // Optional: Button at center
        ) {
            Text("Click Me")
        }

        // Icon at the bottom-right corner of the Box
        IconButton(
            onClick = { /* Handle icon click */ },
            modifier = Modifier
                .align(Alignment.BottomEnd)

        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Favorite Icon",
                tint = Color.White // Optional: Color of the icon
            )
        }
    }
}

// create a composable to
// Draw arc and handle
@Composable
fun Timer(

    // total time of the timer
    totalTime: Long,

    // circular handle color
    handleColor: Color,

    // color of inactive bar / progress bar
    inactiveBarColor: Color,

    // color of active bar
    activeBarColor: Color,
    modifier: Modifier = Modifier,

    // set initial value to 1
    initialValue: Float = 1f,
    strokeWidth: Dp=5.dp
) {
    // create variable for
    // size of the composable
    var size by remember {
        mutableStateOf(IntSize.Zero)
    }
    // create variable for value
    var value by remember {
        mutableStateOf(initialValue)
    }
    // create variable for current time
    var currentTime by remember {
        mutableStateOf(totalTime)
    }
    // create variable for isTimerRunning
    var isTimerRunning by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(key1 = currentTime, key2 = isTimerRunning) {
        if(currentTime > 0 && isTimerRunning) {
            delay(100L)
            currentTime -= 100L
            value = currentTime / totalTime.toFloat()
        }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .onSizeChanged {
                size = it
            }
    ) {
        // draw the timer
        Canvas(modifier = modifier) {
            // draw the inactive arc with following parameters
            drawArc(
                color = inactiveBarColor, // assign the color
                startAngle = -215f, // assign the start angle
                sweepAngle = 250f, // arc angles
                useCenter = false, // prevents our arc to connect at te ends
                size = Size(size.width.toFloat(), size.height.toFloat()),

                // to make ends of arc round
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            // draw the active arc with following parameters
            drawArc(
                color = activeBarColor, // assign the color
                startAngle = -215f,  // assign the start angle
                sweepAngle = 250f * value, // reduce the sweep angle
                // with the current value
                useCenter = false, // prevents our arc to connect at te ends
                size = Size(size.width.toFloat(), size.height.toFloat()),

                // to make ends of arc round
                style = Stroke(strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            // calculate the value from arc pointer position
            val center = Offset(size.width / 2f, size.height / 2f)
            val beta = (250f * value + 145f) * (PI / 180f).toFloat()
            val r = size.width / 2f
            val a = cos(beta) * r
            val b = sin(beta) * r
            // draw the circular pointer/ cap
            drawPoints(
                listOf(Offset(center.x + a, center.y + b)),
                pointMode = PointMode.Points,
                color = handleColor,
                strokeWidth = (strokeWidth * 3f).toPx(),
                cap = StrokeCap.Round  // make the pointer round
            )
        }
        // add value of the timer
        Text(
            text = (currentTime / 1000L).toString(),
            fontSize = 44.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        // create button to start or stop the timer
        Button(
            onClick = {
                if(currentTime <= 0L) {
                    currentTime = totalTime
                    isTimerRunning = true
                } else {
                    isTimerRunning = !isTimerRunning
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
            // change button color
            colors =
            ButtonDefaults.buttonColors(
                if (!isTimerRunning || currentTime <= 0L) {
                    Color.Green
                } else {
                    Color.Red
                }
            )

        ) {
            Text(
                // change the text of button based on values
                text = if (isTimerRunning && currentTime >= 0L) "Stop"
                else if (!isTimerRunning && currentTime >= 0L) "Start"
                else "Restart"
            )
        }
    }
}



