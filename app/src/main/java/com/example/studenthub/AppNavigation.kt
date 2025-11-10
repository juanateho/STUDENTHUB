package com.example.studenthub

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.studenthub.ui.assignments.AssignmentRegistrationScreen
import com.example.studenthub.ui.assignments.AssignmentsScreen
import com.example.studenthub.ui.grades.GradesScreen
import com.example.studenthub.ui.login.LoginScreen
import com.example.studenthub.ui.main.MainScreen
import com.example.studenthub.ui.main.NotificationsDrawer
import com.example.studenthub.ui.main.ProfileDrawer
import com.example.studenthub.ui.profile.ProfileScreen
import com.example.studenthub.ui.reminders.ReminderRegistrationScreen
import com.example.studenthub.ui.stats.StatsScreen
import com.example.studenthub.ui.subjects.SubjectRegistrationScreen
import com.example.studenthub.ui.subjects.SubjectsScreen
import com.example.studenthub.ui.teachers.TeacherRegistrationScreen
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val icon: ImageVector? = null) {
    object Login : Screen("login")
    object Main : Screen("main")
    object SignUp : Screen("signup")
    object Profile : Screen("profile/{userId}") {
        fun createRoute(userId: String): String = "profile/$userId"
    }
    object Assignments : Screen("assignments")
    object AssignmentRegistration : Screen("assignment_registration")
    object AssignmentEdit : Screen("assignment_edit/{assignmentId}") {
        fun createRoute(assignmentId: String): String = "assignment_edit/$assignmentId"
    }
    object SubjectRegistration : Screen("subject_registration")
    object SubjectEdit : Screen("subject_edit/{subjectId}") {
        fun createRoute(subjectId: String): String = "subject_edit/$subjectId"
    }
    object Grades : Screen("grades/{subjectId}") {
        fun createRoute(subjectId: String): String = "grades/$subjectId"
    }
    object TeacherRegistration : Screen("teacher_registration")
    object TeacherEdit : Screen("teacher_edit/{teacherId}") {
        fun createRoute(teacherId: String): String = "teacher_edit/$teacherId"
    }
    object ReminderRegistration : Screen("reminder_registration")
    object Home : Screen("home", Icons.Filled.Home)
    object Subjects : Screen("subjects", Icons.Filled.MenuBook)
    object Calendar : Screen("calendar", Icons.Filled.CalendarToday)
    object Stats : Screen("stats", Icons.Filled.BarChart)
    object List : Screen("list", Icons.Filled.Work)
}

val bottomNavItems = listOf(
    Screen.Calendar,
    Screen.List,
    Screen.Home,
    Screen.Stats,
    Screen.Subjects
)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginClick = { navController.navigate(Screen.Main.route) },
                onSignUpClick = { navController.navigate(Screen.SignUp.route) }
            )
        }
        composable(Screen.Main.route) {
            MainAppScaffold(navController)
        }
        composable(Screen.SignUp.route) {
            ProfileScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.Profile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            ProfileScreen(
                userId = userId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AssignmentRegistration.route) {
            AssignmentRegistrationScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.AssignmentEdit.route,
            arguments = listOf(navArgument("assignmentId") { type = NavType.StringType })
        ) { backStackEntry ->
            val assignmentId = backStackEntry.arguments?.getString("assignmentId")
            AssignmentRegistrationScreen(
                assignmentId = assignmentId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.SubjectRegistration.route) {
            SubjectRegistrationScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.SubjectEdit.route,
            arguments = listOf(navArgument("subjectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId")
            SubjectRegistrationScreen(
                subjectId = subjectId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.TeacherRegistration.route) {
            TeacherRegistrationScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.TeacherEdit.route,
            arguments = listOf(navArgument("teacherId") { type = NavType.StringType })
        ) { backStackEntry ->
            val teacherId = backStackEntry.arguments?.getString("teacherId")
            TeacherRegistrationScreen(
                teacherId = teacherId,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.ReminderRegistration.route) {
            ReminderRegistrationScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.Grades.route,
            arguments = listOf(navArgument("subjectId") { type = NavType.StringType })
        ) { backStackEntry ->
            val subjectId = backStackEntry.arguments?.getString("subjectId")
            GradesScreen(
                subjectId = subjectId ?: "",
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(appNavController: NavController) {
    val profileDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val notificationsDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val bottomNavController = rememberNavController()

    val onLogout: () -> Unit = {
        appNavController.navigate(Screen.Login.route) {
            popUpTo(Screen.Main.route) { inclusive = true }
        }
    }

    val onProfileEdit: (String) -> Unit = { userId ->
        appNavController.navigate(Screen.Profile.createRoute(userId))
    }

    val onAddReminder: () -> Unit = {
        appNavController.navigate(Screen.ReminderRegistration.route)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Student name",
                        modifier = Modifier.clickable { scope.launch { profileDrawerState.open() } })
                },
                navigationIcon = {
                    IconButton(onClick = { scope.launch { profileDrawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle,
                            contentDescription = "User Profile",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { scope.launch { notificationsDrawerState.open() } }) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notifications"
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected =
                            currentDestination?.hierarchy?.any { it.route == screen.route } == true
                        IconButton(onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }) {
                            Icon(
                                screen.icon!!,
                                contentDescription = null,
                                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            ModalNavigationDrawer(
                drawerState = profileDrawerState,
                drawerContent = {
                    ProfileDrawer(
                        onLogout = onLogout,
                        onProfileEdit = { onProfileEdit("user123") }, // Dummy user ID
                        modifier = Modifier.fillMaxWidth(0.8f)
                    )
                },
            ) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    ModalNavigationDrawer(
                        drawerState = notificationsDrawerState,
                        drawerContent = {
                            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                                NotificationsDrawer(
                                    onAddReminder = onAddReminder,
                                    modifier = Modifier.fillMaxWidth(0.8f)
                                )
                            }
                        },
                    ) {
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            NavHost(
                                navController = bottomNavController,
                                startDestination = Screen.Home.route,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                composable(Screen.Home.route) { MainScreen() }
                                composable(Screen.Subjects.route) { SubjectsScreen(navController = appNavController) }
                                composable(Screen.List.route) { AssignmentsScreen(navController = appNavController) }
                                // Add other composables for bottom nav items here
                                composable(Screen.Calendar.route) { Text("Calendar Screen") }
                                composable(Screen.Stats.route) { StatsScreen() }
                            }
                        }
                    }
                }
            }
        }
    }
}
