package com.marmatsan.dev.catalog_ui.screen.plant_screen.components

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import com.marmatsan.dev.core_domain.isNotNull
import com.marmatsan.dev.core_ui.components.illustration.Illustration
import com.marmatsan.dev.core_ui.components.illustration.IllustrationDesign
import com.marmatsan.dev.core_ui.components.twoiconbuttonsheader.TwoIconButtonsHeader
import com.marmatsan.dev.core_ui.theme.WaterMyPlantsTheme
import com.marmatsan.dev.core_ui.theme.padding
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil.CoilImage

@Composable
fun PlantScreenHeader(
    modifier: Modifier = Modifier,
    removePhotoAvailable: Boolean = false,
    aiButtonAvailable: Boolean = false,
    image: Uri? = null,
    onAddImage: (() -> Unit),
    onBackClick: (() -> Unit)? = null,
    onRemoveImage: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
    ) {
        if (image.isNotNull())
            CoilImage(
                modifier = Modifier.fillMaxSize(),
                imageModel = { image },
                imageOptions = ImageOptions(
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center
                )
            )
        else
            Illustration(
                modifier = Modifier.fillMaxSize(),
                illustrationDesign = IllustrationDesign.Four
            )
        HeaderContent(
            modifier = Modifier
                .padding(
                    all = padding.medium
                )
                .fillMaxSize(),
            removePhotoAvailable = removePhotoAvailable,
            aiButtonAvailable = aiButtonAvailable,
            image = image,
            onAddImage = onAddImage,
            onBackClick = onBackClick,
            onRemoveImage = onRemoveImage
        )
    }
}

@Composable
fun HeaderContent(
    modifier: Modifier = Modifier,
    removePhotoAvailable: Boolean = false,
    aiButtonAvailable: Boolean = false,
    onAddImage: (() -> Unit),
    image: Uri? = null,
    onBackClick: (() -> Unit)? = null,
    onRemoveImage: (() -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TwoIconButtonsHeader(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = padding.none),
            showSecondaryButton = removePhotoAvailable,
            onPrimaryIconButtonClick = onBackClick,
            onSecondaryIconButtonClick = onRemoveImage
        )
        PlantScreenActions(
            modifier = Modifier
                .wrapContentSize()
                .padding(all = padding.none),
            aiButtonAvailable = aiButtonAvailable,
            image = image,
            onAddImage = onAddImage,
        )
        PlantScreenActions(
            modifier = Modifier
                .wrapContentSize()
                .padding(all = padding.none),
            aiButtonAvailable = aiButtonAvailable,
            image = image,
            onAddImage = onAddImage,
        )
    }
}

@Preview(
    widthDp = 484,
    heightDp = 484
)
@Composable
private fun PlantScreenHeaderPreview() {
    WaterMyPlantsTheme {
        PlantScreenHeader(
            onAddImage = {}
        )
    }
}