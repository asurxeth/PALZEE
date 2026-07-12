package defpackage;

import android.content.Context;
import android.net.Uri;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.DefaultLoadControl;

public abstract class cx1 {
    public static final ExoPlayer z(Context context, boolean z, String str, Long l2) {
        ExoPlayer.Builder builder = new ExoPlayer.Builder(context.getApplicationContext())
            .setLoadControl(
                new DefaultLoadControl.Builder()
                    .setBufferDurationsMs(1000, 3000, 250, 500)
                    .setPrioritizeTimeOverSizeThresholds(true)
                    .build()
            );
        ExoPlayer player = builder.build();
        player.setRepeatMode(Player.REPEAT_MODE_ONE);
        player.setVolume(z ? 0.0f : 1.0f);
        player.setVideoScalingMode(androidx.media3.common.C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
        player.setPlayWhenReady(true);
        return player;
    }

    public static final void W(ExoPlayer exoPlayer, Uri uri) {
        exoPlayer.setMediaItem(MediaItem.fromUri(uri));
        exoPlayer.prepare();
    }
}
