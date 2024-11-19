package kiwi.hoonkun.ckremote.utils

import cocoapods.WebRTC.RTCAudioSession
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.AVFAudio.AVAudioSession
import platform.AVFAudio.AVAudioSessionCategoryPlayback
import platform.AVFAudio.AVAudioSessionModeDefault
import platform.AVFAudio.AVAudioSessionRouteChangeNotification
import platform.Foundation.NSError
import platform.Foundation.NSNotificationCenter

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
object AudioSessionManager {

    init {
        NSNotificationCenter.defaultCenter
            .removeObserver(
                observer = RTCAudioSession.sharedInstance(),
                name = AVAudioSessionRouteChangeNotification,
                `object` = null
            )
    }

    fun dummy() = true

    fun forceToPlayback() {
        memScoped {
            val session = AVAudioSession.sharedInstance()

            val categoryError = alloc<ObjCObjectVar<NSError?>>()
            val modeError = alloc<ObjCObjectVar<NSError?>>()

            session.setCategory(
                category = AVAudioSessionCategoryPlayback,
                error = categoryError.ptr
            )
            session.setMode(
                mode = AVAudioSessionModeDefault,
                error = modeError.ptr
            )

            categoryError.value?.let {
                println("E AudioSessionManager::onRouteChange[category]: ${it.localizedDescription}")
            }
            modeError.value?.let {
                println("E AudioSessionManager::onRouteChange[mode]: ${it.localizedDescription}")
            }
        }
    }

}
