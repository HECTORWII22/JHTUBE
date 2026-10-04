package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.PureBlack

// Official Branding Colors from User Logo
val RniBlue = Color(0xFF1B3F94)
val RniNavyText = Color(0xFF163E93)
val RniYouTubeRed = Color(0xFFFF0000)
val RniParaGray = Color(0xFF70757A)

/**
 * Exact replica of the circular badge logo ("YouTube PARA").
 */
@Composable
fun RniBadgeLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val scaleFactor = size / 100.dp

    Box(
        modifier = modifier
            .size(size)
            .testTag("rni_badge_logo")
    ) {
        // Main Blue Circle
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(RniBlue)
        ) {
            Column(
                modifier = Modifier
                    .matchParentSize()
                    .padding(top = (8 * scaleFactor).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Red YouTube Play Card
                Box(
                    modifier = Modifier
                        .width((46 * scaleFactor).dp)
                        .height((30 * scaleFactor).dp)
                        .clip(RoundedCornerShape((9 * scaleFactor).dp))
                        .background(RniYouTubeRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier.size((20 * scaleFactor).dp)
                    )
                }

                Spacer(modifier = Modifier.height((2 * scaleFactor).dp))

                // YouTube Text
                Text(
                    text = "YouTube",
                    color = Color(0xFF181818),
                    fontSize = (11 * scaleFactor).sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        // Bottom Left Cutout Arc with "PARA"
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .size((38 * scaleFactor).dp)
                .clip(RoundedCornerShape(topEnd = (24 * scaleFactor).dp))
                .background(Color.White)
                .padding(start = (4 * scaleFactor).dp, bottom = (4 * scaleFactor).dp),
            contentAlignment = Alignment.BottomStart
        ) {
            Text(
                text = "PARA",
                color = RniParaGray,
                fontSize = (8 * scaleFactor).sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Full Horizontal Banner Logo:
 * Badge on left + "RED NACIONAL INTERNET 2.0" on right.
 */
@Composable
fun RniBannerLogo(
    modifier: Modifier = Modifier,
    badgeSize: Dp = 44.dp,
    textColor: Color = Color.White
) {
    Row(
        modifier = modifier.testTag("rni_banner_logo"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RniBadgeLogo(size = badgeSize)

        Spacer(modifier = Modifier.width(10.dp))

        Column(verticalArrangement = Arrangement.Center) {
            Text(
                text = "RED NACIONAL",
                color = if (textColor == Color.White) Color.White else RniNavyText,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                lineHeight = 16.sp
            )
            Text(
                text = "INTERNET 2.0",
                color = if (textColor == Color.White) NeonCyan else RniBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp,
                lineHeight = 16.sp
            )
        }
    }
}
