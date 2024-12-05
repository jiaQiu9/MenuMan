package com.example.menuman

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.menuman.ui.theme.MenuManTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import com.google.firebase.FirebaseApp

//import android.os.Bundle
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.graphics.Color.Companion.Magenta
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle

import androidx.compose.ui.text.font.FontStyle


object AppColors {
    val Primary = Color(0xFF1E88E5)
    val Secondary = Color(0xFFF4511E)
    val Background = Color(0xFFF5F5F5)
    val TextPrimary = Color.Black
    val TextSecondary = Color.DarkGray
    val ButtonBackground = Color(0xFF1976D2)
    val ButtonText = Color.White
}


@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    signUpUser: (String, String, (String) -> Unit) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Email and Password cannot be empty"
                } else {
                    isLoading = true
                    errorMessage = ""
                    signUpUser(email, password) { error ->
                        if (error.isEmpty()) {
                            onSignupSuccess()
                        } else {
                            errorMessage = error
                        }
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Signing up..." else "Sign Up")
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    loginUser: (String, String, (String) -> Unit) -> Unit
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Email and Password cannot be empty"
                } else {
                    isLoading = true
                    errorMessage = ""
                    loginUser(email, password) { error ->
                        if (error.isEmpty()) {
                            onLoginSuccess()
                        } else {
                            errorMessage = error
                        }
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Logging in..." else "Login")
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }
    }
}


class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private val quoteViewModel: QuoteViewModel by viewModels()
    private val recipeViewModel: RecipeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()


        setContent {
            MenuManTheme {
//                MainScreen(
//                    signUpUser = { email, password, callback ->
//                        signUpWithEmail(email, password, callback)
//                    },
//                    loginUser = { email, password, callback ->
//                        loginWithEmail(email, password, callback)
//                    },
//                    quoteViewModel
//                )
                  //GameScreen(quoteViewModel)
//                RecipeScreen(recipeViewModel)
                //internetCheck(this)
                QuoteScreen(quoteViewModel)
            }
        }
    }

    private fun signUpWithEmail(email: String, password: String, callback: (String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    callback("")
                } else {
                    callback(task.exception?.localizedMessage ?: "Signup failed")
                }
            }
    }

    private fun loginWithEmail(email: String, password: String, callback: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    callback("")
                } else {
                    callback(task.exception?.localizedMessage ?: "Login failed")
                }
            }
    }
}

@Composable
fun QuoteScreen( quoteViewModel: QuoteViewModel){
    // Fetch the quote only when changeLevel > 10
    val changeLevel=11
    val quote = quoteViewModel.quote.value // Get the latest quote value

    val gradientColors = listOf(Color(0xFF15f4ee), Blue, Magenta /*...*/)
    if (changeLevel > 10) {
        LaunchedEffect(changeLevel) {
            quoteViewModel.fetchRandomQuote()  // Fetch a new quote when the condition is met
        }
    }
    Text(
        text = quote,
        modifier = Modifier.padding(bottom = 8.dp),
        fontWeight = FontWeight.Bold,
        style = TextStyle(
            brush = Brush.linearGradient(
                colors = gradientColors
            )
        )
    )
}

@Composable
fun MainScreen(
    signUpUser: (String, String, (String) -> Unit) -> Unit,
    loginUser: (String, String, (String) -> Unit) -> Unit,
    quoteViewModel: QuoteViewModel
) {
    var currentScreen by rememberSaveable { mutableStateOf("home") }

    when (currentScreen) {
        "home" -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(onClick = { currentScreen = "login" }) {
                    Text("Login")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { currentScreen = "signup" }) {
                    Text("Sign Up")
                }
            }
        }

        "login" -> {
            LoginScreen(
                onLoginSuccess = { currentScreen = "game" },
                loginUser = loginUser
            )
        }

        "signup" -> {
            SignupScreen(
                onSignupSuccess = { currentScreen = "login" },
                signUpUser = signUpUser
            )
        }

        "game" -> {
            //Text("Game Screen")
            GameScreen(quoteViewModel)
        }
    }
}

@Composable
fun RecipeScreen(recipeViewModel: RecipeViewModel = RecipeViewModel()) {
    val recipe by recipeViewModel.recipe

    LaunchedEffect(Unit) {
        recipeViewModel.getRandomRecipe()
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = recipe)
    }
}


@Composable
fun GameScreen(quoteViewModel: QuoteViewModel) {
    var changeLevel by rememberSaveable { mutableIntStateOf(0) }
    var currentRound by rememberSaveable { mutableIntStateOf(0) }
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp


    // Fetch the quote only when changeLevel > 10
    if (changeLevel > 10) {
        LaunchedEffect(changeLevel) {
            quoteViewModel.fetchRandomQuote()  // Fetch a new quote when the condition is met
        }
    }

    val quote = quoteViewModel.quote.value // Get the latest quote value

    if (changeLevel <= 10) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .border(BorderStroke(2.dp, SolidColor(AppColors.Secondary)))
                    .fillMaxHeight()
            ) {
                var clicked by remember { mutableStateOf(false) }
                val offset by animateIntOffsetAsState(
                    targetValue = if (clicked) IntOffset(4000, 0) else IntOffset(0, 0),
                    animationSpec = tween(
                        durationMillis = 2000,
                        easing = LinearEasing
                    ),
                    label = "Offset Animation"
                )

                Box {
                    StartButton(onClick = {
                        changeLevel = 1
                        if (currentRound == 0) {
                            currentRound = 1
                        }
                        clicked = !clicked
                    })
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .border(BorderStroke(2.dp, AppColors.Secondary))
                            .offset { offset },
                        contentAlignment = Alignment.TopStart
                    ) {
                        if (currentRound == 0) {
                            Image(
                                painter = painterResource(id = R.drawable.menumantest),
                                contentDescription = null,
                            )
                        }
                    }
                }

                if (isLandscape) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Box(modifier = Modifier.offset { offset }) {
                        Button(onClick = {
                            if (currentRound == 1) {
                                currentRound = 2
                            }
                        }) {
                            Text("Round 1")
                        }
                        if (currentRound == 1) {
                            Image(
                                painter = painterResource(id = R.drawable.menumantest),
                                contentDescription = null,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row {
                    Text("Change level $changeLevel", color = AppColors.TextSecondary)
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row {
                    Text("Current round $currentRound", color = AppColors.TextSecondary)
                }
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    item {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(0.66F),
                            verticalArrangement = Arrangement.spacedBy(8.dp), // Optional spacing between buttons
                            contentPadding = PaddingValues(16.dp) // Optional padding for the list
                        ) {
                            item {
                                Button(
                                    onClick = { /* Handle click for Button 1 */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Button 1")
                                }
                            }
                            item {
                                Button(
                                    onClick = { /* Handle click for Button 1 */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Button 2")
                                }
                            }
                            item {
                                Button(
                                    onClick = { /* Handle click for Button 1 */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Button 2")
                                }
                            }
                            item {
                                Button(
                                    onClick = { /* Handle click for Button 1 */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Button 2")
                                }
                            }
                            item {
                                Button(
                                    onClick = { /* Handle click for Button 1 */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Button 2")
                                }
                            }
                            item {
                                Button(
                                    onClick = { /* Handle click for Button 1 */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Button 3")
                                }
                            }
                            item {
                                Button(
                                    onClick = { /* Handle click for Button 1 */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Button 9")
                                }
                            }
                            item {
                                Button(
                                    onClick = { /* Handle click for Button 1 */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Button 12")
                                }
                            }
                            item {
                                Button(
                                    onClick = { /* Handle click for Button 1 */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Button 12")
                                }
                            }
                            item {
                                Button(
                                    onClick = { /* Handle click for Button 1 */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Button 12")
                                }
                            }
                            item {
                                Button(
                                    onClick = { /* Handle click for Button 1 */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Button 12")
                                }
                            }
                            item {
                                Button(
                                    onClick = { /* Handle click for Button 1 */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Button 12")
                                }
                            }
                            item {
                                Box(modifier = Modifier.offset { offset }) {
                                    Button(onClick = {
                                        if (currentRound == 2) {
                                            currentRound = 3
                                        }
                                    }, modifier = Modifier.fillMaxWidth()) {
                                        Text("Round 2")
                                    }
                                    if (currentRound == 2) {
                                        Image(
                                            painter = painterResource(id = R.drawable.menumantest),
                                            contentDescription = null,
                                            modifier = Modifier.matchParentSize()
                                        )
                                    }
                                }
                            }
                            item {
                                Button(
                                    onClick = { /* Handle click for Button 1 */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Button 5")
                                }
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = {},
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Button 1")
                        }
                    }
                    item {
                        Button(
                            onClick = {},
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text("Button 2")
                        }
                    }
                    item {
                        LazyRow(
                            modifier = Modifier
                                .width(200.dp) // Set explicit width for the inner LazyRow
                                .height(120.dp) // Set explicit height for the inner LazyRow
                                .border(2.dp, Color.Blue) // Add a blue border
                                .padding(8.dp) // Add padding inside the border
                        ) {
                            item {
                                Button(
                                    onClick = {},
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("Button 2")
                                }
                            }
                            item {
                                Button(
                                    onClick = {},
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("Button 2")
                                }
                            }
                            item {
                                Button(
                                    onClick = {},
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("Button 2")
                                }
                            }
                            item {
                                Button(
                                    onClick = {},
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text("Button 2")
                                }
                            }
                            item {
                                Box(modifier = Modifier.offset { offset }) {
                                    Button(onClick = {
                                        if (currentRound == 3) {
                                            currentRound = 4
                                        }
                                    }) {
                                        Text("Round 3")
                                    }
                                    if (currentRound == 3) {
                                        Image(
                                            painter = painterResource(id = R.drawable.menumantest),
                                            contentDescription = null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            val gradientColors = listOf(Color(0xFF15f4ee), Blue, Magenta /*...*/)
            Row {
//                Text("Win, replace with a quote from ZenQuotes", color = AppColors.TextPrimary)
                Text(
                    text = quote,
                    modifier = Modifier.padding(bottom = 8.dp),
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = gradientColors
                        )
                    )
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row {
                Button(
                    onClick = {
                        changeLevel = 0
                        currentRound++
                    },
                    colors = ButtonDefaults.buttonColors(AppColors.ButtonBackground)
                ) {
                    Text("Next Round?", color = AppColors.ButtonText)
                }
            }
        }
    }
}

@Composable
fun IconFromDrawable(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.menumantest),
        contentDescription = "Custom Icon",
        modifier = Modifier.size(20.dp),
    )
}

@Composable
fun internetCheck(context: Context){
    Button(onClick={
        if (checkForInternet(context)) {
            Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show()
        }
    }){

        Text("Internet checker.")
    }

}



// from geeksforgeeks https://www.geeksforgeeks.org/how-to-check-internet-connection-in-kotlin/#
private fun checkForInternet(context: Context): Boolean {

    // register activity with the connectivity manager service
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    // if the android version is equal to M
    // or greater we need to use the
    // NetworkCapabilities to check what type of
    // network has the internet connection
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

        // Returns a Network object corresponding to
        // the currently active default data network.
        val network = connectivityManager.activeNetwork ?: return false

        // Representation of the capabilities of an active network.
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            // Indicates this network uses a Wi-Fi transport,
            // or WiFi has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

            // Indicates this network uses a Cellular transport. or
            // Cellular has network connectivity
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

            // else return false
            else -> false
        }
    } else {
        // if the android version is below M
        @Suppress("DEPRECATION") val networkInfo =
            connectivityManager.activeNetworkInfo ?: return false
        @Suppress("DEPRECATION")
        return networkInfo.isConnected
    }
}

@Composable
fun StartButton(onClick: () -> Unit) {
    Button(
        onClick = onClick, // Pass the onClick lambda to the Button
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Blue, // Customize as needed
            contentColor = Color.White
        )
    ) {
        Text(text = "Start Game (Round 0)")
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
    var clicked by remember { mutableStateOf(false)}

    if(Level == requiredLevel){
        Box{
           Button(onClick = {clicked=!clicked},
               colors = ButtonDefaults.buttonColors(
               containerColor = Color.Magenta, // Background color
               contentColor = Color.White
           ) ){
               Text("Win $clicked")
           }
           Icon(
               Icons.Filled.Check, contentDescription="checkMark", modifier= Modifier
                   .align(Alignment.BottomEnd))
        }
    } else{
        Button(onClick = {}) {
            Text("Filled $Level")
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
    strokeWidth: Dp =5.dp
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



