package com.captions.textdraw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.captions.textdraw.tools.TextDrawTool
import com.captions.textdraw.ui.theme.TextDrawTheme
import com.captions.textdraw.ui.widgets.TextDrawComposable

class MainActivity : ComponentActivity() {

    private val textDrawTool: TextDrawTool = TextDrawTool()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TextDrawTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TextDrawComposable(tool = textDrawTool)
                }
            }
        }
    }
}