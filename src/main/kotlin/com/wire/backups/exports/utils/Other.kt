package com.wire.backups.exports.utils

import mu.KLogger
import mu.KLogging
import kotlin.reflect.full.primaryConstructor

inline fun <reified T : Any> primaryConstructorParameters() =
    T::class.primaryConstructor?.parameters?.associateBy { it.name }


val transactionsLogger: KLogger
    get() = KLogging().logger("com.wire.backups.exports.db")
