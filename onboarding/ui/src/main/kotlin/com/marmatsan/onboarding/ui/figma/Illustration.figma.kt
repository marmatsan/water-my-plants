package com.marmatsan.onboarding.ui.figma

import androidx.compose.runtime.Composable
import com.figma.code.connect.Figma
import com.figma.code.connect.FigmaConnect
import com.figma.code.connect.FigmaProperty
import com.figma.code.connect.FigmaType
import com.marmatsan.onboarding.ui.component.Illustration
import com.marmatsan.onboarding.ui.component.IllustrationVariant

@FigmaConnect(
    url = "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants--New-?node-id=62815%3A331",
)
class IllustrationDoc {
    @FigmaProperty(
        type = FigmaType.Enum,
        value = "Illustration",
    )
    val variant: IllustrationVariant =
        Figma.mapping(
            "illustration1" to IllustrationVariant.Illustration1,
            "illustration2" to IllustrationVariant.Illustration2,
            "illustration3" to IllustrationVariant.Illustration3,
        )

    @Composable
    fun Example() {
        Illustration(
            variant = variant,
        )
    }
}
