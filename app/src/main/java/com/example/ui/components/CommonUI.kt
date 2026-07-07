package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarHalf
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.UserRole
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.SaffronPrimary

@Composable
fun GlassyCard(
    modifier: Modifier = Modifier,
    borderColor: Color = Color.White.copy(alpha = 0.15f),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(borderColor, Color.Transparent)
                ),
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun PremiumButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "",
    containerColor: Color = SaffronPrimary,
    contentColor: Color = Color.White
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(50.dp)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun SaffronBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(SaffronPrimary, Color(0xFFFF9E40))
                ),
                shape = RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun StarRatingBar(
    rating: Float,
    reviewCount: Int,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val filledStars = rating.toInt()
        val hasHalf = (rating - filledStars) >= 0.5f
        
        for (i in 1..5) {
            when {
                i <= filledStars -> {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Star",
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
                i == filledStars + 1 && hasHalf -> {
                    Icon(
                        imageVector = Icons.Outlined.StarHalf,
                        contentDescription = "Half Star",
                        tint = GoldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Outlined.StarOutline,
                        contentDescription = "Empty Star",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$rating ($reviewCount)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun CategorySelectorRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory
            val containerColor = if (isSelected) SaffronPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface

            Box(
                modifier = Modifier
                    .background(containerColor, RoundedCornerShape(12.dp))
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category,
                    color = contentColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun LanguageToggler(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), CircleShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        AppLanguage.values().forEach { lang ->
            val isSelected = lang == currentLanguage
            val bgBrush = if (isSelected) {
                Brush.linearGradient(colors = listOf(SaffronPrimary, Color(0xFFFF9E40)))
            } else {
                Brush.linearGradient(colors = listOf(Color.Transparent, Color.Transparent))
            }
            val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)

            Box(
                modifier = Modifier
                    .drawBehind {
                        drawCircle(
                            brush = bgBrush,
                            radius = size.minDimension / 2
                        )
                    }
                    .clip(CircleShape)
                    .clickable { onLanguageSelected(lang) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (lang) {
                        AppLanguage.ENGLISH -> "EN"
                        AppLanguage.MARATHI -> "मरा"
                        AppLanguage.HINDI -> "हिं"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}

@Composable
fun RoleToggleSelector(
    activeRole: UserRole,
    onRoleSelected: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), CircleShape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        UserRole.values().forEach { role ->
            val isSelected = role == activeRole
            val bg = if (isSelected) SaffronPrimary else Color.Transparent
            val textColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)

            Box(
                modifier = Modifier
                    .background(bg, CircleShape)
                    .clip(CircleShape)
                    .clickable { onRoleSelected(role) }
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (role) {
                        UserRole.CUSTOMER -> "Customer"
                        UserRole.VENDOR -> "Vendor Panel"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }
    }
}
