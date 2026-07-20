package com.marmatsan.onboarding.ui.figma

import androidx.compose.runtime.Composable
import com.figma.code.connect.Figma
import com.figma.code.connect.FigmaConnect
import com.figma.code.connect.FigmaProperty
import com.figma.code.connect.FigmaType
import com.marmatsan.onboarding.ui.component.AssetsLeaf
import com.marmatsan.onboarding.ui.component.AssetsLeafVariant

@FigmaConnect(
    url = "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants--New-?node-id=62873%3A2777",
)
class AssetsLeafDoc {
    @FigmaProperty(
        type = FigmaType.Enum,
        value = "leaf",
    )
    val variant: AssetsLeafVariant =
        Figma.mapping(
            "leaf1" to AssetsLeafVariant.Leaf1,
            "leaf2" to AssetsLeafVariant.Leaf2,
            "leaf3" to AssetsLeafVariant.Leaf3,
            "leaf4" to AssetsLeafVariant.Leaf4,
            "leaf5" to AssetsLeafVariant.Leaf5,
        )

    @Composable
    fun Example() {
        AssetsLeaf(
            variant = variant,
        )
    }
}
