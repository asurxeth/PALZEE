package com.finrein.pals.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.finrein.pals.R
import com.finrein.pals.presentation.theme.*

@Composable
fun PalGroupGridScreen(
    isDark: Boolean,
    palTextLogoColor: Color,
    accentColor: Color,
    customAvatarUriString: String?,
    isLoadingPals: Boolean,
    mutedTextColor: Color,
    createdPals: List<PalItem>,
    rotationAngle: Float,
    capturedVlogsPaths: List<String>,
    currentPlayingIndex: Int,
    capturedVlogsCaptions: List<String>,
    capturedVlogsTimes: List<String>,
    vlogPlaybackProgress: Float,
    vlogExoPlayer: androidx.media3.exoplayer.ExoPlayer,
    textColor: Color,
    allPalsMembers: Map<String, List<String>>,
    firstName: String,
    allPalsSubmissions: Map<String, List<SubmissionDbItem>>,
    currentUserId: String,
    circleNumBg: Color,
    circleNumText: Color,
    onPlusClick: () -> Unit,
    onProfileClick: () -> Unit,
    onPalClick: (PalItem) -> Unit,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nonVlogGroups = remember(createdPals) {
        createdPals.filter { !it.isVlog }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF0A0A0A) else Color(0xFFF9F9F9))
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // A. Header Row (Logo, Plus, Profile Avatar)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PAL",
                        fontFamily = OwnglyphFontFamily,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = palTextLogoColor
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Button 1: Plus (+)
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF161616) else Color(0xFFEBEBEB))
                                .border(1.dp, if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f), CircleShape)
                                .clickable { onPlusClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add",
                                tint = if (isDark) Color.White else Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Button 2: User Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(CircleShape)
                                .border(1.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.15f), CircleShape)
                                .clickable { onProfileClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            if (customAvatarUriString != null) {
                                UriImage(
                                    uriString = customAvatarUriString,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(accentColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_smiley_avatar),
                                        contentDescription = "Profile Avatar",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // B. Rotate to capture
            if (createdPals.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(accentColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_smiley_avatar),
                                contentDescription = null,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .rotate(rotationAngle)
                            )
                        }
                        Text(
                            text = "rotate to capture",
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            color = accentColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // C. Vlog Card
            val vlogPal = createdPals.firstOrNull { it.isVlog }
            if (vlogPal != null) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    if (capturedVlogsPaths.isNotEmpty()) {
                        val activeIndex = currentPlayingIndex.coerceIn(0, capturedVlogsPaths.lastIndex.coerceAtLeast(0))
                        val currentCaption = capturedVlogsCaptions.getOrNull(activeIndex) ?: ""
                        val currentTime = capturedVlogsTimes.getOrNull(activeIndex) ?: ""

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color.Black)
                                .clickable { onPalClick(vlogPal) }
                        ) {
                            androidx.compose.ui.viewinterop.AndroidView(
                                factory = { ctx ->
                                    val view = android.view.LayoutInflater.from(ctx)
                                        .inflate(R.layout.player_view_texture, null) as androidx.media3.ui.PlayerView
                                    view.apply {
                                        player = vlogExoPlayer
                                        useController = false
                                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL

                                        vlogExoPlayer.addListener(object : androidx.media3.common.Player.Listener {
                                            override fun onVideoSizeChanged(videoSize: androidx.media3.common.VideoSize) {
                                                super.onVideoSizeChanged(videoSize)
                                                val textureView = getVideoSurfaceView() as? android.view.TextureView ?: return
                                                val containerWidth = width.toFloat()
                                                val containerHeight = height.toFloat()
                                                if (containerWidth > 0f && containerHeight > 0f) {
                                                    if (videoSize.height > videoSize.width) {
                                                        val scaleX = containerHeight / containerWidth
                                                        val scaleY = containerWidth / containerHeight
                                                        textureView.scaleX = scaleX
                                                        textureView.scaleY = scaleY
                                                        textureView.rotation = 270f
                                                    } else {
                                                        textureView.scaleX = 1.0f
                                                        textureView.scaleY = 1.0f
                                                        textureView.rotation = 0f
                                                    }
                                                }
                                            }
                                        })
                                    }
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            Text(
                                text = "vlog",
                                fontFamily = BricolageVariableFontFamily,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .padding(start = 12.dp),
                                style = TextStyle(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                        blurRadius = 3f
                                    )
                                )
                            )

                            Text(
                                text = currentTime,
                                fontFamily = RobotoFontFamily,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color.White,
                                modifier = Modifier
                                    .align(Alignment.CenterEnd)
                                    .padding(end = 12.dp),
                                style = TextStyle(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = Color.Black.copy(alpha = 0.5f),
                                        offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                        blurRadius = 3f
                                    )
                                )
                            )

                            if (currentCaption.isNotEmpty()) {
                                Text(
                                    text = currentCaption,
                                    fontFamily = FontFamily.SansSerif,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Normal,
                                    color = Color.White,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(horizontal = 48.dp),
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(
                                        shadow = androidx.compose.ui.graphics.Shadow(
                                            color = Color.Black.copy(alpha = 0.5f),
                                            offset = androidx.compose.ui.geometry.Offset(1f, 1f),
                                            blurRadius = 3f
                                        )
                                    )
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                capturedVlogsPaths.forEachIndexed { idx, _ ->
                                    val isActive = idx == activeIndex
                                    val isCompleted = idx < activeIndex

                                    if (isCompleted) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(Color.White)
                                        )
                                    } else if (isActive) {
                                        Box(
                                            modifier = Modifier
                                                .width(16.dp)
                                                .height(2.dp)
                                                .clip(RoundedCornerShape(1.dp))
                                                .background(Color.White.copy(alpha = 0.4f)),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxHeight()
                                                    .fillMaxWidth(vlogPlaybackProgress)
                                                    .background(Color.White)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .width(16.dp)
                                                .height(2.dp)
                                                .clip(RoundedCornerShape(1.dp))
                                                .background(Color.White.copy(alpha = 0.4f))
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        GlassmorphicCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onPalClick(vlogPal) },
                            borderRadius = 28.dp,
                            isDark = isDark,
                            gradientColors = if (isDark) listOf(Color.Black, Color.Black) else listOf(Color(0xFFFFFFFF), Color(0xFFFFFFFF)),
                            borderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = vlogPal.name,
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                    Text(
                                        text = buildAnnotatedString {
                                            append("your space. each day runs ")
                                            withStyle(androidx.compose.ui.text.SpanStyle(fontFamily = RobotoFontFamily)) {
                                                append("4am")
                                            }
                                            append(" to ")
                                            withStyle(androidx.compose.ui.text.SpanStyle(fontFamily = RobotoFontFamily)) {
                                                append("4am")
                                            }
                                            append(".")
                                        },
                                        fontFamily = FontFamily.SansSerif,
                                        fontSize = 11.sp,
                                        color = mutedTextColor,
                                        letterSpacing = (-0.2).sp
                                    )
                                }
                                Image(
                                    painter = painterResource(id = R.drawable.dm_star_4),
                                    contentDescription = "Star sticker",
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }
                    }
                }
            }

            // D. Group Cards
            items(nonVlogGroups, span = { GridItemSpan(maxLineSpan) }) { group ->
                PalGroupCard(
                    pal = group,
                    allPalsMembers = allPalsMembers,
                    allPalsSubmissions = allPalsSubmissions,
                    isDark = isDark,
                    accentColor = accentColor,
                    palTextLogoColor = palTextLogoColor,
                    textColor = textColor,
                    mutedTextColor = mutedTextColor,
                    currentUserId = currentUserId,
                    firstName = firstName,
                    onClick = { onPalClick(group) },
                    onCameraClick = onCameraClick
                )
            }

            // E. Onboarding Steps (shown if no group spaces exist)
            if (createdPals.none { !it.isVlog }) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(modifier = Modifier.height(6.dp))

                        val isVlogSent = capturedVlogsPaths.isNotEmpty()
                        Text(
                            text = "your day, side by side.",
                            fontFamily = OwnglyphFontFamily,
                            fontSize = 16.sp,
                            color = textColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = if (isVlogSent) 0.dp else 12.dp)
                                .offset(y = (-10).dp),
                            textAlign = if (isVlogSent) TextAlign.Center else TextAlign.Start
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = (-18).dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(circleNumBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "1",
                                        fontFamily = RobotoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = circleNumText
                                    )
                                }

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "tap + to start",
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        color = textColor
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        GlassmorphicCard(
                                            modifier = Modifier,
                                            borderRadius = 18.dp,
                                            isDark = isDark,
                                            gradientColors = if (isDark) {
                                                listOf(Color(0xFF262626), Color(0xFF262626))
                                            } else {
                                                listOf(Color(0xFFF2F2F2), Color(0xFFF2F2F2))
                                            },
                                            borderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                                    .clickable { onPlusClick() },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "create pal",
                                                    fontFamily = BricolageVariableFontFamily,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = textColor
                                                )
                                            }
                                        }
                                        Text(
                                            text = "(new group)",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.SansSerif,
                                            color = mutedTextColor
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        GlassmorphicCard(
                                            modifier = Modifier,
                                            borderRadius = 18.dp,
                                            isDark = isDark,
                                            gradientColors = if (isDark) {
                                                listOf(Color(0xFF262626), Color(0xFF262626))
                                            } else {
                                                listOf(Color(0xFFF2F2F2), Color(0xFFF2F2F2))
                                            },
                                            borderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                                    .clickable { onPlusClick() },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "join pal",
                                                    fontFamily = BricolageVariableFontFamily,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = textColor
                                                )
                                            }
                                        }
                                        Text(
                                            text = "(with a code)",
                                            fontSize = 11.sp,
                                            fontFamily = FontFamily.SansSerif,
                                            color = mutedTextColor
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(circleNumBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "2",
                                        fontFamily = RobotoFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = circleNumText
                                    )
                                }

                                Column(
                                    verticalArrangement = Arrangement.spacedBy(1.dp)
                                ) {
                                    Text(
                                        text = buildAnnotatedString {
                                            append("add a ")
                                            withStyle(androidx.compose.ui.text.SpanStyle(fontFamily = RobotoFontFamily)) {
                                                append("2s")
                                            }
                                            append(" clip every hour.")
                                        },
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        color = textColor
                                    )
                                    Text(
                                        text = "see everyone's day come together.",
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        color = textColor
                                    )
                                    Text(
                                        text = "solo pals don't have limits.",
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.SansSerif,
                                        color = textColor
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp)
                                .align(Alignment.CenterHorizontally)
                                .offset(y = (-15).dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(
                                    id = if (isDark) R.drawable.blob_dark else R.drawable.blob_light
                                ),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.FillBounds
                            )

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "day resets at 4AM.",
                                    fontFamily = OwnglyphFontFamily,
                                    fontSize = 14.sp,
                                    color = textColor,
                                    textAlign = TextAlign.Center
                                )
                                Text(
                                    text = "find past days in the archive.",
                                    fontFamily = OwnglyphFontFamily,
                                    fontSize = 14.sp,
                                    color = textColor,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Image(
                            painter = painterResource(id = R.drawable.ufo_turtle),
                            contentDescription = "UFO and Turtle Doodle",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .align(Alignment.CenterHorizontally)
                                .offset(y = (-19).dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                val bottomSpacerHeight = if (capturedVlogsPaths.isNotEmpty()) 80.dp else 10.dp
                Spacer(modifier = Modifier.height(bottomSpacerHeight))
            }
        }
    }
}

@Composable
fun PalGroupCard(
    pal: PalItem,
    allPalsMembers: Map<String, List<String>>,
    allPalsSubmissions: Map<String, List<SubmissionDbItem>>,
    isDark: Boolean,
    accentColor: Color,
    palTextLogoColor: Color,
    textColor: Color,
    mutedTextColor: Color,
    currentUserId: String,
    firstName: String,
    onClick: () -> Unit,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val capitalizedGroupName = pal.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    val members = allPalsMembers[pal.code] ?: emptyList()
    val subtitleText = if (members.size <= 1) {
        "only you"
    } else {
        "${members.size} members"
    }

    GlassmorphicCard(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp)),
        borderRadius = 28.dp,
        isDark = isDark,
        gradientColors = if (isDark) listOf(Color.Black, Color.Black) else listOf(Color(0xFFFFFFFF), Color(0xFFFFFFFF)),
        borderColor = if (isDark) Color.White.copy(alpha = 0.08f) else Color.Black.copy(alpha = 0.08f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left portion: group name & subtitle
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = capitalizedGroupName,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    maxLines = 1
                )
                Text(
                    text = subtitleText,
                    fontFamily = FontFamily.SansSerif,
                    fontSize = 11.sp,
                    color = mutedTextColor,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right portion: Smileys & Camera Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val groupSubs = allPalsSubmissions[pal.code] ?: emptyList()
                GroupMembersSmileysRow(
                    members = members,
                    submissions = groupSubs,
                    isDark = isDark,
                    accentColor = accentColor,
                    palTextLogoColor = palTextLogoColor,
                    currentUserId = currentUserId,
                    userFirstName = firstName,
                    smileySize = 22.dp,
                    innerSize = 14.dp,
                    unlitAlpha = 1.0f
                )

                // Compact Camera Button
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF1C1C1E) else Color(0xFFE5E5EA))
                        .clickable { onCameraClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.camera_list_icon),
                        contentDescription = "Camera",
                        modifier = Modifier.size(16.dp),
                        colorFilter = ColorFilter.tint(if (isDark) Color.White else Color.Black)
                    )
                }
            }
        }
    }
}

