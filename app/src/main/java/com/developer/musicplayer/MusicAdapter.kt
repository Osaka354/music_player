package com.developer.musicplayer

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import java.util.concurrent.TimeUnit

class MusicAdapter(
    private val list: List<Music>,
    private val context: Context,
    private val songChangeListener: SongChangeListener = context as SongChangeListener
) : RecyclerView.Adapter<MusicAdapter.MyViewHolder>(){
    private var playingPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.music_adapter_layout, null))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val music: Music = list[position]

        if (music.isPlaying) {
            playingPosition = holder.adapterPosition
            holder.getRootLayout.setBackgroundResource(R.drawable.round_blue_10)
        } else {
            holder.getRootLayout.setBackgroundResource(R.drawable.round_black_10)
        }
        val generateDuration = String.format(
            Locale.getDefault(),
            "%02d:%02d",
            TimeUnit.MILLISECONDS.toMinutes(music.getDuration.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(music.getDuration.toLong()) % 60,
            TimeUnit.MINUTES.toSeconds(music.getDuration.toLong()),
        )
        holder.getTitle.text = music.getTitle
        holder.getArtist.text = music.getArtist
        holder.getDuration.text = generateDuration

        holder.getRootLayout.setOnClickListener {
            list[playingPosition].isPlaying = false
            music.isPlaying = true

            songChangeListener.onChanged(position)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val rootLayout: RelativeLayout = itemView.findViewById(R.id.rootLayout)
//        private val title: TextView = itemView.findViewById(R.id.musicTitle)
//        private val artist: TextView = itemView.findViewById(R.id.musicArtist)
//        private val duration: TextView = itemView.findViewById(R.id.musicDuration)

        val getRootLayout: RelativeLayout get() = itemView.findViewById(R.id.rootLayout)
        val getTitle: TextView get() = itemView.findViewById(R.id.musicTitle)
        val getArtist: TextView get() = itemView.findViewById(R.id.musicArtist)
        val getDuration: TextView get() = itemView.findViewById(R.id.musicDuration)
    }
}
