package com.example.menuman

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Shape


//import android.os.Bundle
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Magenta
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle

import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.sqrt
import kotlin.random.Random


object AppColors {
    val Primary = Color(0xFF1E88E5)
    val Secondary = Color(0xFFF4511E)
    val Background = Color(0xFFF5F5F5)
    val TextPrimary = Color.Black
    val TextSecondary = Color.DarkGray
    val ButtonBackground = Color(0xFF1976D2)
    val ButtonText = Color.White
}
class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private val quoteViewModel: QuoteViewModel by viewModels()
    private val recipeViewModel: RecipeViewModel by viewModels()
    private var showScreen by mutableIntStateOf(0) //change to 0 before deploying


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()


        setContent {
            if (showScreen == 0) {
                MainScreen(
                    signUpUser = { email, password, callback ->
                        signUpWithEmail(email, password, callback)
                    },
                    loginUser = { email, password, callback ->
                        loginWithEmail(email, password, callback)
                    },
                    quoteViewModel,
                    onMainFinished = {showScreen = 1}
                )
            }
            else if(showScreen == 1) {
                IntroScreen(
                    onIntroFinished = {showScreen = 2}
                )
            }
            else if (showScreen == 2) {
                GameScreen(quoteViewModel, false, 0.0F, LocalContext.current)
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



@Composable
fun QuoteScreen(quoteViewModel: QuoteViewModel) {
    // Fetch the quote only when changeLevel > 10
    val changeLevel = 11
    val quote = quoteViewModel.quote.value // Get the latest quote value
    val name = quoteViewModel.name.value
    val category = quoteViewModel.category.value
    val gradientColors = listOf(Color(0xFF15f4ee), Blue, Magenta /*...*/)
    val context = LocalContext.current
    if (changeLevel > 3) {
        LaunchedEffect(changeLevel) {
            quoteViewModel.fetchQuoteFromFirebase()  // Fetch a new quote when the condition is met
            quoteViewModel.fetchQuote(checkForInternet(context))
        }
    }
    Column() {
        Row() {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row() {
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
        Row() {
            Text(
                text = category,
                textAlign = TextAlign.Center
            )
        }
    }


}

@Composable
fun MainScreen(
    signUpUser: (String, String, (String) -> Unit) -> Unit,
    loginUser: (String, String, (String) -> Unit) -> Unit,
    quoteViewModel: QuoteViewModel,
    onMainFinished: () -> Unit
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
                onLoginSuccess = { currentScreen = "intro" },
                loginUser = loginUser
            )
        }

        "signup" -> {
            SignupScreen(
                onSignupSuccess = { currentScreen = "login" },
                signUpUser = signUpUser
            )
        }

        "intro" -> {
            onMainFinished()
        }
    }
}

@Composable
fun RecipeScreen(recipeViewModel: RecipeViewModel = RecipeViewModel()) {
    //val recipe by recipeViewModel.recipe
    val title by recipeViewModel.title
    val instructions by recipeViewModel.instructions
    val ingredients by recipeViewModel.ingredients
    val dbingredients by recipeViewModel.dbingredients
    var currentScreen by rememberSaveable { mutableStateOf("recipe") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        recipeViewModel.fetchRecipe(checkForInternet(context))
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { currentScreen = "game" }) {
            Text("Go back to main game")
        }
        //RecipeTitle(title=title)
        Text(title)
        Spacer(modifier = Modifier.height(10.dp))
        //RecipeInstructions(instructions=instructions)
        Text(instructions)
        Spacer(modifier = Modifier.height(10.dp))
        Text(text = "\n$dbingredients")
        RecipeIngredients(ingredients = ingredients)

    }

    when (currentScreen) {
        "game" -> {
            GameScreen(
                quoteViewModel = QuoteViewModel(),
                motionDone = false,
                lightData = 0.0F,
                LocalContext.current
            )
        }
    }


}

@Composable
fun RecipeTitle(title: String) {
    Text(
        text = "Title: $title"
    )
}

@Composable
fun RecipeInstructions(instructions: String) {
    Text(
        text = "Instructions:\n$instructions"
    )
}

@Composable
fun RecipeIngredients(ingredients: List<String>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Ingredients:")
        ingredients.forEach { ingredient ->
            Text(
                text = "- $ingredient",
                modifier = Modifier.padding(4.dp)
            )
        }
    }

}

@Composable
fun IntroScreen(onIntroFinished: () -> Unit) {
    // Example list of drawable resources
    val imageList = listOf(
        R.drawable.tadpole1,
        R.drawable.tadpole2,
        R.drawable.tadpole3,
        R.drawable.tadpole4,
        R.drawable.frog11,
        R.drawable.frog12,
        R.drawable.frog13,
        R.drawable.frog14,
        R.drawable.frog15,
        R.drawable.frog16,
        R.drawable.frog17,
        R.drawable.whites
    )

    // Current index in the slideshow
    var currentIndex by remember { mutableStateOf(0) }

    val specialIndex = 9  // zero-based: this is the second image in the list

    // Simple handler that goes to the next image (wraps around at the end)
    fun showNextImage() {
        if (currentIndex < 11) {
            currentIndex = (currentIndex + 1)
        }
    }

    val showOverlay = (currentIndex == specialIndex)

    // Display the current image
    Box(
        modifier = Modifier
            .fillMaxSize()
            // If you still want to advance images on click, keep this
            .clickable { showNextImage() }
    ) {
        // 1) The main slideshow image
        Image(
            painter = painterResource(id = imageList[currentIndex]),
            contentDescription = null,
            contentScale = androidx.compose.ui.layout.ContentScale.Companion.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2) Show the special double-overlay only at index=9
        if (currentIndex == 9) {
            SpecialOverlays(
                onBothOverlaysFinished = {
                    // Called when both overlays have faded out.
                    // Optionally advance to next image automatically:
                    showNextImage()
                }
            )
        }
        if (currentIndex == 11) {
            onIntroFinished()
        }
    }
}

@Composable
fun SpecialOverlays(onBothOverlaysFinished: () -> Unit) {
    // Visibility states for each overlay
    var bottomOverlayVisible by remember { mutableStateOf(false) }
    var topOverlayVisible by remember { mutableStateOf(false) }

    // When this composable first appears, show the bottom overlay instantly,
    // then fade in the top overlay.
    LaunchedEffect(Unit) {
        // Appear instantly (bottom overlay)
        bottomOverlayVisible = true
        // Delay a bit before showing top overlay fade-in
        delay(100)
        topOverlayVisible = true
    }

    // Outer Box to control alignment
    Box(modifier = Modifier.fillMaxSize()) {

        // 1) Bottom-center overlay: appears instantly, eventually fades out
        AnimatedVisibility(
            visible = bottomOverlayVisible,
            // Appear instantly (0 ms)
            enter = fadeIn(animationSpec = tween(durationMillis = 0)),
            // Fade out over 1 second
            exit = fadeOut(animationSpec = tween(durationMillis = 1000))
        ) {
            // Put the Image in a Box scope so .align works
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(R.drawable.winbutton),
                    contentDescription = "Bottom Overlay",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(0.4f),  // shrink width so it’s clearly visible
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

    // 2) Top overlay: fades in, then fades out together with the bottom overlay
    AnimatedVisibility(
        visible = topOverlayVisible,
        // Fade in over 1 second
        enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
        // Fade out over 1 second
        exit = fadeOut(animationSpec = tween(durationMillis = 1000))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(R.drawable.tadpolebare),
                contentDescription = "Top Overlay",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.5f),
                contentScale = ContentScale.Fit
            )
        }
    }


    // Fade both out after a few seconds, or on user click:
    LaunchedEffect(Unit) {
        delay(3000)
        bottomOverlayVisible = false
        topOverlayVisible = false
    }

    LaunchedEffect(bottomOverlayVisible, topOverlayVisible) {
        if (!bottomOverlayVisible && !topOverlayVisible) {
            delay(1000)
            // The composable is still in the composition at this moment,
            // so the exit transitions have run. Now call the callback.
            onBothOverlaysFinished()
        }
    }
}


@Composable
fun GameScreen(
    quoteViewModel: QuoteViewModel,
    motionDone: Boolean,
    lightData: Float,
    context: Context
) {
    var changeLevel by rememberSaveable { mutableIntStateOf(0) }
    var currentRound by rememberSaveable { mutableIntStateOf(0) }
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    var currentScreen by rememberSaveable { mutableStateOf("game") }
    var isConnected by rememberSaveable { mutableStateOf(false) }
    val imageAlpha by animateFloatAsState(
        targetValue = if (currentRound == 0) 1f else 0f, // Fade out when currentRound changes
        animationSpec = tween(durationMillis = 1500) // 500ms fade-out animation
    )
    var isClicked by remember { mutableStateOf(false) } // Track whether the button is clicked

    isConnected = checkForInternet(context)

    // Fetch the quote only when changeLevel > 10
//    if (currentRound > 3) {
//        LaunchedEffect(changeLevel) {
//            quoteViewModel.fetchRandomQuote()  // Fetch a new quote when the condition is met
//        }
//    }
//
//    val quote = quoteViewModel.quote.value // Get the latest quote value

//    if (currentRound <= 10 /*change to final round number*/) {
//        Row(modifier = Modifier.fillMaxWidth()) {
//            when (currentScreen) {
//                "recipe" -> {
//                    RecipeScreen(recipeViewModel = RecipeViewModel())
//                }
//            }
//            var clicked by remember { mutableStateOf(false) }
//            val offset by animateIntOffsetAsState(
//                targetValue = if (clicked) IntOffset(4000, 0) else IntOffset(0, 0),
//                animationSpec = tween(
//                    durationMillis = 2000,
//                    easing = LinearEasing
//                ),
//                label = "Offset Animation"
//            )
//
//
//            Box(
//                modifier = Modifier
//                    .padding(16.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                Box(
//                    modifier = Modifier.wrapContentSize()
//                ) {
//                    // OutlinedGameButton in the center
//                    OutlinedGameButton(
//                        text = "Win Game",
//                        modifier = Modifier.padding(16.dp),
//                        defaultBackgroundColor = Color.Gray,
//                        clickedBackgroundColor = if (currentRound == 0) Color.Green else Color.Red,
//                        onClicked = { currentRound++ }, // Increment the round
//                        borderColor = Color.Blue,
//                        borderWidth = 2.dp
//                    )
//                    if (currentRound == 0 || imageAlpha > 0f) { // Ensure visibility during fade
//                        Image(
//                            painter = painterResource(id = R.drawable.menumantest),
//                            contentDescription = null,
//                            modifier = Modifier
//                                .alpha(imageAlpha) // Apply fade-out animation
//                                .align(Alignment.BottomCenter) // Align to the bottom center of the button
//                                .padding(bottom = 8.dp) // Optional: Add spacing between the image and the bottom
//                        )
//                    }
//                }
//            }
//
//            Spacer(modifier = Modifier.height(10.dp))
//            Text("Change level $changeLevel", color = AppColors.TextSecondary)
//            Spacer(modifier = Modifier.height(10.dp))
//            Text("Current round $currentRound", color = AppColors.TextSecondary)
//            Spacer(modifier = Modifier.height(10.dp))
//            LazyRow(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp)
//            ) {
//                item {
//                    LazyColumn(
//                        modifier = Modifier
//                            .fillMaxHeight()
//                            .fillMaxWidth(0.66F),
//                        verticalArrangement = Arrangement.spacedBy(8.dp), // Optional spacing between buttons
//                        contentPadding = PaddingValues(16.dp) // Optional padding for the list
//                    ) {
//                    }
//                }
//                item {
//                    OutlinedGameButton(
//                        text = "Click Me",
//                        onClicked = {},
//                        modifier = Modifier.padding(16.dp),
//                        fontSize = 12.sp,
//                        defaultBackgroundColor = Color.Gray,
//                        clickedBackgroundColor = Color.Red,
//                        borderColor = Blue,
//                        borderWidth = 2.dp
//                    )
//                }
//                item {
//                    FilledGameButton(
//                        text = "Play",
//                        onClicked = {},
//                        modifier = Modifier
//                            .padding(1.dp),
//                        fontSize = 12.sp,
//                        defaultBackgroundColor = Color.Gray,
//                        clickedBackgroundColor = Color.Red,
//                        borderColor = Color.Transparent,
//                        borderWidth = 0.dp,
//                        shape = RoundedCornerShape(12.dp)
//                    )
//                }
//                item {
//                    LazyRow(
//                        modifier = Modifier
//                            .width(200.dp) // Set explicit width for the inner LazyRow
//                            .height(120.dp) // Set explicit height for the inner LazyRow
//                            .border(2.dp, Blue) // Add a blue border
//                            .padding(8.dp) // Add padding inside the border
//                    ) {
//
//
//                    }
//                }
//            }
//        }
//    } else {
//        Column(modifier = Modifier.fillMaxSize()) {
//            val gradientColors = listOf(Color(0xFF15f4ee), Blue, Magenta /*...*/)
//            Row {
////                Text("Win, replace with a quote from ZenQuotes", color = AppColors.TextPrimary)
//                QuoteScreen(quoteViewModel)
//            }
//            Spacer(modifier = Modifier.height(10.dp))
//            Row {
//                Button(
//                    onClick = {
//                        currentRound = 0
//                    },
//                    colors = ButtonDefaults.buttonColors(AppColors.ButtonBackground)
//                ) {
//                    Text("Restart?", color = AppColors.ButtonText)
//                }
//            }
//        }
//    }

    Column(modifier = Modifier.fillMaxSize()) {

        // 1) Top 1/5th (outlined box)
        // Using 'weight(1f)' for top box and 'weight(4f)' for the bottom region
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // top is 1 part out of total 5
                .border(BorderStroke(2.dp, Color.Black)) // outline
        ) {
            val painter = when (currentRound) {
                1 -> painterResource(id = R.drawable.frogbare)
                2 -> painterResource(id = R.drawable.frogbare)
                3 -> painterResource(id = R.drawable.frogbare)
                4 -> painterResource(id = R.drawable.frogbare)
                5 -> painterResource(id = R.drawable.frogbare)
                6 -> painterResource(id = R.drawable.frogbare)
                7 -> painterResource(id = R.drawable.frogbare)
                8 -> painterResource(id = R.drawable.frogbare)
                9 -> painterResource(id = R.drawable.frogbare)
                10 -> painterResource(id = R.drawable.frogbare)
                else -> painterResource(id = R.drawable.frogbare)
            }

            Image(
                painter = painter,
                contentDescription = "Round $currentRound image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // OutlinedGameButton in the center
            OutlinedGameButton(
                text = "Win Game",
                modifier = Modifier.padding(16.dp),
                defaultBackgroundColor = Color.Gray,
                clickedBackgroundColor = if (currentRound == 0) Color.Green else Color.Red,
                onClicked = { currentRound++ }, // Increment the round
                borderColor = Color.Blue,
                borderWidth = 2.dp
            )
        }


        // 2) Bottom 4/5ths (no outline)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(4f) // bottom is 4 parts out of total 5
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier.wrapContentSize()
                ) {

                }
            }
            // Nested LazyColumns and LazyRows that can scroll off-screen
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // Example: 10 "rows" in our LazyColumn
                items(50) { columnIndex ->

                    // Each item is itself a LazyRow, going horizontally off-screen
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Example: 15 items in each row
                        items(50) { rowIndex ->
                            val randomTopStart = remember { Random.nextInt(4, 18).dp }
                            val randomBottomEnd = remember { Random.nextInt(1, 18).dp }
                            val randomWidth = remember { (80..200).random().dp }
                            val randomHeight = remember { (60..120).random().dp }
                            val randomFont = remember { Random.nextInt(9, 16).sp }

                            FilledGameButton(
                                text = "\"Item $columnIndex - $rowIndex\"",
                                onClicked = {},
                                modifier = Modifier
                                    .size(randomWidth, randomHeight)
                                    .padding(vertical = 1.dp),
                                defaultBackgroundColor = Color.LightGray,
                                clickedBackgroundColor = Color.Red,
                                borderColor = Color.Blue,
                                borderWidth = 2.dp,
                                shape = CutCornerShape(
                                    topStart = randomTopStart,
                                    bottomEnd = randomBottomEnd
                                ),
                                fontSize = randomFont
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// Example of a function that returns a filled button
@Composable
fun FilledGameButton(
    text: String,
    onClicked: () -> Unit,
    modifier: Modifier = Modifier,
    defaultBackgroundColor: Color = Color.Green,
    clickedBackgroundColor: Color = Color.Red,
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    fontSize: TextUnit = 14.sp // Add a font size parameter
) {
    // State to track whether the button is clicked
    var isClicked by remember { mutableStateOf(false) }

    // Animate the background color based on the clicked state
    val backgroundColor by animateColorAsState(
        targetValue = if (isClicked) clickedBackgroundColor else defaultBackgroundColor,
        animationSpec = tween(durationMillis = 500), label = "" // 500ms fade duration
    )

    // Reset the clicked state after a delay using LaunchedEffect
    if (isClicked) {
        LaunchedEffect(isClicked) {
            kotlinx.coroutines.delay(500) // Delay before fading back
            isClicked = false
        }
    }

    Box(
        modifier = modifier
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .border(BorderStroke(borderWidth, borderColor), RoundedCornerShape(8.dp))
            .clickable {
                isClicked = true
                onClicked()
            }
            .padding(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            fontSize = fontSize, // Use the smaller font size
            color = Color.Black
        )
    }
}

// Example of a function that returns an outlined button
@Composable
fun OutlinedGameButton(
    text: String,
    onClicked: () -> Unit,
    modifier: Modifier = Modifier,
    defaultBackgroundColor: Color = Color.Transparent,
    clickedBackgroundColor: Color = Color.Red,
    borderColor: Color = Blue,
    borderWidth: Dp = 2.dp,
    fontSize: TextUnit = 14.sp // Add a font size parameter
) {
    // State to track whether the button is clicked
    var isClicked by remember { mutableStateOf(false) }

    // Animate the background color based on the clicked state
    val backgroundColor by animateColorAsState(
        targetValue = if (isClicked) clickedBackgroundColor else defaultBackgroundColor,
        animationSpec = tween(durationMillis = 500), label = "" // 500ms fade duration
    )

    // Reset the clicked state after a delay using LaunchedEffect
    if (isClicked) {
        LaunchedEffect(isClicked) {
            kotlinx.coroutines.delay(500) // Delay before fading back
            isClicked = false
        }
    }

    // Button composable
    Box(
        modifier = modifier
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .border(BorderStroke(borderWidth, borderColor), RoundedCornerShape(8.dp))
            .clickable {
                isClicked = true
                onClicked()
            }
            .padding(16.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.align(Alignment.Center),
            fontSize = fontSize, // Use the smaller font size
            color = Color.Black
        )
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
fun internetCheck(context: Context) {
    Button(onClick = {
        if (checkForInternet(context)) {
            Toast.makeText(context, "Connected", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Disconnected", Toast.LENGTH_SHORT).show()
        }
    }) {

        Text("Internet checker.")
    }

}


// from geeksforgeeks https://www.geeksforgeeks.org/how-to-check-internet-connection-in-kotlin/#
private fun checkForInternet(context: Context): Boolean {

    // register activity with the connectivity manager service
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

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
    strokeWidth: Dp = 5.dp
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
        if (currentTime > 0 && isTimerRunning) {
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
                if (currentTime <= 0L) {
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


class AmbientLight(context: Context) {
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val ambientLight: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    private val _ambientLightData = MutableStateFlow(0f)
    val ambientLightData: MutableStateFlow<Float> = _ambientLightData

    private var lastLightValue: Float = 0f // Store the previous light value


    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val currentLightValue = it.values[0]

                // Calculate the change in light
                val changeValLx = kotlin.math.abs(currentLightValue - lastLightValue)

                // Update StateFlow
                _ambientLightData.value = currentLightValue
                lastLightValue = currentLightValue




                if (changeValLx > 5000) {
                    stopListening()
                    println("Large light intensity change detected: $changeValLx lx. Stopping listener.")
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Handle accuracy changes if needed
        }
    }

    fun startListening() {

        ambientLight?.let {
            sensorManager.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(sensorEventListener)
        println("Sensor listener unregistered.")
    }

}

// for shake motion
class Accelerometer(context: Context) {
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // StateFlow to expose accelerometer data
    private val _accelerometerData = MutableStateFlow(Triple(0f, 0f, 0f))
    val accelerometerData: StateFlow<Triple<Float, Float, Float>> = _accelerometerData

    private var onMotionStopped: (() -> Unit)? = null

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                // Update StateFlow
                _accelerometerData.value = Triple(x, y, z)

                // Compute acceleration magnitude
                val magnitude = sqrt(x * x + y * y + z * z)

                // Stop listening if the magnitude exceeds a threshold (e.g., 15 m/s²)
                if (magnitude > 15) {
                    stopListening()
                    println("Big acceleration detected! Stopping sensor listening.")
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Handle accuracy changes if needed
        }
    }

    fun startListening(onMotionStoppedCallback: () -> Unit) {
        onMotionStopped = onMotionStoppedCallback
        accelerometer?.let {
            sensorManager.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(sensorEventListener)
        onMotionStopped?.invoke() // Invoke the callback when stopping
        println("Sensor listener unregistered.")
    }
}


class spiritLevel(context: Context) {
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // StateFlow to expose accelerometer data
    private val _accelerometerData = MutableStateFlow(Triple(0f, 0f, 0f))
    val accelerometerData: StateFlow<Triple<Float, Float, Float>> = _accelerometerData

    private var onMotionStopped: (() -> Unit)? = null

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            event?.let {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]

                // Update StateFlow
                _accelerometerData.value = Triple(x, y, z)

                // Compute acceleration magnitude
//                val magnitude = sqrt(x * x + y * y + z * z)

                // Stop listening if x and y are 0.0, which is would be having the device place on flat surface
                if ("%.2f".format(x).toFloat() > -0.9 && "%.2f".format(x).toFloat() < 0.9
                    && "%.2f".format(y).toFloat() > -0.9 && "%.2f".format(y).toFloat() < 0.9
                ) {
                    stopListening()
                    println("Big acceleration detected! Stopping sensor listening.")
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Handle accuracy changes if needed
        }
    }

    fun startListening(onMotionStoppedCallback: () -> Unit) {
        onMotionStopped = onMotionStoppedCallback
        accelerometer?.let {
            sensorManager.registerListener(
                sensorEventListener,
                it,
                SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(sensorEventListener)
        onMotionStopped?.invoke() // Invoke the callback when stopping
        println("Sensor listener unregistered.")
    }
}

@Composable
fun spiritLevelScreen() {
    val context = LocalContext.current
    val spiritDetector = remember { spiritLevel(context) }

    val accelerometerData by spiritDetector.accelerometerData.collectAsState()

    var motionDone by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        spiritDetector.startListening {
            motionDone = true
        }
        onDispose {
            spiritDetector.stopListening()
        }
    }

    Column {
        Text(text = "X: ${accelerometerData.first}")
        Text(text = "Y: ${accelerometerData.second}")
        Text(text = "Z: ${accelerometerData.third}")
        Text(text = "MotionDone: $motionDone")
    }
}

@Composable
fun AccelerometerScreen() {
    val context = LocalContext.current
    val accelerometerDetector = remember { Accelerometer(context) }

    val accelerometerData by accelerometerDetector.accelerometerData.collectAsState()

    var motionDone by remember { mutableStateOf(false) }
    DisposableEffect(Unit) {
        accelerometerDetector.startListening {
            motionDone = true
        }
        onDispose {
            accelerometerDetector.stopListening()
        }
    }
    GameScreen(
        quoteViewModel = QuoteViewModel(),
        motionDone = motionDone,
        lightData = 0.0F,
        LocalContext.current
    )

    Column {
        Text(text = "X: ${accelerometerData.first}")
        Text(text = "Y: ${accelerometerData.second}")
        Text(text = "Z: ${accelerometerData.third}")
        Text(text = "MotionDone: $motionDone")
    }
}

@Composable
fun LightScreen() {
    val context = LocalContext.current
    val lightDetector = remember { AmbientLight(context) }
    val lightData by lightDetector.ambientLightData.collectAsState()

    DisposableEffect(Unit) {
        lightDetector.startListening()
        onDispose {
            lightDetector.stopListening()
        }
    }
    Column {
        Text(text = "light lx $lightData")
    }
    GameScreen(
        quoteViewModel = QuoteViewModel(),
        motionDone = false,
        lightData = lightData,
        LocalContext.current
    )

}