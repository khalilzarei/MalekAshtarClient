package com.khz.malekclient.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException
import java.net.NetworkInterface

/**
 * وضعیت اتصال شبکه.
 *
 * - OFFLINE : هیچ شبکه‌ای فعال نیست
 * - VPN     : شبکه فعال است ولی از طریق فیلترشکن/VPN
 * - ONLINE  : اتصال عادی
 */
enum class NetState {
    OFFLINE,
    VPN,
    ONLINE
}

/**
 * خطای اختصاصی وقتی درخواست به‌خاطر وضعیت اتصال رد می‌شود.
 *
 * ارث‌بری از IOException چون در لایه‌ی شبکه (OkHttp) پرتاب می‌شود
 * و تمام لایه‌ها (Retrofit/OkHttp) آن را به‌عنوان خطای شبکه‌ی معمول می‌شناسند.
 * پیام فارسی مناسب داخلش است و ApiErrorHandler مستقیم نمایشش می‌دهد.
 */
class NetworkConnectivityException(override val message: String) : IOException(message)

/**
 * مانیتور وضعیت اینترنت.
 *
 * با استفاده از ConnectivityManager و NetworkCallback، وضعیت را به‌صورت زنده
 * به‌روزرسانی می‌کند و از طریق [state] (StateFlow) در دسترس UI است.
 *
 * شناسایی فیلترشکن/VPN:
 *  1) اگر ترانسپورت شبکه‌ی فعال TRANSPORT_VPN باشد → VPN
 *  2) اگر اینترفیسی با پیشوند tun/ipsec وجود داشته باشد → VPN
 */
class ConnectivityMonitor(context: Context) {

    private val cm = (context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)

    private val _state = MutableStateFlow(NetState.OFFLINE)

    /** وضعیت فعلی (واکنش‌گرا) */
    val state: StateFlow<NetState> = _state

    /** وضعیت فعلی (فوری) */
    fun current(): NetState = _state.value

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            evaluate()
        }

        override fun onLost(network: Network) {
            evaluate()
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            evaluate()
        }
    }

    /** شروع مانیتورینگ (بعد از ساخت، صدا زده شود) */
    fun start() {
        evaluate()
        try {
            cm.registerDefaultNetworkCallback(callback)
        } catch (_: Exception) {
            // در بعضی دستگاه‌ها ممکن است خطا بدهد
        }
    }

    fun stop() {
        try {
            cm.unregisterNetworkCallback(callback)
        } catch (_: Exception) {
        }
    }

    private fun evaluate() {
        val active = cm.activeNetwork
        if (active == null) {
            _state.value = NetState.OFFLINE
            return
        }
        val caps = cm.getNetworkCapabilities(active)
        if (caps == null) {
            _state.value = NetState.OFFLINE
            return
        }
        _state.value = if (isVpn(caps)) NetState.VPN else NetState.ONLINE
    }

    private fun isVpn(caps: NetworkCapabilities): Boolean {
        if (caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
            return true
        }
        return hasTunnelInterface()
    }

    /**
     * بسیاری از فیلترشکن‌ها (V2Ray/Shadowsocks) اینترفیس tunnel می‌سازند.
     * نام اینترفیس‌ها در اندروید کوچک (lowercase) هستند.
     */
    private fun hasTunnelInterface(): Boolean {
        val enumeration = try {
            NetworkInterface.getNetworkInterfaces()
        } catch (_: Exception) {
            return false
        }
                ?: return false

        while (enumeration.hasMoreElements()) {
            val name = enumeration.nextElement().name.lowercase()
            if (name.startsWith("tun") || name.startsWith("ipsec")) {
                return true
            }
        }
        return false
    }

    companion object {
        /** پیام مناسب برای هر وضعیت (null = مشکل ندارد) */
        fun messageFor(state: NetState): String? = when (state) {
            NetState.OFFLINE -> "به اینترنت متصل نیستید. لطفاً اتصال شبکه خود را بررسی کنید."
            NetState.VPN     -> "فیلترشکن/VPN روشن است. لطفاً آن را خاموش کنید و دوباره تلاش کنید."
            NetState.ONLINE  -> null
        }
    }
}
