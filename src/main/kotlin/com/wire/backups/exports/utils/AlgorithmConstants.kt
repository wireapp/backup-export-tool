package com.wire.backups.exports.utils

// taken from rust version of libsodium
// https://docs.rs/libsodium-sys/0.2.6/src/libsodium_sys/sodium_bindings.rs.html


internal const val crypto_pwhash_argon2i_ALG_ARGON2I13 = 1

internal const val crypto_pwhash_argon2i_MEMLIMIT_INTERACTIVE = 33554432
internal const val crypto_pwhash_argon2i_OPSLIMIT_INTERACTIVE = 4

internal const val crypto_pwhash_argon2i_MEMLIMIT_MODERATE = 134217728
internal const val crypto_pwhash_argon2i_OPSLIMIT_MODERATE = 6


// values from Android
internal const val crypto_pwhash_argon2d_MEMLIMIT_INTERACTIVE = 67108864
internal const val crypto_pwhash_argon2d_OPSLIMIT_INTERACTIVE = 2

internal const val crypto_pwhash_argon2d_ALG_ARGON2D13 = 2

// the values Android is using, might change with different libsodium
internal const val crypto_pwhash_alg_default = crypto_pwhash_argon2d_ALG_ARGON2D13
internal const val crypto_pwhash_MEMLIMIT_INTERACTIVE =
    crypto_pwhash_argon2d_MEMLIMIT_INTERACTIVE
internal const val crypto_pwhash_OPSLIMIT_INTERACTIVE =
    crypto_pwhash_argon2d_OPSLIMIT_INTERACTIVE


internal const val crypto_aead_chacha20poly1305_keybytes = 32
internal const val crypto_aead_xchacha20poly1305_ietf_abytes = 16

