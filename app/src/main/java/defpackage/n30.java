package defpackage;

import android.net.Uri;
import android.util.Log;
import com.finrein.pals.presentation.collections.CollectionEditPlaybackView;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.common.PlaybackException;

public final class n30 implements qb2 {
    public final CollectionEditPlaybackView V;
    public final ExoPlayer W;
    public final Uri X;
    public final boolean Y;

    public n30(CollectionEditPlaybackView collectionEditPlaybackView, ExoPlayer exoPlayer, Uri uri, boolean z) {
        this.V = collectionEditPlaybackView;
        this.W = exoPlayer;
        this.X = uri;
        this.Y = z;
    }

    @Override
    public final void C(PlaybackException e) {
        CollectionEditPlaybackView collectionEditPlaybackView = this.V;
        if (collectionEditPlaybackView.r0 != this.W) {
            return;
        }
        Log.w("CollectionEditPlayback", "Collection edit playback failed for uri=" + this.X + " hasAudio=" + this.Y, e);
        if (collectionEditPlaybackView.i0 != null) {
            collectionEditPlaybackView.i0.setVisibility(android.view.View.VISIBLE);
        }
        if (collectionEditPlaybackView.j0 != null) {
            collectionEditPlaybackView.j0.setVisibility(android.view.View.GONE);
        }
    }

    @Override
    public final void k() {
        CollectionEditPlaybackView collectionEditPlaybackView = this.V;
        ExoPlayer exoPlayer = collectionEditPlaybackView.r0;
        if (exoPlayer != this.W) {
            return;
        }
        collectionEditPlaybackView.B0 = true;
        if (collectionEditPlaybackView.w0 && collectionEditPlaybackView.t0 && exoPlayer != null) {
            if (collectionEditPlaybackView.j0 != null) {
                collectionEditPlaybackView.j0.setVisibility(android.view.View.VISIBLE);
            }
            if (collectionEditPlaybackView.i0 != null) {
                collectionEditPlaybackView.i0.setVisibility(android.view.View.GONE);
            }
        }
    }

    @Override
    public final void v(int i) {
        CollectionEditPlaybackView collectionEditPlaybackView = this.V;
        if (collectionEditPlaybackView.r0 == this.W && i == androidx.media3.common.Player.STATE_READY && collectionEditPlaybackView.w0) {
            collectionEditPlaybackView.m();
        }
    }
}
