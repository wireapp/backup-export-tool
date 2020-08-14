package pw.forst.wire.backups.ios

import java.nio.ByteBuffer
import java.util.UUID


internal fun ByteArray.toUuid(): UUID =
    ByteBuffer.wrap(this).let {
        UUID(it.long, it.long)
    }
