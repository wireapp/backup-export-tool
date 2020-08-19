package com.wire.backups.exports.utils

import com.goterl.lazycode.lazysodium.utils.LibraryLoader

/**
 * Strategy used for loading Libsodium libraries.
 */
internal val LIBSODIUM_BINARIES_LOADING = LibraryLoader.Mode.PREFER_SYSTEM
