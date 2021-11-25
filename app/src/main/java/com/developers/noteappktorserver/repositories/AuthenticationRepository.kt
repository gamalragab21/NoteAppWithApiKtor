package com.developers.noteappktorserver.repositories

import com.developers.noteappktorserver.data.network.ApiNoteService
import com.developers.noteappktorserver.entities.MyResponse
import com.developers.noteappktorserver.entities.User
import com.developers.noteappktorserver.qualifiers.IOThread
import com.developers.shopapp.helpers.Resource
import com.developers.noteappktorserver.helpers.safeCall
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.collections.HashMap

class AuthenticationRepository @Inject constructor(
    private val apiShopService: ApiNoteService,
    @IOThread
    private val dispatcher: CoroutineDispatcher
) {

    suspend fun loginUser(email: String, password: String): Resource<MyResponse<String>> =
        withContext(dispatcher) {
            safeCall {
                val hasmap = HashMap<String, String>()
                hasmap["email"] = email
                hasmap["password"] = password
                val result = apiShopService.loginUser(hasmap)
                Resource.Success(result)
            }
        }


    suspend fun createAccount(user: User): Resource<MyResponse<String>> = withContext(dispatcher) {
        safeCall {

            val result = apiShopService.register(user)
            Resource.Success(result)
        }
    }

    suspend fun getProfile(): Resource<MyResponse<User>> = withContext(dispatcher) {
        safeCall {
             val result=apiShopService.getMe()
            Resource.Success(result)
        }
    }
//
//    suspend fun logout():Resource<AuthModel> = withContext(dispatcher){
//        safeCall {
//            val result=apiShopService.logout()
//            Resource.Success(result)
//        }
//    }
//
//    suspend fun verifyEmail(email:String):Resource<AuthModel> = withContext(dispatcher){
//        safeCall {
//            val hashMap=HashMap<String,String>()
//            hashMap["email"] = email
//            val result=apiShopService.verifyEmail(hashMap)
//            Resource.Success(result)
//        }
//    }
//
//    suspend fun verifyCode(email:String,code:String):Resource<AuthModel> = withContext(dispatcher){
//        safeCall {
//            val hashMap=HashMap<String,String>()
//            hashMap["email"] = email
//            hashMap["code"] = code
//            val result=apiShopService.verifyCode(hashMap)
//            Resource.Success(result)
//        }
//    }
//
//    suspend fun resetPassword(password: String, email: String, code: String): Resource<AuthModel> = withContext(dispatcher){
//        safeCall {
//            val hashMap=HashMap<String,String>()
//            hashMap["email"] = email
//            hashMap["code"] = code
//            hashMap["password"] = password
//
//            val result=apiShopService.resetPassword(hashMap)
//            Resource.Success(result)
//        }
//    }
}