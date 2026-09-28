代码:
package com.oklocal.player

import android.content.Intent
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@UnstableApi
class PlayerActivity : AppCompatActivity() {

    private lateinit var playerView: PlayerView
    private lateinit var timeText: TextView
    private lateinit var titleText: TextView
    private lateinit var gestureArea: View
    private lateinit var hintText: TextView
    private lateinit var favoriteButton: Button

    private var player: ExoPlayer? = null

    private var videoUri: String = ""
    private var videoTitle: String = ""

    private var downX = 0f
    private var downY = 0f

    private var startPosition = 0L

    private var volumeStart = 0
    private var brightnessStart = 0f

    private val handler =
        Handler(Looper.getMainLooper())

    private val clockRunnable =
        object : Runnable {

            override fun run() {

                updateBeijingTime()

                handler.postDelayed(
                    this,
                    1000
                )
            }
        }

    private val progressRunnable =
        object : Runnable {

            override fun run() {

                saveProgress()

                handler.postDelayed(
                    this,
                    3000
                )
            }
        }

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        setContentView(
            R.layout.activity_player
        )

        playerView =
            findViewById(
                R.id.playerView
            )

        timeText =
            findViewById(
                R.id.beijingTime
            )

        titleText =
            findViewById(
                R.id.playerTitle
            )

        gestureArea =
            findViewById(
                R.id.gestureArea
            )

        hintText =
            findViewById(
                R.id.hintText
            )

        favoriteButton =
            findViewById(
                R.id.favoriteButton
            )

        videoUri =
            intent.getStringExtra(
                "uri"
            ) ?: ""

        videoTitle =
            intent.getStringExtra(
                "title"
            ) ?: "本地视频"

        titleText.text =
            videoTitle

        updateFavoriteButton()

        if (videoUri.isEmpty()) {

            finish()

            return
        }

        setupGestures()

        initializePlayer()

        handler.post(
            clockRunnable
        )

        handler.post(
            progressRunnable
        )
    }

    private fun initializePlayer() {

        val exo =
            ExoPlayer.Builder(this)
                .build()

        player = exo

        playerView.player = exo

        val mediaItem =
            MediaItem.fromUri(
                Uri.parse(videoUri)
            )

        exo.setMediaItem(
            mediaItem
        )

        exo.addListener(
            object : Player.Listener {

                override fun onPlayerError(
                    error: PlaybackException
                ) {

                    Toast.makeText(
                        this@PlayerActivity,
                        "视频播放失败：${error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                override fun onPlaybackStateChanged(
                    playbackState: Int
                ) {

                    if (
                        playbackState ==
                        Player.STATE_ENDED
                    ) {

                        saveProgress()

                        HistoryManager.clear(
                            this@PlayerActivity,
                            videoUri
                        )
                    }
                }
            }
        )

        exo.prepare()

        val saved =
            HistoryManager.getPosition(
                this,
                videoUri
            )

        if (saved > 0) {

            startPosition = saved

            exo.seekTo(saved)
        }

        exo.playWhenReady = true
    }

    private fun setupGestures() {

        gestureArea.setOnTouchListener { _, event ->

            when (event.action) {

                MotionEvent.ACTION_DOWN -> {

                    downX = event.x
                    downY = event.y

                    volumeStart =
                        getCurrentVolume()

                    brightnessStart =
                        getCurrentBrightness()

                    true
                }

                MotionEvent.ACTION_MOVE -> {

                    val dx =
                        event.x - downX

                    val dy =
                        event.y - downY

                    if (abs(dx) > abs(dy)) {

                        true

                    } else {

                        handleVerticalGesture(
                            event.x,
                            dy
                        )

                        true
                    }
                }

                MotionEvent.ACTION_UP -> {

                    val dx =
                        event.x - downX

                    val dy =
                        event.y - downY

                    if (
                        abs(dx) >
                        150 &&
                        abs(dx) >
                        abs(dy)
                    ) {

                        if (dx > 0) {

                            seekBy(
                                10_000
                            )

                        } else {

                            seekBy(
                                -10_000
                            )
                        }
                    }

                    true
                }

                else -> true
            }
        }
    }

    private fun handleVerticalGesture(
        x: Float,
        dy: Float
    ) {

        val height =
            gestureArea.height
                .coerceAtLeast(1)

        val delta =
            (-dy / height)

        if (x <
            gestureArea.width / 2f
        ) {

            val current =
                brightnessStart +
                delta

            setBrightness(
                current
            )

            showHint(
                "亮度 ${(
                    current * 100
                ).toInt().coerceIn(1, 100)}%"
            )

        } else {

            val maxVolume =
                getMaxVolume()

            val current =
                volumeStart +
                (
                    delta *
                    maxVolume
                ).toInt()

            setVolume(
                current
            )

            showHint(
                "音量 ${(
                    current * 100 /
                    maxVolume.coerceAtLeast(1)
                ).coerceIn(0, 100)}%"
            )
        }
    }

    private fun seekBy(
        amount: Long
    ) {

        val exo =
            player ?: return

        val position =
            max(
                0L,
                min(
                    exo.duration
                        .takeIf { it > 0 }
                        ?: Long.MAX_VALUE,
                    exo.currentPosition +
                        amount
                )
            )

        exo.seekTo(
            position
        )

        showHint(
            if (amount > 0) {
                "+10 秒"
            } else {
                "-10 秒"
            }
        )
    }

    private fun getAudioManager():
            AudioManager {

        return getSystemService(
            AUDIO_SERVICE
        ) as AudioManager
    }

    private fun getCurrentVolume():
            Int {

        return getAudioManager()
            .getStreamVolume(
                AudioManager.STREAM_MUSIC
            )
    }

    private fun getMaxVolume():
            Int {

        return getAudioManager()
            .getStreamMaxVolume(
                AudioManager.STREAM_MUSIC
            )
    }

    private fun setVolume(
        value: Int
    ) {

        val manager =
            getAudioManager()

        manager.setStreamVolume(
            AudioManager.STREAM_MUSIC,
            value.coerceIn(
                0,
                manager.getStreamMaxVolume(
                    AudioManager.STREAM_MUSIC
                )
            ),
            0
        )
    }

    private fun getCurrentBrightness():
            Float {

        val value =
            window.attributes.screenBrightness

        return if (value < 0) {
            0.5f
        } else {
            value
        }
    }

    private fun setBrightness(
        value: Float
    ) {

        val params =
            window.attributes

        params.screenBrightness =
            value.coerceIn(
                0.01f,
                1f
            )

        window.attributes =
            params
    }

    private fun showHint(
        text: String
    ) {

        hintText.text = text

        hintText.visibility =
            View.VISIBLE

        handler.removeCallbacksAndMessages(
            "hint"
        )

        handler.postAtTime(
            {
                hintText.visibility =
                    View.GONE
            },
            "hint",
            System.currentTimeMillis() + 800
        )
    }

    private fun updateBeijingTime() {

        val formatter =
            SimpleDateFormat(
                "HH:mm:ss",
                Locale.US
            )

        formatter.timeZone =
            TimeZone.getTimeZone(
                "Asia/Shanghai"
            )

        timeText.text =
            formatter.format(
                Date()
            )
    }

    private fun saveProgress() {

        val exo =
            player ?: return

        if (exo.currentPosition > 0) {

            HistoryManager.savePosition(
                this,
                videoUri,
                exo.currentPosition
            )
        }
    }

    private fun updateFavoriteButton() {

        val favorite =
            FavoriteManager.isFavorite(
                this,
                videoUri
            )

        favoriteButton.text =
            if (favorite) {
                "★"
            } else {
                "☆"
            }
    }

    fun toggleFavorite(
        view: View
    ) {

        FavoriteManager.toggle(
            this,
            videoUri
        )

        updateFavoriteButton()
    }

    fun changeSpeed(
        view: View
    ) {

        val speeds =
            doubleArrayOf(
                0.5,
                0.75,
                1.0,
                1.25,
                1.5,
                2.0
            )

        val labels =
            arrayOf(
                "0.5x",
                "0.75x",
                "1.0x",
                "1.25x",
                "1.5x",
                "2.0x"
            )

        AlertDialog.Builder(this)
            .setTitle("播放速度")
            .setItems(labels) { _, which ->

                player?.setPlaybackSpeed(
                    speeds[which].toFloat()
                )

                showHint(
                    labels[which]
                )
            }
            .show()
    }

    fun selectAudio(
        view: View
    ) {

        val exo =
            player ?: return

        val groups =
            exo.currentTracks.groups

        val audioTracks =
            mutableListOf<Int>()

        groups.forEachIndexed { index, group ->

            if (
                group.type ==
                C.TRACK_TYPE_AUDIO
            ) {

                audioTracks.add(index)
            }
        }

        if (audioTracks.isEmpty()) {

            Toast.makeText(
                this,
                "没有检测到多音轨",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val labels =
            audioTracks.map {
                "音轨 ${it + 1}"
            }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("选择音轨")
            .setItems(labels) { _, which ->

                val groupIndex =
                    audioTracks[which]

                val group =
                    groups[groupIndex]

                if (
                    group.length > 0
                ) {

                    val format =
                        group.getTrackFormat(0)

                    val parameters =
                        exo.trackSelectionParameters
                            .buildUpon()
                            .setOverrideForType(
                                androidx.media3.common.TrackSelectionOverride(
                                    group.mediaTrackGroup,
                                    0
                                )
                            )
                            .build()

                    exo.trackSelectionParameters =
                        parameters

                    showHint(
                        format.language
                            ?: "音轨 ${which + 1}"
                    )
                }
            }
            .show()
    }

    fun selectSubtitle(
        view: View
    ) {

        val exo =
            player ?: return

        val groups =
            exo.currentTracks.groups

        val subtitles =
            mutableListOf<Int>()

        groups.forEachIndexed { index, group ->

            if (
                group.type ==
                C.TRACK_TYPE_TEXT
            ) {

                subtitles.add(index)
            }
        }

        if (subtitles.isEmpty()) {

            Toast.makeText(
                this,
                "当前视频没有内置字幕",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val labels =
            subtitles.map {
                "字幕 ${it + 1}"
            }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("选择字幕")
            .setItems(labels) { _, which ->

                val group =
                    groups[
                        subtitles[which]
                    ]

                if (
                    group.length > 0
                ) {

                    val parameters =
                        exo.trackSelectionParameters
                            .buildUpon()
                            .setOverrideForType(
                                androidx.media3.common.TrackSelectionOverride(
                                    group.mediaTrackGroup,
                                    0
                                )
                            )
                            .build()

                    exo.trackSelectionParameters =
                        parameters

                    showHint(
                        "字幕 ${which + 1}"
                    )
                }
            }
            .show()
    }

    override fun onPause() {

        saveProgress()

        super.onPause()
    }

    override fun onDestroy() {

        saveProgress()

        handler.removeCallbacks(
            clockRunnable
        )

        handler.removeCallbacks(
            progressRunnable
        )

        player?.release()

        player = null

        super.onDestroy()
    }

    override fun onWindowFocusChanged(
        hasFocus: Boolean
    ) {

        super.onWindowFocusChanged(
            hasFocus
        )

        if (hasFocus) {

            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }
}