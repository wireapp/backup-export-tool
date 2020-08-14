package pw.forst.wire.backups.utils

import kotlin.reflect.full.primaryConstructor

inline fun <reified T : Any> primaryConstructorParameters() =
    T::class.primaryConstructor?.parameters?.associateBy { it.name }
