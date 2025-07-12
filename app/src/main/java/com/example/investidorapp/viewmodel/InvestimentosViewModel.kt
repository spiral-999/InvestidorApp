package com.example.investidorapp.viewmodel

import android.Manifest
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.AndroidViewModel
import com.example.investidorapp.MainActivity
import com.example.investidorapp.R
import com.example.investidorapp.model.Investimento
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class InvestimentosViewModel(application: Application) : AndroidViewModel(application) {

    private val database = FirebaseDatabase.getInstance().getReference("investimentos") // nome do nó no realtime
    private val _investimentos = MutableStateFlow<List<Investimento>>(emptyList())
    val investimentos: StateFlow<List<Investimento>> = _investimentos
    private val currentInvestimentos = mutableMapOf<String, Investimento>()

    init {
        monitorarAlteracoes()
    }

    private fun monitorarAlteracoes() {
        database.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val investimento = snapshot.getValue(Investimento::class.java)
                if (investimento != null) {
                    currentInvestimentos[snapshot.key!!] = investimento
                    atualizarLista()
                }
            } // chamada para quando um novo investimento for adicionado

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val investimento = snapshot.getValue(Investimento::class.java)
                if (investimento != null) {
                    val nome = investimento.nome
                    val valor = investimento.valor
                    Log.d("FirebaseData", "Investimento atualizado: $nome R$ $valor")
                    currentInvestimentos[snapshot.key!!] = investimento
                    atualizarLista()
                    enviarNotificacao("Investimento Atualizado", "$nome agora vale R$ ${"%.2f".format(valor)}") // notificação para a mudança no investimento
                }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                currentInvestimentos.remove(snapshot.key)
                atualizarLista()
            } // investimento removido

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Erro ao monitorar alterações: ${error.message}")
            }
        })
    }

    private fun atualizarLista() {
        _investimentos.value = currentInvestimentos.values.toList()
    }

    private fun enviarNotificacao(titulo: String, mensagem: String) { // funcao das notificações
        val context = getApplication<Application>().applicationContext
        val channelId = "investimentos_notifications"
        val notificationId = (System.currentTimeMillis() % 10000).toInt()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notificações de Investimentos"
            val descriptionText = "Canal para notificações sobre atualizações de investimentos."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        } // intent para abrir o app na notificação
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.investicon) // icone personalizado
            .setContentTitle(titulo)
            .setContentText(mensagem)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Log.w("Notification", "Permissão para notificações não foi concedida.")
            return
        } // verificador de permissões
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }
}