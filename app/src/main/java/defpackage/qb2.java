package defpackage;

import androidx.media3.common.PlaybackException;

public interface qb2 extends androidx.media3.common.Player.Listener {
    default void k() {
    }

    default void C(PlaybackException e) {
    }

    default void v(int state) {
    }

    @Override
    default void onRenderedFirstFrame() {
        k();
    }

    @Override
    default void onPlayerError(PlaybackException error) {
        C(error);
    }

    @Override
    default void onPlaybackStateChanged(int playbackState) {
        v(playbackState);
    }
}
