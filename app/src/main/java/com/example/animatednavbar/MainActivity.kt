package com.example.animatednavbar

import androidx.compose.ui.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffset
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.animatednavbar.ui.theme.AnimatedNavBarTheme
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnimatedNavBarTheme {
                val navController = rememberNavController()

                val items = listOf(
                    NavBarItem("home", "Home", Icons.Filled.Home),
                    NavBarItem("requests", "Requests", Icons.Filled.MailOutline),
                    NavBarItem("new", "New", Icons.Filled.Add),
                    NavBarItem("profile", "Profile", Icons.Filled.Person),
                )
                Scaffold(
                    bottomBar = {
                        AnimatedNavBar(
                            items = items,
                            navController = navController
                        )
                    }
                ) { paddingValues ->
                    DemoNavGraph(navController = navController, modifier = Modifier.padding(paddingValues))
                }
            }
        }
    }
}

@Composable
fun DemoNavGraph(navController: NavHostController, modifier: Modifier) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") { DemoScreen("Home", Color(0xFFB3E5FC)) }
        composable("requests") { DemoScreen("Requests", Color(0xFFC8E6C9)) }
        composable("new") { DemoScreen("New Request", Color(0xFFFFF9C4)) }
        composable("profile") { DemoScreen("Profile", Color(0xFFFFCCBC)) }
    }
}

@Composable
fun DemoScreen(title: String, backgroundColor: Color) {
    Box(
        modifier = Modifier
            .navigationBarsPadding()
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title +" screen \uD83D\uDC4B", fontSize = 30.sp, color = Color.Black, modifier = Modifier.background(color = Color.White, shape = RoundedCornerShape(20.dp)).padding(20.dp))
    }
}

data class NavBarItem(val route: String, val title: String, val icon: ImageVector)

@Composable
fun AnimatedNavBar(
    items: List<NavBarItem>,
    navController: NavController,
    modifier: Modifier = Modifier,
    barColor: Color = Color.White,
    circleColor: Color = Color(0xFF2981FF),
    selectedIconColor: Color = Color.White,
    unselectedIconColor: Color = Color.Gray,
    circleRadius: Dp = 28.dp,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    var selectedItem by rememberSaveable { mutableIntStateOf(0) }
    var barSize by remember { mutableStateOf(IntSize.Zero) }
    val offsetStep = remember(barSize) { barSize.width.toFloat() / (items.size * 2) }
    val offset = remember(selectedItem, offsetStep) { offsetStep + selectedItem * 2 * offsetStep }

    val circleRadiusPx = LocalDensity.current.run { circleRadius.toPx().toInt() }
    val offsetTransition = updateTransition(offset, label = "offset transition")
    val animationSpec = spring<Float>(dampingRatio = 0.9f, stiffness = Spring.StiffnessVeryLow)

    val cutoutOffset by offsetTransition.animateFloat(
        transitionSpec = { if (initialState == 0f) snap() else animationSpec },
        label = "cutout offset"
    ) { it }

    val circleOffset by offsetTransition.animateIntOffset(
        transitionSpec = { if (initialState == 0f) snap() else spring(animationSpec.dampingRatio, animationSpec.stiffness) },
        label = "circle offset"
    ) {
        IntOffset(it.toInt() - circleRadiusPx, -10)
    }

    val barShape = remember(cutoutOffset) {
        BarShape(offset = cutoutOffset, circleRadius = 45.dp, cornerRadius = 25.dp)
    }

    Box(
        modifier = modifier.height(76.dp)
    ) {
        Circle(
            modifier = Modifier
                .offset { IntOffset(circleOffset.x, circleOffset.y - 5.dp.roundToPx()) }
                .zIndex(1f),
            color = circleColor,
            radius = circleRadius,
            item = items[selectedItem],
            iconColor = selectedIconColor,
        )
        Row(
            modifier = Modifier
                .onPlaced { barSize = it.size }
                .graphicsLayer { shape = barShape; clip = true }
                .fillMaxWidth()
                .background(barColor),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            items.forEachIndexed { index, item ->
                val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                if (isSelected) selectedItem = index

                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },

                    icon = {
                        val alpha by animateFloatAsState(
                            targetValue = if (isSelected) 0f else 1f,
                            label = "icon alpha"
                        )
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            modifier = Modifier.alpha(alpha)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedIconColor,
                        unselectedIconColor = unselectedIconColor,
                        indicatorColor = barColor
                    )
                )
            }
        }
    }
}

@Composable
private fun Circle(
    modifier: Modifier,
    color: Color,
    radius: Dp,
    item: NavBarItem,
    iconColor: Color,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(radius * 2)
            .clip(CircleShape)
            .background(color)
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.title,
            tint = iconColor
        )
    }
}

class BarShape(
    private val offset: Float,
    private val circleRadius: Dp,
    private val cornerRadius: Dp,
    private val circleGap: Dp = 8.dp,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(getPath(size, density))
    }

    private fun getPath(size: Size, density: Density): Path {
        val cutoutCenterX = offset
        val cutoutRadius = density.run { (circleRadius + circleGap).toPx() }
        return Path().apply {
            val cutoutEdgeOffset = cutoutRadius * 1.3f
            val cutoutLeftX = cutoutCenterX - cutoutEdgeOffset
            val cutoutRightX = cutoutCenterX + cutoutEdgeOffset

            moveTo(x = 0F, y = size.height)

            if (cutoutLeftX > 0) {
                arcTo(
                    rect = Rect(0f, 0f, 0f, 0f),
                    startAngleDegrees = 180.0f,
                    sweepAngleDegrees = 90.0f,
                    forceMoveTo = false
                )
            }

            lineTo(cutoutLeftX, 0f)

            cubicTo(
                x1 = cutoutCenterX - cutoutRadius / 2,
                y1 = 0f,
                x2 = cutoutCenterX - cutoutRadius,
                y2 = cutoutRadius,
                x3 = cutoutCenterX,
                y3 = cutoutRadius
            )
            cubicTo(
                x1 = cutoutCenterX + cutoutRadius,
                y1 = cutoutRadius,
                x2 = cutoutCenterX + cutoutRadius / 2,
                y2 = 0f,
                x3 = cutoutRightX,
                y3 = 0f
            )

            if (cutoutRightX < size.width) {
                arcTo(
                    rect = Rect(0f, 0f, size.width, 0f),
                    startAngleDegrees = -90.0f,
                    sweepAngleDegrees = 90.0f,
                    forceMoveTo = false
                )
            }

            lineTo(x = size.width, y = size.height)
            close()
        }
    }
}





