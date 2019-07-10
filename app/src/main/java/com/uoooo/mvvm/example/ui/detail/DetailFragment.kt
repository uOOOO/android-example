package com.uoooo.mvvm.example.ui.detail

import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.util.EventLogger
import com.jakewharton.rxbinding3.view.clicks
import com.uoooo.mvvm.example.GlideApp
import com.uoooo.mvvm.example.R
import com.uoooo.mvvm.example.data.ServerConfig
import com.uoooo.mvvm.example.domain.model.Movie
import com.uoooo.mvvm.example.ui.common.getPosterImageUrl
import com.uoooo.mvvm.example.ui.player.ExoPlayerPlayManager
import com.uoooo.mvvm.example.ui.player.rx.*
import io.sellmair.disposer.Disposer
import io.sellmair.disposer.disposeBy
import io.sellmair.disposer.onDestroy
import io.sellmair.disposer.onStop
import kotlinx.android.synthetic.main.fragment_detail.*

class DetailFragment : Fragment() {
    private lateinit var backdropImage: ImageView

    private val playManager: ExoPlayerPlayManager by lazy {
        ExoPlayerPlayManager()
    }

    private val listenerDisposer = Disposer.create(onStop)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val movie: Movie = (arguments?.getSerializable(BUNDLE_OBJECT) ?: return) as Movie

        backdropImage = ImageView(context).apply {
            setColorFilter(Color.parseColor("#949494"), PorterDuff.Mode.MULTIPLY)
            playerView.overlayFrameLayout?.addView(this)
        }

        GlideApp.with(backdropImage)
            .load(getPosterImageUrl(movie.backdropPath, ServerConfig.ImageSize.NORMAL))
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(backdropImage)

        GlideApp.with(posterImage)
            .load(getPosterImageUrl(movie.posterPath, ServerConfig.ImageSize.NORMAL))
            .transition(DrawableTransitionOptions.withCrossFade())
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(posterImage)

        playButton.clicks()
            .subscribe { playerStart() }
            .disposeBy(onDestroy)
    }

    private fun playerStart() {
        val contentUri =
            Uri.parse("https://storage.googleapis.com/wvmedia/clear/h264/tears/tears_sd.mpd")
        playManager.prepare(context!!, playerView, null, contentUri, null)
        playManager.start()
        addPlayerListeners()
        hideInteractionView()
    }

    private fun playerPause() {
        playManager.pause()
    }

    private fun playerRelease() {
        playManager.release()
    }

    private fun addPlayerListeners() {
        playManager.addAnalyticsListener(EventLogger(playManager.trackSelector))
        playManager.player?.apply {
            listenerDisposer += events().subscribe { onExoPlayerEventListener(it) }
            listenerDisposer += videos().subscribe { onExoPlayerVideoListener(it) }
        }
    }

    private fun showInteractionView() {
        playerView.hideController()
        playButton.resumeAnimation()
        interactionGroup.visibility = View.VISIBLE
    }

    private fun hideInteractionView() {
        playButton.pauseAnimation()
        interactionGroup.visibility = View.INVISIBLE
    }

    private fun showBackdropImage() {
        backdropImage.visibility = View.VISIBLE
    }

    private fun hideBackdropImage() {
        backdropImage.visibility = View.INVISIBLE
    }

    override fun onStart() {
        super.onStart()
        showInteractionView()
        showBackdropImage()
    }

    override fun onStop() {
        playerPause()
        super.onStop()
    }

    override fun onDestroy() {
        playerRelease()
        super.onDestroy()
    }

    private fun onExoPlayerEventListener(event: ExoPlayerEvent) {
        when (event) {
            is ExoPlayerEventPlayerStateChanged -> onPlayerStateChanged(event)
        }
    }

    private fun onPlayerStateChanged(event: ExoPlayerEventPlayerStateChanged) {
        Log.d(TAG, "onPlayerStateChanged() event = $event")
        if (event.playbackState == Player.STATE_ENDED) {
            if (!listenerDisposer.isDisposed) listenerDisposer.dispose()
            playerRelease()
            showBackdropImage()
            showInteractionView()
        }
    }

    private fun onExoPlayerVideoListener(event: ExoPlayerVideo) {
        when (event) {
            is ExoPlayerVideoRenderedFirstFrame -> onRenderedFirstFrame()
        }
    }

    private fun onRenderedFirstFrame() {
        Log.d(TAG, "onRenderedFirstFrame()")
        hideBackdropImage()
        hideInteractionView()
    }

    companion object {
        private val TAG: String = DetailFragment::class.java.simpleName

        private const val BUNDLE_OBJECT = "movie"

        fun newInstance(movie: Movie): DetailFragment {
            return DetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(BUNDLE_OBJECT, movie)
                }
            }
        }
    }
}