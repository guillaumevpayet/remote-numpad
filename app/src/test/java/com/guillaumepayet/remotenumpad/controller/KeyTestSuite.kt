/*
 * Remote Numpad - a numpad application on Android for PCs lacking one.
 * Copyright (C) 2016-2022 Guillaume Payet
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

package com.guillaumepayet.remotenumpad.controller

import android.content.Context
import android.content.res.TypedArray
import androidx.appcompat.widget.AppCompatButton
import android.util.AttributeSet
import com.guillaumepayet.remotenumpad.R
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.BDDMockito.*
import org.mockito.Mock
import org.mockito.Mockito.times
import org.powermock.api.support.membermodification.MemberMatcher.constructor
import org.powermock.api.support.membermodification.MemberModifier.suppress
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner

/**
 * Test suite for testing the [Key] widget.
 *
 * @see Key
 */
@RunWith(PowerMockRunner::class)
@PrepareForTest(Key::class)
class KeyTestSuite {

    companion object {

        private const val KEY_VALUE = "TEST"
    }


    @Mock
    private val mockContext: Context? = null

    @Mock
    private val mockAttributeSet: AttributeSet? = null

    @Mock
    private val mockTypedArray: TypedArray? = null


    @Before
    fun setupMocks() {
        // This prevents the key's parent class to throw 'not mocked' exceptions.
        suppress(constructor(AppCompatButton::class.java, Context::class.java, AttributeSet::class.java))

        // Given that a TypedArray object can be obtained from the Context and...
        given(mockContext?.obtainStyledAttributes(mockAttributeSet, R.styleable.Key))
                .willReturn(mockTypedArray)

        // ...that TypedArray contains the value of the Key.
        given(mockTypedArray?.getString(R.styleable.Key_value)).willReturn(KEY_VALUE)
    }


    @Test
    fun constructKeyWithContextAndAttributeSet_KeyRetrievedItsValue() {
        // When a mock Context object and a mock AttributeSet object injected into a Key object...
        val key = Key(mockContext!!, mockAttributeSet!!)

        // ...then the key should be able to retrieve its own value.
        assertThat(key.value, `is`(KEY_VALUE))
    }

    @Test
    fun constructKeyWithContextAndAttributeSet_AttributesAreRecycledOnce() {
        // When a mock Context object and a mock AttributeSet object injected into a Key object...
        Key(mockContext!!, mockAttributeSet!!)

        // ...then the attributes are used and recycled only once.
        then(mockTypedArray).should(times(1))?.recycle()
    }
}