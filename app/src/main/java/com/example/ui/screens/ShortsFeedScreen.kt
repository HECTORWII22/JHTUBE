package com.example.ui.screens

import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ModeComment
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem as Media3Item
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.example.data.model.MediaItem
import com.example.ui.theme.*

@Composable
fun ShortsFeedScreen(
    shortsList: List<MediaItem> = listOf(
        MediaItem(id = "zLDlWNDmR8k", title = "Epic Cinematic Moment #Shorts", author = "JHTube Gaming", streamUrl = "https://...", downloadUrl = "https://..."),
        MediaItem(id = "dQw4w9WgXcQ", title = "Never gonna give you up (Short Edition)", author = "RickAstleyVEVO", streamUrl = "https://...", downloadUrl = "https://..."),
        MediaItem(id = "jNlAc92xgfg", title = "Me at the zoo (Shorts HD)", author = "Jawed", streamUrl = "https://...", downloadUrl = "https://...")
    )
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { shortsList.size })

    VerticalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) { page ->
        val shortItem = shortsList[page]
        val isCurrentPage = pagerState.currentPage == page

        var isLiked by remember { mutableStateOf(false) }
        var likeCount by remember { mutableStateOf(1250 + page * 43) }

        val exoPlayer = remember(shortItem.id) {
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setUserAgent("Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Mobile Safari/537.36")
                .setAllowCrossProtocolRedirects(true)

            val mirrorUrl = com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS[
                Math.abs(shortItem.id.hashCode()) % com.example.data.repository.MediaRepository.RELIABLE_VIDEO_MIRRORS.size
            ]
            val effectiveUrl = if (shortItem.streamUrl.isNotBlank() && !shortItem.streamUrl.contains("workers.dev")) {
                shortItem.streamUrl
            } else {
                mirrorUrl
            }

            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Media3Item.fromUri(Uri.parse(effectiveUrl)))

            ExoPlayer.Builder(context).build().apply {
                setMediaSource(mediaSource)
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 1.0f
                addListener(object : Player.Listener {
                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        val fallbackSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                            .createMediaSource(Media3Item.fromUri(Uri.parse(mirrorUrl)))
                        setMediaSource(fallbackSource)
                        prepare()
                        play()
                    }
                })
                prepare()
            }
        }

        DisposableEffect(isCurrentPage) {
            if (isCurrentPage) {
                exoPlayer.playWhenReady = true
            } else {
                exoPlayer.playWhenReady = false
            }
            onDispose {
                exoPlayer.release()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(PureBlack)
        ) {
            // Video Player
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        player = exoPlayer
                        useController = false
                        resizeMode = androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            // Right side Action Buttons (Like, Comments, Share)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 80.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Like Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {
                            isLiked = !isLiked
                            likeCount += if (isLiked) 1 else -1
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Me gusta",
                            tint = if (isLiked) JhTubeRed else PureWhite,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "$likeCount", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Comments Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.ModeComment,
                            contentDescription = "Comentarios",
                            tint = PureWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "342", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Share Button
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(
                        onClick = {},
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = PureWhite,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Compartir", color = PureWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Bottom Metadata Overlay (Author, Title, Sound)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, end = 80.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = JhTubeRed,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = shortItem.author.take(1).uppercase(),
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "@${shortItem.author.lowercase().replace(" ", "")}",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = shortItem.title,
                    color = PureWhite,
                    fontSize = 14.sp,
                    maxLines = 2
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = PureWhite,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Sonido original - ${shortItem.author}",
                        color = PureWhite,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
