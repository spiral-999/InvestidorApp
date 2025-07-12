package com.example.investidorapp.service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {// metodo chamado quando o app recebe uma mensagem remota
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Mensagem recebida de: ${remoteMessage.from}")
        remoteMessage.notification?.let {
            Log.d("FCM", "Corpo da Notificação: ${it.body}")
        }
    }
    override fun onNewToken(token: String) { // metodo para receber o token novo
        super.onNewToken(token)
        Log.d("FCM", "Novo token gerado: $token")
    }
}