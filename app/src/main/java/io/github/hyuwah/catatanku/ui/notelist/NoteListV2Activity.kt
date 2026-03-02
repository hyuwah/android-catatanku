package io.github.hyuwah.catatanku.ui.notelist

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import dagger.hilt.android.AndroidEntryPoint
import io.github.hyuwah.catatanku.R
import io.github.hyuwah.catatanku.domain.model.Note
import io.github.hyuwah.catatanku.ui.about.AboutActivity
import io.github.hyuwah.catatanku.ui.editor.EditorActivity
import io.github.hyuwah.catatanku.ui.theme.AppTheme
import io.github.hyuwah.catatanku.utils.chrome.CustomTabActivityHelper
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.net.toUri
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@AndroidEntryPoint
class NoteListV2Activity : AppCompatActivity() {

    private val viewModel: NoteListViewModel by viewModels()

    // Double tap back to exit
    private var mBackPressed: Long = 0
    private val TIME_INTERVAL = 2000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                NoteListScreen(
                    pagingData = viewModel.notesPagingData,
                    onNoteClicked = { note ->
                        val intent = Intent(this, EditorActivity::class.java).apply {
                            putExtra("ID", note.id)
                        }
                        startActivity(intent)
                    },
                    onNoteLongClicked = { note ->
                        showDeleteConfirmationDialog(
                            onDeleteClicked = { viewModel.deleteById(note.id) }
                        )
                    },
                    onAddNoteClicked = {
                        startActivity(Intent(this, EditorActivity::class.java))
                    },
                    onSearchQueryChanged = { query ->
                        viewModel.search(query)
                    },
                    onAboutClicked = {
                        startActivity(Intent(this, AboutActivity::class.java))
                    },
                    onGitbookJournalClicked = {
                        openGitbookJournal()
                    }
                )
            }
        }
        onBackPressedDispatcher.addCallback(this) {
            if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
                finish()
            } else {
                Toast.makeText(
                    this@NoteListV2Activity,
                    "Tap back again to exit",
                    Toast.LENGTH_SHORT
                ).show()
            }
            mBackPressed = System.currentTimeMillis()
        }
    }

    private fun showDeleteConfirmationDialog(onDeleteClicked: () -> Unit) {
        AlertDialog.Builder(this)
            .setMessage("Selected note(s) will be deleted!")
            .setPositiveButton("Cancel") { dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setNegativeButton("Delete") { _, _ ->
                onDeleteClicked.invoke()
            }
            .show()
    }

    private fun openGitbookJournal() {
        val url = "https://hyuwah.gitbooks.io/journal-refactory/content/"
        val uri = url.toUri()
        val customTabIntent = CustomTabsIntent.Builder()
            .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setShareState(CustomTabsIntent.SHARE_STATE_ON)
            .setShowTitle(true)
            .build()
        CustomTabActivityHelper.openCustomTab(
            this,
            customTabIntent,
            uri
        ) { activity: Activity, uri1: Uri? ->
            activity.startActivity(Intent(Intent.ACTION_VIEW, uri1))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(
    pagingData: Flow<PagingData<Note>>,
    onNoteClicked: (Note) -> Unit,
    onNoteLongClicked: (Note) -> Unit,
    onAddNoteClicked: () -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onAboutClicked: () -> Unit,
    onGitbookJournalClicked: () -> Unit
) {
    val notes: LazyPagingItems<Note> = pagingData.collectAsLazyPagingItems()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    val emptyNotesLottie by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.empty_status))
    val emptyNotesLottieState by animateLottieCompositionAsState(
        emptyNotesLottie,
        iterations = LottieConstants.IterateForever,
        isPlaying = true
    )
    Scaffold(
        topBar = {
            if (isSearchActive) {
                // Search Mode
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { query ->
                        searchQuery = query
                        onSearchQueryChanged(query)
                    },
                    onSearch = { /* Search handled on query change */ },
                    active = true,
                    onActiveChange = { active ->
                        isSearchActive = active
                        if (!active) {
                            searchQuery = ""
                            onSearchQueryChanged("")
                        }
                    },
                    placeholder = { Text("Search notes...") },
                    leadingIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                            onSearchQueryChanged("")
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = {
                                searchQuery = ""
                                onSearchQueryChanged("")
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    colors = SearchBarDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        dividerColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Search suggestions could go here
                    if (notes.itemCount == 0) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No notes found")
                        }
                    } else {
                        NoteListSection(
                            notes = notes,
                            onNoteClicked = onNoteClicked,
                            onNoteLongClicked = onNoteLongClicked
                        )
                    }
                }
            } else {
                // Normal Mode
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        IconButton(onClick = onAboutClicked) {
                            Icon(
                                Icons.AutoMirrored.Filled.Help,
                                contentDescription = "About",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNoteClicked,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add note"
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                notes.loadState.refresh is androidx.paging.LoadState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                notes.itemCount == 0 && notes.loadState.refresh is androidx.paging.LoadState.NotLoading -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        LottieAnimation(
                            composition = emptyNotesLottie,
                            progress = { emptyNotesLottieState },
                        )
                        Text(
                            text = "No notes yet. Tap + to create one!",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }

                else -> {
                    NoteListSection(
                        notes = notes,
                        onNoteClicked = onNoteClicked,
                        onNoteLongClicked = onNoteLongClicked
                    )
                }
            }
        }
    }
}

@Composable
fun NoteListSection(
    notes: LazyPagingItems<Note>,
    onNoteClicked: (Note) -> Unit,
    onNoteLongClicked: (Note) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(
            count = notes.itemCount,
            key = { index -> notes[index]?.id ?: index }
        ) { index ->
            notes[index]?.let { note ->
                NoteListItem(
                    note = note,
                    onClick = { onNoteClicked(note) },
                    onLongClick = { onNoteLongClicked(note) },
                    modifier = Modifier.animateItem()
                )
            }
        }

        // Loading more indicator
        if (notes.loadState.append is androidx.paging.LoadState.Loading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun NoteListItem(
    note: Note,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Time
            Text(
                text = formatDate(note.createdAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Title
            Text(
                text = note.title.ifBlank { "Untitled" },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Content excerpt
            Text(
                text = note.contentText,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val date = Date(timestamp)
    val dateFormat = SimpleDateFormat("EE, dd/MM/yy - HH:mm:ss", Locale.getDefault())
    return dateFormat.format(date)
}
