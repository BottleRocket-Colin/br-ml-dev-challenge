package com.br.ml.pathfinder.compose.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.br.ml.pathfinder.compose.R
import com.br.ml.pathfinder.compose.resources.PathFinderTheme
import com.br.ml.pathfinder.compose.resources.SplashBackground
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player.Listener
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSource
import com.google.android.exoplayer2.upstream.RawResourceDataSource

@Composable
fun SplashScreen(onSplashEnd: () -> Unit) {
    val context = LocalContext.current

    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            // Setup Media source using raw resource
            setMediaSource(
                ProgressiveMediaSource.Factory(
                    DefaultDataSource.Factory(context)
                ).createMediaSource(
                    MediaItem.fromUri(
                        RawResourceDataSource.buildRawResourceUri(R.raw.splash)
                    )
                )
            )

            // Invoke callback at end
            addListener(object : Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == ExoPlayer.STATE_ENDED) {
                        onSplashEnd()
                    }
                }
            })

            // Start immediately
            playWhenReady = true
            prepare()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground)
    ) {
        AndroidView(factory = {
            StyledPlayerView(context).apply{
                player = exoPlayer
                useController = false
            }
        })
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSplashScreen() {
    PathFinderTheme {
        SplashScreen {}
    }
}