package com.example.swtermproject.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.swtermproject.data.local.BAppDatabase
import com.example.swtermproject.data.local.entity.BNotificationEntity

class BLowStockCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        return runCatching {
            val database = BAppDatabase.getDatabase(applicationContext)
            val lowStockIngredients = database.ingredientDao().getLowStockIngredients()
            val notificationHelper = BNotificationHelper(applicationContext)

            lowStockIngredients.forEach { ingredient ->
                val percent = (ingredient.stockRate() * 100).toInt()
                notificationHelper.showLowStockNotification(
                    ingredientName = ingredient.name,
                    percent = percent
                )

                database.notificationDao().insertNotification(
                    BNotificationEntity(
                        title = "${ingredient.name} 부족 알림",
                        message = "현재 남은 양이 약 $percent% 입니다."
                    )
                )
            }

            Result.success()
        }.getOrElse {
            Result.retry()
        }
    }
}
