package io.github.hyuwah.catatanku.notelist

import android.app.Activity
import android.app.AlertDialog
import android.app.SearchManager
import android.content.ContentUris
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AbsListView.MultiChoiceModeListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import io.github.hyuwah.catatanku.R
import io.github.hyuwah.catatanku.about.AboutActivity
import io.github.hyuwah.catatanku.databinding.ActivityNoteListBinding
import io.github.hyuwah.catatanku.editor.EditorActivity
import io.github.hyuwah.catatanku.utils.chrome.CustomTabActivityHelper
import io.github.hyuwah.catatanku.utils.storage.NoteContract

class NoteListActivity : AppCompatActivity(), NoteListContract.View {
    
    private lateinit var binding: ActivityNoteListBinding
    
    // Views
    private var mPresenter: NoteListPresenter? = null
    private var mBackPressed: Long = 0
    
    private val noteCursorAdapter by lazy {
        NoteCursorAdapter(this, null)
    }
    private val sharedPref by lazy {
        getSharedPreferences(getString(R.string.pref_file_key), MODE_PRIVATE)
    }

    /**
     * Lifecycle Override
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        binding.fab.setOnClickListener { _: View? ->
            val intent = Intent(this, EditorActivity::class.java)
            startActivity(intent)
        }
        prepareListView()
        mPresenter = NoteListPresenter(loaderManager, noteCursorAdapter, this)
    }

    override fun onStart() {
        super.onStart()
        invalidateOptionsMenu()
    }

    public override fun onResume() {
        super.onResume()
        // need to kickoff the loadermanager again
        mPresenter?.start()
    }

    override fun onBackPressed() {
        if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed()
            return
        } else {
            Toast.makeText(baseContext, "Tap back again to exit", Toast.LENGTH_SHORT).show()
        }
        mBackPressed = System.currentTimeMillis()
    }

    /**
     * Overflow Menu Related
     */
    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)
        val insertDummyData = menu.findItem(R.id.action_insert)
        insertDummyData.title = "Insert " + NoteListPresenter.dummyDataCount.toString() + " data"
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_note_list, menu)

        //Associate searchable config with the Searchview
        val searchManager = getSystemService(SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.action_search).actionView as SearchView?
        searchView?.setSearchableInfo(searchManager.getSearchableInfo(componentName))
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(s: String): Boolean {
                return false
            }

            override fun onQueryTextChange(s: String): Boolean {
                mPresenter?.searchQuery(s)
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
                mPresenter?.deleteAllNotes()
                true
            }

            R.id.action_insert -> {
                mPresenter?.generateDummyNotes()
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

    /**
     * Activity Methods
     */
    // ListView Related
    private fun prepareListView() {
        binding.lvNoteList.adapter = noteCursorAdapter
        binding.lvNoteList.emptyView = binding.emptyNoteListView

        // Multi select listview
        binding.lvNoteList.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        binding.lvNoteList.setMultiChoiceModeListener(object : MultiChoiceModeListener {
            override fun onItemCheckedStateChanged(
                actionMode: ActionMode,
                i: Int,
                l: Long,
                b: Boolean
            ) {
                val checkedCount = binding.lvNoteList.checkedItemCount
                actionMode.title = "$checkedCount Selected"
                noteCursorAdapter.toggleSelection(i)
            }

            override fun onCreateActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                actionMode.menuInflater.inflate(R.menu.menu_note_list_onselect, menu)
                return true
            }

            override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                return false
            }

            // ActionMode Menu
            override fun onActionItemClicked(actionMode: ActionMode, menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.onselect_select_all -> {
                        noteCursorAdapter.removeSelection()
                        var x = 0
                        while (x < noteCursorAdapter.count) {
                            binding.lvNoteList.setItemChecked(x, true)
                            x++
                        }
                        val checkedCount = binding.lvNoteList.checkedItemCount
                        actionMode.title = "$checkedCount Selected"
                        true
                    }

                    R.id.onselect_delete -> {
                        showDeleteConfirmationDialog { _: DialogInterface?, _: Int ->
                            mPresenter?.deleteSelectedNotes()?.let { rowsDeleted ->

                                Toast.makeText(
                                    this@NoteListActivity,
                                    "Deleted " + rowsDeleted + if (rowsDeleted > 1) " notes" else " note",
                                    Toast.LENGTH_SHORT
                                ).show()

                            }
                            actionMode.finish()
                        }
                        true
                    }

                    else -> false
                }
            }

            override fun onDestroyActionMode(actionMode: ActionMode) {
                noteCursorAdapter.removeSelection()
            }
        })

        // Onclick listView
        binding.lvNoteList.onItemClickListener =
            OnItemClickListener { _: AdapterView<*>?, _: View?, _: Int, id: Long ->
                val intent = Intent(this@NoteListActivity, EditorActivity::class.java)
                val currentNoteUri =
                    ContentUris.withAppendedId(NoteContract.NotesEntry.CONTENT_URI, id)
                intent.data = currentNoteUri
                startActivity(intent)
            }
    }

    override fun showDeleteConfirmationDialog(deleteClickListener: DialogInterface.OnClickListener?) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Selected note(s) will be deleted!")
            .setPositiveButton("Cancel") { dialogInterface: DialogInterface?, _: Int -> dialogInterface?.dismiss() }
            .setNegativeButton("Delete", deleteClickListener).show()
    }

    private fun openGitbookJournal() {
        val url = "https://hyuwah.gitbooks.io/journal-refactory/content/"
        val uri = Uri.parse(url)
        val builder = CustomTabsIntent.Builder()
        builder.setToolbarColor(ContextCompat.getColor(this, R.color.colorPrimary))
        builder.addDefaultShareMenuItem()
        builder.setShowTitle(true)
        val customTabsIntent = builder.build()
        CustomTabActivityHelper.openCustomTab(
            this, customTabsIntent, uri
        ) { activity: Activity, uri1: Uri? ->
            val intent = Intent(Intent.ACTION_VIEW, uri1)
            activity.startActivity(intent)
        }
    }

    override val activityContext: Context
        get() = this

    companion object {
        // Double tap back to exit
        private const val TIME_INTERVAL =
            2000 // # milliseconds, desired time passed between two back presses.
    }
}