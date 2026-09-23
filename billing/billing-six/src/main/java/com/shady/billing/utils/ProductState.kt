package com.ai.chatmate.billing.utils

sealed interface ProductState {
    object Loading : ProductState
    object Available : ProductState
    object Empty : ProductState
}