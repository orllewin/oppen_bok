package oppen.oppenbok

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import oppen.oppenbok.databinding.ActivityMainBinding
import oppen.oppenbok.io.db.BookDatabase
import oppen.oppenbok.io.file.FileDatastore
import oppen.oppenbok.io.splicer.ChapterSplicer
import oppen.oppenbok.page_turn.PageCurlAdapter
import oppen.oppenbok.page_turn.PageSurfaceView
import oppen.oppenbok.page_turn.ResourceLoader
import oppen.oppenbok.renderer.PageRenderer
import oppen.oppenbok.settings.SettingsModal
import oppen.oppenbok.settings.SettingsOption


const val OPEN_EPUB = 20

class MainActivity : AppCompatActivity() {

  private var pageSurfaceView: PageSurfaceView? = null
  private var renderer: PageRenderer? = null
  private val model: BookViewModel by viewModels()

  private lateinit var binding: ActivityMainBinding
  private lateinit var prefs: SharedPreferences

  private var pageCount = 0
  private var currentPage = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    binding.model = model
    val view = binding.root
    setContentView(view)

    prefs = PreferenceManager.getDefaultSharedPreferences(this)

    cacheDir.deleteRecursively()

    model.initialise(
            BookDatabase(this).book(),
            FileDatastore.getDefault(this) {
              openEPub()
            },
            ChapterSplicer.getDefault(this, binding.headingTemplate, binding.template),
            object : BookViewModel.BookListener {
              override fun ready(pageCount: Int) = setupRenderer(pageCount)
              override fun storeMetadata(title: String?, creator: String?) {
                prefs.edit()
                        .putString("title", title)
                        .putString("creator", creator)
                        .apply()
              }
            })

    pageSurfaceView = PageSurfaceView(this){
      //Long press:
      val title = prefs.getString("title", null)
      val creator = prefs.getString("creator", null)
      SettingsModal(currentPage, pageCount, title, creator){ option ->
        when(option){
          is SettingsOption.PageOption -> pageSurfaceView?.setCurrentPosition(option.pageNumber)
          is SettingsOption.NewOption -> {
            openNew()
          }
          is SettingsOption.AboutOption -> {
            AboutDialog.show(this)
          }
        }

      }.show(this, supportFragmentManager, "settings_modal")
    }

    binding.bookContainer.addView(pageSurfaceView)

    binding.bookContainer.viewTreeObserver.addOnGlobalLayoutListener {
      RuntimeParams.width = binding.bookContainer.width
      RuntimeParams.height = binding.bookContainer.height
      renderer = PageRenderer(this, binding.bookContainer.width, binding.bookContainer.height)
    }
  }

  private fun openNew(){
    AlertDialog.Builder(this)
      .setTitle(getString(R.string.open_new_dialog_title))
      .setMessage(getString(R.string.open_new_dialog_content))
      .setPositiveButton(getString(R.string.open)){ _, _ ->
        model.clearAndOpen()
      }
      .setNegativeButton(getString(R.string.cancel)){ _, _ -> }
      .show()
  }

  private fun setupRenderer(pageCount: Int) = runOnUiThread{

    this.pageCount = pageCount

    if(pageCount == 0) {
      println("No book....")

      pageSurfaceView?.setPageCurlAdapter(null)

      if(binding.emptyLayout.visibility == View.GONE) {

        //Reset empty state:
        prefs.edit().putInt("page_number", 0).apply()
        binding.emptyLayout.visibility = View.VISIBLE
        binding.emptyLayout.isClickable = true
        binding.label.text = getString(R.string.tap_anywhere)
        binding.bookIcon.pauseAnimation()
        binding.bookIcon.frame = 0
        binding.emptyLayout.visibility = View.VISIBLE
        binding.emptyLayout.alpha = 1f
        binding.bookContainer.visibility = View.GONE

        openEPub()
      }
      return@runOnUiThread
    }

    binding.emptyLayout.animate().setDuration(400).alpha(0f).setListener(object : AnimatorListenerAdapter() {

      override fun onAnimationEnd(animation: Animator?) {
        super.onAnimationEnd(animation)
        binding.emptyLayout.visibility = View.GONE
      }
    }).start()

    binding.bookContainer.visibility = View.VISIBLE

    val pageCurlAdapter = PageCurlAdapter(pageCount, object : ResourceLoader {
      override fun loadBitmap(pageNumber: Int, onBitmap: (Bitmap?) -> Unit) {
        currentPage = pageNumber
        prefs.edit().putInt("page_number", currentPage).apply()

        onBitmap(renderer?.render(model.getPage(pageNumber)))
      }
    })

    pageSurfaceView?.setPageCurlAdapter(pageCurlAdapter)
    pageSurfaceView?.setCurrentPosition(prefs.getInt("page_number", 1) - 1)
  }

  override fun onResume() {
    super.onResume()
    if(pageCount > 0) pageSurfaceView?.onResume()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      window.setDecorFitsSystemWindows(false)
      val controller = window.insetsController
      if (controller != null) {
        controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      }
    }
  }

  override fun onPause() {
    super.onPause()

    if(pageCount > 0) pageSurfaceView?.onPause()
  }

  override fun onTouchEvent(event: MotionEvent): Boolean {
    return pageSurfaceView!!.onPageTouchEvent(event)
  }

  private fun openEPub(){
    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
      addCategory(Intent.CATEGORY_OPENABLE)
      type = "application/epub+zip"
    }

    startActivityForResult(intent, OPEN_EPUB)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)

    if(requestCode == OPEN_EPUB && resultCode == RESULT_OK){
      data?.data?.also { uri ->

        binding.emptyLayout.isClickable = false
        binding.bookIcon.playAnimation()
        binding.label.text = getString(R.string.importing_book)

        GlobalScope.launch {
          model.importBook(uri)
        }
      }
    }
  }
}