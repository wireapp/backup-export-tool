package com.wire.backups.exports.utils

import kotlin.reflect.full.primaryConstructor

inline fun <reified T : Any> primaryConstructorParameters() =
    T::class.primaryConstructor?.parameters?.associateBy { it.name }
