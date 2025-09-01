package com.agb.laithsurvey.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class RequestInterceptor(
    private val tokenProvider: () -> String?
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return makeRequest(chain)
    }

    private fun makeRequest(chain: Interceptor.Chain): Response {
        val token = tokenProvider()

        val oldRequest = chain.request().newBuilder()
            .addHeader(ACCEPT, ACCEPT_VALUE)
            .apply {
                if (!token.isNullOrEmpty()) {
                    addHeader(AUTHORIZATION, "Bearer $token")
                }
            }
            .build()

        return chain.proceed(oldRequest)
    }

    companion object {
        private const val ACCEPT = "Accept"
        private const val ACCEPT_VALUE = "application/json"
        private const val AUTHORIZATION = "Authorization"
    }
}
