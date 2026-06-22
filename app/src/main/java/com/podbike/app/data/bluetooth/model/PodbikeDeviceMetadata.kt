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

package com.podbike.app.data.bluetooth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class PodbikeDeviceMetadata(
    val fileName: String? = null,
    @SerialName("Product name")
    val productName: String,
    @SerialName("ReleaseID")
    val releaseId: String,
    @SerialName("ProductID")
    val productId: String,
    @SerialName("Frame number")
    val frameNumber: String,
    @SerialName("ECU Modules")
    val ecuModules: List<EcuModule>,
    @SerialName("AudioFiles")
    val audioFiles: List<AudioFile> = emptyList()
) {
    companion object {
        fun mock(): PodbikeDeviceMetadata = PodbikeDeviceMetadata(
            fileName = "device_metadata.json",
            productName = "Podbike FRIKAR",
            releaseId = "1.0.0",
            productId = "FRK-2024",
            frameNumber = "000000-F8-1-00-000",
            ecuModules = listOf(
                EcuModule(
                    boardName = "Main Control Board",
                    boardId = "MCB-001",
                    serialNumber = "SN123456789",
                    boardPosition = "Front",
                    fwVersion = "v1.2.3"
                ),
                EcuModule(
                    boardName = "Battery Management System",
                    boardId = "BMS-002",
                    serialNumber = "SN987654321",
                    boardPosition = "Rear",
                    fwVersion = "v2.3.4"
                ),
                EcuModule(
                    boardName = "Motor Controller",
                    boardId = "MTC-003",
                    serialNumber = "SN456789123",
                    boardPosition = "Rear",
                    fwVersion = "v3.4.5"
                ),
                EcuModule(
                    boardName = "Display Controller",
                    boardId = "DC-004",
                    serialNumber = "SN789123456",
                    boardPosition = "Front",
                    fwVersion = "v4.5.6"
                )
            ),
            audioFiles = listOf(
                AudioFile(filename = "startup_sound.mp3"),
                AudioFile(filename = "shutdown_sound.mp3")
            )
        )

        fun jsonMock(): PodbikeDeviceMetadata {
            val json = "{\n" +
                    "    \"Product name\": \"Frikar\",\n" +
                    "    \"ReleaseID\":    \"R1.0.0\",\n" +
                    "    \"ProductID\":    \"A294-1\",\n" +
                    "    \"Frame number\": \"000000-F8-1-00-000\",\n" +
                    "    \"ECU Modules\":  [{\n" +
                    "            \"Board name\":   \"Haarek\",\n" +
                    "            \"BoardID\":  \"E126-5\",\n" +
                    "            \"Serial number\":    \"6611800\",\n" +
                    "            \"Board position\":   \"Main Controller\",\n" +
                    "            \"FWVersion\":    \"R01-57\"\n" +
                    "        }, {\n" +
                    "            \"Board name\":   \"BLE\",\n" +
                    "            \"BoardID\":  \"E126-5\",\n" +
                    "            \"Serial number\":    \"6611800\",\n" +
                    "            \"Board position\":   \"Main BLE\",\n" +
                    "            \"FWVersion\":    \"R01-52\"\n" +
                    "        }, {\n" +
                    "            \"Board name\":   \"FENRIS\",\n" +
                    "            \"BoardID\":  \"E133-2\",\n" +
                    "            \"Serial number\":    \"6608467\",\n" +
                    "            \"Board position\":   \"Pedal Generator\",\n" +
                    "            \"FWVersion\":    \"R01-55\"\n" +
                    "        }, {\n" +
                    "            \"Board name\":   \"FENRIS\",\n" +
                    "            \"BoardID\":  \"E133-2\",\n" +
                    "            \"Serial number\":    \"6608549\",\n" +
                    "            \"Board position\":   \"Motor Left\",\n" +
                    "            \"FWVersion\":    \"R01-55\"\n" +
                    "        }, {\n" +
                    "            \"Board name\":   \"FENRIS\",\n" +
                    "            \"BoardID\":  \"E133-2\",\n" +
                    "            \"Serial number\":    \"6608476\",\n" +
                    "            \"Board position\":   \"Motor Right\",\n" +
                    "            \"FWVersion\":    \"R01-55\"\n" +
                    "        }, {\n" +
                    "            \"Board name\":   \"Baldr\",\n" +
                    "            \"BoardID\":  \"E131-1\",\n" +
                    "            \"Serial number\":    \"5110844\",\n" +
                    "            \"Board position\":   \"Front Left\",\n" +
                    "            \"FWVersion\":    \"R01-04\"\n" +
                    "        }, {\n" +
                    "            \"Board name\":   \"Baldr\",\n" +
                    "            \"BoardID\":  \"E131-1\",\n" +
                    "            \"Serial number\":    \"5110838\",\n" +
                    "            \"Board position\":   \"Front Right\",\n" +
                    "            \"FWVersion\":    \"R01-04\"\n" +
                    "        }, {\n" +
                    "            \"Board name\":   \"Baldr\",\n" +
                    "            \"BoardID\":  \"E131-1\",\n" +
                    "            \"Serial number\":    \"5110927\",\n" +
                    "            \"Board position\":   \"Rear Left\",\n" +
                    "            \"FWVersion\":    \"R01-04\"\n" +
                    "        }, {\n" +
                    "            \"Board name\":   \"Baldr\",\n" +
                    "            \"BoardID\":  \"E131-1\",\n" +
                    "            \"Serial number\":    \"5110918\",\n" +
                    "            \"Board position\":   \"Rear Right\",\n" +
                    "            \"FWVersion\":    \"R01-04\"\n" +
                    "        }]\n" +
                    "}"
            val metadata = Json.decodeFromString<PodbikeDeviceMetadata>(json)
            return metadata
        }
    }
}

@Serializable
data class EcuModule(
    @SerialName("Board name")
    val boardName: String,
    @SerialName("BoardID")
    val boardId: String,
    @SerialName("Serial number")
    val serialNumber: String,
    @SerialName("Board position")
    val boardPosition: String,
    @SerialName("FWVersion")
    val fwVersion: String
)

@Serializable
data class AudioFile(
    @SerialName("Filename")
    val filename: String
)
