package com.example.videoapplication.Adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.videoapplication.R;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.MyViewHolder> {

    private Context mContext;
    private List<String> uriList;



    public VideoAdapter(Context mContext, List<String> uriList) {

        this.mContext = mContext;
        this.uriList = uriList;


    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(mContext)
                .inflate(R.layout.video_fragment, viewGroup, false);

        return new MyViewHolder(itemView);


    }

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder myViewHolder, int i) {


        myViewHolder.videoView.setMediaController(new MediaController(mContext));
        myViewHolder.videoView.setVideoURI(Uri.parse(uriList.get(i)));
        myViewHolder.videoView.requestFocus();
        myViewHolder.videoView.start();
        myViewHolder.videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        if (what==MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START)
                            myViewHolder.progressBar.setVisibility(View.GONE);
                        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START)
                        myViewHolder.progressBar.setVisibility(View.VISIBLE);


                        return false;
                    }
                });
            }
        });


    }

    @Override
    public int getItemCount() {

        return uriList.size();
    }



    public class MyViewHolder extends RecyclerView.ViewHolder {
        VideoView videoView;
        ProgressBar progressBar;


        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progress);
            videoView = itemView.findViewById(R.id.video);


        }

    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MyViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.progressBar.setVisibility(View.GONE);
        holder.videoView.requestFocus();
        holder.videoView.stopPlayback();


    }

    @Override
    public void onViewAttachedToWindow(@NonNull MyViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        holder.progressBar.setVisibility(View.VISIBLE);
        holder.videoView.requestFocus();
        holder.videoView.start();
    }

}