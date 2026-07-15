package com.finrein.pals.core.player

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer

class FeedPlayerManager(private val context: Context) {
    private val activePlayers = mutableMapOf<Int, ExoPlayer>()

    fun getPlayerForIndex(index: Int): ExoPlayer {
        return activePlayers.getOrPut(index) {
            DualEnginePlayerFactory.getPooledInstance(context).apply {
                repeatMode = androidx.media3.common.Player.REPEAT_MODE_ALL
                volume = 0f
            }
        }
    }

    fun releaseUnusedPlayers(currentIndex: Int) {
        val indicesToKeep = listOf(currentIndex - 1, currentIndex, currentIndex + 1)
        val iterator = activePlayers.iterator()
        
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.key !in indicesToKeep) {
                DualEnginePlayerFactory.releaseIntoPool(entry.value)
                iterator.remove()
            }
        }
    }

    fun releaseAll() {
        val iterator = activePlayers.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            DualEnginePlayerFactory.releaseIntoPool(entry.value)
            iterator.remove()
        }
    }
}
