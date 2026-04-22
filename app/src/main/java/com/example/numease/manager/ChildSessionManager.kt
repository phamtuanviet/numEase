package com.example.numease.manager


import com.example.numease.data.model.ChildProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChildSessionManager @Inject constructor() {

    private val _activeChild = MutableStateFlow<ChildProfile?>(null)
    val activeChild: StateFlow<ChildProfile?> = _activeChild.asStateFlow()

    fun setActiveChild(child: ChildProfile) {
        _activeChild.value = child
    }

    fun clearSession() {
        _activeChild.value = null
    }
}