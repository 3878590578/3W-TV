代码:
package com.oklocal.player

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var videoList: RecyclerView
    private lateinit var folderList: RecyclerView
    private lateinit var emptyText: TextView
    private lateinit var countText: TextView
    private lateinit var searchInput: EditText

    private lateinit var videoAdapter: VideoAdapter
    private lateinit var folderAdapter: FolderAdapter

    private var allVideos =
        listOf<VideoItem>()

    private var currentFolder: String? =
        null

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->

            if (granted) {

                loadVideos()

            } else {

                Toast.makeText(
                    this,
                    "需要允许访问视频",
                    Toast.LENGTH_LONG
                ).show()

                showEmpty()
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        setContentView(
            R.layout.activity_main
        )

        videoList =
            findViewById(R.id.videoList)

        folderList =
            findViewById(R.id.folderList)

        emptyText =
            findViewById(R.id.emptyText)

        countText =
            findViewById(R.id.countText)

        searchInput =
            findViewById(R.id.searchInput)

        videoAdapter =
            VideoAdapter(
                onClick = {
                    openPlayer(it)
                },
                onLongClick = {
                    toggleFavorite(it)
                }
            )

        folderAdapter =
            FolderAdapter {
                openFolder(it.name)
            }

        videoList.layoutManager =
            LinearLayoutManager(this)

        videoList.adapter =
            videoAdapter

        folderList.layoutManager =
            LinearLayoutManager(this)

        folderList.adapter =
            folderAdapter

        searchInput.addTextChangedListener(
            SimpleTextWatcher {
                filterVideos(it)
            }
        )

        findViewById<View>(
            R.id.backFolder
        ).setOnClickListener {

            currentFolder = null

            render()
        }

        findViewById<View>(
            R.id.refreshButton
        ).setOnClickListener {

            loadVideos()
        }

        requestPermission()
    }

    override fun onResume() {
        super.onResume()

        if (::videoList.isInitialized &&
            hasPermission()
        ) {

            loadVideos()
        }
    }

    private fun requestPermission() {

        if (hasPermission()) {

            loadVideos()

            return
        }

        val permission =
            if (Build.VERSION.SDK_INT >= 33) {

                Manifest.permission.READ_MEDIA_VIDEO

            } else {

                Manifest.permission.READ_EXTERNAL_STORAGE
            }

        permissionLauncher.launch(
            permission
        )
    }

    private fun hasPermission(): Boolean {

        val permission =
            if (Build.VERSION.SDK_INT >= 33) {

                Manifest.permission.READ_MEDIA_VIDEO

            } else {

                Manifest.permission.READ_EXTERNAL_STORAGE
            }

        return ContextCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun loadVideos() {

        Thread {

            val result =
                VideoRepository.queryVideos(
                    this
                )

            runOnUiThread {

                allVideos = result

                render()
            }

        }.start()
    }

    private fun render() {

        val keyword =
            searchInput.text
                .toString()
                .trim()
                .lowercase(Locale.getDefault())

        var list =
            allVideos

        if (currentFolder != null) {

            list =
                list.filter {
                    it.folder == currentFolder
                }
        }

        if (keyword.isNotEmpty()) {

            list =
                list.filter {
                    it.name
                        .lowercase(Locale.getDefault())
                        .contains(keyword)
                }
        }

        videoAdapter.submitList(list)

        countText.text =
            "视频 ${list.size}"

        val folders =
            allVideos
                .groupBy {
                    it.folder
                }
                .map {
                    FolderItem(
                        it.key,
                        it.value.size
                    )
                }
                .sortedBy {
                    it.name.lowercase()
                }

        folderAdapter.submitList(
            folders
        )

        folderList.visibility =
            if (currentFolder == null &&
                keyword.isEmpty()
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }

        findViewById<View>(
            R.id.backFolder
        ).visibility =
            if (currentFolder != null) {
                View.VISIBLE
            } else {
                View.GONE
            }

        if (list.isEmpty()) {

            emptyText.visibility =
                View.VISIBLE

            videoList.visibility =
                View.GONE

        } else {

            emptyText.visibility =
                View.GONE

            videoList.visibility =
                View.VISIBLE
        }
    }

    private fun filterVideos(
        text: String
    ) {

        render()
    }

    private fun openFolder(
        folder: String
    ) {

        currentFolder = folder

        render()
    }

    private fun openPlayer(
        item: VideoItem
    ) {

        val intent =
            Intent(
                this,
                PlayerActivity::class.java
            )

        intent.putExtra(
            "uri",
            item.uri
        )

        intent.putExtra(
            "title",
            item.name
        )

        intent.putExtra(
            "folder",
            item.folder
        )

        startActivity(intent)
    }

    private fun toggleFavorite(
        item: VideoItem
    ): Boolean {

        val result =
            FavoriteManager.toggle(
                this,
                item.uri
            )

        Toast.makeText(
            this,
            if (result) {
                "已收藏"
            } else {
                "已取消收藏"
            },
            Toast.LENGTH_SHORT
        ).show()

        return true
    }

    override fun onBackPressed() {

        if (currentFolder != null) {

            currentFolder = null

            render()

            return
        }

        super.onBackPressed()
    }
}