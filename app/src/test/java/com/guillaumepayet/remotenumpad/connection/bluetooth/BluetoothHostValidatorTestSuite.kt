/*
 * Remote Numpad - a numpad application on Android for PCs lacking one.
 * Copyright (C) 2016-2020 Guillaume Payet
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.guillaumepayet.remotenumpad.connection.bluetooth

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

/**
 * Test suite for testing the [BluetoothHostValidator] class.
 *
 * @see BluetoothHostValidator
 */
class BluetoothHostValidatorTestSuite {

    companion object {

        private val INVALID_ADDRESSES = listOf(
                "",
                ":",
                ":::::",
                "0:0:0:0:0:0",
                "00:00:00:00:00",
                "00:00:00:00:00:",
                "00.00.00.00.00.00",
                "00-00-00-00-00-00",
                "00:00:00:00:00:00:00",
                "00:00:FG:00:00:00",
                "00:00:00:00:00:100",
                "00:00:00:00:aa:00"
        )

        private val VALID_ADDRESSES = listOf(
                "00:00:00:00:00:00",
                "99:99:99:99:99:99",
                "AA:AA:AA:AA:AA:AA",
                "FF:FF:FF:FF:FF:FF"
        )
    }


    @Test
    fun requestValidationOnAnInvalidAddress_ValidatorReturnsFalse() {
        // Given a validator,...
        val validator = BluetoothHostValidator()

        // ...when an invalid address is provided to the validator,...
        val actual = INVALID_ADDRESSES.map { validator.isHostValid(it) }

        // ...then the validator returns false
        actual.forEachIndexed { i, it ->
            assertThat("'${INVALID_ADDRESSES[i]}' should be invalid", it, `is`(false))
        }
    }

    @Test
    fun requestValidationOnAValidAddress_ValidatorReturnsTrue() {
        // Given a validator,...
        val validator = BluetoothHostValidator()

        // ...when a valid adress is provided to the validator,...
        val actual = VALID_ADDRESSES.map { validator.isHostValid(it) }

        // ...then the validator returns true
        actual.forEachIndexed { i, it ->
            assertThat("'${VALID_ADDRESSES[i]}' should be valid", it, `is`(true))
        }
    }
}