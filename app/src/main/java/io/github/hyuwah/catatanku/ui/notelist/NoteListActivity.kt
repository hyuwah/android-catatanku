package io.github.hyuwah.catatanku.ui.notelist

import android.app.Activity
import android.app.AlertDialog
import android.app.SearchManager
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import io.github.hyuwah.catatanku.R
import io.github.hyuwah.catatanku.databinding.ActivityNoteListBinding
import io.github.hyuwah.catatanku.domain.model.Note
import io.github.hyuwah.catatanku.ui.about.AboutActivity
import io.github.hyuwah.catatanku.ui.editor.EditorActivity
import io.github.hyuwah.catatanku.utils.adjustInsets
import io.github.hyuwah.catatanku.utils.chrome.CustomTabActivityHelper

@AndroidEntryPoint
class NoteListActivity : AppCompatActivity(), NotesAdapter.NoteItemListener {
    
    private lateinit var binding: ActivityNoteListBinding

    private val viewModel: NoteListViewModel by viewModels()

    private var mBackPressed: Long = 0

    private val notesAdapter by lazy {
        NotesAdapter(noteItemListener = this)
    }

    private val sharedPref by lazy {
        getSharedPreferences(getString(R.string.pref_file_key), MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        adjustInsets()
        setSupportActionBar(binding.toolbar)
        binding.fab.setOnClickListener {
            startActivity(Intent(this, EditorActivity::class.java))
        }
        prepareNoteList()
    }

    override fun onStart() {
        super.onStart()
        invalidateOptionsMenu()
    }

    override fun onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed()
            return
        } else {
            Toast.makeText(this, "Tap back again to exit", Toast.LENGTH_SHORT).show()
        }
        mBackPressed = System.currentTimeMillis()
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_note_list, menu)

        //Associate searchable config with the Searchview
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.action_search).actionView as? SearchView
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                viewModel.search(s)
                return false
            }
        })

        // Check SharedPreferences for Overflow Menu
        val debugToggle = sharedPref.getBoolean(getString(R.string.pref_key_isdebug), false)
        menu.findItem(R.id.action_group_debug).isVisible = debugToggle
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete_all -> {
                viewModel.deleteAll()
                true
            }

            R.id.action_insert -> {
                viewModel.debugInsert()
                true
            }

            R.id.action_gitbook_journal -> {
                openGitbookJournal()
                true
            }

            R.id.action_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
                true
            }

            R.id.action_search -> {
                (item.actionView as SearchView?)?.run {
                    isFocusable = true
                    queryHint = "Text to search"
                    isIconified = false
                    requestFocusFromTouch()
                }
                false
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun adjustInsets() {
        with(binding) {
            fab.adjustInsets(navigationBar = true)
            rvNoteList.adjustInsets(navigationBar = true)
        }
    }

    private fun prepareNoteList() {
        viewModel.notesLiveData.observe(this) { notes ->
            binding.emptyNoteListView.isVisible = notes.isEmpty()
            notesAdapter.submitList(notes.toList())
        }
        binding.rvNoteList.adapter = notesAdapter
    }

    override fun onNoteClicked(note: Note) {
        val intent = Intent(this, EditorActivity::class.java).apply {
            putExtra("ID", note.id)
        }
        startActivity(intent)
    }

    override fun onNoteLongClicked(note: Note) {
        showDeleteConfirmationDialog(
            onDeleteClicked = { viewModel.deleteById(note.id) }
        )
    }

    private fun showDeleteConfirmationDialog(onDeleteClicked:() -> Unit) {
        AlertDialog.Builder(this)
            .setMessage("Selected note(s) will be deleted!")
            .setPositiveButton("Cancel") { dialogInterface: DialogInterface?, _: Int ->
                dialogInterface?.dismiss()
            }
            .setNegativeButton("Delete") { _: DialogInterface?, _: Int ->
                onDeleteClicked.invoke()
            }
            .show()
    }

    private fun openGitbookJournal() {
        val url = "https://hyuwah.gitbooks.io/journal-refactory/content/"
        val uri = Uri.parse(url)
        val customTabIntent = CustomTabsIntent.Builder()
            .setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
            .setShareState(CustomTabsIntent.SHARE_STATE_ON)
            .setShowTitle(true)
            .build()
        CustomTabActivityHelper.openCustomTab(this, customTabIntent, uri) { activity: Activity, uri1: Uri? ->
            activity.startActivity(Intent(Intent.ACTION_VIEW, uri1))
        }
    }

    companion object {
        // Double tap back to exit
        private const val TIME_INTERVAL =
            2000 // # milliseconds, desired time passed between two back presses.
    }
}