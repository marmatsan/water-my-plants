package com.marmatsan.onboarding_ui.figma

import androidx.compose.runtime.Composable
import com.figma.code.connect.Figma
import com.figma.code.connect.FigmaConnect
import com.figma.code.connect.FigmaProperty
import com.figma.code.connect.FigmaType
import com.marmatsan.onboarding_ui.component.AssetsShape
import com.marmatsan.onboarding_ui.component.AssetsShapeVariant

@FigmaConnect(
    url = "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants--New-?node-id=62873%3A2721"
)
class AssetsShapeDoc {
    @FigmaProperty(
        type = FigmaType.Enum,
        value = "shape"
    )
    val variant: AssetsShapeVariant = Figma.mapping(
        "shape1" to AssetsShapeVariant.Shape1,
        "shape2" to AssetsShapeVariant.Shape2,
        "shape3" to AssetsShapeVariant.Shape3
    )

    @Composable
    fun Example() {
        AssetsShape(
            variant = variant
        )
    }
}