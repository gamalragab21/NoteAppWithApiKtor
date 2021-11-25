package com.developers.noteappktorserver.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.developers.noteappktorserver.entities.MyResponse
import com.developers.noteappktorserver.entities.User
import com.developers.noteappktorserver.helpers.Event
import com.developers.noteappktorserver.helpers.MyValidation
import com.developers.noteappktorserver.qualifiers.MainThread
import com.developers.noteappktorserver.repositories.AuthenticationRepository
import com.developers.shopapp.helpers.Resource
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthenticationViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val repository: AuthenticationRepository,
    @MainThread
    private val dispatcher: CoroutineDispatcher

) : ViewModel() {


    private val _loginUserStatus =
        MutableStateFlow<Event<Resource<MyResponse<String>>>>(Event(Resource.Init()))
    val authLoginUserStatus: MutableStateFlow<Event<Resource<MyResponse<String>>>> = _loginUserStatus

    private val _createAccountStatus =
        MutableStateFlow<Event<Resource<MyResponse<String>>>>(Event(Resource.Init()))
    val createAccountStatus: MutableStateFlow<Event<Resource<MyResponse<String>>>> = _createAccountStatus



//    private val _logoutStatus = MutableStateFlow<Event<Resource<AuthModel>>>(Event(Resource.Init()))
//    val logoutStatus: MutableStateFlow<Event<Resource<AuthModel>>> = _logoutStatus
//
//    private val _verifyEmailStatus = MutableStateFlow<Event<Resource<AuthModel>>>(Event(Resource.Init()))
//    val verifyEmailStatus: MutableStateFlow<Event<Resource<AuthModel>>> = _verifyEmailStatus
//
//    private val _verifyCodeStatus = MutableStateFlow<Event<Resource<AuthModel>>>(Event(Resource.Init()))
//    val verifyCodeStatus: MutableStateFlow<Event<Resource<AuthModel>>> = _verifyCodeStatus
//
//    private val _resetPasswordStatus = MutableStateFlow<Event<Resource<AuthModel>>>(Event(Resource.Init()))
//    val resetPasswordStatus: MutableStateFlow<Event<Resource<AuthModel>>> = _resetPasswordStatus


    fun loginUser(
        inputTextLayoutEmail: TextInputLayout,
        inputTextLayoutPassword: TextInputLayout
    ) {
        viewModelScope.launch(dispatcher) {
            val email = inputTextLayoutEmail.editText!!.text.toString()
            val password = inputTextLayoutPassword.editText!!.text.toString()
            when {
                email.isEmpty() -> {
                    _loginUserStatus.emit(Event(Resource.Error("E-mail is require")))
                    inputTextLayoutEmail.isHelperTextEnabled = true
                    inputTextLayoutEmail.helperText = "Require*"
                    return@launch
                }

                !MyValidation.isValidEmail(email = email) -> {
                    _loginUserStatus.emit(Event(Resource.Error("E-mail is not valid")))
                    inputTextLayoutEmail.isHelperTextEnabled = true
                    inputTextLayoutEmail.helperText = "not valid"
                }
                password.isEmpty() -> {
                    _loginUserStatus.emit(Event(Resource.Error("Password is require")))
                    inputTextLayoutPassword.isHelperTextEnabled = true
                    inputTextLayoutPassword.helperText = "Require*"
                    return@launch
                }
                !MyValidation.validatePass(context, inputTextLayoutPassword) -> {
                    _loginUserStatus.emit(Event(Resource.Error(inputTextLayoutPassword.helperText.toString())))
                }
                else -> {
                    _loginUserStatus.emit(Event(Resource.Loading()))
                    val result = repository.loginUser(email, password)
                    _loginUserStatus.emit(Event(result))
                }
            }
        }

    }

    fun createAccount(
        inputTextLayoutUserName: TextInputLayout,
        inputTextLayoutEmail: TextInputLayout,
        inputTextLayoutPassword: TextInputLayout,
        imageUserUri: String
    ) {
        viewModelScope.launch(dispatcher) {
            val username = inputTextLayoutUserName.editText!!.text.toString()
            val email = inputTextLayoutEmail.editText!!.text.toString()
            val password = inputTextLayoutPassword.editText!!.text.toString()
            when {
                imageUserUri.isEmpty() -> {
                    _createAccountStatus.emit(Event(Resource.Error("Please Selected Your image profile")))
                    return@launch
                }
                username.isEmpty() -> {
                    _createAccountStatus.emit(Event(Resource.Error("Username is require")))
                    inputTextLayoutUserName.isHelperTextEnabled = true
                    inputTextLayoutUserName.helperText = "Require*"
                    return@launch
                }

                email.isEmpty() -> {
                    _createAccountStatus.emit(Event(Resource.Error("E-mail is require")))
                    inputTextLayoutEmail.isHelperTextEnabled = true
                    inputTextLayoutEmail.helperText = "Require*"
                    return@launch
                }

                !MyValidation.isValidEmail(email = email) -> {
                    _createAccountStatus.emit(Event(Resource.Error("E-mail is not valid")))
                    inputTextLayoutEmail.isHelperTextEnabled = true
                    inputTextLayoutEmail.helperText = "not valid"
                    return@launch
                }


                password.isEmpty() -> {
                    _createAccountStatus.emit(Event(Resource.Error("Password is require")))
                    inputTextLayoutPassword.isHelperTextEnabled = true
                    inputTextLayoutPassword.helperText = "Require*"
                    return@launch
                }
                !MyValidation.validatePass(context, inputTextLayoutPassword) -> {
                    _createAccountStatus.emit(Event(Resource.Error(inputTextLayoutPassword.helperText.toString())))
                    return@launch
                }
                else -> {
                    _createAccountStatus.emit(Event(Resource.Loading()))
                    val user=User(
                        username = username,
                        email = email,
                        password = password,
                        image = imageUserUri
                    )
                    val result = repository.createAccount(
                       user
                    )
                    _createAccountStatus.emit(Event(result))
                    return@launch
                }
            }
        }


    }


//
//    fun logout() {
//        viewModelScope.launch(dispatcher) {
//            _logoutStatus.emit(Event(Resource.Loading()))
//            val result = repository.logout()
//            _logoutStatus.emit(Event(result))
//        }
//    }
//
//    fun verifyEmail(inputTextLayoutEmail: TextInputLayout) {
//        viewModelScope.launch(dispatcher) {
//            val email = inputTextLayoutEmail.editText!!.text.toString()
//            when {
//                email.isEmpty() -> {
//                    _loginUserStatus.emit(Event(Resource.Error("E-mail is require")))
//                    inputTextLayoutEmail.isHelperTextEnabled = true
//                    inputTextLayoutEmail.helperText = "Require*"
//                }
//
//                !MyValidation.isValidEmail(email = email) -> {
//                    _loginUserStatus.emit(Event(Resource.Error("E-mail is not valid")))
//                    inputTextLayoutEmail.isHelperTextEnabled = true
//                    inputTextLayoutEmail.helperText = "not valid"
//                }
//                else -> {
//                    _verifyEmailStatus.emit(Event(Resource.Loading()))
//                    val result = repository.verifyEmail(email)
//                    _verifyEmailStatus.emit(Event(result))
//                }
//            }
//        }
//
//    }
//
//    fun verifyCode(email: String, codeEmail: PinView) {
//        viewModelScope.launch(dispatcher) {
//            val code = codeEmail.text.toString()
//            when {
//                code.isEmpty() -> {
//                    _verifyCodeStatus.emit(Event(Resource.Error("Code is require")))
//                }
//
//                code.length<4 -> {
//                    _verifyCodeStatus.emit(Event(Resource.Error("Code content 4 numbers")))
//                }
//                else -> {
//                    _verifyCodeStatus.emit(Event(Resource.Loading()))
//                    val result = repository.verifyCode(email,code)
//                    _verifyCodeStatus.emit(Event(result))
//                }
//            }
//        }
//
//    }
//
//    fun resetPassword(inputTextLayoutPassword: TextInputLayout, email: String, code: String) {
//           viewModelScope.launch {
//               val password=inputTextLayoutPassword.editText?.text.toString()
//               when{
//                   password.isEmpty() -> {
//                       _resetPasswordStatus.emit(Event(Resource.Error("Password is require")))
//                       inputTextLayoutPassword.isHelperTextEnabled = true
//                       inputTextLayoutPassword.helperText = "Require*"
//                   }
//                   !MyValidation.validatePass(context, inputTextLayoutPassword) -> {
//                       _resetPasswordStatus.emit(Event(Resource.Error(inputTextLayoutPassword.helperText.toString())))
//                   }
//                   else ->{
//                       _resetPasswordStatus.emit(Event(Resource.Loading()))
//                       val result = repository.resetPassword(password,email,code)
//                       _resetPasswordStatus.emit(Event(result))
//                   }
//               }
//           }
//    }
}