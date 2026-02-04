package com.marmatsan.onboarding_ui.figma

import androidx.compose.runtime.Composable
import com.figma.code.connect.FigmaConnect
import com.marmatsan.onboarding_ui.component.Background

@FigmaConnect(
    url = "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants--New-?node-id=62803%3A264"
)
class BackgroundDoc {
    @Composable
    fun Example() {
        Background(
            modifier = androidx.compose.ui.Modifier
        )
    }
}