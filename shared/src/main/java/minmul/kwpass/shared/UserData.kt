package minmul.kwpass.shared

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

val Context.dataStore by preferencesDataStore(name = "userInfo")

class UserData @Inject constructor(@ApplicationContext private val context: Context) {
    companion object {
        val KEY_RID = stringPreferencesKey("rid") // 학번
        val KEY_PASSWORD = stringPreferencesKey("password") // 비밀번호
        val KEY_TEL = stringPreferencesKey("tel") // 전화번호
        val KEY_IS_FIRST_RUN = booleanPreferencesKey("is_first_run")
    }


    suspend fun saveUserCredentials(rid: String, pass: String, tel: String) {
        val encryptedRid = CryptoManager.encrypt(rid)
        val encryptedPass = CryptoManager.encrypt(pass)
        val encryptedTel = CryptoManager.encrypt(tel)

        context.dataStore.edit { preferences ->
            preferences[KEY_RID] = encryptedRid
            preferences[KEY_PASSWORD] = encryptedPass
            preferences[KEY_TEL] = encryptedTel
        }
    }

    val userFlow: Flow<Triple<String, String, String>> = context.dataStore.data
        .map { preferences ->
            val encryptedRid = preferences[KEY_RID] ?: ""
            val encryptedPass = preferences[KEY_PASSWORD] ?: ""
            val encryptedTel = preferences[KEY_TEL] ?: ""

            Triple(
                if (encryptedRid.isNotEmpty()) CryptoManager.decrypt(encryptedRid) else "",
                if (encryptedPass.isNotEmpty()) CryptoManager.decrypt(encryptedPass) else "",
                if (encryptedTel.isNotEmpty()) CryptoManager.decrypt(encryptedTel) else "",
            )
        }

    val isFirstRun: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[KEY_IS_FIRST_RUN] ?: true
        }

    suspend fun finishedInitialSetupProcessedStatus() {
        context.dataStore.edit { preferences ->
            preferences[KEY_IS_FIRST_RUN] = false
        }
    }

}