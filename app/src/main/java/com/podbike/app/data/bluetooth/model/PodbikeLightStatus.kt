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

data class PodbikeLightStatus(
    val lowBeam: Boolean = false,
    val highBeam: Boolean = false,
    val rearLight: Boolean = false,
    val brakeLight: Boolean = false,
    val indicatorLeft: Boolean = false,
    val indicatorRight: Boolean = false,
    val reverseLight: Boolean = false,
    val runningLight: Boolean = false
)
