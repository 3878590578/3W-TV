package com.example.player3w

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class `3w_MainActivity` : AppCompatActivity() {

    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView
    private lateinit var btnOpenFile: Button
    private lateinit var tvTitle: TextView

    private var headSkipMs: Long = 0L                // 片头跳过 0 秒
    private var tailSkipMs: Long = 99 * 1000L        // 片尾提前 99 秒跳过

    private val handler = Handler(Looper.getMainLooper())
    private var currentVideoUri: Uri? = null

    private val openDocumentLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            playVideo(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.`3w_activity_main`)

        playerView = findViewById(R.id.player_view_3w)
        btnOpenFile = findViewById(R.id.btn_open_file_3w)
        tvTitle = playerView.findViewById(R.id.tv_title_3w)

        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        btnOpenFile.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("video/*"))
        }

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    btnOpenFile.visibility = View.GONE
                    
                    currentVideoUri?.let { uri ->
                        val savedPos = getSavedProgress(uri)
                        if (savedPos > 0 && player.currentPosition < savedPos) {
                            player.seekTo(savedPos)
                        } else if (headSkipMs > 0 && player.currentPosition < headSkipMs) {
                            player.seekTo(headSkipMs)
                        }
                    }
                }
            }
        })

        handler.post(checkProgressRunnable)
    }

    private fun playVideo(uri: Uri) {
        this.currentVideoUri = uri
        tvTitle.text = uri.lastPathSegment ?: "3W本地视频"

        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    private val checkProgressRunnable = object : Runnable {
        override fun run() {
            if (::player.isInitialized && player.isPlaying) {
                val currentPos = player.currentPosition
                val duration = player.duration

                currentVideoUri?.let { saveProgress(it, currentPos) }

                if (duration > 0 && currentPos >= (duration - tailSkipMs)) {
                    player.pause()
                }
            }
            handler.postDelayed(this, 1000)
        }
    }

    private fun saveProgress(uri: Uri, positionMs: Long) {
        val sp = getSharedPreferences("3w_video_history", Context.MODE_PRIVATE)
        sp.edit().putLong(uri.toString(), positionMs).apply()
    }

    private fun getSavedProgress(uri: Uri): Long {
        val sp = getSharedPreferences("3w_video_history", Context.MODE_PRIVATE)
        return sp.getLong(uri.toString(), 0L)
    }

    override fun onStop() {
        super.onStop()
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(checkProgressRunnable)
        player.release()
    }
}