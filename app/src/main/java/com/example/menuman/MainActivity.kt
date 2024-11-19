package com.example.menuman

import android.os.Bundle
import androidx.compose.material3.Button
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.menuman.ui.theme.MenuManTheme

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
                                Text("Small Top App Bar")
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
                        MainScreen()
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
    if (changeLevel <=10){


    Row(modifier=Modifier.fillMaxWidth()){
        Column(modifier=Modifier.weight(2f)){
            Row(){
                //buttonChange(2, 3)
                StartButton( onRand= { randNum = (1..numButtons).random() })
            }
            Spacer(modifier=Modifier.height(10.dp))
            Row(){
                //buttonChange(2, 2)
            }
            Spacer(modifier=Modifier.height(10.dp))
            Row(){
                LazyWithButtonRand(numButtons,randNum)
            }

        }



        Column(modifier=Modifier.weight(2f)){
            Row(){
                StartButton(onRand={changeLevel=1})
            }
            Spacer(modifier=Modifier.height(10.dp))
            Row(){
                Text("change level ${changeLevel}")
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
                Button(onClick = {changeLevel=0}){
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



