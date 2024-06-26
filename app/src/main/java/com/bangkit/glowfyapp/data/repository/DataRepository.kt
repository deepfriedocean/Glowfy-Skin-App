package com.bangkit.glowfyapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.bangkit.glowfyapp.data.api.ApiConfig
import com.bangkit.glowfyapp.data.api.ApiService
import com.bangkit.glowfyapp.data.models.ErrorResponse
import com.bangkit.glowfyapp.data.models.ResultApi
import com.bangkit.glowfyapp.data.models.auth.LoginResponse
import com.bangkit.glowfyapp.data.models.auth.LoginResult
import com.bangkit.glowfyapp.data.models.auth.RegisterResponse
import com.bangkit.glowfyapp.data.models.items.ArticlesResponse
import com.bangkit.glowfyapp.data.models.items.ProductResponse
import com.bangkit.glowfyapp.data.models.items.SkinsResponse
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException

class DataRepository(
    private val apiService: ApiService,
    private val pref: UserPreference
) {
    suspend fun saveSession(user: LoginResult) {
        pref.saveSession(user)
    }

    fun getUser(): Flow<LoginResult> {
        return pref.getUser()
    }

    suspend fun logout() {
        pref.logout()
    }

    fun registerUser(name: String, email: String, password: String): LiveData<ResultApi<RegisterResponse>> = liveData {
        emit(ResultApi.Loading)
        try {
            val response = apiService.userRegister(name, email, password)
            emit(ResultApi.Success(response))
        } catch (e: HttpException) {
            emit(handleHttpException(e))
        }
    }

    fun loginUser(email: String, password: String): LiveData<ResultApi<LoginResponse>> = liveData {
        emit(ResultApi.Loading)
        try {
            val response = apiService.userLogin(email, password)
            emit(ResultApi.Success(response))
        } catch (e: HttpException) {
            emit(handleHttpException(e))
        }
    }

    fun getArticles(token: String): LiveData<ResultApi<ArticlesResponse>> = liveData {
        emit(ResultApi.Loading)
        try {
            val response = ApiConfig().getApiService(token).getArticles()
            emit(ResultApi.Success(response))
        } catch (e: HttpException) {
            emit(handleHttpException(e))
        }
    }

    fun getSkins(token: String): LiveData<ResultApi<SkinsResponse>> = liveData {
        emit(ResultApi.Loading)
        try {
            val response = ApiConfig().getApiService(token).getSkins()
            emit(ResultApi.Success(response))
        } catch (e: HttpException) {
            emit(handleHttpException(e))
        }
    }

    fun getProducts(token: String): LiveData<ResultApi<ProductResponse>> = liveData {
        emit(ResultApi.Loading)
        try {
            val response = ApiConfig().getApiService(token).getProducts()
            emit(ResultApi.Success(response))
        } catch (e: HttpException) {
            emit(handleHttpException(e))
        }
    }

    fun getProductsByCategory(token: String, category: String): LiveData<ResultApi<ProductResponse>> = liveData {
        emit(ResultApi.Loading)
        try {
            val response = ApiConfig().getApiService(token).getProductsByCategory(category)
            emit(ResultApi.Success(response))
        } catch (e: HttpException) {
            emit(handleHttpException(e))
        }
    }

    private fun handleHttpException(e: HttpException): ResultApi.Error {
        val jsonInString = e.response()?.errorBody()?.string()
        val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
        val errorMessage = errorBody.message
        val errorText= "An error occurred"
        return ResultApi.Error(errorMessage ?: errorText)
    }

    companion object {
        @Volatile
        private var instance: DataRepository? = null
        fun getInstance(
            apiService: ApiService,
            userPreference: UserPreference
        ): DataRepository =
            instance ?: synchronized(this) {
                instance ?: DataRepository(apiService, userPreference)
            }.also { instance = it }
    }
}