package com.codetecuhtli.wesper.core

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import com.codetecuhtli.wesper.ui.components.Board
import com.codetecuhtli.wesper.ui.components.GameScreen
import com.codetecuhtli.wesper.ui.components.ItemSquare
import com.codetecuhtli.wesper.ui.theme.SquareGameTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SquareGameTheme {
                GameScreen()
            }
        }
    }
}