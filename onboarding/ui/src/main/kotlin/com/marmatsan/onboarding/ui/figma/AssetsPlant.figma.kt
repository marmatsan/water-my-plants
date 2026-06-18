package com.marmatsan.onboarding.ui.figma

import androidx.compose.runtime.Composable
import com.figma.code.connect.Figma
import com.figma.code.connect.FigmaConnect
import com.figma.code.connect.FigmaProperty
import com.figma.code.connect.FigmaType
import com.marmatsan.onboarding.ui.component.AssetsPlant
import com.marmatsan.onboarding.ui.component.AssetsPlantVariant

@FigmaConnect(
    url = "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants--New-?node-id=62873%3A2534"
)
class AssetsPlantDoc {
    @FigmaProperty(
        type = FigmaType.Enum,
        value = "plant"
    )
    val variant: AssetsPlantVariant = Figma.mapping(
        "plant1" to AssetsPlantVariant.Plant1,
        "plant2" to AssetsPlantVariant.Plant2,
        "plant3" to AssetsPlantVariant.Plant3,
        "plant4" to AssetsPlantVariant.Plant4
    )

    @Composable
    fun Example() {
        AssetsPlant(
            variant = variant
        )
    }
}