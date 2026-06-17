package com.marmatsan.figmaCatalogChecks.data

import com.marmatsan.figmaCatalogChecks.domain.FigmaNodeReference
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

internal class FigmaNodeUrlTest : FunSpec({

    test("parse returns file key and normalized node id from Figma design URL") {
        // GIVEN
        val url = "https://www.figma.com/design/YBZXsd8oyGLbcI2KWxJvRK/Water-My-Plants?node-id=63075-591&t=gxgxBWEWgZjRldAX-4"

        // WHEN
        val result = FigmaNodeUrl.parse(url)

        // THEN
        result shouldBe FigmaNodeReference(
            fileKey = "YBZXsd8oyGLbcI2KWxJvRK",
            nodeId = "63075:591"
        )
    }
})
