package com.marmatsan.onboarding.ui.figma

import androidx.compose.runtime.Composable
import com.figma.code.connect.Figma
import com.figma.code.connect.FigmaConnect
import com.figma.code.connect.FigmaProperty
import com.figma.code.connect.FigmaType
import com.marmatsan.onboarding.ui.component.AssetsDots
import com.marmatsan.onboarding.ui.component.AssetsDotsVariant

@FigmaConnect(
    url = "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants--New-?node-id=62873%3A2745"
)
class AssetsDotsDoc {
    @FigmaProperty(
        type = FigmaType.Enum,
        value = "dots"
    )
    val variant: AssetsDotsVariant = Figma.mapping(
        "dots1" to AssetsDotsVariant.Dots1,
        "dots2" to AssetsDotsVariant.Dots2
    )

    @Composable
    fun Example() {
        AssetsDots(
            variant = variant
        )
    }
}