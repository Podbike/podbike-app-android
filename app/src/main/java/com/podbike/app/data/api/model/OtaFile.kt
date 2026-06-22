/*
 * Copyright (C) 2026 Phal AS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.podbike.app.data.api.model

const val FRIKAR_TRANSFER_CONFIG_FILE_NAME = "frikar.json"

enum class OtaFileType {
    FRIKAR_TRANSFER_CONFIG, // frikar.json
    FIRMWARE,
    AUDIO;

    companion object {
        fun fromFileName(fileName: String): OtaFileType {
            return when {
                fileName.endsWith(".wav") -> AUDIO
                fileName == FRIKAR_TRANSFER_CONFIG_FILE_NAME -> FRIKAR_TRANSFER_CONFIG
                else -> FIRMWARE
            }
        }
    }
}

data class OtaFile(
    val type: OtaFileType,
    val name: String,
    val bytes: ByteArray?,
)
