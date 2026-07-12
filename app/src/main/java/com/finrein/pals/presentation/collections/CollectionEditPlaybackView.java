package com.finrein.pals.presentation.collections;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import androidx.media3.ui.PlayerView;
import androidx.media3.exoplayer.ExoPlayer;
import defpackage.cx1;
import defpackage.n30;

public class CollectionEditPlaybackView extends FrameLayout {
    public ImageView i0; // Preview image
    public PlayerView j0; // Player View
    public ExoPlayer r0; // Player Instance
    public n30 s0; // Listener/State observer
    public View m0; // Delete button placeholder/view
    public View n0; // Mute/Action button placeholder/view
    
    public boolean B0 = false;
    public boolean w0 = true;
    public boolean t0 = true;
    public boolean u0 = true;
    public boolean v0 = true;
    public boolean x0 = true;
    public boolean y0 = false;
    
    private AudioManager a0;
    private ContentResolver b0;
    private boolean C0 = false;
    private int D0 = 0;

    private final ContentObserver E0 = new ContentObserver(new Handler(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            if (a0 != null) {
                int currentVolume = a0.getStreamVolume(AudioManager.STREAM_MUSIC);
                if (currentVolume != D0) {
                    D0 = currentVolume;
                    if (y0) {
                        y0 = false;
                        a();
                    }
                }
            }
        }
    };

    public CollectionEditPlaybackView(Context context) {
        super(context);
        init(context);
    }

    public CollectionEditPlaybackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CollectionEditPlaybackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Context appContext = context.getApplicationContext();
        a0 = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        b0 = appContext.getContentResolver();
        
        // Initialize placeholders so we don't get NullPointerException
        i0 = new ImageView(context);
        j0 = new PlayerView(context);
        m0 = new View(context);
        n0 = new View(context);
        
        addView(i0);
        addView(j0);
    }

    public final void a() {
        ExoPlayer player = this.r0;
        if (player != null) {
            player.setVolume(j() ? 0.0f : 1.0f);
        }
    }

    public final void b(Uri uri, String str, boolean z, boolean z2) {
        setSaveCompleted(false);
        setSaveButtonEnabled(true);
        this.t0 = str != null && str.toLowerCase().contains("video");
        this.u0 = z;
        this.x0 = z;
        this.y0 = z && z2;
        this.B0 = false;
        p();
        
        if (this.m0 != null) {
            this.m0.setVisibility(this.v0 ? View.VISIBLE : View.GONE);
        }
        if (this.n0 != null) {
            this.n0.setVisibility((this.v0 && this.t0 && z) ? View.VISIBLE : View.GONE);
        }
        if (this.i0 != null) {
            this.i0.setVisibility(View.VISIBLE);
        }
        if (this.j0 != null) {
            this.j0.setVisibility(View.GONE);
        }
        
        i();
        
        if (this.t0) {
            Context context = getContext();
            ExoPlayer exoPlayer = cx1.z(context, true ^ j(), "CollectionEditPlayback", null);
            exoPlayer.setPlayWhenReady(false);
            
            n30 n30Var = new n30(this, exoPlayer, uri, z);
            exoPlayer.addListener(n30Var);
            
            this.r0 = exoPlayer;
            this.s0 = n30Var;
            
            if (this.j0 != null) {
                this.j0.setPlayer(exoPlayer);
                this.j0.setVisibility(View.VISIBLE);
            }
            a();
            cx1.W(exoPlayer, uri);
        }
        n();
    }

    public final void i() {
        ExoPlayer player = this.r0;
        if (player != null) {
            n30 n30Var = this.s0;
            if (n30Var != null) {
                player.removeListener(n30Var);
            }
            player.setPlayWhenReady(false);
            player.stop();
            player.release();
        }
        this.s0 = null;
        this.r0 = null;
        this.B0 = false;
        if (this.j0 != null) {
            this.j0.setPlayer(null);
        }
    }

    public final boolean j() {
        return this.x0 && !this.y0;
    }

    public final void m() {
        ExoPlayer player = this.r0;
        if (player == null) {
            return;
        }
        if (this.j0 != null) {
            this.j0.setVisibility(View.VISIBLE);
        }
        if (player.getPlaybackState() == androidx.media3.common.Player.STATE_READY && this.B0 && this.t0) {
            if (this.j0 != null) {
                this.j0.setVisibility(View.VISIBLE);
            }
            if (this.i0 != null) {
                this.i0.setVisibility(View.GONE);
            }
        }
        player.setPlayWhenReady(true);
    }

    public final void n() {
        boolean z = this.w0;
        if (!z || j() || !c()) {
            if (this.C0) {
                b0.unregisterContentObserver(E0);
                this.C0 = false;
            }
            return;
        }
        if (this.C0) {
            return;
        }
        if (a0 != null) {
            this.D0 = a0.getStreamVolume(AudioManager.STREAM_MUSIC);
        }
        b0.registerContentObserver(Settings.System.CONTENT_URI, true, E0);
        this.C0 = true;
    }

    public final boolean c() {
        if (!this.t0 || !this.u0 || !this.v0) {
            return false;
        }
        return this.n0 != null && this.n0.getVisibility() == View.VISIBLE && this.n0.isEnabled();
    }

    public final void p() {
        // Replicate Setlog's p() helper layout/refresh logic
    }

    public final void setSaveCompleted(boolean z) {
        // Helper UI method placeholder
    }

    public final void setSaveButtonEnabled(boolean z) {
        // Helper UI method placeholder
    }
}
