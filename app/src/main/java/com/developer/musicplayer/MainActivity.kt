package com.developer.musicplayer

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), SongChangeListener {
    private val listMusic = mutableListOf<Music>()
    private lateinit var musicRecyclerView: RecyclerView
    private lateinit var searchBtn: LinearLayout
    private lateinit var menuBtn: LinearLayout
    private lateinit var playPauseCard: CardView
    private lateinit var playPauseImg: ImageView
    private lateinit var prevBtn: ImageView
    private lateinit var nextBtn: ImageView
    private lateinit var startTime: TextView
    private lateinit var endTime: TextView
    private var isPlaying: Boolean = false
    private lateinit var playerSeekBar: SeekBar
    private lateinit var mediaPlayer: MediaPlayer
    private var position: Int? = null
    private lateinit var bottomBar: LinearLayout
    private lateinit var notFoundText: TextView
    private lateinit var playingSongTitle: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val decorView = window.decorView
//
//        val option = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//        decorView.systemUiVisibility = option

        setContentView(R.layout.activity_main)

        initView()
        bottomBar.isVisible = false
        musicRecyclerView.setHasFixedSize(true)
        musicRecyclerView.layoutManager = LinearLayoutManager(this)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            getMusicFiles()
        } else {
            requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 11)
        }

        notFoundText.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getMusicFiles()
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 11)
            }
        }

        playPauseCard.setOnClickListener{
            if (isPlaying) {
                playingSongTitle.isSelected = false
                mediaPlayer.pause()
                playPauseImg.setImageResource(R.drawable.play_icon)
            } else {
                playingSongTitle.isSelected = true
                mediaPlayer.start()
                playPauseImg.setImageResource(R.drawable.pause_icon)
            }
            isPlaying = !isPlaying
        }

        playingSongTitle.isSelected = true

        nextBtn.setOnClickListener{
            val nextPosition: Int = if (position == listMusic.count() - 1) {
                0
            } else {
                position!! + 1
            }
            listMusic[position!!].isPlaying = false
            listMusic[nextPosition].isPlaying = true
            musicRecyclerView.adapter?.notifyDataSetChanged()
            musicRecyclerView.scrollToPosition(nextPosition)
            onChanged(nextPosition)
        }

        prevBtn.setOnClickListener{
            val prevPosition: Int = if (position == 0) {
                0
            } else {
                listMusic.count() - 1
            }
            listMusic[position!!].isPlaying = false
            listMusic[prevPosition].isPlaying = true

            musicRecyclerView.adapter?.notifyDataSetChanged()
            musicRecyclerView.scrollToPosition(prevPosition)
            onChanged(prevPosition)

        }

        playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                if (position != null) {
                    if (p2) { // from user
                        if (!isPlaying) {
                            mediaPlayer.start()
                            playPauseImg.setImageResource(R.drawable.pause_icon)
                        }
                        mediaPlayer.seekTo(p1)
                    }
                }
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }
        })
    }

    private fun initView() {
        musicRecyclerView = findViewById(R.id.musicRecyclerView)
        searchBtn = findViewById(R.id.searchBtn)
        menuBtn = findViewById(R.id.menuBtn)
        playPauseCard = findViewById(R.id.playPauseCard)
        playPauseImg = findViewById(R.id.playPauseImg)
        prevBtn = findViewById(R.id.prevBtn)
        nextBtn = findViewById(R.id.nextBtn)
        startTime = findViewById(R.id.startTime)
        endTime = findViewById(R.id.endTime)
        playerSeekBar = findViewById(R.id.playerSeekBar)
        mediaPlayer = MediaPlayer()
        bottomBar = findViewById(R.id.bottomBar)
        notFoundText = findViewById(R.id.notFoundText)
        playingSongTitle = findViewById(R.id.playingSongTitle)
    }

    private fun getMusicFiles() {
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val cusor = contentResolver.query(uri, null, MediaStore.Audio.Media.DATA + " LIKE?", arrayOf("%.mp3%"), null)

        if (cusor == null) {
            Toast.makeText(this, "Something when wrong", Toast.LENGTH_SHORT).show()
        } else if (!cusor.moveToNext()) {
            Toast.makeText(this, "No Music Found", Toast.LENGTH_SHORT).show()
        } else {
            while (cusor.moveToNext()) {
                val name = cusor.getString(cusor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                val artist = cusor.getString(cusor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                val cusorId = cusor.getLong(cusor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))

                val fileUri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, cusorId)

                val duration: String = cusor.getString(cusor.getColumnIndexOrThrow(MediaStore.Audio.AudioColumns.DURATION))

                val music = Music(name, artist, duration, fileUri)
                listMusic.add(music)
            }
            musicRecyclerView.adapter = MusicAdapter(listMusic, this)
            if (listMusic.isEmpty()) {
                musicRecyclerView.isVisible = false
                notFoundText.isVisible = true
            }
        }
        cusor?.close()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.count() > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getMusicFiles()
        } else {
            Toast.makeText(this, "Permission has declined by user", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onChanged(position: Int) {
        this.position = position
        playingSongTitle.text = listMusic[position].titleAndName
        if (!bottomBar.isVisible) {
            bottomBar.isVisible = true
        }
        try {
            if (mediaPlayer.isPlaying) {
                mediaPlayer.pause()
            }
            mediaPlayer.reset()
            mediaPlayer.setDataSource(this, listMusic[position].getFileUrl)
            mediaPlayer.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Unable to play track", Toast.LENGTH_SHORT).show()
        }


        mediaPlayer.setOnPreparedListener {
            val totalDuration = mediaPlayer.duration
            val generateDuration = String.format(
                Locale.getDefault(),
                "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(totalDuration.toLong()),
                TimeUnit.MILLISECONDS.toSeconds(totalDuration.toLong()) % 60,
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalDuration.toLong())),
            )

            endTime.text = generateDuration

            mediaPlayer.start()
            isPlaying = true

            playerSeekBar.max = totalDuration
            playPauseImg.setImageResource(R.drawable.pause_icon)
        }


        val timer = Timer()

        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    val currentDuration = mediaPlayer.currentPosition
                    val generateDuration = String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(currentDuration.toLong()),
                        TimeUnit.MILLISECONDS.toSeconds(currentDuration.toLong()) % 60,
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(currentDuration.toLong())),
                    )
                    playerSeekBar.progress = currentDuration
                    startTime.text = generateDuration
                }
            }
        }, 1000, 100)

        mediaPlayer.setOnCompletionListener {
            mediaPlayer.reset()
            timer.purge()
            timer.cancel()
            isPlaying = false
            playPauseImg.setImageResource(R.drawable.play_icon)
            playerSeekBar.progress = 0
            // play next song
            val nextPosition: Int = if (position == listMusic.count() - 1) {
                0
            } else {
                position + 1
            }

            listMusic[position].isPlaying = false
            listMusic[nextPosition].isPlaying = true
            onChanged(nextPosition)
            musicRecyclerView.adapter?.notifyDataSetChanged()
            musicRecyclerView.scrollToPosition(nextPosition)
        }
    }
}