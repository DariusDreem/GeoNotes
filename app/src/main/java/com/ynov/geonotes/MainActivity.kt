package com.ynov.geonotes

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ynov.geonotes.ui.NotesViewModel
import com.ynov.geonotes.ui.screens.AddNoteScreen
import com.ynov.geonotes.ui.screens.NoteDetailScreen
import com.ynov.geonotes.ui.screens.NoteListScreen
import com.ynov.geonotes.ui.screens.NotesMapScreen
import com.ynov.geonotes.ui.theme.GeoNotesTheme
import org.osmdroid.config.Configuration

private object Routes {
    const val LIST = "notes"
    const val MAP = "map"
    const val ADD = "add"
    const val DETAIL = "note/{id}"
    fun detail(id: Long) = "note/$id"
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Routes.LIST, "Notes", Icons.AutoMirrored.Filled.List),
    Tab(Routes.MAP, "Carte", Icons.Filled.Place),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Configuration osmdroid : cache des tuiles + user agent exigé par OpenStreetMap.
        Configuration.getInstance().apply {
            load(applicationContext, getSharedPreferences("osmdroid", MODE_PRIVATE))
            userAgentValue = packageName
        }
        enableEdgeToEdge()
        setContent {
            GeoNotesTheme {
                GeoNotesApp()
            }
        }
    }
}

@Composable
fun GeoNotesApp(viewModel: NotesViewModel = viewModel(factory = NotesViewModel.Factory)) {
    val navController = rememberNavController()
    val notes by viewModel.notes.collectAsStateWithLifecycle()

    NavHost(navController, startDestination = Routes.LIST) {
        composable(Routes.LIST) {
            MainTabsScaffold(navController, title = "Mes notes") { modifier ->
                NoteListScreen(
                    notes = notes,
                    onNoteClick = { navController.navigate(Routes.detail(it.id)) },
                    modifier = modifier,
                )
            }
        }
        composable(Routes.MAP) {
            MainTabsScaffold(navController, title = "Carte des notes") { modifier ->
                NotesMapScreen(
                    notes = notes,
                    onNoteClick = { navController.navigate(Routes.detail(it.id)) },
                    modifier = modifier,
                )
            }
        }
        composable(Routes.ADD) {
            AddNoteScreen(
                onSaved = { navController.popBackStack() },
                onCancel = { navController.popBackStack() },
            )
        }
        composable(
            Routes.DETAIL,
            arguments = listOf(navArgument("id") { type = NavType.LongType }),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: return@composable
            val noteFlow = remember(id) { viewModel.note(id) }
            val note by noteFlow.collectAsStateWithLifecycle(initialValue = null)
            NoteDetailScreen(
                note = note,
                onBack = { navController.popBackStack() },
                onDelete = {
                    viewModel.deleteNote(it)
                    navController.popBackStack()
                },
            )
        }
    }
}

/** Squelette commun aux onglets Liste / Carte : barre du haut, barre de navigation et bouton d'ajout. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MainTabsScaffold(
    navController: NavHostController,
    title: String,
    content: @Composable (Modifier) -> Unit,
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { CenterAlignedTopAppBar(title = { Text(title) }) },
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            if (currentRoute != tab.route) {
                                navController.navigate(tab.route) {
                                    popUpTo(Routes.LIST) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = null) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Routes.ADD) }) {
                Icon(Icons.Filled.Add, contentDescription = "Ajouter une note")
            }
        },
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}
