/*
 * Remote Numpad - a numpad application on Android for PCs lacking one.
 * Copyright (C) 2016-2018 Guillaume Payet
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
import androidx.appcompat.widget.AppCompatButton
import android.util.AttributeSet
import com.guillaumepayet.remotenumpad.R

/**
 * A widget to be used as a key for the numpad.
 *
 * @constructor This constructor is called when the key is constructed from the XML layout.\
 * The key is constructed and its value is retrieved from the XML.
 *
 * @param context The context of the application
 * @param attrs   The attributes from the XML
 *
 * Created by guillaume on 12/25/17.
 */
class Key(context: Context, attrs: AttributeSet) : AppCompatButton(context, attrs) {

    /**
     * The value of the key to be sent to the computer.
     */
    var value: String = ""
        private set

    init {
        loadValue(context, attrs)
    }

    /**
     * Load the value of the key from the attribute set.
     * The Context object is used to process the AttributeSet object and produce a TypedArray
     * object. This object contains a subset of the attributes which is easier to manipulate. The
     * value of the key is then retrieved from it and stored.
     *
     * @param context Context of the application
     * @param attrs   The attributes from the XML
     */
    private fun loadValue(context: Context, attrs: AttributeSet) {
        // Produce a TypedArray of the custom attributes of Key (only 1 in this app)
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.Key)

        try {
            // Retrieve the value of the key and store it
            value = attributes.getString(R.styleable.Key_value)!!
        } finally {
            // Recycle the attributes no matter what
            attributes.recycle()
        }
    }
}