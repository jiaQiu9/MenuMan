package com.example.menuman

import android.content.Context
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
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
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Magenta
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.navigation.NavController
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private val quoteViewModel: QuoteViewModel by viewModels()
    private val recipeViewModel: RecipeViewModel by viewModels()
    private var showScreen by mutableIntStateOf(2) //change to 0 before deploying


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        auth = FirebaseAuth.getInstance()


        setContent {
            MainScreen(
                signUpUser = { email, password, callback ->
                    signUpWithEmail(email, password, callback)
                },
                loginUser = { email, password, callback ->
                    loginWithEmail(email, password, callback)
                },
                quoteViewModel,

                )
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
fun QuoteScreen(navController: NavController, quoteViewModel: QuoteViewModel) {
    // Fetch the quote only when changeLevel > 10
    val changeLevel = 11
    val quote = quoteViewModel.quote.value // Get the latest quote value
    val name = quoteViewModel.name.value
    val category = quoteViewModel.category.value
    val gradientColors = listOf(Color(0xFF15f4ee), Blue, Magenta)
    val context = LocalContext.current
    val isConnected by remember {
        connectivityFlow(context)
    }.collectAsState(initial = false)

    LaunchedEffect(changeLevel, isConnected) {
        quoteViewModel.fetchQuoteFromFirebase()
        if (isConnected) {
            quoteViewModel.fetchQuote(true)
        }
    }
    Column {
        Row {
            Text(
                text = name,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 4.dp, end = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row {
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
            Text(
                text = category,
                textAlign = TextAlign.Center
            )
        }
        Button(onClick = {
            navController.navigate("introScreen")
        })
        {
            Text("Replay Game")
        }
    }
}

@Composable
fun MainScreen(
    signUpUser: (String, String, (String) -> Unit) -> Unit,
    loginUser: (String, String, (String) -> Unit) -> Unit,
    quoteViewModel: QuoteViewModel,
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "homeScreen") {
        composable("gameScreen") {
            GameScreen(navController)
        }
        composable("quoteScreen") {
            QuoteScreen(navController, quoteViewModel)
        }
        composable("introScreen") {
            IntroScreen(navController)
        }
        composable("homeScreen") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("Menu Man",
                    fontSize = 50.sp)
                Spacer(modifier = Modifier.height(30.dp))
                Button(onClick = {
                    navController.navigate("loginScreen")
                }) {
                    Text("Login")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { navController.navigate("signupScreen") }) {
                    Text("Sign Up")
                }

            }
        }
        composable("loginScreen") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("introScreen") },
                loginUser = loginUser,
            )
            Button(onClick = { navController.navigate("homeScreen") }) {
                Text("Back")
            }
        }
        composable("signupScreen") {
            SignupScreen(
                onSignupSuccess = { navController.navigate("loginScreen") },
                signUpUser = signUpUser
            )
        }
        composable("recipeScreen") {
            RecipeScreen(navController, RecipeViewModel())
        }
    }
}

@Composable
fun RecipeScreen(
    navController: NavController,
    recipeViewModel: RecipeViewModel = RecipeViewModel()
) {
    //val recipe by recipeViewModel.recipe
    val title by rememberSaveable { recipeViewModel.title }
    val instructions by rememberSaveable { recipeViewModel.instructions }
    val ingredients by rememberSaveable { recipeViewModel.ingredients }
    val dbingredients by rememberSaveable { recipeViewModel.dbingredients }
    val context = LocalContext.current


    LaunchedEffect(Unit) {
        recipeViewModel.fetchRecipe(true)
    }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.weight(5f)) {
            LazyColumn {
                item {
                    //RecipeTitle
                    Text(title)
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                //RecipeInstructions
                item {
                    Text(instructions)
                }
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                }
                item {
                    Text(text = "\n$dbingredients")
                }
                item {
                    RecipeIngredients(ingredients = ingredients)
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(.5f)
                .border(BorderStroke(2.dp, Color.Black)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp) // Set a fixed height for the row to ensure rectangles
            ) {
                // Button 1
                Box(
                    modifier = Modifier
                        .weight(1f) // Each Box fills half the row width
                        .fillMaxHeight() // Fills the Row height
                        .background(Color.White) // Example color
                        .border(BorderStroke(2.dp, Color.Black))
                        .clickable { navController.navigate("gameScreen") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Main Menus", color = Color.Black)
                }

                // Button 2
                Box(
                    modifier = Modifier
                        .weight(1f) // Each Box fills half the row width
                        .fillMaxHeight() // Fills the Row height
                        .background(Color.LightGray) // Example color
                        .border(BorderStroke(2.dp, Color.Black))
                        .clickable { navController.navigate("recipeScreen") },

                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Recipe Book", color = Color.Black)
                }
            }
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
fun IntroScreen(navController: NavController) {
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
        R.drawable.frog17
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
            .clickable {
                if (currentIndex != 9 && currentIndex != 11) {
                    showNextImage()
                }
                if (currentIndex == 11) {
                    navController.navigate("gameScreen")
                }
            }
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
fun GameScreen(navController: NavController) {
    var changeLevel by rememberSaveable { mutableIntStateOf(0) }
    var currentRound by rememberSaveable { mutableIntStateOf(0) }
    val configuration = LocalConfiguration.current
    val orientation = configuration.orientation
    val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    var currentScreen by rememberSaveable { mutableStateOf("game") }
    val imageAlpha by animateFloatAsState(
        targetValue = if (currentRound == 0) 1f else 0f, // Fade out when currentRound changes
        animationSpec = tween(durationMillis = 1500) // 500ms fade-out animation
    )
    var isClicked by remember { mutableStateOf(false) } // Track whether the button is clicked
    val context = LocalContext.current
    val lightDetector = remember { AmbientLight(context) }
    val lightData by lightDetector.ambientLightData.collectAsState()

    DisposableEffect(Unit) {
        lightDetector.startListening()
        onDispose {
            lightDetector.stopListening()
        }
    }
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

    val isConnected by remember {
        connectivityFlow(context)
    }.collectAsState(initial = false)


    // Create your orientation detector once
    val orientation2 = remember { PhoneOrientation(context) }

    // Start and stop listening in a lifecycle-aware manner
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        orientation2.startListening()
        onDispose {
            orientation2.stopListening()
        }
    }

    // Collect the pitch angle from StateFlow
    val pitchDegrees by orientation2.pitch.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        // 1) Top 1/5th (outlined box)
        // Using 'weight(1f)' for top box and 'weight(4f)' for the bottom region
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // top is 1 part out of total 5
                .border(BorderStroke(2.dp, Color.Black)),
        ) {
            val painter = painterResource(id = R.drawable.frogbare)

            val hintText = when (currentRound) {
                0 -> "Where did he go? He must be hiding in one of these buttons..."
                1 -> "His favorite spaceball team just won 43-32. That's probably why he's so energetic"
                2 -> "He comes to earth sometimes to look at the beautiful landscapes"
                3 -> "I can't find him... it's too dark in here"
                4 -> "He's always wanted to feel an earthquake. Spacequakes aren't a thing."
                5 -> "He's tuckered out, let him lie down for a minute."
                6 -> "He still can't sleep with all these internet notifications."
                7 -> "My neck is tingling... it feels like someone is behind me. Nah must be the wind. (Also turn your internet back on)"
                else -> "Where is that lil guy?"
            }
            var offsetX by remember { mutableFloatStateOf(0f) }
            var offsetY by remember { mutableFloatStateOf(0f) }




            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxSize()
            ) {

                Box {
                    if (currentRound == 7 && isConnected) {
                        Image(
                            painter = painterResource(id = R.drawable.tadpolebare),
                            contentDescription = null,
                            modifier = Modifier
                                .clickable {
                                    navController.navigate("quoteScreen")
                                }
                        )
                    }
                    Image(
                        painter = painter,
                        contentDescription = "Round $currentRound image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxHeight()
                            // Apply offset based on offsetX, offsetY
                            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                            // Capture drag gestures and update offsets
                            .pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    // consume the touch event so it doesn't propagate
                                    change.consume()
                                    // Update offsets
                                    offsetX += dragAmount.x
                                    offsetY += dragAmount.y
                                }
                            }
                    )
                }
                // OutlinedGameButton in the center
                Text(
                    text = hintText,
                    color = Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

            }
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
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(count = 50) { columnIndex ->

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(count = 50) { rowIndex ->
                            fun randomLightColor(): Color {
                                val r = Random.nextFloat() * 0.5f + 0.5f  // 0.5 to 1.0
                                val g = Random.nextFloat() * 0.5f + 0.5f
                                val b = Random.nextFloat() * 0.5f + 0.5f
                                return Color(r, g, b)
                            }

                            fun randomDarkColor(): Color {
                                val r = Random.nextFloat() * 0.5f  // 0.5 to 1.0
                                val g = Random.nextFloat() * 0.5f
                                val b = Random.nextFloat() * 0.5f
                                return Color(r, g, b)
                            }

                            val lightColors = remember {
                                List(30) { randomLightColor() }
                            }
                            val darkColors = remember {
                                List(30) { randomDarkColor() }
                            }

                            val randomTopStart =
                                remember { Random.nextInt(from = 4, until = 18).dp }
                            val randomBottomEnd =
                                remember { Random.nextInt(from = 1, until = 18).dp }
                            val randomWidth = remember { (80..200).random().dp }
                            val randomHeight = remember { (60..120).random().dp }
                            val randomFont =
                                remember { Random.nextInt(from = 9, until = 16).sp }
                            val shouldShowImage1 =
                                (columnIndex == 25 && rowIndex == 0 && currentRound == 0 && !isLandscape)
                            val shouldShowImage2 =
                                (columnIndex == 43 && rowIndex == 32 && currentRound == 1 && !isLandscape)
                            val shouldShowImage3 =
                                (columnIndex == 5 && rowIndex == 2 && currentRound == 2 && isLandscape)
                            val shouldShowImage4 =
                                (columnIndex == 0 && rowIndex == 1 && currentRound == 3 && lightData > 300)
                            val shouldShowImage5 =
                                (columnIndex == 2 && rowIndex == 0 && currentRound == 4 && motionDone)
                            val shouldShowImage6 =
                                (columnIndex == 1 && rowIndex == 1 && currentRound == 5 && -4 <= pitchDegrees && pitchDegrees <= 4)
                            val shouldShowImage7 =
                                (columnIndex == 3 && rowIndex == 0 && currentRound == 6 && !isConnected)

                            // If condition is met, load an actual image painter; else pass null
                            val painter =
                                if (shouldShowImage1 || shouldShowImage2 || shouldShowImage3 || shouldShowImage4 || shouldShowImage5 || shouldShowImage6 || shouldShowImage7) {
                                    painterResource(R.drawable.tadpolebare)
                                } else null

                            FilledGameButton(
                                text = "Item $columnIndex - $rowIndex",
                                columnIndex = columnIndex,
                                rowIndex = rowIndex,
                                onClicked = {
                                    if (shouldShowImage1 || shouldShowImage2 || shouldShowImage3 || shouldShowImage4 || shouldShowImage5 || shouldShowImage6 || shouldShowImage7) {
                                        currentRound++
                                    }
                                },
                                modifier = Modifier
                                    .size(randomWidth, randomHeight)
                                    .padding(vertical = 1.dp),
                                currentRound = currentRound,
                                imagePainter = painter,
                                defaultBackgroundColor = lightColors.random(),
                                borderColor = darkColors.random(),
                                borderWidth = 2.dp,
                                shape = RoundedCornerShape(
                                    topStart = randomTopStart,
                                    topEnd = randomBottomEnd,
                                    bottomStart = randomTopStart,
                                    bottomEnd = randomBottomEnd
                                ),
                                fontSize = randomFont,
                                onCurrentRoundChanged = { currentRound++ },
                                shouldShowImage1 = shouldShowImage1,
                                shouldShowImage2 = shouldShowImage2,
                                shouldShowImage3 = shouldShowImage3,
                                shouldShowImage4 = shouldShowImage4,
                                shouldShowImage5 = shouldShowImage5,
                                shouldShowImage6 = shouldShowImage6,
                                shouldShowImage7 = shouldShowImage7

                            )
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(.5f)
                .border(BorderStroke(2.dp, Color.Black)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp) // Set a fixed height for the row to ensure rectangles
            ) {
                // Button 1
                Box(
                    modifier = Modifier
                        .weight(1f) // Each Box fills half the row width
                        .fillMaxHeight() // Fills the Row height
                        .background(Color.LightGray) // Example color
                        .border(BorderStroke(2.dp, Color.Black))
                        .clickable { navController.navigate("gameScreen") },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Main Menus", color = Color.Black)
                }

                // Button 2
                Box(
                    modifier = Modifier
                        .weight(1f) // Each Box fills half the row width
                        .fillMaxHeight() // Fills the Row height
                        .background(Color.White) // Example color
                        .border(BorderStroke(2.dp, Color.Black))
                        .clickable { navController.navigate("recipeScreen") },

                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Recipe Book", color = Color.Black)
                }
            }
        }
    }
}


@Composable
fun FilledGameButton(
    text: String,
    onClicked: () -> Unit,
    // Pass in the currentRound condition
    currentRound: Int,
    // Optional image overlay
    onCurrentRoundChanged: (Int) -> Unit,
    imagePainter: Painter? = null,
    columnIndex: Int,
    rowIndex: Int,
    modifier: Modifier = Modifier,
    defaultBackgroundColor: Color = Color.LightGray,      // e.g., a neutral default
    borderColor: Color = Color.Transparent,
    borderWidth: Dp = 0.dp,
    shape: Shape = RoundedCornerShape(12.dp),
    fontSize: TextUnit = 14.sp,
    animationDurationMs: Int = 500,
    shouldShowImage1: Boolean,
    shouldShowImage2: Boolean,
    shouldShowImage3: Boolean,
    shouldShowImage4: Boolean,
    shouldShowImage5: Boolean,
    shouldShowImage6: Boolean,
    shouldShowImage7: Boolean

) {
    // Track whether the button was clicked
    var isClicked by remember { mutableStateOf(false) }
    var flashGreen by remember { mutableStateOf(false) }

    // Determine the target color based on `isClicked` and `currentRound`
    val targetColor = when {
        flashGreen -> Color.Green
        isClicked -> Color.Red
        else -> defaultBackgroundColor
    }

    // Animate the background color to the target color
    val backgroundColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(500) // 500 ms fade
    )

    if (flashGreen) {
        LaunchedEffect(Unit) {
            delay(500)  // Wait the flash duration
            flashGreen = false
            onCurrentRoundChanged(currentRound + 1)  // increment AFTER the flash
        }
    }

    // Auto-reset the clicked state after the animation delay
    if (isClicked) {
        LaunchedEffect(isClicked) {
            delay(animationDurationMs.toLong())
            isClicked = false
        }
    }

    Box(
        modifier = modifier
            .background(backgroundColor, shape)  // Apply the animated color + shape
            .border(BorderStroke(borderWidth, borderColor), shape)
            .clickable {
                isClicked = true
                if (shouldShowImage1 || shouldShowImage2 || shouldShowImage3 || shouldShowImage4 || shouldShowImage5 || shouldShowImage6 || shouldShowImage7) {
                    flashGreen = true
                }
            }
            .clip(shape)  // forcibly clip the contents to the shape
            .background(backgroundColor)
            .padding(1.dp),
        contentAlignment = Alignment.Center
    ) {
        // Optional image overlay (below text, above background)
        imagePainter?.let { painter ->
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .matchParentSize()
            )
        }

        // Centered text on top
        Text(
            text = text,
            fontSize = fontSize,
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


fun connectivityFlow(context: Context) = callbackFlow<Boolean> {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            trySend(true)  // Network became available
        }

        override fun onLost(network: Network) {
            trySend(false) // Network was lost
        }
    }

    // Register for network callbacks
    val request = NetworkRequest.Builder().build()
    connectivityManager.registerNetworkCallback(request, networkCallback)

    // Clean up callback when the flow collector stops collecting
    awaitClose {
        connectivityManager.unregisterNetworkCallback(networkCallback)
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
    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

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
    private val accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

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

class PhoneOrientation(context: Context) : SensorEventListener {
    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private val _pitch = MutableStateFlow(0f)   // pitch in degrees
    val pitch: StateFlow<Float> get() = _pitch

    fun startListening() {
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            // Convert rotation-vector to a 4x4 matrix
            val rotationMatrix = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)

            // Compute orientation angles: [azimuth (Z), pitch (X), roll (Y)]
            val orientationAngles = FloatArray(3)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            // orientationAngles[1] = pitch in radians
            val pitchDegrees = Math.toDegrees(orientationAngles[1].toDouble()).toFloat()
            _pitch.value = pitchDegrees
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
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
}